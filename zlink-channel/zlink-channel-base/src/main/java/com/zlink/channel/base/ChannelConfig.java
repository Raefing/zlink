package com.zlink.channel.base;

import com.zlink.channel.api.IChannelConfig;

import java.util.ArrayList;
import java.util.List;

public class ChannelConfig implements IChannelConfig {
    private String id;
    private String desc;
    private String exceptionHandler;
    private List<IServiceConfig> beforeServices = new ArrayList<>();
    private List<IServiceConfig> services = new ArrayList<>();
    private List<IServiceConfig> afterServices = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String getExceptionHandler() {
        return exceptionHandler;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setExceptionHandler(String exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public List<IServiceConfig> beforeConfig() {
        return beforeServices;
    }

    @Override
    public List<IServiceConfig> afterConfig() {
        return afterServices;
    }

    @Override
    public List<IServiceConfig> serviceConfig() {
        return services;
    }


    public void addBeforeService(String id, int order) {
        beforeServices.add(new DefaultServiceConfig(id, order));
    }

    public void addService(String id, int order) {
        services.add(new DefaultServiceConfig(id, order));
    }

    public void addAfterService(String id, int order) {
        afterServices.add(new DefaultServiceConfig(id, order));
    }

    class DefaultServiceConfig implements IChannelConfig.IServiceConfig {
        private String serviceId;
        private int order;

        DefaultServiceConfig(String serviceId, int order) {
            this.serviceId = serviceId;
            this.order = order;
        }

        @Override
        public String getService() {
            return serviceId;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
