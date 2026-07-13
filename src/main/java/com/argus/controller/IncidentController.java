package com.argus.controller;

import com.argus.enums.IncidentDateRange;
import com.argus.enums.IncidentStatus;
import com.argus.response.IncidentTimelineItemResponse;
import com.argus.response.PageResponse;
import com.argus.security.ArgusUserDetails;
import com.argus.service.IncidentService;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<IncidentTimelineItemResponse>> list(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Long monitorId,
            @RequestParam(required = false) IncidentDateRange dateRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(incidentService.list(
                userDetails.getUser().getId(),
                status,
                monitorId,
                dateRange,
                startDate,
                endDate,
                page,
                size
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<PageResponse<IncidentTimelineItemResponse>> active(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @RequestParam(required = false) Long monitorId,
            @RequestParam(required = false) IncidentDateRange dateRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(incidentService.list(
                userDetails.getUser().getId(),
                IncidentStatus.ACTIVE,
                monitorId,
                dateRange,
                startDate,
                endDate,
                page,
                size
        ));
    }

    @GetMapping("/resolved")
    public ResponseEntity<PageResponse<IncidentTimelineItemResponse>> resolved(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @RequestParam(required = false) Long monitorId,
            @RequestParam(required = false) IncidentDateRange dateRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(incidentService.list(
                userDetails.getUser().getId(),
                IncidentStatus.RESOLVED,
                monitorId,
                dateRange,
                startDate,
                endDate,
                page,
                size
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<PageResponse<IncidentTimelineItemResponse>> history(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @RequestParam(required = false) Long monitorId,
            @RequestParam(required = false) IncidentDateRange dateRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(incidentService.list(
                userDetails.getUser().getId(),
                null,
                monitorId,
                dateRange,
                startDate,
                endDate,
                page,
                size
        ));
    }

    @GetMapping("/timeline")
    public ResponseEntity<PageResponse<IncidentTimelineItemResponse>> timeline(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Long monitorId,
            @RequestParam(required = false) IncidentDateRange dateRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return list(userDetails, status, monitorId, dateRange, startDate, endDate, page, size);
    }
}
