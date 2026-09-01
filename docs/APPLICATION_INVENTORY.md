# CardDemo Application Inventory

Source of truth: the COBOL, copybook and JCL members in `app/` of this repository. Every figure and name below was extracted by parsing the source files listed in the `Source` columns; nothing is inferred from documentation.

Counts found in the tree: **44 COBOL programs** (31 in `app/cbl/`, 8 in `app/app-authorization-ims-db2-mq/cbl/`, 3 in `app/app-transaction-type-db2/cbl/`, 2 in `app/app-vsam-mq/cbl/`), **65 copybook/DCL members** (30 in `app/cpy/`, 17 BMS map copybooks in `app/cpy-bms/`, 9 + 2 + 1 DCL in the authorization extension, 2 + 2 + 2 DCL in the transaction-type extension), and **38 JCL members** in `app/jcl/` plus 2 procedures in `app/proc/`, 5 authorization-extension and 3 transaction-type-extension JCL members. The task brief mentions 37 core JCL files; the directory actually holds 38 and all 38 are catalogued in section 3.1.

Classification rule applied: a program is *online* when it contains `EXEC CICS`; it is *batch* when it has a `FILE-CONTROL`/`SELECT` section and/or is named on an `EXEC PGM=` card in JCL. `CBSTM03B` and `CSUTLDTC` are called subroutines and have no JCL step of their own; `CBTRN01C` is a batch program that no JCL member in this repository executes.

BMS map copybooks (`COACTUP`, `COSGN00`, `COMEN01`, ...) live in `app/cpy-bms/` and are listed in the copybook column as ordinary `COPY` references. `DFHAID` and `DFHBMSCA` are IBM-supplied CICS copybooks.


## 1. Program inventory

| Program | Source | Type | Purpose | Key I/O | Copybooks referenced (`COPY`) |
|---|---|---|---|---|---|
| `CBACT01C` | `app/cbl/CBACT01C.cbl` | Batch, EXEC PGM in `READACCT` | Read the account master sequentially and write three derived sequential extracts (packed, array and variable-length record formats). | `ACCTFILE` VSAM KSDS, sequential access, key FD-ACCT-ID; `OUTFILE` sequential, sequential access; `ARRYFILE` sequential, sequential access; `VBRCFILE` sequential, sequential access | `CODATECN`, `CVACT01Y` |
| `CBACT02C` | `app/cbl/CBACT02C.cbl` | Batch, EXEC PGM in `READCARD` | Read and print the card master file. | `CARDFILE` VSAM KSDS, sequential access, key FD-CARD-NUM | `CVACT02Y` |
| `CBACT03C` | `app/cbl/CBACT03C.cbl` | Batch, EXEC PGM in `READXREF` | Read and print the card cross-reference file. | `XREFFILE` VSAM KSDS, sequential access, key FD-XREF-CARD-NUM | `CVACT03Y` |
| `CBACT04C` | `app/cbl/CBACT04C.cbl` | Batch, EXEC PGM in `INTCALC` | Interest calculator: walk transaction-category balances, look up the disclosure-group rate, compute monthly interest, update account balances and write interest transactions. | `TCATBALF` VSAM KSDS, sequential access, key FD-TRAN-CAT-KEY; `XREFFILE` VSAM KSDS, random access, key FD-XREF-CARD-NUM; `ACCTFILE` VSAM KSDS, random access, key FD-ACCT-ID; `DISCGRP` VSAM KSDS, random access, key FD-DISCGRP-KEY; `TRANSACT` sequential, sequential access | `CVACT01Y`, `CVACT03Y`, `CVTRA01Y`, `CVTRA02Y`, `CVTRA05Y` |
| `CBCUS01C` | `app/cbl/CBCUS01C.cbl` | Batch, EXEC PGM in `READCUST` | Read and print the customer master file. | `CUSTFILE` VSAM KSDS, sequential access, key FD-CUST-ID | `CVCUS01Y` |
| `CBEXPORT` | `app/cbl/CBEXPORT.cbl` | Batch, EXEC PGM in `CBEXPORT` | Export customer/account/card/xref/transaction data into one multiplexed branch-migration export file. | `CUSTFILE` VSAM KSDS, sequential access, key CUST-ID; `ACCTFILE` VSAM KSDS, sequential access, key ACCT-ID; `XREFFILE` VSAM KSDS, sequential access, key XREF-CARD-NUM; `TRANSACT` VSAM KSDS, sequential access, key TRAN-ID; `CARDFILE` VSAM KSDS, sequential access, key CARD-NUM; `EXPFILE` VSAM KSDS, sequential access, key EXPORT-SEQUENCE-NUM | `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVEXPORT`, `CVTRA05Y` |
| `CBIMPORT` | `app/cbl/CBIMPORT.cbl` | Batch, EXEC PGM in `CBIMPORT` | Import the branch-migration export file and split it back into normalized sequential customer/account/xref/transaction/card outputs plus an error file. | `EXPFILE` VSAM KSDS, sequential access, key EXPORT-SEQUENCE-NUM; `CUSTOUT` sequential, sequential access; `ACCTOUT` sequential, sequential access; `XREFOUT` sequential, sequential access; `TRNXOUT` sequential, sequential access; `CARDOUT` sequential, sequential access; `ERROUT` sequential, sequential access | `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVEXPORT`, `CVTRA05Y` |
| `CBSTM03A` | `app/cbl/CBSTM03A.CBL` | Batch, EXEC PGM in `CREASTMT` | Produce account statements in plain text and HTML; delegates all file access to subroutine CBSTM03B. | `STMTFILE` sequential; `HTMLFILE` sequential | `COSTM01`, `CUSTREC`, `CVACT01Y`, `CVACT03Y` |
| `CBSTM03B` | `app/cbl/CBSTM03B.CBL` | Batch subroutine (called by CBSTM03A) | File-handling subroutine for the statement driver: opens/reads TRNXFILE, XREFFILE, CUSTFILE and ACCTFILE on request. | `TRNXFILE` VSAM KSDS, sequential access, key FD-TRNXS-ID; `XREFFILE` VSAM KSDS, sequential access, key FD-XREF-CARD-NUM; `CUSTFILE` VSAM KSDS, random access, key FD-CUST-ID; `ACCTFILE` VSAM KSDS, random access, key FD-ACCT-ID | none |
| `CBTRN01C` | `app/cbl/CBTRN01C.cbl` | Batch (no JCL in this repo) | Read the daily transaction file and validate each record against xref, card, account and customer masters (no JCL in this repo invokes it). | `DALYTRAN` sequential, sequential access; `CUSTFILE` VSAM KSDS, random access, key FD-CUST-ID; `XREFFILE` VSAM KSDS, random access, key FD-XREF-CARD-NUM; `CARDFILE` VSAM KSDS, random access, key FD-CARD-NUM; `ACCTFILE` VSAM KSDS, random access, key FD-ACCT-ID; `TRANFILE` VSAM KSDS, random access, key FD-TRANS-ID | `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVTRA05Y`, `CVTRA06Y` |
| `CBTRN02C` | `app/cbl/CBTRN02C.cbl` | Batch, EXEC PGM in `POSTTRAN` | Post daily transactions: validate, apply credit-limit checks, update account balances and category balances, write posted transactions or rejects. | `DALYTRAN` sequential, sequential access; `TRANFILE` VSAM KSDS, random access, key FD-TRANS-ID; `XREFFILE` VSAM KSDS, random access, key FD-XREF-CARD-NUM; `DALYREJS` sequential, sequential access; `ACCTFILE` VSAM KSDS, random access, key FD-ACCT-ID; `TCATBALF` VSAM KSDS, random access, key FD-TRAN-CAT-KEY | `CVACT01Y`, `CVACT03Y`, `CVTRA01Y`, `CVTRA05Y`, `CVTRA06Y` |
| `CBTRN03C` | `app/cbl/CBTRN03C.cbl` | Batch, EXEC PGM in `TRANREPT (and proc TRANREPT)` | Print the daily transaction detail report with type/category descriptions and page/account/grand totals. | `TRANFILE` sequential; `CARDXREF` VSAM KSDS, random access, key FD-XREF-CARD-NUM; `TRANTYPE` VSAM KSDS, random access, key FD-TRAN-TYPE; `TRANCATG` VSAM KSDS, random access, key FD-TRAN-CAT-KEY; `TRANREPT` sequential; `DATEPARM` sequential | `CVACT03Y`, `CVTRA03Y`, `CVTRA04Y`, `CVTRA05Y`, `CVTRA07Y` |
| `COACTUPC` | `app/cbl/COACTUPC.cbl` | Online (CICS), TRANID `CAUP` | Online account update: fetch account + customer, field-level edit every attribute, then rewrite both records under a syncpoint. | CICS file `ACCTDAT`; CICS file `CXACAIX`; CICS file `CUSTDAT` | `COACTUP`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSLKPCDY`, `CSMSG01Y`, `CSMSG02Y`, `CSSETATY`, `CSUSR01Y`, `CSUTLDPY`, `CVACT01Y`, `CVACT03Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COACTVWC` | `app/cbl/COACTVWC.cbl` | Online (CICS), TRANID `CAVW` | Online account view: resolve account via xref, read account and customer, display. | CICS file `ACCTDAT`; CICS file `CXACAIX`; CICS file `CUSTDAT` | `COACTVW`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COADM01C` | `app/cbl/COADM01C.cbl` | Online (CICS), TRANID `CA00` | Admin menu; dispatches to admin programs listed in COADM02Y. | none | `COADM01`, `COADM02Y`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COBIL00C` | `app/cbl/COBIL00C.cbl` | Online (CICS), TRANID `CB00` | Bill payment: pay the account balance in full, write the payment transaction and rewrite the account. | CICS file `ACCTDAT`; CICS file `CXACAIX`; CICS file `TRANSACT` | `COBIL00`, `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVACT01Y`, `CVACT03Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COBSWAIT` | `app/cbl/COBSWAIT.cbl` | Batch, EXEC PGM in `WAITSTEP` | Batch utility that waits for the number of centiseconds passed in PARM (calls MVSWAIT). | none | none |
| `COCRDLIC` | `app/cbl/COCRDLIC.cbl` | Online (CICS), TRANID `CCLI` | Credit card list with browse paging; admin sees all cards, users only their account's cards. | CICS file `CARDDAT` | `COCOM01Y`, `COCRDLI`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `DFHAID`, `DFHBMSCA` |
| `COCRDSLC` | `app/cbl/COCRDSLC.cbl` | Online (CICS), TRANID `CCDL` | Credit card detail view via card master or the account alternate index. | CICS file `CARDDAT`; CICS file `CARDAIX` | `COCOM01Y`, `COCRDSL`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COCRDUPC` | `app/cbl/COCRDUPC.cbl` | Online (CICS), TRANID `CCUP` | Credit card update: edit card fields and rewrite the card record. | CICS file `CARDDAT` | `COCOM01Y`, `COCRDUP`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COMEN01C` | `app/cbl/COMEN01C.cbl` | Online (CICS), TRANID `CM00` | Main menu for regular users; dispatches to programs listed in COMEN02Y. | none | `COCOM01Y`, `COMEN01`, `COMEN02Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `CORPT00C` | `app/cbl/CORPT00C.cbl` | Online (CICS), TRANID `CR00` | Transaction report request screen; validates the date range and submits the batch report JCL through an extrapartition TDQ. | none | `COCOM01Y`, `CORPT00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COSGN00C` | `app/cbl/COSGN00C.cbl` | Online (CICS), TRANID `CC00` | Sign-on screen; authenticates against USRSEC and routes to the admin or user menu. | CICS file `USRSEC` | `COCOM01Y`, `COSGN00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COTRN00C` | `app/cbl/COTRN00C.cbl` | Online (CICS), TRANID `CT00` | List transactions from TRANSACT with forward/backward browse. | CICS file `TRANSACT` | `COCOM01Y`, `COTRN00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COTRN01C` | `app/cbl/COTRN01C.cbl` | Online (CICS), TRANID `CT01` | View a single transaction from TRANSACT. | CICS file `TRANSACT` | `COCOM01Y`, `COTRN01`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COTRN02C` | `app/cbl/COTRN02C.cbl` | Online (CICS), TRANID `CT02` | Add a transaction: validate card/account via xref, edit all fields, write to TRANSACT. | CICS file `CCXREF`; CICS file `CXACAIX`; CICS file `TRANSACT` | `COCOM01Y`, `COTRN02`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CVACT01Y`, `CVACT03Y`, `CVTRA05Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR00C` | `app/cbl/COUSR00C.cbl` | Online (CICS), TRANID `CU00` | List users from USRSEC with browse paging, and route to update/delete. | CICS file `USRSEC` | `COCOM01Y`, `COTTL01Y`, `COUSR00`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR01C` | `app/cbl/COUSR01C.cbl` | Online (CICS), TRANID `CU01` | Add a user to USRSEC. | CICS file `USRSEC` | `COCOM01Y`, `COTTL01Y`, `COUSR01`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR02C` | `app/cbl/COUSR02C.cbl` | Online (CICS), TRANID `CU02` | Update a user in USRSEC. | CICS file `USRSEC` | `COCOM01Y`, `COTTL01Y`, `COUSR02`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `COUSR03C` | `app/cbl/COUSR03C.cbl` | Online (CICS), TRANID `CU03` | Delete a user from USRSEC. | CICS file `USRSEC` | `COCOM01Y`, `COTTL01Y`, `COUSR03`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `DFHAID`, `DFHBMSCA` |
| `CSUTLDTC` | `app/cbl/CSUTLDTC.cbl` | Subroutine (static CALL from online programs) | Date-validation subroutine wrapping the LE CEEDAYS callable service; returns a Lillian date or a formatted error. | none | none |
| `CBPAUP0C` | `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl` | Batch, EXEC PGM in `CBPAUP0J` | IMS batch (BMP) purge of expired pending authorizations: deletes PAUTDTL1 details and PAUTSUM0 summaries, taking checkpoints. | EXEC DLI CHKP/DLET/GN/GNP on PAUTDTL1/PAUTSUM0 | `CIPAUDTY`, `CIPAUSMY` |
| `COPAUA0C` | `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | Online (CICS), TRANID `CP00` | Authorization decision engine: MQGETs a request from the request queue, validates card/account/customer, applies limit rules, stores the authorization in IMS and MQPUT1s the reply. | CICS file `ACCTDAT`; CICS file `CCXREF`; CICS file `CUSTDAT`; EXEC DLI GU/ISRT/REPL/SCHD/TERM on PAUTDTL1/PAUTSUM0; MQ MQCLOSE/MQGET/MQOPEN/MQPUT1 | `CCPAUERY`, `CCPAURLY`, `CCPAURQY`, `CIPAUDTY`, `CIPAUSMY`, `CMQGMOV`, `CMQMDV`, `CMQODV`, `CMQPMOV`, `CMQTML`, `CMQV`, `CVACT01Y`, `CVACT03Y`, `CVCUS01Y` |
| `COPAUS0C` | `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | Online (CICS), TRANID `CPVS` | Summary view of pending authorizations for an account (IMS PAUTSUM0/PAUTDTL1). | CICS file `ACCTDAT`; CICS file `CXACAIX`; CICS file `CUSTDAT`; EXEC DLI GNP/GU/SCHD/TERM on PAUTDTL1/PAUTSUM0 | `CIPAUDTY`, `CIPAUSMY`, `COCOM01Y`, `COPAU00`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `DFHAID`, `DFHBMSCA` |
| `COPAUS1C` | `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl` | Online (CICS), TRANID `CPVD` | Detail view of one authorization; can LINK to COPAUS2C to flag fraud. | EXEC DLI GNP/GU/REPL/SCHD/TERM on PAUTDTL1/PAUTSUM0 | `CIPAUDTY`, `CIPAUSMY`, `COCOM01Y`, `COPAU01`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `DFHAID`, `DFHBMSCA` |
| `COPAUS2C` | `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl` | Online (CICS) | Mark an authorization as fraud: INSERT into and UPDATE the DB2 AUTHFRDS table. | DB2 `CARDDEMO.AUTHFRDS` | `CIPAUDTY` + `EXEC SQL INCLUDE` `AUTHFRDS`, `SQLCA` |
| `DBUNLDGS` | `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL` | Batch, EXEC PGM in `UNLDGSAM` | IMS BMP that unloads the pending-authorization database to GSAM output data sets. | IMS `CALL 'CBLTDLI'` | `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, `PADFLPCB`, `PASFLPCB`, `PAUTBPCB` |
| `PAUDBLOD` | `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL` | Batch, EXEC PGM in `LOADPADB` | IMS load program: reads sequential root/child files and ISRTs PAUTSUM0/PAUTDTL1 segments. | `INFILE1` sequential, sequential access; `INFILE2` sequential, sequential access; IMS `CALL 'CBLTDLI'` | `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, `PAUTBPCB` |
| `PAUDBUNL` | `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL` | Batch, EXEC PGM in `UNLDPADB` | IMS unload program: navigates PAUTSUM0/PAUTDTL1 and writes two sequential files. | `OUTFIL1` sequential, sequential access; `OUTFIL2` sequential, sequential access; IMS `CALL 'CBLTDLI'` | `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, `PAUTBPCB` |
| `COBTUPDT` | `app/app-transaction-type-db2/cbl/COBTUPDT.cbl` | Batch, EXEC PGM in `MNTTRDB2` | Batch DB2 maintenance of CARDDEMO.TRANSACTION_TYPE driven by an INPFILE control file (add/update/delete). | `INPFILE` sequential, sequential access; DB2 `CARDDEMO.TRANSACTION_TYPE` | none + `EXEC SQL INCLUDE` `DCLTRTYP`, `SQLCA` |
| `COTRTLIC` | `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` | Online (CICS), TRANID `CTLI` | Online DB2 transaction-type list with forward/backward cursors; supports select-for-update and delete. | DB2 `CARDDEMO.TRANSACTION_TYPE` | `COCOM01Y`, `COTRTLI`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y`, `CVACT02Y`, `CVCRD01Y`, `DFHAID`, `DFHBMSCA` + `EXEC SQL INCLUDE` `CSDB2RPY`, `CSDB2RWY`, `DCLTRTYP`, `SQLCA` |
| `COTRTUPC` | `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` | Online (CICS), TRANID `CTTU` | Online DB2 transaction-type add/update/delete screen. | DB2 `CARDDEMO.TRANSACTION_TYPE` | `COCOM01Y`, `COTRTUP`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSSETATY`, `CSUSR01Y`, `CVCRD01Y`, `DFHAID`, `DFHBMSCA` + `EXEC SQL INCLUDE` `DCLTRCAT`, `DCLTRTYP`, `SQLCA` |
| `COACCT01` | `app/app-vsam-mq/cbl/COACCT01.cbl` | Online (CICS), TRANID `CDRA` | MQ-driven account inquiry: MQGETs a request, reads ACCTDAT via CICS and MQPUTs the account details (or an error message). | CICS file `ACCTDAT`; MQ MQCLOSE/MQGET/MQOPEN/MQPUT | `CMQGMOV`, `CMQMDV`, `CMQODV`, `CMQPMOV`, `CMQTML`, `CMQV`, `CVACT01Y` |
| `CODATE01` | `app/app-vsam-mq/cbl/CODATE01.cbl` | Online (CICS), TRANID `CDRD` | MQ-driven date service: MQGETs a request and MQPUTs the current CICS date/time. | MQ MQCLOSE/MQGET/MQOPEN/MQPUT | `CMQGMOV`, `CMQMDV`, `CMQODV`, `CMQPMOV`, `CMQTML`, `CMQV` |

### 1.1 DB2, IMS and MQ usage

| Program | DB2 tables | SQL statements | IMS segments | MQ |
|---|---|---|---|---|
| `CBPAUP0C` | - | - | `PAUTDTL1`, `PAUTSUM0` | - |
| `COPAUA0C` | - | - | `PAUTDTL1`, `PAUTSUM0` | `MQCLOSE`, `MQGET`, `MQOPEN`, `MQPUT1` |
| `COPAUS0C` | - | - | `PAUTDTL1`, `PAUTSUM0` | - |
| `COPAUS1C` | - | - | `PAUTDTL1`, `PAUTSUM0` | - |
| `COPAUS2C` | `CARDDEMO.AUTHFRDS` | INSERT, UPDATE | - | - |
| `COBTUPDT` | `CARDDEMO.TRANSACTION_TYPE` | DELETE, INSERT, UPDATE | - | - |
| `COTRTLIC` | `CARDDEMO.TRANSACTION_TYPE` | CLOSE, DECLARE, DELETE, FETCH, OPEN, SELECT, UPDATE | - | - |
| `COTRTUPC` | `CARDDEMO.TRANSACTION_TYPE` | DELETE, INSERT, SELECT, UPDATE | - | - |
| `COACCT01` | - | - | - | `MQCLOSE`, `MQGET`, `MQOPEN`, `MQPUT` |
| `CODATE01` | - | - | - | `MQCLOSE`, `MQGET`, `MQOPEN`, `MQPUT` |

`CARDDEMO.TRANSACTION_TYPE` is declared in `app/app-transaction-type-db2/dcl/DCLTRTYP.dcl`, `CARDDEMO.TRANSACTION_TYPE_CATEGORY` in `DCLTRCAT.dcl` and `CARDDEMO.AUTHFRDS` in `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl`. `COTRTUPC` includes `DCLTRCAT` but issues no SQL against the category table; its only SQL targets `TRANSACTION_TYPE`.

`DBUNLDGS` contains `SELECT ... ASSIGN` clauses only inside comment lines (`app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL`, columns 7 = `*`); its live I/O is GSAM through `CALL 'CBLTDLI'`, so it is reported with no `SELECT` files.


## 2. CICS resource definitions

From `app/csd/CARDDEMO.CSD` and the extension CSD members.

| CICS FILE | Underlying data set |
|---|---|
| `ACCTDAT` | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` |
| `CARDAIX` | `AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH` |
| `CARDDAT` | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` |
| `CCXREF` | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` |
| `CUSTDAT` | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` |
| `CXACAIX` | `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH` |
| `TRANSACT` | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` |
| `USRSEC` | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` |

`COCRDSEC` is defined in the CSD (`DEFINE TRANSACTION(CDV1)`) but has no source member in this repository, so it is absent from the program inventory.


Transaction ids: `CA00`=COADM01C, `CAUP`=COACTUPC, `CAVW`=COACTVWC, `CB00`=COBIL00C, `CC00`=COSGN00C, `CCDL`=COCRDSLC, `CCLI`=COCRDLIC, `CCUP`=COCRDUPC, `CDRA`=COACCT01, `CDRD`=CODATE01, `CM00`=COMEN01C, `CP00`=COPAUA0C, `CPVD`=COPAUS1C, `CPVS`=COPAUS0C, `CR00`=CORPT00C, `CT00`=COTRN00C, `CT01`=COTRN01C, `CT02`=COTRN02C, `CTLI`=COTRTLIC, `CTTU`=COTRTUPC, `CU00`=COUSR00C, `CU01`=COUSR01C, `CU02`=COUSR02C, `CU03`=COUSR03C.


## 3. JCL catalogue

Every member below was parsed from its `//name EXEC` and `//name DD` cards (continuations joined, concatenated DDs kept as `NAME (concat)`, comment cards dropped). `inline` marks in-stream control statements; `SYSPRINT`, `SYSOUT`, `SYSUDUMP`, `STEPLIB` and similar housekeeping DDs are omitted from the dataset column. `(read)`/`(write)` is derived from `DISP`: for IDCAMS steps both the input and the target cluster are normally `DISP=SHR`, so the actual direction is given by the `REPRO INFILE/OUTFILE` control statement in `SYSIN`. `&HLQ`, `&LBNM` and similar are unresolved JCL symbols.


### 3.1 Core jobs (`app/jcl/`, 38 members)


**`ACCTFILE`** — `app/jcl/ACCTFILE.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP15` | PGM=`IDCAMS` | `ACCTDATA`=AWS.M2.CARDDEMO.ACCTDATA.PS (read), `ACCTVSAM`=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (read), `SYSIN`=inline |

**`CARDFILE`** — `app/jcl/CARDFILE.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `CLCIFIL` | PGM=`SDSF` | `ISFIN`=inline |
| 2 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 4 | `STEP15` | PGM=`IDCAMS` | `CARDDATA`=AWS.M2.CARDDEMO.CARDDATA.PS (read), `CARDVSAM`=AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS (read), `SYSIN`=inline |
| 5 | `STEP40` | PGM=`IDCAMS` | `SYSIN`=inline |
| 6 | `STEP50` | PGM=`IDCAMS` | `SYSIN`=inline |
| 7 | `STEP60` | PGM=`IDCAMS` | `SYSIN`=inline |
| 8 | `OPCIFIL` | PGM=`SDSF` | `ISFIN`=inline |

**`CBADMCDJ`** — `app/jcl/CBADMCDJ.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP1` | PGM=`DFHCSDUP` | `DFHCSD`=OEM.CICSTS.DFHCSD (read), `OUTDD`=-, `SYSIN`=inline |

**`CBEXPORT`** — `app/jcl/CBEXPORT.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP01` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP02` | PGM=`CBEXPORT` | `CUSTFILE`=AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS (read), `ACCTFILE`=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (read), `XREFFILE`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read), `TRANSACT`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `CARDFILE`=AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS (read), `EXPFILE`=AWS.M2.CARDDEMO.EXPORT.DATA (read) |

**`CBIMPORT`** — `app/jcl/CBIMPORT.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP01` | PGM=`CBIMPORT` | `EXPFILE`=AWS.M2.CARDDEMO.EXPORT.DATA (read), `CUSTOUT`=AWS.M2.CARDDEMO.CUSTDATA.IMPORT (write), `ACCTOUT`=AWS.M2.CARDDEMO.ACCTDATA.IMPORT (write), `XREFOUT`=AWS.M2.CARDDEMO.CARDXREF.IMPORT (write), `TRNXOUT`=AWS.M2.CARDDEMO.TRANSACT.IMPORT (write), `ERROUT`=AWS.M2.CARDDEMO.IMPORT.ERRORS (write) |

**`CLOSEFIL`** — `app/jcl/CLOSEFIL.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `CLCIFIL` | PGM=`SDSF` | `ISFIN`=inline |

**`COMBTRAN`** — `app/jcl/COMBTRAN.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05R` | PGM=`SORT` | `SORTIN`=AWS.M2.CARDDEMO.TRANSACT.BKUP(0) (read), `SORTIN (concat)`=AWS.M2.CARDDEMO.SYSTRAN(0) (read), `SYMNAMES`=inline, `SYSIN`=inline, `SORTOUT`=AWS.M2.CARDDEMO.TRANSACT.COMBINED(+1) (write) |
| 2 | `STEP10` | PGM=`IDCAMS` | `TRANSACT`=AWS.M2.CARDDEMO.TRANSACT.COMBINED(+1) (read), `TRANVSAM`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `SYSIN`=inline |

**`CREASTMT`** — `app/jcl/CREASTMT.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `DELDEF01` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP010` | PGM=`SORT` | `SORTIN`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `SORTOUT`=AWS.M2.CARDDEMO.TRXFL.SEQ (write), `SYSIN`=inline |
| 3 | `STEP020` | PGM=`IDCAMS` | `INFILE`=AWS.M2.CARDDEMO.TRXFL.SEQ (read), `OUTFILE`=AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS (read), `SYSIN`=inline |
| 4 | `STEP030` | PGM=`IEFBR14` | `HTMLFILE`=AWS.M2.CARDDEMO.STATEMNT.HTML, `STMTFILE`=AWS.M2.CARDDEMO.STATEMNT.PS |
| 5 | `STEP040` | PGM=`CBSTM03A` | `TRNXFILE`=AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS (read), `XREFFILE`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read), `ACCTFILE`=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (read), `CUSTFILE`=AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS (read), `STMTFILE`=AWS.M2.CARDDEMO.STATEMNT.PS (write), `HTMLFILE`=AWS.M2.CARDDEMO.STATEMNT.HTML (write) |

**`CUSTFILE`** — `app/jcl/CUSTFILE.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `CLCIFIL` | PGM=`SDSF` | `ISFIN`=inline |
| 2 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 4 | `STEP15` | PGM=`IDCAMS` | `CUSTDATA`=AWS.M2.CARDDEMO.CUSTDATA.PS (read), `CUSTVSAM`=AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS (read), `SYSIN`=inline |
| 5 | `OPCIFIL` | PGM=`SDSF` | `ISFIN`=inline |

**`DALYREJS`** — `app/jcl/DALYREJS.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |

**`DEFCUST`** — `app/jcl/DEFCUST.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |

**`DEFGDGB`** — `app/jcl/DEFGDGB.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |

**`DEFGDGD`** — `app/jcl/DEFGDGD.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP20` | PGM=`IEBGENER` | `SYSIN`=DUMMY, `SYSUT1`=AWS.M2.CARDDEMO.TRANTYPE.PS (read), `SYSUT2`=AWS.M2.CARDDEMO.TRANTYPE.BKUP(+1) (write) |
| 3 | `STEP30` | PGM=`IDCAMS` | `SYSIN`=inline |
| 4 | `STEP40` | PGM=`IEBGENER` | `SYSIN`=DUMMY, `SYSUT1`=AWS.M2.CARDDEMO.TRANCATG.PS (read), `SYSUT2`=AWS.M2.CARDDEMO.TRANCATG.PS.BKUP(+1) (write) |
| 5 | `STEP50` | PGM=`IDCAMS` | `SYSIN`=inline |
| 6 | `STEP60` | PGM=`IEBGENER` | `SYSIN`=DUMMY, `SYSUT1`=AWS.M2.CARDDEMO.DISCGRP.PS (read), `SYSUT2`=AWS.M2.CARDDEMO.DISCGRP.BKUP(+1) (write) |

**`DISCGRP`** — `app/jcl/DISCGRP.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP15` | PGM=`IDCAMS` | `DISCGRP`=AWS.M2.CARDDEMO.DISCGRP.PS (read), `DISCVSAM`=AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS (read), `SYSIN`=inline |

**`DUSRSECJ`** — `app/jcl/DUSRSECJ.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `PREDEL` | PGM=`IEFBR14` | `DD01`=AWS.M2.CARDDEMO.USRSEC.PS |
| 2 | `STEP01` | PGM=`IEBGENER` | `SYSUT1`=inline, `SYSUT2`=AWS.M2.CARDDEMO.USRSEC.PS (write), `SYSIN`=DUMMY |
| 3 | `STEP02` | PGM=`IDCAMS` | `SYSIN`=inline |
| 4 | `STEP03` | PGM=`IDCAMS` | `IN`=AWS.M2.CARDDEMO.USRSEC.PS (read), `OUT`=AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS (read), `SYSIN`=inline |

**`ESDSRRDS`** — `app/jcl/ESDSRRDS.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `PREDEL` | PGM=`IEFBR14` | `DD01`=AWS.M2.CARDDEMO.ESDSRRDS.PS |
| 2 | `STEP01` | PGM=`IEBGENER` | `SYSUT1`=inline, `SYSUT2`=AWS.M2.CARDDEMO.ESDSRRDS.PS (write), `SYSIN`=DUMMY |
| 3 | `STEP02` | PGM=`IDCAMS` | `SYSIN`=inline |
| 4 | `STEP03` | PGM=`IDCAMS` | `IN`=AWS.M2.CARDDEMO.ESDSRRDS.PS (read), `OUT`=AWS.M2.CARDDEMO.USRSEC.VSAM.ESDS (read), `SYSIN`=inline |
| 5 | `STEP04` | PGM=`IDCAMS` | `SYSIN`=inline |
| 6 | `STEP05` | PGM=`IDCAMS` | `IN`=AWS.M2.CARDDEMO.ESDSRRDS.PS (read), `OUT`=AWS.M2.CARDDEMO.USRSEC.VSAM.RRDS (read), `SYSIN`=inline |

**`FTPJCL`** — `app/jcl/FTPJCL.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP1` | PGM=`FTP` | `SYSIN`=inline |

**`INTCALC`** — `app/jcl/INTCALC.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP15` | PGM=`CBACT04C` | `TCATBALF`=AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS (read), `XREFFILE`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read), `XREFFIL1`=AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH (read), `ACCTFILE`=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (read), `DISCGRP`=AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS (read), `TRANSACT`=AWS.M2.CARDDEMO.SYSTRAN(+1) (write) |

**`INTRDRJ1`** — `app/jcl/INTRDRJ1.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `IDCAMS` | PGM=`IDCAMS` | `IN`=AWS.M2.CARDEMO.FTP.TEST (read), `OUT`=AWS.M2.CARDEMO.FTP.TEST.BKUP (read), `SYSIN`=inline |
| 2 | `STEP01` | PGM=`IEBGENER` | `SYSIN`=DUMMY, `SYSUT1`=AWS.M2.CARDDEMO.JCL(INTRDRJ2) (read), `SYSUT2`=- |

**`INTRDRJ2`** — `app/jcl/INTRDRJ2.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `IDCAMS` | PGM=`IDCAMS` | `IN`=AWS.M2.CARDEMO.FTP.TEST.BKUP (read), `OUT`=AWS.M2.CARDEMO.FTP.TEST.BKUP.INTRDR (read), `SYSIN`=inline |

**`OPENFIL`** — `app/jcl/OPENFIL.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `OPCIFIL` | PGM=`SDSF` | `ISFIN`=inline |

**`POSTTRAN`** — `app/jcl/POSTTRAN.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP15` | PGM=`CBTRN02C` | `TRANFILE`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `DALYTRAN`=AWS.M2.CARDDEMO.DALYTRAN.PS (read), `XREFFILE`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read), `DALYREJS`=AWS.M2.CARDDEMO.DALYREJS(+1) (write), `ACCTFILE`=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (read), `TCATBALF`=AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS (read) |

**`PRTCATBL`** — `app/jcl/PRTCATBL.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `DELDEF` | PGM=`IEFBR14` | `THEFILE`=AWS.M2.CARDDEMO.TCATBALF.REPT |
| 2 | `STEP05R` | PROC=`REPROC` | `PRC001.FILEIN`=AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS (read), `PRC001.FILEOUT`=AWS.M2.CARDDEMO.TCATBALF.BKUP(+1) (write) |
| 3 | `STEP10R` | PGM=`SORT` | `SORTIN`=AWS.M2.CARDDEMO.TCATBALF.BKUP(+1) (read), `SYMNAMES`=inline, `SYSIN`=inline, `SORTOUT`=AWS.M2.CARDDEMO.TCATBALF.REPT (write) |

**`READACCT`** — `app/jcl/READACCT.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `PREDEL` | PGM=`IEFBR14` | `DD01`=AWS.M2.CARDDEMO.ACCTDATA.PSCOMP, `DD02`=AWS.M2.CARDDEMO.ACCTDATA.ARRYPS, `DD03`=AWS.M2.CARDDEMO.ACCTDATA.VBPS |
| 2 | `STEP05` | PGM=`CBACT01C` | `ACCTFILE`=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (read), `OUTFILE`=AWS.M2.CARDDEMO.ACCTDATA.PSCOMP (write), `ARRYFILE`=AWS.M2.CARDDEMO.ACCTDATA.ARRYPS (write), `VBRCFILE`=AWS.M2.CARDDEMO.ACCTDATA.VBPS (write) |

**`READCARD`** — `app/jcl/READCARD.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`CBACT02C` | `CARDFILE`=AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS (read) |

**`READCUST`** — `app/jcl/READCUST.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`CBCUS01C` | `CUSTFILE`=AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS (read) |

**`READXREF`** — `app/jcl/READXREF.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`CBACT03C` | `XREFFILE`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read) |

**`REPTFILE`** — `app/jcl/REPTFILE.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |

**`TCATBALF`** — `app/jcl/TCATBALF.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP15` | PGM=`IDCAMS` | `TCATBAL`=AWS.M2.CARDDEMO.TCATBALF.PS (read), `TCATBALV`=AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS (read), `SYSIN`=inline |

**`TRANBKP`** — `app/jcl/TRANBKP.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05R` | PROC=`REPROC` | `PRC001.FILEIN`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `PRC001.FILEOUT`=AWS.M2.CARDDEMO.TRANSACT.BKUP(+1) (write) |
| 2 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |

**`TRANCATG`** — `app/jcl/TRANCATG.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP15` | PGM=`IDCAMS` | `TRANCATG`=AWS.M2.CARDDEMO.TRANCATG.PS (read), `TCATVSAM`=AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS (read), `SYSIN`=inline |

**`TRANFILE`** — `app/jcl/TRANFILE.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `CLCIFIL` | PGM=`SDSF` | `ISFIN`=inline |
| 2 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 4 | `STEP15` | PGM=`IDCAMS` | `TRANSACT`=AWS.M2.CARDDEMO.DALYTRAN.PS.INIT (read), `TRANVSAM`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `SYSIN`=inline |
| 5 | `STEP20` | PGM=`IDCAMS` | `SYSIN`=inline |
| 6 | `STEP25` | PGM=`IDCAMS` | `SYSIN`=inline |
| 7 | `STEP30` | PGM=`IDCAMS` | `SYSIN`=inline |
| 8 | `OPCIFIL` | PGM=`SDSF` | `ISFIN`=inline |

**`TRANIDX`** — `app/jcl/TRANIDX.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP20` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP25` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP30` | PGM=`IDCAMS` | `SYSIN`=inline |

**`TRANREPT`** — `app/jcl/TRANREPT.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05R` | PROC=`REPROC` | `PRC001.FILEIN`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `PRC001.FILEOUT`=AWS.M2.CARDDEMO.TRANSACT.BKUP(+1) (write) |
| 2 | `STEP05R` | PGM=`SORT` | `SORTIN`=AWS.M2.CARDDEMO.TRANSACT.BKUP(+1) (read), `SYMNAMES`=inline, `SYSIN`=inline, `SORTOUT`=AWS.M2.CARDDEMO.TRANSACT.DALY(+1) (write) |
| 3 | `STEP10R` | PGM=`CBTRN03C` | `TRANFILE`=AWS.M2.CARDDEMO.TRANSACT.DALY(+1) (read), `CARDXREF`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read), `TRANTYPE`=AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS (read), `TRANCATG`=AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS (read), `DATEPARM`=AWS.M2.CARDDEMO.DATEPARM (read), `TRANREPT`=AWS.M2.CARDDEMO.TRANREPT(+1) (write) |

**`TRANTYPE`** — `app/jcl/TRANTYPE.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP15` | PGM=`IDCAMS` | `TRANTYPE`=AWS.M2.CARDDEMO.TRANTYPE.PS (read), `TTYPVSAM`=AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS (read), `SYSIN`=inline |

**`TXT2PDF1`** — `app/jcl/TXT2PDF1.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `TXT2PDF` | PGM=`IKJEFT1B` | `INDD`=AWS.M2.CARDDEMO.STATEMNT.PS (read), `SYSTSIN`=inline |

**`WAITSTEP`** — `app/jcl/WAITSTEP.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `WAIT` | PGM=`COBSWAIT` | `SYSIN`=inline |

**`XREFFILE`** — `app/jcl/XREFFILE.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP05` | PGM=`IDCAMS` | `SYSIN`=inline |
| 2 | `STEP10` | PGM=`IDCAMS` | `SYSIN`=inline |
| 3 | `STEP15` | PGM=`IDCAMS` | `XREFDATA`=AWS.M2.CARDDEMO.CARDXREF.PS (read), `XREFVSAM`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read), `SYSIN`=inline |
| 4 | `STEP20` | PGM=`IDCAMS` | `SYSIN`=inline |
| 5 | `STEP25` | PGM=`IDCAMS` | `SYSIN`=inline |
| 6 | `STEP30` | PGM=`IDCAMS` | `SYSIN`=inline |

### 3.2 Procedures (`app/proc/`)


**`REPROC`** — `app/proc/REPROC.prc`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `PRC001` | PGM=`IDCAMS` | `FILEIN`=NULLFILE (read), `FILEOUT`=NULLFILE (read), `SYSIN`=&CNTLLIB(REPROCT)PEND (read) |

**`TRANREPT`** — `app/proc/TRANREPT.prc`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP01R` | PROC=`REPROC` | `PRC001.FILEIN`=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (read), `PRC001.FILEOUT`=AWS.M2.CARDDEMO.TRANSACT.BKUP(+1) (write) |
| 2 | `STEP05R` | PGM=`SORT` | `SORTIN`=AWS.M2.CARDDEMO.TRANSACT.BKUP(+1) (read), `SYMNAMES`=inline, `SYSIN`=inline, `SORTOUT`=AWS.M2.CARDDEMO.TRANSACT.DALY(+1) (write) |
| 3 | `STEP10R` | PGM=`CBTRN03C` | `TRANFILE`=AWS.M2.CARDDEMO.TRANSACT.DALY(+1) (read), `CARDXREF`=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (read), `TRANTYPE`=AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS (read), `TRANCATG`=AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS (read), `DATEPARM`=AWS.M2.CARDDEMO.DATEPARM (read), `TRANREPT`=AWS.M2.CARDDEMO.TRANREPT(+1)PEND (write) |

### 3.3 Extension jobs


**`CBPAUP0J`** — `app/app-authorization-ims-db2-mq/jcl/CBPAUP0J.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP01` | PGM=`DFSRRC00` | `STEPLIB (concat)`=XXXXXXXX.PROD.LOADLIB (read), `DFSRESLB`=IMS.SDFSRESL (read), `PROCLIB`=IMS.PROCLIB (read), `DFSSEL`=IMS.SDFSRESL (read), `IMS`=IMS.PSBLIB (read), `IMS (concat)`=IMS.DBDLIB (read), `SYSIN`=inline, `SYSOUX`=-, `IEFRDER`=DUMMY, `IMSLOGR`=DUMMY, `IMSERR`=- |

**`DBPAUTP0`** — `app/app-authorization-ims-db2-mq/jcl/DBPAUTP0.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEPDEL` | PGM=`IEFBR14` | `SYSUT1`=AWS.M2.CARDDEMO.IMSDATA.DBPAUTP0 |
| 2 | `UNLOAD` | PGM=`DFSRRC00` | `STEPLIB (concat)`=AWS.M2.CARDDEMO.LOADLIB (read), `DFSRESLB`=OEMA.IMS.IMSP.SDFSRESL (read), `IMS`=OEM.IMS.IMSP.PSBLIB (read), `IMS (concat)`=OEM.IMS.IMSP.DBDLIB (read), `DFSURGU1`=AWS.M2.CARDDEMO.IMSDATA.DBPAUTP0 (write), `DDPAUTP0`=OEM.IMS.IMSP.PAUTHDB (read), `DDPAUTX0`=OEM.IMS.IMSP.PAUTHDBX (read), `DFSVSAMP`=OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB) (read), `DFSCTL`=inline, `RECON1`=OEM.IMS.IMSP.RECON1 (read), `RECON2`=OEM.IMS.IMSP.RECON2 (read), `RECON3`=OEM.IMS.IMSP.RECON3 (read), `DFSWRK01`=DUMMY, `DFSSRT01`=DUMMY |

**`LOADPADB`** — `app/app-authorization-ims-db2-mq/jcl/LOADPADB.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP01` | PGM=`DFSRRC00` | `STEPLIB (concat)`=OEMA.IMS.IMSP.SDFSRESL.V151 (read), `STEPLIB (concat)`=AWS.M2.CARDDEMO.LOADLIB (read), `DFSRESLB`=OEMA.IMS.IMSP.SDFSRESL (read), `IMS`=OEM.IMS.IMSP.PSBLIB (read), `IMS (concat)`=OEM.IMS.IMSP.DBDLIB (read), `INFILE1`=AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO (read), `INFILE2`=AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO (read), `DFSVSAMP`=OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB) (read), `IMSLOGR`=DUMMY, `IEFRDER`=DUMMY, `IMSERR`=- |

**`UNLDGSAM`** — `app/app-authorization-ims-db2-mq/jcl/UNLDGSAM.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP01` | PGM=`DFSRRC00` | `STEPLIB (concat)`=OEMA.IMS.IMSP.SDFSRESL.V151 (read), `STEPLIB (concat)`=AWS.M2.CARDDEMO.LOADLIB (read), `DFSRESLB`=OEMA.IMS.IMSP.SDFSRESL (read), `IMS`=OEM.IMS.IMSP.PSBLIB (read), `IMS (concat)`=OEM.IMS.IMSP.DBDLIB (read), `PASFILOP`=AWS.M2.CARDDEMO.PAUTDB.ROOT.GSAM, `PADFILOP`=AWS.M2.CARDDEMO.PAUTDB.CHILD.GSAM, `DDPAUTP0`=OEM.IMS.IMSP.PAUTHDB (read), `DDPAUTX0`=OEM.IMS.IMSP.PAUTHDBX (read), `DFSVSAMP`=OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB) (read), `IMSLOGR`=DUMMY, `IEFRDER`=DUMMY, `IMSERR`=- |

**`UNLDPADB`** — `app/app-authorization-ims-db2-mq/jcl/UNLDPADB.JCL`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP0` | PGM=`IEFBR14` | `SYSDUMP`=-, `DD1`=AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO, `DD2`=AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO |
| 2 | `STEP01` | PGM=`DFSRRC00` | `STEPLIB (concat)`=OEMA.IMS.IMSP.SDFSRESL.V151 (read), `STEPLIB (concat)`=AWS.M2.CARDDEMO.LOADLIB (read), `DFSRESLB`=OEMA.IMS.IMSP.SDFSRESL (read), `IMS`=OEM.IMS.IMSP.PSBLIB (read), `IMS (concat)`=OEM.IMS.IMSP.DBDLIB (read), `OUTFIL1`=AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO (write), `OUTFIL2`=AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO (write), `DDPAUTP0`=OEM.IMS.IMSP.PAUTHDB (read), `DDPAUTX0`=OEM.IMS.IMSP.PAUTHDBX (read), `DFSVSAMP`=OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB) (read), `IMSLOGR`=DUMMY, `IEFRDER`=DUMMY, `IMSERR`=- |

**`CREADB21`** — `app/app-transaction-type-db2/jcl/CREADB21.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `FREEPLN` | PGM=`IKJEFT01` | `STEPLIB (concat)`=OEMA.DB2.VERSIONA.SDSNLOAD (read), `SYSTSIN`=&LBNM..CNTL(DB2FREE) (read) |
| 2 | `CRCRDDB` | PGM=`IKJEFT01` | `STEPLIB (concat)`=OEMA.DB2.VERSIONA.SDSNLOAD (read), `SYSTSIN`=&LBNM..CNTL(DB2TIAD1) (read), `SYSIN`=&LBNM..CNTL(DB2CREAT) (read) |
| 3 | `LDTTYPE` | PGM=`IEFBR14` | - |
| 4 | `RUNTEP2` | PGM=`IKJEFT01` | `STEPLIB (concat)`=OEMA.DB2.VERSIONA.SDSNLOAD (read), `SYSTSIN`=&LBNM..CNTL(DB2TEP41) (read), `SYSIN`=&LBNM..CNTL(DB2LTTYP) (read) |
| 5 | `LDTCCAT` | PGM=`IKJEFT01` | `STEPLIB (concat)`=OEMA.DB2.VERSIONA.SDSNLOAD (read), `SYSTSIN`=&LBNM..CNTL(DB2TEP41) (read), `SYSIN`=&LBNM..CNTL(DB2LTCAT) (read) |

**`MNTTRDB2`** — `app/app-transaction-type-db2/jcl/MNTTRDB2.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP1` | PGM=`IKJEFT01` | `STEPLIB (concat)`=OEMA.DB2.VERSIONA.SDSNLOAD (read), `STEPLIB (concat)`=AWS.M2.CARDDEMO.LOADLIB (read), `DBRMLIB`=AWS.M2.CARDDEMO.DBRMLIB (read), `INPFILE`=INPFILE (read), `SYSTSIN`=inline |

**`TRANEXTR`** — `app/app-transaction-type-db2/jcl/TRANEXTR.jcl`

| # | Step | Program / proc | DD datasets (read/written) |
|---|---|---|---|
| 1 | `STEP10` | PGM=`IEBGENER` | `SYSIN`=DUMMY, `SYSUT1`=&HLQ..TRANTYPE.PS (read), `SYSUT2`=&HLQ..TRANTYPE.BKUP(+1) (write) |
| 2 | `STEP20` | PGM=`IEBGENER` | `SYSIN`=DUMMY, `SYSUT1`=&HLQ..TRANCATG.PS (read), `SYSUT2`=&HLQ..TRANCATG.PS.BKUP(+1) (write) |
| 3 | `STEP30` | PGM=`IEFBR14` | `DD01`=&HLQ..TRANTYPE.PS, `DD02`=&HLQ..TRANCATG.PS |
| 4 | `STEP40` | PGM=`IKJEFT01` | `STEPLIB (concat)`=OEMA.DB2.VERSIONA.SDSNLOAD (read), `SYSPUNCH`=DUMMY, `SYSREC00`=&HLQ..TRANTYPE.PS (write), `SYSIN`=inline, `SYSTSIN`=inline |
| 5 | `STEP50` | PGM=`IKJEFT01` | `STEPLIB (concat)`=OEMA.DB2.VERSIONA.SDSNLOAD (read), `SYSPUNCH`=DUMMY, `SYSREC00`=&HLQ..TRANCATG.PS (write), `SYSIN`=inline, `SYSTSIN`=inline |

### 3.4 Findings from the JCL/COBOL cross-check

- `CBTRN01C` is never executed: no `EXEC PGM=CBTRN01C` card exists in `app/jcl/` or in any extension JCL, and no scheduler definition references it. Its validation logic is duplicated inside `CBTRN02C`.

- `app/jcl/CBIMPORT.jcl` allocates `EXPFILE`, `CUSTOUT`, `ACCTOUT`, `XREFOUT`, `TRNXOUT` and `ERROUT`, but `CBIMPORT.cbl` also `SELECT`s `CARDOUT`; there is no `CARDOUT` DD in the job.

- `CLOSEFIL`/`OPENFIL` each contain a single `SDSF` step whose in-stream commands are `/F CICSAWSA,'CEMT SET FIL(TRANSACT|CCXREF|ACCTDAT|CXACAIX|USRSEC ) CLO'` and the matching `OPE`. `CARDFILE`, `CUSTFILE` and `TRANFILE` embed the same close (`CLCIFIL`) and open (`OPCIFIL`) steps around their own reload steps, so a reload of those clusters is self-contained.


### 3.5 Utilities used

| Utility | Used for | Jobs |
|---|---|---|
| `IDCAMS` | `DELETE`/`DEFINE CLUSTER`, `DEFINE ALTERNATEINDEX`/`DEFINE PATH`/`BLDINDEX`, `REPRO` loads, GDG bases | ACCTFILE, CARDFILE, CBEXPORT, COMBTRAN, CREASTMT, CUSTFILE, DALYREJS, DEFCUST, DEFGDGB, DEFGDGD, DISCGRP, DUSRSECJ, ESDSRRDS, INTRDRJ1, INTRDRJ2, REPROC, REPTFILE, TCATBALF, TRANBKP, TRANCATG, TRANFILE, TRANIDX, TRANTYPE, XREFFILE |
| `IEBGENER` | Copy PS to PS, GDG backups, submit JCL through the internal reader | DEFGDGD, DUSRSECJ, ESDSRRDS, INTRDRJ1, TRANEXTR |
| `SORT` | Sort transaction data before load or report | COMBTRAN, CREASTMT, PRTCATBL, TRANREPT |
| `IEFBR14` | Allocate / pre-delete output data sets | CREADB21, CREASTMT, DBPAUTP0, DUSRSECJ, ESDSRRDS, PRTCATBL, READACCT, TRANEXTR, UNLDPADB |
| `SDSF` | Issue CICS `CEMT SET FILE CLOSE/OPEN` commands | CARDFILE, CLOSEFIL, CUSTFILE, OPENFIL, TRANFILE |
| `DFHCSDUP` | Load the CardDemo CICS CSD group | CBADMCDJ |
| `DFSRRC00` | IMS region controller (BMP / DLI) | CBPAUP0J, DBPAUTP0, LOADPADB, UNLDGSAM, UNLDPADB |
| `IKJEFT01` | TSO batch: DB2 `DSN RUN`, DSNTEP2, unload | CREADB21, MNTTRDB2, TRANEXTR |
| `IKJEFT1B` | TSO batch: TXT2PDF conversion | TXT2PDF1 |
| `FTP` | Send statement output off-platform | FTPJCL |

Application programs are executed by exactly one job each (`CBACT01C`->READACCT, `CBACT02C`->READCARD, `CBACT03C`->READXREF, `CBCUS01C`->READCUST, `CBACT04C`->INTCALC, `CBTRN02C`->POSTTRAN, `CBTRN03C`->TRANREPT, `CBSTM03A`->CREASTMT, `CBEXPORT`->CBEXPORT, `CBIMPORT`->CBIMPORT, `COBSWAIT`->WAITSTEP), and `PROC=REPROC` is invoked by PRTCATBL, TRANBKP and TRANREPT.


### 3.6 Scheduler definitions

`app/scheduler/CardDemo.controlm` (Control-M XML) defines four folders:

| Folder | Job order |
|---|---|
| `DAILY-TransactionBackup` | CLOSEFIL -> TRANBKP -> WAITSTEP -> OPENFIL |
| `WEEKLY-TransactionTypesDBRefresh` | MNTTRDB2 -> (DisclosureGroupsRefresh, TRANEXTR) |
| `WEEKLY-DisclosureGroupsRefresh` | CLOSEFIL -> DISCGRP -> WAITSTEP -> OPENFIL |
| `MONTHLY-InterestCalculation` | CLOSEFIL -> INTCALC -> COMBTRAN -> WAITSTEP -> OPENFIL |

`app/scheduler/CardDemo.ca7` holds CA-7 `LJOB` listings with `TRIGGERED JOBS` chains:

- SCHID 030 authorization/posting chain: `CLOSEFIL -> CBPAUP0J -> POSTTRAN -> WAITSTEP -> OPENFIL`

- Reference-data chain: `CLOSEFIL -> TRANTYPE -> WAITSTEP`, then `CLOSEFIL1 -> TRANCATG -> WAITSTEP` (SCHID 031) and `CLOSEFIL2 -> TCATBALF -> WAITSTEP` (SCHID 032)

- Master-file read chain: `CLOSEFIL -> READACCT -> READCARD -> READCUST -> READXREF -> WAITSTEP -> OPENFIL`

- Statement chain: `CLOSEFIL -> CREASTMT -> TXT2PDF1 -> WAITSTEP -> OPENFIL`

- Category-balance report chain (SCHID 031): `CLOSEFIL -> PRTCATBL -> WAITSTEP -> OPENFIL`

