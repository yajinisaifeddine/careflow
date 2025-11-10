package com.careflow.dtos.auth.reset_password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequest {
    private String token;
    private String newPassword;
    private String confirmNewPassword;
}
