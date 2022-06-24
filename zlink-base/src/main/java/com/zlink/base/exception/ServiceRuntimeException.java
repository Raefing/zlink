package com.zlink.base.exception;

public class ServiceRuntimeException extends ZlinkRuntimeException {

    public ServiceRuntimeException(String message) {
        super(message);
    }

    public ServiceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceRuntimeException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public ServiceRuntimeException(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, errorMsg, cause);
    }
}
