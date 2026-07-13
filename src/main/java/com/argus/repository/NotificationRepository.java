package com.argus.repository;

import com.argus.entity.Monitor;
import com.argus.entity.Notification;
import com.argus.enums.NotificationType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findFirstByIncidentIdAndTypeOrderByCreatedAtDesc(Long incidentId, NotificationType type);

    void deleteByMonitor(Monitor monitor);
}
