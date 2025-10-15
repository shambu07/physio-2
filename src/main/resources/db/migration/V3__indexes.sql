CREATE INDEX idx_appt_doctor_start ON appointment (doctor_id, startTime);
CREATE INDEX idx_appt_patient_start ON appointment (patient_id, startTime);
ALTER TABLE appointment ADD CONSTRAINT uq_doctor_start UNIQUE (doctor_id, startTime);
