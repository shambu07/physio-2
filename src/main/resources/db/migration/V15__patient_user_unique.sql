-- V15__patient_user_unique.sql
ALTER TABLE patient ADD UNIQUE INDEX uq_patient_user_id (user_id);
