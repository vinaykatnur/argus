package com.argus.service;

import com.argus.config.NotificationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RetryNotificationWorker {

    private static final Logger log = LoggerFactory.getLogger(RetryNotificationWorker.class);

    private final NotificationQueueService notificationQueueService;
    private final NotificationDeliveryService notificationDeliveryService;
    private final NotificationProperties notificationProperties;
    private ExecutorService executorService;

    public RetryNotificationWorker(
            NotificationQueueService notificationQueueService,
            NotificationDeliveryService notificationDeliveryService,
            NotificationProperties notificationProperties
    ) {
        this.notificationQueueService = notificationQueueService;
        this.notificationDeliveryService = notificationDeliveryService;
        this.notificationProperties = notificationProperties;
    }

    @PostConstruct
    public void start() {
        executorService = Executors.newFixedThreadPool(notificationProperties.getRetryWorkerPoolSize());
        for (int i = 0; i < notificationProperties.getRetryWorkerPoolSize(); i++) {
            executorService.submit(this::runRetryLoop);
        }
    }

    @PreDestroy
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public void runRetryLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                RetryNotificationJob job = notificationQueueService.pollRetry();
                if (job == null) {
                    Thread.sleep(200);
                    continue;
                }
                notificationDeliveryService.retryPendingDeliveries();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException exception) {
                log.warn("Retry worker loop failed", exception);
            }
        }
    }
}
