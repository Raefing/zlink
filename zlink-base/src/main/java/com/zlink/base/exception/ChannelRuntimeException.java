package com.zlink.base.exception;

public class ChannelRuntimeException extends ZlinkRuntimeException {
    public ChannelRuntimeException(String message) {
        super(message);
    }

    public ChannelRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelRuntimeException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public ChannelRuntimeException(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, errorMsg, cause);
    }
}
