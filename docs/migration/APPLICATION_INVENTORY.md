# CardDemo Application Inventory — COBOL-to-Java Migration Reference

> **Purpose**: This document catalogs every program, copybook, JCL job, and data store in the CardDemo mainframe application. It is designed to be referenced by Devin when performing incremental COBOL-to-Java migration.

---

## Table of Contents

1. [Glossary of Key Terms](#1-glossary-of-key-terms)
2. [Program Inventory — `app/cbl/` (Main Application)](#2-program-inventory--appcbl-main-application)
3. [Program Inventory — Sub-Application: `app/app-authorization-ims-db2-mq/`](#3-program-inventory--sub-application-appapp-authorization-ims-db2-mq)
4. [Program Inventory — Sub-Application: `app/app-transaction-type-db2/`](#4-program-inventory--sub-application-appapp-transaction-type-db2)
5. [Program Inventory — Sub-Application: `app/app-vsam-mq/`](#5-program-inventory--sub-application-appapp-vsam-mq)
6. [Copybook Catalog — `app/cpy/`](#6-copybook-catalog--appcpy)
7. [JCL Job Catalog — `app/jcl/`](#7-jcl-job-catalog--appjcl)
8. [Data Store Summary](#8-data-store-summary)
9. [Batch Job Execution Flow](#9-batch-job-execution-flow-recommended-order)
10. [Notes for Migration](#10-notes-for-migration)

---

## 1. Glossary of Key Terms

| Term | Definition |
|------|-----------|
| **Batch Program** | A COBOL program that runs as a scheduled job (via JCL), processing files/data in bulk without user interaction. Equivalent to a Java batch job (e.g., Spring Batch). |
| **Online Program (CICS)** | A COBOL program that runs interactively inside a CICS transaction server, responding to terminal/user input in real time. Equivalent to a Java web application endpoint/controller. |
| **CICS (Customer Information Control System)** | IBM's online transaction processing (OLTP) middleware. Programs use EXEC CICS commands for screen I/O, file access, and program-to-program calls. Migration target: Spring Boot REST controllers or web UI. |
| **BMS (Basic Mapping Support)** | CICS screen definition maps (like HTML forms). Found in `app/bms/` and `app/cpy-bms/`. Migration target: frontend templates or REST API DTOs. |
| **VSAM (Virtual Storage Access Method)** | IBM's indexed file system used as the primary data store. Key types: KSDS (Key-Sequenced, like a B-tree indexed table), ESDS (Entry-Sequenced, append-only), RRDS (Relative Record). Migration target: relational database tables. |
| **Copybook (.cpy)** | Reusable COBOL data structure definitions included via COPY statement. Equivalent to Java shared POJOs/DTOs/entity classes. |
| **JCL (Job Control Language)** | Scripts that define batch job execution: which programs to run, which files to use, in what order. Equivalent to shell scripts, CI/CD pipelines, or Spring Batch job configurations. |
| **DB2** | IBM's relational database. Some programs use embedded SQL (EXEC SQL). Migration target: any RDBMS via JPA/JDBC. |
| **IMS (Information Management System)** | IBM's hierarchical database and transaction manager. Programs use DL/I calls. Migration target: relational DB with JPA. |
| **MQ (IBM MQ / WebSphere MQ)** | Message queuing middleware. Programs put/get messages from queues. Migration target: JMS, RabbitMQ, or Kafka. |
| **GDG (Generation Data Group)** | Versioned sequential datasets — each run creates a new "generation" like a rolling backup. Migration target: timestamped files or database versioning. |
| **COMMAREA** | Communication area passed between CICS programs, like a shared session/context DTO. |
| **DFHCOMMAREA / LINKAGE SECTION** | The mechanism for passing data between CICS programs. Equivalent to method parameters or session-scoped beans in Java. |
| **TRANSID** | A 4-character CICS transaction identifier that maps to a program. Like a URL route mapping. |
| **Alternate Index (AIX)** | A secondary index on a VSAM file, allowing access by a non-primary key. Like a database secondary index. |
| **IDCAMS** | IBM utility for defining/deleting/copying VSAM files. Used extensively in JCL setup jobs. |
| **SORT** | IBM utility for sorting/filtering flat files. Used in JCL for data transformation steps. |
| **Subroutine** | A COBOL program called by another via CALL statement. Equivalent to a Java service/utility class method. |
| **COMP / COMP-3** | Binary and packed-decimal numeric storage formats. Important for data conversion during migration. |

---

## 2. Program Inventory — `app/cbl/` (Main Application)

| Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|----------|---------|----------------|-------------------|----------------------|
| CBACT01C.cbl | Read account VSAM file and write to multiple output formats (flat, array, variable-length) | Batch | **Read**: ACCTFILE (VSAM KSDS). **Write**: OUTFILE (sequential), ARRYFILE (sequential), VBRCFILE (sequential VB) | CVACT01Y |
| CBACT02C.cbl | Read and print card data file | Batch | **Read**: CARDFILE (VSAM KSDS) | CVACT02Y |
| CBACT03C.cbl | Read and print account cross-reference data file | Batch | **Read**: XREFFILE (VSAM KSDS) | CVACT03Y |
| CBACT04C.cbl | Interest calculator — process transaction category balances, compute interest and fees | Batch | **Read**: TCATBALF (VSAM KSDS), XREFFILE (VSAM KSDS random), ACCTFILE (VSAM KSDS random), DISCGRP (VSAM KSDS random). **Write**: TRANSACT (sequential) | CVACT01Y, CVACT03Y, CVTRA05Y, CSUTLDPY, CSUTLDWY |
| CBCUS01C.cbl | Read and print customer data file | Batch | **Read**: CUSTFILE (VSAM KSDS) | CVCUS01Y |
| CBEXPORT.cbl | Export customer data for branch migration — reads all CardDemo files, creates multi-record indexed export file | Batch | **Read**: CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE (all VSAM KSDS). **Write**: EXPFILE (VSAM KSDS indexed) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVCRD01Y, CVEXPORT |
| CBIMPORT.cbl | Import customer data from export file — splits multi-record layout into normalized target files with validation | Batch | **Read**: EXPFILE (VSAM KSDS). **Write**: CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT (all sequential) | CVEXPORT |
| CBSTM03A.CBL | Print account statements from transaction data in plain text and HTML formats | Batch | **Read**: (via subroutine CBSTM03B: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE). **Write**: STMTFILE, HTMLFILE | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| CBSTM03B.CBL | Subroutine — file processing for transaction statement report (called by CBSTM03A) | Batch (Subroutine) | **Read**: TRNXFILE (VSAM KSDS), XREFFILE (VSAM KSDS), CUSTFILE (VSAM KSDS random), ACCTFILE (VSAM KSDS random) | CVACT03Y, CUSTREC, CVACT01Y |
| CBTRN01C.cbl | Post records from daily transaction file — validate and write to transaction master | Batch | **Read**: DALYTRAN (sequential), CUSTFILE (VSAM KSDS random), XREFFILE (VSAM KSDS random), CARDFILE (VSAM KSDS random), ACCTFILE (VSAM KSDS random). **Write**: TRANFILE (VSAM KSDS random) | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVTRA05Y |
| CBTRN02C.cbl | Process daily transactions — validate, post to transaction master, update account balances, maintain category balances, write rejects | Batch | **Read**: DALYTRAN (sequential), XREFFILE (VSAM KSDS random), ACCTFILE (VSAM KSDS random). **Write**: TRANFILE (VSAM KSDS random), DALYREJS (sequential), TCATBALF (VSAM KSDS random) | CVACT01Y, CVACT03Y, CVTRA05Y |
| CBTRN03C.cbl | Print transaction detail report — filter by date range, look up card cross-ref and transaction types | Batch | **Read**: TRANFILE (sequential), CARDXREF (VSAM KSDS random), TRANTYPE (VSAM KSDS random), TRANCATG (VSAM KSDS random), DATEPARM (sequential). **Write**: TRANREPT (sequential report) | CVTRA05Y, CVTRA06Y |
| COBSWAIT.cbl | Utility — wait for specified centiseconds (via MVSWAIT system call) | Batch (Utility) | **Read**: SYSIN (parm input). Calls MVSWAIT | None |
| CSUTLDTC.cbl | Date validation utility — calls LE CEEDAYS API to validate date strings | Utility (called by both batch and online) | Calls CEEDAYS API. Accepts date via LINKAGE SECTION | CSUTLDPY (implied via caller) |
| COSGN00C.cbl | Sign-on screen — authenticate users against USRSEC VSAM file | Online (CICS) | **Read**: USRSEC (CICS FILE READ). **Screen**: COSGN00 (BMS map) | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COMEN01C.cbl | Main menu for regular users — navigation to sub-functions | Online (CICS) | **Screen**: COMEN01 (BMS map). Navigates to other programs via XCTL | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COADM01C.cbl | Admin menu for admin users | Online (CICS) | **Screen**: COADM01 (BMS map). Navigates to admin sub-functions via XCTL | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COACTUPC.cbl | Account update — accept and process account modifications | Online (CICS) | **Read/Write**: ACCTDAT (CICS FILE READ/REWRITE), CCXREF, CXACAIX. **Screen**: COACTUPC BMS map | COCOM01Y, CVACT01Y, CVACT02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CSSETATY, DFHAID, DFHBMSCA, plus BMS copybooks |
| COACTVWC.cbl | Account view — display account details | Online (CICS) | **Read**: ACCTDAT, CCXREF, CXACAIX (CICS FILE READ). **Screen**: COACTVW BMS map | COCOM01Y, CVACT01Y, CVACT02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COBIL00C.cbl | Bill payment — pay account balance, create payment transaction | Online (CICS) | **Read/Write**: TRANSACT (CICS FILE READ/WRITE), ACCTDAT, CXACAIX. **Screen**: COBIL00 BMS map | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COCRDLIC.cbl | List credit cards — shows all cards (admin) or account-specific cards | Online (CICS) | **Read**: CARDDAT (CICS FILE BROWSE), CXACAIX, CARDAIX. **Screen**: COCRDLI BMS map | COCOM01Y, CVCRD01Y, CVACT03Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COCRDSLC.cbl | Credit card detail view — display card details | Online (CICS) | **Read**: CARDDAT, CCXREF (CICS FILE READ). **Screen**: COCRDSL BMS map | COCOM01Y, CVCRD01Y, CVACT03Y, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| COCRDUPC.cbl | Credit card update — modify card details | Online (CICS) | **Read/Write**: CARDDAT (CICS FILE READ/REWRITE), CCXREF. **Screen**: COCRDUP BMS map | COCOM01Y, CVCRD01Y, CVACT03Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA |
| CORPT00C.cbl | Submit transaction reports — triggers batch job from online via extra-partition TDQ | Online (CICS) | **Read**: TRANSACT (CICS FILE BROWSE). **Write**: TDQ (INTRDR - internal reader for batch job submission). **Screen**: CORPT00 BMS map | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COTRN00C.cbl | List transactions from TRANSACT file | Online (CICS) | **Read**: TRANSACT (CICS FILE BROWSE). **Screen**: COTRN00 BMS map | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COTRN01C.cbl | View a transaction detail | Online (CICS) | **Read**: TRANSACT (CICS FILE READ). **Screen**: COTRN01 BMS map | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COTRN02C.cbl | Add a new transaction | Online (CICS) | **Read/Write**: TRANSACT (CICS FILE WRITE), ACCTDAT, CCXREF, CXACAIX. **Screen**: COTRN02 BMS map. Calls CSUTLDTC | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COUSR00C.cbl | List all users from USRSEC file | Online (CICS) | **Read**: USRSEC (CICS FILE BROWSE). **Screen**: COUSR00 BMS map | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COUSR01C.cbl | Add a new user (Regular/Admin) to USRSEC file | Online (CICS) | **Write**: USRSEC (CICS FILE WRITE). **Screen**: COUSR01 BMS map | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COUSR02C.cbl | Update a user in USRSEC file | Online (CICS) | **Read/Write**: USRSEC (CICS FILE READ/REWRITE). **Screen**: COUSR02 BMS map | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COUSR03C.cbl | Delete a user from USRSEC file | Online (CICS) | **Read/Write**: USRSEC (CICS FILE READ/DELETE). **Screen**: COUSR03 BMS map | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |

---

## 3. Program Inventory — Sub-Application: `app/app-authorization-ims-db2-mq/`

| Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|----------|---------|----------------|-------------------|----------------------|
| CBPAUP0C.cbl | Delete expired pending authorization messages from IMS DB | Batch (IMS) | **IMS DB**: DL/I calls (GU, GN, DLET on PAUTHDBS). | CCPAURQY, CCPAURLY, PAUTBPCB, IMSFUNCS |
| COPAUA0C.cbl | Card authorization decision — reads MQ requests, checks card/account/customer, writes MQ replies | Online (CICS/IMS/MQ) | **MQ**: Request queue (GET), Reply queue (PUT). **CICS Files**: ACCTDAT, CUSTDAT, CARDDAT, CARDAIX, CCXREF. **IMS DB**: DL/I calls for auth records. | COCOM01Y, CCPAURQY, CCPAURLY, CCPAUERY, CVACT01Y, CVCRD01Y, CVCUS01Y, CVACT03Y, PAUTBPCB, IMSFUNCS |
| COPAUS0C.cbl | Summary view of authorization messages — browse IMS DB and display on BMS screen | Online (CICS/IMS/BMS) | **IMS DB**: DL/I calls (GU, GNP). **CICS Files**: ACCTDAT, CUSTDAT, CARDDAT, CXACAIX, CCXREF. **Screen**: CIPAUSM BMS map | COCOM01Y, CIPAUSMY, CVACT01Y, CVCRD01Y, CVCUS01Y, CVACT03Y, PAUTBPCB, PASFLPCB, IMSFUNCS, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COPAUS1C.cbl | Detail view of a single authorization message | Online (CICS/IMS/BMS) | **IMS DB**: DL/I calls (GU). **Screen**: CIPAUDT BMS map | COCOM01Y, CIPAUDTY, PAUTBPCB, PADFLPCB, IMSFUNCS, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COPAUS2C.cbl | Mark authorization message as fraud — updates IMS DB record and inserts DB2 fraud record | Online (CICS/IMS/DB2) | **IMS DB**: DL/I calls (GU, REPL). **DB2**: INSERT into fraud table. **Screen**: BMS map | COCOM01Y, PAUTBPCB, PADFLPCB, IMSFUNCS, DB2 DCLGEN (implied), COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| DBUNLDGS.CBL | IMS database unload using GSAM (Generalized Sequential Access Method) | Batch (IMS Utility) | **IMS DB**: DL/I GN calls. **Write**: Output files (commented out in source, likely dynamically allocated) | IMS PCBs |
| PAUDBLOD.CBL | Load IMS database from sequential input files | Batch (IMS Utility) | **Read**: INFILE1 (sequential), INFILE2 (sequential). **IMS DB**: DL/I ISRT calls | IMS PCBs |
| PAUDBUNL.CBL | Unload IMS database to sequential output files | Batch (IMS Utility) | **IMS DB**: DL/I GN calls. **Write**: OUTFIL1 (sequential), OUTFIL2 (sequential) | IMS PCBs |

### Sub-Application JCL Jobs (`app/app-authorization-ims-db2-mq/jcl/`)

| JCL Job | Purpose | Steps |
|---------|---------|-------|
| CBPAUP0J.jcl | Purge expired pending authorizations | Runs CBPAUP0C with IMS |
| DBPAUTP0.jcl | Define/setup IMS DB for pending authorizations | IDCAMS / IMS DB setup |
| LOADPADB.JCL | Load IMS authorization database from files | Runs PAUDBLOD |
| UNLDGSAM.JCL | Unload IMS DB via GSAM | Runs DBUNLDGS |
| UNLDPADB.JCL | Unload IMS authorization database to files | Runs PAUDBUNL |

---

## 4. Program Inventory — Sub-Application: `app/app-transaction-type-db2/`

| Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|----------|---------|----------------|-------------------|----------------------|
| COBTUPDT.cbl | Batch update of transaction types from input file — inserts/updates/deletes DB2 TRAN_TYPE table | Batch (DB2) | **Read**: INPFILE (sequential). **DB2**: SELECT, INSERT, UPDATE, DELETE on TRAN_TYPE table (EXEC SQL) | CSDB2RPY, CSDB2RWY (DB2 DCLGEN) |
| COTRTLIC.cbl | List transaction types with paging — demonstrates DB2 cursor paging, select/delete/update | Online (CICS/DB2) | **DB2**: DECLARE CURSOR, OPEN, FETCH, CLOSE on TRAN_TYPE. **Screen**: COTRTLI BMS map | COCOM01Y, CSDB2RPY, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COTRTUPC.cbl | Accept and process transaction type updates via DB2 | Online (CICS/DB2) | **DB2**: SELECT, UPDATE, INSERT, DELETE on TRAN_TYPE. **Screen**: COTRTUP BMS map | COCOM01Y, CSDB2RPY, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |

### Sub-Application JCL Jobs (`app/app-transaction-type-db2/jcl/`)

| JCL Job | Purpose | Steps |
|---------|---------|-------|
| CREADB21.jcl | Create DB2 TRAN_TYPE table and load initial data | DDL execution, data load |
| MNTTRDB2.jcl | Maintain transaction type DB2 data via batch | Runs COBTUPDT |
| TRANEXTR.jcl | Extract transaction type data from DB2 | DB2 unload/extract |

---

## 5. Program Inventory — Sub-Application: `app/app-vsam-mq/`

| Filename | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|----------|---------|----------------|-------------------|----------------------|
| COACCT01.cbl | CICS MQ account inquiry — reads account requests from MQ queue, processes via VSAM lookups, sends responses to reply queue | Online (CICS/MQ) | **MQ**: Request queue (MQGET), Reply queue (MQPUT), Error queue (MQPUT). **CICS Files**: ACCTDAT, CCXREF, CXACAIX | MQ copybooks (CMQV, etc.) |
| CODATE01.cbl | CICS MQ date service — reads date requests from MQ, returns formatted date responses | Online (CICS/MQ) | **MQ**: Request queue (MQGET), Reply queue (MQPUT), Error queue (MQPUT) | MQ copybooks |

---

## 6. Copybook Catalog — `app/cpy/`

| Copybook | Purpose |
|----------|---------|
| COADM02Y.cpy | Admin menu options data structure |
| COCOM01Y.cpy | Common communication area (COMMAREA) shared across all CICS programs |
| CODATECN.cpy | Date conversion routines/fields |
| COMEN02Y.cpy | Menu options data structure for regular users |
| COSTM01.CPY | Statement print working storage variables |
| COTTL01Y.cpy | Title/header line for screens |
| CSDAT01Y.cpy | Date working storage variables |
| CSLKPCDY.cpy | Lookup code data structure |
| CSMSG01Y.cpy | Message area for screens (error/info messages) |
| CSMSG02Y.cpy | Extended message area |
| CSSETATY.cpy | Screen attribute setting utility |
| CSSTRPFY.cpy | String processing functions |
| CSUSR01Y.cpy | User information data structure |
| CSUTLDPY.cpy | Date utility parameters (CSUTLDTC parms) |
| CSUTLDWY.cpy | Date utility working storage |
| CUSTREC.cpy | Customer record layout |
| CVACT01Y.cpy | Account record layout (FD for account VSAM) |
| CVACT02Y.cpy | Card record layout (FD for card VSAM) |
| CVACT03Y.cpy | Cross-reference record layout (FD for xref VSAM) |
| CVCRD01Y.cpy | Credit card detail data structure |
| CVCUS01Y.cpy | Customer detail data structure |
| CVEXPORT.cpy | Export file record layout (multi-record format) |
| CVTRA01Y.cpy | Transaction record layout variant 1 |
| CVTRA02Y.cpy | Transaction record layout variant 2 |
| CVTRA03Y.cpy | Transaction record layout variant 3 |
| CVTRA04Y.cpy | Transaction record layout variant 4 |
| CVTRA05Y.cpy | Transaction record layout (primary, used by most programs) |
| CVTRA06Y.cpy | Transaction report record layout |
| CVTRA07Y.cpy | Transaction record layout variant 7 |
| UNUSED1Y.cpy | Unused/placeholder copybook |

### Sub-Application Copybooks

**`app/app-authorization-ims-db2-mq/cpy/`**

| Copybook | Purpose |
|----------|---------|
| CCPAUERY.cpy | Authorization error response layout |
| CCPAURLY.cpy | Authorization reply layout |
| CCPAURQY.cpy | Authorization request layout |
| CIPAUDTY.cpy | Authorization detail BMS data structure |
| CIPAUSMY.cpy | Authorization summary BMS data structure |
| IMSFUNCS.cpy | IMS DL/I function codes |
| PADFLPCB.cpy | IMS PCB definition (detail) |
| PASFLPCB.cpy | IMS PCB definition (summary) |
| PAUTBPCB.cpy | IMS PCB definition (authorization base) |

**`app/app-transaction-type-db2/cpy/`**

| Copybook | Purpose |
|----------|---------|
| CSDB2RPY.cpy | DB2 read-only DCLGEN |
| CSDB2RWY.cpy | DB2 read-write DCLGEN |

---

## 7. JCL Job Catalog — `app/jcl/`

| JCL Job | Purpose | Step Sequence |
|---------|---------|---------------|
| ACCTFILE.jcl | Delete/define/load Account VSAM KSDS | STEP05: IDCAMS DELETE cluster → STEP10: IDCAMS DEFINE cluster (KEYS 11 0, RECSIZE 300) → STEP15: IDCAMS REPRO from ACCTDATA.PS to VSAM |
| CARDFILE.jcl | Delete/define/load Card Data VSAM KSDS + AIX | CLCIFIL: Close CICS files → STEP05: IDCAMS DELETE cluster+AIX → STEP10: IDCAMS DEFINE cluster (KEYS 16 0, RECSIZE 150) → STEP15: IDCAMS REPRO from PS → STEP40: DEFINE AIX (KEYS 11 16, acct-id) → STEP50: DEFINE PATH → STEP60: BLDINDEX → OPCIFIL: Open CICS files |
| CBADMCDJ.jcl | Create CICS CSD resource definitions for CardDemo | STEP1: DFHCSDUP — defines all programs, transactions, mapsets, files, TDQueues for the CardDemo CICS application |
| CBEXPORT.jcl | Export CardDemo data for branch migration | STEP01: IDCAMS DELETE/DEFINE export VSAM cluster → STEP02: Run CBEXPORT (reads CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE; writes EXPFILE) |
| CBIMPORT.jcl | Import CardDemo data from export file | STEP01: Run CBIMPORT (reads EXPFILE; writes CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, ERROUT) |
| CLOSEFIL.jcl | Close CICS files | CLCIFIL: SDSF CEMT SET FIL CLO for TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC |
| COMBTRAN.jcl | Combine transaction backup + system-generated transactions, reload to master | STEP05R: SORT/merge TRANSACT.BKUP(0) + SYSTRAN(0) → STEP10: IDCAMS REPRO combined file to TRANSACT.VSAM.KSDS |
| CREASTMT.JCL | Create account statements (text + HTML) | DELDEF01: IDCAMS delete/define TRXFL work file → STEP010: SORT transact by card+tran-id → STEP020: REPRO to VSAM → STEP030: Delete old reports → STEP040: Run CBSTM03A (reads TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE; writes STMTFILE, HTMLFILE) |
| CUSTFILE.jcl | Delete/define/load Customer VSAM KSDS | CLCIFIL: Close CICS → STEP05: DELETE → STEP10: DEFINE (KEYS 9 0, RECSIZE 500) → STEP15: REPRO from PS → OPCIFIL: Open CICS |
| DALYREJS.jcl | Define GDG base for daily rejects | STEP05: IDCAMS DEFINE GDG (DALYREJS, LIMIT 5) |
| DEFCUST.jcl | Define Customer VSAM cluster (alternate site) | STEP05: DELETE → STEP05: DEFINE CLUSTER |
| DEFGDGB.jcl | Define all GDG bases for CardDemo | STEP05: IDCAMS defines GDGs: TRANSACT.BKUP, TRANSACT.DALY, TRANREPT, TCATBALF.BKUP, SYSTRAN, TRANSACT.COMBINED |
| DEFGDGD.jcl | Define GDGs for DB2-related reference data + first gen backups | STEP10: GDG TRANTYPE.BKUP → STEP20: IEBGENER first gen → STEP30: GDG TRANCATG.PS.BKUP → STEP40: first gen → STEP50: GDG DISCGRP.BKUP → STEP60: first gen |
| DISCGRP.jcl | Delete/define/load Disclosure Group VSAM KSDS | STEP05: DELETE → STEP10: DEFINE (KEYS 16 0, RECSIZE 50) → STEP15: REPRO from PS |
| DUSRSECJ.jcl | Create User Security VSAM file with initial data | PREDEL: Delete old PS → STEP01: IEBGENER create PS from instream (10 users) → STEP02: IDCAMS DEFINE VSAM KSDS (KEYS 8 0, RECSIZE 80) → STEP03: REPRO PS to VSAM |
| ESDSRRDS.jcl | Define ESDS and RRDS VSAM files for user security (demo of VSAM types) | PREDEL → STEP01: Create PS → STEP02: DEFINE ESDS → STEP03: REPRO to ESDS → STEP04: DEFINE RRDS → STEP05: REPRO to RRDS |
| FTPJCL.JCL | FTP job to send/receive files | STEP1: FTP PUT mainframe file to remote server |
| INTCALC.jcl | Run interest calculator batch | STEP15: Run CBACT04C with PARM date (reads TCATBALF, XREFFILE, ACCTFILE, DISCGRP; writes TRANSACT/SYSTRAN GDG) |
| INTRDRJ1.JCL | Internal reader job — triggers INTRDRJ2 | IDCAMS: REPRO backup → STEP01: IEBGENER submits INTRDRJ2 via internal reader |
| INTRDRJ2.JCL | Second-stage internal reader job | IDCAMS: REPRO backup copy |
| OPENFIL.jcl | Open CICS files | OPCIFIL: SDSF CEMT SET FIL OPE for TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC |
| POSTTRAN.jcl | Process and load daily transactions | STEP15: Run CBTRN02C (reads DALYTRAN, XREFFILE, ACCTFILE; writes TRANFILE, DALYREJS GDG, TCATBALF) |
| PRTCATBL.jcl | Print transaction category balance report | DELDEF → STEP05R: REPRO TCATBALF to backup → STEP10R: SORT and format for report output |
| READACCT.jcl | Read and display account data | PREDEL: Delete old output → STEP05: Run CBACT01C (reads ACCTFILE; writes OUTFILE, ARRYFILE, VBRCFILE) |
| READCARD.jcl | Read and display card data | STEP05: Run CBACT02C (reads CARDFILE) |
| READCUST.jcl | Read and display customer data | STEP05: Run CBCUS01C (reads CUSTFILE) |
| READXREF.jcl | Read and display cross-reference data | STEP05: Run CBACT03C (reads XREFFILE) |
| REPTFILE.jcl | Define GDG base for transaction reports | STEP05: IDCAMS DEFINE GDG (TRANREPT, LIMIT 10) |
| TCATBALF.jcl | Delete/define/load Transaction Category Balance VSAM | STEP05: DELETE → STEP10: DEFINE (KEYS 17 0, RECSIZE 50) → STEP15: REPRO from PS |
| TRANBKP.jcl | Backup and recreate transaction master VSAM | STEP05R: REPRO to backup GDG → STEP05: DELETE cluster+AIX → STEP10: DEFINE new cluster |
| TRANCATG.jcl | Delete/define/load Transaction Category VSAM | STEP05: DELETE → STEP10: DEFINE (KEYS 6 0, RECSIZE 60) → STEP15: REPRO from PS |
| TRANFILE.jcl | Delete/define/load Transaction Master VSAM + AIX | CLCIFIL: Close CICS → STEP05: DELETE cluster+AIX → STEP10: DEFINE (KEYS 16 0, RECSIZE 350) → STEP15: REPRO → STEP20: DEFINE AIX (KEYS 26 304, timestamp) → STEP25: DEFINE PATH → STEP30: BLDINDEX → OPCIFIL: Open CICS |
| TRANIDX.jcl | Define alternate index on transaction master | STEP20: DEFINE AIX → STEP25: DEFINE PATH → STEP30: BLDINDEX |
| TRANREPT.jcl | Generate transaction detail report | STEP05R: REPRO transact to backup → STEP05R: SORT/filter by date range → STEP10R: Run CBTRN03C (reads filtered TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM; writes TRANREPT GDG) |
| TRANTYPE.jcl | Delete/define/load Transaction Type VSAM | STEP05: DELETE → STEP10: DEFINE (KEYS 2 0, RECSIZE 60) → STEP15: REPRO from PS |
| TXT2PDF1.JCL | Convert text statement to PDF | TXT2PDF: Run IKJEFT1B with TXT2PDF REXX exec |
| WAITSTEP.jcl | Wait utility job | WAIT: Run COBSWAIT with centiseconds parm (SYSIN: 00003600 = 36 seconds) |
| XREFFILE.jcl | Delete/define/load Card Cross-Reference VSAM + AIX | STEP05: DELETE cluster+AIX → STEP10: DEFINE (KEYS 16 0, RECSIZE 50) → STEP15: REPRO → STEP20: DEFINE AIX (KEYS 11 25, acct-id) → STEP25: DEFINE PATH → STEP30: BLDINDEX |

---

## 8. Data Store Summary

| Logical Name | VSAM Dataset | Key | Record Size | Used By |
|-------------|-------------|-----|------------|---------|
| ACCTFILE/ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | 11 bytes @ offset 0 (Account ID) | 300 | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03B, CBEXPORT, COACTUPC, COACTVWC, COBIL00C, COTRN02C |
| CARDFILE/CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | 16 bytes @ 0 (Card Number) | 150 | CBACT02C, CBTRN01C, CBEXPORT, COCRDLIC, COCRDSLC, COCRDUPC |
| CUSTFILE/CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | 9 bytes @ 0 (Customer ID) | 500 | CBCUS01C, CBTRN01C, CBSTM03B, CBEXPORT, COSGN00C |
| XREFFILE/CCXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | 16 bytes @ 0 (Card Number) | 50 | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT, COACTUPC, COACTVWC, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COTRN02C |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | 16 bytes @ 0 (Transaction ID) | 350 | CBTRN02C, CBEXPORT, COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | 8 bytes @ 0 (User ID) | 80 | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| TCATBALF | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | 17 bytes @ 0 (Acct+Type+Cat) | 50 | CBACT04C, CBTRN02C |
| DISCGRP | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | 16 bytes @ 0 | 50 | CBACT04C |
| TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | 2 bytes @ 0 | 60 | CBTRN03C |
| TRANCATG | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | 6 bytes @ 0 | 60 | CBTRN03C |

---

## 9. Batch Job Execution Flow (Recommended Order)

The typical nightly batch cycle runs in this sequence:

| Step | JCL Job | Action |
|------|---------|--------|
| 1 | CLOSEFIL | Close CICS files |
| 2 | TRANBKP | Backup transaction master |
| 3 | POSTTRAN | Post daily transactions (CBTRN02C) |
| 4 | INTCALC | Calculate interest (CBACT04C) |
| 5 | COMBTRAN | Combine transactions + system-generated |
| 6 | TRANREPT | Generate transaction report (CBTRN03C) |
| 7 | CREASTMT | Create account statements (CBSTM03A/B) |
| 8 | OPENFIL | Reopen CICS files |

---

## 10. Notes for Migration

- The `CO*` prefix programs are CICS online programs; `CB*` prefix are batch programs; `CS*` are shared utilities.
- All online programs use COMMAREA (COCOM01Y) for inter-program communication — this maps to session/request scope in Java.
- CICS programs use pseudo-conversational pattern (RETURN TRANSID) — each user interaction is a separate task. This maps naturally to stateless REST endpoints.
- The VSAM files map directly to relational database tables. The copybook record layouts define the column structures.
- CBSTM03A calls CBSTM03B as a subroutine — these should be migrated as a single Java service with the subroutine becoming a private method or separate service class.
- CORPT00C demonstrates online-to-batch bridging via internal reader TDQ — in Java, this maps to async job submission (e.g., Spring Batch JobLauncher).
- The sub-applications add IMS (hierarchical DB), DB2 (relational), and MQ (messaging) — each requires specific migration patterns.
- COMP and COMP-3 fields require careful numeric conversion during migration.
- BMS maps (in `app/bms/` and `app/cpy-bms/`) define screen layouts — these map to REST API request/response DTOs or frontend templates.
