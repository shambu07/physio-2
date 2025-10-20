// src/main/java/com/clinic/physioclinic/security/JwtUtil.java
package com.clinic.physioclinic.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration:86400000}") long expirationMs
    ) {
        byte[] decoded = Decoders.BASE64.decode(base64Secret);

        if (decoded.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes) when Base64-decoded.");
        }

        this.key = Keys.hmacShaKeyFor(decoded);
        this.expirationMs = expirationMs;
    }

    public String generate(String subjectEmail, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subjectEmail)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

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
