package com.careflow.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.careflow.services.auth.AuthService;
import com.careflow.services.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class AuthRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Role patientRole;
    private Role doctorRole;
    private Role nurseRole;
    private Role adminRole;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Setup roles
        patientRole = new Role();
        patientRole.setId(1L);
        patientRole.setName("ROLE_PATIENT");

        doctorRole = new Role();
        doctorRole.setId(2L);
        doctorRole.setName("ROLE_DOCTOR");

        nurseRole = new Role();
        nurseRole.setId(3L);
        nurseRole.setName("ROLE_NURSE");

        adminRole = new Role();
        adminRole.setId(4L);
        adminRole.setName("ROLE_ADMIN");

        // Setup default register request
        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .role("PATIENT")
                .build();
    }

    @Test
    void testRegisterWithValidPatientData_returnsRegisterResponseWithToken() {
        // Arrange
        String encodedPassword = "encodedPassword123";
        String expectedAccessToken = "access.token.jwt";

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_PATIENT")).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn(expectedAccessToken);

        // Act
        RegisterResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(registerRequest.getEmail(), response.getEmail(), "Email should match");
        assertEquals(registerRequest.getFullName(), response.getFullName(), "Full name should match");
        assertEquals("patient", response.getRole(), "Role should be 'patient'");
        assertEquals(expectedAccessToken, response.getAccessToken(), "Token should match");

        // Verify user was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(registerRequest.getFullName(), savedUser.getFullName());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals("local", savedUser.getProvider());
        assertEquals(patientRole, savedUser.getRole());
    }

    @Test
    void testRegisterWithExistingEmail_throwsUserAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registerRequest),
                "Should throw UserAlreadyExistsException when email already exists"
        );

        assertTrue(exception.getMessage().contains(registerRequest.getEmail()),
                "Exception message should contain the email");

        // Verify that no user was saved
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void testRegisterWithAdminRole_throwsAccessDeniedException() {
        // Arrange
        RegisterRequest adminRequest = RegisterRequest.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password("password123")
                .role("ADMIN")
                .build();

        when(userRepository.existsByEmail(adminRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> authService.register(adminRequest),
                "Should throw AccessDeniedException when trying to register as ADMIN"
        );

        assertEquals("cannot assign to admin", exception.getMessage());

        // Verify that no user was saved
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void testRegisterWithNonExistentRole_throwsRoleNotFoundException() {
        // Arrange
        RegisterRequest invalidRoleRequest = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .role("INVALID_ROLE")
                .build();

        when(userRepository.existsByEmail(invalidRoleRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_INVALID_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> authService.register(invalidRoleRequest),
                "Should throw RoleNotFoundException when role doesn't exist"
        );

        assertTrue(exception.getMessage().contains("INVALID_ROLE"),
                "Exception message should contain the role name");

        // Verify that no user was saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterWithNullRole_defaultsToPatient() {
        // Arrange
        RegisterRequest requestWithoutRole = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("password123")
                .role(null)
                .build();

        when(userRepository.existsByEmail(requestWithoutRole.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_PATIENT")).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("token");

        // Act
        RegisterResponse response = authService.register(requestWithoutRole);

        // Assert
        assertEquals("patient", response.getRole(), "Should default to PATIENT role");
        verify(roleRepository, times(1)).findByName("ROLE_PATIENT");
    }

    @Test
    void testRegisterWithBlankRole_defaultsToPatient() {
        // Arrange
        RegisterRequest requestWithBlankRole = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("password123")
                .role("   ")
                .build();

        when(userRepository.existsByEmail(requestWithBlankRole.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_PATIENT")).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("token");

        // Act
        RegisterResponse response = authService.register(requestWithBlankRole);

        // Assert
        assertEquals("patient", response.getRole(), "Should default to PATIENT role when blank");
        verify(roleRepository, times(1)).findByName("ROLE_PATIENT");
    }

    @Test
    void testRegisterWithDoctorRole_returnsCorrectRole() {
        // Arrange
        RegisterRequest doctorRequest = RegisterRequest.builder()
                .fullName("Dr. Smith")
                .email("doctor@example.com")
                .password("password123")
                .role("DOCTOR")
                .build();

        when(userRepository.existsByEmail(doctorRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("token");

        // Act
        RegisterResponse response = authService.register(doctorRequest);

        // Assert
        assertEquals("doctor", response.getRole(), "Role should be 'doctor'");
    }

    @Test
    void testRegisterWithNurseRole_returnsCorrectRole() {
        // Arrange
        RegisterRequest nurseRequest = RegisterRequest.builder()
                .fullName("Nurse Jane")
                .email("nurse@example.com")
                .password("password123")
                .role("NURSE")
                .build();

        when(userRepository.existsByEmail(nurseRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_NURSE")).thenReturn(Optional.of(nurseRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("token");

        // Act
        RegisterResponse response = authService.register(nurseRequest);

        // Assert
        assertEquals("nurse", response.getRole(), "Role should be 'nurse'");
    }

    @Test
    void testRegisterWithLowercaseRole_convertsToUppercase() {
        // Arrange
        RegisterRequest lowercaseRoleRequest = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .role("doctor")
                .build();

        when(userRepository.existsByEmail(lowercaseRoleRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("token");

        // Act
        RegisterResponse response = authService.register(lowercaseRoleRequest);

        // Assert
        assertEquals("doctor", response.getRole());
        verify(roleRepository, times(1)).findByName("ROLE_DOCTOR");
    }

    @Test
    void testRegisterWithRolePrefixAlreadyPresent_handlesCorrectly() {
        // Arrange
        RegisterRequest roleWithPrefixRequest = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .role("ROLE_PATIENT")
                .build();

        when(userRepository.existsByEmail(roleWithPrefixRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_PATIENT")).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("token");

        // Act
        RegisterResponse response = authService.register(roleWithPrefixRequest);

        // Assert
        assertEquals("patient", response.getRole());
        verify(roleRepository, times(1)).findByName("ROLE_PATIENT");
    }

    @Test
    void testRegisterPasswordIsEncoded() {
        // Arrange
        String plainPassword = "plainPassword123";
        String encodedPassword = "encodedPassword456";

        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password(plainPassword)
                .role("PATIENT")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_PATIENT")).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("token");

        // Act
        authService.register(request);

        // Assert
        verify(passwordEncoder, times(1)).encode(plainPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPassword(),
                "Password should be encoded before saving");
        assertNotEquals(plainPassword, savedUser.getPassword(),
                "Plain password should not be stored");
    }
}