# Application Inventory — CardDemo COBOL Estate

## Overview

The CardDemo application is a mainframe-based credit card management system running on z/OS with CICS for online processing, VSAM for file storage, DB2 for relational data, and IBM MQ for messaging. The system supports customer account management, credit card operations, transaction processing, bill payment, reporting, and user security administration.

---

## 1. COBOL Programs — Core Application (`app/cbl/`)

### 1.1 Batch Programs

| # | Filename | Program-ID | Purpose | Key I/O Operations | Copybooks Referenced |
|---|----------|-----------|---------|---------------------|---------------------|
| 1 | CBACT01C.cbl | CBACT01C | Read account VSAM file and write to multiple output formats (flat, array, variable-length) | **Read:** ACCTFILE (KSDS) **Write:** OUTFILE, ARRYFILE, VBRCFILE | CVACT01Y, CODATECN |
| 2 | CBACT02C.cbl | CBACT02C | Read and print card data file | **Read:** CARDFILE (KSDS) | CVACT02Y |
| 3 | CBACT03C.cbl | CBACT03C | Read and print account cross-reference data file | **Read:** XREFFILE (KSDS) | CVACT03Y |
| 4 | CBACT04C.cbl | CBACT04C | Interest calculator — compute interest charges based on transaction category balances and disclosure group rates | **Read:** TCATBALF (KSDS), XREFFILE, ACCTFILE, DISCGRP, TRANSACT **Write:** TRANFILE **Rewrite:** ACCTFILE | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| 5 | CBCUS01C.cbl | CBCUS01C | Read and print customer data file | **Read:** CUSTFILE (KSDS) | CVCUS01Y |
| 6 | CBEXPORT.cbl | CBEXPORT | Export customer data for branch migration — reads normalized CardDemo files and creates multi-record export file | **Read:** CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE **Write:** EXPFILE | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 7 | CBIMPORT.cbl | CBIMPORT | Import customer data from branch migration export — splits multi-record file into normalized targets with validation | **Read:** EXPFILE **Write:** CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 8 | CBSTM03A.CBL | CBSTM03A | Print account statements from transaction data — master control program | **Write:** STMTFILE, HTMLFILE **Calls:** CBSTM03B | COSTM01, CUSTREC, CVACT01Y, CVACT03Y |
| 9 | CBSTM03B.CBL | CBSTM03B | Statement file processing sub-program — reads transaction, xref, customer, and account files for statement generation | **Read:** TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | (none — receives data via USING clause) |
| 10 | CBTRN01C.cbl | CBTRN01C | Post records from daily transaction file — initial validation and cross-reference lookup | **Read:** DALYTRAN (SEQ), CUSTFILE, XREFFILE, CARDFILE, ACCTFILE **Write:** TRANFILE | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| 11 | CBTRN02C.cbl | CBTRN02C | Post records from daily transaction file — full posting with reject handling, category balance updates, and account balance updates | **Read:** DALYTRAN (SEQ), XREFFILE, ACCTFILE, TCATBALF **Write:** TRANFILE, DALYREJS, TCATBALF **Rewrite:** ACCTFILE, TCATBALF | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| 12 | CBTRN03C.cbl | CBTRN03C | Print transaction detail report — formatted report with type/category descriptions | **Read:** TRANFILE (SEQ), CARDXREF, TRANTYPE, TRANCATG, DATEPARM **Write:** TRANREPT (report file) | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| 13 | CBSTM03A.CBL | CBSTM03A | Statement generation (master) | **Write:** STMTFILE, HTMLFILE | COSTM01, CUSTREC, CVACT01Y, CVACT03Y |
| 14 | COBSWAIT.cbl | COBSWAIT | Utility program to wait (parameter in centiseconds) | **Calls:** MVSWAIT | (none) |
| 15 | CSUTLDTC.cbl | CSUTLDTC | Date utility — convert and validate dates using LE CEEDAYS | **Calls:** CEEDAYS | (none) |

### 1.2 Online (CICS) Programs

| # | Filename | Program-ID | Purpose | Key I/O Operations | Copybooks Referenced |
|---|----------|-----------|---------|---------------------|---------------------|
| 1 | COSGN00C.cbl | COSGN00C | Sign-on screen — authenticate users against USRSEC file | **CICS READ:** USRSEC **CICS XCTL:** to menu programs | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 2 | COMEN01C.cbl | COMEN01C | Main menu for regular users — route to sub-functions | **CICS XCTL:** to selected program **CICS INQUIRE** | COCOM01Y, COMEN01, COMEN02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 3 | COADM01C.cbl | COADM01C | Admin menu for admin users — route to admin sub-functions | **CICS SEND/RECEIVE** | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 4 | COACTVWC.cbl | COACTVWC | Account view — display account details, card info, and customer info | **CICS READ:** CARDDAT (AIX), ACCTDAT, CUSTDAT | CVCRD01Y, COCOM01Y, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 5 | COACTUPC.cbl | COACTUPC | Account update — accept and validate account field changes, update account and customer records | **CICS READ:** CARDDAT (AIX), ACCTDAT, CUSTDAT **CICS REWRITE:** ACCTDAT, CUSTDAT | CSUTLDWY, CVCRD01Y, CSLKPCDY, COACTUP, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CSSETATY, DFHAID, DFHBMSCA |
| 6 | COCRDLIC.cbl | COCRDLIC | Credit card list — browse card file with forward/backward paging | **CICS STARTBR/READNEXT/READPREV/ENDBR:** CARDDAT | COCOM01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CVCRD01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 7 | COCRDSLC.cbl | COCRDSLC | Credit card detail view — display full card info with customer and account details | **CICS READ:** CARDDAT, ACCTDAT, CUSTDAT | COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCRD01Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 8 | COCRDUPC.cbl | COCRDUPC | Credit card update — modify card details (status, expiration, name) | **CICS READ/REWRITE:** CARDDAT **CICS READ:** ACCTDAT, CUSTDAT | COCOM01Y, COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCRD01Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 9 | COTRN00C.cbl | COTRN00C | Transaction list — browse TRANSACT file with paging | **CICS STARTBR/READNEXT/READPREV/ENDBR:** TRANSACT | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 10 | COTRN01C.cbl | COTRN01C | Transaction view — display single transaction details | **CICS READ:** TRANSACT | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 11 | COTRN02C.cbl | COTRN02C | Transaction add — create new transaction with validation | **CICS READ:** ACCTDAT, XREFDAT **CICS WRITE:** TRANSACT **Calls:** CSUTLDTC | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 12 | CORPT00C.cbl | CORPT00C | Transaction reports — submit batch report jobs via CICS TD queue | **CICS WRITEQ TD** **Calls:** CSUTLDTC | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 13 | COBIL00C.cbl | COBIL00C | Bill payment — pay account balance and generate payment transaction | **CICS READ/REWRITE:** ACCTDAT **CICS WRITE:** TRANSACT **CICS STARTBR/READPREV/ENDBR:** TRANSACT | COBIL00, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 14 | COUSR00C.cbl | COUSR00C | User list — browse USRSEC file with paging | **CICS STARTBR/READNEXT/READPREV/ENDBR:** USRSEC | COCOM01Y, COTTL01Y, COUSR00, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 15 | COUSR01C.cbl | COUSR01C | User add — create new regular/admin user in USRSEC file | **CICS WRITE:** USRSEC | COCOM01Y, COTTL01Y, COUSR01, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 16 | COUSR02C.cbl | COUSR02C | User update — modify user record in USRSEC file | **CICS READ/REWRITE:** USRSEC | COCOM01Y, COTTL01Y, COUSR02, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 17 | COUSR03C.cbl | COUSR03C | User delete — remove user from USRSEC file | **CICS READ/DELETE:** USRSEC | COCOM01Y, COTTL01Y, COUSR03, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |

---

## 2. Sub-Application Programs

### 2.1 Authorization Sub-Application (`app/app-authorization-ims-db2-mq/cbl/`)

Uses IMS DB, DB2, and IBM MQ for real-time card authorization processing.

| # | Filename | Program-ID | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-----------|---------|---------------|---------------------|---------------------|
| 1 | COPAUA0C.cbl | COPAUA0C | Card authorization decision — real-time auth via MQ request/reply | Online | **MQ:** MQOPEN, MQGET, MQPUT1, MQCLOSE **CICS READ:** VSAM files **CICS WRITEQ** | CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, CMQGMOV, CMQMDV, CMQODV, CMQPMOV, CMQTML, CMQV, CVACT01Y, CVACT03Y, CVCUS01Y |
| 2 | COPAUS0C.cbl | COPAUS0C | Summary view of authorization messages — paginated display | Online | **CICS READ/SEND/RECEIVE** | CIPAUDTY, CIPAUSMY, COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, DFHAID, DFHBMSCA |
| 3 | COPAUS1C.cbl | COPAUS1C | Detail view of authorization message | Online | **CICS LINK/SEND/RECEIVE** | CIPAUDTY, CIPAUSMY, COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA |
| 4 | COPAUS2C.cbl | COPAUS2C | Mark authorization message as fraud | Online | **CICS ASKTIME/FORMATTIME** | CIPAUDTY |
| 5 | CBPAUP0C.cbl | CBPAUP0C | Delete expired pending authorization messages (purge) | Batch | **IMS DL/I calls** | CIPAUDTY, CIPAUSMY |
| 6 | PAUDBLOD.CBL | PAUDBLOD | Load IMS database with authorization data | Batch | **IMS ISRT** (DL/I) | CIPAUDTY, CIPAUSMY, IMSFUNCS, PAUTBPCB |
| 7 | PAUDBUNL.CBL | PAUDBUNL | Unload IMS database — extract authorization data | Batch | **IMS GN/GNP** (DL/I) | CIPAUDTY, CIPAUSMY, IMSFUNCS, PAUTBPCB |
| 8 | DBUNLDGS.CBL | DBUNLDGS | Unload IMS database (generic sample) | Batch | **IMS GN/GNP/ISRT** (DL/I) | CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB |

### 2.2 Transaction Type DB2 Sub-Application (`app/app-transaction-type-db2/cbl/`)

Manages transaction type reference data via DB2 with CICS online screens.

| # | Filename | Program-ID | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-----------|---------|---------------|---------------------|---------------------|
| 1 | COTRTLIC.cbl | COTRTLIC | List transaction types — paginated DB2 cursor browse with select/delete | Online | **DB2:** SELECT with CURSOR, DELETE **CICS SEND/RECEIVE** | CSDB2RPY, CSDB2RWY (via embedded SQL) |
| 2 | COTRTUPC.cbl | COTRTUPC | Transaction type update — accept and process changes to transaction type records | Online | **DB2:** SELECT, UPDATE, INSERT **CICS SEND/RECEIVE** | CSDB2RPY, CSDB2RWY (via embedded SQL) |
| 3 | COBTUPDT.cbl | COBTUPDT | Batch transaction type update — update transaction types based on user input | Batch | **DB2:** UPDATE, SELECT | (embedded SQL) |

### 2.3 VSAM-MQ Sub-Application (`app/app-vsam-mq/cbl/`)

Event-driven account processing via IBM MQ.

| # | Filename | Program-ID | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-----------|---------|---------------|---------------------|---------------------|
| 1 | COACCT01.cbl | COACCT01 | Account event processor — MQ-triggered account data operations | Online | **MQ:** MQOPEN, MQGET, MQPUT, MQCLOSE **CICS READ** | CVACT01Y |
| 2 | CODATE01.cbl | CODATE01 | Date event processor — MQ-triggered date/time operations | Online | **MQ:** MQOPEN, MQGET, MQPUT, MQCLOSE **CICS ASKTIME/FORMATTIME** | (none) |

---

## 3. JCL Job Catalog (`app/jcl/`)

### 3.1 VSAM File Definition Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|--------------|
| 1 | ACCTFILE.jcl | Delete/define/load Account VSAM KSDS | STEP05: IDCAMS DELETE → STEP10: IDCAMS DEFINE CLUSTER → STEP15: IDCAMS REPRO (load from PS) |
| 2 | CARDFILE.jcl | Delete/define/load Card Data VSAM KSDS + AIX | CLCIFIL: SDSF CLOSE CICS files → STEP05: IDCAMS DELETE → STEP10: IDCAMS DEFINE CLUSTER → STEP15: IDCAMS REPRO → STEP40: DEFINE AIX → STEP50: DEFINE PATH → STEP60: BLDINDEX → OPCIFIL: SDSF OPEN CICS files |
| 3 | CUSTFILE.jcl | Delete/define/load Customer VSAM KSDS | CLCIFIL: SDSF CLOSE → STEP05: IDCAMS DELETE → STEP10: IDCAMS DEFINE → STEP15: IDCAMS REPRO → OPCIFIL: SDSF OPEN |
| 4 | XREFFILE.jcl | Delete/define/load Cross-Reference VSAM KSDS + AIX | STEP05: IDCAMS DELETE → STEP10: IDCAMS DEFINE CLUSTER → STEP15: IDCAMS REPRO → STEP20: DEFINE AIX → STEP25: DEFINE PATH → STEP30: BLDINDEX |
| 5 | TRANFILE.jcl | Define Transaction Master VSAM KSDS + AIX | CLCIFIL: SDSF CLOSE → STEP05: DELETE → STEP10: DEFINE CLUSTER → STEP15: REPRO → STEP20: DEFINE AIX → STEP25: DEFINE PATH → STEP30: BLDINDEX → OPCIFIL: SDSF OPEN |
| 6 | TCATBALF.jcl | Define Transaction Category Balance VSAM KSDS | STEP05: DELETE → STEP10: DEFINE CLUSTER → STEP15: REPRO |
| 7 | DISCGRP.jcl | Define Disclosure Group VSAM KSDS | STEP05: DELETE → STEP10: DEFINE CLUSTER → STEP15: REPRO |
| 8 | TRANTYPE.jcl | Define Transaction Type VSAM KSDS | STEP05: DELETE → STEP10: DEFINE CLUSTER → STEP15: REPRO |
| 9 | TRANCATG.jcl | Define Transaction Category VSAM KSDS | STEP05: DELETE → STEP10: DEFINE CLUSTER → STEP15: REPRO |
| 10 | TRANIDX.jcl | Define AIX on Transaction Master | STEP20: DEFINE AIX → STEP25: DEFINE PATH → STEP30: BLDINDEX |
| 11 | DEFCUST.jcl | Alternate customer file definition | STEP05: IDCAMS DELETE → STEP05: IDCAMS DEFINE |
| 12 | DUSRSECJ.jcl | Define User Security file (USRSEC) | PREDEL: IEFBR14 → STEP01: IEBGENER → STEP02: IDCAMS DEFINE → STEP03: IDCAMS REPRO |
| 13 | ESDSRRDS.jcl | Define ESDS and RRDS files | PREDEL: IEFBR14 → STEP01: IEBGENER → STEP02-05: IDCAMS DEFINE CLUSTER |

### 3.2 Batch Processing Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|--------------|
| 1 | POSTTRAN.jcl | Post daily transactions | STEP15: PGM=CBTRN02C |
| 2 | INTCALC.jcl | Calculate interest charges | STEP15: PGM=CBACT04C (PARM='2022071800') |
| 3 | TRANREPT.jcl | Generate transaction detail report | STEP05R: SORT (pre-sort) → STEP10R: PGM=CBTRN03C |
| 4 | READACCT.jcl | Read/print account data | PREDEL: IEFBR14 → STEP05: PGM=CBACT01C |
| 5 | READCARD.jcl | Read/print card data | STEP05: PGM=CBACT02C |
| 6 | READCUST.jcl | Read/print customer data | STEP05: PGM=CBCUS01C |
| 7 | READXREF.jcl | Read/print cross-reference data | STEP05: PGM=CBACT03C |
| 8 | CBEXPORT.jcl | Export customer data for branch migration | STEP01: IDCAMS (verify files) → STEP02: PGM=CBEXPORT |
| 9 | CBIMPORT.jcl | Import data from branch migration export | STEP01: PGM=CBIMPORT |
| 10 | COMBTRAN.jcl | Combine daily transactions into master | STEP05R: SORT → STEP10: IDCAMS REPRO |
| 11 | CREASTMT.JCL | Create account statements | DELDEF01: IDCAMS → STEP010: SORT → STEP020: IDCAMS |
| 12 | PRTCATBL.jcl | Print transaction category balance file | DELDEF: IEFBR14 → STEP05R: REPROC → STEP10R: SORT |
| 13 | WAITSTEP.jcl | Utility wait step | WAIT: PGM=COBSWAIT |
| 14 | TRANBKP.jcl | Backup and clear transaction master | STEP05R: REPROC → STEP05: IDCAMS REPRO → STEP10: IDCAMS DELETE |

### 3.3 Infrastructure / GDG Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|--------------|
| 1 | DEFGDGB.jcl | Define GDG bases for batch outputs | STEP05: IDCAMS DEFINE GDG |
| 2 | DEFGDGD.jcl | Define GDG for DB2 extracts | STEP10-60: IDCAMS + IEBGENER (multiple GDGs) |
| 3 | DALYREJS.jcl | Define GDG for daily rejects | STEP05: IDCAMS DEFINE GDG |
| 4 | REPTFILE.jcl | Define GDG for report files | STEP05: IDCAMS DEFINE GDG |

### 3.4 CICS File Operations

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|--------------|
| 1 | CLOSEFIL.jcl | Close CICS-managed files | CLCIFIL: SDSF (CEMT SET FIL CLO) |
| 2 | OPENFIL.jcl | Open CICS-managed files | OPCIFIL: SDSF (CEMT SET FIL OPE) |

### 3.5 Utility Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|--------------|
| 1 | CBADMCDJ.jcl | CICS resource definitions (CSD update) | STEP1: PGM=DFHCSDUP |
| 2 | FTPJCL.JCL | FTP transfer of data files | (FTP commands) |
| 3 | INTRDRJ1.JCL | Internal reader job 1 | (internal reader submission) |
| 4 | INTRDRJ2.JCL | Internal reader job 2 | (internal reader submission) |
| 5 | TXT2PDF1.JCL | Convert text report to PDF | (PDF conversion) |

---

## 4. Sub-Application JCL

### 4.1 Authorization IMS (`app/app-authorization-ims-db2-mq/jcl/`)

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|--------------|
| 1 | CBPAUP0J.jcl | Run authorization purge batch | PGM=CBPAUP0C |
| 2 | DBPAUTP0.jcl | IMS database operations for auth data | IMS DL/I utilities |
| 3 | LOADPADB.JCL | Load authorization IMS database | PGM=PAUDBLOD |
| 4 | UNLDPADB.JCL | Unload authorization IMS database | PGM=PAUDBUNL |
| 5 | UNLDGSAM.JCL | Unload generic IMS sample | PGM=DBUNLDGS |

### 4.2 Transaction Type DB2 (`app/app-transaction-type-db2/jcl/`)

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|--------------|
| 1 | CREADB21.jcl | Create DB2 tables for transaction types | DB2 DDL execution |
| 2 | MNTTRDB2.jcl | Maintain transaction type DB2 tables | DB2 DML operations |
| 3 | TRANEXTR.jcl | Extract transaction types from DB2 | DB2 SELECT → flat file |

---

## 5. Summary Statistics

| Category | Count |
|----------|-------|
| Core Batch Programs | 15 |
| Core Online Programs | 17 |
| Authorization Sub-App Programs | 8 |
| Transaction Type DB2 Programs | 3 |
| VSAM-MQ Sub-App Programs | 2 |
| **Total COBOL Programs** | **45** |
| Core JCL Jobs | 36 |
| Sub-Application JCL Jobs | 8 |
| **Total JCL Jobs** | **44** |
| VSAM Datasets Managed | 9+ |
| BMS Maps | 18 |
