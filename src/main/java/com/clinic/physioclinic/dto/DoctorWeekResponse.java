package com.clinic.physioclinic.dto;

import java.time.LocalDate;
import java.util.List;

public record DoctorWeekResponse(
        LocalDate start,
        LocalDate end,
        List<AppointmentResDto> appointments
) {}
