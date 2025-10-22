CREATE TABLE idempotency_keys (
                                  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  idempotency_key VARCHAR(128) NOT NULL,
                                  endpoint        VARCHAR(128) NOT NULL,
                                  request_hash    CHAR(64)     NOT NULL,
                                  response_id     BIGINT       NULL,       -- optional: store created appointment id
                                  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT uq_idem UNIQUE (idempotency_key, endpoint)
);

-- (Optional) purge old keys daily with an event/cron if needed.
