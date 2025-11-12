package com.careflow.dtos.auth;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
@Builder
public class LoginResponse {
    private HttpStatus status;
    private String message;
    private Map data;
}