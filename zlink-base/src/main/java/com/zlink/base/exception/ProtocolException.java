package com.zlink.base.exception;

public class ProtocolException extends ZlinkException {
    public ProtocolException() {
        super();
    }

    public ProtocolException(String msg) {
        super(msg);
    }


    public ProtocolException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
