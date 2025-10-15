package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    Optional<Availability> findByDoctorIdAndDayOfWeek(Long doctorId, Integer dayOfWeek);
}
