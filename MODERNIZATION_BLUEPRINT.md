# CardDemo Modernization Blueprint

## Executive Summary

CardDemo is a credit-card management system comprising **31 COBOL programs** (~20,650 LOC), **30 copybooks**, **17 BMS maps**, **40+ JCL jobs**, and **8 VSAM KSDS datasets**, with extensions for IMS DB, DB2, and MQ integration. This blueprint evaluates four modernization strategies for each functional area and recommends the optimal approach.

### Strategy Definitions

| Strategy | Description |
|----------|-------------|
| **Strangler Pattern** | Wrap existing programs with REST/event APIs; route traffic through a facade; incrementally replace back-end programs while keeping the system live. |
| **Replatform** | Retain COBOL source and re-host on a cloud-compatible runtime (e.g., AWS Mainframe Modernization with Micro Focus / NTT DATA UniKix). No code rewrite. |
| **Refactor** | Restructure the COBOL source for maintainability (modularize paragraphs, extract copybooks, remove dead code) without changing the language. |
| **Rewrite** | Translate to a modern language (Java/Kotlin) with equivalent business logic, backed by a relational database (PostgreSQL/Aurora). |

---

## 1. Authentication & Session Management

**Programs:** COSGN00C (260 LOC)
**Copybooks:** COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y
**Data Stores:** USRSEC (VSAM KSDS)
**CICS Transactions:** CC00

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Low** — credential lookup against USRSEC, type-flag routing (Admin vs. Regular). |
| Data coupling | **Low** — reads only USRSEC; writes COMMAREA for downstream navigation. |
| Team skill availability | High — authentication is a well-understood domain in Java/Spring Security. |
| Risk tolerance | **High** — isolated, stateless check; easy to test in parallel. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐⭐ | Feasible but over-engineered for a simple sign-on screen. |
| Replatform | ⭐⭐ | Works but perpetuates VSAM-based credential storage. |
| Refactor | ⭐ | Little value — code is already compact. |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace with Spring Security + JWT/OAuth2. Eliminates 3270 dependency and USRSEC VSAM file. Enables SSO integration. |

---

## 2. Menu & Navigation

**Programs:** COMEN01C (308 LOC), COADM01C (288 LOC)
**Copybooks:** COMEN02Y, COADM02Y, COCOM01Y, COTTL01Y
**BMS Maps:** COMEN01, COADM01
**CICS Transactions:** CM00, CA00

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Very Low** — menu option arrays and XCTL dispatching. |
| Data coupling | **Minimal** — reads COMMAREA for user type; no direct data-file access. |
| Team skill availability | High — routing/navigation is trivial in any web framework. |
| Risk tolerance | **High** — no business data at risk. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐ | Unnecessary wrapper for static menus. |
| Replatform | ⭐⭐ | Keeps 3270 UI paradigm needlessly. |
| Refactor | ⭐ | Almost no logic to restructure. |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace with a modern web/SPA frontend. Menu definitions move to configuration or a routing table. Delivered as part of the new UI layer. |

---

## 3. Account Management

**Programs:** COACTVWC (941 LOC), COACTUPC (4,236 LOC — largest program)
**Copybooks:** CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CSLKPCDY
**BMS Maps:** COACTVW, COACTUP
**Data Stores:** ACCTDAT, CARDDAT, CCXREF, CXACAIX, CUSTDAT (VSAM)
**CICS Transactions:** CAVW, CAUP

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **High** — COACTUPC at 4,236 LOC contains credit-limit validation, cash-credit-limit checks, balance updates, multi-field numeric parsing (NUMVAL-C), and cross-entity consistency checks across accounts, cards, and customers. |
| Data coupling | **Very High** — reads/writes 5 VSAM datasets; shares ACCTDAT, CARDDAT, and CCXREF with transaction processing, billing, and batch jobs. |
| Team skill availability | Medium — account domain logic is understandable but the COBOL-specific numeric parsing and CICS pseudo-conversational patterns require careful translation. |
| Risk tolerance | **Low** — incorrect account updates could corrupt balances. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ⭐⭐⭐⭐⭐ | **Recommended.** Expose account view/update via REST APIs. The facade reads/writes the same VSAM data initially. Incrementally migrate the back-end from COBOL to Java services. Allows coexistence with batch jobs that still access VSAM. |
| Replatform | ⭐⭐⭐ | Feasible for short-term cloud hosting. |
| Refactor | ⭐⭐ | Could modularize COACTUPC but doesn't address VSAM or 3270 dependency. |
| Rewrite | ⭐⭐⭐ | Desirable long-term but high risk as an initial move given the data coupling. |

---

## 4. Credit Card Management

**Programs:** COCRDLIC (1,459 LOC), COCRDSLC (887 LOC), COCRDUPC (1,560 LOC)
**Copybooks:** CVCRD01Y, CVACT02Y, CVCUS01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y
**BMS Maps:** COCRDLI, COCRDSL, COCRDUP
**Data Stores:** CARDDAT, CARDAIX, CCXREF, CUSTDAT (VSAM)
**CICS Transactions:** CCLI, CCDL, CCUP

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Medium-High** — list/search with browsing (STARTBR/READNEXT), detail view with cross-reference resolution, card update with field-level validation. |
| Data coupling | **High** — shares CARDDAT and CCXREF with account management and transaction processing. CARDAIX alternate index adds complexity. |
| Team skill availability | Medium — VSAM browsing patterns need careful mapping to cursor/paginated queries. |
| Risk tolerance | **Medium** — card data is PCI-sensitive; migration must maintain security controls. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ⭐⭐⭐⭐⭐ | **Recommended.** Wrap with REST APIs for card CRUD. Co-migrate with Account Management since they share CARDDAT and CCXREF. The facade pattern allows PCI-compliant API security (tokenization, encryption) while gradually replacing VSAM. |
| Replatform | ⭐⭐⭐ | Quick-win for compliance but doesn't address PCI modernization. |
| Refactor | ⭐⭐ | Limited value. |
| Rewrite | ⭐⭐⭐ | Good long-term target within the strangler migration. |

---

## 5. Transaction Processing (Online)

**Programs:** COTRN00C (699 LOC), COTRN01C (330 LOC), COTRN02C (783 LOC)
**Copybooks:** CVTRA05Y, CVACT01Y, CVACT03Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y
**BMS Maps:** COTRN00, COTRN01, COTRN02
**Data Stores:** TRANSACT, ACCTDAT, CCXREF (VSAM)
**CICS Transactions:** CT00, CT01, CT02

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Medium** — list/view/add transactions with date validation, numeric conversion (NUMVAL/NUMVAL-C), and STARTBR/READPREV browsing for ID generation. |
| Data coupling | **High** — TRANSACT is shared with daily batch posting (CBTRN01C, CBTRN02C), interest calculation (CBACT04C), and statement generation (CBSTM03A). |
| Team skill availability | Medium — transaction entry is well-understood, but VSAM key generation needs careful mapping. |
| Risk tolerance | **Medium** — transaction data feeds into financial calculations. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ⭐⭐⭐⭐⭐ | **Recommended.** Expose transaction entry/query as REST APIs. The TRANSACT VSAM file is the most heavily shared data store; the strangler facade allows online programs to migrate while batch jobs continue to read the same data. |
| Replatform | ⭐⭐⭐ | Viable interim. |
| Refactor | ⭐⭐ | Moderate value in separating ID-generation logic. |
| Rewrite | ⭐⭐⭐ | Desirable after batch migration stabilizes. |

---

## 6. Bill Payment

**Programs:** COBIL00C (572 LOC)
**Copybooks:** CVACT01Y, CVACT03Y, CVTRA05Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y
**BMS Maps:** COBIL00
**Data Stores:** ACCTDAT, CCXREF, TRANSACT (VSAM)
**CICS Transaction:** CB00

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Medium** — balance-in-full and partial payment logic, REWRITE to update balances, WRITE new transaction records, STARTBR/READPREV for sequencing. |
| Data coupling | **High** — reads/writes ACCTDAT and TRANSACT; tightly coupled with account and transaction domains. |
| Team skill availability | High — payment logic maps cleanly to a service. |
| Risk tolerance | **Low** — payment errors directly impact customer balances. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ⭐⭐⭐⭐⭐ | **Recommended.** Expose as a Payment API behind the same facade as Account and Transaction services. Shares the same data stores, so co-migration minimizes dual-write risks. |
| Replatform | ⭐⭐⭐ | Acceptable short-term. |
| Refactor | ⭐⭐ | Minor gains. |
| Rewrite | ⭐⭐⭐ | Good target after Account/Transaction services stabilize. |

---

## 7. Reporting (Online Submission + Batch Generation)

**Programs:** CORPT00C (649 LOC — online), CBTRN03C (649 LOC — batch), CBSTM03A (924 LOC — batch), CBSTM03B (230 LOC — batch subroutine)
**Copybooks:** CVTRA05Y, CVTRA03Y, CVTRA04Y, CVTRA07Y, CVACT03Y, COSTM01, CUSTREC, CVACT01Y, COCOM01Y
**Data Stores:** TRANSACT, CCXREF, TRANTYPE, TRANCATG (VSAM); output: flat-file reports, HTML statements
**CICS Transaction:** CR00

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Medium** — date-range parameterization, CSUTLDTC date utility calls, transaction-type/category lookups, page-formatted line printing, HTML generation. |
| Data coupling | **Medium** — reads TRANSACT and reference data (TRANTYPE, TRANCATG); read-only, no writes to shared files. |
| Team skill availability | High — report generation is straightforward in Java/Jasper/BIRT. |
| Risk tolerance | **High** — reports are read-only; errors do not corrupt data. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐⭐ | Could wrap report submission but gains are marginal. |
| Replatform | ⭐⭐ | Keeps COBOL report logic needlessly. |
| Refactor | ⭐⭐ | Little benefit. |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace with a modern reporting engine (Jasper Reports / BIRT / custom PDF/HTML generation). Batch report jobs become Spring Batch tasks or scheduled cloud functions. Read-only nature makes this low-risk. |

---

## 8. User Administration (Security)

**Programs:** COUSR00C (695 LOC), COUSR01C (299 LOC), COUSR02C (414 LOC), COUSR03C (359 LOC)
**Copybooks:** CSUSR01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y
**BMS Maps:** COUSR00, COUSR01, COUSR02, COUSR03
**Data Stores:** USRSEC (VSAM KSDS)
**CICS Transactions:** CU00, CU01, CU02, CU03

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Low** — CRUD operations against a single VSAM file (list, add, update, delete users). |
| Data coupling | **Low** — only USRSEC; shared with sign-on (COSGN00C) only. |
| Team skill availability | High — user management is a commodity in modern frameworks. |
| Risk tolerance | **High** — isolated data; easy to validate with parallel run. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐⭐ | Possible but over-complex for a small, isolated domain. |
| Replatform | ⭐⭐ | Perpetuates VSAM user store. |
| Refactor | ⭐ | No meaningful improvement. |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace with Spring Security user management (or integrate with an external IdP like Keycloak/Okta). Migrate USRSEC to a relational user table. Co-deliver with Authentication (Area 1). |

---

## 9. Batch Transaction Posting & Interest Calculation

**Programs:** CBTRN01C (494 LOC), CBTRN02C (731 LOC), CBACT04C (652 LOC)
**Copybooks:** CVTRA06Y, CVTRA05Y, CVTRA01Y, CVTRA02Y, CVACT01Y, CVACT03Y, CVCUS01Y, CVACT02Y
**Data Stores:** DALYTRAN (input), TRANSACT, ACCTDAT, CCXREF, TCATBALF, DISCGRP, DALYREJS (output)
**JCL Jobs:** POSTTRAN, DALYREJS, INTCALC, COMBTRAN

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **High** — daily transaction posting with validation and rejection handling (CBTRN02C), interest calculation by disclosure group and transaction category (CBACT04C), category balance aggregation. This is the core financial engine. |
| Data coupling | **Very High** — touches 7 data files; TRANSACT and ACCTDAT are shared with online programs. TCATBALF and DISCGRP are used only here. |
| Team skill availability | Low-Medium — interest-calculation business rules may contain tribal knowledge (disclosure-group rates, compounding rules). |
| Risk tolerance | **Very Low** — errors in interest calculation or posting have direct financial impact. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ⭐⭐⭐⭐ | **Recommended.** Wrap batch jobs with a cloud-native orchestration layer (AWS Step Functions / Spring Batch). The COBOL programs initially run unchanged on the rehosted environment while new Java equivalents are developed and validated in parallel. Dual-run reconciliation is essential before cutover. |
| Replatform | ⭐⭐⭐⭐ | Strong interim option — keep COBOL on UniKix/Micro Focus while building Java replacements. |
| Refactor | ⭐⭐⭐ | Moderate value in extracting interest-rate logic into separate paragraphs. |
| Rewrite | ⭐⭐⭐ | Long-term target but high risk to rewrite financial calculations without extensive dual-run validation. |

---

## 10. Batch Data Utilities & File Management

**Programs:** CBACT01C (430 LOC), CBACT02C (178 LOC), CBACT03C (178 LOC), CBCUS01C (178 LOC), COBSWAIT (41 LOC)
**Copybooks:** CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CODATECN
**Data Stores:** ACCTDAT, CARDDAT, CCXREF, CUSTDAT (VSAM — read-only)
**JCL Jobs:** READACCT, READCARD, READXREF, READCUST, CLOSEFIL, OPENFIL, WAITSTEP

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Low** — read-and-print utilities; CLOSEFIL/OPENFIL are CICS file control operations; COBSWAIT is a trivial assembler delay wrapper. |
| Data coupling | **Low** — read-only access to shared VSAM files. |
| Team skill availability | High — trivial logic. |
| Risk tolerance | **High** — utility programs with no write operations. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐ | Overkill for utility programs. |
| Replatform | ⭐⭐⭐ | Acceptable — they work on UniKix/Micro Focus as-is. |
| Refactor | ⭐ | No value. |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace with simple Java batch utilities or database scripts. CLOSEFIL/OPENFIL/WAITSTEP become unnecessary once VSAM is replaced with a relational database. Read-and-print utilities become SQL queries. |

---

## 11. Branch Migration (Export/Import)

**Programs:** CBEXPORT (582 LOC), CBIMPORT (487 LOC)
**Copybooks:** CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT
**Data Stores:** CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE (input); EXPFILE (export output)
**JCL Jobs:** CBEXPORT, CBIMPORT

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Medium** — multi-entity export with header/trailer records, record-type marshalling; import with validation, error reporting, and date stamping. |
| Data coupling | **Very High** — reads/writes 5 VSAM data files plus a custom export flat file. Touches nearly every data entity in the system. |
| Team skill availability | Medium — ETL patterns are well-understood but the CVEXPORT record format is proprietary. |
| Risk tolerance | **Medium** — used for branch migrations; errors could cause data loss. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐⭐ | Could wrap with an API but adds complexity for a batch operation. |
| Replatform | ⭐⭐⭐⭐ | **Strong interim.** Keep running on rehosted environment until VSAM is replaced. |
| Refactor | ⭐⭐ | Limited value. |
| **Rewrite** | ⭐⭐⭐⭐ | **Recommended long-term.** Once data stores are migrated to a relational DB, export/import becomes standard ETL (e.g., Spring Batch + CSV/JSON). However, defer until after core data migration is complete. |

---

## 12. Authorization Processing (IMS/DB2/MQ Extension)

**Programs:** CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, PAUDBLOD, PAUDBUNL, DBUNLDGS (8 programs)
**Technologies:** IMS DB (HIDAM), DB2 (fraud table), MQ (request/response queues)
**Data Stores:** IMS HIDAM databases (DBPAUTP0/DBPAUTX0), DB2 AUTHFRDS table, MQ queues

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Very High** — real-time authorization with fraud detection, two-phase commit across IMS and DB2, MQ request/response processing, credit-limit validation, merchant category checks. |
| Data coupling | **Very High** — spans IMS DB, DB2, MQ, and VSAM (for account/customer lookup). Cross-subsystem transactions. |
| Team skill availability | **Low** — IMS DB expertise is rare; MQ integration patterns need specialized knowledge. |
| Risk tolerance | **Very Low** — authorization failures or fraud-detection gaps have immediate financial impact. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace MQ with a modern event bus (Kafka/SQS). Build a new authorization microservice that reads account data from the migrated relational DB. IMS DB content migrates to PostgreSQL/Aurora. The MQ facade routes requests to either old or new path during transition. |
| Replatform | ⭐⭐⭐ | Complex — IMS DB rehosting requires specialized tooling. |
| Refactor | ⭐ | IMS/DB2/MQ coupling makes refactoring alone insufficient. |
| Rewrite | ⭐⭐⭐⭐ | Desirable but should be done behind a strangler facade for safety. |

---

## 13. Transaction Type Management (DB2 Extension)

**Programs:** COTRTLIC, COTRTUPC, COBTUPDT (3 programs)
**Technologies:** DB2 (embedded static SQL), CICS
**Data Stores:** DB2 TRANSACTION_TYPE, TRANSACTION_TYPE_CATEGORY tables
**CICS Transactions:** CTLI, CTTU
**JCL Jobs:** CREADB21, TRANEXTR, MNTTRDB2

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Low-Medium** — CRUD with cursors on DB2 tables; batch extract to VSAM-compatible flat files. |
| Data coupling | **Medium** — DB2 tables feed into VSAM TRANTYPE/TRANCATG files used by batch reporting. |
| Team skill availability | High — DB2 SQL is directly portable to modern relational databases. |
| Risk tolerance | **High** — reference data management; errors are easily correctable. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐⭐ | Possible but small scope. |
| Replatform | ⭐⭐⭐ | DB2 tables can be migrated to cloud DB2/PostgreSQL easily. |
| Refactor | ⭐⭐ | SQL is already clean. |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Straightforward migration of DB2 tables to PostgreSQL/Aurora. COBOL programs become a simple Spring Boot REST service with JPA. The TRANEXTR batch job becomes unnecessary once VSAM is eliminated. |

---

## 14. Account Extractions via MQ (VSAM-MQ Extension)

**Programs:** COACCT01, CODATE01 (2 programs)
**Technologies:** MQ, VSAM, CICS
**Data Stores:** ACCTDAT (VSAM), MQ queues (CARDDEMO.REQUEST.QUEUE, CARDDEMO.RESPONSE.QUEUE)

### Analysis

| Criterion | Assessment |
|-----------|------------|
| Business logic complexity | **Low** — simple request/response MQ patterns for date inquiry and account lookup. |
| Data coupling | **Medium** — reads ACCTDAT; depends on MQ infrastructure. |
| Team skill availability | High — REST APIs are a direct replacement for MQ request/response. |
| Risk tolerance | **High** — read-only operations. |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ⭐⭐⭐ | Possible but the entire module is small. |
| Replatform | ⭐⭐ | Keeps MQ dependency. |
| Refactor | ⭐ | No value. |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace with REST API endpoints on the new Account service. MQ consumers redirect to the new API. The entire module becomes redundant once the Account API is live. |

---

## Strategy Summary

| # | Functional Area | LOC | Strategy | Risk | Priority |
|---|----------------|-----|----------|------|----------|
| 1 | Authentication & Session | 260 | Rewrite | Low | P1 |
| 2 | Menu & Navigation | 596 | Rewrite | Low | P1 |
| 3 | Account Management | 5,177 | Strangler | High | P2 |
| 4 | Credit Card Management | 3,906 | Strangler | Medium | P2 |
| 5 | Transaction Processing (Online) | 1,812 | Strangler | Medium | P2 |
| 6 | Bill Payment | 572 | Strangler | Medium | P3 |
| 7 | Reporting | 2,452 | Rewrite | Low | P3 |
| 8 | User Administration | 1,767 | Rewrite | Low | P1 |
| 9 | Batch Posting & Interest Calc | 1,877 | Strangler | High | P4 |
| 10 | Data Utilities & File Mgmt | 1,005 | Rewrite | Low | P5 |
| 11 | Branch Migration | 1,069 | Rewrite (deferred) | Medium | P5 |
| 12 | Authorization (IMS/DB2/MQ) | ~2,000 | Strangler | Very High | P4 |
| 13 | Txn Type Mgmt (DB2) | ~1,200 | Rewrite | Low | P3 |
| 14 | Account Extractions (MQ) | ~400 | Rewrite | Low | P3 |

### Key Principles

1. **Strangler for high-coupling areas** — Account, Card, Transaction, and Billing share 5+ VSAM datasets; a facade enables coexistence during migration.
2. **Rewrite for isolated, low-complexity areas** — Authentication, User Admin, Menus, Reporting, and Reference Data have low coupling and well-understood logic.
3. **Defer batch financial logic** — Interest calculation and transaction posting carry the highest financial risk; migrate last with extensive dual-run validation.
4. **Eliminate VSAM early in the target architecture** — Once the strangler facade is in place, migrate VSAM → relational DB as the single highest-value infrastructure change.
