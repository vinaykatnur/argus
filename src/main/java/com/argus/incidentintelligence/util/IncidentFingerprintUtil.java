package com.argus.incidentintelligence.util;

import com.argus.incidentintelligence.dto.HistoryPoint;
import com.argus.incidentintelligence.dto.IncidentContext;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IncidentFingerprintUtil {

    private IncidentFingerprintUtil() {
    }

    public static String failureSignature(String failureReason, Integer statusCode, List<HistoryPoint> history) {
        Integer observedStatus = statusCode != null
                ? statusCode
                : history.stream()
                .filter(point -> !point.successful())
                .map(HistoryPoint::httpStatusCode)
                .filter(code -> code != null)
                .findFirst()
                .orElse(null);
        String observedReason = firstText(failureReason, history.stream()
                .filter(point -> !point.successful())
                .map(HistoryPoint::failureReason)
                .filter(IncidentFingerprintUtil::hasText)
                .findFirst()
                .orElse(null));

        if (observedStatus != null) {
            if (observedStatus >= 500) {
                return "HTTP_5XX";
            }
            if (observedStatus >= 400) {
                return "HTTP_4XX";
            }
            return "HTTP_" + observedStatus;
        }

        String reason = observedReason == null ? "" : observedReason.toLowerCase(Locale.ROOT);
        if (reason.contains("http 500") || reason.contains("http 5xx") || reason.contains("http 5")) {
            return "HTTP_5XX";
        }
        if (reason.contains("http 400") || reason.contains("http 4xx") || reason.contains("http 4")) {
            return "HTTP_4XX";
        }
        if (reason.contains("timeout") || reason.contains("timed out")) {
            return "TIMEOUT";
        }
        if (reason.contains("ssl") || reason.contains("certificate") || reason.contains("tls")) {
            return "SSL_FAILURE";
        }
        if (reason.contains("dns") || reason.contains("unknown host")) {
            return "DNS_FAILURE";
        }
        if (reason.contains("connection refused")) {
            return "CONNECTION_REFUSED";
        }
        if (hasText(observedReason)) {
            return "MONITOR_FAILURE";
        }
        return "UNKNOWN_FAILURE";
    }

    public static String currentFailureSignature(IncidentContext context) {
        return failureSignature(context.incident().failureReason(), null, context.responseTrend());
    }

    public static String trendSignature(List<HistoryPoint> history) {
        List<Long> responseTimes = history.stream()
                .filter(HistoryPoint::successful)
                .map(HistoryPoint::responseTimeMillis)
                .filter(time -> time != null && time > 0)
                .toList();
        long failedChecks = history.stream().filter(point -> !point.successful()).count();

        if (failedChecks > 0 && responseTimes.isEmpty()) {
            return "IMMEDIATE_FAILURE";
        }
        if (responseTimes.size() < 3) {
            return failedChecks > 0 ? "LIMITED_HISTORY_WITH_FAILURES" : "LIMITED_HISTORY";
        }

        long first = responseTimes.getFirst();
        long middle = responseTimes.get(responseTimes.size() / 2);
        long last = responseTimes.getLast();
        if (last > first * 2 && last > middle) {
            return "GRADUAL_INCREASE";
        }
        if (last > first * 3) {
            return "SPIKE";
        }
        if (failedChecks > 0) {
            return "DEGRADATION_TO_FAILURE";
        }
        return "NORMAL";
    }

    private static String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
