CREATE TABLE IF NOT EXISTS daily_reject (
    reject_id     SERIAL       PRIMARY KEY,
    tran_id       VARCHAR(16),
    reject_reason INT,
    reject_desc   VARCHAR(100),
    reject_ts     TIMESTAMP    DEFAULT NOW()
);
