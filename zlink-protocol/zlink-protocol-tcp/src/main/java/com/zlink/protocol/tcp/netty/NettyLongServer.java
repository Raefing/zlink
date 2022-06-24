package com.zlink.protocol.tcp.netty;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.AbstractProtocolServer;
import com.zlink.protocol.base.ProtocolConstants;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.tcp.TCPProtocolConfig;
import com.zlink.protocol.tcp.netty.codec.NettyMessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyLongServer extends AbstractProtocolServer<byte[]> {

    private static final String NETTY_LOG_NAME = "logName";
    private static final String NETTY_LOG_LEVEL = "logLevel";

    private TCPProtocolConfig config;
    /**
     * netty 下水位 恢复到下水位下 writable is true
     */
    private int nettyBufferLowWatermark = 32 * 1024;
    /**
     * netty 上水位 超过上水位 writable is false
     */
    private int nettyBufferHighWatermark = 64 * 1024;
    //服务器端口
    private String host;
    private int port;
    private int ioThread;
    private int workerThread;
    private int connectTimeout = 60_000;
    private int readTimeout = 60_000;
    private String nettyLogName = NettyShortServer.class.getName();
    private String nettyLogLevel = "debug";
    private String readPolicy;
    private String writePolicy;
    private String localCharset = "UTF-8";
    private String remoteCharset = "UTF-8";
    private InnerServer innerServer;

    @Override
    public void initServer(IProtocolConfig c) {
        this.config = (TCPProtocolConfig) c;
        this.readPolicy = config.getPolicy();
        Asserts.check(StringUtils.isNotBlank(this.readPolicy)
                        && !this.readPolicy.equalsIgnoreCase(ProtocolConstants.DEFAULT_POLICY),
                "长连接读写策略应为确定的已知策略");
        this.writePolicy = config.getPolicy();
        this.remoteCharset = config.getEncoding();
        this.connectTimeout = config.getConnectTimeout();
        this.readTimeout = config.getReadTimeout();
        this.ioThread = Math.max(Runtime.getRuntime().availableProcessors(), config.getIoThread());
        this.workerThread = ioThread * 8;
        if (config.getWorkThread() > 0) {
            this.workerThread = config.getWorkThread();
        }
        this.nettyLogName = config.getAttachedParam(NETTY_LOG_NAME, String.class, this.nettyLogName);
        this.nettyLogName = config.getAttachedParam(NETTY_LOG_LEVEL, String.class, this.nettyLogName);
        this.host = config.getBindHost();
        this.port = config.getBindPort();
        innerServer = new InnerServer(host, port);
        try {
            innerServer.initProtocol();
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startServer() throws ProtocolException {
        innerServer.startProtocol();
    }

    @Override
    public void stopServer() throws ProtocolException {
        innerServer.stopProtocol();
    }

    public int msgSize(byte[] msg) {
        return msg.length;
    }

    @Override
    public String convertDet(byte[] message) {
        return new String(message);
    }

    @Override
    public byte[] convertDetRet(String message) {
        return message.getBytes();
    }

    class InnerServer {
        private EventLoopGroup boss;
        private EventLoopGroup worker;
        private ServerBootstrap strap;
        private String host;
        private int port;

        InnerServer(String host, int port) {
            this.host = host;
            this.port = port;
        }

        void initProtocol() throws ProtocolException {
            this.boss = new NioEventLoopGroup();
            this.worker = new NioEventLoopGroup(workerThread);
            this.strap = new ServerBootstrap();
            this.strap.group(boss, worker);
            this.strap.channel(NioServerSocketChannel.class);
            this.strap.option(ChannelOption.SO_BACKLOG, 1024 * 1024);
            this.strap.option(ChannelOption.SO_REUSEADDR, true);
            //this.strap.option(ChannelOption.SO_KEEPALIVE, true);
            this.strap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(nettyBufferLowWatermark, nettyBufferHighWatermark));
            this.strap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
            this.strap.childOption(ChannelOption.TCP_NODELAY, true);
            this.strap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(nettyLogName, LogLevel.valueOf(nettyLogLevel.toUpperCase())));
                    ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast(new NettyMessageCodec(readPolicy, writePolicy, localCharset, remoteCharset));
                    ch.pipeline().addLast(new InternalNettyInboundMessageHandler());
                }
            });
        }

        public void startProtocol() throws ProtocolException {
            // 绑定端口，同步等待成功
            ChannelFuture future;
            try {
                if (StringUtils.isBlank(host)){
                    host = "0.0.0.0";
                }
                future = this.strap.bind(host, port).sync();
                if (future.isSuccess()) {
                    log.info("启动协议[{}],监听端口[{}]", getId(), port);
                } else {
                    log.error("启动协议[{}]失败", getId());
                }
            } catch (Exception e) {
                log.error("启动协议[{}]失败", getId(), e);
                throw new ProtocolException("启动协议[" + getId() + "]失败", e);
            }
        }

        public void stopProtocol() throws ProtocolException {
            // 优雅地退出，释放线程池资源
            this.boss.shutdownGracefully();
            this.worker.shutdownGracefully();
        }


        class InternalNettyInboundMessageHandler extends ChannelInboundHandlerAdapter {

            private int idleTimes = 0;

            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                collector.active();
            }

            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                collector.inactive();
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof byte[]) {
                    byte[] msgBytes = (byte[]) msg;
                    byte[] retMsg = onMessage(msgBytes);
                    ctx.writeAndFlush(retMsg);
                }
            }

            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                if (evt instanceof IdleStateEvent) {
                    IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                    if (IdleState.READER_IDLE == idleStateEvent.state()) {
                        if (StringUtils.isNotBlank(detect) && StringUtils.isNotBlank(detectRet)) {
                            if (idleTimes <= 2) {
                                idleTimes++;
                            } else {
                                ctx.close();
                            }
                        }
                    }
                }
            }
        }
    }
}
