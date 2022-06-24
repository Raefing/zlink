package com.zlink.protocol.http;


import com.zlink.protocol.exception.ProtocolRuntimeException;
import io.netty.handler.ssl.SslHandler;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Base64;

public class SSLContextFactory {
    private static final String[] PROTOCOLS = {"TLSv1", "TLSv1.1", "TLSv1.2", "SSLv3"};

    private String protocol;
    private boolean isServer;
    private boolean isDuplexAuth;
    @Getter
    private SSLContext sslContext;
    @Getter
    private KeyManagerFactory keyManagerFactory;
    @Getter
    private TrustManagerFactory trustManagerFactory;

    public SSLContextFactory(HTTPProtocolConfig sslConfig) {
        this.protocol = sslConfig.getProtocol();
        this.isServer = sslConfig.isServer();
        this.isDuplexAuth = sslConfig.isDuplexAuth();
        this.keyManagerFactory = loadKeyManagerFactory(sslConfig.getKeyStorePath(), sslConfig.getKeyStoreCheckPasswd(), sslConfig.getKeyStorePasswd());
        this.trustManagerFactory = loadTrustManagerFactory(sslConfig.getCerStorePath(), sslConfig.getCerStorePasswd());
        if (keyManagerFactory != null || trustManagerFactory != null) {
            try {
                sslContext = SSLContext.getInstance(protocol);
                sslContext.init(keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                        trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null,
                        new SecureRandom());
            } catch (Exception e) {
                throw new ProtocolRuntimeException("初始化协议安全套接字失败", e);
            }
        }
    }

    public SSLSocketFactory sslSocketFactory() {
        return sslContext == null ? null : sslContext.getSocketFactory();
    }

    public SslHandler getSslHandler() {
        if (sslContext != null) {
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setEnabledProtocols(PROTOCOLS);
            sslEngine.setUseClientMode(!isServer);
            sslEngine.setNeedClientAuth(isDuplexAuth);
            return new SslHandler(sslEngine);
        } else {
            return null;
        }
    }

    private KeyManagerFactory loadKeyManagerFactory(String storePath, String storeCheckPwd, String storePasswd) {
        KeyManagerFactory keyManagerFactory = null;
        InputStream in = null;
        try {
            if (storePath != null) {
                KeyStore ks = KeyStore.getInstance("JKS");
                in = new FileInputStream(loadFile(storePath));
                ks.load(in, getPassword(storeCheckPwd));
                keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                keyManagerFactory.init(ks, getPassword(storePasswd));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {

            }
        }
        return keyManagerFactory;
    }

    private TrustManagerFactory loadTrustManagerFactory(String caPath, String cerPasswd) {
        TrustManagerFactory trustManagerFactory = null;
        InputStream in = null;
        try {
            if (caPath != null) {
                KeyStore tks = KeyStore.getInstance("JKS");
                in = new FileInputStream(loadFile(caPath));
                tks.load(in, getPassword(cerPasswd));
                trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
                trustManagerFactory.init(tks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {

            }
        }
        return trustManagerFactory;
    }

    private char[] getPassword(String password) {
        if (StringUtils.isNotBlank(password)) {
            try {
                byte[] pwd = Base64.getDecoder().decode(password);
                return new String(pwd).toCharArray();
            } catch (Exception e) {
                return password.toCharArray();
            }
        } else {
            return null;
        }
    }

    private File loadFile(String path) throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = resourcePatternResolver.getResource(path);
        return resource.getFile();
    }
}
