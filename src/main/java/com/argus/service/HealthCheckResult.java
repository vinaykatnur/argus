package com.argus.service;

public record HealthCheckResult(
        boolean successful,
        Integer httpStatusCode,
        Long responseTimeMillis,
        String failureReason
) {
}
