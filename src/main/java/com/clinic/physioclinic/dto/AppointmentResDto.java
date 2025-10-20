// src/main/java/com/clinic/physioclinic/dto/AppointmentResDto.java
package com.clinic.physioclinic.dto;

import com.clinic.physioclinic.entity.Appointment;
import java.util.Objects;

public record AppointmentResDto(
        Long id,
        Long patientId,
        Long doctorId,
        String startTime,  // ISO-8601 text e.g. "2025-10-19T10:00:00"
        String endTime,
        String type,
        String status,
        String notes
) {
    public static AppointmentResDto from(Appointment a) {
        return new AppointmentResDto(
                a.getId(),
                a.getPatientId(),
                a.getDoctorId(),
                a.getStartTime() != null ? a.getStartTime().toString() : null,
                a.getEndTime()   != null ? a.getEndTime().toString()   : null,
                Objects.toString(a.getType(), null),
                Objects.toString(a.getStatus(), null),
                a.getNotes()
        );
    }
}
