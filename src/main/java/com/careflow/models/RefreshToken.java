package com.careflow.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the actual token value (secure, long random string or JWT)
    @Column(nullable = false, unique = true)
    private String token;

    // link to the user who owns it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // expiration time
    @Column(name = "expires_at", nullable = false)
    private Date expiresAt;

    // if revoked or already used in rotation
    private boolean revoked;

    // timestamp of creation or last use
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;
}