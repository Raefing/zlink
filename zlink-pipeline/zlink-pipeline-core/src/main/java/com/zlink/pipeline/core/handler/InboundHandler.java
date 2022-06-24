package com.zlink.pipeline.core.handler;

import com.zlink.pipeline.api.IPipelineContext;
import com.zlink.pipeline.api.IPipelineInBoundHandler;

public class InboundHandler implements IPipelineInBoundHandler {
    @Override
    public void handle(IPipelineContext context) throws Exception {
    }

    @Override
    public void onException(IPipelineContext context, Throwable throwable) {
    }

    @Override
    public void onActive(IPipelineContext context) throws Exception {
    }
}
