# CardDemo Cutover Plan

This document details the migration execution plan for converting the CardDemo COBOL/CICS application to Java microservices using the strangler fig pattern.

---

## 1. Migration Sequence

Migration proceeds in four phases ordered by ascending risk. Each phase builds on the services and APIs delivered in prior phases.

### Phase 1 -- Low Risk

**Goal:** Migrate self-contained, read-heavy programs with minimal cross-domain writes.

| Program | Domain | Data Stores | APIs to Create |
|:--------|:-------|:------------|:---------------|
| COUSR00C | User Security | USRSEC (R+B) | `GET /api/v1/users` |
| COUSR01C | User Security | USRSEC (W) | `POST /api/v1/users` |
| COUSR02C | User Security | USRSEC (R+W) | `PUT /api/v1/users/{userId}` |
| COUSR03C | User Security | USRSEC (R+W) | `DELETE /api/v1/users/{userId}` |
| COSGN00C | Auth & Session | USRSEC (R) | `POST /api/v1/auth/login`, `POST /api/v1/auth/logout` |
| COMEN01C | Auth & Session | -- | `GET /api/v1/menu` (navigation) |
| COADM01C | Auth & Session | -- | `GET /api/v1/admin/menu` (navigation) |
| COTRN00C | Transaction Read | TRANSACT (B), CCXREF (R) | `GET /api/v1/transactions` |
| COTRN01C | Transaction Read | TRANSACT (R), CCXREF (R) | `GET /api/v1/transactions/{tranId}` |

**Dependencies on other phases:** None. These programs are entry points or read-only.

**Exit criteria:** All Phase 1 APIs pass integration tests. Strangler proxy routes read traffic to Java services while COBOL handles writes.

### Phase 2 -- Medium Risk

**Goal:** Migrate read-heavy programs with moderate complexity (browse cursors, report generation) and the isolated DB2 extension.

| Program | Domain | Data Stores | APIs to Create |
|:--------|:-------|:------------|:---------------|
| COCRDLIC | Card List | CARDDAT (B), ACCTDAT (R) | `GET /api/v1/cards` |
| COCRDSLC | Card View | CARDDAT (R), CCXREF (R), ACCTDAT (R) | `GET /api/v1/cards/{cardNum}` |
| COACTVWC | Account View | ACCTDAT (R), CARDAIX (R), CCXREF (R), CUSTDAT (R) | `GET /api/v1/accounts/{acctId}` |
| CORPT00C | Reports | TRANSACT (R) | `GET /api/v1/transactions/reports` |
| CBTRN03C | Reports (batch) | TRANSACT (R), CCXREF (R) | `POST /api/v1/transactions/reports/generate` |
| COTRTLIC | Tran Type List | DB2 TRANSACTION_TYPE | `GET /api/v1/transaction-types` |
| COTRTUPC | Tran Type Edit | DB2 TRANSACTION_TYPE | `POST/PUT /api/v1/transaction-types` |
| COBTUPDT | Tran Type Batch | DB2 TRANSACTION_TYPE | Batch job endpoint |

**Dependencies on other phases:** Requires Phase 1 auth services for session management.

**Exit criteria:** Card and account read APIs pass integration tests. Report generation produces identical output to COBOL. DB2 extension fully migrated.

### Phase 3 -- High Risk

**Goal:** Migrate programs with write operations, state machines, and data conversion complexity.

| Program | Domain | Data Stores | APIs to Create |
|:--------|:-------|:------------|:---------------|
| COCRDUPC | Card Update | CARDDAT (R+W), CCXREF (R), ACCTDAT (R), CUSTDAT (R) | `PUT /api/v1/cards/{cardNum}` |
| COTRN02C (online) | Transaction Add | TRANSACT (W), CCXREF (R), CARDDAT (R) | `POST /api/v1/transactions` |
| CBEXPORT | Customer Export | CUSTDAT (R), ACCTDAT (R), CARDDAT (R), CCXREF (R) | `POST /api/v1/customers/export` |
| CBIMPORT | Customer Import | CUSTDAT (W), ACCTDAT (W), CARDDAT (W), CCXREF (W) | `POST /api/v1/customers/import` |

**Dependencies on other phases:** Requires Phase 2 card and account read APIs. COCRDUPC depends on CCXREF lookup service.

**Exit criteria:** Card update state machine passes all 10-state transition tests. Transaction add is idempotent. Export/import round-trip test passes with all COMP/COMP-3 fields validated.

### Phase 4 -- Critical Risk

**Goal:** Migrate the most complex programs requiring rewrites, saga patterns, and financial calculation validation.

| Program | Domain | Data Stores | APIs to Create |
|:--------|:-------|:------------|:---------------|
| COBIL00C | Bill Payment | TRANSACT (W), ACCTDAT (R+W), CCXREF (R) | `POST /api/v1/transactions/bill-payment` |
| CBTRN02C (batch) | Batch Post | TRANSACT, ACCTDAT (R+W), TCATBALF (R+W), DALYTRAN (R), CCXREF (R), TRANTYPE (R), TRANCATG (R) | `POST /api/v1/transactions/batch/post` |
| CBSTM03A/B | Statement Gen | TRANSACT (R), ACCTDAT (R), CUSTDAT (R) | `GET /api/v1/transactions/statements/{acct}` |
| CBACT04C | Interest Calc | ACCTDAT (R+W), TRANSACT (R), TCATBALF (R+W), DISCGRP (R), TRANCATG (R) | `POST /api/v1/accounts/batch/interest` |
| COACTUPC | Account Update | ACCTDAT (R+W), CUSTDAT (R+W), CARDAIX (R), CCXREF (R) | `PUT /api/v1/accounts/{acctId}` |
| COPAUA0C | Auth Request | IMS DB, MQ, ACCTDAT (R), CCXREF (R), CUSTDAT (R) | `POST /api/v1/authorizations` |
| COPAUS0C | Auth Summary | IMS DB, ACCTDAT (R), CCXREF (R) | `GET /api/v1/authorizations` |
| COPAUS1C | Auth Details | IMS DB, DB2 | `GET /api/v1/authorizations/{authId}` |
| COPAUS2C | Auth Sub-prog | IMS DB | Internal service |
| CBPAUP0C | Purge Auth | IMS DB | `POST /api/v1/authorizations/batch/purge` |
| DBUNLDGS | IMS Unload | IMS DB | Batch utility |
| PAUDBLOD | IMS Load | IMS DB | Batch utility |
| PAUDBUNL | IMS Unload | IMS DB | Batch utility |

**Dependencies on other phases:** All prior phases must be complete. Account and Card APIs must support both read and write.

**Exit criteria:** Financial calculations match COBOL output to the penny. Saga patterns for dual/triple-writes have compensating transactions. Statement output matches behavioral spec. Authorization extension data model redesigned from hierarchical to relational.

---

## 2. Pre-Migration Actions by Risk Tier

### CRITICAL Programs -- Document Before Touching Code

#### COACTUPC (Account Update, 4,236 lines)

1. **Extract validation rules:** Catalog all ~50 validation flags and their business rules into a formal specification document.
2. **Decompose dual-entity logic:** Separate Account Update and Customer Update into independent services with clear API boundaries.
3. **Map the 16-state EVALUATE dispatcher:** Document every state transition and the conditions that trigger them.
4. **Write integration tests:** Create tests for each validation path, including the undocumented SSN exclusion rules (0, 666, 900-999).

#### CBSTM03A (Statement Generation)

1. **Do not translate -- rewrite from scratch.** The ALTER/GO TO self-modifying control flow and PSA/TCB/TIOT pointer arithmetic are z/OS-specific and have no Java equivalent.
2. **Capture output format:** Use actual statement output files as the behavioral specification.
3. **Build golden-file tests:** Compare Java output against known-good COBOL statement files field by field.
4. **Document CBSTM03B interface:** Map the sub-program call contract (parameters, return values).

#### CBACT04C (Interest Calculation)

1. **Document the interest formula:** `monthly_interest = category_balance * rate_percentage / 1200`.
2. **Document the default-group fallback:** When file status '23' (record not found) is returned from DISCGRP, the program falls back to a default disclosure group.
3. **Stub out 1400-COMPUTE-FEES:** This section is referenced but incomplete -- define the intended behavior.
4. **Define cycle-reset semantics:** The program destructively zeros `ACCT-CURR-CYC-CREDIT` and `ACCT-CURR-CYC-DEBIT` after calculation.
5. **Document transaction ID generation:** Concatenates PARM-DATE with sequential suffix -- replace with sequence generator.

#### CBTRN02C (Batch Transaction Posting)

1. **Map the triple-write sequence:** Documents writes to TCATBALF + ACCTFILE + TRANSACT with no transactional boundary.
2. **Define saga/compensating transactions:** Design compensating writes for each step in case of failure.
3. **Document the TCATBALF upsert pattern:** Implicit upsert triggered by file status '23' (record not found triggers INSERT instead of UPDATE).
4. **Complete validation logic:** Address the "ADD MORE VALIDATIONS HERE" comment at line 377.
5. **Document overlimit calculation:** Currently undocumented; extract from code and specify.

#### COBIL00C (Bill Payment)

1. **Replace READPREV-based ID generation:** The current pattern reads the last transaction ID and adds 1, creating a race condition under concurrent access. Replace with a database sequence.
2. **Define saga for dual-write:** TRANSACT + ACCTDAT writes happen in a single CICS pseudo-conversation without transactional guarantee.
3. **Document hardcoded values:** Transaction type '02', category 2, merchant ID 999999999.
4. **Document "pay in full" behavior:** Only full-balance payment is supported (no partial payment). Confirm if this is intentional.

### HIGH Programs -- Add Test Harnesses First

#### COCRDUPC (Credit Card Update, 1,560 lines)

1. **Map all 10 states and transitions:** Document the complete state machine including guard conditions.
2. **Test optimistic locking:** Verify concurrent update behavior and ensure data integrity.
3. **Document private COMMAREA append pattern:** The program extends the standard COMMAREA via offset arithmetic -- this pattern is fragile and must be refactored.

#### COTRN02C (Online Transaction Add)

1. **Test CCXREF validation paths:** Ensure all card-to-account lookup scenarios are covered (valid card, expired card, invalid card, suspended card).
2. **Ensure WRITE to TRANSACT is idempotent:** Duplicate submissions should not create duplicate transaction records.
3. **Document transaction ID generation:** Same READPREV pattern as COBIL00C -- replace with sequence generator.

#### CBEXPORT / CBIMPORT (Customer Data Migration)

1. **Validate all COMP/COMP-3 conversions:** The CVEXPORT copybook uses packed decimal fields and REDEFINES for polymorphic record types. Numeric conversion errors could corrupt data.
2. **Build round-trip test:** Export from COBOL, import to Java, re-export from Java, and compare with original.
3. **Test all REDEFINES variants:** Ensure each polymorphic record type is correctly deserialized.

---

## 3. Strangler Fig Implementation

Transaction Processing is extracted first because it has the highest business value, well-defined read/write boundaries, and clear API contracts.

### Phase 1 APIs -- Read-Only (Lowest Risk)

| API | Source Program | VSAM Files | Notes |
|:----|:--------------|:-----------|:------|
| `GET /api/v1/transactions` | COTRN00C | TRANSACT (browse), CCXREF (read) | Pagination via cursor |
| `GET /api/v1/transactions/{tranId}` | COTRN01C | TRANSACT (read), CCXREF (read) | Single record lookup |

**Implementation:** Deploy Java service behind API gateway. Route `/api/v1/transactions` GET requests to Java; all other transaction traffic continues to COBOL via CICS.

### Phase 2 APIs -- Writes (Medium Risk)

| API | Source Program | VSAM Files | Notes |
|:----|:--------------|:-----------|:------|
| `POST /api/v1/transactions` | COTRN02C (online) | TRANSACT (write), CCXREF (read), CARDDAT (read) | Validate card via CCXREF before write |
| `POST /api/v1/transactions/bill-payment` | COBIL00C | TRANSACT (write), ACCTDAT (read+write), CCXREF (read) | Saga pattern for dual-write |

**Implementation:** Add write endpoints to Transaction Service. Consume Account Service API for balance updates (anti-corruption layer). Replace READPREV ID generation with sequence.

### Phase 3 APIs -- Batch (Highest Risk)

| API | Source Program | VSAM Files | Notes |
|:----|:--------------|:-----------|:------|
| `POST /api/v1/transactions/batch/post` | CBTRN02C (batch) | Multiple (see Phase 4) | Spring Batch job; saga for triple-write |
| `GET /api/v1/transactions/reports` | CORPT00C, CBTRN03C | TRANSACT, CCXREF | Report generation |
| `GET /api/v1/transactions/statements/{acct}` | CBSTM03A/B | TRANSACT, ACCTDAT, CUSTDAT | Rewrite from scratch |

**Implementation:** Spring Batch for POSTTRAN replacement. Statement generator is a full rewrite using behavioral spec.

### Anti-Corruption Layer -- Consumed APIs

| Service | API | Used By | Purpose |
|:--------|:----|:--------|:--------|
| Account Service | `GET /api/v1/accounts/{acctId}` | COBIL00C, CBTRN02C, CBSTM03A | Retrieve account for validation/display |
| Account Service | `PUT /api/v1/accounts/{acctId}` | COBIL00C, CBTRN02C | Update balances |
| Account Service | `GET /api/v1/accounts/by-card/{cardNum}` | COTRN00-02C, COBIL00C | Card-to-account lookup (replaces CCXREF read) |
| Customer Service | `GET /api/v1/customers/{custId}` | CBSTM03A | Customer data for statement headers |

---

## 4. Batch Cycle Cutover

### Current JCL Job Chain

The following 13 core batch jobs execute in sequence as the daily batch cycle (from `README.md` lines 296-327):

| Step | Job | Program | Purpose |
|:----:|:----|:--------|:--------|
| 1 | CLOSEFIL | IEFBR14 | Close VSAM files in CICS |
| 2 | COMBTRAN | SORT | Combine system transactions with daily ones |
| 3 | POSTTRAN | CBTRN02C | Core transaction posting |
| 4 | INTCALC | CBACT04C | Interest calculation |
| 5 | CREASTMT | CBSTM03A | Statement generation |
| 6 | TRANREPT | CBTRN03C | Transaction report |
| 7 | TRANBKP | IDCAMS | Backup transaction master |
| 8 | ACCTFILE | IDCAMS | Refresh account master |
| 9 | CARDFILE | IDCAMS | Refresh card master |
| 10 | CUSTFILE | IDCAMS | Refresh customer master |
| 11 | XREFFILE | IDCAMS | Refresh cross-reference |
| 12 | TRANIDX | IDCAMS | Define AIX for transaction file |
| 13 | OPENFIL | IEFBR14 | Open files in CICS |

### Target Scheduler

Replace JCL with **Spring Batch** jobs orchestrated by **Spring Cloud Data Flow** or **Apache Airflow**.

**Execution DAG:**

```
CLOSEFIL ──> COMBTRAN ──> POSTTRAN ──> INTCALC ──> CREASTMT ──> TRANREPT ──> OPENFIL
                                                                    │
                                                                    v
                                                              (data refreshes)
                                                            TRANBKP, ACCTFILE,
                                                            CARDFILE, CUSTFILE,
                                                            XREFFILE, TRANIDX
```

Each job is implemented as a Spring Batch `Job` with explicit step dependencies enforced by the orchestrator.

### Dual-Run Validation Strategy

During the parallel-run period:

1. **Run both COBOL and Java batch jobs** against the same input data (DALYTRAN feed).
2. **Compare outputs field-by-field** using the test harness (`test-harness/` directory) for TRANSACT, ACCTDAT, TCATBALF, and statement files.
3. **Track discrepancies** in a validation report with per-record match/mismatch status.
4. **Promote Java** only when 100% match is achieved over a minimum of 5 consecutive batch cycles.

---

## 5. Rollback Strategy

### Per-Phase Rollback

| Phase | Rollback Approach | Data Implications |
|:------|:------------------|:------------------|
| Phase 1 (Read-only) | Redirect API gateway back to COBOL CICS | No data changes to reconcile |
| Phase 2 (Read + Reports) | Redirect API gateway; discard generated reports | No data changes; re-run reports from COBOL |
| Phase 3 (Writes) | Redirect API gateway; replay missed transactions from audit log | Reconcile TRANSACT and CARDDAT from COBOL master |
| Phase 4 (Batch + Financial) | Revert to JCL batch cycle; restore VSAM files from pre-cutover backup | Full VSAM restore required; reconcile any Java-posted transactions |

### Data Synchronization During Parallel Run

- **Read APIs:** No synchronization needed -- both systems read from the same VSAM files (or their Java-side replicas).
- **Write APIs:** During Phase 3+, implement **dual-write via change data capture (CDC)**:
  - Java service writes to its database AND publishes events
  - A CDC connector writes changes back to VSAM for COBOL fallback
  - On rollback, VSAM is the source of truth
- **Batch jobs:** During dual-run, both COBOL and Java process the same DALYTRAN feed. The Java output is validated but not promoted until it passes all checks.

### Emergency Rollback Procedure

1. **Halt** all Java batch jobs and API traffic via feature flags.
2. **Restore** VSAM files from the most recent pre-batch backup (GDG generation).
3. **Re-route** API gateway to COBOL CICS endpoints.
4. **Re-run** the COBOL batch cycle from CLOSEFIL through OPENFIL.
5. **Validate** data integrity by running the test harness comparison.
6. **Notify** stakeholders and initiate root cause analysis.
