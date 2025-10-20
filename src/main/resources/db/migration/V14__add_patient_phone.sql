-- V14: add 'phone' to patient if it's missing (portable across MySQL variants)
SET @sql := IF (
  (SELECT COUNT(*)
     FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME  = 'patient'
      AND COLUMN_NAME = 'phone') = 0,
  'ALTER TABLE patient ADD COLUMN phone VARCHAR(25) NULL AFTER email',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
