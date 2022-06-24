package com.zlink.event.configuration;

import com.zlink.base.ZlinkContents;
import com.zlink.base.threadpool.ThreadPoolConfig;
import com.zlink.base.threadpool.ThreadPoolService;
import com.zlink.event.EventService;
import com.zlink.event.IEventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class EventAutoConfiguration {

    @Value("${zlink.event.queueSize:1000}")
    private int queueSize;

    @Bean
    public ThreadPoolConfig eventThreadPoolConfig() {
        return ThreadPoolConfig.builder()
                .name(ZlinkContents.THREAD_POOL.EVENT)
                .core(20)
                .idle(60000)
                .scheduled(true)
                .build();
    }

    @Bean(initMethod = "start")
    public IEventService eventService(ThreadPoolService threadPoolService) {
        ScheduledExecutorService executorService = threadPoolService.getScheduledExecutorService(ZlinkContents.THREAD_POOL.EVENT);
        EventService eventService = new EventService(executorService, queueSize);
        return eventService;
    }

}
