// src/main/java/com/clinic/physioclinic/web/DevErrorAdvice.java
package com.clinic.physioclinic.web;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
class DevErrorAdvice {
    @ExceptionHandler(Exception.class)
    ResponseEntity<String> onAny(Exception ex) {
        String msg = ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "(no message)" : ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}
