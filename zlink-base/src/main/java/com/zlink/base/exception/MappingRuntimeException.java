package com.zlink.base.exception;

public class MappingRuntimeException extends ZlinkRuntimeException {
    public MappingRuntimeException(String message) {
        super(message);
    }

    public MappingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MappingRuntimeException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public MappingRuntimeException(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, errorMsg, cause);
    }
}
