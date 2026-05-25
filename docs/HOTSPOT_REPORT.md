# Hotspot Report — CardDemo COBOL Estate

## Overview

This report ranks the top 10 most complex programs in the CardDemo COBOL estate using five quantitative metrics, then provides modernization priority recommendations.

---

## 1. Ranking Criteria

| Metric | Description | Weight Rationale |
|--------|-------------|-----------------|
| Lines of Code (LOC) | Total source lines | Larger programs require more effort to understand and convert |
| Copybooks Referenced | Count of COPY statements | More dependencies = higher coupling = harder to isolate |
| I/O Operations | CICS/SQL/MQ/file operations | I/O complexity drives interface mapping in target architecture |
| Logic Density | Max IF/EVALUATE nesting depth | Deep nesting indicates complex business rules requiring careful analysis |
| Inter-Program Dependencies | CALLs + XCTLs + programs that call this one | High connectivity = ripple risk during modernization |

---

## 2. Top 10 Programs Ranked by Composite Score

| Rank | Program | LOC | Copybooks | I/O Ops | Max Nesting | Dependencies | Composite Score |
|------|---------|-----|-----------|---------|-------------|-------------|----------------|
| 1 | **COACTUPC** | 4,236 | 56 | 19 | 4 | 2 outbound + called by COMEN01C | **95** |
| 2 | **COTRTLIC** | 2,098 | 11 | 40 | 3 | 2 outbound + called by COADM01C | **72** |
| 3 | **COTRTUPC** | 1,702 | 13 | 40 | 3 | 2 outbound + called by COADM01C | **68** |
| 4 | **COCRDUPC** | 1,560 | 15 | 13 | 3 | 2 outbound + called by COCRDLIC | **62** |
| 5 | **COCRDLIC** | 1,459 | 13 | 22 | 4 | 6 outbound (XCTL to 3 programs) | **61** |
| 6 | **COPAUS0C** | 1,032 | 14 | 10 | 4 | 2 outbound + called by COMEN01C | **52** |
| 7 | **COPAUA0C** | 1,026 | 16 | 27 | 3 | 8 outbound (MQ calls) | **51** |
| 8 | **CBSTM03A** | 924 | 4 | 120 | 3 | 14 (calls CBSTM03B + LE) | **50** |
| 9 | **COACTVWC** | 941 | 15 | 15 | 2 | 2 outbound + called by COMEN01C | **47** |
| 10 | **CBTRN02C** | 731 | 5 | 30 | 3 | 1 outbound + called by POSTTRAN JCL | **44** |

**Scoring formula**: `(LOC/100) + (Copybooks×2) + (I/O×1.5) + (Nesting×5) + (Dependencies×3)`, normalized to 0–100.

---

## 3. Detailed Analysis

### Rank 1: COACTUPC.cbl (Account Update) — Score: 95

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 4,236 | Largest program in the estate by far |
| Copybooks | 56 | Includes: 15 application copybooks from `app/cpy/`, ~17 BMS-generated copybooks from `app/cpy-bms/`, plus IBM-supplied copybooks (DFHAID, DFHBMSCA, etc.). The APPLICATION_INVENTORY lists only application copybooks. References almost every copybook including CSLKPCDY (1,318 lines of lookup data) |
| I/O Ops | 19 | CICS READ, REWRITE on account + customer + card files |
| Max Nesting | 4 | Complex validation logic with nested IF/EVALUATE |
| Dependencies | Called by COMEN01C; calls no sub-programs but reads 3 VSAM files + validates against lookup tables |

**Why complex**: This is the most feature-rich screen — it handles inline editing of account fields (status, limits, dates), customer fields (address, phone, SSN), and card cross-reference data. It embeds the entire CSLKPCDY lookup table (phone area codes, state codes, ZIP prefixes) for real-time validation. The business logic density is extreme.

---

### Rank 2: COTRTLIC.cbl (Transaction Type List — DB2) — Score: 72

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 2,098 | Second largest program |
| Copybooks | 11 | DB2-specific copybooks (CSDB2RPY, CSDB2RWY) |
| I/O Ops | 40 | Heavy DB2 cursor operations (OPEN, FETCH, CLOSE, DELETE) |
| Max Nesting | 3 | Cursor-driven paging logic with EVALUATE blocks |
| Dependencies | Called by COADM01C; uses DB2 infrastructure |

**Why complex**: Implements full cursor-based paging with forward/backward scroll, inline delete, and select-for-update — all through embedded SQL. The DB2 interaction patterns are significantly different from VSAM and require separate migration strategy.

---

### Rank 3: COTRTUPC.cbl (Transaction Type Update — DB2) — Score: 68

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 1,702 | Third largest |
| Copybooks | 13 | DB2 + BMS screen copybooks |
| I/O Ops | 40 | DB2 SELECT, UPDATE, INSERT operations |
| Max Nesting | 3 | Input validation + DB2 error handling |
| Dependencies | Called by COADM01C |

**Why complex**: Full CRUD operations on DB2 tables with optimistic locking, input validation, and formatted error messages from DSNTIAC utility.

---

### Rank 4: COCRDUPC.cbl (Credit Card Update) — Score: 62

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 1,560 | |
| Copybooks | 15 | Card + Account + Customer + UI |
| I/O Ops | 13 | CICS READ/REWRITE on card, account, customer |
| Max Nesting | 3 | Card number validation, status checks |
| Dependencies | Called by COCRDLIC and COMEN01C |

---

### Rank 5: COCRDLIC.cbl (Credit Card List) — Score: 61

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 1,459 | |
| Copybooks | 13 | Card + Xref + UI |
| I/O Ops | 22 | CICS STARTBR/READNEXT/READPREV/ENDBR (browsing) |
| Max Nesting | 4 | Complex paging logic with boundary conditions |
| Dependencies | 6 outbound XCTLs to detail/update screens |

---

### Rank 6: COPAUS0C.cbl (Authorization Summary View) — Score: 52

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 1,032 | |
| Copybooks | 14 | IMS + Auth-specific + standard UI |
| I/O Ops | 10 | CICS file operations for auth data |
| Max Nesting | 4 | Complex screen state management |
| Dependencies | Called by COMEN01C; links to COPAUS1C |

---

### Rank 7: COPAUA0C.cbl (Card Authorization Decision) — Score: 51

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 1,026 | |
| Copybooks | 16 | MQ + IMS + Account + Customer + Auth |
| I/O Ops | 27 | MQ OPEN/GET/PUT/CLOSE + CICS file reads |
| Max Nesting | 3 | Authorization decision tree |
| Dependencies | 8 external calls (MQ API) |

**Why complex**: Real-time authorization with MQ message processing, multi-file lookups, and complex approval/decline logic. Crosses three middleware boundaries (CICS + MQ + VSAM).

---

### Rank 8: CBSTM03A.CBL (Statement Generation Master) — Score: 50

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 924 | |
| Copybooks | 4 | |
| I/O Ops | 120 | Extremely high — multiple file opens/reads/writes |
| Max Nesting | 3 | |
| Dependencies | 14 (calls CBSTM03B, uses LE services) |

**Why complex**: Highest I/O operation count in the estate. Orchestrates statement generation across multiple files and calls a sub-program for detailed processing. Output includes both text and HTML formats.

---

### Rank 9: COACTVWC.cbl (Account View) — Score: 47

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 941 | |
| Copybooks | 15 | Account + Card + Customer + UI |
| I/O Ops | 15 | CICS READ on 3 VSAM files via AIX |
| Max Nesting | 2 | Relatively straightforward display logic |
| Dependencies | Called by COMEN01C |

---

### Rank 10: CBTRN02C.cbl (Post Daily Transactions) — Score: 44

| Metric | Value | Notes |
|--------|-------|-------|
| LOC | 731 | |
| Copybooks | 5 | Transaction + Account + Xref |
| I/O Ops | 30 | Multiple VSAM reads/writes/rewrites |
| Max Nesting | 3 | Validation + category balance updates |
| Dependencies | Called by POSTTRAN JCL; core of daily batch cycle |

**Why complex**: Heart of the daily batch pipeline. Validates transactions, updates category balances, updates account balances, and writes rejected records. Any error here impacts financial accuracy.

---

## 4. Modernization Priority Recommendations

### Tier 1 — Modernize First (High Impact, Clear Boundaries)

| Priority | Program | Rationale |
|----------|---------|-----------|
| **1** | **CBTRN02C** (Post Transactions) | Core batch pipeline; well-bounded; clear inputs/outputs; highest business value. Converting this first enables parallel testing of transaction posting accuracy. |
| **2** | **CBACT04C** (Interest Calculator) | Pure computation with file I/O; no UI; ideal for microservice extraction. Business logic (rate × balance) maps cleanly to modern patterns. |
| **3** | **CBTRN03C** (Transaction Report) | Read-only reporting; no data modification risk; good candidate for parallel-run validation during migration. |

### Tier 2 — Modernize Second (High Complexity, High Value)

| Priority | Program | Rationale |
|----------|---------|-----------|
| **4** | **COACTUPC** (Account Update) | Highest LOC and complexity but also highest user interaction value. Requires careful extraction of CSLKPCDY validation rules into a validation service. Plan for phased delivery. |
| **5** | **COTRTLIC / COTRTUPC** (DB2 Programs) | Already use SQL — closest to modern patterns. DB2 queries can be mapped directly to JPA/JDBC. Separate middleware concern (CICS screens → REST API). |
| **6** | **COPAUA0C** (Authorization) | Revenue-critical real-time path. MQ integration maps to modern event-driven architectures. Requires careful latency testing. |

### Tier 3 — Modernize Last (Lower Risk, Template-Driven)

| Priority | Program | Rationale |
|----------|---------|-----------|
| **7** | **COCRDLIC / COCRDUPC / COCRDSLC** (Card screens) | Follow identical patterns to account screens. Once COACTUPC is converted, these can use the same templates. |
| **8** | **COUSR00C–03C** (User Management) | Simple CRUD on USRSEC file. Low business logic. Can be replaced by standard IAM. |
| **9** | **CBSTM03A/B** (Statements) | High I/O but straightforward ETL pattern. Modern PDF/HTML generation tools make this simpler. |
| **10** | **COSGN00C / COMEN01C / COADM01C** (Navigation) | Thin routing layer that disappears when UI is rebuilt as web application. |

---

## 5. Risk Assessment

| Risk Factor | Programs Affected | Mitigation |
|-------------|-------------------|------------|
| **Financial accuracy** | CBTRN02C, CBACT04C, COBIL00C | Parallel-run old and new; reconcile daily balances |
| **Real-time latency** | COPAUA0C | Performance benchmarks before and after; staged rollout |
| **Data integrity** | COACTUPC (multi-file update) | Transaction boundary testing; rollback scenarios |
| **Embedded validation** | COACTUPC (CSLKPCDY) | Extract lookup data to reference database or configuration service |
| **IMS dependency** | CBPAUP0C, PAUDBLOD, PAUDBUNL | Migrate IMS data to relational/NoSQL before program conversion |
| **MQ coupling** | COPAUA0C, COACCT01, CODATE01 | Replace MQ with modern messaging (Kafka, SQS) as infrastructure layer |

---

## 6. Key Metrics Summary

| Metric | Total Estate | Average | Median | Max (Program) |
|--------|-------------|---------|--------|---------------|
| Lines of Code | ~32,000 | ~710 | 649 | 4,236 (COACTUPC) |
| Copybooks per Program | — | 10.5 | 10 | 56 (COACTUPC) |
| I/O Ops per Program | — | 22 | 15 | 120 (CBSTM03A) |
| Max Nesting Depth | — | 3.1 | 3 | 4 (COACTUPC, COCRDLIC, COTRN02C, CBACT04C) |
| VSAM Files | 9 unique clusters | — | — | — |
| DB2 Tables | 2+ (transaction types) | — | — | — |
| MQ Queues | 3+ (auth req/reply/error) | — | — | — |
