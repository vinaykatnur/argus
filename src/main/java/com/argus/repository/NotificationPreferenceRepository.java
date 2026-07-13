package com.argus.repository;

import com.argus.entity.Monitor;
import com.argus.entity.NotificationPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByMonitor(Monitor monitor);

    Optional<NotificationPreference> findByMonitorId(Long monitorId);

    void deleteByMonitor(Monitor monitor);
}
