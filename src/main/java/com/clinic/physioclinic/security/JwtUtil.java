// src/main/java/com/clinic/physioclinic/security/JwtUtil.java
package com.clinic.physioclinic.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.access-expiration:3600000}") long accessExpirationMs,    // 1h default
            @Value("${app.jwt.refresh-expiration:604800000}") long refreshExpirationMs // 7d default
    ) {
        byte[] decoded = Decoders.BASE64.decode(base64Secret);
        if (decoded.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes) when Base64-decoded.");
        }
        this.key = Keys.hmacShaKeyFor(decoded);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // === ACCESS TOKEN ===
    public String generateAccessToken(String subjectEmail, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subjectEmail)
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpirationMs)))
                .signWith(key)
                .compact();
    }

    // === REFRESH TOKEN ===
    public String generateRefreshToken(String subjectEmail) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subjectEmail)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshExpirationMs)))
                .signWith(key)
                .compact();
    }

    public boolean isRefreshToken(String token) {
        try {
            String type = parse(token).getPayload().get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    // === EXISTING METHODS ===
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public String getSubject(String token) {
        return parse(token).getPayload().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) parse(token).getPayload().get("roles", List.class);
    }
}
