package com.clinic.physioclinic.api;

import java.util.List;

public record ValidationError(String error, List<FieldErrorDto> fieldErrors) {}
