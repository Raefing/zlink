package com.zlink.base.exception;

public class ZlinkException extends Exception {

    public ZlinkException() {
        super();
    }

    public ZlinkException(String msg) {
        super(msg);
    }


    public ZlinkException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
