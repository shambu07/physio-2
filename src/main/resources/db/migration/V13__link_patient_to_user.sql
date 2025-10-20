-- V13: link patient to user (works on older MySQL 8.0 minors; no procedural IF)

-- 1) add column only if missing (via INFORMATION_SCHEMA + prepared stmt)
SET @has_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'patients'
    AND COLUMN_NAME  = 'user_id'
);

SET @sql := IF(
  @has_col = 0,
  'ALTER TABLE patients ADD COLUMN user_id BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) ensure index on patients.user_id (name it consistently)
SET @idx_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'patients'
    AND INDEX_NAME   = 'idx_patients_user_id'
);

SET @sql := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_patients_user_id ON patients(user_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) add FK only if it doesn't already exist
SET @fk_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME        = 'patients'
    AND CONSTRAINT_NAME   = 'fk_patient_user'
    AND CONSTRAINT_TYPE   = 'FOREIGN KEY'
);

SET @sql := IF(
  @fk_exists = 0,
  'ALTER TABLE patients
     ADD CONSTRAINT fk_patient_user
     FOREIGN KEY (user_id) REFERENCES users(id)
     ON DELETE SET NULL
     ON UPDATE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
