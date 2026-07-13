package com.argus.repository;

import com.argus.entity.RefreshToken;
import com.argus.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken token
            set token.revokedAt = :revokedAt
            where token.user = :user and token.revokedAt is null
            """)
    void revokeActiveTokensForUser(@Param("user") User user, @Param("revokedAt") Instant revokedAt);
}
