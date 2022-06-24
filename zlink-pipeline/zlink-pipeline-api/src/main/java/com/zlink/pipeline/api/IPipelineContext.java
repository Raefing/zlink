package com.zlink.pipeline.api;

public interface IPipelineContext {

    void fireInBoundEvent();

    void fireOutBoundEvent();

    void fireExceptionCaught(Throwable throwable);

    void fireActive();

    void fireInactive();

    ContextStatus status();

    void status(ContextStatus status);

    Object request();

    Object response();

    void write(Object obj);

}
