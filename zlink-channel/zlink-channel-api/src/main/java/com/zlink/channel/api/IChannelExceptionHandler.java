package com.zlink.channel.api;


public interface IChannelExceptionHandler<T> {

    /**
     * 渠道异常处理接口
     *
     * @param obj
     * @param throwable
     * @return
     */
    T onException(T obj, Throwable throwable);

}
