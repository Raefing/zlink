package com.zlink.protocol.http.netty;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.AbstractProtocolClient;
import com.zlink.protocol.base.data.HttpData;
import com.zlink.protocol.base.MessageSyncer;
import com.zlink.protocol.base.ProtocolUtil;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.exception.ProtocolRuntimeException;
import com.zlink.protocol.http.HTTPProtocolConfig;
import com.zlink.protocol.http.SSLContextFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyHttpClientConnector extends AbstractProtocolClient<HttpData, URL> {


    private static final String NETTY_LOG_NAME = "logName";
    private static final String NETTY_LOG_LEVEL = "logLevel";

    private HTTPProtocolConfig protocolConfig;
    /**
     * netty 下水位 恢复到下水位下 writable is true
     */
    private int nettyBufferLowWatermark = 32 * 1024;
    /**
     * netty 上水位 超过上水位 writable is false
     */
    private int nettyBufferHighWatermark = 64 * 1024;
    private String mediaType;
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private int connectTimeout = 60_000;
    private int readTimeout = 60_000;
    private int ioThread;
    private int workThread;
    private String nettyLogName = NettyHttpClientConnector.class.getName();
    private String nettyLogLevel = "debug";
    private String localCharset = "UTF-8";
    private String connection = "keep-alive";
    private MessageSyncer<byte[]> syncer = MessageSyncer.getInstance();
    private SSLContextFactory contextFactory;
    @Override
    public void initClient(IProtocolConfig config) {
        this.protocolConfig = (HTTPProtocolConfig) config;
        this.ioThread = Math.max(Runtime.getRuntime().availableProcessors(), protocolConfig.getIoThread());
        this.workThread = ioThread * 8;
        if (protocolConfig.getWorkThread() > 0) {
            this.workThread = protocolConfig.getWorkThread();
        }
        this.remoteCharset = protocolConfig.getEncoding();
        this.connectTimeout = protocolConfig.getConnectTimeout();
        this.readTimeout = protocolConfig.getReadTimeout();
        this.mediaType = protocolConfig.getContentType();
        this.connection = protocolConfig.getAttachedParam("connection", String.class, "keep-alive");
        this.nettyLogName = protocolConfig.getAttachedParam(NETTY_LOG_NAME, String.class, this.nettyLogName);
        this.nettyLogName = protocolConfig.getAttachedParam(NETTY_LOG_LEVEL, String.class, this.nettyLogName);
        this.contextFactory = new SSLContextFactory(protocolConfig);
        workerGroup = new NioEventLoopGroup(workThread);
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
                SslHandler sslHandler = contextFactory.getSslHandler();
                if(sslHandler != null){
                    ch.pipeline().addLast(sslHandler);
                }
                ch.pipeline().addLast(new LoggingHandler(nettyLogName, LogLevel.valueOf(nettyLogLevel.toUpperCase())));
                ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, readTimeout, TimeUnit.MILLISECONDS));
                ch.pipeline().addLast(new HttpResponseDecoder());
                ch.pipeline().addLast(new HttpRequestEncoder());
                ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 1024, true));
                ch.pipeline().addLast(new InnerNettyHttpHandler());
            }
        });
    }

    @Override
    public void startClient() {

    }

    @Override
    public void stopClient() {

    }

    @Override
    public void reConnect(URL target) throws ProtocolException {

    }

    @Override
    public HttpData send(URL sub_uri, HttpData data) {
        String path = null;
        String method = data.method();
        try {
            URL sendUrl = new URL(sub_uri.getProtocol(), sub_uri.getHost(), sub_uri.getPort(), path == null ? sub_uri.getFile() : path);
            return send(sendUrl, method, data.data(), data.headers());
        } catch (MalformedURLException e) {
            throw new ProtocolRuntimeException("发送消息时发生异常", e);
        }
    }

    @Override
    public int msgSize(HttpData msg) {
        if (msg.data() != null) {
            return msg.data().length;
        }
        return 0;
    }

    private HttpData send(URL url, String method, byte[] bytes, Map<String, String> headers) {
        HttpData retMsg = HttpData.builder().build();
        ChannelFuture connect = null;
        try {
            FullHttpRequest request = null;
            if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
                byte[] requestBytes = ProtocolUtil.convertCharset(bytes, localCharset, remoteCharset);
                request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method), url.getFile(), Unpooled.wrappedBuffer(requestBytes));
                request.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
            } else if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("DELETE")) {
                request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method), url.getFile());
                request.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
            }
            if (request != null) {
                for (String key : headers.keySet()) {
                    String value = headers.get(key);
                    request.headers().set(key, value);
                }
                request.headers().set(HttpHeaderNames.CONTENT_TYPE, mediaType);
                request.headers().set(HttpHeaderNames.HOST, url.toString());
                request.headers().set(HttpHeaderNames.USER_AGENT, "netty-http-client");
                request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, localCharset);
                request.headers().set(HttpHeaderNames.CONNECTION, connection);
                connect = bootstrap.connect(url.getHost(), url.getPort()).sync();
                Channel channel = connect.channel();
                channel.writeAndFlush(request).sync();
                retMsg.data(syncer.get(channel.id().asLongText(), readTimeout));
            }
        } catch (Exception e) {
            throw new ProtocolRuntimeException("发送消息时发生异常", e);
        } finally {
            try {
                if (connect != null) {
                    connect.channel().closeFuture().sync();
                }
            } catch (InterruptedException e) {
            }
        }
        return retMsg;
    }

    @Override
    public List<URL> initProtocolTarget() {
        String urlStr = protocolConfig.getUrl();
        if (StringUtils.isBlank(urlStr)) {
            throw new ProtocolRuntimeException("协议[" + getId() + "]的URI属性为空");
        }
        return ProtocolUtil.createUrlList(urlStr);
    }

    class InnerNettyHttpHandler extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            boolean close = true;
            try {
                if (msg instanceof FullHttpResponse) {
                    FullHttpResponse response = (FullHttpResponse) msg;
                    String connectionCtl = response.headers().get(HttpHeaderNames.CONNECTION);
                    if (HttpHeaderValues.KEEP_ALIVE.contentEqualsIgnoreCase(connectionCtl)) {
                        close = false;
                    } else {
                        close = true;
                    }
                    if (response.status().code() == HttpResponseStatus.OK.code()) {
                        ByteBuf buf = response.content();
                        int len = buf.readableBytes();
                        byte[] bodyData = new byte[len];
                        buf.readBytes(bodyData);
                        syncer.removeFlag(ctx.channel().id().asLongText(), ProtocolUtil.convertCharset(bodyData, remoteCharset, localCharset));
                    } else {
                        syncer.removeFlag(ctx.channel().id().asLongText(), null);
                        String errMsg = "Return status code is " + response.status().code();
                        throw new IOException(errMsg + ", HTTP read response failed");
                    }
                } else {
                    syncer.removeFlag(ctx.channel().id().asLongText(), null);
                }
            } finally {
                if (close) {
                    ctx.close();
                }
            }
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            syncer.markFlag(ctx.channel().id().asLongText());
            ctx.write(msg, promise);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                if (IdleState.READER_IDLE == idleStateEvent.state() || IdleState.ALL_IDLE == idleStateEvent.state()) {
                    log.error("读取连接[{}]返回数据超时,关闭连接", ctx.channel().remoteAddress());
                    ctx.close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("连接[{}]发生异常,关闭连接", ctx.channel().remoteAddress(), cause);
            ctx.close();
        }
    }
}
