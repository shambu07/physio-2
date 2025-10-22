// src/main/java/com/clinic/physioclinic/config/SecurityConfig.java
package com.clinic.physioclinic.config;

import com.clinic.physioclinic.repository.UserRepository;
import com.clinic.physioclinic.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // ===== Shared beans =====

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Loads users by email; adjust to findByEmail(...) if that's your repo method.
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByEmailIgnoreCase(username)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .disabled(!u.isEnabled())
                        .authorities(
                                u.getRoles().stream()
                                        .map(r -> "ROLE_" + r.name())
                                        .toArray(String[]::new)
                        )
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Content-Type", "Location"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // Use one ObjectMapper for security JSON responses
    @Bean
    public ObjectMapper securityObjectMapper() {
        return new ObjectMapper();
    }

    // Small helper to consistently write JSON errors
    private void writeJsonError(HttpServletResponse res, int status, String message, String path) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        securityObjectMapper().writeValue(res.getWriter(), Map.of(
                "status", status,
                "error", message,
                "path", path
        ));
    }

    // ===== Chain 1: Actuator (management) endpoints =====
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        // expose others only if you want them public; otherwise require auth here
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", req.getRequestURI()))
                        .accessDeniedHandler((req, res, e) ->
                                writeJsonError(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden", req.getRequestURI()))
                );
        return http.build();
    }

    // ===== Chain 2: Application endpoints (API + docs + static) =====
    @Bean
    @Order(1)
    public SecurityFilterChain appSecurity(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // Match all app routes including /api/**
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                writeJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", req.getRequestURI()))
                        .accessDeniedHandler((req, res, ex) ->
                                writeJsonError(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden", req.getRequestURI()))
                )
                .authorizeHttpRequests(reg -> reg
                        // Public static + OpenAPI/Swagger
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico", "/assets/**",
                                "/webjars/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // Auth endpoints are public
                        .requestMatchers("/api/auth/**").permitAll()

                        // Allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Domain-specific rules (keep only if you actually have these top-level routes)
                        .requestMatchers("/api/patients/**").hasRole("PATIENT")
                        .requestMatchers("/api/doctors/**").hasRole("DOCTOR")

                        // Appointments:
                        .requestMatchers(HttpMethod.GET, "/api/appointments").hasAnyRole("DOCTOR", "ADMIN")
                        // All other appointment endpoints require auth; method security enforces ownership/roles
                        .requestMatchers("/api/appointments/**").authenticated()

                        // Everything else under /api must be authenticated
                        .requestMatchers("/api/**").authenticated()

                        // Any other unmatched paths (e.g., SPA catch-all) are permitted
                        .anyRequest().permitAll()
                )
                // Ensure JWT filter runs for ALL /api/** (don’t whitelist inside the filter)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
