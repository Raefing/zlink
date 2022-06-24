package com.zlink.protocol.api;

import com.zlink.protocol.api.ext.IMessageHandler;
import com.zlink.protocol.exception.ProtocolRuntimeException;

/**
 * 服务端协议接口定义
 */
public interface IProtocolServer<T> extends IProtocol {

    T onMessage(T message);

    void setMessageHandler(IMessageHandler handler);

    /**
     * 发送
     */
    default void asyncSend(T msg) {
        throw new ProtocolRuntimeException("此协议不支持异步处理");
    }

    /**
     * 接收
     *
     * @return
     */
    default T asyncRecv() {
        throw new ProtocolRuntimeException("此协议不支持异步处理");
    }

}
