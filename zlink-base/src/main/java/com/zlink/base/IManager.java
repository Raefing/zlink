package com.zlink.base;


/**
 * 顶层管理器对象接口定义
 */
public interface IManager<T> {

    void load(T target);

    void start();

    void stop();

    void reload(T target);

}
