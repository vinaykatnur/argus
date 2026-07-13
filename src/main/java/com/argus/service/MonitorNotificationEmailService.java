package com.argus.service;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MonitorNotificationEmailService {

    private final JavaMailSender mailSender;

    public MonitorNotificationEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDowntimeEmail(Monitor monitor, Incident incident) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(monitor.getOwner().getEmail());
        message.setSubject("ARGUS downtime detected");
        message.setText("""
                ARGUS has confirmed downtime for this monitor:

                Monitor: %s
                URL: %s
                Started at: %s

                You are receiving this email because downtime notifications are enabled for this monitor.
                """.formatted(displayName(monitor), monitor.getUrl(), incident.getStartedAt()));
        mailSender.send(message);
    }

    public void sendRecoveryEmail(Monitor monitor, Incident incident) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(monitor.getOwner().getEmail());
        message.setSubject("ARGUS recovery detected");
        message.setText("""
                ARGUS has detected recovery for this monitor:

                Monitor: %s
                URL: %s
                Resolved at: %s
                Downtime: %d ms

                You are receiving this email because recovery notifications are enabled for this monitor.
                """.formatted(
                displayName(monitor),
                monitor.getUrl(),
                incident.getResolvedAt(),
                incident.getDowntimeMillis()
        ));
        mailSender.send(message);
    }

    private String displayName(Monitor monitor) {
        return monitor.getDisplayName() == null ? "Unnamed monitor" : monitor.getDisplayName();
    }
}
