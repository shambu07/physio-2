package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.*;
import com.clinic.physioclinic.model.AppointmentStatus;
import com.clinic.physioclinic.repository.DoctorRepository;
import com.clinic.physioclinic.repository.PatientRepository;
import com.clinic.physioclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin
public class AppointmentController {

    private final AppointmentService svc;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;

    // ============ READ (Doctor) ============

    @GetMapping("/doctor/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResDto> doctorMine(Authentication auth) {
        var doc = doctorRepo.findByUserEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Doctor not found for: " + auth.getName()));
        return svc.getByDoctor(doc.getId());
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<AppointmentResDto> doctorById(@PathVariable Long doctorId, Authentication auth) {
        var isDoctor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        if (isDoctor) {
            var me = doctorRepo.findByUserEmailIgnoreCase(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Doctor not found for: " + auth.getName()));
            if (!me.getId().equals(doctorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot view other doctor's appointments");
            }
        }
        return svc.getByDoctor(doctorId);
    }

    // ============ READ (Patient) ============

    @GetMapping("/patient/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResDto> patientMine(Authentication auth) {
        var pat = patientRepo.findByUserEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Patient not found for: " + auth.getName()));
        return svc.getByPatient(pat.getId());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public List<AppointmentResDto> patientById(@PathVariable Long patientId, Authentication auth) {
        var isPatient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        if (isPatient) {
            var me = patientRepo.findByUserEmailIgnoreCase(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Patient not found for: " + auth.getName()));
            if (!me.getId().equals(patientId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot view other patient's appointments");
            }
        }
        return svc.getByPatient(patientId);
    }

    // ============ WRITE (Doctor/Admin) ============

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status. Allowed: SCHEDULED, CANCELLED, COMPLETED");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? svc.updateStatus(id, status)
                : svc.updateStatusAsDoctor(id, status, auth.getName());
    }

    @PatchMapping("/{id}/time")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public AppointmentResDto reschedule(@PathVariable Long id,
                                        @RequestBody @Valid AppointmentTimeUpdateRequest req,
                                        Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? svc.updateTimes(id, req.startTime(), req.endTime())
                : svc.updateTimesAsDoctor(id, req.startTime(), req.endTime(), auth.getName());
    }

    // ============ WRITE (Patient) ============

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public AppointmentResDto book(@RequestBody @Valid AppointmentCreateRequest req,
                                  Authentication auth) {
        // Bind patient identity to JWT, not to request body
        var pat = patientRepo.findByUserEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Patient not found for: " + auth.getName()));

        // Make sure doctor exists (avoid 500 if bad id)
        doctorRepo.findById(req.doctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Doctor not found: " + req.doctorId()));

        // Delegate to service using authenticated patient id
        var status = (req.status() != null ? req.status().name() : AppointmentStatus.SCHEDULED.name());
        return svc.create(
                pat.getId(),
                req.doctorId(),
                req.startTime(),
                req.endTime(),
                req.type(),
                status,
                req.notes()
        );
    }
}
