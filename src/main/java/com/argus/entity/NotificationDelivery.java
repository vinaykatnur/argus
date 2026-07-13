package com.argus.entity;

import com.argus.enums.NotificationChannel;
import com.argus.enums.NotificationStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "notification_deliveries",
        indexes = {
                @Index(name = "idx_notification_deliveries_notification", columnList = "notification_id"),
                @Index(name = "idx_notification_deliveries_status", columnList = "status"),
                @Index(name = "idx_notification_deliveries_channel", columnList = "channel")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false, length = 320)
    private String recipient;

    @Column(length = 200)
    private String subject;

    @Column(nullable = false)
    private int attemptCount;

    @Column(length = 1000)
    private String failureReason;

    private Instant queuedAt;

    private Instant sendingAt;

    private Instant sentAt;

    private Instant nextRetryAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public NotificationDelivery(
            Notification notification,
            NotificationChannel channel,
            String recipient,
            String subject
    ) {
        this.notification = notification;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
