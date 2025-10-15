-- V8__add_end_time_column.sql
-- Add appointment.end_time only if it doesn't already exist

SET @need_add :=
  (SELECT COUNT(*) = 0
   FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'appointment'
     AND COLUMN_NAME = 'end_time');

SET @sql := IF(@need_add,
  'ALTER TABLE appointment ADD COLUMN end_time TIMESTAMP NULL',
  'SELECT 1');

PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;
