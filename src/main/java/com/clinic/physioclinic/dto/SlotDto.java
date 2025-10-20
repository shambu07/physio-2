package com.clinic.physioclinic.dto;

import java.time.LocalDateTime;

public record SlotDto(LocalDateTime start, LocalDateTime end) {}
