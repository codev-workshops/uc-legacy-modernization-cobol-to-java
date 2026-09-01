# CardDemo Modernization Hotspot Report

Every number below was counted from the source member itself, not estimated. Definitions:

| Metric | How it is counted |
| --- | --- |
| Lines of code | physical lines excluding comment lines (`*`/`/` in column 7) and blank lines |
| Copybooks | distinct members named on a `COPY` statement (a `PERFORM ... REPLACING` is not a `COPY`) |
| DCLGEN includes | distinct members pulled in with `EXEC SQL INCLUDE` (e.g. `DCLTRTYP`, `SQLCA`); these are not `COPY` statements and are counted separately |
| I/O operations | `SELECT` clauses + native COBOL `READ`/`WRITE`/`REWRITE`/`DELETE` + `EXEC CICS` file commands (`READ`/`WRITE`/`REWRITE`/`DELETE`/`STARTBR`/`READNEXT`/`READPREV`/`ENDBR`) + `EXEC SQL` + `EXEC DLI` statements. Verbs inside quoted literals (`DISPLAY 'TOTAL READ :'`) and inside `EXEC` blocks are not double-counted. |
| Branch constructs | `IF` + `EVALUATE` counted in the PROCEDURE DIVISION only |
| Max nesting | deepest simultaneously open `IF`/`EVALUATE`, reset at every sentence-ending period |
| Dependencies | in + out edges of the call graph in `DEPENDENCY_MAP.md` (self-edges and external routines excluded) |

## Full metric table

| Program | Type | LOC | Copybooks | DCLGEN includes | I/O ops | `SELECT` | file verbs | CICS file ops | `EXEC SQL` | `EXEC DLI` | `IF` | `EVALUATE` | Max nesting | In | Out |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `CBACT01C` | batch | 358 | 2 | 0 | 9 | 4 | 5 | 0 | 0 | 0 | 22 | 0 | 2 | 0 | 0 |
| `CBACT02C` | batch | 129 | 1 | 0 | 2 | 1 | 1 | 0 | 0 | 0 | 11 | 0 | 2 | 0 | 0 |
| `CBACT03C` | batch | 130 | 1 | 0 | 2 | 1 | 1 | 0 | 0 | 0 | 11 | 0 | 2 | 0 | 0 |
| `CBACT04C` | batch | 552 | 5 | 0 | 12 | 5 | 7 | 0 | 0 | 0 | 43 | 0 | 4 | 0 | 0 |
| `CBCUS01C` | batch | 130 | 1 | 0 | 2 | 1 | 1 | 0 | 0 | 0 | 11 | 0 | 2 | 0 | 0 |
| `CBEXPORT` | batch | 396 | 6 | 0 | 16 | 6 | 10 | 0 | 0 | 0 | 16 | 0 | 1 | 0 | 0 |
| `CBIMPORT` | batch | 337 | 6 | 0 | 14 | 7 | 7 | 0 | 0 | 0 | 14 | 1 | 1 | 0 | 0 |
| `CBPAUP0C` | batch | 266 | 2 | 0 | 5 | 0 | 0 | 0 | 0 | 5 | 17 | 2 | 2 | 0 | 0 |
| `CBSTM03A` | batch | 784 | 4 | 0 | 97 | 2 | 95 | 0 | 0 | 0 | 15 | 5 | 2 | 0 | 1 |
| `CBSTM03B` | batch | 162 | 0 | 0 | 8 | 4 | 4 | 0 | 0 | 0 | 12 | 1 | 1 | 1 | 0 |
| `CBTRN01C` | batch | 415 | 6 | 0 | 9 | 6 | 3 | 0 | 0 | 0 | 33 | 0 | 3 | 0 | 0 |
| `CBTRN02C` | batch | 619 | 5 | 0 | 15 | 6 | 9 | 0 | 0 | 0 | 48 | 0 | 3 | 0 | 0 |
| `CBTRN03C` | batch | 545 | 5 | 0 | 12 | 6 | 6 | 0 | 0 | 0 | 38 | 2 | 4 | 0 | 0 |
| `COACCT01` | online | 500 | 7 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 7 | 9 | 2 | 0 | 0 |
| `COACTUPC` | online | 3368 | 16 | 0 | 7 | 0 | 0 | 7 | 0 | 0 | 164 | 10 | 3 | 1 | 1 |
| `COACTVWC` | online | 703 | 14 | 0 | 3 | 0 | 0 | 3 | 0 | 0 | 28 | 5 | 2 | 1 | 1 |
| `COADM01C` | online | 189 | 9 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 6 | 2 | 3 | 7 | 7 |
| `COBIL00C` | online | 420 | 10 | 0 | 7 | 0 | 0 | 7 | 0 | 0 | 10 | 9 | 4 | 1 | 2 |
| `COBSWAIT` | batch | 13 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| `COBTUPDT` | batch | 177 | 0 | 2 | 7 | 1 | 1 | 0 | 5 | 0 | 2 | 4 | 1 | 0 | 0 |
| `COCRDLIC` | online | 1093 | 10 | 0 | 8 | 0 | 0 | 8 | 0 | 0 | 59 | 9 | 4 | 1 | 2 |
| `COCRDSLC` | online | 642 | 12 | 0 | 2 | 0 | 0 | 2 | 0 | 0 | 33 | 4 | 2 | 2 | 1 |
| `COCRDUPC` | online | 1194 | 12 | 0 | 3 | 0 | 0 | 3 | 0 | 0 | 72 | 8 | 3 | 2 | 1 |
| `CODATE01` | online | 409 | 6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 6 | 8 | 2 | 0 | 0 |
| `COMEN01C` | online | 213 | 9 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 3 | 3 | 11 | 12 |
| `COPAUA0C` | online | 771 | 14 | 0 | 11 | 0 | 0 | 3 | 0 | 8 | 26 | 5 | 3 | 0 | 0 |
| `COPAUS0C` | online | 792 | 14 | 0 | 9 | 0 | 0 | 3 | 0 | 6 | 25 | 11 | 4 | 2 | 3 |
| `COPAUS1C` | online | 461 | 10 | 0 | 7 | 0 | 0 | 0 | 0 | 7 | 17 | 5 | 3 | 1 | 2 |
| `COPAUS2C` | online | 201 | 1 | 2 | 4 | 0 | 0 | 0 | 4 | 0 | 3 | 0 | 2 | 1 | 0 |
| `CORPT00C` | online | 498 | 8 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 20 | 5 | 3 | 1 | 3 |
| `COSGN00C` | online | 172 | 8 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 4 | 3 | 3 | 12 | 2 |
| `COTRN00C` | online | 529 | 8 | 0 | 4 | 0 | 0 | 4 | 0 | 0 | 26 | 8 | 4 | 2 | 3 |
| `COTRN01C` | online | 231 | 8 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 7 | 3 | 4 | 2 | 3 |
| `COTRN02C` | online | 614 | 10 | 0 | 6 | 0 | 0 | 6 | 0 | 0 | 14 | 13 | 4 | 1 | 3 |
| `COTRTLIC` | online | 1597 | 10 | 4 | 16 | 0 | 0 | 0 | 16 | 0 | 86 | 16 | 4 | 1 | 2 |
| `COTRTUPC` | online | 1241 | 11 | 3 | 7 | 0 | 0 | 0 | 7 | 0 | 49 | 13 | 3 | 2 | 1 |
| `COUSR00C` | online | 531 | 8 | 0 | 4 | 0 | 0 | 4 | 0 | 0 | 25 | 8 | 4 | 1 | 4 |
| `COUSR01C` | online | 198 | 8 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 4 | 3 | 3 | 1 | 2 |
| `COUSR02C` | online | 303 | 8 | 0 | 2 | 0 | 0 | 2 | 0 | 0 | 13 | 5 | 4 | 2 | 2 |
| `COUSR03C` | online | 251 | 8 | 0 | 2 | 0 | 0 | 2 | 0 | 0 | 8 | 5 | 4 | 2 | 2 |
| `CSUTLDTC` | batch | 114 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 1 | 2 | 0 |
| `DBUNLDGS` | batch | 198 | 6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 9 | 0 | 2 | 0 | 0 |
| `PAUDBLOD` | batch | 251 | 4 | 0 | 4 | 2 | 2 | 0 | 0 | 0 | 17 | 0 | 2 | 0 | 0 |
| `PAUDBUNL` | batch | 207 | 4 | 0 | 4 | 2 | 2 | 0 | 0 | 0 | 11 | 0 | 2 | 0 | 0 |

## Rankings

### Top 10 by lines of code

| # | Program | lines of code | Type | Total lines incl. comments |
| ---: | --- | ---: | ---: | ---: |
| 1 | `COACTUPC` | 3368 | online | 4236 |
| 2 | `COTRTLIC` | 1597 | online | 2098 |
| 3 | `COTRTUPC` | 1241 | online | 1702 |
| 4 | `COCRDUPC` | 1194 | online | 1560 |
| 5 | `COCRDLIC` | 1093 | online | 1459 |
| 6 | `COPAUS0C` | 792 | online | 1032 |
| 7 | `CBSTM03A` | 784 | batch | 924 |
| 8 | `COPAUA0C` | 771 | online | 1026 |
| 9 | `COACTVWC` | 703 | online | 941 |
| 10 | `COCRDSLC` | 642 | online | 887 |

### Top 10 by copybooks referenced

| # | Program | copybooks referenced | LOC |
| ---: | --- | ---: | ---: |
| 1 | `COACTUPC` | 16 | 3368 |
| 2 | `COACTVWC` | 14 | 703 |
| 3 | `COPAUA0C` | 14 | 771 |
| 4 | `COPAUS0C` | 14 | 792 |
| 5 | `COCRDSLC` | 12 | 642 |
| 6 | `COCRDUPC` | 12 | 1194 |
| 7 | `COTRTUPC` | 11 | 1241 |
| 8 | `COBIL00C` | 10 | 420 |
| 9 | `COCRDLIC` | 10 | 1093 |
| 10 | `COTRN02C` | 10 | 614 |

### Top 10 by I/O operations

| # | Program | I/O operations | `SELECT` | file verbs | CICS file ops | `EXEC SQL` | `EXEC DLI` |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | `CBSTM03A` | 97 | 2 | 95 | 0 | 0 | 0 |
| 2 | `CBEXPORT` | 16 | 6 | 10 | 0 | 0 | 0 |
| 3 | `COTRTLIC` | 16 | 0 | 0 | 0 | 16 | 0 |
| 4 | `CBTRN02C` | 15 | 6 | 9 | 0 | 0 | 0 |
| 5 | `CBIMPORT` | 14 | 7 | 7 | 0 | 0 | 0 |
| 6 | `CBACT04C` | 12 | 5 | 7 | 0 | 0 | 0 |
| 7 | `CBTRN03C` | 12 | 6 | 6 | 0 | 0 | 0 |
| 8 | `COPAUA0C` | 11 | 0 | 0 | 3 | 0 | 8 |
| 9 | `CBACT01C` | 9 | 4 | 5 | 0 | 0 | 0 |
| 10 | `CBTRN01C` | 9 | 6 | 3 | 0 | 0 | 0 |

### Top 10 by branch constructs (`IF` + `EVALUATE`)

| # | Program | branch constructs (`IF` + `EVALUATE`) | `IF` | `EVALUATE` | Max nesting |
| ---: | --- | ---: | ---: | ---: | ---: |
| 1 | `COACTUPC` | 174 | 164 | 10 | 3 |
| 2 | `COTRTLIC` | 102 | 86 | 16 | 4 |
| 3 | `COCRDUPC` | 80 | 72 | 8 | 3 |
| 4 | `COCRDLIC` | 68 | 59 | 9 | 4 |
| 5 | `COTRTUPC` | 62 | 49 | 13 | 3 |
| 6 | `CBTRN02C` | 48 | 48 | 0 | 3 |
| 7 | `CBACT04C` | 43 | 43 | 0 | 4 |
| 8 | `CBTRN03C` | 40 | 38 | 2 | 4 |
| 9 | `COCRDSLC` | 37 | 33 | 4 | 2 |
| 10 | `COPAUS0C` | 36 | 25 | 11 | 4 |

### Top 10 by maximum nesting depth

| # | Program | maximum nesting depth | `IF` | `EVALUATE` |
| ---: | --- | ---: | ---: | ---: |
| 1 | `CBACT04C` | 4 | 43 | 0 |
| 2 | `CBTRN03C` | 4 | 38 | 2 |
| 3 | `COBIL00C` | 4 | 10 | 9 |
| 4 | `COCRDLIC` | 4 | 59 | 9 |
| 5 | `COTRN00C` | 4 | 26 | 8 |
| 6 | `COTRN01C` | 4 | 7 | 3 |
| 7 | `COTRN02C` | 4 | 14 | 13 |
| 8 | `COUSR00C` | 4 | 25 | 8 |
| 9 | `COUSR02C` | 4 | 13 | 5 |
| 10 | `COUSR03C` | 4 | 8 | 5 |

### Top 10 by inter-program dependencies (in + out)

| # | Program | inter-program dependencies (in + out) | In | Out |
| ---: | --- | ---: | ---: | ---: |
| 1 | `COMEN01C` | 23 | 11 | 12 |
| 2 | `COADM01C` | 14 | 7 | 7 |
| 3 | `COSGN00C` | 14 | 12 | 2 |
| 4 | `COTRN00C` | 5 | 2 | 3 |
| 5 | `COTRN01C` | 5 | 2 | 3 |
| 6 | `COUSR00C` | 5 | 1 | 4 |
| 7 | `COPAUS0C` | 5 | 2 | 3 |
| 8 | `CORPT00C` | 4 | 1 | 3 |
| 9 | `COTRN02C` | 4 | 1 | 3 |
| 10 | `COUSR02C` | 4 | 2 | 2 |

## Programs called out for special attention

| Program | LOC | Copybooks | I/O ops | Branches | Max nesting | In/Out | Why it matters |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| `COACTUPC` | 3368 | 16 | 7 | 174 | 3 | 1/1 | The account-update screen: the largest online program, the most copybooks of any member, and field-by-field validation (name, address, state/ZIP pair, phone area code, FICO 300-850, Y/N flags) hand-written as nested `IF`s over `app/cpy/CSLKPCDY.cpy` lookup tables. It also holds the optimistic-locking re-read/compare logic before `REWRITE`. |
| `CBTRN01C` | 415 | 6 | 9 | 33 | 3 | 0/0 | Daily-transaction validation, but no JCL step executes it; its logic is duplicated inside `CBTRN02C`. Decide whether the rules are canonical here or in `CBTRN02C` before porting either. |
| `CBTRN02C` | 619 | 5 | 15 | 48 | 3 | 0/0 | The posting engine: reads `DALYTRAN`, validates against `CARDXREF`/`ACCTDATA`, updates balances (`OPEN I-O`), writes `TRANSACT` and the `DALYREJS` GDG. Financial correctness centre of the batch chain. |
| `CBACT04C` | 552 | 5 | 12 | 43 | 4 | 0/0 | Interest calculation over `TCATBALF` x `DISCGRP`, updating `ACCTDATA` in place and emitting interest transactions to a GDG. Rounding and rate-lookup semantics must be reproduced exactly (`BigDecimal`, not `double`). |
| `CBSTM03A` | 784 | 4 | 97 | 20 | 2 | 0/1 | Statement driver: writes both plain-text and HTML statements, with report formatting embedded in COBOL string handling. |
| `CBSTM03B` | 162 | 0 | 8 | 13 | 1 | 1/0 | The file-access subroutine behind `CBSTM03A`; all four statement input files are opened here, so the pair must be modernized together. |
| `COTRTLIC` | 1597 | 10 | 16 | 102 | 4 | 1/2 | Transaction-type list over DB2 with cursor paging: a small, self-contained SQL program. |
| `COTRTUPC` | 1241 | 11 | 7 | 62 | 3 | 2/1 | Transaction-type update over DB2: `SELECT`/`UPDATE`/`INSERT` on `CARDDEMO.TRANSACTION_TYPE`, contained scope. |
| `COBTUPDT` | 177 | 0 | 7 | 6 | 1 | 0/0 | Batch DB2 transaction-type loader driven by a sequential input file. |

## Consolidated modernization recommendation

**Wave 1 - contained DB2 slices (lowest risk, fastest feedback).** `COTRTLIC`, `COTRTUPC` and `COBTUPDT` already talk to relational tables (`CARDDEMO.TRANSACTION_TYPE`, `CARDDEMO.TRANSACTION_TYPE_CATEGORY`) through `EXEC SQL`, so the data model needs no conversion; only the CICS screen and the cursor paging have to be replaced. They are small, have few dependencies, and prove out the target stack end to end.

**Wave 2 - the financial batch chain (highest business value, highest correctness risk).** `CBTRN02C` -> `CBACT04C`, with `CBTRN03C` for reporting. These own balance updates, rejection handling and interest accrual. Port them with a record-level parallel-run harness against the existing `DALYTRAN`/`TCATBALF`/`ACCTDATA` extracts and compare outputs byte for byte. Resolve the `CBTRN01C` duplication first: it is dead code in the JCL, so the validation rules must be taken from whichever copy the business confirms is current.

**Wave 3 - the account-update screen.** `COACTUPC` concentrates the application's business rules and its copybook surface is the widest in the repository; it is also the only online program doing an optimistic-locking `REWRITE`. Extract its validation rules into a testable service before touching the UI, and treat `app/cpy/CSLKPCDY.cpy` (phone area codes, state codes, state/ZIP pairs) as reference data to be moved into tables rather than code.

**Wave 4 - statement generation.** `CBSTM03A` + `CBSTM03B` must move as a unit. The generated HTML is assembled in COBOL working storage, so the modernized version should switch to a template engine; the risk here is output fidelity rather than logic.

**Cross-cutting decisions to take before any wave starts.**

- Monetary fields are `PIC S9(n)V99` `COMP-3`; map them to `BigDecimal` with explicit scale, never to `double`.

- The Close-Process-Open pattern (`CLOSEFIL`/`OPENFIL`) exists only because CICS and batch cannot share VSAM. Once the data is relational the batch window can shrink, which changes the scheduler definitions in `app/scheduler/`.

- Dynamic `XCTL PROGRAM(CDEMO-TO-PROGRAM)` navigation and the menu tables (`app/cpy/COMEN02Y.cpy`, `app/cpy/COADM02Y.cpy`) are the routing layer; they become configuration, not code.

- `COCRDSEC` is referenced by CICS transaction `CDV1` in the CSD but has no source member in this repository, and the authorization/MQ/IMS extension programs depend on IMS and MQ infrastructure that has no equivalent in the target stack. Both need an explicit decision before the corresponding waves start.

