package com.zlink.protocol.api;

import com.zlink.protocol.exception.ProtocolRuntimeException;

/**
 * 客户端协议接口
 */
public interface IProtocolClient<T> extends IProtocol {
    /**
     * 发送并同步返回
     *
     * @param msg
     * @return
     */
    T send(T msg);

    /**
     * 异步发送
     * 发送后即刻返回，不等待发送结果
     */
    default void asyncSend(T msg) {
        throw new ProtocolRuntimeException("此协议不支持异步处理");
    }

    /**
     * 异步接收，如果存在待处理消息，则返回消息，否则返回null，
     * 该方法不阻塞
     *
     * @return
     */
    default T asyncRecv() {
        throw new ProtocolRuntimeException("此协议不支持异步处理");
    }
}
