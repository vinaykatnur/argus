package com.argus.repository;

import com.argus.entity.NotificationDelivery;
import com.argus.enums.NotificationStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    long countByStatus(NotificationStatus status);

    List<NotificationDelivery> findByNotificationId(Long notificationId);

    List<NotificationDelivery> findByStatusOrderByCreatedAtAsc(NotificationStatus status);

    List<NotificationDelivery> findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            NotificationStatus status,
            java.time.Instant availableAt
    );

    void deleteByNotification_Monitor_Id(Long monitorId);

    @Query("select d from NotificationDelivery d where d.notification.monitor.owner.id = :ownerId "
            + "and (:monitorId is null or d.notification.monitor.id = :monitorId)")
    Page<NotificationDelivery> findHistory(
            @Param("ownerId") Long ownerId,
            @Param("monitorId") Long monitorId,
            Pageable pageable
    );

    @Query("select count(d) from NotificationDelivery d where d.notification.id = :notificationId "
            + "and d.status = :status")
    long countByNotificationIdAndStatus(
            @Param("notificationId") Long notificationId,
            @Param("status") NotificationStatus status
    );

    @Query("select count(d) from NotificationDelivery d where d.notification.id = :notificationId")
    long countByNotificationId(@Param("notificationId") Long notificationId);
}
