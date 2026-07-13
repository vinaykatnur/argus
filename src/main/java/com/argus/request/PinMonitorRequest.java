package com.argus.request;

import jakarta.validation.constraints.Positive;

public record PinMonitorRequest(
        @Positive
        Integer position
) {
}
