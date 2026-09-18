# CardDemo Application Inventory

Related documents: [DEPENDENCY_MAP](./DEPENDENCY_MAP.md), [DATA_DICTIONARY](./DATA_DICTIONARY.md), [HOTSPOT_REPORT](./HOTSPOT_REPORT.md).

Generated from static analysis of `app/` on branch develop-asiri; extraction rules are described in HOTSPOT_REPORT.md §1.

Static inventory of the AWS CardDemo mainframe estate: the base COBOL/CICS/VSAM application under `app/` plus three extension sub-apps (`app-authorization-ims-db2-mq`, `app-transaction-type-db2`, `app-vsam-mq`). IBM-supplied copybooks are marked `*`.

## 1. Overview

| Sub-app | Programs (online/batch) | Copybooks | JCL jobs | BMS mapsets | CSD files | DDL | IMS defs | ASM |
|---|---|---|---|---|---|---|---|---|
| base | 31 (17 online / 14 batch) | 47 | 38 | 17 | 1 | 0 | 0 | 2 |
| authorization | 8 (4 online / 4 batch) | 12 | 5 | 2 | 1 | 2 | 8 | 0 |
| trantype | 3 (2 online / 1 batch) | 6 | 3 | 2 | 1 | 4 | 0 | 0 |
| vsam-mq | 2 (2 online / 0 batch) | 0 | 0 | 0 | 1 | 0 | 0 | 0 |
| **Total** | **44** | **65** | **46** | **21** | **4** | **6** | **8** | **2** |

**Programs never referenced by CSD or JCL:** CBPAUP0C, CBSTM03B, CBTRN01C, COBTUPDT, COPAUS2C, CSUTLDTC, DBUNLDGS, PAUDBLOD, PAUDBUNL

## 2. Programs

### Sub-app: base

**Online**

| Program | File | Tran / Job | Purpose | Stores | Files read | Files written | DB2 tables | Copybooks |
|---|---|---|---|---|---|---|---|---|
| COACTUPC | `app/cbl/COACTUPC.cbl` | CICS CAUP | Accept and process ACCOUNT UPDATE | VSAM | ACCTDAT, CUSTDAT, CXACAIX | ACCTDAT, CUSTDAT | - | CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA*, DFHAID*, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY, CSSTRPFY, CSUTLDPY |
| COACTVWC | `app/cbl/COACTVWC.cbl` | CICS CAVW | Accept and process Account View request | VSAM | ACCTDAT, CUSTDAT, CXACAIX | - | - | CVCRD01Y, COCOM01Y, DFHBMSCA*, DFHAID*, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| COADM01C | `app/cbl/COADM01C.cbl` | CICS CA00 | Admin Menu for Admin users | - | - | - | - | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID*, DFHBMSCA* |
| COBIL00C | `app/cbl/COBIL00C.cbl` | CICS CB00 | Bill Payment - Pay account balance in full and a tractionsaction for the online bill payment. | VSAM | ACCTDAT, CXACAIX, TRANSACT | ACCTDAT, TRANSACT | - | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID*, DFHBMSCA* |
| COCRDLIC | `app/cbl/COCRDLIC.cbl` | CICS CCLI | List Credit Cards a) All cards if no context passed and admin user b) Only the ones associated with ACCT in COMMAREA if user is not admin | VSAM | CARDDAT | - | - | CVCRD01Y, COCOM01Y, DFHBMSCA*, DFHAID*, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY |
| COCRDSLC | `app/cbl/COCRDSLC.cbl` | CICS CCDL | Accept and process credit card detail request | VSAM | CARDAIX, CARDDAT | - | - | CVCRD01Y, COCOM01Y, DFHBMSCA*, DFHAID*, COTTL01Y, COCRDSL, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| COCRDUPC | `app/cbl/COCRDUPC.cbl` | CICS CCUP | Accept and process credit card detail request | VSAM | CARDDAT | CARDDAT | - | CVCRD01Y, COCOM01Y, DFHBMSCA*, DFHAID*, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| COMEN01C | `app/cbl/COMEN01C.cbl` | CICS CM00 | Main Menu for the Regular users | - | - | - | - | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID*, DFHBMSCA* |
| CORPT00C | `app/cbl/CORPT00C.cbl` | CICS CR00 | Print Transaction reports by submitting batch job from online using extra partition TDQ. | - | - | - | - | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID*, DFHBMSCA* |
| COSGN00C | `app/cbl/COSGN00C.cbl` | CICS CC00 | Signon Screen for the CardDemo Application | VSAM | USRSEC | - | - | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID*, DFHBMSCA* |
| COTRN00C | `app/cbl/COTRN00C.cbl` | CICS CT00 | List Transactions from TRANSACT file | VSAM | TRANSACT | - | - | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID*, DFHBMSCA* |
| COTRN01C | `app/cbl/COTRN01C.cbl` | CICS CT01 | View a Transaction from TRANSACT file [READ UPDATE on TRANSACT with no REWRITE (lock held, no update)] | VSAM | TRANSACT | TRANSACT | - | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID*, DFHBMSCA* |
| COTRN02C | `app/cbl/COTRN02C.cbl` | CICS CT02 | Add a new Transaction to TRANSACT file | VSAM | CCXREF, CXACAIX, TRANSACT | TRANSACT | - | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID*, DFHBMSCA* |
| COUSR00C | `app/cbl/COUSR00C.cbl` | CICS CU00 | List all users from USRSEC file | VSAM | USRSEC | - | - | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID*, DFHBMSCA* |
| COUSR01C | `app/cbl/COUSR01C.cbl` | CICS CU01 | Add a new Regular/Admin user to USRSEC file | VSAM | - | USRSEC | - | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID*, DFHBMSCA* |
| COUSR02C | `app/cbl/COUSR02C.cbl` | CICS CU02 | Update a user in USRSEC file | VSAM | USRSEC | USRSEC | - | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID*, DFHBMSCA* |
| COUSR03C | `app/cbl/COUSR03C.cbl` | CICS CU03 | Delete a user from USRSEC file | VSAM | USRSEC | USRSEC | - | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID*, DFHBMSCA* |

**Batch**

| Program | File | Tran / Job | Purpose | Stores | Files read | Files written | DB2 tables | Copybooks |
|---|---|---|---|---|---|---|---|---|
| CBACT01C | `app/cbl/CBACT01C.cbl` | JCL READACCT | READ THE ACCOUNT FILE AND WRITE INTO FILES. | VSAM | ACCTFILE | ARRYFILE, OUTFILE, VBRCFILE | - | CVACT01Y, CODATECN |
| CBACT02C | `app/cbl/CBACT02C.cbl` | JCL READCARD | Read and print card data file. | VSAM | CARDFILE | - | - | CVACT02Y |
| CBACT03C | `app/cbl/CBACT03C.cbl` | JCL READXREF | Read and print account cross reference data file. | VSAM | XREFFILE | - | - | CVACT03Y |
| CBACT04C | `app/cbl/CBACT04C.cbl` | JCL INTCALC | This is a interest calculator program. | VSAM | ACCTFILE, DISCGRP, TCATBALF, XREFFILE | ACCTFILE, TRANSACT | - | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| CBCUS01C | `app/cbl/CBCUS01C.cbl` | JCL READCUST | Read and print customer data file. | VSAM | CUSTFILE | - | - | CVCUS01Y |
| CBEXPORT | `app/cbl/CBEXPORT.cbl` | JCL CBEXPORT | Export Customer Data for Branch Migration Reads normalized CardDemo files and creates multi-record export file for data migration | VSAM | ACCTFILE, CARDFILE, CUSTFILE, TRANSACT, XREFFILE | EXPFILE | - | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| CBIMPORT | `app/cbl/CBIMPORT.cbl` | JCL CBIMPORT | Import Customer Data from Branch Migration Export Reads multi-record export file and splits it into separate normalized target files (CUSTOMER, ACCOUN CARD-XREF, TRANSACTION) with validation | VSAM | EXPFILE | ERROUT | - | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| CBSTM03A | `app/cbl/CBSTM03A.CBL` | JCL CREASTMT | Print Account Statements from Transaction data in two formats : 1/plain text and 2/HTML | VSAM | - | HTMLFILE, STMTFILE | - | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| CBSTM03B | `app/cbl/CBSTM03B.CBL` | - | Does file processing related to Transact Report | VSAM | ACCTFILE, CUSTFILE, TRNXFILE, XREFFILE | - | - |  |
| CBTRN01C | `app/cbl/CBTRN01C.cbl` | - | Post the records from daily transaction file. | VSAM | ACCTFILE, CARDFILE, CUSTFILE, DALYTRAN, TRANFILE, XREFFILE | - | - | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| CBTRN02C | `app/cbl/CBTRN02C.cbl` | JCL POSTTRAN | Post the records from daily transaction file. | VSAM | ACCTFILE, DALYTRAN, TCATBALF, XREFFILE | ACCTFILE, DALYREJS, TCATBALF, TRANFILE | - | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| CBTRN03C | `app/cbl/CBTRN03C.cbl` | JCL TRANREPT | Print the transaction detail report. | VSAM | CARDXREF, DATEPARM, TRANCATG, TRANFILE, TRANTYPE | TRANREPT | - | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| COBSWAIT | `app/cbl/COBSWAIT.cbl` | JCL WAITSTEP | UTILITY PROGRAM TO WAIT (PARM IN CENTISECONDS) | - | - | - | - |  |
| CSUTLDTC | `app/cbl/CSUTLDTC.cbl` | - | Batch utility: date conversion via CEEDAYS (called by CORPT00C/COTRN02C) | - | - | - | - |  |

### Sub-app: authorization

**Online**

| Program | File | Tran / Job | Purpose | Stores | Files read | Files written | DB2 tables | Copybooks |
|---|---|---|---|---|---|---|---|---|
| COPAUA0C | `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | CICS CP00 | Card Authorization Decision Program | VSAM, IMS, MQ | ACCTDAT, CCXREF, CUSTDAT | - | - | CMQODV*, CMQMDV*, CMQV*, CMQTML*, CMQPMOV*, CMQGMOV*, CCPAURQY, CCPAURLY, CCPAUERY, CIPAUSMY, CIPAUDTY, CVACT03Y, CVACT01Y, CVCUS01Y |
| COPAUS0C | `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | CICS CPVS | Summary View of Authoriation Messages | VSAM, IMS | ACCTDAT, CUSTDAT, CXACAIX | - | - | COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CIPAUSMY, CIPAUDTY, DFHAID*, DFHBMSCA* |
| COPAUS1C | `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl` | CICS CPVD | Detail View of Authorization Message | IMS | - | - | - | COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CIPAUSMY, CIPAUDTY, DFHAID*, DFHBMSCA* |
| COPAUS2C | `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl` | - | Mark Authorization Message Fraud | DB2 | - | - | CARDDEMO.AUTHFRDS | CIPAUDTY, SQLCA*, AUTHFRDS |

**Batch**

| Program | File | Tran / Job | Purpose | Stores | Files read | Files written | DB2 tables | Copybooks |
|---|---|---|---|---|---|---|---|---|
| CBPAUP0C | `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl` | - | Delete Expired Pending Authoriation Messages | IMS | - | - | - | CIPAUSMY, CIPAUDTY |
| DBUNLDGS | `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL` | - | Batch: unload pending-authorization IMS GSAM database to sequential file | IMS | - | - | - | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB, PASFLPCB, PADFLPCB |
| PAUDBLOD | `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL` | - | Batch: load pending-authorization IMS database (PAUTDB) from unload file | VSAM, IMS | INFILE1, INFILE2 | - | - | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |
| PAUDBUNL | `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL` | - | Batch: unload pending-authorization IMS database (PAUTDB) to sequential files | VSAM, IMS | - | OUTFIL1, OUTFIL2 | - | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |

### Sub-app: trantype

**Online**

| Program | File | Tran / Job | Purpose | Stores | Files read | Files written | DB2 tables | Copybooks |
|---|---|---|---|---|---|---|---|---|
| COTRTLIC | `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` | CICS CTLI | List Transaction Type for updates and deletes Demonstrates paging with cursors in Db2 and Simple, select, delete and update use cases | DB2 | - | - | CARDDEMO.TRANSACTION_TYPE | CVCRD01Y, COCOM01Y, DFHBMSCA*, DFHAID*, COTTL01Y, COTRTLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY, CSDB2RWY, SQLCA*, DCLTRTYP, CSDB2RPY |
| COTRTUPC | `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` | CICS CTTU | Accept and process TRANSACTION TYPE UPDATE | DB2 | - | - | CARDDEMO.TRANSACTION_TYPE | CSUTLDWY, CVCRD01Y, DFHBMSCA*, DFHAID*, COTTL01Y, COTRTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, COCOM01Y, CSSETATY, CSSTRPFY, SQLCA*, DCLTRTYP, DCLTRCAT |

**Batch**

| Program | File | Tran / Job | Purpose | Stores | Files read | Files written | DB2 tables | Copybooks |
|---|---|---|---|---|---|---|---|---|
| COBTUPDT | `app/app-transaction-type-db2/cbl/COBTUPDT.cbl` | - | Update Transaction type based on user input | VSAM, DB2 | INPFILE | - | CARDDEMO.TRANSACTION_TYPE | SQLCA*, DCLTRTYP |

### Sub-app: vsam-mq

**Online**

| Program | File | Tran / Job | Purpose | Stores | Files read | Files written | DB2 tables | Copybooks |
|---|---|---|---|---|---|---|---|---|
| COACCT01 | `app/app-vsam-mq/cbl/COACCT01.cbl` | CICS CDRA | Inquire account details via MQ | VSAM, MQ | ACCTDAT | - | - | CMQGMOV*, CMQPMOV*, CMQMDV*, CMQODV*, CMQV*, CMQTML*, CVACT01Y |
| CODATE01 | `app/app-vsam-mq/cbl/CODATE01.cbl` | CICS CDRD | Inquire System Date via MQ | MQ | - | - | - | CMQGMOV*, CMQPMOV*, CMQMDV*, CMQODV*, CMQV*, CMQTML* |

## 3. JCL jobs

| Job | File | Steps (STEP:PGM/PROC) | Inputs (DSN) | Outputs (DSN) | Purpose |
|---|---|---|---|---|---|
| CBPAUP0J | `app/app-authorization-ims-db2-mq/jcl/CBPAUP0J.jcl` | STEP01:DFSRRC00(BMP) | IMS.SDFSRESL, IMS.PROCLIB, IMS.PSBLIB | - | Purge Expired Authorizations |
| DBPAUTP0 | `app/app-authorization-ims-db2-mq/jcl/DBPAUTP0.jcl` | STEPDEL:IEFBR14 → UNLOAD:DFSRRC00((ULU) | AWS.M2.CARDDEMO.IMSDATA.DBPAUTP0, OEMA.IMS.IMSP.SDFSRESL, OEM.IMS.IMSP.PSBLIB, OEM.IMS.IMSP.PAUTHDB, OEM.IMS.IMSP.PAUTHDBX, OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB), OEM.IMS.IMSP.RECON1, OEM.IMS.IMSP.RECON2, OEM.IMS.IMSP.RECON3 | - | IMS DB load / unload utilities for pending-authorization DB (PAUTDB) |
| LOADPADB | `app/app-authorization-ims-db2-mq/jcl/LOADPADB.JCL` | STEP01:DFSRRC00(BMP) | OEMA.IMS.IMSP.SDFSRESL, OEM.IMS.IMSP.PSBLIB, AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO, AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO, OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB) | - | IMS DB load / unload utilities for pending-authorization DB (PAUTDB) |
| UNLDGSAM | `app/app-authorization-ims-db2-mq/jcl/UNLDGSAM.JCL` | STEP01:DFSRRC00(DLI) | OEMA.IMS.IMSP.SDFSRESL, OEM.IMS.IMSP.PSBLIB, AWS.M2.CARDDEMO.PAUTDB.ROOT.GSAM, AWS.M2.CARDDEMO.PAUTDB.CHILD.GSAM, OEM.IMS.IMSP.PAUTHDB, OEM.IMS.IMSP.PAUTHDBX, OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB) | - | IMS DB load / unload utilities for pending-authorization DB (PAUTDB) |
| UNLDPADB | `app/app-authorization-ims-db2-mq/jcl/UNLDPADB.JCL` | STEP0:IEFBR14 → STEP01:DFSRRC00(DLI) | AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO, AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO, OEMA.IMS.IMSP.SDFSRESL, OEM.IMS.IMSP.PSBLIB, OEM.IMS.IMSP.PAUTHDB, OEM.IMS.IMSP.PAUTHDBX, OEMPP.IMS.V15R01MB.PROCLIB(DFSVSMDB) | AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO, AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO | IMS DB load / unload utilities for pending-authorization DB (PAUTDB) |
| ACCTFILE | `app/jcl/ACCTFILE.jcl` | STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS | AWS.M2.CARDDEMO.ACCTDATA.PS | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | Refresh Account Master |
| CARDFILE | `app/jcl/CARDFILE.jcl` | CLCIFIL:SDSF → STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS → STEP40:IDCAMS → STEP50:IDCAMS → STEP60:IDCAMS → OPCIFIL:SDSF | AWS.M2.CARDDEMO.CARDDATA.PS, AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS, AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX | Refresh Card Master |
| CBADMCDJ | `app/jcl/CBADMCDJ.jcl` | STEP1:DFHCSDUP(CSD(READWRITE)) | OEM.CICSTS.V05R06M0.CICS.SDFHLOAD, OEM.CICSTS.DFHCSD | - | Create Resources for Card Demo application |
| CBEXPORT | `app/jcl/CBEXPORT.jcl` | STEP01:IDCAMS → STEP02:CBEXPORT | AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS, AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS, AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS, AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS, AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS, AWS.M2.CARDDEMO.EXPORT.DATA | - | EXPORT CUSTOMER DATA FROM VSAM FILES TO MULTI-RECORD EXPORT FILE |
| CBIMPORT | `app/jcl/CBIMPORT.jcl` | STEP01:CBIMPORT | AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.EXPORT.DATA | AWS.M2.CARDDEMO.CUSTDATA.IMPORT, AWS.M2.CARDDEMO.ACCTDATA.IMPORT, AWS.M2.CARDDEMO.CARDXREF.IMPORT, AWS.M2.CARDDEMO.TRANSACT.IMPORT, AWS.M2.CARDDEMO.IMPORT.ERRORS | IMPORT CUSTOMER DATA FROM MULTI-RECORD EXPORT FILE AND SPLIT |
| CLOSEFIL | `app/jcl/CLOSEFIL.jcl` | CLCIFIL:SDSF | - | - | Close VSAM files in CICS |
| COMBTRAN | `app/jcl/COMBTRAN.jcl` | STEP05R:SORT → STEP10:IDCAMS | AWS.M2.CARDDEMO.TRANSACT.BKUP(0), AWS.M2.CARDDEMO.TRANSACT.COMBINED(+1) | AWS.M2.CARDDEMO.TRANSACT.COMBINED(+1), AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | Combine transaction files |
| CREASTMT | `app/jcl/CREASTMT.JCL` | DELDEF01:IDCAMS → STEP010:SORT → STEP020:IDCAMS → STEP030:IEFBR14 → STEP040:CBSTM03A | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS, AWS.M2.CARDDEMO.TRXFL.SEQ, AWS.M2.CARDDEMO.STATEMNT.HTML, AWS.M2.CARDDEMO.STATEMNT.PS, AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS, AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS, AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS, AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | AWS.M2.CARDDEMO.TRXFL.SEQ, AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS, AWS.M2.CARDDEMO.STATEMNT.PS, AWS.M2.CARDDEMO.STATEMNT.HTML | Produce transaction statement |
| CUSTFILE | `app/jcl/CUSTFILE.jcl` | CLCIFIL:SDSF → STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS → OPCIFIL:SDSF | AWS.M2.CARDDEMO.CUSTDATA.PS | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | Refresh Customer Master |
| DALYREJS | `app/jcl/DALYREJS.jcl` | STEP05:IDCAMS | - | - | DELETE TRANSACATION MASTER VSAM FILE IF ONE ALREADY EXISTS |
| DEFCUST | `app/jcl/DEFCUST.jcl` | STEP05:IDCAMS → STEP05:IDCAMS | - | - | DELETE CUSTOMER VSAM FILE IF ONE ALREADY EXISTS |
| DEFGDGB | `app/jcl/DEFGDGB.jcl` | STEP05:IDCAMS | - | - | Setup GDG Bases |
| DEFGDGD | `app/jcl/DEFGDGD.jcl` | STEP10:IDCAMS → STEP20:IEBGENER → STEP30:IDCAMS → STEP40:IEBGENER → STEP50:IDCAMS → STEP60:IEBGENER | AWS.M2.CARDDEMO.TRANTYPE.PS, AWS.M2.CARDDEMO.TRANCATG.PS, AWS.M2.CARDDEMO.DISCGRP.PS | AWS.M2.CARDDEMO.TRANTYPE.BKUP(+1), AWS.M2.CARDDEMO.TRANCATG.PS.BKUP(+1), AWS.M2.CARDDEMO.DISCGRP.BKUP(+1) | Setup more GDG Bases for Db2 |
| DISCGRP | `app/jcl/DISCGRP.jcl` | STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS | AWS.M2.CARDDEMO.DISCGRP.PS | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | Load Disclosure Group File |
| DUSRSECJ | `app/jcl/DUSRSECJ.jcl` | PREDEL:IEFBR14 → STEP01:IEBGENER → STEP02:IDCAMS → STEP03:IDCAMS | AWS.M2.CARDDEMO.USRSEC.PS | AWS.M2.CARDDEMO.USRSEC.PS, AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | Initial Load of User security file |
| ESDSRRDS | `app/jcl/ESDSRRDS.jcl` | PREDEL:IEFBR14 → STEP01:IEBGENER → STEP02:IDCAMS → STEP03:IDCAMS → STEP04:IDCAMS → STEP05:IDCAMS | AWS.M2.CARDDEMO.ESDSRRDS.PS | AWS.M2.CARDDEMO.ESDSRRDS.PS, AWS.M2.CARDDEMO.USRSEC.VSAM.ESDS, AWS.M2.CARDDEMO.USRSEC.VSAM.RRDS | Create ESDS and RRDS VSAM files |
| FTPJCLS | `app/jcl/FTPJCL.JCL` | STEP1:FTP | - | - | FTP JOB TO RECEIVE A FILE |
| INTCALC | `app/jcl/INTCALC.jcl` | STEP15:CBACT04C(2022071800) | AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS, AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS, AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH, AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS, AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | AWS.M2.CARDDEMO.SYSTRAN(+1) | Run interest calculations |
| INTRDRJ1 | `app/jcl/INTRDRJ1.JCL` | IDCAMS:IDCAMS → STEP01:IEBGENER | AWS.M2.CARDEMO.FTP.TEST, AWS.M2.CARDEMO.FTP.TEST.BKUP, AWS.M2.CARDDEMO.JCL(INTRDRJ2) | - | THIS INTRDR JOB WILL TRIGGER ANOTHER JCL |
| INTRDRJ2 | `app/jcl/INTRDRJ2.JCL` | IDCAMS:IDCAMS | AWS.M2.CARDEMO.FTP.TEST.BKUP, AWS.M2.CARDEMO.FTP.TEST.BKUP.INTRDR | - | THIS JOB IS TO CREATE PHYSICAL VSAM FILE FOR IMS DEMODB |
| OPENFIL | `app/jcl/OPENFIL.jcl` | OPCIFIL:SDSF | - | - | Open files in CICS |
| POSTTRAN | `app/jcl/POSTTRAN.jcl` | STEP15:CBTRN02C | AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS, AWS.M2.CARDDEMO.DALYTRAN.PS, AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS, AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS, AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | AWS.M2.CARDDEMO.DALYREJS(+1) | Transaction processing job |
| PRTCATBL | `app/jcl/PRTCATBL.jcl` | DELDEF:IEFBR14 → STEP05R:REPROC(proc) → STEP10R:SORT | AWS.M2.CARDDEMO.TCATBALF.REPT, AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS, AWS.M2.CARDDEMO.CNTL(REPROCT), AWS.M2.CARDDEMO.TCATBALF.BKUP(+1) | AWS.M2.CARDDEMO.TCATBALF.BKUP(+1), AWS.M2.CARDDEMO.TCATBALF.REPT |  |
| READACCT | `app/jcl/READACCT.jcl` | PREDEL:IEFBR14 → STEP05:CBACT01C | AWS.M2.CARDDEMO.ACCTDATA.PSCOMP, AWS.M2.CARDDEMO.ACCTDATA.ARRYPS, AWS.M2.CARDDEMO.ACCTDATA.VBPS, AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | AWS.M2.CARDDEMO.ACCTDATA.PSCOMP, AWS.M2.CARDDEMO.ACCTDATA.ARRYPS, AWS.M2.CARDDEMO.ACCTDATA.VBPS | PRE DELETE STEP |
| READCARD | `app/jcl/READCARD.jcl` | STEP05:CBACT02C | AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | - | RUN THE PROGRAM THAT READS THE CARD MASTER VSAM FILE |
| READCUST | `app/jcl/READCUST.jcl` | STEP05:CBCUS01C | AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | - | RUN THE PROGRAM THAT READS THE CUSTOMER MASTER VSAM FILE |
| READXREF | `app/jcl/READXREF.jcl` | STEP05:CBACT03C | AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | - | RUN THE PROGRAM THAT READS THE XREF MASTER VSAM FILE |
| REPTFILE | `app/jcl/REPTFILE.jcl` | STEP05:IDCAMS | - | - | DELETE TRANSACATION MASTER VSAM FILE IF ONE ALREADY EXISTS |
| TCATBALF | `app/jcl/TCATBALF.jcl` | STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS | AWS.M2.CARDDEMO.TCATBALF.PS | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | Refresh Transaction Category Balance |
| TRANBKP | `app/jcl/TRANBKP.jcl` | STEP05R:REPROC(proc) → STEP05:IDCAMS → STEP10:IDCAMS | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS, AWS.M2.CARDDEMO.CNTL(REPROCT) | AWS.M2.CARDDEMO.TRANSACT.BKUP(+1) | Refresh Transaction Master |
| TRANCATG | `app/jcl/TRANCATG.jcl` | STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS | AWS.M2.CARDDEMO.TRANCATG.PS | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | Load Transaction category types |
| TRANFILE | `app/jcl/TRANFILE.jcl` | CLCIFIL:SDSF → STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS → STEP20:IDCAMS → STEP25:IDCAMS → STEP30:IDCAMS → OPCIFIL:SDSF | AWS.M2.CARDDEMO.DALYTRAN.PS.INIT, AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS, AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX | Load Transaction Master file |
| TRANIDX | `app/jcl/TRANIDX.jcl` | STEP20:IDCAMS → STEP25:IDCAMS → STEP30:IDCAMS | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX | Define AIX for transaction file |
| TRANREPT | `app/jcl/TRANREPT.jcl` | STEP05R:REPROC(proc) → STEP05R:SORT → STEP10R:CBTRN03C | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS, AWS.M2.CARDDEMO.CNTL(REPROCT), AWS.M2.CARDDEMO.TRANSACT.BKUP(+1), AWS.M2.CARDDEMO.LOADLIB, AWS.M2.CARDDEMO.TRANSACT.DALY(+1), AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS, AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS, AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS, AWS.M2.CARDDEMO.DATEPARM | AWS.M2.CARDDEMO.TRANSACT.BKUP(+1), AWS.M2.CARDDEMO.TRANSACT.DALY(+1), AWS.M2.CARDDEMO.TRANREPT(+1) | Transaction Report - Submitted from CICS |
| TRANTYPE | `app/jcl/TRANTYPE.jcl` | STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS | AWS.M2.CARDDEMO.TRANTYPE.PS | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | Load Transaction type file |
| TXT2PDF1 | `app/jcl/TXT2PDF1.JCL` | TXT2PDF:IKJEFT1B(() | AWS.M2.LBD.TXT2PDF.LOAD, AWS.M2.LBD.TXT2PDF.EXEC, AWS.M2.CARDDEMO.STATEMNT.PS | - | CONVERT TEXT FILE TO A PDF FILE |
| WAITSTEP | `app/jcl/WAITSTEP.jcl` | WAIT:COBSWAIT | AWS.M2.CARDDEMO.LOADLIB | - | Wait job for given time |
| XREFFILE | `app/jcl/XREFFILE.jcl` | STEP05:IDCAMS → STEP10:IDCAMS → STEP15:IDCAMS → STEP20:IDCAMS → STEP25:IDCAMS → STEP30:IDCAMS | AWS.M2.CARDDEMO.CARDXREF.PS, AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS, AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX | Account, Card and Customer cross reference |
| CREADB2 | `app/app-transaction-type-db2/jcl/CREADB21.jcl` | FREEPLN:IKJEFT01 → CRCRDDB:IKJEFT01 → LDTTYPE:IEFBR14 → RUNTEP2:IKJEFT01 → LDTCCAT:IKJEFT01 | OEM.DB2.DAZ1.SDSNEXIT, OEM.DB2.&DB2S..RUNLIB.LOAD | - | Creates CardDemo Db2 database and loads tables |
| MNTTRDB2 | `app/app-transaction-type-db2/jcl/MNTTRDB2.jcl` | STEP1:IKJEFT01 | OEM.DB2.DAZ1.SDSNEXIT, AWS.M2.CARDDEMO.DBRMLIB, INPFILE | - | Maintain Transaction type table |
| TRANEXTR | `app/app-transaction-type-db2/jcl/TRANEXTR.jcl` | STEP10:IEBGENER → STEP20:IEBGENER → STEP30:IEFBR14 → STEP40:IKJEFT01 → STEP50:IKJEFT01 | OEM.DB2.DAZ1.RUNLIB.LOAD | - | Extracts latest Db2 data for Transaction types |

### PROCs and control cards

| Member | File | Content |
|---|---|---|
| TRANREPT.prc | `app/proc/TRANREPT.prc` | PROC executing PROC=REPROC, PGM=SORT, PGM=CBTRN03C |
| REPROC.prc | `app/proc/REPROC.prc` | PROC executing PGM=IDCAMS |
| REPROCT.ctl | `app/ctl/REPROCT.ctl` | `REPRO INFILE(FILEIN) OUTFILE(FILEOUT)` |
| DB2CREAT.ctl | `app/app-transaction-type-db2/ctl/DB2CREAT.ctl` | `SET CURRENT SQLID = 'SYSADM'; CREATE DATABASE CARDDEMO STOGROUP AWST1STG BUFFERPOOL BP0 CCSID EBCDIC; COMMIT ; CREATE TABLESPACE CARDSPC1 IN` |
| DB2LTTYP.ctl | `app/app-transaction-type-db2/ctl/DB2LTTYP.ctl` | `INSERT INTO CARDDEMO.TRANSACTION_TYPE (TR_TYPE,TR_DESCRIPTION) SELECT '01','PURCHASE'      FROM SYSIBM.SYSDUMMY1 UNION ALL SELECT '02','PAYM` |
| DB2TIAD1.ctl | `app/app-transaction-type-db2/ctl/DB2TIAD1.ctl` | `DSN SYSTEM(DAZ1) RUN PROGRAM(DSNTIAD) - PARMS('RC0')` |
| REPROCT.ctl | `app/app-transaction-type-db2/ctl/REPROCT.ctl` | `REPRO INFILE(FILEIN) OUTFILE(FILEOUT)` |
| DB2FREE.ctl | `app/app-transaction-type-db2/ctl/DB2FREE.ctl` | `DSN SYSTEM(DAZ1) FREE PLAN(CARDDEMO) FREE PLAN(COTRTLIC) FREE PACKAGE(COTRTLIC.*) END` |
| DB2LTCAT.ctl | `app/app-transaction-type-db2/ctl/DB2LTCAT.ctl` | `INSERT INTO CARDDEMO.TRANSACTION_TYPE_CATEGORY (TRC_TYPE_CODE    , TRC_TYPE_CATEGORY, TRC_CAT_DATA     ) WITH DMY AS (SELECT * FROM SYSIBM.S` |
| DB2TEP41.ctl | `app/app-transaction-type-db2/ctl/DB2TEP41.ctl` | `DSN SYSTEM(DAZ1) RUN PROGRAM(DSNTEP4) - PLAN(DSNTEP4) - PARMS('/ALIGN(LHS) MIXED')` |

## 4. Copybook catalog

| Copybook | Dir | PII | Record/01 | Length (bytes) | Fields | Used by | Description |
|---|---|---|---|---|---|---|---|
| CCPAUERY | cpy | N | ERROR-LOG-RECORD | 122 | 12 | COPAUA0C | PENDING AUTHORIZATION ERROR LOGS |
| CCPAURLY | cpy | Y | - | 55 | 6 | COPAUA0C | PENDING AUTHORIZATION RESPONSE |
| CCPAURQY | cpy | Y | - | 151 | 18 | COPAUA0C | PENDING AUTHORIZATION REQUEST |
| CIPAUDTY | cpy | Y | - | 200 | 29 | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, DBUNLDGS, PAUDBLOD, PAUDBUNL | IMS child segment PAUTDTL1 under PAUTSUM0 (200 bytes), one per authorization request, keyed by PA-AUTHORIZATION-KEY (date+time, packed). ISRTed by COPAUA0C 8500; viewed by COPAUS1C; fraud flag set by COPAUS2C (which also inserts the row into DB2 CARDDEMO.AUTHFRDS); DLETed by CBPAUP0C when older than the expiry window. |
| CIPAUSMY | cpy | Y | - | 100 | 13 | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, DBUNLDGS, PAUDBLOD, PAUDBUNL | IMS root segment PAUTSUM0 in HIDAM DB DBPAUTP0 (100 bytes), one per account, keyed PA-ACCT-ID. Created/REPLaced by COPAUA0C 8400 on every authorization; displayed by COPAUS0C; deleted by CBPAUP0C when no detail children remain. |
| IMSFUNCS | cpy | N | FUNC-CODES | 40 | 11 | DBUNLDGS, PAUDBLOD, PAUDBUNL |  |
| PADFLPCB | cpy | N | PADFLPCB | 291 | 10 | DBUNLDGS |  |
| PASFLPCB | cpy | N | PASFLPCB | 136 | 10 | DBUNLDGS |  |
| PAUTBPCB | cpy | N | PAUTBPCB | 291 | 10 | DBUNLDGS, PAUDBLOD, PAUDBUNL |  |
| CSDB2RPY | cpy | N | - | 0 | 0 | COTRTLIC | CardDemo - Common Procedures for Db2 Dummy call to verify connectivity to Db2 |
| CSDB2RWY | cpy | N | - | 816 | 14 | COTRTLIC | CardDemo - Common Working Storage for Db2 Db2 Common variables |
| COADM02Y | cpy | N | CARDDEMO-ADMIN-MENU-OPTIONS | 407 | 26 | COADM01C | Admin-menu option table (user list/add/update/delete plus the DB2 transaction-type options 5/6 when installed); same dispatch pattern via COADM01C. |
| COCOM01Y | cpy | Y | CARDDEMO-COMMAREA | 160 | 22 | COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC, COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | CICS COMMAREA (160 bytes) passed on every EXEC CICS RETURN/XCTL between the 21 online programs. Carries navigation context (from/to tranid+program), the signed-on user, and the customer/account/card the user is currently working on. This is the pseudo-conversational session state. |
| CODATECN | cpy | N | CODATECN-REC | 80 | 32 | CBACT01C | Parameter/control record for CBACT01C date-format conversion demo (COBDATFT assembler): input date, from/to format codes, and converted output. |
| COMEN02Y | cpy | N | CARDDEMO-MAIN-MENU-OPTIONS | 554 | 53 | COMEN01C | Main-menu option table for regular users: option number, text, target program name, and admin-only flag; COMEN01C dispatches by MOVEing the selected CDEMO-MENU-OPT-PGMNAME to CDEMO-TO-PROGRAM and XCTLing. |
| COSTM01 | cpy | Y | TRNX-RECORD | 350 | 17 | CBSTM03A | Transaction record layout re-declared with TRNX- prefix for CBSTM03A statement generation; byte-identical to CVTRA05Y. |
| COTTL01Y | cpy | N | CCDA-SCREEN-TITLE | 120 | 4 | COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC, COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | Screen title lines shown at the top of every BMS map. |
| CSDAT01Y | cpy | N | WS-DATE-TIME | 58 | 39 | COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC, COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | Current date/time work area filled by EXEC CICS ASKTIME/FORMATTIME in every online program; provides YYYYMMDD, HHMMSS, MM/DD/YY, HH:MM:SS and a DB2-format timestamp for screen headers and TRAN-PROC-TS. |
| CSLKPCDY | cpy | N | WS-US-PHONE-AREA-CODE-TO-EDIT | 12 | 5 | COACTUPC | Reference lookup tables implemented as 88-level VALUE lists: valid North-American phone area codes, valid US state codes, and valid state + first-2-ZIP-digit combinations. Used only by COACTUPC edits 1260/1270/1280. |
| CSMSG01Y | cpy | N | CCDA-COMMON-MESSAGES | 100 | 3 | COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC, COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | Two common screen messages (thank-you on exit, invalid key). |
| CSMSG02Y | cpy | N | ABEND-DATA | 134 | 5 | COPAUS0C, COPAUS1C, COTRTUPC, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC | Abend work area (code, culprit program, reason, message) used by the CICS abend handler paragraphs (SEND-PLAIN-TEXT / ABEND-ROUTINE) in the account/card programs. |
| CSSETATY | cpy | N | - | 0 | 0 | COTRTUPC, COACTUPC | Procedure-division snippet used with COPY ... REPLACING to set BMS field attributes (protected/unprotected, colour, cursor) for one map field; copied ~40 times in COACTUPC and COTRTUPC. |
| CSSTRPFY | cpy | N | - | 0 | 0 | COTRTLIC, COTRTUPC, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Procedure-division snippet that maps EIBAID to the CCARD-AID 88-level values (ENTER/CLEAR/PA/PF keys). |
| CSUSR01Y | cpy | Y | SEC-USER-DATA | 80 | 7 | COTRTLIC, COTRTUPC, COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | Application user / security record (80 bytes). VSAM KSDS USRSEC, key SEC-USR-ID. Read at sign-on (COSGN00C) and maintained by COUSR00C-03C (admin only). Passwords are stored in clear text. |
| CSUTLDPY | cpy | N | - | 0 | 0 | COACTUPC | Procedure-division copybook: EDIT-DATE-CCYYMMDD validates a date (century 19/20, month 1-12, day within month incl. Feb-29 leap rule) then calls CSUTLDTC (CEEDAYS) for a final Lilian-date check; EDIT-DATE-OF-BIRTH additionally requires the date to be in the past. |
| CSUTLDWY | cpy | N | - | 115 | 38 | COTRTUPC, COACTUPC | Working storage for the reusable date-edit routine (CSUTLDPY): the CCYYMMDD input split into parts plus result flags (valid / blank / not numeric / bad century / bad month / bad day / bad leap-day) and the CEEDAYS feedback area. |
| CUSTREC | cpy | Y | CUSTOMER-RECORD | 500 | 20 | CBSTM03A | Data-structure for Customer entity (RECLN 500) |
| CVACT01Y | cpy | Y | ACCOUNT-RECORD | 300 | 14 | COPAUA0C, COPAUS0C, COACCT01, CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COBIL00C, COTRN02C | Account master record (300 bytes). VSAM KSDS AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (CICS file ACCTDAT), key ACCT-ID. Read online by COACTVWC/COACTUPC/COBIL00C/COTRN02C/COPAUA0C/COPAUS0C/COACCT01; rewritten by COACTUPC (update), COBIL00C (bill pay), CBTRN02C (posting), CBACT04C (interest/cycle reset). |
| CVACT02Y | cpy | Y | CARD-RECORD | 150 | 8 | COPAUS0C, COTRTLIC, CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Credit card master record (150 bytes). VSAM KSDS CARDDATA (CICS CARDDAT), key CARD-NUM, with AIX CARDAIX on CARD-ACCT-ID. Maintained by COCRDLIC/COCRDSLC/COCRDUPC. |
| CVACT03Y | cpy | Y | CARD-XREF-RECORD | 50 | 5 | COPAUA0C, COPAUS0C, CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COTRN02C | Card -> customer -> account cross-reference (50 bytes). VSAM KSDS CARDXREF (CICS CCXREF), key XREF-CARD-NUM, with AIX path CXACAIX on XREF-ACCT-ID. This is the join hub of the model: every card-to-account and account-to-customer lookup goes through it. |
| CVCRD01Y | cpy | Y | CC-WORK-AREAS | 213 | 14 | COTRTLIC, COTRTUPC, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Working-storage area shared by the account/card/tran-type screens (COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC): decoded AID key, next program/map, messages, and the raw account/card/customer search keys typed by the user (X with numeric REDEFINES for edit). |
| CVCUS01Y | cpy | Y | CUSTOMER-RECORD | 500 | 20 | COPAUA0C, COPAUS0C, CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC | Customer master record (500 bytes). VSAM KSDS CUSTDATA (CICS CUSTDAT), key CUST-ID. Read by COACTVWC/COCRDSLC/COPAUA0C/COPAUS0C; rewritten by COACTUPC (account update screen also edits customer data). CUSTREC is a byte-identical copy used by CBSTM03A. |
| CVEXPORT | cpy | Y | EXPORT-RECORD | 500 | 72 | CBEXPORT, CBIMPORT | Multi-record export file layout (500 bytes) for CBEXPORT/CBIMPORT branch-migration utilities: a common header (record type C/A/X/T/D, customer id, sequence, timestamp) followed by a REDEFINES union of the customer, account, xref, transaction and card record bodies. |
| CVTRA01Y | cpy | Y | TRAN-CAT-BAL-RECORD | 50 | 7 | CBACT04C, CBTRN02C | Transaction category balance (50 bytes). VSAM KSDS TCATBALF, composite key (ACCT-ID, TYPE-CD, CAT-CD). Accumulated by CBTRN02C 2700 during posting; read sequentially by CBACT04C to compute interest per category. |
| CVTRA02Y | cpy | N | DIS-GROUP-RECORD | 50 | 7 | CBACT04C | Disclosure group / pricing record (50 bytes). VSAM KSDS DISCGRP, composite key (ACCT-GROUP-ID, TRAN-TYPE-CD, TRAN-CAT-CD). Provides the annual interest rate applied by CBACT04C. |
| CVTRA03Y | cpy | N | TRAN-TYPE-RECORD | 60 | 4 | CBTRN03C | Transaction type reference (60 bytes). VSAM KSDS TRANTYPE, key TRAN-TYPE. With the DB2 extension the master copy lives in CARDDEMO.TRANSACTION_TYPE and TRANEXTR unloads it to this file. |
| CVTRA04Y | cpy | N | TRAN-CAT-RECORD | 60 | 6 | CBTRN03C | Transaction category reference (60 bytes). VSAM KSDS TRANCATG, composite key (TYPE-CD, CAT-CD). DB2 master: CARDDEMO.TRANSACTION_TYPE_CATEGORY. |
| CVTRA05Y | cpy | Y | TRAN-RECORD | 350 | 15 | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C | Posted transaction / ledger record (350 bytes). VSAM KSDS TRANSACT (CICS TRANSACT), key TRAN-ID, AIX on TRAN-PROC-TS (TRANIDX job). Written by CBTRN02C 2900 (batch posting), COTRN02C (online add), COBIL00C (bill payment) and CBACT04C 1300-B (system interest transactions via SYSTRAN merge). Same physical layout as CVTRA06Y and COSTM01. |
| CVTRA06Y | cpy | Y | DALYTRAN-RECORD | 350 | 15 | CBTRN01C, CBTRN02C | Daily incoming transaction feed record (350 bytes), sequential file AWS.M2.CARDDEMO.DALYTRAN.PS. Byte-identical to CVTRA05Y with DALYTRAN- prefix; CBTRN02C copies it field-for-field into TRAN-RECORD after validation. Rejects are written to DALYREJS with an 80-byte trailer (reason code 100/101/102/103 + text). |
| CVTRA07Y | cpy | Y | REPORT-NAME-HEADER | 808 | 45 | CBTRN03C | Print-line layouts for the daily transaction report produced by CBTRN03C (TRANREPT job): report header with date range, column headers, detail line (tran id, account, type/category with descriptions, source, amount), and page/account/grand total lines. |
| UNUSED1Y | cpy | N | UNUSED-DATA | 80 | 7 | - | Intentionally unused copybook (not COPYed by any program) - present to exercise dead-code detection in analysis tooling. |
| COPAU00 | cpy-bms | Y | COPAU0AI | - | 748 | COPAUS0C | BMS symbolic map for mapset COPAU0AI |
| COPAU01 | cpy-bms | Y | COPAU1AI | - | 328 | COPAUS1C | BMS symbolic map for mapset COPAU1AI |
| COTRTLI | cpy-bms | N | CTRTLIAI | - | 484 | COTRTLIC | BMS symbolic map for mapset CTRTLIAI |
| COTRTUP | cpy-bms | N | CTRTUPAI | - | 184 | COTRTUPC | BMS symbolic map for mapset CTRTUPAI |
| COACTUP | cpy-bms | Y | CACTUPAI | - | 652 | COACTUPC | BMS symbolic map for mapset CACTUPAI |
| COACTVW | cpy-bms | Y | CACTVWAI | - | 448 | COACTVWC | BMS symbolic map for mapset CACTVWAI |
| COADM01 | cpy-bms | Y | COADM1AI | - | 244 | COADM01C | BMS symbolic map for mapset COADM1AI |
| COBIL00 | cpy-bms | Y | COBIL0AI | - | 124 | COBIL00C | BMS symbolic map for mapset COBIL0AI |
| COCRDLI | cpy-bms | Y | CCRDLIAI | - | 544 | COCRDLIC | BMS symbolic map for mapset CCRDLIAI |
| COCRDSL | cpy-bms | Y | CCRDSLAI | - | 184 | COCRDSLC | BMS symbolic map for mapset CCRDSLAI |
| COCRDUP | cpy-bms | Y | CCRDUPAI | - | 208 | COCRDUPC | BMS symbolic map for mapset CCRDUPAI |
| COMEN01 | cpy-bms | Y | COMEN1AI | - | 244 | COMEN01C | BMS symbolic map for mapset COMEN1AI |
| CORPT00 | cpy-bms | Y | CORPT0AI | - | 208 | CORPT00C | BMS symbolic map for mapset CORPT0AI |
| COSGN00 | cpy-bms | Y | COSGN0AI | - | 136 | COSGN00C | BMS symbolic map for mapset COSGN0AI |
| COTRN00 | cpy-bms | Y | COTRN0AI | - | 712 | COTRN00C | BMS symbolic map for mapset COTRN0AI |
| COTRN01 | cpy-bms | Y | COTRN1AI | - | 256 | COTRN01C | BMS symbolic map for mapset COTRN1AI |
| COTRN02 | cpy-bms | Y | COTRN2AI | - | 256 | COTRN02C | BMS symbolic map for mapset COTRN2AI |
| COUSR00 | cpy-bms | Y | COUSR0AI | - | 712 | COUSR00C | BMS symbolic map for mapset COUSR0AI |
| COUSR01 | cpy-bms | Y | COUSR1AI | - | 148 | COUSR01C | BMS symbolic map for mapset COUSR1AI |
| COUSR02 | cpy-bms | Y | COUSR2AI | - | 148 | COUSR02C | BMS symbolic map for mapset COUSR2AI |
| COUSR03 | cpy-bms | Y | COUSR3AI | - | 136 | COUSR03C | BMS symbolic map for mapset COUSR3AI |
| AUTHFRDS | dcl | Y | CARDDEMO.AUTHFRDS | - | 26 | COPAUS2C | DCLGEN CARDDEMO.AUTHFRDS |
| DCLTRCAT | dcl | N | CARDDEMO.TRANSACTION_TYPE_CATEGORY | - | 3 | COTRTUPC | DCLGEN CARDDEMO.TRANSACTION_TYPE_CATEGORY |
| DCLTRTYP | dcl | N | CARDDEMO.TRANSACTION_TYPE | - | 2 | COBTUPDT, COTRTLIC, COTRTUPC | DCLGEN CARDDEMO.TRANSACTION_TYPE |

## 5. Other artifacts

### BMS mapsets

| Mapset | File | Used by (program) |
|---|---|---|
| COBIL00 | `app/bms/COBIL00.bms` | COBIL00C |
| COACTUP | `app/bms/COACTUP.bms` | COACTUPC |
| COUSR01 | `app/bms/COUSR01.bms` | COUSR01C |
| COUSR00 | `app/bms/COUSR00.bms` | COUSR00C |
| COCRDUP | `app/bms/COCRDUP.bms` | COCRDUPC |
| COUSR02 | `app/bms/COUSR02.bms` | COUSR02C |
| COUSR03 | `app/bms/COUSR03.bms` | COUSR03C |
| CORPT00 | `app/bms/CORPT00.bms` | CORPT00C |
| COCRDLI | `app/bms/COCRDLI.bms` | COCRDLIC |
| COTRN02 | `app/bms/COTRN02.bms` | COTRN02C |
| COTRN00 | `app/bms/COTRN00.bms` | COTRN00C |
| COTRN01 | `app/bms/COTRN01.bms` | COTRN01C |
| COACTVW | `app/bms/COACTVW.bms` | COACTVWC |
| COSGN00 | `app/bms/COSGN00.bms` | COSGN00C |
| COADM01 | `app/bms/COADM01.bms` | COADM01C |
| COMEN01 | `app/bms/COMEN01.bms` | COMEN01C |
| COCRDSL | `app/bms/COCRDSL.bms` | COCRDSLC |
| COTRTLI | `app/app-transaction-type-db2/bms/COTRTLI.bms` | COTRTLIC |
| COTRTUP | `app/app-transaction-type-db2/bms/COTRTUP.bms` | COTRTUPC |
| COPAU00 | `app/app-authorization-ims-db2-mq/bms/COPAU00.bms` | COPAUS0C |
| COPAU01 | `app/app-authorization-ims-db2-mq/bms/COPAU01.bms` | COPAUS1C |

### CSD groups

| File | Transactions | Programs | Files | Mapsets |
|---|---|---|---|---|
| `app/app-authorization-ims-db2-mq/csd/CRDDEMO2.csd` | 3 | 4 | 0 | 2 |
| `app/app-transaction-type-db2/csd/CRDDEMOD.csd` | 2 | 2 | 0 | 2 |
| `app/app-vsam-mq/csd/CRDDEMOM.csd` | 2 | 2 | 0 | 0 |
| `app/csd/CARDDEMO.CSD` | 18 | 18 | 8 | 17 |

### DB2 DDL and DCLGEN

| File | Content |
|---|---|
| `app/app-transaction-type-db2/ddl/TRNTYPE.ddl` | TABLE CARDDEMO.TRANSACTION_TYPE |
| `app/app-transaction-type-db2/ddl/XTRNTYPE.ddl` | DDL |
| `app/app-transaction-type-db2/ddl/XTRNTYCAT.ddl` | DDL |
| `app/app-transaction-type-db2/ddl/TRNTYCAT.ddl` | TABLE CARDDEMO.TRANSACTION_TYPE_CATEGORY |
| `app/app-authorization-ims-db2-mq/ddl/XAUTHFRD.ddl` | DDL |
| `app/app-authorization-ims-db2-mq/ddl/AUTHFRDS.ddl` | TABLE CARDDEMO.AUTHFRDS |
| `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl` | DCLGEN CARDDEMO.AUTHFRDS (26 columns) |
| `app/app-transaction-type-db2/dcl/DCLTRCAT.dcl` | DCLGEN CARDDEMO.TRANSACTION_TYPE_CATEGORY (3 columns) |
| `app/app-transaction-type-db2/dcl/DCLTRTYP.dcl` | DCLGEN CARDDEMO.TRANSACTION_TYPE (2 columns) |

### IMS definitions

| File | Type |
|---|---|
| `app/app-authorization-ims-db2-mq/ims/PADFLDBD.DBD` | DBD |
| `app/app-authorization-ims-db2-mq/ims/DBPAUTP0.dbd` | DBD |
| `app/app-authorization-ims-db2-mq/ims/PSBPAUTB.psb` | PSB |
| `app/app-authorization-ims-db2-mq/ims/PSBPAUTL.psb` | PSB |
| `app/app-authorization-ims-db2-mq/ims/PASFLDBD.DBD` | DBD |
| `app/app-authorization-ims-db2-mq/ims/DLIGSAMP.PSB` | PSB |
| `app/app-authorization-ims-db2-mq/ims/DBPAUTX0.dbd` | DBD |
| `app/app-authorization-ims-db2-mq/ims/PAUTBUNL.PSB` | PSB |

### Assembler and macros

| File | Type |
|---|---|
| `app/asm/MVSWAIT.asm` | Assembler source |
| `app/asm/COBDATFT.asm` | Assembler source |
| `app/maclib/COCDATFT.mac` | Assembler macro |
| `app/maclib/ASMWAIT.mac` | Assembler macro |

### Data files (EBCDIC/ASCII sample data)

| File | Matching copybook |
|---|---|
| `app/data/ASCII/tcatbal.txt` | CVTRA01Y |
| `app/data/ASCII/acctdata.txt` | CVACT01Y |
| `app/data/ASCII/discgrp.txt` | CVTRA02Y |
| `app/data/ASCII/custdata.txt` | CVCUS01Y |
| `app/data/ASCII/dailytran.txt` | CVTRA06Y |
| `app/data/ASCII/trancatg.txt` | CVTRA04Y |
| `app/data/ASCII/trantype.txt` | CVTRA03Y |
| `app/data/ASCII/carddata.txt` | CVACT02Y |
| `app/data/ASCII/cardxref.txt` | CVACT03Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.CUSTDATA.PS` | CVCUS01Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.EXPORT.DATA.PS` | CVEXPORT |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.DALYTRAN.PS` | CVTRA06Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.CARDDATA.PS` | CVACT02Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS` | CSUSR01Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.DISCGRP.PS` | CVTRA02Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.CARDXREF.PS` | CVACT03Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.TCATBALF.PS` | CVTRA01Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.ACCDATA.PS` | CVACT01Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.TRANTYPE.PS` | CVTRA03Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.TRANCATG.PS` | CVTRA04Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS` | CVACT01Y |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.DALYTRAN.PS.INIT` | CVTRA06Y |
| `app/app-authorization-ims-db2-mq/data/EBCDIC/AWS.M2.CARDDEMO.IMSDATA.DBPAUTP0.dat` | CIPAUSMY/CIPAUDTY |

### Catalog / scheduler

- `app/catlg/LISTCAT.txt`
- `app/scheduler/CardDemo.ca7`
- `app/scheduler/CardDemo.controlm`

## 6. CICS transactions

| Tranid | Program | Mapset | Sub-app |
|---|---|---|---|
| CPVD | COPAUS1C | COPAU1AI | authorization |
| CPVS | COPAUS0C | COPAU0AI | authorization |
| CP00 | COPAUA0C | - | authorization |
| CTLI | COTRTLIC | CTRTLIAI | trantype |
| CTTU | COTRTUPC | CTRTUPAI | trantype |
| CDRA | COACCT01 | - | vsam-mq |
| CDRD | CODATE01 | - | vsam-mq |
| CAUP | COACTUPC | CACTUPAI | base |
| CAVW | COACTVWC | CACTVWAI | base |
| CA00 | COADM01C | COADM1AI | base |
| CB00 | COBIL00C | COBIL0AI | base |
| CCDL | COCRDSLC | CCRDSLAI | base |
| CCLI | COCRDLIC | CCRDLIAI | base |
| CCUP | COCRDUPC | CCRDUPAI | base |
| CC00 | COSGN00C | COSGN0AI | base |
| CDV1 | COCRDSEC (no source in repo) | - | base |
| CM00 | COMEN01C | COMEN1AI | base |
| CR00 | CORPT00C | CORPT0AI | base |
| CT00 | COTRN00C | COTRN0AI | base |
| CT01 | COTRN01C | COTRN1AI | base |
| CT02 | COTRN02C | COTRN2AI | base |
| CU00 | COUSR00C | COUSR0AI | base |
| CU01 | COUSR01C | COUSR1AI | base |
| CU02 | COUSR02C | COUSR2AI | base |
| CU03 | COUSR03C | COUSR3AI | base |
