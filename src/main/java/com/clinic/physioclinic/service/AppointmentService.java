package com.clinic.physioclinic.service;

import com.clinic.physioclinic.dto.AppointmentCreateRequest;
import com.clinic.physioclinic.dto.AppointmentResDto;
import com.clinic.physioclinic.dto.DoctorDayResponse;
import com.clinic.physioclinic.dto.DoctorWeekResponse;
import com.clinic.physioclinic.dto.DaySlotsResponse;
import com.clinic.physioclinic.entity.Appointment;
import com.clinic.physioclinic.model.AppointmentStatus;
import com.clinic.physioclinic.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository apptRepo;
    private final AvailabilityService availabilityService;

    public AppointmentService(AppointmentRepository apptRepo,
                              AvailabilityService availabilityService) {
        this.apptRepo = apptRepo;
        this.availabilityService = availabilityService;
    }

    /* ---------- helpers ---------- */

    public List<AppointmentResDto> getAll() {
        return apptRepo.findAll()
                .stream()
                .map(AppointmentResDto::from)
                .toList();
    }

    public List<AppointmentResDto> getByPatient(Long patientId) {
        return apptRepo.findByPatientIdOrderByStartTimeAsc(patientId)
                .stream()
                .map(AppointmentResDto::from)
                .toList();
    }

    public List<AppointmentResDto> getForDoctorOnDay(Long doctorId, LocalDate day) {
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return apptRepo.findForDoctorOnDay(doctorId, startOfDay, endOfDay)
                .stream()
                .map(AppointmentResDto::from)
                .toList();
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

        Appointment entity = new Appointment();
        entity.setPatientId(patientId);
        entity.setDoctorId(doctorId);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setType(type);
        entity.setStatus(status);
        entity.setNotes(notes);

        return AppointmentResDto.from(apptRepo.save(entity));
    }

    public AppointmentResDto updateTimes(Long appointmentId,
                                         LocalDateTime startTime,
                                         LocalDateTime endTime) {
        Appointment entity = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        if (hasOverlap(entity.getDoctorId(), startTime, endTime)) {
            throw new IllegalArgumentException("Overlapping appointment for doctor " + entity.getDoctorId());
        }

        entity.setStartTime(startTime);
        entity.setEndTime(endTime);

        return AppointmentResDto.from(apptRepo.save(entity));
    }

    /** For AvailabilityController */
    public DaySlotsResponse getDoctorFreeSlots(Long doctorId, LocalDate day) {
        return availabilityService.getFreeSlots(doctorId, day);
    }

    /* ---------- methods used by controllers ---------- */

    public List<AppointmentResDto> patientAppointments(long patientId) {
        return getByPatient(patientId);
    }

    public DoctorDayResponse doctorDay(Long doctorId, LocalDate day) {
        var list = getForDoctorOnDay(doctorId, day);
        return new DoctorDayResponse(day, list);
    }

    public DoctorWeekResponse doctorWeek(Long doctorId, LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = start.plusDays(7);
        var list = apptRepo.findByDoctorIdAndStartTimeBetween(doctorId, start, end)
                .stream()
                .map(AppointmentResDto::from)
                .toList();
        return new DoctorWeekResponse(weekStart, weekStart.plusDays(6), list);
    }

    public AppointmentResDto book(AppointmentCreateRequest req) {
        String status = (req.status() != null ? req.status().name() : AppointmentStatus.SCHEDULED.name());
        return create(
                req.patientId(),
                req.doctorId(),
                req.startTime(),
                req.endTime(),
                req.type(),
                status,
                req.notes()
        );
    }

    public AppointmentResDto updateStatus(Long appointmentId, AppointmentStatus status) {
        Appointment entity = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
        entity.setStatus(status.name());
        return AppointmentResDto.from(apptRepo.save(entity));
    }
}
