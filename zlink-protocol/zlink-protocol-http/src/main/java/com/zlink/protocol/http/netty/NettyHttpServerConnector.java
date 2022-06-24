package com.zlink.protocol.http.netty;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.AbstractProtocolServer;
import com.zlink.protocol.base.data.HttpData;
import com.zlink.protocol.base.ProtocolUtil;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.exception.ProtocolRuntimeException;
import com.zlink.protocol.http.HTTPProtocolConfig;
import com.zlink.protocol.http.HttpPathMatcher;
import com.zlink.protocol.http.SSLContextFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.*;


@Slf4j
public class NettyHttpServerConnector extends AbstractProtocolServer<HttpData> {
    private static final String NETTY_LOG_NAME = "logName";
    private static final String NETTY_LOG_LEVEL = "logLevel";
    /**
     * netty 下水位 恢复到下水位下 writable is true
     */
    private int nettyBufferLowWatermark = 32 * 1024;
    /**
     * netty 上水位 超过上水位 writable is false
     */
    private int nettyBufferHighWatermark = 64 * 1024;
    private ServerBootstrap server;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int ioThread;
    private int workThread;
    private int connectTimeout;
    private int readTimeout;
    private String nettyLogName = NettyHttpServerConnector.class.getName();
    private String nettyLogLevel = "error";
    private String localCharset = "UTF-8";
    private String remoteCharset = "UTF-8";
    private HTTPProtocolConfig protocolConfig;
    private String host;
    private int port;
    private String path;
    private String detectPath;
    private SSLContextFactory contextFactory;
    @Override
    public void startServer() throws ProtocolException {
        try {
            log.info("启动协议[{}],ContentRootPath[{}]", getId(), protocolConfig.getUrl());
            server.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopServer() throws ProtocolException {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    @Override
    public void initServer(IProtocolConfig config) {
        this.protocolConfig = (HTTPProtocolConfig) config;
        this.ioThread = Math.max(Runtime.getRuntime().availableProcessors(), protocolConfig.getIoThread());
        this.workThread = ioThread * 8;
        if (protocolConfig.getWorkThread() > 0) {
            this.workThread = protocolConfig.getWorkThread();
        }
        String urlStr = protocolConfig.getUrl();
        if (StringUtils.isBlank(urlStr)) {
            throw new ProtocolRuntimeException("协议[" + getId() + "]的URI属性为空");
        }
        URL url = ProtocolUtil.createUrlList(urlStr).get(0);
        this.path = url.getPath();
        this.detectPath = config.getAttachedParam("detectPath", String.class, "/detect");
        this.remoteCharset = protocolConfig.getEncoding();
        this.connectTimeout = protocolConfig.getConnectTimeout();
        this.readTimeout = protocolConfig.getReadTimeout();
        this.nettyLogName = config.getAttachedParam(NETTY_LOG_NAME, String.class, this.nettyLogName);
        this.nettyLogName = config.getAttachedParam(NETTY_LOG_LEVEL, String.class, this.nettyLogName);
        this.host = url.getHost();
        if (StringUtils.isBlank(host)) {
            this.host = "0.0.0.0";
        }
        this.port = url.getPort();
        if (this.port <= 0) {
            this.port = 80;
        }
        bossGroup = new NioEventLoopGroup(ioThread);
        workerGroup = new NioEventLoopGroup(workThread);
        this.contextFactory = new SSLContextFactory(protocolConfig);
        server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(nettyBufferLowWatermark, nettyBufferHighWatermark))
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        SslHandler sslHandler = contextFactory.getSslHandler();
                        if(sslHandler != null){
                            ch.pipeline().addLast(sslHandler);
                        }
                        ch.pipeline().addLast(new LoggingHandler(nettyLogName, LogLevel.valueOf(nettyLogLevel.toUpperCase())));
                        ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, readTimeout, TimeUnit.MILLISECONDS));
                        ch.pipeline().addLast(new HttpResponseEncoder());
                        ch.pipeline().addLast(new HttpRequestDecoder());
                        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 1024, true));
                        ch.pipeline().addLast(new InnerNettyHttpHandler(getId(), localCharset, remoteCharset));
                    }
                });
    }

    @Override
    public int msgSize(HttpData msg) {
        if (msg.data() != null) {
            return msg.data().length;
        }
        return 0;
    }

    @Override
    public String convertDet(HttpData message) {
        if (message.data() != null) {
            return new String(message.data());
        } else {
            return null;
        }
    }

    @Override
    public HttpData convertDetRet(String message) {
        return HttpData.builder().data(message.getBytes()).build();
    }

    public class InnerNettyHttpHandler extends ChannelInboundHandlerAdapter {

        private String protocolId;
        private String localCharset;
        private String remoteCharset;

        public InnerNettyHttpHandler(String protocolId, String localCharset, String remoteCharset) {
            this.protocolId = protocolId;
            this.localCharset = localCharset;
            this.remoteCharset = remoteCharset;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            HttpHeaders headers = httpRequest.headers();
            HttpMethod method = httpRequest.method();//获取请求方法
            String uri = httpRequest.uri();
            HttpData httpData = HttpData.builder()
                    .method(method.name().toUpperCase())
                    .path(uri)
                    .query(uri)
                    .headers(headerMap(headers))
                    .build();
            boolean close = headers.contains(CONNECTION, HttpHeaderValues.CLOSE, true)
                    || httpRequest.protocolVersion().equals(HttpVersion.HTTP_1_0)
                    && !headers.contains(CONNECTION, HttpHeaderValues.KEEP_ALIVE, true);
            if (!(msg instanceof FullHttpRequest)) {
                byte[] retBytes = "未知请求!".getBytes();
                send(ctx, retBytes, HttpResponseStatus.BAD_REQUEST, close, httpData.headers());
                return;
            }
            try {
                if (HttpPathMatcher.isMatch(new String[]{path, detectPath}, uri)) {
                    Map<String, List<String>> params = getQueryParams(uri);
                    Map<String, Object> param = new HashMap<>();
                    params.forEach((key, value) -> {
                        param.put(key, value.get(0));
                    });
                    httpData.params(param);
                    if (HttpMethod.GET.equals(method) || HttpMethod.DELETE.equals(method)) {
                        //接受到的消息，做业务逻辑处理...
                        HttpData ret = onMessage(httpData);
                        send(ctx, ProtocolUtil.convertCharset(ret.data(), localCharset, remoteCharset), HttpResponseStatus.OK, close, ret.headers());
                        return;
                    } else if (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)) {
                        byte[] body = ProtocolUtil.convertCharset(getBody(httpRequest), remoteCharset, localCharset);     //获取POST报文
                        //接受到的消息，做业务逻辑处理...
                        httpData.data(body);
                        HttpData ret = onMessage(httpData);
                        send(ctx, ProtocolUtil.convertCharset(ret.data(), localCharset, remoteCharset), HttpResponseStatus.OK, close, ret.headers());
                        return;
                    } else {
                        byte[] retBytes = new byte[0];
                        send(ctx, retBytes, HttpResponseStatus.METHOD_NOT_ALLOWED, close, httpData.headers());
                        return;
                    }
                } else {
                    byte[] retBytes = new byte[0];
                    send(ctx, retBytes, HttpResponseStatus.NOT_FOUND, close, httpData.headers());
                    return;
                }
            } catch (ProtocolRuntimeException e) {
                log.error("处理请求失败!", e);
                byte[] retBytes = new byte[0];
                send(ctx, retBytes, HttpResponseStatus.INTERNAL_SERVER_ERROR, close, httpData.headers());
            } finally {
                //释放请求
                httpRequest.release();
            }
        }

        private byte[] getBody(FullHttpRequest request) {
            ByteBuf buf = request.content();
            int len = buf.readableBytes();
            byte[] body = new byte[len];
            buf.readBytes(body);
            return body;
        }

        private void send(ChannelHandlerContext ctx, byte[] context, HttpResponseStatus status, boolean close, Map<String, String> headers) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(context));
            response.headers().set(CONTENT_LENGTH, context.length);
            headers(response.headers(), headers);
            ChannelFuture future = ctx.channel().writeAndFlush(response);
            // Close the connection after the write operation is done if necessary.
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private void headers(HttpHeaders headers, Map<String, String> map) {
            map.forEach((key, v) -> headers.set(key, v));
        }

        private Map<String, String> headerMap(HttpHeaders headers) {
            Map<String, String> map = new HashMap<>();
            headers.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
            return map;
        }

        public Map<String, List<String>> getQueryParams(String url) {
            try {
                Map<String, List<String>> params = new HashMap<String, List<String>>();
                String[] urlParts = url.split("\\?");//这里分割uri成请求路径和请求参数两部分，注意这里的?不能直接作为分隔符，需要转义
                if (urlParts.length > 1) {
                    String query = urlParts[1];//获取到参数字符串
                    for (String param : query.split("&")) {
                        String[] pair = param.split("=");
                        String key = URLDecoder.decode(pair[0], "UTF-8");
                        String value = "";
                        if (pair.length > 1) {
                            value = URLDecoder.decode(pair[1], "UTF-8");
                        }

                        List<String> values = params.get(key);
                        if (values == null) {
                            values = new ArrayList<String>();
                            params.put(key, values);
                        }
                        values.add(value);
                    }
                }
                return params;
            } catch (UnsupportedEncodingException ex) {
                throw new AssertionError(ex);
            }
        }

        public String getPath(String url) {
            String[] urlParts = url.split("\\?");//这里分割uri成请求路径和请求参数两部分，注意这里的?不能直接作为分隔符，需要转义
            return urlParts[0];
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                if (IdleState.READER_IDLE == idleStateEvent.state() || IdleState.ALL_IDLE == idleStateEvent.state()) {
                    log.error("读取连接[{}]请求数据超时,关闭连接", ctx.channel().remoteAddress());
                    ctx.close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("Error:{}", cause);
            ctx.close();
        }
    }
}
