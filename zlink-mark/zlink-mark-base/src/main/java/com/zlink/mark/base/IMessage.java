package com.zlink.mark.base;

public interface IMessage {
    short getType();

    String getMessageId();

    long getTimestamp();
}
