package com.zlink.runtime.base;

public enum ConfigType {
    XML,
    YAML,
    PROPERTIES,
    MYSQL,
    ORACLE,
    DB2,
    TIDB,
    OCEAN_BASE,
    FILE,
    DB,
    CONFIG_CENTER;

    public static ConfigType of(String value) {
        return ConfigType.valueOf(value);
    }

}
