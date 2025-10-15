package com.clinic.physioclinic.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String specialization;
    /** default slot minutes per doctor; can be overridden in Availability */
    @Column(nullable=false) private Integer defaultSlotMinutes;

    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;

    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
