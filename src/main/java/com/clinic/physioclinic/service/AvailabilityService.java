package com.clinic.physioclinic.service;

import com.clinic.physioclinic.dto.AvailabilityUpsertRequest;
import com.clinic.physioclinic.dto.DaySlotsResponse;
import com.clinic.physioclinic.model.Availability;
import com.clinic.physioclinic.model.Doctor;
import com.clinic.physioclinic.repository.AvailabilityRepository;
import com.clinic.physioclinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepo;
    private final DoctorRepository doctorRepo;

    // If you don’t actually use this, you can remove it, but keep signature if referenced elsewhere
    public List<LocalDateTime> freeSlots(Long doctorId, LocalDate day) {
        return List.of();
    }

    public DaySlotsResponse getFreeSlots(Long doctorId, LocalDate day) {
        return new DaySlotsResponse(day, freeSlots(doctorId, day));
    }

    /** Upsert availability for the weekday of request.date (recurring weekly pattern). */
    public void upsert(AvailabilityUpsertRequest req) {
        // derive weekday 1..7 from the provided date
        int dow = req.date.getDayOfWeek().getValue();

        // load doctor (repo method in SlotService uses doctorId, so both paths are fine)
        Doctor doc = doctorRepo.findById(req.doctorId).orElseThrow();

        // find existing row for doctor + weekday, or create new
        Availability av = availabilityRepo
                .findByDoctorIdAndDayOfWeek(doc.getId(), dow)
                .orElseGet(() -> {
                    Availability a = new Availability();
                    a.setDoctor(doc);          // if your entity uses ManyToOne Doctor
                    a.setDayOfWeek(dow);
                    return a;
                });

        av.setStartTime(req.open);
        av.setEndTime(req.close);
        av.setSlotMinutes(req.slotMinutes);

        availabilityRepo.save(av);
    }
}
