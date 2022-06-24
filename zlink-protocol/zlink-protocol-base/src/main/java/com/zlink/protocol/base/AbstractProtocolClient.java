package com.zlink.protocol.base;

import com.zlink.protocol.api.IProtocolClient;
import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.api.ext.BalanceableClient;
import com.zlink.protocol.api.ext.ReloadableProtocol;
import com.zlink.protocol.exception.ProtocolException;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class AbstractProtocolClient<T, B> extends AbstractProtocol implements IProtocolClient<T>, BalanceableClient, ReloadableProtocol {
    protected String detectType;
    protected String detectMethod;
    protected String detectPath;
    protected String detect;
    protected String detectRet;
    protected int detectInterval;
    @Getter
    private List<B> targetUrls = new ArrayList<>();
    protected BaseLoadbalancer<B> loadbalancer;
    private ScheduledExecutorService executorService;
    private Map<String, BaseClientDetector> detectorMap = new HashMap<>();

    @Override
    public String getType() {
        return type.name();
    }

    @Override
    public void setLoadBalancer(String loadBalancePolicy) {
        this.loadbalancer.setLoadBalance(loadBalancePolicy);
    }

    @Override
    public void initProtocol(IProtocolConfig protocolConfig) {
        this.detectType = protocolConfig.getAttachedParam("detectType", String.class);
        this.detectMethod = protocolConfig.getAttachedParam("detectMethod", String.class);
        this.detectPath = protocolConfig.getAttachedParam("detectPath", String.class);
        this.detect = protocolConfig.getAttachedParam("detect", String.class);
        this.detectRet = protocolConfig.getAttachedParam("detectRet", String.class);
        this.detectInterval = protocolConfig.getAttachedParam("detectInterval", Integer.class, 10000);
        String loadBalance = protocolConfig.getAttachedParam("loadBalance", String.class, "fast");
        this.loadbalancer = new BaseLoadbalancer<>(id, loadBalance);
        initClient(protocolConfig);
        this.targetUrls = initProtocolTarget();
        this.loadbalancer.init(targetUrls.stream().collect(Collectors.toMap(B::toString, b -> b)));
        if (action == ProtocolAction.SHORT) {
            executorService = Executors.newScheduledThreadPool(targetUrls.size() + 1);
            targetUrls.forEach((b) -> {
                DetectorConfig config = new DetectorConfig();
                config.detectMsg(detect)
                        .detectRetMsg(detectRet)
                        .protocolType(type)
                        .detectInterval(detectInterval)
                        .daemon(true)
                        .encoding(remoteCharset)
                        .detectType(detectType == null ? DetectorConfig.DetectType.NONE : DetectorConfig.DetectType.valueOf(detectType))
                        .method(detectMethod)
                        .path(detectPath);
                if (b instanceof URL) {
                    URL url = (URL) b;
                    config.host(url.getHost()).port(url.getPort());
                } else {
                    String addr = b.toString();
                    if (addr.indexOf(":") != -1) {
                        String host = addr.split(":")[0];
                        int port = Integer.parseInt(addr.split(":")[1]);
                        config.host(host).port(port);
                    } else {
                        config = null;
                    }
                }
                if (config != null && config.detectType() != DetectorConfig.DetectType.NONE) {
                    detectorMap.put(b.toString(), new BaseClientDetector(config));
                }
            });
        }
    }

    public T send(T data) {
        B target = loadbalancer.loadBalance();
        try {
            collector.send(msgSize(data));
            T ret = send(target, data);
            collector.recv(msgSize(data));
            collector.success();
            return ret;
        } catch (Exception e) {
            collector.error();
            throw e;
        }
    }

    public abstract int msgSize(T msg);

    public void startProtocol() throws ProtocolException {
        startClient();
        if (executorService != null) {
            executorService.scheduleAtFixedRate(() -> {
                detectorMap.forEach((key, value) -> {
                    executorService.execute(() -> {
                        loadbalancer.recordStatus(key, value.detect());
                    });
                });
            }, detectInterval, detectInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void stopProtocol() throws ProtocolException {
        stopClient();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public abstract T send(B target, T data);

    public abstract List<B> initProtocolTarget();

    public abstract void initClient(IProtocolConfig config);

    public abstract void startClient() throws ProtocolException;

    public abstract void stopClient() throws ProtocolException;

    public abstract void reConnect(B target) throws ProtocolException;

}
