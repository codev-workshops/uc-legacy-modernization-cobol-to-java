# CBACT03C — Detailed Requirement Specification

## 1. Overview

| Field        | Value                                                                                     |
|--------------|-------------------------------------------------------------------------------------------|
| Program ID   | CBACT03C                                                                                  |
| Type         | Batch COBOL Program                                                                       |
| Purpose      | Read and print every record from the Card Cross-Reference VSAM KSDS file (`AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`) to SYSOUT |
| Source       | `app/cbl/CBACT03C.cbl`                                                                    |

## 2. Input

- **File**: `XREFFILE` (VSAM KSDS, `ORGANIZATION IS INDEXED`, `ACCESS MODE IS SEQUENTIAL`)
- **Record key**: `FD-XREF-CARD-NUM` (`PIC X(16)`, offset 0)
- **Record layout** (copybook `CVACT03Y`, file `app/cpy/CVACT03Y.cpy`):

| Field          | Picture      | Length | Description                        |
|----------------|--------------|--------|------------------------------------|
| XREF-CARD-NUM  | PIC X(16)    | 16     | 16-character card number (primary key) |
| XREF-CUST-ID   | PIC 9(09)    | 9      | 9-digit numeric customer ID        |
| XREF-ACCT-ID   | PIC 9(11)    | 11     | 11-digit numeric account ID        |
| FILLER         | PIC X(14)    | 14     | Reserved / unused                  |
| **Total**      |              | **50** | Fixed-length record                |

- **VSAM cluster definition** (`app/jcl/XREFFILE.jcl`, STEP10):
  - `KEYS(16 0)` — primary key is the first 16 bytes
  - `RECORDSIZE(50 50)` — fixed 50-byte records
  - `INDEXED`
  - `SHAREOPTIONS(2 3)`
- **Alternate index** (`app/jcl/XREFFILE.jcl`, STEP20):
  - On `XREF-ACCT-ID` at offset 25, length 11
  - `NONUNIQUEKEY`
  - Defined as `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX`

## 3. Output

- All output is to SYSOUT (console / `DISPLAY`); no output files are created.
- **Output sequence**:
  1. `'START OF EXECUTION OF PROGRAM CBACT03C'` (line 71)
  2. For each record: the raw 50-byte `CARD-XREF-RECORD` is displayed **twice** per iteration:
     - Once in `1000-XREFFILE-GET-NEXT` (line 96, upon successful read with status `'00'`)
     - Once in the main `PROCEDURE DIVISION` loop (line 78, after confirming `END-OF-FILE = 'N'`)
     - This duplicate display is a **known quirk/bug** in the program.
  3. `'END OF EXECUTION OF PROGRAM CBACT03C'` (line 85)
- **On error**: one of the following messages is displayed, followed by a formatted file status via `9910-DISPLAY-IO-STATUS`, then `ABEND` with code 999:
  - `'ERROR OPENING XREFFILE'` (line 129)
  - `'ERROR READING XREFFILE'` (line 110)
  - `'ERROR CLOSING XREFFILE'` (line 147)

## 4. Processing Logic

### PROCEDURE DIVISION (lines 70–87) — Main Driver

```
DISPLAY 'START OF EXECUTION OF PROGRAM CBACT03C'
PERFORM 0000-XREFFILE-OPEN
PERFORM UNTIL END-OF-FILE = 'Y'
    IF END-OF-FILE = 'N'
        PERFORM 1000-XREFFILE-GET-NEXT
        IF END-OF-FILE = 'N'
            DISPLAY CARD-XREF-RECORD        ← second display per record
        END-IF
    END-IF
END-PERFORM
PERFORM 9000-XREFFILE-CLOSE
DISPLAY 'END OF EXECUTION OF PROGRAM CBACT03C'
GOBACK
```

### 0000-XREFFILE-OPEN (lines 118–134)

Opens `XREFFILE-FILE` for `INPUT`.

1. Sets `APPL-RESULT` to 8 (pre-load a non-zero value).
2. `OPEN INPUT XREFFILE-FILE`.
3. If `XREFFILE-STATUS = '00'` → sets `APPL-RESULT` to 0 (success).
4. Otherwise → sets `APPL-RESULT` to 12 (error).
5. If not `APPL-AOK`:
   - Displays `'ERROR OPENING XREFFILE'`.
   - Copies `XREFFILE-STATUS` to `IO-STATUS`.
   - Performs `9910-DISPLAY-IO-STATUS`.
   - Performs `9999-ABEND-PROGRAM`.

### 1000-XREFFILE-GET-NEXT (lines 92–116)

Reads the next record sequentially into `CARD-XREF-RECORD`.

1. `READ XREFFILE-FILE INTO CARD-XREF-RECORD`.
2. Status handling:
   - `'00'` → `MOVE 0 TO APPL-RESULT`, then `DISPLAY CARD-XREF-RECORD` (**first display**).
   - `'10'` → `MOVE 16 TO APPL-RESULT` (EOF indicator).
   - Other → `MOVE 12 TO APPL-RESULT` (error indicator).
3. Result evaluation:
   - `APPL-AOK` (0) → `CONTINUE` (return to caller).
   - `APPL-EOF` (16) → `MOVE 'Y' TO END-OF-FILE`.
   - Otherwise → displays `'ERROR READING XREFFILE'`, shows IO status, abends.

### 9000-XREFFILE-CLOSE (lines 136–152)

Closes the file.

1. `ADD 8 TO ZERO GIVING APPL-RESULT` — equivalent to `MOVE 8 TO APPL-RESULT`.
2. `CLOSE XREFFILE-FILE`.
3. If `XREFFILE-STATUS = '00'` → `SUBTRACT APPL-RESULT FROM APPL-RESULT` — equivalent to `MOVE 0 TO APPL-RESULT`.
4. Otherwise → `ADD 12 TO ZERO GIVING APPL-RESULT` (error).
5. If not `APPL-AOK`:
   - Displays `'ERROR CLOSING XREFFILE'`.
   - Copies `XREFFILE-STATUS` to `IO-STATUS`.
   - Performs `9910-DISPLAY-IO-STATUS`.
   - Performs `9999-ABEND-PROGRAM`.

### 9910-DISPLAY-IO-STATUS (lines 161–174)

Formats file status for display. Two branches:

- **(a)** If `IO-STATUS` is not numeric **OR** `IO-STAT1 = '9'` (VSAM-extended status):
  - Interprets `IO-STAT2` as binary — moves it into `TWO-BYTES-RIGHT`, converts `TWO-BYTES-BINARY` to `IO-STATUS-0403` (3-digit numeric).
  - Displays `'FILE STATUS IS: NNNN' IO-STATUS-04`.
- **(b)** Otherwise (simple numeric status):
  - Moves `'0000'` to `IO-STATUS-04`.
  - Overlays bytes 3–4 with `IO-STATUS`.
  - Displays `'FILE STATUS IS: NNNN' IO-STATUS-04`.

### 9999-ABEND-PROGRAM (lines 154–158)

1. Displays `'ABENDING PROGRAM'`.
2. `MOVE 0 TO TIMING`.
3. `MOVE 999 TO ABCODE`.
4. `CALL 'CEE3ABD' USING ABCODE, TIMING` — Language Environment abend.

## 5. Error Handling

- File-status-based error detection at `OPEN`, `READ`, and `CLOSE` operations.
- All errors abend via `CEE3ABD` with code **999**.
- IO status is formatted and displayed via `9910-DISPLAY-IO-STATUS` before every abend.

## 6. Return Codes

| Condition          | Return Code                   |
|--------------------|-------------------------------|
| Normal completion  | `GOBACK` (RC = 0)             |
| Abnormal           | `ABEND` code **999**          |

## 7. JCL Reference

Reference JCL: `app/jcl/READXREF.jcl`

```jcl
//STEP05 EXEC PGM=CBACT03C
//STEPLIB  DD DISP=SHR,DSN=AWS.M2.CARDDEMO.LOADLIB
//XREFFILE DD DISP=SHR,DSN=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS
//SYSOUT   DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
```

## 8. Data Dependencies

### Population

The XREF file is populated by:

1. **`app/jcl/XREFFILE.jcl`** (STEP15) — `IDCAMS REPRO` from flat file `AWS.M2.CARDDEMO.CARDXREF.PS`.
2. **`app/cbl/CBIMPORT.cbl`** (paragraph `2500-PROCESS-XREF-RECORD`, lines 352–369) — branch migration import. Reads export records with type `'X'`, initializes `CARD-XREF-RECORD`, maps `EXP-XREF-CARD-NUM`, `EXP-XREF-CUST-ID`, and `EXP-XREF-ACCT-ID`, then writes to sequential `XREF-OUTPUT`.

### Consumers

The XREF file is read (not written) by 15+ other programs including: `CBACT04C`, `CBTRN01C`, `CBTRN02C`, `CBTRN03C`, `CBSTM03A`, `CBEXPORT`, and multiple CICS online programs.

## 9. Known Issues

- **Duplicate DISPLAY**: each successfully read record is printed **twice** per iteration — once in `1000-XREFFILE-GET-NEXT` (line 96) and once in the main loop (line 78). This is a known bug in the original program.
