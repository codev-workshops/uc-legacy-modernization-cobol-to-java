DROP TABLE IF EXISTS disclosure_group;
DROP TABLE IF EXISTS tran_cat_balance;
DROP TABLE IF EXISTS tran_category;
DROP TABLE IF EXISTS tran_type;
DROP TABLE IF EXISTS daily_transaction;
DROP TABLE IF EXISTS card_xref;
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS user_security;

CREATE TABLE account (
    acct_id BIGINT PRIMARY KEY,
    acct_active_status VARCHAR(1),
    acct_curr_bal DECIMAL(12,2),
    acct_credit_limit DECIMAL(12,2),
    acct_cash_credit_limit DECIMAL(12,2),
    acct_open_date DATE,
    acct_expiration_date DATE,
    acct_reissue_date DATE,
    acct_curr_cyc_credit DECIMAL(12,2),
    acct_curr_cyc_debit DECIMAL(12,2),
    acct_addr_zip VARCHAR(10),
    acct_group_id VARCHAR(10)
);

CREATE TABLE customer (
    cust_id BIGINT PRIMARY KEY,
    cust_first_name VARCHAR(25),
    cust_middle_name VARCHAR(25),
    cust_last_name VARCHAR(25),
    cust_addr_line_1 VARCHAR(50),
    cust_addr_line_2 VARCHAR(50),
    cust_addr_line_3 VARCHAR(50),
    cust_addr_state_cd VARCHAR(2),
    cust_addr_country_cd VARCHAR(3),
    cust_addr_zip VARCHAR(10),
    cust_phone_num_1 VARCHAR(15),
    cust_phone_num_2 VARCHAR(15),
    cust_ssn BIGINT,
    cust_govt_issued_id VARCHAR(20),
    cust_dob DATE,
    cust_eft_account_id VARCHAR(10),
    cust_pri_card_holder_ind VARCHAR(1),
    cust_fico_credit_score INT
);

CREATE TABLE card (
    card_num VARCHAR(16) PRIMARY KEY,
    card_acct_id BIGINT,
    card_cvv_cd INT,
    card_embossed_name VARCHAR(50),
    card_expiration_date DATE,
    card_active_status VARCHAR(1)
);

CREATE TABLE card_xref (
    xref_card_num VARCHAR(16) PRIMARY KEY,
    xref_cust_id BIGINT NOT NULL,
    xref_acct_id BIGINT NOT NULL
);

CREATE TABLE daily_transaction (
    tran_id VARCHAR(16) PRIMARY KEY,
    tran_type_cd VARCHAR(2),
    tran_cat_cd INT,
    tran_source VARCHAR(10),
    tran_desc VARCHAR(100),
    tran_amt DECIMAL(11,2),
    tran_merchant_id BIGINT,
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip VARCHAR(10),
    tran_card_num VARCHAR(16),
    tran_orig_ts TIMESTAMP,
    tran_proc_ts TIMESTAMP
);

CREATE TABLE tran_type (
    tran_type VARCHAR(2) PRIMARY KEY,
    tran_type_desc VARCHAR(50)
);

CREATE TABLE tran_category (
    tran_type_cd VARCHAR(2) NOT NULL,
    tran_cat_cd INT NOT NULL,
    tran_cat_type_desc VARCHAR(50),
    PRIMARY KEY (tran_type_cd, tran_cat_cd)
);

CREATE TABLE tran_cat_balance (
    trancat_acct_id BIGINT NOT NULL,
    trancat_type_cd VARCHAR(2) NOT NULL,
    trancat_cd INT NOT NULL,
    tran_cat_bal DECIMAL(11,2),
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

CREATE TABLE disclosure_group (
    dis_acct_group_id VARCHAR(10) NOT NULL,
    dis_tran_type_cd VARCHAR(2) NOT NULL,
    dis_tran_cat_cd INT NOT NULL,
    dis_int_rate DECIMAL(6,2),
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

CREATE TABLE user_security (
    usr_id VARCHAR(8) PRIMARY KEY,
    usr_fname VARCHAR(20),
    usr_lname VARCHAR(20),
    usr_pwd VARCHAR(72),
    usr_type VARCHAR(1),
    usr_filler VARCHAR(23)
);
