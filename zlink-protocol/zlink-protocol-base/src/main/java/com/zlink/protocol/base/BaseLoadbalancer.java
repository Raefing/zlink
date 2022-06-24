package com.zlink.protocol.base;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BaseLoadbalancer<T> {

    private String loadBalance;
    private String protocolId;
    private List<Sta> statusList = new ArrayList<>();

    public BaseLoadbalancer(String protocolId) {
        this(protocolId, "fast");
    }

    public BaseLoadbalancer(String protocolId, String loadBalance) {
        this.protocolId = protocolId;
        this.loadBalance = loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public void init(Map<String, T> map) {
        map.forEach((key, t) -> {
            statusList.add(new Sta(key, false, t));
        });
    }

    public void recordStatus(String key, boolean status) {
        statusList.forEach(sta -> {
            if (sta.getKey().equals(key)) {
                sta.setStatus(status);
                return;
            }
        });
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Protocol:" + protocolId + "[");
        statusList.forEach(sta -> {
            stringBuilder.append("(Key:" + sta.getKey());
            stringBuilder.append("Status:" + (sta.getStatus() ? "OK" : "FAIL") + ")");
        });
        stringBuilder.append("]");
        return stringBuilder.toString();
    }


    public Map<String, Boolean> getAllStatus() {
        return statusList.stream().collect(Collectors.toMap(sta -> sta.getKey(), sta -> sta.getStatus()));
    }

    public List<T> allTargets() {
        return statusList.stream().map(Sta::getTarget).collect(Collectors.toList());
    }

    private int rollingIndex = 0;

    public T loadBalance() {
        T t = null;
        if (statusList.size() > 0) {
            List<Sta> list = statusList.stream().filter(sta -> sta.getStatus()).collect(Collectors.toList());
            if (list.size() > 0) {
                if ("random".equalsIgnoreCase(loadBalance)) {
                    int index = (int) (Math.random() * (list.size() - 1));
                    t = list.get(index).getTarget();
                } else if ("rolling".equalsIgnoreCase(loadBalance)) {
                    if (rollingIndex >= (list.size() - 1)) {
                        rollingIndex = 0;
                    } else {
                        rollingIndex += 1;
                    }
                    t = list.get(rollingIndex).getTarget();
                } else {
                    t = list.get(0).getTarget();
                }
            } else {
                t = statusList.get(0).getTarget();
            }
        }
        return t;
    }

    @Data
    @AllArgsConstructor
    class Sta {
        String key;
        Boolean status;
        T target;
    }

}
