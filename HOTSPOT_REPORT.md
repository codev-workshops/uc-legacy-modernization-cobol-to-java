# CardDemo – Hotspot Report

Scope: all 44 COBOL programs under `app/cbl`, `app/app-authorization-ims-db2-mq/cbl`,
`app/app-transaction-type-db2/cbl`, `app/app-vsam-mq/cbl`. Metrics were extracted mechanically from source
(fixed-format columns 8–72, comments excluded) — see method notes at the end. Rankings use **code LOC**
(non-comment, non-blank), not physical lines.

Metric definitions

| Metric | How measured |
|---|---|
| LOC | non-comment, non-blank lines (physical lines shown in brackets) |
| Copybooks | distinct `COPY` + `EXEC SQL INCLUDE` members |
| I/O ops | count of I/O statements: batch `OPEN/CLOSE/READ/WRITE/REWRITE/DELETE/START`, `EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT/READPREV/ENDBR`, `EXEC SQL` (non-INCLUDE), `EXEC DLI`, `CBLTDLI`, `MQ*` calls |
| Nesting | max simultaneous depth of `IF`/`EVALUATE` (reset at sentence period) |
| Logic density | (`IF` + `EVALUATE` + `WHEN`) per 100 code LOC |
| Dependencies | fan-in (programs that XCTL/LINK/CALL/menu into it) + fan-out (programs it transfers to) + JCL jobs that execute it |

---

## 1. Top-10 rankings per metric

### 1.1 By LOC

| # | Program | Code LOC (physical) | Component | Notes |
|--:|---|--:|---|---|
| 1 | COACTUPC | 3,368 (4,236) | base online | Account/customer update; 149 paragraphs, 165 IFs, 51 GO TOs, 39 `COPY CSSETATY REPLACING` expansions |
| 2 | COTRTLIC | 1,861 (2,098) | DB2 sub-app | Tran-type list with fwd/back cursors; 28 GO TOs |
| 3 | COTRTUPC | 1,429 (1,702) | DB2 sub-app | Tran-type add/update; 104 WHEN clauses |
| 4 | COCRDUPC | 1,195 (1,560) | base online | Card update |
| 5 | COCRDLIC | 1,093 (1,459) | base online | Card list with paging |
| 6 | COPAUS0C | 792 (1,032) | IMS sub-app | Pending-auth summary (VSAM + IMS) |
| 7 | CBSTM03A | 784 (924) | base batch | Statement generator; uses `ALTER … GO TO` (4) |
| 8 | COPAUA0C | 771 (1,026) | IMS sub-app | MQ-triggered authorization engine |
| 9 | COACTVWC | 703 (941) | base online | Account view |
| 10 | COCRDSLC | 642 (887) | base online | Card view |

Estate total: 23,636 code LOC in 44 programs; the top 5 hold 38 %.

### 1.2 By number of copybooks referenced

| # | Program | Copybooks | Of which entity layouts |
|--:|---|--:|---|
| 1 | COACTUPC | 18 | CVACT01Y, CVACT03Y, CVCUS01Y + CSLKPCDY, CSSETATY, CSSTRPFY, CSUTLDPY, CSUTLDWY |
| 2 | COTRTUPC | 16 | DCLTRTYP, DCLTRCAT, SQLCA + CSSTRPFY, CSUTLDWY |
| 3 | COTRTLIC | 15 | DCLTRTYP, SQLCA, CSDB2RWY, CSDB2RPY, CVACT02Y, CSSTRPFY |
| 3 | COACTVWC | 15 | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| 5 | COPAUA0C | 14 | CVACT01Y, CVACT03Y, CVCUS01Y, CIPAUSMY, CIPAUDTY, CCPAURQY/RLY/ERY + 6 CMQ* |
| 5 | COPAUS0C | 14 | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CIPAUSMY, CIPAUDTY |
| 7 | COCRDSLC | 13 | CVACT02Y, CVCUS01Y, CSSTRPFY |
| 7 | COCRDUPC | 13 | CVACT02Y, CVCUS01Y, CSSTRPFY |
| 9 | COCRDLIC | 11 | CVACT02Y, CSSTRPFY |
| 10 | COBIL00C | 10 | CVACT01Y, CVACT03Y, CVTRA05Y |
| 10 | COPAUS1C | 10 | CIPAUSMY, CIPAUDTY |
| 10 | COTRN02C | 10 | CVTRA05Y, CVACT01Y, CVACT03Y |

(`CSSTRPFY` / `CSUTLDWY` are pulled in with the quoted form `COPY 'CSSTRPFY'.`, used only by the COACT*/COCRD*/COTRT* family.)

### 1.3 By number of I/O operations

| # | Program | I/O ops | Breakdown |
|--:|---|--:|---|
| 1 | CBSTM03A | 98 | 92 `WRITE` (statement/HTML lines) + OPEN/CLOSE; all reads delegated to CBSTM03B |
| 2 | CBEXPORT | 28 | 6 VSAM inputs → 1 KSDS output |
| 2 | CBIMPORT | 28 | 1 KSDS input → 6 sequential outputs |
| 4 | CBTRN02C | 22 | 6 files: DALYTRAN in, TRANSACT out, ACCTDAT/TCATBALF I-O, XREF in, DALYREJS out |
| 5 | COPAUA0C | 21 | 3 CICS READ + 8 DLI + 4 MQ + SCHD/TERM |
| 6 | CBTRN03C | 19 | 6 files, report writer |
| 7 | CBACT04C | 18 | 5 files incl. ACCTDAT I-O, SYSTRAN out |
| 7 | CBTRN01C | 18 | 6 files, read-only validator |
| 9 | COTRTLIC | 16 | 2 cursors (DECLARE/OPEN/FETCH×3/CLOSE), SELECT, UPDATE, DELETE |
| 10 | CBPAUP0C | 15 | DLI GN/GNP/DLET×2/CHKP |

### 1.4 By business-logic density (nesting depth, then IF+EVALUATE volume)

12 programs tie at max nesting depth 4; ordered by decision volume:

| # | Program | Max nest | IF / EVALUATE / WHEN | Density (/100 LOC) | Character |
|--:|---|:-:|---|--:|---|
| 1 | COTRTLIC | 4 | 87 / 16 / 63 | 8.9 | cursor paging state machine + DB2 SQLCODE handling |
| 2 | COCRDLIC | 4 | 61 / 9 / 43 | 10.3 | paging, row-select, filter edits |
| 3 | CBACT04C | 4 | 43 / 0 / 0 | 7.8 | interest by account/category; rate fallback to DEFAULT group |
| 4 | CBTRN03C | 4 | 38 / 2 / 6 | 8.4 | control-break report (page/account/grand totals) |
| 5 | COPAUS0C | 4 | 25 / 11 / 46 | 10.4 | IMS browse + VSAM lookups + screen paging |
| 6 | COTRN00C | 4 | 26 / 8 / 50 | 15.9 | transaction list paging |
| 7 | COUSR00C | 4 | 25 / 8 / 52 | 16.0 | user list paging |
| 8 | COTRN02C | 4 | 14 / 13 / 63 | 14.7 | transaction-add field validation (key-by-key EVALUATE) |
| 9 | COBIL00C | 4 | 10 / 9 / 32 | 12.1 | bill payment: balance check, tran-id generation, ACCTDAT rewrite |
| 10 | COUSR02C | 4 | 13 / 5 / 20 | 12.5 | user update |
| — | COACTUPC | 3 | **165 / 10 / 127** | 9.0 | shallow but by far the largest decision volume (field-level edit rules) |

### 1.5 By inter-program dependencies

| # | Program | Fan-in | Fan-out | JCL jobs | Total | Role |
|--:|---|--:|--:|--:|--:|---|
| 1 | COMEN01C | 7 | 12 | 0 | 19 | main-menu hub (`COMEN02Y` table drives 11 XCTLs) |
| 2 | COSGN00C | 12 | 2 | 0 | 14 | sign-on; every screen returns here on PF3/exit |
| 3 | COADM01C | 5 | 7 | 0 | 12 | admin-menu hub (`COADM02Y`) |
| 4 | CORPT00C | 1 | 4 | 0 | 5 | online→batch bridge (INTRDR submit of TRANREPT) |
| 4 | COTRN00C | 2 | 3 | 0 | 5 | |
| 4 | COTRN01C | 2 | 3 | 0 | 5 | |
| 4 | COUSR00C | 1 | 4 | 0 | 5 | |
| 8 | COTRN02C | 1 | 3 | 0 | 4 | also CALLs CSUTLDTC |
| 8 | COUSR02C | 2 | 2 | 0 | 4 | |
| 8 | COUSR03C | 2 | 2 | 0 | 4 | |

Data-coupling view (programs touching the most distinct stores): CBIMPORT 7, COPAUA0C 6 (3 VSAM + 2 IMS segments + MQ),
CBEXPORT/CBTRN01C/CBTRN02C/CBTRN03C 6, COPAUS0C 5, CBACT04C 5.

---

## 2. Composite ranking

Sum of ranks across the five metrics (LOC, copybooks, I/O, nesting, dependencies); lower = hotter.

| # | Program | LOC rk | Cpy rk | I/O rk | Nest rk | Dep rk | Composite | Stores |
|--:|---|--:|--:|--:|--:|--:|--:|---|
| 1 | COTRTLIC | 2 | 3 | 9 | 1 | 13 | **28** | CICS + DB2 |
| 2 | COPAUS0C | 6 | 5 | 15 | 3 | 16 | **45** | CICS + VSAM + IMS |
| 3 | COTRTUPC | 3 | 2 | 14 | 14 | 14 | **47** | CICS + DB2 |
| 4 | COCRDLIC | 5 | 9 | 19 | 2 | 15 | **50** | CICS + VSAM |
| 5 | COACTUPC | 1 | 1 | 18 | 13 | 20 | **53** | CICS + VSAM |
| 6 | COTRN02C | 12 | 10 | 25 | 4 | 8 | **59** | CICS + VSAM |
| 7 | COUSR00C | 16 | 16 | 26 | 7 | 4 | **69** | CICS + VSAM |
| 8 | COTRN00C | 17 | 17 | 27 | 8 | 5 | **74** | CICS + VSAM |
| 9 | COPAUA0C | 8 | 6 | 5 | 16 | 41 | **76** | CICS + VSAM + IMS + MQ |
| 9 | CBTRN03C | 15 | 31 | 6 | 6 | 18 | **76** | batch VSAM/seq |

---

## 3. Modernization recommendations

### 3.1 Sequencing principle

Rank by **business criticality × data-store risk × isolation**, not by raw size. CardDemo's core value chain
(post → balance → interest → statement) lives in small, self-contained batch programs; the biggest online programs
are mostly screen-edit code that a UI rewrite will replace rather than translate.

### 3.2 Recommended order

**Wave 1 — Core domain & batch posting kernel (highest value, lowest coupling)**

| Order | Program(s) | Why first |
|--:|---|---|
| 1 | **CBTRN02C** (POSTTRAN) | The system-of-record writer: validates and posts transactions, updates account balances and category balances. Only 619 LOC, no CICS, 3-deep nesting, 4 validation rules (100–103) that become the domain's invariants. Rebuilding `TRANSACT` via `OPEN OUTPUT` must be redesigned as append/merge. Every downstream job depends on its outputs. |
| 2 | **CBACT04C** (INTCALC) | Interest engine and cycle close. 552 LOC, pure batch, rate lookup with `DEFAULT` fallback, resets cycle counters — the second writer of account balance. Contains the only financial formula (`bal × rate / 1200`) and an unimplemented fee hook (`1400-COMPUTE-FEES`). Migrating it together with CBTRN02C yields one consistent `AccountLedger` service. |
| 3 | **Record copybooks → domain model** (CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVTRA05Y/06Y, CVTRA01Y–04Y, CSUSR01Y) | Not programs, but the highest fan-in artefacts (10–14 programs each). Define JPA entities / schema once; reconcile type mismatches (`CDEMO-CARD-NUM 9(16)` vs `CARD-NUM X(16)`, `PA-ACCT-ID S9(11) COMP-3` vs `ACCT-ID 9(11)`). Unblocks every later wave. |
| 4 | **CBTRN03C + TRANREPT proc, CBSTM03A/CBSTM03B + CREASTMT** | Reporting and statements. CBSTM03A is #1 by I/O count but the I/O is 92 formatted `WRITE`s — a templating problem, not logic. It also carries the estate's only `ALTER … GO TO` and PSA/TCB/TIOT control-block walking (non-portable), plus a hard 51-card × 10-transaction in-memory limit and no cycle-date filter. Replace outright with a template-based statement service reading the new domain model. |

**Wave 2 — Authorization sub-system (highest architectural risk, isolated)**

| Order | Program(s) | Why |
|--:|---|---|
| 5 | **COPAUA0C** (+ CBPAUP0C, COPAUS0C/1C/2C) | Composite #9 but the most heterogeneous program in the estate: CICS + MQ + VSAM + IMS DLI with per-message SYNCPOINT, descending-key IMS inserts, and a stubbed rules engine (`5600-READ-PROFILE-DATA` is `CONTINUE`; reason codes 4200/4300/5100/5200 are never set). Zero program fan-in (MQ-triggered) makes it a clean bounded context: an `AuthorizationService` consuming a queue and owning a pending-auth store. Its unfinished link to posting (`PA-MATCH-STATUS='M'` never set) means the auth→clearing matcher is new design work — schedule it here so the domain model from Wave 1 is available. |

**Wave 3 — Online transaction screens (medium value; mostly UI edits)**

| Order | Program(s) | Why |
|--:|---|---|
| 6 | **COBIL00C, COTRN02C** | The two online programs that *write* business data (bill payment rewrites ACCTDAT and writes TRANSACT; transaction add writes TRANSACT without a balance update). Fold both into the Wave-1 ledger service as commands, fixing the inconsistency. Small (420 / 614 LOC) with dense but shallow validation logic worth capturing as tests. |
| 7 | **COACTUPC, COCRDUPC** | Largest online programs (3,368 / 1,195 LOC) but the bulk is field-edit ceremony: 39 `CSSETATY REPLACING` attribute-setting expansions, 51 GO TOs, phone/state/zip lookups (`CSLKPCDY`), date edits (`CSUTLDPY`/`CSUTLDTC`). Extract the validation rules into a bean-validation layer; do **not** line-translate. Highest LOC-reduction opportunity in the estate. |
| 8 | **COCRDLIC, COTRN00C, COUSR00C, COPAUS0C** | The four paging/list screens (all nesting-4). Shared pattern: STARTBR/READNEXT/READPREV with first/last-key COMMAREA state. One generic paginated-query component replaces all four. |

**Wave 4 — DB2 reference-data sub-app**

| Order | Program(s) | Why |
|--:|---|---|
| 9 | **COTRTLIC, COTRTUPC, COBTUPDT + TRANEXTR** | Composite #1 and #3, yet lowest business value: CRUD on a 2-column reference table. High LOC is DB2 cursor/SQLCA boilerplate and screen edits (COTRTLIC has 28 GO TOs). Once reference data lives in the target DB, the DB2→VSAM replication (`TRANEXTR` → `TRANTYPE`/`TRANCATG` REPRO jobs) and `COBTUPDT` disappear. Treat as a simple admin CRUD rewrite late in the programme. |

**Wave 5 — Framework / infrastructure (replace, don't migrate)**

| Program(s) | Disposition |
|---|---|
| COSGN00C, COMEN01C, COADM01C (+ COCOM01Y, COMEN02Y, COADM02Y) | Highest fan-in/fan-out but zero domain logic: sign-on and menu routing → IAM + UI navigation. Plaintext passwords in `USRSEC` must be replaced. |
| COUSR01C/02C/03C | User admin → identity provider. |
| CORPT00C | Composes JCL and writes to TDQ `JOBS` → async report-request API. |
| CODATE01, COACCT01 (app-vsam-mq) | MQ echo/lookup demos → REST or messaging adapters over the account service. |
| CBACT01C/02C/03C, CBCUS01C, CBEXPORT/CBIMPORT, PAUDBLOD/PAUDBUNL/DBUNLDGS | Data-dump / unload-reload utilities → one-off migration tooling; do not carry forward. |
| COBSWAIT/MVSWAIT, COBDATFT (asm), CSUTLDTC, CBSTM03B | Platform utilities → JDK equivalents (`Thread.sleep`, `java.time`, JPA). |
| CBTRN01C | No JCL invokes it; read-only validator superseded by CBTRN02C → retire. |

### 3.3 Risk flags surfaced by the metrics

| Flag | Where | Impact |
|---|---|---|
| `ALTER … GO TO` (4) and control-block (PSA/TCB/TIOT) addressing | CBSTM03A | Not translatable; forces rewrite |
| `GO TO` density | COACTUPC 51, COTRTLIC 28, COTRTUPC 23, COCRDLIC 16, CBSTM03A 14 | Structured-conversion effort; test coverage needed before refactor |
| `COPY … REPLACING` macro-style expansion | COACTUPC (39×CSSETATY), COTRTUPC | Generated-code bloat; inflates LOC metrics |
| Stubbed logic | COPAUA0C `5600-READ-PROFILE-DATA`; CBACT04C `1400-COMPUTE-FEES` | Requirements gaps to close with the business |
| Never-set status values | `PA-MATCH-STATUS` 'M'/'E' (CIPAUDTY) | Missing auth→posting reconciliation |
| Full-file rebuild semantics | CBTRN02C `OPEN OUTPUT TRANSACT`; TRANBKP/COMBTRAN DELETE-DEFINE-REPRO | Persistence redesign |
| Three uncoordinated writers of account balance | CBTRN02C, CBACT04C, COBIL00C (COTRN02C omits it) | Consolidate into one ledger service with tests |
| Plaintext credentials | CSUSR01Y `SEC-USR-PWD`, DUSRSECJ in-stream data | Security remediation before any cloud deployment |

---

## 4. Method notes

* Source of numbers: `/tmp/carddemo_analysis/metrics.py` run over the repository (JSON at
  `/tmp/carddemo_analysis/metrics.json`). Physical vs code LOC differ because CardDemo carries a
  16-line licence header and heavy inline commentary in every member.
* Nesting depth is computed lexically (`IF`/`EVALUATE` increment, `END-IF`/`END-EVALUATE` decrement,
  sentence period resets). It undercounts nesting expressed through nested `PERFORM` paragraphs, which is why
  COACTUPC (165 IFs) shows depth 3 — its complexity is spread across 149 paragraphs rather than deep blocks.
* Menu-driven XCTLs (`CDEMO-TO-PROGRAM`) were resolved via the `COMEN02Y` / `COADM02Y` option tables; the
  `CORPT00C → TRANREPT → CBTRN03C` edge was added manually from the INTRDR submission code.
* `CBADMCDJ` (DFHCSDUP definitions) is excluded from the "JCL jobs" count because it defines rather than executes programs.
