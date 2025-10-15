-- Rename camelCase columns to snake_case (keep types the same)
ALTER TABLE availability
    CHANGE COLUMN dayOfWeek   day_of_week   INT  NOT NULL,
    CHANGE COLUMN startTime   start_time    TIME NOT NULL,
    CHANGE COLUMN endTime     end_time      TIME NOT NULL,
    CHANGE COLUMN slotMinutes slot_minutes  INT  NOT NULL;

-- (Optional but helpful) add an index commonly used by lookups
CREATE INDEX idx_availability_doctor_dow ON availability (doctor_id, day_of_week);
