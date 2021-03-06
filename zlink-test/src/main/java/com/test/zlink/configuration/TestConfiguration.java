package com.test.zlink.configuration;

import com.zlink.channel.api.*;
import com.zlink.channel.base.ChannelConfig;
import com.zlink.channel.base.ChannelContext;
import com.zlink.protocol.api.IProtocolManager;
import com.zlink.protocol.api.ext.IMessageHandler;
import com.zlink.protocol.base.ProtocolAction;
import com.zlink.protocol.base.ProtocolPolicy;
import com.zlink.protocol.tcp.TCPProtocolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class TestConfiguration {
    @Autowired
    private IProtocolManager iProtocolManager;
    @Autowired
    private IChannelManager iChannelManager;

    @PostConstruct
    public void doInit() {
        iProtocolManager.load(TCPProtocolConfig.builder()
                .tcpServer("TS", "TS")
                .action(ProtocolAction.LONG)
                .bindPort(10011)
                .policy(ProtocolPolicy.LENGTH(0, 8))
                .encoding("GBK")
                .build());
        iProtocolManager.registerHandler("*", (id, request) -> {
            IChannel<ChannelContext> channel = iChannelManager.getChannel(id);
            ChannelContext ret = channel.doFlow(new ChannelContext(request));
            return ret.getData();
        });
        iProtocolManager.start();
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.setId("TS");
        channelConfig.setDesc("TS");
        channelConfig.addService("iService", 0);
        channelConfig.setExceptionHandler("exceptionHandler");
        iChannelManager.load(channelConfig);
        iChannelManager.start();
    }

    @Bean
    public IChannelService iService() {
        return new IChannelService<ChannelContext<byte[]>>() {
            private IChannelConfig.IServiceConfig config;

            @Override
            public void init(IChannelConfig.IServiceConfig config) {
                this.config = config;
            }

            @Override
            public String getId() {
                return config.getService();
            }

            @Override
            public ChannelContext<byte[]> doService(ChannelContext<byte[]> o) {
                byte[] data = o.getData();
                log.info("Requests:{}", new String(data));
                o.setData("????????????".getBytes());
                return o;
            }
        };
    }

    @Bean
    public IChannelExceptionHandler<ChannelContext<byte[]>> exceptionHandler() {
        return (obj, throwable) -> {
            obj.setData(("????????????????????????:" + throwable.getMessage()).getBytes());
            return obj;
        };
    }
}
