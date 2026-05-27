# Application Inventory

## 1. Program Catalog — `app/cbl/`

The CardDemo application contains 31 COBOL programs: 13 batch programs and 18 online (CICS) programs.

### 1.1 Batch Programs (prefix CB)

| Filename | Program ID | Purpose | Lines | Key I/O Operations | Copybooks Referenced |
|----------|-----------|---------|-------|---------------------|---------------------|
| CBACT01C.cbl | CBACT01C | Read account VSAM file, write to output/array/VBR files | 430 | ACCTFILE(R), OUTFILE(W), ARRYFILE(W), VBRCFILE(W) | CVACT01Y, CODATECN |
| CBACT02C.cbl | CBACT02C | Read and print card data file | 178 | CARDFILE(R) | CVACT02Y |
| CBACT03C.cbl | CBACT03C | Read and print cross-reference file | 178 | XREFFILE(R) | CVACT03Y |
| CBACT04C.cbl | CBACT04C | Interest calculator — reads balances, computes interest, updates accounts | 652 | TCATBALF(R), XREFFILE(R), DISCGRP(R), ACCTFILE(I-O), TRANSACT(W) | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| CBCUS01C.cbl | CBCUS01C | Read and print customer file | 178 | CUSTFILE(R) | CVCUS01Y |
| CBEXPORT.cbl | CBEXPORT | Export customer data for branch migration | 582 | CUSTFILE(R), ACCTFILE(R), XREFFILE(R), TRANSACT(R), CARDFILE(R), EXPFILE(W) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| CBIMPORT.cbl | CBIMPORT | Import from export file, split into entity-specific outputs | 487 | EXPFILE(R), CUSTOUT(W), ACCTOUT(W), XREFOUT(W), TRNXOUT(W), CARDOUT(W), ERROUT(W) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| CBSTM03A.CBL | CBSTM03A | Print account statements from transaction data (plain text and HTML) | 924 | Transaction files(R), Statement output(W) | COSTM01 |
| CBSTM03B.CBL | CBSTM03B | Subroutine for file processing related to transaction report | 230 | Transaction files(R) | — |
| CBTRN01C.cbl | CBTRN01C | Post daily transaction file | 494 | DALYTRAN(R), CUSTFILE(R), XREFFILE(R), CARDFILE(R), ACCTFILE(R), TRANFILE(W) | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| CBTRN02C.cbl | CBTRN02C | Validate and post daily transactions, write rejects | 731 | DALYTRAN(R), XREFFILE(R), ACCTFILE(I-O), TCATBALF(I-O), TRANFILE(W), DALYREJS(W) | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| CBTRN03C.cbl | CBTRN03C | Print transaction detail report | 649 | TRANFILE(R), CARDXREF(R), TRANTYPE(R), TRANCATG(R), DATEPARM(R), TRANREPT(W) | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| COBSWAIT.cbl | COBSWAIT | Utility wait program | 41 | None | — |

### 1.2 Online (CICS) Programs (prefix CO)

| Filename | Program ID | Purpose | Lines | Key I/O Operations | Copybooks Referenced |
|----------|-----------|---------|-------|---------------------|---------------------|
| COSGN00C.cbl | COSGN00C | Sign-on screen — authenticates users | 260 | USRSEC(R) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COMEN01C.cbl | COMEN01C | Main menu for regular users | 308 | — | COCOM01Y, COMEN02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COADM01C.cbl | COADM01C | Admin menu | 288 | — | COCOM01Y, COADM02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COACTVWC.cbl | COACTVWC | Account view | 941 | ACCTDAT(R), CUSTDAT(R), CXACAIX(R) | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| COACTUPC.cbl | COACTUPC | Account update — largest program with extensive field validation | 4236 | ACCTDAT(R/W), CUSTDAT(R), CXACAIX(R) | CSUTLDWY, CVCRD01Y, CSLKPCDY, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y |
| COCRDLIC.cbl | COCRDLIC | List credit cards | 1459 | CARDDAT(R), CARDAIX(R) | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y |
| COCRDSLC.cbl | COCRDSLC | Credit card detail view | 887 | CARDDAT(R) | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| COCRDUPC.cbl | COCRDUPC | Credit card update | 1560 | CARDDAT(R/W) | CVCRD01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| COBIL00C.cbl | COBIL00C | Bill payment | 572 | TRANSACT(R/W), ACCTDAT(R/W), CXACAIX(R) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y |
| COTRN00C.cbl | COTRN00C | List transactions | 699 | TRANSACT(R) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y |
| COTRN01C.cbl | COTRN01C | View transaction detail | 330 | TRANSACT(R) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y |
| COTRN02C.cbl | COTRN02C | Add new transaction | 783 | TRANSACT(R/W), CCXREF(R), CXACAIX(R) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y |
| CORPT00C.cbl | CORPT00C | Submit batch report job via TDQ | 649 | TDQ(W) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y |
| COUSR00C.cbl | COUSR00C | List users | 695 | USRSEC(R) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COUSR01C.cbl | COUSR01C | Add user | 299 | USRSEC(W) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COUSR02C.cbl | COUSR02C | Update user | 414 | USRSEC(R/W) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COUSR03C.cbl | COUSR03C | Delete user | 359 | USRSEC(R/D) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| CSUTLDTC.cbl | CSUTLDTC | Date validation utility (CALLs CEEDAYS) | 157 | — | — |

---

## 2. Sub-Application Programs

### 2.1 Authorization Module — `app/app-authorization-ims-db2-mq/cbl/`

This module handles credit card authorization using IMS DB, DB2, and MQ.

| Filename | Program ID | Type | Purpose | Lines |
|----------|-----------|------|---------|-------|
| CBPAUP0C.cbl | CBPAUP0C | Batch (IMS) | Delete expired pending authorization messages | 386 |
| COPAUA0C.cbl | COPAUA0C | CICS (IMS/MQ) | Card authorization decision program | 1026 |
| COPAUS0C.cbl | COPAUS0C | CICS (IMS/DB2) | List pending authorization messages | 1032 |
| COPAUS1C.cbl | COPAUS1C | CICS (IMS/DB2) | View authorization message detail | 604 |
| COPAUS2C.cbl | COPAUS2C | CICS (IMS/DB2) | Mark authorization message as fraud | 244 |
| DBUNLDGS.CBL | DBUNLDGS | Batch | Unload IMS DB segments to flat file | 366 |
| PAUDBLOD.CBL | PAUDBLOD | Batch | Load authorization data into IMS DB | 369 |
| PAUDBUNL.CBL | PAUDBUNL | Batch | Unload authorization data from IMS DB | 317 |

### 2.2 Transaction Type Module — `app/app-transaction-type-db2/cbl/`

This module manages transaction type reference data using DB2.

| Filename | Program ID | Type | Purpose | Lines |
|----------|-----------|------|---------|-------|
| COBTUPDT.cbl | COBTUPDT | Batch | Update transaction type in DB2 based on user input | 237 |
| COTRTLIC.cbl | COTRTLIC | CICS (DB2) | List transaction types with DB2 cursor paging for updates/deletes | 2098 |
| COTRTUPC.cbl | COTRTUPC | CICS (DB2) | Update/delete transaction type record in DB2 | 1702 |

### 2.3 VSAM-MQ Module — `app/app-vsam-mq/cbl/`

This module handles account processing via VSAM files and IBM MQ messaging.

| Filename | Program ID | Type | Purpose | Lines |
|----------|-----------|------|---------|-------|
| COACCT01.cbl | COACCT01 | CICS (MQ) | Process account requests from MQ queue, read VSAM, reply via MQ | 620 |
| CODATE01.cbl | CODATE01 | CICS (MQ) | Date formatting utility triggered by MQ messages | 524 |

---

## 3. JCL Job Catalog — `app/jcl/`

| JCL File | Job Name | Purpose | Steps (EXEC PGM/PROC) | Key Datasets |
|----------|----------|---------|------------------------|--------------|
| ACCTFILE.jcl | ACCTFILE | Delete and define account VSAM cluster with AIX | IDCAMS (x3) | AWS.M2.CARDDEMO.ACCTDATA.PS |
| CARDFILE.jcl | CARDFILE | Delete and define card data VSAM cluster with AIX paths | SDSF, IDCAMS (x6), SDSF | AWS.M2.CARDDEMO.CARDDATA.PS |
| CBADMCDJ.jcl | CBADMCDJ | Define CICS CSD resources for CardDemo | DFHCSDUP | CICS SDFHLOAD |
| CBEXPORT.jcl | CBEXPORT | Export customer data for branch migration | IDCAMS, CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, EXPFILE |
| CBIMPORT.jcl | CBIMPORT | Import CardDemo data from export file | CBIMPORT | EXPFILE, CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT |
| CLOSEFIL.jcl | CLOSEFIL | Close CICS-managed files via SDSF | SDSF | CICS file resources |
| COMBTRAN.jcl | COMBTRAN | Combine and sort transactions | SORT, IDCAMS | Transaction datasets |
| CREASTMT.JCL | CREASTMT | Create account statements | SORT, CBSTM03A | Statement files, TRANFILE |
| CUSTFILE.jcl | CUSTFILE | Define customer VSAM cluster | SDSF, IDCAMS (x3), SDSF | AWS.M2.CARDDEMO.CUSTDATA.PS |
| DALYREJS.jcl | DALYREJS | Define GDG base for daily rejects | IDCAMS | AWS.M2.CARDDEMO.DALYREJS |
| DEFCUST.jcl | DEFCUST | Define customer data file (alternate definition) | IDCAMS (x2) | Customer VSAM |
| DEFGDGB.jcl | DEFGDGB | Define GDG bases for transaction backups | IDCAMS | GDG base definitions |
| DEFGDGD.jcl | DEFGDGD | Define DB2-related GDG and backup datasets | IDCAMS, IEBGENER (x3) | TRANTYPE, TRANCATG, DISCGRP backups |
| DISCGRP.jcl | DISCGRP | Define disclosure group VSAM cluster | IDCAMS (x3) | AWS.M2.CARDDEMO.DISCGRP |
| DUSRSECJ.jcl | DUSRSECJ | Define and load user security VSAM file | IEFBR14, IEBGENER, IDCAMS (x2) | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS |
| ESDSRRDS.jcl | ESDSRRDS | Define ESDS and RRDS VSAM clusters | IEFBR14, IEBGENER, IDCAMS (x4) | AWS.M2.CARDDEMO.USRSEC.VSAM.ESDS/RRDS |
| FTPJCL.JCL | FTPJCL | FTP data transfer job | FTP | Remote datasets |
| INTCALC.jcl | INTCALC | Run interest calculation batch program | CBACT04C | TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT |
| INTRDRJ1.JCL | INTRDRJ1 | Internal reader job submission (variant 1) | Internal reader | — |
| INTRDRJ2.JCL | INTRDRJ2 | Internal reader job submission (variant 2) | Internal reader | — |
| OPENFIL.jcl | OPENFIL | Open CICS-managed files via SDSF | SDSF | CICS file resources |
| POSTTRAN.jcl | POSTTRAN | Post daily transactions (validation + posting) | CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF, TRANFILE, DALYREJS |
| PRTCATBL.jcl | PRTCATBL | Print transaction category balance file | IEFBR14, REPROC, SORT | TCATBALF |
| READACCT.jcl | READACCT | Read and export account data to PS/array/VB formats | IEFBR14, CBACT01C | ACCTFILE, PSCOMP, ARRYPS, VBPS |
| READCARD.jcl | READCARD | Read and print card data | CBACT02C | CARDFILE |
| READCUST.jcl | READCUST | Read and print customer data | CBCUS01C | CUSTFILE |
| READXREF.jcl | READXREF | Read and print cross-reference data | CBACT03C | XREFFILE |
| REPTFILE.jcl | REPTFILE | Define GDG for report file | IDCAMS | AWS.M2.CARDDEMO.TRANREPT |
| TCATBALF.jcl | TCATBALF | Define transaction category balance VSAM cluster | IDCAMS (x3) | AWS.M2.CARDDEMO.TCATBAL |
| TRANBKP.jcl | TRANBKP | REPRO and delete transaction master | REPROC, IDCAMS (x2) | TRANSACT, backup GDG |
| TRANCATG.jcl | TRANCATG | Define transaction category VSAM cluster | IDCAMS (x3) | AWS.M2.CARDDEMO.TRANCATG |
| TRANFILE.jcl | TRANFILE | Define transaction master VSAM cluster with AIX | SDSF, IDCAMS (x6), SDSF | AWS.M2.CARDDEMO.TRANSACT |
| TRANIDX.jcl | TRANIDX | Define AIX on transaction master | IDCAMS (x3) | TRANSACT AIX |
| TRANREPT.jcl | TRANREPT | Generate transaction detail report | REPROC, SORT, CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, TRANREPT |
| TRANTYPE.jcl | TRANTYPE | Define transaction type VSAM cluster | IDCAMS (x3) | AWS.M2.CARDDEMO.TRANTYPE |
| TXT2PDF1.JCL | TXT2PDF1 | Convert text report to PDF | TXT2PDF utility | Report files |
| WAITSTEP.jcl | WAITSTEP | Execute wait utility program | COBSWAIT | — |
| XREFFILE.jcl | XREFFILE | Define cross-reference VSAM cluster | IDCAMS (x3) | AWS.M2.CARDDEMO.CARDXREF |
