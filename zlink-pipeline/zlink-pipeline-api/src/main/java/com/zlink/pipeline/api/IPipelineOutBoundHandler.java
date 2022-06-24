package com.zlink.pipeline.api;

public interface IPipelineOutBoundHandler extends IPipelineHandler {
    void onInactive(IPipelineContext context) throws Exception;
}
