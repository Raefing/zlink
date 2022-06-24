package com.zlink.service.api.annotation.register;

import com.zlink.service.api.annotation.Service;
import com.zlink.service.api.annotation.ServiceScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

@Slf4j
public class ServiceRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {

    private ClassLoader classLoader;
    private Environment environment;
    private ResourceLoader resourceLoader;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        //获取所有注解的属性和值
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(ServiceScan.class.getName()));
        if (annoAttrs!=null) {
            //获取到basePackage的值
            String[] basePackages = annoAttrs.getStringArray("basePackages");
            //如果没有设置basePackage 扫描路径,就扫描对应包下面的值
            if (basePackages.length == 0) {
                basePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
            }

            //自定义的包扫描器
            FindServiceClassPathScanHandle scanHandle = new FindServiceClassPathScanHandle(beanDefinitionRegistry, false);
            if (resourceLoader != null) {
                scanHandle.setResourceLoader(resourceLoader);
            }
            //扫描指定路径下的接口
            Set<BeanDefinitionHolder> beanDefinitionHolders = scanHandle.doScan(basePackages);
            beanDefinitionHolders.forEach(beanDefinitionHolder -> {
                log.error("--->{}", beanDefinitionHolder);
                beanDefinitionHolder.getBeanDefinition().getBeanClassName();
            });
        }
    }

    class FindServiceClassPathScanHandle extends ClassPathBeanDefinitionScanner {

        public FindServiceClassPathScanHandle(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
            super(registry, useDefaultFilters);
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            //添加过滤条件，这里是只添加了@Service的注解才会被扫描到
            addIncludeFilter(new AnnotationTypeFilter(Service.class));
            //调用spring的扫描
            Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
            return beanDefinitionHolders;
        }


    }
}
