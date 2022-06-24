package com.zlink.protocol.base;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageSyncer<T> {
    private Map<String, T> messagePool = new HashMap<>();
    private Map<String, CountDownLatch> countDowns = new HashMap<>();

    private static MessageSyncer syncer = new MessageSyncer();

    private MessageSyncer() {
    }

    public static MessageSyncer getInstance() {
        return syncer;
    }

    public T get(String key, int timeOut) throws InterruptedException, TimeoutException {
        CountDownLatch countDownLatch = countDowns.get(key);
        if(countDownLatch != null) {
            boolean isDown = countDownLatch.await(timeOut, TimeUnit.MILLISECONDS);
            try {
                if (isDown) {
                    return messagePool.get(key);
                } else {
                    throw new TimeoutException("读取返回报文超时");
                }
            } finally {
                countDowns.remove(key);
                messagePool.remove(key);
            }
        }
        return null;
    }

    public void markFlag(String key) {
        countDowns.put(key, new CountDownLatch(1));
    }

    public void removeFlag(String key, T obj) {
        if (countDowns.containsKey(key)) {
            CountDownLatch countDownLatch = countDowns.get(key);
            countDownLatch.countDown();
        }
        messagePool.put(key, obj);
    }
}
