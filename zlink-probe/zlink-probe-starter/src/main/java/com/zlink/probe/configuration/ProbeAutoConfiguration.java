package com.zlink.probe.configuration;


import com.zlink.base.ZlinkContents;
import com.zlink.base.report.IReporter;
import com.zlink.base.report.ReportType;
import com.zlink.base.threadpool.ThreadPoolConfig;
import com.zlink.base.threadpool.ThreadPoolService;
import com.zlink.probe.api.IProbeReporter;
import com.zlink.probe.core.ReportService;
import com.zlink.probe.core.reporter.DefaultLogProbeReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Configuration
public class ProbeAutoConfiguration {

    private Map<ReportType, Long> delayMap = new HashMap<>();

    @PostConstruct
    public void initDelayMap() {
        log.info("Init Delay Map");
    }

    @Bean
    public ThreadPoolConfig probeThreadPoolConfig() {
        return ThreadPoolConfig.builder()
                .name(ZlinkContents.THREAD_POOL.PROBE)
                .core(20)
                .idle(60000)
                .scheduled(true)
                .build();
    }

    @Lazy
    @Bean(initMethod = "start")
    public IReporter reporter(ThreadPoolService threadPoolService, IProbeReporter iProbeReporter) {
        ScheduledExecutorService executorService = threadPoolService.getScheduledExecutorService(ZlinkContents.THREAD_POOL.PROBE);
        return new ReportService(executorService, iProbeReporter, delayMap);
    }

    @Lazy
    @Bean
    public BeanPostProcessor beanPostProcessor(IReporter reporter) {
        return new ProbePointLeaderAutoRegister(reporter);
    }

    @Lazy
    @Bean
    public IProbeReporter iProbeReporter() {
        return new DefaultLogProbeReporter();
    }
}
