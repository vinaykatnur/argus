package com.argus.service;

import com.argus.entity.NotificationDelivery;
import com.argus.enums.NotificationChannel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationDeliveryService implements NotificationChannelDeliveryService {

    private final JavaMailSender mailSender;

    public EmailNotificationDeliveryService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationDelivery delivery, NotificationContent content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(delivery.getRecipient());
            helper.setSubject(content.subject());
            helper.setText(content.plainTextBody(), content.htmlBody());
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Unable to build email notification", exception);
        } catch (MailException exception) {
            throw exception;
        }
    }
}
