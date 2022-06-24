package com.zlink.protocol.api.ext;

public interface ClientDetector {
    /**
     * 探测
     *
     * @return
     */
    boolean detect();

    /**
     *
     */
    void start();

    /**
     * 销毁
     */
    void stop();

}

