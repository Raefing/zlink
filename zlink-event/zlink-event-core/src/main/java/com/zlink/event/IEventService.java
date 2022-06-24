package com.zlink.event;

public interface IEventService {
    void addEventListener(IEventListener listener);

    void addEventListener(EventType type, IEventListener listener);

    void addEventListener(EventType type, String tag, IEventListener listener);

    void pubEvent(IEvent event);

    void start();

    void stop();
}
