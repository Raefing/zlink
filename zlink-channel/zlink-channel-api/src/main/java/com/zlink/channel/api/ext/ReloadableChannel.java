package com.zlink.channel.api.ext;

import com.zlink.base.exception.ChannelException;
import com.zlink.channel.api.IChannelConfig;

public interface ReloadableChannel {
    void reload(IChannelConfig config) throws ChannelException;
}
