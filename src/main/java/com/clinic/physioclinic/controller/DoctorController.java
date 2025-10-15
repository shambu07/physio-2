package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.IdNameDto;
import com.clinic.physioclinic.model.Doctor;
import com.clinic.physioclinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.clinic.physioclinic.dto.IdNameDto;

import java.util.List;

@RestController @RequestMapping("/api/doctors") @RequiredArgsConstructor @CrossOrigin
public class DoctorController {
    private final DoctorRepository repo;

    @GetMapping public List<IdNameDto> list() {
        return repo.findAll().stream().map(d -> new IdNameDto(d.getId(), d.getName())).toList();
    }

    @GetMapping("/{id}") public Doctor one(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }
}
