CREATE TABLE IF NOT EXISTS card (
    card_num             VARCHAR(16)  NOT NULL PRIMARY KEY,
    card_acct_id         BIGINT,
    card_cvv_cd          INT,
    card_embossed_name   VARCHAR(50),
    card_expiration_date DATE,
    card_active_status   CHAR(1)
);
