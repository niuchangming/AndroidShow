package com.luck.picture.lib.utils;


import android.support.v4.util.ArrayMap;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/7/2
 * @description 定时任务的工具类
 */
public class ScheduledExecutorManager {

    private ArrayMap<Runnable, ScheduledFuture> futureMap = new ArrayMap<>();
    private ScheduledThreadPoolExecutor executorService = null;

    private ScheduledExecutorManager(){
        executorService = new ScheduledThreadPoolExecutor(10, new DefaultThreadFactory());
    }

    private static class SingletonHolder {
        private static ScheduledExecutorManager instance = new ScheduledExecutorManager();
    }

    public static ScheduledExecutorManager getInstance(){
        return SingletonHolder.instance;
    }

    public void schedule(Runnable runnable, long initialDelay, long periodInMills) {
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(runnable, initialDelay, periodInMills, TimeUnit.MILLISECONDS);
        futureMap.put(runnable, future);
    }

    public void cancelSchedule(Runnable runnable) {
        ScheduledFuture scheduledFuture = futureMap.get(runnable);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            futureMap.remove(runnable);
        }
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-show-scheduled-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
