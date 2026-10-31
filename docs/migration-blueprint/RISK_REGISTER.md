# CardDemo COBOL to Java Migration - Risk Register

> Top risks for the migration described in [MIGRATION_BLUEPRINT.md](MIGRATION_BLUEPRINT.md) and [CUTOVER_PLAN.md](CUTOVER_PLAN.md). Each risk is grounded in findings from the COBOL source (see [HOTSPOT_REPORT.md](../modernization-analysis/HOTSPOT_REPORT.md) and [DATA_DICTIONARY.md](../modernization-analysis/DATA_DICTIONARY.md)).
> Scoring: Likelihood (L) and Impact (I) are rated 1-5; Score = L x I. 15 or more = **Critical**, 10-14 = **High**, 5-9 = Medium, 1-4 = Low.

## 1. Heat Map

| Impact \ Likelihood | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|
| **5** | | R-11 | R-03, R-06, R-08 | R-01, R-02 | |
| **4** | | R-14 | R-05, R-07, R-09, R-10 | R-04, R-12 | |
| **3** | | R-16 | R-13, R-15 | R-17 | |
| **2** | | | R-18 | | |

## 2. Register

| ID | Risk | Category | Evidence in the code base | L | I | Score | Mitigation | Contingency / rollback | Phase | Owner |
|---|---|---|---|---|---|---|---|---|---|---|
| R-01 | **Numeric conversion errors**: zoned, packed (COMP-3) and binary fields with implied decimals (`V99`) are converted wrongly, corrupting balances | Data | `ACCT-CURR-BAL PIC S9(10)V99`; CVEXPORT mixes COMP-3, zoned and COMP for the same logical fields | 4 | 5 | **20** | Field-level codecs generated from the copybook layouts; property-based tests per PIC type; hash-total reconciliation of every money column; never use `double` | Re-run conversion from retained unloads; block the cutover if any reconciliation difference is non-zero | 0, 5 | Data lead |
| R-02 | **Business-rule parity loss** in posting and interest (reject codes, category-balance create-or-add, interest truncation, DEFAULT fallback, cycle reset) | Functional | CBTRN02C codes 100/101/102/103/109; CBACT04C `bal x rate / 1200` with no `ROUNDED` anywhere in the code base | 4 | 5 | **20** | Refactor (not rewrite) of CBTRN02C/CBACT04C; `RoundingMode.DOWN`; rule-level unit tests named after the COBOL paragraphs; parallel run of at least 20 daily and 2 monthly cycles | Extend the parallel run; keep the mainframe batch as the system of record until zero differences | 5 | Ledger lead |
| R-03 | **Dual-write / split-brain** if account or transaction writers move separately | Architecture | ACCTDATA written by COACTUPC, COBIL00C, CBTRN02C and CBACT04C; TRANSACT by COTRN02C, COBIL00C, CBTRN02C and COMBTRAN | 3 | 5 | **15** | Move all writers together in Phase 5. Earlier phases are read-only on a replica. No dual writes | Routing rollback to COBOL plus restore from the final unload | 2-5 | Architect |
| R-04 | **Replication lag or inconsistency** in read models during coexistence (Phases 2-4) | Data | Batch updates happen in CLOSEFIL/OPENFIL windows; VSAM has no native change log | 4 | 4 | **16** | Choose a CDC/replication approach in Phase 0 and measure lag; show "as of" time in the UI; reconcile counts daily | Fall back to nightly full refresh; route inquiries back to CICS | 0, 2-4 | Data lead |
| R-05 | **Collation / ordering differences** (EBCDIC vs ASCII/UTF-8) change browse paging, SORT outputs and key ranges | Data | STARTBR/READNEXT/READPREV browsing in COCRDLIC, COTRN00C and COUSR00C; HIGH-VALUES/LOW-VALUES key positioning (46 occurrences); SORT steps in TRANREPT, COMBTRAN and CREASTMT | 3 | 4 | 12 | Use an EBCDIC-compatible collation, or binary ordering on numeric-text keys; golden tests on paging order and sorted outputs | Add explicit ORDER BY expressions that emulate EBCDIC order | 2, 3, 5 | Tech lead |
| R-06 | **PCI DSS / privacy breach** while migrating PAN, CVV, SSN and DOB stored in clear | Security / compliance | `CARD-CVV-CD`, `CARD-NUM`, `CUST-SSN`, `CUST-GOVT-ISSUED-ID` in VSAM and in the CBEXPORT file | 3 | 5 | **15** | Tokenise PAN; do not migrate CVV; encrypt PII at rest and in transit; restrict migration-tool access; mask data in non-prod | Incident response plan; purge non-prod copies | 0, 5 | Security officer |
| R-07 | **Plain-text credentials** carried into the new system or exposed during migration | Security | USRSEC passwords compared in clear (COSGN00C); seeded in DUSRSECJ; FTP host and password in FTPJCL | 3 | 4 | 12 | Do not migrate passwords; IdP with re-enrolment; rotate any credential that appears in JCL; secrets from a vault | Disable legacy accounts at Phase 4 exit | 4 | Security officer |
| R-08 | **Authorization outage or wrong decisions** at switch-over (real-time path) | Operational | COPAUA0C is MQ-triggered; MQGET uses `MQGMO-NO-SYNCPOINT` while IMS updates are under CICS syncpoint | 3 | 5 | **15** | Shadow mode with a 100% decision match before routing; progressive routing by card range; transacted JMS + outbox; latency SLOs | Instant routing rollback to COPAUA0C per card range | 6 | Auth lead |
| R-09 | **Non-idempotent batch on rerun** (double posting or double interest) after a Java job failure | Operational | CBTRN02C and CBACT04C have no restart/idempotency logic (inference from source); TRANBKP deletes and redefines TRANSACT | 3 | 4 | 12 | Spring Batch restartable chunk jobs; idempotency keys (`TRAN-ID`, interest cycle id); one DB transaction per chunk | Point-in-time restore of the ledger schema | 5 | Ledger lead |
| R-10 | **Legacy control flow cannot be translated** (ALTER, GO TO, z/OS control-block access), causing schedule overrun | Technical | CBSTM03A: ALTER and PSA/TIOT POINTER walking; 185 GO TO in 10 programs (COACTUPC 51) | 3 | 4 | 12 | Rewrite these modules from specifications and golden outputs instead of translating them; time-box spikes in Phase 0 | Keep the COBOL statement job running until the rewrite passes | 3, 5 | Tech lead |
| R-11 | **Knowledge loss**: no automated tests; mainframe SMEs not available | People | No test suites in the repo; behaviour only documented by code and sample data | 2 | 5 | 10 | Capture rules in executable tests before conversion; record 3270 scenarios; retain SMEs through hypercare | Extend SME contracts; pause the phase | 0-7 | Program manager |
| R-12 | **Hidden defects propagated or "fixed" silently**, so parity tests disagree with business expectations | Functional | CBPAUP0C duplicated `PA-APPROVED-AUTH-CNT` condition; CBIMPORT CARDOUT without DD; EXPORT.DATA `KEYS(4 28)`; fee stub in CBACT04C | 4 | 4 | **16** | Defect log with a business decision per item (preserve or fix) before the parallel run; tests encode the decision | Toggle behind a feature flag where feasible | 0, 5, 6 | Product owner |
| R-13 | **Concurrency and ID-generation collisions** after moving to multi-instance Java | Technical | COBIL00C and COTRN02C allocate `TRAN-ID` as the last key + 1 | 3 | 3 | 9 | DB sequence/UUID; optimistic locking (`@Version`) for COACTUPC/COCRDUPC | Unique-constraint retry | 5 | Tech lead |
| R-14 | **Batch/online scheduling dependencies broken** (Control-M/CA-7 chains, CLOSEFIL/OPENFIL) | Operational | Control-M daily/weekly/monthly chains; CA-7 triggers CBPAUP0J -> POSTTRAN | 2 | 4 | 8 | Model every chain in the target scheduler; dependency tests in a staging cycle; remove CLOSEFIL/OPENFIL only after data moves | Re-enable mainframe chains | 5 | Ops lead |
| R-15 | **Dual system of record for reference data** diverges during coexistence | Data | TRANSACTION_TYPE/_CATEGORY in DB2, copied weekly to VSAM by TRANEXTR | 3 | 3 | 9 | Keep TRANEXTR running until the last VSAM consumer (CBTRN03C) is gone; freeze reference changes during cutover windows | Re-run TRANEXTR | 1, 3 | Data lead |
| R-16 | **Date and timestamp parsing differences** (text dates, CEEDAYS semantics) | Data | All dates are `X(10)`/`X(26)` text; CSUTLDTC wraps LE CEEDAYS | 2 | 3 | 6 | Strict `java.time` parsing; migration rejects invalid dates to an exceptions file; tests compare CSUTLDTC results | Load invalid dates as NULL plus an exception list for business correction | 0, 5 | Data lead |
| R-17 | **User adoption / retraining** from 3270 screens and PF keys to web UI | Change | 21 BMS maps, PF3/PF7/PF8 navigation | 4 | 3 | 12 | Screen-parity first release; training per phase; super-users | Keep CICS screens available during each phase's hypercare | 2-5 | Change manager |
| R-18 | **Scope gaps** from missing or dormant artifacts | Scope | CSD `CDV1 -> COCRDSEC` has no source; CBTRN01C orphan; UNUSED1Y | 3 | 2 | 6 | Confirm with owners in Phase 0; exclude formally | Add a backlog item if discovered later | 0 | Product owner |

## 3. Top 5 Risks by Score

| Rank | ID | Risk | Score | Key mitigation |
|---|---|---|---|---|
| 1 | R-01 | Numeric conversion errors | 20 | Copybook-generated codecs + hash-total reconciliation |
| 2 | R-02 | Posting/interest rule parity | 20 | Refactor + COBOL truncation + 20/2-cycle parallel run |
| 3 | R-04 | Replication lag during coexistence | 16 | Phase 0 CDC decision, "as of" UI, daily reconciliation |
| 4 | R-12 | Hidden defects | 16 | Preserve-or-fix decision log before the parallel run |
| 5 | R-03 / R-06 / R-08 | Split-brain writes / PCI exposure / authorization outage | 15 | Single ledger cutover / tokenisation / shadow + progressive routing |

## 4. Review Cadence
- Reviewed at each phase gate (CUTOVER_PLAN section 4) and every two weeks during Phases 5 and 6.
- A risk is closed only when its mitigation is verified by evidence (test report, reconciliation sign-off, pen-test report).
