package com.argus.controller;

import com.argus.enums.MonitorSortOption;
import com.argus.enums.MonitorStatus;
import com.argus.request.PinMonitorRequest;
import com.argus.request.CreateMonitorRequest;
import com.argus.request.UpdateMonitorRequest;
import com.argus.response.MessageResponse;
import com.argus.response.MonitorDetailsResponse;
import com.argus.response.MonitorListItemResponse;
import com.argus.response.MonitorResponse;
import com.argus.response.PageResponse;
import com.argus.security.ArgusUserDetails;
import com.argus.service.MonitorManagementService;
import com.argus.service.MonitorQueryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/monitors")
public class MonitorController {

    private final MonitorManagementService monitorManagementService;
    private final MonitorQueryService monitorQueryService;

    public MonitorController(
            MonitorManagementService monitorManagementService,
            MonitorQueryService monitorQueryService
    ) {
        this.monitorManagementService = monitorManagementService;
        this.monitorQueryService = monitorQueryService;
    }

    @PostMapping
    public ResponseEntity<MonitorResponse> create(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @Valid @RequestBody CreateMonitorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(monitorManagementService.create(userDetails.getUser().getId(), request));
    }

    @PutMapping("/{monitorId}")
    public ResponseEntity<MonitorResponse> update(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId,
            @Valid @RequestBody UpdateMonitorRequest request
    ) {
        return ResponseEntity.ok(monitorManagementService.update(userDetails.getUser().getId(), monitorId, request));
    }

    @DeleteMapping("/{monitorId}")
    public ResponseEntity<MessageResponse> delete(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        return ResponseEntity.ok(monitorManagementService.delete(userDetails.getUser().getId(), monitorId));
    }

    @PatchMapping("/{monitorId}/pause")
    public ResponseEntity<MonitorResponse> pause(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        return ResponseEntity.ok(monitorManagementService.pause(userDetails.getUser().getId(), monitorId));
    }

    @PatchMapping("/{monitorId}/resume")
    public ResponseEntity<MonitorResponse> resume(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        return ResponseEntity.ok(monitorManagementService.resume(userDetails.getUser().getId(), monitorId));
    }

    @PatchMapping("/{monitorId}/pin")
    public ResponseEntity<MonitorResponse> pin(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId,
            @Valid @RequestBody(required = false) PinMonitorRequest request
    ) {
        Integer position = request == null ? null : request.position();
        return ResponseEntity.ok(monitorManagementService.pin(userDetails.getUser().getId(), monitorId, position));
    }

    @PatchMapping("/{monitorId}/unpin")
    public ResponseEntity<MonitorResponse> unpin(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        return ResponseEntity.ok(monitorManagementService.unpin(userDetails.getUser().getId(), monitorId));
    }

    @PostMapping("/{monitorId}/health-check")
    public ResponseEntity<MonitorResponse> runManualHealthCheck(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        return ResponseEntity.ok(monitorManagementService.runManualHealthCheck(userDetails.getUser().getId(), monitorId));
    }

    @GetMapping("/{monitorId}")
    public ResponseEntity<MonitorResponse> get(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        return ResponseEntity.ok(monitorManagementService.get(userDetails.getUser().getId(), monitorId));
    }

    @GetMapping("/{monitorId}/details")
    public ResponseEntity<MonitorDetailsResponse> getDetails(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long monitorId
    ) {
        return ResponseEntity.ok(monitorQueryService.getDetails(userDetails.getUser().getId(), monitorId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MonitorListItemResponse>> list(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<MonitorStatus> status,
            @RequestParam(required = false) Boolean activeIncident,
            @RequestParam(defaultValue = "LAST_CHECKED") MonitorSortOption sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(monitorQueryService.list(
                userDetails.getUser().getId(),
                search,
                status,
                activeIncident,
                sort,
                page,
                size
        ));
    }
}
