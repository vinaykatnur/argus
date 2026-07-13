package com.argus.repository;

import com.argus.entity.PasswordResetToken;
import com.argus.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update PasswordResetToken token
            set token.usedAt = :usedAt
            where token.user = :user and token.usedAt is null
            """)
    void markUnusedTokensAsUsed(@Param("user") User user, @Param("usedAt") Instant usedAt);
}
