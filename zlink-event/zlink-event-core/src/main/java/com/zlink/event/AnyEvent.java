package com.zlink.event;

public class AnyEvent extends AbstractEvent {
    @Override
    public EventType getType() {
        return EventType.ALL_EVENT;
    }
}
