package com.argus.response;

import java.time.Instant;

public record AnalyticsTrendPointResponse(
        Instant timestamp,
        double value
) {
}
