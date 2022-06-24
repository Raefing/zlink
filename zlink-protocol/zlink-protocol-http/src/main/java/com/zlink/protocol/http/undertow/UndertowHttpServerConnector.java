package com.zlink.protocol.http.undertow;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.AbstractProtocolServer;
import com.zlink.protocol.base.data.HttpData;
import com.zlink.protocol.base.ProtocolUtil;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.exception.ProtocolRuntimeException;
import com.zlink.protocol.http.HTTPProtocolConfig;
import com.zlink.protocol.http.HttpPathMatcher;
import com.zlink.protocol.http.SSLContextFactory;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mortbay.jetty.HttpStatus;
import org.xnio.Options;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UndertowHttpServerConnector extends AbstractProtocolServer<HttpData> {

    private HTTPProtocolConfig protocolConfig;
    /**
     * netty 下水位 恢复到下水位下 writable is true
     */
    private int nettyBufferLowWatermark = 32 * 1024;
    /**
     * netty 上水位 超过上水位 writable is false
     */
    private int nettyBufferHighWatermark = 64 * 1024;
    //服务器端口
    private int port;
    private int ioThread;
    private int workThread;
    private int readTimeout = 60_000;
    private String localCharset = "UTF-8";
    private String remoteCharset = "UTF-8";
    private Undertow webServer;
    private String pathPattern;
    private String detectPath;
    private SSLContextFactory contextFactory;

    @Override
    public void initServer(IProtocolConfig config) {
        this.protocolConfig = (HTTPProtocolConfig) config;
        this.ioThread = Math.max(Runtime.getRuntime().availableProcessors(), protocolConfig.getIoThread());
        this.workThread = ioThread * 8;
        if (protocolConfig.getWorkThread() > 0) {
            this.workThread = protocolConfig.getWorkThread();
        }
        String uriStr = protocolConfig.getUrl();
        if (StringUtils.isBlank(uriStr)) {
            throw new ProtocolRuntimeException("协议[" + getId() + "]的URI属性为空");
        }
        URI uri = URI.create(uriStr);
        this.detectPath = protocolConfig.getAttachedParam("detectPath", String.class, "/detect");
        this.pathPattern = uri.getPath();
        this.port = uri.getPort();
        this.remoteCharset = protocolConfig.getEncoding();
        this.readTimeout = protocolConfig.getReadTimeout();
        this.contextFactory = new SSLContextFactory(protocolConfig);
        Undertow.Builder builder = Undertow.builder()
                .setIoThreads(ioThread)
                .setWorkerThreads(workThread)
                .setSocketOption(Options.READ_TIMEOUT, readTimeout)
                .setSocketOption(Options.CONNECTION_HIGH_WATER, nettyBufferHighWatermark)
                .setSocketOption(Options.CONNECTION_LOW_WATER, nettyBufferLowWatermark);
        SSLContext context = contextFactory.getSslContext();
        if (context != null) {
            builder.addHttpsListener(port, uri.getHost(), context);
        } else {
            builder.addHttpListener(port, uri.getHost());
        }
        this.webServer = builder
                .setHandler(httpServerExchange -> {
                    String requestPath = httpServerExchange.getRequestPath();
                    if (HttpPathMatcher.isMatch(new String[]{pathPattern, detectPath}, requestPath)) {
                        httpServerExchange.dispatch(new InnerHttpHandler());
                    } else {
                        httpServerExchange.setStatusCode(HttpStatus.ORDINAL_404_Not_Found).endExchange();
                    }
                })
                .build();
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

    @Override
    public void startServer() throws ProtocolException {
        log.info("启动协议[{}],ContentRootPath[{}]", getId(), protocolConfig.getUrl());
        webServer.start();
    }

    @Override
    public void stopServer() throws ProtocolException {
        webServer.stop();
    }

    class InnerHttpHandler implements HttpHandler {
        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            HeaderMap headerMap = httpServerExchange.getRequestHeaders();
            HttpString method = httpServerExchange.getRequestMethod();
            Map<String, Deque<String>> params = httpServerExchange.getQueryParameters();
            HttpData httpData = HttpData.builder()
                    .path(httpServerExchange.getRequestPath())
                    .query(httpServerExchange.getQueryString())
                    .build();
            Map<String, Object> param = new HashMap<>();
            params.forEach((key, v) -> {
                param.put(key, v.getFirst());
            });
            httpData.params(param);
            httpData.method(method.toString().toUpperCase());
            httpData.headers(headersMap(headerMap));
            if (method.equals(HttpString.tryFromString("POST")) || method.equals(HttpString.tryFromString("PUT"))) {
                httpServerExchange.getRequestReceiver().receiveFullBytes((callBack, bytes) -> {
                    httpData.data(ProtocolUtil.convertCharset(bytes, remoteCharset, localCharset));
                    HttpData ret = onMessage(httpData);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(ProtocolUtil.convertCharset(ret.data(), localCharset, remoteCharset));
                    headers(httpServerExchange.getResponseHeaders(), ret.headers());
                    callBack.getResponseSender().send(byteBuffer);
                });
            } else if (method.equals(HttpString.tryFromString("GET")) || method.equals(HttpString.tryFromString("DELETE"))) {
                HttpData ret = onMessage(httpData);
                ByteBuffer byteBuffer = ByteBuffer.wrap(ProtocolUtil.convertCharset(ret.data(), localCharset, remoteCharset));
                headers(httpServerExchange.getResponseHeaders(), ret.headers());
                httpServerExchange.getResponseSender().send(byteBuffer);
            } else {
                headers(httpServerExchange.getResponseHeaders(), httpData.headers());
                httpServerExchange.setStatusCode(HttpStatus.ORDINAL_400_Bad_Request);
                throw new ProtocolRuntimeException("this protocol can't support '" + method.toString() + "' method");
            }
        }

        private Map<String, String> headersMap(HeaderMap headerMap) {
            Map<String, String> map = new HashMap<>();
            headerMap.getHeaderNames().forEach(httpString -> {
                map.put(httpString.toString(), headerMap.getFirst(httpString));
            });
            return map;
        }

        private void headers(HeaderMap headerMap, Map<String, String> map) {
            map.forEach((k, v) -> headerMap.put(HttpString.tryFromString(k), v));
        }
    }
}
