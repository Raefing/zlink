package com.zlink.protocol.configuration;

import com.zlink.base.IBeanFactory;
import com.zlink.protocol.api.IProtocolManager;
import com.zlink.protocol.api.ext.IMessageHandler;
import com.zlink.protocol.base.ProtocolManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
public class ProtocolAutoConfiguration {

    @Bean
    public IProtocolManager iProtocolManager(IBeanFactory beanFactory) {
        return new ProtocolManager(beanFactory);
    }
}
