package com.zlink.base.configuration;

import com.zlink.base.threadpool.ThreadPoolConfig;
import com.zlink.base.threadpool.ThreadPoolService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BaseAutoConfiguration {

    @Bean
    public ThreadPoolService threadPoolManager(List<ThreadPoolConfig> configList) {
        ThreadPoolService threadPoolService = new ThreadPoolService();
        configList.forEach(c -> threadPoolService.load(c));
        return threadPoolService;
    }
}
