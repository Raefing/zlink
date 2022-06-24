package com.zlink.protocol.base;

import com.zlink.protocol.api.ext.ClientDetector;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BaseClientDetector implements ClientDetector {

    private ScheduledExecutorService executorService;

    private boolean status = true;
    private DetectorConfig config;
    private HttpClient client = HttpClients.createDefault();

    public BaseClientDetector(DetectorConfig config) {
        this.config = config;
        this.init();
    }

    public void init() {
        this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            String name = "Detect-" + config.protocolType() + "/" + config.host() + ":" + config.port();
            Thread thread = new Thread(r, name);
            thread.setDaemon(config.daemon());
            return thread;
        });
    }

    @Override
    public void start() {
        executorService.scheduleWithFixedDelay(() -> {
            status = innerDetect();
        }, config.detectInterval(), config.detectInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean detect() {
        return status;
    }

    @Override
    public void stop() {
        executorService.shutdown();
        executorService = null;
    }

    private boolean innerDetect() {
        if (config.detectType() == DetectorConfig.DetectType.CONN) {
            return innerConnDetect();
        } else {
            if (config.protocolType() == ProtocolType.HTTP) {
                return innerHttpDetect();
            }
            if (config.protocolType() == ProtocolType.TCP) {
                return innerTcpDetect();
            }
        }
        return false;
    }

    private boolean innerHttpDetect() {
        boolean status = false;
        try {
            HttpUriRequest request = null;
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(config.detectInterval() / 2).setConnectTimeout(config.detectInterval() / 2).build();
            URI uri = createUri("http", config.host(), config.port(), config.path());
            if (config.method().equalsIgnoreCase("GET")) {
                HttpGet httpGet = new HttpGet();
                httpGet.setURI(uri);
                httpGet.setConfig(requestConfig);
                request = httpGet;
            }
            if (config.method().equalsIgnoreCase("POST")) {
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(uri);
                httpPost.setEntity(new StringEntity(config.detectMsg()));
                httpPost.setConfig(requestConfig);
                request = httpPost;
            }
            if (config.method().equalsIgnoreCase("PUT")) {
                HttpPut httpPut = new HttpPut();
                httpPut.setURI(uri);
                httpPut.setEntity(new StringEntity(config.detectMsg()));
                httpPut.setConfig(requestConfig);
                request = httpPut;
            }
            if (config.method().equalsIgnoreCase("DELETE")) {
                HttpDelete httpDelete = new HttpDelete();
                httpDelete.setURI(uri);
                httpDelete.setConfig(requestConfig);
                request = httpDelete;
            }
            if (request != null) {
                HttpResponse response = client.execute(request);
                if (null != response && response.getStatusLine().getStatusCode() == 200) {
                    String ret = EntityUtils.toString(response.getEntity(), config.encoding());
                    status = StringUtils.isNotBlank(ret) && ret.equals(config.detectMsg());
                } else {
                    status = false;
                }
            } else {
                status = false;
            }
        } catch (Exception e) {
            status = false;
        }
        return status;
    }

    private boolean innerTcpDetect() {
        boolean status = false;
        Socket socket = new Socket();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            socket.setSoTimeout(config.detectInterval() / 2);
            socket.setTcpNoDelay(true);
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(config.host(), config.port()), config.detectInterval() / 2);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            outputStream.write(config.detectMsg().getBytes("UTF-8"));
            outputStream.flush();
            byte[] bytes = new byte[config.detectRetMsg().length()];
            int l = 0;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            while ((l = inputStream.read(bytes)) != -1) {
                bout.write(bytes, 0, l);
                if (config.detectRetMsg().length() == l) {
                    break;
                }
            }
            String ret = new String(bout.toByteArray(), config.encoding());
            status = StringUtils.isNotBlank(ret) && ret.equals(config.detectMsg());
        } catch (Exception e) {
            status = false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                }
            }
            if (socket != null && socket.isConnected()) {
                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
            socket = null;
        }
        return status;
    }

    private boolean innerConnDetect() {
        boolean status = false;
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(config.host(), config.port()), config.detectInterval() / 2);
            status = true;
        } catch (Exception e) {
            status = false;
        } finally {
            if (socket != null && socket.isConnected()) {
                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
            socket = null;
        }
        return status;
    }

    private URI createUri(String protocol, String host, int port, String path) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            if (StringUtils.isNotBlank(protocol)) {
                stringBuffer.append(protocol).append("://");
            }
            if (StringUtils.isNotBlank(host)) {
                stringBuffer.append(host);
            } else {
                stringBuffer.append("localhost");
            }
            if (port > 0) {
                stringBuffer.append(":" + port);
            }
            if (StringUtils.isNotBlank(path)) {
                stringBuffer.append(path);
            } else {
                stringBuffer.append("/");
            }
            URI uri = new URI(stringBuffer.toString());
            return uri;
        } catch (Exception e) {
            return null;
        }
    }
}
