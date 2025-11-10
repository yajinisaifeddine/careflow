package com.careflow.services.auth;

import com.careflow.models.RefreshToken;
import com.careflow.models.User;
import com.careflow.repositories.RefreshTokenRepository;
import com.careflow.repositories.UserRepository;
import com.careflow.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;


@Slf4j
@AllArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private  final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }



    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken =  refreshTokenRepository.findByToken(token).orElseThrow(()->{
        throw new InvalidBearerTokenException("token is invalid");});
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.updateRevokedByUser(user,true);
    }
    public String generateRefreshToken(UserDetails userDetails) {
        log.info("generating refresh token with user details");
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // 1️⃣ Generate the JWT token string
        log.info("building jwt");
        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(jwtUtils.generateRefreshTokenExpirationDate())
                .signWith(jwtUtils.getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        // 2️⃣ Save the token in the database
        log.info("finding user with jwt");
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("faild to find user");
                    return new UsernameNotFoundException("User not found");
                });
log.info("saving token to database");
        RefreshToken refreshToken = RefreshToken.builder()
                                .token(token)
                                .user(user)
                                .expiresAt(jwtUtils.generateRefreshTokenExpirationDate())
                                .revoked(false)
                                .createdAt(new Date())
                                .updatedAt(new Date()).build();


        refreshTokenRepository.save(refreshToken);

        // 3️⃣ Return the token string to the client
        return token;
    }

}