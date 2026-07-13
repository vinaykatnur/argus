package com.argus.entity;

import com.argus.enums.AlertType;
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
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "alerts",
        indexes = {
                @Index(name = "idx_alerts_monitor_created", columnList = "monitor_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AlertType type;

    @Column(nullable = false, length = 500)
    private String message;

    private Integer httpStatusCode;

    private Long responseTimeMillis;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Alert(
            Monitor monitor,
            AlertType type,
            String message,
            Integer httpStatusCode,
            Long responseTimeMillis
    ) {
        this.monitor = monitor;
        this.type = type;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
        this.responseTimeMillis = responseTimeMillis;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
