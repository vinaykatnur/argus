package com.argus.controller;

import com.argus.incidentintelligence.service.ai.AiProviderConfigurationService;
import com.argus.response.RuntimeDiagnosticsResponse;
import com.argus.security.ArgusUserDetails;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diagnostics")
public class RuntimeDiagnosticsController {

    private final DataSource dataSource;
    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final AiProviderConfigurationService aiProviderConfigurationService;

    public RuntimeDiagnosticsController(
            DataSource dataSource,
            ApplicationContext applicationContext,
            Environment environment,
            AiProviderConfigurationService aiProviderConfigurationService
    ) {
        this.dataSource = dataSource;
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.aiProviderConfigurationService = aiProviderConfigurationService;
    }

    @GetMapping("/runtime")
    public ResponseEntity<RuntimeDiagnosticsResponse> runtime(
            @AuthenticationPrincipal ArgusUserDetails userDetails
    ) {
        List<RuntimeDiagnosticsResponse.ServiceHealth> services = new ArrayList<>();
        services.add(new RuntimeDiagnosticsResponse.ServiceHealth("Backend Health", "UP", "API request completed.", 0L));
        services.add(databaseHealth());
        services.add(beanHealth("Scheduler", "centralMonitoringScheduler", "Monitoring scheduler bean is available."));
        services.add(beanHealth("Analytics Engine", "analyticsAggregationService", "Analytics aggregation bean is available."));
        services.add(beanHealth("Incident Intelligence Engine", "incidentIntelligenceEngine", "Deterministic analysis bean is available."));
        services.add(aiNarrativeHealth(userDetails));

        String overallStatus = services.stream().anyMatch(service -> "DOWN".equals(service.status())) ? "DEGRADED" : "UP";
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();

        return ResponseEntity.ok(new RuntimeDiagnosticsResponse(
                overallStatus,
                Instant.now(),
                environmentLabel(),
                applicationVersion(),
                System.getProperty("java.version"),
                formatDuration(Duration.ofMillis(uptimeMillis)),
                uptimeMillis,
                memoryUsage(),
                diskUsage(),
                cpuUsage(),
                services
        ));
    }

    private RuntimeDiagnosticsResponse.ServiceHealth databaseHealth() {
        long started = System.nanoTime();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            long elapsed = elapsedMillis(started);
            return new RuntimeDiagnosticsResponse.ServiceHealth(
                    "Database",
                    valid ? "UP" : "DOWN",
                    valid ? "Connectivity verified." : "Database connection was not valid.",
                    elapsed
            );
        } catch (Exception exception) {
            return new RuntimeDiagnosticsResponse.ServiceHealth(
                    "Database",
                    "DOWN",
                    "Database connectivity failed.",
                    elapsedMillis(started)
            );
        }
    }

    private RuntimeDiagnosticsResponse.ServiceHealth beanHealth(String name, String beanName, String detail) {
        try {
            applicationContext.getBean(beanName);
            return new RuntimeDiagnosticsResponse.ServiceHealth(name, "UP", detail, null);
        } catch (NoSuchBeanDefinitionException exception) {
            return new RuntimeDiagnosticsResponse.ServiceHealth(name, "UNAVAILABLE", "No runtime endpoint is available.", null);
        }
    }

    private RuntimeDiagnosticsResponse.ServiceHealth aiNarrativeHealth(ArgusUserDetails userDetails) {
        try {
            applicationContext.getBean("incidentNarrativeGenerationService");
        } catch (NoSuchBeanDefinitionException exception) {
            return new RuntimeDiagnosticsResponse.ServiceHealth("AI Narrative Engine", "UNAVAILABLE", "No runtime endpoint is available.", null);
        }

        boolean configured = userDetails != null
                && aiProviderConfigurationService.get(userDetails.getUser().getId()).configured();
        return new RuntimeDiagnosticsResponse.ServiceHealth(
                "AI Narrative Engine",
                configured ? "UP" : "OPTIONAL",
                configured ? "Optional AI narrative provider configured." : "Optional AI provider is not configured.",
                null
        );
    }

    private RuntimeDiagnosticsResponse.RuntimeUsage memoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long used = heap.getUsed();
        long max = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        return new RuntimeDiagnosticsResponse.RuntimeUsage("UP", formatBytes(used) + " / " + formatBytes(max), used, max, percentage(used, max));
    }

    private RuntimeDiagnosticsResponse.RuntimeUsage diskUsage() {
        File root = new File(".");
        long total = root.getTotalSpace();
        long free = root.getFreeSpace();
        long used = Math.max(0L, total - free);
        return new RuntimeDiagnosticsResponse.RuntimeUsage("UP", formatBytes(used) + " / " + formatBytes(total), used, total, percentage(used, total));
    }

    private RuntimeDiagnosticsResponse.RuntimeUsage cpuUsage() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        Double percentage = null;
        if (bean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
            double load = sunBean.getCpuLoad();
            if (load >= 0) {
                percentage = load * 100.0;
            }
        }

        String value = percentage == null
                ? "Load average " + String.format("%.2f", bean.getSystemLoadAverage())
                : String.format("%.1f%%", percentage);
        return new RuntimeDiagnosticsResponse.RuntimeUsage("UP", value, null, null, percentage);
    }

    private String environmentLabel() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "default" : String.join(", ", profiles);
    }

    private String applicationVersion() {
        String fromEnvironment = environment.getProperty("info.app.version");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        Package appPackage = RuntimeDiagnosticsController.class.getPackage();
        String implementationVersion = appPackage.getImplementationVersion();
        return implementationVersion == null ? "0.0.1-SNAPSHOT" : implementationVersion;
    }

    private long elapsedMillis(long startedNanos) {
        return Duration.ofNanos(System.nanoTime() - startedNanos).toMillis();
    }

    private Double percentage(long used, long total) {
        return total <= 0 ? null : (used * 100.0) / total;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double value = bytes;
        List<String> units = Arrays.asList("KB", "MB", "GB", "TB");
        int unitIndex = -1;
        while (value >= 1024 && unitIndex < units.size() - 1) {
            value = value / 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", value, units.get(unitIndex));
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m " + duration.toSecondsPart() + "s";
    }
}
