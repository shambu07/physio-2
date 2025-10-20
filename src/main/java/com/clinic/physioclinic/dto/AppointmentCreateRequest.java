package com.clinic.physioclinic.dto;

import com.clinic.physioclinic.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;

/**
 * DTO used for creating a new Appointment.
 * Mapped from JSON payload in POST /api/appointments.
 */
public record AppointmentCreateRequest(

        /** ID of the patient booking the appointment */
        @NotNull(message = "patientId must not be null")
        Long patientId,

        /** ID of the doctor for whom the appointment is scheduled */
        @NotNull(message = "doctorId must not be null")
        Long doctorId,

        /** Appointment start time (ISO 8601 format: yyyy-MM-dd'T'HH:mm:ss) */
        @NotNull(message = "startTime must not be null")
        @Future(message = "startTime must be in the future")
        LocalDateTime startTime,

        /** Appointment end time */
        @NotNull(message = "endTime must not be null")
        @Future(message = "endTime must be in the future")
        LocalDateTime endTime,

        /** Type of appointment (e.g., CONSULTATION, FOLLOW_UP, THERAPY_SESSION) */
        @NotNull(message = "type must not be null")
        String type,

        /** Optional notes provided by patient or doctor */
        String notes,

        /**
         * Optional field: if not provided, defaults to SCHEDULED
         * This is handled inside AppointmentService before saving.
         */
        AppointmentStatus status
) {}
