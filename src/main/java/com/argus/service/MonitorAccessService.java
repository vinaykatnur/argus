package com.argus.service;

import com.argus.entity.Monitor;
import com.argus.exception.ResourceNotFoundException;
import com.argus.repository.MonitorRepository;
import org.springframework.stereotype.Service;

@Service
public class MonitorAccessService {

    private final MonitorRepository monitorRepository;

    public MonitorAccessService(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    public Monitor findOwnedMonitor(Long ownerId, Long monitorId) {
        return monitorRepository.findByIdAndOwnerId(monitorId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));
    }
}
