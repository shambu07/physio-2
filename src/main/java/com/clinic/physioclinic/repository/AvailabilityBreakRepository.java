package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.AvailabilityBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityBreakRepository extends JpaRepository<AvailabilityBreak, Long> {
    List<AvailabilityBreak> findByAvailabilityId(Long availabilityId);
}
