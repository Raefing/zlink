package com.zlink.protocol.base;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProtocolUtil {
    public static byte[] convertCharset(byte[] req, String sourceCharset, String targetCharset) {
        if (req != null) {
            if (!sourceCharset.equalsIgnoreCase(targetCharset)) {
                try {
                    String str = new String(req, sourceCharset);
                    return str.getBytes(targetCharset);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return req;
        } else {
            return new byte[0];
        }
    }

    public static List<URL> createUrlList(String str) {
        List<URL> urls = new ArrayList<>();
        if (str.indexOf(",") != -1) {
            try {
                URL url = new URL(str);
                String protocol = url.getProtocol();
                String[] hosts = url.getHost().split(",");
                int port = url.getPort();
                String path = url.getPath();
                String query = url.getQuery();
                for (String host : hosts) {
                    URL urlsub = createURL(protocol, host, port, path, query);
                    if (urlsub != null) {
                        urls.add(urlsub);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            URL url = null;
            try {
                url = new URL(str);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (url != null) {
                urls.add(url);
            }
        }
        return urls;
    }

    public static URL createURL(String protocol, String host, int port, String path, String query) {
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
            if (StringUtils.isNotBlank(query)) {
                stringBuffer.append("?").append(query);
            }
            URL url1 = new URL(stringBuffer.toString());
            return url1;
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
