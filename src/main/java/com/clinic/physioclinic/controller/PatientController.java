// src/main/java/com/clinic/physioclinic/controller/PatientController.java
package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.model.Patient;
import com.clinic.physioclinic.repository.PatientRepository;
import com.clinic.physioclinic.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patients;
    private final UserRepository users;

    public PatientController(PatientRepository patients, UserRepository users) {
        this.patients = patients;
        this.users = users;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Patient p : patients.findAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", p.getId());
            row.put("name", p.getUser() != null ? p.getUser().getFullName() : p.getName());
            row.put("email", p.getEmail());
            row.put("phone", p.getPhone());
            out.add(row);
        }
        return out;
    }

    // ✅ Current user's profile
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        String email = auth.getName(); // set by JwtAuthenticationFilter
        Patient p = patients.findByUserEmailIgnoreCase(email)
                .or(() -> patients.findByEmailIgnoreCase(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("name", p.getName());
        out.put("email", p.getEmail());
        out.put("phone", p.getPhone());
        return out;
    }

    // (Optional) self-heal: create & link if missing
    @PostMapping("/link-me")
    public Map<String, Object> linkMe(Authentication auth, @RequestBody(required = false) Map<String, String> body) {
        String email = auth.getName();

        return patients.findByUserEmailIgnoreCase(email)
                .or(() -> patients.findByEmailIgnoreCase(email))
                .map(existing -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("id", existing.getId());
                    out.put("name", existing.getName());
                    out.put("email", existing.getEmail());
                    out.put("phone", existing.getPhone());
                    return out;
                })
                .orElseGet(() -> {
                    var user = users.findByEmail(email)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
                    var p = new Patient();
                    p.setUser(user);
                    p.setName(body != null && body.get("name") != null ? body.get("name") : user.getFullName());
                    p.setEmail(email);
                    p.setPhone(body != null ? body.get("phone") : null);
                    var saved = patients.save(p);
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("id", saved.getId());
                    out.put("name", saved.getName());
                    out.put("email", saved.getEmail());
                    out.put("phone", saved.getPhone());
                    return out;
                });
    }
}
