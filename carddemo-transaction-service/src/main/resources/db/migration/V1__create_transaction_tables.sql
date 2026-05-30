CREATE TABLE transaction_types (
    tran_type       CHAR(2)      NOT NULL PRIMARY KEY,
    tran_type_desc  VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction_categories (
    tran_type_cd        CHAR(2)      NOT NULL,
    tran_cat_cd         INTEGER      NOT NULL,  -- PIC 9(04)
    tran_cat_type_desc  VARCHAR(50),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tran_type_cd, tran_cat_cd),
    FOREIGN KEY (tran_type_cd) REFERENCES transaction_types(tran_type)
);

CREATE TABLE transactions (
    tran_id             VARCHAR(16)  NOT NULL PRIMARY KEY,
    tran_type_cd        CHAR(2),
    tran_cat_cd         INTEGER,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            DECIMAL(11,2),  -- PIC S9(09)V99
    tran_merchant_id    BIGINT,         -- PIC 9(09)
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16),
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tran_type_cd) REFERENCES transaction_types(tran_type)
);

CREATE TABLE daily_transactions (
    dalytran_id             VARCHAR(16)  NOT NULL PRIMARY KEY,
    dalytran_type_cd        CHAR(2),
    dalytran_cat_cd         INTEGER,
    dalytran_source         VARCHAR(10),
    dalytran_desc           VARCHAR(100),
    dalytran_amt            DECIMAL(11,2),
    dalytran_merchant_id    BIGINT,
    dalytran_merchant_name  VARCHAR(50),
    dalytran_merchant_city  VARCHAR(50),
    dalytran_merchant_zip   VARCHAR(10),
    dalytran_card_num       VARCHAR(16),
    dalytran_orig_ts        VARCHAR(26),
    dalytran_proc_ts        VARCHAR(26),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tran_cat_balance (
    trancat_acct_id     BIGINT       NOT NULL,  -- PIC 9(11)
    trancat_type_cd     CHAR(2)      NOT NULL,
    trancat_cd          INTEGER      NOT NULL,  -- PIC 9(04)
    tran_cat_bal        DECIMAL(11,2),           -- PIC S9(09)V99
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

CREATE TABLE disclosure_groups (
    dis_acct_group_id   VARCHAR(10)  NOT NULL,
    dis_tran_type_cd    CHAR(2)      NOT NULL,
    dis_tran_cat_cd     INTEGER      NOT NULL,  -- PIC 9(04)
    dis_int_rate        DECIMAL(6,2),            -- PIC S9(04)V99
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

CREATE TABLE daily_rejects (
    reject_id           SERIAL PRIMARY KEY,
    dalytran_id         VARCHAR(16),
    reject_reason       VARCHAR(100),
    rejected_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_card ON transactions(tran_card_num);
CREATE INDEX idx_daily_transactions_card ON daily_transactions(dalytran_card_num);
CREATE INDEX idx_tran_cat_balance_acct ON tran_cat_balance(trancat_acct_id);
