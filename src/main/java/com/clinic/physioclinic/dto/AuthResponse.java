// src/main/java/com/clinic/physioclinic/dto/AuthResponse.java
package com.clinic.physioclinic.dto;

import java.util.Set;

public class AuthResponse {
    private String token;          // access token (unchanged)
    private String refreshToken;   // ✅ new field
    private String fullName;
    private String email;
    private Set<String> roles;

    // === constructors ===
    public AuthResponse(String token, String fullName, String email, Set<String> roles) {
        this.token = token;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }

    // ✅ new constructor with refresh token
    public AuthResponse(String token, String refreshToken, String fullName, String email, Set<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }

    // === getters ===
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
}
