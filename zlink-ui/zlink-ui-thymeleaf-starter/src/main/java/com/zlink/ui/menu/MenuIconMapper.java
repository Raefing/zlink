package com.zlink.ui.menu;

import java.util.HashMap;
import java.util.Map;

public final class MenuIconMapper {
    private static Map<String, String> stringMap = new HashMap<>();

    static {
        stringMap.put("HOME", "layui-icon-home");
        stringMap.put("COMS", "layui-icon-component");
        stringMap.put("SETS", "layui-icon-set");
        stringMap.put("APPS", "layui-icon-app");
        stringMap.put("USER", "layui-icon-user");
        stringMap.put("AUTH", "layui-icon-auz");
        stringMap.put("SENR", "layui-icon-senior");
        stringMap.put("TEMP", "layui-icon-template");
        stringMap.put("WEBS", "layui-icon-website");
        stringMap.put("NOTC", "layui-icon-notice");
        stringMap.put("THEM", "layui-icon-theme");
        stringMap.put("MARK", "layui-icon-note");
    }

    private MenuIconMapper() {
    }

    public static String map(String id) {
        if (stringMap.containsKey(id)) {
            return stringMap.get(id);
        } else {
            return "layui-icon-app";
        }
    }

}
