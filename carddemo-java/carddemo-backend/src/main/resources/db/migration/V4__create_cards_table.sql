CREATE TABLE cards (
    card_num           VARCHAR(16)    PRIMARY KEY,
    card_acct_id       BIGINT,
    cvv_cd             INT,
    embossed_name      VARCHAR(50),
    expiration_date    VARCHAR(10),
    active_status      VARCHAR(1),
    CONSTRAINT fk_cards_account FOREIGN KEY (card_acct_id) REFERENCES accounts (acct_id)
);
