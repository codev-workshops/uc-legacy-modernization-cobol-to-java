# Dependency Map — CardDemo COBOL Estate

## Overview

This document maps inter-program call relationships, dataset lineage through JCL pipelines, and end-to-end batch processing flows for the CardDemo application.

---

## 1. Inter-Program Call Graph

### 1.1 Direct CALL Relationships

```
CBACT01C ──CALL──→ COBDATFT (Assembler date formatter)
         ──CALL──→ CEE3ABD  (LE abnormal termination)

CBACT02C ──CALL──→ CEE3ABD
CBACT03C ──CALL──→ CEE3ABD
CBACT04C ──CALL──→ CEE3ABD
CBCUS01C ──CALL──→ CEE3ABD
CBTRN01C ──CALL──→ CEE3ABD
CBTRN02C ──CALL──→ CEE3ABD
CBTRN03C ──CALL──→ CEE3ABD
CBEXPORT ──CALL──→ CEE3ABD
CBIMPORT ──CALL──→ CEE3ABD

CBSTM03A ──CALL──→ CBSTM03B (Sub-program for file processing)
         ──CALL──→ CEE3ABD

CORPT00C ──CALL──→ CSUTLDTC (Date utility)
COTRN02C ──CALL──→ CSUTLDTC (Date utility)
CSUTLDTC ──CALL──→ CEEDAYS  (LE date conversion)

COBSWAIT ──CALL──→ MVSWAIT  (MicroFocus wait routine)
```

### 1.2 CICS XCTL (Transfer Control) Relationships

```
COSGN00C ──XCTL──→ COMEN01C (Regular user menu)
         ──XCTL──→ COADM01C (Admin user menu)

COMEN01C ──XCTL──→ COACTVWC | COACTUPC | COCRDLIC | COCRDSLC |
                    COCRDUPC | COTRN00C | COTRN01C | COTRN02C |
                    CORPT00C | COBIL00C | COPAUS0C

COADM01C ──XCTL──→ COUSR00C | COUSR01C | COUSR02C | COUSR03C |
                    COTRTLIC | COTRTUPC

COACTVWC ──XCTL──→ COMEN01C (return to menu)
COACTUPC ──XCTL──→ COMEN01C
COCRDLIC ──XCTL──→ COCRDSLC | COCRDUPC | COMEN01C
COCRDSLC ──XCTL──→ COCRDUPC | COMEN01C
COCRDUPC ──XCTL──→ COMEN01C
```

### 1.3 CICS LINK Relationships

```
COPAUS1C ──LINK──→ COPAUS2C (Mark fraud sub-program)
```

### 1.4 MQ Call Relationships (Authorization Sub-App)

```
COPAUA0C ──MQ──→ Request Queue (MQGET)
         ──MQ──→ Reply Queue (MQPUT1)
         ──MQ──→ MQOPEN / MQCLOSE

COACCT01 ──MQ──→ Input Queue (MQGET)
         ──MQ──→ Output Queue (MQPUT)
         ──MQ──→ Error Queue (MQPUT)

CODATE01 ──MQ──→ Input Queue (MQGET)
         ──MQ──→ Output Queue (MQPUT)
         ──MQ──→ Error Queue (MQPUT)
```

### 1.5 IMS DL/I Call Relationships

```
DBUNLDGS ──DL/I──→ CBLTDLI (GN, GNP, ISRT functions)
PAUDBLOD ──DL/I──→ CBLTDLI (ISRT function - load)
PAUDBUNL ──DL/I──→ CBLTDLI (GN, GNP functions - unload)
```

---

## 2. Dataset Lineage

### 2.1 VSAM Dataset → Program → JCL Mapping

| Dataset (DD Name) | VSAM Cluster | Programs That READ | Programs That WRITE/REWRITE | JCL That Defines |
|-------------------|-------------|-------------------|---------------------------|------------------|
| ACCTFILE / ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBSTM03B, COACTUPC, COACTVWC, COBIL00C, COTRN02C | CBACT04C (REWRITE), CBTRN02C (REWRITE), COACTUPC (REWRITE), COBIL00C (REWRITE), CBIMPORT | ACCTFILE.jcl |
| CARDFILE / CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | CBACT02C, CBTRN01C, CBEXPORT, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COACTUPC | COCRDUPC (REWRITE), CBIMPORT | CARDFILE.jcl |
| CUSTFILE / CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | CBCUS01C, CBTRN01C, CBEXPORT, CBSTM03B, COACTVWC, COACTUPC, COCRDSLC | COACTUPC (REWRITE), CBIMPORT | CUSTFILE.jcl |
| XREFFILE / XREFDAT | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBSTM03B, COACTVWC, COACTUPC, COTRN02C | CBIMPORT | XREFFILE.jcl |
| TRANFILE / TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | CBACT04C, CBTRN03C, CBEXPORT, COTRN00C, COTRN01C | CBTRN01C, CBTRN02C, COTRN02C (WRITE), COBIL00C (WRITE) | TRANFILE.jcl |
| DALYTRAN | AWS.M2.CARDDEMO.DALYTRAN.PS | CBTRN01C, CBTRN02C | (External feed) | (Sequential PS file) |
| TCATBALF | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | CBACT04C, CBTRN02C | CBTRN02C (WRITE/REWRITE), CBACT04C | TCATBALF.jcl |
| DISCGRP | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | CBACT04C | (Reference data) | DISCGRP.jcl |
| TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | CBTRN03C | (Reference data) | TRANTYPE.jcl |
| TRANCATG | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | CBTRN03C | (Reference data) | TRANCATG.jcl |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) | DUSRSECJ.jcl |
| DALYREJS | (GDG) | — | CBTRN02C (rejected transactions) | DALYREJS.jcl |
| TRANREPT | (GDG) | — | CBTRN03C (report output) | REPTFILE.jcl |
| EXPFILE | (Sequential) | CBIMPORT | CBEXPORT | CBEXPORT.jcl |

### 2.2 Sequential / Output Datasets

| Dataset | Created By | Consumed By | Purpose |
|---------|-----------|-------------|---------|
| OUTFILE | CBACT01C | External | Flat file account extract |
| ARRYFILE | CBACT01C | External | Array-format account extract |
| VBRCFILE | CBACT01C | External | Variable-block account extract |
| STMTFILE | CBSTM03A | TXT2PDF1.JCL | Text statements |
| HTMLFILE | CBSTM03A | External | HTML statements |
| DATEPARM | External | CBTRN03C | Report date parameters |
| ERROUT | CBIMPORT | Operations | Import error records |

---

## 3. End-to-End Batch Pipeline Flow

### 3.1 Daily Transaction Processing Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DAILY BATCH CYCLE                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. CLOSEFIL.jcl          Close CICS files for batch                │
│         │                                                            │
│         ▼                                                            │
│  2. POSTTRAN.jcl          Post daily transactions                   │
│     └─ CBTRN02C           ┌─ Read: DALYTRAN (daily feed)            │
│                           ├─ Read: XREFFILE (card→account lookup)   │
│                           ├─ Read: ACCTFILE (account validation)    │
│                           ├─ Read: TCATBALF (category balances)     │
│                           ├─ Write: TRANFILE (posted transactions)  │
│                           ├─ Write: TCATBALF (updated balances)     │
│                           ├─ Rewrite: ACCTFILE (updated balances)   │
│                           └─ Write: DALYREJS (rejected records)     │
│         │                                                            │
│         ▼                                                            │
│  3. INTCALC.jcl           Calculate interest charges                │
│     └─ CBACT04C           ┌─ Read: TCATBALF (category balances)     │
│                           ├─ Read: XREFFILE, ACCTFILE               │
│                           ├─ Read: DISCGRP (interest rates)         │
│                           ├─ Rewrite: ACCTFILE (interest charges)   │
│                           └─ Write: TRANSACT (interest transactions)│
│         │                                                            │
│         ▼                                                            │
│  4. TRANREPT.jcl          Generate daily transaction report         │
│     ├─ SORT (pre-sort)                                              │
│     └─ CBTRN03C           ┌─ Read: TRANFILE, CARDXREF              │
│                           ├─ Read: TRANTYPE, TRANCATG              │
│                           └─ Write: TRANREPT (formatted report)    │
│         │                                                            │
│         ▼                                                            │
│  5. OPENFIL.jcl           Reopen CICS files for online             │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 Statement Generation Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│                 STATEMENT GENERATION                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. CREASTMT.JCL                                                    │
│     ├─ DELDEF01: IDCAMS    Define output datasets                   │
│     ├─ STEP010: SORT       Sort transactions by card + ID           │
│     └─ STEP020: IDCAMS     Copy sorted data                        │
│         │                                                            │
│         ▼                                                            │
│  2. CBSTM03A (statement master)                                     │
│     └─ CALL CBSTM03B      ┌─ Read: TRNXFILE (sorted transactions)  │
│                           ├─ Read: XREFFILE (card→customer)        │
│                           ├─ Read: CUSTFILE (customer details)     │
│                           ├─ Read: ACCTFILE (account details)      │
│                           ├─ Write: STMTFILE (text statements)     │
│                           └─ Write: HTMLFILE (HTML statements)     │
│         │                                                            │
│         ▼                                                            │
│  3. TXT2PDF1.JCL          Convert to PDF                           │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.3 Branch Migration Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│                 BRANCH MIGRATION (EXPORT)                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  CBEXPORT.jcl                                                       │
│  ├─ STEP01: IDCAMS         Verify source files exist                │
│  └─ STEP02: CBEXPORT       ┌─ Read: CUSTFILE                       │
│                            ├─ Read: ACCTFILE                        │
│                            ├─ Read: XREFFILE                        │
│                            ├─ Read: TRANSACT                        │
│                            ├─ Read: CARDFILE                        │
│                            └─ Write: EXPFILE (multi-record export)  │
│                                                                      │
├─────────────────────────────────────────────────────────────────────┤
│                 BRANCH MIGRATION (IMPORT)                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  CBIMPORT.jcl                                                       │
│  └─ STEP01: CBIMPORT       ┌─ Read: EXPFILE                        │
│                            ├─ Write: CUSTOUT (customer records)     │
│                            ├─ Write: ACCTOUT (account records)      │
│                            ├─ Write: XREFOUT (xref records)        │
│                            ├─ Write: TRNXOUT (transaction records)  │
│                            ├─ Write: CARDOUT (card records)         │
│                            └─ Write: ERROUT (error/reject records)  │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.4 File Maintenance / Initialization Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│              VSAM FILE INITIALIZATION (Run Once)                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Execution Order:                                                    │
│  1. DEFGDGB.jcl    → Define GDG bases                              │
│  2. ACCTFILE.jcl   → Define + Load Account KSDS                    │
│  3. CARDFILE.jcl   → Define + Load Card KSDS + AIX                 │
│  4. CUSTFILE.jcl   → Define + Load Customer KSDS                   │
│  5. XREFFILE.jcl   → Define + Load Cross-Ref KSDS + AIX           │
│  6. TRANFILE.jcl   → Define + Load Transaction KSDS + AIX         │
│  7. TCATBALF.jcl   → Define + Load Category Balance KSDS          │
│  8. DISCGRP.jcl    → Define + Load Disclosure Group KSDS          │
│  9. TRANTYPE.jcl   → Define + Load Transaction Type KSDS          │
│  10. TRANCATG.jcl  → Define + Load Transaction Category KSDS      │
│  11. DUSRSECJ.jcl  → Define + Load User Security KSDS             │
│  12. DALYREJS.jcl  → Define GDG for daily rejects                 │
│  13. REPTFILE.jcl  → Define GDG for reports                       │
│  14. CBADMCDJ.jcl  → CICS CSD resource definitions                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.5 Transaction Backup Pipeline

```
TRANBKP.jcl
├─ STEP05R: REPROC (copy transaction master to backup GDG)
├─ STEP05:  IDCAMS REPRO (backup VSAM to sequential)
└─ STEP10:  IDCAMS DELETE (clear transaction master for new cycle)
```

> **Timing:** TRANBKP.jcl runs AFTER statement generation (CREASTMT.JCL) and AFTER
> the reporting pipeline (TRANREPT.jcl), typically at cycle-end (monthly or billing
> period close). It MUST NOT run before statements are produced, as it deletes the
> transaction master. The sequence is:
> 1. Daily cycles run for the billing period
> 2. Statement generation (CREASTMT.JCL)
> 3. Transaction backup (TRANBKP.jcl) — archives and clears for next period

### 3.6 Transaction Consolidation Pipeline

COMBTRAN.jcl combines intra-day transaction segments into the master transaction file.
Typical execution: after POSTTRAN.jcl and before TRANREPT.jcl (when daily transactions
arrive in multiple batches).

```
COMBTRAN.jcl
├─ STEP05R: SORT (sort daily segments by transaction ID)
└─ STEP10:  IDCAMS REPRO (merge sorted output into TRANSACT master)
```

> **Open Question:** Confirm whether COMBTRAN runs between POSTTRAN and INTCALC,
> or only when multi-batch daily feeds are received.

---

## 4. Copybook Dependency Matrix

Shows which programs depend on which copybooks (core `app/cpy/` only):

| Copybook | Used By Programs |
|----------|-----------------|
| CVACT01Y (Account) | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COBIL00C, COPAUS0C, COTRN02C, COACCT01 |
| CVACT02Y (Card) | CBACT02C, CBEXPORT, CBIMPORT, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C |
| CVACT03Y (Xref) | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COPAUS0C, COTRN02C |
| CVCUS01Y (Customer) | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COPAUS0C |
| CVTRA05Y (Transaction) | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, COTRN00C, COTRN01C, COTRN02C, CORPT00C |
| CVTRA06Y (Daily Trans) | CBTRN01C, CBTRN02C |
| CVTRA01Y (Cat Balance) | CBACT04C, CBTRN02C |
| CVTRA02Y (Disc Group) | CBACT04C |
| CVTRA03Y (Tran Type) | CBTRN03C |
| CVTRA04Y (Tran Category) | CBTRN03C |
| COCOM01Y (COMMAREA) | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COPAUS0C, COPAUS1C |
| CSUSR01Y (Security) | COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| COTTL01Y (Titles) | All online programs |
| CSDAT01Y (Date/Time) | All online programs |
| CSMSG01Y (Messages) | All online programs |
| DFHAID (AID keys) | All online programs |
| DFHBMSCA (BMS attrs) | All online programs |
| CSLKPCDY (Lookups) | COACTUPC |
| CVEXPORT (Export) | CBEXPORT, CBIMPORT |
| CSMSG02Y (Messages-2) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C |
| CSSTRPFY (String Format) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CVCRD01Y (Card Navigation) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C |
| CSUTLDWY (Date Utility WS) | COACTUPC |
| CSSETATY (Screen Attributes) | COACTUPC |
| CUSTREC (Statement Customer) | CBSTM03A |
| CSUTLDPY (Date Utility — unused) | (No program references found — likely dead code) |
| UNUSED1Y (Explicitly unused) | (No program references — dead artifact, candidate for deletion) |

---

### 5.1 Known Processing Asymmetries

#### Online Transaction Add (COTRN02C) vs Batch Posting (CBTRN02C)

The online program `COTRN02C` writes directly to `TRANSACT` via CICS WRITE but does
NOT update:
- `TCATBALF` (transaction category balances)
- `ACCT-CURR-BAL` in ACCTFILE

In contrast, batch `CBTRN02C` updates all three (TRANSACT, TCATBALF, ACCTFILE).

**Impact:** Online-added transactions are not reflected in interest calculations
(`CBACT04C` reads `TCATBALF`) until a subsequent batch cycle processes them. This
means interest for online-added transactions may be delayed by one cycle.

**Migration consideration:** The modernized Transaction Service must decide whether
to update balances synchronously (like batch) or maintain the async pattern.

### 5.2 Authorization Fraud Detection Flow (app-authorization-ims-db2-mq)

The fraud detection and flagging data flow:

1. `COPAUA0C` (Authorization Decision) checks fraud flags during authorization.
   - If card or merchant is flagged, declines with reason `5100` (card fraud) or `5200` (merchant fraud)
   - Source of fraud flags: DB2 table `AUTHFRDS`

2. `COPAUS2C` (Mark Fraud) — online CICS program that allows operators to flag
   an authorization as fraudulent. Writes/updates `AUTHFRDS` DB2 table.

3. Fraud data lineage:
   - Detection: External fraud system or operator marks via COPAUS2C → AUTHFRDS (DB2)
   - Enforcement: COPAUA0C reads AUTHFRDS during auth decision → decline if flagged

> **Open Question:** Confirm whether COPAUA0C reads AUTHFRDS directly during every
> authorization decision, or whether fraud flags are cached/pre-loaded. The current
> source code shows fraud reason codes but the DB2 SELECT for fraud checking needs
> to be verified in the full COPAUA0C source.

### 5.3 Authorization-to-Settlement Handoff

The `DALYTRAN` file (input to CBTRN02C) is marked as "(External feed)" in the
dataset mapping. The mechanism by which approved authorizations (stored in IMS DB
by COPAUA0C) become settled transactions in DALYTRAN is NOT documented in this
codebase.

Possible scenarios:
1. An external clearing house/payment network produces DALYTRAN
2. A separate extraction job reads IMS auth records and creates DALYTRAN
3. DALYTRAN comes from a completely external acquiring system

> **Action Required:** The migration team must determine the DALYTRAN source to
> design the equivalent event/messaging bridge in the modernized system.

---

## 5. Online Transaction Flow (CICS)

```
Terminal User
     │
     ▼
TransID: CC00
     │
     ▼
┌─────────┐     Auth OK      ┌──────────┐
│ COSGN00C├────────────────→ │ COMEN01C │ (Regular User)
│ (Signon)│                   │  (Menu)  │
│         ├────────────────→ │ COADM01C │ (Admin User)
└─────────┘     Admin Auth    └────┬─────┘
                                   │
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                    ▼
        ┌──────────┐       ┌──────────┐        ┌──────────┐
        │ Account  │       │  Card    │        │  Trans   │
        │ Functions│       │ Functions│        │ Functions│
        ├──────────┤       ├──────────┤        ├──────────┤
        │COACTVWC  │       │COCRDLIC  │        │COTRN00C  │
        │COACTUPC  │       │COCRDSLC  │        │COTRN01C  │
        │COBIL00C  │       │COCRDUPC  │        │COTRN02C  │
        └──────────┘       └──────────┘        │CORPT00C  │
                                                └──────────┘
```
