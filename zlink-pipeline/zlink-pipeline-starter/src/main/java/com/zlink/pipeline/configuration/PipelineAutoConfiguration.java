package com.zlink.pipeline.configuration;

import com.zlink.base.ZlinkContents;
import com.zlink.base.threadpool.ThreadPoolConfig;
import com.zlink.base.threadpool.ThreadPoolService;
import com.zlink.pipeline.api.IPipelineService;
import com.zlink.pipeline.core.PipelineService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
@ConditionalOnClass(PipelineService.class)
public class PipelineAutoConfiguration {

    @Bean
    public ThreadPoolConfig pipelineThreadPoolConfig() {
        return ThreadPoolConfig.builder()
                .name(ZlinkContents.THREAD_POOL.PIPELINE)
                .core(20)
                .idle(60000)
                .scheduled(true)
                .build();
    }

    @Lazy
    @Bean
    @ConditionalOnMissingBean
    public IPipelineService pipelineService(ThreadPoolService threadPoolService) {
        IPipelineService pipelineService = new PipelineService();
        ScheduledExecutorService service = threadPoolService.getScheduledExecutorService(ZlinkContents.THREAD_POOL.PIPELINE);
        if (service != null) {
            pipelineService.group(service);
        }
        return pipelineService;
    }
}
