package com.clinic.physioclinic.dto;

import java.time.Instant;

public class AppointmentDto {
    public Long id;
    public Long patientId;
    public Long doctorId;
    public Instant startTime;
    public Instant endTime;
    public String type;
    public String status;
    public String notes;

    public AppointmentDto(Long id, Long patientId, Long doctorId, Instant startTime, Instant endTime, String type, String status, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.status = status;
        this.notes = notes;
    }
}
