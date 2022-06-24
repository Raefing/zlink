package com.zlink.probe.core;

import com.zlink.base.service.CloseableService;
import com.zlink.base.report.IReporter;
import com.zlink.base.report.ProbePointLeader;
import com.zlink.base.report.ReportData;
import com.zlink.base.report.ReportType;
import com.zlink.probe.api.IProbeReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReportService implements IReporter, CloseableService {

    private static final long DEFAULT_DELAY = 5000;
    private Map<ReportType, ProbePointLeader> handle = new HashMap<>();
    private Map<ReportType, Long> delayMap;
    private ScheduledExecutorService executorService;
    private IProbeReporter iProbeReporter;

    public ReportService(ScheduledExecutorService executorService, IProbeReporter iProbeReporter, Map<ReportType, Long> delayMap) {
        this.executorService = executorService;
        this.iProbeReporter = iProbeReporter;
        this.delayMap = delayMap;
    }

    @Override
    public void report(List<ReportData> data) {
        iProbeReporter.report(data);
    }

    @Override
    public void report(ProbePointLeader leader) {
        handle.put(leader.getType(), leader);
    }


    public void start() {
        executorService.scheduleAtFixedRate(() -> {
            handle.forEach((k, v) -> {
                if (v.getStatus() == 0) {
                    executorService.scheduleAtFixedRate(() -> report(v.collectData()), getDelay(k), getDelay(k), TimeUnit.MILLISECONDS);
                    v.setStatus(1);
                }
            });
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        handle.forEach((k, v) -> v.setStatus(0));
        executorService.shutdown();
    }

    private long getDelay(ReportType type) {
        if (delayMap.containsKey(type)) {
            return delayMap.get(type);
        } else {
            return DEFAULT_DELAY;
        }
    }

}
