package com.careflow.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.careflow.dtos.auth.RegisterRequest;
import com.careflow.dtos.auth.RegisterResponse;
import com.careflow.exceptions.auth.AccessDeniedException;
import com.careflow.exceptions.auth.RoleNotFoundException;
import com.careflow.exceptions.auth.UserAlreadyExistsException;
import com.careflow.models.Role;
import com.careflow.models.User;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.services.auth.AuthService;
import com.careflow.services.auth.JwtService;

public class AuthRegisterTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager; // Not used in register, but kept for completeness

    @InjectMocks
    private AuthService authService;

    private RegisterRequest request;

    // Constants for cleaner tests
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PASSWORD = "Password123*";
    private static final String ROLE_USER_NAME = "ROLE_USER";
    private static final String ROLE_ADMIN_NAME = "ROLE_ADMIN";
    private static final String MOCKED_TOKEN = "mocked.jwt.token";

    @BeforeEach
    void setUp() {
        // Initialize mocks before each test
        MockitoAnnotations.openMocks(this);

        // Initialize the request object with valid, non-admin data
        request = new RegisterRequest();
        request.setEmail(VALID_EMAIL);
        request.setFullName("John Doe");
        request.setPassword(VALID_PASSWORD);
        request.setRole("user");
    }

    @Test
    void registerWithValidCredentials_success() {
        // 1. Mock: User does not exist
        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);

        // 2. Mock: Find the 'ROLE_USER' role
        Role mockUserRole = Role.builder().name(ROLE_USER_NAME).build();
        when(roleRepository.findByName(ROLE_USER_NAME)).thenReturn(Optional.of(mockUserRole));

        // 3. Mock: Password encoding
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

        // 4. Mock: Saving the User
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 5. Mock: JWT generation
        when(jwtService.generateToken(any(User.class))).thenReturn(MOCKED_TOKEN);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(MOCKED_TOKEN, response.getToken());
        assertEquals(VALID_EMAIL, response.getEmail());

        // Verify key interactions
        verify(userRepository, times(1)).existsByEmail(VALID_EMAIL);
        verify(roleRepository, times(1)).findByName(ROLE_USER_NAME);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerWithExistingEmail_throwsUserAlreadyExistsException() {
        // Arrange: User exists
        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request),
                "Should throw UserAlreadyExistsException when email is taken");

        // Verify no further interactions (like role lookup or save) happened
        verify(roleRepository, times(0)).findByName(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void userCantSelfAssignToAdmin_throwsAccessDeniedException() {
        // Arrange
        request.setRole("admin");
        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);

        // Mock the repository to return the ROLE_ADMIN object when requested
        Role mockAdminRole = Role.builder().name(ROLE_ADMIN_NAME).build();
        when(roleRepository.findByName(ROLE_ADMIN_NAME)).thenReturn(Optional.of(mockAdminRole));

        // Act & Assert
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> authService.register(request),
                "Should throw AccessDeniedException when trying to register as admin");

        // Verify the exception message matches the service implementation
        assertEquals("cannot assign to admin", thrown.getMessage());

        // Verify that saving/encoding was skipped
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void registerWithNullRole_defaultsToPatient() {
        // Arrange
        request.setRole(null); // Test with null role
        String defaultRoleName = "ROLE_PATIENT";
        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);

        // Mock the default 'ROLE_PATIENT' role
        Role mockPatientRole = Role.builder().name(defaultRoleName).build();
        when(roleRepository.findByName(defaultRoleName)).thenReturn(Optional.of(mockPatientRole));

        // Mock remaining successful steps
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(jwtService.generateToken(any(User.class))).thenReturn(MOCKED_TOKEN);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        authService.register(request);

        // Assert: Verify that the correct default role was looked up
        verify(roleRepository, times(1)).findByName(defaultRoleName);
    }

    @Test
    void registerWithUnknownRole_throwsRoleNotFoundException() {
        // Arrange
        request.setRole("CFO");
        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);
        // The service logic transforms "CFO" to "ROLE_CFO" and we mock it not being
        // found
        when(roleRepository.findByName("ROLE_CFO")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () -> authService.register(request),
                "Should throw RoleNotFoundException if the role does not exist in the database");

        // Verify the correct (transformed) role name was looked up
        verify(roleRepository, times(1)).findByName("ROLE_CFO");
    }
}
