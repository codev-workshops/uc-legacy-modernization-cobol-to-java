# CardDemo COBOL Estate — Hotspot Report

Static-complexity ranking of all 44 COBOL programs under `app/`, followed by modernization-order recommendations.

> **Caveats (read first)**
> * Every number below is a **static-analysis proxy** measured from the source files in this repository. No runtime profiling, transaction volumes, CPU or abend data were available; "hot" here means *complex, coupled and financially sensitive*, not *frequently executed*.
> * Regulatory statements (interest/APR disclosure, statement accuracy, PII/password handling) are **generic banking-domain inferences**, not a review against any specific regulation applicable to this system.
> * The brief's prior figures (e.g. "COACTUPC ~2500+ lines, ~128 PERFORM, ~34 EXEC CICS") were **not** relied upon; all metrics were re-measured and several differ materially (see §2).

## 1. Method

| Metric | How measured |
|---|---|
| Code lines | Lines in cols 8–72 after removing fixed-format comment (`*`, `/` in col 7) and blank lines; total physical lines shown in parentheses. |
| COPY count | Distinct `COPY xxx` statements (BMS symbolic copybooks and `DFHAID`/`DFHBMSCA` included; `EXEC SQL INCLUDE` not counted). |
| I/O operations | `SELECT … ASSIGN` clauses + distinct `EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT/READPREV/ENDBR` on a `DATASET`/`FILE` + `EXEC SQL` statements (excluding `INCLUDE`) + `EXEC DLI` statements + MQI `CALL`s. |
| IF / EVALUATE / PERFORM / COMPUTE | Keyword counts in the `PROCEDURE DIVISION` (comment lines removed). `PERFORM` includes inline and out-of-line; `COMPUTE` excludes `ADD`/`SUBTRACT`/`MULTIPLY`. |
| Max nesting | Deepest stack of open `IF`/`EVALUATE` blocks (closed by `END-IF`/`END-EVALUATE`; period-terminated IFs may under-count by one level). |
| Fan-out / fan-in | Program-to-program edges from `DEPENDENCY_MAP.md` §1–2 (XCTL literals, runtime-resolved menu tables, LINK, `CALL 'literal'`); system routines (`CEEDAYS`, `CBLTDLI`, `MQ*`, `DSNTIAC`) excluded. |
| Score | Weighted sum of each metric normalised to the estate maximum: lines 20, IF 15, I/O 15, COPY 10, EVALUATE 10, PERFORM 10, fan (in+out) 10, nesting 5, COMPUTE 5 (= 100 max). Weights are a judgement call, stated so they can be changed. |

## 2. Scored table — top 10 (full 44-row table in Appendix A)

| # | Program | Code lines (total) | COPY | I/O ops (SELECT/CICS file/SQL/DLI/MQ) | IF | EVALUATE | Max nest | PERFORM | COMPUTE | Fan-out/in | Score |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | `COACTUPC` | 3368 (4236) | 16 | 7 (0/7/0/0/0) | 328 | 20 | 3 | 61 | 5 | 2/1 | 73.3 |
| 2 | `COTRTLIC` | 1597 (2098) | 10 | 16 (0/0/16/0/0) | 172 | 32 | 4 | 69 | 9 | 2/1 | 68.3 |
| 3 | `COPAUA0C` | 771 (1026) | 14 | 15 (0/3/0/8/4) | 53 | 10 | 3 | 38 | 9 | 0/0 | 46.0 |
| 4 | `COTRTUPC` | 1241 (1702) | 11 | 7 (0/0/7/0/0) | 98 | 26 | 3 | 40 | 5 | 1/2 | 46.0 |
| 5 | `COPAUS0C` | 792 (1032) | 14 | 9 (0/3/0/6/0) | 50 | 22 | 4 | 48 | 3 | 3/1 | 45.5 |
| 6 | `COCRDLIC` | 1093 (1459) | 10 | 8 (0/8/0/0/0) | 120 | 18 | 4 | 33 | 2 | 3/1 | 43.3 |
| 7 | `COTRN02C` | 614 (783) | 10 | 6 (0/6/0/0/0) | 28 | 26 | 4 | 61 | 4 | 3/1 | 41.6 |
| 8 | `COCRDUPC` | 1194 (1560) | 12 | 3 (0/3/0/0/0) | 146 | 16 | 3 | 26 | 0 | 1/2 | 37.5 |
| 9 | `COTRN00C` | 529 (699) | 8 | 4 (0/4/0/0/0) | 52 | 16 | 4 | 46 | 4 | 3/2 | 34.2 |
| 10 | `COUSR00C` | 531 (695) | 8 | 4 (0/4/0/0/0) | 50 | 16 | 4 | 45 | 4 | 4/1 | 34.0 |

Notable programs just outside the top 10 by raw score, but central to the recommendations: `COBIL00C` (#11, 33.6), `CBTRN03C` (#13, 31.7), `CBACT04C` (#14, 30.1), `CBTRN02C` (#17, 29.5), `CBSTM03A` (#24, 22.4).

**Differences from the brief's prior figures:** `COACTUPC` is 3,368 code lines (4,236 physical) — larger than "~2500+" — but has **61 PERFORM and 17 `EXEC CICS`**, not ~128/~34; its complexity is in 328 `IF`s of field editing, not in control flow. `COTRTLIC` (not in the brief's list) is the #2 hotspot: 16 embedded SQL statements plus the highest `EVALUATE` count (32). `CBTRN03C` has 73 `PERFORM`s — the highest in the estate, as the brief anticipated.

## 3. Per-program rationale (top 10 + recommendation set)

### `COACTUPC` — Account/customer update screen (score 73.3, #1)
4,236 physical lines, the largest program by a factor of two. 328 `IF`s implement per-field edits for ~30 account and customer attributes (name alphabetic checks, SSN part rules, FICO 300–850, state/ZIP/area-code lookups via `CSLKPCDY`, date edits via `CSUTLDPY`→`CSUTLDTC`), then an optimistic-lock compare of every old/new field before `REWRITE` of both `ACCTDAT` and `CUSTDAT`. 16 copybooks give it the widest data coupling of any online program. Risk is *regression*: hundreds of edit rules with no test harness. Complexity is broad, not deep (max nest 3) — amenable to decomposition into a validation-rule catalogue.

### `COTRTLIC` — Transaction-type list with DB2 cursors (68.3, #2)
The only program with bidirectional cursor paging (`DECLARE … CURSOR`, forward and backward FETCH, in-place `UPDATE`/`DELETE` of `CARDDEMO.TRANSACTION_TYPE`) — 16 SQL statements, 32 `EVALUATE`s, 172 `IF`s, 69 `PERFORM`s, nesting 4. Its size is disproportionate to its business value (reference-data maintenance). Migration cost is technical (cursor semantics → JPA/JDBC paging) rather than financial.

### `COPAUA0C` — Authorization decision engine (46.0, #3)
Touches every subsystem: MQ (`MQGET` request, `MQPUT1` reply), CICS VSAM (`CCXREF`, `ACCTDAT`, `CUSTDAT`), IMS (`PAUTSUM0`, `PAUTDTL1` via `EXEC DLI`). 9 `COMPUTE`s implement the approve/decline arithmetic (available credit vs. `PA-RQ-TRANSACTION-AMT`, counters/totals in the IMS root). Highest *infrastructure* coupling in the estate — hard to unit-test without IMS/MQ emulation.

### `COTRTUPC` — Transaction-type add/update (46.0, #4)
7 SQL statements (SELECT/INSERT/UPDATE), 98 `IF`s, 26 `EVALUATE`s. Paired with `COTRTLIC`; shares `DCLTRTYP`/`DCLTRCAT` host structures. Straightforward CRUD once DB2 access is abstracted.

### `COPAUS0C` — Pending-authorization summary (45.5, #5)
14 copybooks, 6 `EXEC DLI` + 3 CICS reads, 22 `EVALUATE`s, nest 4. Read-mostly IMS browse; complexity is screen paging over the child-segment list. Blocked on IMS target.

### `COCRDLIC` — Card list (43.3, #6)
8 distinct CICS browse operations (`STARTBR`/`READNEXT`/`READPREV`/`ENDBR` on `CARDDAT`), 120 `IF`s, nest 4, fan-out 3 (`COCRDSLC`, `COCRDUPC`, menu). Classic paging-state machine; representative of the browse pattern shared by `COTRN00C`, `COUSR00C`, `COPAUS0C`.

### `COTRN02C` — Add transaction (41.6, #7)
Writes to `TRANSACT` from the online side, deriving the next `TRAN-ID` via `READPREV` + 1 (a concurrency hazard when parallelised) and validating dates via two `CALL 'CSUTLDTC'`. 26 `EVALUATE`s, 61 `PERFORM`s. Financially relevant because it creates ledger records outside the batch posting path.

### `COCRDUPC` — Card update (37.5, #8)
146 `IF`s (second highest), 12 copybooks; edit-and-`REWRITE` of `CARDDAT` with old/new comparison, same shape as `COACTUPC` at one-third the size.

### `COTRN00C` / `COUSR00C` — Transaction list / user list (34.2 / 34.0, #9–10)
Near-identical browse programs (529/531 code lines, 16 `EVALUATE`s each, nest 4). Low individual risk; high value as a shared "paged list" pattern to implement once.

### `COBIL00C` — Bill payment (33.6, #11)
Only 420 code lines but 7 CICS file operations across three files in one unit of work: `READ`/`REWRITE` `ACCTDAT` (sets balance to zero), `STARTBR`/`READPREV`/`ENDBR`/`WRITE` `TRANSACT` (payment record), `READ` `CXACAIX`. A money-moving transaction with no compensating logic if the second write fails — financial-integrity risk far above its complexity score.

### `CBTRN03C` — Transaction detail report (31.7, #13)
73 `PERFORM`s (estate maximum), 75 `IF`s, 6 files, nest 4. Control-break report with page/account/grand totals and type/category description lookups. Deterministic file-in/report-out makes it an ideal golden-master candidate.

### `CBACT04C` — Interest calculation (30.1, #14)
552 code lines, 5 files, 86 `IF`s, nest 4, 5 `COMPUTE`s. Core formula (verified): `COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200`, accumulated into `WS-TOTAL-INT` and applied with `ADD WS-TOTAL-INT TO ACCT-CURR-BAL`; falls back to disclosure group `'DEFAULT'` when the account's group/type/category key is missing; `1400-COMPUTE-FEES` is an unimplemented stub. COBOL fixed-point truncation semantics (`S9(09)V99` intermediates, no `ROUNDED`) must be reproduced exactly in `BigDecimal` (scale 2, `RoundingMode.DOWN`) or every statement will differ by cents. Inferred regulatory exposure: interest/APR computation and disclosure.

### `CBTRN02C` — Daily transaction posting (29.5, #17)
619 code lines, 96 `IF`s, 6 files, updates three masters (`TRANFILE` WRITE, `ACCTFILE` REWRITE, `TCATBALF` WRITE/REWRITE) and emits rejects with reason codes 100 (card not in xref), 101 (account not found), 102 (over limit: `ACCT-CREDIT-LIMIT >= ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT`), 103 (transaction after `ACCT-EXPIRAION-DATE`). It is the sole producer of `TCATBALF` balances consumed by `CBACT04C`, so a posting defect propagates into interest. No two-phase commit across the three VSAM files — partial updates on abend are possible today and must be handled by transaction boundaries in Java.

### `CBSTM03A` (+ `CBSTM03B`) — Statement generation (22.4, #24)
784 code lines, but logic is spread across `CBSTM03B` (all I/O through a request area and `CALL 'CBSTM03B'`), 2-D `OCCURS` tables holding a page of transactions, and two output renderers (fixed-text and HTML). Low cyclomatic score understates risk: the statement is the customer-facing artifact that must match the legacy output byte-for-byte (inferred regulatory exposure: periodic statement accuracy).

### `COPAUA0C` / `CBPAUP0C` — Authorization engine and expiry purge
`CBPAUP0C` (266 code lines, 5 `EXEC DLI`) is simple, but both depend on the IMS `PAUTHDB` hierarchy, the MQ request/reply queues, and (via `COPAUS2C`) DB2 `AUTHFRDS`. Blast radius is the whole auth sub-app (8 programs, 10 copybooks, 5 JCL, DBD/PSB).

## 4. Modernization-first recommendations

| Priority | Programs | Why |
|---|---|---|
| **P1** | `CBACT04C`, `CBTRN02C` | Core financial engine. `CBTRN02C` posts and rejects transactions and maintains `ACCT-CURR-BAL`/cycle totals and `TCATBALF`; `CBACT04C` turns those balances into interest and writes it back to the account. Both are pure batch (files in, files out), have no CICS/IMS/MQ dependency, and are covered end-to-end by `POSTTRAN`→`INTCALC` JCL, so **golden-master tests** (run legacy and Java on the same `DALYTRAN.PS`/VSAM extracts, diff `TRANSACT`, `ACCTDATA`, `TCATBALF`, `DALYREJS`, `SYSTRAN` byte-for-byte) are feasible on day one. `BigDecimal` parity for `S9(10)V99` arithmetic and COBOL truncation is the single biggest correctness risk in the estate; interest/APR is the highest inferred regulatory exposure. Fan-in of their copybooks (`CVACT01Y` ×14, `CVTRA05Y` ×11) means the domain model they define is reused everywhere — get it right first. |
| **P2** | `COACTUPC`, `COBIL00C`, `CBSTM03A` (+`CBSTM03B`) | `COACTUPC` is the largest and most edit-dense program; extracting its rules into a testable validation layer de-risks every other update screen (`COCRDUPC`, `COUSR02C`, `COTRTUPC` follow the same pattern). `COBIL00C` moves money online across three files without atomicity — must sit on the same ledger/transaction model as P1. `CBSTM03A` renders the customer statement from P1's outputs; statement fidelity is an inferred regulatory obligation and a natural golden-master (text + HTML diff). |
| **P3** | `CBTRN03C`, `COTRTLIC`/`COTRTUPC`, `COTRN02C` | `CBTRN03C` is a deterministic report — high PERFORM count but low risk, and its `TRANTYPE`/`TRANCATG` lookups are the VSAM mirror of the DB2 tables. `COTRTLIC`/`COTRTUPC` are the only embedded-SQL programs; migrating them establishes the JDBC/JPA reference-data pattern and retires DB2 cursor paging. `COTRN02C` creates ledger rows online (needs the P1 transaction model and an id-generation strategy replacing `READPREV`+1). |
| **Deferred** | `COPAUA0C`, `CBPAUP0C` (and `COPAUS0C/1C/2C`, `PAUDBLOD/UNL`, `DBUNLDGS`) | Blocked on target infrastructure for IMS `PAUTHDB`, MQ request/reply queues and DB2 `AUTHFRDS`. Highest integration coupling with the lowest overlap with the core ledger; defer until a messaging/event platform and an IMS replacement store are decided. Keep the MQ request/reply contract (`CCPAURQY`/`CCPAURLY` fixed-width layouts) as the integration seam. |

Remaining programs (menus, sign-on, browse/list screens, user admin, file dump utilities, export/import, MQ date/account responders) are low-complexity and follow shared patterns; migrate them as pattern instances after P2 establishes the screen framework.

### Reasoning summary

* **Financial integrity** — `CBTRN02C` and `CBACT04C` are the only writers of `TCATBALF` and the batch writers of `ACCT-CURR-BAL`; `COBIL00C` is the only online writer of `ACCT-CURR-BAL` (`COTRN02C` writes `TRANSACT` records but never touches account balances). Any drift here is a ledger error, so these get parity testing before UI work.
* **Regulatory exposure (inferred)** — interest computation and disclosure-group rates (`CBACT04C`, `DISCGRP`), periodic statements (`CBSTM03A`), and plain-text credentials (`SEC-USR-PWD`, `COSGN00C`/`COUSR*`) are the areas a banking auditor would inspect first.
* **Coupling / blast radius** — `CVACT01Y`, `CVACT03Y` (14 active includers each) and `CVTRA05Y` (11) are the shared record layouts; their Java equivalents must be settled by P1. `COMEN01C` (fan-out 11) and `COADM01C` (6) are the navigation hubs and become the routing layer in P2. The auth module is a self-contained island (own copybooks, own DB, own queues) — which is precisely why it can be deferred without blocking the rest.

## Appendix A — full ranking (44 programs)

| # | Program | Code lines (total) | COPY | I/O ops (SELECT/CICS file/SQL/DLI/MQ) | IF | EVALUATE | Max nest | PERFORM | COMPUTE | Fan-out/in | Score |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | `COACTUPC` | 3368 (4236) | 16 | 7 (0/7/0/0/0) | 328 | 20 | 3 | 61 | 5 | 2/1 | 73.3 |
| 2 | `COTRTLIC` | 1597 (2098) | 10 | 16 (0/0/16/0/0) | 172 | 32 | 4 | 69 | 9 | 2/1 | 68.3 |
| 3 | `COPAUA0C` | 771 (1026) | 14 | 15 (0/3/0/8/4) | 53 | 10 | 3 | 38 | 9 | 0/0 | 46.0 |
| 4 | `COTRTUPC` | 1241 (1702) | 11 | 7 (0/0/7/0/0) | 98 | 26 | 3 | 40 | 5 | 1/2 | 46.0 |
| 5 | `COPAUS0C` | 792 (1032) | 14 | 9 (0/3/0/6/0) | 50 | 22 | 4 | 48 | 3 | 3/1 | 45.5 |
| 6 | `COCRDLIC` | 1093 (1459) | 10 | 8 (0/8/0/0/0) | 120 | 18 | 4 | 33 | 2 | 3/1 | 43.3 |
| 7 | `COTRN02C` | 614 (783) | 10 | 6 (0/6/0/0/0) | 28 | 26 | 4 | 61 | 4 | 3/1 | 41.6 |
| 8 | `COCRDUPC` | 1194 (1560) | 12 | 3 (0/3/0/0/0) | 146 | 16 | 3 | 26 | 0 | 1/2 | 37.5 |
| 9 | `COTRN00C` | 529 (699) | 8 | 4 (0/4/0/0/0) | 52 | 16 | 4 | 46 | 4 | 3/2 | 34.2 |
| 10 | `COUSR00C` | 531 (695) | 8 | 4 (0/4/0/0/0) | 50 | 16 | 4 | 45 | 4 | 4/1 | 34.0 |
| 11 | `COBIL00C` | 420 (572) | 10 | 7 (0/7/0/0/0) | 20 | 18 | 4 | 38 | 1 | 2/1 | 33.6 |
| 12 | `COACCT01` | 500 (620) | 9 | 5 (0/1/0/0/4) | 20 | 18 | 3 | 32 | 11 | 0/0 | 33.0 |
| 13 | `CBTRN03C` | 545 (649) | 5 | 6 (6/0/0/0/0) | 75 | 4 | 4 | 73 | 0 | 0/0 | 31.7 |
| 14 | `CBACT04C` | 552 (652) | 5 | 5 (5/0/0/0/0) | 86 | 0 | 4 | 57 | 5 | 0/0 | 30.1 |
| 15 | `COPAUS1C` | 461 (604) | 10 | 7 (0/0/0/7/0) | 34 | 10 | 3 | 34 | 0 | 2/1 | 29.7 |
| 16 | `CODATE01` | 409 (524) | 8 | 4 (0/0/0/0/4) | 18 | 16 | 3 | 28 | 11 | 0/0 | 29.6 |
| 17 | `CBTRN02C` | 619 (731) | 5 | 6 (6/0/0/0/0) | 96 | 0 | 3 | 62 | 1 | 0/0 | 29.5 |
| 18 | `COACTVWC` | 703 (941) | 14 | 3 (0/3/0/0/0) | 56 | 10 | 2 | 18 | 0 | 1/1 | 27.1 |
| 19 | `COCRDSLC` | 642 (887) | 12 | 2 (0/2/0/0/0) | 68 | 8 | 3 | 19 | 0 | 1/2 | 26.3 |
| 20 | `CORPT00C` | 498 (649) | 8 | 0 (0/0/0/0/0) | 40 | 10 | 3 | 35 | 7 | 3/1 | 26.1 |
| 21 | `COMEN01C` | 213 (308) | 9 | 0 (0/0/0/0/0) | 14 | 6 | 3 | 15 | 0 | 11/16 | 25.2 |
| 22 | `CBTRN01C` | 415 (494) | 6 | 6 (6/0/0/0/0) | 66 | 0 | 3 | 43 | 0 | 0/0 | 24.5 |
| 23 | `COUSR02C` | 303 (414) | 8 | 2 (0/2/0/0/0) | 26 | 10 | 4 | 31 | 0 | 2/2 | 23.7 |
| 24 | `CBSTM03A` | 784 (924) | 4 | 2 (2/0/0/0/0) | 30 | 9 | 2 | 33 | 4 | 1/0 | 22.4 |
| 25 | `COUSR03C` | 251 (359) | 8 | 2 (0/2/0/0/0) | 16 | 10 | 4 | 26 | 0 | 2/2 | 22.3 |
| 26 | `CBEXPORT` | 396 (582) | 6 | 6 (6/0/0/0/0) | 32 | 0 | 1 | 50 | 0 | 0/0 | 21.3 |
| 27 | `CBIMPORT` | 337 (487) | 6 | 7 (7/0/0/0/0) | 28 | 2 | 1 | 30 | 0 | 0/0 | 19.6 |
| 28 | `COSGN00C` | 172 (260) | 8 | 1 (0/1/0/0/0) | 8 | 6 | 3 | 11 | 0 | 2/12 | 19.6 |
| 29 | `COADM01C` | 189 (288) | 9 | 0 (0/0/0/0/0) | 12 | 4 | 3 | 15 | 0 | 6/7 | 19.2 |
| 30 | `COTRN01C` | 231 (330) | 8 | 1 (0/1/0/0/0) | 14 | 6 | 4 | 17 | 0 | 3/2 | 19.0 |
| 31 | `COUSR01C` | 198 (299) | 8 | 1 (0/1/0/0/0) | 8 | 6 | 3 | 20 | 0 | 2/1 | 17.0 |
| 32 | `CBACT01C` | 358 (430) | 2 | 4 (4/0/0/0/0) | 44 | 0 | 2 | 36 | 0 | 1/0 | 16.9 |
| 33 | `CBPAUP0C` | 266 (386) | 2 | 5 (0/0/0/5/0) | 36 | 4 | 2 | 19 | 2 | 0/0 | 16.4 |
| 34 | `COBTUPDT` | 177 (237) | 0 | 6 (1/0/5/0/0) | 4 | 8 | 1 | 15 | 0 | 0/0 | 12.7 |
| 35 | `PAUDBLOD` | 251 (369) | 4 | 2 (2/0/0/0/0) | 33 | 0 | 2 | 12 | 0 | 0/0 | 11.5 |
| 36 | `PAUDBUNL` | 207 (317) | 4 | 2 (2/0/0/0/0) | 21 | 0 | 2 | 8 | 0 | 0/0 | 10.2 |
| 37 | `DBUNLDGS` | 198 (366) | 6 | 0 (0/0/0/0/0) | 17 | 0 | 2 | 10 | 0 | 0/0 | 9.6 |
| 38 | `COPAUS2C` | 201 (244) | 1 | 4 (0/0/4/0/0) | 6 | 0 | 2 | 1 | 1 | 0/1 | 9.3 |
| 39 | `CBSTM03B` | 162 (230) | 0 | 4 (4/0/0/0/0) | 24 | 1 | 1 | 4 | 0 | 0/1 | 8.3 |
| 40 | `CBACT02C` | 129 (178) | 1 | 1 (1/0/0/0/0) | 22 | 0 | 2 | 11 | 0 | 0/0 | 7.3 |
| 41 | `CBACT03C` | 130 (178) | 1 | 1 (1/0/0/0/0) | 22 | 0 | 2 | 11 | 0 | 0/0 | 7.3 |
| 42 | `CBCUS01C` | 130 (178) | 1 | 1 (1/0/0/0/0) | 22 | 0 | 2 | 11 | 0 | 0/0 | 7.3 |
| 43 | `CSUTLDTC` | 114 (157) | 0 | 0 (0/0/0/0/0) | 0 | 2 | 1 | 1 | 0 | 0/3 | 3.8 |
| 44 | `COBSWAIT` | 13 (41) | 0 | 0 (0/0/0/0/0) | 0 | 0 | 0 | 0 | 0 | 1/0 | 0.4 |

Notes: `DBUNLDGS`/`PAUDBLOD`/`PAUDBUNL` perform IMS I/O through `CALL 'CBLTDLI'`, which is not counted in the I/O column (only `EXEC DLI` is); `CBSTM03A`'s file reads are delegated to `CBSTM03B` and therefore appear on the callee. `COBTUPDT`'s and `COPAUS2C`'s SQL counts exclude `INCLUDE` statements.
