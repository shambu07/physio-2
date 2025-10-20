package com.clinic.physioclinic.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentTimeUpdateRequest(
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {}
