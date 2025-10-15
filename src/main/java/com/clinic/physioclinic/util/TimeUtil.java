package com.clinic.physioclinic.util;

import java.time.*;

public final class TimeUtil {
    public static final ZoneId CLINIC_TZ = ZoneId.of("America/Chicago");

    private TimeUtil() {}

    public static Instant localDayStartInstant(LocalDate date) {
        return date.atStartOfDay(CLINIC_TZ).toInstant();
    }
    public static Instant localDayEndInstant(LocalDate date) {
        return date.plusDays(1).atStartOfDay(CLINIC_TZ).toInstant();
    }
    public static OffsetDateTime toClinicOffset(Instant instant) {
        return instant.atZone(CLINIC_TZ).toOffsetDateTime();
    }
}
