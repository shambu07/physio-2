package com.clinic.physioclinic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityUpsertRequest {
    @NotNull public Long doctorId;
    @NotNull public LocalDate date;
    @NotNull public LocalTime open;   // e.g., 09:00
    @NotNull public LocalTime close;  // e.g., 17:00
    @Min(5) public int slotMinutes = 30;
}
