package com.clinic.physioclinic.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DaySlotsResponse(
        LocalDate day,
        List<LocalDateTime> freeSlots
) {}
