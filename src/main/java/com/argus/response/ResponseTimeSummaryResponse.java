package com.argus.response;

public record ResponseTimeSummaryResponse(
        Long currentResponseTimeMillis,
        Long averageResponseTimeMillis,
        Long fastestResponseTimeMillis,
        Long slowestResponseTimeMillis
) {
}
