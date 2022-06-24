package com.zlink.pipeline.api;

@FunctionalInterface
public interface IPipelineCallback<T> {
    void callBack(T obj);
}
