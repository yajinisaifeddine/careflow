package com.careflow.auth;

import com.careflow.dtos.auth.RegisterRequest;
import com.careflow.dtos.auth.RegisterResponse;
import com.careflow.models.Role;
import com.careflow.models.User;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.services.auth.AuthService;
import com.careflow.services.auth.JwtService;
import com.careflow.services.auth.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;


    private Role rolePatient = new Role(1l, "ROLE_PATIENT");
    private Role roleDoctor = new Role(2l, "ROLE_DOCTOR");
    private Role roleAdmin = new Role(3l, "ROLE_ADMIN");
    private RegisterRequest request;
    private RegisterResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(roleRepository.save(rolePatient)).thenReturn(Role.builder().name("ROLE_PATIENT").build());
        roleRepository.save(roleAdmin);
        roleRepository.save(roleAdmin);

    }

    @Test
    void testRegisterWithValidUserNameAndPassword_thenReturnsAccessAndRefreshToken() {
        //given
        request = RegisterRequest.builder()
                .email("s@a.f")
                .password("Password-1")
                .fullName("si fawei")
                .role(rolePatient.getName())
                .build();
        //when
        when(authService.register(request)).thenReturn(RegisterResponse.builder().accessToken("dummy-access-token").refreshToken("dummy-refreToken").build());
        response = authService.register(request);

        User user = userRepository.findByEmail(response.getEmail()).orElse(null);
        //then
        assertNotNull(user);
        assertEquals(response.getAccessToken(), "dummy-access-token");
        assertEquals(response.getRefreshToken(), "dummy-refreToken");


        verify(jwtService, times(1)).generateAccessToken(user);
        verify(refreshTokenService, times(1)).generateRefreshToken(user);

    }
}