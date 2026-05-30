INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES
('USER0001', 'John', 'Doe', 'password1', 'A'),
('USER0002', 'Jane', 'Smith', 'password2', 'U');

INSERT INTO customer (cust_id, cust_first_name, cust_middle_name, cust_last_name,
    cust_addr_line_1, cust_addr_line_2, cust_addr_line_3,
    cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip,
    cust_phone_num_1, cust_phone_num_2, cust_ssn, cust_govt_issued_id,
    cust_dob, cust_eft_account_id, cust_pri_card_holder_ind, cust_fico_credit_score) VALUES
(1, 'Alice', 'M', 'Johnson', '123 Main St', 'Apt 1', '', 'NY', 'USA', '10001',
 '5551234567', '5559876543', 123456789, 'DL12345', '1980-01-15', '0000000001', 'Y', 750);

INSERT INTO account (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit,
    acct_cash_credit_limit, acct_open_date, acct_expiration_date, acct_reissue_date,
    acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id) VALUES
(1, 'Y', 1500.00, 10000.00, 5000.00, '2020-01-01', '2025-12-31', '2025-01-01', 0.00, 0.00, '10001', 'A000000000');

INSERT INTO card (card_num, card_acct_id, card_cvv_cd, card_embossed_name,
    card_expiration_date, card_active_status) VALUES
('4111111111111111', 1, 123, 'ALICE JOHNSON', '2025-12-31', 'Y');

INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES
('4111111111111111', 1, 1);

INSERT INTO transaction (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc,
    tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city,
    tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) VALUES
('TX00000000000001', '01', 1, 'POS TERM', 'Purchase at Store', 50.00, 800000000,
 'Test Store', 'New York', '10001', '4111111111111111',
 '2024-01-15 10:30:00', '2024-01-15 10:30:01');

INSERT INTO daily_transaction (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc,
    tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city,
    tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) VALUES
('DT00000000000001', '01', 1, 'POS TERM', 'Daily Purchase', 25.00, 800000000,
 'Daily Store', 'Boston', '02101', '4111111111111111',
 '2024-01-16 14:00:00', '2024-01-16 14:00:01');

INSERT INTO tran_type (tran_type, tran_type_desc) VALUES
('01', 'Purchase'),
('02', 'Payment');

INSERT INTO tran_category (tran_type_cd, tran_cat_cd, tran_cat_type_desc) VALUES
('01', 1, 'Regular Sales Draft'),
('01', 2, 'Regular Cash Advance');

INSERT INTO tran_cat_balance (trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) VALUES
(1, '01', 1, 50.00);

INSERT INTO disclosure_group (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd, dis_int_rate) VALUES
('A000000000', '01', 1, 15.00);
