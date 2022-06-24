package com.zlink.channel.api;

import java.util.List;

public interface IChannelConfig {

    String getId();

    String getDesc();

    List<IServiceConfig> beforeConfig();

    List<IServiceConfig> afterConfig();

    List<IServiceConfig> serviceConfig();

    String getExceptionHandler();

    interface IServiceConfig {
        String getService();

        int getOrder();
    }
}
