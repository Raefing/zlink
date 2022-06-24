package com.zlink.base.threadpool;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ThreadPoolConfig {
    private String name;
    private boolean single;
    private boolean scheduled;
    private int core;
    private int max;
    private long idle;
}
