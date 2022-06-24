package com.zlink.protocol.tcp.netty;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.AbstractProtocolClient;
import com.zlink.protocol.base.MessageSyncer;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyShortClient extends AbstractProtocolClient<byte[], String> {

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
    //服务器端口
    private int connectTimeout = 60_000;
    private int readTimeout = 60_000;
    private int writeTimeout = 60_000;
    private String nettyLogName = NettyShortClient.class.getName();
    private String nettyLogLevel = "debug";
    private String readPolicy = "UN_KNOW";
    private String writePolicy = "UN_KNOW";
    private String localCharset = "UTF-8";
    private MessageSyncer<byte[]> syncer = MessageSyncer.getInstance();
    private Map<String, InnerClient> innerClientMap = new ConcurrentHashMap<>();

    @Override
    public List<String> initProtocolTarget() {
        if (config.getTargets() == null || config.getTargets().size() <= 0) {
            return Arrays.asList(config.getBindHost() + ":" + config.getBindPort());
        }
        return config.getTargets();
    }

    @Override
    public void initClient(IProtocolConfig c) {
        this.config = (TCPProtocolConfig) c;
        this.readPolicy = config.getPolicy();
        this.writePolicy = config.getPolicy();
        this.remoteCharset = config.getEncoding();
        this.connectTimeout = config.getConnectTimeout();
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
    }

    @Override
    public byte[] send(String host, byte[] bytes) {
        if (innerClientMap.size() > 0) {
            if (innerClientMap.containsKey(host)) {
                return innerClientMap.get(host).send(bytes);
            } else {
                throw new ProtocolRuntimeException("未找到可用的协议目标");
            }
        } else {
            throw new ProtocolRuntimeException("未找到可用的协议目标");
        }
    }

    @Override
    public int msgSize(byte[] msg) {
        return msg.length;
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
    }

    @Override
    public void reConnect(String target) throws ProtocolException {

    }

    class InnerClient {
        private EventLoopGroup workerGroup;
        private Bootstrap bootstrap;
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
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(nettyBufferLowWatermark, nettyBufferHighWatermark));
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LoggingHandler(nettyLogName, LogLevel.valueOf(nettyLogLevel.toUpperCase())));
                    ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast(new NettyMessageCodec(readPolicy, writePolicy, localCharset, remoteCharset));
                    ch.pipeline().addLast(new InnerNettyOutboundMessageHandler());
                }
            });
        }

        public void startProtocol() throws ProtocolException {
        }

        public void stopProtocol() throws ProtocolException {
            workerGroup.shutdownGracefully();
        }

        public byte[] send(byte[] bytes) {
            ChannelFuture connect = null;
            try {
                connect = bootstrap.connect(ip, port).sync();
                Channel channel = connect.channel();
                String id = channel.id().asLongText();
                channel.writeAndFlush(bytes).sync();
                return syncer.get(id, readTimeout);
            } catch (Exception e) {
                throw new ProtocolRuntimeException("发送消息时发生异常", e);
            } finally {
                try {
                    connect.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        class InnerNettyOutboundMessageHandler extends ChannelDuplexHandler {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                byte[] retMsg = (byte[]) msg;
                syncer.removeFlag(ctx.channel().id().asLongText(), retMsg);
                ctx.close();
            }

            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                syncer.markFlag(ctx.channel().id().asLongText());
                ctx.write(msg, promise);
            }

            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                if (evt instanceof IdleStateEvent) {
                    IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                    if (IdleState.READER_IDLE == idleStateEvent.state()) {
                        log.error("读取连接[{}]返回数据超时,关闭连接", ctx.channel().remoteAddress());
                        ctx.close();
                        syncer.get(ctx.channel().id().asLongText(), 0);
                    }
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                log.error("连接[{}]发生异常,关闭连接", ctx.channel().remoteAddress(), cause);
                syncer.get(ctx.channel().id().asLongText(), 0);
                ctx.close();
            }
        }
    }
}
