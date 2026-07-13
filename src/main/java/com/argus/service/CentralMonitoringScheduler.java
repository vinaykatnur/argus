package com.argus.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CentralMonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(CentralMonitoringScheduler.class);

    private final MonitorSchedulingService monitorSchedulingService;
    private final MonitoringDispatcher monitoringDispatcher;

    public CentralMonitoringScheduler(
            MonitorSchedulingService monitorSchedulingService,
            MonitoringDispatcher monitoringDispatcher
    ) {
        this.monitorSchedulingService = monitorSchedulingService;
        this.monitoringDispatcher = monitoringDispatcher;
    }

    @Scheduled(fixedDelayString = "${argus.monitoring.scheduler-fixed-delay-millis}")
    public void dispatchDueMonitors() {
        List<Long> monitorIds = monitorSchedulingService.claimDueMonitorIds();
        if (monitorIds.isEmpty()) {
            return;
        }

        log.info("Dispatching {} due monitors", monitorIds.size());
        monitorIds.forEach(monitoringDispatcher::dispatch);
    }
}
