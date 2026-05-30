#!/usr/bin/env python3
"""
Synthetic test-data generator for CBACT03C (Card Cross-Reference reader).

Record layout (copybook CVACT03Y, file app/cpy/CVACT03Y.cpy — 50 bytes):
  Offset  0: XREF-CARD-NUM  PIC X(16)   — primary key
  Offset 16: XREF-CUST-ID   PIC 9(09)   — 9-digit numeric customer ID
  Offset 25: XREF-ACCT-ID   PIC 9(11)   — 11-digit numeric account ID
  Offset 36: FILLER         PIC X(14)

Export layout (copybook CVEXPORT, file app/cpy/CVEXPORT.cpy — 500 bytes):
  Offset  0: EXPORT-REC-TYPE          PIC X(1)    — 'X' for XREF
  Offset  1: EXPORT-TIMESTAMP         PIC X(26)
  Offset 27: EXPORT-SEQUENCE-NUM      PIC 9(9) COMP   — 4 bytes big-endian
  Offset 31: EXPORT-BRANCH-ID         PIC X(4)
  Offset 35: EXPORT-REGION-CODE       PIC X(5)
  Offset 40: EXPORT-RECORD-DATA       PIC X(460)
    within EXPORT-CARD-XREF-DATA (REDEFINES):
      Offset 40: EXP-XREF-CARD-NUM    PIC X(16)
      Offset 56: EXP-XREF-CUST-ID     PIC 9(09)   — display
      Offset 65: EXP-XREF-ACCT-ID     PIC 9(11) COMP — 8 bytes big-endian
      Offset 73: FILLER               PIC X(427)

CBACT03C reads XREFFILE (VSAM KSDS, sequential) and DISPLAYs each record
TWICE per iteration (known bug: line 96 + line 78 in app/cbl/CBACT03C.cbl).
"""
from __future__ import annotations

import argparse
import os
import random
import struct
import sys
from datetime import datetime
from pathlib import Path
from typing import List, Optional, Tuple

# ---------------------------------------------------------------------------
# Constants — derived from COBOL copybooks
# ---------------------------------------------------------------------------
RECORD_LEN = 50        # CVACT03Y total record length
CARD_NUM_LEN = 16      # XREF-CARD-NUM  PIC X(16)
CUST_ID_LEN = 9        # XREF-CUST-ID   PIC 9(09)
ACCT_ID_LEN = 11       # XREF-ACCT-ID   PIC 9(11)
FILLER_LEN = 14        # FILLER          PIC X(14)

EXPORT_REC_LEN = 500   # CVEXPORT total record length
EXPORT_DATA_OFFSET = 40
EXPORT_DATA_LEN = 460

# EBCDIC code page 037 translation table (ASCII -> EBCDIC)
_ASCII_TO_EBCDIC_037 = (
    b'\x00\x01\x02\x03\x37\x2d\x2e\x2f\x16\x05\x25\x0b\x0c\x0d\x0e\x0f'
    b'\x10\x11\x12\x13\x3c\x3d\x32\x26\x18\x19\x3f\x27\x1c\x1d\x1e\x1f'
    b'\x40\x5a\x7f\x7b\x5b\x6c\x50\x7d\x4d\x5d\x5c\x4e\x6b\x60\x4b\x61'
    b'\xf0\xf1\xf2\xf3\xf4\xf5\xf6\xf7\xf8\xf9\x7a\x5e\x4c\x7e\x6e\x6f'
    b'\x7c\xc1\xc2\xc3\xc4\xc5\xc6\xc7\xc8\xc9\xd1\xd2\xd3\xd4\xd5\xd6'
    b'\xd7\xd8\xd9\xe2\xe3\xe4\xe5\xe6\xe7\xe8\xe9\xad\xe0\xbd\x5f\x6d'
    b'\x79\x81\x82\x83\x84\x85\x86\x87\x88\x89\x91\x92\x93\x94\x95\x96'
    b'\x97\x98\x99\xa2\xa3\xa4\xa5\xa6\xa7\xa8\xa9\xc0\x4f\xd0\xa1\x07'
    b'\x20\x21\x22\x23\x24\x15\x06\x17\x28\x29\x2a\x2b\x2c\x09\x0a\x1b'
    b'\x30\x31\x1a\x33\x34\x35\x36\x08\x38\x39\x3a\x3b\x04\x14\x3e\xff'
    b'\x41\xaa\x4a\xb1\x9f\xb2\x6a\xb5\xbb\xb4\x9a\x8a\xb0\xca\xaf\xbc'
    b'\x90\x8f\xea\xfa\xbe\xa0\xb6\xb3\x9d\xda\x9b\x8b\xb7\xb8\xb9\xab'
    b'\x64\x65\x62\x66\x63\x67\x9e\x68\x74\x71\x72\x73\x78\x75\x76\x77'
    b'\xac\x69\xed\xee\xeb\xef\xec\xbf\x80\xfd\xfe\xfb\xfc\xba\xae\x59'
    b'\x44\x45\x42\x46\x43\x47\x9c\x48\x54\x51\x52\x53\x58\x55\x56\x57'
    b'\x8c\x49\xcd\xce\xcb\xcf\xcc\xe1\x70\xdd\xde\xdb\xdc\x8d\x8e\xdf'
)


def ascii_to_ebcdic(data: bytes) -> bytes:
    """Translate ASCII bytes to EBCDIC code page 037."""
    return data.translate(_ASCII_TO_EBCDIC_037)


# ---------------------------------------------------------------------------
# Record helpers
# ---------------------------------------------------------------------------

def generate_xref_record(
    card_num: str,
    cust_id: int,
    acct_id: int,
    filler: Optional[str] = None,
) -> bytes:
    """Build a 50-byte fixed-length XREF record.

    Args:
        card_num: Card number, left-justified, space-padded to 16 chars.
        cust_id:  Customer ID, zero-padded to 9 digits.
        acct_id:  Account ID, zero-padded to 11 digits.
        filler:   14 bytes of filler; defaults to spaces.

    Returns:
        50-byte ``bytes`` object matching CVACT03Y layout.
    """
    # XREF-CARD-NUM — PIC X(16): left-justified, space-padded
    b_card = card_num.ljust(CARD_NUM_LEN)[:CARD_NUM_LEN].encode("ascii")
    # XREF-CUST-ID — PIC 9(09): zero-padded display numeric
    b_cust = f"{cust_id:09d}".encode("ascii")
    # XREF-ACCT-ID — PIC 9(11): zero-padded display numeric
    b_acct = f"{acct_id:011d}".encode("ascii")
    # FILLER — PIC X(14)
    if filler is None:
        b_fill = b" " * FILLER_LEN
    else:
        b_fill = filler.ljust(FILLER_LEN)[:FILLER_LEN].encode("ascii")

    rec = b_card + b_cust + b_acct + b_fill
    assert len(rec) == RECORD_LEN, f"Record length {len(rec)} != {RECORD_LEN}"
    return rec


def generate_export_record(
    card_num: str,
    cust_id: int,
    acct_id: int,
    seq_num: int = 1,
    branch_id: str = "BR01",
    region_code: str = "US-E1",
    timestamp: Optional[str] = None,
) -> bytes:
    """Build a 500-byte CBIMPORT-compatible export record (type 'X').

    Layout per CVEXPORT.cpy (app/cpy/CVEXPORT.cpy):
      Byte  0     : EXPORT-REC-TYPE       PIC X(1)   = 'X'
      Bytes 1-26  : EXPORT-TIMESTAMP      PIC X(26)
      Bytes 27-30 : EXPORT-SEQUENCE-NUM   PIC 9(9) COMP  (4 bytes big-endian)
      Bytes 31-34 : EXPORT-BRANCH-ID      PIC X(4)
      Bytes 35-39 : EXPORT-REGION-CODE    PIC X(5)
      Bytes 40-499: EXPORT-RECORD-DATA    PIC X(460)
        EXPORT-CARD-XREF-DATA (REDEFINES):
          Bytes 40-55 : EXP-XREF-CARD-NUM   PIC X(16)
          Bytes 56-64 : EXP-XREF-CUST-ID    PIC 9(09)  — display
          Bytes 65-72 : EXP-XREF-ACCT-ID    PIC 9(11) COMP — 8 bytes big-endian
          Bytes 73-499: FILLER              PIC X(427)
    """
    if timestamp is None:
        timestamp = datetime.now().strftime("%Y-%m-%d-%H.%M.%S.%f")[:26]

    rec_type = b"X"
    ts = timestamp.ljust(26)[:26].encode("ascii")
    seq = struct.pack(">I", seq_num)                  # PIC 9(9) COMP = 4 bytes
    branch = branch_id.ljust(4)[:4].encode("ascii")
    region = region_code.ljust(5)[:5].encode("ascii")

    # EXPORT-CARD-XREF-DATA within EXPORT-RECORD-DATA (460 bytes)
    exp_card = card_num.ljust(CARD_NUM_LEN)[:CARD_NUM_LEN].encode("ascii")
    exp_cust = f"{cust_id:09d}".encode("ascii")
    # EXP-XREF-ACCT-ID is PIC 9(11) COMP — 8-byte big-endian binary
    exp_acct = struct.pack(">q", acct_id)
    exp_filler = b" " * 427

    data_area = exp_card + exp_cust + exp_acct + exp_filler
    assert len(data_area) == EXPORT_DATA_LEN, (
        f"Export data area length {len(data_area)} != {EXPORT_DATA_LEN}"
    )

    record = rec_type + ts + seq + branch + region + data_area
    assert len(record) == EXPORT_REC_LEN, (
        f"Export record length {len(record)} != {EXPORT_REC_LEN}"
    )
    return record


def build_expected_output(records: List[bytes]) -> str:
    """Build expected SYSOUT output for CBACT03C.

    CBACT03C displays each record TWICE per iteration:
      1. In 1000-XREFFILE-GET-NEXT (line 96) upon successful READ
      2. In the main PROCEDURE DIVISION loop (line 78)
    """
    lines: list[str] = []
    lines.append("START OF EXECUTION OF PROGRAM CBACT03C")
    for rec in records:
        display_text = rec.decode("ascii", errors="replace")
        # Duplicate DISPLAY — known bug (line 96 + line 78)
        lines.append(display_text)
        lines.append(display_text)
    lines.append("END OF EXECUTION OF PROGRAM CBACT03C")
    return "\n".join(lines) + "\n"


# ---------------------------------------------------------------------------
# Scenario catalogue
# ---------------------------------------------------------------------------

SCENARIOS: dict[str, str] = {
    "TC-A01": "Empty XREF file (0 records)",
    "TC-A02": "Single record with valid data",
    "TC-A03": "5 records with distinct card numbers, sorted ascending",
    "TC-A04": "50 records matching app/data/ASCII/cardxref.txt format",
    "TC-A05": "Boundary values — max card num, max cust ID, max acct ID",
    "TC-B01": "Card number with leading zeros (0000000000000001)",
    "TC-B02": "Alphanumeric card number (ABCD123456789012)",
    "TC-B03": "Customer ID with leading zeros (000000001)",
    "TC-B04": "Account ID near 11-digit max",
    "TC-B05": "FILLER area filled with non-space data ('X' characters)",
    "TC-B06": "Zero customer ID (000000000)",
    "TC-B07": "Zero account ID (00000000000)",
    "TC-E04": "INITIALIZE defaults — zeros for numeric, spaces for alpha",
    "TC-F01": "Stress test — 10,000 records with randomized valid data",
    "TC-F02": "Key boundaries — all zeros and all nines card numbers",
}


def _gen_records(scenario: str, record_count: Optional[int]) -> List[bytes]:
    """Return a list of 50-byte records for the given scenario."""

    if scenario == "TC-A01":
        # Empty file
        return []

    if scenario == "TC-A02":
        # Single valid record
        return [generate_xref_record("4000123456789012", 1, 1)]

    if scenario == "TC-A03":
        # 5 records, sorted ascending by card_num
        n = record_count if record_count is not None else 5
        data = [
            ("1000000000000001", 100, 1001),
            ("2000000000000002", 200, 2002),
            ("3000000000000003", 300, 3003),
            ("4000000000000004", 400, 4004),
            ("5000000000000005", 500, 5005),
        ]
        return [generate_xref_record(c, cu, a) for c, cu, a in data[:n]]

    if scenario == "TC-A04":
        # 50 records matching app/data/ASCII/cardxref.txt format
        # The sample file has 36-byte lines (no FILLER); we pad to 50.
        n = record_count if record_count is not None else 50
        sample_path = (
            Path(__file__).resolve().parent.parent
            / "app" / "data" / "ASCII" / "cardxref.txt"
        )
        records: list[bytes] = []
        if sample_path.exists():
            with open(sample_path, "r") as f:
                for line in f:
                    line = line.rstrip("\n\r")
                    if not line:
                        continue
                    # Pad to 50 bytes with spaces (FILLER)
                    rec = line.ljust(RECORD_LEN)[:RECORD_LEN].encode("ascii")
                    records.append(rec)
        else:
            # Generate synthetic data if sample file not available
            for i in range(n):
                card = f"{(i + 1) * 200000000000:016d}"
                records.append(generate_xref_record(card, i + 1, i + 1))
        records.sort()  # KSDS order: ascending primary key
        return records[:n]

    if scenario == "TC-A05":
        # Max boundary values
        return [
            generate_xref_record("9999999999999999", 999999999, 99999999999)
        ]

    if scenario == "TC-B01":
        # Leading zeros in card number
        return [generate_xref_record("0000000000000001", 1, 1)]

    if scenario == "TC-B02":
        # Alphanumeric card number — PIC X(16) allows any characters
        return [generate_xref_record("ABCD123456789012", 12345, 67890)]

    if scenario == "TC-B03":
        # Customer ID with leading zeros
        return [generate_xref_record("5000000000000001", 1, 1)]

    if scenario == "TC-B04":
        # Account ID near 11-digit max
        return [generate_xref_record("6000000000000001", 1, 99999999999)]

    if scenario == "TC-B05":
        # FILLER filled with 'X' characters
        return [
            generate_xref_record("7000000000000001", 1, 1, filler="X" * 14)
        ]

    if scenario == "TC-B06":
        # Zero customer ID
        return [generate_xref_record("8000000000000001", 0, 1)]

    if scenario == "TC-B07":
        # Zero account ID
        return [generate_xref_record("8100000000000001", 1, 0)]

    if scenario == "TC-E04":
        # INITIALIZE defaults: PIC X → spaces, PIC 9 → zeros
        # After INITIALIZE, CBIMPORT moves fields. If source is also zero,
        # the resulting record has zeros for numerics, spaces for alpha/filler.
        return [
            generate_xref_record(
                " " * 16,  # XREF-CARD-NUM = all spaces (INITIALIZE default for PIC X)
                0,         # XREF-CUST-ID = 000000000
                0,         # XREF-ACCT-ID = 00000000000
            )
        ]

    if scenario == "TC-F01":
        # Stress: 10,000 records (overridable)
        n = record_count if record_count is not None else 10000
        rng = random.Random(42)  # deterministic
        card_nums: set[str] = set()
        while len(card_nums) < n:
            card_nums.add(f"{rng.randint(0, 9999999999999999):016d}")
        records = []
        for card in sorted(card_nums):
            cust = rng.randint(0, 999999999)
            acct = rng.randint(0, 99999999999)
            records.append(generate_xref_record(card, cust, acct))
        return records

    if scenario == "TC-F02":
        # Key boundary records — all zeros and all nines
        return sorted([
            generate_xref_record("0000000000000000", 0, 0),
            generate_xref_record("9999999999999999", 999999999, 99999999999),
        ])

    raise ValueError(f"Unknown scenario: {scenario}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate synthetic test data for CBACT03C.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=(
            "Examples:\n"
            "  %(prog)s --scenario TC-A02 --output-dir ./test-data\n"
            "  %(prog)s --scenario TC-F01 --record-count 5000\n"
            "  %(prog)s --scenario TC-A04 --generate-export-file\n"
            "  %(prog)s --list-scenarios\n"
        ),
    )
    parser.add_argument(
        "--scenario",
        help="Test scenario ID (e.g. TC-A01, TC-B05, TC-F01).",
    )
    parser.add_argument(
        "--output-dir",
        default="./test-data",
        help="Output directory (default: ./test-data).",
    )
    parser.add_argument(
        "--record-count",
        type=int,
        default=None,
        help="Override number of records (scenario-dependent default).",
    )
    parser.add_argument(
        "--format",
        choices=["ascii", "ebcdic"],
        default="ascii",
        help="Output encoding (default: ascii).",
    )
    parser.add_argument(
        "--generate-export-file",
        action="store_true",
        help="Also generate a CBIMPORT-compatible 500-byte export file.",
    )
    parser.add_argument(
        "--list-scenarios",
        action="store_true",
        help="Print all available scenarios with descriptions and exit.",
    )

    args = parser.parse_args()

    # --list-scenarios ---------------------------------------------------
    if args.list_scenarios:
        print("Available scenarios:\n")
        for sid, desc in SCENARIOS.items():
            print(f"  {sid:8s}  {desc}")
        print()
        sys.exit(0)

    if not args.scenario:
        parser.error("--scenario is required (or use --list-scenarios)")

    scenario = args.scenario.upper()
    if scenario not in SCENARIOS:
        parser.error(
            f"Unknown scenario '{scenario}'. Use --list-scenarios to see options."
        )

    # Generate records ---------------------------------------------------
    records = _gen_records(scenario, args.record_count)

    # Create output directory --------------------------------------------
    out_dir = Path(args.output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    # Write XREF data file ----------------------------------------------
    suffix = ".ebcdic" if args.format == "ebcdic" else ".dat"
    xref_path = out_dir / f"xref_{scenario.lower()}{suffix}"
    with open(xref_path, "wb") as f:
        for rec in records:
            out_rec = ascii_to_ebcdic(rec) if args.format == "ebcdic" else rec
            f.write(out_rec)
            f.write(b"\n" if args.format == "ascii" else b"")
    print(f"XREF data : {xref_path}  ({len(records)} records)")

    # Write expected output file -----------------------------------------
    exp_path = out_dir / f"expected_output_{scenario.lower()}.txt"
    expected = build_expected_output(records)
    with open(exp_path, "w") as f:
        f.write(expected)
    print(f"Expected  : {exp_path}")

    # Optionally write CBIMPORT-compatible export file -------------------
    if args.generate_export_file:
        export_path = out_dir / f"export_{scenario.lower()}{suffix}"
        with open(export_path, "wb") as f:
            for seq, rec in enumerate(records, start=1):
                # Parse the 50-byte record back into fields
                card_num = rec[0:CARD_NUM_LEN].decode("ascii").rstrip()
                cust_id = int(rec[CARD_NUM_LEN:CARD_NUM_LEN + CUST_ID_LEN])
                acct_id_str = rec[
                    CARD_NUM_LEN + CUST_ID_LEN
                    : CARD_NUM_LEN + CUST_ID_LEN + ACCT_ID_LEN
                ].decode("ascii")
                acct_id = int(acct_id_str)
                exp_rec = generate_export_record(
                    card_num, cust_id, acct_id, seq_num=seq
                )
                if args.format == "ebcdic":
                    exp_rec = ascii_to_ebcdic(exp_rec)
                f.write(exp_rec)
                f.write(b"\n" if args.format == "ascii" else b"")
        print(f"Export    : {export_path}  ({len(records)} records)")

    print("Done.")


if __name__ == "__main__":
    main()
