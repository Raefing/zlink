package com.zlink.protocol.http.okhttp;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.AbstractProtocolClient;
import com.zlink.protocol.base.ProtocolUtil;
import com.zlink.protocol.base.data.HttpData;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.exception.ProtocolRuntimeException;
import com.zlink.protocol.http.HTTPProtocolConfig;
import com.zlink.protocol.http.SSLContextFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OKHttpClientConnector extends AbstractProtocolClient<HttpData, URL> {

    private OkHttpClient okHttpClient;
    private HTTPProtocolConfig config;
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private String localCharset = "UTF-8";
    private String remoteCharset = "UTF-8";
    private String mediaType;
    private String connection = "keep-alive";
    private SSLContextFactory sslContextFactory;

    @Override
    public List<URL> initProtocolTarget() {
        return ProtocolUtil.createUrlList(config.getUrl());
    }

    @Override
    public void initClient(IProtocolConfig c) {
        this.config = (HTTPProtocolConfig) c;
        this.connectTimeout = config.getConnectTimeout();
        this.readTimeout = config.getReadTimeout();
        this.remoteCharset = config.getEncoding();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        if (StringUtils.isNotBlank(config.getKeyStorePath()) || StringUtils.isNotBlank(config.getCerStorePath())) {
            this.sslContextFactory = new SSLContextFactory(config);
            SSLSocketFactory sslSocketFactory = sslContextFactory.sslSocketFactory();
            X509TrustManager trustManager = null;
            TrustManagerFactory trustManagerFactory = sslContextFactory.getTrustManagerFactory();
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers != null && trustManagers[0] instanceof X509TrustManager) {
                trustManager = (X509TrustManager) trustManagers[0];
            }
            if (sslSocketFactory != null || trustManager != null) {
                builder.sslSocketFactory(sslSocketFactory, trustManager);
            }
            builder.hostnameVerifier((hostname, session) -> true);
        }
        okHttpClient = builder.build();
        this.mediaType = config.getContentType();
        this.connection = config.getAttachedParam("connection", String.class);
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
    public HttpData send(URL loadUri, HttpData data) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        HttpData response = HttpData.builder().build();
        Request request = null;
        String method = data.method();
        URL newUrl = ProtocolUtil.createURL(loadUri.getProtocol(), loadUri.getHost(), loadUri.getPort(), data.path(), data.query());
        Request.Builder builder = new Request.Builder().url(newUrl);
        data.headers().forEach((key, value) -> {
            builder.addHeader(key, value);
        });
        builder.addHeader("connection", connection);
        builder.addHeader("content-type", mediaType);
        builder.addHeader("host", loadUri.getHost() + ":" + loadUri.getPort());
        builder.addHeader("user-agent", "okhttp3 client");
        builder.addHeader("accept-encoding", localCharset);
        if (method.equalsIgnoreCase("POST")) {
            request = builder
                    .post(RequestBody.create(MediaType.parse(mediaType), ProtocolUtil.convertCharset(data.data(), localCharset, remoteCharset)))
                    .build();
        } else if (method.equalsIgnoreCase("PUT")) {
            request = builder
                    .post(RequestBody.create(MediaType.parse(mediaType), ProtocolUtil.convertCharset(data.data(), localCharset, remoteCharset)))
                    .build();
        } else if (method.equalsIgnoreCase("GET")) {
            request = builder
                    .get()
                    .build();
        } else if (method.equalsIgnoreCase("DELETE")) {
            request = builder
                    .delete()
                    .build();
        }
        if (request != null) {
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    countDownLatch.countDown();
                }

                @Override
                public void onResponse(Call call, Response res) throws IOException {
                    try {
                        if (res.code() == 200) {
                            response.data(res.body().bytes());
                        } else {
                            String errMsg = "Return status code is " + res.code();
                            throw new IOException(errMsg + ", HTTP read response failed");
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
            try {
                if (countDownLatch.await(readTimeout, TimeUnit.MILLISECONDS)) {
                    return response;
                } else {
                    log.error("等待返回超时");
                    throw new ProtocolRuntimeException("http协议返回超时");
                }
            } catch (InterruptedException e) {
                log.debug("等待返回发生异常", e);
                throw new ProtocolRuntimeException("等待返回发生异常", e);
            }
        } else {
            log.error("不支持的协议方法[{}]", method);
            throw new ProtocolRuntimeException("不支持的协议方法[" + method + "]");
        }
    }

    @Override
    public int msgSize(HttpData msg) {
        if (msg.data() != null) {
            return msg.data().length;
        }
        return 0;
    }
}
