// src/main/java/com/clinic/physioclinic/controller/AppointmentController.java
package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.*;
import com.clinic.physioclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin
public class AppointmentController {
    private final AppointmentService svc;

    // NEW: base GET (optionally filter by patientId)
    @GetMapping
    public List<AppointmentResDto> list(@RequestParam(required = false) Long patientId) {
        long pid = (patientId != null) ? patientId : 1L; // mock me=1
        return svc.patientAppointments(pid);
    }

    // Mock "me": patientId=1 for now
    @GetMapping("/me")
    public List<AppointmentResDto> mine() {
        return svc.patientAppointments(1L);
    }

    @GetMapping("/doctor/{doctorId}")
    public DoctorDayResponse doctorDay(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return svc.doctorDay(doctorId, date);
    }

    @GetMapping("/doctor/{doctorId}/week")
    public DoctorWeekResponse doctorWeek(
            @PathVariable Long doctorId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return svc.doctorWeek(doctorId, weekStart);
    }

    // VALIDATE + return 201 Created
    @PostMapping
    public ResponseEntity<AppointmentResDto> book(@RequestBody @Valid AppointmentCreateRequest req) {
        var out = svc.book(req);
        return ResponseEntity.created(URI.create("/api/appointments/" + out.id())).body(out);
    }

    @PatchMapping("/{id}/status")
    public AppointmentResDto status(@PathVariable Long id, @RequestBody AppointmentStatusUpdateRequest req) {
        return svc.updateStatus(id, req.status());
    }
}
