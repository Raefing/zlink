package com.zlink.channel.api;


import com.zlink.base.exception.ChannelException;
import com.zlink.base.report.ProbePoint;

/**
 * 渠道对象抽象定义
 */
public interface IChannel<T> extends ProbePoint {

    /**
     * 渠道ID
     *
     * @return
     */
    String getId();

    /**
     * 渠道描述
     *
     * @return
     */
    String getDesc();

    /**
     * 执行渠道流程
     *
     * @param obj 原始请求对象
     */
    T doFlow(T obj);

    /**
     * 设置渠道异常处理
     *
     * @param handler
     */
    void setExceptionHandler(IChannelExceptionHandler handler);

    /**
     * 设置渠道执行流程
     *
     * @param flow
     */
    void setFlow(IChannelFlow<T> flow);

    void init(IChannelConfig config) throws ChannelException;

    void start() throws ChannelException;

    void stop() throws ChannelException;
}
