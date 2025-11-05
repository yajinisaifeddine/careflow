package com.careflow.services.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.careflow.dtos.auth.LoginRequest;
import com.careflow.dtos.auth.LoginResponse;
import com.careflow.dtos.auth.RegisterRequest;
import com.careflow.dtos.auth.RegisterResponse;
import com.careflow.exceptions.auth.AccessDeniedException;
import com.careflow.exceptions.auth.RoleNotFoundException;
import com.careflow.exceptions.auth.UserAlreadyExistsException;
import com.careflow.models.Role;
import com.careflow.models.User;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public RegisterResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new UserAlreadyExistsException(
                                        "user with email " + request.getEmail() + " already exists");
                }
                log.info("getting role");

                String rolePrefix = "ROLE_";
                String requestedRole = (request.getRole() == null || request.getRole().isBlank())
                                ? "PATIENT"
                                : request.getRole().toUpperCase();
                String finalRole = !requestedRole.startsWith(rolePrefix)
                                ? rolePrefix + requestedRole
                                : requestedRole;

                Role role = roleRepository.findByName(finalRole)
                                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + requestedRole));

                if (role.getName().equals("ROLE_ADMIN")) {
                        throw new AccessDeniedException("cannot assign to admin");
                }

                User user = User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .provider("local")
                                .role(role)
                                .build();

                log.info("registering user " + user);

                userRepository.save(user);

                String token = jwtService.generateToken(user);
                return RegisterResponse.builder()
                                .token(token)
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .role(user.getRole().getName().split("_")[1].toLowerCase())
                                .build();
        }

        public LoginResponse login(LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
                User user = (User) authentication.getPrincipal();
                String token = jwtService.generateToken(user);
                return LoginResponse.builder()
                                .email(user.getEmail())
                                .token(token)
                                .fullName(user.getFullName())
                                .role(user.getRole().getName().split("_")[1].toLowerCase())
                                .build();
        }

}
