package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    Optional<Availability> findByDoctorIdAndDayOfWeek(Long doctorId, int dayOfWeek);
}
