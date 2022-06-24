package com.zlink.base.exception;

public class PackRuntimeException extends ZlinkRuntimeException {
    public PackRuntimeException(String message) {
        super(message);
    }

    public PackRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PackRuntimeException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public PackRuntimeException(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, errorMsg, cause);
    }
}
