package com.clinic.physioclinic.dto;

import com.clinic.physioclinic.entity.Appointment;

import java.time.LocalDateTime;

public record AppointmentResDto(
        Long id,
        Long patientId,
        Long doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String type,
        String status,
        String notes
) {
    public static AppointmentResDto from(Appointment a) {
        // Use patientId/doctorId directly (no getPatient()/getDoctor() calls)
        return new AppointmentResDto(
                a.getId(),
                a.getPatientId(),
                a.getDoctorId(),
                a.getStartTime(),
                a.getEndTime(),
                a.getType(),
                a.getStatus(),
                a.getNotes()
        );
    }
}
