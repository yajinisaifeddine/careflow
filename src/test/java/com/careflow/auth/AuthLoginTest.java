package com.careflow.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.careflow.dtos.auth.LoginRequest; // Corrected package import
import com.careflow.dtos.auth.LoginResponse; // Corrected package import
import com.careflow.models.Role; // Necessary for mocking user roles
import com.careflow.models.User;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.services.auth.AuthService;
import com.careflow.services.auth.JwtService;

public class AuthLoginTest {

    // Dependencies to be injected into AuthService
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    // NOTE: The mock declaration for LoginResponse 'response' has been removed
    // as the response object should be returned by the service.

    @InjectMocks
    private AuthService authService;

    private com.careflow.dtos.auth.LoginRequest request;

    // Constants for cleaner tests
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PASSWORD = "Password123*";
    private static final String MOCKED_TOKEN = "mocked.jwt.token";

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Prepare a standard login request
        request = new LoginRequest();
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
    }

    @Test
    void testLoginWithValidCredentials_returnsLoginResponseWithTokenAndUserDetails() {
        // Mock data necessary for the LoginResponse
        final String mockUserName = "John Doe";
        final String mockRoleName = "ROLE_PATIENT";
        final String expectedRoleName = "patient";

        // 1. Arrange: Define the authenticated User object (mocked to get full data)
        User mockUser = mock(User.class);
        when(mockUser.getEmail()).thenReturn(VALID_EMAIL);
        when(mockUser.getFullName()).thenReturn(mockUserName);

        // Mock role logic (assuming the service uses the first role found)
        Role mockRole = mock(Role.class);
        when(mockRole.getName()).thenReturn(mockRoleName);
        when(mockUser.getRole()).thenReturn(mockRole);

        // 2. Arrange: Create a mock Authentication object
        Authentication mockAuthentication = mock(Authentication.class);

        // 3. Mock: Configure the Authentication object to return our user
        when(mockAuthentication.getPrincipal()).thenReturn(mockUser);

        // 4. Mock: Configure the AuthenticationManager to return the successful
        // Authentication object
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // 5. Mock: Configure the JwtService to generate a token for the user
        when(jwtService.generateToken(mockUser)).thenReturn(MOCKED_TOKEN);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(MOCKED_TOKEN, response.getToken());
        assertEquals(VALID_EMAIL, response.getEmail());
        assertEquals(mockUserName, response.getFullName());
        assertEquals(expectedRoleName, response.getRole());
    }

    @Test
    void testLoginWithInvalidCredentials_throwsBadCredentialsException() {
        // Arrange
        // Configure the AuthenticationManager to throw the standard Spring Security
        // exception when authentication fails (e.g., wrong password)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        // We expect the BadCredentialsException to be thrown by the service layer
        assertThrows(BadCredentialsException.class, () -> authService.login(request),
                "Should throw BadCredentialsException for invalid login credentials.");
    }
}
