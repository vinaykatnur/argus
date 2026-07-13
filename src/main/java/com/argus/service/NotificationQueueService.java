package com.argus.service;

import java.time.Instant;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueueService {

    private final LinkedBlockingQueue<NotificationJob> mainQueue = new LinkedBlockingQueue<>();
    private final DelayQueue<RetryNotificationJob> retryQueue = new DelayQueue<>();

    public void enqueue(Long deliveryId) {
        mainQueue.offer(new NotificationJob(deliveryId));
    }

    public void enqueueRetry(Long deliveryId, Instant availableAt) {
        retryQueue.offer(new RetryNotificationJob(deliveryId, availableAt));
    }

    public NotificationJob take() throws InterruptedException {
        return mainQueue.take();
    }

    public NotificationJob poll() {
        return mainQueue.poll();
    }

    public RetryNotificationJob takeRetry() throws InterruptedException {
        return retryQueue.take();
    }

    public RetryNotificationJob pollRetry() {
        return retryQueue.poll();
    }

    public int mainQueueSize() {
        return mainQueue.size();
    }

    public int retryQueueSize() {
        return retryQueue.size();
    }
}
