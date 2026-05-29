#!/usr/bin/env python3
"""
Parse ASCII fixed-width data files using COBOL copybook PIC clause definitions
and produce structured JSON golden-reference files.

Usage:
    python3 generate_golden_files.py
"""

import json
import os
import re
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(os.path.dirname(SCRIPT_DIR), "app", "data", "ASCII")
OUTPUT_DIR = SCRIPT_DIR

# ---------------------------------------------------------------------------
# Zoned-decimal (sign overpunch) decoder for ASCII COBOL DISPLAY fields
# ---------------------------------------------------------------------------
OVERPUNCH_POS = {'{': 0, 'A': 1, 'B': 2, 'C': 3, 'D': 4,
                 'E': 5, 'F': 6, 'G': 7, 'H': 8, 'I': 9}
OVERPUNCH_NEG = {'}': 0, 'J': 1, 'K': 2, 'L': 3, 'M': 4,
                 'N': 5, 'O': 6, 'P': 7, 'Q': 8, 'R': 9}


def decode_zoned_decimal(raw: str, scale: int) -> str:
    """Decode an ASCII zoned-decimal field with trailing sign overpunch."""
    if not raw or raw.isspace():
        return "0"
    last_char = raw[-1]
    digits = raw[:-1]
    if last_char in OVERPUNCH_POS:
        sign = ""
        digits += str(OVERPUNCH_POS[last_char])
    elif last_char in OVERPUNCH_NEG:
        sign = "-"
        digits += str(OVERPUNCH_NEG[last_char])
    elif last_char.isdigit():
        sign = ""
        digits += last_char
    else:
        return raw.strip()

    digits = digits.lstrip("0") or "0"
    if scale > 0:
        if len(digits) <= scale:
            digits = digits.zfill(scale + 1)
        integer_part = digits[:-scale]
        decimal_part = digits[-scale:]
        return f"{sign}{integer_part}.{decimal_part}"
    return f"{sign}{digits}"


# ---------------------------------------------------------------------------
# Layout definitions (derived from copybooks)
# ---------------------------------------------------------------------------

ACCTDATA_LAYOUT = {
    "copybook": "CVACT01Y.cpy",
    "record_length": 300,
    "description": "Account master record (KSDS, key=ACCT-ID)",
    "fields": [
        {"name": "ACCT-ID",                  "offset": 0,   "length": 11, "type": "numeric",       "pic": "PIC 9(11)",        "desc": "Account identifier"},
        {"name": "ACCT-ACTIVE-STATUS",       "offset": 11,  "length": 1,  "type": "alphanumeric",  "pic": "PIC X(01)",        "desc": "Y=active, N=inactive"},
        {"name": "ACCT-CURR-BAL",            "offset": 12,  "length": 12, "type": "signed_display", "pic": "PIC S9(10)V99",   "desc": "Current account balance", "scale": 2},
        {"name": "ACCT-CREDIT-LIMIT",        "offset": 24,  "length": 12, "type": "signed_display", "pic": "PIC S9(10)V99",   "desc": "Credit limit", "scale": 2},
        {"name": "ACCT-CASH-CREDIT-LIMIT",   "offset": 36,  "length": 12, "type": "signed_display", "pic": "PIC S9(10)V99",   "desc": "Cash advance credit limit", "scale": 2},
        {"name": "ACCT-OPEN-DATE",           "offset": 48,  "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Account open date (YYYY-MM-DD)"},
        {"name": "ACCT-EXPIRAION-DATE",      "offset": 58,  "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Account expiration date"},
        {"name": "ACCT-REISSUE-DATE",        "offset": 68,  "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Last reissue date"},
        {"name": "ACCT-CURR-CYC-CREDIT",     "offset": 78,  "length": 12, "type": "signed_display", "pic": "PIC S9(10)V99",   "desc": "Cycle credit total", "scale": 2},
        {"name": "ACCT-CURR-CYC-DEBIT",      "offset": 90,  "length": 12, "type": "signed_display", "pic": "PIC S9(10)V99",   "desc": "Cycle debit total", "scale": 2},
        {"name": "ACCT-ADDR-ZIP",            "offset": 102, "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Account ZIP code"},
        {"name": "ACCT-GROUP-ID",            "offset": 112, "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Disclosure group identifier"},
        {"name": "FILLER",                   "offset": 122, "length": 178, "type": "filler",       "pic": "PIC X(178)",       "desc": "Reserved space"},
    ]
}

CARDDATA_LAYOUT = {
    "copybook": "CVACT02Y.cpy",
    "record_length": 150,
    "description": "Card master record (KSDS, key=CARD-NUM, AIX on CARD-ACCT-ID)",
    "fields": [
        {"name": "CARD-NUM",                 "offset": 0,   "length": 16, "type": "alphanumeric",  "pic": "PIC X(16)",        "desc": "16-digit card number"},
        {"name": "CARD-ACCT-ID",             "offset": 16,  "length": 11, "type": "numeric",       "pic": "PIC 9(11)",        "desc": "Owning account ID"},
        {"name": "CARD-CVV-CD",              "offset": 27,  "length": 3,  "type": "numeric",       "pic": "PIC 9(03)",        "desc": "Card verification value"},
        {"name": "CARD-EMBOSSED-NAME",       "offset": 30,  "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Name embossed on card"},
        {"name": "CARD-EXPIRAION-DATE",      "offset": 80,  "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Card expiration date"},
        {"name": "CARD-ACTIVE-STATUS",       "offset": 90,  "length": 1,  "type": "alphanumeric",  "pic": "PIC X(01)",        "desc": "Y=active, N=inactive"},
        {"name": "FILLER",                   "offset": 91,  "length": 59, "type": "filler",        "pic": "PIC X(59)",        "desc": "Reserved space"},
    ]
}

CARDXREF_LAYOUT = {
    "copybook": "CVACT03Y.cpy",
    "record_length": 50,
    "description": "Card cross-reference linking cards to customers and accounts (KSDS, key=XREF-CARD-NUM)",
    "fields": [
        {"name": "XREF-CARD-NUM",            "offset": 0,   "length": 16, "type": "alphanumeric",  "pic": "PIC X(16)",        "desc": "Card number (FK to CARDDATA)"},
        {"name": "XREF-CUST-ID",             "offset": 16,  "length": 9,  "type": "numeric",       "pic": "PIC 9(09)",        "desc": "Customer ID (FK to CUSTDATA)"},
        {"name": "XREF-ACCT-ID",             "offset": 25,  "length": 11, "type": "numeric",       "pic": "PIC 9(11)",        "desc": "Account ID (FK to ACCTDATA)"},
        {"name": "FILLER",                   "offset": 36,  "length": 14, "type": "filler",        "pic": "PIC X(14)",        "desc": "Reserved space"},
    ]
}

CUSTDATA_LAYOUT = {
    "copybook": "CVCUS01Y.cpy",
    "record_length": 500,
    "description": "Customer master record (KSDS, key=CUST-ID)",
    "fields": [
        {"name": "CUST-ID",                  "offset": 0,   "length": 9,  "type": "numeric",       "pic": "PIC 9(09)",        "desc": "Customer identifier"},
        {"name": "CUST-FIRST-NAME",          "offset": 9,   "length": 25, "type": "alphanumeric",  "pic": "PIC X(25)",        "desc": "First name"},
        {"name": "CUST-MIDDLE-NAME",         "offset": 34,  "length": 25, "type": "alphanumeric",  "pic": "PIC X(25)",        "desc": "Middle name"},
        {"name": "CUST-LAST-NAME",           "offset": 59,  "length": 25, "type": "alphanumeric",  "pic": "PIC X(25)",        "desc": "Last name"},
        {"name": "CUST-ADDR-LINE-1",         "offset": 84,  "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Address line 1"},
        {"name": "CUST-ADDR-LINE-2",         "offset": 134, "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Address line 2"},
        {"name": "CUST-ADDR-LINE-3",         "offset": 184, "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Address line 3"},
        {"name": "CUST-ADDR-STATE-CD",       "offset": 234, "length": 2,  "type": "alphanumeric",  "pic": "PIC X(02)",        "desc": "State code"},
        {"name": "CUST-ADDR-COUNTRY-CD",     "offset": 236, "length": 3,  "type": "alphanumeric",  "pic": "PIC X(03)",        "desc": "Country code"},
        {"name": "CUST-ADDR-ZIP",            "offset": 239, "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "ZIP/postal code"},
        {"name": "CUST-PHONE-NUM-1",         "offset": 249, "length": 15, "type": "alphanumeric",  "pic": "PIC X(15)",        "desc": "Primary phone"},
        {"name": "CUST-PHONE-NUM-2",         "offset": 264, "length": 15, "type": "alphanumeric",  "pic": "PIC X(15)",        "desc": "Secondary phone"},
        {"name": "CUST-SSN",                 "offset": 279, "length": 9,  "type": "numeric",       "pic": "PIC 9(09)",        "desc": "Social Security Number"},
        {"name": "CUST-GOVT-ISSUED-ID",      "offset": 288, "length": 20, "type": "alphanumeric",  "pic": "PIC X(20)",        "desc": "Government-issued ID"},
        {"name": "CUST-DOB-YYYY-MM-DD",      "offset": 308, "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Date of birth"},
        {"name": "CUST-EFT-ACCOUNT-ID",      "offset": 318, "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Electronic funds transfer account"},
        {"name": "CUST-PRI-CARD-HOLDER-IND", "offset": 328, "length": 1,  "type": "alphanumeric",  "pic": "PIC X(01)",        "desc": "Y=primary cardholder"},
        {"name": "CUST-FICO-CREDIT-SCORE",   "offset": 329, "length": 3,  "type": "numeric",       "pic": "PIC 9(03)",        "desc": "FICO credit score"},
        {"name": "FILLER",                   "offset": 332, "length": 168, "type": "filler",       "pic": "PIC X(168)",       "desc": "Reserved space"},
    ]
}

DAILYTRAN_LAYOUT = {
    "copybook": "CVTRA06Y.cpy",
    "record_length": 350,
    "description": "Daily transaction feed record (sequential, input to POSTTRAN)",
    "fields": [
        {"name": "DALYTRAN-ID",              "offset": 0,   "length": 16, "type": "alphanumeric",  "pic": "PIC X(16)",        "desc": "Transaction identifier"},
        {"name": "DALYTRAN-TYPE-CD",         "offset": 16,  "length": 2,  "type": "alphanumeric",  "pic": "PIC X(02)",        "desc": "Transaction type code (FK to TRANTYPE)"},
        {"name": "DALYTRAN-CAT-CD",          "offset": 18,  "length": 4,  "type": "numeric",       "pic": "PIC 9(04)",        "desc": "Transaction category code"},
        {"name": "DALYTRAN-SOURCE",          "offset": 22,  "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Transaction source system"},
        {"name": "DALYTRAN-DESC",            "offset": 32,  "length": 100,"type": "alphanumeric",  "pic": "PIC X(100)",       "desc": "Transaction description"},
        {"name": "DALYTRAN-AMT",             "offset": 132, "length": 11, "type": "signed_display", "pic": "PIC S9(09)V99",   "desc": "Transaction amount", "scale": 2},
        {"name": "DALYTRAN-MERCHANT-ID",     "offset": 143, "length": 9,  "type": "numeric",       "pic": "PIC 9(09)",        "desc": "Merchant identifier"},
        {"name": "DALYTRAN-MERCHANT-NAME",   "offset": 152, "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Merchant name"},
        {"name": "DALYTRAN-MERCHANT-CITY",   "offset": 202, "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Merchant city"},
        {"name": "DALYTRAN-MERCHANT-ZIP",    "offset": 252, "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Merchant ZIP code"},
        {"name": "DALYTRAN-CARD-NUM",        "offset": 262, "length": 16, "type": "alphanumeric",  "pic": "PIC X(16)",        "desc": "Card number used (FK to CARDXREF)"},
        {"name": "DALYTRAN-ORIG-TS",         "offset": 278, "length": 26, "type": "alphanumeric",  "pic": "PIC X(26)",        "desc": "Origination timestamp"},
        {"name": "DALYTRAN-PROC-TS",         "offset": 304, "length": 26, "type": "alphanumeric",  "pic": "PIC X(26)",        "desc": "Processing timestamp"},
        {"name": "FILLER",                   "offset": 330, "length": 20, "type": "filler",        "pic": "PIC X(20)",        "desc": "Reserved space"},
    ]
}

DISCGRP_LAYOUT = {
    "copybook": "CVTRA02Y.cpy",
    "record_length": 50,
    "description": "Disclosure group record — interest rates by group/type/category",
    "fields": [
        {"name": "DIS-ACCT-GROUP-ID",        "offset": 0,   "length": 10, "type": "alphanumeric",  "pic": "PIC X(10)",        "desc": "Account group identifier (FK to ACCTDATA.ACCT-GROUP-ID)"},
        {"name": "DIS-TRAN-TYPE-CD",         "offset": 10,  "length": 2,  "type": "alphanumeric",  "pic": "PIC X(02)",        "desc": "Transaction type code"},
        {"name": "DIS-TRAN-CAT-CD",          "offset": 12,  "length": 4,  "type": "numeric",       "pic": "PIC 9(04)",        "desc": "Transaction category code"},
        {"name": "DIS-INT-RATE",             "offset": 16,  "length": 6,  "type": "signed_display", "pic": "PIC S9(04)V99",   "desc": "Interest rate (e.g., 1500 = 15.00%)", "scale": 2},
        {"name": "FILLER",                   "offset": 22,  "length": 28, "type": "filler",        "pic": "PIC X(28)",        "desc": "Reserved space"},
    ]
}

TCATBAL_LAYOUT = {
    "copybook": "CVTRA01Y.cpy",
    "record_length": 50,
    "description": "Transaction category balance — running totals by account/type/category",
    "fields": [
        {"name": "TRANCAT-ACCT-ID",          "offset": 0,   "length": 11, "type": "numeric",       "pic": "PIC 9(11)",        "desc": "Account ID (FK to ACCTDATA)"},
        {"name": "TRANCAT-TYPE-CD",          "offset": 11,  "length": 2,  "type": "alphanumeric",  "pic": "PIC X(02)",        "desc": "Transaction type code"},
        {"name": "TRANCAT-CD",               "offset": 13,  "length": 4,  "type": "numeric",       "pic": "PIC 9(04)",        "desc": "Transaction category code"},
        {"name": "TRAN-CAT-BAL",             "offset": 17,  "length": 11, "type": "signed_display", "pic": "PIC S9(09)V99",   "desc": "Category balance amount", "scale": 2},
        {"name": "FILLER",                   "offset": 28,  "length": 22, "type": "filler",        "pic": "PIC X(22)",        "desc": "Reserved space"},
    ]
}

TRANCATG_LAYOUT = {
    "copybook": "CVTRA04Y.cpy",
    "record_length": 60,
    "description": "Transaction category reference — description for each type+category combination",
    "fields": [
        {"name": "TRAN-TYPE-CD",             "offset": 0,   "length": 2,  "type": "alphanumeric",  "pic": "PIC X(02)",        "desc": "Transaction type code"},
        {"name": "TRAN-CAT-CD",              "offset": 2,   "length": 4,  "type": "numeric",       "pic": "PIC 9(04)",        "desc": "Transaction category code"},
        {"name": "TRAN-CAT-TYPE-DESC",       "offset": 6,   "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Category description"},
        {"name": "FILLER",                   "offset": 56,  "length": 4,  "type": "filler",        "pic": "PIC X(04)",        "desc": "Reserved space"},
    ]
}

TRANTYPE_LAYOUT = {
    "copybook": "CVTRA03Y.cpy",
    "record_length": 60,
    "description": "Transaction type reference — description for each transaction type",
    "fields": [
        {"name": "TRAN-TYPE",                "offset": 0,   "length": 2,  "type": "alphanumeric",  "pic": "PIC X(02)",        "desc": "Transaction type code (01=Purchase, 02=Payment, etc.)"},
        {"name": "TRAN-TYPE-DESC",           "offset": 2,   "length": 50, "type": "alphanumeric",  "pic": "PIC X(50)",        "desc": "Type description"},
        {"name": "FILLER",                   "offset": 52,  "length": 8,  "type": "filler",        "pic": "PIC X(08)",        "desc": "Reserved space"},
    ]
}


def parse_field(raw_line: str, field: dict) -> object:
    """Extract and decode a single field from a fixed-width record line."""
    offset = field["offset"]
    length = field["length"]

    # Pad line to full record length if trailing spaces were stripped
    if len(raw_line) < offset + length:
        raw_line = raw_line.ljust(offset + length)

    raw_value = raw_line[offset:offset + length]
    ftype = field["type"]

    if ftype == "filler":
        return raw_value.rstrip()
    elif ftype == "alphanumeric":
        return raw_value.rstrip()
    elif ftype == "numeric":
        stripped = raw_value.strip()
        return stripped if stripped else "0"
    elif ftype == "signed_display":
        scale = field.get("scale", 0)
        return decode_zoned_decimal(raw_value, scale)
    return raw_value.rstrip()


def parse_file(filepath: str, layout: dict) -> list:
    """Parse a fixed-width data file into a list of record dicts."""
    records = []
    rec_len = layout["record_length"]
    with open(filepath, "r", encoding="ascii", errors="replace") as f:
        for line_num, raw_line in enumerate(f, 1):
            # Strip newline characters but preserve spaces within record
            raw_line = raw_line.rstrip("\n").rstrip("\r")
            if not raw_line.strip():
                continue
            record = {"_record_number": line_num}
            for field in layout["fields"]:
                record[field["name"]] = parse_field(raw_line, field)
            records.append(record)
    return records


def generate_golden_file(data_filename: str, layout: dict, output_dir: str) -> str:
    """Parse a data file and write its golden JSON representation."""
    input_path = os.path.join(DATA_DIR, data_filename)
    if not os.path.exists(input_path):
        print(f"WARNING: {input_path} not found, skipping", file=sys.stderr)
        return None

    records = parse_file(input_path, layout)
    output = {
        "source_file": f"app/data/ASCII/{data_filename}",
        "copybook": layout["copybook"],
        "description": layout["description"],
        "record_length": layout["record_length"],
        "record_count": len(records),
        "field_definitions": [
            {
                "name": f["name"],
                "offset": f["offset"],
                "length": f["length"],
                "pic": f["pic"],
                "description": f["desc"],
            }
            for f in layout["fields"]
            if f["type"] != "filler"
        ],
        "records": records,
    }

    out_filename = data_filename.replace(".txt", ".golden.json")
    out_path = os.path.join(output_dir, out_filename)
    with open(out_path, "w") as f:
        json.dump(output, f, indent=2)
    print(f"Generated {out_path} ({len(records)} records)")
    return out_path


def main():
    files_and_layouts = [
        ("acctdata.txt", ACCTDATA_LAYOUT),
        ("carddata.txt", CARDDATA_LAYOUT),
        ("cardxref.txt", CARDXREF_LAYOUT),
        ("custdata.txt", CUSTDATA_LAYOUT),
        ("dailytran.txt", DAILYTRAN_LAYOUT),
        ("discgrp.txt", DISCGRP_LAYOUT),
        ("tcatbal.txt", TCATBAL_LAYOUT),
        ("trancatg.txt", TRANCATG_LAYOUT),
        ("trantype.txt", TRANTYPE_LAYOUT),
    ]

    for data_file, layout in files_and_layouts:
        generate_golden_file(data_file, layout, OUTPUT_DIR)

    print("\nGolden file generation complete.")


if __name__ == "__main__":
    main()
