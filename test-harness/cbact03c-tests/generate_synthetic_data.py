#!/usr/bin/env python3
"""Generate synthetic test data for CBACT03C test cases.

Record layout (CVACT03Y copybook — 50 bytes fixed):
  XREF-CARD-NUM   PIC X(16)   offset  0  length 16  ALPHANUMERIC
  XREF-CUST-ID    PIC 9(09)   offset 16  length  9  DISPLAY_NUMERIC
  XREF-ACCT-ID    PIC 9(11)   offset 25  length 11  DISPLAY_NUMERIC
  FILLER          PIC X(14)   offset 36  length 14  FILLER
"""

from __future__ import annotations

import os
import pathlib

RECORD_LEN = 50

# Output directory
SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
OUTPUT_DIR = SCRIPT_DIR / "data" / "input"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def make_record(
    card_num: str,
    cust_id: int | str = 0,
    acct_id: int | str = 0,
    filler: str = "",
) -> bytes:
    """Build a single 50-byte ASCII record.

    * card_num — left-justified, space-padded to 16 bytes (PIC X(16))
    * cust_id  — zero-padded left to 9 digits (PIC 9(09)); if str, used raw
    * acct_id  — zero-padded left to 11 digits (PIC 9(11)); if str, used raw
    * filler   — left-justified, space-padded to 14 bytes (PIC X(14))
    """
    # Card number: PIC X(16) — left-justified, space-padded right
    card_field = card_num.ljust(16)[:16]

    # Customer ID: PIC 9(09) — zero-padded left
    if isinstance(cust_id, int):
        cust_field = str(cust_id).zfill(9)[:9]
    else:
        cust_field = str(cust_id).ljust(9)[:9]

    # Account ID: PIC 9(11) — zero-padded left
    if isinstance(acct_id, int):
        acct_field = str(acct_id).zfill(11)[:11]
    else:
        acct_field = str(acct_id).ljust(11)[:11]

    # Filler: PIC X(14) — left-justified, space-padded right
    filler_field = filler.ljust(14)[:14]

    rec = (card_field + cust_field + acct_field + filler_field).encode("ascii")
    assert len(rec) == RECORD_LEN, f"Record length {len(rec)} != {RECORD_LEN}"
    return rec


def write_dat(filename: str, records: list[bytes]) -> None:
    """Write records to a .dat file (raw concatenation, no line separators)."""
    path = OUTPUT_DIR / filename
    with open(path, "wb") as f:
        for rec in records:
            f.write(rec)
    print(f"  wrote {path.name}  ({len(records)} record(s), {path.stat().st_size} bytes)")


# ---------------------------------------------------------------------------
# Test-case generators
# ---------------------------------------------------------------------------

def generate_all() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # TC-01: Single valid record
    write_dat("tc01_single_record.dat", [
        make_record("4111111111111111", 123456789, 12345678901),
    ])

    # TC-02: Multiple valid records (5)
    write_dat("tc02_multiple_records.dat", [
        make_record("1000000000000001", 100000001, 10000000001),
        make_record("2000000000000002", 200000002, 20000000002),
        make_record("3000000000000003", 300000003, 30000000003),
        make_record("4000000000000004", 400000004, 40000000004),
        make_record("5000000000000005", 500000005, 50000000005),
    ])

    # TC-03: Large dataset (100 records)
    write_dat("tc03_large_dataset.dat", [
        make_record(
            f"CARD{i:012d}",
            cust_id=100000000 + i,
            acct_id=10000000000 + i,
        )
        for i in range(1, 101)
    ])

    # TC-04: All-zero numeric fields
    write_dat("tc04_zero_ids.dat", [
        make_record("ZEROTEST00000001", 0, 0),
        make_record("ZEROTEST00000002", 0, 0),
    ])

    # TC-05: Leading spaces in card number
    write_dat("tc05_leading_spaces.dat", [
        make_record("   1234567890123", 111111111, 11111111111),
    ])

    # TC-06: Special characters in card number
    write_dat("tc06_special_chars.dat", [
        make_record("@#$%&*()-+=<>{}", 222222222, 22222222222),
    ])

    # TC-07: Maximum CUST-ID
    write_dat("tc07_max_cust_id.dat", [
        make_record("MAXCUST000000001", 999999999, 55555555555),
    ])

    # TC-08: Maximum ACCT-ID
    write_dat("tc08_max_acct_id.dat", [
        make_record("MAXACCT000000001", 555555555, 99999999999),
    ])

    # TC-09: Card number all spaces
    write_dat("tc09_all_spaces_cardnum.dat", [
        make_record("                ", 333333333, 33333333333),
    ])

    # TC-10: Non-empty FILLER
    write_dat("tc10_nonempty_filler.dat", [
        make_record("FILLERTEST000001", 444444444, 44444444444, "TESTFILLER1234"),
    ])

    # TC-12: Typical card numbers
    write_dat("tc12_typical_cards.dat", [
        make_record("3782822463100050", 300000001, 30000000001),  # Amex
        make_record("4111111111111111", 400000001, 40000000001),  # Visa
        make_record("5500000000000004", 500000001, 50000000001),  # MC
    ])

    # TC-13: CBIMPORT initialized records (FILLER = spaces)
    write_dat("tc13_initialized_filler.dat", [
        make_record("IMPRT00000000001", 600000001, 60000000001, "              "),
        make_record("IMPRT00000000002", 600000002, 60000000002, "              "),
        make_record("IMPRT00000000003", 600000003, 60000000003, "              "),
    ])

    # TC-16: Records NOT in key order (for REPRO sort test)
    write_dat("tc16_unsorted_input.dat", [
        make_record("EEEEE00000000005", 700000005, 70000000005),
        make_record("CCCCC00000000003", 700000003, 70000000003),
        make_record("AAAAA00000000001", 700000001, 70000000001),
        make_record("DDDDD00000000004", 700000004, 70000000004),
        make_record("BBBBB00000000002", 700000002, 70000000002),
    ])

    # TC-18: Non-unique AIX (same ACCT-ID, different card numbers)
    write_dat("tc18_nonunique_aix.dat", [
        make_record("AIXTEST000000001", 800000001, 12345678),
        make_record("AIXTEST000000002", 800000002, 12345678),
        make_record("AIXTEST000000003", 800000003, 12345678),
    ])

    # TC-19: Empty file (0 records)
    write_dat("tc19_empty.dat", [])


if __name__ == "__main__":
    print("Generating synthetic test data for CBACT03C …")
    generate_all()
    print("Done.")
