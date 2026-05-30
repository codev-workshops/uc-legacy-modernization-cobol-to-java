# CBACT03C Test Data Generator

Generates synthetic XREF (Card Cross-Reference) data files and expected output for testing the `CBACT03C` batch COBOL program.

## Prerequisites

- Python 3.8+
- No external dependencies required (stdlib only)

## Usage

```bash
# List all available scenarios
python generate_cbact03c_test_data.py --list-scenarios

# Generate data for a specific scenario
python generate_cbact03c_test_data.py --scenario TC-A02

# Override output directory
python generate_cbact03c_test_data.py --scenario TC-A03 --output-dir ./my-data

# Override record count (for scenarios that support it)
python generate_cbact03c_test_data.py --scenario TC-F01 --record-count 5000

# Generate EBCDIC-encoded output
python generate_cbact03c_test_data.py --scenario TC-A02 --format ebcdic

# Also generate a CBIMPORT-compatible export file (500-byte records)
python generate_cbact03c_test_data.py --scenario TC-A04 --generate-export-file
```

## Output Files

For each scenario, the script generates:

| File | Description |
|------|-------------|
| `xref_<scenario>.dat` | 50-byte fixed-length XREF records (ASCII) |
| `xref_<scenario>.ebcdic` | Same records in EBCDIC code page 037 (with `--format ebcdic`) |
| `expected_output_<scenario>.txt` | Expected SYSOUT output from CBACT03C |
| `export_<scenario>.dat` | CBIMPORT-compatible 500-byte export file (with `--generate-export-file`) |

## Record Layout

The XREF record is 50 bytes (copybook `CVACT03Y`, file `app/cpy/CVACT03Y.cpy`):

```
Offset  Length  Field           Picture      Description
──────  ──────  ──────────────  ───────────  ─────────────────────────
 0      16      XREF-CARD-NUM   PIC X(16)    Card number (primary key)
16       9      XREF-CUST-ID    PIC 9(09)    Customer ID (display numeric)
25      11      XREF-ACCT-ID    PIC 9(11)    Account ID (display numeric)
36      14      FILLER          PIC X(14)    Reserved
```

## Expected Output Format

CBACT03C displays each record **twice** per iteration (known bug — `DISPLAY` at line 96 in `1000-XREFFILE-GET-NEXT` + `DISPLAY` at line 78 in main loop):

```
START OF EXECUTION OF PROGRAM CBACT03C
<50-byte record>     ← first display (line 96)
<50-byte record>     ← second display (line 78)
...
END OF EXECUTION OF PROGRAM CBACT03C
```

## Scenario Summary

| ID | Category | Description |
|----|----------|-------------|
| TC-A01 | Happy Path | Empty file (0 records) |
| TC-A02 | Happy Path | Single valid record |
| TC-A03 | Happy Path | 5 records, ascending key order |
| TC-A04 | Happy Path | 50 records (matches sample data) |
| TC-A05 | Happy Path | Max boundary values |
| TC-B01 | Data Variations | Leading-zero card number |
| TC-B02 | Data Variations | Alphanumeric card number |
| TC-B03 | Data Variations | Leading-zero customer ID |
| TC-B04 | Data Variations | Near-max account ID |
| TC-B05 | Data Variations | Non-space FILLER data |
| TC-B06 | Data Variations | Zero customer ID |
| TC-B07 | Data Variations | Zero account ID |
| TC-E04 | CBIMPORT Edge Cases | INITIALIZE defaults |
| TC-F01 | Stress | 10,000 records (randomized) |
| TC-F02 | Boundary | All-zero and all-nine keys |

## Round-Trip Testing via CBIMPORT

Use `--generate-export-file` to create a file that can be fed through `CBIMPORT` (`app/cbl/CBIMPORT.cbl`) before loading into VSAM. This validates the end-to-end data pipeline:

```
Export file → CBIMPORT → XREF-OUTPUT → IDCAMS REPRO → VSAM KSDS → CBACT03C
```
