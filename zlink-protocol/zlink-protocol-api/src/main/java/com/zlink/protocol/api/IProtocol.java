package com.zlink.protocol.api;


import com.zlink.base.report.ProbePoint;
import com.zlink.protocol.exception.ProtocolException;

/**
 * 顶层协议接口定义
 */
public interface IProtocol extends ProbePoint {

    String getId();

    String getName();

    String getType();

    String getMode();

    String getAction();

    String getSide();

    String getDesc();

    Class<? extends IProtocol> getProtocolClass();

    void init(IProtocolConfig config) throws ProtocolException;

    void start() throws ProtocolException;

    void stop() throws ProtocolException;

}
