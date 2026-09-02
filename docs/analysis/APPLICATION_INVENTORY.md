# CardDemo COBOL Estate — Application Inventory

Static analysis of every COBOL program and JCL job under `app/`. All filenames, DD names, dataset names, copybook names and step sequences below were taken from the source files; purpose text is **inferred** from each program's header comment block and `PROCEDURE DIVISION` unless quoted.

Scope analysed:

| Area | Directory | Count |
|---|---|---|
| Core programs | `app/cbl/` | 31 (`ls app/cbl` — the brief said 30; `CSUTLDTC.cbl` and `COBSWAIT.cbl` are utility subroutines) |
| Core copybooks | `app/cpy/` | 30 |
| BMS symbolic copybooks / maps | `app/cpy-bms/`, `app/bms/` | 17 / 17 |
| Core JCL / PROCs / ASM | `app/jcl/`, `app/proc/`, `app/asm/` | 37 / 2 / 2 |
| Authorization sub-app (IMS/DB2/MQ) | `app/app-authorization-ims-db2-mq/` | 8 programs, 10 copybooks, 5 JCL, 1 DCL, 2 DDL, IMS DBD/PSB, CSD |
| Transaction-type sub-app (DB2) | `app/app-transaction-type-db2/` | 3 programs, 2 copybooks, 3 JCL, 2 DCL, 4 DDL, CTL, CSD |
| VSAM/MQ sub-app | `app/app-vsam-mq/` | 2 programs, CSD |
| Scheduler exports | `app/scheduler/` | `CardDemo.controlm` (Control-M, 4 folders), `CardDemo.ca7` (CA-7 LJOB listings) — chains documented in `DEPENDENCY_MAP.md` §4.1 |

**Total: 44 COBOL programs, 42 copybooks (excl. BMS), 45 JCL jobs + 2 PROCs.**

Classification rules applied (as requested): *online/CICS* = uses `EXEC CICS` (all `CO*` programs, some without map SEND/RECEIVE are noted as "CICS, no BMS"); *batch* = `CB*` programs and programs using `SELECT … ASSIGN`; IMS batch programs invoked under `DFSRRC00` are marked *batch/IMS*.

Runtime file names for CICS `DATASET(...)` operations are resolved from the `WORKING-STORAGE` literals in each program (e.g. `WS-ACCTDAT-FILE PIC X(08) VALUE 'ACCTDAT '`). CICS logical files in the estate: `ACCTDAT`, `CARDDAT`, `CARDAIX`, `CXACAIX`, `CUSTDAT`, `CCXREF`, `TRANSACT`, `USRSEC`.

---

## 1. Program inventory

### 1.1 `app/cbl/` — core application (31 programs)

| File | Inferred purpose (from header + PROCEDURE DIVISION) | Class | Key I/O | Copybooks (`COPY`) |
|---|---|---|---|---|
| `CBACT01C.cbl` | Header: "READ THE ACCOUNT FILE AND WRITE INTO FILES." Sequentially reads the account KSDS and writes three variants (fixed, array, variable-block) of the record; calls `COBDATFT` (asm) for date formatting. | Batch | `SELECT` ACCTFILE-FILE→`ACCTFILE` (in), OUT-FILE→`OUTFILE`, ARRY-FILE→`ARRYFILE`, VBRC-FILE→`VBRCFILE` (out) | `CODATECN`, `CVACT01Y` |
| `CBACT02C.cbl` | "Read and print card data file." Sequential dump of card KSDS to SYSOUT. | Batch | `SELECT` CARDFILE-FILE→`CARDFILE` (in) | `CVACT02Y` |
| `CBACT03C.cbl` | "Read and print account cross reference data file." | Batch | `SELECT` XREFFILE-FILE→`XREFFILE` (in) | `CVACT03Y` |
| `CBACT04C.cbl` | "This is a interest calculator program." For each `TCATBALF` record: looks up xref→account, reads disclosure group rate (falls back to group `'DEFAULT'`), `COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200`, writes an interest transaction, then `ADD WS-TOTAL-INT TO ACCT-CURR-BAL` and rewrites the account. `1400-COMPUTE-FEES` is a stub ("To be implemented"). | Batch | `SELECT` TCATBAL-FILE→`TCATBALF` (in, seq), XREF-FILE→`XREFFILE` (in), ACCOUNT-FILE→`ACCTFILE` (in/rewrite), DISCGRP-FILE→`DISCGRP` (in), TRANSACT-FILE→`TRANSACT` (out) | `CVACT01Y`, `CVACT03Y`, `CVTRA01Y`, `CVTRA02Y`, `CVTRA05Y` |
| `CBCUS01C.cbl` | "Read and print customer data file." | Batch | `SELECT` CUSTFILE-FILE→`CUSTFILE` (in) | `CVCUS01Y` |
| `CBEXPORT.cbl` | "Export Customer Data for Branch Migration" — reads 5 master files and writes a polymorphic export record (`EXPORT-REC-TYPE` = C/A/X/T/D). | Batch | `SELECT` CUSTOMER-INPUT→`CUSTFILE`, ACCOUNT-INPUT→`ACCTFILE`, XREF-INPUT→`XREFFILE`, TRANSACTION-INPUT→`TRANSACT`, CARD-INPUT→`CARDFILE` (in); EXPORT-OUTPUT→`EXPFILE` (out) | `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVEXPORT`, `CVTRA05Y` |
| `CBIMPORT.cbl` | "Import Customer Data from Branch Migration Export" — splits export file back into 5 output files plus an error file. | Batch | `SELECT` EXPORT-INPUT→`EXPFILE` (in); CUSTOMER-OUTPUT→`CUSTOUT`, ACCOUNT-OUTPUT→`ACCTOUT`, XREF-OUTPUT→`XREFOUT`, TRANSACTION-OUTPUT→`TRNXOUT`, CARD-OUTPUT→`CARDOUT`, ERROR-OUTPUT→`ERROUT` (out) | `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVEXPORT`, `CVTRA05Y` |
| `CBSTM03A.CBL` | "Print Account Statements from Transaction data in two formats: 1/plain text and 2/HTML". Drives `CBSTM03B` via `CALL 'CBSTM03B'` for all record I/O; builds per-account statements. | Batch | `SELECT` STMT-FILE→`STMTFILE`, HTML-FILE→`HTMLFILE` (out); TRNXFILE/XREFFILE/CUSTFILE/ACCTFILE read indirectly via `CBSTM03B` | `COSTM01`, `CUSTREC`, `CVACT01Y`, `CVACT03Y` |
| `CBSTM03B.CBL` | "Does file processing related to Transact Report" — subroutine: OPEN/READ/CLOSE dispatcher over 4 files driven by a caller-supplied request area. | Batch (subroutine) | `SELECT` TRNX-FILE→`TRNXFILE`, XREF-FILE→`XREFFILE`, CUST-FILE→`CUSTFILE`, ACCT-FILE→`ACCTFILE` (in) | none |
| `CBTRN01C.cbl` | "Post the records from daily transaction file." Earlier/simpler posting variant: for each `DALYTRAN` record looks up xref, then account; no balance update logic. *Inferred*: superseded by `CBTRN02C` (not referenced by any JCL in the repo). | Batch | `SELECT` DALYTRAN-FILE→`DALYTRAN`, CUSTOMER-FILE→`CUSTFILE`, XREF-FILE→`XREFFILE`, CARD-FILE→`CARDFILE`, ACCOUNT-FILE→`ACCTFILE`, TRANSACT-FILE→`TRANFILE` | `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVTRA05Y`, `CVTRA06Y` |
| `CBTRN02C.cbl` | "Post the records from daily transaction file." Validates each daily transaction (card in xref → reason 100; account exists → 101; over-limit via `ACCT-CREDIT-LIMIT` vs `ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + amount` → 102; after expiry → 103), writes rejects with reason to `DALYREJS`, otherwise writes `TRANFILE`, updates/creates `TCATBALF` category balance (`ADD DALYTRAN-AMT TO TRAN-CAT-BAL`), and updates `ACCT-CURR-BAL`, `ACCT-CURR-CYC-CREDIT`/`-DEBIT`. | Batch | `SELECT` DALYTRAN-FILE→`DALYTRAN` (in), TRANSACT-FILE→`TRANFILE` (out), XREF-FILE→`XREFFILE` (in), DALYREJS-FILE→`DALYREJS` (out), ACCOUNT-FILE→`ACCTFILE` (in/rewrite), TCATBAL-FILE→`TCATBALF` (read/write/rewrite) | `CVACT01Y`, `CVACT03Y`, `CVTRA01Y`, `CVTRA05Y`, `CVTRA06Y` |
| `CBTRN03C.cbl` | "Print the transaction detail report." Reads a date-range parm, walks sorted transactions, resolves type/category descriptions, prints page/account/grand totals using `CVTRA07Y` print layouts. | Batch | `SELECT` TRANSACT-FILE→`TRANFILE`, XREF-FILE→`CARDXREF`, TRANTYPE-FILE→`TRANTYPE`, TRANCATG-FILE→`TRANCATG`, DATE-PARMS-FILE→`DATEPARM` (in); REPORT-FILE→`TRANREPT` (out) | `CVACT03Y`, `CVTRA03Y`, `CVTRA04Y`, `CVTRA05Y`, `CVTRA07Y` |
| `COACTUPC.cbl` | "Accept and process ACCOUNT UPDATE" — 4,236-line screen program: field-by-field edit of account + customer data (uses `CSLKPCDY` state/zip/area-code tables and `CSUTLDPY` date edits), optimistic re-read before `REWRITE` of both records. | Online/CICS | `EXEC CICS READ` `ACCTDAT`, `CXACAIX`, `CUSTDAT` (via `LIT-*` literals); `REWRITE` `ACCTDAT`, `CUSTDAT`; SEND/RECEIVE MAP `COACTUP` | `COACTUP`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSLKPCDY`, `CSMSG01Y`, `CSMSG02Y`, `CSSETATY`, `CSUSR01Y`, `CSUTLDPY`, `CVACT01Y`, `CVACT03Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COACTVWC.cbl` | "Accept and process Account View request" — reads xref by account (AIX path), then account and customer, displays. | Online/CICS | `READ` `ACCTDAT`, `CXACAIX`, `CUSTDAT`; map `COACTVW` | `COACTVW`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COADM01C.cbl` | "Admin Menu for Admin users" — renders options from `COADM02Y` table and `XCTL`s to `CDEMO-ADMIN-OPT-PGMNAME(idx)`. | Online/CICS | map `COADM01`; no file I/O | `COADM01`, `COADM02Y`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COBIL00C.cbl` | "Bill Payment - Pay account balance in full" — reads account, browses `TRANSACT` backwards to derive next transaction id, writes a payment transaction, rewrites account with zeroed balance. | Online/CICS | `READ`/`REWRITE` `ACCTDAT`; `READ` `CXACAIX`; `STARTBR`/`READPREV`/`ENDBR`/`WRITE` `TRANSACT`; map `COBIL00` | `COBIL00`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVACT01Y`, `CVACT03Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COBSWAIT.cbl` | "UTILITY PROGRAM TO WAIT (PARM IN CENTISECONDS)" — thin wrapper calling asm `MVSWAIT`. | Batch (utility) | none | none |
| `COCRDLIC.cbl` | "List Credit Cards" — paged browse of `CARDDAT` (all cards for admin, account-filtered otherwise); selection routes to detail/update. | Online/CICS | `STARTBR`/`READNEXT`/`READPREV`/`ENDBR` `CARDDAT`; map `COCRDLI` | `COCOM01Y`, `COCRDLI`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `DFHAID`, `DFHBMSCA` |
| `COCRDSLC.cbl` | "Accept and process credit card detail request" — read-only card view by card number or account AIX. | Online/CICS | `READ` `CARDDAT`, `CARDAIX`; map `COCRDSL` | `COCOM01Y`, `COCRDSL`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COCRDUPC.cbl` | Header says "credit card detail request" but PROCEDURE DIVISION edits name/status/expiry and `REWRITE`s the card — *inferred*: card update. | Online/CICS | `READ`/`REWRITE` `CARDDAT`; map `COCRDUP` | `COCOM01Y`, `COCRDUP`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COMEN01C.cbl` | "Main Menu for the Regular users" — renders `COMEN02Y` options, `XCTL`s to `CDEMO-MENU-OPT-PGMNAME(idx)`. | Online/CICS | map `COMEN01`; no file I/O | `COCOM01Y`, `COMEN01`, `COMEN02Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `CORPT00C.cbl` | "Print Transaction reports by submitting batch" — validates date range (calls `CSUTLDTC`), builds `TRANREPT` JCL in memory and writes it to the internal reader via a CICS TDQ. | Online/CICS | `EXEC CICS WRITEQ TD` (JOBS/intrdr); map `CORPT00` | `COCOM01Y`, `CORPT00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COSGN00C.cbl` | "Signon Screen for the CardDemo Application" — reads `USRSEC` by user id, compares plain-text password, `XCTL` to literal `COADM01C` (type A) or `COMEN01C` (type U). | Online/CICS | `READ` `USRSEC`; map `COSGN00` | `COCOM01Y`, `COSGN00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COTRN00C.cbl` | "List Transactions from TRANSACT file" — paged browse. | Online/CICS | `STARTBR`/`READNEXT`/`READPREV`/`ENDBR` `TRANSACT`; map `COTRN00` | `COCOM01Y`, `COTRN00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COTRN01C.cbl` | "View a Transaction from TRANSACT file". | Online/CICS | `READ` `TRANSACT`; map `COTRN01` | `COCOM01Y`, `COTRN01`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COTRN02C.cbl` | "Add a new Transaction to TRANSACT file" — validates account/card via xref, dates via `CSUTLDTC`, derives next id with `READPREV`, `WRITE`s. | Online/CICS | `READ` `CCXREF`, `CXACAIX`; `STARTBR`/`READPREV`/`ENDBR`/`WRITE` `TRANSACT`; map `COTRN02` | `COCOM01Y`, `COTRN02`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVACT01Y`, `CVACT03Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR00C.cbl` | "List all users from USRSEC file" — paged browse. | Online/CICS | `STARTBR`/`READNEXT`/`READPREV`/`ENDBR` `USRSEC`; map `COUSR00` | `COCOM01Y`, `COTTL01Y`, `COUSR00`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR01C.cbl` | "Add a new Regular/Admin user to USRSEC file". | Online/CICS | `WRITE` `USRSEC`; map `COUSR01` | `COCOM01Y`, `COTTL01Y`, `COUSR01`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR02C.cbl` | "Update a user in USRSEC file". | Online/CICS | `READ`/`REWRITE` `USRSEC`; map `COUSR02` | `COCOM01Y`, `COTTL01Y`, `COUSR02`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR03C.cbl` | "Delete a user from USRSEC file". | Online/CICS | `READ`/`DELETE` `USRSEC`; map `COUSR03` | `COCOM01Y`, `COTTL01Y`, `COUSR03`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `CSUTLDTC.cbl` | Date-validation subroutine: "CALL TO CEEDAYS" — converts a date string + mask through LE `CEEDAYS`, returns severity/message in the `WS-DATE-VALIDATION-RESULT` layout. Called by `CORPT00C`, `COTRN02C`, and `CSUTLDPY` (copied into `COACTUPC`). | Callable subroutine (no I/O) | none | none |

### 1.2 `app/app-authorization-ims-db2-mq/cbl/` — authorization module (8 programs)

| File | Inferred purpose | Class | Key I/O | Copybooks |
|---|---|---|---|---|
| `CBPAUP0C.cbl` | Header: "BATCH COBOL IMS Program — Delete Expired Pending Authoriation Messages". Walks `PAUTSUM0` roots and deletes/aggregates expired `PAUTDTL1` children (run as BMP `PSBPAUTB` via `CBPAUP0J.jcl`). | Batch/IMS | `EXEC DLI` GU/GN/GNP/DLET on segments `PAUTSUM0`, `PAUTDTL1` | `CIPAUDTY`, `CIPAUSMY` |
| `COPAUA0C.cbl` | "CICS COBOL IMS MQ Program — Card Authorization Decision Program". MQ-triggered (`TRANSID CP00`): `MQGET` request, reads xref/account/customer VSAM, reads/inserts IMS summary+detail, applies approve/decline rules (credit limit, expiry, status, fraud), `MQPUT1` reply. | Online/CICS (no BMS) | `MQOPEN`/`MQGET`/`MQPUT1`/`MQCLOSE`; `EXEC CICS READ` `CCXREF`, `ACCTDAT`, `CUSTDAT`; `EXEC DLI` on `PAUTSUM0`, `PAUTDTL1` | `CCPAUERY`, `CCPAURLY`, `CCPAURQY`, `CIPAUDTY`, `CIPAUSMY`, `CMQGMOV`, `CMQMDV`, `CMQODV`, `CMQPMOV`, `CMQTML`, `CMQV`, `CVACT01Y`, `CVACT03Y`, `CVCUS01Y` |
| `COPAUS0C.cbl` | "Summary View of Authoriation Messages" (transaction `CPVS`) — reads account/customer, lists pending auths from IMS, selection → `COPAUS1C`. | Online/CICS | `READ` `ACCTDAT`, `CXACAIX`, `CUSTDAT`; `EXEC DLI` `PAUTSUM0`, `PAUTDTL1`; map `COPAU00` | `CIPAUDTY`, `CIPAUSMY`, `COCOM01Y`, `COPAU00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COPAUS1C.cbl` | "Detail View of Authorization Message" (`CPVD`) — shows one `PAUTDTL1`; PF key marks fraud via `EXEC CICS LINK PROGRAM(WS-PGM-AUTH-FRAUD)` = `COPAUS2C`. | Online/CICS | `EXEC DLI` GU/REPL on `PAUTSUM0`, `PAUTDTL1`; map `COPAU01` | `CIPAUDTY`, `CIPAUSMY`, `COCOM01Y`, `COPAU01`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `DFHAID`, `DFHBMSCA` |
| `COPAUS2C.cbl` | "Mark Authorization Message Fraud" — LINKed; `INSERT INTO CARDDEMO.AUTHFRDS` or `UPDATE CARDDEMO.AUTHFRDS` from the passed `PA-AUTH-DETAILS`. | Online/CICS (no BMS) | `EXEC SQL INCLUDE AUTHFRDS`; INSERT/UPDATE `CARDDEMO.AUTHFRDS` | `CIPAUDTY` (+ SQL INCLUDE `AUTHFRDS`) |
| `DBUNLDGS.CBL` | IMS batch: unload `PAUTHDB` root/child segments to GSAM files via `CALL 'CBLTDLI'` (PSB `DLIGSAMP`). | Batch/IMS | `CBLTDLI` on PCBs `PAUTBPCB`, `PASFLPCB`, `PADFLPCB` | `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, `PADFLPCB`, `PASFLPCB`, `PAUTBPCB` |
| `PAUDBLOD.CBL` | IMS batch load: reads two sequential files and `ISRT`s `PAUTSUM0`/`PAUTDTL1` (`LOADPADB.JCL`). | Batch/IMS | `SELECT` INFILE1→`INFILE1`, INFILE2→`INFILE2`; `CBLTDLI` ISRT | `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, `PAUTBPCB` |
| `PAUDBUNL.CBL` | IMS batch unload to flat files (`UNLDPADB.JCL`). | Batch/IMS | `SELECT` OPFILE1→`OUTFIL1`, OPFILE2→`OUTFIL2` (out); `CBLTDLI` GN | `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, `PAUTBPCB` |

IMS database: `ims/` holds the DBD/PSB for `PAUTHDB` (root `PAUTSUM0`, child `PAUTDTL1`; PSBs `PSBPAUTB`, `DLIGSAMP`, `PAUTBUNL` per JCL PARMs). DB2: `dcl/AUTHFRDS.dcl` declares `CARDDEMO.AUTHFRDS` (26 columns incl. `TRANSACTION_AMT DECIMAL(12,2)`, `AUTH_TS TIMESTAMP`, PK `(CARD_NUM, AUTH_TS)` per `ddl/AUTHFRDS.ddl`; unique index `ddl/XAUTHFRD.ddl`).

### 1.3 `app/app-transaction-type-db2/cbl/` — transaction-type module (3 programs)

| File | Inferred purpose | Class | Key I/O | Copybooks |
|---|---|---|---|---|
| `COBTUPDT.cbl` | "Update Transaction type based on user input" — batch reads control records (`A`dd/`U`pdate/`D`elete) and applies SQL to `CARDDEMO.TRANSACTION_TYPE` (run via `IKJEFT01 … RUN PROGRAM(COBTUPDT) PLAN(CARDDEMO)` in `MNTTRDB2.jcl`). | Batch/DB2 | `SELECT` TR-RECORD→`INPFILE`; `EXEC SQL` INSERT/UPDATE/DELETE `CARDDEMO.TRANSACTION_TYPE` | none (SQL `INCLUDE SQLCA`, `DCLTRTYP`) |
| `COTRTLIC.cbl` | "List Transaction Type for updates and deletes — Demonstrates paging with cursors in Db2" (`CTLI`). Forward/backward cursors over `CARDDEMO.TRANSACTION_TYPE`; in-place UPDATE/DELETE; `XCTL` to `LIT-ADDTPGM`=`COTRTUPC` or `LIT-ADMINPGM`=`COADM01C`. | Online/CICS+DB2 | 16 `EXEC SQL` (DECLARE CURSOR/OPEN/FETCH/CLOSE/UPDATE/DELETE) on `CARDDEMO.TRANSACTION_TYPE`; map `COTRTLI` | `COCOM01Y`, `COTRTLI`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `DFHAID`, `DFHBMSCA` (+ `CSDB2RWY`/`CSDB2RPY` via SQL INCLUDE/COPY) |
| `COTRTUPC.cbl` | "Accept and process TRANSACTION TYPE UPDATE" (`CTTU`) — add/update a type row. | Online/CICS+DB2 | 7 `EXEC SQL` SELECT/INSERT/UPDATE `CARDDEMO.TRANSACTION_TYPE`; map `COTRTUP` | `COCOM01Y`, `COTRTUP`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSSETATY`, `CSUSR01Y`, `CVCRD01Y`, `DFHAID`, `DFHBMSCA` |

DB2 objects: `dcl/DCLTRTYP.dcl` → `CARDDEMO.TRANSACTION_TYPE (TR_TYPE CHAR(2) NOT NULL, TR_DESCRIPTION VARCHAR(50) NOT NULL)` PK `TR_TYPE`; `dcl/DCLTRCAT.dcl` → `CARDDEMO.TRANSACTION_TYPE_CATEGORY (TRC_TYPE_CODE CHAR(2), TRC_TYPE_CATEGORY CHAR(4), TRC_CAT_DATA VARCHAR(50))` PK `(TRC_TYPE_CODE, TRC_TYPE_CATEGORY)`; DDL `TRNTYPE.ddl`, `TRNTYCAT.ddl`, indexes `XTRNTYPE.ddl`, `XTRNTYCAT.ddl`.

### 1.4 `app/app-vsam-mq/cbl/` — VSAM/MQ module (2 programs)

| File | Inferred purpose | Class | Key I/O | Copybooks |
|---|---|---|---|---|
| `COACCT01.cbl` | MQ request/reply: `MQGET` an account id, `EXEC CICS READ` `ACCTDAT`, `MQPUT` the account record to reply queue `CARD.DEMO.REPLY.ACCT` (transaction `CDRA`). | Online/CICS (MQ, no BMS) | `MQOPEN`/`MQGET`/`MQPUT`/`MQCLOSE`; `READ` `ACCTDAT` | `CMQGMOV`, `CMQMDV`, `CMQODV`, `CMQPMOV`, `CMQTML`, `CMQV`, `CVACT01Y` |
| `CODATE01.cbl` | MQ request/reply: returns current date/time to `CARD.DEMO.REPLY.DATE` (transaction `CDRD`). | Online/CICS (MQ, no BMS) | `MQOPEN`/`MQGET`/`MQPUT`/`MQCLOSE` | `CMQGMOV`, `CMQMDV`, `CMQODV`, `CMQPMOV`, `CMQTML`, `CMQV` |

### 1.5 Non-COBOL executables

| File | Role |
|---|---|
| `app/asm/COBDATFT.asm` | Date-format assembler routine called by `CBACT01C` |
| `app/asm/MVSWAIT.asm` | STIMER wait routine called by `COBSWAIT` |
| `app/bms/*.bms` (17) | BMS map source for `COACTUP`, `COACTVW`, `COADM01`, `COBIL00`, `COCRDLI`, `COCRDSL`, `COCRDUP`, `COMEN01`, `CORPT00`, `COSGN00`, `COTRN00/01/02`, `COUSR00/01/02/03` (symbolic copybooks in `app/cpy-bms/*.CPY`, same names) |

CICS transaction ids (from `csd/`): `CC00`→`COSGN00C`, `CM00`→`COMEN01C`, `CA00`→`COADM01C`, `CAVW`→`COACTVWC`, `CAUP`→`COACTUPC`, `CCLI`→`COCRDLIC`, `CCDL`→`COCRDSLC`, `CCUP`→`COCRDUPC`, `CT00/CT01/CT02`→`COTRN00C/01C/02C`, `CR00`→`CORPT00C`, `CB00`→`COBIL00C`, `CU00–CU03`→`COUSR00C–03C`, `CP00`→`COPAUA0C`, `CPVS`→`COPAUS0C`, `CPVD`→`COPAUS1C`, `CTLI`→`COTRTLIC`, `CTTU`→`COTRTUPC`, `CDRA`→`COACCT01`, `CDRD`→`CODATE01`.

---

## 2. JCL job catalog

Ordered step sequence per job. Datasets are as coded in the `DD` statements (continuation lines resolved). Steps consisting only of `SYSIN` control cards show the IDCAMS/SORT verb. HLQ `AWS.M2.CARDDEMO.` abbreviated to `…` in dataset names.

### 2.1 `app/jcl/` (37 jobs)

| Job | Steps (in order) → program | Datasets bound via DD |
|---|---|---|
| `ACCTFILE.jcl` | STEP05 `IDCAMS` (DELETE …ACCTDATA.VSAM.KSDS) → STEP10 `IDCAMS` (DEFINE CLUSTER) → STEP15 `IDCAMS` (REPRO) | ACCTDATA=`…ACCTDATA.PS` → ACCTVSAM=`…ACCTDATA.VSAM.KSDS` |
| `CARDFILE.jcl` | CLCIFIL `SDSF` (CEMT CLO `CARDDAT`,`CARDAIX`) → STEP05 `IDCAMS` (DELETE KSDS+AIX) → STEP10 `IDCAMS` (DEFINE CLUSTER) → STEP15 `IDCAMS` (REPRO) → STEP40 `IDCAMS` (DEFINE AIX) → STEP50 `IDCAMS` (DEFINE PATH) → STEP60 `IDCAMS` (BLDINDEX) → OPCIFIL `SDSF` (CEMT OPE) | CARDDATA=`…CARDDATA.PS` → CARDVSAM=`…CARDDATA.VSAM.KSDS`; AIX `…CARDDATA.VSAM.AIX` |
| `CBADMCDJ.jcl` | STEP1 `DFHCSDUP` | DFHCSD=`OEM.CICSTS.DFHCSD`; instream DEFINE MAPSET/PROGRAM/TRANSACTION for group `CARDDEMO` |
| `CBEXPORT.jcl` | STEP01 `IDCAMS` (DELETE/DEFINE `…EXPORT.DATA`) → STEP02 `CBEXPORT` | CUSTFILE=`…CUSTDATA.VSAM.KSDS`, ACCTFILE=`…ACCTDATA.VSAM.KSDS`, XREFFILE=`…CARDXREF.VSAM.KSDS`, TRANSACT=`…TRANSACT.VSAM.KSDS`, CARDFILE=`…CARDDATA.VSAM.KSDS` → EXPFILE=`…EXPORT.DATA` |
| `CBIMPORT.jcl` | STEP01 `CBIMPORT` | EXPFILE=`…EXPORT.DATA` → CUSTOUT=`…CUSTDATA.IMPORT`, ACCTOUT=`…ACCTDATA.IMPORT`, XREFOUT=`…CARDXREF.IMPORT`, TRNXOUT=`…TRANSACT.IMPORT`, ERROUT=`…IMPORT.ERRORS`. **Defect:** no `CARDOUT` DD, although `CBIMPORT.cbl` has `SELECT CARD-OUTPUT ASSIGN TO CARDOUT` and `OPEN OUTPUT CARD-OUTPUT` — the job as checked in fails at open (program displays "Cannot open CARD-OUTPUT") before processing; a `CARDOUT` DD (presumably `…CARDDATA.IMPORT`, **inferred** from the sibling names) would need to be added. |
| `CLOSEFIL.jcl` | CLCIFIL `SDSF` | CEMT SET FIL CLO: `TRANSACT`, `CCXREF`, `ACCTDAT`, `CXACAIX`, `USRSEC` |
| `COMBTRAN.jcl` | STEP05R `SORT` (FIELDS=(TRAN-ID,A)) → STEP10 `IDCAMS` (REPRO) | SORTIN=`…TRANSACT.BKUP(0)` + concatenated `…SYSTRAN(0)` → SORTOUT=`…TRANSACT.COMBINED(+1)`; REPRO `…TRANSACT.COMBINED(+1)` → `…TRANSACT.VSAM.KSDS` |
| `CREASTMT.JCL` | DELDEF01 `IDCAMS` (DELETE `…TRXFL.SEQ`, DELETE/DEFINE `…TRXFL.VSAM.KSDS`) → STEP010 `SORT` (FIELDS=(263,16,CH,A,1,16,CH,A); OUTREC re-keys by card number) → STEP020 `IDCAMS` (REPRO) → STEP030 `IEFBR14` (delete old outputs) → STEP040 `CBSTM03A` | SORTIN=`…TRANSACT.VSAM.KSDS` → SORTOUT=`…TRXFL.SEQ` → `…TRXFL.VSAM.KSDS`; CBSTM03A: TRNXFILE=`…TRXFL.VSAM.KSDS`, XREFFILE=`…CARDXREF.VSAM.KSDS`, ACCTFILE=`…ACCTDATA.VSAM.KSDS`, CUSTFILE=`…CUSTDATA.VSAM.KSDS` → STMTFILE=`…STATEMNT.PS`, HTMLFILE=`…STATEMNT.HTML` |
| `CUSTFILE.jcl` | CLCIFIL `SDSF` (CLO `CUSTDAT`) → STEP05 `IDCAMS` (DELETE) → STEP10 `IDCAMS` (DEFINE) → STEP15 `IDCAMS` (REPRO) → OPCIFIL `SDSF` (OPE) | CUSTDATA=`…CUSTDATA.PS` → CUSTVSAM=`…CUSTDATA.VSAM.KSDS` |
| `DALYREJS.jcl` | STEP05 `IDCAMS` (DEFINE GDG `…DALYREJS`) | — |
| `DEFCUST.jcl` | STEP05 `IDCAMS` (DELETE `AWS.CCDA.CUSTDATA.CLUSTER`) → STEP05 `IDCAMS` (DEFINE `AWS.CUSTDATA.CLUSTER`) — *note: duplicate step name and inconsistent HLQ in source* | — |
| `DEFGDGB.jcl` | STEP05 `IDCAMS` (DEFINE GDGs) | GDGs: `…TRANSACT.BKUP`, `…TRANSACT.DALY`, `…TRANREPT`, `…TCATBALF.BKUP`, `…SYSTRAN`, `…TRANSACT.COMBINED` |
| `DEFGDGD.jcl` | STEP10 `IDCAMS` (GDG `…TRANTYPE.BKUP`) → STEP20 `IEBGENER` → STEP30 `IDCAMS` (GDG `…TRANCATG.PS.BKUP`) → STEP40 `IEBGENER` → STEP50 `IDCAMS` (GDG `…DISCGRP.BKUP`) → STEP60 `IEBGENER` | `…TRANTYPE.PS`→`…TRANTYPE.BKUP(+1)`; `…TRANCATG.PS`→`…TRANCATG.PS.BKUP(+1)`; `…DISCGRP.PS`→`…DISCGRP.BKUP(+1)` |
| `DISCGRP.jcl` | STEP05 `IDCAMS` (DELETE) → STEP10 `IDCAMS` (DEFINE) → STEP15 `IDCAMS` (REPRO) | DISCGRP=`…DISCGRP.PS` → DISCVSAM=`…DISCGRP.VSAM.KSDS` |
| `DUSRSECJ.jcl` | PREDEL `IEFBR14` → STEP01 `IEBGENER` (instream user records) → STEP02 `IDCAMS` (DELETE/DEFINE) → STEP03 `IDCAMS` (REPRO) | `…USRSEC.PS` → `…USRSEC.VSAM.KSDS` |
| `ESDSRRDS.jcl` | PREDEL `IEFBR14` → STEP01 `IEBGENER` → STEP02 `IDCAMS` (DEFINE ESDS) → STEP03 `IDCAMS` (REPRO) → STEP04 `IDCAMS` (DEFINE RRDS) → STEP05 `IDCAMS` (REPRO) | `…ESDSRRDS.PS` → `…USRSEC.VSAM.ESDS`, `…USRSEC.VSAM.RRDS` |
| `FTPJCL.JCL` | STEP1 `FTP` | PUT `AWS.M2.CARDEMO.FTP.TEST` |
| `INTCALC.jcl` | STEP15 `CBACT04C` PARM=`'2022071800'` | TCATBALF=`…TCATBALF.VSAM.KSDS`, XREFFILE=`…CARDXREF.VSAM.KSDS`, XREFFIL1=`…CARDXREF.VSAM.AIX.PATH`, ACCTFILE=`…ACCTDATA.VSAM.KSDS`, DISCGRP=`…DISCGRP.VSAM.KSDS` → TRANSACT=`…SYSTRAN(+1)` |
| `INTRDRJ1.JCL` | IDCAMS `IDCAMS` (REPRO) → STEP01 `IEBGENER` (submits `INTRDRJ2` to INTRDR) | `AWS.M2.CARDEMO.FTP.TEST` → `.BKUP`; SYSUT1=`…JCL(INTRDRJ2)` |
| `INTRDRJ2.JCL` | IDCAMS `IDCAMS` (REPRO) | `AWS.M2.CARDEMO.FTP.TEST.BKUP` → `.BKUP.INTRDR` |
| `OPENFIL.jcl` | OPCIFIL `SDSF` | CEMT SET FIL OPE: `TRANSACT`, `CCXREF`, `ACCTDAT`, `CXACAIX`, `USRSEC` |
| `POSTTRAN.jcl` | STEP15 `CBTRN02C` | DALYTRAN=`…DALYTRAN.PS`, XREFFILE=`…CARDXREF.VSAM.KSDS` (in); TRANFILE=`…TRANSACT.VSAM.KSDS`, ACCTFILE=`…ACCTDATA.VSAM.KSDS`, TCATBALF=`…TCATBALF.VSAM.KSDS` (update); DALYREJS=`…DALYREJS(+1)` (out) |
| `PRTCATBL.jcl` | DELDEF `IEFBR14` → STEP05R `PROC=REPROC` (IDCAMS REPRO) → STEP10R `SORT` | `…TCATBALF.VSAM.KSDS` → `…TCATBALF.BKUP(+1)` → SORT → `…TCATBALF.REPT` |
| `READACCT.jcl` | PREDEL `IEFBR14` → STEP05 `CBACT01C` | ACCTFILE=`…ACCTDATA.VSAM.KSDS` → OUTFILE=`…ACCTDATA.PSCOMP`, ARRYFILE=`…ACCTDATA.ARRYPS`, VBRCFILE=`…ACCTDATA.VBPS` |
| `READCARD.jcl` | STEP05 `CBACT02C` | CARDFILE=`…CARDDATA.VSAM.KSDS` |
| `READCUST.jcl` | STEP05 `CBCUS01C` | CUSTFILE=`…CUSTDATA.VSAM.KSDS` |
| `READXREF.jcl` | STEP05 `CBACT03C` | XREFFILE=`…CARDXREF.VSAM.KSDS` |
| `REPTFILE.jcl` | STEP05 `IDCAMS` (DEFINE GDG `…TRANREPT`) | — |
| `TCATBALF.jcl` | STEP05 `IDCAMS` (DELETE) → STEP10 `IDCAMS` (DEFINE) → STEP15 `IDCAMS` (REPRO) | TCATBAL=`…TCATBALF.PS` → TCATBALV=`…TCATBALF.VSAM.KSDS` |
| `TRANBKP.jcl` | STEP05R `PROC=REPROC` → STEP05 `IDCAMS` (DELETE KSDS+AIX) → STEP10 `IDCAMS` (DEFINE CLUSTER) | `…TRANSACT.VSAM.KSDS` → `…TRANSACT.BKUP(+1)` |
| `TRANCATG.jcl` | STEP05/10/15 `IDCAMS` (DELETE/DEFINE/REPRO) | TRANCATG=`…TRANCATG.PS` → TCATVSAM=`…TRANCATG.VSAM.KSDS` |
| `TRANFILE.jcl` | CLCIFIL `SDSF` (CLO `TRANSACT`,`CXACAIX`) → STEP05 `IDCAMS` (DELETE) → STEP10 `IDCAMS` (DEFINE) → STEP15 `IDCAMS` (REPRO) → STEP20 `IDCAMS` (DEFINE AIX) → STEP25 `IDCAMS` (DEFINE PATH) → STEP30 `IDCAMS` (BLDINDEX) → OPCIFIL `SDSF` (OPE) | TRANSACT=`…DALYTRAN.PS.INIT` → TRANVSAM=`…TRANSACT.VSAM.KSDS`; AIX `…TRANSACT.VSAM.AIX` |
| `TRANIDX.jcl` | STEP20 `IDCAMS` (DEFINE AIX) → STEP25 `IDCAMS` (DEFINE PATH) → STEP30 `IDCAMS` (BLDINDEX) | `…TRANSACT.VSAM.AIX` |
| `TRANREPT.jcl` | STEP05R `PROC=REPROC` → STEP05R `SORT` (FIELDS=(TRAN-CARD-NUM,A), INCLUDE on TRAN-PROC-DT range) → STEP10R `CBTRN03C` | `…TRANSACT.VSAM.KSDS` → `…TRANSACT.BKUP(+1)` → `…TRANSACT.DALY(+1)`; CBTRN03C: TRANFILE=`…TRANSACT.DALY(+1)`, CARDXREF=`…CARDXREF.VSAM.KSDS`, TRANTYPE=`…TRANTYPE.VSAM.KSDS`, TRANCATG=`…TRANCATG.VSAM.KSDS`, DATEPARM=`…DATEPARM` → TRANREPT=`…TRANREPT(+1)` |
| `TRANTYPE.jcl` | STEP05/10/15 `IDCAMS` (DELETE/DEFINE/REPRO) | TRANTYPE=`…TRANTYPE.PS` → TTYPVSAM=`…TRANTYPE.VSAM.KSDS` |
| `TXT2PDF1.JCL` | TXT2PDF `IKJEFT1B` (REXX `TXT2PDF`) | SYSEXEC=`AWS.M2.LBD.TXT2PDF.EXEC`; INDD=`…STATEMNT.PS` |
| `WAITSTEP.jcl` | WAIT `COBSWAIT` | — |
| `XREFFILE.jcl` | STEP05 `IDCAMS` (DELETE KSDS+AIX) → STEP10 `IDCAMS` (DEFINE) → STEP15 `IDCAMS` (REPRO) → STEP20 `IDCAMS` (DEFINE AIX) → STEP25 `IDCAMS` (DEFINE PATH) → STEP30 `IDCAMS` (BLDINDEX) | XREFDATA=`…CARDXREF.PS` → XREFVSAM=`…CARDXREF.VSAM.KSDS`; AIX `…CARDXREF.VSAM.AIX` |

### 2.2 `app/proc/` (2 PROCs)

| PROC | Steps | Datasets |
|---|---|---|
| `REPROC.prc` | PRC001 `IDCAMS` REPRO | FILEIN/FILEOUT default `NULLFILE` (overridden by callers `PRTCATBL`, `TRANBKP`, `TRANREPT`) |
| `TRANREPT.prc` | STEP01R `PROC=REPROC` → STEP05R `SORT` → STEP10R `CBTRN03C` | same datasets as `TRANREPT.jcl` (PROC form generated by `CORPT00C`) |

### 2.3 `app/app-authorization-ims-db2-mq/jcl/` (5 jobs)

| Job | Steps | Datasets |
|---|---|---|
| `CBPAUP0J.jcl` | STEP01 `DFSRRC00` PARM=`'BMP,CBPAUP0C,PSBPAUTB'` | STEPLIB `IMS.SDFSRESL`, IMS=`IMS.PSBLIB`, IMSLOGR=DUMMY |
| `DBPAUTP0.jcl` | STEPDEL `IEFBR14` → UNLOAD `DFSRRC00` PARM=`(ULU,DFSURGU0,DBPAUTP0)` | DFSURGU1=`…IMSDATA.DBPAUTP0`; DDPAUTP0=`OEM.IMS.IMSP.PAUTHDB`, DDPAUTX0=`OEM.IMS.IMSP.PAUTHDBX`; RECON1–3 |
| `LOADPADB.JCL` | STEP01 `DFSRRC00` PARM=`'BMP,PAUDBLOD,PSBPAUTB'` | INFILE1=`…PAUTDB.ROOT.FILEO`, INFILE2=`…PAUTDB.CHILD.FILEO` |
| `UNLDGSAM.JCL` | STEP01 `DFSRRC00` PARM=`'DLI,DBUNLDGS,DLIGSAMP,…'` | PASFILOP=`…PAUTDB.ROOT.GSAM`, PADFILOP=`…PAUTDB.CHILD.GSAM`; DDPAUTP0/DDPAUTX0 |
| `UNLDPADB.JCL` | STEP0 `IEFBR14` → STEP01 `DFSRRC00` PARM=`'DLI,PAUDBUNL,PAUTBUNL,…'` | OUTFIL1=`…PAUTDB.ROOT.FILEO`, OUTFIL2=`…PAUTDB.CHILD.FILEO`; DDPAUTP0/DDPAUTX0 |

### 2.4 `app/app-transaction-type-db2/jcl/` (3 jobs)

| Job | Steps | Datasets |
|---|---|---|
| `CREADB21.jcl` | FREEPLN `IKJEFT01` (`CNTL(DB2FREE)`) → CRCRDDB `IKJEFT01` (`DB2TIAD1` + `DB2CREAT`) → LDTTYPE `IEFBR14` → RUNTEP2 `IKJEFT01` (`DB2TEP41` + `DB2LTTYP`) → LDTCCAT `IKJEFT01` (`DB2TEP41` + `DB2LTCAT`) | control members from `&LBNM..CNTL`; creates and loads `TRANSACTION_TYPE` / `TRANSACTION_TYPE_CATEGORY` (see `ctl/`) |
| `MNTTRDB2.jcl` | STEP1 `IKJEFT01` → `RUN PROGRAM(COBTUPDT) PLAN(CARDDEMO)` | DBRMLIB=`…DBRMLIB`; INPFILE (instream control records) |
| `TRANEXTR.jcl` | STEP10 `IEBGENER` → STEP20 `IEBGENER` → STEP30 `IEFBR14` → STEP40 `IKJEFT01` (`DSNTIAUL` unload `TRANSACTION_TYPE`) → STEP50 `IKJEFT01` (`DSNTIAUL` unload `TRANSACTION_TYPE_CATEGORY`) | `&HLQ..TRANTYPE.PS`→`.BKUP(+1)`, `&HLQ..TRANCATG.PS`→`.PS.BKUP(+1)`; SYSREC00=`&HLQ..TRANTYPE.PS`, `&HLQ..TRANCATG.PS` |

`app/app-vsam-mq/` has no JCL (CSD only).

---

## 3. Observations relevant to migration (inferred)

* `CBTRN01C` is not referenced by any JCL; `CBTRN02C` (job `POSTTRAN`) is the live posting program.
* `CBADMCDJ.jcl` defines program names (`COACT00C`, `COTRNVWC`, `COTSTP1C`…) that do not exist in `app/cbl/`; treat as stale CSD material.
* `INTCALC.jcl` allocates `XREFFIL1` (AIX path) but `CBACT04C` only `SELECT`s `XREFFILE`; the extra DD is unused by the program.
* All CICS file access is by literal dataset name resolved in WORKING-STORAGE; the eight logical files above are the complete VSAM surface for the online estate.
