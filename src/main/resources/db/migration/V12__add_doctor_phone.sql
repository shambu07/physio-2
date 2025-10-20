-- V12: add doctor phone (idempotent; safe on MySQL)
SET @sql := IF (
  (SELECT COUNT(*)
     FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME   = 'doctors'
       AND COLUMN_NAME  = 'phone') = 0,
  'ALTER TABLE doctors ADD COLUMN phone VARCHAR(25) NULL AFTER specialization',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
