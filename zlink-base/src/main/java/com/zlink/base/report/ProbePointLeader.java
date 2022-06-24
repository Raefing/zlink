package com.zlink.base.report;

import java.util.List;

public interface ProbePointLeader {
    ReportType getType();

    List<ReportData> collectData();

    int getStatus();

    void setStatus(int status);
}
