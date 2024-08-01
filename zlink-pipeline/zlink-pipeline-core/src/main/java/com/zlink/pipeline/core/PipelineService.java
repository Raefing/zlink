package com.zlink.pipeline.core;

import com.zlink.pipeline.api.IPipelineCallback;
import com.zlink.pipeline.api.IPipelineInitializer;
import com.zlink.pipeline.api.IPipelineService;
import com.zlink.pipeline.api.exception.NullOfInitializerException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class PipelineService<T, S> implements IPipelineService<T, S> {
    private ScheduledExecutorService executor;
    private final Map<Class<?>, IPipelineInitializer> initializerMap = new ConcurrentHashMap<>();

    @Override
    public void pipelineInitializer(Class<?> c, IPipelineInitializer pipelineInitializer) {
        initializerMap.put(c, pipelineInitializer);
    }

    @Override
    public void group(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void stream(T obj) {
        stream(obj, null);
    }

    @Override
    public void stream(T obj, IPipelineCallback<S> callback) {
        innerStream(obj, callback);
    }


    @Override
    public void asyncStream(T obj) {
        asyncStream(obj, null);
    }

    @Override
    public void asyncStream(T obj, IPipelineCallback<S> callback) {
        executor.execute(() -> {
            innerStream(obj, callback);
        });
    }

    private void innerStream(T obj, IPipelineCallback<S> callback) {
        Class<?> clazz = getClass(obj);
        IPipelineInitializer initializer = initializerMap.get(clazz);
        if (initializer != null) {
            DefaultPipeline defaultPipeline = new DefaultPipeline();
            initializer.initPipeline(defaultPipeline);
            defaultPipeline.start(new PipelineContext(obj), callback);
        } else {
            throw new NullOfInitializerException();
        }
    }

    private Class<?> getClass(T obj) {
        Class<?> clazz = obj.getClass();
        while (!initializerMap.containsKey(clazz)) {
            clazz = clazz.getSuperclass();
            if (clazz.equals(Object.class)) {
                break;
            }
        }
        if (!initializerMap.containsKey(clazz)) {
            throw new NullOfInitializerException();
        }
        return clazz;
    }
}
