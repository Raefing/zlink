package com.zlink.channel.base;

import com.zlink.base.report.ProbePoint;
import com.zlink.base.report.ReportData;
import com.zlink.base.report.ReportType;

public class ChannelItemReportCollector implements ProbePoint {
    private long start;
    private long end;
    private int call;
    private int success;
    private int error;

    public ChannelItemReportCollector() {
        this.start = System.currentTimeMillis();
    }

    @Override
    public ReportData collect() {
        this.end = System.currentTimeMillis();
        ReportData data = ReportData.build(ReportType.CHANNEL);
        data.addData("start", start);
        data.addData("end", end);
        data.addData("call", call);
        data.addData("success", success);
        data.addData("error", error);

        this.start = end;
        return data;
    }

    public void start(String key) {
        call++;
    }

    public void end(String key, boolean status) {
        if (status) {
            success++;
        } else {
            error++;
        }
    }

    public void startService(String key, String id) {

    }

    public void endService(String key, String id, boolean status) {

    }

}
