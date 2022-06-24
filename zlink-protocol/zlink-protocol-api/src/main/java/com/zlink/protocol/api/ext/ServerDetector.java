package com.zlink.protocol.api.ext;

public interface ServerDetector {
    boolean isDetect(String msg);

    String detect(String detectRequest);
}
