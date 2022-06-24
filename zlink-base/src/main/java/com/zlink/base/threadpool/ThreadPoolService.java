package com.zlink.base.threadpool;

import com.zlink.base.AbstractManager;
import com.zlink.base.service.CloseableService;
import com.zlink.base.service.ReloadableService;
import com.zlink.base.report.ReportData;
import com.zlink.base.report.ReportType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolService extends AbstractManager<ThreadPoolConfig> implements CloseableService , ReloadableService<ThreadPoolConfig> {

    private Map<String, ThreadPoolExecutor> executorServiceMap = new HashMap<>();

    public ThreadPoolService() {
        super(null);
    }

    @Override
    public void load(ThreadPoolConfig target) {
        ThreadPoolExecutor threadPoolExecutor = null;
        if (target.isSingle()) {
            threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), (r) -> {
                Thread thread = new Thread(r, target.getName());
                thread.setDaemon(target.isScheduled());
                return thread;
            });
        } else {
            ThreadFactory threadFactory = new ThreadFactory() {
                private String prefix = target.getName() + "-";
                private AtomicInteger atomicInteger = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, prefix + atomicInteger.incrementAndGet());
                    thread.setDaemon(target.isScheduled());
                    return thread;
                }
            };
            if (target.isScheduled()) {
                threadPoolExecutor = new ScheduledThreadPoolExecutor(target.getCore(), threadFactory);
            } else {
                threadPoolExecutor = new ThreadPoolExecutor(target.getCore(), target.getMax(), target.getIdle(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
            }
        }
        if (threadPoolExecutor != null) {
            executorServiceMap.put(target.getName(), threadPoolExecutor);
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        executorServiceMap.forEach((k, v) -> {
            v.shutdown();
        });
    }

    @Override
    public void reload(ThreadPoolConfig target) {
        if (executorServiceMap.containsKey(target.getName())) {
            executorServiceMap.remove(target.getName()).shutdown();
            load(target);
        }
    }

    public ExecutorService getExecutorService(String name) {
        return executorServiceMap.get(name);
    }

    public ScheduledExecutorService getScheduledExecutorService(String name) {
        if (executorServiceMap.get(name) instanceof ScheduledExecutorService) {
            return (ScheduledExecutorService) executorServiceMap.get(name);
        }
        return null;
    }

    @Override
    public ReportType getType() {
        return ReportType.THREAD_POOL;
    }

    @Override
    public List<ReportData> collectData() {
        List<ReportData> reportDataList = new ArrayList<>();
        executorServiceMap.forEach((k, v) -> {
            ReportData data = ReportData.build(ReportType.THREAD_POOL);
            data.addData("id", k);
            data.addData("active", v.getActiveCount());
            data.addData("core", v.getCorePoolSize());
            data.addData("max", v.getMaximumPoolSize());
            reportDataList.add(data);
        });
        return reportDataList;
    }
}
