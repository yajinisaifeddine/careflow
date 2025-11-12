package com.careflow.controllers;

import com.careflow.dtos.auth.UserDto;
import com.careflow.models.User;
import com.careflow.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<?> getAuthenticatedUser() {
        User user = jwtUtils.getAuthenticatedUser();
        UserDto userDto = new UserDto(user);
        return ResponseEntity.ok(
                Map.of(
                        "status", HttpStatus.OK,
                        "data", userDto
                )
        );
    }
}
