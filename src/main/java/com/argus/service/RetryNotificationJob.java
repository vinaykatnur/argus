package com.argus.service;

import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public record RetryNotificationJob(Long deliveryId, Instant availableAt) implements Delayed {

    @Override
    public long getDelay(TimeUnit unit) {
        long delayMillis = availableAt.toEpochMilli() - Instant.now().toEpochMilli();
        return unit.convert(delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(
                getDelay(TimeUnit.MILLISECONDS),
                other.getDelay(TimeUnit.MILLISECONDS)
        );
    }
}
