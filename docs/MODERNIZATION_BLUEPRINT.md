# Modernization Blueprint — CardDemo COBOL Estate

## Overview

This blueprint evaluates four modernization strategies for each major functional area of the CardDemo credit card management system. For each area, we recommend the optimal approach based on business value, technical complexity, risk, and cost.

### Strategy Definitions

| Strategy | Description | Best When |
|----------|-------------|-----------|
| **Strangler Pattern** | Wrap existing COBOL with APIs; incrementally route traffic to new implementations while the legacy system continues to operate | You need zero-downtime migration and the system has well-defined entry points |
| **Replatform** | Keep COBOL source, move from z/OS to a cloud-compatible COBOL runtime (e.g., AWS M2, Micro Focus on Linux) | Business logic is stable, ROI of rewriting is low, and the goal is infrastructure cost reduction |
| **Refactor** | Restructure COBOL for maintainability — modularize monolithic paragraphs, extract shared logic, improve naming — without changing language | Codebase must remain COBOL for regulatory or staffing reasons, but quality is a bottleneck |
| **Rewrite** | Translate to a modern language (Java/Kotlin) with equivalent business logic, backed by modern data stores (RDBMS, messaging) | Long-term maintainability, talent availability, and ecosystem integration outweigh short-term migration cost |

---

## 1. Transaction Processing (Batch Posting Pipeline)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| CBTRN02C | 731 | Post daily transactions — validation, category balance updates, account balance updates, reject handling |
| CBTRN01C | 600 | Pre-validation and cross-reference lookup (simpler posting variant) |
| CBTRN03C | 550 | Transaction detail report generation |
| COBSWAIT | 50 | Timer utility |

### JCL Pipeline

`CLOSEFIL → POSTTRAN (CBTRN02C) → INTCALC (CBACT04C) → TRANREPT (CBTRN03C) → OPENFIL`

### Data Files

DALYTRAN (input), TRANFILE/TRANSACT, TCATBALF, ACCTFILE, XREFFILE, DALYREJS, TRANREPT (output)

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐⭐ | Could wrap with a REST/event API, but the batch pipeline has no natural HTTP entry point — would require synthetic request mapping |
| Replatform | ⭐⭐ | Works for quick lift-and-shift, but COBOL batch on cloud still requires VSAM-compatible file stores and JCL scheduling, negating much of the cloud benefit |
| Refactor | ⭐⭐ | CBTRN02C is already reasonably modular (731 LOC, 3-deep nesting); limited refactor ROI |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Clear inputs/outputs, pure computation, no UI. Transaction posting maps cleanly to a Spring Batch job with JPA entities. Enables parallel-run validation against the COBOL original. |

### Recommendation: **Rewrite to Java (Spring Batch)**

**Justification:**
- The daily posting pipeline is the financial backbone of the system — getting it right in Java first enables rigorous parallel-run testing.
- CBTRN02C has well-defined file I/O contracts: reads DALYTRAN, writes to TRANFILE/TCATBALF/ACCTFILE/DALYREJS.
- Spring Batch's chunk-oriented processing (reader → processor → writer) maps naturally to COBOL's sequential-read-process-write paradigm.
- At 731 LOC with 5 copybooks, CBTRN02C is among the smaller programs — manageable rewrite scope.
- Reject handling (DALYREJS) translates to Spring Batch skip/retry policies.

---

## 2. Interest Calculation Engine

### Programs

| Program | LOC | Role |
|---------|-----|------|
| CBACT04C | 650 | Compute interest charges based on category balances and disclosure group rates |

### Data Files

TCATBALF (category balances), DISCGRP (interest rates), XREFFILE, ACCTFILE, TRANSACT

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐ | No external-facing interface to wrap |
| Replatform | ⭐⭐ | Interest logic is business-critical; cloud COBOL doesn't improve testability or auditability |
| Refactor | ⭐⭐ | Already a single-purpose calculator; refactoring COBOL doesn't add much value |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Pure algorithmic computation (rate × balance × days). Ideal for unit-testable Java service with BigDecimal precision. |

### Recommendation: **Rewrite to Java (Spring Batch / Domain Service)**

**Justification:**
- Interest calculation is pure business logic: `interest = balance × rate × (days / 365)` per category.
- The disclosure group lookup (DISCGRP → rate by account group + transaction type + category) maps directly to a JPA repository query.
- A rewrite allows comprehensive unit testing of interest math, which is currently untestable in the COBOL batch environment.
- Financial regulators benefit from auditable, traceable Java code over COBOL paragraphs.
- CBACT04C reads TCATBALF and DISCGRP, rewrites ACCTFILE, writes to TRANSACT — all can become database operations.

---

## 3. Account Management (Online CICS)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COACTUPC | 4,236 | Account update — field editing, validation, multi-file writes |
| COACTVWC | 941 | Account view — display account, card, customer details |
| COBIL00C | 800 | Bill payment — pay balance, generate payment transaction |

### Data Files

ACCTFILE, CARDFILE (via AIX), CUSTFILE, XREFFILE, TRANSACT

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Pattern** | ⭐⭐⭐⭐⭐ | **Recommended.** Expose account operations as REST APIs behind an API gateway. The existing CICS programs continue to serve 3270 terminals while the new API serves web/mobile clients. Migrate traffic incrementally. |
| Replatform | ⭐⭐⭐ | CICS emulators (Micro Focus, UniKix) on cloud work, but you remain locked to 3270 UIs and CICS programming model |
| Refactor | ⭐⭐ | COACTUPC at 4,236 LOC needs decomposition, but refactoring COBOL doesn't solve the UI modernization need |
| Rewrite | ⭐⭐⭐⭐ | Eventually the right move, but COACTUPC is the largest and most complex program — rewrite risk is highest here |

### Recommendation: **Strangler Pattern → Progressive Rewrite**

**Justification:**
- COACTUPC is the most complex program in the estate (4,236 LOC, 56 copybooks, 4-deep nesting). A big-bang rewrite carries high risk.
- The Strangler approach lets us wrap account operations (view, update, bill-pay) with REST APIs immediately, routing web/mobile traffic to new services while 3270 users continue on CICS.
- Start with the simpler COACTVWC (read-only, 941 LOC) as the first strangled endpoint.
- COACTUPC's embedded CSLKPCDY validation (1,318 lines of area codes, state codes, ZIP prefixes) should be extracted to a Validation Reference Service early — this is reusable across all services.
- COBIL00C (bill payment) is a strong early candidate because it has a clear transactional boundary: read balance → debit → write transaction.

---

## 4. Credit Card Management (Online CICS)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COCRDLIC | 1,459 | Card list — paginated browse with forward/backward paging |
| COCRDSLC | 800 | Card detail view |
| COCRDUPC | 1,560 | Card update — modify card details (status, expiration, name) |

### Data Files

CARDFILE (KSDS + AIX), ACCTFILE, CUSTFILE, XREFFILE

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Pattern** | ⭐⭐⭐⭐⭐ | **Recommended.** Same rationale as Account Management — CICS screens can be fronted with REST APIs. |
| Replatform | ⭐⭐⭐ | Works but retains 3270 UI debt |
| Refactor | ⭐⭐ | Card programs follow identical patterns to account programs; refactoring one refactors neither |
| Rewrite | ⭐⭐⭐⭐ | These programs follow template patterns — once the Account Service rewrite is done, Card Service is a "copy-adapt" exercise |

### Recommendation: **Strangler Pattern → Template-Based Rewrite**

**Justification:**
- Card programs share the same CICS UI patterns (list/view/update) as account programs. Once the Account Service establishes the template, Card Service can be cloned and adapted.
- COCRDLIC and COCRDUPC are the 4th and 5th most complex programs; they benefit from the Strangler approach's incremental risk reduction.
- Card data has a clear entity boundary (CARDFILE + XREFFILE) that maps to a Card Service domain.
- The AIX (Alternate Index) on CARDFILE — allowing lookup by account ID — translates to a simple secondary index in a relational database.

---

## 5. Transaction Inquiry & Reporting (Online + Batch)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COTRN00C | 700 | Transaction list — paginated browse |
| COTRN01C | 500 | Transaction view — single record display |
| COTRN02C | 600 | Transaction add — create new transaction with validation |
| CORPT00C | 400 | Report submission — submit batch jobs via CICS TD queue |
| CBTRN03C | 550 | Transaction report — formatted detail report |

### Data Files

TRANSACT (KSDS + AIX), ACCTFILE, XREFFILE, TRANTYPE, TRANCATG

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Pattern** | ⭐⭐⭐⭐ | Online inquiry screens are good candidates for REST API wrapping |
| Replatform | ⭐⭐ | Minimal benefit — these are simple programs |
| Refactor | ⭐⭐ | Programs are already reasonably clean |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Read-only inquiry + simple CRUD + reporting. Low-risk rewrite targets. |

### Recommendation: **Rewrite (Online) + Rewrite (Batch Reporting)**

**Justification:**
- Transaction inquiry (list/view) is read-only — zero risk of data corruption during migration.
- COTRN02C (add) has a known asymmetry: it writes to TRANSACT but does NOT update TCATBALF or account balances (unlike batch CBTRN02C). The rewrite is an opportunity to unify online and batch posting logic.
- CORPT00C (report submission) is a thin shell that writes to a CICS TD queue — translates to a message publish in the new architecture.
- CBTRN03C (reporting) is a straightforward read-format-write pattern → modern reporting framework (JasperReports, PDF generation).

---

## 6. Statement Generation

### Programs

| Program | LOC | Role |
|---------|-----|------|
| CBSTM03A | 924 | Statement master — orchestrates statement generation |
| CBSTM03B | 600 | Statement sub-program — reads files, formats output |

### JCL Pipeline

`CREASTMT → SORT → CBSTM03A (calls CBSTM03B) → TXT2PDF1`

### Data Files

TRNXFILE (sorted transactions), XREFFILE, CUSTFILE, ACCTFILE → STMTFILE (text), HTMLFILE (HTML)

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐ | Batch process — no real-time interface to strangle |
| **Replatform** | ⭐⭐⭐ | Lift COBOL to cloud runtime for quick win while planning rewrite |
| Refactor | ⭐⭐ | High I/O (120 ops) but logic is straightforward ETL |
| **Rewrite** | ⭐⭐⭐⭐ | **Recommended.** Modern template engines (Thymeleaf, iText) dramatically simplify PDF/HTML generation vs. COBOL WRITE statements. |

### Recommendation: **Rewrite to Java (Spring Batch + Template Engine)**

**Justification:**
- CBSTM03A has the highest I/O count in the estate (120 operations) — this is because COBOL requires explicit OPEN/READ/WRITE for every operation. A JPA-based approach reduces this to repository calls.
- Statement generation is an ideal ETL workload for Spring Batch: read sorted transactions → join with customer/account data → render templates → output PDF/HTML.
- Modern PDF libraries (iText, Apache PDFBox) and HTML template engines (Thymeleaf) replace hundreds of lines of COBOL formatting logic.
- Statement generation has no real-time dependency — it can be migrated independently with parallel-run validation.

---

## 7. User Security & Authentication

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COSGN00C | 500 | Sign-on screen — authenticate against USRSEC |
| COUSR00C | 600 | User list — browse USRSEC with paging |
| COUSR01C | 400 | User add — create user in USRSEC |
| COUSR02C | 400 | User update — modify user record |
| COUSR03C | 300 | User delete — remove user |

### Data Files

USRSEC (KSDS) — 80-byte records with cleartext passwords

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐ | Authentication is all-or-nothing; partial strangling creates security gaps |
| Replatform | ⭐ | Cleartext passwords on any platform are unacceptable |
| Refactor | ⭐ | Cannot fix fundamental security flaws by refactoring COBOL |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace entirely with modern IAM (Spring Security + OAuth2/JWT). |

### Recommendation: **Rewrite — Replace with Modern IAM**

**Justification:**
- USRSEC stores passwords in cleartext (PIC X(08)) — this is a critical security vulnerability that no amount of refactoring or replatforming can fix.
- The user model is simple: 5 fields (ID, first name, last name, password, type='A'/'U'). This maps trivially to a Spring Security UserDetailsService.
- Modern IAM (JWT tokens, bcrypt hashing, role-based access) replaces COSGN00C's CICS-session authentication.
- The admin CRUD screens (COUSR00C–03C) are simple list/add/update/delete — standard Spring MVC REST controllers.
- This should be one of the first services built because every other service depends on authentication.

---

## 8. Card Authorization Sub-Application (IMS/DB2/MQ)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COPAUA0C | 1,026 | Real-time authorization decision via MQ request/reply |
| COPAUS0C | 1,032 | Authorization summary view (paginated) |
| COPAUS1C | 500 | Authorization detail view |
| COPAUS2C | 200 | Mark fraud flag |
| CBPAUP0C | 300 | Purge expired authorizations (batch) |
| PAUDBLOD | 200 | IMS database load |
| PAUDBUNL | 200 | IMS database unload |
| DBUNLDGS | 200 | IMS generic unload |

### Data Stores

IMS DB (hierarchical), DB2 (AUTHFRDS fraud table), MQ (request/reply/error queues)

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐⭐ | Could intercept MQ messages with a modern consumer, but the IMS dependency makes this fragile |
| Replatform | ⭐⭐ | IMS on cloud is possible (AWS M2) but expensive and limits future flexibility |
| Refactor | ⭐ | IMS DL/I calls cannot be meaningfully refactored without changing the data store |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Replace IMS with PostgreSQL, MQ with Kafka/SQS. Authorization decision logic becomes a stateless microservice. |

### Recommendation: **Rewrite to Java (Event-Driven Microservice)**

**Justification:**
- The authorization sub-app crosses three middleware boundaries (CICS + MQ + IMS). Replatforming requires maintaining all three on cloud — expensive and complex.
- Authorization logic (approve/decline based on account status, credit limit, fraud flags) is algorithmic and testable — ideal for a microservice.
- MQ request/reply maps to modern request-response patterns (REST, gRPC) or event-driven patterns (Kafka, SQS).
- IMS hierarchical data (pending authorizations) maps to a simple relational schema (authorization_requests table with FK to accounts).
- Fraud detection (AUTHFRDS DB2 table) becomes a query against the same relational database.
- The purge batch job (CBPAUP0C) becomes a scheduled task or database TTL policy.

---

## 9. Transaction Type Management (DB2 Sub-Application)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COTRTLIC | 2,098 | List transaction types — DB2 cursor browse with delete |
| COTRTUPC | 1,702 | Transaction type update — DB2 CRUD with optimistic locking |
| COBTUPDT | 300 | Batch transaction type update |

### Data Stores

DB2 tables (transaction type reference data)

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐⭐ | Could wrap DB2 access with REST, but the CICS screens would still need DB2 |
| Replatform | ⭐⭐⭐ | DB2 on cloud (RDS, Aurora) is straightforward, but CICS screens remain |
| Refactor | ⭐⭐ | SQL is already close to modern — refactoring COBOL around it adds little |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** These programs already use SQL — the DB2 queries map directly to JPA repositories. Easiest rewrite in the estate. |

### Recommendation: **Rewrite to Java (Spring Data JPA)**

**Justification:**
- COTRTLIC and COTRTUPC already use embedded SQL (SELECT, UPDATE, INSERT, DELETE with cursors). These are the closest to modern patterns in the entire COBOL estate.
- DB2 cursor-based paging translates directly to Spring Data's `Pageable` interface.
- Optimistic locking in COTRTUPC maps to JPA's `@Version` annotation.
- The DB2 tables can be migrated to PostgreSQL with minimal schema changes.
- At 2,098 and 1,702 LOC respectively, these are the 2nd and 3rd most complex programs — but the complexity is in CICS screen handling, not business logic. The actual SQL operations are simple CRUD.

---

## 10. Branch Migration (Export/Import)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| CBEXPORT | 600 | Export customer data — read all files, write multi-record export |
| CBIMPORT | 700 | Import from export file — split and validate into target files |

### Data Files

All core VSAM files (read) → EXPFILE (multi-record export) → target VSAM files (write)

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐ | No external interface to wrap |
| **Replatform** | ⭐⭐⭐⭐ | **Short-term option.** If export/import is used during the migration itself, keep it running on a cloud COBOL runtime until migration completes |
| Refactor | ⭐⭐ | Simple sequential processing — little to refactor |
| **Rewrite** | ⭐⭐⭐⭐ | **Long-term recommendation.** Once all data is in relational databases, export/import becomes standard database dump/restore or API-based data sync. |

### Recommendation: **Replatform (Short-Term) → Retire (Long-Term)**

**Justification:**
- Export/Import utilities are used during the migration process itself — they may need to keep running while other components are being rewritten.
- Once all data is migrated to relational databases, the CVEXPORT multi-record format becomes obsolete.
- The short-term strategy is to run CBEXPORT/CBIMPORT on AWS M2 or GnuCOBOL during the migration cutover.
- Post-migration, data portability is handled by database-native tools (pg_dump, API exports, ETL pipelines).

---

## 11. MQ Event Processing (VSAM-MQ Sub-Application)

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COACCT01 | 400 | MQ-triggered account data operations |
| CODATE01 | 200 | MQ-triggered date/time operations |

### Data Stores

MQ queues (input/output/error), VSAM files

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐⭐⭐ | Could consume MQ messages from a modern service |
| Replatform | ⭐⭐ | MQ on cloud works but retains COBOL dependency |
| Refactor | ⭐⭐ | Simple programs — limited refactor value |
| **Rewrite** | ⭐⭐⭐⭐⭐ | **Recommended.** Small, well-bounded programs. MQ → Kafka/SQS migration. |

### Recommendation: **Rewrite to Java (Event Consumer)**

**Justification:**
- Both programs are small (<400 LOC) with clear MQ contracts (MQGET → process → MQPUT).
- MQ request-response maps to Kafka consumer/producer or SQS/SNS patterns.
- These can be absorbed into the Account Service as event listeners rather than standalone services.

---

## 12. Utility Programs & Navigation

### Programs

| Program | LOC | Role |
|---------|-----|------|
| COSGN00C | 500 | Sign-on (covered in Security above) |
| COMEN01C | 800 | Main menu — route to sub-functions |
| COADM01C | 600 | Admin menu — route to admin sub-functions |
| CSUTLDTC | 200 | Date utility |
| COBSWAIT | 50 | Wait utility |
| CBACT01C | 500 | Account file reader/printer |
| CBACT02C | 300 | Card file reader/printer |
| CBACT03C | 300 | Xref file reader/printer |
| CBCUS01C | 300 | Customer file reader/printer |

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Pattern | ⭐ | Menu routing disappears when you build a web UI |
| Replatform | ⭐ | No value in keeping CICS menus on cloud |
| Refactor | ⭐ | No value in refactoring programs that will be retired |
| **Retire** | ⭐⭐⭐⭐⭐ | **Recommended.** These are infrastructure artifacts of the 3270/CICS environment. |

### Recommendation: **Retire (Do Not Migrate)**

**Justification:**
- COMEN01C and COADM01C are pure navigation shells — their function is replaced by a web frontend's routing layer (React Router, Angular Router, etc.).
- File reader/printer utilities (CBACT01C–03C, CBCUS01C) are diagnostic tools — replaced by SQL queries or admin dashboards.
- CSUTLDTC (date utility) is replaced by `java.time` APIs.
- COBSWAIT is replaced by `Thread.sleep()` or scheduled executors.

---

## Summary Matrix

| # | Functional Area | Strategy | Target Technology | Complexity | Priority |
|---|----------------|----------|-------------------|------------|----------|
| 1 | Transaction Processing | **Rewrite** | Spring Batch + JPA | Medium | **Phase 1** |
| 2 | Interest Calculation | **Rewrite** | Spring Batch + JPA | Medium | **Phase 1** |
| 3 | Account Management | **Strangler → Rewrite** | Spring Boot REST + JPA | Very High | **Phase 2** |
| 4 | Credit Card Management | **Strangler → Rewrite** | Spring Boot REST + JPA | High | **Phase 2** |
| 5 | Transaction Inquiry | **Rewrite** | Spring Boot REST + JPA | Medium | **Phase 1** |
| 6 | Statement Generation | **Rewrite** | Spring Batch + iText/Thymeleaf | Medium | **Phase 3** |
| 7 | User Security | **Rewrite** | Spring Security + JWT | Low | **Phase 1** |
| 8 | Card Authorization | **Rewrite** | Event-Driven Microservice (Kafka/SQS) | High | **Phase 2** |
| 9 | Transaction Type Mgmt | **Rewrite** | Spring Data JPA | Medium | **Phase 2** |
| 10 | Branch Migration | **Replatform → Retire** | GnuCOBOL/AWS M2 (temp) | Low | **Phase 4** |
| 11 | MQ Event Processing | **Rewrite** | Kafka/SQS Consumer | Low | **Phase 3** |
| 12 | Utilities & Navigation | **Retire** | N/A | None | **Phase 1** |
