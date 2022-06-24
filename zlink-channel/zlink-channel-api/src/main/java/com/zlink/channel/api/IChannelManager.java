package com.zlink.channel.api;

import com.zlink.base.IManager;

/**
 * 渠道管理器
 */
public interface IChannelManager extends IManager<IChannelConfig> {
    /**
     * 根据渠道ID获取渠道实例
     *
     * @param channelId
     * @return
     */
    IChannel getChannel(String channelId);

}
