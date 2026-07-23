package com.argus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.argus.config.AuthProperties;
import com.argus.entity.EmailVerificationToken;
import com.argus.entity.User;
import com.argus.enums.Role;
import com.argus.exception.AccountNotVerifiedException;
import com.argus.exception.AuthenticationFailedException;
import com.argus.exception.ResourceConflictException;
import com.argus.repository.EmailVerificationTokenRepository;
import com.argus.repository.PasswordResetTokenRepository;
import com.argus.repository.UserRepository;
import com.argus.request.LoginRequest;
import com.argus.request.RegisterRequest;
import com.argus.response.AuthenticationResponse;
import com.argus.response.MessageResponse;
import com.argus.security.JwtService;
import com.argus.security.SecureTokenService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthenticationServiceTests {

    private UserRepository userRepository;
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private PasswordEncoder passwordEncoder;
    private SecureTokenService secureTokenService;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private AuthEmailService authEmailService;
    private AuthProperties authProperties;
    
    private AuthenticationService authenticationService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        emailVerificationTokenRepository = mock(EmailVerificationTokenRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        secureTokenService = mock(SecureTokenService.class);
        jwtService = mock(JwtService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        authEmailService = mock(AuthEmailService.class);
        authProperties = mock(AuthProperties.class);
        when(authProperties.isRequireEmailVerification()).thenReturn(true);

        authenticationService = new AuthenticationService(
                userRepository,
                emailVerificationTokenRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                secureTokenService,
                jwtService,
                refreshTokenService,
                authEmailService,
                authProperties
        );

        testUser = new User("Taylor Nguyen", "taylor@company.com", "encoded-pass", Role.USER);
    }

    @Test
    void registerSucceedsAndCreatesToken() {
        RegisterRequest request = new RegisterRequest("Taylor Nguyen", "taylor@company.com", "plain-password");
        when(userRepository.existsByEmail("taylor@company.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-pass");
        when(secureTokenService.generateToken()).thenReturn("raw-token");
        when(secureTokenService.hashToken("raw-token")).thenReturn("hashed-token");
        when(authProperties.getVerificationTokenExpirationMinutes()).thenReturn(60L);

        MessageResponse response = authenticationService.register(request);

        assertThat(response.message()).contains("successful");
        verify(userRepository).save(any(User.class));
        verify(authEmailService).sendVerificationEmail(any(User.class), eq("raw-token"));
    }

    @Test
    void registerThrowsConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Taylor Nguyen", "taylor@company.com", "plain-password");
        when(userRepository.existsByEmail("taylor@company.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void verifyEmailSetsVerifiedTrue() {
        EmailVerificationToken token = new EmailVerificationToken(
                testUser,
                "hashed-token",
                Instant.now().plusSeconds(3600)
        );
        when(secureTokenService.hashToken("raw-token")).thenReturn("hashed-token");
        when(emailVerificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        MessageResponse response = authenticationService.verifyEmail("raw-token");

        assertThat(response.message()).contains("verified successfully");
        assertThat(testUser.isEmailVerified()).isTrue();
    }

    @Test
    void loginThrowsUnverifiedForUnverifiedUser() {
        LoginRequest request = new LoginRequest("taylor@company.com", "plain-password");
        testUser.setEmailVerified(false); // Make sure it's unverified

        when(userRepository.findByEmail("taylor@company.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plain-password", "encoded-pass")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(AccountNotVerifiedException.class);
    }

    @Test
    void loginSucceedsForVerifiedUser() {
        LoginRequest request = new LoginRequest("taylor@company.com", "plain-password");
        testUser.setEmailVerified(true); // Verified

        when(userRepository.findByEmail("taylor@company.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plain-password", "encoded-pass")).thenReturn(true);
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        AuthenticationResponse response = authenticationService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().email()).isEqualTo("taylor@company.com");
    }

    @Test
    void loginFailsWithWrongPassword() {
        LoginRequest request = new LoginRequest("taylor@company.com", "wrong-password");
        when(userRepository.findByEmail("taylor@company.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong-password", "encoded-pass")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }
}
