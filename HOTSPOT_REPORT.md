# HOTSPOT REPORT — CardDemo Modernization Priority Analysis

## 1. Top 10 Programs by Lines of Code

| Rank | Program | Lines | Classification |
|------|---------|-------|----------------|
| 1 | **COACTUPC.cbl** | 4,236 | CICS Online |
| 2 | **COCRDUPC.cbl** | 1,560 | CICS Online |
| 3 | **COCRDLIC.cbl** | 1,459 | CICS Online |
| 4 | **COACTVWC.cbl** | 941 | CICS Online |
| 5 | **COCRDSLC.cbl** | 887 | CICS Online |
| 6 | **COTRN02C.cbl** | 783 | CICS Online |
| 7 | **CBTRN02C.cbl** | 731 | Batch |
| 8 | **COTRN00C.cbl** | 699 | CICS Online |
| 9 | **COUSR00C.cbl** | 695 | CICS Online |
| 10 | **CBACT04C.cbl** | 652 | Batch |

---

## 2. Top 10 Programs by Copybooks Referenced

| Rank | Program | Copybooks | Key Copybooks |
|------|---------|-----------|---------------|
| 1 | **COACTUPC.cbl** | 56 | CSUTLDWY, CSLKPCDY, CSSETATY, CSUTLDPY, COCOM01Y, CVACT01Y, CVCUS01Y, CVACT03Y + 12 BMS maps |
| 2 | **COACTVWC.cbl** | 15 | CVCRD01Y, COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| 3 | **COCRDSLC.cbl** | 15 | CVCRD01Y, COCOM01Y, CVACT02Y, CVCUS01Y, CSMSG02Y, CSSTRPFY |
| 4 | **COCRDUPC.cbl** | 15 | CVCRD01Y, COCOM01Y, CVACT02Y, CVCUS01Y, CSMSG02Y, CSSTRPFY |
| 5 | **COCRDLIC.cbl** | 13 | CVCRD01Y, COCOM01Y, CVACT02Y, CSUSR01Y, CSSTRPFY |
| 6 | **COBIL00C.cbl** | 10 | COCOM01Y, CVACT01Y, CVACT03Y, CVTRA05Y |
| 7 | **COTRN02C.cbl** | 10 | COCOM01Y, CVTRA05Y, CVACT01Y, CVACT03Y |
| 8 | **COADM01C.cbl** | 9 | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSUSR01Y |
| 9 | **COMEN01C.cbl** | 9 | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSUSR01Y |
| 10 | **COSGN00C.cbl** | 9 | COCOM01Y, COSGN00, COTTL01Y, CSUSR01Y |

---

## 3. Top 10 Programs by I/O Operations

Counts include: READ, WRITE, REWRITE, OPEN, CLOSE, STARTBR, READNEXT, READPREV, ENDBR, DELETE (batch file + EXEC CICS).

| Rank | Program | I/O Ops | Details |
|------|---------|---------|---------|
| 1 | **COACTUPC.cbl** | 110 | 5× READ, 2× REWRITE, extensive SEND/RECEIVE maps, multiple file lookups |
| 2 | **CBTRN03C.cbl** | 73 | 5 files: TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM; writes report |
| 3 | **CBTRN01C.cbl** | 53 | 6 files opened; validates daily transactions against all master files |
| 4 | **CBTRN02C.cbl** | 53 | 6 files; full transaction posting with reject handling |
| 5 | **CBEXPORT.cbl** | 47 | 5 input files + 1 output; multi-entity export |
| 6 | **COCRDLIC.cbl** | 43 | Paginated card browsing: STARTBR/READNEXT/READPREV/ENDBR |
| 7 | **CBACT04C.cbl** | 42 | 5 files; interest calculation with cross-file lookups |
| 8 | **CBIMPORT.cbl** | 40 | 1 input + 6 outputs; multi-entity import |
| 9 | **CBACT01C.cbl** | 36 | 1 input + 3 outputs with date formatting |
| 10 | **COTRN02C.cbl** | 27 | Transaction add with STARTBR/READPREV for ID generation |

---

## 4. Top 10 Programs by Business Logic Density

Measured by IF statements + EVALUATE statements (branching complexity).

| Rank | Program | IF Stmts | EVALUATE Stmts | Total Branches | Logic Density (branches/100 LOC) |
|------|---------|----------|----------------|----------------|----------------------------------|
| 1 | **COACTUPC.cbl** | 164 | 10 | 174 | 4.1 |
| 2 | **COCRDUPC.cbl** | 72 | 16 | 88 | 5.6 |
| 3 | **COCRDLIC.cbl** | 59 | 18 | 77 | 5.3 |
| 4 | **CBTRN02C.cbl** | 48 | 0 | 48 | 6.6 |
| 5 | **CBACT04C.cbl** | 43 | 0 | 43 | 6.6 |
| 6 | **CBTRN03C.cbl** | 38 | 4 | 42 | 6.5 |
| 7 | **COCRDSLC.cbl** | 33 | 8 | 41 | 4.6 |
| 8 | **CBTRN01C.cbl** | 33 | 0 | 33 | 6.7 |
| 9 | **COTRN00C.cbl** | 26 | 8 | 34 | 4.9 |
| 10 | **COACTVWC.cbl** | 28 | 10 | 38 | 4.0 |

---

## 5. Top 10 Programs by Inter-Program Dependencies

Counts references from other programs and JCL (called_by), outgoing XCTL/LINK/CALL, and CICS file connections.

| Rank | Program | Referenced By | Outgoing Transfers | Connected Files | Total Dependencies |
|------|---------|---------------|--------------------|-----------------|--------------------|
| 1 | **COSGN00C.cbl** | 13 | 2 (XCTL) | USRSEC | 16 |
| 2 | **COMEN01C.cbl** | 12 | 1 (XCTL) | USRSEC | 14 |
| 3 | **COADM01C.cbl** | 6 | 0 | USRSEC | 7 |
| 4 | **COCRDLIC.cbl** | 5 | 3 (XCTL) | CARDDAT | 9 |
| 5 | **COCRDSLC.cbl** | 5 | 1 (XCTL) | CARDDAT | 7 |
| 6 | **COCRDUPC.cbl** | 4 | 1 (XCTL) | CARDDAT | 6 |
| 7 | **CSUTLDTC.cbl** | 3 | 0 | _(none)_ | 3 |
| 8 | **COTRN00C.cbl** | 3 | 0 | TRANSACT | 4 |
| 9 | **COACTUPC.cbl** | 2 | 1 (XCTL) | ACCTDAT, CUSTDAT, CXACAIX | 7 |
| 10 | **CBTRN02C.cbl** | 2 | 0 | 6 VSAM files | 8 |

---

## 6. Composite Hotspot Score

Weighted composite score: **LOC (20%) + Copybooks (15%) + I/O (20%) + Logic Density (25%) + Dependencies (20%)**

Each metric is normalized to 0–10 scale relative to the max in that category.

| Rank | Program | LOC Score | Copy Score | I/O Score | Logic Score | Dep Score | **Composite** | Type |
|------|---------|-----------|------------|-----------|-------------|-----------|---------------|------|
| **1** | **COACTUPC.cbl** | 10.0 | 10.0 | 10.0 | 10.0 | 4.4 | **9.2** | CICS |
| **2** | **COCRDUPC.cbl** | 3.7 | 2.7 | 1.8 | 5.1 | 3.8 | **3.5** | CICS |
| **3** | **COCRDLIC.cbl** | 3.4 | 2.3 | 3.9 | 4.4 | 5.6 | **3.9** | CICS |
| **4** | **CBTRN02C.cbl** | 1.7 | 0.9 | 4.8 | 2.8 | 5.0 | **3.0** | Batch |
| **5** | **CBACT04C.cbl** | 1.5 | 0.9 | 3.8 | 2.5 | 1.3 | **2.1** | Batch |
| **6** | **CBTRN03C.cbl** | 1.5 | 0.9 | 6.6 | 2.4 | 1.3 | **2.6** | Batch |
| **7** | **COACTVWC.cbl** | 2.2 | 2.7 | 1.7 | 2.2 | 1.3 | **2.0** | CICS |
| **8** | **COCRDSLC.cbl** | 2.1 | 2.7 | 1.2 | 2.4 | 4.4 | **2.5** | CICS |
| **9** | **COTRN02C.cbl** | 1.8 | 1.8 | 2.5 | 1.6 | 0.6 | **1.6** | CICS |
| **10** | **CBEXPORT.cbl** | 1.4 | 1.1 | 4.3 | 0.9 | 1.3 | **1.7** | Batch |

---

## 7. Modernization Recommendations

### Tier 1 — Modernize First (Highest Impact + Highest Complexity)

#### 1. COACTUPC.cbl — Account Update
- **Why first:** At 4,236 lines it is the single largest and most complex program. It contains 56 COPY statements (including date validation, phone/state/ZIP lookup tables, and field-attribute macros), 174 branching statements, and touches 3 VSAM files with read/rewrite operations. It is a central hub for account and customer management.
- **Risk:** High — extensive validation logic (date, phone area code, state code, ZIP prefix) that must be preserved exactly. Embedded CICS pseudo-conversational flow with BMS map interaction.
- **Approach:** Decompose into domain services: AccountService, CustomerService, ValidationService. Extract CSLKPCDY lookup tables into reference data. Date validation (CSUTLDPY/CSUTLDTC) becomes a shared DateValidator utility.

#### 2. CBTRN02C.cbl — Transaction Posting
- **Why early:** Core business process — the daily batch posting job. Writes to the transaction master, updates account balances, maintains category balances, and handles rejects. Touches 6 VSAM files. Any error here affects financial integrity.
- **Risk:** Medium-high — transactional consistency across multiple files must be maintained.
- **Approach:** Map to a transactional service with database transactions replacing multi-file VSAM I-O. The reject handling becomes exception management.

#### 3. CBACT04C.cbl — Interest Calculation
- **Why early:** Business-critical financial calculation that reads disclosure group rates and computes interest per category balance. Core revenue logic.
- **Risk:** Medium — precision of decimal arithmetic (PIC S9(10)V99) must match exactly.
- **Approach:** Convert to a scheduled batch service with BigDecimal arithmetic. Disclosure group rates become a configuration table.

### Tier 2 — Modernize Second (High Business Value)

#### 4. COCRDLIC.cbl + COCRDSLC.cbl + COCRDUPC.cbl (Card Management Suite)
- **Why together:** These three programs form a cohesive card management workflow (list → view → update). They share the same copybooks and access the same CARDDAT file. Modernizing as a unit reduces integration risk.
- **Combined size:** 3,906 lines across 3 programs.
- **Approach:** Create a CardService with REST endpoints for list/view/update. Paginated browsing (STARTBR/READNEXT/READPREV) maps to cursor-based pagination.

#### 5. COTRN00C.cbl + COTRN01C.cbl + COTRN02C.cbl (Transaction Management Suite)
- **Why together:** Transaction list/view/add workflow. Similar pagination patterns to card management.
- **Combined size:** 1,812 lines across 3 programs.
- **Approach:** Create a TransactionService. Transaction ID auto-generation (READPREV from end of file) becomes a sequence generator.

#### 6. CBTRN03C.cbl — Transaction Reporting
- **Why here:** Report generation is a high-value batch function. Enriches transactions with type/category descriptions and produces formatted reports.
- **Approach:** Replace with a reporting service using a SQL query joining transaction, type, and category tables. Output to HTML/PDF instead of fixed-width text.

### Tier 3 — Modernize Third (Lower Risk, Clear Patterns)

#### 7. COBIL00C.cbl — Bill Payment
- 572 lines; well-defined business logic (pay balance in full, create transaction).
- Straightforward conversion to a PaymentService.

#### 8. COUSR00C–03C.cbl (User Management Suite)
- 4 programs (1,767 lines total) with identical CRUD pattern on USRSEC file.
- Maps directly to a UserService with standard CRUD operations. Authentication should migrate to a proper identity provider.

#### 9. CBEXPORT.cbl + CBIMPORT.cbl (Data Migration)
- Export/import pair with CVEXPORT packed-format records.
- Convert to standard data exchange formats (JSON/CSV). Good candidate for an ETL pipeline.

#### 10. COACTVWC.cbl — Account View
- Read-only view program. Lower risk since no writes.
- Can share the AccountService from COACTUPC modernization.

### Tier 4 — Modernize Last (Utility / Infrastructure)

- **COSGN00C.cbl** — Replace with modern authentication (OAuth2/JWT)
- **COADM01C.cbl + COMEN01C.cbl** — Menu navigation replaced by web UI routing
- **CORPT00C.cbl** — Report submission replaced by reporting framework
- **CBACT01–03C.cbl, CBCUS01C.cbl** — Simple data dump utilities; may not need modernization at all
- **CSUTLDTC.cbl, COBSWAIT.cbl** — Utility programs; replace with standard library functions

### Key Modernization Principles

1. **Start with COACTUPC** — it is the integration point for account/customer data and contains the most complex validation rules. Getting it right establishes patterns for all other programs.
2. **Batch before online for financial processes** — CBTRN02C and CBACT04C handle money; modernize and validate these early.
3. **Modernize related programs together** — the card suite (COCRDLIC/COCRDSLC/COCRDUPC) and transaction suite (COTRN00C/01C/02C) share copybooks and file access patterns.
4. **Extract shared utilities first** — date validation (CSUTLDPY/CSUTLDTC), lookup tables (CSLKPCDY), and commarea (COCOM01Y) become shared Java services/libraries that all other programs depend on.
5. **Replace VSAM with RDBMS** — all 10 VSAM KSDS files map cleanly to relational tables with the keys already defined. The cross-reference pattern (CVACT03Y) becomes a standard join table.
