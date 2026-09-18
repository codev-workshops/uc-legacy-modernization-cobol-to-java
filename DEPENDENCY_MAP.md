# CardDemo – Dependency Map

Derived from static analysis of every `.cbl`, `.jcl`, `.prc`, `.cpy` and scheduler member under `app/`
(44 programs, 48 JCL/PROC members). Call edges come from `CALL`, `EXEC CICS LINK/XCTL/RETURN TRANSID`,
menu tables (`COMEN02Y`, `COADM02Y`) and MQ/TDQ triggers. Dataset edges come from JCL `DD` statements,
IDCAMS control cards and COBOL `SELECT … ASSIGN` / `EXEC CICS … DATASET(…)` clauses.

Legend: `──▶` control transfer · `──▷` data flow · `⋯▶` asynchronous / indirect trigger

---

## 1. Program call graph

### 1.1 Online (CICS) navigation graph

All online programs share one pattern: entered via a 4-char transaction, pseudo-conversational
(`EXEC CICS RETURN TRANSID(self) COMMAREA(CARDDEMO-COMMAREA)`), and navigate by
`EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` where the target is resolved from `COCOM01Y` fields
populated from the menu tables or hard-coded `MOVE 'xxxxxxxx' TO CDEMO-TO-PROGRAM`.

```
                       CC00 ─▶ COSGN00C (sign-on, reads USRSEC)
                                 │ user type 'A'            │ user type 'U'
                                 ▼                          ▼
                    CA00 ─▶ COADM01C (admin menu)   CM00 ─▶ COMEN01C (main menu)
                    table COADM02Y                   table COMEN02Y
        ┌────────┬────────┬────────┬───────┬────────┐   ┌───────┬───────┬────────┬────────┬────────┬────────┬────────┬───────┬────────┬────────┐
        ▼        ▼        ▼        ▼       ▼        ▼   ▼       ▼       ▼        ▼        ▼        ▼        ▼        ▼       ▼        ▼        ▼
     COUSR00C COUSR01C COUSR02C COUSR03C COTRTLIC COTRTUPC COACTVWC COACTUPC COCRDLIC COCRDSLC COCRDUPC COTRN00C COTRN01C COTRN02C CORPT00C COBIL00C COPAUS0C
      (CU00)   (CU01)   (CU02)   (CU03)   (CTLI)   (CTTU)   (CAVW)   (CAUP)   (CCLI)   (CCDL)   (CCUP)   (CT00)   (CT01)   (CT02)   (CR00)   (CB00)   (CPVS)
        │ S/U/D select                     │ ◀────────┘ (XCTL back to CTTU for add)  │ select     │ select                  │ TDQ 'JOBS'        │ select
        ├─▶ COUSR02C / COUSR03C            │                                        └─▶ COCRDSLC └─▶ COTRN01C                ⋯▶ INTRDR          ▼
        │                                  │                                                                              (submits TRANREPT)  COPAUS1C (CPVD)
        └─▶ back: COADM01C / COSGN00C      └─▶ back: COADM01C                                                                                   │ PF5
                                                                                                                                                 ▼
   MQ trigger ⋯▶ CP00 ─▶ COPAUA0C   (no screen; reads CCXREF/ACCTDAT/CUSTDAT, IMS DBPAUTP0, MQ reply)                                   LINK ─▶ COPAUS2C (DB2 AUTHFRDS insert)
   MQ trigger ⋯▶ CDRD ─▶ CODATE01   (no screen; MQ request/reply, system date)
   MQ trigger ⋯▶ CDRA ─▶ COACCT01   (no screen; MQ request/reply, reads ACCTDAT)
```

Resolved XCTL / LINK / CALL edges (from source, not README):

| From | Kind | To | Mechanism |
|---|---|---|---|
| COSGN00C | XCTL | COADM01C, COMEN01C | on `SEC-USR-TYPE` = 'A' / 'U' |
| COMEN01C | XCTL | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | option 1–11 in `COMEN02Y` |
| COADM01C | XCTL | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | option 1–6 in `COADM02Y` |
| COUSR00C | XCTL | COUSR02C, COUSR03C, COADM01C, COSGN00C | row select 'U'/'D'; PF3 |
| COUSR01C/02C/03C | XCTL | COADM01C, COSGN00C | PF3 / PF4 |
| COCRDLIC | XCTL | COCRDSLC, COCRDUPC, COMEN01C | row select 'S'/'U'; PF3 (via `CCARD-NEXT-PROG`) |
| COCRDSLC, COCRDUPC, COACTVWC, COACTUPC | XCTL | `CDEMO-TO-PROGRAM` (COMEN01C by default) | PF3 |
| COTRN00C | XCTL | COTRN01C, COMEN01C, COSGN00C | row select; PF3 |
| COTRN01C | XCTL | COTRN00C, COMEN01C, COSGN00C | PF4/PF5; PF3 |
| COTRN02C | CALL | CSUTLDTC | date validation subroutine |
| COTRN02C, CORPT00C, COBIL00C | XCTL | COMEN01C, COSGN00C | PF3 |
| CORPT00C | CALL | CSUTLDTC | date validation |
| CORPT00C | TDQ WRITEQ 'JOBS' | JES internal reader → job `TRNRPT00` → PROC **TRANREPT** | online → batch bridge |
| COTRTLIC | XCTL | COTRTUPC, `CDEMO-TO-PROGRAM` | PF-key add; PF3 |
| COTRTUPC | XCTL | `CDEMO-TO-PROGRAM` (COADM01C) | PF3 |
| COPAUS0C | XCTL | COPAUS1C, COSGN00C, `CDEMO-TO-PROGRAM` | row select; PF3 |
| COPAUS1C | LINK | COPAUS2C | PF5 mark fraud (IMS REPL + DB2 INSERT under one SYNCPOINT) |
| COPAUS1C | XCTL | COPAUS0C via `CDEMO-TO-PROGRAM` | PF3 |
| COPAUA0C, CODATE01, COACCT01 | — | (started by MQ trigger monitor `EXEC CICS RETRIEVE INTO(MQTM)`) | no outbound program calls |

### 1.2 Batch call graph

```
JCL job ──▶ main program ──▶ CALLed routines

POSTTRAN ──▶ CBTRN02C ──▶ CEE3ABD (LE abend)
INTCALC  ──▶ CBACT04C ──▶ CEE3ABD
TRANREPT ──▶ CBTRN03C ──▶ CEE3ABD
CREASTMT ──▶ CBSTM03A ──▶ CBSTM03B (generic file I/O: OPEN/READ/CLOSE by DD name)   ──▶ CEE3ABD
READACCT ──▶ CBACT01C ──▶ COBDATFT (asm date-format utility, app/asm)  ──▶ CEE3ABD
READCARD ──▶ CBACT02C ──▶ CEE3ABD
READCUST ──▶ CBCUS01C ──▶ CEE3ABD
READXREF ──▶ CBACT03C ──▶ CEE3ABD
CBEXPORT ──▶ CBEXPORT ──▶ CEE3ABD
CBIMPORT ──▶ CBIMPORT ──▶ CEE3ABD
WAITSTEP ──▶ COBSWAIT ──▶ MVSWAIT (asm STIMER wait, app/asm)
MNTTRDB2 ──▶ IKJEFT01/DSN RUN ──▶ COBTUPDT (DB2 static SQL)
CBPAUP0J ──▶ DFSRRC00 (IMS BMP) ──▶ CBPAUP0C   (PSB PSBPAUTB)
UNLDPADB ──▶ DFSRRC00 ──▶ PAUDBUNL             (PSB PAUTBUNL; DLI CBLTDLI)
LOADPADB ──▶ DFSRRC00 ──▶ PAUDBLOD             (PSB PSBPAUTL)
UNLDGSAM ──▶ DFSRRC00 ──▶ DBUNLDGS             (PSB DLIGSAMP; GSAM output PCBs PASFLPCB/PADFLPCB)

(no JCL in repo) CBTRN01C — validation-only reader of DALYTRAN (no output file); DBPAUTP0.jcl uses IMS DFSURGU0 unload utility, not an app program.
```

Programs with **no inbound edge** (dead or externally driven): `CBTRN01C` (no JCL), `UNUSED1Y` (copybook, no COPY),
`CBSTM03B`/`CSUTLDTC`/`COBSWAIT` are reached only via CALL.

### 1.3 Shared-copybook coupling (fan-in ≥ 10 programs)

| Copybook | Programs | Coupling type |
|---|:-:|---|
| COCOM01Y (COMMAREA) | 21 | every online program; schema change = recompile all CICS programs |
| COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA | 21 | UI framework |
| CVACT01Y (Account), CVACT03Y (Xref) | 14 | record layouts shared online + batch |
| CSUSR01Y (User) | 14 | sign-on + user admin + card programs (user-type check) |
| CVTRA05Y (Transaction) | 11 | online tran screens + all posting/report batch |
| CVACT02Y (Card), CVCUS01Y (Customer) | 10 | |

---

## 2. Dataset lineage

### 2.1 Master VSAM files – producers and consumers

| Dataset (AWS.M2.CARDDEMO.*) | CICS FILE | Layout | Loaded by (JCL) | Batch readers | Batch writers | Online readers | Online writers |
|---|---|---|---|---|---|---|---|
| ACCTDATA.VSAM.KSDS | ACCTDAT | CVACT01Y | ACCTFILE (REPRO ← ACCTDATA.PS) | CBACT01C, CBTRN01C, CBEXPORT, CBSTM03B | **CBTRN02C** (REWRITE balances), **CBACT04C** (REWRITE interest/cycle reset) | COACTVWC, COACTUPC, COBIL00C, COTRN02C*, COPAUA0C, COPAUS0C, COACCT01 | **COACTUPC** (REWRITE), **COBIL00C** (REWRITE balance) |
| CUSTDATA.VSAM.KSDS | CUSTDAT | CVCUS01Y | CUSTFILE (REPRO ← CUSTDATA.PS) | CBCUS01C, CBTRN01C, CBEXPORT, CBSTM03B | — | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COPAUA0C, COPAUS0C | **COACTUPC** (REWRITE) |
| CARDDATA.VSAM.KSDS (+ .AIX by acct id) | CARDDAT / CARDAIX | CVACT02Y | CARDFILE (REPRO ← CARDDATA.PS, BLDINDEX) | CBACT02C, CBTRN01C, CBEXPORT | — | COCRDLIC, COCRDSLC, COCRDUPC | **COCRDUPC** (REWRITE) |
| CARDXREF.VSAM.KSDS (+ .AIX by acct id) | CCXREF / CXACAIX | CVACT03Y | XREFFILE (REPRO ← CARDXREF.PS, BLDINDEX) | CBACT03C, CBTRN01C, CBTRN02C, CBTRN03C, CBACT04C, CBEXPORT, CBSTM03B | — | COACTVWC, COACTUPC, COTRN02C, COBIL00C, COPAUA0C, COPAUS0C | — |
| TRANSACT.VSAM.KSDS (+ .AIX by TRAN-PROC-TS) | TRANSACT | CVTRA05Y | TRANFILE (REPRO ← DALYTRAN.PS.INIT), COMBTRAN (REPRO ← COMBINED), TRANIDX | CBTRN01C, CBEXPORT, TRANBKP/TRANREPT/CREASTMT (REPRO/SORT) | **CBTRN02C** (OPEN OUTPUT – rebuilds) | COTRN00C, COTRN01C, COTRN02C, COBIL00C | **COTRN02C** (WRITE), **COBIL00C** (WRITE) |
| USRSEC.VSAM.KSDS | USRSEC | CSUSR01Y | DUSRSECJ (REPRO ← USRSEC.PS) | — | — | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) |
| TCATBALF.VSAM.KSDS | — | CVTRA01Y | TCATBALF (REPRO ← TCATBALF.PS) | CBACT04C, PRTCATBL (REPRO/SORT) | **CBTRN02C** (WRITE/REWRITE) | — | — |
| DISCGRP.VSAM.KSDS | — | CVTRA02Y | DISCGRP (REPRO ← DISCGRP.PS) | CBACT04C | — | — | — |
| TRANTYPE.VSAM.KSDS | — | CVTRA03Y | TRANTYPE (REPRO ← TRANTYPE.PS) | CBTRN03C | — | — | — |
| TRANCATG.VSAM.KSDS | — | CVTRA04Y | TRANCATG (REPRO ← TRANCATG.PS) | CBTRN03C | — | — | — |
| EXPORT.DATA (KSDS) | — | CVEXPORT | CBEXPORT job (DEFINE) | CBIMPORT | CBEXPORT | — | — |

\* COTRN02C reads XREF only; balance is not updated by online transaction add.

### 2.2 Sequential / GDG datasets

| Dataset | Produced by | Consumed by | Notes |
|---|---|---|---|
| DALYTRAN.PS | **external feed** (sample data only; no program writes it) | POSTTRAN → CBTRN02C; CBTRN01C | CVTRA06Y layout = CVTRA05Y |
| DALYTRAN.PS.INIT | sample data | TRANFILE (initial load of TRANSACT) | |
| DALYREJS(+1) GDG | POSTTRAN → CBTRN02C | — (no consumer) | 350-byte tran + 80-byte reason trailer; GDG base by DALYREJS.jcl |
| TRANSACT.BKUP(+1) GDG | TRANBKP, TRANREPT (REPROC) | COMBTRAN (SORTIN BKUP(0)), TRANREPT SORT | GDG base by DEFGDGB |
| SYSTRAN(+1) GDG | INTCALC → CBACT04C | COMBTRAN (SORTIN SYSTRAN(0)) | system-generated interest transactions |
| TRANSACT.COMBINED(+1) GDG | COMBTRAN SORT | COMBTRAN REPRO → TRANSACT.VSAM.KSDS | |
| TRANSACT.DALY(+1) GDG | TRANREPT SORT (date-window filter on TRAN-PROC-DT, sorted by card) | TRANREPT → CBTRN03C | |
| TRANREPT(+1) GDG | TRANREPT → CBTRN03C | print / TXT2PDF (manual) | GDG base by REPTFILE.jcl |
| DATEPARM | CORPT00C (in-stream in generated job) / manual | CBTRN03C | start/end date |
| TRXFL.SEQ, TRXFL.VSAM.KSDS | CREASTMT SORT (re-key card‖tranid) + REPRO | CBSTM03A/B | temp, rebuilt each run |
| STATEMNT.PS, STATEMNT.HTML | CREASTMT → CBSTM03A | TXT2PDF1 (STATEMNT.PS → STATEMNT.PS.PDF) | |
| TCATBALF.BKUP(+1), TCATBALF.REPT | PRTCATBL | — | report of category balances |
| TRANTYPE.PS, TRANCATG.PS | TRANEXTR (DSNTIAUL unload from DB2) | TRANTYPE.jcl, TRANCATG.jcl (REPRO → VSAM) | DB2 → VSAM reference-data sync; backups TRANTYPE.BKUP / TRANCATG.PS.BKUP GDGs (DEFGDGD) |
| ACCTDATA.PSCOMP / ARRYPS / VBPS | READACCT → CBACT01C | — | demo of COMP/array/VB record formats |
| CUSTDATA.IMPORT, ACCTDATA.IMPORT, CARDXREF.IMPORT, TRANSACT.IMPORT, IMPORT.ERRORS | CBIMPORT | — (would feed *FILE load jobs) | |
| PAUTDB.ROOT.FILEO / CHILD.FILEO | UNLDPADB → PAUDBUNL | LOADPADB → PAUDBLOD | IMS unload/reload round-trip |
| PAUTDB.ROOT.GSAM / CHILD.GSAM | UNLDGSAM → DBUNLDGS | — | GSAM variant of unload |
| IMSDATA.DBPAUTP0 | DBPAUTP0.jcl (DFSURGU0 HD unload) | — | shipped as sample under data/EBCDIC |

### 2.3 Non-VSAM stores

| Store | Object | Written by | Read by |
|---|---|---|---|
| IMS DB DBPAUTP0 (HIDAM) + DBPAUTX0 (index) | PAUTSUM0 root / PAUTDTL1 child | COPAUA0C (ISRT/REPL), COPAUS1C (REPL fraud flag), CBPAUP0C (DLET), PAUDBLOD (ISRT) | COPAUS0C, COPAUS1C, CBPAUP0C, PAUDBUNL, DBUNLDGS |
| DB2 CARDDEMO.AUTHFRDS | fraud-flagged authorizations | COPAUS2C (INSERT) | — (analytics; no reader in repo) |
| DB2 CARDDEMO.TRANSACTION_TYPE | tran type reference | COTRTUPC (INSERT/UPDATE/DELETE), COTRTLIC (UPDATE/DELETE), COBTUPDT (I/U/D) | COTRTLIC (cursor), COTRTUPC, TRANEXTR (DSNTIAUL) |
| DB2 CARDDEMO.TRANSACTION_TYPE_CATEGORY | tran category reference | CREADB21 load only | COTRTUPC (FK check), TRANEXTR |
| MQ AWS.M2.CARDDEMO.PAUTH.REQUEST / .REPLY | auth request / response | external POS client / COPAUA0C | COPAUA0C / external client |
| MQ CARDDEMO.REQUEST.QUEUE / RESPONSE.QUEUE | date & account inquiry | external / CODATE01, COACCT01 | CODATE01, COACCT01 / external |
| CICS TDQ `JOBS` (INTRDR) | generated JCL | CORPT00C | JES |

---

## 3. End-to-end batch pipeline

### 3.1 Logical flow (data-driven order)

```
 ┌─────────────── ENVIRONMENT / REFERENCE DATA (one-off or weekly) ────────────────┐
 │ DEFGDGB, DEFGDGD, DALYREJS, REPTFILE     define GDG bases                        │
 │ DUSRSECJ ─▷ USRSEC          ACCTFILE ─▷ ACCTDAT     CUSTFILE ─▷ CUSTDAT          │
 │ CARDFILE ─▷ CARDDAT+AIX     XREFFILE ─▷ CCXREF+AIX  TRANFILE ─▷ TRANSACT(+AIX)   │
 │ DISCGRP  ─▷ DISCGRP         TCATBALF ─▷ TCATBALF                                 │
 │ [DB2 opt] CREADB21 ─▷ TRANSACTION_TYPE(_CATEGORY) ─▷ TRANEXTR ─▷ TRANTYPE.PS,    │
 │           TRANCATG.PS ─▷ TRANTYPE.jcl / TRANCATG.jcl ─▷ TRANTYPE, TRANCATG VSAM  │
 │ [DB2 opt] MNTTRDB2 (COBTUPDT) batch maintenance of TRANSACTION_TYPE              │
 └──────────────────────────────────────────────────────────────────────────────────┘
                                          │
 ┌────────────────────────── DAILY CYCLE ─┼──────────────────────────────────────────┐
 │ CLOSEFIL (CEMT CLOSE CICS files)       ▼                                          │
 │ [IMS opt] CBPAUP0J ─▶ CBPAUP0C : purge expired IMS pending auths (≥ N days)       │
 │ TRANBKP  : REPRO TRANSACT ─▷ TRANSACT.BKUP(+1); DELETE/DEFINE TRANSACT KSDS       │
 │ POSTTRAN ─▶ CBTRN02C : DALYTRAN.PS ─▷ validate (XREF, ACCT, limit, expiry)        │
 │              ├─▷ TRANSACT (rebuilt)  ├─▷ ACCTDAT balances  ├─▷ TCATBALF           │
 │              └─▷ DALYREJS(+1)                                                     │
 │ TRANIDX  : rebuild TRANSACT AIX                                                   │
 │ WAITSTEP ─▶ COBSWAIT/MVSWAIT (pacing)                                             │
 │ OPENFIL  (CEMT OPEN CICS files)                                                   │
 └───────────────────────────────────────────────────────────────────────────────────┘
                                          │
 ┌───────────────────────── MONTHLY / CYCLE-END ─────────────────────────────────────┐
 │ CLOSEFIL                                                                          │
 │ INTCALC ─▶ CBACT04C : TCATBALF × DISCGRP rate ─▷ SYSTRAN(+1) interest trans       │
 │                        ACCTDAT: CURR-BAL += interest; CYC-CREDIT/DEBIT := 0       │
 │ COMBTRAN : SORT TRANSACT.BKUP(0) + SYSTRAN(0) by TRAN-ID ─▷ TRANSACT.COMBINED(+1) │
 │            REPRO COMBINED ─▷ TRANSACT.VSAM.KSDS                                   │
 │ WAITSTEP, OPENFIL                                                                 │
 └───────────────────────────────────────────────────────────────────────────────────┘
                                          │
 ┌───────────────────────── REPORTING / STATEMENTS ──────────────────────────────────┐
 │ CREASTMT : SORT TRANSACT ─▷ TRXFL.SEQ (key card‖tranid) ─▷ REPRO TRXFL.VSAM       │
 │            CBSTM03A/CBSTM03B : per XREF card + CUST + ACCT ─▷ STATEMNT.PS, .HTML   │
 │ TXT2PDF1 : STATEMNT.PS ─▷ STATEMNT.PS.PDF                                         │
 │ TRANREPT (PROC, also submitted online by CORPT00C via INTRDR):                    │
 │            REPRO TRANSACT ─▷ BKUP(+1) ─▷ SORT date-window ─▷ TRANSACT.DALY(+1)     │
 │            ─▶ CBTRN03C (+CCXREF, TRANTYPE, TRANCATG, DATEPARM) ─▷ TRANREPT(+1)     │
 │ PRTCATBL : REPRO TCATBALF ─▷ BKUP(+1) ─▷ SORT ─▷ TCATBALF.REPT                     │
 └───────────────────────────────────────────────────────────────────────────────────┘

 Utility / ad-hoc: READACCT, READCARD, READCUST, READXREF (dump VSAM to SYSOUT / alt formats),
 CBEXPORT → EXPORT.DATA → CBIMPORT (multi-entity export/import), ESDSRRDS (ESDS/RRDS demo),
 DEFCUST, FTPJCL, INTRDRJ1/2, CBADMCDJ (DFHCSDUP), UNLDPADB/LOADPADB/UNLDGSAM/DBPAUTP0 (IMS utilities).
```

### 3.2 Scheduler-defined orderings (as shipped)

**Control-M** (`app/scheduler/CardDemo.controlm`, condition-chained folders):

| Folder | Order |
|---|---|
| DAILY-TransactionBackup | CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL |
| MONTHLY-InterestCalculation | CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL |
| WEEKLY-TransactionTypesDBRefresh | MNTTRDB2 → TRANEXTR |
| WEEKLY-DisclosureGroupsRefresh (after the above) | CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL |

**CA-7** (`app/scheduler/CardDemo.ca7`, trigger chains, SCHID 030/031/032):

| Chain | Order |
|---|---|
| Daily posting | CLOSEFIL → CBPAUP0J → POSTTRAN → WAITSTEP → OPENFIL |
| Reference refresh | CLOSEFIL → TRANTYPE → WAITSTEP → {CLOSEFIL1 → TRANCATG → WAITSTEP} ∥ {CLOSEFIL2 → TCATBALF → WAITSTEP} → CLOSEFIL |
| Master dumps | CLOSEFIL → READACCT → READCARD → READCUST → READXREF → WAITSTEP → OPENFIL |
| Statements | CLOSEFIL → CREASTMT → TXT2PDF1 → WAITSTEP → OPENFIL |
| Category report | CLOSEFIL → PRTCATBL → WAITSTEP → OPENFIL |

Note: neither scheduler includes `COMBTRAN` after `POSTTRAN` nor `TRANIDX`; the README's manual
sequence (TRANBKP → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX) is the only place the
full chain is expressed.

### 3.3 Critical observations for modernization

1. **Two disconnected pipelines.** Real-time authorization (MQ → COPAUA0C → IMS) never feeds
   `DALYTRAN.PS`; `PA-MATCH-STATUS = 'M'` (matched) is defined in `CIPAUDTY` but never set. The
   auth→clearing matcher must be designed, not migrated.
2. **`TRANSACT` is rebuilt, not updated.** `CBTRN02C` opens it `OUTPUT`; `TRANBKP`/`COMBTRAN`
   delete/define/REPRO the cluster. Any Java target needs an append/merge design plus AIX rebuild
   semantics (`TRANIDX`).
3. **Three writers of account balance** with different rules: `CBTRN02C` (post), `CBACT04C`
   (interest + cycle reset), `COBIL00C` (online bill payment). `COTRN02C` writes a transaction
   **without** touching the balance — an inconsistency to resolve.
4. **DB2 → VSAM reference-data replication** (`TRANEXTR` → `TRANTYPE.PS`/`TRANCATG.PS` → REPRO)
   exists only so batch can read VSAM; collapsing to one store removes 4 jobs.
5. **Online→batch bridge**: `CORPT00C` composes JCL text and writes it to TDQ `JOBS`; replace with
   an async job/report service.
6. **CICS file open/close choreography** (`CLOSEFIL`/`OPENFIL` via SDSF `/F CICS…CEMT`) exists
   only because batch and CICS share VSAM; disappears with a DBMS.
7. **GDG-based hand-offs** (`BKUP`, `SYSTRAN`, `COMBINED`, `DALY`, `DALYREJS`, `TRANREPT`) are
   implicit contracts between jobs with relative-generation semantics `(0)`/`(+1)` that must be
   made explicit (e.g. run-id-keyed staging tables or object keys).
