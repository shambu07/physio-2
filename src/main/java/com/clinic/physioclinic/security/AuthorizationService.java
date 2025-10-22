// src/main/java/com/clinic/physioclinic/security/AuthorizationService.java
package com.clinic.physioclinic.security;

import com.clinic.physioclinic.repository.AppointmentRepository; // ⬅ add
import com.clinic.physioclinic.repository.DoctorRepository;
import com.clinic.physioclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class AuthorizationService {

    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final AppointmentRepository appointmentRepo; // ⬅ add

    /** Used by: @PreAuthorize("hasRole('PATIENT') and @authz.isPatientSelf(#patientId)") */
    public boolean isPatientSelf(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (patientId == null || auth == null || auth.getName() == null) return false;

        return patientRepo.findByUserEmailIgnoreCase(auth.getName())
                .map(p -> p.getId().equals(patientId))
                .orElse(false);
    }

    /** Optional: for symmetry on doctor endpoints: @authz.isDoctorSelf(#doctorId) */
    public boolean isDoctorSelf(Long doctorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (doctorId == null || auth == null || auth.getName() == null) return false;

        return doctorRepo.findByUserEmailIgnoreCase(auth.getName())
                .map(d -> d.getId().equals(doctorId))
                .orElse(false);
    }

    /** ✅ Used by GET /api/appointments/{id} */
    public boolean canViewAppointment(Long apptId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return false;

        var isDoctor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        var isAdmin  = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isDoctor || isAdmin) return true;

        // Patients can view their own appointments
        return appointmentRepo.findById(apptId).map(a ->
                patientRepo.findByUserEmailIgnoreCase(auth.getName())
                        .map(p -> p.getId().equals(a.getPatientId()))
                        .orElse(false)
        ).orElse(false);
    }
}
