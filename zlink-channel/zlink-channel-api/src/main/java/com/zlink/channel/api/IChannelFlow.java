package com.zlink.channel.api;

import java.util.List;

/**
 * 渠道流程定义
 */
public interface IChannelFlow<T> {

    List<IChannelBeforeService<T>> beforeServices();

    List<IChannelService<T>> service();

    List<IChannelAfterService<T>> afterServices();

}
