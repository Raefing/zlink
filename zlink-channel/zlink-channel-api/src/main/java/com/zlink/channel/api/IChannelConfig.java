package com.zlink.channel.api;

import java.util.List;

public interface IChannelConfig {

    String getId();

    String getDesc();

    List<IServiceConfig> getBeforeConfig();

    List<IServiceConfig> getAfterConfig();

    List<IServiceConfig> getServiceConfig();

    String getExceptionHandler();

    interface IServiceConfig {
        String getServiceId();

        int getOrder();
    }
}
