package com.zlink.event;

import java.util.Map;

public interface IEvent {
    /**
     * 事件类型
     *
     * @return
     */
    EventType getType();

    /**
     * 事件标签
     *
     * @return
     */
    String getTag();

    /**
     * 事件源
     *
     * @return
     */
    Object getSource();

    /**
     * 事件附带的数据
     *
     * @return
     */
    Map<String, Object> getData();
}
