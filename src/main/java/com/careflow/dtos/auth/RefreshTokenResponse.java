package com.careflow.dtos.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenResponse {
    // Le refresh a pour but de fournir un nouvel Access Token
    private String accessToken;
    // Idéalement, on renvoie un nouveau Refresh Token aussi (rotation)
    private String refreshToken;
}