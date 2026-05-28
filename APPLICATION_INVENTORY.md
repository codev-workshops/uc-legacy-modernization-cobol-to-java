# APPLICATION INVENTORY — CardDemo COBOL Estate

## 1. COBOL Programs (app/cbl/)

### 1.1 Batch Programs

| # | Filename | Purpose | Key I/O Operations | Copybooks Referenced |
|---|----------|---------|---------------------|----------------------|
| 1 | **CBACT01C.cbl** (430 lines) | Read account VSAM KSDS file; write records to flat file, array file, and variable-length file with date reformatting | **Read:** ACCTFILE (VSAM KSDS) · **Write:** OUTFILE (sequential), ARRYFILE (sequential), VBRCFILE (variable-length sequential) · **Call:** COBDATFT (date formatting) | CVACT01Y, CODATECN |
| 2 | **CBACT02C.cbl** (178 lines) | Read and print card data file (VSAM KSDS sequential scan) | **Read:** CARDFILE (VSAM KSDS) | CVACT02Y |
| 3 | **CBACT03C.cbl** (178 lines) | Read and print card–account cross-reference data file | **Read:** XREFFILE (VSAM KSDS) | CVACT03Y |
| 4 | **CBACT04C.cbl** (652 lines) | Interest calculator — reads transaction-category balances, cross-references, disclosure-group rates; computes interest; updates account balances; writes interest transactions | **Read:** TCATBALF (VSAM KSDS), XREFFILE (VSAM KSDS random), DISCGRP (VSAM KSDS random), ACCTFILE (VSAM KSDS I-O) · **Write:** TRANSACT (sequential) · **Rewrite:** ACCTFILE | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| 5 | **CBCUS01C.cbl** (178 lines) | Read and print customer data file | **Read:** CUSTFILE (VSAM KSDS) | CVCUS01Y |
| 6 | **CBEXPORT.cbl** (582 lines) | Export customer data for branch migration — reads all normalized CardDemo files (customer, account, card xref, transaction, card) and creates multi-record export file | **Read:** CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE (all VSAM KSDS) · **Write:** EXPFILE (indexed sequential) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 7 | **CBIMPORT.cbl** (487 lines) | Import customer data from branch migration export — reads multi-record export file, splits into normalized target files with validation, generates error reports | **Read:** EXPFILE (indexed sequential) · **Write:** CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT (all sequential) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 8 | **CBTRN01C.cbl** (494 lines) | Post daily transactions — reads daily transaction file, validates card numbers via cross-reference, looks up accounts | **Read:** DALYTRAN (sequential), CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE (all VSAM KSDS random) | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| 9 | **CBTRN02C.cbl** (731 lines) | Post daily transactions with full validation — reads daily transactions, validates via xref, updates account balances, writes to transaction master, updates category balances, rejects invalid records | **Read:** DALYTRAN (sequential), XREFFILE, ACCTFILE (I-O), TCATBALF (I-O) · **Write:** TRANFILE (indexed), DALYREJS (sequential) · **Rewrite:** ACCTFILE, TCATBALF | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| 10 | **CBTRN03C.cbl** (649 lines) | Print transaction detail report — reads transaction file, enriches with xref, type, and category descriptions; writes formatted report with page/account/grand totals | **Read:** TRANFILE (sequential), CARDXREF (VSAM KSDS random), TRANTYPE (VSAM KSDS random), TRANCATG (VSAM KSDS random), DATEPARM (sequential) · **Write:** TRANREPT (sequential report) | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| 11 | **COBSWAIT.cbl** (41 lines) | Utility — waits for specified time (centiseconds via PARM/SYSIN) | **Call:** MVSWAIT | _(none)_ |
| 12 | **CSUTLDTC.cbl** (157 lines) | Date validation utility — validates dates using LE CEEDAYS API; called by online programs | **Call:** CEEDAYS (LE API) | _(none)_ |

### 1.2 Online (CICS) Programs

| # | Filename | Purpose | CICS Transaction | Key VSAM Files Accessed | Copybooks Referenced |
|---|----------|---------|------------------|-------------------------|----------------------|
| 1 | **COSGN00C.cbl** (260 lines) | Sign-on screen — authenticates users, routes to admin menu (COADM01C) or regular menu (COMEN01C) | CC00 | USRSEC (READ) | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| 2 | **COADM01C.cbl** (288 lines) | Admin menu — displays administrative options and routes to selected function programs | CA00 | USRSEC (referenced) | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| 3 | **COMEN01C.cbl** (308 lines) | Main menu — displays menu options for regular users and routes to selected functions | CM00 | USRSEC (referenced) | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| 4 | **COACTVWC.cbl** (941 lines) | Account view — displays account details with card cross-reference and customer information (read-only) | — | ACCTDAT (READ), CARDDAT (READ via AIX), CUSTDAT (READ) | CVCRD01Y, COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| 5 | **COACTUPC.cbl** (4236 lines) | Account update — full CRUD with extensive field-level validation (dates, phone numbers, state codes, amounts); updates account and customer records | — | ACCTDAT (READ/REWRITE), CUSTDAT (READ/REWRITE), CARDDAT (READ via AIX CXACAIX) | CSUTLDWY, CVCRD01Y, CSLKPCDY, COACTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY, CSSTRPFY, CSUTLDPY |
| 6 | **COCRDLIC.cbl** (1459 lines) | Credit card list — browse/list credit cards with pagination (STARTBR/READNEXT/READPREV); supports forward/backward scrolling | — | CARDDAT (STARTBR, READNEXT, READPREV, ENDBR) | CVCRD01Y, COCOM01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY |
| 7 | **COCRDSLC.cbl** (887 lines) | Credit card view — displays credit card details including customer information (read-only) | — | CARDDAT (READ), CARDDAT-ACCT-PATH (READ via AIX) | CVCRD01Y, COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 8 | **COCRDUPC.cbl** (1560 lines) | Credit card update — update credit card details with validation; rewrites card record | — | CARDDAT (READ, REWRITE) | CVCRD01Y, COCOM01Y, COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 9 | **COTRN00C.cbl** (699 lines) | Transaction list — browse/list transactions with pagination (STARTBR/READNEXT/READPREV) | — | TRANSACT (STARTBR, READNEXT, READPREV, ENDBR) | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y |
| 10 | **COTRN01C.cbl** (330 lines) | Transaction view — display a single transaction detail (read-only) | — | TRANSACT (READ) | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y |
| 11 | **COTRN02C.cbl** (783 lines) | Transaction add — add new transaction to TRANSACT file with validation; looks up account via xref, auto-generates transaction ID | — | TRANSACT (READ, WRITE), ACCTDAT (READ), CXACAIX (STARTBR, READPREV, ENDBR) | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y |
| 12 | **COBIL00C.cbl** (572 lines) | Bill payment — pay account balance in full; creates payment transaction, updates account balance | CB00 | TRANSACT (READ, WRITE), ACCTDAT (READ, REWRITE), CXACAIX (READ, STARTBR, READPREV, ENDBR) | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y |
| 13 | **CORPT00C.cbl** (649 lines) | Transaction reports — submits batch job for transaction report printing via TDQ (JOBS queue); accepts date range parameters | — | TRANSACT (referenced), TDQ JOBS (WRITEQ TD) | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y |
| 14 | **COUSR00C.cbl** (695 lines) | User list — browse/list all users from USRSEC file with pagination | — | USRSEC (STARTBR, READNEXT, READPREV, ENDBR) | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| 15 | **COUSR01C.cbl** (299 lines) | User add — add new regular/admin user to USRSEC security file | — | USRSEC (WRITE) | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| 16 | **COUSR02C.cbl** (414 lines) | User update — update existing user in USRSEC security file | — | USRSEC (READ, REWRITE) | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| 17 | **COUSR03C.cbl** (359 lines) | User delete — delete user from USRSEC security file | — | USRSEC (READ, DELETE) | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |

### 1.3 Summary Statistics

| Metric | Count |
|--------|-------|
| Total COBOL programs | 29 |
| Batch programs | 12 |
| Online (CICS) programs | 17 |
| Total lines of code | ~18,395 |
| Unique copybooks used | 30 |

---

## 2. JCL Jobs (app/jcl/)

### 2.1 Data Definition / VSAM Setup Jobs

| # | Filename | Purpose | Steps |
|---|----------|---------|-------|
| 1 | **ACCTFILE.jcl** | Delete/define/load Account VSAM KSDS from flat file | STEP05: IDCAMS DELETE → STEP10: IDCAMS DEFINE CLUSTER → STEP15: IDCAMS REPRO |
| 2 | **CARDFILE.jcl** | Delete/define/load Card data VSAM KSDS; define AIX and path | CLCIFIL: SDSF (close) → STEP05: IDCAMS DELETE → STEP10: DEFINE CLUSTER → STEP15: REPRO → STEP40–60: Define AIX/path/build index → OPCIFIL: SDSF (open) |
| 3 | **CUSTFILE.jcl** | Delete/define/load Customer VSAM KSDS | CLCIFIL: SDSF → STEP05: DELETE → STEP10: DEFINE → STEP15: REPRO → OPCIFIL: SDSF |
| 4 | **XREFFILE.jcl** | Delete/define/load Card–Account cross-reference VSAM KSDS; define AIX and path | STEP05: DELETE → STEP10: DEFINE → STEP15: REPRO → STEP20–30: AIX/path/build |
| 5 | **TRANFILE.jcl** | Define transaction master VSAM KSDS; define AIX and path | CLCIFIL: SDSF → STEP05–15: DELETE/DEFINE/REPRO → STEP20–30: AIX/path/build → OPCIFIL: SDSF |
| 6 | **TCATBALF.jcl** | Define transaction category balance VSAM KSDS | STEP05: DELETE → STEP10: DEFINE → STEP15: REPRO |
| 7 | **TRANTYPE.jcl** | Define transaction type VSAM KSDS | STEP05: DELETE → STEP10: DEFINE → STEP15: REPRO |
| 8 | **TRANCATG.jcl** | Define transaction category VSAM KSDS | STEP05: DELETE → STEP10: DEFINE → STEP15: REPRO |
| 9 | **DISCGRP.jcl** | Define disclosure group VSAM KSDS | STEP05: DELETE → STEP10: DEFINE → STEP15: REPRO |
| 10 | **DUSRSECJ.jcl** | Define user security VSAM KSDS from inline data | PREDEL: IEFBR14 → STEP01: IEBGENER (create flat file) → STEP02: IDCAMS DEFINE → STEP03: IDCAMS REPRO |
| 11 | **ESDSRRDS.jcl** | Define ESDS and RRDS VSAM datasets (test/demo) | PREDEL: IEFBR14 → STEP01–05: Various DEFINE/REPRO for ESDS and RRDS |
| 12 | **DEFCUST.jcl** | Define customer data file (alternate method) | STEP05: IDCAMS DELETE → STEP05: IDCAMS DEFINE |
| 13 | **TRANIDX.jcl** | Define AIX on transaction master | STEP20–30: IDCAMS DEFINE AIX/path/build |

### 2.2 GDG Base Definition Jobs

| # | Filename | Purpose | Steps |
|---|----------|---------|-------|
| 1 | **DEFGDGB.jcl** | Define GDG bases for TRANSACT.BKUP, DALYREJS, SYSTRAN, TRANREPT | STEP05: IDCAMS DEFINE GDG |
| 2 | **DEFGDGD.jcl** | Define GDG bases for Db2 reference data (TRANTYPE, TRANCATG, DISCGRP); backup existing data | STEP10: IDCAMS GDG → STEP20: IEBGENER backup → STEP30–60: more GDG/backup pairs |
| 3 | **DALYREJS.jcl** | Define GDG base for daily rejected transactions | STEP05: IDCAMS DEFINE GDG |
| 4 | **REPTFILE.jcl** | Define GDG base for report file | STEP05: IDCAMS DEFINE GDG |

### 2.3 Batch Processing Jobs

| # | Filename | Purpose | Steps |
|---|----------|---------|-------|
| 1 | **READACCT.jcl** | Run CBACT01C — read accounts and write to flat/array/VB files | PREDEL: IEFBR14 (cleanup) → STEP05: PGM=CBACT01C |
| 2 | **READCARD.jcl** | Run CBACT02C — read and print card data | STEP05: PGM=CBACT02C |
| 3 | **READCUST.jcl** | Run CBCUS01C — read and print customer data | STEP05: PGM=CBCUS01C |
| 4 | **READXREF.jcl** | Run CBACT03C — read and print cross-reference data | STEP05: PGM=CBACT03C |
| 5 | **POSTTRAN.jcl** | Run CBTRN02C — post daily transactions to master file | STEP15: PGM=CBTRN02C |
| 6 | **INTCALC.jcl** | Run CBACT04C — calculate interest on accounts | STEP15: PGM=CBACT04C (PARM='2022071800') |
| 7 | **TRANREPT.jcl** | Run CBTRN03C — generate transaction detail report | STEP05R: REPROC (backup TRANSACT) → STEP05R: SORT (sort by date) → STEP10R: PGM=CBTRN03C |
| 8 | **TRANBKP.jcl** | Backup and clear transaction master | STEP05R: REPROC (REPRO to GDG) → STEP05: IDCAMS DELETE → STEP10: IDCAMS DEFINE (recreate empty) |
| 9 | **COMBTRAN.jcl** | Combine transaction backup with system transactions | STEP05R: SORT (merge TRANSACT.BKUP + SYSTRAN → COMBINED) → STEP10: IDCAMS REPRO to VSAM |
| 10 | **PRTCATBL.jcl** | Print transaction category balance file | DELDEF: IEFBR14 → STEP05R: REPROC → STEP10R: SORT |
| 11 | **CREASTMT.JCL** | Create customer statements (HTML + PS) | DELDEF01: IDCAMS → STEP010: SORT → STEP020: IDCAMS REPRO → STEP030: IEFBR14 → STEP040: PGM=CBSTM03A |
| 12 | **CBEXPORT.jcl** | Run CBEXPORT — export customer data for migration | STEP01: IDCAMS (define export file) → STEP02: PGM=CBEXPORT |
| 13 | **CBIMPORT.jcl** | Run CBIMPORT — import customer data from export | STEP01: PGM=CBIMPORT |
| 14 | **WAITSTEP.jcl** | Run COBSWAIT — utility wait step | WAIT: PGM=COBSWAIT |

### 2.4 CICS Administration / Utility Jobs

| # | Filename | Purpose | Steps |
|---|----------|---------|-------|
| 1 | **CBADMCDJ.jcl** | CICS CSD update — define programs, mapsets, transactions, files for CardDemo in CICS | STEP1: PGM=DFHCSDUP |
| 2 | **CLOSEFIL.jcl** | Close CICS files for batch processing | CLCIFIL: PGM=SDSF |
| 3 | **OPENFIL.jcl** | Open CICS files after batch processing | OPCIFIL: PGM=SDSF |
| 4 | **FTPJCL.JCL** | FTP file transfer utility | STEP1: PGM=FTP |
| 5 | **INTRDRJ1.JCL** | Internal reader job — IDCAMS REPRO + submit INTRDRJ2 via IEBGENER | IDCAMS: REPRO → STEP01: IEBGENER (submit next job) |
| 6 | **INTRDRJ2.JCL** | Internal reader job (chained from INTRDRJ1) | IDCAMS: REPRO |
| 7 | **TXT2PDF1.JCL** | Convert text file (statement) to PDF | TXT2PDF: PGM=IKJEFT1B |
