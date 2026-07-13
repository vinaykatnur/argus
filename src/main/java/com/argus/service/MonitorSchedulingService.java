package com.argus.service;

import com.argus.config.MonitorProperties;
import com.argus.entity.Monitor;
import com.argus.repository.MonitorRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitorSchedulingService {

    private final MonitorRepository monitorRepository;
    private final MonitorProperties monitorProperties;

    public MonitorSchedulingService(MonitorRepository monitorRepository, MonitorProperties monitorProperties) {
        this.monitorRepository = monitorRepository;
        this.monitorProperties = monitorProperties;
    }

    @Transactional
    public List<Long> claimDueMonitorIds() {
        PageRequest pageRequest = PageRequest.of(
                0,
                monitorProperties.getSchedulerBatchSize(),
                Sort.by(Sort.Direction.ASC, "nextCheckAt")
        );

        return monitorRepository
                .findByActiveTrueAndCheckInProgressFalseAndNextCheckAtLessThanEqual(Instant.now(), pageRequest)
                .stream()
                .filter(Monitor::isEligibleForMonitoring)
                .peek(monitor -> monitor.setCheckInProgress(true))
                .map(Monitor::getId)
                .toList();
    }
}
