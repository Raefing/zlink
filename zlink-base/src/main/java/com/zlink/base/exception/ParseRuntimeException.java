package com.zlink.base.exception;

public class ParseRuntimeException extends ZlinkRuntimeException {
    public ParseRuntimeException(String message) {
        super(message);
    }

    public ParseRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseRuntimeException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public ParseRuntimeException(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, errorMsg, cause);
    }
}
