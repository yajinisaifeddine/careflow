package com.careflow.auth;

import com.careflow.dtos.auth.RefreshTokenRequest;
import com.careflow.dtos.auth.RefreshTokenResponse;
import com.careflow.models.User;
import com.careflow.repositories.RoleRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.services.auth.AuthService;
import com.careflow.services.auth.JwtService;
import com.careflow.services.auth.RefreshTokenService;
import com.careflow.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Utilisation de @SpringBootTest ou @ExtendWith(MockitoExtension.class) est nécessaire en réalité
// J'utilise ici @SpringBootTest pour simplifier les imports dans cet environnement
@SpringBootTest
public class AuthRefreshTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtUtils jwtUtils;
    // Ajouté un import statique pour les méthodes Mockito.any/never
    @InjectMocks
    private AuthService authService;


    private RefreshTokenRequest request;
    private User user;

    // Déclarations des valeurs attendues pour le cas nominal
    private final String expectedNewAccessToken = "newAccessTokenAfterRefresh";
    private final String expectedNewRefreshToken = "newRefreshTokenAfterRefresh";

    @BeforeEach
    void setUp() {
        request = new RefreshTokenRequest();
        request.setToken("dummyRefreshToken");

        user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
    }

    /**
     * Teste le cas nominal où un token de rafraîchissement valide est fourni.
     * Le service doit générer un nouvel Access Token ET un nouveau Refresh Token (rotation).
     */
    @Test
    void validRefreshToken_returnsNewTokens(){
        // Arrange
        when(jwtUtils.extractUsername("dummyRefreshToken")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // 2. Validation of the old token
        when(jwtUtils.isTokenValid("dummyRefreshToken", user)).thenReturn(true);

        // 3. Generation of the NEW tokens (Crucial step for token rotation)
        when(jwtService.generateAccessToken(user)).thenReturn(expectedNewAccessToken);
        when(refreshTokenService.generateRefreshToken(user)).thenReturn(expectedNewRefreshToken);

        // Act
        RefreshTokenResponse response = authService.refresh(request);

        // Assert
        // 1. Verify response contents
        assertEquals(expectedNewAccessToken, response.getAccessToken(), "Should return a new Access Token");
        assertEquals(expectedNewRefreshToken, response.getRefreshToken(), "Should return a new Refresh Token (rotation)");

        // 2. Verify interactions
        verify(jwtUtils).extractUsername("dummyRefreshToken");
        verify(userRepository).findByEmail("user@example.com");
        verify(jwtUtils).isTokenValid("dummyRefreshToken", user);
        verify(jwtService).generateAccessToken(user);  // Ensure the access token was generated
        verify(refreshTokenService).generateRefreshToken(user); // Ensure the refresh token was rotated
    }

    /**
     * Teste le scénario où le token est corrompu (signature invalide, malformé).
     * Ceci provoque une RuntimeException (via JJWT) qui est capturée et re-jetée en InvalidBearerTokenException.
     */
    @Test
    void corruptRefreshToken_throwsInvalidBearerTokenException(){
        // Arrange
        // Mock extractUsername to throw a generic RuntimeException (simulating JJWT error)
        when(jwtUtils.extractUsername("dummyRefreshToken"))
                .thenThrow(new RuntimeException("JJWT Signature Error Simulated"));

        // Act & Assert
        assertThrows(InvalidBearerTokenException.class, () -> {
            authService.refresh(request);
        }, "Should throw InvalidBearerTokenException when token is corrupt.");

        verify(jwtUtils).extractUsername("dummyRefreshToken");
        verify(userRepository, Mockito.never()).findByEmail(any());
        verify(jwtUtils, Mockito.never()).isTokenValid(any(), any());
    }

    /**
     * Teste le scénario où le token n'est plus considéré comme valide
     * (e.g., il est expiré après les 30 jours, ou a été révoqué manuellement dans un système stateful).
     * Ceci provoque une InvalidBearerTokenException dans le bloc 'if (!jwtService.isTokenValid)'
     */
    @Test
    void expiredOrInvalidRefreshToken_throwsInvalidBearerTokenException(){
        // Arrange
        when(jwtUtils.extractUsername("dummyRefreshToken")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        // Mock isTokenValid to return false
        when(jwtUtils.isTokenValid("dummyRefreshToken", user)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidBearerTokenException.class, () -> {
            authService.refresh(request);
        }, "Should throw InvalidBearerTokenException when isTokenValid returns false.");

        verify(jwtUtils).isTokenValid("dummyRefreshToken", user);
        verify(jwtService, Mockito.never()).generateAccessToken(any());
        verify(refreshTokenService, Mockito.never()).generateRefreshToken(any());
    }

    /**
     * Teste le scénario où le token de rafraîchissement est manquant (null).
     * Ceci est géré par la première garde 'if (oldRefreshToken == null)'.
     */
    @Test
    void missingRefreshToken_throwsInvalidBearerTokenException() {
        // Arrange
        request.setToken(null);

        // Act & Assert
        assertThrows(InvalidBearerTokenException.class, () -> {
            authService.refresh(request);
        }, "Should throw InvalidBearerTokenException when token is null.");

        // Verify no service calls were made
        verify(jwtUtils, Mockito.never()).extractUsername(any());
        verify(userRepository, Mockito.never()).findByEmail(any());
    }

    /**
     * Teste le scénario où l'email extrait du token ne correspond à aucun utilisateur en base.
     */
    @Test
    void userNotFoundForToken_throwsInvalidBearerTokenException() {
        // Arrange
        when(jwtUtils.extractUsername("dummyRefreshToken")).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidBearerTokenException.class, () -> {
            authService.refresh(request);
        }, "Should throw UserNotFoundException if user extracted from token doesn't exist.");

        verify(jwtUtils).extractUsername("dummyRefreshToken");
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(jwtUtils, Mockito.never()).isTokenValid(any(), any());
    }
}