package com.zlink.protocol.base;

import com.zlink.base.report.ReportData;
import com.zlink.protocol.api.IProtocol;
import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.exception.ProtocolException;

public abstract class AbstractProtocol implements IProtocol {
    protected String id;
    protected ProtocolType type;
    protected ProtocolMode mode;
    protected ProtocolSide side;
    protected ProtocolAction action;
    protected String name;
    protected Class<? extends IProtocol> protocolClass;
    protected String desc;
    protected String remoteCharset;
    protected ProtocolDataCollector collector;

    @Override
    public ReportData collect() {
        return collector.convert();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type.name();
    }

    @Override
    public String getMode() {
        return mode.name();
    }

    @Override
    public String getAction() {
        return action.name();
    }

    @Override
    public String getSide() {
        return side.name();
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public Class<? extends IProtocol> getProtocolClass() {
        return protocolClass;
    }

    @Override
    public void init(IProtocolConfig config) {
        this.id = config.getId();
        this.protocolClass = config.getProtocolClass();
        this.type = ProtocolType.valueOf(config.getType().toUpperCase());
        this.mode = ProtocolMode.valueOf(config.getMode().toUpperCase());
        this.side = ProtocolSide.valueOf(config.getSide().toUpperCase());
        this.action = ProtocolAction.valueOf(config.getAction().toUpperCase());
        this.name = config.getName();
        this.desc = config.getDesc();
        this.remoteCharset = config.getEncoding();
        collector = new ProtocolDataCollector(id, type, name, desc, side);
        initProtocol(config);
        collector.setStatus(0);
    }

    public void start() throws ProtocolException {
        startProtocol();
        collector.setStatus(1);
    }

    public void stop() throws ProtocolException {
        stopProtocol();
        collector.setStatus(2);
    }

    public void reload(IProtocolConfig newConfig) throws ProtocolException {
        stop();
        init(newConfig);
        start();
    }


    public abstract void initProtocol(IProtocolConfig config);

    public abstract void startProtocol() throws ProtocolException;

    public abstract void stopProtocol() throws ProtocolException;
}
