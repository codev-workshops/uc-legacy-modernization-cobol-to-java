CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num  VARCHAR(16)  NOT NULL PRIMARY KEY,
    xref_cust_id   BIGINT       NOT NULL,
    xref_acct_id   BIGINT       NOT NULL
);
