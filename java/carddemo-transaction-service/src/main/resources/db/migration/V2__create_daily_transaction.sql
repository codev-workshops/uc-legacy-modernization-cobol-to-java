CREATE TABLE IF NOT EXISTS daily_transaction (
    tran_id            VARCHAR(16)   NOT NULL PRIMARY KEY,
    tran_type_cd       VARCHAR(2),
    tran_cat_cd        INT,
    tran_source        VARCHAR(10),
    tran_desc          VARCHAR(100),
    tran_amt           NUMERIC(11,2),
    tran_merchant_id   BIGINT,
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip  VARCHAR(10),
    tran_card_num      VARCHAR(16),
    tran_orig_ts       TIMESTAMP,
    tran_proc_ts       TIMESTAMP
);
