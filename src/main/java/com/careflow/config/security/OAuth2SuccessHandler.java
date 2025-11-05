package com.careflow.config.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.careflow.models.Role;
import com.careflow.models.User;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.services.auth.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User authUser = (OAuth2User) authentication.getPrincipal();
        String email = authUser.getAttribute("email");
        String name = authUser.getAttribute("name");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found by the provider");
            return;
        }
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // Get the default PATIENT role
            Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_PATIENT not found"));

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setProvider("google");
            newUser.setRole(patientRole);
            return userRepository.save(newUser);
        });

        String token = jwtService.generateToken(user);

        // save user to databse and authenticate it then redirect it to frontend with
        // token
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\": \"" + token + "\"}");

    }

}
