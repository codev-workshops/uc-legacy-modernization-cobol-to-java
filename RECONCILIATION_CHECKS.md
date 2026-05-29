# CardDemo Batch Job Reconciliation Checks

This document details every batch job defined in `app/jcl/`, including what it
reads, what it writes, what reconciliation checks should pass, and what business
rules it enforces.

---

## 1. POSTTRAN — Daily Transaction Posting

**JCL**: `POSTTRAN.jcl`
**Program**: `CBTRN02C`

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `DALYTRAN` | `CARDDEMO.DALYTRAN.PS` | `CVTRA06Y.cpy` (350 bytes) | Daily transaction feed |
| `XREFFILE` | `CARDDEMO.CARDXREF.VSAM.KSDS` | `CVACT03Y.cpy` (50 bytes) | Card-to-account cross-reference |
| `ACCTFILE` | `CARDDEMO.ACCTDATA.VSAM.KSDS` | `CVACT01Y.cpy` (300 bytes) | Account master |
| `TCATBALF` | `CARDDEMO.TCATBALF.VSAM.KSDS` | `CVTRA01Y.cpy` (50 bytes) | Transaction category balance |
| `TRANFILE` | `CARDDEMO.TRANSACT.VSAM.KSDS` | `CVTRA05Y.cpy` (350 bytes) | Transaction master |

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TRANFILE` | `CARDDEMO.TRANSACT.VSAM.KSDS` | `CVTRA05Y.cpy` (350 bytes) | Updated transaction master |
| `ACCTFILE` | `CARDDEMO.ACCTDATA.VSAM.KSDS` | `CVACT01Y.cpy` (300 bytes) | Updated account balances |
| `TCATBALF` | `CARDDEMO.TCATBALF.VSAM.KSDS` | `CVTRA01Y.cpy` (50 bytes) | Updated category balances |
| `DALYREJS` | `CARDDEMO.DALYREJS(+1)` | 430 bytes (350 tran + 80 reason) | Rejected transactions |

### Reconciliation Checks

1. **Record count balance**:
   `input_dalytran_count = posted_count + rejected_count`
   Every input transaction must either be posted or rejected. No records lost.

2. **Account balance integrity**:
   For each account that had transactions posted:
   `ACCT-CURR-BAL(after) = ACCT-CURR-BAL(before) + SUM(DALYTRAN-AMT for that account)`

3. **Cycle credit/debit split**:
   - `ACCT-CURR-CYC-CREDIT = SUM(positive DALYTRAN-AMT posted this cycle)`
   - `ACCT-CURR-CYC-DEBIT = SUM(negative DALYTRAN-AMT posted this cycle)`

4. **Category balance accumulation**:
   For each `(ACCT-ID, TYPE-CD, CAT-CD)` combination:
   `TRAN-CAT-BAL(after) = TRAN-CAT-BAL(before) + SUM(DALYTRAN-AMT matching that key)`

5. **Transaction master growth**:
   `TRANSACT record count(after) = TRANSACT record count(before) + posted_count`

### Business Rules Enforced

- **Rule 102 — Credit limit check**: Reject if
  `ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT > ACCT-CREDIT-LIMIT`
- **Rule 103 — Expiration check**: Reject if
  `DALYTRAN-ORIG-TS > ACCT-EXPIRAION-DATE`
- **Card lookup**: Reject if `DALYTRAN-CARD-NUM` is not found in `CARDXREF`
- **Account status**: Reject if account is not active (`ACCT-ACTIVE-STATUS ≠ 'Y'`)

---

## 2. INTCALC — Interest and Fee Calculation

**JCL**: `INTCALC.jcl`
**Program**: `CBACT04C`
**Parameters**: Date parameter (e.g., `2022071800`)

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TCATBALF` | `CARDDEMO.TCATBALF.VSAM.KSDS` | `CVTRA01Y.cpy` (50 bytes) | Category balances |
| `XREFFILE` | `CARDDEMO.CARDXREF.VSAM.KSDS` | `CVACT03Y.cpy` (50 bytes) | Card cross-reference |
| `XREFFIL1` | `CARDDEMO.CARDXREF.VSAM.AIX.PATH` | — | AIX path for account lookup |
| `ACCTFILE` | `CARDDEMO.ACCTDATA.VSAM.KSDS` | `CVACT01Y.cpy` (300 bytes) | Account master |
| `DISCGRP` | `CARDDEMO.DISCGRP.VSAM.KSDS` | `CVTRA02Y.cpy` (50 bytes) | Interest rate reference |

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TRANSACT` | `CARDDEMO.SYSTRAN(+1)` | `CVTRA05Y.cpy` (350 bytes) | System-generated interest transactions |

### Reconciliation Checks

1. **One interest transaction per category balance**:
   For each non-zero `TRAN-CAT-BAL` record, exactly one interest transaction
   should be generated.

2. **Interest amount calculation**:
   `TRAN-AMT = TRAN-CAT-BAL × DIS-INT-RATE / 1200` (monthly rate)
   Look up `DIS-INT-RATE` by matching `(ACCT-GROUP-ID, TRANCAT-TYPE-CD, TRANCAT-CD)`.

3. **Output record count**:
   `SYSTRAN record count = count of non-zero TCATBALF records`

4. **Interest transaction type**:
   All generated transactions should have `TRAN-TYPE-CD = '01'` and
   `TRAN-CAT-CD = '0005'` (Interest Amount).

### Business Rules Enforced

- Interest is only calculated on non-zero category balances.
- The disclosure group rate lookup uses `ACCT-GROUP-ID` from the account
  record, cross-referenced through `CARDXREF`.
- If no matching disclosure group exists, no interest is calculated for that
  category balance.

---

## 3. TRANREPT — Daily Transaction Report

**JCL**: `TRANREPT.jcl`
**Program**: `CBTRN03C` (preceded by SORT step)

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TRANFILE` | `CARDDEMO.TRANSACT.DALY(+1)` | `CVTRA05Y.cpy` (350 bytes) | Filtered/sorted transactions |
| `CARDXREF` | `CARDDEMO.CARDXREF.VSAM.KSDS` | `CVACT03Y.cpy` (50 bytes) | Card cross-reference |
| `TRANTYPE` | `CARDDEMO.TRANTYPE.VSAM.KSDS` | `CVTRA03Y.cpy` (60 bytes) | Transaction type descriptions |
| `TRANCATG` | `CARDDEMO.TRANCATG.VSAM.KSDS` | `CVTRA04Y.cpy` (60 bytes) | Transaction category descriptions |
| `DATEPARM` | `CARDDEMO.DATEPARM` | — | Date range parameter |

### Pre-processing (SORT step)

The SORT step filters transactions within a date range and sorts by card number:
- `INCLUDE COND=(TRAN-PROC-DT,GE,PARM-START-DATE,AND,TRAN-PROC-DT,LE,PARM-END-DATE)`
- `SORT FIELDS=(TRAN-CARD-NUM,A)`

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TRANREPT` | `CARDDEMO.TRANREPT(+1)` | 133 bytes | Formatted report |

### Reconciliation Checks

1. **Report record accounting**:
   Total number of detail lines in report = number of transactions in filtered input.

2. **Page totals**:
   Sum of all page-total lines = grand total line.

3. **Account totals**:
   Sum of transaction amounts for each account = account-total line for that
   account.

4. **Grand total**:
   `Grand Total = SUM(TRAN-AMT for all filtered transactions)`

5. **Type and category descriptions**:
   Every `TRAN-TYPE-CD` and `TRAN-CAT-CD` in the report must resolve to a
   valid description from `TRANTYPE` and `TRANCATG`.

---

## 4. COMBTRAN — Combine Transaction Files

**JCL**: `COMBTRAN.jcl`
**Programs**: SORT, IDCAMS

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `SORTIN` (1) | `CARDDEMO.TRANSACT.BKUP(0)` | `CVTRA05Y.cpy` (350 bytes) | Prior transaction backup |
| `SORTIN` (2) | `CARDDEMO.SYSTRAN(0)` | `CVTRA05Y.cpy` (350 bytes) | System-generated transactions |

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `SORTOUT` | `CARDDEMO.TRANSACT.COMBINED(+1)` | 350 bytes | Sorted combined transactions |
| `TRANVSAM` | `CARDDEMO.TRANSACT.VSAM.KSDS` | 350 bytes | Reloaded transaction master |

### Reconciliation Checks

1. **Record count conservation**:
   `COMBINED count = BKUP count + SYSTRAN count`

2. **Sort order verification**:
   Output records are sorted ascending by `TRAN-ID` (positions 1–16).

3. **No duplicate keys**:
   Every `TRAN-ID` in the combined output is unique.

4. **VSAM reload integrity**:
   `TRANSACT.VSAM count = COMBINED count`

---

## 5. CBEXPORT — Multi-Record Data Export

**JCL**: `CBEXPORT.jcl`
**Program**: `CBEXPORT`

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `CUSTFILE` | `CARDDEMO.CUSTDATA.VSAM.KSDS` | `CVCUS01Y.cpy` (500 bytes) | Customer master |
| `ACCTFILE` | `CARDDEMO.ACCTDATA.VSAM.KSDS` | `CVACT01Y.cpy` (300 bytes) | Account master |
| `XREFFILE` | `CARDDEMO.CARDXREF.VSAM.KSDS` | `CVACT03Y.cpy` (50 bytes) | Card cross-reference |
| `TRANSACT` | `CARDDEMO.TRANSACT.VSAM.KSDS` | `CVTRA05Y.cpy` (350 bytes) | Transaction master |
| `CARDFILE` | `CARDDEMO.CARDDATA.VSAM.KSDS` | `CVACT02Y.cpy` (150 bytes) | Card master |

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `EXPFILE` | `CARDDEMO.EXPORT.DATA` | `CVEXPORT.cpy` (500 bytes) | Multi-record type export |

### Reconciliation Checks

1. **Record type counts**:
   - Customer records (type `C`) = CUSTDATA count
   - Account records (type `A`) = ACCTDATA count
   - Transaction records (type `T`) = TRANSACT count
   - Cross-reference records (type `X`) = CARDXREF count

2. **Total export records**:
   `EXPORT count = CUST count + ACCT count + TRAN count + XREF count`

3. **Data fidelity**: Key fields in export records must match source records
   (e.g., `EXP-CUST-ID` = `CUST-ID` for customer records).

4. **Sequence numbering**: `EXPORT-SEQUENCE-NUM` must be consecutive starting
   from 1.

---

## 6. CBIMPORT — Data Import and Normalization

**JCL**: `CBIMPORT.jcl`
**Program**: `CBIMPORT`

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `EXPFILE` | `CARDDEMO.EXPORT.DATA` | `CVEXPORT.cpy` (500 bytes) | Multi-record export file |

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `CUSTOUT` | `CARDDEMO.CUSTDATA.IMPORT` | 500 bytes | Normalized customer records |
| `ACCTOUT` | `CARDDEMO.ACCTDATA.IMPORT` | 300 bytes | Normalized account records |
| `XREFOUT` | `CARDDEMO.CARDXREF.IMPORT` | 50 bytes | Normalized cross-reference records |
| `TRNXOUT` | `CARDDEMO.TRANSACT.IMPORT` | 350 bytes | Normalized transaction records |
| `ERROUT` | `CARDDEMO.IMPORT.ERRORS` | 132 bytes | Error/rejected records |

### Reconciliation Checks

1. **Round-trip integrity** (Export → Import):
   - `CUSTOUT count = original CUSTDATA count`
   - `ACCTOUT count = original ACCTDATA count`
   - `XREFOUT count = original CARDXREF count`
   - `TRNXOUT count = original TRANSACT count`

2. **Input accounting**:
   `EXPFILE count = CUSTOUT count + ACCTOUT count + XREFOUT count + TRNXOUT count + ERROUT count`

3. **Data equivalence**: After import, each normalized file should be
   byte-identical (or field-equivalent) to the original source file.

---

## 7. TRANBKP — Transaction Backup

**JCL**: `TRANBKP.jcl`
**Program**: IDCAMS REPRO

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TRANVSAM` | `CARDDEMO.TRANSACT.VSAM.KSDS` | `CVTRA05Y.cpy` (350 bytes) | Transaction master VSAM |

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TRANBKUP` | `CARDDEMO.TRANSACT.BKUP(+1)` | 350 bytes | Sequential backup (GDG) |

### Reconciliation Checks

1. **Record count match**: `BKUP count = VSAM count`
2. **Byte-for-byte fidelity**: REPRO output must be identical to VSAM content.

---

## 8. PRTCATBL — Print Transaction Category Balance

**JCL**: `PRTCATBL.jcl`
**Programs**: IDCAMS REPRO, SORT

### Inputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TCATBALF` | `CARDDEMO.TCATBALF.VSAM.KSDS` | `CVTRA01Y.cpy` (50 bytes) | Category balance VSAM |

### Outputs

| DD Name | Dataset | Layout | Description |
|---|---|---|---|
| `TCATBALF.BKUP(+1)` | Sequential backup | 50 bytes | Raw VSAM backup |
| `TCATBALF.REPT` | Formatted report | 40 bytes | Sorted/formatted balance report |

### Reconciliation Checks

1. **Backup record count**: `BKUP count = VSAM count`
2. **Report record count**: `REPT count = BKUP count` (all records in report)
3. **Sort order**: Output sorted by `(TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD)` ascending.
4. **Formatted amounts**: `TRAN-CAT-BAL` formatted as `TTTTTTTTT.TT` in report.

---

## 9. Dataset Definition Jobs

These jobs define VSAM clusters and GDG bases. They produce no business output
but must succeed for the batch cycle to run.

| JCL | Purpose | Key VSAM Parameters |
|---|---|---|
| `ACCTFILE.jcl` | Define ACCTDATA KSDS | KEYS(11 0), RECSIZE(300 300) |
| `CARDFILE.jcl` | Define CARDDATA KSDS + AIX | KEYS(16 0), RECSIZE(150 150), AIX KEYS(11 16) |
| `CUSTFILE.jcl` | Define CUSTDATA KSDS | KEYS(9 0), RECSIZE(500 500) |
| `XREFFILE.jcl` | Define CARDXREF KSDS | KEYS(16 0), RECSIZE(50 50) |
| `TCATBALF.jcl` | Define TCATBALF KSDS | KEYS(17 0), RECSIZE(50 50) |
| `TRANFILE.jcl` | Define TRANSACT KSDS | KEYS(16 0), RECSIZE(350 350) |
| `TRANTYPE.jcl` | Define TRANTYPE KSDS | KEYS(2 0), RECSIZE(60 60) |
| `TRANCATG.jcl` | Define TRANCATG KSDS | KEYS(6 0), RECSIZE(60 60) |
| `DISCGRP.jcl` | Define DISCGRP KSDS | KEYS(16 0), RECSIZE(50 50) |
| `DALYREJS.jcl` | Define DALYREJS GDG base | LIMIT(5), SCRATCH |
| `DEFGDGB.jcl` | Define TRANSACT.BKUP GDG | LIMIT(5), SCRATCH |
| `DEFGDGD.jcl` | Define TRANSACT.DALY GDG | LIMIT(5), SCRATCH |

### Reconciliation Check for All Definition Jobs

After each definition job:
- VSAM LISTCAT shows the cluster exists with the correct key length,
  record size, and index.
- REPRO from flat file to VSAM: `VSAM record count = flat file record count`.

---

## 10. CBADMCDJ — CICS Resource Definitions

**JCL**: `CBADMCDJ.jcl`
**Program**: `DFHCSDUP`

This job defines CICS resources (mapsets, programs, transactions, files) for
the CardDemo online system. It has no batch data output, but the following
should hold after execution:

- All MAPSET, PROGRAM, TRANSACTION, and FILE entries are installed in the
  CICS CSD.
- `CEMT INQ PROG(COSGN00C)` returns `ENABLED`.

---

## 11. Daily Batch Cycle Order

The intended daily execution sequence is:

```
1. CLOSEFIL  — Close CICS files for batch window
2. TRANBKP   — Backup current transaction master
3. POSTTRAN   — Post daily transactions
4. INTCALC    — Calculate interest/fees
5. COMBTRAN   — Combine backup + system transactions
6. TRANREPT   — Generate daily report
7. PRTCATBL   — Print category balances
8. OPENFIL    — Reopen CICS files
```

### End-of-Day Reconciliation

After the full cycle:

| Check | Formula |
|---|---|
| Total records conserved | `TRANSACT(final) = TRANSACT(before) + posted_from_dalytran + interest_transactions` |
| Balance sheet balanced | `SUM(ACCT-CURR-BAL) = SUM(ACCT-CURR-BAL before) + SUM(posted amounts) + SUM(interest amounts)` |
| Rejects accounted for | `DALYREJS count = DALYTRAN count - posted_count` |
| Category balances updated | For each `(acct, type, cat)`: `TCATBAL(final) = TCATBAL(before) + SUM(posted amounts for that key)` |
| Report total matches | `TRANREPT grand total = SUM(all filtered TRAN-AMT)` |
