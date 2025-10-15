package com.clinic.physioclinic.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(indexes = @Index(columnList = "availability_id"))
public class AvailabilityBreak {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Availability availability;

    @Column(nullable=false) private LocalTime startTime;
    @Column(nullable=false) private LocalTime endTime;
}
