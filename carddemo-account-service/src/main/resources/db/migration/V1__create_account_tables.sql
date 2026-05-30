CREATE TABLE accounts (
    acct_id                 BIGINT       NOT NULL PRIMARY KEY,  -- PIC 9(11)
    acct_active_status      CHAR(1),
    acct_curr_bal           DECIMAL(12,2),   -- PIC S9(10)V99
    acct_credit_limit       DECIMAL(12,2),
    acct_cash_credit_limit  DECIMAL(12,2),
    acct_open_date          VARCHAR(10),
    acct_expiration_date    VARCHAR(10),
    acct_reissue_date       VARCHAR(10),
    acct_curr_cyc_credit    DECIMAL(12,2),
    acct_curr_cyc_debit     DECIMAL(12,2),
    acct_addr_zip           VARCHAR(10),
    acct_group_id           VARCHAR(10),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customers (
    cust_id                     BIGINT       NOT NULL PRIMARY KEY,  -- PIC 9(09)
    cust_first_name             VARCHAR(25),
    cust_middle_name            VARCHAR(25),
    cust_last_name              VARCHAR(25),
    cust_addr_line_1            VARCHAR(50),
    cust_addr_line_2            VARCHAR(50),
    cust_addr_line_3            VARCHAR(50),
    cust_addr_state_cd          CHAR(2),
    cust_addr_country_cd        CHAR(3),
    cust_addr_zip               VARCHAR(10),
    cust_phone_num_1            VARCHAR(15),
    cust_phone_num_2            VARCHAR(15),
    cust_ssn                    BIGINT,       -- PIC 9(09)
    cust_govt_issued_id         VARCHAR(20),
    cust_dob                    VARCHAR(10),  -- PIC X(10) YYYY-MM-DD
    cust_eft_account_id         VARCHAR(10),
    cust_pri_card_holder_ind    CHAR(1),
    cust_fico_credit_score      SMALLINT,     -- PIC 9(03)
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cards (
    card_num                VARCHAR(16)  NOT NULL PRIMARY KEY,
    card_acct_id            BIGINT       NOT NULL REFERENCES accounts(acct_id),
    card_cvv_cd             SMALLINT,    -- PIC 9(03)
    card_embossed_name      VARCHAR(50),
    card_expiration_date    VARCHAR(10),
    card_active_status      CHAR(1),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE card_xref (
    xref_card_num   VARCHAR(16)  NOT NULL PRIMARY KEY,
    xref_cust_id    BIGINT       NOT NULL REFERENCES customers(cust_id),
    xref_acct_id    BIGINT       NOT NULL REFERENCES accounts(acct_id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cards_acct_id ON cards(card_acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref(xref_cust_id);
CREATE INDEX idx_card_xref_acct_id ON card_xref(xref_acct_id);
