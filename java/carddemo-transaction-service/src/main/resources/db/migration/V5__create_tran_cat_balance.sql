CREATE TABLE IF NOT EXISTS tran_cat_balance (
    trancat_acct_id    BIGINT      NOT NULL,
    trancat_type_cd    VARCHAR(2)  NOT NULL,
    trancat_cd         INT         NOT NULL,
    tran_cat_bal       NUMERIC(11,2),
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);
