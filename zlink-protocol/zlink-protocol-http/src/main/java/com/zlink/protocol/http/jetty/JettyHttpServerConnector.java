package com.zlink.protocol.http.jetty;


import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.AbstractProtocolServer;
import com.zlink.protocol.base.data.HttpData;
import com.zlink.protocol.base.ProtocolUtil;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.exception.ProtocolRuntimeException;
import com.zlink.protocol.http.HTTPProtocolConfig;
import com.zlink.protocol.http.HttpPathMatcher;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mortbay.jetty.*;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;

@Slf4j
public class JettyHttpServerConnector extends AbstractProtocolServer<HttpData> {

    private Server server;
    private int ioThread;
    private int workThread;
    private String localCharset = "UTF-8";
    private String remoteCharset = "UTF-8";
    private String host;
    private int port;
    private String pattern;
    private String detectPath;
    private HTTPProtocolConfig protocolConfig;

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
        this.detectPath = protocolConfig.getAttachedParam("detectPath", String.class, "/detect");
        URI uri = URI.create(uriStr);
        this.pattern = uri.getPath();
        this.host = uri.getHost();
        this.port = uri.getPort();
        this.server = new Server();
        AbstractConnector connector = null;
        if (StringUtils.isNotBlank(protocolConfig.getKeyStorePath()) || StringUtils.isNotBlank(protocolConfig.getCerStorePath())) {
            SslSocketConnector sslSocketConnector = new SslSocketConnector();
            sslSocketConnector.setKeystore(protocolConfig.getKeyStorePath());
            sslSocketConnector.setPassword(protocolConfig.getKeyStoreCheckPasswd());
            sslSocketConnector.setKeyPassword(protocolConfig.getKeyStorePasswd());
            sslSocketConnector.setTruststore(protocolConfig.getCerStorePath());
            sslSocketConnector.setTrustPassword(protocolConfig.getCerStorePasswd());
            connector = sslSocketConnector;
        } else {
            connector = new SelectChannelConnector();
        }
        connector.setHost(host);
        connector.setPort(port);
        connector.setAcceptors(ioThread);
        connector.setMaxIdleTime(30000);
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMinThreads(ioThread);
        pool.setMaxThreads(workThread);
        pool.setMaxIdleTimeMs(300000);
        connector.setThreadPool(pool);
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(new DefaultServlet() {
            @Override
            public void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
                InputStream in = servletRequest.getInputStream();
                OutputStream out = servletResponse.getOutputStream();
                HttpData httpData = HttpData.builder()
                        .path(servletRequest.getPathInfo())
                        .query(servletRequest.getQueryString())
                        .method(servletRequest.getMethod().toUpperCase())
                        .headers(headerMap(servletRequest))
                        .build();
                Map<String, String[]> params = servletRequest.getParameterMap();
                Map<String, Object> param = new HashMap<>();
                params.forEach((key, v) -> {
                    param.put(key, v[0]);
                });
                httpData.params(param);
                if (HttpPathMatcher.isMatch(new String[]{pattern, detectPath}, httpData.path())) {
                    HttpData ret = onMessage(httpData);
                    convertHeader(servletResponse, ret.headers());
                    out.write(ProtocolUtil.convertCharset(ret.data(), localCharset, remoteCharset));
                } else {
                    convertHeader(servletResponse, httpData.headers());
                    servletResponse.setStatus(HttpStatus.ORDINAL_404_Not_Found);
                }
                if (needClose(servletRequest)) {
                    in.close();
                    out.close();
                }
            }

            @Override
            public void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
                InputStream in = servletRequest.getInputStream();
                OutputStream out = servletResponse.getOutputStream();
                String path = servletRequest.getPathInfo();
                HttpData httpData = HttpData.builder()
                        .path(path)
                        .query(servletRequest.getQueryString())
                        .method(servletRequest.getMethod().toUpperCase())
                        .headers(headerMap(servletRequest))
                        .build();
                Map<String, String[]> params = servletRequest.getParameterMap();
                Map<String, Object> param = new HashMap<>();
                params.forEach((key, v) -> {
                    param.put(key, v[0]);
                });
                httpData.params(param);
                if (HttpPathMatcher.isMatch(new String[]{pattern, detectPath}, path)) {
                    byte[] bts = new byte[1024];
                    int len = 0;
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    while ((len = in.read(bts)) > 0) {
                        bout.write(bts, 0, len);
                    }
                    byte[] requestData = bout.toByteArray();
                    byte[] bytes = ProtocolUtil.convertCharset(requestData, remoteCharset, localCharset);
                    httpData.data(bytes);
                    HttpData ret = onMessage(httpData);
                    convertHeader(servletResponse, ret.headers());
                    out.write(ProtocolUtil.convertCharset(ret.data(), localCharset, remoteCharset));
                } else {
                    convertHeader(servletResponse, httpData.headers());
                    servletResponse.setStatus(HttpStatus.ORDINAL_404_Not_Found);
                }
                if (needClose(servletRequest)) {
                    in.close();
                    out.close();
                }
            }
        }), "/*");
        server.setConnectors(new Connector[]{connector});
        server.setHandler(servletHandler);
    }

    @Override
    public int msgSize(HttpData msg) {
        if (msg.data() != null) {
            return msg.data().length;
        }
        return 0;
    }

    private boolean needClose(HttpServletRequest request) {
        boolean close = !request.getHeader(CONNECTION.toString()).equalsIgnoreCase(HttpHeaderValues.KEEP_ALIVE)
                || request.getProtocol().equals(HttpVersion.HTTP_1_0);
        return close;
    }

    private void convertHeader(HttpServletResponse res, Map<String, String> map) {
        map.forEach((key, value) -> res.setHeader(key, value));
    }

    private Map<String, String> headerMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> headsers = request.getHeaderNames();
        while (headsers.hasMoreElements()) {
            String name = headsers.nextElement();
            String value = request.getHeader(name);
            map.put(name, value);
        }
        return map;
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
        try {
            log.info("启动协议[{}],ContentRootPath[{}]", getId(), protocolConfig.getUrl());
            server.start();
        } catch (Exception e) {
            throw new ProtocolException("启动协议[" + getId() + "]发生异常", e);
        }
    }

    @Override
    public void stopServer() throws ProtocolException {
        try {
            server.stop();
        } catch (Exception e) {
            throw new ProtocolException("停止协议[" + getId() + "]发生异常", e);
        }
    }
}
