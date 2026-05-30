-- Flattened from IMS HIDAM segments PAUTSUM0 (summary) and PAUTDTL1 (detail)
CREATE TABLE authorizations (
    auth_id             SERIAL PRIMARY KEY,
    card_num            VARCHAR(16)  NOT NULL,
    auth_ts             TIMESTAMP    NOT NULL,
    auth_type           CHAR(4),
    card_expiry_date    CHAR(4),
    message_type        CHAR(6),
    message_source      CHAR(6),
    auth_id_code        CHAR(6),
    auth_resp_code      CHAR(2),
    auth_resp_reason    CHAR(4),
    processing_code     CHAR(6),
    transaction_amt     DECIMAL(12,2),
    approved_amt        DECIMAL(12,2),
    merchant_category_code CHAR(4),
    acqr_country_code   CHAR(3),
    pos_entry_mode      SMALLINT,
    merchant_id         VARCHAR(15),
    merchant_name       VARCHAR(22),
    merchant_city       VARCHAR(13),
    merchant_state      CHAR(2),
    merchant_zip        VARCHAR(9),
    transaction_id      VARCHAR(15),
    match_status        CHAR(1),
    acct_id             BIGINT,
    cust_id             BIGINT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- From DB2 AUTHFRDS DDL
CREATE TABLE auth_fraud (
    card_num                VARCHAR(16)  NOT NULL,
    auth_ts                 TIMESTAMP    NOT NULL,
    auth_type               CHAR(4),
    card_expiry_date        CHAR(4),
    message_type            CHAR(6),
    message_source          CHAR(6),
    auth_id_code            CHAR(6),
    auth_resp_code          CHAR(2),
    auth_resp_reason        CHAR(4),
    processing_code         CHAR(6),
    transaction_amt         DECIMAL(12,2),
    approved_amt            DECIMAL(12,2),
    merchant_category_code  CHAR(4),
    acqr_country_code       CHAR(3),
    pos_entry_mode          SMALLINT,
    merchant_id             VARCHAR(15),
    merchant_name           VARCHAR(22),
    merchant_city           VARCHAR(13),
    merchant_state          CHAR(2),
    merchant_zip            VARCHAR(9),
    transaction_id          VARCHAR(15),
    match_status            CHAR(1),
    auth_fraud              CHAR(1),
    fraud_rpt_date          DATE,
    acct_id                 BIGINT,
    cust_id                 BIGINT,
    PRIMARY KEY (card_num, auth_ts)
);

CREATE INDEX idx_authorizations_card ON authorizations(card_num);
CREATE INDEX idx_authorizations_acct ON authorizations(acct_id);
CREATE INDEX idx_auth_fraud_card ON auth_fraud(card_num);
