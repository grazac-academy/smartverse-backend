package org.smartvert.smartvert.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.TokenPair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair generateTokenPair(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildTokenPair(userDetails.getUsername());
    }

    public TokenPair generateTokenPair(UserDetails userDetails) {
        return buildTokenPair(userDetails.getUsername());
    }

    public TokenPair buildTokenPair(String username) {
        String accessToken = generateAccessToken(username);
        String refreshToken = generateRefreshToken(username);
        return new TokenPair(accessToken, refreshToken);
    }

    public String generateAccessToken(String username) {
        return generateToken(username, jwtProperties.getExpirationMs(), Map.of());
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, jwtProperties.getRefreshExpirationMs(), Map.of());
    }

    public String generateToken(String username, long expirationMs, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder();
        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        String email = claims.get("email", String.class);
        return email != null ? email : claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
