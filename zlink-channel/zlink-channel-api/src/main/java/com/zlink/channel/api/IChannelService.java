package com.zlink.channel.api;

import com.zlink.base.exception.ChannelException;

public interface IChannelService<T> {

    void init(IChannelConfig.IServiceConfig config);

    String getId();

    T doService(T obj) throws ChannelException;
}
