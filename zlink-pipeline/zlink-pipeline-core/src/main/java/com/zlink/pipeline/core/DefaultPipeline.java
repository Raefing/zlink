package com.zlink.pipeline.core;

import com.zlink.pipeline.api.*;
import lombok.extern.slf4j.Slf4j;


@Slf4j
class DefaultPipeline extends AbstractPipeline {

    final PipelineStub head = new PipelineStub.Head();
    final PipelineStub tail = new PipelineStub.Tail();

    DefaultPipeline() {
        head.next(tail);
    }

    @Override
    public IPipeline addLast(String name, IPipelineHandler handler) {
        PipelineStub stub = new PipelineStub(handler);
        stub.setName(name);
        stub.next(tail);
        PipelineStub mark = head;
        while (mark.hasNext()) {
            PipelineStub sub = mark.next();
            if (sub instanceof PipelineStub.Tail) {
                mark.next(stub);
                stub.next(sub);
            }
            mark = sub;

        }
        return this;
    }

    @Override
    public IPipeline addFirst(String name, IPipelineHandler handler) {
        PipelineStub stub = new PipelineStub(handler);
        stub.setName(name);
        PipelineStub next = head.next();
        head.next(stub);
        stub.next(next);
        return this;
    }

    @Override
    public IPipeline add(String name, IPipelineHandler handler) {
        return addLast(name, handler);
    }

    @Override
    protected void start(IPipelineContext context, IPipelineCallback callback) {
        innerStart(context);
        if (callback != null) {
            callback.callBack(context.response());
        }
    }

    private void innerStart(IPipelineContext context) {
        //log.debug("Start Pipeline stream with:{}", context);
        while (context.status() != ContextStatus.INACTIVE) {
            if (context.status() == ContextStatus.ACTIVE) {
                doActions(context, ContextStatus.ACTIVE, ContextStatus.IN_BOUND);
            } else if (context.status() == ContextStatus.IN_BOUND) {
                doActions(context, ContextStatus.IN_BOUND, ContextStatus.IN_COMPLETE);
            } else if (context.status() == ContextStatus.OUT_BOUND) {
                doActions(context, ContextStatus.OUT_BOUND, ContextStatus.OUT_COMPLETE);
            } else if (context.status() == ContextStatus.EXCEPTION) {
                doActions(context, ContextStatus.EXCEPTION, ContextStatus.EXCEPTION_COMPLETE);
            } else {
                doActions(context, context.status(), ContextStatus.INACTIVE);
            }
        }
    }

    private void doActions(IPipelineContext context, ContextStatus status, ContextStatus next) {
        PipelineStub current = head;
        while (current.hasNext()) {
            current.doStub(context);
            if (context.status() != status) {
                break;
            }
            try {
                current = current.next();
            } catch (Exception e) {
                break;
            }
        }
        //log.debug("Change Status from:{},to:{}", status, next);
        context.status(next);
    }

    @Override

    protected PipelineStub toHead() {
        return head;
    }

    @Override
    protected PipelineStub toTail() {
        return tail;
    }
}
