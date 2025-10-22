// src/main/java/com/clinic/physioclinic/repository/UserRepository.java
package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Lookups
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    // Existence checks (for signup, etc.)
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
}
