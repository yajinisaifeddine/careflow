package com.careflow.repositories;

import com.careflow.models.RefreshToken;
import com.careflow.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUser(User user);
    /**
     * Updates the 'revoked' status for all RefreshTokens associated with a specific user.
     * @param revoked The new boolean value for the revoked status.
     * @param user The User entity whose tokens should be updated.
     * @return The number of entities updated.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = :revoked WHERE rt.user = :user")
    int updateRevokedByUser(@Param("user") User user, @Param("revoked") boolean revoked);
}
