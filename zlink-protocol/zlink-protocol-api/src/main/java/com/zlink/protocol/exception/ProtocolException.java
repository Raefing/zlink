package com.zlink.protocol.exception;

public class ProtocolException extends Exception {
    public ProtocolException(String msg) {
        super(msg);
    }

    public ProtocolException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
