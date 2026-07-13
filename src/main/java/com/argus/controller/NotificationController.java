package com.argus.controller;

import com.argus.entity.Monitor;
import com.argus.entity.NotificationPreference;
import com.argus.request.NotificationPreferencesRequest;
import com.argus.response.MessageResponse;
import com.argus.response.NotificationHealthResponse;
import com.argus.response.NotificationHistoryResponse;
import com.argus.response.NotificationPreferencesResponse;
import com.argus.security.ArgusUserDetails;
import com.argus.service.MonitorAccessService;
import com.argus.service.NotificationPreferenceService;
import com.argus.service.NotificationQueueService;
import com.argus.service.NotificationAuditService;
import com.argus.service.NotificationDeliveryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationPreferenceService notificationPreferenceService;
    private final MonitorAccessService monitorAccessService;
    private final NotificationDeliveryService notificationDeliveryService;
    private final NotificationQueueService notificationQueueService;
    private final NotificationAuditService notificationAuditService;

    public NotificationController(
            NotificationPreferenceService notificationPreferenceService,
            MonitorAccessService monitorAccessService,
            NotificationDeliveryService notificationDeliveryService,
            NotificationQueueService notificationQueueService,
            NotificationAuditService notificationAuditService
    ) {
        this.notificationPreferenceService = notificationPreferenceService;
        this.monitorAccessService = monitorAccessService;
        this.notificationDeliveryService = notificationDeliveryService;
        this.notificationQueueService = notificationQueueService;
        this.notificationAuditService = notificationAuditService;
    }

    @GetMapping("/preferences/{monitorId}")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        Monitor monitor = monitorAccessService.findOwnedMonitor(userDetails.getUser().getId(), monitorId);
        NotificationPreference preference = notificationPreferenceService.findExistingOrDefault(monitor);
        return ResponseEntity.ok(NotificationPreferencesResponse.from(preference));
    }

    @PutMapping("/preferences/{monitorId}")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId,
            @Valid @RequestBody NotificationPreferencesRequest request
    ) {
        Monitor monitor = monitorAccessService.findOwnedMonitor(userDetails.getUser().getId(), monitorId);
        NotificationPreference preference = notificationPreferenceService.configure(monitor, request);
        return ResponseEntity.ok(NotificationPreferencesResponse.from(preference));
    }

    @GetMapping("/history/{monitorId}")
    public ResponseEntity<Page<NotificationHistoryResponse>> getHistory(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        monitorAccessService.findOwnedMonitor(userDetails.getUser().getId(), monitorId);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationHistoryResponse> history = notificationDeliveryService.history(userDetails.getUser().getId(), monitorId, pageable)
                .map(NotificationHistoryResponse::from);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/health")
    public ResponseEntity<NotificationHealthResponse> getHealth() {
        return ResponseEntity.ok(new NotificationHealthResponse(
                notificationQueueService.mainQueueSize(),
                notificationQueueService.retryQueueSize(),
                notificationAuditService.failedNotificationCount()
        ));
    }

    @PostMapping("/requeue")
    public ResponseEntity<MessageResponse> requeue() {
        notificationDeliveryService.requeuePendingDeliveries();
        return ResponseEntity.ok(new MessageResponse("Notification requeue requested"));
    }
}
