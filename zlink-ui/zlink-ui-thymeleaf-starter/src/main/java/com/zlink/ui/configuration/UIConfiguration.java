package com.zlink.ui.configuration;

import com.zlink.ui.img.ImageLoader;
import com.zlink.ui.interceptor.LoginHandlerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@MapperScan(basePackages = "com.zlink.ui")
public class UIConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginHandlerInterceptor())
                .addPathPatterns("/**")//拦截所有
                .excludePathPatterns(
                        "/system",
                        "/user/login",
                        "/user/login.html",
                        "/user/register",
                        "/user/register.html",
                        "/static/**/*"
                );//排除
    }

    @Bean
    public ImageLoader imageLoader() {
        return new ImageLoader();
    }
}
