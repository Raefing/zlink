package com.zlink.event;

@FunctionalInterface
public interface IEventListener {
    void onEvent(IEvent event);
}
