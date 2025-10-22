// src/main/java/com/clinic/physioclinic/repository/PatientRepository.java
package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Find patient via linked user.email (case-insensitive)
    Optional<Patient> findByUserEmailIgnoreCase(String email);
    // PatientRepository.java
    Optional<Patient> findByUser_Id(Long userId);

    // Fallback lookup by plain stored email
    Optional<Patient> findByEmailIgnoreCase(String email);
}