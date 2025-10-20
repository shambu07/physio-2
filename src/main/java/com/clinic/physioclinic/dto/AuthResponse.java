package com.clinic.physioclinic.dto;


import java.util.Set;


public class AuthResponse {
    private String token;
    private String fullName;
    private String email;
    private Set<String> roles;


    public AuthResponse(String token, String fullName, String email, Set<String> roles) {
        this.token = token; this.fullName = fullName; this.email = email; this.roles = roles;
    }


    public String getToken() { return token; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
}