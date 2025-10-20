// src/main/java/com/clinic/physioclinic/repository/DoctorRepository.java
package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Eagerly fetch the linked User to avoid LazyInitializationException in controller
    @Query("""
           SELECT d
           FROM Doctor d
           LEFT JOIN FETCH d.user u
           WHERE LOWER(u.email) = LOWER(:email)
           """)
    Optional<Doctor> findByUserEmailIgnoreCase(String email);
}
