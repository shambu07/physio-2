package com.clinic.physioclinic.controller;

import com.clinic.physioclinic.dto.AvailabilityUpsertRequest;
import com.clinic.physioclinic.dto.DaySlotsResponse;
import com.clinic.physioclinic.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@CrossOrigin
public class AvailabilityController {
    private final AvailabilityService svc;

    @GetMapping("/{doctorId}")
    public DaySlotsResponse daySlots(@PathVariable Long doctorId,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return svc.getFreeSlots(doctorId, date);
    }

    @PostMapping
    public void upsert(@RequestBody @Valid AvailabilityUpsertRequest req) {
        svc.upsert(req);
    }
}
