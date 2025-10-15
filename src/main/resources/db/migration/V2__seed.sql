INSERT INTO doctor (name, specialization, default_slot_minutes, created_at, updated_at)
VALUES ('Dr. Jane Physio', 'Physiotherapy', 30, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

INSERT INTO patient (name, email, created_at, updated_at)
VALUES ('John Patient', 'john.patient@example.com', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

-- M-F 09:00-17:00 with 30m slots, lunch break 12:00-13:00
-- dayOfWeek: Mon=1 .. Sun=7
INSERT INTO availability (doctor_id, dayOfWeek, startTime, endTime, slotMinutes)
VALUES
    (1,1,'09:00:00','17:00:00',30),
    (1,2,'09:00:00','17:00:00',30),
    (1,3,'09:00:00','17:00:00',30),
    (1,4,'09:00:00','17:00:00',30),
    (1,5,'09:00:00','17:00:00',30);

-- Breaks 12:00-13:00 for weekdays
INSERT INTO availability_break (availability_id, startTime, endTime)
SELECT id, '12:00:00','13:00:00' FROM availability WHERE doctor_id=1;
