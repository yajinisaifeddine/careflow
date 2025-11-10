package com.careflow.dtos.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String email;
    private String fullName;
    private String role;
    private String accessToken;
    private String refreshToken;
}