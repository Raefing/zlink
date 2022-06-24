package com.zlink.protocol.base;

import com.zlink.protocol.api.ext.IMessageHandler;
import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.api.IProtocolServer;
import com.zlink.protocol.api.ext.*;
import com.zlink.protocol.exception.ProtocolException;
import com.zlink.protocol.exception.ProtocolRuntimeException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public abstract class AbstractProtocolServer<T> extends AbstractProtocol implements IProtocolServer<T>, ReloadableProtocol {
    protected String detect;
    protected String detectRet;
    private ServerDetector detector;
    private IMessageHandler<T> handler;

    @Override
    public void initProtocol(IProtocolConfig protocolConfig) {
        this.detect = protocolConfig.getAttachedParam("detect", String.class);
        this.detectRet = protocolConfig.getAttachedParam("detectRet", String.class);
        if (StringUtils.isNotBlank(detect) && StringUtils.isNotBlank(detectRet)) {
            detector = new BaseServerDetector(detect, detectRet);
        }
        initServer(protocolConfig);
    }

    public void startProtocol() throws ProtocolException {
        startServer();
    }

    public void stopProtocol() throws ProtocolException {
        stopServer();
    }

    public abstract void initServer(IProtocolConfig config);

    @Override
    public void setMessageHandler(IMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public T onMessage(T message) throws ProtocolRuntimeException {
        String msg = convertDet(message);
        if (detector != null && detector.isDetect(msg)) {
            return convertDetRet(detector.detect(msg));
        } else {
            collector.recv(msgSize(message));
            if (handler != null) {
                try {
                    T ret = handler.handle(getId(), message);
                    collector.send(msgSize(ret));
                    collector.success();
                    return ret;
                } catch (Exception e) {
                    collector.error();
                    throw e;
                }
            } else {
                collector.error();
                throw new ProtocolRuntimeException("协议[" + getId() + "]未配置消息处理器");
            }
        }
    }

    public abstract int msgSize(T msg);

    /**
     * 探测请求报文转换
     *
     * @param message 请求报文
     * @return
     */
    public abstract String convertDet(T message);

    /**
     * 探测响应报文转换
     *
     * @param message 响应报文
     * @return
     */
    public abstract T convertDetRet(String message);

    public abstract void startServer() throws ProtocolException;

    public abstract void stopServer() throws ProtocolException;

}
