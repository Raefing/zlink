package com.zlink.base.configuration;

import com.zlink.base.IBeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanFactoryAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public IBeanFactory beanFactory() {
        return new IBeanFactory() {
            @Override
            public Object getBean(String name) {
                return applicationContext.getBean(name);
            }

            @Override
            public <T> T getBean(String name, Class<T> tClass) {
                return applicationContext.getBean(name, tClass);
            }

            @Override
            public void registrationBean(String name, Object obj) {
                applicationContext.getAutowireCapableBeanFactory().configureBean(obj, name);
            }
        };
    }
}
