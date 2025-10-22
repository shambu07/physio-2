// src/main/java/com/clinic/physioclinic/security/JwtAuthenticationFilter.java
package com.clinic.physioclinic.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;

    public JwtAuthenticationFilter(JwtUtil jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        // Be permissive: if there is no bearer header, just continue unauthenticated.
        String header = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(String::trim)
                .orElse(null);

        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) { // case-insensitive
            String token = header.substring(7).trim();
            if (!token.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    var jws = jwt.parse(token);
                    String email = Optional.ofNullable(jws.getBody().getSubject()).orElse("").trim();

                    if (!email.isEmpty()) {
                        // Extract authorities from several possible claim shapes.
                        Collection<? extends GrantedAuthority> authorities = extractAuthorities(jws.getBody());

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (JwtException | IllegalArgumentException ex) {
                    // Any JWT issue (expired, malformed, bad signature, etc.) -> no auth; let entrypoint/handlers respond.
                    SecurityContextHolder.clearContext();
                }
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Extracts authorities from common claim keys:
     * - "roles": List<String> or comma-separated String
     * - "authorities": List<String> or comma-separated String
     * - "scope"/"scp": space- or comma-separated String
     * Returns an empty list if none present. Always normalizes to SimpleGrantedAuthority with "ROLE_" if needed.
     */
    private Collection<? extends GrantedAuthority> extractAuthorities(io.jsonwebtoken.Claims claims) {
        Set<String> raw = new LinkedHashSet<>();

        // roles (List or String)
        addAllStrings(raw, claims.get("roles"));
        addAllStrings(raw, claims.get("authorities"));
        addAllStringsFromDelimited(raw, (String) claims.get("scope"), " ", ",");
        addAllStringsFromDelimited(raw, (String) claims.get("scp"), " ", ",");

        if (raw.isEmpty()) return List.of();

        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::toSpringRole)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    private String toSpringRole(String r) {
        // If already prefixed (e.g., ROLE_DOCTOR), keep it; else add ROLE_
        return r.startsWith("ROLE_") ? r : "ROLE_" + r;
    }

    @SuppressWarnings("unchecked")
    private void addAllStrings(Set<String> out, @Nullable Object value) {
        if (value == null) return;
        if (value instanceof Collection<?> c) {
            for (Object o : c) if (o != null) out.add(o.toString());
        } else {
            out.add(value.toString());
        }
    }

    private void addAllStringsFromDelimited(Set<String> out, @Nullable String value, String... seps) {
        if (value == null || value.isBlank()) return;
        String regex = Arrays.stream(seps).map(this::escape).collect(Collectors.joining("|"));
        for (String part : value.split(regex)) {
            if (!part.isBlank()) out.add(part.trim());
        }
    }

    private String escape(String s) {
        // minimal escape for regex metacharacters we might pass in (space/comma won't matter, but be safe)
        return s.replace("\\", "\\\\").replace("|", "\\|").replace(".", "\\.").replace("?", "\\?")
                .replace("*", "\\*").replace("+", "\\+").replace("[", "\\[").replace("]", "\\]")
                .replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}")
                .replace("^", "\\^").replace("$", "\\$");
    }
}
