package com.careflow.repositories;

import com.careflow.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUserEmailAndTokenHashAndIsUsedFalse(
            String email,
            String token
    );

    List<PasswordResetToken> findByUserEmailAndIsUsedFalseAndExpiresAtAfter(
            String email,
            Date now
    );

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.isUsed = true WHERE p.user.id = :userId")
    void invalidateAllTokensForUser(Long userId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(Date now);

    // Count active tokens for rate limiting
    long countByUserEmailAndCreatedAtAfter(String email, Date since);
}