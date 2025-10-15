// src/main/java/com/clinic/physioclinic/api/ApiError.java
package com.clinic.physioclinic.api;

import java.time.OffsetDateTime;

public record ApiError(int status, String error, String path, OffsetDateTime timestamp) {
    public static ApiError of(int status, String error, String path) {
        return new ApiError(status, error, path, OffsetDateTime.now());
    }
}