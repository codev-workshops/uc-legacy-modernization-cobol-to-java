# CardDemo Reconciliation Checks (per job)

Validation specs for every CardDemo batch job that moves or changes data. A migrated (Java / Spring Batch) job is accepted
only when every check for that job passes against the same inputs as the COBOL job. Strategy and the other test dimensions
are in [TEST_STRATEGY.md](TEST_STRATEGY.md).

**Status column.**
- **Automated:** implemented in [`test-harness/carddemo_harness/reconcile.py`](../../test-harness/carddemo_harness/reconcile.py), run by `./run_tests.sh`, with its baseline result in [`golden-files/jobs/reconciliation-baseline.json`](../../golden-files/jobs/reconciliation-baseline.json).
- **Spec:** defined here, to be automated when that job's migration phase starts.

**Tolerance.** Zero for every count and money check unless stated otherwise. Timestamps written by the job (`TRAN-PROC-TS`, interest `TRAN-ORIG-TS`) are excluded from record-level comparison.

## 0. Master-data preconditions (run before every batch cycle)

| Check ID | Rule | Status | Baseline |
|---|---|---|---|
| `MASTER.card.fk.CARD-ACCT-ID` | Every card's account exists in ACCTDATA | Automated | 0 orphans |
| `MASTER.card_xref.fk.XREF-CARD-NUM` | Every xref card exists in CARDDATA | Automated | 0 orphans |
| `MASTER.card_xref.fk.XREF-ACCT-ID` | Every xref account exists in ACCTDATA | Automated | 0 orphans |
| `MASTER.card_xref.fk.XREF-CUST-ID` | Every xref customer exists in CUSTDATA | Automated | 0 orphans |
| `MASTER.category_balance.fk.TRANCAT-ACCT-ID` | Every TCATBALF account exists in ACCTDATA | Automated | 0 orphans |
| `MASTER.transaction_category.fk.TRAN-TYPE-CD` | Every category's type exists in TRANTYPE | Automated | 0 orphans |
| `MASTER.account.domain.ACCT-ACTIVE-STATUS` | Value in {Y, N} | Automated | pass |
| `MASTER.card.domain.CARD-ACTIVE-STATUS` | Value in {Y, N} | Automated | pass |
| `MASTER.daily_transaction.domain.DALYTRAN-TYPE-CD` | Daily type code exists in TRANTYPE | Automated | pass |
| `MASTER.daily_transaction.fk.type-category` | Daily (type, category) exists in TRANCATG | Automated | pass |

## 1. Initial-load jobs (IDCAMS REPRO into VSAM / Flyway + loader into the target DB)

Applies to ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANTYPE, TRANCATG, DISCGRP and TCATBALF. The same checks are reused at the Phase 5 unload and load cutover, with the unloaded VSAM as the source.

| Job | Dataset (copybook) | Checks (Automated) | Baseline |
|---|---|---|---|
| ACCTFILE | ACCTDATA (CVACT01Y) | `.count`, `.unique-key` (ACCT-ID), `.sum` of ACCT-CURR-BAL, ACCT-CREDIT-LIMIT, ACCT-CASH-CREDIT-LIMIT, ACCT-CURR-CYC-CREDIT, ACCT-CURR-CYC-DEBIT | 50 rows; 12269.00 / 233711.00 / 122148.00 / 0.00 / 0.00 |
| CARDFILE | CARDDATA (CVACT02Y) | `.count`, `.unique-key` (CARD-NUM) | 50 |
| CUSTFILE | CUSTDATA (CVCUS01Y) | `.count`, `.unique-key` (CUST-ID) | 50 |
| XREFFILE | CARDXREF (CVACT03Y) | `.count`, `.unique-key` (XREF-CARD-NUM) | 50 |
| TRANTYPE | TRANTYPE (CVTRA03Y) | `.count`, `.unique-key` (TRAN-TYPE) | 7 |
| TRANCATG | TRANCATG (CVTRA04Y) | `.count`, `.unique-key` (TRAN-TYPE-CD + TRAN-CAT-CD) | 18 |
| DISCGRP | DISCGRP (CVTRA02Y) | `.count`, `.unique-key` (group + type + category), `.sum.DIS-INT-RATE` | 51; 375.00 |
| TCATBALF | TCATBALF (CVTRA01Y) | `.count`, `.unique-key` (account + type + category), `.sum.TRAN-CAT-BAL` | 50; 0.00 |

Additional Spec checks for the target database load:
- Record-level `compare` of every row against the golden file (zero mismatches).
- The `contract` check passes for every row.
- A hash total per key range (for example, the sum of ACCT-ID per 10,000 accounts) to localise any difference.

## 2. POSTTRAN: daily posting (CBTRN02C)

Inputs:
- DALYTRAN.PS (CVTRA06Y)
- CARDXREF
- ACCTDATA
- TCATBALF

Outputs:
- TRANSACT writes (CVTRA05Y)
- DALYREJS(+1) rejects with reason code
- ACCTDATA and TCATBALF rewrites
- RC 4 if any reject

| Check ID | Rule | Status | Baseline (sample data) |
|---|---|---|---|
| `POSTTRAN.count.in=posted+rejected` | DALYTRAN records = TRANSACT writes + DALYREJS writes | Automated | 300 = 262 + 38 |
| `POSTTRAN.amount.in=posted+rejected` | Σ DALYTRAN-AMT = Σ posted + Σ rejected | Automated | 104801.54 |
| `POSTTRAN.acct.balance-delta` | Δ Σ ACCT-CURR-BAL = Σ posted TRAN-AMT | Automated | 77954.70 |
| `POSTTRAN.acct.cycle-credit-delta` | Δ Σ ACCT-CURR-CYC-CREDIT = Σ posted amounts ≥ 0 | Automated | 102353.99 |
| `POSTTRAN.acct.cycle-debit-delta` | Δ Σ ACCT-CURR-CYC-DEBIT = Σ posted amounts < 0 | Automated | -24399.29 |
| `POSTTRAN.tcatbal.balance-delta` | Δ Σ TRAN-CAT-BAL = Σ posted TRAN-AMT | Automated | 77954.70 |
| `POSTTRAN.tcatbal.rows-created` | New TCATBALF rows = new (account, type, category) keys | Automated | 50 |
| `POSTTRAN.acct.count-unchanged` | ACCTDATA record count unchanged | Automated | 50 |
| `POSTTRAN.reject.reason-codes` | Every reject code in {100, 101, 102, 103, 109} | Automated | {102: 38} |
| `POSTTRAN.return-code` | RC = 4 when any reject, else 0 | Automated | 4 |
| `POSTTRAN.transact.unique-key` | No duplicate TRAN-ID written | Automated | pass |
| `POSTTRAN.diff.transact` | TRANSACT writes equal `POSTTRAN.expected.json` `transact` by TRAN-ID, ignoring TRAN-PROC-TS | Spec (expected output generated) | 262 records |
| `POSTTRAN.diff.dalyrejs` | Rejected IDs and reason codes equal `dalyrejs` | Spec (expected output generated) | 38 records |
| `POSTTRAN.diff.acctdata` | Every account equals `acctdata_after` | Spec (expected output generated) | 50 records |
| `POSTTRAN.diff.tcatbalf` | Every category balance equals `tcatbalf_after` | Spec (expected output generated) | 100 records |
| `POSTTRAN.rerun.idempotent` | Rerunning the same DALYTRAN after a restart must not change balances a second time | Spec | COBOL re-applies (inference); Java must use a run-id guard |
| `POSTTRAN.reject.layout` | DALYREJS = 350-byte transaction + 4-digit code + 76-byte description | Spec | — |

## 3. INTCALC: monthly interest (CBACT04C)

Inputs:
- TCATBALF
- CARDXREF
- ACCTDATA
- DISCGRP
- PARM date

Outputs:
- SYSTRAN(+1) interest transactions
- ACCTDATA rewrites (balance + interest; cycle totals set to 0)

The golden run is chained on the POSTTRAN result with PARM-DATE `2022-07-21`.

| Check ID | Rule | Status | Baseline |
|---|---|---|---|
| `INTCALC.acct.balance-delta` | Δ Σ ACCT-CURR-BAL = Σ interest of updated accounts | Automated | 1260.39 (of 1279.16 written; 18.77 for the last account is not applied, finding F-2) |
| `INTCALC.acct.cycle-reset` | Updated accounts have ACCT-CURR-CYC-CREDIT = ACCT-CURR-CYC-DEBIT = 0 | Automated | pass |
| `INTCALC.acct.untouched` | Accounts without TCATBALF rows are unchanged | Automated | pass |
| `INTCALC.tran.id-format` | TRAN-ID = PARM-DATE (10) + 6-digit sequence | Automated | pass |
| `INTCALC.tran.type` | Type 01, category 05, source `System` | Automated | pass |
| `INTCALC.interest_tran.unique-key` | No duplicate TRAN-ID | Automated | pass |
| `INTCALC.tran.count` | ≤ one interest transaction per TCATBALF row (zero rates write none) | Automated | 50 of 100 rows |
| `INTCALC.diff.systran` | Interest transactions equal `INTCALC.expected.json` `systran` (ignoring TRAN-ORIG-TS/PROC-TS) | Spec (expected output generated) | 50 records |
| `INTCALC.diff.acctdata` | Accounts equal `acctdata_after` | Spec (expected output generated) | 50 records |
| `INTCALC.formula` | Each amount = trunc(TRAN-CAT-BAL × rate / 1200, 2) toward zero | Automated as unit tests | `test_models.py` |
| `INTCALC.rate.default` | Group fallback to `DEFAULT` counted and reported | Automated as unit tests | 100 of 100 lookups use DEFAULT (finding F-1) |
| `INTCALC.rate.missing-abend` | No group or DEFAULT rate leads to an abend, not a silent zero | Automated as unit tests | — |
| `INTCALC.fees` | `1400-COMPUTE-FEES` is empty; Java must write no fee transactions until the business specifies them | Spec | 0 fee transactions |

## 4. COMBTRAN: merge interest into TRANSACT (SORT + IDCAMS REPRO)

| Check ID | Rule | Status |
|---|---|---|
| `COMBTRAN.count` | Output records = TRANSACT backup + SYSTRAN(0) | Spec |
| `COMBTRAN.amount` | Σ TRAN-AMT out = Σ backup + Σ SYSTRAN | Spec |
| `COMBTRAN.unique-key` | TRAN-ID unique after the merge, so interest IDs cannot collide with daily IDs | Spec |
| `COMBTRAN.order` | Output sorted by TRAN-ID (KSDS key order, EBCDIC collation on the mainframe vs UTF-8 in Java; see R-05) | Spec |

## 5. TRANBKP: TRANSACT backup and redefine

| Check ID | Rule | Status |
|---|---|---|
| `TRANBKP.count` | Backup GDG(+1) count = TRANSACT count before the delete/define | Spec |
| `TRANBKP.hash` | Σ TRAN-AMT and a hash of TRAN-ID identical between source and backup | Spec |

## 6. TRANREPT: transaction detail report (SORT + CBTRN03C)

Inputs:
- TRANSACT backup filtered by the DATEPARM range
- CARDXREF
- TRANTYPE
- TRANCATG

| Check ID | Rule | Status |
|---|---|---|
| `TRANREPT.selection` | Report lines = transactions with TRAN-PROC-TS date within DATEPARM start/end | Spec |
| `TRANREPT.account-totals` | Each account total = Σ TRAN-AMT of its selected transactions | Spec |
| `TRANREPT.grand-total` | Grand total = Σ account totals = Σ selected TRAN-AMT | Spec |
| `TRANREPT.lookups` | Every line has a type and category description (no blank lookups) | Spec |
| `TRANREPT.layout` | Report record width and header/trailer lines match the COBOL output (contract) | Spec |

## 7. CREASTMT: customer statements (SORT + IDCAMS + CBSTM03A/B)

| Check ID | Rule | Status |
|---|---|---|
| `CREASTMT.statement-count` | One statement per card in CARDXREF that has transactions in TRXFL | Spec |
| `CREASTMT.statement-total` | Each statement total = Σ TRAN-AMT for that card | Spec |
| `CREASTMT.grand-total` | Σ statement totals = Σ TRAN-AMT in TRXFL | Spec |
| `CREASTMT.header` | Customer name and address and the account balance on each statement equal CUSTDATA/ACCTDATA | Spec |
| `CREASTMT.formats` | Text and HTML statements carry identical figures | Spec |

## 8. CBEXPORT / CBIMPORT: branch data exchange

| Check ID | Rule | Status |
|---|---|---|
| `CBEXPORT.count-by-type` | Export records per type (C, A, X, T, D) = source file counts | Spec |
| `CBEXPORT.control-totals` | Σ account balances and Σ TRAN-AMT in the export = source | Spec |
| `CBIMPORT.round-trip` | Import of an export reproduces each source file exactly (record compare by key) | Spec |
| `CBIMPORT.errors` | ERROUT records = export records with unknown type; 0 for a clean export | Spec |
| `CBIMPORT.card-output` | CARDOUT is opened by the program but has no DD in CBIMPORT.jcl; Java must define the card output explicitly | Spec |

## 9. CBPAUP0J: purge expired pending authorizations (CBPAUP0C, IMS)

| Check ID | Rule | Status |
|---|---|---|
| `CBPAUP0J.purged` | Details deleted = details older than the expiry days (default 5) | Spec |
| `CBPAUP0J.retained` | Details remaining + deleted = details before | Spec |
| `CBPAUP0J.summary` | A summary segment is deleted only when its approved and declined counts are both 0. COBOL checks the approved count twice (defect), so record parity and the fix separately | Spec |
| `CBPAUP0J.checkpoint` | Restart from the last checkpoint purges no record twice | Spec |

## 10. Reference-data jobs (MNTTRDB2 / TRANEXTR, DB2)

| Check ID | Rule | Status |
|---|---|---|
| `TRANEXTR.vsam-db2-parity` | TRANSACTION_TYPE and TRANSACTION_TYPE_CATEGORY rows equal the TRANTYPE/TRANCATG files (count + record compare) | Spec |
| `MNTTRDB2.apply` | Rows after = rows before + inserts - deletes, with updates applied, for the INPFILE batch | Spec |
| `MNTTRDB2.fk` | Every category's type exists (DB2 foreign key) | Spec |

## 11. Evidence and sign-off

Each run of a migrated job stores four pieces of evidence:
- the `reconcile` JSON report
- the `compare` summary per output
- the JUnit XML from `./run_tests.sh`
- the input file hashes

Phase go/no-go (see [CUTOVER_PLAN.md](../migration-blueprint/CUTOVER_PLAN.md)) requires every Automated check, and every Spec check for the jobs in that phase, to pass with zero tolerance.
