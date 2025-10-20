package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.AvailabilityBreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityBreakRepository extends JpaRepository<AvailabilityBreak, Long> {
    List<AvailabilityBreak> findByAvailabilityId(Long availabilityId);
}
