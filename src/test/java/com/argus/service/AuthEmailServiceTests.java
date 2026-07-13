package com.argus.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.argus.config.AuthProperties;
import com.argus.entity.User;
import com.argus.enums.Role;

class AuthEmailServiceTests {

    private JavaMailSender mailSender;
    private AuthProperties authProperties;
    private Environment environment;
    private AuthEmailService authEmailService;
    private User testUser;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        authProperties = mock(AuthProperties.class);
        environment = mock(Environment.class);
        
        when(authProperties.getFrontendBaseUrl()).thenReturn("http://localhost:3000");
        
        authEmailService = new AuthEmailService(mailSender, authProperties, environment);

        testUser = new User("Test User", "test@example.com", "hash", Role.USER);
    }

    @Test
    void sendVerificationEmailSucceedsNormally() {
        authEmailService.sendVerificationEmail(testUser, "valid-token");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationEmailSwallowsExceptionInDevProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> authEmailService.sendVerificationEmail(testUser, "valid-token"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendVerificationEmailPropagatesExceptionInProdProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> authEmailService.sendVerificationEmail(testUser, "valid-token"))
                .isInstanceOf(MailException.class);
    }

    @Test
    void sendPasswordResetEmailPropagatesExceptionInAllProfiles() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> authEmailService.sendPasswordResetEmail(testUser, "valid-token"))
                .isInstanceOf(MailException.class);
    }
}