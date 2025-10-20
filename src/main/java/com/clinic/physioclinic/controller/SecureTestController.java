package com.clinic.physioclinic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SecureTestController {

    @GetMapping("/secure-test")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<?> secureTest(Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "message", "Access granted",
                "user", auth != null ? auth.getName() : "anonymous",
                "authorities", auth != null ? auth.getAuthorities() : "none"
        ));
    }
}
