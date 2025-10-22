// src/main/java/com/clinic/physioclinic/controller/AppointmentController.java
package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.AppointmentCreateRequest;
import com.clinic.physioclinic.dto.AppointmentResDto;
import com.clinic.physioclinic.dto.AppointmentTimeUpdateRequest;
import com.clinic.physioclinic.model.AppointmentStatus;
import com.clinic.physioclinic.repository.DoctorRepository;
import com.clinic.physioclinic.repository.PatientRepository;
import com.clinic.physioclinic.service.AppointmentService;
import com.clinic.physioclinic.service.IdempotencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;              // ⬅ add
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;                                        // ⬅ add
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin
public class AppointmentController {

    private final AppointmentService svc;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final IdempotencyService idempotencyService;

    // ---------- NEW: GET by id ----------
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @authz.canViewAppointment(#id)")
    public AppointmentResDto getById(@PathVariable Long id) {
        return svc.getByIdDto(id); // returns 404 if not found
    }

    // ---------- NEW: Doctor day view ----------
    @GetMapping("/doctor/{doctorId}/day")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')") // ⬅ changed: avoid SpEL bean call to ensure clean 403 for patients
    public List<AppointmentResDto> doctorDay(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return svc.getDoctorDay(doctorId, date);
    }

    // ===================== READ (Doctor) =====================

    @GetMapping("/doctor/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResDto> doctorMine(Authentication auth) {
        var doc = doctorRepo.findByUserEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Doctor not found for: " + auth.getName()
                ));
        return svc.getByDoctor(doc.getId());
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<AppointmentResDto> doctorById(@PathVariable Long doctorId, Authentication auth) {
        var isDoctor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        if (isDoctor) {
            var me = doctorRepo.findByUserEmailIgnoreCase(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Doctor not found for: " + auth.getName()
                    ));
            if (!me.getId().equals(doctorId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Cannot view other doctor's appointments"
                );
            }
        }
        return svc.getByDoctor(doctorId);
    }

    // ===================== READ (Patient) =====================

    @GetMapping("/patient/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResDto> patientMine(Authentication auth) {
        var pat = patientRepo.findByUserEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patient not found for: " + auth.getName()
                ));
        return svc.getByPatient(pat.getId());
    }

    /** Alias for clients calling /api/appointments/mine */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResDto> mine(Authentication auth) {
        var pat = patientRepo.findByUserEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patient not found for: " + auth.getName()
                ));
        return svc.getByPatient(pat.getId());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or (hasRole('PATIENT') and @authz.isPatientSelf(#patientId))")
    public List<AppointmentResDto> patientById(@PathVariable("patientId") Long patientId) {
        return svc.getByPatient(patientId);
    }

    // ===================== WRITE (Doctor/Admin) =====================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public AppointmentResDto status(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    Authentication auth) {
        String raw = body.get("status");
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field 'status' is required");
        }
        final AppointmentStatus status;
        try {
            status = AppointmentStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status. Allowed: SCHEDULED, CANCELLED, COMPLETED"
            );
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin
                ? svc.updateStatus(id, status)
                : svc.updateStatusAsDoctor(id, status, auth.getName());
    }

    @PatchMapping("/{id}/time")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public AppointmentResDto reschedule(@PathVariable Long id,
                                        @RequestBody @Valid AppointmentTimeUpdateRequest req,
                                        Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin
                ? svc.updateTimes(id, req.startTime(), req.endTime())
                : svc.updateTimesAsDoctor(id, req.startTime(), req.endTime(), auth.getName());
    }

    // ===================== WRITE (Patient) =====================

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public AppointmentResDto book(
            // ✅ accept either header name (your client used X-Idempotency-Key)
            @RequestHeader(name = "X-Idempotency-Key", required = false) String idemKey1,
            @RequestHeader(name = "Idempotency-Key", required = false) String idemKey2,
            @RequestBody @Valid AppointmentCreateRequest req,
            Authentication auth) {

        String idemKey = (idemKey1 != null && !idemKey1.isBlank()) ? idemKey1 : idemKey2;

        var pat = patientRepo.findByUserEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patient not found for: " + auth.getName()
                ));

        doctorRepo.findById(req.doctorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Doctor not found: " + req.doctorId()
                ));

        final String normalizedStatus = (req.status() != null
                ? req.status().name()
                : AppointmentStatus.SCHEDULED.name());

        if (idemKey != null && !idemKey.isBlank()) {
            final String endpoint = "POST:/api/appointments";

            String hashSource = pat.getId() + "|" + req.doctorId() + "|"
                    + req.startTime() + "|" + req.endTime() + "|"
                    + req.type() + "|" + normalizedStatus + "|"
                    + Optional.ofNullable(req.notes()).orElse("");

            Long existingId = idempotencyService.checkOrReserve(endpoint, idemKey, hashSource);
            if (existingId != null) {
                return svc.findById(existingId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.CONFLICT, "Idempotent replay not available yet"));
            }

            var dto = svc.create(
                    pat.getId(),
                    req.doctorId(),
                    req.startTime(),
                    req.endTime(),
                    req.type(),
                    normalizedStatus,
                    req.notes()
            );
            idempotencyService.attachResponse(endpoint, idemKey, dto.id());
            return dto;
        }

        return svc.create(
                pat.getId(),
                req.doctorId(),
                req.startTime(),
                req.endTime(),
                req.type(),
                normalizedStatus,
                req.notes()
        );
    }
}
