// src/main/java/com/clinic/physioclinic/service/AppointmentService.java
package com.clinic.physioclinic.service;

import com.clinic.physioclinic.dto.AppointmentCreateRequest;
import com.clinic.physioclinic.dto.AppointmentResDto;
import com.clinic.physioclinic.entity.Appointment;
import com.clinic.physioclinic.model.AppointmentStatus;
import com.clinic.physioclinic.repository.AppointmentRepository;
import com.clinic.physioclinic.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.LocalDate; // ⬅ add
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository apptRepo;
    private final AvailabilityService availabilityService;
    private final DoctorRepository doctorRepo;

    public AppointmentService(AppointmentRepository apptRepo,
                              AvailabilityService availabilityService,
                              DoctorRepository doctorRepo) {
        this.apptRepo = apptRepo;
        this.availabilityService = availabilityService;
        this.doctorRepo = doctorRepo;
    }

    /* ---------- READ helpers ---------- */

    public List<AppointmentResDto> getAll() {
        return apptRepo.findAll().stream().map(AppointmentResDto::from).toList();
    }

    public List<AppointmentResDto> getByPatient(Long patientId) {
        return apptRepo.findByPatientIdOrderByStartTimeAsc(patientId)
                .stream().map(AppointmentResDto::from).toList();
    }

    public List<AppointmentResDto> getByDoctor(Long doctorId) {
        return apptRepo.findByDoctorIdOrderByStartTimeAsc(doctorId)
                .stream()
                .map(AppointmentResDto::from)
                .toList();
    }

    public Optional<AppointmentResDto> findById(Long id) {
        return apptRepo.findById(id).map(AppointmentResDto::from);
    }

    /** ✅ used by GET /api/appointments/{id} */
    public AppointmentResDto getByIdDto(Long id) {
        return apptRepo.findById(id)
                .map(AppointmentResDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    /** ✅ used by GET /api/appointments/doctor/{doctorId}/day?date=YYYY-MM-DD */
    public List<AppointmentResDto> getDoctorDay(Long doctorId, LocalDate date) {
        // If/when you store doctor TZ, use it; else default clinic TZ
        ZoneId zone = ZoneId.of("America/Chicago");

        ZonedDateTime zStart = date.atStartOfDay(zone);
        ZonedDateTime zEnd   = zStart.plusDays(1);

        LocalDateTime startUtc = LocalDateTime.ofInstant(zStart.toInstant(), ZoneOffset.UTC);
        LocalDateTime endUtc   = LocalDateTime.ofInstant(zEnd.toInstant(),   ZoneOffset.UTC);

        var list = apptRepo.findForDoctorOnDay(doctorId, startUtc, endUtc); // JPQL below if you need it
        return list.stream().map(AppointmentResDto::from).toList();
    }

    /* ---------- validation / overlap ---------- */

    public boolean hasOverlap(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return apptRepo.existsOverlapping(doctorId, start, end);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times are required");
        }
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End must be after start");
        }
    }

    /* ---------- create / update ---------- */

    public AppointmentResDto create(Long patientId,
                                    Long doctorId,
                                    LocalDateTime startTime,
                                    LocalDateTime endTime,
                                    String type,
                                    String status,
                                    String notes) {
        validateTimes(startTime, endTime);

        if (hasOverlap(doctorId, startTime, endTime)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Overlapping appointment for doctor " + doctorId
            );
        }

        var a = new Appointment();
        a.setPatientId(patientId);
        a.setDoctorId(doctorId);
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        a.setType(type);
        a.setStatus(status);
        a.setNotes(notes);
        return AppointmentResDto.from(apptRepo.save(a));
    }

    public AppointmentResDto updateTimes(Long id, LocalDateTime start, LocalDateTime end) {
        validateTimes(start, end);

        var a = apptRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        boolean unchanged = start.equals(a.getStartTime()) && end.equals(a.getEndTime());
        if (!unchanged && hasOverlap(a.getDoctorId(), start, end)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Overlapping appointment for doctor " + a.getDoctorId()
            );
        }

        a.setStartTime(start);
        a.setEndTime(end);
        return AppointmentResDto.from(apptRepo.save(a));
    }

    public AppointmentResDto updateTimesAsDoctor(Long apptId, LocalDateTime start, LocalDateTime end, String email) {
        var doc = doctorRepo.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        var appt = apptRepo.findById(apptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appt.getDoctorId().equals(doc.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another doctor's appointment");
        }

        return updateTimes(apptId, start, end);
    }

    public AppointmentResDto updateStatus(Long apptId, AppointmentStatus status) {
        var a = apptRepo.findById(apptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        a.setStatus(status.name());
        return AppointmentResDto.from(apptRepo.save(a));
    }

    public AppointmentResDto updateStatusAsDoctor(Long apptId, AppointmentStatus status, String email) {
        var doc = doctorRepo.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        var a = apptRepo.findById(apptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!a.getDoctorId().equals(doc.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change status of another doctor's appointment");
        }

        a.setStatus(status.name());
        return AppointmentResDto.from(apptRepo.save(a));
    }

    public List<AppointmentResDto> patientAppointments(long patientId) {
        return getByPatient(patientId);
    }

    public AppointmentResDto book(Long patientId, AppointmentCreateRequest req) {
        String status = (req.status() != null ? req.status().name() : AppointmentStatus.SCHEDULED.name());
        return create(patientId, req.doctorId(), req.startTime(), req.endTime(), req.type(), status, req.notes());
    }
}
