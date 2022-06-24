package com.zlink.protocol.api;

import com.zlink.base.IManager;
import com.zlink.base.report.ProbePointLeader;
import com.zlink.protocol.api.ext.IMessageHandler;

/**
 * 协议管理器接口定义
 */
public interface IProtocolManager extends IManager<IProtocolConfig>, ProbePointLeader {

    IProtocolClient getClient(String id);

    IProtocolServer getServer(String id);

    void registerHandler(String id, IMessageHandler handler);
}
