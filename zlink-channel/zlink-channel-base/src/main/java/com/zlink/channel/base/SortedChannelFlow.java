package com.zlink.channel.base;

import com.zlink.channel.api.IChannelAfterService;
import com.zlink.channel.api.IChannelBeforeService;
import com.zlink.channel.api.IChannelFlow;
import com.zlink.channel.api.IChannelService;
import com.zlink.channel.api.ext.Orderable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortedChannelFlow implements IChannelFlow {
    private List<IChannelBeforeService> beforeServices = new ArrayList<>();
    private List<IChannelAfterService> afterServices = new ArrayList<>();
    private List<IChannelService> services = new ArrayList<>();

    public SortedChannelFlow() {
        beforeServices.sort(new InnerComparator());
        afterServices.sort(new InnerComparator());
    }

    public void addBefore(IChannelBeforeService beforeService) {
        this.beforeServices.add(beforeService);
    }

    public void addService(IChannelService service) {
        this.services.add(service);
    }

    public void addAfter(IChannelAfterService afterService) {
        this.afterServices.add(afterService);
    }

    @Override
    public List<IChannelBeforeService> beforeServices() {
        return beforeServices;
    }

    @Override
    public List<IChannelService> service() {
        return services;
    }

    @Override
    public List<IChannelAfterService> afterServices() {
        return afterServices;
    }

    class InnerComparator implements Comparator<Orderable> {
        @Override
        public int compare(Orderable o1, Orderable o2) {
            return o1.order() - o2.order();
        }
    }
}
