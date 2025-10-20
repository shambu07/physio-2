// src/main/java/com/clinic/physioclinic/controller/AuthController.java
package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.AuthRequest;
import com.clinic.physioclinic.dto.AuthResponse;
import com.clinic.physioclinic.dto.RegisterRequest;
import com.clinic.physioclinic.model.Doctor;
import com.clinic.physioclinic.model.Patient;
import com.clinic.physioclinic.model.Role;
import com.clinic.physioclinic.model.User;
import com.clinic.physioclinic.repository.DoctorRepository;
import com.clinic.physioclinic.repository.PatientRepository;
import com.clinic.physioclinic.repository.UserRepository;
import com.clinic.physioclinic.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository users,
                          PatientRepository patients,
                          DoctorRepository doctors,
                          PasswordEncoder encoder,
                          AuthenticationManager authManager,
                          JwtUtil jwtUtil) {
        this.users = users;
        this.patients = patients;
        this.doctors = doctors;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        User u = new User();
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(req.getRole());
        u.setRoles(roles);
        users.save(u);

        if (req.getRole() == Role.PATIENT) {
            Patient p = new Patient();
            p.setUser(u);
            // ✅ satisfy NOT NULL columns on patient
            p.setName(u.getFullName());
            p.setEmail(u.getEmail());
            p.setPhone(req.getPhone());
            patients.save(p);
        } else if (req.getRole() == Role.DOCTOR) {
            Doctor d = new Doctor();
            d.setUser(u);
            d.setSpecialization(req.getSpecialization());
            doctors.save(d);
        }

        var token = jwtUtil.generate(u.getEmail(), roles.stream().map(Enum::name).toList());
        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        u.getFullName(),
                        u.getEmail(),
                        roles.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        var email = auth.getName();
        var u = users.findByEmail(email).orElseThrow();

        var token = jwtUtil.generate(u.getEmail(), u.getRoles().stream().map(Enum::name).toList());
        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        u.getFullName(),
                        u.getEmail(),
                        u.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
                )
        );
    }
}
