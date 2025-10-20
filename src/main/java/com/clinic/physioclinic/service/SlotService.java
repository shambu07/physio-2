package com.clinic.physioclinic.service;

import com.clinic.physioclinic.dto.SlotDto;
import com.clinic.physioclinic.entity.Appointment;
import com.clinic.physioclinic.model.Availability;
import com.clinic.physioclinic.model.AvailabilityBreak;
import com.clinic.physioclinic.model.Doctor;
import com.clinic.physioclinic.repository.AppointmentRepository;
import com.clinic.physioclinic.repository.AvailabilityBreakRepository;
import com.clinic.physioclinic.repository.AvailabilityRepository;
import com.clinic.physioclinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final DoctorRepository doctorRepo;
    private final AvailabilityRepository availabilityRepo;
    private final AvailabilityBreakRepository breakRepo;
    private final AppointmentRepository appointmentRepo;

    /** Generate free slots for a doctor on a given date, honoring breaks and existing appointments. */
    public List<SlotDto> freeSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();
        int dow = date.getDayOfWeek().getValue(); // 1..7

        Availability av = availabilityRepo.findByDoctorIdAndDayOfWeek(doctor.getId(), dow).orElse(null);
        if (av == null) return List.of();

        LocalTime start = av.getStartTime();
        LocalTime end   = av.getEndTime();
        int slotMinutes = av.getSlotMinutes();
        if (start == null || end == null || slotMinutes <= 0) return List.of();

        LocalDateTime dayStart = date.atTime(start);
        LocalDateTime dayEnd   = date.atTime(end);
        Duration step = Duration.ofMinutes(slotMinutes);

        // Breaks for the day
        List<AvailabilityBreak> breaks = breakRepo.findByAvailabilityId(av.getId());

        // Already booked
        List<Appointment> appts = appointmentRepo.findByDoctorIdAndStartTimeBetween(
                doctorId, dayStart, dayEnd
        );

        List<SlotDto> free = new ArrayList<>();
        for (LocalDateTime t = dayStart; !t.plus(step).isAfter(dayEnd); t = t.plus(step)) {
            LocalDateTime tEnd = t.plus(step);

            // inside any break?
            boolean inBreak = false;
            for (AvailabilityBreak b : breaks) {
                if (b.getStartTime() == null || b.getEndTime() == null) continue;
                LocalDateTime bs = date.atTime(b.getStartTime());
                LocalDateTime be = date.atTime(b.getEndTime());
                if (!t.isBefore(bs) && t.isBefore(be)) { // [bs, be)
                    inBreak = true;
                    break;
                }
            }
            if (inBreak) continue;

            // already taken? (avoid lambda capturing non-final variable)
            boolean taken = false;
            for (Appointment a : appts) {
                if (a.getStartTime().equals(t)) {
                    taken = true;
                    break;
                }
            }

            if (!taken) {
                free.add(new SlotDto(t, tEnd));
            }
        }
        return free;
    }
}
