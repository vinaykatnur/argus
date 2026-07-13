package com.argus.repository;

import com.argus.entity.NotificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, Long> {

    void deleteByNotification_Monitor_Id(Long monitorId);
}
