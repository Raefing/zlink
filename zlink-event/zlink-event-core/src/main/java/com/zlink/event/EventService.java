package com.zlink.event;


import com.zlink.base.service.CloseableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class EventService implements IEventService, CloseableService {
    private int queueSize;
    private Map<EventType, Queue<IEvent>> queueMap = new ConcurrentHashMap<>();
    private Map<EventType, Map<String, List<IEventListener>>> listeners = new ConcurrentHashMap();
    private ScheduledExecutorService executorService;

    public EventService(ScheduledExecutorService threadPoolExecutor, int queueSize) {
        this.executorService = threadPoolExecutor;
        this.queueSize = queueSize;
    }

    @Override
    public void addEventListener(IEventListener listener) {
        addEventListener(EventType.ALL_EVENT, listener);
    }

    @Override
    public void addEventListener(EventType type, IEventListener listener) {
        addEventListener(type, "*", listener);
    }

    @Override
    public void addEventListener(EventType type, String tag, IEventListener listener) {
        Map<String, List<IEventListener>> listenerMap;
        if (listeners.containsKey(type)) {
            listenerMap = listeners.get(type);
        } else {
            listenerMap = new ConcurrentHashMap<>();
            listeners.put(type, listenerMap);
        }
        List<IEventListener> list;
        if (listenerMap.containsKey(tag)) {
            list = listenerMap.get(tag);
        } else {
            list = new ArrayList<>();
            listenerMap.put(tag, list);
        }
        list.add(listener);
    }

    @Override
    public void pubEvent(IEvent event) {
        EventType type = event.getType();
        Queue queue;
        if (queueMap.containsKey(type)) {
            queue = queueMap.get(type);
        } else {
            queue = new ArrayBlockingQueue(queueSize);
            queueMap.put(type, queue);
        }
        queue.offer(event);
    }

    public void start() {
        queueMap.forEach((key, queue) -> {
            executorService.scheduleWithFixedDelay(() -> {
                IEvent event = queue.peek();
                dispatchEvent(event);
            }, 1000, 1, TimeUnit.MILLISECONDS);
        });
    }

    public void stop() {
        executorService.shutdown();
    }

    private void dispatchEvent(IEvent event) {
        EventType type = event.getType();
        String tag = event.getTag();
        if (listeners.containsKey(type)) {
            Map<String, List<IEventListener>> map = listeners.get(type);
            map.forEach((key, list) -> {
                if (isMatch(tag, key)) {
                    list.forEach(listener -> listener.onEvent(event));
                }
            });
        }
    }

    private boolean isMatch(String tag, String reg) {
        Pattern pattern = Pattern.compile(reg.replaceAll("\\*", ".\\*"));
        return pattern.matcher(tag).matches();
    }
}
