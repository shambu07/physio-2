-- Convert request_hash from CHAR to VARCHAR(64) to match JPA mapping
ALTER TABLE idempotency_keys
    MODIFY COLUMN request_hash VARCHAR(64) NOT NULL;

-- (Optional but recommended) keep a unique constraint for deduplication
-- Uncomment if you rely on uniqueness:
-- ALTER TABLE idempotency_keys ADD UNIQUE KEY uq_idem_request_hash (request_hash);
