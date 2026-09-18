# CardDemo Hotspot Report

Related documents: [APPLICATION_INVENTORY](./APPLICATION_INVENTORY.md), [DEPENDENCY_MAP](./DEPENDENCY_MAP.md), [DATA_DICTIONARY](./DATA_DICTIONARY.md).

Generated from static analysis of `app/` on branch develop-asiri; extraction rules are described in HOTSPOT_REPORT.md §1.

## 1. Methodology

Metrics computed by `extract.py` over fixed-format COBOL (cols 1-6 and 73+ stripped; col-7 `*`/`/` = comment; comments excluded):

- **LOC code** = non-blank, non-comment physical lines; **LOC total** = physical lines.
- **Copybooks** = unique COPY targets + EXEC SQL INCLUDE members (raw statement count in JSON).
- **I/O ops** = batch file verbs (OPEN/CLOSE/READ/WRITE/REWRITE/DELETE/START, scanned statement-wide, excluding EXEC blocks) + EXEC CICS file verbs (one per EXEC CICS…END-EXEC block) + EXEC SQL data verbs (SELECT/INSERT/UPDATE/DELETE/FETCH) + EXEC DLI data verbs + MQGET/MQPUT/MQPUT1. DISPLAY, SEND/RECEIVE MAP, cursor/queue OPEN/CLOSE excluded.
- **Decision points** = IF + EVALUATE + WHEN tokens.
- **Max nesting** = push on IF/EVALUATE/inline PERFORM, pop on END-IF/END-EVALUATE/END-PERFORM; a sentence-ending period resets the stack (COBOL scope terminator). Computed inside PROCEDURE DIVISION only.
- **Dependencies** = inbound refs + outbound refs + JCL EXEC PGM= refs.


_I/O ops counts statements; see Recommendations for the CBSTM03A caveat._

## 2. Full metrics table

| Program | Sub-app | Class | LOC code | LOC total | Copybooks | I/O ops | Screen ops | Decisions | Max nest | Paras | GOTO | ALTER | Dyn CALL/XCTL | Out | In | JCL | Stores | Notes |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| COACTUPC | base | online | 3368 | 4236 | 18 | 7 | 2 | 301 | 3 | 87 | 51 | 0 | 1 | 6 | 1 | 0 | VSAM | 40+ COPY CSSETATY REPLACING; 51 GO TO |
| COTRTLIC | trantype | online | 1597 | 2098 | 15 | 10 | 2 | 165 | 5 | 56 | 28 | 0 | 1 | 3 | 2 | 0 | DB2 |  |
| COTRTUPC | trantype | online | 1241 | 1702 | 16 | 10 | 2 | 166 | 3 | 64 | 23 | 0 | 1 | 3 | 2 | 0 | DB2 |  |
| COCRDUPC | base | online | 1194 | 1560 | 13 | 3 | 2 | 139 | 3 | 46 | 21 | 0 | 1 | 4 | 4 | 0 | VSAM |  |
| COCRDLIC | base | online | 1093 | 1459 | 11 | 8 | 2 | 111 | 5 | 39 | 16 | 0 | 2 | 4 | 5 | 0 | VSAM |  |
| COPAUS0C | authorization | online | 792 | 1032 | 14 | 6 | 3 | 82 | 4 | 25 | 0 | 0 | 2 | 4 | 2 | 0 | VSAM, IMS |  |
| CBSTM03A | base | batch | 784 | 924 | 4 | 98 | 0 | 36 | 3 | 73 | 14 | 4 | 0 | 2 | 0 | 1 | VSAM | ALTER/GO TO dispatch; all file I/O delegated to CBSTM03B |
| COPAUA0C | authorization | online | 771 | 1026 | 14 | 14 | 0 | 52 | 3 | 65 | 0 | 0 | 0 | 4 | 0 | 0 | VSAM, IMS, MQ | MQ trigger; IMS+VSAM; only 4100/3100 decline reasons reachable |
| COACTVWC | base | online | 703 | 941 | 15 | 3 | 2 | 49 | 2 | 35 | 9 | 0 | 1 | 5 | 1 | 0 | VSAM |  |
| COCRDSLC | base | online | 642 | 887 | 13 | 2 | 2 | 53 | 2 | 35 | 9 | 0 | 1 | 3 | 5 | 0 | VSAM |  |
| CBTRN02C | base | batch | 619 | 731 | 5 | 22 | 0 | 48 | 4 | 56 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM | core posting; 4 reject codes 100-103 |
| COTRN02C | base | online | 614 | 783 | 10 | 6 | 2 | 90 | 4 | 34 | 0 | 0 | 1 | 4 | 1 | 0 | VSAM |  |
| CBACT04C | base | batch | 552 | 652 | 5 | 18 | 0 | 43 | 5 | 47 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM | interest = bal*rate/1200; cycle reset |
| CBTRN03C | base | batch | 545 | 649 | 5 | 19 | 0 | 46 | 5 | 52 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM |  |
| COUSR00C | base | online | 531 | 695 | 8 | 4 | 3 | 85 | 4 | 34 | 0 | 0 | 3 | 5 | 1 | 0 | VSAM |  |
| COTRN00C | base | online | 529 | 699 | 8 | 4 | 3 | 84 | 4 | 34 | 0 | 0 | 2 | 4 | 2 | 0 | VSAM |  |
| COACCT01 | vsam-mq | online | 500 | 620 | 7 | 7 | 0 | 35 | 2 | 23 | 0 | 0 | 0 | 4 | 0 | 0 | VSAM, MQ |  |
| CORPT00C | base | online | 498 | 649 | 8 | 8 | 3 | 44 | 3 | 20 | 1 | 0 | 1 | 4 | 1 | 0 | - |  |
| COPAUS1C | authorization | online | 461 | 604 | 10 | 4 | 3 | 42 | 3 | 16 | 0 | 0 | 1 | 3 | 1 | 0 | IMS |  |
| COBIL00C | base | online | 420 | 572 | 10 | 7 | 2 | 51 | 4 | 29 | 0 | 0 | 1 | 3 | 1 | 0 | VSAM |  |
| CBTRN01C | base | batch | 415 | 494 | 6 | 18 | 0 | 33 | 4 | 36 | 0 | 0 | 0 | 1 | 0 | 0 | VSAM |  |
| CODATE01 | vsam-mq | online | 409 | 524 | 6 | 6 | 0 | 30 | 2 | 23 | 0 | 0 | 0 | 4 | 0 | 0 | MQ |  |
| CBEXPORT | base | batch | 396 | 582 | 6 | 17 | 0 | 16 | 1 | 34 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM |  |
| CBACT01C | base | batch | 358 | 430 | 2 | 11 | 0 | 22 | 3 | 40 | 0 | 0 | 0 | 2 | 0 | 1 | VSAM |  |
| CBIMPORT | base | batch | 337 | 487 | 6 | 15 | 0 | 21 | 1 | 22 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM |  |
| COUSR02C | base | online | 303 | 414 | 8 | 2 | 2 | 38 | 4 | 23 | 0 | 0 | 1 | 3 | 2 | 0 | VSAM |  |
| CBPAUP0C | authorization | batch | 266 | 386 | 2 | 10 | 0 | 26 | 3 | 28 | 0 | 0 | 0 | 0 | 0 | 0 | IMS |  |
| PAUDBLOD | authorization | batch | 251 | 369 | 4 | 8 | 0 | 17 | 2 | 38 | 0 | 0 | 0 | 1 | 0 | 0 | VSAM, IMS |  |
| COUSR03C | base | online | 251 | 359 | 8 | 3 | 2 | 29 | 4 | 23 | 0 | 0 | 1 | 3 | 2 | 0 | VSAM |  |
| COTRN01C | base | online | 231 | 330 | 8 | 1 | 2 | 20 | 4 | 18 | 0 | 0 | 1 | 4 | 2 | 0 | VSAM |  |
| COMEN01C | base | online | 213 | 308 | 9 | 0 | 2 | 29 | 3 | 13 | 0 | 0 | 1 | 13 | 12 | 0 | - | hub: navigation via menu tables |
| PAUDBUNL | authorization | batch | 207 | 317 | 4 | 7 | 0 | 11 | 2 | 24 | 0 | 0 | 0 | 1 | 0 | 0 | VSAM, IMS |  |
| COPAUS2C | authorization | online | 201 | 244 | 3 | 2 | 0 | 3 | 2 | 2 | 0 | 0 | 0 | 0 | 1 | 0 | DB2 |  |
| DBUNLDGS | authorization | batch | 198 | 366 | 6 | 4 | 0 | 9 | 2 | 33 | 0 | 0 | 0 | 1 | 0 | 0 | IMS |  |
| COUSR01C | base | online | 198 | 299 | 8 | 1 | 2 | 21 | 3 | 17 | 0 | 0 | 1 | 3 | 1 | 0 | VSAM |  |
| COADM01C | base | online | 189 | 288 | 9 | 0 | 2 | 22 | 3 | 15 | 0 | 0 | 1 | 8 | 7 | 0 | - | hub: navigation via menu tables |
| COBTUPDT | trantype | batch | 177 | 237 | 2 | 6 | 0 | 19 | 1 | 24 | 0 | 0 | 0 | 0 | 0 | 0 | VSAM, DB2 |  |
| COSGN00C | base | online | 172 | 260 | 8 | 1 | 2 | 16 | 3 | 17 | 0 | 0 | 0 | 2 | 12 | 0 | VSAM |  |
| CBSTM03B | base | batch | 162 | 230 | 0 | 12 | 0 | 18 | 1 | 31 | 13 | 0 | 0 | 0 | 1 | 0 | VSAM |  |
| CBACT03C | base | batch | 130 | 178 | 1 | 4 | 0 | 11 | 3 | 11 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM |  |
| CBCUS01C | base | batch | 130 | 178 | 1 | 4 | 0 | 11 | 3 | 11 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM |  |
| CBACT02C | base | batch | 129 | 178 | 1 | 4 | 0 | 11 | 3 | 11 | 0 | 0 | 0 | 1 | 0 | 1 | VSAM |  |
| CSUTLDTC | base | batch | 114 | 157 | 0 | 0 | 0 | 11 | 1 | 2 | 0 | 0 | 0 | 1 | 3 | 0 | - |  |
| COBSWAIT | base | batch | 13 | 41 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 1 | - |  |

## 3. Top-10 tables

### By LOC (code lines)

| Rank | Program | Value | Context |
|---|---|---|---|
| 1 | COACTUPC | 3368 | online/base, LOC 3368 |
| 2 | COTRTLIC | 1597 | online/trantype, LOC 1597 |
| 3 | COTRTUPC | 1241 | online/trantype, LOC 1241 |
| 4 | COCRDUPC | 1194 | online/base, LOC 1194 |
| 5 | COCRDLIC | 1093 | online/base, LOC 1093 |
| 6 | COPAUS0C | 792 | online/authorization, LOC 792 |
| 7 | CBSTM03A | 784 | batch/base, LOC 784 |
| 8 | COPAUA0C | 771 | online/authorization, LOC 771 |
| 9 | COACTVWC | 703 | online/base, LOC 703 |
| 10 | COCRDSLC | 642 | online/base, LOC 642 |

### By unique copybooks

| Rank | Program | Value | Context |
|---|---|---|---|
| 1 | COACTUPC | 18 | online/base, LOC 3368 |
| 2 | COTRTUPC | 16 | online/trantype, LOC 1241 |
| 3 | COTRTLIC | 15 | online/trantype, LOC 1597 |
| 4 | COACTVWC | 15 | online/base, LOC 703 |
| 5 | COPAUS0C | 14 | online/authorization, LOC 792 |
| 6 | COPAUA0C | 14 | online/authorization, LOC 771 |
| 7 | COCRDUPC | 13 | online/base, LOC 1194 |
| 8 | COCRDSLC | 13 | online/base, LOC 642 |
| 9 | COCRDLIC | 11 | online/base, LOC 1093 |
| 10 | COTRN02C | 10 | online/base, LOC 614 |

### By I/O ops

| Rank | Program | Value | Context |
|---|---|---|---|
| 1 | CBSTM03A | 98 | batch/base, LOC 784 |
| 2 | CBTRN02C | 22 | batch/base, LOC 619 |
| 3 | CBTRN03C | 19 | batch/base, LOC 545 |
| 4 | CBACT04C | 18 | batch/base, LOC 552 |
| 5 | CBTRN01C | 18 | batch/base, LOC 415 |
| 6 | CBEXPORT | 17 | batch/base, LOC 396 |
| 7 | CBIMPORT | 15 | batch/base, LOC 337 |
| 8 | COPAUA0C | 14 | online/authorization, LOC 771 |
| 9 | CBSTM03B | 12 | batch/base, LOC 162 |
| 10 | CBACT01C | 11 | batch/base, LOC 358 |

### By max nesting depth

| Rank | Program | Value | Context |
|---|---|---|---|
| 1 | COTRTLIC | 5 | online/trantype, LOC 1597 |
| 2 | COCRDLIC | 5 | online/base, LOC 1093 |
| 3 | CBACT04C | 5 | batch/base, LOC 552 |
| 4 | CBTRN03C | 5 | batch/base, LOC 545 |
| 5 | COPAUS0C | 4 | online/authorization, LOC 792 |
| 6 | CBTRN02C | 4 | batch/base, LOC 619 |
| 7 | COTRN02C | 4 | online/base, LOC 614 |
| 8 | COUSR00C | 4 | online/base, LOC 531 |
| 9 | COTRN00C | 4 | online/base, LOC 529 |
| 10 | COBIL00C | 4 | online/base, LOC 420 |

### By dependencies (in+out+jcl)

| Rank | Program | Value | Context |
|---|---|---|---|
| 1 | COMEN01C | 25 | online/base, LOC 213 |
| 2 | COADM01C | 15 | online/base, LOC 189 |
| 3 | COSGN00C | 14 | online/base, LOC 172 |
| 4 | COCRDLIC | 9 | online/base, LOC 1093 |
| 5 | COCRDUPC | 8 | online/base, LOC 1194 |
| 6 | COCRDSLC | 8 | online/base, LOC 642 |
| 7 | COACTUPC | 7 | online/base, LOC 3368 |
| 8 | COPAUS0C | 6 | online/authorization, LOC 792 |
| 9 | COACTVWC | 6 | online/base, LOC 703 |
| 10 | COUSR00C | 6 | online/base, LOC 531 |

## 4. Composite ranking

Each metric normalized to 0-1 (value / max) and summed.

| Rank | Program | Score | LOC | Cpy | I/O | Nest | Dep |
|---|---|---|---|---|---|---|---|
| 1 | COACTUPC | 2.95 | 1.00 | 1.00 | 0.07 | 0.60 | 0.28 |
| 2 | COTRTLIC | 2.61 | 0.47 | 0.83 | 0.10 | 1.00 | 0.20 |
| 3 | COCRDLIC | 2.38 | 0.32 | 0.61 | 0.08 | 1.00 | 0.36 |
| 4 | CBSTM03A | 2.18 | 0.23 | 0.22 | 1.00 | 0.60 | 0.12 |
| 5 | COMEN01C | 2.16 | 0.06 | 0.50 | 0.00 | 0.60 | 1.00 |
| 6 | COTRTUPC | 2.16 | 0.37 | 0.89 | 0.10 | 0.60 | 0.20 |
| 7 | COPAUS0C | 2.11 | 0.24 | 0.78 | 0.06 | 0.80 | 0.24 |
| 8 | COCRDUPC | 2.03 | 0.35 | 0.72 | 0.03 | 0.60 | 0.32 |
| 9 | COPAUA0C | 1.91 | 0.23 | 0.78 | 0.14 | 0.60 | 0.16 |
| 10 | COTRN02C | 1.80 | 0.18 | 0.56 | 0.06 | 0.80 | 0.20 |

## Recommendations

### How to read the metrics

Two caveats before ranking anything:

- **I/O ops is a statement count, not a "files touched" count.** `CBSTM03A` scores 98 because it emits the HTML statement one `WRITE` per tag line (~90 WRITE statements against two files); it is not I/O-complex, it is verbose. `CBTRN02C` (22 ops across 6 files) and `CBACT04C` (18 across 5) are the genuinely I/O-dense programs. Read the I/O column together with "Files read/written" in the inventory.
- **Max nesting is uniformly low (≤5).** The estate is written in a flat style — every `IF` closes with `END-IF`, paragraphs are short, and control flow is expressed with `PERFORM`, `GO TO ... -EXIT` and `EVALUATE`. Nesting therefore does not separate programs well; the *decision-point* column (IF+EVALUATE+WHEN) is the better density signal, and it points at the same five programs as LOC: `COACTUPC` (301), `COTRTUPC` (166), `COTRTLIC` (165), `COCRDUPC` (139), `COCRDLIC` (111).

### Recommended modernization order

The ordering below optimises for *risk retirement per unit of effort* rather than for the composite score alone: the composite rewards big screens, but the programs that gate the business are the small batch ones.

| Wave | Programs | Why first | Java shape |
|---|---|---|---|
| **1. Ledger core** | `CBTRN02C`, `CBACT04C` + copybooks `CVACT01Y`, `CVACT03Y`, `CVTRA05Y/06Y`, `CVTRA01Y`, `CVTRA02Y` | These two programs are the only writers of `ACCTDATA`, `TCATBALF` and (batch) `TRANSACT`. They own every monetary rule in the estate — the four reject codes, the sign-based cycle buckets, `balance × rate / 1200`, the cycle reset — in ~1,170 LOC with zero screen code. They are also the reason CICS files must be closed nightly (`CLOSEFIL/OPENFIL`). Migrating them first (a) delivers the operational win of removing the batch window, (b) is cheap to golden-master test (flat-file in, VSAM image out — the `DALYREJS`/`SYSTRAN` GDGs are ready-made expected outputs), and (c) fixes the data model everything else depends on. | Spring Batch / plain JDBC posting service; `TranCatBalance`, `Account`, `Transaction` entities; interest as a domain service. |
| **2. Navigation shell + security** | `COSGN00C`, `COMEN01C`, `COADM01C`, `COCOM01Y`, `COMEN02Y`, `COADM02Y`, `CSUSR01Y` + `COUSR00C-03C` | Highest inbound dependency (COMEN01C 12 in / 13 out, COSGN00C 12 in). Nothing online can be cut over until the COMMAREA contract (`COCOM01Y`) has a replacement — session/JWT claims carrying user-type, account-in-context, card-in-context. Tiny programs (170–530 LOC), so this wave is mostly design (auth model, role gating, menu-table → route table) not code. Also retires the plaintext-password `USRSEC` file. | Auth service + role-based routing; user admin CRUD. |
| **3. Account/Card maintenance** | `COACTUPC`, `COACTVWC`, `COCRDLIC`, `COCRDSLC`, `COCRDUPC` (+ `CSUTLDPY/CSUTLDWY/CSUTLDTC`, `CSLKPCDY`, `CVCRD01Y`) | Composite #1, #3 and #8. `COACTUPC` alone is 3,368 LOC, 18 copybooks, 51 `GO TO`, 40+ `COPY … REPLACING` expansions of `CSSETATY`, and it rewrites two masters in one screen. It carries the full customer/account edit rule set (SSN, FICO 300-850, state/ZIP combos, phone area codes, date edits via CEEDAYS) — that rule set should be extracted to a validation library *before* re-platforming and reused by every later wave. The card trio shares `CVCRD01Y` + `CSSTRPFY` and the optimistic-lock pattern (`Record changed by some one else`). | Account/Card REST resources; Bean-Validation constraints generated from the dictionary rules. |
| **4. Transaction online + reporting** | `COTRN00C/01C/02C`, `COBIL00C`, `CORPT00C` → `CBTRN03C`, `CREASTMT` → `CBSTM03A/B` | Depends on wave 1 (ledger schema) and wave 2 (context). `COTRN02C` and `COBIL00C` write the ledger directly, bypassing posting validation — a behaviour to consciously keep or drop. `CBSTM03A` is the most *idiosyncratic* code in the estate (`ALTER`/`GO TO` dispatch into `CBSTM03B`, `READ UPDATE` in `COTRN01C` without a `REWRITE`); rewrite from the *output* (statement text/HTML) rather than translate. | Ledger query API; statement/report generation as templated jobs. |
| **5. Authorization extension** | `COPAUA0C`, `COPAUS0C/1C/2C`, `CBPAUP0C`, `CIPAUSMY/CIPAUDTY`, IMS DBD/PSB, `AUTHFRDS` | Isolated: only inbound edge is the menu option; only outbound edges are VSAM reads that wave 1 already re-homes. Defer because (a) it needs MQ + IMS emulation or a real queue to test, (b) the decision logic is small (`6000-MAKE-DECISION`: one available-credit check; reasons 4200/4300/5100/5200 are unreachable) and will be re-specified rather than ported, and (c) there is no auth → posting bridge to preserve. | Event-driven auth service (queue in/out); pending-auth store as an aggregate keyed by account. |
| **6. Transaction-type admin (DB2)** | `COTRTLIC`, `COTRTUPC`, `COBTUPDT`, `TRANEXTR` | Composite #2 and #6 by score, but pure reference-data CRUD (two tables, one FK). The DB2 tables can be lifted into the target schema as-is; the VSAM projection (`TRANEXTR`) disappears once wave 1/4 read reference data from the same store. High LOC is screen paging boilerplate, not business logic. | JPA entities + admin CRUD; drop the nightly extract. |
| **Retire / don't port** | `CBACT01-03C`, `CBCUS01C` (read-and-print smoke tests), `CBEXPORT/CBIMPORT` (one-off migration utility — reuse as a data-migration *input*, not a target), `COBSWAIT/MVSWAIT`, `COBDATFT`, `CODATE01/COACCT01` (MQ demo wrappers), `UNUSED1Y`, `COCRDSEC` (CSD entry with no source), `CBTRN01C` (earlier posting variant not referenced by any JCL), IMS unload/load jobs. | | |

### Cross-cutting items to fix during migration, not after

1. **Shared record layouts** — `CVTRA05Y`, `CVTRA06Y` and `COSTM01` are byte-identical; `CVCUS01Y` and `CUSTREC` are byte-identical. Model each once.
2. **Misspelled identifiers** (`EXPIRAION`, `CATAGORY`, `Authoriation`) are part of the on-disk contract and appear in DB2 column names (`MERCHANT_CATAGORY_CODE`); decide the canonical Java names now and keep a mapping table for data migration.
3. **Sign convention** — positive `TRAN-AMT` = charge and is accumulated into a field named `CYC-CREDIT`. Name the target fields by meaning (`cycleCharges` / `cyclePayments`) and cover it with a golden-master test in wave 1.
4. **Validation rules live in one program** (`COACTUPC`) but apply to three entities. Lift them into the domain model in wave 3 so waves 4–6 inherit them.
5. **Dead reason codes and unused fields** (`PA-MATCH-STATUS` E/M, decline reasons 4200–5200, `1400-COMPUTE-FEES`, `CSMSG02Y` abend area in most programs) are requirements that were never implemented — log them as product decisions, not as behaviour to preserve.
