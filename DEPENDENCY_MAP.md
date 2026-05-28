# Dependency Map — CardDemo COBOL Estate

## 1. Inter-Program Call Graph

### 1.1 Direct CALL Statements

```
CBACT01C ──CALL──► COBDATFT   (assembler date formatter)
CBACT01C ──CALL──► CEE3ABD    (LE abend handler)

CBACT02C ──CALL──► CEE3ABD

CBACT03C ──CALL──► CEE3ABD

CBACT04C ──CALL──► CEE3ABD

CBCUS01C ──CALL──► CEE3ABD

CBEXPORT ──CALL──► CEE3ABD

CBIMPORT ──CALL──► CEE3ABD

CBSTM03A ──CALL──► CBSTM03B   (11 calls — file I/O subroutine)
CBSTM03A ──CALL──► CEE3ABD

CBTRN01C ──CALL──► CEE3ABD

CBTRN02C ──CALL──► CEE3ABD

CBTRN03C ──CALL──► CEE3ABD

COBSWAIT ──CALL──► MVSWAIT    (assembler wait routine)

CORPT00C ──CALL──► CSUTLDTC   (date validation utility, 2 calls)

COTRN02C ──CALL──► CSUTLDTC   (date validation utility, 2 calls)
```

### 1.2 CICS XCTL Transfers (Online Program Navigation)

```
COSGN00C ──XCTL──► COADM01C   (admin users → admin menu)
COSGN00C ──XCTL──► COMEN01C   (regular users → main menu)

COMEN01C ──XCTL──► (dynamic)  via CDEMO-MENU-OPT-PGMNAME:
           ├──► COACTVWC   (option 1: Account View)
           ├──► COACTUPC   (option 2: Account Update)
           ├──► COCRDLIC   (option 3: Credit Card List)
           ├──► COCRDSLC   (option 4: Credit Card Detail)
           ├──► COCRDUPC   (option 5: Credit Card Update)
           ├──► COTRN00C   (option 6: Transaction List)
           ├──► COTRN01C   (option 7: Transaction View)
           ├──► COTRN02C   (option 8: Transaction Add)
           ├──► COBIL00C   (option 9: Bill Payment)
           ├──► CORPT00C   (option 10: Print Reports)
           └──► COPAUS0C   (option 11: Auth Summary)

COADM01C ──XCTL──► (dynamic)  via CDEMO-ADMIN-OPT-PGMNAME:
           ├──► COUSR00C   (option 1: User List)
           ├──► COUSR01C   (option 2: User Add)
           ├──► COUSR02C   (option 3: User Update)
           ├──► COUSR03C   (option 4: User Delete)
           ├──► COTRTLIC   (option 5: Transaction Type List)
           └──► COTRTUPC   (option 6: Transaction Type Maintenance)

COACTVWC ──XCTL──► CDEMO-TO-PROGRAM  (return to menu)

COACTUPC ──XCTL──► CDEMO-TO-PROGRAM  (return to menu)

COCRDLIC ──XCTL──► LIT-MENUPGM       (return to menu)
COCRDLIC ──XCTL──► CCARD-NEXT-PROG   (drill into card detail/update)
           ├──► COCRDSLC   (card detail)
           └──► COCRDUPC   (card update)

COCRDSLC ──XCTL──► CDEMO-TO-PROGRAM  (return to caller)

COCRDUPC ──XCTL──► CDEMO-TO-PROGRAM  (return to caller)

COTRTLIC ──XCTL──► (dynamic)         (return or navigate)
COTRTUPC ──XCTL──► (dynamic)         (return or navigate)
```

### 1.3 MQ Interactions (COPAUA0C)

```
COPAUA0C ──MQOPEN──► Request Queue
COPAUA0C ──MQGET───► Request Queue (receives authorization request)
COPAUA0C ──MQPUT1──► Reply Queue   (sends authorization decision)
COPAUA0C ──MQCLOSE─► Request Queue
```

### 1.4 IMS DL/I Calls

```
PAUDBLOD ──CBLTDLI──► ISRT  (insert root + child segments)
PAUDBLOD ──CBLTDLI──► GU    (get unique for parent lookup)

PAUDBUNL ──CBLTDLI──► GN    (get next root segment)
PAUDBUNL ──CBLTDLI──► GNP   (get next child within parent)

DBUNLDGS ──CBLTDLI──► GN    (get next root → GSAM write)
DBUNLDGS ──CBLTDLI──► GNP   (get next child → GSAM write)
DBUNLDGS ──CBLTDLI──► ISRT  (GSAM output insert)

CBPAUP0C ──DL/I───► GN, GNP, DLET  (navigate + delete expired auths)
```

### 1.5 VSAM-MQ Programs

```
COACCT01 ──MQOPEN──►  Request Queue, Reply Queue, Dead Letter Queue
COACCT01 ──MQGET───►  Request Queue (account inquiry request)
COACCT01 ──MQPUT───►  Reply Queue (account data response)
COACCT01 ──MQPUT───►  Dead Letter Queue (on error)
COACCT01 ──MQCLOSE─►  All queues

CODATE01 ──MQOPEN──►  Request Queue, Reply Queue, Dead Letter Queue
CODATE01 ──MQGET───►  Request Queue (date service request)
CODATE01 ──MQPUT───►  Reply Queue (date response)
CODATE01 ──MQCLOSE─►  All queues
```

---

## 2. Online Application Flow Diagram

```
                            ┌─────────────┐
                            │  COSGN00C   │
                            │  (Sign-on)  │
                            └──────┬──────┘
                                   │
                    ┌──────────────┴──────────────┐
                    ▼                              ▼
             ┌─────────────┐               ┌─────────────┐
             │  COMEN01C   │               │  COADM01C   │
             │ (Main Menu) │               │(Admin Menu) │
             └──────┬──────┘               └──────┬──────┘
                    │                              │
    ┌───────────────┼───────────────┐     ┌────────┼────────┐
    ▼               ▼               ▼     ▼        ▼        ▼
┌────────┐   ┌────────┐   ┌────────┐ ┌────────┐┌────────┐┌────────┐
│COACTVWC│   │COCRDLIC│   │COTRN00C│ │COUSR00C││COTRTLIC││COTRTUPC│
│AcctView│   │CardList│   │TranList│ │UserList││TranType││TranType│
└────────┘   └───┬────┘   └───┬────┘ └───┬────┘│  List  ││ Update │
                 │             │          │     └────────┘└────────┘
    ┌────────┐   ├─────┐   ┌──┴────┐  ┌──┴────┐
    │COACTUPC│   │     │   │       │  │       │
    │AcctUpd │ ┌─┴──┐┌─┴──┐│COTRN01│ │COUSR01│ │COUSR02│ │COUSR03│
    └────────┘ │COCRD││COCRD││TranVw │ │UsrAdd │ │UsrUpd │ │UsrDel │
               │SLC  ││UPC  │└──────┘ └───────┘ └───────┘ └───────┘
               │Dtl  ││Upd  │
               └─────┘└─────┘   ┌────────┐  ┌────────┐  ┌────────┐
                                │COTRN02C│  │COBIL00C│  │CORPT00C│
                                │TranAdd │  │BillPay │  │Reports │
                                └────────┘  └────────┘  └────────┘
```

---

## 3. Dataset Lineage — JCL Jobs ↔ Programs ↔ VSAM Files

### 3.1 Core VSAM Datasets

| Dataset (Logical) | Physical DSN Pattern | VSAM Type | Record Layout | Programs That Read | Programs That Write | JCL That Defines |
|-------------------|---------------------|-----------|---------------|-------------------|--------------------|--------------------|
| ACCTDAT / ACCTFILE | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | CVACT01Y (300 bytes) | CBACT01C, CBACT04C, CBEXPORT, CBSTM03A/B, CBTRN01C, CBTRN02C, COACTVWC, COACTUPC, COBIL00C, COTRN02C | CBTRN02C (REWRITE), COACTUPC (REWRITE) | ACCTFILE.jcl |
| CARDDAT / CARDFILE | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | CVACT02Y (150 bytes) | CBACT02C, CBEXPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC (REWRITE) | CARDFILE.jcl |
| CUSTDAT / CUSTFILE | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | CVCUS01Y (500 bytes) | CBCUS01C, CBEXPORT, CBSTM03A/B, CBTRN01C, COACTVWC, COACTUPC | — | CUSTFILE.jcl |
| CARDXREF / XREFFILE | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS + AIX | CVACT03Y (50 bytes) | CBACT03C, CBACT04C, CBEXPORT, CBSTM03A/B, CBTRN01C, CBTRN02C, CBTRN03C, COACTVWC, COACTUPC, COBIL00C, COCRDSLC, COTRN02C | — | XREFFILE.jcl |
| TRANSACT / TRANFILE | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | CVTRA05Y (350 bytes) | CBEXPORT, COTRN00C, COTRN01C, CBTRN03C | CBTRN01C (WRITE), CBTRN02C (WRITE), COTRN02C (WRITE), COBIL00C (WRITE) | TRANFILE.jcl |
| TCATBALF | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | KSDS | CVTRA01Y (50 bytes) | CBACT04C | CBTRN02C (WRITE/REWRITE) | TCATBALF.jcl |
| TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | KSDS | CVTRA03Y (60 bytes) | CBTRN03C | — | TRANTYPE.jcl |
| TRANCATG | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | KSDS | CVTRA04Y (60 bytes) | CBTRN03C | — | TRANCATG.jcl |
| DISCGRP | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | KSDS | CVTRA02Y (50 bytes) | CBACT04C | — | DISCGRP.jcl |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | CSUSR01Y (80 bytes) | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) | DUSRSECJ.jcl |
| DALYREJS | AWS.M2.CARDDEMO.DALYREJS | KSDS/GDG | CVTRA05Y variant | — | CBTRN02C (WRITE) | DALYREJS.jcl |

### 3.2 Sequential/Flat Datasets

| Dataset | Programs That Read | Programs That Write | JCL Jobs |
|---------|-------------------|--------------------|--------------------|
| AWS.M2.CARDDEMO.DALYTRAN.PS | CBTRN01C, CBTRN02C | *(external feed)* | POSTTRAN.jcl |
| AWS.M2.CARDDEMO.ACCTDATA.PS | — | — | ACCTFILE.jcl (REPRO source) |
| AWS.M2.CARDDEMO.CARDDATA.PS | — | — | CARDFILE.jcl (REPRO source) |
| AWS.M2.CARDDEMO.CUSTDATA.PS | — | — | CUSTFILE.jcl (REPRO source) |
| AWS.M2.CARDDEMO.CARDXREF.PS | — | — | XREFFILE.jcl (REPRO source) |
| AWS.M2.CARDDEMO.TRANTYPE.PS | — | — | TRANTYPE.jcl (REPRO source) |
| AWS.M2.CARDDEMO.TRANCATG.PS | — | — | TRANCATG.jcl (REPRO source) |
| AWS.M2.CARDDEMO.DISCGRP.PS | — | — | DISCGRP.jcl (REPRO source) |
| AWS.M2.CARDDEMO.TCATBALF.PS | — | — | TCATBALF.jcl (REPRO source) |
| AWS.M2.CARDDEMO.ACCTDATA.PSCOMP | — | CBACT01C (OUTFILE) | READACCT.jcl |
| AWS.M2.CARDDEMO.ACCTDATA.ARRYPS | — | CBACT01C (ARRYFILE) | READACCT.jcl |
| AWS.M2.CARDDEMO.ACCTDATA.VBPS | — | CBACT01C (VBRCFILE) | READACCT.jcl |
| AWS.M2.CARDDEMO.EXPORT.DATA | CBIMPORT | CBEXPORT | CBEXPORT.jcl, CBIMPORT.jcl |
| AWS.M2.CARDDEMO.STATEMNT.PS | — | CBSTM03A (text output) | TXT2PDF1.JCL |

### 3.3 GDG (Generation Data Group) Datasets

| GDG Base | Used By | Purpose |
|----------|---------|---------|
| AWS.M2.CARDDEMO.TRANSACT.BKUP | TRANBKP.jcl, TRANREPT.jcl, COMBTRAN.jcl | Transaction backup generations |
| AWS.M2.CARDDEMO.DALYREJS | POSTTRAN.jcl | Daily rejection generations |
| AWS.M2.CARDDEMO.TRANSACT.DALY | TRANREPT.jcl | Daily transaction extract for reporting |
| AWS.M2.CARDDEMO.TRANSACT.COMBINED | COMBTRAN.jcl | Combined transaction sets |
| AWS.M2.CARDDEMO.TRANTYPE.BKUP | DEFGDGD.jcl, TRANEXTR.jcl | Transaction type backup |
| AWS.M2.CARDDEMO.TRANCATG.PS.BKUP | TRANEXTR.jcl | Transaction category backup |
| AWS.M2.CARDDEMO.TCATBALF.BKUP | PRTCATBL.jcl | Category balance backup |

---

## 4. End-to-End Batch Pipeline Flow

### 4.1 Initial Data Setup Pipeline

```
 ┌──────────────────────────────────────────────────────────────────┐
 │                    ONE-TIME SETUP PIPELINE                      │
 ├──────────────────────────────────────────────────────────────────┤
 │                                                                 │
 │  DEFGDGB.jcl ─► Define GDG bases for backups                   │
 │       │                                                         │
 │       ▼                                                         │
 │  ACCTFILE.jcl ─► PS → VSAM KSDS (accounts)                     │
 │  CARDFILE.jcl ─► PS → VSAM KSDS + AIX (cards)                  │
 │  CUSTFILE.jcl ─► PS → VSAM KSDS (customers)                    │
 │  XREFFILE.jcl ─► PS → VSAM KSDS + AIX (cross-refs)            │
 │  TRANFILE.jcl ─► PS → VSAM KSDS (transactions)                 │
 │  TCATBALF.jcl ─► PS → VSAM KSDS (category balances)            │
 │  TRANTYPE.jcl ─► PS → VSAM KSDS (transaction types)            │
 │  TRANCATG.jcl ─► PS → VSAM KSDS (transaction categories)       │
 │  DISCGRP.jcl  ─► PS → VSAM KSDS (discount groups)              │
 │  DUSRSECJ.jcl ─► Create + load user security file               │
 │  DALYREJS.jcl ─► Define daily rejects file                      │
 │  REPTFILE.jcl ─► Define report output file                      │
 │       │                                                         │
 │       ▼                                                         │
 │  CBADMCDJ.jcl ─► Define CICS CSD resources                     │
 │  OPENFIL.jcl  ─► Open all CICS files                            │
 │                                                                 │
 └──────────────────────────────────────────────────────────────────┘
```

### 4.2 Daily Batch Processing Pipeline

```
 ┌──────────────────────────────────────────────────────────────────┐
 │                     DAILY BATCH PIPELINE                        │
 ├──────────────────────────────────────────────────────────────────┤
 │                                                                 │
 │  STEP 1: Close CICS files for batch processing                  │
 │  ─────────────────────────────────────────────                  │
 │  CLOSEFIL.jcl                                                   │
 │       │                                                         │
 │       ▼                                                         │
 │  STEP 2: Backup transaction data                                │
 │  ───────────────────────────────                                │
 │  TRANBKP.jcl ─► REPROC: TRANSACT VSAM → GDG backup             │
 │                  IDCAMS: Delete + redefine TRANSACT VSAM        │
 │       │                                                         │
 │       ▼                                                         │
 │  STEP 3: Post daily transactions                                │
 │  ───────────────────────────────                                │
 │  POSTTRAN.jcl ─► PGM=CBTRN02C                                  │
 │    Input:  DALYTRAN.PS (daily feed)                             │
 │    Output: TRANSACT.VSAM (posted transactions)                  │
 │            DALYREJS.GDG (rejected transactions)                 │
 │            ACCTDATA.VSAM (account balance updates)              │
 │            TCATBALF.VSAM (category balance updates)             │
 │       │                                                         │
 │       ▼                                                         │
 │  STEP 4: Calculate interest                                     │
 │  ──────────────────────────                                     │
 │  INTCALC.jcl ─► PGM=CBACT04C                                   │
 │    Input:  TCATBALF.VSAM, XREFFILE.VSAM, ACCTDATA.VSAM,        │
 │            DISCGRP.VSAM                                         │
 │    Output: Updated ACCTDATA.VSAM (interest applied)             │
 │       │                                                         │
 │       ▼                                                         │
 │  STEP 5: Generate reports                                       │
 │  ────────────────────────                                       │
 │  TRANREPT.jcl ─► REPROC + SORT + PGM=CBTRN03C                  │
 │    Input:  TRANSACT.VSAM, CARDXREF.VSAM, TRANTYPE.VSAM,        │
 │            TRANCATG.VSAM                                        │
 │    Output: TRANREPT (formatted report)                          │
 │                                                                 │
 │  PRTCATBL.jcl ─► REPROC + SORT                                 │
 │    Input:  TCATBALF.VSAM                                        │
 │    Output: TCATBALF.REPT (category balance report)              │
 │       │                                                         │
 │       ▼                                                         │
 │  STEP 6: Reopen CICS files                                     │
 │  ──────────────────────────                                     │
 │  OPENFIL.jcl                                                    │
 │                                                                 │
 └──────────────────────────────────────────────────────────────────┘
```

### 4.3 Statement Generation Pipeline

```
 ┌──────────────────────────────────────────────────────────────────┐
 │                  STATEMENT GENERATION PIPELINE                  │
 ├──────────────────────────────────────────────────────────────────┤
 │                                                                 │
 │  CREASTMT.JCL                                                   │
 │  ─────────────                                                  │
 │  DELDEF01 ─► Cleanup previous statement workfiles                │
 │  STEP010  ─► SORT: TRANSACT.VSAM → sorted sequential            │
 │  STEP020  ─► IDCAMS REPRO: Load sorted data to TRXFL.VSAM       │
 │       │                                                         │
 │       ▼                                                         │
 │  (Batch program CBSTM03A called separately or via JCL)          │
 │  CBSTM03A ─► CALLs CBSTM03B for file I/O                       │
 │    Input:  TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE               │
 │    Output: Statement text file, HTML file                       │
 │       │                                                         │
 │       ▼                                                         │
 │  TXT2PDF1.JCL ─► Convert text statement to PDF                  │
 │    Input:  STATEMNT.PS                                           │
 │    Output: PDF statement                                         │
 │                                                                 │
 └──────────────────────────────────────────────────────────────────┘
```

### 4.4 Branch Migration Pipeline

```
 ┌──────────────────────────────────────────────────────────────────┐
 │                   BRANCH MIGRATION PIPELINE                     │
 ├──────────────────────────────────────────────────────────────────┤
 │                                                                 │
 │  CBEXPORT.jcl ─► PGM=CBEXPORT                                  │
 │    Input:  CUSTDATA.VSAM, ACCTDATA.VSAM, CARDXREF.VSAM,        │
 │            TRANSACT.VSAM, CARDDATA.VSAM                         │
 │    Output: EXPORT.DATA (multi-record format per CVEXPORT.cpy)   │
 │                                                                 │
 │       ────── file transfer to target branch ──────               │
 │                                                                 │
 │  CBIMPORT.jcl ─► PGM=CBIMPORT                                  │
 │    Input:  EXPORT.DATA                                           │
 │    Output: CUSTDATA.IMPORT, ACCTDATA.IMPORT,                    │
 │            CARDXREF.IMPORT, TRANSACT.IMPORT,                    │
 │            CARDDATA.IMPORT, IMPORT.ERRORS                       │
 │                                                                 │
 └──────────────────────────────────────────────────────────────────┘
```

### 4.5 Authorization IMS Pipeline

```
 ┌──────────────────────────────────────────────────────────────────┐
 │              AUTHORIZATION IMS/DB2/MQ PIPELINE                  │
 ├──────────────────────────────────────────────────────────────────┤
 │                                                                 │
 │  Online (real-time):                                            │
 │  ──────────────────                                             │
 │  External System ──MQ──► COPAUA0C (authorization decision)      │
 │                            │ Reads: CARDXREF, ACCTDATA, CUSTDAT │
 │                            │ Reads/Writes: IMS auth DB          │
 │                            └──MQ──► External System (reply)     │
 │                                                                 │
 │  Online (CICS screens):                                         │
 │  ──────────────────────                                         │
 │  COPAUS0C ─► Auth summary browse (IMS + BMS)                    │
 │  COPAUS1C ─► Auth detail view   (IMS + BMS)                     │
 │  COPAUS2C ─► Mark as fraud      (IMS + DB2)                     │
 │                                                                 │
 │  Batch:                                                         │
 │  ──────                                                         │
 │  CBPAUP0J.jcl ─► PGM=CBPAUP0C: purge expired auths             │
 │  UNLDPADB.JCL ─► PGM=PAUDBUNL: unload IMS → flat files         │
 │  UNLDGSAM.JCL ─► PGM=DBUNLDGS: unload IMS → GSAM files        │
 │  LOADPADB.JCL ─► PGM=PAUDBLOD: load flat files → IMS           │
 │                                                                 │
 └──────────────────────────────────────────────────────────────────┘
```

### 4.6 Transaction Type DB2 Pipeline

```
 ┌──────────────────────────────────────────────────────────────────┐
 │               TRANSACTION TYPE DB2 PIPELINE                     │
 ├──────────────────────────────────────────────────────────────────┤
 │                                                                 │
 │  Setup:                                                         │
 │  ──────                                                         │
 │  CREADB21.jcl ─► Create DB2 tables (TR_TYPE, TR_CAT)            │
 │                                                                 │
 │  Batch maintenance:                                             │
 │  ─────────────────                                              │
 │  MNTTRDB2.jcl ─► PGM=COBTUPDT: batch updates from input file   │
 │  TRANEXTR.jcl ─► Backup TRANTYPE + TRANCATG                    │
 │                                                                 │
 │  Online (CICS):                                                 │
 │  ──────────────                                                 │
 │  COTRTLIC ─► Browse/select transaction types (DB2 cursors)       │
 │  COTRTUPC ─► Add/update/delete transaction types (DB2 DML)      │
 │                                                                 │
 └──────────────────────────────────────────────────────────────────┘
```

---

## 5. Copybook Dependency Matrix

| Copybook | Used By Programs |
|----------|-----------------|
| CVACT01Y | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COBIL00C, COTRN02C, COPAUA0C, COPAUS0C, COACCT01 |
| CVACT02Y | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C, COTRTLIC |
| CVACT03Y | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COTRN02C, COPAUA0C, COPAUS0C |
| CVCUS01Y | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COPAUA0C, COPAUS0C |
| CVTRA05Y | CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, COTRN00C, COTRN01C, COTRN02C, CORPT00C |
| CVTRA06Y | CBTRN01C, CBTRN02C |
| CVTRA01Y | CBACT04C, CBTRN02C |
| CVTRA02Y | CBACT04C |
| CVTRA03Y | CBTRN03C |
| CVTRA04Y | CBTRN03C |
| CVTRA07Y | CBTRN03C |
| CVEXPORT | CBEXPORT, CBIMPORT |
| COCOM01Y | All 17+ CICS online programs |
| CSUSR01Y | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| COTTL01Y | All CICS online programs |
| CSDAT01Y | All CICS online programs |
| CSMSG01Y | All CICS online programs |
| DFHAID | All CICS online programs |
| DFHBMSCA | All CICS online programs |
| CVCRD01Y | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC |
| CIPAUSMY | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| CIPAUDTY | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| CCPAURQY | COPAUA0C |
| CCPAURLY | COPAUA0C |
| CCPAUERY | COPAUA0C |
| COSTM01 | CBSTM03A |
| CUSTREC | CBSTM03A |
| CODATECN | CBACT01C |
| CSUTLDWY | COACTUPC, COTRTUPC |
| CSLKPCDY | COACTUPC |
| COMEN02Y | COMEN01C |
| COADM02Y | COADM01C |
| IMSFUNCS | DBUNLDGS, PAUDBLOD, PAUDBUNL |
