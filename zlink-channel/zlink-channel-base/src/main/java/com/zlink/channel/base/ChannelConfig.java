package com.zlink.channel.base;

import com.zlink.channel.api.IChannelConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChannelConfig implements IChannelConfig {
    private String id;
    private String desc;
    private String exceptionHandler;
    private final List<IServiceConfig> beforeConfig = new ArrayList<>();
    private final List<IServiceConfig> serviceConfig = new ArrayList<>();
    private final List<IServiceConfig> afterConfig = new ArrayList<>();

    public void addBeforeService(String id, int order) {
        beforeConfig.add(new DefaultServiceConfig(id, order));
    }

    public void addService(String id, int order) {
        serviceConfig.add(new DefaultServiceConfig(id, order));
    }

    public void addAfterService(String id, int order) {
        afterConfig.add(new DefaultServiceConfig(id, order));
    }

    @Data
    static class DefaultServiceConfig implements IChannelConfig.IServiceConfig {
        private final String serviceId;
        private final int order;
        DefaultServiceConfig(String serviceId, int order) {
            this.serviceId = serviceId;
            this.order = order;
        }
    }
}
