-- Synthetic test dataset covering edge cases from COBOL batch programs

-- Accounts: normal, overlimit, expired, negative balance, zero debit
INSERT INTO accounts (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id)
VALUES
    (10000000001, 'Y', 1940.00, 20200.00, 10200.00, '2014-11-20', '2025-05-20', '2025-05-20', 0.00, 0.00, '12345', 'A000000000'),
    -- Zero debit substitution: CBACT01C.cbl:236-238
    (10000000002, 'Y', 1580.00, 61300.00, 54480.00, '2013-06-19', '2024-08-11', '2024-08-11', 500.00, 0.00, '22770', 'A000000000'),
    -- Overlimit: curr_bal exceeds credit_limit (CBTRN02C.cbl:393-421)
    (10000000003, 'Y', 50000.00, 10000.00, 5000.00, '2013-08-23', '2025-01-10', '2025-01-10', 2000.00, 1500.00, '33456', 'A000000000'),
    -- Expired card (CBTRN02C.cbl:414-420)
    (10000000004, 'Y', 5000.00, 20000.00, 10000.00, '2010-01-01', '2020-12-31', '2020-12-31', 1000.00, 800.00, '44567', 'B000000000'),
    -- Negative balance
    (10000000005, 'Y', -1025.00, 15000.00, 7500.00, '2015-03-15', '2026-03-15', '2026-03-15', 3000.00, 4025.00, '55678', 'A000000000'),
    -- Multi-card account
    (10000000006, 'Y', 8500.00, 25000.00, 12000.00, '2016-07-01', '2027-07-01', '2027-07-01', 1200.00, 900.00, '66789', 'B000000000'),
    -- Post-interest cycle reset: CBACT04C.cbl:350-356
    (10000000007, 'Y', 12000.00, 30000.00, 15000.00, '2012-01-01', '2025-12-31', '2025-12-31', 0.00, 0.00, '77890', 'A000000000');

-- Customers
INSERT INTO customers (cust_id, first_name, middle_name, last_name,
    addr_line_1, addr_line_2, addr_line_3, state_code, country_code, zip,
    phone1, phone2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score)
VALUES
    (100000001, 'John', 'Michael', 'Smith', '123 Main St', 'Apt 4B', '', 'NY', 'USA', '10001', '2125551234', '2125555678', 123456789, 'DL12345678', '1980-05-15', '0053581756', 'Y', 750),
    (100000002, 'Jane', 'Marie', 'Doe', '456 Oak Ave', '', '', 'CA', 'USA', '90210', '3105559876', '3105554321', 987654321, 'DL87654321', '1975-11-22', '0069194009', 'Y', 680),
    (100000003, 'Robert', '', 'Johnson', '789 Elm Blvd', 'Suite 100', '', 'TX', 'USA', '75201', '2145552468', '2145551357', 111222333, 'PP11223344', '1990-03-08', '0045678901', 'N', 720);

-- Cards (multi-card for account 10000000006)
INSERT INTO cards (card_num, acct_id, cvv_cd, embossed_name, expiration_date, active_status)
VALUES
    ('4500123456789012', 10000000001, 123, 'JOHN M SMITH', '2025-05-20', 'Y'),
    ('4500234567890123', 10000000002, 456, 'JANE M DOE', '2024-08-11', 'Y'),
    ('4500345678901234', 10000000003, 789, 'ROBERT JOHNSON', '2025-01-10', 'Y'),
    ('4500456789012345', 10000000004, 321, 'JOHN M SMITH', '2020-12-31', 'Y'),
    ('4500567890123456', 10000000005, 654, 'JANE M DOE', '2026-03-15', 'Y'),
    ('4500678901234567', 10000000006, 987, 'ROBERT JOHNSON', '2027-07-01', 'Y'),
    ('4500789012345678', 10000000006, 111, 'ROBERT JOHNSON JR', '2027-07-01', 'Y'),
    ('4500890123456789', 10000000007, 222, 'JOHN M SMITH', '2025-12-31', 'Y');

-- Card cross-references
INSERT INTO card_xref (xref_card_num, cust_id, acct_id)
VALUES
    ('4500123456789012', 100000001, 10000000001),
    ('4500234567890123', 100000002, 10000000002),
    ('4500345678901234', 100000003, 10000000003),
    ('4500456789012345', 100000001, 10000000004),
    ('4500567890123456', 100000002, 10000000005),
    ('4500678901234567', 100000003, 10000000006),
    ('4500789012345678', 100000003, 10000000006),
    ('4500890123456789', 100000001, 10000000007);

-- Transaction types
INSERT INTO tran_types (tran_type, tran_type_desc) VALUES
    ('01', 'Purchase'),
    ('02', 'Payment'),
    ('03', 'Cash Advance'),
    ('04', 'Balance Transfer'),
    ('05', 'Fee');

-- Transaction categories
INSERT INTO tran_categories (type_cd, cat_cd, tran_cat_type_desc) VALUES
    ('01', 1, 'Regular Sales Draft'),
    ('01', 2, 'Regular Cash Advance'),
    ('02', 1, 'Payment - Thank You'),
    ('03', 1, 'ATM Cash Advance');

-- Transaction category balances
INSERT INTO tran_cat_balances (acct_id, type_cd, cat_cd, tran_cat_bal) VALUES
    (10000000001, '01', 1, 0.00),
    (10000000002, '01', 1, 500.00),
    (10000000003, '01', 1, 50000.00),
    (10000000005, '01', 1, -1025.00);

-- Disclosure groups
INSERT INTO disclosure_groups (acct_group_id, type_cd, cat_cd, int_rate) VALUES
    ('A000000000', '01', 1, 15.00),
    ('A000000000', '01', 2, 25.00),
    ('B000000000', '01', 1, 18.00),
    ('B000000000', '03', 1, 22.50);

-- Daily transactions covering edge cases
INSERT INTO daily_transactions (tran_id, type_cd, cat_cd, source, description, amt,
    merchant_id, merchant_name, merchant_city, merchant_zip, card_num, orig_ts, proc_ts)
VALUES
    -- Normal purchase
    ('0000000000000001', '01', 1, 'POS TERM', 'Purchase at Store A', 50.47, 800000000, 'Store A', 'New York', '10001', '2024-01-15 10:30:00.000000', '2024-01-15 10:30:01.000000'),
    -- Overlimit transaction attempt
    ('0000000000000002', '01', 1, 'OPERATOR', 'Purchase at Store B', 5000.00, 800000001, 'Store B', 'Los Angeles', '90210', '2024-01-15 11:00:00.000000', '2024-01-15 11:00:01.000000'),
    -- Expired card transaction
    ('0000000000000003', '01', 1, 'POS TERM', 'Purchase at Store C', 25.00, 800000002, 'Store C', 'Dallas', '75201', '2024-01-15 12:00:00.000000', '2024-01-15 12:00:01.000000'),
    -- Payment (negative amount)
    ('0000000000000004', '02', 1, 'ONLINE', 'Payment received', -500.00, 800000003, 'Online Payment', 'Anywhere', '00000', '2024-01-15 13:00:00.000000', '2024-01-15 13:00:01.000000');
