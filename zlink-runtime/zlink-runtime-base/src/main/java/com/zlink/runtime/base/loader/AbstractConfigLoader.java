package com.zlink.runtime.base.loader;

import com.zlink.runtime.base.ConfigLoader;
import com.zlink.runtime.base.ConfigType;

public abstract class AbstractConfigLoader implements ConfigLoader {
    @Override
    public <T> T load(String resource, Class<T> clazz) {
        if (resource.contains("://")) {
            String[] parts = resource.split("://", 2);
            if (parts.length == 2) {
                String schame = parts[0];
                ConfigType configType = ConfigType.valueOf(schame);
                String partResource = parts[1];
                if (configType == ConfigType.DB) {
                    return loadFromDb(partResource, clazz);
                }
                if (configType == ConfigType.FILE) {
                    return loadFromFile(partResource, clazz);
                }
                if (configType == ConfigType.CONFIG_CENTER) {
                    return loadFromConfig(partResource, clazz);
                }
            }
        }
        return null;
    }

    protected abstract <T> T loadFromDb(String resource, Class<T> clazz);

    protected abstract <T> T loadFromFile(String resource, Class<T> clazz);

    protected abstract <T> T loadFromConfig(String resource, Class<T> clazz);

}
