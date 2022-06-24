package com.zlink.runtime.base;

public enum ConfigFileType {
    ALL, XML, YAML, PROPERTIES;

    public ConfigFileType value(String value) {
        if (value.equalsIgnoreCase("*") || value.equalsIgnoreCase("*.*")) {
            return ALL;
        }
        if (value.equalsIgnoreCase("XML")) {
            return XML;
        }
        if (value.equalsIgnoreCase("YAML") || value.equalsIgnoreCase("YML")) {
            return YAML;
        }
        if (value.equalsIgnoreCase("PROPERTIES")) {
            return PROPERTIES;
        }
        return null;
    }

    public String extension(ConfigFileType type) {
        switch (type) {
            case YAML:
                return ".y[a]ml";
            case XML:
                return ".xml";
            case PROPERTIES:
                return ".properties";
            case ALL:
            default:
                return ".*";
        }
    }
}
