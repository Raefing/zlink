package com.zlink.pipeline.core;

import com.zlink.pipeline.api.*;

class PipelineContext implements IPipelineContext {
    private ContextStatus status;
    private Object request;
    private Object response;

    PipelineContext(Object obj) {
        this.request = obj;
        this.response = null;
        status = ContextStatus.ACTIVE;
    }

    @Override
    public ContextStatus status() {
        return status;
    }

    @Override
    public void status(ContextStatus status) {
        this.status = status;
    }

    @Override
    public Object request() {
        return request;
    }

    @Override
    public Object response() {
        return response;
    }

    @Override
    public void fireInBoundEvent() {
        status = ContextStatus.IN_COMPLETE;
    }

    @Override
    public void fireOutBoundEvent() {
        status = ContextStatus.OUT_COMPLETE;
    }

    @Override
    public void fireExceptionCaught(Throwable throwable) {
        status = ContextStatus.EXCEPTION_COMPLETE;
    }

    @Override
    public void fireActive() {
        status = ContextStatus.ACTIVE;
    }

    @Override
    public void fireInactive() {
        status = ContextStatus.INACTIVE;
    }

    @Override
    public void write(Object o) {
        response = o;
        status = ContextStatus.IN_COMPLETE;
    }
}
