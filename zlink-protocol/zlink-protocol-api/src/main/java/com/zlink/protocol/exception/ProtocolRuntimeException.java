package com.zlink.protocol.exception;

public class ProtocolRuntimeException extends RuntimeException {
    public ProtocolRuntimeException(String msg){
        super(msg);
    }

    public ProtocolRuntimeException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
