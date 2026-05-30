-- Test data for AccountReaderWriterJob integration test
-- Covers: normal account, zero-debit substitution, non-zero debit

DELETE FROM accounts;

INSERT INTO accounts (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id)
VALUES
    -- Zero debit → 2525.00 substitution (CBACT01C.cbl:236-238)
    (10000000001, 'Y', 1940.00, 20200.00, 10200.00, '2014-11-20', '2025-05-20', '2025-05-20', 0.00, 0.00, '12345', 'A000000000'),
    -- Another zero debit account
    (10000000002, 'Y', 1580.00, 61300.00, 54480.00, '2013-06-19', '2024-08-11', '2024-08-11', 500.00, 0.00, '22770', 'A000000000'),
    -- Non-zero debit passes through
    (10000000003, 'Y', 50000.00, 10000.00, 5000.00, '2013-08-23', '2025-01-10', '2025-01-10', 2000.00, 1500.00, '33456', 'A000000000');
