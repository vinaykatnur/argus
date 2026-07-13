package com.argus.response;

import java.time.Instant;

public record AnalyticsReportResponse(
        Long requestId,
        String status,
        String warningMessage,
        Instant requestedAt,
        Instant completedAt,
        String resultJson
) {
}
