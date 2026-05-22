# Migration Test Strategy: COBOL-to-Java (CardDemo)

## 1. Overview

This document defines the testing approach for migrating the CardDemo
mainframe credit-card management system from COBOL/CICS/VSAM to Java
microservices. Every test category targets **functional equivalence**
between the legacy COBOL implementation and the new Java implementation.

### Scope

| Layer | COBOL artefact | Java target |
|-------|---------------|-------------|
| Batch programs | `CBACT01C` .. `CBTRN03C` (14 programs) | Spring Batch jobs or equivalent |
| Online transactions | `CO*.cbl` (17 CICS programs) | REST/gRPC microservices |
| Data stores | VSAM KSDS/AIX, DB2 tables | RDBMS (PostgreSQL / MySQL) |
| Reference data | `trantype.txt`, `trancatg.txt`, `discgrp.txt` | DB seed / config |

---

## 2. Golden-File Tests

Golden-file tests capture the **known-correct output** of the legacy system
for a fixed set of inputs so that the Java implementation can be validated
against the same baseline.

### 2.1 Which Programs to Capture Outputs For

| Program | Purpose | Golden file |
|---------|---------|-------------|
| `CBACT01C` | Account file batch processing | `golden-files/acctdata.json` |
| `CBACT02C` | Card data maintenance | `golden-files/carddata.json` |
| `CBACT03C` | Card cross-reference processing | `golden-files/cardxref.json` |
| `CBCUS01C` | Customer file processing | `golden-files/custdata.json` |
| `CBTRN01C` | Daily transaction processing | `golden-files/dailytran.json` |
| `CBTRN02C` | Transaction category balance | `golden-files/tcatbal.json` |
| `CBTRN03C` | Transaction report generation | Report output file |
| `CBSTM03A` | Statement generation (driver) | Statement output |
| `CBSTM03B` | Statement generation (detail) | Statement detail |

### 2.2 Input Data

All inputs come from the fixed-width ASCII files in `app/data/ASCII/`:

| File | Copybook | Record Length | Records |
|------|----------|--------------|---------|
| `acctdata.txt` | `CVACT01Y.cpy` (RECLN 300) | 300 bytes | 50 |
| `carddata.txt` | `CVACT02Y.cpy` (RECLN 150) | 150 bytes | 50 |
| `cardxref.txt` | `CVACT03Y.cpy` (RECLN 50) | 36 bytes* | 50 |
| `custdata.txt` | `CVCUS01Y.cpy` (RECLN 500) | 500 bytes | 50 |
| `dailytran.txt` | `CVTRA06Y.cpy` (RECLN 350) | 350 bytes | 300 |
| `discgrp.txt` | `CVTRA02Y.cpy` (RECLN 50) | 50 bytes | 51 |
| `tcatbal.txt` | `CVTRA01Y.cpy` (RECLN 50) | 51 bytes* | 50 |
| `trancatg.txt` | `CVTRA04Y.cpy` (RECLN 60) | 61 bytes* | 18 |
| `trantype.txt` | `CVTRA03Y.cpy` (RECLN 60) | 61 bytes* | 7 |

\* Minor padding/newline differences between copybook RECLN and actual file
line length are expected and documented in the golden files.

### 2.3 How to Compare

1. **Parse** each ASCII data file using the copybook field layout
   (`test-harness/cobol_parser.py`).
2. **Serialize** to canonical JSON (sorted keys, 2-space indent).
3. **Diff** against the golden reference in `golden-files/<name>.json`.
4. Report field-level mismatches with field name, position, expected vs
   actual value.

The comparison utility (`test-harness/field_comparator.py`) performs:
- Exact string match for alphanumeric (`PIC X`) fields (trailing spaces
  stripped).
- Numeric tolerance match for `PIC S9(n)V99` fields (epsilon = 0.005).
- Date format validation for `PIC X(10)` date fields (YYYY-MM-DD).

---

## 3. Differential Tests

Differential tests run the **same logical operation** through both the COBOL
and Java code paths and compare results.

### 3.1 Architecture

```
 +-----------+       +------------------+       +----------+
 | Test Data | ----> | COBOL (GnuCOBOL) | ----> | Output A |
 |  (ASCII)  |       +------------------+       +----------+
 |           |                                        |
 |           |       +------------------+       +----------+
 |           | ----> | Java (Spring)    | ----> | Output B |
 +-----------+       +------------------+       +----------+
                                                      |
                                               +-------------+
                                               | Diff Engine |
                                               +-------------+
```

### 3.2 How to Run Side-by-Side

1. **COBOL side**: Compile with GnuCOBOL (`cobc -ftab-width=1`), load
   ASCII data into indexed files, execute the batch program, capture
   output files.
2. **Java side**: Run the equivalent Spring Batch job / service method
   with the same input data loaded into the target database.
3. **Export** Java output to the same fixed-width format (or JSON).
4. **Compare** using `test-harness/field_comparator.py`.

### 3.3 Test Matrix

| Scenario | COBOL program | Java equivalent | Input | Comparison point |
|----------|--------------|-----------------|-------|-----------------|
| Account read | `CBACT01C` | AccountService.list() | acctdata.txt | All account fields |
| Card lookup | `CBACT02C` | CardService.findByNum() | carddata.txt | Card + embossed name |
| Xref resolve | `CBACT03C` | XrefService.resolve() | cardxref.txt | Card-to-account mapping |
| Customer read | `CBCUS01C` | CustomerService.list() | custdata.txt | All customer fields |
| Post transaction | `CBTRN01C` | TransactionService.post() | dailytran.txt | Balance updates |
| Category balance | `CBTRN02C` | BalanceService.compute() | tcatbal.txt | Category totals |
| Tran report | `CBTRN03C` | ReportService.generate() | dailytran.txt | Report line items |
| Interest calc | `INTCALC` | InterestService.calculate() | acctdata + discgrp | Interest amounts |

---

## 4. Batch Reconciliation

After each batch run, the following reconciliation checks must pass.

### 4.1 Record Counts

| Check | Description |
|-------|-------------|
| Input = Output | Number of records read must equal records written (for 1:1 jobs) |
| Reject count | Rejected records must be accounted for: input = output + rejects |
| Cross-file consistency | `cardxref.txt` record count <= `carddata.txt` record count |

### 4.2 Numeric Totals

| Check | Fields | Rule |
|-------|--------|------|
| Balance integrity | `ACCT-CURR-BAL` | Sum of all account balances before = sum after +/- posted transactions |
| Transaction total | `TRAN-AMT` in dailytran | Sum of daily transactions = sum of category balance changes |
| Credit limit | `ACCT-CREDIT-LIMIT` | Must not change during transaction posting |
| Cycle credits/debits | `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT` | Must reconcile with posted transactions |

### 4.3 Checksums

- **Hash-based**: SHA-256 of the sorted, serialized JSON output for each
  file. Store in `golden-files/<name>.sha256`.
- **Field-sum**: For numeric fields, store the expected sum as a control
  total in the golden file metadata.

### 4.4 Implementation

See `test-harness/reconciliation.py` which provides:
- `validate_record_counts()` - verifies expected record counts per file
- `validate_numeric_sums()` - sums signed-numeric fields and compares
- `validate_xref_integrity()` - every card in cardxref has a matching
  account in acctdata and a matching customer in custdata
- `validate_checksums()` - SHA-256 comparison against golden baselines

---

## 5. Contract Tests

Contract tests codify the **file formats, record layouts, and interface
contracts** so that the Java implementation produces byte-compatible output.

### 5.1 File Format Contracts

| Contract | Rule |
|----------|------|
| Fixed-width records | Each record is exactly RECLN bytes (no delimiters) |
| Character encoding | ASCII (migrated from EBCDIC; conversion already done) |
| Signed numerics | `PIC S9(n)V99` uses trailing-sign convention (`{` = +0, etc.) |
| Padding | Alphanumeric fields right-padded with spaces |
| Numeric fields | Left-padded with zeros |

### 5.2 Record Layout Contracts

Each copybook defines a contract. The test harness validates:

1. **Field positions**: Start byte and length match the copybook PIC clause.
2. **Data types**: Numeric fields contain only digits (and sign characters).
3. **Date formats**: Date fields match `YYYY-MM-DD` or `YYYYMMDD` patterns.
4. **Key uniqueness**: Primary key fields (e.g., `ACCT-ID`, `CUST-ID`,
   `CARD-NUM`) are unique within each file.

### 5.3 Interface Contracts (Cross-File)

| Producer | Consumer | Contract |
|----------|----------|----------|
| `acctdata.txt` | `cardxref.txt` | Every `XREF-ACCT-ID` exists in `ACCT-ID` |
| `custdata.txt` | `cardxref.txt` | Every `XREF-CUST-ID` exists in `CUST-ID` |
| `carddata.txt` | `cardxref.txt` | Every `XREF-CARD-NUM` exists in `CARD-NUM` |
| `trantype.txt` | `dailytran.txt` | Every `DALYTRAN-TYPE-CD` exists in `TRAN-TYPE` |
| `trancatg.txt` | `dailytran.txt` | Every (`TYPE-CD`, `CAT-CD`) pair in dailytran exists in trancatg |
| `discgrp.txt` | `acctdata.txt` | Every `ACCT-GROUP-ID` has matching disclosure group entries |
| `tcatbal.txt` | `acctdata.txt` | Every `TRANCAT-ACCT-ID` exists in `ACCT-ID` |

### 5.4 Java API Contracts

When Java microservices expose REST APIs, contract tests should verify:
- Response JSON field names match the copybook field names (camelCase
  conversion).
- Numeric precision matches COBOL PIC clause decimal places.
- Date serialization uses ISO-8601 (`YYYY-MM-DD`).
- List endpoints return the same record count as the input file.

---

## 6. Test Execution Workflow

```
1. Load ASCII data files
       |
2. Parse with copybook layouts (cobol_parser.py)
       |
3. Generate / validate golden-file JSON
       |
4. Run COBOL batch programs (GnuCOBOL)
       |
5. Run Java equivalent
       |
6. Compare outputs (field_comparator.py)
       |
7. Run reconciliation checks (reconciliation.py)
       |
8. Report pass/fail per check
```

### Commands

```bash
# Generate golden files from ASCII data
python test-harness/cobol_parser.py --input-dir app/data/ASCII --copybook-dir app/cpy --output-dir golden-files

# Compare two output directories
python test-harness/field_comparator.py --expected golden-files --actual output/java

# Run reconciliation checks
python test-harness/reconciliation.py --data-dir golden-files
```

---

## 7. Risk Areas

| Risk | Mitigation |
|------|-----------|
| EBCDIC sign encoding (`{`, `}`, etc.) | Parser handles trailing-sign convention explicitly |
| COMP/COMP-3 fields in export records | Export copybook uses packed-decimal; parser supports both |
| Implicit decimal points (`V99`) | Parser inserts decimal point at correct position |
| FILLER fields may contain data | Golden files preserve FILLER content for completeness |
| REDEFINES overlays | Parser uses primary definition; REDEFINES documented but not re-parsed |
| GDG versioning | Tests use a single generation (latest); GDG logic tested separately |
