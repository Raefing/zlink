package com.zlink.base.exception;

public class ProtocolRuntimeException extends ZlinkRuntimeException {
    public ProtocolRuntimeException(String message) {
        super(message);
    }

    public ProtocolRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolRuntimeException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public ProtocolRuntimeException(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, errorMsg, cause);
    }
}
