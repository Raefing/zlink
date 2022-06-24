package com.zlink.protocol.base;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class DetectorConfig {
    /**
     * Http,TCP
     */
    private ProtocolType protocolType;
    /**
     *
     */
    private ProtocolMode mode;
    /**
     * MSG,CONN,BUSI
     */
    private DetectType detectType = DetectType.NONE;
    /**
     * 主机
     */
    private String host;
    /**
     * 端口
     */
    private int port;
    /**
     * path http专属
     */
    private String path = "/detect";
    /**
     * method http专属
     */
    private String method = "GET";
    /**
     * 测试报文 MSG模式
     */
    private String detectMsg = "DETECT";
    /**
     * 测试返回报文 MSG模式
     */
    private String detectRetMsg = "SUCCESS";
    /**
     *
     */
    private String encoding = "UTF-8";
    /**
     * 测试间隔 ms
     */
    private int detectInterval = 5000;
    /**
     *
     */
    private boolean daemon = true;


    public DetectorConfig detectInterval(int detectInterval) {
        //Assert.isTrue(detectInterval > 0, "Detect Interval must be lage 0");
        this.detectInterval = detectInterval;
        return this;
    }

    public static DetectorConfig http(String ip, int port) {
        DetectorConfig detectorConfig = new DetectorConfig();
        detectorConfig.protocolType(ProtocolType.HTTP)
                .host(ip)
                .port(port);
        return detectorConfig;
    }

    public static DetectorConfig tcp(String ip, int port) {
        DetectorConfig detectorConfig = new DetectorConfig();
        detectorConfig.protocolType(ProtocolType.TCP)
                .host(ip)
                .port(port);
        return detectorConfig;
    }

    public enum DetectType {
        CONN,
        MSG,
        NONE,
    }
}
