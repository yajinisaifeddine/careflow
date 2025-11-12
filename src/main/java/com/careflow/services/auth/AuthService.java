package com.careflow.services.auth;

import com.careflow.dtos.auth.*;
import com.careflow.exceptions.auth.AccessDeniedException;
import com.careflow.exceptions.auth.RoleNotFoundException;
import com.careflow.exceptions.auth.UserAlreadyExistsException;
import com.careflow.exceptions.auth.UserNotFoundException;
import com.careflow.models.RefreshToken;
import com.careflow.models.Role;
import com.careflow.models.User;
import com.careflow.repositories.RefreshTokenRepository;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("user with email " + request.getEmail() + " already exists");
        }
        log.info("getting role");

        String rolePrefix = "ROLE_";
        String requestedRole = (request.getRole() == null || request.getRole().isBlank()) ? "PATIENT" : request.getRole().toUpperCase();
        String finalRole = !requestedRole.startsWith(rolePrefix) ? rolePrefix + requestedRole : requestedRole;

        Role role = roleRepository.findByName(finalRole).orElseThrow(() -> new RoleNotFoundException("Role not found: " + requestedRole));

        // Sécurité: Interdiction d'auto-assignation du rôle ADMIN
        if (role.getName().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("cannot assign to admin");
        }

        User user = User.builder().fullName(request.getFullName()).email(request.getEmail()).password(passwordEncoder.encode(request.getPassword())).provider("local").role(role).build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user);


        if (accessToken == null || accessToken.isEmpty() || refreshToken == null || refreshToken.isEmpty()) {
            log.info(accessToken + refreshToken);
            userRepository.delete(user);
            throw new InvalidBearerTokenException("access or refresh token are invalid");
        }
        log.info("registering user " + user.getEmail());


        // Génère l'Access Token pour la réponse d'enregistrement

        return RegisterResponse.builder()
                .status(HttpStatus.OK)
                .message("registration is successfll for user " + user.getFullName())
                .data(Map.of(
                        "user", UserDto.builder()
                                .id(user.getId())
                                .fullName(user.getFullName())
                                .email(user.getFullName())
                                .role(user.getRole().getName())
                                .build(),
                        "tokens", Map.of("refreshToken", refreshToken, "accessToken", accessToken)

                )).build();
    }
//Map.of("status", 200, "message", "registration of user " + user.getEmail() + " successflly", "data", Map.of("user", User.builder().email(user.getUsername()).fullName(user.getFullName()).provider("local").build()));

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user);

        return LoginResponse.builder()
                .status(HttpStatus.OK)
                .message("login is successfull for user " + user.getFullName())
                .data(Map.of(
                        "user", UserDto.builder().email(user.getEmail()).fullName(user.getFullName()).id(user.getId()).role(user.getRole().getName()).build(),
                        "tokens", Map.of("refreshToken", refreshToken, "accessToken", accessToken)
                ))
                .build();

    }

    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        final String oldRefreshToken = request.getToken();

        if (oldRefreshToken == null) {
            throw new InvalidBearerTokenException("Refresh token is missing");
        }

        try {
            // 1. Extraction de l'email (vérifie la signature et le format)
            String email = jwtUtils.extractUsername(oldRefreshToken);

            // 2. Recherche de l'utilisateur
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found for token email"));

            // 3. Validation du token
            // isTokenValid doit vérifier que le token n'est pas expiré et qu'il correspond à l'utilisateur
            // Si le token est expiré (après 30 jours), on lève l'exception
            if (!jwtUtils.isTokenValid(oldRefreshToken, user)) {
                // Si le token est invalide pour toute autre raison (ex: mismatch user), c'est une erreur.
                throw new InvalidBearerTokenException("Invalid refresh token (Signature or User mismatch)");
            }

            // 4. Génération de NOUVEAUX tokens (rotation)
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = refreshTokenService.generateRefreshToken(user);

            log.info("Tokens refreshed successfully for user {}", user.getEmail());

            return RefreshTokenResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();

        } catch (Exception e) {
            // Capture les exceptions levées par jjwt (ExpiredJwtException, SignatureException, etc.)
            log.error("Refresh token processing failed: {}", e.getMessage());
            throw new InvalidBearerTokenException("Invalid or expired refresh token provided.");
        }
    }

    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        User user = jwtUtils.getAuthenticatedUser();
        if (user == null) throw new UserNotFoundException("user is not found");
        log.info(request.getRefreshToken());
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken()).orElseThrow(() -> new InvalidBearerTokenException("token is invalid"));
        if (!token.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("cannot logout another user's session");
        }
        refreshTokenRepository.updateRevokedByUser(user, true);

        return LogoutResponse.builder()
                .message("logout successflly")
                .status(HttpStatus.OK)
                .build();
    }
}