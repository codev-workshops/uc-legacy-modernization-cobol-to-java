# CBACT03C — Test Cases

> **Record layout (CVACT03Y):** 50 bytes fixed  
> `XREF-CARD-NUM X(16) | XREF-CUST-ID 9(09) | XREF-ACCT-ID 9(11) | FILLER X(14)`

## Legend

| Column              | Description                                                      |
| ------------------- | ---------------------------------------------------------------- |
| **ID**              | Unique test case identifier                                      |
| **Description**     | What is being tested                                             |
| **Input**           | Characteristics of the `.dat` input data                         |
| **Expected Output** | What CBACT03C should produce on SYSOUT                           |
| **Pass/Fail**       | Criteria for determining pass or fail                            |

---

## Category A: Happy Path

### TC-01 — Single Valid Record

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-01                                                              |
| **Description**   | One valid 50-byte record with typical field values                 |
| **Input**         | 1 record: card `4111111111111111`, cust `000123456`, acct `00012345678`, filler spaces |
| **Expected Output** | `START OF EXECUTION OF PROGRAM CBACT03C` + 2x DISPLAY of the record + `END OF EXECUTION OF PROGRAM CBACT03C` |
| **Pass/Fail**     | PASS if output contains exactly 4 lines: header, 2x record, footer. Record lines are identical 50-byte strings. |

### TC-02 — Multiple Valid Records (5)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-02                                                              |
| **Description**   | Five valid records with distinct card numbers                      |
| **Input**         | 5 records with different XREF-CARD-NUM values, valid numeric fields |
| **Expected Output** | Header + 10 DISPLAY lines (2 per record) + footer = 12 lines total. Records appear in primary key (XREF-CARD-NUM ascending) order. |
| **Pass/Fail**     | PASS if all 5 records displayed twice each, in ascending card-number order. |

### TC-03 — Large Dataset (100+ Records)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-03                                                              |
| **Description**   | 100 records to verify no truncation or premature termination       |
| **Input**         | 100 records with sequentially generated card numbers               |
| **Expected Output** | Header + 200 DISPLAY lines + footer = 202 lines total             |
| **Pass/Fail**     | PASS if exactly 202 output lines, all 100 records displayed twice, no truncation. |

### TC-04 — All-Zero Numeric Fields

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-04                                                              |
| **Description**   | Records with XREF-CUST-ID = `000000000` and XREF-ACCT-ID = `00000000000` |
| **Input**         | 2 records with all-zero numeric fields, distinct card numbers       |
| **Expected Output** | Each record DISPLAY shows `000000000` for CUST-ID and `00000000000` for ACCT-ID |
| **Pass/Fail**     | PASS if zero-padded numeric fields appear correctly in DISPLAY output. |

---

## Category B: Boundary/Edge Cases (CVACT03Y Field Definitions)

### TC-05 — XREF-CARD-NUM with Leading Spaces

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-05                                                              |
| **Description**   | Card number with leading spaces (valid PIC X)                      |
| **Input**         | 1 record: card `"   1234567890123"` (3 leading spaces)             |
| **Expected Output** | DISPLAY shows the raw 50-byte record with leading spaces preserved |
| **Pass/Fail**     | PASS if leading spaces in card number appear in output.            |

### TC-06 — XREF-CARD-NUM with Special Characters

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-06                                                              |
| **Description**   | Card number containing special characters (valid PIC X(16))        |
| **Input**         | 1 record: card `"@#$%&*()-+=<>{}"` (16 special chars)             |
| **Expected Output** | DISPLAY shows the raw record with special chars preserved          |
| **Pass/Fail**     | PASS if special characters in card number appear verbatim.         |

### TC-07 — XREF-CUST-ID at Maximum Value

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-07                                                              |
| **Description**   | XREF-CUST-ID = 999999999 (maximum 9-digit value)                  |
| **Input**         | 1 record with CUST-ID `999999999`                                  |
| **Expected Output** | DISPLAY shows `999999999` in the CUST-ID field position           |
| **Pass/Fail**     | PASS if maximum CUST-ID value displayed correctly.                 |

### TC-08 — XREF-ACCT-ID at Maximum Value

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-08                                                              |
| **Description**   | XREF-ACCT-ID = 99999999999 (maximum 11-digit value)               |
| **Input**         | 1 record with ACCT-ID `99999999999`                                |
| **Expected Output** | DISPLAY shows `99999999999` in the ACCT-ID field position         |
| **Pass/Fail**     | PASS if maximum ACCT-ID value displayed correctly.                 |

### TC-09 — XREF-CARD-NUM All Spaces

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-09                                                              |
| **Description**   | Card number is 16 spaces — valid PIC X but lowest VSAM key value  |
| **Input**         | 1 record: card `"                "` (16 spaces)                    |
| **Expected Output** | DISPLAY shows 50-byte record; first 16 bytes are spaces            |
| **Pass/Fail**     | PASS if all-space card number displayed. Note: single record at lowest key value. |

### TC-10 — FILLER Contains Non-Space Data

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-10                                                              |
| **Description**   | FILLER field (bytes 36-49) contains non-space characters           |
| **Input**         | 1 record with FILLER = `"TESTFILLER1234"`                         |
| **Expected Output** | DISPLAY shows full 50-byte record including non-space FILLER data  |
| **Pass/Fail**     | PASS if FILLER content appears in raw record dump.                 |

### TC-11 — Minimum Dataset (Exactly 1 Record)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-11                                                              |
| **Description**   | Equivalent to TC-01; confirms boundary of minimum valid dataset    |
| **Input**         | 1 valid record                                                     |
| **Expected Output** | Header + 2x DISPLAY + footer = 4 lines                            |
| **Pass/Fail**     | PASS if exactly 4 output lines.                                    |

### TC-12 — Typical Card Numbers (Mixed Alpha-Numeric)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-12                                                              |
| **Description**   | Realistic card numbers (Visa 4111…, MC 5500…, Amex 3782…)         |
| **Input**         | 3 records with typical card number patterns                        |
| **Expected Output** | All records displayed twice, in ascending XREF-CARD-NUM order      |
| **Pass/Fail**     | PASS if all card number patterns displayed correctly.              |

---

## Category C: Edge Cases from CBIMPORT Writer (2500-PROCESS-XREF-RECORD)

### TC-13 — CBIMPORT INITIALIZE Behaviour (FILLER = Spaces)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-13                                                              |
| **Description**   | Records as CBIMPORT would write them — FILLER initialised to spaces |
| **Input**         | 3 records with FILLER = 14 spaces (0x20 in ASCII)                  |
| **Expected Output** | DISPLAY shows spaces in FILLER position (bytes 36-49)              |
| **Pass/Fail**     | PASS if FILLER bytes are all spaces in output.                     |

### TC-14 — COMP-to-DISPLAY Conversion for ACCT-ID

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-14                                                              |
| **Description**   | Verify CBIMPORT's COMP→DISPLAY mapping for XREF-ACCT-ID is reflected correctly |
| **Input**         | 1 record with ACCT-ID = `12345678901` (known value for validation) |
| **Expected Output** | DISPLAY shows `12345678901` at byte positions 25-35                |
| **Pass/Fail**     | PASS if ACCT-ID value matches expected DISPLAY-format representation. |

### TC-15 — Non-Numeric Data in XREF-CUST-ID

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-15                                                              |
| **Description**   | CBIMPORT writes without validation; garbage in CUST-ID field       |
| **Input**         | 1 record with CUST-ID bytes = `"ABCDEFGHI"` (non-numeric in PIC 9 field) |
| **Expected Output** | CBACT03C should still DISPLAY the raw record (no field validation in reader) |
| **Pass/Fail**     | PASS if record displayed despite non-numeric data in numeric field. |

---

## Category D: Edge Cases from XREFFILE.jcl Loader

### TC-16 — Flat File Records Not in Key Order

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-16                                                              |
| **Description**   | Input records deliberately unsorted; VSAM returns in key order     |
| **Input**         | 5 records with card numbers in descending order in the `.dat` file |
| **Expected Output** | CBACT03C output shows records in ascending XREF-CARD-NUM order (VSAM sorts) |
| **Pass/Fail**     | PASS if output records are in ascending primary key order regardless of input order. |

### TC-17 — Duplicate Primary Keys in Flat File

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-17                                                              |
| **Description**   | Data quality scenario — IDCAMS REPRO would reject duplicates       |
| **Input**         | N/A — CBACT03C never sees these records (REPRO fails)              |
| **Expected Output** | N/A — this is a data-load-time failure, not a CBACT03C test        |
| **Pass/Fail**     | DOCUMENT ONLY — no `.dat` file generated. Verify REPRO rejects duplicate keys. |

### TC-18 — Non-Unique AIX (Same ACCT-ID, Different CARD-NUM)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-18                                                              |
| **Description**   | Multiple records share the same XREF-ACCT-ID but have unique card numbers |
| **Input**         | 3 records with same ACCT-ID `00012345678`, different card numbers   |
| **Expected Output** | All 3 records displayed (2x each), in ascending XREF-CARD-NUM order |
| **Pass/Fail**     | PASS if all records appear; AIX non-uniqueness doesn't affect primary key read. |

---

## Category E: Failure/Error Cases

### TC-19 — Empty VSAM File (0 Records)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-19                                                              |
| **Description**   | VSAM file exists but contains zero records                         |
| **Input**         | Empty `.dat` file (0 bytes)                                        |
| **Expected Output** | Header + footer only = 2 lines. No record DISPLAY lines. First READ returns status `'10'` (EOF). |
| **Pass/Fail**     | PASS if exactly 2 output lines (header + footer), no abend.       |

### TC-20 — File Not Found / Cannot Open

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-20                                                              |
| **Description**   | XREFFILE DD is missing or file does not exist                      |
| **Input**         | No input file provided                                             |
| **Expected Output** | `START OF EXECUTION…` + `ERROR OPENING XREFFILE` + `FILE STATUS IS: NNNN…` + `ABENDING PROGRAM` + abend 999 |
| **Pass/Fail**     | PASS if program abends with code 999 after displaying open error.  |

### TC-21 — I/O Error During Read

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-21                                                              |
| **Description**   | Read returns status other than `'00'` or `'10'`                    |
| **Input**         | Simulated I/O error after N successful reads                       |
| **Expected Output** | N records displayed (2x each), then `ERROR READING XREFFILE` + IO status + abend 999 |
| **Pass/Fail**     | PASS if error message and abend occur after the failed read.       |

### TC-22 — I/O Error During Close

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-22                                                              |
| **Description**   | Close returns status other than `'00'`                             |
| **Input**         | Simulated close error after successful read of all records         |
| **Expected Output** | All records displayed, then `ERROR CLOSING XREFFILE` + IO status + abend 999 |
| **Pass/Fail**     | PASS if close error message and abend occur after all records read. |

### TC-23 — Non-Numeric File Status (IO-STAT1 = '9')

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-23                                                              |
| **Description**   | Triggers the binary status code conversion path in 9910-DISPLAY-IO-STATUS |
| **Input**         | Simulated error with IO-STAT1 = '9', binary IO-STAT2              |
| **Expected Output** | `FILE STATUS IS: NNNN9NNNN` where last 3 digits are binary conversion of IO-STAT2 |
| **Pass/Fail**     | PASS if binary-to-numeric conversion in status display is correct. |

### TC-24 — File Corruption Mid-Read

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-24                                                              |
| **Description**   | I/O error triggered partway through reading (after some records succeed) |
| **Input**         | Simulated: 3 successful reads then error on 4th read               |
| **Expected Output** | 3 records (2x each = 6 DISPLAY lines), then error message + abend |
| **Pass/Fail**     | PASS if first 3 records displayed correctly, then error path triggered. |

---

## Category F: Output Format Verification

### TC-25 — Record DISPLAY Line Length

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-25                                                              |
| **Description**   | Verify each DISPLAY line is exactly 50 characters (raw CARD-XREF-RECORD) |
| **Input**         | 3 records with known content                                       |
| **Expected Output** | Each record DISPLAY line is exactly 50 characters long             |
| **Pass/Fail**     | PASS if all record lines are 50 characters. Header/footer lines excluded from length check. |

### TC-26 — Duplicate-Display Verification

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **ID**            | TC-26                                                              |
| **Description**   | Confirm each successfully read record appears exactly twice in SYSOUT |
| **Input**         | 5 records with distinct content                                     |
| **Expected Output** | Each record's 50-byte string appears exactly 2 times in output     |
| **Pass/Fail**     | PASS if every unique record line has count == 2. Total record DISPLAY lines = 2 * N. |
