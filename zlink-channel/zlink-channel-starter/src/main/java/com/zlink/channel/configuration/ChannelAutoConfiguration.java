package com.zlink.channel.configuration;

import com.zlink.base.IBeanFactory;
import com.zlink.channel.api.IChannelManager;
import com.zlink.channel.base.ChannelManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnClass(ChannelManager.class)
public class ChannelAutoConfiguration {

    @Bean
    public IChannelManager channelManager(IBeanFactory beanFactory) {
        return new ChannelManager(beanFactory);
    }

}
