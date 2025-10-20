-- Add audit timestamps to appointment
ALTER TABLE appointment
    ADD COLUMN created_at TIMESTAMP(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE appointment
    ADD COLUMN updated_at TIMESTAMP(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6);
