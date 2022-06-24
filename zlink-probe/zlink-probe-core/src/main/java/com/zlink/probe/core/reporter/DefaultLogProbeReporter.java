package com.zlink.probe.core.reporter;

import com.zlink.base.report.ReportData;
import com.zlink.probe.api.IProbeReporter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DefaultLogProbeReporter implements IProbeReporter {
    @Override
    public void report(List<ReportData> reportDataList) {
        reportDataList.forEach(reportData -> log.debug("{}:{}-->{}", reportData.getTimeStamp(), reportData.getType(), reportData.getDataMap()));
    }
}
