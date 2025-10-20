CREATE TABLE appointments (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              patient_email VARCHAR(255) NOT NULL,
                              doctor_name   VARCHAR(255) NOT NULL,
                              start_time    TIMESTAMP NOT NULL,
                              end_time      TIMESTAMP NOT NULL,
                              status        VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
                              notes         TEXT,
                              created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appt_patient_email ON appointments(patient_email);
