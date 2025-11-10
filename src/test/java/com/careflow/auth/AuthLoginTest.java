package com.careflow.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.careflow.models.RefreshToken;
import com.careflow.repositories.RefreshTokenRepository;
import com.careflow.services.auth.AuthService;
import com.careflow.services.auth.JwtService;
import com.careflow.services.auth.RefreshTokenService;
import com.careflow.utils.JwtUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.careflow.dtos.auth.LoginRequest;
import com.careflow.dtos.auth.LoginResponse;
import com.careflow.models.Role;
import com.careflow.models.User;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
@SpringBootTest
class AuthLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test role
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_PATIENT");

        // Setup login request
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        refreshTokenRepository.deleteAll(); // Clean DB before each test
        userRepository.deleteAll();

        // create user
        Role role = Role.builder().name("ROLE_PATIENT").build();
        testUser = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password")
                .role(role)
                .provider("local")
                .build();
        userRepository.save(testUser);

    }

    @Test
    void testLoginWithValidCredentials_returnsLoginResponseWithTokenAndUserDetails() {
        // Arrange
        String expectedAccessToken = "access.token.jwt";
        String expectedRefreshToken = "refresh.token.jwt";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtService.generateAccessToken(testUser)).thenReturn(expectedAccessToken);
        when(refreshTokenService.generateRefreshToken(testUser)).thenReturn(expectedRefreshToken);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(testUser.getEmail(), response.getEmail(), "Email should match");
        assertEquals(testUser.getFullName(), response.getFullName(), "Full name should match");
        assertEquals("patient", response.getRole(), "Role should be 'patient' (lowercase, without ROLE_ prefix)");
        assertEquals(expectedAccessToken, response.getAccessToken(), "Access token should match");
        assertEquals(expectedRefreshToken, response.getRefreshToken(), "Refresh token should match");

        // Verify interactions
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateAccessToken(testUser);
        verify(refreshTokenService, times(1)).generateRefreshToken(testUser);
    }

    @Test
    void testLoginWithFailedAuthentication_throwsBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest),
                "Should throw BadCredentialsException when authentication fails"
        );

        assertEquals("Invalid credentials", exception.getMessage(), "Exception message should match");

        // Verify that JWT tokens were never generated
        verify(jwtService, never()).generateAccessToken(any(User.class));
        verify(refreshTokenService, never()).generateRefreshToken(any(User.class));
    }

    @Test
    void testLoginWithDoctorRole_returnsCorrectRoleInResponse() {
        // Arrange
        Role doctorRole = new Role();
        doctorRole.setId(2L);
        doctorRole.setName("ROLE_DOCTOR");

        User doctorUser = User.builder()
                .id(2L)
                .email("doctor@example.com")
                .fullName("Dr. Smith")
                .password("encodedPassword")
                .provider("local")
                .role(doctorRole)
                .build();

        LoginRequest doctorLoginRequest = LoginRequest.builder()
                .email("doctor@example.com")
                .password("password123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(doctorUser);
        when(jwtService.generateAccessToken(doctorUser)).thenReturn("access.token");
        when(refreshTokenService.generateRefreshToken(doctorUser)).thenReturn("refresh.token");

        // Act
        LoginResponse response = authService.login(doctorLoginRequest);

        // Assert
        assertEquals("doctor", response.getRole(), "Role should be 'doctor'");
    }

    @Test
    void testLoginWithNurseRole_returnsCorrectRoleInResponse() {
        // Arrange
        Role nurseRole = new Role();
        nurseRole.setId(3L);
        nurseRole.setName("ROLE_NURSE");

        User nurseUser = User.builder()
                .id(3L)
                .email("nurse@example.com")
                .fullName("Nurse Jane")
                .password("encodedPassword")
                .provider("local")
                .role(nurseRole)
                .build();

        LoginRequest nurseLoginRequest = LoginRequest.builder()
                .email("nurse@example.com")
                .password("password123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(nurseUser);
        when(jwtService.generateAccessToken(nurseUser)).thenReturn("access.token");
        when(refreshTokenService.generateRefreshToken(nurseUser)).thenReturn("refresh.token");

        // Act
        LoginResponse response = authService.login(nurseLoginRequest);

        // Assert
        assertEquals("nurse", response.getRole(), "Role should be 'nurse'");
    }

    @Test
    void testGenerateAndStoreRefreshToken() {
        // Generate refresh token
        UserDetails userDetails = testUser; // assuming User implements UserDetails
        String token = refreshTokenService.generateRefreshToken(userDetails);

        // Verify token is stored
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(testUser);
        assertThat(tokens).hasSize(1);
        RefreshToken storedToken = tokens.get(0);
        assertThat(storedToken.getToken()).isEqualTo(token);
        assertThat(storedToken.isRevoked()).isFalse();
        assertThat(storedToken.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void testRevokeRefreshToken() {
        // Generate token
        String token = refreshTokenService.generateRefreshToken(testUser);

        // Revoke token
        refreshTokenService.revokeRefreshToken(token);

        // Verify it is revoked
        Optional<RefreshToken> revokedToken = refreshTokenRepository.findByToken(token);
        assertTrue(revokedToken.isPresent());
        assertThat(revokedToken.get().isRevoked()).isTrue();
    }

    @Test
    void testRevokeAllUserTokens() {
        // Generate multiple tokens
        String token1 = refreshTokenService.generateRefreshToken(testUser);
        String token2 = refreshTokenService.generateRefreshToken(testUser);

        // Revoke all
        refreshTokenService.revokeAllUserTokens(testUser);

        // Verify all tokens are revoked
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(testUser);
        assertThat(tokens).allMatch(RefreshToken::isRevoked);
    }
}