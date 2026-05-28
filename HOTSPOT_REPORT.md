# Hotspot Report — CardDemo COBOL Estate

> Programs ranked by complexity and modernization priority.
> Metrics collected via static analysis of all `.cbl` / `.CBL` files.

---

## 1. Top 10 Programs by Composite Complexity Score

Each program is scored across five dimensions. The composite score is a weighted sum:
- **Lines of Code (LOC)** — weight 1
- **Copybooks Referenced** — weight 5
- **I/O Operations** (SELECT, READ, WRITE, REWRITE, DELETE, OPEN, CLOSE, EXEC CICS file ops) — weight 3
- **Business Logic Density** (IF + EVALUATE count, max nesting depth as multiplier) — weight 2
- **Inter-Program Dependencies** (CALL, XCTL, MQ, DL/I calls) — weight 4

| Rank | Program | LOC | Copybooks | I/O Ops | IF+EVAL | Max Nest | CALL/XCTL | Composite Score | Classification |
|------|---------|-----|-----------|---------|---------|----------|-----------|-----------------|----------------|
| 1 | **COACTUPC** | 4,236 | 58 | 18 | 184 | 4 | 1 (XCTL) | **6,186** | Online (CICS) |
| 2 | **COTRTLIC** | 2,098 | 12 | 29 | 115 | 5 | 2 (XCTL) | **3,543** | Online (CICS/DB2) |
| 3 | **COTRTUPC** | 1,702 | 15 | 31 | 134 | 3 | 1 (XCTL) | **2,773** | Online (CICS/DB2) |
| 4 | **COCRDUPC** | 1,560 | 16 | 5 | 167 | 3 | 1 (XCTL) | **2,564** | Online (CICS) |
| 5 | **COCRDLIC** | 1,459 | 14 | 6 | 149 | 4 | 3 (XCTL) | **2,479** | Online (CICS) |
| 6 | **COPAUA0C** | 1,026 | 17 | 14 | 62 | 3 | 4 (MQ) | **1,619** | Online (CICS/IMS/MQ) |
| 7 | **COPAUS0C** | 1,032 | 15 | 1 | 36 | 4 | 0 | **1,388** | Online (CICS/IMS/BMS) |
| 8 | **CBSTM03A** | 924 | 5 | 119 | 20 | 3 | 14 (CALL) | **1,452** | Batch |
| 9 | **COACTVWC** | 941 | 16 | 4 | 70 | 2 | 1 (XCTL) | **1,277** | Online (CICS) |
| 10 | **COCRDSLC** | 887 | 16 | 4 | 80 | 2 | 1 (XCTL) | **1,265** | Online (CICS) |

### Honorable Mentions (11–15)

| Rank | Program | LOC | Copybooks | I/O Ops | IF+EVAL | Classification |
|------|---------|-----|-----------|---------|---------|----------------|
| 11 | COTRN02C | 783 | 11 | 0* | 27 | Online (CICS) |
| 12 | CBTRN02C | 731 | 6 | 29 | 93 | Batch |
| 13 | COTRN00C | 699 | 9 | 0* | 34 | Online (CICS) |
| 14 | COUSR00C | 695 | 9 | 0* | 33 | Online (CICS) |
| 15 | CBACT04C | 652 | 6 | 24 | 86 | Batch |

> *CICS programs use EXEC CICS READ/WRITE rather than native file I/O; the "0" reflects only batch-style SELECT/READ/WRITE counts.

---

## 2. Detailed Program-by-Program Analysis

### Rank 1: COACTUPC.cbl (4,236 LOC)

**Purpose:** Account Update — accept and process account field changes with extensive validation.

**Why it ranks #1:**
- By far the largest program in the estate (4,236 lines — 2× the next largest)
- References **58 copybooks** (includes 38 instances of CSSETATY for BMS attribute control)
- Contains **174 IF statements** and **10 EVALUATE blocks** with up to 4 levels of nesting
- Complex validation logic for: dates (CSUTLDWY), US state codes (CSLKPCDY), phone area codes, ZIP code-state combinations
- Reads/writes to 3 VSAM files (ACCTDAT, CARDXREF, CUSTDAT) via CICS
- Pseudo-conversational CICS with full field-level validation

**Modernization risk:** HIGH — Dense validation logic mixed with screen handling. Field-by-field input validation will need careful extraction.

---

### Rank 2: COTRTLIC.cbl (2,098 LOC)

**Purpose:** Transaction Type List — DB2 cursor-based browsing with forward/backward paging.

**Why it ranks #2:**
- 2,098 lines with embedded SQL (EXEC SQL) for DB2 cursors
- **99 IF statements**, **16 EVALUATE blocks** with 5-level nesting (deepest in estate)
- **29 I/O operations** across DB2 cursors and CICS BMS maps
- Mixed technology: CICS + DB2 + BMS

**Modernization risk:** HIGH — DB2 cursor management and CICS screen interaction tightly coupled. Requires SQL extraction and UI separation.

---

### Rank 3: COTRTUPC.cbl (1,702 LOC)

**Purpose:** Transaction Type Update — DB2 DML (INSERT/UPDATE/DELETE) with validation.

**Why it ranks #3:**
- 1,702 lines with embedded SQL for multiple DML operations
- **108 IF statements**, **26 EVALUATE blocks** (most EVALUATEs in estate)
- **31 I/O operations** (highest I/O count among CICS programs)
- Complex state machine for add/update/delete modes

**Modernization risk:** HIGH — Multiple DB2 operations with error handling and mode switching.

---

### Rank 4: COCRDUPC.cbl (1,560 LOC)

**Purpose:** Credit Card Update — accept and process card detail changes.

**Why it ranks #4:**
- 1,560 lines with **151 IF statements** (second-highest IF count)
- **16 EVALUATE blocks** for screen flow control
- Field validation for card numbers, expiration dates, names

**Modernization risk:** MEDIUM-HIGH — Similar pattern to COACTUPC but narrower domain (card entity only).

---

### Rank 5: COCRDLIC.cbl (1,459 LOC)

**Purpose:** Credit Card List — browse card file with forward/backward paging.

**Why it ranks #5:**
- 1,459 lines with **131 IF statements**, **18 EVALUATE blocks**
- Complex CICS browse logic (STARTBR/READNEXT/READPREV/ENDBR)
- 3 XCTL targets for navigation
- Array handling for display rows

**Modernization risk:** MEDIUM — Browsing/pagination is a well-understood pattern but CICS browse API is stateful.

---

### Rank 6: COPAUA0C.cbl (1,026 LOC)

**Purpose:** Card Authorization Decision — MQ-based real-time authorization with IMS DB access.

**Why it ranks #6:**
- 1,026 lines with 4 MQ calls (MQOPEN, MQGET, MQPUT1, MQCLOSE)
- Crosses 3 middleware boundaries: CICS + IMS + MQ
- **52 IF statements**, **10 EVALUATE blocks**
- Complex authorization rules including credit limit checks, card status, fraud detection

**Modernization risk:** VERY HIGH — Most architecturally complex program. Involves MQ messaging, IMS database, and CICS. Requires understanding of all three middleware stacks.

---

### Rank 7: COPAUS0C.cbl (1,032 LOC)

**Purpose:** Authorization Summary View — CICS BMS screens with IMS database access.

**Why it ranks #7:**
- 1,032 lines with IMS DL/I calls for data retrieval
- **25 IF statements**, **11 EVALUATE blocks** with 4-level nesting
- 15 copybooks including IMS segment layouts

**Modernization risk:** HIGH — IMS database dependency requires replacement with relational DB access.

---

### Rank 8: CBSTM03A.CBL (924 LOC)

**Purpose:** Print Account Statements — reads multiple files, generates text and HTML output.

**Why it ranks #8:**
- 924 lines with **14 CALL statements** to subroutine CBSTM03B (most CALLs in estate)
- **119 I/O operations** (highest I/O count in entire estate)
- Generates both plain text and HTML output formats
- Complex report formatting with page breaks, totals, and multi-file joins

**Modernization risk:** MEDIUM — Well-structured with clear subroutine separation. The multi-format output generation maps well to modern template engines.

---

### Rank 9: COACTVWC.cbl (941 LOC)

**Purpose:** Account View — display account details with linked card and customer data.

**Why it ranks #9:**
- 941 lines, 16 copybooks
- Multi-entity lookup: reads CARDXREF (via AIX), ACCTDAT, CUSTDAT
- **60 IF statements**, **10 EVALUATE blocks**

**Modernization risk:** MEDIUM — Read-only program with clear data flow. Good candidate for early modernization.

---

### Rank 10: COCRDSLC.cbl (887 LOC)

**Purpose:** Credit Card Detail View — display card information with customer lookup.

**Why it ranks #10:**
- 887 lines, 16 copybooks
- **72 IF statements**, **8 EVALUATE blocks**
- Multi-file reads (card + customer data)

**Modernization risk:** MEDIUM — Similar to COACTVWC; read-only inquiry pattern.

---

## 3. Complexity Metrics Summary

### By Lines of Code (Top 10)

| Rank | Program | LOC |
|------|---------|-----|
| 1 | COACTUPC | 4,236 |
| 2 | COTRTLIC | 2,098 |
| 3 | COTRTUPC | 1,702 |
| 4 | COCRDUPC | 1,560 |
| 5 | COCRDLIC | 1,459 |
| 6 | COPAUS0C | 1,032 |
| 7 | COPAUA0C | 1,026 |
| 8 | COACTVWC | 941 |
| 9 | CBSTM03A | 924 |
| 10 | COCRDSLC | 887 |

### By Copybooks Referenced

| Rank | Program | Count |
|------|---------|-------|
| 1 | COACTUPC | 58 |
| 2 | COPAUA0C | 17 |
| 3 | COACTVWC | 16 |
| 4 | COCRDSLC | 16 |
| 5 | COCRDUPC | 16 |
| 6 | COPAUS0C | 15 |
| 7 | COTRTUPC | 15 |
| 8 | COCRDLIC | 14 |
| 9 | COTRTLIC | 12 |
| 10 | COBIL00C | 11 |

### By I/O Operations

| Rank | Program | Count | Type |
|------|---------|-------|------|
| 1 | CBSTM03A | 119 | Batch file I/O (via CALL to CBSTM03B) |
| 2 | CBIMPORT | 36 | Batch file I/O |
| 3 | CBEXPORT | 35 | Batch file I/O |
| 4 | COTRTUPC | 31 | CICS + DB2 |
| 5 | CBTRN02C | 29 | Batch file I/O |
| 6 | COTRTLIC | 29 | CICS + DB2 |
| 7 | CBTRN03C | 28 | Batch file I/O |
| 8 | CBACT04C | 24 | Batch file I/O |
| 9 | CBTRN01C | 23 | Batch file I/O |
| 10 | CBSTM03B | 21 | Batch file I/O (subroutine) |

### By Business Logic Density (IF + EVALUATE)

| Rank | Program | IF | EVALUATE | Total | Max Nest |
|------|---------|----|---------|----|----------|
| 1 | COACTUPC | 174 | 10 | 184 | 4 |
| 2 | COCRDUPC | 151 | 16 | 167 | 3 |
| 3 | COCRDLIC | 131 | 18 | 149 | 4 |
| 4 | COTRTUPC | 108 | 26 | 134 | 3 |
| 5 | COTRTLIC | 99 | 16 | 115 | 5 |
| 6 | CBTRN02C | 93 | 0 | 93 | 3 |
| 7 | CBACT04C | 86 | 0 | 86 | 4 |
| 8 | COCRDSLC | 72 | 8 | 80 | 2 |
| 9 | CBTRN03C | 75 | 4 | 79 | 4 |
| 10 | COACTVWC | 60 | 10 | 70 | 2 |

### By Max Nesting Depth

| Depth | Programs |
|-------|----------|
| 5 | COTRTLIC |
| 4 | COACTUPC, COCRDLIC, COPAUS0C, COTRN02C, COTRN00C, COUSR00C, CBACT04C, CBTRN03C, COBIL00C |
| 3 | COTRTUPC, COCRDUPC, COPAUA0C, CBSTM03A, CBTRN02C, CORPT00C, CBTRN01C |
| 2 | COACTVWC, COCRDSLC |

### By Inter-Program Dependencies

| Rank | Program | Outgoing Deps | Type |
|------|---------|---------------|------|
| 1 | CBSTM03A | 14 | 11 CALLs to CBSTM03B + CEE3ABD |
| 2 | COACCT01 | 9 | MQ calls (OPEN×3, GET, PUT×2, CLOSE×3) |
| 3 | CODATE01 | 9 | MQ calls (OPEN×3, GET, PUT×2, CLOSE×3) |
| 4 | COPAUA0C | 4 | MQ calls (OPEN, GET, PUT1, CLOSE) |
| 5 | COCRDLIC | 3 | XCTLs to menu + 2 card programs |
| 6 | CBACT01C | 2 | CALLs to COBDATFT + CEE3ABD |
| 7 | CORPT00C | 2 | CALLs to CSUTLDTC ×2 |
| 8 | COTRN02C | 2 | CALLs to CSUTLDTC ×2 |

---

## 4. Modernization Priority Recommendations

### Wave 1 — Quick Wins (Low Risk, High Value)

| Priority | Program | Rationale |
|----------|---------|-----------|
| 1 | **COACTVWC** | Read-only account view. Clean data flow, no writes. Good first proof-of-concept for the online layer. Validates VSAM-to-RDBMS migration for account/card/customer entities. |
| 2 | **COCRDSLC** | Read-only card detail view. Similar pattern to COACTVWC. Exercises card entity lookup. |
| 3 | **COTRN01C** | Read-only transaction view. Single-record lookup. Simple, low-risk. |
| 4 | **COSGN00C** | Sign-on screen. Small (260 LOC), well-bounded. Can validate authentication migration to Spring Security or equivalent. |
| 5 | **COMEN01C / COADM01C** | Menu programs. Small, no file I/O. Navigation routing that maps directly to a web application router/controller. |

**Rationale:** Start with read-only programs to validate data access layer and UI framework choices without risking data integrity.

### Wave 2 — Core Batch Programs (Medium Risk)

| Priority | Program | Rationale |
|----------|---------|-----------|
| 6 | **CBACT01C** | Simple read-and-write batch. 430 LOC. Good pilot for batch framework validation (Spring Batch). |
| 7 | **CBTRN03C** | Report generation. 649 LOC. Validates multi-file join and report output patterns. |
| 8 | **CBEXPORT / CBIMPORT** | Data migration pair. Well-structured, clear record parsing. Tests multi-record format handling. |
| 9 | **CBSTM03A / CBSTM03B** | Statement generation. Already has subroutine separation that maps to service layer. HTML output is a bridge to modern UI. |

### Wave 3 — Complex Online CRUD (High Risk)

| Priority | Program | Rationale |
|----------|---------|-----------|
| 10 | **COBIL00C** | Bill payment. First write-path online program. Validates transaction integrity in the new platform. |
| 11 | **COTRN02C** | Add transaction. Writes to multiple files. Tests data consistency. |
| 12 | **COCRDUPC** | Card update. 1,560 LOC of validation and update logic. Significant effort but contained to card entity. |
| 13 | **COACTUPC** | Account update. **Largest program** (4,236 LOC). Should be last among online programs due to extreme complexity and validation density. |

### Wave 4 — Middleware-Dependent Programs (Very High Risk)

| Priority | Program | Rationale |
|----------|---------|-----------|
| 14 | **COTRTLIC / COTRTUPC** | DB2-dependent. Requires DB2-to-RDBMS SQL migration and cursor management replacement. |
| 15 | **CBTRN01C / CBTRN02C** | Daily transaction posting. Core batch pipeline. Requires careful testing of balance calculations and reject handling. |
| 16 | **CBACT04C** | Interest calculator. Complex financial logic with multi-file random access. Requires precise numeric accuracy validation. |
| 17 | **COPAUA0C** | Authorization decision. **Most architecturally complex** — MQ + IMS + CICS. Requires message queue replacement (e.g. Kafka/RabbitMQ), IMS-to-RDBMS migration, and real-time decision logic extraction. |
| 18 | **COPAUS0C / COPAUS1C / COPAUS2C** | Authorization screens + fraud marking. Dependent on IMS database and DB2 fraud table. |
| 19 | **IMS batch programs** (PAUDBLOD, PAUDBUNL, DBUNLDGS, CBPAUP0C) | IMS DL/I database utilities. Can be replaced with SQL-based equivalents once IMS data is migrated. |

### Wave 5 — Utility and Infrastructure

| Priority | Program | Rationale |
|----------|---------|-----------|
| 20 | **CSUTLDTC** | Date utility. Replace with Java `java.time` APIs. |
| 21 | **COBSWAIT** | Wait utility. Replace with `Thread.sleep()` or scheduled executor. |
| 22 | **COUSR00C–COUSR03C** | User CRUD. Replace with Spring Security user management. |
| 23 | **COACCT01 / CODATE01** | VSAM-MQ service programs. Replace with REST/gRPC microservices. |

---

## 5. Key Risks and Observations

### Data Integrity Risks
- **Decimal precision:** COBOL PIC S9(10)V99 has exact 2-decimal arithmetic. Java `BigDecimal` must be used — never `double`/`float`.
- **Packed decimal (COMP-3):** Used extensively in IMS segments and export records. Requires custom byte-level conversion during data migration.
- **Binary (COMP):** Used for counters and sequence numbers. Maps to Java `int`/`long` but byte-order matters during file conversion.

### Architectural Complexity
- **CICS pseudo-conversational model:** All online programs use EXEC CICS RETURN TRANSID for pseudo-conversational processing. This state management model has no direct equivalent in modern web frameworks — requires redesign to session-based or stateless patterns.
- **BMS maps:** 21 BMS map definitions drive screen layout. These define both field positions and attribute bytes. Modern UI will require complete reimplementation.
- **COMMAREA passing:** Inter-program communication via COCOM01Y COMMAREA. This is effectively a shared memory structure — replace with DTOs or session state.

### Testing Recommendations
- **CBACT04C (interest calculator):** Create comprehensive test cases with known-good results before modernizing. Financial calculations must produce identical output.
- **CBTRN02C (transaction posting):** Build golden-file tests — process a known daily transaction file and compare outputs field-by-field.
- **CBEXPORT/CBIMPORT:** Use round-trip testing — export then import should produce identical data.
