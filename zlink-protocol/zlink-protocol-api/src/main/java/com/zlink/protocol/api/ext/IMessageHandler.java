package com.zlink.protocol.api.ext;

@FunctionalInterface
public interface IMessageHandler<T> {
    T handle(String id, T request);
}
