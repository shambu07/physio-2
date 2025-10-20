package com.clinic.physioclinic.mapper;

import com.clinic.physioclinic.dto.AppointmentCreateRequest;
import com.clinic.physioclinic.dto.AppointmentResDto;
import com.clinic.physioclinic.entity.Appointment;
import com.clinic.physioclinic.model.AppointmentStatus;

public final class AppointmentMapper {
    private AppointmentMapper() {}

    // Keep for compatibility if any code still calls it
    public static AppointmentResDto toResDto(Appointment a) {
        return AppointmentResDto.from(a);
    }

    // Optional helper to build an entity from a create request
    public static Appointment fromCreate(AppointmentCreateRequest req) {
        Appointment e = new Appointment();
        e.setPatientId(req.patientId());
        e.setDoctorId(req.doctorId());
        e.setStartTime(req.startTime());
        e.setEndTime(req.endTime());
        e.setType(req.type());
        e.setStatus(req.status() != null ? req.status().name() : AppointmentStatus.SCHEDULED.name());
        e.setNotes(req.notes());
        return e;
    }
}
