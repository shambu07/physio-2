package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.SlotDto;
import com.clinic.physioclinic.entity.Appointment;
import com.clinic.physioclinic.repository.AppointmentRepository;
import com.clinic.physioclinic.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin // allow Vite dev server on 5173
@RequiredArgsConstructor
public class SlotsController {

    private final SlotService slotService;
    private final AppointmentRepository appointmentRepo;

    /** Patient: Find free slots for a date */
    @GetMapping("/slots")
    public List<SlotDto> slots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return slotService.freeSlots(doctorId, date);
    }

    /** Doctor/Admin: Day schedule (existing appointments for that date) */
    @GetMapping("/schedule")
    public List<Appointment> schedule(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        var start = date.atStartOfDay();
        var end = date.plusDays(1).atStartOfDay();
        return appointmentRepo.findByDoctorIdAndStartTimeBetween(doctorId, start, end);
    }
}
