package com.zlink.protocol.base;


import com.zlink.base.AbstractManager;
import com.zlink.base.IBeanFactory;
import com.zlink.base.report.ReportData;
import com.zlink.base.report.ReportType;
import com.zlink.protocol.api.*;
import com.zlink.protocol.api.ext.IMessageHandler;
import com.zlink.protocol.api.ext.ReloadableProtocol;
import com.zlink.protocol.exception.ProtocolException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ProtocolManager extends AbstractManager<IProtocolConfig> implements IProtocolManager {

    private Map<String, IMessageHandler> handlers = new HashMap<>();

    public ProtocolManager(IBeanFactory factory) {
        super(factory);
    }

    private Map<String, IProtocol> protocolMap = new ConcurrentHashMap<>();

    @Override
    public void load(IProtocolConfig config) {
        Class<? extends IProtocol> clazz = config.getProtocolClass();
        IProtocol protocol = null;
        try {
            //通过反射获取实例
            Object obj = clazz.newInstance();
            if (obj instanceof IProtocol) {
                //转换
                protocol = (IProtocol) obj;
                try {
                    //初始化
                    protocol.init(config);
                    //注册
                    protocolMap.put(protocol.getId(), protocol);
                } catch (ProtocolException e) {
                    log.error("初始化协议[{}]发生异常", config.getId(), e);
                }
            } else {
                log.error("加载协议[{}]失败:class [{}] not implement IProtocol", config.getId(), clazz.getName());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("加载协议[{}]失败", config.getId(), e);
        }
    }

    @Override
    public void reload(IProtocolConfig config) {
        IProtocol iProtocol = protocolMap.get(config.getId());
        if (iProtocol != null && iProtocol instanceof ReloadableProtocol) {
            ReloadableProtocol reloadableProtocol = (ReloadableProtocol) iProtocol;
            try {
                reloadableProtocol.reload(config);
            } catch (Exception e) {
                log.error("重载协议[{}]发生异常", config.getId(), e);
            }
        }
    }

    public void start() {
        List<IProtocolServer> servers = getServerAll();
        servers.forEach(server -> {
            try {
                server.setMessageHandler(getHandler(server.getId(), server.getType()));
                server.start();
            } catch (ProtocolException e) {
                log.error("启动接入协议[{}]发生异常", server.getId(), e);
            }
        });
        List<IProtocolClient> clients = getClientAll();
        clients.forEach(client -> {
            try {
                client.start();
            } catch (ProtocolException e) {
                log.error("启动接出协议[{}]发生异常", client.getId(), e);
            }
        });
    }

    @Override
    public ReportType getType() {
        return ReportType.PROTOCOL;
    }

    @Override
    public List<ReportData> collectData() {
        return protocolMap.values().stream().map(IProtocol::collect).collect(Collectors.toList());
    }

    public void stop() {
        List<IProtocolServer> servers = getServerAll();
        servers.forEach(server -> {
            try {
                server.stop();
            } catch (ProtocolException e) {
                log.error("停止接入协议[{}]发生异常", server.getId(), e);
            }
        });
        List<IProtocolClient> clients = getClientAll();
        clients.forEach(client -> {
            try {
                client.stop();
            } catch (ProtocolException e) {
                log.error("停止接出协议[{}]发生异常", client.getId(), e);
            }
        });
    }

    @Override
    public IProtocolClient getClient(String id) {
        return (IProtocolClient) protocolMap.get(id);
    }

    @Override
    public IProtocolServer getServer(String id) {
        return (IProtocolServer) protocolMap.get(id);
    }

    @Override
    public void registerHandler(String id, IMessageHandler handler) {
        handlers.put(id, handler);
    }

    private IMessageHandler getHandler(String key, String type) {
        if (!handlers.containsKey(key)) {
            key = "*";
        }
        return handlers.get(key);
    }

    private List<IProtocolClient> getClientAll() {
        return protocolMap.values()
                .stream()
                .filter((s) -> s instanceof IProtocolClient)
                .map((s) -> (IProtocolClient) s)
                .collect(Collectors.toList());
    }

    private List<IProtocolServer> getServerAll() {
        return protocolMap.values()
                .stream()
                .filter(s -> s instanceof IProtocolServer)
                .map((s) -> (IProtocolServer) s)
                .collect(Collectors.toList());
    }
}
