package com.zlink.protocol.tcp.netty;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.api.ext.IMessageHandler;
import com.zlink.protocol.base.AbstractProtocolClient;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.exception.ProtocolRuntimeException;
import com.zlink.protocol.tcp.TCPProtocolConfig;
import com.zlink.protocol.tcp.netty.codec.NettyMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
public class NettyAsyncLongClient extends AbstractProtocolClient<byte[], String> {

    private TCPProtocolConfig config;

    private static final String NETTY_LOG_NAME = "logName";
    private static final String NETTY_LOG_LEVEL = "logLevel";
    /*
     * netty 下水位 恢复到下水位下 writable is true
     */
    private int nettyBufferLowWatermark = 32 * 1024;
    /**
     * netty 上水位 超过上水位 writable is false
     */
    private int nettyBufferHighWatermark = 64 * 1024;
    private Map<String, InnerClient> innerClientMap = new ConcurrentHashMap<>();
    private int connectTimeout = 60_000;
    private int readTimeout = 60_000;
    private int writeTimeout = 60_000;
    private String nettyLogName = NettyAsyncLongClient.class.getName();
    private String nettyLogLevel = "debug";
    private String readPolicy = "UN_KNOW";
    private String writePolicy = "UN_KNOW";
    private String localCharset = "UTF-8";
    private String remoteCharset = "UTF-8";
    private ScheduledExecutorService executorService;

    @Override
    public List<String> initProtocolTarget() {
        return config.getTargets();
    }

    @Override
    public void initClient(IProtocolConfig c) {
        this.config = (TCPProtocolConfig) c;
        this.readPolicy = config.getPolicy();
        this.writePolicy = config.getPolicy();
        this.remoteCharset = config.getEncoding();
        this.connectTimeout = config.getConnectTimeout();
        this.readTimeout = config.getReadTimeout();
        this.nettyLogName = config.getAttachedParam(NETTY_LOG_NAME, String.class, this.nettyLogName);
        this.nettyLogName = config.getAttachedParam(NETTY_LOG_LEVEL, String.class, this.nettyLogName);
        initProtocolTarget().forEach(address -> {
            String host = "localhost";
            int port;
            if (address.indexOf(":") != -1) {
                String[] parts = address.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            } else {
                port = Integer.parseInt(address);
            }
            InnerClient client = new InnerClient(host, port);
            try {
                client.initProtocol();
                innerClientMap.put(address, client);
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        });
        this.executorService = Executors.newScheduledThreadPool(1, r -> {
            String name = "Reconnect-" + getId();
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void startClient() throws ProtocolException {
        innerClientMap.forEach((key, client) -> {
            try {
                client.startProtocol();
            } catch (ProtocolException e) {
                log.error("启动协议[{}]发生异常", getId(), e);
            }
        });
        executorService.scheduleAtFixedRate(() -> {
            innerClientMap.forEach((key, client) -> {
                loadbalancer.recordStatus(key, client.isActive());
                collector.setTargetStatus(key, client.isActive() ? 1 : 0);
                if (!client.isActive()) {
                    try {
                        reConnect(key);
                    } catch (ProtocolException e) {
                        //e.printStackTrace();
                    }
                }
            });
        }, connectTimeout, connectTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopClient() throws ProtocolException {
        innerClientMap.forEach((key, client) -> {
            try {
                client.stopProtocol();
            } catch (ProtocolException e) {
                log.error("停止协议[{}]发生异常", getId(), e);
            }
        });
        executorService.shutdown();
    }

    @Override
    public void reConnect(String target) throws ProtocolException {
        executorService.execute(() -> {
            InnerClient client = innerClientMap.get(target);
            try {
                client.stopProtocol();
                client.initProtocol();
                client.startProtocol();
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int msgSize(byte[] msg) {
        return msg.length;
    }

    @Override
    public byte[] send(String host, byte[] bytes) {
        if (innerClientMap.size() > 0) {
            if (innerClientMap.containsKey(host)) {
                InnerClient client = innerClientMap.get(host);
                if (client.isActive()) {
                    collector.send(msgSize(bytes));
                    client.send(bytes);
                } else {
                    throw new ProtocolRuntimeException("协议目标[" + host + "]不可用");
                }
            } else {
                throw new ProtocolRuntimeException("协议目标[" + host + "]不存在");
            }
        } else {
            throw new ProtocolRuntimeException("未找到可用的协议目标[" + host + "]");
        }
        return null;
    }

    @Override
    public void asyncSend(byte[] bytes) {
        String host = loadbalancer.loadBalance();
        send(host, bytes);
    }

    private Queue<byte[]> queue = new ArrayBlockingQueue(100);

    @Override
    public byte[] asyncRecv() {
        return queue.poll();
    }

    public
    class InnerClient {
        private EventLoopGroup workerGroup;
        private Bootstrap bootstrap;
        private Channel channel;
        private String ip;
        private int port;

        InnerClient(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public void initProtocol() throws ProtocolException {
            workerGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(nettyBufferLowWatermark, nettyBufferHighWatermark));
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LoggingHandler(nettyLogName, LogLevel.valueOf(nettyLogLevel.toUpperCase())));
                    ch.pipeline().addLast(new IdleStateHandler(0, detectInterval, 0, TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast(new NettyMessageCodec(readPolicy, writePolicy, localCharset, remoteCharset));
                    ch.pipeline().addLast(new InnerNettyOutboundMessageHandler());
                }
            });
        }

        public void startProtocol() throws ProtocolException {
            try {
                ChannelFuture connect = bootstrap.connect(ip, port).sync();
                channel = connect.channel();
            } catch (Exception e) {
                throw new ProtocolException("启动协议[" + getId() + "],目标[" + ip + ":" + port + "]失败", e);
            }
        }

        public void stopProtocol() throws ProtocolException {
            workerGroup.shutdownGracefully();
        }

        public void send(byte[] bytes) {
            try {
                channel.writeAndFlush(bytes);
            } catch (Exception e) {
                throw new ProtocolRuntimeException("发送消息时发生异常", e);
            }
        }

        public boolean isActive() {
            return channel != null && channel.isActive();
        }

        class InnerNettyOutboundMessageHandler extends ChannelDuplexHandler {

            private int idleTimes = 0;

            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                loadbalancer.recordStatus(ip + ":" + port, true);
            }

            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                loadbalancer.recordStatus(ip + ":" + port, false);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext var1, Throwable var2) throws Exception {
                loadbalancer.recordStatus(ip + ":" + port, false);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                byte[] retMsg = (byte[]) msg;
                idleTimes = 0;
                if (new String(retMsg, remoteCharset).equals(detectRet)) {
                    loadbalancer.recordStatus(ip + ":" + port, true);
                } else {
                    collector.recv(retMsg.length);
                    queue.offer(retMsg);
                }
            }

            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                if (evt instanceof IdleStateEvent) {
                    IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                    if (IdleState.WRITER_IDLE == idleStateEvent.state()) {
                        if (idleTimes > 3) {
                            ctx.close();
                            loadbalancer.recordStatus(ip + ":" + port, false);
                        } else if (StringUtils.isNotBlank(detect) && StringUtils.isNotBlank(detectRet)) {
                            idleTimes++;
                            try {
                                ctx.writeAndFlush(detect.getBytes(remoteCharset));
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        }
    }
}
