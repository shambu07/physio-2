package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.model.Doctor;
import com.clinic.physioclinic.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctors;

    public DoctorController(DoctorRepository doctors) {
        this.doctors = doctors;
    }

    // === 1. List all doctors (admin/general use)
    @GetMapping
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Doctor d : doctors.findAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", d.getId());
            row.put("name", d.getUser() != null ? d.getUser().getFullName() : null);
            row.put("specialization", d.getSpecialization());
            out.add(row);
        }
        return out;
    }

    // === 2. Authenticated doctor's own profile
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        String email = auth.getName();
        Doctor d = doctors.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", d.getId());
        out.put("name", d.getUser() != null ? d.getUser().getFullName() : null);
        out.put("email", email);
        out.put("specialization", d.getSpecialization());
        return out;
    }
}
