package com.argus.incidentintelligence.dto;

import java.time.Instant;
import java.util.List;

public record IncidentContext(
        MonitorContext monitor,
        IncidentFacts incident,
        AnalyticsContext analytics,
        List<HistoryPoint> responseTrend,
        List<HistoricalIncidentContext> historicalIncidents,
        List<String> historicalResolutions,
        Instant timelineSeed,
        boolean analyticsAvailable,
        boolean historyAvailable
) {
    public IncidentContext {
        responseTrend = List.copyOf(responseTrend);
        historicalIncidents = List.copyOf(historicalIncidents);
        historicalResolutions = List.copyOf(historicalResolutions);
    }
}
