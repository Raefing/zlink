package com.zlink.protocol.tcp;

import com.zlink.protocol.api.IProtocol;
import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.base.*;
import com.zlink.protocol.tcp.netty.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.*;

@Getter
public class TCPProtocolConfig implements IProtocolConfig {

    private TCPProtocolConfig() {
    }

    /**
     * 基础属性
     */
    private String id;
    private String name;
    private String desc;
    private ProtocolType type;
    private ProtocolMode mode;
    private ProtocolSide side;
    private ProtocolAction action;
    private Class<? extends IProtocol> protocolClass;
    /**
     * 个性属性
     */
    /**
     * 主机列表 IP:PORT,IP:PORT方式，客户端模式使用
     */
    private List<String> targets;
    /**
     * Server模式下使用
     */
    private String bindHost;
    private int bindPort;
    /***
     * 读写策略
     */
    private String policy;
    /**
     * 对端系统编码
     */
    private String encoding = ProtocolConstants.DEFAULT_ENCODING;
    /**
     * 连接超时时间
     */
    private int connectTimeout = ProtocolConstants.DEFAULT_CONN_TIMEOUT;
    /**
     * 数据读超时时间
     */
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
    private Map<String, Object> param;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String desc;
        private ProtocolType type;
        private ProtocolMode mode = ProtocolMode.SYNCHRONOUS;
        private ProtocolSide side;
        private ProtocolAction action;
        private List<String> targets;
        private String bindHost = "0.0.0.0";
        private int bindPort;
        private String policy = ProtocolPolicy.UNKNOWN;
        private String encoding = ProtocolConstants.DEFAULT_ENCODING;
        private int connectTimeout = ProtocolConstants.DEFAULT_CONN_TIMEOUT;
        private int readTimeout = ProtocolConstants.DEFAULT_READ_TIMEOUT;
        private int ioThread;
        private int workThread;
        private Map<String, Object> param;

        public Builder tcpClient(String id, String name) {
            init(id, name);
            type = ProtocolType.TCP;
            side = ProtocolSide.CLIENT;
            action = ProtocolAction.SHORT;
            return this;
        }

        public Builder tcpServer(String id, String name) {
            init(id, name);
            type = ProtocolType.TCP;
            side = ProtocolSide.SERVER;
            action = ProtocolAction.SHORT;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder desc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder type(ProtocolType type) {
            this.type = type;
            return this;
        }

        public Builder mode(ProtocolMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder side(ProtocolSide side) {
            this.side = side;
            return this;
        }

        public Builder action(ProtocolAction action) {
            this.action = action;
            return this;
        }

        public Builder bindHost(String bindHost) {
            this.bindHost = bindHost;
            return this;
        }

        public Builder bindPort(int bindPort) {
            this.bindPort = bindPort;
            return this;
        }

        /**
         * 设置读写策略
         * @param policy 读写策略 默认length:S=n,E=m/ FIX:n/ UNKNOWN;
         * @return
         */
        public Builder policy(String policy) {
            this.policy = policy;
            return this;
        }

        public Builder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder ioThread(int ioThread) {
            this.ioThread = ioThread;
            return this;
        }

        public Builder workThread(int workThread) {
            this.workThread = workThread;
            return this;
        }

        private void init(String id, String name) {
            this.id = id;
            this.name = name;
            this.desc = name;
        }

        public Builder addParam(String key, Object value) {
            param.put(key, value);
            return this;
        }

        public Builder addTarget(String target) {
            targets.add(target);
            return this;
        }

        public TCPProtocolConfig build() {
            TCPProtocolConfig config = new TCPProtocolConfig();
            config.id = this.id;
            config.name = this.name;
            config.desc = this.desc;
            config.type = this.type;
            config.mode = this.mode;
            config.side = this.side;
            config.action = this.action;
            if (config.action == ProtocolAction.LONG) {
                if (config.side == ProtocolSide.CLIENT) {
                    config.protocolClass = NettyLongClient.class;
                } else {
                    config.protocolClass = NettyLongServer.class;
                }
            } else {
                if (config.side == ProtocolSide.CLIENT) {
                    config.protocolClass = NettyShortClient.class;
                } else {
                    config.protocolClass = NettyShortServer.class;
                }
            }
            config.targets = this.targets;
            config.bindHost = this.bindHost;
            config.bindPort = this.bindPort;
            config.policy = this.policy;
            config.encoding = this.encoding;
            config.connectTimeout = this.connectTimeout;
            config.readTimeout = this.readTimeout;
            config.ioThread = this.ioThread;
            config.workThread = this.workThread;
            config.param = this.param;
            return config;
        }
    }
}
