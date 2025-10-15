// src/main/java/.../advice/ApiExceptionHandler.java
package com.clinic.physioclinic.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException ex) {
        return "Validation failed";
    }

    // ❌ Do NOT do: @ExceptionHandler(Exception.class) -> 500 for everything, including '/'
}
