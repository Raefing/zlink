package com.zlink.protocol.http;


import com.zlink.protocol.api.IProtocol;
import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
@Builder
public class HTTPProtocolConfig implements IProtocolConfig {
    /**
     * 基础属性
     */
    private String id;
    private String name;
    private String desc;
    @Builder.Default
    private ProtocolType type = ProtocolType.HTTP;
    @Builder.Default
    private ProtocolMode mode = ProtocolMode.SYNCHRONOUS;
    private ProtocolSide side;
    @Builder.Default
    private ProtocolAction action = ProtocolAction.SHORT;
    private Class<? extends IProtocol> protocolClass;
    @Builder.Default
    private String encoding = ProtocolConstants.DEFAULT_ENCODING;
    private String url;
    private String contentType;
    /**
     * 连接超时时间
     */
    @Builder.Default
    private int connectTimeout = ProtocolConstants.DEFAULT_CONN_TIMEOUT;
    /**
     * 数据读超时时间
     */
    @Builder.Default
    private int readTimeout = ProtocolConstants.DEFAULT_READ_TIMEOUT;
    /**
     * NIO框架中的selector数量
     */
    private int ioThread;
    /**
     * NIO 框架中的业务处理线程数量
     */
    private int workThread;
    /**
     * 扩展参数
     */
    @Singular(value = "addParam")
    private Map<String, Object> param;

    @Builder.Default
    private String protocol = "TLS";
    private String keyStorePath;
    private String keyStoreCheckPasswd;
    private String keyStorePasswd;
    @Builder.Default
    private boolean duplexAuth = false;
    private boolean isServer;
    private String cerStorePath;
    private String cerStorePasswd;

    public String getType() {
        return type.name();
    }

    public String getMode() {
        return mode.name();
    }

    public String getSide() {
        return side.name();
    }

    public String getAction() {
        return action.name();
    }

    @Override
    public <T> T getAttachedParam(String name, Class<T> tClass) {
        if (param != null && param.containsKey(name)) {
            return (T) param.get(name);
        }
        return null;
    }

    @Override
    public <T> T getAttachedParam(String name, Class<T> tClass, T defaultValue) {
        if (param != null && param.containsKey(name)) {
            return (T) param.get(name);
        }
        return defaultValue;
    }
}
