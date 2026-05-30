#!/usr/bin/env python3
"""Generate golden JSON expected-output files for CBACT03C test cases.

Reads each .dat file from data/input/, parses 50-byte CVACT03Y records,
and produces structured JSON describing the expected SYSOUT output.

Also creates test-harness/src/main/resources/layouts/xreffile-layout.json.
"""

from __future__ import annotations

import json
import pathlib

RECORD_LEN = 50

SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
INPUT_DIR = SCRIPT_DIR / "data" / "input"
GOLDEN_DIR = SCRIPT_DIR / "data" / "golden"
LAYOUT_DIR = SCRIPT_DIR.parent / "src" / "main" / "resources" / "layouts"

# Mapping of input filename → (test-case ID, description)
TEST_CASE_META: dict[str, tuple[str, str]] = {
    "tc01_single_record.dat":       ("TC-01", "Single valid record"),
    "tc02_multiple_records.dat":    ("TC-02", "Multiple valid records (5)"),
    "tc03_large_dataset.dat":       ("TC-03", "Large dataset (100 records)"),
    "tc04_zero_ids.dat":            ("TC-04", "All-zero numeric fields"),
    "tc05_leading_spaces.dat":      ("TC-05", "XREF-CARD-NUM with leading spaces"),
    "tc06_special_chars.dat":       ("TC-06", "XREF-CARD-NUM with special characters"),
    "tc07_max_cust_id.dat":         ("TC-07", "XREF-CUST-ID at maximum value"),
    "tc08_max_acct_id.dat":         ("TC-08", "XREF-ACCT-ID at maximum value"),
    "tc09_all_spaces_cardnum.dat":  ("TC-09", "XREF-CARD-NUM all spaces"),
    "tc10_nonempty_filler.dat":     ("TC-10", "FILLER contains non-space data"),
    "tc12_typical_cards.dat":       ("TC-12", "Typical card numbers"),
    "tc13_initialized_filler.dat":  ("TC-13", "CBIMPORT INITIALIZE — FILLER spaces"),
    "tc16_unsorted_input.dat":      ("TC-16", "Records not in key order"),
    "tc18_nonunique_aix.dat":       ("TC-18", "Non-unique AIX — same ACCT-ID"),
    "tc19_empty.dat":               ("TC-19", "Empty VSAM file (0 records)"),
}


def parse_record(raw: bytes) -> dict:
    """Parse a single 50-byte record into structured fields."""
    text = raw.decode("ascii")
    card_num = text[0:16]
    cust_id_str = text[16:25]
    acct_id_str = text[25:36]
    filler = text[36:50]

    # Convert numeric fields; handle non-numeric gracefully
    try:
        cust_id = int(cust_id_str)
    except ValueError:
        cust_id = cust_id_str  # non-numeric data (TC-15 scenario)

    try:
        acct_id = int(acct_id_str)
    except ValueError:
        acct_id = acct_id_str

    return {
        "XREF-CARD-NUM": card_num,
        "XREF-CUST-ID": cust_id,
        "XREF-ACCT-ID": acct_id,
        "FILLER": filler,
    }


def process_dat_file(dat_path: pathlib.Path) -> dict:
    """Read a .dat file, parse records, build golden JSON structure."""
    filename = dat_path.name
    tc_id, description = TEST_CASE_META.get(filename, ("UNKNOWN", filename))

    data = dat_path.read_bytes()
    num_records = len(data) // RECORD_LEN if RECORD_LEN > 0 else 0

    records_out = []
    # CBACT03C reads by primary key (XREF-CARD-NUM) order from VSAM KSDS,
    # so we sort parsed records by card number for the golden output.
    parsed = []
    for i in range(num_records):
        raw = data[i * RECORD_LEN : (i + 1) * RECORD_LEN]
        parsed.append((raw, parse_record(raw)))

    # Sort by XREF-CARD-NUM (ascending, ASCII collation) to simulate VSAM key order
    parsed.sort(key=lambda x: x[1]["XREF-CARD-NUM"])

    for idx, (raw, fields) in enumerate(parsed):
        raw_line = raw.decode("ascii")
        records_out.append({
            "recordIndex": idx,
            "rawLine": raw_line,
            "fields": fields,
            "displayCount": 2,
        })

    # totalDisplayLines = 1 (header) + 2*N (each record twice) + 1 (footer)
    total_display_lines = 1 + 2 * num_records + 1

    return {
        "testCaseId": tc_id,
        "description": description,
        "inputFile": filename,
        "expectedOutput": {
            "headerLine": "START OF EXECUTION OF PROGRAM CBACT03C",
            "footerLine": "END OF EXECUTION OF PROGRAM CBACT03C",
            "records": records_out,
            "totalDisplayLines": total_display_lines,
            "abend": False,
        },
    }


def write_xreffile_layout() -> None:
    """Create xreffile-layout.json in the test-harness layouts directory."""
    layout = [
        {
            "name": "XREF-CARD-NUM",
            "offset": 0,
            "length": 16,
            "type": "ALPHANUMERIC",
            "scale": 0,
            "dateField": False,
            "occurrences": 1,
        },
        {
            "name": "XREF-CUST-ID",
            "offset": 16,
            "length": 9,
            "type": "DISPLAY_NUMERIC",
            "scale": 0,
            "dateField": False,
            "occurrences": 1,
        },
        {
            "name": "XREF-ACCT-ID",
            "offset": 25,
            "length": 11,
            "type": "DISPLAY_NUMERIC",
            "scale": 0,
            "dateField": False,
            "occurrences": 1,
        },
        {
            "name": "FILLER",
            "offset": 36,
            "length": 14,
            "type": "FILLER",
            "scale": 0,
            "dateField": False,
            "occurrences": 1,
        },
    ]
    LAYOUT_DIR.mkdir(parents=True, exist_ok=True)
    out_path = LAYOUT_DIR / "xreffile-layout.json"
    with open(out_path, "w") as f:
        json.dump(layout, f, indent=2)
        f.write("\n")
    print(f"  wrote {out_path}")


def generate_all() -> None:
    GOLDEN_DIR.mkdir(parents=True, exist_ok=True)

    dat_files = sorted(INPUT_DIR.glob("*.dat"))
    if not dat_files:
        print("ERROR: No .dat files found in", INPUT_DIR)
        return

    for dat_path in dat_files:
        if dat_path.name not in TEST_CASE_META:
            print(f"  skipping {dat_path.name} (no metadata entry)")
            continue
        golden = process_dat_file(dat_path)
        tc_id = golden["testCaseId"].lower().replace("-", "")
        out_name = f"{tc_id}_golden.json"
        out_path = GOLDEN_DIR / out_name
        with open(out_path, "w") as f:
            json.dump(golden, f, indent=2)
            f.write("\n")
        print(f"  wrote {out_name}")


if __name__ == "__main__":
    print("Generating golden JSON files for CBACT03C …")
    generate_all()

    print("\nGenerating xreffile-layout.json …")
    write_xreffile_layout()

    print("Done.")
