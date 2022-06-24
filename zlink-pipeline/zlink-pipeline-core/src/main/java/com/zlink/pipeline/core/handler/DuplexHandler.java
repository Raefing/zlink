package com.zlink.pipeline.core.handler;

import com.zlink.pipeline.api.IPipelineContext;
import com.zlink.pipeline.api.IPipelineOutBoundHandler;

public class DuplexHandler extends InboundHandler implements IPipelineOutBoundHandler {
    @Override
    public void onInactive(IPipelineContext context) throws Exception {

    }
}
