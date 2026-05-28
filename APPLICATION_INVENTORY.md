# Application Inventory — CardDemo COBOL Estate

## 1. COBOL Programs — Main Application (`app/cbl/`)

### 1.1 Batch Programs

| # | Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|---------|----------------|---------------------|---------------------|
| 1 | CBACT01C.cbl | Read account VSAM file, write to flat file, array file, and variable-length file | Batch | **Read:** ACCTFILE (VSAM KSDS) · **Write:** OUTFILE (seq), ARRYFILE (seq), VBRCFILE (VB seq) | CVACT01Y, CODATECN |
| 2 | CBACT02C.cbl | Read and print card data file | Batch | **Read:** CARDFILE (VSAM KSDS) | CVACT02Y |
| 3 | CBACT03C.cbl | Read and print account cross-reference data file | Batch | **Read:** XREFFILE (VSAM KSDS) | CVACT03Y |
| 4 | CBACT04C.cbl | Interest calculator — computes interest on transaction category balances using discount group rates | Batch | **Read:** TCATBALF (VSAM KSDS), XREFFILE (VSAM KSDS + AIX), ACCTFILE (VSAM KSDS), DISCGRP (VSAM KSDS), TRANSACT (seq) | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| 5 | CBCUS01C.cbl | Read and print customer data file | Batch | **Read:** CUSTFILE (VSAM KSDS) | CVCUS01Y |
| 6 | CBEXPORT.cbl | Export customer data for branch migration — reads all normalized CardDemo files and creates multi-record export file | Batch | **Read:** CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE (all VSAM KSDS) · **Write:** EXPFILE (VSAM KSDS, 500-byte records) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 7 | CBIMPORT.cbl | Import customer data from branch migration export — splits multi-record export file into normalized target files with validation | Batch | **Read:** EXPFILE (VSAM KSDS) · **Write:** CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT (all seq) · ERROUT (seq) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 8 | CBSTM03A.CBL | Print account statements from transaction data in plain text and HTML formats | Batch | **Write:** STMTFILE (seq), HTMLFILE (seq) · **Read** (via CBSTM03B): TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| 9 | CBSTM03B.CBL | Subroutine for CBSTM03A — file I/O handler for transaction report data | Batch (Sub) | **Read:** TRNXFILE (VSAM KSDS), XREFFILE (VSAM KSDS), CUSTFILE (VSAM KSDS), ACCTFILE (VSAM KSDS) | *(none — inline FDs)* |
| 10 | CBTRN01C.cbl | Post records from daily transaction file — validates and writes to transaction master | Batch | **Read:** DALYTRAN (seq), CUSTFILE (VSAM), XREFFILE (VSAM), CARDFILE (VSAM), ACCTFILE (VSAM) · **Write:** TRANFILE (VSAM KSDS) | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |

> **Note:** CBTRN01C appears to be superseded by CBTRN02C. Only CBTRN02C is referenced in POSTTRAN.jcl and the daily batch pipeline. CBTRN01C may be dead code — confirm before investing modernization effort.

| 11 | CBTRN02C.cbl | Post records from daily transaction file — enhanced version with reject file, account updates, and category balance updates | Batch | **Read:** DALYTRAN (seq), XREFFILE (VSAM), ACCTFILE (VSAM) · **Write:** TRANFILE (VSAM), DALYREJS (seq), TCATBALF (VSAM) | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| 12 | CBTRN03C.cbl | Print transaction detail report with type/category lookups and date filtering | Batch | **Read:** TRANFILE (seq), CARDXREF (VSAM KSDS), TRANTYPE (VSAM KSDS), TRANCATG (VSAM KSDS), DATEPARM (seq) · **Write:** TRANREPT (seq) | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| 13 | COBSWAIT.cbl | Utility program to wait (parameter in centiseconds) | Batch (Util) | *(none)* | *(none)* |
| 14 | CSUTLDTC.cbl | Date validation utility — converts and validates dates using CEEDAYS | Batch (Util) | *(none)* | *(none)* |

### 1.3 Assembler Programs

| # | Filename | Purpose | Classification | Location | Macro |
|---|----------|---------|----------------|----------|-------|
| A1 | COBDATFT.asm | Date format conversion utility (called by CBACT01C) | Assembler Utility | `app/asm/` | COCDATFT.mac (`app/maclib/`) |
| A2 | MVSWAIT.asm | Timer/wait routine (called by COBSWAIT) | Assembler Utility | `app/asm/` | ASMWAIT.mac (`app/maclib/`) |

### 1.2 Online (CICS) Programs

| # | Filename | Purpose | Classification | Key I/O (CICS file ops) | Copybooks Referenced |
|---|----------|---------|----------------|-------------------------|---------------------|
| 15 | COSGN00C.cbl | Sign-on screen — authenticates users against USRSEC file | Online (CICS) | **Read:** USRSEC (CICS READ) | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 16 | COMEN01C.cbl | Main menu for regular users — routes to sub-functions | Online (CICS) | *(map send/receive only)* | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 17 | COADM01C.cbl | Admin menu for admin users — routes to security management and DB2 functions | Online (CICS) | *(map send/receive only)* | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 18 | COACTVWC.cbl | Account view — displays account details with card and customer lookup | Online (CICS) | **Read:** CARDXREF (CICS READ via CARDAIX), ACCTDAT (CICS READ), CUSTDAT (CICS READ) | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| 19 | COACTUPC.cbl | Account update — accept and process account field updates with extensive input validation | Online (CICS) | **Read/Rewrite:** ACCTDAT, CARDXREF, CUSTDAT (CICS READ/REWRITE) | CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY ×38, CSSTRPFY, CSUTLDPY |
| 20 | COCRDLIC.cbl | List credit cards — browse card file with forward/backward paging | Online (CICS) | **Browse:** CARDDAT (CICS STARTBR/READNEXT/READPREV/ENDBR) | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY |
| 21 | COCRDSLC.cbl | Credit card detail view — display card details with customer lookup | Online (CICS) | **Read:** CARDDAT (CICS READ), CARDAIX (CICS READ) | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDSL, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 22 | COCRDUPC.cbl | Credit card update — accept and process card detail changes | Online (CICS) | **Read/Rewrite:** CARDDAT (CICS READ/REWRITE) | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 23 | COBIL00C.cbl | Bill payment — pay account balance in full or partial amount | Online (CICS) | **Read/Write:** ACCTDAT, CARDXREF, TRANSACT (CICS READ/WRITE) | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 24 | COTRN00C.cbl | List transactions from TRANSACT file with paging | Online (CICS) | **Browse:** TRANSACT (CICS STARTBR/READNEXT/READPREV/ENDBR) | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 25 | COTRN01C.cbl | View a single transaction from TRANSACT file | Online (CICS) | **Read:** TRANSACT (CICS READ) | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 26 | COTRN02C.cbl | Add a new transaction to TRANSACT file | Online (CICS) | **Read:** ACCTDAT, CARDXREF (CICS READ) · **Write:** TRANSACT (CICS WRITE) | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| 27 | CORPT00C.cbl | Print transaction reports by submitting batch JCL via internal reader | Online (CICS) | **Write:** CSSL (CICS WRITEQ TD — transient data for JCL submission) | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 28 | COUSR00C.cbl | List all users from USRSEC file | Online (CICS) | **Browse:** USRSEC (CICS STARTBR/READNEXT/READPREV/ENDBR) | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 29 | COUSR01C.cbl | Add a new regular/admin user to USRSEC file | Online (CICS) | **Write:** USRSEC (CICS WRITE) | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 30 | COUSR02C.cbl | Update a user in USRSEC file | Online (CICS) | **Read/Rewrite:** USRSEC (CICS READ/REWRITE) | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 31 | COUSR03C.cbl | Delete a user from USRSEC file | Online (CICS) | **Read/Delete:** USRSEC (CICS READ/DELETE) | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |

---

## 2. Sub-Application Programs

### 2.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

| # | Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|---------|----------------|---------------------|---------------------|
| 32 | CBPAUP0C.cbl | Delete expired pending authorization messages from IMS DB | Batch (IMS) | **IMS DB:** PAUTBPCB (DL/I GN, GNP, DLET) | CIPAUSMY, CIPAUDTY |
| 33 | COPAUA0C.cbl | Card authorization decision program — receives MQ request, evaluates authorization rules, sends MQ reply | Online (CICS/IMS/MQ) | **MQ:** MQOPEN, MQGET, MQPUT1, MQCLOSE · **CICS:** CARDXREF, ACCTDAT, CUSTDAT reads | CMQODV, CMQMDV, CMQV, CMQTML, CMQPMOV, CMQGMOV, CCPAURQY, CCPAURLY, CCPAUERY, CIPAUSMY, CIPAUDTY, CVACT03Y, CVACT01Y, CVCUS01Y |
| 34 | COPAUS0C.cbl | Summary view of authorization messages via CICS BMS screens | Online (CICS/IMS/BMS) | **IMS DB:** DL/I reads · **CICS:** BMS map send/receive | COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |
| 35 | COPAUS1C.cbl | Detail view of a single authorization message | Online (CICS/IMS/BMS) | **IMS DB:** DL/I reads · **CICS:** BMS map send/receive | COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |
| 36 | COPAUS2C.cbl | Mark authorization message as fraud — updates IMS DB and writes to DB2 fraud table | Online (CICS/IMS/DB2) | **IMS DB:** DL/I update · **DB2:** INSERT into AUTHFRDS | CIPAUDTY |
| 37 | DBUNLDGS.CBL | Unload IMS database to GSAM files (root and child segments) | Batch (IMS) | **IMS DB:** DL/I GN, GNP · **Write:** PASFILOP (GSAM), PADFILOP (GSAM) | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB, PASFLPCB, PADFLPCB |
| 38 | PAUDBLOD.CBL | Load IMS database from flat files | Batch (IMS) | **Read:** INFILE1, INFILE2 (seq) · **IMS DB:** DL/I ISRT, GU | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |
| 39 | PAUDBUNL.CBL | Unload IMS database to flat files (root and child segments) | Batch (IMS) | **IMS DB:** DL/I GN, GNP · **Write:** OPFILE1, OPFILE2 (seq) | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |

### 2.4 IMS Database Definitions

Files in `app/app-authorization-ims-db2-mq/ims/`:

| File | Type | Purpose |
|------|------|---------|
| DBPAUTP0.dbd | DBD | Primary authorization HIDAM database |
| DBPAUTX0.dbd | DBD | Authorization index database |
| PADFLDBD.DBD | DBD | GSAM output for detail segments |
| PASFLDBD.DBD | DBD | GSAM output for summary segments |
| PSBPAUTB.psb | PSB | Batch authorization PSB |
| PSBPAUTL.psb | PSB | Load authorization PSB |
| PAUTBUNL.PSB | PSB | Unload authorization PSB |
| DLIGSAMP.PSB | PSB | GSAM sample PSB |

### 2.5 DB2 Schema Definitions

**Authorization module** (`app/app-authorization-ims-db2-mq/`):

| File | Location | Purpose |
|------|----------|---------|
| AUTHFRDS.ddl | `ddl/` | CREATE TABLE for CARDDEMO.AUTHFRDS (fraud/authorization detail) |
| XAUTHFRD.ddl | `ddl/` | CREATE UNIQUE INDEX on AUTHFRDS (CARD_NUM ASC, AUTH_TS DESC) |
| AUTHFRDS.dcl | `dcl/` | DCLGEN — COBOL host variable declarations for AUTHFRDS |

**Transaction Type module** (`app/app-transaction-type-db2/`):

| File | Location | Purpose |
|------|----------|---------|
| TRNTYPE.ddl | `ddl/` | CREATE TABLE for CARDDEMO.TRANSACTION_TYPE |
| TRNTYCAT.ddl | `ddl/` | CREATE TABLE for CARDDEMO.TRANSACTION_TYPE_CATEGORY (FK to TRANSACTION_TYPE) |
| XTRNTYPE.ddl | `ddl/` | CREATE UNIQUE INDEX on TRANSACTION_TYPE (TR_TYPE ASC) |
| XTRNTYCAT.ddl | `ddl/` | CREATE UNIQUE INDEX on TRANSACTION_TYPE_CATEGORY (TRC_TYPE_CODE ASC, TRC_TYPE_CATEGORY ASC) |

### 2.6 CSD Resource Definitions

| File | Location | Purpose |
|------|----------|---------|
| CARDDEMO.CSD | `app/csd/` | Base application CICS resource definitions (transactions, programs, files) |
| CRDDEMO2.csd | `app/app-authorization-ims-db2-mq/csd/` | Authorization module CICS resources |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|---------|----------------|---------------------|---------------------|
| 40 | COTRTLIC.cbl | List transaction types for updates and deletes — DB2 cursor-based browsing | Online (CICS/DB2) | **DB2:** SELECT from TR_TYPE, TR_CAT tables | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COTRTLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY |
| 41 | COTRTUPC.cbl | Accept and process transaction type updates — DB2 INSERT/UPDATE/DELETE | Online (CICS/DB2) | **DB2:** SELECT, INSERT, UPDATE, DELETE on TR_TYPE, TR_CAT tables | CSUTLDWY, CVCRD01Y, DFHBMSCA, DFHAID, COTTL01Y, COTRTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, COCOM01Y, CSSETATY, CSSTRPFY |
| 42 | COBTUPDT.cbl | Batch update of transaction types based on user input file | Batch (DB2) | **Read:** INPFILE (seq) · **DB2:** INSERT/UPDATE/DELETE on TR_TYPE | *(none — inline SQL)* |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|---------|----------------|---------------------|---------------------|
| 43 | COACCT01.cbl | Account inquiry via MQ — receives MQ request, reads account data, sends MQ reply | Online (CICS/MQ) | **MQ:** MQOPEN ×3, MQGET, MQPUT ×2, MQCLOSE ×3 · **VSAM:** Account data reads | CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML, CVACT01Y |
| 44 | CODATE01.cbl | Date service via MQ — receives MQ request, processes date operations, sends MQ reply | Online (CICS/MQ) | **MQ:** MQOPEN ×3, MQGET, MQPUT ×2, MQCLOSE ×3 | CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML |

---

## 3. JCL Job Catalog (`app/jcl/`)

### 3.1 Data Setup / VSAM File Definition Jobs

| # | JCL Job | Purpose | Step Sequence |
|---|---------|---------|---------------|
| 1 | ACCTFILE.jcl | Define and load Account VSAM KSDS from PS file | STEP05 (IDCAMS: delete), STEP10 (IDCAMS: define cluster), STEP15 (IDCAMS: REPRO load from ACCTDATA.PS) |
| 2 | CARDFILE.jcl | Define and load Card VSAM KSDS from PS; create AIX and PATH | CLCIFIL (SDSF: close files), STEP05–STEP15 (IDCAMS: delete/define/repro), STEP40–STEP60 (IDCAMS: define AIX, build AIX, define PATH), OPCIFIL (SDSF: open files) |
| 3 | CUSTFILE.jcl | Define and load Customer VSAM KSDS from PS file | CLCIFIL (SDSF: close), STEP05–STEP15 (IDCAMS: delete/define/repro), OPCIFIL (SDSF: open) |
| 4 | XREFFILE.jcl | Define and load Card Cross-Reference VSAM KSDS from PS; create AIX and PATH | STEP05–STEP15 (IDCAMS: delete/define/repro), STEP20 (IDCAMS: define AIX, build AIX, define PATH) |
| 5 | TRANFILE.jcl | Define and load Daily Transaction VSAM KSDS from PS | CLCIFIL (SDSF: close), STEP05–STEP15 (IDCAMS: delete/define/repro from DALYTRAN.PS.INIT) |
| 6 | TCATBALF.jcl | Define and load Transaction Category Balance VSAM KSDS from PS | STEP05–STEP15 (IDCAMS: delete/define/repro from TCATBALF.PS) |
| 7 | TRANCATG.jcl | Define and load Transaction Category VSAM KSDS from PS | STEP05–STEP15 (IDCAMS: delete/define/repro from TRANCATG.PS) |
| 8 | TRANTYPE.jcl | Define and load Transaction Type VSAM KSDS from PS | STEP05–STEP15 (IDCAMS: delete/define/repro from TRANTYPE.PS) |
| 9 | DISCGRP.jcl | Define and load Discount Group VSAM KSDS from PS | STEP05–STEP15 (IDCAMS: delete/define/repro from DISCGRP.PS) |
| 10 | DALYREJS.jcl | Define Daily Rejects VSAM KSDS | STEP05 (IDCAMS: define cluster) |
| 11 | REPTFILE.jcl | Define Report output VSAM KSDS | STEP05 (IDCAMS: define cluster) |
| 12 | TRANIDX.jcl | Define transaction index VSAM clusters | STEP20–STEP30 (IDCAMS: define/build alternate indexes) |
| 13 | DUSRSECJ.jcl | Define and load User Security VSAM KSDS (with initial admin user data) | PREDEL (IEFBR14: delete PS), STEP01 (IEBGENER: create PS), STEP02–STEP03 (IDCAMS: define cluster, REPRO load) |
| 14 | ESDSRRDS.jcl | Define ESDS and RRDS VSAM clusters (test/sample datasets) | PREDEL (IEFBR14: delete), STEP01 (IEBGENER: create PS), STEP02–STEP05 (IDCAMS: define ESDS, load, define RRDS, load) |
| 15 | DEFCUST.jcl | Define additional customer-related VSAM clusters | STEP05 ×2 (IDCAMS: define clusters) |
| 16 | DEFGDGB.jcl | Define GDG base for transaction backups | STEP05 (IDCAMS: define GDG base) |

### 3.2 Batch Processing Jobs

| # | JCL Job | Purpose | Step Sequence |
|---|---------|---------|---------------|
| 17 | READACCT.jcl | Read and print account data | PREDEL (IEFBR14: cleanup), STEP05 (PGM=**CBACT01C**: read ACCTDATA VSAM → OUTFILE, ARRYFILE, VBRCFILE) |
| 18 | READCARD.jcl | Read and print card data | STEP05 (PGM=**CBACT02C**: read CARDDATA VSAM) |
| 19 | READCUST.jcl | Read and print customer data | STEP05 (PGM=**CBCUS01C**: read CUSTDATA VSAM) |
| 20 | READXREF.jcl | Read and print cross-reference data | STEP05 (PGM=**CBACT03C**: read CARDXREF VSAM) |
| 21 | INTCALC.jcl | Calculate interest on account balances | STEP15 (PGM=**CBACT04C** PARM='2022071800': read TCATBALF, XREFFILE, ACCTFILE, DISCGRP) |
| 22 | POSTTRAN.jcl | Post daily transactions to master file | STEP15 (PGM=**CBTRN02C**: read DALYTRAN → write TRANFILE, DALYREJS, update ACCTFILE, TCATBALF) |
| 23 | TRANREPT.jcl | Generate transaction detail report | STEP05R (REPROC: backup TRANSACT), STEP05R (SORT: sort by card+date), STEP10R (PGM=**CBTRN03C**: generate report) |
| 24 | CREASTMT.JCL | Create account statements | DELDEF01 (IDCAMS: cleanup), STEP010 (SORT: sort transactions), STEP020 (IDCAMS: load sorted data to VSAM) |
| 25 | COMBTRAN.jcl | Combine transaction files (merge backup + system transactions) | STEP05R (SORT: merge and sort), STEP10 (IDCAMS: REPRO to VSAM) |
| 26 | TRANBKP.jcl | Backup transaction VSAM to GDG | STEP05R (REPROC: backup), STEP05 (IDCAMS: delete VSAM), STEP10 (IDCAMS: redefine VSAM) |
| 27 | PRTCATBL.jcl | Print transaction category balances report | DELDEF (cleanup), STEP05R (REPROC: copy TCATBALF), STEP10R (SORT: sort and format) |
| 28 | CBEXPORT.jcl | Export all CardDemo data for branch migration | STEP01 (IDCAMS: define export cluster), STEP02 (PGM=**CBEXPORT**: read all VSAM files → export file) |
| 29 | CBIMPORT.jcl | Import branch migration data into CardDemo | STEP01 (PGM=**CBIMPORT**: read export file → split to CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT + ERROUT) |
| 30 | WAITSTEP.jcl | Execute wait/delay utility | WAIT (PGM=**COBSWAIT**: pause execution) |
| 31 | DEFGDGD.jcl | GDG backup and reload for transaction types | STEP10–STEP60 (IDCAMS/IEBGENER: backup TRANTYPE, delete, reload; backup TRANCATG) |

### 3.3 CICS Administration Jobs

| # | JCL Job | Purpose | Step Sequence |
|---|---------|---------|---------------|
| 32 | CBADMCDJ.jcl | Define CICS CSD resources (transactions and programs) | STEP1 (PGM=DFHCSDUP: add transaction/program definitions) |
| 33 | CLOSEFIL.jcl | Close CICS files via SDSF | CLCIFIL (SDSF: SET FILE CLOSE) |
| 34 | OPENFIL.jcl | Open CICS files via SDSF | OPCIFIL (SDSF: SET FILE OPEN) |

### 3.4 Utility / File Transfer Jobs

| # | JCL Job | Purpose | Step Sequence |
|---|---------|---------|---------------|
| 35 | FTPJCL.JCL | FTP file transfer | STEP1 (PGM=FTP) |
| 36 | INTRDRJ1.JCL | Submit JCL via internal reader (chain to INTRDRJ2) | IDCAMS (REPRO), STEP01 (IEBGENER → INTRDR) |
| 37 | INTRDRJ2.JCL | Second-stage internal reader job | IDCAMS (REPRO backup) |
| 38 | TXT2PDF1.JCL | Convert text statement to PDF | TXT2PDF (PGM=IKJEFT1B: run TXT2PDF REXX) |

### 3.5 Sub-Application JCL

#### Authorization IMS-DB2-MQ (`app/app-authorization-ims-db2-mq/jcl/`)

| # | JCL Job | Purpose | Step Sequence |
|---|---------|---------|---------------|
| 39 | CBPAUP0J.jcl | Execute IMS program to delete expired authorizations | STEP01 (PGM=DFSRRC00: run CBPAUP0C via IMS) |
| 40 | DBPAUTP0.jcl | Unload IMS auth database to flat file | STEPDEL (IEFBR14: cleanup), UNLOAD (PGM=DFSRRC00: unload) |
| 41 | LOADPADB.JCL | Load IMS auth database from flat files | STEP01 (PGM=DFSRRC00: run PAUDBLOD) |
| 42 | UNLDGSAM.JCL | Unload IMS auth database to GSAM files | STEP01 (PGM=DFSRRC00: run DBUNLDGS) |
| 43 | UNLDPADB.JCL | Unload IMS auth database to sequential files | STEP0 (IEFBR14: cleanup), STEP01 (PGM=DFSRRC00: run PAUDBUNL) |

#### Transaction Type DB2 (`app/app-transaction-type-db2/jcl/`)

| # | JCL Job | Purpose | Step Sequence |
|---|---------|---------|---------------|
| 44 | CREADB21.jcl | Create DB2 tables for transaction types and categories | FREEPLN (IKJEFT01: free plan), CRCRDDB (IKJEFT01: create tables, bind) |
| 45 | MNTTRDB2.jcl | Maintain transaction type DB2 data (batch add/update/delete) | STEP1 (IKJEFT01: run COBTUPDT) |
| 46 | TRANEXTR.jcl | Extract and backup transaction type data | STEP10 (IEBGENER: backup TRANTYPE), STEP20 (IEBGENER: backup TRANCATG), STEP30 (IEFBR14: delete originals) |

### 3.6 Shared JCL Procedures

Files in `app/proc/`:

| Procedure | Purpose | Used By |
|-----------|---------|---------|
| REPROC.prc | Reusable IDCAMS REPRO procedure | TRANBKP.jcl, TRANREPT.jcl, PRTCATBL.jcl |
| TRANREPT.prc | Transaction report procedure | TRANREPT.jcl |

---

## 5. Scheduler Definitions

Files in `app/scheduler/`:

| File | Purpose |
|------|---------|
| CardDemo.ca7 | CA-7 scheduling definitions — defines trigger chains for the daily batch pipeline |
| CardDemo.controlm | Control-M equivalent definitions |

**Key CA-7 trigger chains:**

```
Chain 1 (Daily Batch):
  CLOSEFIL → CBPAUP0J → POSTTRAN → WAITSTEP → OPENFIL

Chain 2 (Statement Generation):
  CLOSEFIL → CREASTMT → TXT2PDF1 → WAITSTEP → OPENFIL

Chain 3 (Category Balance Report):
  CLOSEFIL → PRTCATBL → ...

Chain 4 (Data Setup):
  CLOSEFIL → TRANTYPE → WAITSTEP → CLOSEFIL1/CLOSEFIL2
  CLOSEFIL1 → TRANCATG → WAITSTEP → CLOSEFIL → READACCT → READCARD → READCUST → READXREF → WAITSTEP → OPENFIL
  CLOSEFIL2 → TCATBALF → WAITSTEP → CLOSEFIL → ...
```

---

## 6. Sample Data Files

EBCDIC-encoded sample datasets in `app/data/EBCDIC/`:

| File | Description |
|------|-----------|
| AWS.M2.CARDDEMO.ACCDATA.PS | Account data (variant) |
| AWS.M2.CARDDEMO.ACCTDATA.PS | Account data (primary) |
| AWS.M2.CARDDEMO.CARDDATA.PS | Card data |
| AWS.M2.CARDDEMO.CARDXREF.PS | Card cross-reference data |
| AWS.M2.CARDDEMO.CUSTDATA.PS | Customer data |
| AWS.M2.CARDDEMO.DALYTRAN.PS | Daily transaction feed |
| AWS.M2.CARDDEMO.DISCGRP.PS | Discount group data |
| AWS.M2.CARDDEMO.TCATBALF.PS | Transaction category balance data |
| AWS.M2.CARDDEMO.TRANCATG.PS | Transaction category data |
| AWS.M2.CARDDEMO.TRANTYPE.PS | Transaction type data |
| AWS.M2.CARDDEMO.USRSEC.PS | User security data |

> **Note:** AWS.M2.CARDDEMO.ACCDATA.PS appears to be a stale/misspelled variant of ACCTDATA.PS — no JCL or program references ACCDATA.

> **Note:** No sample data exists for IMS authorization database segments, DB2 tables (AUTHFRDS, TR_TYPE, TR_CAT), or MQ messages.

---

## 7. Test Harness

A Java-based test harness exists in `test-harness/`:

- **64 unit tests** that validate Java rewrites against known-good COBOL output
- Field-by-field comparison using record layouts from COBOL copybooks and FD sections
- **Coverage:** CBACT01C (edge cases: zero debit substitution, date truncation, array slots), CBTRN02C/CBACT04C (business rules: record counts, balance integrity, credit limits, expiration)
- Hand-rolled ASCII zoned decimal and COMP-3 codecs
- See `test-harness/README.md` for details

---

## 8. Scope Boundaries

Planned but unimplemented features (from README.md roadmap):

- **DB2 Rewards** — stored procedures, functions, dynamic SQL
- **IMS DC** — Data Communications (IMS online transaction processing)
- **FTP/SFTP integration** — file transfer automation
- **Web Service connectivity** — external service interfaces

---

## 9. Summary Statistics

| Metric | Count |
|--------|-------|
| Total programs | 46 (44 COBOL + 2 Assembler) |
| COBOL batch programs | 19 |
| COBOL online (CICS) programs | 17 |
| COBOL online (CICS/IMS) programs | 4 |
| COBOL online (CICS/MQ) programs | 2 |
| COBOL online (CICS/DB2) programs | 2 |
| COBOL utility/subroutine programs | 3 |
| Assembler programs | 2 |
| IMS definitions (DBD/PSB) | 8 |
| DB2 schema files (DDL/DCL) | 7 |
| CSD definitions | 2 |
| JCL jobs (main) | 38 |
| JCL jobs (sub-applications) | 8 |
| Total JCL jobs | 46 |
| JCL procedures | 2 |
| Scheduler definitions | 2 |
| Copybooks — business/utility (`app/cpy/`) | 30 |
| Copybooks — BMS-generated (`cpy-bms/`) | 21 (17 base + 2 auth + 2 DB2) |
| Copybooks — sub-application | 13 |
| BMS maps | 21 (17 base + 2 auth + 2 DB2) |
| Sample data files (EBCDIC) | 11 |

| Metric | Count |
|--------|-------|
| Total COBOL programs | 44 |
| Batch programs | 19 |
| Online (CICS) programs | 17 |
| Online (CICS/IMS) programs | 4 |
| Online (CICS/MQ) programs | 2 |
| Online (CICS/DB2) programs | 2 |
| Utility/subroutine programs | 3 |
| JCL jobs (main) | 38 |
| JCL jobs (sub-applications) | 8 |
| Total JCL jobs | 46 |
| Copybooks (main app/cpy/) | 30 |
| Copybooks (sub-application) | 13 |
| BMS maps | 21 |
