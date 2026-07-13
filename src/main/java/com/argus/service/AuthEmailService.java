package com.argus.service;

import com.argus.config.AuthProperties;
import com.argus.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Arrays;

@Service
public class AuthEmailService {

    private static final Logger log = LoggerFactory.getLogger(AuthEmailService.class);
    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;
    private final Environment environment;

    public AuthEmailService(JavaMailSender mailSender, AuthProperties authProperties, Environment environment) {
        this.mailSender = mailSender;
        this.authProperties = authProperties;
        this.environment = environment;
    }

    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = UriComponentsBuilder.fromUriString(authProperties.getFrontendBaseUrl())
                .path("/verify-email")
                .queryParam("token", token)
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your ARGUS account");
        message.setText("""
                Welcome to ARGUS.

                Verify your email address using this link:
                %s

                If you did not create this account, ignore this email.
                """.formatted(verificationUrl));

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev")
                    || environment.getActiveProfiles().length == 0;
            if (isDev) {
                log.warn("SMTP email delivery failed in development mode. Emitting local verification link: {}", verificationUrl, exception);
            } else {
                throw exception;
            }
        }
    }

    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl = UriComponentsBuilder.fromUriString(authProperties.getFrontendBaseUrl())
                .path("/reset-password")
                .queryParam("token", token)
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset your ARGUS password");
        message.setText("""
                Use this link to reset your ARGUS password:
                %s

                If you did not request a password reset, ignore this email.
                """.formatted(resetUrl));
        
        mailSender.send(message);
    }
}
