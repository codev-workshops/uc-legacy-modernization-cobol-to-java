# CBACT03C — Detailed Test Scenarios

> **Source program**: `app/cbl/CBACT03C.cbl`
> **Record layout**: `app/cpy/CVACT03Y.cpy` (50 bytes: 16 + 9 + 11 + 14)
> **Import program**: `app/cbl/CBIMPORT.cbl`
> **Export layout**: `app/cpy/CVEXPORT.cpy`

---

## Category A: Normal Processing (Happy Path)

### TC-A01 — Empty XREF file (0 records)

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-A01 |
| **Description**  | Process an empty VSAM KSDS file (defined cluster, zero records loaded). |
| **Preconditions**| XREFFILE VSAM KSDS cluster is defined but contains no records. |
| **Input Data**   | Empty XREF file (0 bytes of record data). |
| **Expected Output** | `START OF EXECUTION OF PROGRAM CBACT03C` followed immediately by `END OF EXECUTION OF PROGRAM CBACT03C`. No record output lines. |
| **Expected RC**  | 0 (normal `GOBACK`) |

### TC-A02 — Single record file

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-A02 |
| **Description**  | Process a file with exactly one XREF record. |
| **Preconditions**| XREFFILE contains 1 valid record. |
| **Input Data**   | Card `4000123456789012`, Cust `000000001`, Acct `00000000001`, Filler 14 spaces. |
| **Expected Output** | START banner, the 50-byte record displayed **twice** (once in `1000-XREFFILE-GET-NEXT` line 96, once in main loop line 78), END banner. |
| **Expected RC**  | 0 |

### TC-A03 — Multiple records (5 records)

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-A03 |
| **Description**  | Process 5 distinct records. Verify KSDS key-order output and duplicate display. |
| **Preconditions**| XREFFILE contains 5 records with distinct card numbers. |
| **Input Data**   | 5 records with card numbers sorted ascending (KSDS order). |
| **Expected Output** | START banner, each of the 5 records displayed twice (10 record DISPLAY lines total), in ascending `XREF-CARD-NUM` order, END banner. |
| **Expected RC**  | 0 |

### TC-A04 — Large file (50 records, sample data)

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-A04 |
| **Description**  | Process the full sample dataset from `app/data/ASCII/cardxref.txt` (50 records). |
| **Preconditions**| XREFFILE loaded with all 50 records from `cardxref.txt`. |
| **Input Data**   | 50 records as defined in `app/data/ASCII/cardxref.txt`. |
| **Expected Output** | START banner, 50 records x 2 displays = 100 DISPLAY lines for records, in KSDS key-ascending order, END banner. |
| **Expected RC**  | 0 |

### TC-A05 — Maximum boundary values

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-A05 |
| **Description**  | Records with maximum-length/max-value fields. |
| **Preconditions**| XREFFILE contains records at field boundaries. |
| **Input Data**   | `XREF-CARD-NUM` = `9999999999999999` (all 16 chars used), `XREF-CUST-ID` = `999999999`, `XREF-ACCT-ID` = `99999999999`. |
| **Expected Output** | START banner, record displayed twice with all fields at max values visible in the 50-byte output, END banner. |
| **Expected RC**  | 0 |

---

## Category B: Data Content Variations (from CBIMPORT analysis)

> Based on analysis of `app/cbl/CBIMPORT.cbl` paragraph `2500-PROCESS-XREF-RECORD` (lines 352–369) and the export layout in `app/cpy/CVEXPORT.cpy` (lines 84–88).

### TC-B01 — Card number with leading zeros

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B01 |
| **Description**  | `XREF-CARD-NUM` with leading zeros. CBIMPORT maps from `EXP-XREF-CARD-NUM PIC X(16)` — alphanumeric, so leading zeros are preserved. |
| **Preconditions**| XREFFILE contains a record with leading-zero card number. |
| **Input Data**   | `XREF-CARD-NUM` = `0000000000000001`. |
| **Expected Output** | Record displayed twice; card number shows full 16 chars including leading zeros. |
| **Expected RC**  | 0 |

### TC-B02 — Alphanumeric card number

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B02 |
| **Description**  | `XREF-CARD-NUM` is `PIC X(16)`, so non-numeric characters are valid. Test with letters in card number. |
| **Preconditions**| XREFFILE contains a record with alphanumeric card number. |
| **Input Data**   | `XREF-CARD-NUM` = `ABCD123456789012`. |
| **Expected Output** | Record displayed twice; card number shows mixed alpha-numeric characters. |
| **Expected RC**  | 0 |

### TC-B03 — Customer ID with leading zeros

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B03 |
| **Description**  | `XREF-CUST-ID` with leading zeros (`PIC 9(09)` display format). CBIMPORT moves from `EXP-XREF-CUST-ID PIC 9(09)` — same format, no conversion issue. |
| **Preconditions**| XREFFILE contains a record with small customer ID. |
| **Input Data**   | `XREF-CUST-ID` = `000000001`. |
| **Expected Output** | Record displayed twice; customer ID field shows `000000001` with leading zeros preserved. |
| **Expected RC**  | 0 |

### TC-B04 — Account ID data type mismatch (COMP-to-display)

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B04 |
| **Description**  | CBIMPORT source is `EXP-XREF-ACCT-ID PIC 9(11) COMP` (binary, `app/cpy/CVEXPORT.cpy` line 87) but target is `XREF-ACCT-ID PIC 9(11)` (display). When COMP value is moved to display format, verify CBACT03C reads and displays the stored value correctly. |
| **Preconditions**| XREFFILE contains a record where XREF-ACCT-ID is near the 11-digit max. |
| **Input Data**   | `XREF-ACCT-ID` = `99999999999` (maximum 11-digit value). |
| **Expected Output** | Record displayed twice; account ID shows `99999999999`. |
| **Expected RC**  | 0 |

### TC-B05 — FILLER with non-space data

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B05 |
| **Description**  | FILLER area contains non-space data (e.g., loaded via IDCAMS REPRO from flat file where FILLER has residual data). Verify CBACT03C displays the full 50-byte raw record including FILLER content. |
| **Preconditions**| XREFFILE contains a record with non-space FILLER bytes. |
| **Input Data**   | `FILLER` = `XXXXXXXXXXXXXX` (14 `X` characters). |
| **Expected Output** | Record displayed twice; full 50-byte record includes the `X` characters in positions 37–50. |
| **Expected RC**  | 0 |

### TC-B06 — Zero customer ID

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B06 |
| **Description**  | Edge case: `XREF-CUST-ID` = `000000000` (zero customer ID). Could occur if CBIMPORT export file had a zero value. |
| **Preconditions**| XREFFILE contains a record with zero customer ID. |
| **Input Data**   | `XREF-CUST-ID` = `000000000`. |
| **Expected Output** | Record displayed twice; customer ID field shows `000000000`. |
| **Expected RC**  | 0 |

### TC-B07 — Zero account ID

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B07 |
| **Description**  | Edge case: `XREF-ACCT-ID` = `00000000000` (zero account ID). |
| **Preconditions**| XREFFILE contains a record with zero account ID. |
| **Input Data**   | `XREF-ACCT-ID` = `00000000000`. |
| **Expected Output** | Record displayed twice; account ID field shows `00000000000`. |
| **Expected RC**  | 0 |

### TC-B08 — Duplicate key rejection by KSDS

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-B08 |
| **Description**  | CBIMPORT writes sequentially to `XREF-OUTPUT`, but if the output is later loaded into VSAM KSDS, duplicate primary keys are rejected by IDCAMS REPRO. CBACT03C always reads a KSDS, so all records are unique by definition. Verify CBACT03C reads all unique records without issue. |
| **Preconditions**| XREFFILE KSDS contains only unique records (duplicates were rejected at load time). |
| **Input Data**   | 3 records with distinct card numbers. |
| **Expected Output** | All 3 records displayed twice each. No errors. |
| **Expected RC**  | 0 |

---

## Category C: File Error Scenarios

### TC-C01 — XREFFILE DD is missing

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-C01 |
| **Description**  | JCL does not include the XREFFILE DD statement. OPEN fails. |
| **Preconditions**| JCL submitted without `//XREFFILE DD` card. |
| **Input Data**   | N/A |
| **Expected Output** | `START OF EXECUTION OF PROGRAM CBACT03C`, `ERROR OPENING XREFFILE`, formatted file status via `9910-DISPLAY-IO-STATUS`, `ABENDING PROGRAM`. |
| **Expected RC**  | ABEND code 999 |

### TC-C02 — XREFFILE points to non-existent dataset

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-C02 |
| **Description**  | XREFFILE DD points to a dataset that does not exist. |
| **Preconditions**| `//XREFFILE DD DSN=NONEXISTENT.DATASET`. |
| **Input Data**   | N/A |
| **Expected Output** | Same as TC-C01: `ERROR OPENING XREFFILE`, formatted status, ABEND 999. |
| **Expected RC**  | ABEND code 999 |

### TC-C03 — Empty VSAM cluster (defined, no records)

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-C03 |
| **Description**  | XREFFILE points to a valid VSAM KSDS cluster that was defined but never had data loaded. |
| **Preconditions**| VSAM cluster defined via IDCAMS DEFINE but REPRO step skipped or loaded 0 records. |
| **Input Data**   | Empty cluster. |
| **Expected Output** | First `READ` returns status `'10'` (EOF). Program sets `END-OF-FILE = 'Y'`, skips display, closes normally. Output: START banner, END banner, no record lines. Equivalent to TC-A01. |
| **Expected RC**  | 0 |

### TC-C04 — XREFFILE is a non-VSAM file

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-C04 |
| **Description**  | XREFFILE DD points to a sequential (PS) file instead of a VSAM KSDS. |
| **Preconditions**| `//XREFFILE DD DSN=some.sequential.file`. |
| **Input Data**   | Sequential file with 50-byte records. |
| **Expected Output** | OPEN may fail or READ may fail depending on environment. Error handling path fires: error message, formatted status, ABEND 999. |
| **Expected RC**  | ABEND code 999 |

### TC-C05 — I/O error during READ

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-C05 |
| **Description**  | VSAM cluster is damaged or has an I/O error mid-read. |
| **Preconditions**| XREFFILE VSAM cluster has been corrupted after the first few records. |
| **Input Data**   | Partially readable VSAM cluster. |
| **Expected Output** | READ returns status other than `'00'` or `'10'` (e.g., `'92'` for logic error). `APPL-RESULT` = 12. Displays `ERROR READING XREFFILE`, formatted status, ABEND 999. Earlier records may have been displayed before the error. |
| **Expected RC**  | ABEND code 999 |

### TC-C06 — Incompatible SHAREOPTIONS lock conflict

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-C06 |
| **Description**  | XREFFILE VSAM cluster is open by another job with incompatible SHAREOPTIONS. |
| **Preconditions**| Another batch job holds an exclusive lock on the XREF VSAM cluster. |
| **Input Data**   | N/A |
| **Expected Output** | OPEN fails with status `'93'` or similar. Error path fires: `ERROR OPENING XREFFILE`, formatted status, ABEND 999. |
| **Expected RC**  | ABEND code 999 |

---

## Category D: IO Status Display Logic (9910-DISPLAY-IO-STATUS)

> Tests validate the formatting logic in paragraph `9910-DISPLAY-IO-STATUS` (lines 161–174).

### TC-D01 — Numeric file status (e.g., '35')

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-D01 |
| **Description**  | File status is a simple numeric value (e.g., `'35'` — file not found). Exercises the `ELSE` branch (line 169–172). |
| **Preconditions**| Trigger an OPEN error that produces numeric file status `'35'`. |
| **Input Data**   | N/A |
| **Expected Output** | `FILE STATUS IS: NNNN0035` — the status is placed into bytes 3–4 of `IO-STATUS-04`, with `'0000'` as prefix. |
| **Expected RC**  | ABEND code 999 |

### TC-D02 — VSAM extended status (STAT1 = '9')

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-D02 |
| **Description**  | File status with `STAT1 = '9'` (VSAM return code). Exercises the `IF` branch (lines 162–168). `STAT2` is interpreted as binary and displayed as a numeric code. |
| **Preconditions**| Trigger a VSAM error that produces file status starting with `'9'` (e.g., `'93'` — VSAM resource not available). |
| **Input Data**   | N/A |
| **Expected Output** | `FILE STATUS IS: NNNN` followed by `IO-STATUS-04` where the first byte is `'9'` and the remaining 3 digits are the binary value of `STAT2`. |
| **Expected RC**  | ABEND code 999 |

### TC-D03 — Non-numeric file status

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-D03 |
| **Description**  | File status contains non-numeric characters (unusual but possible). Exercises the `IO-STATUS NOT NUMERIC` condition (line 162). |
| **Preconditions**| Trigger a condition that produces a non-numeric file status. |
| **Input Data**   | N/A |
| **Expected Output** | Binary-to-numeric conversion path: `STAT2` is interpreted as binary and converted. Display: `FILE STATUS IS: NNNN` followed by the formatted 4-digit code. |
| **Expected RC**  | ABEND code 999 |

---

## Category E: CBIMPORT-Derived Edge Cases

> Tests validate how data quality issues originating in `CBIMPORT` (`app/cbl/CBIMPORT.cbl`) affect CBACT03C.

### TC-E01 — Normal CBIMPORT XREF record

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-E01 |
| **Description**  | CBIMPORT processes a record with type `'X'` (paragraph `2500-PROCESS-XREF-RECORD`, lines 352–369). The resulting XREF file is loaded into VSAM and read by CBACT03C. |
| **Preconditions**| CBIMPORT has successfully processed export records with `EXPORT-REC-TYPE = 'X'` and the output was loaded via IDCAMS REPRO. |
| **Input Data**   | Standard XREF record created by CBIMPORT. |
| **Expected Output** | CBACT03C reads and displays the record twice, normally. |
| **Expected RC**  | 0 |

### TC-E02 — CBIMPORT unknown record type

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-E02 |
| **Description**  | CBIMPORT encounters an unknown record type (`WHEN OTHER` in EVALUATE at lines 272–285, paragraph `2700-PROCESS-UNKNOWN-RECORD` at lines 425–434). This writes to the error file, increments the unknown count, but does NOT write to XREF-OUTPUT. Verify CBACT03C is unaffected. |
| **Preconditions**| CBIMPORT processed an export file containing unknown record types; XREF-OUTPUT was not polluted. |
| **Input Data**   | XREF file contains only valid records (unknown types were diverted to error output). |
| **Expected Output** | CBACT03C processes all records normally. |
| **Expected RC**  | 0 |

### TC-E03 — CBIMPORT abends mid-import (partial file)

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-E03 |
| **Description**  | CBIMPORT abends mid-import (e.g., write error at lines 363–367). Partial `XREF-OUTPUT` file produced. If this partial file is loaded into VSAM, CBACT03C should read whatever records exist and terminate normally at EOF. |
| **Preconditions**| Partial XREF-OUTPUT loaded into VSAM KSDS. |
| **Input Data**   | Subset of records (e.g., 3 out of 10 expected). |
| **Expected Output** | CBACT03C reads and displays whatever records are present, then hits EOF, closes normally. |
| **Expected RC**  | 0 |

### TC-E04 — CBIMPORT INITIALIZE defaults

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-E04 |
| **Description**  | CBIMPORT `INITIALIZE CARD-XREF-RECORD` (line 354) sets all fields to their default values (zeros for `PIC 9` numeric, spaces for `PIC X` alphanumeric) before the `MOVE` statements. Verify CBACT03C can read records where FILLER is all spaces (the INITIALIZE default). |
| **Preconditions**| XREFFILE contains a record produced by CBIMPORT with default FILLER. |
| **Input Data**   | Record with `XREF-CARD-NUM` = spaces/zeros (from INITIALIZE), `XREF-CUST-ID` = `000000000`, `XREF-ACCT-ID` = `00000000000`, `FILLER` = 14 spaces. |
| **Expected Output** | Record displayed twice; FILLER area is all spaces. |
| **Expected RC**  | 0 |

### TC-E05 — COMP-to-display conversion for account ID

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-E05 |
| **Description**  | CBIMPORT moves `EXP-XREF-ACCT-ID` (`PIC 9(11) COMP`, binary) to `XREF-ACCT-ID` (`PIC 9(11)` display). Create export data where the COMP value produces leading zeros when converted to display format. Verify CBACT03C displays correctly. |
| **Preconditions**| XREFFILE contains a record where XREF-ACCT-ID has significant leading zeros after COMP-to-display conversion. |
| **Input Data**   | `XREF-ACCT-ID` = `00000000042` (display representation of binary value 42). |
| **Expected Output** | Record displayed twice; account ID shows `00000000042` with leading zeros. |
| **Expected RC**  | 0 |

---

## Category F: Boundary / Stress

### TC-F01 — Very large XREF file (10,000+ records)

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-F01 |
| **Description**  | Stress test with a very large XREF file (10,000+ records). Verify program completes without resource issues. |
| **Preconditions**| XREFFILE VSAM KSDS loaded with 10,000+ records with unique card numbers. |
| **Input Data**   | 10,000 records with randomized valid data, sorted by `XREF-CARD-NUM`. |
| **Expected Output** | START banner, 10,000 records x 2 displays = 20,000 DISPLAY lines, END banner. Program completes normally. |
| **Expected RC**  | 0 |

### TC-F02 — Key boundary records

| Field            | Value |
|------------------|-------|
| **Test ID**      | TC-F02 |
| **Description**  | Records at KSDS primary key boundaries: all zeros, all nines, all spaces (if loadable). |
| **Preconditions**| XREFFILE contains boundary-key records. |
| **Input Data**   | Record 1: `XREF-CARD-NUM` = `0000000000000000` (all zeros). Record 2: `XREF-CARD-NUM` = `9999999999999999` (all nines). |
| **Expected Output** | Both records displayed twice each, in ascending key order (all-zeros first, all-nines second). |
| **Expected RC**  | 0 |
