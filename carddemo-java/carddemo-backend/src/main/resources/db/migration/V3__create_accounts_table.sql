CREATE TABLE accounts (
    acct_id            BIGINT         PRIMARY KEY,
    active_status      VARCHAR(1),
    curr_bal           DECIMAL(12,2),
    credit_limit       DECIMAL(12,2),
    cash_credit_limit  DECIMAL(12,2),
    open_date          VARCHAR(10),
    expiration_date    VARCHAR(10),
    reissue_date       VARCHAR(10),
    curr_cyc_credit    DECIMAL(12,2),
    curr_cyc_debit     DECIMAL(12,2),
    addr_zip           VARCHAR(10),
    group_id           VARCHAR(10)
);
