package com.clinic.physioclinic.service;

import com.clinic.physioclinic.dto.AvailabilityUpsertRequest;
import com.clinic.physioclinic.dto.DaySlotsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AvailabilityService {

    // Inject your repositories as needed via constructor (omitted here)

    /**
     * Existing internal logic that returns raw free slots.
     * If you already have this, keep it as is.
     */
    public List<LocalDateTime> freeSlots(Long doctorId, LocalDate day) {
        // TODO: existing computation for free slots
        return List.of(); // <- replace with your real logic
    }

    /**
     * New wrapper used by controllers/services that expect a DTO.
     */
    public DaySlotsResponse getFreeSlots(Long doctorId, LocalDate day) {
        return new DaySlotsResponse(day, freeSlots(doctorId, day));
    }

    /**
     * Make sure this exists if your controller calls svc.upsert(...)
     */
    public void upsert(AvailabilityUpsertRequest request) {
        // TODO: implement save/update of availability (kept empty to compile)
    }
}
