CREATE TABLE card_xref (
    xref_card_num      VARCHAR(16)    PRIMARY KEY,
    xref_cust_id       BIGINT,
    xref_acct_id       BIGINT,
    CONSTRAINT fk_xref_card     FOREIGN KEY (xref_card_num) REFERENCES cards (card_num),
    CONSTRAINT fk_xref_customer FOREIGN KEY (xref_cust_id)  REFERENCES customers (cust_id),
    CONSTRAINT fk_xref_account  FOREIGN KEY (xref_acct_id)  REFERENCES accounts (acct_id)
);
