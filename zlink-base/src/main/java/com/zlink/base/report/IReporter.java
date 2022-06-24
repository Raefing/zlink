package com.zlink.base.report;


import java.util.List;

public interface IReporter {
    /**
     * 上报数据
     *
     * @param datas
     */
    void report(List<ReportData> datas);

    /**
     * 放置上报句柄
     */
    void report(ProbePointLeader leader);

    /**
     * 开启定时收集
     */
    void start();
}
