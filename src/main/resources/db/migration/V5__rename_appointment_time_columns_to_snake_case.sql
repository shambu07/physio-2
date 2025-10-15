-- Align column names with Hibernate naming (snake_case)
ALTER TABLE appointment
    CHANGE COLUMN `startTime` `start_time` TIMESTAMP(6) NOT NULL,
    CHANGE COLUMN `endTime`   `end_time`   TIMESTAMP(6) NOT NULL;
