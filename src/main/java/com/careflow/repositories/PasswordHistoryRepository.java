package com.careflow.repositories;

import com.careflow.models.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user.id = :userId " +
            "ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}