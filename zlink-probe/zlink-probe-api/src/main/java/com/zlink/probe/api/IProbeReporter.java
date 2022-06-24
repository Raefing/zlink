package com.zlink.probe.api;

import com.zlink.base.report.ReportData;
import com.zlink.base.report.ReportType;

import java.util.List;

@FunctionalInterface
public interface IProbeReporter {

    void report(List<ReportData> reportDataList);

}
