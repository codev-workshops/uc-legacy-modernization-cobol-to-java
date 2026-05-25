# Application Inventory — CardDemo Legacy COBOL System

> Complete inventory of all source artifacts in the CardDemo credit-card management application.

---

## Section A: Core Programs (`app/cbl/`)

### Batch Programs

| # | File | PROGRAM-ID | Lines | Purpose | Key I/O (DD Names) | Copybooks | External CALLs |
|---|------|-----------|-------|---------|---------------------|-----------|----------------|
| 1 | CBACT01C.cbl | CBACT01C | 430 | Read the account file and write into files | ACCTFILE, OUTFILE, ARRYFILE, VBRCFILE | CVACT01Y, CODATECN | COBDATFT, CEE3ABD |
| 2 | CBACT02C.cbl | CBACT02C | 178 | Read and print card data file | CARDFILE | CVACT02Y | CEE3ABD |
| 3 | CBACT03C.cbl | CBACT03C | 178 | Read and print account cross reference file | XREFFILE | CVACT03Y | CEE3ABD |
| 4 | CBACT04C.cbl | CBACT04C | 652 | Calculate interest on outstanding balances | ACCTFILE, XREFFILE, TRANFILE, TCATBALF, DALYTRAN, DISCGRP | CVACT01Y, CVACT03Y, CVTRA05Y, CVTRA01Y, CVTRA02Y, CVTRA04Y | CEE3ABD |
| 5 | CBCUS01C.cbl | CBCUS01C | 178 | Read and print customer data file | CUSTFILE | CVCUS01Y | CEE3ABD |
| 6 | CBEXPORT.cbl | CBEXPORT | 582 | Export account, customer, card, and xref data to flat file | ACCTFILE, CUSTFILE, CARDFILE, XREFFILE, EXPFILE | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVEXPORT | CEE3ABD |
| 7 | CBIMPORT.cbl | CBIMPORT | 487 | Import multi-entity records from flat file | ACCTFILE, CUSTFILE, CARDFILE, XREFFILE, IMPFILE | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVEXPORT | CEE3ABD |
| 8 | CBSTM03A.CBL | CBSTM03A | 924 | Print account statements from transaction data (plain text and HTML) | STMTFILE, HTMLFILE | COSTM01, CVACT03Y, CUSTREC, CVACT01Y | CBSTM03B, CEE3ABD |
| 9 | CBSTM03B.CBL | CBSTM03B | 230 | Subroutine — file processing for transaction report | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | _(none)_ | _(none)_ |
| 10 | CBTRN01C.cbl | CBTRN01C | 494 | Post records from daily transaction file (validate and post) | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y | CEE3ABD |
| 11 | CBTRN02C.cbl | CBTRN02C | 731 | Post records from daily transaction file (full processing with reject handling) | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE, DALYREJS, TCATBALF | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT02Y, CVACT01Y, CVCUS01Y, CVTRA01Y | CEE3ABD |
| 12 | CBTRN03C.cbl | CBTRN03C | 649 | Generate transaction detail report | TRANFILE, XREFFILE, TRANTYPE, TRANCATG, DATEPARM, TRANREPT | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y | CEE3ABD |
| 13 | COBSWAIT.cbl | COBSWAIT | 41 | Mainframe WAIT — calls ASM timer routine | _(none)_ | _(none)_ | MVSWAIT |

### Online/CICS Programs

| # | File | PROGRAM-ID | Lines | Purpose | CICS DATASET(s) | Copybooks | XCTL/LINK Targets |
|---|------|-----------|-------|---------|-----------------|-----------|-------------------|
| 14 | COACTUPC.cbl | COACTUPC | 4236 | Account update (view/edit account, card, customer) | CXACAIX (CARDXREF AIX), ACCTDAT, CUSTDAT | CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY | XCTL → caller/COMEN01C |
| 15 | COACTVWC.cbl | COACTVWC | 941 | Account view (read-only account/card/customer) | CXACAIX, ACCTDAT, CUSTDAT | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY | XCTL → caller/COMEN01C |
| 16 | COADM01C.cbl | COADM01C | 288 | Admin menu — dispatches to user management and DB2 programs | _(none)_ | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL → COUSR00C/COUSR01C/COUSR02C/COUSR03C/COTRTLIC/COTRTUPC |
| 17 | COBIL00C.cbl | COBIL00C | 572 | Bill payment processing (view balance, pay from transaction) | ACCTDAT, CXACAIX, TRANSACT | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA | XCTL → COMEN01C |
| 18 | COCRDLIC.cbl | COCRDLIC | 1459 | List credit cards (all for admin, account-scoped for user) | CARDDAT | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY | XCTL → COCRDSLC, COCRDUPC, COMEN01C |
| 19 | COCRDSLC.cbl | COCRDSLC | 887 | Card detail view (read-only display of selected card) | CARDDAT, ACCTDAT, CUSTDAT | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDSL, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y | XCTL → COCRDLIC |
| 20 | COCRDUPC.cbl | COCRDUPC | 1560 | Card update (edit card data, account, customer refs) | CARDDAT, ACCTDAT, CUSTDAT | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CSSETATY | XCTL → COCRDLIC |
| 21 | COMEN01C.cbl | COMEN01C | 308 | Main menu — dispatches to functional modules | _(none)_ | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL → COACTVWC/COACTUPC/COCRDLIC/COTRN00C/COTRN02C/COBIL00C/CORPT00C |
| 22 | CORPT00C.cbl | CORPT00C | 649 | Report generation (submit batch report jobs via TDQ JOBS) | TRANSACT | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | CALL CSUTLDTC; XCTL → COMEN01C |
| 23 | COSGN00C.cbl | COSGN00C | 260 | Sign-on screen (authentication against USRSEC) | USRSEC | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL → COADM01C (admin), COMEN01C (user) |
| 24 | COTRN00C.cbl | COTRN00C | 699 | Transaction list — browse all transactions | TRANSACT | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | XCTL → COTRN01C, COMEN01C |
| 25 | COTRN01C.cbl | COTRN01C | 330 | Transaction detail view (single transaction display) | TRANSACT | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | XCTL → COTRN00C |
| 26 | COTRN02C.cbl | COTRN02C | 783 | Transaction add/update (post new transaction online) | CXACAIX, CCXREF, TRANSACT | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA | CALL CSUTLDTC; XCTL → COMEN01C |
| 27 | COUSR00C.cbl | COUSR00C | 695 | List all users from USRSEC file | USRSEC | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL → COUSR02C, COUSR03C |
| 28 | COUSR01C.cbl | COUSR01C | 299 | Add new Regular/Admin user to USRSEC file | USRSEC | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL → COUSR00C |
| 29 | COUSR02C.cbl | COUSR02C | 414 | Update a user in USRSEC file | USRSEC | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL → COUSR00C |
| 30 | COUSR03C.cbl | COUSR03C | 359 | Delete a user from USRSEC file | USRSEC | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL → COUSR00C |

### Utility Program

| # | File | PROGRAM-ID | Lines | Purpose | Copybooks | External CALLs |
|---|------|-----------|-------|---------|-----------|----------------|
| 31 | CSUTLDTC.cbl | CSUTLDTC | 157 | Date conversion utility — Gregorian ↔ Julian | CSUTLDPY, CSUTLDWY | CEEDAYS |

---

## Section B: Authorization Extension Programs (`app/app-authorization-ims-db2-mq/cbl/`)

| # | File | PROGRAM-ID | Lines | Type | Purpose | Technologies | Copybooks |
|---|------|-----------|-------|------|---------|-------------|-----------|
| 32 | COPAUA0C.cbl | COPAUA0C | 1026 | CICS/IMS/MQ | Card Authorization Decision Program (MQ trigger CP00) | CICS + IMS DL/I + MQ | CMQODV, CMQMDV, CMQV, CMQTML, CMQPMOV, CMQGMOV, CCPAURQY, CCPAURLY, CCPAUERY, CIPAUSMY, CIPAUDTY, CVACT03Y, CVACT01Y, CVCUS01Y |
| 33 | COPAUS0C.cbl | COPAUS0C | 1032 | CICS/IMS/BMS | Summary View of Authorization Messages (CPVS) | CICS + IMS DL/I + BMS | COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CIPAUSMY, DFHAID, DFHBMSCA, CVACT03Y, CVACT01Y, CVCUS01Y |
| 34 | COPAUS1C.cbl | COPAUS1C | 604 | CICS/IMS/BMS | Detail View of Authorization Message (CPVD) | CICS + IMS DL/I + BMS | COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CIPAUDTY, DFHAID, DFHBMSCA |
| 35 | COPAUS2C.cbl | COPAUS2C | 244 | CICS/IMS/DB2 | Mark Authorization Message as Fraud (called program) | CICS + IMS DL/I + DB2 | CIPAUDTY, SQLCA |
| 36 | CBPAUP0C.cbl | CBPAUP0C | 386 | Batch/IMS | Delete Expired Pending Authorization Messages | IMS DL/I (GN, GNP, DLET, CHKP) | CIPAUSMY, CIPAUDTY |
| 37 | DBUNLDGS.CBL | DBUNLDGS | 366 | Batch/IMS | DL/I GSAM unload utility | IMS DL/I + GSAM | _(IMS PCBs)_ |
| 38 | PAUDBLOD.CBL | PAUDBLOD | 369 | Batch/IMS | IMS DB load from flat file | IMS DL/I (ISRT) | PADFLPCB, PAUTBPCB, CIPAUSMY, CIPAUDTY |
| 39 | PAUDBUNL.CBL | PAUDBUNL | 317 | Batch/IMS | IMS DB unload to flat file | IMS DL/I (GN, GNP) + GSAM | PASFLPCB, PAUTBPCB, CIPAUSMY, CIPAUDTY |

---

## Section C: Transaction Type DB2 Extension Programs (`app/app-transaction-type-db2/cbl/`)

| # | File | PROGRAM-ID | Lines | Type | Purpose | Technologies | Copybooks |
|---|------|-----------|-------|------|---------|-------------|-----------|
| 40 | COTRTLIC.cbl | COTRTLIC | 2098 | CICS/DB2 | List Transaction Types for updates/deletes with DB2 cursors (CTLI) | CICS + DB2 (DECLARE CURSOR, OPEN, FETCH, CLOSE, DELETE, UPDATE) | COCOM01Y, COTRTLI, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA, CSDB2RPY, DCLTRTYP, DCLTRCAT |
| 41 | COTRTUPC.cbl | COTRTUPC | 1702 | CICS/DB2 | Accept and process Transaction Type Update (CTTU) | CICS + DB2 (SELECT, UPDATE, DELETE, INSERT) | COCOM01Y, COTRTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA, CSDB2RWY, DCLTRTYP, DCLTRCAT |
| 42 | COBTUPDT.cbl | COBTUPDT | 237 | Batch/DB2 | Batch update Transaction type based on user input | DB2 (INSERT, UPDATE, DELETE) | DCLTRTYP, SQLCA |

---

## Section D: VSAM-MQ Extension Programs (`app/app-vsam-mq/cbl/`)

| # | File | PROGRAM-ID | Lines | Type | Purpose | Technologies | Key Resources |
|---|------|-----------|-------|------|---------|-------------|---------------|
| 43 | COACCT01.cbl | COACCT01 | 620 | CICS/MQ | Inquire account details via MQ (CDRA) | CICS + MQ (MQGET/MQPUT/MQOPEN/MQCLOSE) | Request Queue → VSAM ACCTDAT → Response Queue |
| 44 | CODATE01.cbl | CODATE01 | 524 | CICS/MQ | Inquire System Date via MQ (CDRD) | CICS + MQ (MQGET/MQPUT/MQOPEN/MQCLOSE) | Request Queue → System Date → Response Queue |

---

## Section E: ASM Programs (`app/asm/`)

| File | Purpose | Called By |
|------|---------|-----------|
| COBDATFT.asm | Date format converter (YYYYMMDD ↔ YYYY-MM-DD) using register manipulation | CBACT01C |
| MVSWAIT.asm | MVS WAIT service — interval control timer using ASMWAIT macro | COBSWAIT |

**Supporting Macros** (`app/maclib/`):
- `ASMWAIT.mac` — WAIT macro expansion for interval timer
- `COCDATFT.mac` — Date format DSECT/record mapping

---

## Section F: JCL Jobs

### Core JCL (`app/jcl/`) — 38 Jobs

| # | Job Name | Purpose | EXEC Steps |
|---|----------|---------|------------|
| 1 | ACCTFILE | Delete/define/load Account VSAM KSDS | IDCAMS ×3 (delete, define, repro) |
| 2 | CARDFILE | Delete/define/load Card VSAM KSDS + AIX + PATH | SDSF (close), IDCAMS ×6, SDSF (open) |
| 3 | CBADMCDJ | Admin batch utility (custom module load) | PGM=custom |
| 4 | CBEXPORT | Export all entities to flat file | PGM=CBEXPORT |
| 5 | CBIMPORT | Import all entities from flat file | PGM=CBIMPORT |
| 6 | CLOSEFIL | Close CICS files for batch processing | PGM=SDSF |
| 7 | COMBTRAN | Combine daily transaction rejects with main file | IDCAMS REPRO |
| 8 | CREASTMT | Create account statements (text + HTML) | PGM=CBSTM03A |
| 9 | CUSTFILE | Delete/define/load Customer VSAM KSDS | IDCAMS ×3 |
| 10 | DALYREJS | Delete/define Daily Rejects sequential file | IDCAMS |
| 11 | DEFCUST | Define Customer VSAM cluster | IDCAMS |
| 12 | DEFGDGB | Define GDG base for backups | IDCAMS |
| 13 | DEFGDGD | Define GDG base for daily data | IDCAMS |
| 14 | DISCGRP | Load disclosure group data to VSAM | PROC=REPROC |
| 15 | DUSRSECJ | Delete/define/load User Security VSAM | IDCAMS ×3 |
| 16 | ESDSRRDS | Define ESDS/RRDS example clusters | IDCAMS |
| 17 | FTPJCL | FTP transfer JCL (template) | PGM=FTP |
| 18 | INTCALC | Calculate interest on balances | PGM=CBACT04C |
| 19 | INTRDRJ1 | Internal reader job submission example 1 | PGM=IEBGENER |
| 20 | INTRDRJ2 | Internal reader job submission example 2 | PGM=IEBGENER |
| 21 | OPENFIL | Open CICS files after batch | PGM=SDSF |
| 22 | POSTTRAN | Post daily transactions | PGM=CBTRN02C |
| 23 | PRTCATBL | Print transaction category balance report | PGM=CBTRN03C |
| 24 | READACCT | Read/print account data | PGM=CBACT01C |
| 25 | READCARD | Read/print card data | PGM=CBACT02C |
| 26 | READCUST | Read/print customer data | PGM=CBCUS01C |
| 27 | READXREF | Read/print cross-reference data | PGM=CBACT03C |
| 28 | REPTFILE | Delete/define report output file | IDCAMS |
| 29 | TCATBALF | Load transaction category balance to VSAM | PROC=REPROC |
| 30 | TRANBKP | Backup transaction file via REPRO | PROC=REPROC |
| 31 | TRANCATG | Load transaction category to VSAM | PROC=REPROC |
| 32 | TRANFILE | Delete/define/load Transaction VSAM KSDS | IDCAMS ×3 |
| 33 | TRANIDX | Define transaction alternate index | IDCAMS |
| 34 | TRANREPT | Transaction report pipeline (REPRO → SORT → CBTRN03C) | PROC=TRANREPT |
| 35 | TRANTYPE | Load transaction type to VSAM | PROC=REPROC |
| 36 | TXT2PDF1 | Convert text report to PDF | PGM=TXT2PDF |
| 37 | WAITSTEP | Pause between batch steps | PGM=COBSWAIT |
| 38 | XREFFILE | Delete/define/load Card Cross-Reference VSAM | IDCAMS ×3 |

### Authorization Extension JCL (`app/app-authorization-ims-db2-mq/jcl/`) — 5 Jobs

| # | Job Name | Purpose | EXEC Steps |
|---|----------|---------|------------|
| 39 | CBPAUP0J | Execute IMS purge of expired authorizations | PGM=DFSRRC00 (BMP, CBPAUP0C, PSBPAUTB) |
| 40 | DBPAUTP0 | Unload IMS DB DBPAUTP0 to flat file | IEFBR14 (delete), DFSRRC00 (ULU, DFSURGU0) |
| 41 | LOADPADB | Load IMS PAUT database from flat file | PGM=DFSRRC00 (PAUDBLOD) |
| 42 | UNLDGSAM | Unload IMS via GSAM utility | PGM=DFSRRC00 (DBUNLDGS) |
| 43 | UNLDPADB | Unload IMS PAUT database to flat file | PGM=DFSRRC00 (PAUDBUNL) |

### Transaction Type DB2 Extension JCL (`app/app-transaction-type-db2/jcl/`) — 3 Jobs

| # | Job Name | Purpose | EXEC Steps |
|---|----------|---------|------------|
| 44 | CREADB21 | Create DB2 tables and indexes | PGM=IKJEFT01 (SYSTSIN: DDL) |
| 45 | MNTTRDB2 | Batch maintain transaction types in DB2 | PGM=COBTUPDT |
| 46 | TRANEXTR | Extract DB2 transaction types to VSAM | PGM=IKJEFT01 (SPUFI unload) |

---

## Section G: JCL Procedures (`app/proc/`)

| Procedure | Steps | Purpose |
|-----------|-------|---------|
| REPROC.prc | 1 step: PGM=IDCAMS | IDCAMS REPRO utility for VSAM load/unload; controlled by `REPROCT.ctl` |
| TRANREPT.prc | 3 steps: REPROC (VSAM→flat), SORT (filter by date), CBTRN03C (report) | End-to-end transaction report generation pipeline |

---

## Section H: Scheduler Definitions

### CA-7 (`app/scheduler/CardDemo.ca7`)

**Batch Chain (triggered sequence):**
```
CLOSEFIL → CBPAUP0J → POSTTRAN → WAITSTEP → OPENFIL
                                           ↓
                         CLOSEFIL → TRANTYPE → WAITSTEP → CLOSEFIL1 / CLOSEFIL2
                                                            ↓              ↓
                                                        TRANCATG       TCATBALF
                                                            ↓              ↓
                                                        WAITSTEP       WAITSTEP
                                                            ↓              ↓
                                                        CLOSEFIL → READACCT → READCARD → READCUST → READXREF → WAITSTEP → OPENFIL
                                                                                                                                ↓
                                                                              CLOSEFIL → CREASTMT → TXT2PDF1 → PRTCATBL
```

### Control-M (`app/scheduler/CardDemo.controlm`)

| Folder | Frequency | Chain |
|--------|-----------|-------|
| DAILY-TransactionBackup | Daily | CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL |
| WEEKLY-TransactionTypesDBRefresh | Weekly | MNTTRDB2 → TRANEXTR |
| WEEKLY-DisclosureGroupsRefresh | Weekly | CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL |
| MONTHLY-InterestCalculation | Monthly | CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL |

---

## Section I: CICS Resource Definitions

### From `app/csd/CARDDEMO.CSD` (Core)

**VSAM Files (8):**

| File Name | Dataset | Description |
|-----------|---------|-------------|
| ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | Account master |
| CARDAIX | AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH | Card alternate index path |
| CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | Card data |
| CCXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | Card-to-account cross reference |
| CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | Customer data |
| CXACAIX | AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH | Cross-ref alternate index (by account) |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | Transaction data |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | User security/authentication |

**Mapsets (17):** COACTUP, COACTVW, COADM01, COBIL00, COCRDLI, COCRDSL, COCRDUP, COMEN01, CORPT00, COSGN00, COTRN00, COTRN01, COTRN02, COUSR00, COUSR01, COUSR02, COUSR03

**Programs (20):** COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSEC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C + COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C (auth extension)

**Transactions (17 core):** CAUP, CAVW, CA00, CB00, CCDL, CCLI, CCUP, CC00, CDV1, CM00, CR00, CT00, CT01, CT02, CU00, CU01, CU02, CU03

**TDQ:** JOBS — Transient Data Queue for internal reader (batch job submission from CICS)

**Libraries:** CARDDLIB (AWS.M2.CARDDEMO.LOADLIB), COM2DOLL

### From `app/app-authorization-ims-db2-mq/csd/CRDDEMO2.csd`
- Mapsets: COPAU00, COPAU01
- Programs: COPAUA0C (CP00), COPAUS0C (CPVS), COPAUS1C (CPVD), COPAUS2C (CPVD)
- Transactions: CP00, CPVS, CPVD
- DB2Entry: AWS01PLN; DB2Tran: CPVDTRAN

### From `app/app-transaction-type-db2/csd/CRDDEMOD.csd`
- Mapsets: COTRTLI, COTRTUP
- Programs: COTRTLIC (CTLI), COTRTUPC (CTTU)
- Transactions: CTLI, CTTU
- DB2Entry: CARDDEMO; DB2Trans: CTLITRAN, CTTUTRAN

### From `app/app-vsam-mq/csd/CRDDEMOM.csd`
- Programs: COACCT01 (CDRA), CODATE01 (CDRD)
- Transactions: CDRA, CDRD
- Library: CARDDLIB
