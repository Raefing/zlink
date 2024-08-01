package com.zlink.pipeline.api;

import java.util.concurrent.ScheduledExecutorService;

public interface IPipelineService<T, S> {
    void pipelineInitializer(Class<?> clazz, IPipelineInitializer pipelineInitializer);

    void group(ScheduledExecutorService executor);

    void stream(T obj);

    void stream(T obj, IPipelineCallback<S> callback);

    void asyncStream(T obj);

    void asyncStream(T obj, IPipelineCallback<S> callback);
}
