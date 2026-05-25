# Cutover Plan — CardDemo COBOL-to-Java Migration

## Overview

This plan sequences the migration of the CardDemo COBOL estate into 4 phases over an estimated 12–18 months. Each phase specifies which programs migrate, affected data stores, required integration bridges, rollback procedures, and acceptance criteria for advancing to the next phase.

### Guiding Principles

1. **Migrate data stores first, then programs.** Move VSAM/IMS/DB2 data to PostgreSQL before rewriting COBOL programs, so that new Java services operate on the same data.
2. **Parallel-run everything.** Run old and new systems simultaneously for each phase, comparing outputs before cutting over.
3. **Start with lowest-risk, highest-isolation services.** Build confidence and team velocity on simpler services before tackling high-complexity areas.
4. **Never break the daily batch cycle.** The CLOSEFIL→POSTTRAN→INTCALC→TRANREPT→OPENFIL pipeline is the financial heartbeat — it must keep running until fully replaced.

---

## Phase 0: Foundation (Weeks 1–6)

### Objective
Establish the target platform infrastructure, data migration pipelines, and CI/CD foundation before migrating any business logic.

### Activities

| # | Activity | Details |
|---|----------|---------|
| 1 | **Provision target infrastructure** | PostgreSQL (RDS/Aurora), Kafka/SQS, API Gateway, container orchestration (EKS/ECS), CI/CD pipeline |
| 2 | **Design and create target database schema** | Map all VSAM record layouts to relational tables (see DATA_DICTIONARY.md). Key tables: `accounts`, `customers`, `cards`, `card_xref`, `transactions`, `category_balances`, `disclosure_groups`, `transaction_types`, `transaction_categories`, `users` |
| 3 | **Build data migration ETL** | Convert EBCDIC data files to PostgreSQL inserts. Handle: fixed-width parsing, COMP/COMP-3 fields, signed decimals, EBCDIC→ASCII encoding. Use app/data/ASCII/ files as test fixtures. |
| 4 | **Establish parallel-run framework** | Build a reconciliation tool that compares COBOL batch outputs against Java service outputs (e.g., compare account balances, transaction counts, reject records) |
| 5 | **Set up service scaffolding** | Spring Boot parent POM, shared libraries (error handling, logging, security), service templates, Docker images |
| 6 | **Deploy monitoring/observability** | Distributed tracing, centralized logging, metrics dashboards, alerting |

### Data Stores Affected

| Data Store | Action | Notes |
|------------|--------|-------|
| PostgreSQL | Create schema | All tables with proper FK relationships, indexes, constraints |
| VSAM files | Read-only ETL source | No changes to VSAM — keep operational |
| Kafka/SQS | Create topics/queues | `transaction-posted`, `authorization-decided`, `batch-job-status` |

### Acceptance Criteria for Phase 1

- [ ] PostgreSQL schema deployed with all tables matching VSAM record layouts
- [ ] ETL pipeline successfully loads all ASCII test data into PostgreSQL
- [ ] Data reconciliation: 100% match between VSAM file records and PostgreSQL rows
- [ ] CI/CD pipeline deploys a "hello world" Spring Boot service to staging
- [ ] Parallel-run reconciliation framework tested with sample data

---

## Phase 1: Core Services — Low Risk (Weeks 7–16)

### Objective
Migrate the most isolated, lowest-risk services to build team confidence and establish patterns for subsequent phases.

### Programs Migrating

| Service | COBOL Programs | LOC | Risk Level |
|---------|---------------|-----|------------|
| **Auth Service** | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | ~2,200 | Low |
| **Reference Data Service** | COTRTLIC, COTRTUPC, COBTUPDT | ~4,100 | Low–Medium |
| **Transaction Inquiry** | COTRN00C, COTRN01C | ~1,200 | Low |
| **Reporting Service** | CBTRN03C, CORPT00C | ~950 | Low |

### Data Stores Affected

| Data Store | Migration Action |
|------------|-----------------|
| USRSEC (VSAM) → `users` table | Full data migration. After migration, new Auth Service is source of truth. COBOL sign-on program reads from VSAM (still operational for remaining CICS programs). |
| DB2 tran-type tables → `transaction_types`, `transaction_categories` | Full data migration. DB2 sub-app programs retired. |
| TRANTYPE, TRANCATG (VSAM) → `transaction_types`, `transaction_categories` | Reference data loaded into PostgreSQL. |
| TRANSACT (VSAM) → `transactions` table | Read-only copy for inquiry. VSAM remains source of truth for batch pipeline. |

### Integration Bridges Required

| Bridge | Purpose | Technology | Decommission Trigger |
|--------|---------|------------|---------------------|
| **USRSEC Dual-Write Bridge** | When Auth Service creates/updates a user, also write to USRSEC VSAM so COBOL sign-on still works | COBOL batch job reads PostgreSQL `users` table and syncs to USRSEC nightly | Decommission when COSGN00C is retired (Phase 2) |
| **Transaction Read Bridge** | Transaction Inquiry service reads from PostgreSQL, but data is populated by COBOL batch (CBTRN02C writes to VSAM) | ETL job copies new VSAM TRANSACT records to PostgreSQL after each batch cycle | Decommission when Transaction Service (Phase 2) owns writes |
| **Reference Data Sync** | New Reference Data Service owns transaction types; COBOL reporting programs (CBTRN03C) still read VSAM files | Export PostgreSQL reference data to VSAM nightly | Decommission when CBTRN03C is retired (Phase 2) |

### Rollback Plan

| Trigger | Action |
|---------|--------|
| Auth Service failure | Revert to COBOL sign-on (COSGN00C). Disable JWT validation in API gateway; route authentication to CICS. |
| Reference Data Service failure | COBOL programs continue reading VSAM TRANTYPE/TRANCATG files unchanged. No user-facing impact. |
| Transaction Inquiry failure | COBOL COTRN00C/COTRN01C remain active on CICS terminals. Web/mobile inquiry disabled. |
| Data reconciliation failure (>0.01% mismatch) | Halt migration. Investigate ETL pipeline. Do not proceed to Phase 2. |

### Acceptance Criteria for Phase 2

- [ ] Auth Service: Users can log in via JWT; CRUD operations on users pass integration tests
- [ ] Auth Service: USRSEC dual-write bridge verified — COBOL sign-on still works for 3270 users
- [ ] Reference Data Service: All transaction types and categories match VSAM reference data (100% match)
- [ ] Transaction Inquiry: Query results match COBOL COTRN00C/COTRN01C output for 100 sample accounts
- [ ] Reporting Service: CBTRN03C-equivalent report output matches COBOL output (character-by-character comparison on 5 sample reports)
- [ ] All services pass load testing at 2× expected production volume
- [ ] Zero P1/P2 incidents in 2-week parallel-run period

---

## Phase 2: Financial Core — High Value (Weeks 17–32)

### Objective
Migrate the financial processing pipeline — transaction posting, interest calculation, and account/card management. This is the highest-risk, highest-value phase.

### Programs Migrating

| Service | COBOL Programs | LOC | Risk Level |
|---------|---------------|-----|------------|
| **Transaction Service** | CBTRN02C, CBTRN01C, COTRN02C | ~1,931 | High |
| **Interest Calculation Service** | CBACT04C | ~650 | High |
| **Account Service** | COACTUPC, COACTVWC, COBIL00C | ~5,977 | Very High |
| **Card Service** | COCRDLIC, COCRDSLC, COCRDUPC | ~3,819 | High |

### Data Stores Affected

| Data Store | Migration Action |
|------------|-----------------|
| ACCTFILE → `accounts` table | Promoted to source of truth in PostgreSQL. COBOL batch reads from VSAM (bridge syncs PostgreSQL → VSAM). |
| CARDFILE → `cards` table | Full migration. Card Service owns writes. |
| CUSTFILE → `customers` table | Full migration. Account Service owns writes. |
| XREFFILE → `card_xref` table | Full migration. Card Service owns data. |
| TRANSACT/TRANFILE → `transactions` table | Promoted to source of truth in PostgreSQL. Transaction Service owns all writes. |
| TCATBALF → `category_balances` table | Full migration. Transaction Service updates during posting; Interest Calc reads. |
| DALYTRAN → Kafka topic `daily-transactions` | External feed publishes to Kafka instead of VSAM sequential file. |
| DISCGRP → `disclosure_groups` table | Already in PostgreSQL (Phase 1 Reference Data). Interest Calc reads via API. |
| DALYREJS → `rejected_transactions` table | Transaction Service writes rejects to database + dead-letter queue. |

### Integration Bridges Required

| Bridge | Purpose | Technology | Decommission Trigger |
|--------|---------|------------|---------------------|
| **VSAM Sync Bridge (Account)** | Sync PostgreSQL `accounts` → ACCTFILE VSAM for any remaining COBOL programs | Nightly batch sync job | Decommission when all COBOL programs retired (Phase 3+) |
| **VSAM Sync Bridge (Card/Xref)** | Sync PostgreSQL `cards`/`card_xref` → CARDFILE/XREFFILE | Nightly batch sync job | Decommission when all COBOL programs retired |
| **DALYTRAN Feed Adapter** | Convert external DALYTRAN sequential file feed to Kafka messages | File watcher → Kafka producer. Runs until external feed switches to API/Kafka | Decommission when upstream system publishes directly to Kafka |
| **Batch Cycle Orchestrator** | Replace JCL CLOSEFIL→POSTTRAN→INTCALC→TRANREPT→OPENFIL with Spring Batch orchestration | Spring Batch job launcher with step dependencies. During parallel-run, both COBOL and Java pipelines execute. | COBOL pipeline decommissioned after 30-day parallel-run with 100% reconciliation |
| **CICS Online Bridge** | Remaining CICS programs (Statement, Authorization) still need file access | VSAM Sync Bridges keep VSAM files current | Decommission when Phases 3–4 complete |

### Migration Sub-Phases

#### Phase 2A: Transaction Posting (Weeks 17–22)

1. Deploy Transaction Service with `POST /transactions/batch-post` endpoint
2. Implement DALYTRAN feed adapter (file → Kafka → Transaction Service)
3. Parallel-run: both COBOL CBTRN02C and Java Transaction Service process same daily feed
4. Reconcile: compare TRANSACT records, TCATBALF balances, ACCTFILE balances, DALYREJS rejects
5. **Gate:** 30 consecutive days of 100% financial reconciliation before proceeding

#### Phase 2B: Interest Calculation (Weeks 21–26)

1. Deploy Interest Calculation Service
2. Parallel-run: both CBACT04C and Java service calculate interest on same data
3. Reconcile: compare interest charges per account (must match to the cent)
4. **Gate:** 2 billing cycles of 100% interest reconciliation

#### Phase 2C: Account & Card Management (Weeks 25–32)

1. Deploy Account Service (start with read-only COACTVWC equivalent)
2. Deploy Card Service (start with read-only COCRDLIC/COCRDSLC equivalents)
3. Enable write operations (COACTUPC, COCRDUPC, COBIL00C equivalents)
4. Extract CSLKPCDY validation rules to Validation Reference Service
5. Route web/mobile traffic to new services; 3270 traffic remains on CICS
6. **Gate:** All account/card operations tested with 100 sample accounts; no data corruption

### Rollback Plan

| Trigger | Action |
|---------|--------|
| Transaction posting discrepancy | Immediately halt Java pipeline. COBOL CBTRN02C continues as primary. Investigate discrepancy. Restore VSAM from pre-batch backup (TRANBKP). |
| Interest calculation mismatch | Revert to CBACT04C. Interest Service runs in shadow mode only (calculates but does not write). Investigate per-account. |
| Account/Card Service data corruption | VSAM files are the rollback source. Restore from VSAM (which is sync'd nightly). Disable write operations in new services. |
| DALYTRAN feed adapter failure | Fall back to direct VSAM sequential file processing via COBOL CBTRN02C. |
| Full Phase 2 rollback | All COBOL programs remain operational. VSAM is still source of truth via sync bridges. Disable all Java service write paths. |

### Acceptance Criteria for Phase 3

- [ ] Transaction posting: 30 consecutive days of 100% financial reconciliation (balances, reject counts, category totals)
- [ ] Interest calculation: 2 billing cycles with per-account interest matching to the cent
- [ ] Account Service: All COACTUPC validation rules pass (phone area codes, state codes, ZIP prefixes from CSLKPCDY)
- [ ] Card Service: Card lifecycle operations (list, view, update) match COBOL output
- [ ] Bill payment: COBIL00C-equivalent creates correct payment transactions and updates account balances
- [ ] Online/batch asymmetry resolved: online transaction add now updates balances (unlike legacy COTRN02C)
- [ ] Performance: batch posting completes within batch window (< 4 hours for 1M transactions)
- [ ] Zero P1 financial incidents in 4-week parallel-run period

---

## Phase 3: Supporting Services (Weeks 33–42)

### Objective
Migrate remaining services: Statement Generation, Card Authorization, and MQ Event Processing. Decommission CICS for batch and online programs replaced in Phases 1–2.

### Programs Migrating

| Service | COBOL Programs | LOC | Risk Level |
|---------|---------------|-----|------------|
| **Statement Service** | CBSTM03A, CBSTM03B | ~1,524 | Medium |
| **Authorization Service** | COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C | ~3,058 | High |
| **MQ Event Consumers** | COACCT01, CODATE01 | ~600 | Low |

### Data Stores Affected

| Data Store | Migration Action |
|------------|-----------------|
| IMS DB → `authorizations` table | Full migration. IMS decommissioned after authorization migration. |
| DB2 AUTHFRDS → `fraud_flags` table | Full migration to PostgreSQL. |
| MQ queues → Kafka topics | `auth-request`, `auth-response`, `account-events` |
| STMTFILE/HTMLFILE → Object storage (S3) | Statement output written to S3 instead of mainframe datasets |

### Integration Bridges Required

| Bridge | Purpose | Decommission Trigger |
|--------|---------|---------------------|
| **IMS Data Migration** | One-time extraction of IMS pending authorizations to PostgreSQL | After migration verified |
| **MQ-to-Kafka Bridge** | Route existing MQ messages to Kafka during transition | When all MQ producers switch to Kafka |
| **Statement Output Adapter** | Convert PDF/HTML output to S3 storage; legacy consumers read from S3 instead of mainframe datasets | When legacy consumers updated |

### Rollback Plan

| Trigger | Action |
|---------|--------|
| Authorization latency exceeds SLA | Fall back to COPAUA0C on CICS. MQ-to-Kafka bridge routes traffic back to MQ. |
| Statement generation errors | Re-run CBSTM03A/B on cloud COBOL runtime (AWS M2 / GnuCOBOL). |
| IMS data loss | Restore from IMS unload (PAUDBUNL output, taken before migration). |

### Acceptance Criteria for Phase 4

- [ ] Statement Service: Generated statements match COBOL output (layout, amounts, dates) for 50 sample accounts
- [ ] Authorization Service: Authorization decisions match COPAUA0C for 1,000 test transactions (approve/decline/fraud)
- [ ] Authorization latency: P99 < 200ms (current MQ-based latency baseline)
- [ ] Fraud detection: All fraud-flagged cards correctly declined
- [ ] MQ event consumers: Account inquiry and date inquiry work via Kafka
- [ ] IMS database fully decommissioned; no programs reference IMS DL/I
- [ ] MQ infrastructure decommissioned or in maintenance-only mode
- [ ] All VSAM Sync Bridges operating correctly for remaining consumers

---

## Phase 4: Decommission & Cleanup (Weeks 43–52)

### Objective
Decommission all remaining mainframe components, retire integration bridges, and complete the migration.

### Activities

| # | Activity | Details |
|---|----------|---------|
| 1 | **Decommission CICS region** | All online programs migrated; no 3270 terminal users remain |
| 2 | **Decommission VSAM files** | All data in PostgreSQL; VSAM Sync Bridges stopped |
| 3 | **Retire Export/Import utilities** | CBEXPORT/CBIMPORT no longer needed — data portability via database tools/APIs |
| 4 | **Retire navigation programs** | COMEN01C, COADM01C — replaced by web frontend routing |
| 5 | **Retire utility programs** | CSUTLDTC (→ java.time), COBSWAIT (→ Thread.sleep), CBACT01C–03C, CBCUS01C (→ SQL queries) |
| 6 | **Remove integration bridges** | USRSEC Dual-Write, VSAM Sync, DALYTRAN Feed Adapter, MQ-to-Kafka Bridge |
| 7 | **Decommission JCL scheduler** | All batch jobs now orchestrated by Spring Batch / cloud scheduler |
| 8 | **Archive COBOL source** | Tag final COBOL source in git; archive for compliance/audit purposes |
| 9 | **Decommission mainframe LPAR** | Final infrastructure shutdown |
| 10 | **Post-migration audit** | Financial reconciliation, security audit, performance baseline |

### Programs Retired (Not Migrated)

| Program | Reason |
|---------|--------|
| COMEN01C | Navigation shell → web frontend router |
| COADM01C | Admin navigation → web frontend router |
| CBACT01C | Account file printer → SQL query |
| CBACT02C | Card file printer → SQL query |
| CBACT03C | Xref file printer → SQL query |
| CBCUS01C | Customer file printer → SQL query |
| CSUTLDTC | Date utility → `java.time` API |
| COBSWAIT | Wait utility → `Thread.sleep()` / `ScheduledExecutorService` |
| CBEXPORT | Data export → `pg_dump` or API |
| CBIMPORT | Data import → database restore or API |
| PAUDBLOD | IMS load → PostgreSQL migration |
| PAUDBUNL | IMS unload → PostgreSQL export |
| DBUNLDGS | IMS generic unload → retired with IMS |

### Rollback Plan

At this stage, rollback to mainframe is a **business continuity decision**, not a technical revert:

- COBOL source is archived and tagged in git
- VSAM data can be reconstructed from PostgreSQL via export tools
- Mainframe LPAR can be re-provisioned from archived system image (if maintained)
- **Recommended:** Maintain mainframe disaster recovery capability for 6 months post-decommission

### Acceptance Criteria for Completion

- [ ] All 44 COBOL programs either migrated to Java services or retired with documented justification
- [ ] All 44 JCL jobs either replaced by Spring Batch jobs or retired
- [ ] All VSAM files decommissioned; data in PostgreSQL
- [ ] IMS database decommissioned
- [ ] DB2 tables migrated to PostgreSQL
- [ ] MQ infrastructure decommissioned
- [ ] CICS region shut down
- [ ] No integration bridges remaining
- [ ] Financial audit: 12-month transaction history reconciled between old and new systems
- [ ] Security audit: No cleartext passwords; all authentication via JWT/OAuth2
- [ ] Performance benchmark: All SLAs met (batch window, API latency, throughput)
- [ ] Team training: Operations team certified on new platform
- [ ] Disaster recovery: DR tested and documented for new architecture

---

## Timeline Summary

```
Week:  1────6   7───────16   17──────────────32   33─────42   43─────52
       │         │            │                     │           │
       ▼         ▼            ▼                     ▼           ▼
    Phase 0    Phase 1      Phase 2              Phase 3     Phase 4
   Foundation  Low-Risk     Financial Core       Supporting  Decommission
               Services     (2A→2B→2C)           Services

   Infra +     Auth +       Transactions +       Statements  Retire COBOL
   Schema +    Ref Data +   Interest +           Auth Svc +  Retire CICS
   ETL +       Inquiry +    Accounts +           MQ Events   Retire VSAM
   CI/CD       Reporting    Cards                            Retire IMS
```

### Parallel-Run Durations

| Component | Parallel-Run Duration | Reconciliation Frequency |
|-----------|----------------------|-------------------------|
| Transaction Posting | 30 days minimum | Daily |
| Interest Calculation | 2 billing cycles | Per billing cycle |
| Account/Card Services | 4 weeks | Daily |
| Statement Generation | 1 billing cycle | Per statement run |
| Authorization | 2 weeks | Per authorization decision |
