CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num  VARCHAR(16)  NOT NULL PRIMARY KEY,
    xref_cust_id   BIGINT       NOT NULL REFERENCES customer(cust_id),
    xref_acct_id   BIGINT       NOT NULL REFERENCES account(acct_id)
);

CREATE INDEX idx_card_xref_acct_id ON card_xref(xref_acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref(xref_cust_id);
