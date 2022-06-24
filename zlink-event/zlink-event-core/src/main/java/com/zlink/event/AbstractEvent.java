package com.zlink.event;


import java.util.Map;

public abstract class AbstractEvent implements IEvent {
    protected String tag;
    protected Object source;
    protected Map<String, Object> data;

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
