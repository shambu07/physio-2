// src/main/java/com/clinic/physioclinic/repository/UserRepository.java
package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
