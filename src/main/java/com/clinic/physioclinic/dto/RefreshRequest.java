// src/main/java/com/clinic/physioclinic/dto/RefreshRequest.java
package com.clinic.physioclinic.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String refreshToken
) {}
