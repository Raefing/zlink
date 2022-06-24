package com.zlink.protocol.base;

import com.zlink.protocol.api.ext.ServerDetector;

public class BaseServerDetector implements ServerDetector {
    private String detect;
    private String detectRet;

    public BaseServerDetector(String detect, String detectRet) {
        this.detect = detect;
        this.detectRet = detectRet;
    }

    @Override
    public boolean isDetect(String msg) {
        if (detect == null) {
            return false;
        } else {
            return detect.equals(msg);
        }
    }

    @Override
    public String detect(String detectRequest) {
        return detectRet;
    }
}
