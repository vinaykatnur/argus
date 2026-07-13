package com.argus.service;

import com.argus.config.NotificationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorker.class);

    private final NotificationQueueService notificationQueueService;
    private final NotificationDeliveryService notificationDeliveryService;
    private final NotificationProperties notificationProperties;
    private ExecutorService executorService;

    public NotificationWorker(
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
        executorService = Executors.newFixedThreadPool(notificationProperties.getWorkerPoolSize());
        for (int i = 0; i < notificationProperties.getWorkerPoolSize(); i++) {
            executorService.submit(this::runMainLoop);
        }
    }

    @PreDestroy
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Async
    public void runMainLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                NotificationJob job = notificationQueueService.poll();
                if (job == null) {
                    Thread.sleep(200);
                    continue;
                }
                notificationDeliveryService.processDelivery(job.deliveryId());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException exception) {
                log.warn("Notification worker loop failed", exception);
            }
        }
    }
}
