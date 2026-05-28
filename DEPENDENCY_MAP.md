# DEPENDENCY MAP — CardDemo Call Graph & Dataset Lineage

## 1. Inter-Program Call Graph

### 1.1 Batch Program Calls

```
CBACT01C ──CALL──► COBDATFT  (date formatting assembler)
         ──CALL──► CEE3ABD   (LE abend handler)

CBACT02C ──CALL──► CEE3ABD

CBACT04C ──(no external CALL)──  [self-contained interest calculation]

CBEXPORT ──CALL──► CEE3ABD

CBIMPORT ──CALL──► CEE3ABD

CBTRN01C ──(no external CALL)──

CBTRN02C ──(no external CALL)──

CBTRN03C ──(no external CALL)──

COBSWAIT ──CALL──► MVSWAIT   (assembler wait service)

CSUTLDTC ──CALL──► CEEDAYS   (LE date validation API)
```

### 1.2 CICS Program Transfer Graph (XCTL/LINK)

```
                      ┌─────────────────────────────────────────┐
                      │          COSGN00C (Sign-on)             │
                      │  Authenticates via USRSEC file read     │
                      └────────┬────────────────┬───────────────┘
                               │ XCTL           │ XCTL
                    (Admin)    ▼                 ▼  (Regular)
              ┌─────────────────────┐   ┌──────────────────────┐
              │  COADM01C (Admin    │   │  COMEN01C (Main      │
              │  Menu)              │   │  Menu)               │
              └────────┬────────────┘   └──────────┬───────────┘
                       │ XCTL to:                  │ XCTL to:
           ┌───────────┼───────────┐     ┌─────────┼──────────────────┐
           ▼           ▼           ▼     ▼         ▼                  ▼
      COUSR00C    COUSR01C    COUSR02C  COACTVWC  COACTUPC      COCRDLIC
      (List       (Add        (Update   (View     (Update        (List
      Users)      User)       User)     Account)  Account)       Cards)
                                  │                                │
                                  ▼                          ┌─────┼──────┐
                             COUSR03C                        ▼     ▼      ▼
                             (Delete                    COCRDSLC COCRDUPC COTRN00C
                              User)                     (View    (Update  (List
                                                         Card)   Card)    Trans)
                                                                    │
                                                              ┌─────┼──────┐
                                                              ▼     ▼      ▼
                                                         COTRN01C COTRN02C CORPT00C
                                                         (View    (Add     (Report
                                                          Trans)  Trans)   Submit)
                                                                     │
                                                                     ▼
                                                               COBIL00C
                                                               (Bill Pay)
```

### 1.3 Utility Program Dependencies

```
COACTUPC ──COPY/PERFORM──► CSUTLDPY (date validation procedures)
         ──COPY/PERFORM──► CSSETATY (field attribute setting)
         ──COPY/PERFORM──► CSSTRPFY (PF key storage)
                           └──(CSUTLDPY internally calls CSUTLDTC)

COACTVWC ──COPY/PERFORM──► CSSTRPFY

COCRDLIC ──COPY/PERFORM──► CSSTRPFY

COCRDSLC ──COPY/PERFORM──► CSSTRPFY

COCRDUPC ──COPY/PERFORM──► CSSTRPFY

COTRN02C ──CALL──► CSUTLDTC (date validation)

CORPT00C ──CALL──► CSUTLDTC (date validation)
```

---

## 2. Dataset Lineage

### 2.1 VSAM Master Files

| Dataset (DSN) | VSAM Type | Key | Record Length | JCL Definition | Programs (Read) | Programs (Write/Update) |
|---------------|-----------|-----|---------------|----------------|-----------------|------------------------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | ACCT-ID (11,0) | 300 | ACCTFILE.jcl | CBACT01C, CBACT04C, CBEXPORT, CBTRN01C, CBTRN02C · CICS: COACTVWC, COACTUPC, COBIL00C, COTRN02C | CBACT04C (rewrite), CBTRN02C (rewrite) · CICS: COACTUPC (rewrite), COBIL00C (rewrite) |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | CARD-NUM (16,0) | 150 | CARDFILE.jcl | CBACT02C, CBEXPORT, CBTRN01C · CICS: COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COACTUPC | CICS: COCRDUPC (rewrite) |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | CUST-ID (9,0) | 500 | CUSTFILE.jcl | CBCUS01C, CBEXPORT, CBTRN01C · CICS: COACTVWC, COACTUPC | CICS: COACTUPC (rewrite) |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS | XREF-CARD-NUM (16,0) | 50 | XREFFILE.jcl | CBACT03C, CBACT04C, CBEXPORT, CBTRN01C, CBTRN02C, CBTRN03C · CICS: COACTVWC, COACTUPC, COBIL00C, COTRN02C | — |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | TRAN-ID (16,0) | 350 | TRANFILE.jcl | CBEXPORT, CBTRN03C · CICS: COTRN00C, COTRN01C, COBIL00C | CBTRN02C (write), CBACT04C (write) · CICS: COTRN02C (write), COBIL00C (write) |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | KSDS | TRAN-CAT-KEY (17,0) | 50 | TCATBALF.jcl | CBACT04C | CBTRN02C (write/rewrite), CBACT04C (rewrite) |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | KSDS | TRAN-TYPE (2,0) | 60 | TRANTYPE.jcl | CBTRN03C | — |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | KSDS | TRAN-CAT-KEY (6,0) | 60 | TRANCATG.jcl | CBTRN03C | — |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | KSDS | DIS-GROUP-KEY (16,0) | 50 | DISCGRP.jcl | CBACT04C | — |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | SEC-USR-ID (8,0) | 80 | DUSRSECJ.jcl | CICS: COSGN00C, COUSR00C, COUSR02C, COUSR03C | CICS: COUSR01C (write), COUSR02C (rewrite), COUSR03C (delete) |

### 2.2 Alternate Index Paths

| AIX Path | Base Cluster | AIX Key | Used By |
|----------|-------------|---------|---------|
| CARDXREF.VSAM.AIX.PATH (CXACAIX) | CARDXREF.VSAM.KSDS | XREF-ACCT-ID (9,16) | CICS: COACTVWC, COACTUPC, COBIL00C, COTRN02C |
| CARDDATA.VSAM.AIX.PATH | CARDDATA.VSAM.KSDS | CARD-ACCT-ID | CICS: COCRDLIC (LIT-CARD-FILE-ACCT-PATH) |
| TRANSACT.VSAM.AIX.PATH | TRANSACT.VSAM.KSDS | TRAN-CARD-NUM+TRAN-ID | TRANIDX.jcl, CREASTMT.JCL |

### 2.3 Sequential / GDG Datasets

| Dataset (DSN) | Type | Producer (Write) | Consumer (Read) |
|---------------|------|-------------------|------------------|
| CARDDEMO.DALYTRAN.PS | Sequential | External feed / TRANFILE.jcl (init) | CBTRN01C, CBTRN02C (daily transactions) |
| CARDDEMO.DALYREJS(+n) | GDG | CBTRN02C (rejected transactions) | Manual review |
| CARDDEMO.SYSTRAN(+n) | GDG | CBACT04C (interest transactions) | COMBTRAN.jcl |
| CARDDEMO.TRANSACT.BKUP(+n) | GDG | TRANBKP.jcl, TRANREPT.jcl (backup) | COMBTRAN.jcl |
| CARDDEMO.TRANSACT.COMBINED(+n) | GDG | COMBTRAN.jcl | COMBTRAN.jcl (→ REPRO to VSAM) |
| CARDDEMO.TRANSACT.DALY(+n) | GDG | TRANREPT.jcl (sorted subset) | CBTRN03C (reporting) |
| CARDDEMO.TRANREPT(+n) | GDG | CBTRN03C (report output) | TXT2PDF1.JCL, manual review |
| CARDDEMO.ACCTDATA.PSCOMP | Sequential | CBACT01C (full dump) | Manual review |
| CARDDEMO.ACCTDATA.ARRYPS | Sequential | CBACT01C (array format) | Manual review |
| CARDDEMO.ACCTDATA.VBPS | Sequential | CBACT01C (variable-length) | Manual review |
| CARDDEMO.TCATBALF.REPT | Sequential | PRTCATBL.jcl (formatted report) | Manual review |
| CARDDEMO.EXPORT.DATA | Sequential | CBEXPORT | CBIMPORT |
| CARDDEMO.DATEPARM | Sequential | Manual/external | CBTRN03C (date range parms) |
| CARDDEMO.STATEMNT.PS | Sequential | CREASTMT.JCL / CBSTM03A | TXT2PDF1.JCL |
| CARDDEMO.STATEMNT.HTML | Sequential | CREASTMT.JCL / CBSTM03A | External viewing |

### 2.4 Flat File Sources (Initial Load)

| Dataset (DSN) | Purpose | Loaded Into |
|---------------|---------|-------------|
| CARDDEMO.ACCTDATA.PS | Account seed data | ACCTDATA.VSAM.KSDS |
| CARDDEMO.CARDDATA.PS | Card seed data | CARDDATA.VSAM.KSDS |
| CARDDEMO.CUSTDATA.PS | Customer seed data | CUSTDATA.VSAM.KSDS |
| CARDDEMO.CARDXREF.PS | Cross-reference seed data | CARDXREF.VSAM.KSDS |
| CARDDEMO.TCATBALF.PS | Category balance seed data | TCATBALF.VSAM.KSDS |
| CARDDEMO.TRANTYPE.PS | Transaction type seed data | TRANTYPE.VSAM.KSDS |
| CARDDEMO.TRANCATG.PS | Transaction category seed data | TRANCATG.VSAM.KSDS |
| CARDDEMO.DISCGRP.PS | Disclosure group seed data | DISCGRP.VSAM.KSDS |
| CARDDEMO.USRSEC.PS | User security seed data | USRSEC.VSAM.KSDS |

---

## 3. End-to-End Batch Pipeline Flow

### 3.1 One-Time Setup Pipeline

```
[1] Define GDG Bases          [2] Define VSAM Clusters          [3] Load Seed Data
    DEFGDGB.jcl                   ACCTFILE.jcl (STEP05-10)          ACCTFILE.jcl (STEP15)
    DEFGDGD.jcl                   CARDFILE.jcl                      CARDFILE.jcl
    DALYREJS.jcl                  CUSTFILE.jcl                      CUSTFILE.jcl
    REPTFILE.jcl                  XREFFILE.jcl                      XREFFILE.jcl
                                  TRANFILE.jcl                      TRANFILE.jcl
                                  TCATBALF.jcl                      TCATBALF.jcl
                                  TRANTYPE.jcl                      TRANTYPE.jcl
                                  TRANCATG.jcl                      TRANCATG.jcl
                                  DISCGRP.jcl                       DISCGRP.jcl
                                  DUSRSECJ.jcl                      DUSRSECJ.jcl

[4] Define AIX/Paths           [5] Register with CICS
    CARDFILE.jcl (STEP40-60)       CBADMCDJ.jcl (DFHCSDUP)
    XREFFILE.jcl (STEP20-30)       OPENFIL.jcl
    TRANFILE.jcl (STEP20-30)
    TRANIDX.jcl
```

### 3.2 Daily Batch Cycle

```
Phase 1: Pre-Batch                Phase 2: Transaction Processing
─────────────────────             ──────────────────────────────
CLOSEFIL.jcl                      POSTTRAN.jcl
  └─ Close CICS files                └─ PGM=CBTRN02C
     for exclusive batch access          ├─ Read DALYTRAN.PS (daily feed)
                                         ├─ Validate via CARDXREF
                                         ├─ Update ACCTDATA balances
                                         ├─ Write to TRANSACT master
                                         ├─ Update TCATBALF category balances
                                         └─ Reject invalid → DALYREJS(+1)

Phase 3: Interest Calculation     Phase 4: Reporting
──────────────────────────        ─────────────────
INTCALC.jcl                       TRANREPT.jcl
  └─ PGM=CBACT04C                    ├─ Backup TRANSACT → BKUP(+1)
       ├─ Read TCATBALF               ├─ SORT by date → DALY(+1)
       ├─ Lookup DISCGRP rates        └─ PGM=CBTRN03C
       ├─ Compute interest                 ├─ Read sorted transactions
       ├─ Update ACCTDATA                  ├─ Enrich with TRANTYPE/TRANCATG
       └─ Write interest trans             └─ Write TRANREPT(+1)
          → SYSTRAN(+1)
                                  PRTCATBL.jcl
                                    └─ Backup + format TCATBALF

Phase 5: Maintenance              Phase 6: Post-Batch
────────────────────              ──────────────────
TRANBKP.jcl                       COMBTRAN.jcl
  ├─ Backup TRANSACT → BKUP(+1)     ├─ SORT merge BKUP + SYSTRAN
  └─ DELETE/DEFINE (reset master)    └─ REPRO combined → TRANSACT VSAM

                                  OPENFIL.jcl
                                    └─ Re-open CICS files
```

### 3.3 Periodic / Ad-Hoc Jobs

```
Export/Import (Migration):
  CBEXPORT.jcl → reads all VSAM files → EXPORT.DATA
  CBIMPORT.jcl → reads EXPORT.DATA → writes import files

Statement Generation:
  CREASTMT.JCL → SORT TRANSACT → CBSTM03A → STATEMNT.PS + STATEMNT.HTML
  TXT2PDF1.JCL → converts STATEMNT.PS to PDF

Data Inspection:
  READACCT.jcl → CBACT01C → dumps account data
  READCARD.jcl → CBACT02C → prints card data
  READCUST.jcl → CBCUS01C → prints customer data
  READXREF.jcl → CBACT03C → prints cross-reference data
```

### 3.4 Dataset Flow Diagram

```
External Feed                                   VSAM Masters
────────────                                    ────────────
DALYTRAN.PS ──► CBTRN02C ──► TRANSACT.VSAM.KSDS ◄──► CICS Online
                   │              │                    Programs
                   ├──► DALYREJS(+n)  ▲                  │
                   │              │    │                  │
                   ▼              ▼    │                  ▼
             ACCTDATA ◄─────── CBACT04C ◄── DISCGRP    USRSEC
             (updated)    │                              │
                         ▼                               │
                    SYSTRAN(+n)                          ▼
                         │                         CARDDATA
                         ▼                         CUSTDATA
                    COMBTRAN.jcl                   CARDXREF
                         │
                         ▼
                    TRANSACT.VSAM (reloaded)
                         │
                         ▼
                    CBTRN03C ──► TRANREPT(+n)
                                     │
                                     ▼
                               TXT2PDF1.JCL ──► PDF
```
