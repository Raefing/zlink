package com.zlink.base.exception;

public class ChannelException extends ZlinkException {

    public ChannelException() {
        super();
    }

    public ChannelException(String msg) {
        super(msg);
    }


    public ChannelException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
