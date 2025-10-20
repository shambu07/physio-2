// src/main/java/com/clinic/physioclinic/security/JwtAuthenticationFilter.java
package com.clinic.physioclinic.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
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

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    var jws = jwt.parse(token);
                    String email = jws.getBody().getSubject();

                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) jws.getBody().get("roles", List.class);

                    Collection<? extends GrantedAuthority> authorities =
                            roles == null ? List.of()
                                    : roles.stream()
                                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                    .collect(Collectors.toList());

                    var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (JwtException | IllegalArgumentException ex) {
                    // Bad/expired token -> do not authenticate; let entry point return 401
                    SecurityContextHolder.clearContext();
                }
            }
        }

        chain.doFilter(request, response);
    }
}
