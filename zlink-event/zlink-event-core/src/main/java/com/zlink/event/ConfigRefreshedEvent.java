package com.zlink.event;

public class ConfigRefreshedEvent extends AbstractEvent {

    @Override
    public EventType getType() {
        return EventType.CONFIG_REFRESHED_EVENT;
    }

}
