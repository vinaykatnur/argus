package com.argus.service;

import com.argus.config.AuthProperties;
import com.argus.entity.EmailVerificationToken;
import com.argus.entity.PasswordResetToken;
import com.argus.entity.RefreshToken;
import com.argus.entity.User;
import com.argus.enums.Role;
import com.argus.exception.AccountDisabledException;
import com.argus.exception.AccountNotVerifiedException;
import com.argus.exception.AuthenticationFailedException;
import com.argus.exception.InvalidTokenException;
import com.argus.exception.ResourceConflictException;
import com.argus.repository.EmailVerificationTokenRepository;
import com.argus.repository.PasswordResetTokenRepository;
import com.argus.repository.UserRepository;
import com.argus.request.ForgotPasswordRequest;
import com.argus.request.LoginRequest;
import com.argus.request.RefreshTokenRequest;
import com.argus.request.RegisterRequest;
import com.argus.request.ResetPasswordRequest;
import com.argus.response.AuthenticationResponse;
import com.argus.response.MessageResponse;
import com.argus.response.UserResponse;
import com.argus.security.ArgusUserDetails;
import com.argus.security.JwtService;
import com.argus.security.SecureTokenService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final String PASSWORD_RESET_RESPONSE =
            "If an eligible account exists for this email, a password reset link has been sent";

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenService secureTokenService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthEmailService authEmailService;
    private final AuthProperties authProperties;

    public AuthenticationService(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            SecureTokenService secureTokenService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AuthEmailService authEmailService,
            AuthProperties authProperties
    ) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.secureTokenService = secureTokenService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authEmailService = authEmailService;
        this.authProperties = authProperties;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email is already registered");
        }

        User user = new User(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                Role.USER
        );
        userRepository.save(user);

        String rawToken = createEmailVerificationToken(user);
        authEmailService.sendVerificationEmail(user, rawToken);

        return new MessageResponse("Registration successful. Verify your email before signing in");
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByTokenHash(secureTokenService.hashToken(token))
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.isUsed() || verificationToken.isExpired()) {
            throw new InvalidTokenException("Invalid verification token");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        verificationToken.setUsedAt(Instant.now());
        emailVerificationTokenRepository.markUnusedTokensAsUsed(user, Instant.now());

        return new MessageResponse("Email verified successfully");
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        validateUserCanAuthenticate(user);

        return buildAuthenticationResponse(user, refreshTokenService.createRefreshToken(user));
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(User::isEnabled)
                .filter(User::isEmailVerified)
                .ifPresent(this::sendPasswordResetToken);

        return new MessageResponse(PASSWORD_RESET_RESPONSE);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHash(secureTokenService.hashToken(request.token()))
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new InvalidTokenException("Invalid password reset token");
        }

        User user = resetToken.getUser();
        validateUserCanAuthenticate(user);

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsedAt(Instant.now());
        refreshTokenService.revokeActiveTokensForUser(user);

        return new MessageResponse("Password reset successful");
    }

    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());
        User user = refreshToken.getUser();
        validateUserCanAuthenticate(user);

        refreshTokenService.revoke(refreshToken);
        return buildAuthenticationResponse(user, refreshTokenService.createRefreshToken(user));
    }

    private String createEmailVerificationToken(User user) {
        emailVerificationTokenRepository.markUnusedTokensAsUsed(user, Instant.now());
        String rawToken = secureTokenService.generateToken();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                user,
                secureTokenService.hashToken(rawToken),
                Instant.now().plus(authProperties.getVerificationTokenExpirationMinutes(), ChronoUnit.MINUTES)
        );
        emailVerificationTokenRepository.save(verificationToken);
        return rawToken;
    }

    private void sendPasswordResetToken(User user) {
        passwordResetTokenRepository.markUnusedTokensAsUsed(user, Instant.now());
        String rawToken = secureTokenService.generateToken();
        PasswordResetToken resetToken = new PasswordResetToken(
                user,
                secureTokenService.hashToken(rawToken),
                Instant.now().plus(authProperties.getPasswordResetTokenExpirationMinutes(), ChronoUnit.MINUTES)
        );
        passwordResetTokenRepository.save(resetToken);
        authEmailService.sendPasswordResetEmail(user, rawToken);
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String refreshToken) {
        ArgusUserDetails userDetails = new ArgusUserDetails(user);
        return new AuthenticationResponse(
                jwtService.generateAccessToken(userDetails),
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                UserResponse.from(user)
        );
    }

    private void validateUserCanAuthenticate(User user) {
        if (!user.isEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }
        if (!user.isEmailVerified()) {
            throw new AccountNotVerifiedException("Email verification is required");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
