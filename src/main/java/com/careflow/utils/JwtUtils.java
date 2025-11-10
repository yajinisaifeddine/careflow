package com.careflow.utils;

import com.careflow.exceptions.auth.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtils {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY ;


    @Value("${jwt.access-token.expiration}")
    private   long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token.expiration}")
    private  long REFRESH_TOKEN_EXPIRATION;
    public Date generateAccessTokenExpirationDate(){
        return  new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION); // 15 minuts
    }
    public Date generateRefreshTokenExpirationDate(){
        return  new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION); // 30 days
    }

    // === Validate token ===
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // === Check expiration ===
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public  Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // === Internal helpers ===
    public Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
        catch (JwtException e){
            throw  new ExpiredJwtException("token is expired");
        }catch (Exception e) {
            log.error("invalid token",e);
            throw new InvalidBearerTokenException("invalid token");
        }
    }

    public Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    // === Extract any claim using a resolver ===
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}
