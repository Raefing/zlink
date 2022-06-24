package com.zlink.pipeline.api;

import lombok.Data;

@Data
public final class ContextStatus {
    /**
     * 管道激活
     */
    public static final ContextStatus ACTIVE = new ContextStatus(0);

    /**
     * 入栈
     */
    public static final ContextStatus IN_BOUND = new ContextStatus(1);

    /**
     * 入栈完成
     */
    public static final ContextStatus IN_COMPLETE = new ContextStatus(2);

    /**
     * 出栈
     */
    public static final ContextStatus OUT_BOUND = new ContextStatus(3);

    /**
     * 出栈完成
     */
    public static final ContextStatus OUT_COMPLETE = new ContextStatus(4);


    /**
     * 异常
     */
    public static final ContextStatus EXCEPTION = new ContextStatus(5);

    /**
     * 异常处理完毕
     */
    public static final ContextStatus EXCEPTION_COMPLETE = new ContextStatus(6);
    /**
     * 管道关闭
     */
    public static final ContextStatus INACTIVE = new ContextStatus(7);
    /**
     *
     */
    private static final ContextStatus STOP = new ContextStatus(8);

    private int status;

    private ContextStatus(int i) {
        status = i;
    }

    public int value() {
        return status;
    }

}
