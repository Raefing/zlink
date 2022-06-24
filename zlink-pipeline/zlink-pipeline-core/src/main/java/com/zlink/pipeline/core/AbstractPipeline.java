package com.zlink.pipeline.core;

import com.zlink.pipeline.api.*;

abstract class AbstractPipeline implements IPipeline {

    private PipelineStub pipelineStub;

    @Override
    public IPipeline addLast(IPipelineHandler handler) {
        return addLast(handler.getClass().getSimpleName(),handler);
    }

    @Override
    public abstract IPipeline addLast(String name, IPipelineHandler handler);

    @Override
    public IPipeline addFirst(IPipelineHandler handler) {
        return addFirst(handler.getClass().getSimpleName(),handler);
    }

    @Override
    public abstract IPipeline addFirst(String name, IPipelineHandler handler);

    @Override
    public IPipeline add(IPipelineHandler handler) {
        return add(handler.getClass().getSimpleName(), handler);
    }

    @Override
    public abstract IPipeline add(String name, IPipelineHandler handler);
    /*{
        init();
        PipelineStub stub = new PipelineStub(handler);
        PipelineStub next = null;
        try {
            next = pipelineStub.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (next != null) {
            pipelineStub.next(stub);
            stub.prev(pipelineStub);
            stub.next(next);
            next.prev(stub);
        }
        pipelineStub = stub;
        return this;
    }

    private void init() {
        if (pipelineStub == null) {
            this.pipelineStub = toHead();
        }
    }*/

    protected abstract void start(IPipelineContext context, IPipelineCallback callback);

    protected abstract PipelineStub toHead();

    protected abstract PipelineStub toTail();
}
