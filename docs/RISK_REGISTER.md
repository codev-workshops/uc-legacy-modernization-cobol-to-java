# Risk Register — CardDemo COBOL-to-Java Migration

## Overview

This register documents the top 10 migration risks ranked by composite score (likelihood × impact). Each risk includes a mitigation strategy and early warning indicators that signal the risk is materializing.

### Scoring Scale

| Score | Likelihood | Impact |
|-------|-----------|--------|
| 5 | Almost certain (>80%) | Catastrophic — financial loss, regulatory violation, extended outage |
| 4 | Likely (60–80%) | Major — multi-day outage, significant rework, data integrity issue |
| 3 | Possible (40–60%) | Moderate — days of delay, partial feature loss, workaround needed |
| 2 | Unlikely (20–40%) | Minor — hours of delay, cosmetic issues, easy fix |
| 1 | Rare (<20%) | Negligible — no user impact, documentation-only fix |

---

## Risk Register

### RISK-01: Financial Calculation Discrepancy

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-01 |
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Catastrophic) |
| **Composite Score** | **20** |
| **Affected Programs** | CBTRN02C (posting), CBACT04C (interest), COBIL00C (bill payment) |
| **Affected Phase** | Phase 2 (Financial Core) |

**Description:** The Java implementations of transaction posting, interest calculation, and bill payment produce different financial results than the COBOL originals due to differences in decimal arithmetic handling, rounding behavior, or edge-case logic.

COBOL uses fixed-point decimal arithmetic (`PIC S9(10)V99`) with truncation semantics. Java's `BigDecimal` uses banker's rounding by default. A 1-cent discrepancy across millions of accounts compounds into material financial variance.

**Mitigation Strategy:**
1. Mandate `BigDecimal` with `RoundingMode.DOWN` (truncation) to match COBOL behavior for all financial calculations.
2. Build a parallel-run reconciliation framework that compares every account balance, category balance, and interest charge between COBOL and Java outputs — to the cent.
3. Run 30 consecutive days of parallel posting before cutover. Run 2 full billing cycles of parallel interest calculation.
4. Create a "golden file" test suite: 500+ accounts with known balances, processed through both systems, with automated comparison.
5. Engage an external auditor to validate the reconciliation methodology.

**Early Warning Indicators:**
- Any non-zero balance discrepancy during parallel-run, even $0.01
- DALYREJS (reject file) record counts differ between COBOL and Java
- Category balance totals (TCATBALF) don't match after posting
- Interest charges differ for accounts in the same disclosure group

---

### RISK-02: CSLKPCDY Validation Rule Extraction Failure

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-02 |
| **Category** | Business Logic |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Composite Score** | **16** |
| **Affected Programs** | COACTUPC (4,236 LOC — largest program in estate) |
| **Affected Phase** | Phase 2C (Account Management) |

**Description:** COACTUPC embeds 1,318 lines of validation lookup data (CSLKPCDY copybook) containing phone area codes, US state codes, and ZIP prefix mappings as COBOL 88-level condition names. Extracting these rules into a modern validation service while preserving 100% behavioral equivalence is error-prone.

The validation rules are deeply woven into COACTUPC's procedural flow — field-level validation triggers screen attribute changes (CSSETATY — red highlighting, asterisk markers) and error messages. Missing a single area code or ZIP prefix would cause the new system to reject valid data or accept invalid data.

**Mitigation Strategy:**
1. Parse CSLKPCDY programmatically — extract all 88-level condition names and their VALUE clauses into a structured format (JSON/database).
2. Build a Validation Reference Service with a REST API: `POST /validate/phone`, `POST /validate/address`.
3. Create exhaustive test cases: every area code, every state code, every ZIP prefix in CSLKPCDY.
4. Run the COBOL COACTUPC and Java Account Service against the same set of 10,000 synthetic customer records — compare validation outcomes.
5. Source NANPA area codes from an authoritative external provider to cross-validate CSLKPCDY data freshness.

**Early Warning Indicators:**
- Account updates rejected in Java that succeed in COBOL (or vice versa)
- Customer address changes failing for specific states or ZIP codes
- Phone number validation discrepancies for area codes in the 800–899 range (toll-free codes change frequently)
- CSLKPCDY data is stale (hasn't been updated for new area codes since the codebase was frozen)

---

### RISK-03: Online/Batch Transaction Asymmetry Regression

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-03 |
| **Category** | Architecture |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Catastrophic) |
| **Composite Score** | **15** |
| **Affected Programs** | COTRN02C (online add), CBTRN02C (batch posting) |
| **Affected Phase** | Phase 2A (Transaction Processing) |

**Description:** The legacy system has a known asymmetry: online transaction add (COTRN02C) writes to TRANSACT but does NOT update TCATBALF or account balances. Batch posting (CBTRN02C) updates all three. The modernization plan intends to unify this behavior so that all transaction creation updates balances synchronously.

This architectural change means the new system will behave differently from the legacy system by design. However, if the unification is done incorrectly — or if some edge cases rely on the delayed-update behavior — the result could be double-counting, missing interest calculations, or balance drift.

**Mitigation Strategy:**
1. Document the asymmetry explicitly in the Transaction Service design spec (reference DEPENDENCY_MAP.md § 5.1).
2. During parallel-run, account for the asymmetry in reconciliation: online transactions in COBOL won't update balances until the next batch cycle; Java updates them immediately.
3. Design the reconciliation to compare balances at batch-cycle boundaries (after COBOL batch has processed), not in real-time.
4. Add a feature flag to the Transaction Service: `SYNC_BALANCE_UPDATE=true/false` — allowing rollback to the legacy async pattern if issues arise.
5. Test edge case: what happens when an online transaction is added between batch close (CLOSEFIL) and batch post (POSTTRAN)?

**Early Warning Indicators:**
- Balance drift between COBOL and Java systems that resolves after each batch cycle (indicates the asymmetry isn't accounted for in reconciliation)
- Interest calculations differ because Java includes online-added transactions in category balances earlier than COBOL
- Reports show different transaction counts for the same period depending on when they run relative to the batch cycle

---

### RISK-04: DALYTRAN External Feed Source Unknown

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-04 |
| **Category** | Integration |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Catastrophic) |
| **Composite Score** | **15** |
| **Affected Programs** | CBTRN02C (consumes DALYTRAN) |
| **Affected Phase** | Phase 2A (Transaction Processing) |

**Description:** The DALYTRAN file (daily transaction feed) is documented as "(External feed)" with no specification of where it comes from. Possible sources include an external clearing house, a payment network, or an internal extraction from the IMS authorization database. Without understanding the DALYTRAN source, we cannot design the equivalent ingest mechanism for the new Transaction Service.

If the source cannot be identified or cannot be modified to publish to Kafka/API, the entire transaction posting pipeline is blocked.

**Mitigation Strategy:**
1. **Immediate action (Phase 0):** Engage mainframe operations team to trace DALYTRAN provenance — check JCL scheduler dependencies, FTP logs, and upstream job chains.
2. Build a file-to-Kafka adapter as an interim bridge: watch for DALYTRAN file arrival → parse records → publish to Kafka topic.
3. If the source is the authorization sub-app (IMS → DALYTRAN), plan the Authorization Service migration to produce events directly to the Transaction Service.
4. If the source is an external payment network, negotiate API/SFTP delivery to a modern endpoint.
5. Maintain the DALYTRAN file format adapter for at least 6 months post-cutover as a fallback.

**Early Warning Indicators:**
- Operations team cannot identify DALYTRAN source within 2 weeks of investigation
- DALYTRAN file format or arrival timing changes without notice
- Authorization Service migration (Phase 3) reveals dependencies on DALYTRAN not documented in the codebase
- Multiple DALYTRAN files arrive per day (suggesting COMBTRAN.jcl is actively used — adds complexity)

---

### RISK-05: IMS-to-Relational Data Migration Fidelity

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-05 |
| **Category** | Data Migration |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Composite Score** | **12** |
| **Affected Programs** | COPAUA0C, COPAUS0C–2C, CBPAUP0C, PAUDBLOD, PAUDBUNL |
| **Affected Phase** | Phase 3 (Authorization Service) |

**Description:** The authorization sub-application stores pending authorizations in an IMS hierarchical database. IMS data structures (segments, hierarchies, secondary indexes) do not map 1:1 to relational tables. Parent-child relationships in IMS are implicit in the physical storage; in PostgreSQL they require explicit FK constraints.

The PAUDBUNL (unload) program extracts IMS data, but the output format and completeness need to be validated. If the unload misses secondary index data or hierarchical relationships, the migrated data will be incomplete.

**Mitigation Strategy:**
1. Run PAUDBUNL to extract all IMS data before migration. Compare record counts against IMS database statistics.
2. Map the IMS DBD (Database Description) to a relational schema. Document each segment → table mapping with cardinality.
3. Build a bi-directional verification: load extracted data into PostgreSQL, then export it back to IMS format and compare byte-for-byte with the original unload.
4. Test COPAUA0C authorization decisions against the PostgreSQL data to verify all fraud flags and account lookups return identical results.
5. If IMS data is too complex to migrate reliably, consider the Authorization Service as a "greenfield" implementation that starts with empty history and only processes new authorizations.

**Early Warning Indicators:**
- IMS unload record count doesn't match expected authorization history
- PAUDBUNL fails or produces incomplete output for certain segment types
- IMS DBD documentation is missing or outdated
- Authorization decisions differ between IMS-backed and PostgreSQL-backed systems for historical authorizations

---

### RISK-06: COBOL Talent Shortage During Migration

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-06 |
| **Category** | People / Organizational |
| **Likelihood** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Composite Score** | **12** |
| **Affected Programs** | All — especially COACTUPC (4,236 LOC), COTRTLIC (2,098 LOC) |
| **Affected Phase** | All phases |

**Description:** The migration requires deep COBOL expertise to verify behavioral equivalence between legacy and new systems. COBOL developers are increasingly scarce; the ones who understand CardDemo's business logic may retire or become unavailable during the 12–18 month migration.

Without COBOL expertise, validating that the Java implementation matches the COBOL behavior — especially for undocumented edge cases, implicit truncation, and GO TO-based control flow — becomes unreliable.

**Mitigation Strategy:**
1. **Knowledge capture sprint (Phase 0):** Conduct structured interviews with COBOL developers for each functional area. Document undocumented business rules, edge cases, and "tribal knowledge."
2. **Automated test generation:** Use the existing COBOL programs + test data to generate golden-file test cases. For each program, capture: input data → expected output data. These tests survive after the COBOL developers leave.
3. **Pair programming:** Assign COBOL developers to pair with Java developers during the rewrite. The COBOL developer explains the logic; the Java developer implements it.
4. **Contract COBOL consultants:** Budget for specialist consultants for Phases 2 and 3 as a safety net.
5. **Self-documenting code:** The existing docs (APPLICATION_INVENTORY, DEPENDENCY_MAP, DATA_DICTIONARY, HOTSPOT_REPORT) reduce but do not eliminate the need for COBOL expertise.

**Early Warning Indicators:**
- COBOL developer announces retirement or role change during the migration
- Java developer cannot explain the purpose of a COBOL paragraph without COBOL developer help
- Number of "unknown behavior" items in the test suite grows
- Parallel-run discrepancies that no one can explain

---

### RISK-07: Authorization Latency SLA Breach

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-07 |
| **Category** | Performance |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Composite Score** | **12** |
| **Affected Programs** | COPAUA0C (real-time authorization) |
| **Affected Phase** | Phase 3 (Authorization Service) |

**Description:** COPAUA0C processes real-time card authorizations via MQ request/reply. This is a latency-sensitive, revenue-critical path — every authorization decision must complete within the payment network's timeout window (typically 2–5 seconds end-to-end).

The new Authorization Service adds network hops (API Gateway → Auth Service → Account Service API → Card Service API → Database) that don't exist in the monolithic CICS/MQ/VSAM architecture. Each API call adds latency.

**Mitigation Strategy:**
1. **Baseline current latency:** Measure COPAUA0C's end-to-end response time (MQ GET → decision → MQ PUT) in production. This is the SLA target.
2. **Minimize network hops:** Consider co-locating Authorization Service with Account/Card data (read replicas or cached data) rather than making synchronous API calls.
3. **Pre-load fraud flags:** Cache fraud flags in-memory (Redis) rather than querying the database for every authorization.
4. **Load test before cutover:** Simulate peak authorization volume (estimate from production MQ queue depth) against the new service.
5. **Circuit breaker pattern:** If Account Service is slow, the Authorization Service should decline-with-reason rather than timeout.

**Early Warning Indicators:**
- Load test P99 latency exceeds 200ms
- Database query time for fraud flag lookup exceeds 10ms
- API calls to Account/Card Services add >50ms per hop
- MQ-to-Kafka message delivery latency is non-trivial (>20ms)

---

### RISK-08: Data Migration — EBCDIC/COMP/COMP-3 Conversion Errors

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-08 |
| **Category** | Data Migration |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Composite Score** | **12** |
| **Affected Programs** | All batch programs, CBEXPORT (uses COMP/COMP-3 in export records) |
| **Affected Phase** | Phase 0 (Foundation) |

**Description:** CardDemo data exists in EBCDIC encoding with packed-decimal (COMP-3), binary (COMP), signed zoned decimal, and fixed-width record formats. Converting this data to PostgreSQL requires byte-level parsing that handles:
- EBCDIC → ASCII character conversion
- COMP-3 packed decimal (nibble-based) to Java BigDecimal
- COMP binary (big-endian) to Java integers
- Signed zoned decimal (sign in the zone nibble of the last byte)
- Fixed-width record parsing with FILLER fields

A single byte-offset error in record parsing corrupts all subsequent fields in that record and every record that follows.

**Mitigation Strategy:**
1. **Use ASCII test data first:** The `app/data/ASCII/` directory contains ASCII-encoded test fixtures. Validate the parsing pipeline against these before attempting EBCDIC conversion.
2. **Validate field-by-field:** For each copybook record layout, write unit tests that parse a known record and assert each field value.
3. **Count-based validation:** Compare record counts per file between VSAM and PostgreSQL. Compare sum/min/max of numeric fields.
4. **Use proven EBCDIC libraries:** JRecord, IBM Data Studio, or Precisely (formerly Syncsort). Do not write custom EBCDIC parsers.
5. **Round-trip test:** Export PostgreSQL data back to fixed-width format and compare byte-for-byte against the original VSAM data.

**Early Warning Indicators:**
- Numeric fields in PostgreSQL don't match expected ranges (e.g., negative account IDs)
- Character fields contain garbage or control characters after conversion
- Record count in PostgreSQL doesn't match VSAM file record count
- COMP-3 fields show values that are exactly 2× or 0.5× the expected value (nibble-shift error)

---

### RISK-09: Integration Bridge Accumulation

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-09 |
| **Category** | Architecture |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Composite Score** | **9** |
| **Affected Programs** | All — bridges connect old and new systems |
| **Affected Phase** | Phases 1–3 |

**Description:** The phased migration requires multiple integration bridges (USRSEC Dual-Write, VSAM Sync, DALYTRAN Feed Adapter, Transaction Read Bridge, Reference Data Sync, MQ-to-Kafka Bridge). Each bridge is a piece of custom middleware that must be built, tested, monitored, and maintained.

If the migration schedule slips, bridges accumulate and compound operational complexity. Each bridge is a potential point of failure. A cascade of bridge failures during a batch cycle could corrupt data across both systems.

**Mitigation Strategy:**
1. **Track bridge inventory:** Maintain an explicit registry of active bridges with their decommission triggers (documented in CUTOVER_PLAN.md).
2. **Set bridge TTLs:** Each bridge has a maximum lifetime (e.g., 3 months). If it's still active past its TTL, escalate to project leadership.
3. **Monitor bridge health:** Each bridge gets its own health check, error counter, and alerting threshold.
4. **Minimize bridges by accelerating phases:** The faster we complete each phase, the sooner we decommission bridges. Don't let Phase 2 slip while bridges from Phase 1 accumulate.
5. **Prefer one-directional bridges:** VSAM → PostgreSQL (read from VSAM, write to PostgreSQL) is safer than bidirectional sync. Only use dual-write when absolutely necessary (USRSEC).

**Early Warning Indicators:**
- More than 5 bridges active simultaneously
- A bridge fails silently (no alerting triggered) and data drifts
- Bridge maintenance consumes >20% of team capacity
- Phase timeline slips by >4 weeks while bridges are active
- Dual-write bridge creates conflicting records (USRSEC and PostgreSQL diverge)

---

### RISK-10: Cleartext Password Exposure During Migration

| Attribute | Value |
|-----------|-------|
| **ID** | RISK-10 |
| **Category** | Security |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 5 (Catastrophic) |
| **Composite Score** | **10** |
| **Affected Programs** | COSGN00C, COUSR00C–03C, USRSEC file |
| **Affected Phase** | Phase 1 (Auth Service) |

**Description:** The USRSEC file stores passwords in cleartext (PIC X(08)). During migration, these passwords must be extracted from VSAM, transformed (hashed), and loaded into PostgreSQL. At multiple points in this pipeline, cleartext passwords are in transit or in temporary storage:
- VSAM file on the mainframe (existing exposure)
- ETL pipeline output (new exposure)
- Network transfer between mainframe and cloud (new exposure)
- Temporary staging tables or files (new exposure)

A breach at any point exposes all user credentials.

**Mitigation Strategy:**
1. **Hash on extraction:** The ETL pipeline must bcrypt-hash passwords at the earliest possible point — ideally as part of the VSAM read step, before data leaves the mainframe.
2. **Never store cleartext in PostgreSQL:** The `users` table stores bcrypt hashes only. There is no migration path that involves cleartext passwords in PostgreSQL, even temporarily.
3. **Force password reset:** After migration, require all users to reset their passwords on first login to the new Auth Service. This eliminates the need to migrate legacy passwords at all (preferred approach).
4. **Encrypt ETL pipeline:** All data in transit must use TLS. Temporary files must be encrypted at rest and deleted after migration.
5. **Audit trail:** Log every access to the USRSEC file and the password migration pipeline. Alert on any unexpected reads.
6. **Security review:** Engage security team to review the password migration pipeline before execution.

**Early Warning Indicators:**
- ETL pipeline logs contain password field values (even partially)
- Temporary files with cleartext data exist for more than 1 hour
- Network traffic between mainframe and cloud is unencrypted
- Password migration step is not included in the security review scope
- Users can log in with their old 8-character passwords without being forced to reset

---

## Risk Summary Matrix

| Rank | Risk ID | Risk | Likelihood | Impact | Score | Phase |
|------|---------|------|:----------:|:------:|:-----:|-------|
| 1 | RISK-01 | Financial Calculation Discrepancy | 4 | 5 | **20** | Phase 2 |
| 2 | RISK-02 | CSLKPCDY Validation Extraction | 4 | 4 | **16** | Phase 2C |
| 3 | RISK-03 | Online/Batch Asymmetry Regression | 3 | 5 | **15** | Phase 2A |
| 4 | RISK-04 | DALYTRAN Source Unknown | 3 | 5 | **15** | Phase 2A |
| 5 | RISK-05 | IMS Data Migration Fidelity | 3 | 4 | **12** | Phase 3 |
| 6 | RISK-06 | COBOL Talent Shortage | 4 | 3 | **12** | All |
| 7 | RISK-07 | Authorization Latency SLA | 3 | 4 | **12** | Phase 3 |
| 8 | RISK-08 | EBCDIC/COMP-3 Conversion Errors | 3 | 4 | **12** | Phase 0 |
| 9 | RISK-10 | Cleartext Password Exposure | 2 | 5 | **10** | Phase 1 |
| 10 | RISK-09 | Integration Bridge Accumulation | 3 | 3 | **9** | Phases 1–3 |

---

## Risk Burndown Targets

| Phase | Risks Mitigated | Residual Risks |
|-------|----------------|----------------|
| Phase 0 | RISK-08 (data conversion validated), RISK-04 (DALYTRAN source identified), RISK-06 (knowledge capture started) | 7 remaining |
| Phase 1 | RISK-10 (passwords hashed/reset) | 6 remaining |
| Phase 2 | RISK-01 (parallel-run validated), RISK-02 (CSLKPCDY extracted), RISK-03 (asymmetry resolved) | 3 remaining |
| Phase 3 | RISK-05 (IMS migrated), RISK-07 (latency validated) | 1 remaining |
| Phase 4 | RISK-09 (all bridges decommissioned) | **0 remaining** |
