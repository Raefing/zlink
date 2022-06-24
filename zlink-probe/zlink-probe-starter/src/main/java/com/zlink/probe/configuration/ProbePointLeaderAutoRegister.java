package com.zlink.probe.configuration;

import com.zlink.base.report.IReporter;
import com.zlink.base.report.ProbePointLeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j
public class ProbePointLeaderAutoRegister implements BeanPostProcessor {

    private IReporter reporter;

    public ProbePointLeaderAutoRegister(IReporter reporter) {
        this.reporter = reporter;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ProbePointLeader) {
            log.info("注册采集点[{}],实现类[{}]", beanName, bean.getClass());
            ProbePointLeader leader = (ProbePointLeader) bean;
            reporter.report(leader);
        }
        return bean;
    }
}
