package com.zlink.channel.base;

import com.zlink.base.AbstractManager;
import com.zlink.base.IBeanFactory;
import com.zlink.base.exception.ChannelException;
import com.zlink.base.report.ReportData;
import com.zlink.base.report.ReportType;
import com.zlink.channel.api.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ChannelManager extends AbstractManager<IChannelConfig> implements IChannelManager {

    private Map<String, IChannel> channelMap = new ConcurrentHashMap<>();

    public ChannelManager(IBeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public IChannel getChannel(String channelId) {
        return channelMap.get(channelId);
    }

    @Override
    public void load(IChannelConfig o) {
        IChannel channel = new AbstractChannel.DefaultChannel();
        try {
            channel.init(o);
            SortedChannelFlow sortedFlow = new SortedChannelFlow();
            o.beforeConfig().forEach(iServiceConfig -> {
                IChannelService iService = null;
                try {
                    iService = getService(iServiceConfig);
                    if (iService instanceof IChannelBeforeService) {
                        sortedFlow.addBefore((IChannelBeforeService) iService);
                    }
                } catch (ChannelException e) {
                    e.printStackTrace();
                }
            });
            o.serviceConfig().forEach(iServiceConfig -> {
                IChannelService iService = null;
                try {
                    iService = getService(iServiceConfig);
                    sortedFlow.addService(iService);
                } catch (ChannelException e) {
                    e.printStackTrace();
                }
            });
            o.afterConfig().forEach(iServiceConfig -> {
                IChannelService iService = null;
                try {
                    iService = getService(iServiceConfig);
                    if (iService instanceof IChannelAfterService) {
                        sortedFlow.addAfter((IChannelAfterService) iService);
                    }
                } catch (ChannelException e) {
                    e.printStackTrace();
                }
            });
            channel.setFlow(sortedFlow);
            String handler = o.getExceptionHandler();
            if (StringUtils.isNotBlank(handler)) {
                IChannelExceptionHandler exceptionHandler = getHandler(handler);
                if (exceptionHandler != null) {
                    channel.setExceptionHandler(exceptionHandler);
                }
            }
            channelMap.put(channel.getId(), channel);
        } catch (ChannelException e) {
            log.error("初始化渠道[{}]失败", o.getId(), e);
        }
    }

    @Override
    public void reload(IChannelConfig o) {
        IChannel channel = channelMap.get(o.getId());
        if (channel != null && channel instanceof AbstractChannel) {
            try {
                AbstractChannel abstractChannel = (AbstractChannel) channel;
                abstractChannel.reload(o);
                log.info("重新加载渠道[{}]成功", channel.getId());
            } catch (ChannelException e) {
                log.error("重新加载渠道[{}]失败", channel.getId(), e);
            }
        }
    }

    @Override
    public void start() {
        channelMap.forEach((k, v) -> {
            try {
                v.start();
                log.info("启动渠道[{}]成功", k);
            } catch (ChannelException e) {
                log.error("启动渠道[{}]失败", k, e);
            }
        });
    }

    @Override
    public void stop() {
        channelMap.forEach((k, v) -> {
            try {
                v.stop();
                log.info("停止渠道[{}]成功", k);
            } catch (ChannelException e) {
                log.error("停止渠道[{}]失败", k, e);
            }
        });
    }


    @Override
    public ReportType getType() {
        return ReportType.CHANNEL;
    }

    @Override
    public List<ReportData> collectData() {
        return channelMap.values().stream().map(IChannel::collect).collect(Collectors.toList());
    }

    private IChannelService getService(IChannelConfig.IServiceConfig config) throws ChannelException {
        IChannelService service =  getBean(config.getService(), IChannelService.class);
        service.init(config);
        return service;
    }

    private IChannelExceptionHandler getHandler(String handler) {
        return getBean(handler, IChannelExceptionHandler.class);
    }
}
