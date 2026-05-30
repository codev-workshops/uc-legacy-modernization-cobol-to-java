# CBACT03C — Requirements Specification

## 1. Program Overview

**CBACT03C** is a batch COBOL diagnostic utility within the CardDemo application.
It sequentially reads every record from the **CARDXREF VSAM KSDS** file and
DISPLAYs each record to SYSOUT. The program performs no data transformation,
filtering, or output file writing — it is a pure read-and-dump diagnostic tool.

- **Source**: `app/cbl/CBACT03C.cbl` (178 lines)
- **Copybook**: `app/cpy/CVACT03Y.cpy` — defines the `CARD-XREF-RECORD` layout
- **Program ID**: `CBACT03C`
- **Author**: AWS

---

## 2. Input Specification

### 2.1 File Definition

| Attribute          | Value                                      |
| ------------------ | ------------------------------------------ |
| Logical name       | `XREFFILE-FILE`                            |
| DD name            | `XREFFILE`                                 |
| Organisation       | INDEXED (VSAM KSDS)                        |
| Access mode        | SEQUENTIAL                                 |
| Primary key        | `FD-XREF-CARD-NUM` — bytes 0-15, PIC X(16)|
| File status        | `XREFFILE-STATUS` (2 bytes)                |

### 2.2 FD Record Layout

```
FD  XREFFILE-FILE.
01  FD-XREFFILE-REC.
    05 FD-XREF-CARD-NUM    PIC X(16).
    05 FD-XREF-DATA        PIC X(34).
```

### 2.3 Working-Storage Record Layout (CVACT03Y copybook)

Records are read `INTO CARD-XREF-RECORD`:

| Field            | PIC       | Offset | Length | Type             |
| ---------------- | --------- | ------ | ------ | ---------------- |
| XREF-CARD-NUM    | X(16)     | 0      | 16     | ALPHANUMERIC     |
| XREF-CUST-ID     | 9(09)     | 16     | 9      | DISPLAY_NUMERIC  |
| XREF-ACCT-ID     | 9(11)     | 25     | 11     | DISPLAY_NUMERIC  |
| FILLER           | X(14)     | 36     | 14     | FILLER           |

**Total record length: 50 bytes fixed.**

---

## 3. Output Specification

CBACT03C writes **only** to SYSOUT (DISPLAY). There are no output files.

### 3.1 SYSOUT Format

| Order | Content                                        | Source line |
| ----- | ---------------------------------------------- | ----------- |
| 1     | `START OF EXECUTION OF PROGRAM CBACT03C`       | Line 71     |
| 2..N  | Raw `CARD-XREF-RECORD` (50 bytes per DISPLAY)  | Lines 78,96 |
| Last  | `END OF EXECUTION OF PROGRAM CBACT03C`         | Line 85     |

### 3.2 Duplicate-Display Behaviour (Known Bug/Feature)

Each successfully read record is DISPLAYed **twice**:

1. **Line 96** — inside `1000-XREFFILE-GET-NEXT`, immediately after a successful
   read (status `'00'`).
2. **Line 78** — in the main `PERFORM UNTIL` loop, after control returns from
   `1000-XREFFILE-GET-NEXT` and `END-OF-FILE` is still `'N'`.

Both DISPLAY statements reference the same `CARD-XREF-RECORD` variable, so the
content is identical. Total DISPLAY lines per run:

```
totalDisplayLines = 1 (header) + 2 * N (records) + 1 (footer)
```

where N = number of records successfully read.

---

## 4. Processing Logic

### 4.1 Main Flow

```
PROCEDURE DIVISION.
    DISPLAY 'START OF EXECUTION OF PROGRAM CBACT03C'
    PERFORM 0000-XREFFILE-OPEN
    PERFORM UNTIL END-OF-FILE = 'Y'
        IF END-OF-FILE = 'N'
            PERFORM 1000-XREFFILE-GET-NEXT
            IF END-OF-FILE = 'N'
                DISPLAY CARD-XREF-RECORD        ← 2nd display
            END-IF
        END-IF
    END-PERFORM
    PERFORM 9000-XREFFILE-CLOSE
    DISPLAY 'END OF EXECUTION OF PROGRAM CBACT03C'
    GOBACK.
```

### 4.2 Open — 0000-XREFFILE-OPEN

1. `MOVE 8 TO APPL-RESULT`
2. `OPEN INPUT XREFFILE-FILE`
3. If `XREFFILE-STATUS = '00'` → `MOVE 0 TO APPL-RESULT`; else `MOVE 12 TO APPL-RESULT`
4. If NOT `APPL-AOK`:
   - DISPLAY `'ERROR OPENING XREFFILE'`
   - Move status to `IO-STATUS`, perform `9910-DISPLAY-IO-STATUS`
   - Perform `9999-ABEND-PROGRAM`

### 4.3 Read — 1000-XREFFILE-GET-NEXT

1. `READ XREFFILE-FILE INTO CARD-XREF-RECORD`
2. Status routing:
   - `'00'` → `APPL-RESULT=0`, **DISPLAY CARD-XREF-RECORD** (1st display)
   - `'10'` → `APPL-RESULT=16` (EOF)
   - Other  → `APPL-RESULT=12` (error)
3. Post-read evaluation:
   - `APPL-AOK` (0) → CONTINUE
   - `APPL-EOF` (16) → `MOVE 'Y' TO END-OF-FILE`
   - Otherwise → DISPLAY `'ERROR READING XREFFILE'`, display IO status, abend

### 4.4 Close — 9000-XREFFILE-CLOSE

1. `ADD 8 TO ZERO GIVING APPL-RESULT`
2. `CLOSE XREFFILE-FILE`
3. If `XREFFILE-STATUS = '00'` → `SUBTRACT APPL-RESULT FROM APPL-RESULT` (=0)
4. Else → `ADD 12 TO ZERO GIVING APPL-RESULT`
5. If NOT `APPL-AOK`:
   - DISPLAY `'ERROR CLOSING XREFFILE'`
   - Move status to `IO-STATUS`, perform `9910-DISPLAY-IO-STATUS`
   - Perform `9999-ABEND-PROGRAM`

---

## 5. Error Handling

### 5.1 IO Status Display — 9910-DISPLAY-IO-STATUS

Handles two cases:

**Case 1 — Numeric status, IO-STAT1 != '9':**
```
MOVE '0000' TO IO-STATUS-04
MOVE IO-STATUS TO IO-STATUS-04(3:2)
DISPLAY 'FILE STATUS IS: NNNN' IO-STATUS-04
```
Output example: `FILE STATUS IS: NNNN00000035`

**Case 2 — Non-numeric status or IO-STAT1 = '9' (binary status):**
```
MOVE IO-STAT1 TO IO-STATUS-04(1:1)
MOVE 0 TO TWO-BYTES-BINARY
MOVE IO-STAT2 TO TWO-BYTES-RIGHT
MOVE TWO-BYTES-BINARY TO IO-STATUS-0403
DISPLAY 'FILE STATUS IS: NNNN' IO-STATUS-04
```
This converts the binary second byte of the file status into a 3-digit
numeric representation. Output example: `FILE STATUS IS: NNNN90034`

### 5.2 Abend — 9999-ABEND-PROGRAM

```
DISPLAY 'ABENDING PROGRAM'
MOVE 0 TO TIMING
MOVE 999 TO ABCODE
CALL 'CEE3ABD' USING ABCODE, TIMING.
```

Abend code: **999**. Timing: **0** (immediate).

---

## 6. VSAM Constraints (from XREFFILE.jcl)

| Property        | Value                                            |
| --------------- | ------------------------------------------------ |
| Cluster name    | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`           |
| Primary key     | `KEYS(16 0)` — 16 bytes starting at offset 0    |
| Record size     | `RECORDSIZE(50 50)` — fixed 50 bytes             |
| Share options   | `SHAREOPTIONS(2 3)`                              |
| Organisation    | INDEXED (KSDS)                                   |
| AIX name        | `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX`             |
| AIX key         | `KEYS(11,25)` — XREF-ACCT-ID, 11 bytes at offset 25 |
| AIX uniqueness  | `NONUNIQUEKEY`                                   |
| AIX path        | `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH`        |

---

## 7. Data Writers

### 7.1 XREFFILE.jcl — Bulk Load

- Uses `IDCAMS REPRO` to load records from flat file
  (`AWS.M2.CARDDEMO.CARDXREF.PS`) into the VSAM KSDS.
- No field-level validation — raw byte copy.

### 7.2 CBIMPORT.cbl — Paragraph 2500-PROCESS-XREF-RECORD

- `INITIALIZE CARD-XREF-RECORD` (sets FILLER to spaces, numerics to zeros)
- Maps export fields:
  - `EXP-XREF-CARD-NUM → XREF-CARD-NUM`
  - `EXP-XREF-CUST-ID → XREF-CUST-ID`
  - `EXP-XREF-ACCT-ID → XREF-ACCT-ID`
- Writes `CARD-XREF-RECORD` — no field validation performed.
