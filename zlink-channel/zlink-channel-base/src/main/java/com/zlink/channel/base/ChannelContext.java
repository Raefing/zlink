package com.zlink.channel.base;

import com.zlink.base.IContext;
import lombok.Setter;

import java.util.UUID;

public class ChannelContext<T> implements IContext {
    private static final String PRE = "CHANNEL-";

    private String id;
    @Setter
    private T data;

    public ChannelContext(T data) {
        id = PRE + UUID.randomUUID().toString();
        this.data = data;
    }

    @Override
    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }
}
