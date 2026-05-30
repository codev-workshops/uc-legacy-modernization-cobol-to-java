#!/usr/bin/env python3
"""Parse COBOL fixed-width data files and generate Flyway repeatable SQL seed migrations."""

import os
import sys

DATA_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'data', 'ASCII')

# COBOL signed overpunch mapping (last byte of signed zoned decimal)
POSITIVE_OVERPUNCH = {'{': '0', 'A': '1', 'B': '2', 'C': '3', 'D': '4',
                      'E': '5', 'F': '6', 'G': '7', 'H': '8', 'I': '9'}
NEGATIVE_OVERPUNCH = {'}': '0', 'J': '1', 'K': '2', 'L': '3', 'M': '4',
                      'N': '5', 'O': '6', 'P': '7', 'Q': '8', 'R': '9'}


def parse_signed_decimal(raw, integer_digits, decimal_digits):
    """Parse a COBOL S9(n)V99 field in display format (zoned decimal with overpunch)."""
    if not raw or raw.strip() == '':
        return '0'
    last_char = raw[-1]
    sign = '+'
    digit = last_char
    if last_char in POSITIVE_OVERPUNCH:
        digit = POSITIVE_OVERPUNCH[last_char]
        sign = '+'
    elif last_char in NEGATIVE_OVERPUNCH:
        digit = NEGATIVE_OVERPUNCH[last_char]
        sign = '-'
    elif last_char.isdigit():
        digit = last_char
        sign = '+'
    
    digits = raw[:-1] + digit
    # Insert decimal point
    int_part = digits[:integer_digits]
    dec_part = digits[integer_digits:integer_digits + decimal_digits]
    value = f"{int_part}.{dec_part}" if dec_part else int_part
    # Strip leading zeros but keep at least one digit
    value = value.lstrip('0') or '0'
    if value.startswith('.'):
        value = '0' + value
    if sign == '-' and float(value) != 0:
        value = '-' + value
    return value


def sql_str(val):
    """Escape a string for SQL, trimming trailing spaces."""
    val = val.rstrip()
    val = val.replace("'", "''")
    return f"'{val}'"


def sql_date(val):
    """Convert a date string, returning NULL for empty/invalid."""
    val = val.strip()
    if not val or val == '0000-00-00' or len(val) < 10:
        return 'NULL'
    return f"'{val}'"


def sql_int(val):
    """Parse an integer, returning 0 for blanks."""
    val = val.strip()
    if not val:
        return '0'
    return str(int(val))


def sql_bigint(val):
    """Parse a bigint, returning 0 for blanks."""
    val = val.strip()
    if not val:
        return '0'
    return str(int(val))


def parse_acctdata():
    """Parse acctdata.txt (CVACT01Y layout, 300 bytes/record)."""
    # Fields: ACCT-ID(11), ACTIVE-STATUS(1), CURR-BAL(S9(10)V99=12), CREDIT-LIMIT(12),
    # CASH-CREDIT-LIMIT(12), OPEN-DATE(10), EXPIRATION-DATE(10), REISSUE-DATE(10),
    # CURR-CYC-CREDIT(12), CURR-CYC-DEBIT(12), ADDR-ZIP(10), GROUP-ID(10)
    fields = [
        ('acct_id', 11, 'bigint'),
        ('acct_active_status', 1, 'str'),
        ('acct_curr_bal', 12, 'signed_12_2'),
        ('acct_credit_limit', 12, 'signed_12_2'),
        ('acct_cash_credit_limit', 12, 'signed_12_2'),
        ('acct_open_date', 10, 'date'),
        ('acct_expiration_date', 10, 'date'),
        ('acct_reissue_date', 10, 'date'),
        ('acct_curr_cyc_credit', 12, 'signed_12_2'),
        ('acct_curr_cyc_debit', 12, 'signed_12_2'),
        ('acct_addr_zip', 10, 'str'),
        ('acct_group_id', 10, 'str'),
    ]
    return parse_file('acctdata.txt', 'account', fields)


def parse_custdata():
    """Parse custdata.txt (CVCUS01Y layout, 500 bytes/record)."""
    fields = [
        ('cust_id', 9, 'bigint'),
        ('cust_first_name', 25, 'str'),
        ('cust_middle_name', 25, 'str'),
        ('cust_last_name', 25, 'str'),
        ('cust_addr_line_1', 50, 'str'),
        ('cust_addr_line_2', 50, 'str'),
        ('cust_addr_line_3', 50, 'str'),
        ('cust_addr_state_cd', 2, 'str'),
        ('cust_addr_country_cd', 3, 'str'),
        ('cust_addr_zip', 10, 'str'),
        ('cust_phone_num_1', 15, 'str'),
        ('cust_phone_num_2', 15, 'str'),
        ('cust_ssn', 9, 'bigint'),
        ('cust_govt_issued_id', 20, 'str'),
        ('cust_dob', 10, 'date'),
        ('cust_eft_account_id', 10, 'str'),
        ('cust_pri_card_holder_ind', 1, 'str'),
        ('cust_fico_credit_score', 3, 'int'),
    ]
    return parse_file('custdata.txt', 'customer', fields)


def parse_carddata():
    """Parse carddata.txt (CVACT02Y layout, 150 bytes/record)."""
    fields = [
        ('card_num', 16, 'str'),
        ('card_acct_id', 11, 'bigint'),
        ('card_cvv_cd', 3, 'int'),
        ('card_embossed_name', 50, 'str'),
        ('card_expiration_date', 10, 'date'),
        ('card_active_status', 1, 'str'),
    ]
    return parse_file('carddata.txt', 'card', fields)


def parse_cardxref():
    """Parse cardxref.txt (CVACT03Y layout)."""
    fields = [
        ('xref_card_num', 16, 'str'),
        ('xref_cust_id', 9, 'bigint'),
        ('xref_acct_id', 11, 'bigint'),
    ]
    return parse_file('cardxref.txt', 'card_xref', fields)


def parse_dailytran():
    """Parse dailytran.txt (CVTRA06Y layout, 350 bytes/record)."""
    fields = [
        ('tran_id', 16, 'str'),
        ('tran_type_cd', 2, 'str'),
        ('tran_cat_cd', 4, 'int'),
        ('tran_source', 10, 'str'),
        ('tran_desc', 100, 'str'),
        ('tran_amt', 11, 'signed_9_2'),
        ('tran_merchant_id', 9, 'bigint'),
        ('tran_merchant_name', 50, 'str'),
        ('tran_merchant_city', 50, 'str'),
        ('tran_merchant_zip', 10, 'str'),
        ('tran_card_num', 16, 'str'),
        ('tran_orig_ts', 26, 'timestamp'),
        ('tran_proc_ts', 26, 'timestamp'),
    ]
    return parse_file('dailytran.txt', 'daily_transaction', fields)


def parse_trantype():
    """Parse trantype.txt (CVTRA03Y layout)."""
    fields = [
        ('tran_type', 2, 'str'),
        ('tran_type_desc', 50, 'str'),
    ]
    return parse_file('trantype.txt', 'tran_type', fields)


def parse_trancatg():
    """Parse trancatg.txt (CVTRA04Y layout)."""
    fields = [
        ('tran_type_cd', 2, 'str'),
        ('tran_cat_cd', 4, 'int'),
        ('tran_cat_type_desc', 50, 'str'),
    ]
    return parse_file('trancatg.txt', 'tran_category', fields)


def parse_tcatbal():
    """Parse tcatbal.txt (CVTRA01Y layout)."""
    fields = [
        ('trancat_acct_id', 11, 'bigint'),
        ('trancat_type_cd', 2, 'str'),
        ('trancat_cd', 4, 'int'),
        ('tran_cat_bal', 11, 'signed_9_2'),
    ]
    return parse_file('tcatbal.txt', 'tran_cat_balance', fields)


def parse_discgrp():
    """Parse discgrp.txt (CVTRA02Y layout)."""
    fields = [
        ('dis_acct_group_id', 10, 'str'),
        ('dis_tran_type_cd', 2, 'str'),
        ('dis_tran_cat_cd', 4, 'int'),
        ('dis_int_rate', 6, 'signed_4_2'),
    ]
    return parse_file('discgrp.txt', 'disclosure_group', fields)


def parse_file(filename, table_name, fields):
    """Generic parser for fixed-width COBOL data files."""
    filepath = os.path.join(DATA_DIR, filename)
    if not os.path.exists(filepath):
        print(f"WARNING: {filepath} not found, skipping")
        return []

    inserts = []
    col_names = [f[0] for f in fields]

    with open(filepath, 'r') as f:
        for line_num, line in enumerate(f, 1):
            line = line.rstrip('\n').rstrip('\r')
            if not line.strip():
                continue

            pos = 0
            values = []
            for col_name, width, dtype in fields:
                raw = line[pos:pos + width] if pos + width <= len(line) else line[pos:]
                pos += width

                if dtype == 'str':
                    values.append(sql_str(raw))
                elif dtype == 'int':
                    values.append(sql_int(raw))
                elif dtype == 'bigint':
                    values.append(sql_bigint(raw))
                elif dtype == 'date':
                    values.append(sql_date(raw))
                elif dtype == 'timestamp':
                    ts = raw.strip()
                    if ts and ts != '0000-00-00-00.00.00.000000':
                        ts = ts.replace('.', ':',  2)
                        values.append(f"'{ts}'")
                    else:
                        values.append('NULL')
                elif dtype.startswith('signed_'):
                    parts = dtype.split('_')
                    int_d = int(parts[1])
                    dec_d = int(parts[2])
                    values.append(parse_signed_decimal(raw, int_d, dec_d))

            insert = f"INSERT INTO {table_name} ({', '.join(col_names)}) VALUES ({', '.join(values)});"
            inserts.append(insert)

    return inserts


def write_account_service_seed():
    """Generate R__load_test_data.sql for account-service."""
    output_path = os.path.join(os.path.dirname(__file__),
                               'carddemo-account-service', 'src', 'main', 'resources', 'db', 'migration',
                               'R__load_test_data.sql')

    lines = ['-- Repeatable migration: seed test data from COBOL fixed-width files',
             '-- Generated by generate_seed_data.py',
             '',
             '-- Clear existing data (order matters for FK constraints)',
             'DELETE FROM card_xref;',
             'DELETE FROM card;',
             'DELETE FROM account;',
             'DELETE FROM customer;',
             '']

    lines.append('-- Customer data (custdata.txt / CVCUS01Y)')
    lines.extend(parse_custdata())
    lines.append('')

    lines.append('-- Account data (acctdata.txt / CVACT01Y)')
    lines.extend(parse_acctdata())
    lines.append('')

    lines.append('-- Card data (carddata.txt / CVACT02Y)')
    lines.extend(parse_carddata())
    lines.append('')

    lines.append('-- Card cross-reference data (cardxref.txt / CVACT03Y)')
    lines.extend(parse_cardxref())
    lines.append('')

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'w') as f:
        f.write('\n'.join(lines) + '\n')
    print(f"Wrote {len(lines)} lines to {output_path}")


def write_transaction_service_seed():
    """Generate R__load_test_data.sql for transaction-service."""
    output_path = os.path.join(os.path.dirname(__file__),
                               'carddemo-transaction-service', 'src', 'main', 'resources', 'db', 'migration',
                               'R__load_test_data.sql')

    lines = ['-- Repeatable migration: seed test data from COBOL fixed-width files',
             '-- Generated by generate_seed_data.py',
             '',
             '-- Clear existing data',
             'DELETE FROM daily_transaction;',
             'DELETE FROM tran_cat_balance;',
             'DELETE FROM disclosure_group;',
             'DELETE FROM tran_category;',
             'DELETE FROM tran_type;',
             '']

    lines.append('-- Transaction type data (trantype.txt / CVTRA03Y)')
    lines.extend(parse_trantype())
    lines.append('')

    lines.append('-- Transaction category data (trancatg.txt / CVTRA04Y)')
    lines.extend(parse_trancatg())
    lines.append('')

    lines.append('-- Daily transaction data (dailytran.txt / CVTRA06Y)')
    lines.extend(parse_dailytran())
    lines.append('')

    lines.append('-- Transaction category balance data (tcatbal.txt / CVTRA01Y)')
    lines.extend(parse_tcatbal())
    lines.append('')

    lines.append('-- Disclosure group data (discgrp.txt / CVTRA02Y)')
    lines.extend(parse_discgrp())
    lines.append('')

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'w') as f:
        f.write('\n'.join(lines) + '\n')
    print(f"Wrote {len(lines)} lines to {output_path}")


def write_auth_service_seed():
    """Generate R__load_test_data.sql for auth-service with placeholder users."""
    output_path = os.path.join(os.path.dirname(__file__),
                               'carddemo-auth-service', 'src', 'main', 'resources', 'db', 'migration',
                               'R__load_test_data.sql')

    lines = [
        '-- Repeatable migration: seed test users for auth-service',
        '-- Generated by generate_seed_data.py',
        '',
        'DELETE FROM user_security;',
        '',
        "INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES ('admin01', 'System', 'Admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBi0eI7X.jMU0WG10XU8p.KEWKG', 'A');",
        "INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES ('user0001', 'Test', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBi0eI7X.jMU0WG10XU8p.KEWKG', 'U');",
        '',
    ]

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'w') as f:
        f.write('\n'.join(lines) + '\n')
    print(f"Wrote {len(lines)} lines to {output_path}")


if __name__ == '__main__':
    write_auth_service_seed()
    write_account_service_seed()
    write_transaction_service_seed()
    print("Done generating seed data SQL files.")
