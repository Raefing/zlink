package com.zlink.pipeline.core;

import com.zlink.pipeline.api.IPipelineCallback;
import com.zlink.pipeline.api.IPipelineService;
import com.zlink.pipeline.api.PipelineInitializer;
import com.zlink.pipeline.api.exception.NullOfInitializerException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class PipelineService implements IPipelineService {
    private ScheduledExecutorService executor;
    private Map<Class, PipelineInitializer> initializerMap = new ConcurrentHashMap<>();

    @Override
    public IPipelineService pipelineInitializer(Class c, PipelineInitializer pipelineInitializer) {
        initializerMap.put(c, pipelineInitializer);
        return this;
    }

    @Override
    public IPipelineService group(ScheduledExecutorService executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public void stream(Object obj) {
        stream(obj, null);
    }

    @Override
    public void stream(Object obj, IPipelineCallback callback) {
        innerStream(obj, callback);
    }


    @Override
    public void asyncStream(Object obj) {
        asyncStream(obj, null);
    }

    @Override
    public void asyncStream(Object obj, IPipelineCallback callback) {
        executor.execute(() -> {
            innerStream(obj, callback);
        });
    }

    private void innerStream(Object obj, IPipelineCallback callback) {
        Class clazz = getClass(obj);
        PipelineInitializer initializer = initializerMap.get(clazz);
        if (initializer != null) {
            DefaultPipeline defaultPipeline = new DefaultPipeline();
            initializer.initPipeline(defaultPipeline);
            defaultPipeline.start(new PipelineContext(obj), callback);
        } else {
            throw new NullOfInitializerException();
        }
    }

    private Class getClass(Object obj) {
        Class clazz = obj.getClass();
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
