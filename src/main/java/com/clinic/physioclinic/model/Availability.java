package com.clinic.physioclinic.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id","dayOfWeek"}))
public class Availability {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Doctor doctor;

    /** 1=Monday .. 7=Sunday (ISO) */
    @Column(nullable=false) private Integer dayOfWeek;

    @Column(nullable=false) private LocalTime startTime;
    @Column(nullable=false) private LocalTime endTime;

    /** slot granularity in minutes for this day */
    @Column(nullable=false) private Integer slotMinutes;
}
