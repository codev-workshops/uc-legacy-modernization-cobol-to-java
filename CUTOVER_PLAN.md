# CardDemo Migration Cutover Plan

## Guiding Principles

1. **Lowest-risk, highest-value first** — Start with isolated, well-understood domains that deliver immediate value.
2. **Strangler facade** — Maintain a coexistence layer so legacy and modern systems run in parallel.
3. **Data migration before code migration** — Move data to the target relational database early; both old and new code can read from it via the facade.
4. **Dual-run validation** — Critical financial processes run on both old and new systems with automated reconciliation before cutover.
5. **Reversibility** — Every phase has a rollback plan that can be executed within the batch window.

---

## Phase 0: Foundation (Weeks 1–4)

### Objective
Establish the target platform, CI/CD pipeline, and strangler facade infrastructure.

### Programs Migrating
None — infrastructure only.

### Activities

| # | Activity | Duration |
|---|----------|----------|
| 1 | Provision cloud environment (AWS/Azure): compute, PostgreSQL/Aurora, Kafka/SQS, container orchestration (EKS/ECS). | Week 1 |
| 2 | Set up CI/CD pipeline for Java/Kotlin microservices (GitHub Actions / Jenkins). | Week 1–2 |
| 3 | Deploy rehosting environment (NTT DATA UniKix or Micro Focus on EC2) for COBOL coexistence. | Week 2–3 |
| 4 | Build the **API Gateway / Strangler Facade** — routes requests to either legacy CICS or new REST services. | Week 2–4 |
| 5 | Design and create the target relational schema based on VSAM record layouts (copybook → DDL mapping). | Week 3–4 |
| 6 | Implement automated data sync: VSAM → PostgreSQL replication via CDC or batch extract/load. | Week 3–4 |
| 7 | Establish monitoring, logging (ELK/CloudWatch), and alerting for both legacy and modern stacks. | Week 4 |

### Data Stores Affected
- Target PostgreSQL/Aurora schema created (empty).
- VSAM files continue as source of truth.

### Integration Bridges
- API Gateway configured but routing 100% to legacy.
- CDC/ETL pipeline established for continuous data sync.

### Rollback Plan
- Tear down cloud infrastructure; revert to mainframe-only operation.
- No production data has been migrated; no business risk.

### Acceptance Criteria
- [ ] Target database schema deployed and validated against copybook record layouts.
- [ ] API Gateway routes test requests to legacy CICS and returns correct responses.
- [ ] CDC/ETL pipeline syncs sample data from VSAM to PostgreSQL with <1-minute lag.
- [ ] CI/CD pipeline builds and deploys a skeleton Spring Boot service.
- [ ] Rehosted COBOL environment passes smoke tests with production-like data.

---

## Phase 1: Identity & User Administration (Weeks 5–8)

### Objective
Replace USRSEC VSAM-based authentication with modern identity management. This is the lowest-risk extraction because USRSEC is used by only 5 programs and is completely isolated from financial data.

### Programs Migrating

| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| COSGN00C | Sign-on | 260 | Spring Security + JWT |
| COUSR00C | User List | 695 | User Management REST API |
| COUSR01C | User Add | 299 | User Management REST API |
| COUSR02C | User Update | 414 | User Management REST API |
| COUSR03C | User Delete | 359 | User Management REST API |

### Data Stores Affected

| Store | Action |
|-------|--------|
| USRSEC (VSAM) | Migrate to `users` table in PostgreSQL. Decommission VSAM file after validation. |

### Integration Bridges
- **3270-to-API bridge:** During transition, the legacy menu programs (COMEN01C, COADM01C) call the new Identity Service via the API Gateway instead of reading USRSEC directly. COMMAREA user fields populated from JWT claims.
- **Dual authentication:** Both USRSEC and the new user store accept logins for 2 weeks of parallel run.

### Rollback Plan
- Re-enable USRSEC VSAM file as the authentication source.
- API Gateway routes sign-on requests back to COSGN00C.
- User data remains in both VSAM and PostgreSQL during parallel run; VSAM is the fallback source of truth.

### Acceptance Criteria
- [ ] All existing users from USRSEC successfully migrated to PostgreSQL.
- [ ] Login via new Identity Service returns valid JWT accepted by all downstream programs.
- [ ] User CRUD operations (list/add/update/delete) functional via REST API.
- [ ] Legacy 3270 sign-on screen continues to work via the bridge (for users who haven't migrated to web UI).
- [ ] Zero authentication failures during 2-week parallel run.
- [ ] USRSEC VSAM file decommissioned.

---

## Phase 2: Reference Data & Reporting (Weeks 9–14)

### Objective
Migrate read-only and reference-data domains. These have minimal write-coupling and pose the lowest risk to operational data.

### Programs Migrating

| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| COTRTLIC | Transaction Type List (DB2) | ~400 | Reference Data REST API |
| COTRTUPC | Transaction Type Add/Edit (DB2) | ~400 | Reference Data REST API |
| COBTUPDT | Batch Txn Type Maintenance | ~400 | Reference Data REST API (or Spring Batch) |
| CORPT00C | Report Submission (online) | 649 | Reporting REST API |
| CBTRN03C | Transaction Detail Report (batch) | 649 | Spring Batch + Jasper/BIRT |
| CBSTM03A | Account Statements (batch) | 924 | Spring Batch + PDF/HTML generation |
| CBSTM03B | Statement Subroutine | 230 | Merged into statement generator |

### Data Stores Affected

| Store | Action |
|-------|--------|
| DB2 TRANSACTION_TYPE | Migrate to PostgreSQL `transaction_types` table. |
| DB2 TRANSACTION_TYPE_CATEGORY | Migrate to PostgreSQL `transaction_categories` table. |
| VSAM TRANTYPE / TRANCATG | Decommission — no longer needed once reports read from PostgreSQL directly. |
| TRANSACT (VSAM) | Read-only access via CDC-replicated `transactions` table in PostgreSQL. |

### Integration Bridges
- **Report trigger bridge:** CORPT00C currently submits batch jobs via CICS WRITEQ TD. The new Reporting Service accepts REST requests and triggers Spring Batch jobs.
- **TRANEXTR elimination:** The weekly DB2→VSAM extract job becomes unnecessary; reporting reads directly from PostgreSQL.

### Rollback Plan
- Re-enable DB2 tables and VSAM extract job (TRANEXTR) as the reference data source.
- Route reporting requests back to legacy CORPT00C and batch JCL.
- CDC-replicated `transactions` table remains available; reports can switch between sources.

### Acceptance Criteria
- [ ] Transaction type CRUD operations functional via REST API with PostgreSQL backend.
- [ ] Transaction detail reports match legacy output byte-for-byte (or pixel-for-pixel for formatted output).
- [ ] Account statements generate identical content to CBSTM03A output.
- [ ] TRANEXTR weekly extract job decommissioned; no VSAM TRANTYPE/TRANCATG references remain.
- [ ] Control-M WEEKLY-TransactionTypesDBRefresh folder updated to trigger new batch jobs.
- [ ] Report generation SLA maintained (same or better completion time).

---

## Phase 3: Account & Card Services (Weeks 15–24)

### Objective
Build the core Account Service behind the strangler facade. This is the highest-coupling domain and requires the most careful migration.

### Programs Migrating

| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| COACTVWC | Account View | 941 | Account REST API |
| COACTUPC | Account Update | 4,236 | Account REST API |
| COCRDLIC | Card List | 1,459 | Card REST API |
| COCRDSLC | Card Detail/Search | 887 | Card REST API |
| COCRDUPC | Card Update | 1,560 | Card REST API |
| CBACT01C | Batch Account Read | 430 | Eliminated (SQL query) |
| CBACT02C | Batch Card Read | 178 | Eliminated (SQL query) |
| CBACT03C | Batch Cross-Ref Read | 178 | Eliminated (SQL query) |
| CBCUS01C | Batch Customer Read | 178 | Eliminated (SQL query) |

### Data Stores Affected

| Store | Action |
|-------|--------|
| ACCTDAT (VSAM) | Primary migration to `accounts` table. Bi-directional sync during transition. |
| CARDDAT (VSAM) | Migrate to `cards` table. |
| CCXREF (VSAM + AIX) | Migrate to `card_account_xref` table (or FK relationship). |
| CUSTDAT (VSAM) | Migrate to `customers` table. |

### Integration Bridges
- **VSAM-to-DB sync:** Bi-directional during transition. New API writes to PostgreSQL; CDC replicates back to VSAM for batch programs that haven't migrated yet (Phases 4–5).
- **3270-to-API bridge:** CICS online programs replaced with REST APIs. A thin 3270 emulation layer (optional) routes terminal users to the web UI.
- **Batch consumers:** CBTRN01C, CBTRN02C, CBACT04C, and CBSTM03B continue to read VSAM files via the sync bridge.

### Rollback Plan
- Disable new Account/Card APIs in the API Gateway; route all traffic back to CICS programs.
- Bi-directional sync ensures VSAM files are current; COBOL programs resume from consistent state.
- Rollback window: up to 4 hours (time to re-enable CICS transactions and verify data consistency).

### Acceptance Criteria
- [ ] Account CRUD via REST API passes all functional tests (credit limit, cash limit, balance validation).
- [ ] Card list/search/update via REST API matches legacy CICS behavior (pagination, AIX browsing).
- [ ] Customer data accessible via Account Service API.
- [ ] Bi-directional VSAM↔PostgreSQL sync verified: changes in either direction propagate within 30 seconds.
- [ ] Batch programs (CBTRN01C, CBTRN02C, CBACT04C) continue to run successfully against synced VSAM data.
- [ ] PCI DSS compliance validated for card data handling in the new service.
- [ ] 2-week parallel run with zero data discrepancies.
- [ ] COACTUPC validation rules (credit limit, cash limit, balance) produce identical outcomes in Java.

---

## Phase 4: Transaction Processing & Payments (Weeks 25–34)

### Objective
Migrate online transaction management and bill payment, then migrate batch transaction posting.

### Sub-Phase 4a: Online Transactions & Payments (Weeks 25–28)

| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| COTRN00C | Transaction List | 699 | Transaction REST API |
| COTRN01C | Transaction View | 330 | Transaction REST API |
| COTRN02C | Transaction Add | 783 | Transaction REST API |
| COBIL00C | Bill Payment | 572 | Payment REST API |

### Sub-Phase 4b: Batch Transaction Posting (Weeks 29–34)

| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| CBTRN01C | Post Daily Transactions | 494 | Spring Batch job |
| CBTRN02C | Post with Rejections | 731 | Spring Batch job |

### Data Stores Affected

| Store | Action |
|-------|--------|
| TRANSACT (VSAM) | Primary migration to `transactions` table. VSAM file decommissioned after batch posting migrates. |
| DALYTRAN | Migrate to `daily_transactions` staging table. |
| DALYREJS | Migrate to `rejected_transactions` table. |
| TCATBALF | Continue on VSAM until Phase 5. |

### Integration Bridges
- **Transaction write bridge (4a):** New Transaction API writes to PostgreSQL; CDC replicates to VSAM TRANSACT for batch programs still on COBOL during 4a.
- **Daily file bridge (4b):** Daily transaction input (DALYTRAN) fed from the new system's transaction queue or staging table instead of the legacy flat file.
- **Payment → Account/Transaction events:** Bill payment triggers account-balance-update and transaction-creation events via Kafka/SQS instead of direct VSAM writes.

### Rollback Plan
- **4a rollback:** Re-enable CICS transaction programs; API Gateway routes back to legacy. Bi-directional sync ensures data consistency.
- **4b rollback:** Re-enable JCL batch posting jobs (POSTTRAN); feed DALYTRAN from the original source. Control-M DAILY-TransactionBackup folder reverts to original configuration.

### Acceptance Criteria
- [ ] Online transaction list/view/add produces identical results to legacy CICS programs.
- [ ] Bill payment correctly updates account balances and creates transaction records.
- [ ] Batch posting (CBTRN02C equivalent) processes daily transactions with identical acceptance/rejection logic.
- [ ] Rejected transactions written to `rejected_transactions` table match legacy DALYREJS output.
- [ ] Category balances (TCATBALF) updated correctly during posting.
- [ ] 4-week dual-run for batch posting with automated reconciliation: zero discrepancies.
- [ ] Control-M DAILY-TransactionBackup schedule updated to trigger new Spring Batch jobs.
- [ ] TRANSACT VSAM file decommissioned (end of Phase 4b).

---

## Phase 5: Financial Calculations & Authorization (Weeks 35–46)

### Objective
Migrate the highest-risk batch processes (interest calculation) and the most complex extension (authorization processing).

### Sub-Phase 5a: Interest Calculation (Weeks 35–40)

| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| CBACT04C | Interest Calculator | 652 | Spring Batch job (Interest Calculation Engine) |

### Sub-Phase 5b: Authorization Processing (Weeks 41–46)

| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| CBPAUP0C | Batch Auth Processing | — | Authorization Microservice (event-driven) |
| COPAUA0C | Auth Admin Online | — | Authorization REST API |
| COPAUS0C–2C | Auth Screen Programs | — | Authorization Web UI |
| PAUDBLOD/PAUDBUNL/DBUNLDGS | IMS DB Load/Unload | — | PostgreSQL migration scripts |
| COACCT01 | Account MQ Inquiry | — | Eliminated (Account Service API) |
| CODATE01 | Date MQ Inquiry | — | Eliminated (standard Java date API) |

### Data Stores Affected

| Store | Action |
|-------|--------|
| TCATBALF (VSAM) | Migrate to `category_balances` table. Decommission. |
| DISCGRP (VSAM) | Migrate to `disclosure_groups` table. Decommission. |
| IMS DB (DBPAUTP0/DBPAUTX0) | Migrate to `authorizations` table in PostgreSQL. Decommission IMS. |
| DB2 AUTHFRDS | Migrate to `fraud_cases` table in PostgreSQL. Decommission DB2. |
| MQ Queues | Replace with Kafka topics or SQS queues. Decommission MQ. |

### Integration Bridges
- **Interest calc dual-run:** Run both COBOL and Java interest calculations on the same data for 3 monthly cycles. Automated comparison of every account balance.
- **Auth routing:** MQ facade routes authorization requests to either legacy or new authorization service. Traffic gradually shifted (10% → 50% → 100%).
- **IMS migration bridge:** IMS data extracted via PAUDBUNL, transformed, and loaded into PostgreSQL.

### Rollback Plan
- **5a rollback:** Re-enable JCL INTCALC/COMBTRAN jobs. TCATBALF/DISCGRP VSAM files restored from backup. Control-M MONTHLY-InterestCalculation reverts. Rollback must occur before next month's cycle.
- **5b rollback:** MQ facade routes 100% traffic back to legacy CICS authorization programs. IMS DB remains available as fallback. Rollback window: 1 hour (MQ routing change).

### Acceptance Criteria
- [ ] Interest calculation produces identical results for all accounts across 3 monthly dual-run cycles.
- [ ] Disclosure-group rates and compounding rules validated by business SMEs.
- [ ] Authorization service processes requests within SLA (<500ms p99).
- [ ] Fraud detection logic produces identical results to legacy DB2 + IMS path.
- [ ] MQ → Kafka/SQS migration transparent to upstream authorization requestors.
- [ ] IMS data fully migrated to PostgreSQL with zero data loss.
- [ ] All VSAM files (TCATBALF, DISCGRP) decommissioned.
- [ ] Control-M MONTHLY-InterestCalculation updated to new Spring Batch triggers.

---

## Phase 6: Cleanup & Decommissioning (Weeks 47–52)

### Objective
Migrate remaining utilities, decommission all VSAM files, remove the COBOL rehosting environment, and complete the modernization.

### Programs Migrating

| Program | Function | Target |
|---------|----------|--------|
| CBEXPORT | Branch Data Export | Spring Batch ETL (JSON/CSV) |
| CBIMPORT | Branch Data Import | Spring Batch ETL |
| COBSWAIT | Wait Utility | Eliminated |
| CSUTLDTC | Date Utility | Java `java.time` API |
| COBDATFT | Date Format (ASM) | Java `DateTimeFormatter` |
| COMEN01C | Main Menu | Web UI (already delivered) |
| COADM01C | Admin Menu | Web UI (already delivered) |

### Data Stores Affected

| Store | Action |
|-------|--------|
| All remaining VSAM files | Verify zero active references; archive and decommission. |
| Rehosted COBOL environment | Shut down after 4-week soak period with zero traffic. |

### Integration Bridges
- None — all bridges decommissioned. All traffic routes to new services.

### Rollback Plan
- Rehosted COBOL environment maintained in cold standby for 90 days after decommissioning.
- VSAM data archived to S3 (Glacier) for compliance retention.
- In case of critical issues, the rehosted environment can be restarted and the API Gateway reconfigured within 4 hours.

### Acceptance Criteria
- [ ] Zero programs reference VSAM files.
- [ ] All Control-M schedules updated to new Spring Batch / cloud-native triggers.
- [ ] Branch export/import functional via new ETL pipeline.
- [ ] Rehosted COBOL environment receives zero requests for 4 consecutive weeks.
- [ ] COBOL environment decommissioned; cold standby available for 90 days.
- [ ] All EBCDIC data archived to cloud storage with documented retention policy.
- [ ] Complete system operates on modern stack: Java/Kotlin + Spring Boot + PostgreSQL + Kafka.
- [ ] End-to-end integration tests pass for all business scenarios.
- [ ] Performance benchmarks meet or exceed mainframe SLAs.

---

## Timeline Summary

```
Phase 0: Foundation                         [Weeks 1–4]
  ├── Cloud infra, CI/CD, schema design
  └── Rehosting env, API Gateway, CDC pipeline

Phase 1: Identity & User Admin             [Weeks 5–8]        ◄─ Lowest risk
  └── USRSEC → PostgreSQL, Spring Security

Phase 2: Reference Data & Reporting        [Weeks 9–14]
  └── DB2 tables → PostgreSQL, Report engine

Phase 3: Account & Card Services           [Weeks 15–24]      ◄─ Highest value
  └── ACCTDAT/CARDDAT/CCXREF/CUSTDAT → PostgreSQL

Phase 4: Transactions & Payments           [Weeks 25–34]
  ├── 4a: Online transaction CRUD
  └── 4b: Batch posting (dual-run)

Phase 5: Financial Calc & Authorization    [Weeks 35–46]      ◄─ Highest risk
  ├── 5a: Interest calculation (3-month dual-run)
  └── 5b: IMS/DB2/MQ → Kafka/PostgreSQL

Phase 6: Cleanup & Decommissioning         [Weeks 47–52]
  └── Branch migration, utilities, COBOL shutdown
```

---

## Risk Gates Between Phases

Each phase transition requires sign-off on the acceptance criteria above, plus:

| Gate | Requirement |
|------|-------------|
| Phase 0 → 1 | CDC pipeline lag < 1 minute; API Gateway routing verified. |
| Phase 1 → 2 | Zero authentication failures in parallel run. |
| Phase 2 → 3 | Reports match legacy output; reference data CRUD verified. |
| Phase 3 → 4 | Bi-directional sync verified; batch programs unaffected. |
| Phase 4 → 5 | Transaction posting dual-run: zero discrepancies for 4 weeks. |
| Phase 5 → 6 | Interest calculation dual-run: zero discrepancies for 3 monthly cycles. |
