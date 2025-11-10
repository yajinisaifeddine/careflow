package com.careflow.controllers;

import com.careflow.dtos.auth.reset_password.PasswordResetRequest;
import com.careflow.dtos.auth.reset_password.ResetPasswordRequest;
import com.careflow.dtos.auth.reset_password.ResetPasswordVerifyRequest;
import com.careflow.services.auth.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {
    private final ResetPasswordService resetPasswordService;

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(resetPasswordService.forgot(request));
    }

    @GetMapping("/request")
    public ResponseEntity<?> request() {
        return ResponseEntity.ok(resetPasswordService.request());
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody ResetPasswordVerifyRequest request) {
        return ResponseEntity.ok(resetPasswordService.verify(request));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(resetPasswordService.reset(request));
    }
}
