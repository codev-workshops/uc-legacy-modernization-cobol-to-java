# CardDemo Migration Test Strategy

This document defines the testing approach for migrating the CardDemo COBOL
application to Java. Every test category described below is designed to be
executed automatically so that regressions are caught on every build.

---

## 1. Golden-File Tests

Golden-file tests capture the known-good output produced by the COBOL system and
compare future Java output byte-for-byte (or field-by-field) against that
reference.

### 1.1 Data Files to Capture

| Data File | Copybook | Record Length | Records | Key Fields |
|---|---|---|---|---|
| `acctdata.txt` | `CVACT01Y.cpy` | 300 | 50 | `ACCT-ID (PIC 9(11))` |
| `carddata.txt` | `CVACT02Y.cpy` | 150 | 50 | `CARD-NUM (PIC X(16))` |
| `cardxref.txt` | `CVACT03Y.cpy` | 50 | 50 | `XREF-CARD-NUM (PIC X(16))` |
| `custdata.txt` | `CVCUS01Y.cpy` | 500 | 50 | `CUST-ID (PIC 9(09))` |
| `dailytran.txt` | `CVTRA06Y.cpy` | 350 | 300 | `DALYTRAN-ID (PIC X(16))` |
| `discgrp.txt` | `CVTRA02Y.cpy` | 50 | 51 | `DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD` |
| `tcatbal.txt` | `CVTRA01Y.cpy` | 50 | 50 | `TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD` |
| `trancatg.txt` | `CVTRA04Y.cpy` | 60 | 18 | `TRAN-TYPE-CD + TRAN-CAT-CD` |
| `trantype.txt` | `CVTRA03Y.cpy` | 60 | 7 | `TRAN-TYPE (PIC X(02))` |

### 1.2 Golden-File Generation

For each data file:

1. Parse the fixed-width ASCII record using the PIC clause definitions from the
   corresponding copybook.
2. Emit one JSON object per record with field names, decoded values (including
   zoned-decimal sign-overpunch decoding), and positional metadata.
3. Store the result in `golden-files/<datafile>.golden.json`.

### 1.3 Golden-File Comparison

```
java -cp test-harness/target/classes com.carddemo.harness.Main \
    --cobol-dir=golden-files/ --java-dir=<java-output-dir>/
```

The comparator works field-by-field:

- **Alphanumeric fields**: right-trim trailing spaces, then compare.
- **Signed numeric (zoned decimal)**: decode overpunch characters (`{`/`A`–`I`
  = positive, `}`/`J`–`R` = negative), apply implied decimal, then compare
  within configurable tolerance (`tolerance.properties`).
- **Date fields**: normalize separators (`-`, `/`, `.`) before comparing.
- **FILLER fields**: skipped by default (`ignore.filler=true`).

### 1.4 Inputs for Golden-File Tests

Use the production-representative data already in `app/data/ASCII/`:
- These files contain 50 accounts, 50 cards, 50 customers, 300 daily
  transactions, and supporting reference data.
- For POSTTRAN output, run the COBOL batch once and capture the resulting
  ACCTDATA, TCATBALF, TRANSACT, and DALYREJS files as additional golden
  references.

---

## 2. Differential Tests

Differential tests run both the COBOL and Java implementations with identical
inputs and compare their outputs.

### 2.1 Execution Model

```
┌──────────────┐         ┌──────────────┐
│  Input Data  │────────►│  COBOL Batch  │──► COBOL Output
│  (DALYTRAN,  │         │  (CBTRN02C,   │
│   ACCTDATA,  │         │   CBACT04C…)  │
│   etc.)      │         └──────────────┘
│              │
│              │         ┌──────────────┐
│              │────────►│  Java Batch   │──► Java Output
│              │         │  (equivalent  │
│              │         │   programs)   │
│              │         └──────────────┘
└──────────────┘
                              │
                    ┌─────────▼──────────┐
                    │  RecordComparator   │──► Diff Report
                    │  (field-by-field)   │
                    └────────────────────┘
```

### 2.2 Steps

1. **Prepare canonical inputs** — copy `app/data/ASCII/` files into a temp
   directory.
2. **Run COBOL path** — invoke the batch program (or its UniKix rehost) against
   the temp inputs, capturing all output files.
3. **Run Java path** — invoke the equivalent Java batch program against the same
   temp inputs.
4. **Compare** — use `RecordComparator.compareFixedLength()` for each output
   file pair, using the matching `RecordLayout`.
5. **Assert** — fail the test if any field-level mismatch is found (outside
   configured tolerance).

### 2.3 Programs to Test Differentially

| COBOL Program | JCL Job | Description |
|---|---|---|
| `CBTRN02C` | `POSTTRAN.jcl` | Daily transaction posting |
| `CBACT04C` | `INTCALC.jcl` | Interest / fee calculation |
| `CBTRN03C` | `TRANREPT.jcl` | Transaction reporting |
| `CBEXPORT` | `CBEXPORT.jcl` | Multi-record data export |
| `CBIMPORT` | `CBIMPORT.jcl` | Data import / normalization |

---

## 3. Batch Reconciliation

After each batch run (COBOL or Java), verify aggregate totals and counts to
confirm no records were lost or corrupted.

### 3.1 Record Count Validation

```
input_records = processed_records + rejected_records
```

Applies to every batch program. The Java `BusinessValidator.validateRecordCounts()`
implements this check.

### 3.2 Numeric Field Sum Validation

| Check | Formula |
|---|---|
| Account balance | `ACCT-CURR-BAL(new) = ACCT-CURR-BAL(old) + SUM(posted TRAN-AMT)` |
| Cycle credit | `ACCT-CURR-CYC-CREDIT = SUM(positive TRAN-AMT this cycle)` |
| Cycle debit | `ACCT-CURR-CYC-DEBIT = SUM(negative TRAN-AMT this cycle)` |
| Category balance | `TRAN-CAT-BAL = SUM(TRAN-AMT) for matching (acct, type, category)` |

### 3.3 Checksum Verification

For each output file, compute:
- **Record count** (total lines / records)
- **Numeric hash** — sum of all signed-numeric field values
- **Key hash** — sorted concatenation of all primary key values, SHA-256 hashed

Compare COBOL checksums to Java checksums. Any divergence fails the build.

---

## 4. Contract Tests

Contract tests codify the file formats, record layouts, and interface contracts
so that any structural change is caught immediately.

### 4.1 Record Layout Contracts

Each copybook defines a contract. The `RecordLayout` class encodes:
- Field name, offset, length, PIC type, implied decimal scale
- Total record length

A layout contract test reads a sample record from the golden file and asserts:
- The record is exactly `RECLN` bytes long.
- Each field at its declared offset can be decoded without error.
- Key fields are non-blank and pass format validation.

### 4.2 File Format Contracts

| File | Format | RECFM | LRECL |
|---|---|---|---|
| `ACCTDATA` | Fixed-length, KSDS | FB | 300 |
| `CARDDATA` | Fixed-length, KSDS with AIX | FB | 150 |
| `CARDXREF` | Fixed-length, KSDS | FB | 50 |
| `CUSTDATA` | Fixed-length, KSDS | FB | 500 |
| `DAILYTRAN` | Fixed-length, sequential | FB | 350 |
| `TRANSACT` | Fixed-length, KSDS | FB | 350 |
| `TCATBALF` | Fixed-length, KSDS | FB | 50 |
| `DISCGRP` | Fixed-length, KSDS | FB | 50 |
| `TRANCATG` | Fixed-length, KSDS | FB | 60 |
| `TRANTYPE` | Fixed-length, KSDS | FB | 60 |
| `DALYREJS` | Fixed-length, GDG | FB | 430 |
| `TRANREPT` | Fixed-length, GDG | FB | 133 |
| `EXPORT` | Fixed-length, KSDS | FB | 500 |

### 4.3 Cross-Reference Integrity Contracts

These invariants must hold across files:

1. Every `XREF-CARD-NUM` in `cardxref.txt` must appear as `CARD-NUM` in
   `carddata.txt`.
2. Every `XREF-ACCT-ID` in `cardxref.txt` must appear as `ACCT-ID` in
   `acctdata.txt`.
3. Every `XREF-CUST-ID` in `cardxref.txt` must appear as `CUST-ID` in
   `custdata.txt`.
4. Every `DALYTRAN-CARD-NUM` in `dailytran.txt` must appear as
   `XREF-CARD-NUM` in `cardxref.txt`.
5. Every `TRANCAT-ACCT-ID` in `tcatbal.txt` must appear as `ACCT-ID` in
   `acctdata.txt`.
6. Every `DIS-ACCT-GROUP-ID` in `discgrp.txt` must appear as `ACCT-GROUP-ID`
   in `acctdata.txt`.

### 4.4 Interface Contracts Between Batch Jobs

The POSTTRAN job reads `DALYTRAN` and writes to `TRANSACT`, `ACCTDATA`,
`TCATBALF`, and `DALYREJS`. The Java replacement must consume and produce
files with identical layouts. Contract tests verify:

- Output record length matches the copybook RECLN.
- Output key fields are in the expected sort order.
- Reject file records contain the original transaction plus a 80-byte reason
  trailer (LRECL 430 = 350 + 80).

---

## 5. Test Execution

### 5.1 Running the Full Suite

```bash
cd test-harness
mvn clean test
```

### 5.2 Running Individual Components

```bash
# Parse data files and generate golden JSON
mvn exec:java -Dexec.mainClass=com.carddemo.harness.parser.DataFileParser

# Run field-by-field comparison
mvn exec:java -Dexec.mainClass=com.carddemo.harness.Main \
    -Dexec.args="--cobol-dir=<path> --java-dir=<path>"

# Run reconciliation checks
mvn exec:java -Dexec.mainClass=com.carddemo.harness.reconciliation.ReconciliationRunner
```

### 5.3 CI Integration

The test suite runs as part of `mvn test`. Golden-file JSON and reconciliation
results are generated during the `test` phase. Any mismatch fails the build.
