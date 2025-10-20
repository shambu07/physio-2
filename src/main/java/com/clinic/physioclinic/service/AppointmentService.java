package com.clinic.physioclinic.service;

import com.clinic.physioclinic.dto.*;
import com.clinic.physioclinic.entity.Appointment;
import com.clinic.physioclinic.model.AppointmentStatus;
import com.clinic.physioclinic.repository.AppointmentRepository;
import com.clinic.physioclinic.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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

    /* ---------- helpers ---------- */

    public List<AppointmentResDto> getAll() {
        return apptRepo.findAll().stream().map(AppointmentResDto::from).toList();
    }

    public List<AppointmentResDto> getByPatient(Long patientId) {
        return apptRepo.findByPatientIdOrderByStartTimeAsc(patientId)
                .stream().map(AppointmentResDto::from).toList();
    }

    public List<AppointmentResDto> getByDoctor(Long doctorId) {
        return apptRepo.findByDoctorIdAndStartTimeBetween(
                doctorId, LocalDateTime.MIN, LocalDateTime.MAX
        ).stream().map(AppointmentResDto::from).toList();
    }

    public boolean hasOverlap(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return apptRepo.existsOverlapping(doctorId, start, end);
    }

    public AppointmentResDto create(Long patientId,
                                    Long doctorId,
                                    LocalDateTime startTime,
                                    LocalDateTime endTime,
                                    String type,
                                    String status,
                                    String notes) {
        if (hasOverlap(doctorId, startTime, endTime)) {
            throw new IllegalArgumentException("Overlapping appointment for doctor " + doctorId);
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
        if (start == null || end == null) throw new IllegalArgumentException("Start and end times are required");
        if (!end.isAfter(start)) throw new IllegalArgumentException("End must be after start");

        var a = apptRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        boolean unchanged = start.equals(a.getStartTime()) && end.equals(a.getEndTime());
        if (!unchanged && hasOverlap(a.getDoctorId(), start, end)) {
            throw new IllegalArgumentException("Overlapping appointment for doctor " + a.getDoctorId());
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

        if (!appt.getDoctorId().equals(doc.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another doctor's appointment");

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

        if (!a.getDoctorId().equals(doc.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change status of another doctor's appointment");

        a.setStatus(status.name());
        return AppointmentResDto.from(apptRepo.save(a));
    }

    public List<AppointmentResDto> patientAppointments(long patientId) {
        return getByPatient(patientId);
    }

    public AppointmentResDto book(AppointmentCreateRequest req) {
        String status = (req.status() != null ? req.status().name() : AppointmentStatus.SCHEDULED.name());
        return create(req.patientId(), req.doctorId(), req.startTime(), req.endTime(), req.type(), status, req.notes());
    }

    public Optional<AppointmentResDto> findById(Long id) {
        return apptRepo.findById(id).map(AppointmentResDto::from);
    }
}
