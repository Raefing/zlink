package com.zlink.pipeline.core;

import com.zlink.pipeline.api.*;
import com.zlink.pipeline.api.exception.EndOfPipelineException;
import com.zlink.pipeline.api.exception.StartOfPipelineException;
import com.zlink.pipeline.core.handler.InboundHandler;
import com.zlink.pipeline.core.handler.OutboundHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.zlink.pipeline.api.ContextStatus.*;

@Slf4j
class PipelineStub {
    @Setter
    @Getter
    private String name;
    private PipelineStub next;
    //private PipelineStub prev;
    private boolean in;
    private boolean out;
    private IPipelineHandler handler;
    private Exception exception;

    PipelineStub(IPipelineHandler handler) {
        this.handler = handler;
        if (handler instanceof IPipelineInBoundHandler) {
            in = true;
        }
        if (handler instanceof IPipelineOutBoundHandler) {
            out = true;
        }
    }

    public void doStub(IPipelineContext context) {
        innerDoSub(context);
    }

    private void innerDoSub(IPipelineContext context) {
        if (context.status() == ACTIVE) {
            doActive(context);
        }
        if (context.status() == IN_BOUND) {
            doInbound(context);
        }
        if (context.status() == IN_COMPLETE) {

        }
        if (context.status() == OUT_BOUND) {
            doOutbound(context);
        }
        if (context.status() == OUT_COMPLETE) {

        }
        if (context.status() == EXCEPTION) {
            doException(context, exception);
        }
        if (context.status() == EXCEPTION_COMPLETE) {

        }
        if (context.status() == INACTIVE) {
            doInactive(context);
        }
    }

    private void doInactive(IPipelineContext context) {
        if (out) {
            try {
                log.debug("PipelineStub:{},Outbound onInactive {}",name,handler.getClass().getName());
                ((IPipelineOutBoundHandler) handler).onInactive(context);
            } catch (Exception e) {
                context.status(ContextStatus.EXCEPTION);
                exception = e;
            }
        }
    }

    private void doOutbound(IPipelineContext context) {
        if (out) {
            try {
                log.debug("PipelineStub:{},Outbound handle {}",name,handler.getClass().getName());
                handler.handle(context);
            } catch (Exception e) {
                context.status(ContextStatus.EXCEPTION);
                exception = e;
            }
        }
    }


    private void doInbound(IPipelineContext context) {
        if (in) {
            try {
                log.debug("PipelineStub:{},Inbound handle {}",name,handler.getClass().getName());
                handler.handle(context);
            } catch (Exception e) {
                context.status(ContextStatus.EXCEPTION);
                exception = e;
            }
        }
    }

    private void doActive(IPipelineContext context) {
        if (in) {
            try {
                log.debug("PipelineStub:{},Inbound onActive {}",name,handler.getClass().getName());
                ((IPipelineInBoundHandler) handler).onActive(context);
            } catch (Exception e) {
                context.status(ContextStatus.EXCEPTION);
                exception = e;
            }
        }
    }


    private void doException(IPipelineContext context, Throwable throwable) {
        handler.onException(context, throwable);
    }

    public boolean hasNext() {
        return next != null;
    }

    /*public boolean hasPrev() {
        return prev != null;
    }*/


    public PipelineStub next() {
        return next;
    }

    /*public PipelineStub prev() {
        return prev;
    }*/

    public void next(PipelineStub next) {
        this.next = next;
    }

    /*public void prev(PipelineStub prev) {
        this.prev = prev;
    }*/


    static class Head extends PipelineStub {
        Head() {
            super(new InboundHandler());
            setName("Head");
        }

        /*@Override
        public PipelineStub prev() {
            throw new StartOfPipelineException();
        }*/
    }

    static class Tail extends PipelineStub {
        Tail() {
            super(new OutboundHandler());
            setName("Tail");
        }

        @Override
        public PipelineStub next() {
            throw new EndOfPipelineException();
        }
    }
}
