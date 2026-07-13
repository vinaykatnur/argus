package com.argus.response;

import java.time.Instant;
import java.util.List;

public record RuntimeDiagnosticsResponse(
        String status,
        Instant generatedAt,
        String environment,
        String applicationVersion,
        String javaVersion,
        String uptime,
        long uptimeMillis,
        RuntimeUsage memory,
        RuntimeUsage disk,
        RuntimeUsage cpu,
        List<ServiceHealth> services
) {

    public record RuntimeUsage(
            String status,
            String value,
            Long used,
            Long total,
            Double percentage
    ) {
    }

    public record ServiceHealth(
            String name,
            String status,
            String detail,
            Long responseTimeMillis
    ) {
    }
}
