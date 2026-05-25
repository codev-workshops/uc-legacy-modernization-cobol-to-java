# Hotspot Report — CardDemo Legacy COBOL System

> Comprehensive complexity analysis ranking all 44 programs by multiple dimensions to guide modernization priority.

---

## Lines of Code (Top 15)

| Rank | Program | Lines | Module | Classification |
|------|---------|-------|--------|----------------|
| 1 | COACTUPC | 4,236 | Core | Online/CICS |
| 2 | COTRTLIC | 2,098 | DB2 Extension | Online/CICS+DB2 |
| 3 | COTRTUPC | 1,702 | DB2 Extension | Online/CICS+DB2 |
| 4 | COCRDUPC | 1,560 | Core | Online/CICS |
| 5 | COCRDLIC | 1,459 | Core | Online/CICS |
| 6 | COPAUS0C | 1,032 | Auth Extension | Online/CICS+IMS |
| 7 | COPAUA0C | 1,026 | Auth Extension | Online/CICS+IMS+MQ |
| 8 | COACTVWC | 941 | Core | Online/CICS |
| 9 | CBSTM03A | 924 | Core | Batch |
| 10 | COCRDSLC | 887 | Core | Online/CICS |
| 11 | COTRN02C | 783 | Core | Online/CICS |
| 12 | CBTRN02C | 731 | Core | Batch |
| 13 | COTRN00C | 699 | Core | Online/CICS |
| 14 | COUSR00C | 695 | Core | Online/CICS |
| 15 | CBACT04C | 652 | Core | Batch |

**Full listing (all 44):** COACTUPC (4236) > COTRTLIC (2098) > COTRTUPC (1702) > COCRDUPC (1560) > COCRDLIC (1459) > COPAUS0C (1032) > COPAUA0C (1026) > COACTVWC (941) > CBSTM03A (924) > COCRDSLC (887) > COTRN02C (783) > CBTRN02C (731) > COTRN00C (699) > COUSR00C (695) > CBACT04C (652) > CORPT00C (649) > CBTRN03C (649) > COACCT01 (620) > COPAUS1C (604) > CBEXPORT (582) > COBIL00C (572) > CODATE01 (524) > CBTRN01C (494) > CBIMPORT (487) > CBACT01C (430) > COUSR02C (414) > CBPAUP0C (386) > PAUDBLOD (369) > DBUNLDGS (366) > COUSR03C (359) > COTRN01C (330) > PAUDBUNL (317) > COMEN01C (308) > COUSR01C (299) > COADM01C (288) > COSGN00C (260) > COPAUS2C (244) > COBTUPDT (237) > CBSTM03B (230) > CBCUS01C (178) > CBACT03C (178) > CBACT02C (178) > CSUTLDTC (157) > COBSWAIT (41)

**Total:** 28,614 lines of COBOL across 44 programs.

---

## Copybook References (Top 10)

| Rank | Program | COPY Count | Notable Includes |
|------|---------|-----------|------------------|
| 1 | COACTUPC | 58 | 13 unique copybooks + CSSETATY REPLACING ×4, CSSTRPFY |
| 2 | COPAUA0C | 17 | 8 MQ copybooks + IMS + business entity copybooks |
| 3 | COCRDUPC | 16 | CSSETATY REPLACING, all entity copybooks |
| 4 | COCRDSLC | 16 | All entity + BMS + message copybooks |
| 5 | COACTVWC | 16 | Entity + BMS + CSSTRPFY |
| 6 | COTRTUPC | 15 | DB2 DCL includes + BMS + common |
| 7 | COPAUS0C | 15 | IMS segments + entity + BMS |
| 8 | COCRDLIC | 14 | Entity + BMS + CSSTRPFY |
| 9 | COTRTLIC | 12 | DB2 DCL + read params + BMS |
| 10 | COTRN02C | 11 | Transaction + account entity + BMS |

---

## I/O Operations (Top 10)

Counts include EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT/READPREV/ENDBR, batch file READ/WRITE, EXEC SQL, EXEC DLI, and MQ operations.

| Rank | Program | I/O Ops | Technologies |
|------|---------|---------|-------------|
| 1 | CBSTM03A | 101 | Batch file (4 files, heavy read/write cycles) |
| 2 | COPAUA0C | 20 | CICS VSAM + IMS DL/I + MQ operations |
| 3 | COTRTLIC | 20 | CICS + DB2 cursor (DECLARE/OPEN/FETCH/CLOSE/DELETE/UPDATE) |
| 4 | COACCT01 | 19 | CICS + MQ (MQOPEN/GET/PUT/CLOSE for 3 queues) |
| 5 | CODATE01 | 18 | CICS + MQ (MQOPEN/GET/PUT/CLOSE for 3 queues) |
| 6 | CBPAUP0C | 15 | IMS DL/I (GN, GNP, DLET, CHKP sequential scan) |
| 7 | CBTRN02C | 12 | Batch (6 VSAM files, complex posting logic) |
| 8 | COCRDLIC | 11 | CICS VSAM (STARTBR/READNEXT/ENDBR paging) |
| 9 | COACTUPC | 11 | CICS VSAM (READ/REWRITE across 3 datasets) |
| 10 | CBEXPORT | 11 | Batch (4 input VSAM + 1 output sequential) |

---

## Business Logic Density

Measured as: (conditional statements + PERFORM + EVALUATE + COMPUTE) / total lines.

| Rank | Program | Density | Key Patterns |
|------|---------|---------|-------------|
| 1 | COACTUPC | Very High | 4,236 lines of validation, cross-entity updates, 13 copybooks |
| 2 | CBTRN02C | High | Transaction posting with reject handling, balance updates |
| 3 | CBACT04C | High | Interest calculation with category-based rates, disclosure groups |
| 4 | COTRTLIC | High | DB2 cursor paging with FETCH loops, delete/update selection matrix |
| 5 | COPAUA0C | High | Authorization decision engine, multi-technology validation |
| 6 | COTRTUPC | High | CRUD operations with optimistic locking, DB2 commit patterns |
| 7 | CBSTM03A | Moderate | ALTER/GO TO patterns, 2D arrays, PSA addressing (deliberate legacy) |
| 8 | CBTRN01C | Moderate | Transaction validation, cross-reference lookups |
| 9 | COTRN02C | Moderate | Online transaction posting with real-time validation |
| 10 | COCRDLIC | Moderate | Browsable list with selection handling, admin vs user logic |

---

## Technology Complexity

Programs ranked by number of distinct middleware technologies used:

| Rank | Program | Technologies | Count | Details |
|------|---------|-------------|-------|---------|
| 1 | COPAUA0C | CICS + IMS + MQ | 3 | MQ trigger, DL/I write, CICS VSAM read — highest integration complexity |
| 2 | COPAUS2C | CICS + IMS + DB2 | 3 | DL/I read + SQL INSERT (fraud logging) — two-phase potential |
| 3 | COTRTLIC | CICS + DB2 | 2 | Full cursor management with paging, DELETE/UPDATE |
| 4 | COTRTUPC | CICS + DB2 | 2 | SELECT/UPDATE/DELETE/INSERT with optimistic locking |
| 5 | COACCT01 | CICS + MQ | 2 | MQ request/response pattern with VSAM lookup |
| 6 | CODATE01 | CICS + MQ | 2 | MQ request/response for system date |
| 7 | COPAUS0C | CICS + IMS | 2 | DL/I navigation with BMS screen |
| 8 | COPAUS1C | CICS + IMS | 2 | DL/I detail retrieval with BMS |
| 9 | CBPAUP0C | Batch + IMS | 2 | BMP batch with DL/I sequential processing + checkpoint |
| 10 | CBSTM03A | Batch + ASM | 2 | ALTER/GO TO + PSA addressing + CALL subroutine + CEE3ABD |
| 11 | CBACT01C | Batch + ASM | 2 | CALL COBDATFT (ASM date converter) |
| 12 | COBTUPDT | Batch + DB2 | 2 | Batch EXEC SQL with file input |
| — | All other CO* | CICS only | 1 | Standard CICS/VSAM online programs |
| — | All other CB* | Batch only | 1 | Standard batch VSAM programs |

---

## Modernization Recommendations

### Priority 1: COACTUPC (Account Update)
- **Metrics:** 4,236 LOC | 58 COPY refs | 11 I/O ops | 3 VSAM datasets
- **Justification:** Largest program by far (48% larger than #2). Deep field-level validation across account, card, and customer entities. 13 unique copybooks including CSSETATY REPLACING patterns. CSLKPCDY phone lookup table. Most complex screen interaction in the system.
- **Modernization approach:** Decompose into Account, Card, and Customer microservices with shared validation library.

### Priority 2: COPAUA0C (Authorization Decision Engine)
- **Metrics:** 1,026 LOC | 17 COPY refs | 20 I/O ops | 3 technologies (CICS+IMS+MQ)
- **Justification:** Highest technology complexity (CICS + IMS + MQ). Real-time authorization decision path — latency-critical. MQ trigger-based architecture. Reads VSAM (card/account/customer validation), writes IMS (pending auth), responds via MQ.
- **Modernization approach:** Event-driven microservice with message broker, relational DB for auth store, REST/gRPC for synchronous lookups.

### Priority 3: CBTRN02C (Batch Transaction Posting)
- **Metrics:** 731 LOC | 7 COPY refs | 12 I/O ops | 6 VSAM files
- **Justification:** Central to daily processing cycle (POSTTRAN job). Processes daily transactions with validation against card/account/customer, posts to transaction VSAM, generates rejects, updates category balances. Core batch engine.
- **Modernization approach:** Spring Batch with chunk-oriented processing, JPA entities for VSAM-equivalent tables.

### Priority 4: CBSTM03A/CBSTM03B (Statement Generation)
- **Metrics:** 924+230 LOC | 4 COPY refs | 101 I/O ops | Deliberate legacy patterns
- **Justification:** Deliberately exercises legacy patterns: ALTER verb, GO TO, mainframe PSA (address register manipulation), 2D arrays, CALL subroutine. Highest I/O operation count. Generates both text and HTML output.
- **Modernization approach:** Template-based report generator (e.g., JasperReports or Apache POI). The ALTER/GO TO patterns require careful control flow analysis.

### Priority 5: COPAUS2C (Fraud Marking — CICS+IMS+DB2)
- **Metrics:** 244 LOC | 7 COPY refs | 7 I/O ops | 3 technologies
- **Justification:** Despite small size, combines CICS + IMS DL/I + DB2 SQL in a single program. Marks authorization as fraudulent (IMS update + DB2 INSERT for audit). Potential two-phase commit boundary.
- **Modernization approach:** Transactional service with saga pattern for cross-store consistency.

### Priority 6: COTRTLIC (Transaction Type List — CICS+DB2)
- **Metrics:** 2,098 LOC | 12 COPY refs | 20 I/O ops | DB2 cursor paging
- **Justification:** Second largest program. Demonstrates full DB2 cursor-based paging (DECLARE CURSOR, OPEN, FETCH loop, CLOSE). Supports inline delete and update selection. Reference implementation for DB2 integration patterns.
- **Modernization approach:** JPA/Spring Data with pageable queries, REST API with offset/cursor pagination.

### Priority 7: CBACT04C (Interest Calculation)
- **Metrics:** 652 LOC | 6 COPY refs | 9 I/O ops | Critical business logic
- **Justification:** Implements interest calculation rules using disclosure group rates and category balances. Monthly batch critical path. Complex financial arithmetic with S9(10)V99 precision.
- **Modernization approach:** Business rules engine (e.g., Drools) or dedicated calculation service with BigDecimal precision.

### Priority 8: COCRDUPC (Card Update)
- **Metrics:** 1,560 LOC | 16 COPY refs | 12 I/O ops | 3 datasets
- **Justification:** Fourth largest. Full CRUD operations on card data with cross-entity validation (account + customer). CSSETATY REPLACING for dynamic attribute management.
- **Modernization approach:** Card management service with validation middleware.

### Priority 9: CBEXPORT/CBIMPORT (Data Migration Pair)
- **Metrics:** 582+487 LOC | Multi-entity record handling
- **Justification:** Export/import pair handles 4 entity types in a single flat file using record-type discrimination (CVEXPORT copybook). Critical for data migration and disaster recovery.
- **Modernization approach:** ETL pipeline (Spring Batch or Apache Camel) with entity-specific processors.

### Priority 10: Remaining Programs (in order)
1. **COCRDLIC** (1,459 LOC) — Card list with browsing
2. **COPAUS0C** (1,032 LOC) — Auth summary with IMS navigation
3. **COTRTUPC** (1,702 LOC) — Transaction type CRUD (DB2)
4. **COACTVWC** (941 LOC) — Account view (read-only, less risky)
5. **COTRN00C** (699 LOC) — Transaction list browsing
6. **COUSR00C** (695 LOC) — User list
7. **CORPT00C** (649 LOC) — Report submission
8. **COBIL00C** (572 LOC) — Bill payment
9. **All remaining programs** — Lower complexity, single-technology

---

## Summary Statistics

| Metric | Value |
|--------|-------|
| Total Programs | 44 |
| Total Lines of Code | 28,614 |
| Online/CICS Programs | 26 |
| Batch Programs | 16 |
| Utility Programs | 2 |
| Programs using IMS DL/I | 8 |
| Programs using DB2 SQL | 5 |
| Programs using MQ | 4 |
| Programs using ASM calls | 2 |
| Multi-technology programs (≥2) | 12 |
| Copybooks (logic) | 41 |
| Copybooks (BMS-generated) | 21 |
| JCL Jobs | 46 |
| VSAM Datasets (CSD-defined) | 8 |
| DB2 Tables | 3 |
| IMS Databases | 4 |
| MQ Queues | 4 |
| BMS Map Sources | 21 |
| CICS Transactions | 24 |
