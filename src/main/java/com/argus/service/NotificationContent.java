package com.argus.service;

public record NotificationContent(
        String subject,
        String htmlBody,
        String plainTextBody
) {
}
