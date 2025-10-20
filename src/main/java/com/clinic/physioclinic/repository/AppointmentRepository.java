package com.clinic.physioclinic.repository;

import com.clinic.physioclinic.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Patient "My Appointments"
    List<Appointment> findByPatientIdOrderByStartTimeAsc(Long patientId);

    // Doctor day schedule (half-open [start, end) window)
    @Query("""
           SELECT a FROM Appointment a
           WHERE a.doctorId = :doctorId
             AND a.startTime >= :start
             AND a.startTime <  :end
           ORDER BY a.startTime ASC
           """)
    List<Appointment> findForDoctorOnDay(@Param("doctorId") Long doctorId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end")   LocalDateTime end);

    // Prevent overlapping bookings (start < end && end > start)
    @Query("""
           SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
           FROM Appointment a
           WHERE a.doctorId = :doctorId
             AND a.startTime < :end
             AND a.endTime   > :start
           """)
    boolean existsOverlapping(@Param("doctorId") Long doctorId,
                              @Param("start") LocalDateTime start,
                              @Param("end")   LocalDateTime end);

    // Week query used by service
    List<Appointment> findByDoctorIdAndStartTimeBetween(Long doctorId,
                                                        LocalDateTime start,
                                                        LocalDateTime end);

    boolean existsByDoctorIdAndStartTime(Long doctorId, LocalDateTime start);

    // (Optional derived equivalents shown previously)
}
