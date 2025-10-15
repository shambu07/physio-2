package com.clinic.physioclinic.dto;

import java.time.LocalDate;
import java.util.List;

public record DoctorDayResponse(
        LocalDate day,
        List<AppointmentResDto> appointments
) {}
