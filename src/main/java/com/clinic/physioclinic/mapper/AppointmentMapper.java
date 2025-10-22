// src/main/java/com/clinic/physioclinic/mapper/AppointmentMapper.java
package com.clinic.physioclinic.mapper;

import com.clinic.physioclinic.dto.AppointmentCreateRequest;
import com.clinic.physioclinic.entity.Appointment;
import com.clinic.physioclinic.model.AppointmentStatus;

public final class AppointmentMapper {

    private AppointmentMapper() {}

    /** Build an Appointment entity using patientId from JWT (controller/service) */
    public static Appointment toEntity(AppointmentCreateRequest req, Long patientId) {
        var a = new Appointment();
        a.setPatientId(patientId);                     // from auth context, not request body
        a.setDoctorId(req.doctorId());
        a.setStartTime(req.startTime());
        a.setEndTime(req.endTime());
        a.setType(req.type());
        a.setStatus(req.status() != null
                ? req.status().name()
                : AppointmentStatus.SCHEDULED.name());
        a.setNotes(req.notes());
        return a;
    }
}
