// src/main/java/com/clinic/physioclinic/config/SecurityConfig.java
package com.clinic.physioclinic.config;

import com.clinic.physioclinic.repository.UserRepository;
import com.clinic.physioclinic.security.JwtAuthenticationFilter;
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

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // ===== Shared Beans =====

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByEmail(username)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .disabled(!u.isEnabled())
                        .authorities(u.getRoles().stream()
                                .map(r -> "ROLE_" + r.name())
                                .toArray(String[]::new))
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

    // ===== Chain 1: Actuator endpoints (Order 0) =====
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
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                );
        return http.build();
    }

    // ===== Chain 2: Application endpoints (Order 1) =====
    @Bean
    @Order(1)
    public SecurityFilterChain appSecurity(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .securityMatcher(
                        "/", "/index.html", "/favicon.ico", "/assets/**",
                        "/webjars/**", "/swagger-ui.html", "/swagger-ui/**",
                        "/v3/api-docs/**", "/api/**"
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, ex) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )
                .authorizeHttpRequests(reg -> reg
                        // Public static & API docs
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico", "/assets/**", "/webjars/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // Auth endpoints public
                        .requestMatchers("/api/auth/**").permitAll()

                        // Allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Patients endpoints require PATIENT role
                        .requestMatchers("/api/patients/**").hasRole("PATIENT")
                        .requestMatchers("/api/doctors/**").hasRole("DOCTOR")
                        .requestMatchers("/api/doctors/**").hasRole("DOCTOR")

                        // Appointments rules (keep your logic)
                        .requestMatchers(HttpMethod.GET, "/api/appointments/patient/**").hasAnyRole("PATIENT","DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/appointments").hasAnyRole("DOCTOR","ADMIN")
                        .requestMatchers("/api/appointments/**").authenticated()

                        // Everything else under /api requires JWT
                        .requestMatchers("/api/**").authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
