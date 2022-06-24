package com.zlink.runtime.base.loader;

import com.zlink.runtime.base.ConfigFileType;
import com.zlink.runtime.base.ConfigLoader;

import java.util.Arrays;
import java.util.List;

public class ProtocolConfigLoader implements ConfigLoader {
    private String path;
    private List<ConfigFileType> types;

    @Override
    public void accept(String path, ConfigFileType... type) {
        this.path = path;
        this.types = Arrays.asList(type);
    }

    @Override
    public void load() {

    }
}
