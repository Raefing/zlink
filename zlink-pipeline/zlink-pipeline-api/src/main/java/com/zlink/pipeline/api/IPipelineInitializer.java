package com.zlink.pipeline.api;

public interface IPipelineInitializer {

    Class<?> supported();

    void initPipeline(IPipeline pipeline);
}
