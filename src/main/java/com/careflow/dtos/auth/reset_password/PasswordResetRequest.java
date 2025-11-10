package com.careflow.dtos.auth.reset_password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "nust provid token")
    @NotNull(message = "nust provid token")
    @Length(max = 6, min = 6, message = "token is 6 charachters long")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$",
            message = "Password must contain at least one number and one special character"
    )
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String confirmNewPassword;

}
