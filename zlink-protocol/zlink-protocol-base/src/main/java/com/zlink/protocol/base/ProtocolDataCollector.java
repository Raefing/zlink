package com.zlink.protocol.base;

import com.zlink.base.report.ReportData;
import com.zlink.base.report.ReportType;

import java.util.HashMap;
import java.util.Map;

public class ProtocolDataCollector {
    private String id;
    private ProtocolType type;
    private ProtocolSide side;
    private String name;
    private String desc;
    private long startTimeStamp;
    private long endTimeStamp;
    private int status;
    private long send;
    private long sendSize;
    private long recv;
    private long recvSize;
    private long success;
    private long error;
    private int activeCon;
    private Map<String, Integer> targetStatus = new HashMap<>();

    public ProtocolDataCollector(String id, ProtocolType type, String name, String desc, ProtocolSide side) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.side = side;
        this.startTimeStamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public ProtocolType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }


    public long getEndTimeStamp() {
        return endTimeStamp;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getSend() {
        return send;
    }

    public void send(long sendSize) {
        this.send += 1;
        this.sendSize += sendSize;
    }

    public long getSendSize() {
        return sendSize;
    }


    public long getRecv() {
        return recv;
    }

    public void recv(long recv) {
        this.recv += 1;
        this.recvSize += recv;
    }

    public long getRecvSize() {
        return recvSize;
    }


    public long getSuccess() {
        return success;
    }

    public void success() {
        this.success += 1;
    }

    public long getError() {
        return error;
    }

    public void error() {
        this.error += 1;
    }

    public int getActiveCon() {
        return activeCon;
    }

    public void active() {
        this.activeCon += 1;
    }

    public void inactive() {
        this.activeCon -= 1;
    }

    public Map<String, Integer> getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String target, int status) {
        this.targetStatus.put(target, status);
    }

    public ReportData convert() {
        ReportData reportData = ReportData.build(ReportType.PROTOCOL);
        /**
         *
         */
        endTimeStamp = System.currentTimeMillis();
        reportData.addData("id", id);
        reportData.addData("name", name);
        reportData.addData("type", type.name());
        reportData.addData("side", side.name());
        reportData.addData("desc", desc);
        reportData.addData("start", startTimeStamp);
        reportData.addData("end", endTimeStamp);
        reportData.addData("send", send);
        reportData.addData("sendSize", sendSize);
        reportData.addData("recv", recv);
        reportData.addData("recvSize", recvSize);
        reportData.addData("success", success);
        reportData.addData("error", error);
        reportData.addData("status", status);
        if (side == ProtocolSide.SERVER) {
            reportData.addData("active", activeCon);
        }
        if (side == ProtocolSide.CLIENT) {
            reportData.addData("target", targetStatus);
        }
        this.recv = 0;
        this.recvSize = 0;
        this.send = 0;
        this.sendSize = 0;
        this.error = 0;
        this.success = 0;
        /**
         *
         */
        startTimeStamp = endTimeStamp;
        return reportData;
    }
}
