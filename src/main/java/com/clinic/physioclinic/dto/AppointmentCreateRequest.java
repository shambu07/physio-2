package com.clinic.physioclinic.dto;

import com.clinic.physioclinic.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentCreateRequest(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull String type,
        String notes,
        // optional; defaulted to SCHEDULED in service if null
        AppointmentStatus status
) {}
