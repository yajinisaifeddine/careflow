package com.careflow.dtos.auth.reset_password;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordVerifyRequest {
    @NotBlank(message = "nust provid token")
    @NotNull(message = "nust provid token")
    @Length(max = 6, min = 6, message = "token is 6 charachters long")
    private String token;
}
