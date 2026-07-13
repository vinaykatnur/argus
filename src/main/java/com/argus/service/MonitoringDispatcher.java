package com.argus.service;

import com.argus.config.MonitorProperties;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MonitoringDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MonitoringDispatcher.class);

    private final MonitoringWorker monitoringWorker;
    private final ThreadPoolExecutor executor;

    public MonitoringDispatcher(MonitorProperties monitorProperties, MonitoringWorker monitoringWorker) {
        this.monitoringWorker = monitoringWorker;
        int workerPoolSize = monitorProperties.getWorkerPoolSize();
        this.executor = new ThreadPoolExecutor(
                workerPoolSize,
                workerPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new MonitoringThreadFactory()
        );
        this.executor.prestartAllCoreThreads();
        log.info("Monitoring worker pool started with {} workers", workerPoolSize);
    }

    public void dispatch(Long monitorId) {
        executor.execute(() -> monitoringWorker.process(monitorId));
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    private static final class MonitoringThreadFactory implements ThreadFactory {

        private final AtomicInteger threadCounter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("argus-monitor-worker-" + threadCounter.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }
}
