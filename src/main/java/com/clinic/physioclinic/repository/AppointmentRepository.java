package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Was looked for as findByPatientIdOrderByStartAsc – rename in service to ...StartTimeAsc
    List<Appointment> findByPatientIdOrderByStartTimeAsc(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndStartTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    // What your service expects as "findForDoctorOnDay"
    @Query("""
           SELECT a FROM Appointment a
           WHERE a.doctorId = :doctorId
             AND a.startTime >= :startOfDay
             AND a.startTime <  :endOfDay
           ORDER BY a.startTime ASC
           """)
    List<Appointment> findForDoctorOnDay(@Param("doctorId") Long doctorId,
                                         @Param("startOfDay") LocalDateTime startOfDay,
                                         @Param("endOfDay") LocalDateTime endOfDay);

    // What your service expects as "existsOverlapping"
    // Overlap if NOT (existing ends before start OR existing starts after/at end)
    @Query("""
           SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
             FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND NOT (a.endTime <= :start OR a.startTime >= :end)
           """)
    boolean existsOverlapping(@Param("doctorId") Long doctorId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);
}
