package com.zlink.runtime.base;

public interface ConfigLoader {
    /**
     * 设置loader接受的配置文件路径和类型
     *
     * @param path
     * @param type
     */
    void accept(String path, ConfigFileType... type);

    /**
     * 加载配置
     */
    void load();

}
