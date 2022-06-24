package com.zlink.base.report;


import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ToString
public class ReportData implements Serializable {
    private long timeStamp;
    private ReportType type;
    private Map<String, Object> dataMap = new HashMap<>();

    public void addData(String key, Object data) {
        dataMap.put(key, data);
    }

    public Object getData(String key) {
        return dataMap.get(key);
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public <T> T getData(String key, Class<T> tClass) {
        return (T) getData(key);
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    private ReportData(ReportType type) {
        this.type = type;
        timeStamp = System.currentTimeMillis();
    }

    public static ReportData build(ReportType type) {
        return new ReportData(type);
    }
}
