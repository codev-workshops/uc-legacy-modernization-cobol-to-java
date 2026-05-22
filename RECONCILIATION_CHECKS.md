# Reconciliation Checks by Batch Job

This document catalogues every JCL batch job in `app/jcl/`, what data it
reads and writes, the reconciliation checks that should pass after
execution, and the business rules each job enforces.

---

## Infrastructure / Setup Jobs

These jobs create or manage VSAM clusters, GDG bases, and CICS resources.
They do not transform application data but must succeed (RC 0 or 4) before
the processing jobs can run.

### ACCTFILE.jcl — Define Account VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Delete and re-define the Account master VSAM KSDS cluster |
| **Program** | `IDCAMS` (utility) |
| **Reads** | None (IDCAMS control statements) |
| **Writes** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (cluster definition) |
| **Record Layout** | `CVACT01Y.cpy` — RECLN 300, key = `ACCT-ID` (11 bytes, offset 0) |
| **Reconciliation** | Cluster exists with correct key length (11) and record size (300) |
| **Business Rules** | None (infrastructure only) |

### CARDFILE.jcl — Define Card Data VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Close CICS files, delete and re-define Card VSAM KSDS + AIX |
| **Program** | `IDCAMS`, `SDSF` (CICS file close) |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` + AIX cluster |
| **Record Layout** | `CVACT02Y.cpy` — RECLN 150, key = `CARD-NUM` (16 bytes, offset 0) |
| **Reconciliation** | Cluster and AIX exist; AIX relates to base cluster |
| **Business Rules** | None (infrastructure only) |

### CUSTFILE.jcl — Define Customer VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Close CICS customer file, delete and re-define Customer VSAM KSDS |
| **Program** | `IDCAMS`, `SDSF` |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` |
| **Record Layout** | `CVCUS01Y.cpy` — RECLN 500, key = `CUST-ID` (9 bytes, offset 0) |
| **Reconciliation** | Cluster exists with key length 9 and record size 500 |
| **Business Rules** | None (infrastructure only) |

### DEFCUST.jcl — Define Alternate Customer Cluster

| Attribute | Value |
|-----------|-------|
| **Purpose** | Delete/define an alternate customer VSAM cluster (`AWS.CCDA.CUSTDATA.CLUSTER`) |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | `AWS.CCDA.CUSTDATA.CLUSTER` |
| **Record Layout** | `CVCUS01Y.cpy` — RECLN 500, key length 10 |
| **Reconciliation** | Cluster created; RC <= 8 |
| **Business Rules** | None |

### XREFFILE.jcl — Define Card Cross-Reference VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Delete and re-define Card xref VSAM KSDS + AIX |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` + AIX |
| **Record Layout** | `CVACT03Y.cpy` — RECLN 50, key = `XREF-CARD-NUM` (16 bytes, offset 0) |
| **Reconciliation** | Cluster and AIX created |
| **Business Rules** | None |

### TRANFILE.jcl — Define Transaction Master VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Close CICS transaction files, delete and re-define Transaction master VSAM KSDS + AIX |
| **Program** | `IDCAMS`, `SDSF` |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` + AIX |
| **Record Layout** | `CVTRA05Y.cpy` — RECLN 350, key = `TRAN-ID` (16 bytes, offset 0) |
| **Reconciliation** | Cluster and AIX exist |
| **Business Rules** | None |

### TRANIDX.jcl — Define AIX on Transaction Master

| Attribute | Value |
|-----------|-------|
| **Purpose** | Create alternate index on processed timestamp field |
| **Program** | `IDCAMS` |
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (base cluster) |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX` + PATH |
| **AIX Key** | Processed timestamp — 26 bytes at offset 304, non-unique |
| **Reconciliation** | AIX and PATH created; BLDINDEX completes RC 0 |
| **Business Rules** | None |

### TCATBALF.jcl — Define Transaction Category Balance VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Delete and re-define transaction category balance VSAM KSDS |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` |
| **Record Layout** | `CVTRA01Y.cpy` — RECLN 50, key = compound (`ACCT-ID` + `TYPE-CD` + `CAT-CD`, 17 bytes) |
| **Reconciliation** | Cluster exists with key length 17 |
| **Business Rules** | None |

### DISCGRP.jcl — Define Disclosure Group VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Delete and re-define disclosure group VSAM KSDS |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` |
| **Record Layout** | `CVTRA02Y.cpy` — RECLN 50, key = compound (`GROUP-ID` + `TYPE-CD` + `CAT-CD`, 16 bytes) |
| **Reconciliation** | Cluster exists with key length 16 |
| **Business Rules** | None |

### TRANCATG.jcl — Define Transaction Category Type VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Delete and re-define transaction category type VSAM KSDS |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` |
| **Record Layout** | `CVTRA04Y.cpy` — RECLN 60, key = compound (`TYPE-CD` + `CAT-CD`, 6 bytes) |
| **Reconciliation** | Cluster exists with key length 6 |
| **Business Rules** | None |

### TRANTYPE.jcl — Define Transaction Type VSAM File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Delete and re-define transaction type VSAM KSDS |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` |
| **Record Layout** | `CVTRA03Y.cpy` — RECLN 60, key = `TRAN-TYPE` (2 bytes, offset 0) |
| **Reconciliation** | Cluster exists with key length 2 |
| **Business Rules** | None |

### DEFGDGB.jcl — Define GDG Bases

| Attribute | Value |
|-----------|-------|
| **Purpose** | Define Generation Data Group bases for transaction backups and reports |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | GDG bases: `TRANSACT.BKUP`, `TRANSACT.DALY`, `TRANREPT` (limit 5 each) |
| **Reconciliation** | All three GDG bases defined |
| **Business Rules** | None |

### DEFGDGD.jcl — Define DB2 GDG + Load Transaction Type

| Attribute | Value |
|-----------|-------|
| **Purpose** | Define GDG for transaction type backup; load first generation from PS |
| **Program** | `IDCAMS`, `IEBGENER` |
| **Reads** | `AWS.M2.CARDDEMO.TRANTYPE.PS` (flat file seed data) |
| **Writes** | GDG base `TRANTYPE.BKUP`; first generation `TRANTYPE.BKUP(+1)` |
| **Reconciliation** | GDG defined; first generation loaded with correct record count |
| **Business Rules** | Transaction type reference data must be seeded before batch processing |

### DALYREJS.jcl — Define GDG for Daily Rejects

| Attribute | Value |
|-----------|-------|
| **Purpose** | Define GDG base for daily transaction rejects |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | GDG base `AWS.M2.CARDDEMO.DALYREJS` (limit 5) |
| **Reconciliation** | GDG base defined |
| **Business Rules** | None |

### REPTFILE.jcl — Define GDG for Report Files

| Attribute | Value |
|-----------|-------|
| **Purpose** | Define GDG base for transaction reports |
| **Program** | `IDCAMS` |
| **Reads** | None |
| **Writes** | GDG base `AWS.M2.CARDDEMO.TRANREPT` (limit 10) |
| **Reconciliation** | GDG base defined |
| **Business Rules** | None |

### DUSRSECJ.jcl — Load User Security File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Create PS file with in-stream user/admin security records |
| **Program** | `IEBGENER` |
| **Reads** | In-stream data (ADMIN001..ADMIN005, USER0001...) |
| **Writes** | `AWS.M2.CARDDEMO.USRSEC.PS` |
| **Reconciliation** | File created with expected number of user records; admin users have type `A`, regular users have type `U` |
| **Business Rules** | Security file must be loaded before CICS sign-on can work |

### ESDSRRDS.jcl — Load ESDS/RRDS Security File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Create alternate PS file for ESDS/RRDS-format user security data |
| **Program** | `IEBGENER` |
| **Reads** | In-stream data |
| **Writes** | `AWS.M2.CARDDEMO.ESDSRRDS.PS` |
| **Reconciliation** | File created |
| **Business Rules** | Same user data as DUSRSECJ; alternate storage format |

### CBADMCDJ.jcl — CICS Resource Definitions

| Attribute | Value |
|-----------|-------|
| **Purpose** | Define CICS resources (programs, files, transactions) for CardDemo |
| **Program** | `DFHCSDUP` |
| **Reads** | In-stream CSD commands |
| **Writes** | CICS CSD (System Definition) |
| **Reconciliation** | All CARDDEMO group resources installed |
| **Business Rules** | Must run before CICS online transactions can execute |

### CLOSEFIL.jcl — Close CICS Files

| Attribute | Value |
|-----------|-------|
| **Purpose** | Close VSAM files in the CICS region before batch processing |
| **Program** | `SDSF` (CEMT SET FIL ... CLO) |
| **Reads** | None |
| **Writes** | None (state change) |
| **Reconciliation** | All files closed (TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC) |
| **Business Rules** | Sandwich pattern: files must be closed before batch jobs modify them |

### OPENFIL.jcl — Open CICS Files

| Attribute | Value |
|-----------|-------|
| **Purpose** | Re-open VSAM files in the CICS region after batch processing |
| **Program** | `SDSF` (CEMT SET FIL ... OPE) |
| **Reads** | None |
| **Writes** | None (state change) |
| **Reconciliation** | All files open (TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC) |
| **Business Rules** | Sandwich pattern: files must be re-opened after batch completes |

### WAITSTEP.jcl — Timer Wait

| Attribute | Value |
|-----------|-------|
| **Purpose** | Wait for a specified number of centiseconds (scheduling delay) |
| **Program** | `COBSWAIT` |
| **Reads** | Centisecond value from SYSIN (e.g., `00003600` = 36 seconds) |
| **Writes** | None |
| **Reconciliation** | Job completes RC 0 after the specified wait |
| **Business Rules** | Used for scheduling delays between batch steps |

---

## Data Processing Jobs

These jobs execute COBOL programs that read, transform, and write
application data. Each has specific reconciliation checks.

### READACCT.jcl — Read Account Master

| Attribute | Value |
|-----------|-------|
| **Purpose** | Read all records from Account VSAM file and write to flat PS file |
| **Program** | `CBACT01C` |
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (RECLN 300) |
| **Writes** | `AWS.M2.CARDDEMO.ACCTDATA.PSCOMP` (RECFM=FB, LRECL=107) |
| **Reconciliation Checks** | |
| Record count | Output records = input VSAM records |
| Field integrity | All `ACCT-ID` values in output match source |
| Key order | Output is in `ACCT-ID` ascending order |
| **Business Rules** | Read-only extract; no data transformation |

### READCARD.jcl — Read Card Master

| Attribute | Value |
|-----------|-------|
| **Purpose** | Read all records from Card VSAM file and print to SYSOUT |
| **Program** | `CBACT02C` |
| **Reads** | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` (RECLN 150) |
| **Writes** | SYSOUT (print) |
| **Reconciliation Checks** | |
| Record count | Number of printed records = VSAM record count |
| **Business Rules** | Read-only; diagnostic/audit job |

### READCUST.jcl — Read Customer Master

| Attribute | Value |
|-----------|-------|
| **Purpose** | Read all records from Customer VSAM file and print to SYSOUT |
| **Program** | `CBCUS01C` |
| **Reads** | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` (RECLN 500) |
| **Writes** | SYSOUT (print) |
| **Reconciliation Checks** | |
| Record count | Number of printed records = VSAM record count |
| **Business Rules** | Read-only; diagnostic/audit job |

### READXREF.jcl — Read Card Cross-Reference

| Attribute | Value |
|-----------|-------|
| **Purpose** | Read all records from Card xref VSAM file and print to SYSOUT |
| **Program** | `CBACT03C` |
| **Reads** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` (RECLN 50) |
| **Writes** | SYSOUT (print) |
| **Reconciliation Checks** | |
| Record count | Number of printed records = VSAM record count |
| Referential integrity | Every `XREF-ACCT-ID` exists in acctdata; every `XREF-CUST-ID` exists in custdata |
| **Business Rules** | Read-only; every xref record must reference valid account and customer |

### POSTTRAN.jcl — Post Daily Transactions

| Attribute | Value |
|-----------|-------|
| **Purpose** | Process daily transactions: validate, post to transaction master, update category balances, update account balances |
| **Program** | `CBTRN02C` |
| **Reads** | |
| | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` — Transaction master |
| | `AWS.M2.CARDDEMO.DALYTRAN.PS` — Daily transaction input (RECLN 350) |
| | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` — Card cross-reference |
| | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` — Account master |
| **Writes** | |
| | Transaction master (updated with new transactions) |
| | `AWS.M2.CARDDEMO.DALYREJS(+1)` — Rejected transactions (LRECL 430) |
| | Account master (balances updated) |
| **Reconciliation Checks** | |
| Input = Output + Rejects | `daily_input_count = posted_count + reject_count` |
| Balance integrity | For each account: `new_balance = old_balance + sum(posted transactions for that account)` |
| Cycle totals | `ACCT-CURR-CYC-CREDIT` increased by sum of credit transactions; `ACCT-CURR-CYC-DEBIT` increased by sum of debit transactions |
| Card validation | Every `DALYTRAN-CARD-NUM` must resolve via `CARDXREF` to a valid account |
| Amount non-zero | Transaction amount (`DALYTRAN-AMT`) must be non-zero |
| **Business Rules** | |
| | Transactions with invalid card numbers are rejected |
| | Transactions that would exceed credit limit are rejected |
| | Transaction type code must exist in transaction type reference |
| | Rejected transactions include original record + 80-byte reject reason |

### INTCALC.jcl — Interest Calculation

| Attribute | Value |
|-----------|-------|
| **Purpose** | Calculate interest and fees on category balances |
| **Program** | `CBACT04C` (module, called with date PARM `2022071800`) |
| **Reads** | |
| | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` — Category balances |
| | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` + AIX PATH — Card xref |
| | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` — Account master |
| | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` — Disclosure groups (interest rates) |
| **Writes** | |
| | System-generated transactions (LRECL 350) |
| | Updated account balances |
| **Reconciliation Checks** | |
| Interest accuracy | For each (account, category): `interest = balance * rate / 365 * days` where rate comes from disclosure group |
| Rate lookup | Every account's `ACCT-GROUP-ID` must have matching entries in `discgrp` for each category's type code |
| Output count | One interest transaction generated per non-zero category balance |
| Balance update | Account `ACCT-CURR-BAL` increased by sum of interest charges |
| **Business Rules** | |
| | Interest rate is looked up by: `ACCT-GROUP-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD` |
| | Date parameter determines the calculation date |
| | Zero-balance categories generate no interest transaction |

### COMBTRAN.jcl — Combine Transactions

| Attribute | Value |
|-----------|-------|
| **Purpose** | Sort and merge current transaction backup with system-generated transactions |
| **Program** | `SORT` (DFSORT/SYNCSORT) |
| **Reads** | |
| | `AWS.M2.CARDDEMO.TRANSACT.BKUP(0)` — Current transaction backup |
| | `AWS.M2.CARDDEMO.SYSTRAN(0)` — System-generated transactions |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.COMBINED(+1)` |
| **Reconciliation Checks** | |
| Record count | `combined_count = backup_count + systran_count` |
| Sort order | Output sorted ascending by `TRAN-ID` (positions 1-16) |
| No data loss | Every record from both inputs present in output |
| **Business Rules** | |
| | Merge is a union; no deduplication |
| | Combined file becomes the new transaction master input |

### TRANBKP.jcl — Backup Transaction Master

| Attribute | Value |
|-----------|-------|
| **Purpose** | REPRO the transaction master VSAM to a new GDG generation, then delete and re-define the VSAM cluster |
| **Program** | `REPROC` (proc), `IDCAMS` |
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.BKUP(+1)` (LRECL 350, FB) |
| **Reconciliation Checks** | |
| Record count | Backup GDG generation record count = VSAM record count before delete |
| Byte-for-byte | Backup content SHA-256 = VSAM export SHA-256 |
| VSAM empty | After re-define, VSAM cluster has 0 records |
| **Business Rules** | |
| | Must run before POSTTRAN to preserve pre-posting state |
| | Supports rollback: if POSTTRAN fails, restore from backup |

### TRANREPT.jcl — Transaction Report

| Attribute | Value |
|-----------|-------|
| **Purpose** | Unload transaction master, filter by date, sort by card number, generate daily transaction report |
| **Program** | `REPROC` (proc), `SORT`, `CBTRN03C` |
| **Reads** | |
| | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` — Transaction master |
| | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` — Card xref (for account lookup) |
| | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` — Type descriptions |
| | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` — Category descriptions |
| **Writes** | |
| | `AWS.M2.CARDDEMO.TRANSACT.BKUP(+1)` — Unloaded transactions |
| | `AWS.M2.CARDDEMO.TRANREPT(+1)` — Formatted report |
| **Reconciliation Checks** | |
| Report totals | Grand total on report = sum of all `TRAN-AMT` for filtered transactions |
| Account totals | Each account subtotal = sum of that account's transactions |
| Page totals | Each page total = sum of transactions on that page |
| Record coverage | Every transaction in the date range appears on the report |
| Type/Category desc | Report type/category descriptions match reference data |
| **Business Rules** | |
| | Report layout defined in `CVTRA07Y.cpy` |
| | Transactions filtered by date range from PARM |
| | Sorted by card number within the date range |
| | Account totals printed after last transaction per account |
| | Grand total printed at end of report |

### PRTCATBL.jcl — Print Category Balance File

| Attribute | Value |
|-----------|-------|
| **Purpose** | Unload category balance VSAM to flat file and backup |
| **Program** | `REPROC` (proc) |
| **Reads** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` (RECLN 50) |
| **Writes** | `AWS.M2.CARDDEMO.TCATBALF.BKUP(+1)` (LRECL 50, FB) |
| **Reconciliation Checks** | |
| Record count | Backup count = VSAM count |
| Balance sum | Sum of `TRAN-CAT-BAL` in backup = sum in VSAM |
| **Business Rules** | Read-only extract for audit/reporting purposes |

### CBEXPORT.jcl — Export Customer Data for Migration

| Attribute | Value |
|-----------|-------|
| **Purpose** | Export customer, account, card, transaction, and xref data to a single multi-record export file for branch migration |
| **Program** | `CBEXPORT` |
| **Reads** | |
| | Customer, Account, Card, Transaction, Card-Xref VSAM files |
| **Writes** | `AWS.M2.CARDDEMO.EXPORT.DATA` (VSAM KSDS, RECLN 500) |
| **Record Layout** | `CVEXPORT.cpy` — multi-record type with REDEFINES |
| **Reconciliation Checks** | |
| Record count | Total export records = sum of (customer + account + card + transaction + xref) records |
| Record types | Export contains records of each type (C, A, T, X, D for Customer, Account, Transaction, Xref, carD) |
| Key integrity | All IDs in export records match source VSAM files |
| Sequence numbers | Export sequence numbers are contiguous within each record type |
| **Business Rules** | |
| | Export record type field (`EXPORT-REC-TYPE`) distinguishes entity type |
| | Uses COMP/COMP-3 for numeric fields (storage optimization) |
| | Branch ID and region code tag each record for routing |

### CBIMPORT.jcl — Import Customer Data

| Attribute | Value |
|-----------|-------|
| **Purpose** | Read multi-record export file and split into separate normalized flat files |
| **Program** | `CBIMPORT` |
| **Reads** | `AWS.M2.CARDDEMO.EXPORT.DATA` |
| **Writes** | |
| | `AWS.M2.CARDDEMO.CUSTDATA.IMPORT` (LRECL 500) |
| | `AWS.M2.CARDDEMO.ACCTDATA.IMPORT` |
| | Other entity-specific import files |
| **Reconciliation Checks** | |
| Record count | Sum of all output file records = export file record count |
| Roundtrip | Import -> re-export should produce identical export file |
| Data integrity | Each output file's records match the corresponding entity in the export |
| **Business Rules** | |
| | Splits by `EXPORT-REC-TYPE` |
| | Reverse of CBEXPORT: normalizes packed-decimal fields back to display format |

---

## Data Loading Jobs (REPRO from flat files)

These steps (embedded in the DEFINE jobs above) load ASCII/PS data into
the newly defined VSAM clusters using `IDCAMS REPRO`.

| Job | Source (PS/ASCII) | Target (VSAM) | Key | RECLN |
|-----|------------------|---------------|-----|-------|
| `ACCTFILE.jcl` (STEP15) | `ACCTDATA.PS` | `ACCTDATA.VSAM.KSDS` | `ACCT-ID` (11,0) | 300 |
| `CARDFILE.jcl` (STEP15) | `CARDDATA.PS` | `CARDDATA.VSAM.KSDS` | `CARD-NUM` (16,0) | 150 |
| `CUSTFILE.jcl` (STEP15) | `CUSTDATA.PS` | `CUSTDATA.VSAM.KSDS` | `CUST-ID` (9,0) | 500 |
| `XREFFILE.jcl` (STEP15) | `CARDXREF.PS` | `CARDXREF.VSAM.KSDS` | `XREF-CARD-NUM` (16,0) | 50 |
| `DISCGRP.jcl` (STEP15) | `DISCGRP.PS` | `DISCGRP.VSAM.KSDS` | compound (16,0) | 50 |
| `TCATBALF.jcl` (STEP15) | `TCATBALF.PS` | `TCATBALF.VSAM.KSDS` | compound (17,0) | 50 |
| `TRANCATG.jcl` (STEP15) | `TRANCATG.PS` | `TRANCATG.VSAM.KSDS` | compound (6,0) | 60 |
| `TRANTYPE.jcl` (STEP15) | `TRANTYPE.PS` | `TRANTYPE.VSAM.KSDS` | `TRAN-TYPE` (2,0) | 60 |

**Reconciliation for all REPRO steps:**
- Input record count = VSAM record count after REPRO
- Key values in VSAM are unique and sorted ascending
- REPRO RC = 0

---

## FTP/Transfer Jobs

### FTPJCL.JCL — FTP Transfer

| Attribute | Value |
|-----------|-------|
| **Purpose** | FTP data files to/from remote systems |
| **Program** | FTP |
| **Reconciliation** | Transfer completion RC 0; file sizes match |
| **Business Rules** | Used for data distribution; not part of core batch processing |

### TXT2PDF1.JCL — Convert Text to PDF

| Attribute | Value |
|-----------|-------|
| **Purpose** | Convert text reports to PDF format |
| **Program** | TXT2PDF utility |
| **Reconciliation** | PDF generated; page count matches source |
| **Business Rules** | Formatting job; no data transformation |

### INTRDRJ1.JCL / INTRDRJ2.JCL — Internal Reader Jobs

| Attribute | Value |
|-----------|-------|
| **Purpose** | Submit jobs via internal reader |
| **Reconciliation** | Submitted jobs complete successfully |
| **Business Rules** | Job scheduling mechanism |

---

## Batch Processing Sequence

The standard nightly batch cycle runs in this order:

```
1. CLOSEFIL     — Close CICS files (sandwich open)
2. TRANBKP      — Backup transaction master
3. POSTTRAN      — Post daily transactions
4. INTCALC       — Calculate interest
5. COMBTRAN      — Combine transactions
6. TRANREPT      — Generate transaction report
7. PRTCATBL      — Print category balances
8. OPENFIL       — Re-open CICS files (sandwich close)
```

### End-to-End Reconciliation

After the full batch cycle:

| Check | Formula |
|-------|---------|
| Transaction conservation | `backup_count + daily_count - reject_count + interest_count = new_master_count` |
| Balance sheet | `sum(ACCT-CURR-BAL after) = sum(ACCT-CURR-BAL before) + sum(posted daily) + sum(interest)` |
| Category balance | `sum(TRAN-CAT-BAL after) = sum(TRAN-CAT-BAL before) + sum(posted by category) + sum(interest by category)` |
| Report completeness | Transaction report covers all transactions for the reporting date |
| Reject accounting | All rejects have a documented reason code |
| Referential integrity | All FK relationships still valid post-batch |
