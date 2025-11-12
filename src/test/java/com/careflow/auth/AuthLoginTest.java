package com.careflow.auth;

import com.careflow.dtos.auth.LoginRequest;
import com.careflow.dtos.auth.LoginResponse;
import com.careflow.models.Role;
import com.careflow.models.User;
import com.careflow.repositories.RefreshTokenRepository;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.services.auth.AuthService;
import com.careflow.services.auth.JwtService;
import com.careflow.services.auth.RefreshTokenService;
import com.careflow.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
class AuthLoginTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

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
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    private User user;
    private Role rolePatient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup Role
        rolePatient = Role.builder().name("ROLE_PATIENT").build();

        // Setup User
        user = User.builder().fullName("si fawzi").email("sifawzi@gmail.com").password("password") // raw password
                .role(rolePatient).provider("local").build();

        // Mock repository calls
        when(userRepository.findByEmail("sifawzi@gmail.com")).thenReturn(Optional.of(user));

        // Mock password encoder
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);


        // Mock token services
        when(jwtService.generateAccessToken(user)).thenReturn("mocked-access-token");
        when(refreshTokenService.generateRefreshToken(user)).thenReturn("mocked-refresh-token");


    }

    @Test
    void testLoginWithValidCredentials_returnAccessAndRefreshToken() {
        // When: login is called
        // Setup LoginRequest and expected LoginResponse
        loginRequest = LoginRequest.builder().email("sifawzi@gmail.com").password("password").build();

        loginResponse = LoginResponse.builder().accessToken("mocked-access-token").refreshToken("mocked-refresh-token").build();
        var resp = authService.login(loginRequest);
        // Then: tokens match expected values
        assertEquals(loginResponse.getAccessToken(), resp.getAccessToken());
        assertEquals(loginResponse.getRefreshToken(), resp.getRefreshToken());

        // Verify mocks were called correctly
        verify(jwtService, times(1)).generateAccessToken(user);
        verify(refreshTokenService, times(1)).generateRefreshToken(user);
    }

    @Test
    void testLoginWithInvalidCredentials_throwsBadCredentialsException() {
        //given
        loginRequest = LoginRequest.builder().email("monsieuslimen@gmail.com").password("monsieuslimen").build();

        //when
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("invalid credetials"));

        //then
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

    }


}
