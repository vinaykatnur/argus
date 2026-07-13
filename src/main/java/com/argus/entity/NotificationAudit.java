package com.argus.entity;

import com.argus.enums.NotificationAuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "notification_audits",
        indexes = {
                @Index(name = "idx_notification_audits_notification", columnList = "notification_id"),
                @Index(name = "idx_notification_audits_created", columnList = "created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false, updatable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", updatable = false)
    private NotificationDelivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 40)
    private NotificationAuditAction action;

    @Column(nullable = false, updatable = false, length = 500)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public NotificationAudit(
            Notification notification,
            NotificationDelivery delivery,
            NotificationAuditAction action,
            String message
    ) {
        this.notification = notification;
        this.delivery = delivery;
        this.action = action;
        this.message = message;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
