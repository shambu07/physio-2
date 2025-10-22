// src/main/java/com/clinic/physioclinic/dto/AppointmentCreateRequest.java
package com.clinic.physioclinic.dto;

import com.clinic.physioclinic.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AppointmentCreateRequest(
        @NotNull Long doctorId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull String type,
        String notes,
        AppointmentStatus status // optional; defaults to SCHEDULED
) {}
