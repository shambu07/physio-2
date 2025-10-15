ALTER TABLE availability_break
    CHANGE COLUMN startTime start_time TIME NOT NULL,
    CHANGE COLUMN endTime   end_time   TIME NOT NULL;
