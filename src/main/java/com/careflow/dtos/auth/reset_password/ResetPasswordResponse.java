package com.careflow.dtos.auth.reset_password;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordResponse {
    private boolean success;
    private String message;
}
