package com.argus.service;

import com.argus.config.JwtProperties;
import com.argus.entity.RefreshToken;
import com.argus.entity.User;
import com.argus.exception.InvalidTokenException;
import com.argus.repository.RefreshTokenRepository;
import com.argus.security.SecureTokenService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureTokenService secureTokenService;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            SecureTokenService secureTokenService,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secureTokenService = secureTokenService;
        this.jwtProperties = jwtProperties;
    }

    public String createRefreshToken(User user) {
        String rawToken = secureTokenService.generateToken();
        RefreshToken refreshToken = new RefreshToken(
                user,
                secureTokenService.hashToken(rawToken),
                Instant.now().plus(jwtProperties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS)
        );
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public RefreshToken validateRefreshToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(secureTokenService.hashToken(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        return refreshToken;
    }

    public void revoke(RefreshToken refreshToken) {
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeActiveTokensForUser(User user) {
        refreshTokenRepository.revokeActiveTokensForUser(user, Instant.now());
    }
}
