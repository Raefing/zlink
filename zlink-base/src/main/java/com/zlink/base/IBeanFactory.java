package com.zlink.base;

public interface IBeanFactory {
    Object getBean(String name);

    <T> T getBean(String name, Class<T> tClass);

    void registrationBean(String name, Object obj);
}
