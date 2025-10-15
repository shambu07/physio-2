CREATE TABLE patient (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         created_at TIMESTAMP(6) NOT NULL,
                         updated_at TIMESTAMP(6) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE doctor (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(255) NOT NULL,
                        specialization VARCHAR(255) NOT NULL,
                        default_slot_minutes INT NOT NULL,
                        created_at TIMESTAMP(6) NOT NULL,
                        updated_at TIMESTAMP(6) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE availability (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              doctor_id BIGINT NOT NULL,
                              dayOfWeek INT NOT NULL, -- 1..7
                              startTime TIME NOT NULL,
                              endTime TIME NOT NULL,
                              slotMinutes INT NOT NULL,
                              CONSTRAINT uq_doctor_day UNIQUE (doctor_id, dayOfWeek),
                              CONSTRAINT fk_av_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(id)
                                  ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE availability_break (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    availability_id BIGINT NOT NULL,
                                    startTime TIME NOT NULL,
                                    endTime TIME NOT NULL,
                                    INDEX idx_break_av (availability_id),
                                    CONSTRAINT fk_break_av FOREIGN KEY (availability_id) REFERENCES availability(id)
                                        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE appointment (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             patient_id BIGINT NOT NULL,
                             doctor_id BIGINT NOT NULL,
                             startTime TIMESTAMP(6) NOT NULL, -- UTC
                             endTime   TIMESTAMP(6) NOT NULL, -- UTC
                             type VARCHAR(20) NOT NULL,
                             status VARCHAR(20) NOT NULL,
                             notes VARCHAR(1000),
                             CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patient(id),
                             CONSTRAINT fk_appt_doctor  FOREIGN KEY (doctor_id) REFERENCES doctor(id),
                             CONSTRAINT chk_type CHECK (type IN ('CONSULTATION','FOLLOW_UP','PHYSIO')),
                             CONSTRAINT chk_status CHECK (status IN ('SCHEDULED','CANCELLED','COMPLETED'))
) ENGINE=InnoDB;
