# Hotspot Report — Modernization Complexity Analysis

## 1. Lines of Code — Top 10

| Rank | Program | LOC | Classification | Primary Function |
|------|---------|-----|----------------|-----------------|
| 1 | COACTUPC | 4,236 | Online (CICS) | Account update with extensive field validation |
| 2 | COCRDUPC | 1,560 | Online (CICS) | Credit card update |
| 3 | COCRDLIC | 1,459 | Online (CICS) | Credit card listing with pagination |
| 4 | COACTVWC | 941 | Online (CICS) | Account view |
| 5 | CBSTM03A | 924 | Batch | Account statement generation (text + HTML) |
| 6 | COCRDSLC | 887 | Online (CICS) | Credit card detail view |
| 7 | COTRN02C | 783 | Online (CICS) | Add new transaction |
| 8 | CBTRN02C | 731 | Batch | Validate and post daily transactions |
| 9 | COTRN00C | 699 | Online (CICS) | Transaction listing |
| 10 | COUSR00C | 695 | Online (CICS) | User listing |

---

## 2. Copybook References — Top 10

| Rank | Program | Copybook Count | Copybooks |
|------|---------|---------------|-----------|
| 1 | COACTUPC | 12 | CSUTLDWY, CVCRD01Y, CSLKPCDY, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y |
| 2 | COACTVWC | 12 | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| 3 | COCRDSLC | 10 | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 4 | COCRDUPC | 10 | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 5 | COTRN02C | 7 | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y |
| 6 | COCRDLIC | 7 | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y |
| 7 | CBEXPORT | 6 | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 8 | CBIMPORT | 6 | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 9 | CBTRN01C | 6 | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| 10 | CBTRN02C | 5 | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |

---

## 3. I/O Operations Density

Programs ranked by number of distinct dataset interactions (OPEN/READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT/READPREV):

| Rank | Program | Distinct I/O Files | Operations | Classification |
|------|---------|-------------------|------------|----------------|
| 1 | CBTRN02C | 6 | READ, WRITE, REWRITE on DALYTRAN, XREFFILE, ACCTFILE, TCATBALF, TRANFILE, DALYREJS | Batch |
| 2 | CBEXPORT | 6 | READ (5 files), WRITE (1 file) | Batch |
| 3 | CBIMPORT | 7 | READ (1 file), WRITE (6 files) | Batch |
| 4 | CBTRN01C | 6 | READ on DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE; WRITE to TRANFILE | Batch |
| 5 | CBACT04C | 5 | READ on TCATBALF, XREFFILE, DISCGRP; REWRITE ACCTFILE; WRITE TRANSACT | Batch |
| 6 | CBTRN03C | 5 | READ on TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM; WRITE TRANREPT | Batch |
| 7 | COACTUPC | 3 | READ/REWRITE ACCTDAT; READ CUSTDAT, CXACAIX | Online |
| 8 | COACTVWC | 3 | READ on ACCTDAT, CUSTDAT, CXACAIX (with STARTBR/READNEXT) | Online |
| 9 | COTRN02C | 3 | READ/WRITE TRANSACT; READ CCXREF, CXACAIX | Online |
| 10 | COBIL00C | 3 | READ/WRITE TRANSACT, ACCTDAT; READ CXACAIX | Online |

---

## 4. Business Logic Density

Programs ranked by control flow complexity (EVALUATE statements, nested IF depth, validation logic):

| Rank | Program | Complexity Indicators | Description |
|------|---------|----------------------|-------------|
| 1 | COACTUPC | Deep IF nesting (10+ levels), extensive EVALUATE, ~50 field validations | Every account field validated with format/range/business rules; cross-entity lookups |
| 2 | CBTRN02C | Multiple EVALUATE, overlimit checks, expiration validation, balance updates | Core posting logic: validates card, checks limits, updates multiple files atomically |
| 3 | COCRDUPC | Deep IF nesting, card number validation, status transitions | Card update with Luhn-style validation, date checks, status state machine |
| 4 | CBACT04C | Nested loops, interest calculation per category per account | Financial computation: iterates TCATBALF by account, looks up rates, compounds |
| 5 | COCRDLIC | Pagination logic with STARTBR/READNEXT/READPREV, array management | Complex cursor-based paging over VSAM with forward/backward navigation |
| 6 | COTRN02C | Multi-step validation (card exists, account active, amount valid) | Transaction add with cross-reference validation chain |
| 7 | CBTRN03C | Report formatting with break logic, date range filtering | Formatted report with control breaks on account/type/category |
| 8 | CORPT00C | Date parameter validation, TDQ submission logic | Report request processing with flexible date ranges |
| 9 | COTRN00C | Pagination with dynamic scroll, date filtering | Transaction list with cursor management |
| 10 | CBEXPORT | Multi-entity serialization, record type routing | Multiplexes 5 entity types into single sequential export stream |

---

## 5. Inter-Program Dependencies

Programs ranked by total inbound + outbound XCTL/CALL references:

| Rank | Program | Inbound | Outbound | Total | Role |
|------|---------|---------|----------|-------|------|
| 1 | COMEN01C | 3 | 11 | 14 | Central hub — main menu dispatches to all user functions |
| 2 | COADM01C | 1 | 6 | 7 | Admin hub — dispatches to user/DB2 management |
| 3 | COACTUPC | 1 | 3 | 4 | Account update — navigates to card screens and menu |
| 4 | COCRDLIC | 1 | 2 | 3 | Card list — navigates to card view/update |
| 5 | COACTVWC | 1 | 3 | 4 | Account view — navigates to card screens and menu |
| 6 | COUSR00C | 1 | 2 | 3 | User list — navigates to update/delete |
| 7 | COTRN00C | 1 | 1 | 2 | Transaction list — navigates to detail |
| 8 | COSGN00C | 0 | 2 | 2 | Entry point — branches to admin or main menu |
| 9 | CSUTLDTC | 2 (called) | 1 | 3 | Utility — called by CORPT00C and COTRN02C |
| 10 | COPAUS0C | 1 | 2 | 3 | Auth view — navigates to detail/fraud |

---

## 6. Modernization Recommendations

### 6.1 Priority Matrix

| Priority | Program(s) | Rationale |
|----------|-----------|-----------|
| **P1 — Batch First** | CBTRN02C | Core business logic (transaction validation + posting). Clear I/O contracts (6 files). Standalone batch with no UI dependencies. Highest business value — all daily revenue flows through this. |
| **P1 — Batch First** | CBACT04C | Financial computation (interest calculation). Well-bounded: reads rates + balances, writes transactions. Algorithmically complex but deterministic — ideal for unit testing. |
| **P2 — Utility Extraction** | CSUTLDTC | Date validation service called by multiple programs. Small (157 lines), self-contained, and reusable. Natural microservice candidate. |
| **P2 — Utility Extraction** | CBEXPORT / CBIMPORT | Data migration utilities with clear serialization contracts. Good candidates for ETL service extraction. |
| **P3 — Simple CRUD** | COUSR01C, COUSR03C | Simplest online programs (299/359 lines). Single-entity CRUD on USRSEC. Minimal validation, no cross-entity dependencies. Ideal for learning the CICS-to-REST migration pattern. |
| **P3 — Simple CRUD** | COUSR02C | Slightly more complex user update (414 lines) but still single-entity. |
| **P4 — Complex Online** | COCRDLIC, COCRDSLC | Medium complexity (1459/887 lines). Pagination and detail view patterns. Good for establishing list/detail UI component patterns. |
| **P5 — High Complexity** | COACTUPC | Largest program (4236 lines). Extensive field validation, multi-entity interactions. Modernize last — requires all supporting services to exist first. |

### 6.2 Recommended Modernization Sequence

```
Phase 1: Foundation Services
├── CSUTLDTC → DateValidationService (REST)
├── CBTRN02C → TransactionPostingService (batch/event-driven)
└── CBACT04C → InterestCalculationService (batch/scheduled)

Phase 2: Data Migration
├── CBEXPORT → DataExportService
└── CBIMPORT → DataImportService

Phase 3: Simple Online → REST APIs
├── COUSR01C → POST /api/users
├── COUSR03C → DELETE /api/users/{id}
├── COUSR02C → PUT /api/users/{id}
└── COUSR00C → GET /api/users

Phase 4: Medium Online → REST APIs
├── COCRDLIC → GET /api/cards
├── COCRDSLC → GET /api/cards/{num}
├── COCRDUPC → PUT /api/cards/{num}
├── COTRN00C → GET /api/transactions
├── COTRN01C → GET /api/transactions/{id}
└── COTRN02C → POST /api/transactions

Phase 5: Complex Online → REST APIs
├── COACTVWC → GET /api/accounts/{id}
├── COBIL00C → POST /api/payments
├── CORPT00C → POST /api/reports
└── COACTUPC → PUT /api/accounts/{id}
```

### 6.3 Rationale

**Why batch-first?**

1. **Clear I/O contracts** — Batch programs read from defined input files and write to defined output files. This maps cleanly to service interfaces (request/response or event-driven).

2. **No UI dependencies** — Batch programs have no BMS maps, no pseudo-conversational logic, and no terminal handling. They are pure business logic.

3. **Testability** — Input/output are files with known record formats. Test cases can be constructed from sample data without needing CICS infrastructure.

4. **Highest business value** — CBTRN02C processes every daily transaction. CBACT04C computes all interest charges. These are the revenue-critical paths.

5. **Incremental replacement** — A modernized CBTRN02C can initially write the same VSAM files, allowing the online programs to continue unchanged during transition.

**Why defer COACTUPC?**

- At 4,236 lines with 12 copybook dependencies, it is the most coupled program.
- It requires account, customer, card, and cross-reference services to all exist before it can be fully decomposed.
- Its validation logic touches every entity in the system — it is effectively the integration test for the entire modernized backend.
