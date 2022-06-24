package com.zlink.service.api;

import com.zlink.base.IManager;

public interface IServiceManager extends IManager<Class<? extends IService>> {

    String DEFAULT_VERSION = "V1";

    default IService getService(String serviceName) {
        return getService(serviceName, DEFAULT_VERSION);
    }

    IService getService(String serviceName, String version);

    default IService getService(Class<? extends IService> clazz) {
        return getService(clazz, DEFAULT_VERSION);
    }

    IService getService(Class<? extends IService> clazz, String version);


}
