-- CardDemo schema: all 11 tables derived from COBOL copybooks

CREATE TABLE users (
    usr_id      VARCHAR(8)  NOT NULL PRIMARY KEY,
    first_name  VARCHAR(20),
    last_name   VARCHAR(20),
    password    VARCHAR(8),
    usr_type    VARCHAR(1)
);

CREATE TABLE accounts (
    acct_id           BIGINT       NOT NULL PRIMARY KEY,
    active_status     VARCHAR(1),
    curr_bal          DECIMAL(12,2),
    credit_limit      DECIMAL(12,2),
    cash_credit_limit DECIMAL(12,2),
    open_date         VARCHAR(10),
    expiration_date   VARCHAR(10),
    reissue_date      VARCHAR(10),
    curr_cyc_credit   DECIMAL(12,2),
    curr_cyc_debit    DECIMAL(12,2),
    addr_zip          VARCHAR(10),
    group_id          VARCHAR(10)
);

CREATE TABLE customers (
    cust_id             BIGINT       NOT NULL PRIMARY KEY,
    first_name          VARCHAR(25),
    middle_name         VARCHAR(25),
    last_name           VARCHAR(25),
    addr_line_1         VARCHAR(50),
    addr_line_2         VARCHAR(50),
    addr_line_3         VARCHAR(50),
    state_code          VARCHAR(2),
    country_code        VARCHAR(3),
    zip                 VARCHAR(10),
    phone1              VARCHAR(15),
    phone2              VARCHAR(15),
    ssn                 BIGINT,
    govt_issued_id      VARCHAR(20),
    dob                 VARCHAR(10),
    eft_account_id      VARCHAR(10),
    pri_card_holder_ind VARCHAR(1),
    fico_credit_score   INTEGER
);

CREATE TABLE cards (
    card_num        VARCHAR(16) NOT NULL PRIMARY KEY,
    acct_id         BIGINT,
    cvv_cd          INTEGER,
    embossed_name   VARCHAR(50),
    expiration_date VARCHAR(10),
    active_status   VARCHAR(1)
);

CREATE TABLE card_xref (
    xref_card_num VARCHAR(16) NOT NULL PRIMARY KEY,
    cust_id       BIGINT      NOT NULL,
    acct_id       BIGINT      NOT NULL
);

CREATE TABLE transactions (
    tran_id       VARCHAR(16)  NOT NULL PRIMARY KEY,
    type_cd       VARCHAR(2),
    cat_cd        INTEGER,
    source        VARCHAR(10),
    description   VARCHAR(100),
    amt           DECIMAL(11,2),
    merchant_id   BIGINT,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip  VARCHAR(10),
    card_num      VARCHAR(16),
    orig_ts       VARCHAR(26),
    proc_ts       VARCHAR(26)
);

CREATE TABLE daily_transactions (
    tran_id       VARCHAR(16)  NOT NULL,
    type_cd       VARCHAR(2),
    cat_cd        INTEGER,
    source        VARCHAR(10),
    description   VARCHAR(100),
    amt           DECIMAL(11,2),
    merchant_id   BIGINT,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip  VARCHAR(10),
    card_num      VARCHAR(16)  NOT NULL,
    orig_ts       VARCHAR(26)  NOT NULL,
    proc_ts       VARCHAR(26),
    PRIMARY KEY (tran_id, card_num, orig_ts)
);

CREATE TABLE tran_cat_balances (
    acct_id      BIGINT      NOT NULL,
    type_cd      VARCHAR(2)  NOT NULL,
    cat_cd       INTEGER     NOT NULL,
    tran_cat_bal DECIMAL(11,2),
    PRIMARY KEY (acct_id, type_cd, cat_cd)
);

CREATE TABLE disclosure_groups (
    acct_group_id VARCHAR(10) NOT NULL,
    type_cd       VARCHAR(2)  NOT NULL,
    cat_cd        INTEGER     NOT NULL,
    int_rate      DECIMAL(6,2),
    PRIMARY KEY (acct_group_id, type_cd, cat_cd)
);

CREATE TABLE tran_types (
    tran_type      VARCHAR(2)  NOT NULL PRIMARY KEY,
    tran_type_desc VARCHAR(50)
);

CREATE TABLE tran_categories (
    type_cd            VARCHAR(2)  NOT NULL,
    cat_cd             INTEGER     NOT NULL,
    tran_cat_type_desc VARCHAR(50),
    PRIMARY KEY (type_cd, cat_cd)
);
