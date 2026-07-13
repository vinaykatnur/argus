package com.argus.response;

public record DashboardMessagesResponse(
        String noIncidents,
        String noAlerts,
        String noDownMonitors
) {

    public static DashboardMessagesResponse defaults() {
        return new DashboardMessagesResponse(
                "No incidents yet. Your monitors are currently quiet.",
                "No alerts require your attention.",
                "No monitors are down."
        );
    }
}
