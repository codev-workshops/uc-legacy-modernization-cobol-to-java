CREATE TABLE IF NOT EXISTS account (
    acct_id               BIGINT        NOT NULL PRIMARY KEY,
    acct_active_status    CHAR(1),
    acct_curr_bal         NUMERIC(12,2),
    acct_credit_limit     NUMERIC(12,2),
    acct_cash_credit_limit NUMERIC(12,2),
    acct_open_date        DATE,
    acct_expiration_date  DATE,
    acct_reissue_date     DATE,
    acct_curr_cyc_credit  NUMERIC(12,2),
    acct_curr_cyc_debit   NUMERIC(12,2),
    acct_addr_zip         VARCHAR(10),
    acct_group_id         VARCHAR(10)
);
