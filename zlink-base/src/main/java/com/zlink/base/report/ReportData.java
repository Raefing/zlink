package com.zlink.base.report;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ReportData implements Serializable {
    private long timeStamp;
    private ReportType type;
    private Map<String, Object> dataMap = new HashMap<>();

    public ReportData addData(String key, Object data) {
        dataMap.put(key, data);
        return this;
    }

    public Object getData(String key) {
        return dataMap.get(key);
    }

    public <T> T getData(String key, Class<T> tClass) {
        return (T) getData(key);
    }

    private ReportData(ReportType type) {
        this.type = type;
        timeStamp = System.currentTimeMillis();
    }

    public static ReportData build(ReportType type) {
        return new ReportData(type);
    }
}
