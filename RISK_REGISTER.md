# CardDemo Migration Risk Register

## Risk Scoring Matrix

| Impact \ Likelihood | Low | Medium | High |
|-------------------|-----|--------|------|
| **High** | Medium | High | Critical |
| **Medium** | Low | Medium | High |
| **Low** | Info | Low | Medium |

---

## Risk 1: Hidden Business Rules in Interest Calculation (Tribal Knowledge)

| Attribute | Value |
|-----------|-------|
| **Category** | Business Logic |
| **Likelihood** | **High** |
| **Impact** | **High** |
| **Risk Level** | **Critical** |

### Description
CBACT04C (652 LOC) calculates interest using disclosure-group rates from DISCGRP and category balances from TCATBALF. The compounding rules, grace-period logic, and rate-tier thresholds are encoded in COBOL with minimal comments. These rules likely evolved over years with undocumented regulatory or business-specific adjustments (e.g., promotional rates, penalty APRs, minimum-charge floors). No specification document exists in the repository.

### Evidence from Codebase
- CBACT04C reads DISCGRP (disclosure groups) and applies rates per transaction category — the rate-selection logic is embedded in procedural COBOL with no external configuration.
- CVTRA01Y (category balance) and CVTRA02Y (transaction type for interest) copybooks define numeric fields in COMP-3 packed-decimal, which may have implicit precision/rounding assumptions.
- No test cases exist for interest calculation in the test-harness directory.

### Mitigation Strategy
1. Conduct business-rule extraction workshops with SMEs before Phase 5a begins.
2. Build an automated regression test suite by running CBACT04C against production-like data and capturing output as golden-file baselines.
3. Implement 3-month dual-run (COBOL + Java) with automated per-account reconciliation during Phase 5a.
4. Document all extracted rules in a business-rules catalog (Decision Model Notation or spreadsheet).

### Early Warning Indicators
- SMEs cannot explain specific rate-selection or rounding behavior.
- Dual-run reconciliation produces discrepancies in the first cycle.
- Edge cases emerge in test data that produce different results in Java vs. COBOL.

---

## Risk 2: VSAM-to-Relational Data Fidelity Loss

| Attribute | Value |
|-----------|-------|
| **Category** | Data Migration |
| **Likelihood** | **Medium** |
| **Impact** | **High** |
| **Risk Level** | **High** |

### Description
VSAM KSDS files store records in EBCDIC with packed-decimal (COMP-3), zoned-decimal, and fixed-length character fields. Converting to a relational schema risks precision loss (decimal rounding), character encoding errors (EBCDIC → UTF-8 for special characters), and key-format mismatches (e.g., CCXREF's 16-byte card number key stored as a numeric vs. string).

### Evidence from Codebase
- ACCTDAT uses a record size of 300 bytes with an 11-byte key (account ID as zoned decimal).
- CARDDAT uses a 150-byte record with a 16-byte key (card number).
- EBCDIC data files in `app/data/EBCDIC/` contain packed-decimal fields that must be unpacked during migration.
- The test-harness uses JSON layout files to validate binary record parsing — these document the expected field positions but not all edge cases.

### Mitigation Strategy
1. Use the existing test-harness JSON layouts as authoritative field-position maps for schema generation.
2. Implement bi-directional data sync (Phase 3) with automated row-count and checksum validation.
3. Run the CBACT01C/CBACT02C/CBACT03C/CBCUS01C read-and-print batch programs against both VSAM and the migrated relational data — compare output byte-for-byte.
4. Test with production-volume data, not just sample datasets in `app/data/`.

### Early Warning Indicators
- Row counts differ between VSAM and PostgreSQL after initial migration.
- Numeric field values differ after round-trip conversion (EBCDIC → decimal → EBCDIC).
- Read-and-print batch programs produce different output against migrated data.

---

## Risk 3: Shared Data Coupling Causes Inconsistency During Transition

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | **High** |
| **Impact** | **High** |
| **Risk Level** | **Critical** |

### Description
ACCTDAT, CCXREF, and TRANSACT are accessed by 11+ programs across 5 bounded contexts. During the strangler transition (Phases 3–4), some programs will write to PostgreSQL while others still write to VSAM. Bi-directional sync introduces the risk of stale reads, lost updates, and split-brain scenarios.

### Evidence from Codebase
- ACCTDAT is written by: COACTUPC (online account update), COBIL00C (bill payment), CBTRN02C (batch posting), CBACT04C (interest calc).
- TRANSACT is written by: COTRN02C (online add), COBIL00C (payment), CBTRN01C/CBTRN02C (batch posting).
- No locking or versioning mechanism exists beyond VSAM record-level locking (SHAREOPTIONS(2,3) in the CSD).

### Mitigation Strategy
1. Implement optimistic concurrency control in the new services (version column on each row).
2. During bi-directional sync, designate one system as the "source of truth" for each entity at any given time (e.g., PostgreSQL for accounts in Phase 3, VSAM for transactions until Phase 4b).
3. Implement conflict detection in the CDC pipeline: log and alert on concurrent modifications to the same record.
4. Schedule batch jobs (CLOSEFIL → batch → OPENFIL) to run during a window when online programs are routed to the new API, avoiding concurrent VSAM writes.

### Early Warning Indicators
- CDC conflict alerts during normal operation.
- Batch jobs read stale data from VSAM after online updates went to PostgreSQL.
- Account balance discrepancies between the two stores.

---

## Risk 4: IMS DB Migration Complexity

| Attribute | Value |
|-----------|-------|
| **Category** | Environment / Technology |
| **Likelihood** | **Medium** |
| **Impact** | **High** |
| **Risk Level** | **High** |

### Description
The Authorization extension uses IMS HIDAM databases (DBPAUTP0/DBPAUTX0) for authorization storage. IMS hierarchical data does not map cleanly to relational tables — parent/child segment relationships, twin chains, and secondary indexes require careful denormalization. IMS expertise is increasingly rare.

### Evidence from Codebase
- `app/app-authorization-ims-db2-mq/ims/` contains DBD and PSB definitions for HIDAM databases.
- Programs use IMS DL/I calls (GU, GN, GNP, ISRT, REPL) that have no direct SQL equivalent.
- Two-phase commit across IMS and DB2 (for fraud detection) adds transactional complexity.

### Mitigation Strategy
1. Engage an IMS specialist during Phase 5b planning (before code migration begins).
2. Extract the full IMS database using PAUDBUNL/DBUNLDGS programs already in the codebase — these produce unload datasets that can be parsed and loaded into PostgreSQL.
3. Map the HIDAM segment hierarchy to relational tables with foreign keys. Document the mapping with sample data.
4. Replace DL/I calls with JPA repository methods; verify with golden-file regression tests.
5. Eliminate the IMS-DB2 two-phase commit by co-locating authorization and fraud data in the same PostgreSQL database with standard transactions.

### Early Warning Indicators
- IMS unload produces records that don't match expected segment layouts.
- Authorization queries return different results after relational migration.
- No available IMS specialist by Phase 5b planning kickoff.

---

## Risk 5: CICS Pseudo-Conversational Pattern Translation

| Attribute | Value |
|-----------|-------|
| **Category** | Technical Debt |
| **Likelihood** | **Medium** |
| **Impact** | **Medium** |
| **Risk Level** | **Medium** |

### Description
All online CICS programs use the pseudo-conversational pattern: `EXEC CICS RETURN TRANSID(...)` stores state in COMMAREA between user interactions. This stateless-but-stateful pattern doesn't map directly to REST APIs or SPAs. Incorrectly translating the multi-screen workflow (e.g., COCRDLIC → COCRDSLC → COCRDUPC) could lose navigation state or break validation sequences.

### Evidence from Codebase
- COCOM01Y defines a 150+ byte COMMAREA carrying user context, customer/account/card IDs, and last-map information across program transitions.
- COACTUPC (4,236 LOC) has extensive `CDEMO-PGM-CONTEXT` handling (ENTER vs. RE-ENTER) to manage multi-step update workflows.
- BMS maps (17 total) each represent a 3270 screen with specific field attributes (protected, numeric, bright/dark).

### Mitigation Strategy
1. Map each CICS pseudo-conversational workflow to a modern UX flow (multi-page form or SPA with client-side state).
2. Replace COMMAREA with server-side session state (Redis) or client-side state (JWT + encrypted cookies) for migration transition.
3. Build workflow-level integration tests (not just API-level) that validate the full navigation path (login → menu → list → detail → update → confirmation).
4. Use the BMS map definitions to auto-generate UI field specifications.

### Early Warning Indicators
- Users report "lost context" when navigating between screens.
- Multi-step update workflows fail on the confirmation step.
- State inconsistencies between concurrent sessions for the same user.

---

## Risk 6: Batch Job Scheduling & Sequencing Errors

| Attribute | Value |
|-----------|-------|
| **Category** | Operations |
| **Likelihood** | **Medium** |
| **Impact** | **Medium** |
| **Risk Level** | **Medium** |

### Description
Control-M orchestrates 3 batch chains (DAILY, WEEKLY, MONTHLY) with strict sequencing dependencies (CLOSEFIL → process → WAITSTEP → OPENFIL). Migrating to cloud-native scheduling (Step Functions, Airflow, or Spring Cloud Task) requires replicating these dependencies. Missed sequencing could cause jobs to run on locked files or stale data.

### Evidence from Codebase
- `app/scheduler/CardDemo.controlm` defines:
  - DAILY-TransactionBackup: CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL
  - WEEKLY-DisclosureGroupsRefresh: CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL (after MNTTRDB2)
  - MONTHLY-InterestCalculation: CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL
- CLOSEFIL/OPENFIL use CICS SDSF commands to close/open VSAM files — a pattern that doesn't exist in relational databases.
- WAITSTEP (COBSWAIT) calls an assembler routine to pause execution — a mainframe-specific wait mechanism.

### Mitigation Strategy
1. Map Control-M dependencies to a cloud-native DAG (directed acyclic graph) in Airflow or Step Functions.
2. CLOSEFIL/OPENFIL/WAITSTEP become no-ops once VSAM is replaced with a database — remove them from the schedule.
3. Implement job-level health checks: verify preconditions (e.g., previous job completed, data available) before each step.
4. Run the new schedule in parallel with Control-M for 2 batch cycles before cutting over.

### Early Warning Indicators
- Batch jobs run out of order in the new scheduler.
- Jobs fail because the previous step's output is not yet available.
- Data corruption from concurrent batch and online access.

---

## Risk 7: Inadequate Test Data Representativeness

| Attribute | Value |
|-----------|-------|
| **Category** | Testing |
| **Likelihood** | **High** |
| **Impact** | **Medium** |
| **Risk Level** | **High** |

### Description
The `app/data/` directory contains small sample datasets (EBCDIC and ASCII). These are unlikely to cover all business scenarios (e.g., accounts with zero balance, expired cards, edge-case interest tiers, maximum transaction volumes). Migrating and testing with only sample data may miss production-specific edge cases.

### Evidence from Codebase
- `app/data/ASCII/` contains 9 data files (acctdata.txt, carddata.txt, custdata.txt, etc.) — likely a handful of records each.
- `app/data/EBCDIC/` contains 13 datasets including an initial daily transaction file (DALYTRAN.PS.INIT).
- The test-harness (`test-harness/`) validates binary parsing but does not include business-logic test cases.
- No production-volume test data exists in the repository.

### Mitigation Strategy
1. Generate synthetic test data that covers edge cases: zero-balance accounts, expired cards, all disclosure groups, boundary amounts, maximum transaction volumes.
2. If possible, obtain anonymized/masked production data extracts for migration testing.
3. Build property-based tests for financial calculations (fuzz testing with random valid inputs).
4. Use the test-harness JSON layouts to auto-generate valid records spanning all field value ranges.

### Early Warning Indicators
- Test coverage reports show business-logic branches not exercised.
- First production cutover reveals data scenarios not seen in testing.
- Interest calculation produces unexpected results for specific account/disclosure-group combinations.

---

## Risk 8: MQ-to-Event-Bus Migration Message Loss

| Attribute | Value |
|-----------|-------|
| **Category** | Integration |
| **Likelihood** | **Medium** |
| **Impact** | **High** |
| **Risk Level** | **High** |

### Description
The Authorization extension and Account Extraction extension use IBM MQ for request/response messaging. Migrating from MQ to Kafka/SQS changes the messaging semantics (exactly-once delivery, message ordering, reply-queue correlation). Incorrect migration could cause authorization requests to be lost, duplicated, or processed out of order.

### Evidence from Codebase
- `app/app-vsam-mq/` defines MQ queues: CARDDEMO.REQUEST.QUEUE, CARDDEMO.RESPONSE.QUEUE.
- `app/app-authorization-ims-db2-mq/` uses MQ for authorization request/response with correlation IDs.
- MQ request/response is inherently synchronous (request → wait for reply); Kafka is asynchronous by design.

### Mitigation Strategy
1. During Phase 5b, deploy an MQ-to-Kafka bridge that preserves message correlation IDs and ordering.
2. Implement idempotency in the new authorization service (deduplicate by correlation ID).
3. Use Kafka transactions or SQS FIFO queues to maintain message ordering where required.
4. Monitor the bridge for message-in-flight counts: alert if the pending count exceeds a threshold.
5. Keep MQ infrastructure running during the transition; only decommission after 4 weeks of zero legacy traffic.

### Early Warning Indicators
- Message-in-flight count grows during bridge operation (messages entering but not exiting).
- Authorization response times increase beyond SLA.
- Duplicate authorization approvals or declines.

---

## Risk 9: COBOL Numeric Representation Precision Mismatches

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Likelihood** | **High** |
| **Impact** | **Medium** |
| **Risk Level** | **High** |

### Description
COBOL uses fixed-point decimal arithmetic (COMP-3 packed decimal, zoned decimal) with explicit PIC clauses defining precision (e.g., `PIC S9(13)V9(2) COMP-3`). Java uses `BigDecimal` or `double`. Incorrect mapping could cause rounding differences in financial calculations, particularly for interest computation, balance aggregation, and transaction amounts.

### Evidence from Codebase
- CVACT01Y defines account balance fields with implicit decimal positions.
- CVTRA05Y defines transaction amounts in packed decimal.
- CBACT04C performs interest calculations with MULTIPLY/DIVIDE operations that may have implicit ROUNDED clauses.
- COACTUPC uses `FUNCTION NUMVAL-C` to parse display-formatted numbers — a COBOL-specific intrinsic with defined rounding behavior.

### Mitigation Strategy
1. Use `java.math.BigDecimal` with `ROUND_HALF_EVEN` (banker's rounding) to match COBOL default behavior.
2. Map every PIC clause to a BigDecimal with explicit scale and precision.
3. Build unit tests for every arithmetic operation in CBACT04C that compare COBOL output (golden files) with Java output to 2+ decimal places.
4. Document the rounding mode used by each COBOL COMPUTE/MULTIPLY/DIVIDE statement.
5. Never use `double` or `float` for financial calculations.

### Early Warning Indicators
- Penny-level discrepancies in dual-run reconciliation.
- Interest calculation produces different totals in the 3rd or 4th decimal place.
- `FUNCTION NUMVAL-C` conversions produce different results than `new BigDecimal(String)` for edge-case inputs.

---

## Risk 10: Team Skill Gap for Mainframe Technologies

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Likelihood** | **High** |
| **Impact** | **Medium** |
| **Risk Level** | **High** |

### Description
The migration requires concurrent expertise in COBOL, CICS, VSAM, IMS DB, DB2, MQ, JCL, Control-M, and BMS on the legacy side, plus Java/Kotlin, Spring Boot, PostgreSQL, Kafka, and cloud infrastructure on the modern side. Few engineers possess both skill sets. COBOL and IMS expertise is aging out of the workforce.

### Evidence from Codebase
- 31 COBOL programs with CICS EXEC commands requiring CICS-specific knowledge.
- IMS DL/I calls in 8 authorization programs requiring IMS database expertise.
- Control-M scheduling definitions requiring batch-operations expertise.
- Assembler routines (COBDATFT, MVSWAIT) requiring z/OS system-programming knowledge.

### Mitigation Strategy
1. Form paired teams: each pair has one COBOL/mainframe specialist and one Java/cloud specialist.
2. Engage COBOL and IMS specialists as contractors for Phases 3–5 (peak mainframe expertise need).
3. Invest in COBOL reading training for the Java team (sufficient to understand, not write, COBOL).
4. Build comprehensive documentation of COBOL program behavior before the mainframe specialists leave.
5. Prioritize automated test coverage so that future maintainers don't need to understand the original COBOL.

### Early Warning Indicators
- Key COBOL/IMS specialists unavailable or leaving before Phase 5.
- Java developers unable to explain the business logic in COBOL programs they're rewriting.
- Code review reveals Java implementations that don't match COBOL behavior.

---

## Risk Summary

| # | Risk | Likelihood | Impact | Level | Phase Affected |
|---|------|-----------|--------|-------|----------------|
| 1 | Hidden business rules in interest calculation | High | High | **Critical** | Phase 5a |
| 2 | VSAM-to-relational data fidelity loss | Medium | High | **High** | Phase 3–4 |
| 3 | Shared data coupling during transition | High | High | **Critical** | Phase 3–4 |
| 4 | IMS DB migration complexity | Medium | High | **High** | Phase 5b |
| 5 | CICS pseudo-conversational translation | Medium | Medium | **Medium** | Phase 3 |
| 6 | Batch scheduling & sequencing errors | Medium | Medium | **Medium** | Phase 4b–5a |
| 7 | Inadequate test data representativeness | High | Medium | **High** | All phases |
| 8 | MQ-to-event-bus message loss | Medium | High | **High** | Phase 5b |
| 9 | Numeric precision mismatches | High | Medium | **High** | Phase 3–5 |
| 10 | Team skill gap for mainframe tech | High | Medium | **High** | All phases |

### Top Actions

1. **Begin business-rule extraction for interest calculation immediately** (Risk 1) — this is on the critical path for Phase 5a.
2. **Establish bi-directional sync with conflict detection early in Phase 3** (Risk 3) — test it extensively before relying on it for production data.
3. **Invest in test data generation** (Risk 7) — synthetic data covering all edge cases reduces risk across every phase.
4. **Secure IMS and COBOL expertise now** (Risks 4, 10) — contractor availability may be limited; engage early.
5. **Mandate BigDecimal everywhere** (Risk 9) — establish this as an architectural decision record before any financial code is written.
