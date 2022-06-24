package com.zlink.channel.base;

import com.zlink.base.IContext;
import com.zlink.base.exception.ChannelException;
import com.zlink.base.report.ReportData;
import com.zlink.channel.api.*;
import com.zlink.channel.api.ext.ReloadableChannel;

public abstract class AbstractChannel<T> implements IChannel<T>, ReloadableChannel {

    protected IChannelConfig config;
    protected ChannelItemReportCollector collector;
    protected boolean status;
    private String id;
    private String desc;
    private IChannelExceptionHandler<T> handler;
    private IChannelFlow<T> flow;

    @Override
    public void start() {
        this.status = true;
    }

    @Override
    public void stop() {
        this.status = false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public void init(IChannelConfig config) {
        this.config = config;
        this.id = config.getId();
        this.desc = config.getDesc();
        this.collector = new ChannelItemReportCollector();
    }

    @Override
    public void reload(IChannelConfig config) throws ChannelException {
        stop();
        init(config);
        start();
    }

    @Override
    public ReportData collect() {
        ReportData reportData = collector.collect();
        reportData.addData("id", id);
        reportData.addData("desc", desc);
        return reportData;
    }


    @Override
    public T doFlow(T obj) {
        T temp = obj;
        String key = getContextId(obj);
        try {
            collector.start(key);
            for (IChannelBeforeService<T> beforeService : flow.beforeServices()) {
                try {
                    collector.startService(key, beforeService.getId());
                    temp = beforeService.doService(temp);
                    collector.endService(key, beforeService.getId(), true);
                } catch (ChannelException e) {
                    collector.endService(key, beforeService.getId(), false);
                    throw e;
                }
            }
            for (IChannelService<T> service : flow.service()) {
                try {
                    collector.startService(key, service.getId());
                    temp = service.doService(temp);
                    collector.endService(key, service.getId(), true);
                } catch (ChannelException e) {
                    collector.endService(key, service.getId(), false);
                    throw e;
                }
            }
            for (IChannelAfterService<T> afterService : flow.afterServices()) {
                try {
                    collector.startService(key, afterService.getId());
                    temp = afterService.doService(temp);
                    collector.endService(key, afterService.getId(), true);
                } catch (ChannelException e) {
                    collector.endService(key, afterService.getId(), false);
                    throw e;
                }
            }
            collector.end(key, true);
        } catch (Exception e) {
            collector.end(key, false);
            temp = handler.onException(temp, e);
        }
        return temp;
    }

    @Override
    public void setExceptionHandler(IChannelExceptionHandler handler) {
        this.handler = handler;
    }

    public void setFlow(IChannelFlow<T> flow) {
        this.flow = flow;
    }

    public abstract void initChannel(IChannelConfig config);

    public abstract void startChannel(IChannelConfig config);

    public abstract void stopChannel(IChannelConfig config);

    public abstract String getContextId(T obj);

    public static class DefaultChannel extends AbstractChannel<IContext> {

        @Override
        public void initChannel(IChannelConfig config) {

        }

        @Override
        public void startChannel(IChannelConfig config) {

        }

        @Override
        public void stopChannel(IChannelConfig config) {

        }

        @Override
        public String getContextId(IContext obj) {
            return obj.getId();
        }
    }

}
