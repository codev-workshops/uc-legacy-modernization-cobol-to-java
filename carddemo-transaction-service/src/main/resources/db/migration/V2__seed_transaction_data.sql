-- Seed transaction_types from trantype.txt
INSERT INTO transaction_types (tran_type, tran_type_desc) VALUES
('01', 'Purchase'),
('02', 'Payment'),
('03', 'Credit'),
('04', 'Authorization'),
('05', 'Refund'),
('06', 'Reversal'),
('07', 'Adjustment');

-- Seed transaction_categories from trancatg.txt
INSERT INTO transaction_categories (tran_type_cd, tran_cat_cd, tran_cat_type_desc) VALUES
('01', 1, 'Regular Sales Draft'),
('01', 2, 'Regular Cash Advance'),
('01', 3, 'Convenience Check Debit'),
('01', 4, 'ATM Cash Advance'),
('01', 5, 'Interest Amount'),
('02', 1, 'Cash payment'),
('02', 2, 'Electronic payment'),
('02', 3, 'Check payment'),
('03', 1, 'Credit to Account'),
('03', 2, 'Credit to Purchase balance'),
('03', 3, 'Credit to Cash balance'),
('04', 1, 'Zero dollar authorization'),
('04', 2, 'Online purchase authorization'),
('04', 3, 'Travel booking authorization'),
('05', 1, 'Refund credit'),
('06', 1, 'Fraud reversal'),
('06', 2, 'Non-fraud reversal'),
('07', 1, 'Sales draft credit adjustment');

-- Seed disclosure_groups for testing
INSERT INTO disclosure_groups (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd, dis_int_rate) VALUES
('GRP001', '01', 1, 18.99),
('GRP001', '01', 2, 24.99),
('GRP001', '01', 3, 24.99),
('GRP001', '01', 4, 24.99),
('GRP001', '01', 5, 0.00),
('GRP001', '02', 1, 0.00),
('GRP001', '03', 1, 0.00),
('GRP002', '01', 1, 15.99),
('GRP002', '01', 2, 21.99);

-- Seed tran_cat_balance for testing
INSERT INTO tran_cat_balance (trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) VALUES
(1000000001, '01', 1, 5000.00),
(1000000001, '01', 2, 2000.00),
(1000000001, '02', 1, -1500.00),
(1000000002, '01', 1, 3000.00),
(1000000002, '01', 2, 1000.00);
