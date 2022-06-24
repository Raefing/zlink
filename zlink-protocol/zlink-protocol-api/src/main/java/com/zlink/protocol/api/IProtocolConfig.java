package com.zlink.protocol.api;


/**
 * 协议的配置类, 应包含该协议的所有必须配置
 * <p>
 * Configuration of protocol, records all necessary informations.
 *
 * @author chentao
 * <p>
 * Created on 2010-12-31
 */
public interface IProtocolConfig {
    /**
     * 获取该配置的id
     * <p>
     * Get id of this configuration
     *
     * @return
     */
    String getId();

    /**
     * 获取该配置的协议类型名称
     * <p>
     * Get config Protocol Name
     *
     * @return
     */
    String getName();

    /**
     * 协议描述
     */
    String getDesc();

    /**
     * 获取当前协议类型
     * <p>
     * get the protocol type
     *
     * @return
     */
    String getType();

    /**
     * 协议部署类型server,client
     *
     * @return
     */
    String getSide();

    /**
     * 获取协议传输方式，同步、异步
     */
    String getMode();

    /**
     * 获取协议长端类型
     */
    String getAction();

    /**
     * @return
     */
    Class<? extends IProtocol> getProtocolClass();

    /**
     *
     */
    String getEncoding();

    /**
     * @param name
     * @param tClass
     * @param <T>
     * @return
     */
    <T> T getAttachedParam(String name, Class<T> tClass);

    /**
     * @param name
     * @param tClass
     * @param defaultValue
     * @param <T>
     * @return
     */
    <T> T getAttachedParam(String name, Class<T> tClass, T defaultValue);
}
