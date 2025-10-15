package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.IdNameDto;
import com.clinic.physioclinic.model.Patient;
import com.clinic.physioclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.clinic.physioclinic.dto.IdNameDto;

import java.util.List;

@RestController @RequestMapping("/api/patients") @RequiredArgsConstructor @CrossOrigin
public class PatientController {
    private final PatientRepository repo;

    @GetMapping public List<IdNameDto> list() {
        return repo.findAll().stream().map(p -> new IdNameDto(p.getId(), p.getName())).toList();
    }

    @GetMapping("/{id}") public Patient one(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }
}
