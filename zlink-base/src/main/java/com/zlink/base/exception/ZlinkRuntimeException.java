package com.zlink.base.exception;

public class ZlinkRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String errCode;
    private String errMessage;

    public ZlinkRuntimeException(String message) {
        super(message);
        this.errMessage = message;
    }

    public ZlinkRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.errMessage = message;
    }

    public ZlinkRuntimeException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errCode = errorCode;
        this.errMessage = errorMsg;
    }

    public ZlinkRuntimeException(String errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errCode = errorCode;
        this.errMessage = errorMsg;

    }

    public String getErrCode() {
        return this.errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BranchRuntimeException{");
        sb.append("errCode='").append(errCode).append('\'');
        sb.append(", errMessage='").append(errMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
