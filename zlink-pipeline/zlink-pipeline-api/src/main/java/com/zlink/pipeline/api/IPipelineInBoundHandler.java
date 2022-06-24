package com.zlink.pipeline.api;

public interface IPipelineInBoundHandler extends IPipelineHandler{

    void onActive(IPipelineContext context) throws Exception;

}
