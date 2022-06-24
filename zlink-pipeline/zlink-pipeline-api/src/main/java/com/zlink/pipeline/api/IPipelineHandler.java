package com.zlink.pipeline.api;

public interface IPipelineHandler {
    void handle(IPipelineContext context) throws Exception;

    void onException(IPipelineContext context, Throwable throwable);
}
