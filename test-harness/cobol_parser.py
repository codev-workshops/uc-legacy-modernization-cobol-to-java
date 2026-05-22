#!/usr/bin/env python3
"""
cobol_parser.py - Parse fixed-width COBOL data files using copybook PIC
clause definitions and produce structured JSON golden-file output.

Usage:
    python cobol_parser.py --input-dir app/data/ASCII \
                           --copybook-dir app/cpy \
                           --output-dir golden-files

Each output JSON file contains:
    {
        "metadata": { copybook, record_length, field_count, record_count, ... },
        "fields": [ { name, pic, type, start, length, description }, ... ],
        "records": [ { field_name: value, ... }, ... ],
        "control_totals": { field_name: sum_value, ... }
    }
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys
from dataclasses import asdict, dataclass, field
from pathlib import Path
from typing import Any


# ---------------------------------------------------------------------------
# PIC clause helpers
# ---------------------------------------------------------------------------

@dataclass
class FieldDef:
    """A single field parsed from a copybook."""
    name: str
    pic: str
    pic_type: str       # "alpha", "numeric", "signed_decimal"
    start: int          # 0-based byte offset
    length: int         # byte length in the data file
    decimal_places: int
    description: str

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


def pic_length(pic: str) -> tuple[int, int, str]:
    """Return (byte_length, decimal_places, type) for a PIC clause."""
    pic = pic.upper().replace(" ", "")

    # Expand shorthand: 9(11) -> 99999999999, X(50) -> XX...
    def expand(p: str) -> str:
        return re.sub(
            r"([9XSVA])(\((\d+)\))",
            lambda m: m.group(1) * int(m.group(3)),
            p,
        )

    expanded = expand(pic)

    # Remove leading S (sign)
    has_sign = expanded.startswith("S")
    core = expanded.lstrip("S")

    if "V" in core:
        int_part, dec_part = core.split("V", 1)
        int_digits = len(int_part.replace("9", "9"))
        dec_digits = len(dec_part.replace("9", "9"))
        total_length = int_digits + dec_digits
        if has_sign:
            # Trailing sign occupies last digit position in display format
            pass
        return total_length, dec_digits, "signed_decimal" if has_sign else "numeric"

    if set(core) <= {"9"}:
        return len(core), 0, "numeric"

    if set(core) <= {"X"}:
        return len(core), 0, "alpha"

    # Fallback
    clean = core.replace("S", "").replace("V", "")
    return len(clean), 0, "alpha"


# ---------------------------------------------------------------------------
# Copybook layout definitions (hard-coded from app/cpy/)
# ---------------------------------------------------------------------------

LAYOUTS: dict[str, list[tuple[str, str, str]]] = {}
"""Maps data-file stem -> list of (field_name, pic_clause, description)."""

LAYOUTS["acctdata"] = [
    ("ACCT-ID",                "9(11)",       "Account identifier (primary key)"),
    ("ACCT-ACTIVE-STATUS",     "X(01)",       "Account active flag (Y/N)"),
    ("ACCT-CURR-BAL",          "S9(10)V99",   "Current balance (signed, 2 decimals)"),
    ("ACCT-CREDIT-LIMIT",      "S9(10)V99",   "Credit limit"),
    ("ACCT-CASH-CREDIT-LIMIT", "S9(10)V99",   "Cash advance credit limit"),
    ("ACCT-OPEN-DATE",         "X(10)",       "Account open date (YYYY-MM-DD)"),
    ("ACCT-EXPIRAION-DATE",    "X(10)",       "Account expiration date (YYYY-MM-DD)"),
    ("ACCT-REISSUE-DATE",      "X(10)",       "Card reissue date (YYYY-MM-DD)"),
    ("ACCT-CURR-CYC-CREDIT",   "S9(10)V99",  "Current cycle credit total"),
    ("ACCT-CURR-CYC-DEBIT",    "S9(10)V99",  "Current cycle debit total"),
    ("ACCT-ADDR-ZIP",          "X(10)",       "Account holder ZIP code"),
    ("ACCT-GROUP-ID",          "X(10)",       "Disclosure group identifier"),
    ("FILLER",                 "X(178)",      "Reserved / unused space"),
]

LAYOUTS["carddata"] = [
    ("CARD-NUM",               "X(16)",       "Card number (16-digit PAN)"),
    ("CARD-ACCT-ID",           "9(11)",       "Associated account identifier"),
    ("CARD-CVV-CD",            "9(03)",       "Card verification value"),
    ("CARD-EMBOSSED-NAME",     "X(50)",       "Name embossed on the card"),
    ("CARD-EXPIRAION-DATE",    "X(10)",       "Card expiration date (YYYY-MM-DD)"),
    ("CARD-ACTIVE-STATUS",     "X(01)",       "Card active flag (Y/N)"),
    ("FILLER",                 "X(59)",       "Reserved / unused space"),
]

LAYOUTS["cardxref"] = [
    ("XREF-CARD-NUM",          "X(16)",       "Card number (foreign key to carddata)"),
    ("XREF-CUST-ID",           "9(09)",       "Customer identifier (FK to custdata)"),
    ("XREF-ACCT-ID",           "9(11)",       "Account identifier (FK to acctdata)"),
    ("FILLER",                 "X(14)",       "Reserved / unused space"),
]

LAYOUTS["custdata"] = [
    ("CUST-ID",                "9(09)",       "Customer identifier (primary key)"),
    ("CUST-FIRST-NAME",        "X(25)",       "Customer first name"),
    ("CUST-MIDDLE-NAME",       "X(25)",       "Customer middle name"),
    ("CUST-LAST-NAME",         "X(25)",       "Customer last name"),
    ("CUST-ADDR-LINE-1",       "X(50)",       "Address line 1"),
    ("CUST-ADDR-LINE-2",       "X(50)",       "Address line 2"),
    ("CUST-ADDR-LINE-3",       "X(50)",       "Address line 3 (city)"),
    ("CUST-ADDR-STATE-CD",     "X(02)",       "State code"),
    ("CUST-ADDR-COUNTRY-CD",   "X(03)",       "Country code"),
    ("CUST-ADDR-ZIP",          "X(10)",       "ZIP / postal code"),
    ("CUST-PHONE-NUM-1",       "X(15)",       "Primary phone number"),
    ("CUST-PHONE-NUM-2",       "X(15)",       "Secondary phone number"),
    ("CUST-SSN",               "9(09)",       "Social Security Number"),
    ("CUST-GOVT-ISSUED-ID",    "X(20)",       "Government-issued ID number"),
    ("CUST-DOB-YYYY-MM-DD",    "X(10)",       "Date of birth (YYYY-MM-DD)"),
    ("CUST-EFT-ACCOUNT-ID",    "X(10)",       "EFT / bank account identifier"),
    ("CUST-PRI-CARD-HOLDER-IND", "X(01)",     "Primary card holder indicator"),
    ("CUST-FICO-CREDIT-SCORE", "9(03)",       "FICO credit score"),
    ("FILLER",                 "X(168)",      "Reserved / unused space"),
]

LAYOUTS["dailytran"] = [
    ("DALYTRAN-ID",            "X(16)",       "Transaction identifier"),
    ("DALYTRAN-TYPE-CD",       "X(02)",       "Transaction type code"),
    ("DALYTRAN-CAT-CD",        "9(04)",       "Transaction category code"),
    ("DALYTRAN-SOURCE",        "X(10)",       "Transaction source system"),
    ("DALYTRAN-DESC",          "X(100)",      "Transaction description"),
    ("DALYTRAN-AMT",           "S9(09)V99",   "Transaction amount (signed, 2 dec)"),
    ("DALYTRAN-MERCHANT-ID",   "9(09)",       "Merchant identifier"),
    ("DALYTRAN-MERCHANT-NAME", "X(50)",       "Merchant name"),
    ("DALYTRAN-MERCHANT-CITY", "X(50)",       "Merchant city"),
    ("DALYTRAN-MERCHANT-ZIP",  "X(10)",       "Merchant ZIP code"),
    ("DALYTRAN-CARD-NUM",      "X(16)",       "Card number used in transaction"),
    ("DALYTRAN-ORIG-TS",       "X(26)",       "Original timestamp"),
    ("DALYTRAN-PROC-TS",       "X(26)",       "Processing timestamp"),
    ("FILLER",                 "X(20)",       "Reserved / unused space"),
]

LAYOUTS["discgrp"] = [
    ("DIS-ACCT-GROUP-ID",      "X(10)",       "Disclosure account group ID"),
    ("DIS-TRAN-TYPE-CD",       "X(02)",       "Transaction type code"),
    ("DIS-TRAN-CAT-CD",        "9(04)",       "Transaction category code"),
    ("DIS-INT-RATE",           "S9(04)V99",   "Interest rate (signed, 2 decimals)"),
    ("FILLER",                 "X(28)",       "Reserved / unused space"),
]

LAYOUTS["tcatbal"] = [
    ("TRANCAT-ACCT-ID",        "9(11)",       "Account identifier"),
    ("TRANCAT-TYPE-CD",        "X(02)",       "Transaction type code"),
    ("TRANCAT-CD",             "9(04)",       "Transaction category code"),
    ("TRAN-CAT-BAL",           "S9(09)V99",   "Category balance (signed, 2 dec)"),
    ("FILLER",                 "X(22)",       "Reserved / unused space"),
]

LAYOUTS["trancatg"] = [
    ("TRAN-TYPE-CD",           "X(02)",       "Transaction type code"),
    ("TRAN-CAT-CD",            "9(04)",       "Transaction category code"),
    ("TRAN-CAT-TYPE-DESC",     "X(50)",       "Category type description"),
    ("FILLER",                 "X(04)",       "Reserved / unused space"),
]

LAYOUTS["trantype"] = [
    ("TRAN-TYPE",              "X(02)",       "Transaction type code"),
    ("TRAN-TYPE-DESC",         "X(50)",       "Transaction type description"),
    ("FILLER",                 "X(08)",       "Reserved / unused space"),
]

COPYBOOK_MAP: dict[str, str] = {
    "acctdata":  "CVACT01Y.cpy",
    "carddata":  "CVACT02Y.cpy",
    "cardxref":  "CVACT03Y.cpy",
    "custdata":  "CVCUS01Y.cpy",
    "dailytran": "CVTRA06Y.cpy",
    "discgrp":   "CVTRA02Y.cpy",
    "tcatbal":   "CVTRA01Y.cpy",
    "trancatg":  "CVTRA04Y.cpy",
    "trantype":  "CVTRA03Y.cpy",
}


# ---------------------------------------------------------------------------
# Signed-numeric decoding (COBOL display / trailing-sign)
# ---------------------------------------------------------------------------

POSITIVE_SIGNS = {"{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
                  "E": "5", "F": "6", "G": "7", "H": "8", "I": "9"}
NEGATIVE_SIGNS = {"}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
                  "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9"}


def decode_signed_numeric(raw: str, decimal_places: int) -> float | str:
    """Decode a COBOL display-format signed numeric field."""
    raw = raw.strip()
    if not raw:
        return 0.0

    last_char = raw[-1]
    if last_char in POSITIVE_SIGNS:
        digits = raw[:-1] + POSITIVE_SIGNS[last_char]
        sign = 1
    elif last_char in NEGATIVE_SIGNS:
        digits = raw[:-1] + NEGATIVE_SIGNS[last_char]
        sign = -1
    elif last_char.isdigit():
        digits = raw
        sign = 1
    else:
        return raw  # Can't decode; return as-is

    try:
        int_val = int(digits)
    except ValueError:
        return raw

    if decimal_places > 0:
        return sign * int_val / (10 ** decimal_places)
    return sign * int_val


# ---------------------------------------------------------------------------
# Field definition builder
# ---------------------------------------------------------------------------

def build_field_defs(layout_name: str) -> list[FieldDef]:
    """Build FieldDef list from the hard-coded layout."""
    raw = LAYOUTS[layout_name]
    defs: list[FieldDef] = []
    offset = 0
    for name, pic, desc in raw:
        length, dec, ptype = pic_length(pic)
        defs.append(FieldDef(
            name=name, pic=pic, pic_type=ptype,
            start=offset, length=length,
            decimal_places=dec, description=desc,
        ))
        offset += length
    return defs


# ---------------------------------------------------------------------------
# Record parser
# ---------------------------------------------------------------------------

def parse_record(line: str, field_defs: list[FieldDef]) -> dict[str, Any]:
    """Parse a single fixed-width record into a dict."""
    record: dict[str, Any] = {}
    for fd in field_defs:
        raw = line[fd.start:fd.start + fd.length]

        if fd.pic_type == "alpha":
            record[fd.name] = raw.rstrip()
        elif fd.pic_type == "signed_decimal":
            record[fd.name] = decode_signed_numeric(raw, fd.decimal_places)
        elif fd.pic_type == "numeric":
            stripped = raw.strip()
            try:
                record[fd.name] = int(stripped) if stripped else 0
            except ValueError:
                record[fd.name] = stripped
        else:
            record[fd.name] = raw.rstrip()

    return record


def parse_file(data_path: Path, layout_name: str) -> dict[str, Any]:
    """Parse an entire data file and return the golden-file structure."""
    field_defs = build_field_defs(layout_name)
    expected_reclen = sum(fd.length for fd in field_defs)

    records: list[dict[str, Any]] = []
    with open(data_path, "r", encoding="ascii", errors="replace") as fh:
        for line in fh:
            line = line.rstrip("\n").rstrip("\r")
            if not line:
                continue
            # Pad or truncate to expected record length
            line = line.ljust(expected_reclen)
            records.append(parse_record(line, field_defs))

    # Compute control totals for numeric / signed-decimal fields
    control_totals: dict[str, float] = {}
    for fd in field_defs:
        if fd.pic_type in ("numeric", "signed_decimal") and fd.name != "FILLER":
            total = 0.0
            for rec in records:
                val = rec.get(fd.name, 0)
                if isinstance(val, (int, float)):
                    total += val
            control_totals[fd.name] = round(total, 2)

    # SHA-256 of canonical JSON records
    canonical = json.dumps(records, sort_keys=True, separators=(",", ":"))
    sha256 = hashlib.sha256(canonical.encode("utf-8")).hexdigest()

    return {
        "metadata": {
            "source_file": data_path.name,
            "copybook": COPYBOOK_MAP.get(layout_name, "unknown"),
            "record_length": expected_reclen,
            "field_count": len(field_defs),
            "record_count": len(records),
            "sha256": sha256,
        },
        "fields": [fd.to_dict() for fd in field_defs],
        "records": records,
        "control_totals": control_totals,
    }


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Parse COBOL fixed-width data files into golden-file JSON."
    )
    parser.add_argument("--input-dir", required=True, help="Directory with ASCII .txt files")
    parser.add_argument("--copybook-dir", default=None, help="Copybook directory (for reference)")
    parser.add_argument("--output-dir", required=True, help="Output directory for JSON files")
    parser.add_argument("--files", nargs="*", default=None, help="Specific file stems to process")
    args = parser.parse_args()

    input_dir = Path(args.input_dir)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    stems = args.files if args.files else list(LAYOUTS.keys())

    for stem in stems:
        if stem not in LAYOUTS:
            print(f"WARNING: No layout defined for '{stem}', skipping.", file=sys.stderr)
            continue

        data_path = input_dir / f"{stem}.txt"
        if not data_path.exists():
            print(f"WARNING: Data file not found: {data_path}, skipping.", file=sys.stderr)
            continue

        print(f"Parsing {data_path.name} with layout '{stem}'...")
        result = parse_file(data_path, stem)

        out_path = output_dir / f"{stem}.json"
        with open(out_path, "w", encoding="utf-8") as fh:
            json.dump(result, fh, indent=2, sort_keys=False)

        # Also write SHA-256 sidecar
        sha_path = output_dir / f"{stem}.sha256"
        sha_path.write_text(result["metadata"]["sha256"] + "\n")

        print(f"  -> {out_path} ({result['metadata']['record_count']} records)")

    print("Done.")


if __name__ == "__main__":
    main()
