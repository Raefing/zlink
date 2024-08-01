package com.zlink.runtime.base;

public interface ConfigLoader {
    /**
     * 加载配置
     */
    <T> T load(String resource, Class<T> clazz);

}
