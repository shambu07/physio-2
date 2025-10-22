package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByIdempotencyKeyAndEndpoint(String idempotencyKey, String endpoint);
}
