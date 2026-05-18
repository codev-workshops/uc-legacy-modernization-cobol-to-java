# CardDemo JCL Job Frequency Classification

This document classifies all JCL jobs in the CardDemo application by their scheduling frequency, based on evidence from the Control-M scheduler definitions (`app/scheduler/CardDemo.controlm`), the CA7 scheduler output (`app/scheduler/CardDemo.ca7`), the `README.md`, and the JCL source files in `app/jcl/`.

---

## Summary of Evidence Sources

| Source | Description |
|:-------|:------------|
| **Control-M** (`CardDemo.controlm`) | XML definitions with explicit frequency in folder names (`DAILY-TransactionBackup`, `WEEKLY-TransactionTypesDBRefresh`, `WEEKLY-DisclosureGroupsRefresh`, `MONTHLY-InterestCalculation`) and `DAYS` attributes (`ALL` = daily, `SA` = Saturday/weekly). |
| **CA7** (`CardDemo.ca7`) | CA7 scheduler output showing trigger chains between jobs (e.g., CLOSEFIL triggers CBPAUP0J triggers POSTTRAN). |
| **README.md** | Batch job sequence (lines 209-234) and component inventory (lines 269-327). |
| **app/jcl/** | Complete set of 38 JCL member files. |

---

## 1. DAILY Jobs

**Source:** Control-M folder `DAILY-TransactionBackup` with `DAYS="ALL"` and CA7 trigger chain.

**Control-M Chain:** `CLOSEFIL` -> `TRANBKP` -> `WAITSTEP` -> `OPENFIL`

**CA7 Chain:** `CLOSEFIL` -> `CBPAUP0J` -> `POSTTRAN` -> `WAITSTEP` -> `OPENFIL`

| Job Name | Program | Description | Source | Pipeline | Dependencies |
|:---------|:--------|:------------|:-------|:---------|:-------------|
| CLOSEFIL | IEFBR14 | Close VSAM files for batch window | Control-M, CA7 | Daily Transaction Backup | None (chain initiator) |
| TRANBKP | IDCAMS | Backup transaction database | Control-M | Daily Transaction Backup | CLOSEFIL |
| CBPAUP0J | CBPAUP0C | Purge expired authorizations | CA7 | Daily Posting Chain | CLOSEFIL (CA7 trigger) |
| POSTTRAN | CBTRN02C | Core transaction posting | CA7 | Daily Posting Chain | CBPAUP0J (CA7 trigger) |
| WAITSTEP | COBSWAIT | Timed wait between steps | Control-M, CA7 | Daily Transaction Backup | TRANBKP (Control-M) / POSTTRAN (CA7) |
| OPENFIL | IEFBR14 | Reopen VSAM files for CICS | Control-M, CA7 | Daily Transaction Backup | WAITSTEP |

> **Note:** CBPAUP0J and POSTTRAN appear only in the CA7 trigger chain (CLOSEFIL -> CBPAUP0J -> POSTTRAN) and are absent from the Control-M `DAILY-TransactionBackup` folder. See [Cross-Reference Note 1](#cross-reference-note-1).

---

## 2. WEEKLY Jobs

**Source:** Control-M folders `WEEKLY-TransactionTypesDBRefresh` and `WEEKLY-DisclosureGroupsRefresh` with `DAYS="SA"` (Saturday). CA7 trigger chains for TRANTYPE, TRANCATG, and TCATBALF.

**Control-M Chains:**
- `MNTTRDB2` -> `TRANEXTR` (TransactionTypesDBRefresh SMART folder)
- `MNTTRDB2` -> `CLOSEFIL` -> `DISCGRP` -> `WAITSTEP` -> `OPENFIL` (DisclosureGroupsRefresh SMART folder)

**CA7 Chains:**
- `CLOSEFIL` -> `TRANTYPE` -> `WAITSTEP` -> `CLOSEFIL1` / `CLOSEFIL2`
- `CLOSEFIL1` -> `TRANCATG` -> `WAITSTEP` -> `CLOSEFIL` (loops back)
- `CLOSEFIL2` -> `TCATBALF` -> `WAITSTEP` -> `CLOSEFIL` (loops back)

| Job Name | Program | Description | Source | Pipeline | Dependencies |
|:---------|:--------|:------------|:-------|:---------|:-------------|
| MNTTRDB2 | COBTUPDT | Maintain transaction type DB2 table | Control-M | Weekly DB Refresh | None (chain initiator) |
| TRANEXTR | DSNTIAUL | Extract DB2 data for transaction types | Control-M | Weekly DB Refresh | MNTTRDB2 |
| DISCGRP | IDCAMS | Refresh disclosure group VSAM file | Control-M | Weekly Disclosure Refresh | CLOSEFIL (within DisclosureGroupsRefresh chain, depends on MNTTRDB2 completion) |
| TRANTYPE | IDCAMS | Refresh transaction type VSAM file | CA7 | Weekly Reference Data Refresh | CLOSEFIL (CA7 trigger) |
| TRANCATG | IDCAMS | Refresh transaction category VSAM file | CA7 | Weekly Reference Data Refresh | CLOSEFIL1 (CA7 trigger, logically after TRANTYPE) |
| TCATBALF | IDCAMS | Refresh transaction category balance file | CA7 | Weekly Reference Data Refresh | CLOSEFIL2 (CA7 trigger, logically after TRANTYPE) |

> **Note:** MNTTRDB2 does not have a JCL file in `app/jcl/`. It is referenced only in the Control-M scheduler and the README batch components table (program: COBTUPDT).

---

## 3. MONTHLY Jobs

**Source:** Control-M folder `MONTHLY-InterestCalculation`.

**Control-M Chain:** `CLOSEFIL` -> `INTCALC` -> `COMBTRAN` -> `WAITSTEP` -> `OPENFIL`

**CA7 Chains (extending the monthly cycle):**
- `CLOSEFIL` -> `CREASTMT` -> `TXT2PDF1` -> `WAITSTEP` -> `OPENFIL`
- `OPENFIL` -> `CLOSEFIL` -> `PRTCATBL` -> `WAITSTEP` -> `OPENFIL`

| Job Name | Program | Description | Source | Pipeline | Dependencies |
|:---------|:--------|:------------|:-------|:---------|:-------------|
| INTCALC | CBACT04C | Interest calculation | Control-M | Monthly Interest Calculation | CLOSEFIL |
| COMBTRAN | SORT | Combine system transactions with daily ones | Control-M | Monthly Interest Calculation | INTCALC |
| CREASTMT | CBSTM03A | Produce transaction statement | CA7 | Monthly Statement Generation | CLOSEFIL (CA7 trigger, follows monthly cycle) |
| TXT2PDF1 | IKJEFT1B (TXT2PDF REXX) | Convert statement text to PDF | CA7 | Monthly Statement Generation | CREASTMT (CA7 trigger) |
| PRTCATBL | (see JCL) | Print category balance report | CA7 | Monthly Reporting | CLOSEFIL (CA7 trigger, after CREASTMT chain completes) |

> **Note:** CREASTMT, TXT2PDF1, and PRTCATBL appear only in CA7 trigger chains and are absent from the Control-M `MONTHLY-InterestCalculation` folder. See [Cross-Reference Note 2](#cross-reference-note-2).

---

## 4. ON-DEMAND / USER-TRIGGERED Jobs

These jobs are not present in either scheduler (Control-M or CA7) and are submitted manually or triggered from CICS online transactions.

| Job Name | Program | Description | Source | Pipeline | Dependencies |
|:---------|:--------|:------------|:-------|:---------|:-------------|
| TRANREPT | CBTRN03C | Transaction detail report | Neither (submitted from CICS via CORPT00C's TDQ) | On-Demand Reporting | None (user-triggered from online CR00 transaction) |
| CBEXPORT | CBEXPORT | Customer data export for branch migration | Neither | Branch Migration | None (manual submission) |
| CBIMPORT | CBIMPORT | Customer data import from branch migration | Neither | Branch Migration | Requires CBEXPORT output file |
| CBADMCDJ | DFHCSDUP | Admin card job - CICS CSD resource definition | Neither | CICS Administration | None (manual submission) |
| REPTFILE | IDCAMS | Define GDG base for report files (TRANREPT GDG) | Neither | Reporting Setup | None (run before TRANREPT if GDG not defined) |
| DALYREJS | IDCAMS | Define GDG base for rejected transactions | Neither | Reporting Setup | None (run before POSTTRAN if GDG not defined) |
| READACCT | CBACT01C | Read and print account master VSAM file | CA7 only | Data Audit/Verification | See [Cross-Reference Note 3](#cross-reference-note-3) |
| READCARD | (see JCL) | Read and print card master VSAM file | CA7 only | Data Audit/Verification | READACCT (CA7 trigger) |
| READCUST | (see JCL) | Read and print customer master VSAM file | CA7 only | Data Audit/Verification | READCARD (CA7 trigger) |
| READXREF | (see JCL) | Read and print cross-reference VSAM file | CA7 only | Data Audit/Verification | READCUST (CA7 trigger) |
| FTPJCL | FTP | FTP file transfer utility | Neither | File Transfer | None (manual submission) |
| INTRDRJ1 | IDCAMS / IEBGENER | Internal reader job 1 - copies file and triggers INTRDRJ2 | Neither | Internal Reader Demo | None (manual submission) |
| INTRDRJ2 | IDCAMS | Internal reader job 2 - triggered by INTRDRJ1 via internal reader | Neither | Internal Reader Demo | INTRDRJ1 (triggered via internal reader) |

---

## 5. SETUP / ONE-TIME INITIALIZATION Jobs

These jobs are used for initial environment setup and data loading. They appear in the README installation sequence (lines 146-167) but not in either scheduler.

| Job Name | Program | Description | Source | Pipeline | Dependencies |
|:---------|:--------|:------------|:-------|:---------|:-------------|
| ACCTFILE | IDCAMS | Load/refresh account master VSAM | Neither | Initial Data Load | CLOSEFIL (files must be closed) |
| CARDFILE | IDCAMS | Load/refresh card master VSAM | Neither | Initial Data Load | CLOSEFIL |
| CUSTFILE | IDCAMS | Load/refresh customer master VSAM | Neither | Initial Data Load | CLOSEFIL |
| XREFFILE | IDCAMS | Load/refresh cross-reference VSAM | Neither | Initial Data Load | CLOSEFIL |
| TRANFILE | IDCAMS | Load transaction master file | Neither | Initial Data Load | CLOSEFIL |
| DUSRSECJ | IEBGENER | Initial load of user security file | Neither | Security Setup | None |
| DEFGDGB | IDCAMS | Setup GDG bases | Neither | Environment Setup | None |
| DEFGDGD | IDCAMS | Setup GDG bases for DB2 | Neither | Environment Setup | None |
| DEFCUST | IDCAMS | Define customer file | Neither | Environment Setup | None |
| ESDSRRDS | IDCAMS | Create ESDS and RRDS VSAM files | Neither | Environment Setup | None |
| TRANIDX | IDCAMS | Define alternate index on transaction file | Neither | Environment Setup | TRANFILE (transaction file must exist) |
| CREADB21 | DSNTEP4 | Create CardDemo DB2 database and load tables | Neither | DB2 Setup | None (optional module: Db2 Transaction Type Mgmt) |

---

## 6. UTILITY Jobs (Reused as Sub-Steps)

These jobs are not independently scheduled but appear as bookend operations across multiple chains (daily, weekly, monthly).

| Job Name | Program | Description | Reused In |
|:---------|:--------|:------------|:----------|
| CLOSEFIL | IEFBR14 | Close VSAM files in CICS | Daily, Weekly (DisclosureGroupsRefresh), Monthly, CA7 chains |
| OPENFIL | IEFBR14 | Open files in CICS | Daily, Weekly (DisclosureGroupsRefresh), Monthly, CA7 chains |
| WAITSTEP | COBSWAIT | Timed wait between steps | Daily, Weekly (DisclosureGroupsRefresh), Monthly, CA7 chains |

---

## Cross-Reference Notes

### Cross-Reference Note 1
**CA7 daily posting chain not in Control-M.**
The CA7 file defines a daily posting chain: `CLOSEFIL` -> `CBPAUP0J` -> `POSTTRAN` -> `WAITSTEP` -> `OPENFIL`. This chain is **not** present in the Control-M `DAILY-TransactionBackup` folder, which only contains `CLOSEFIL` -> `TRANBKP` -> `WAITSTEP` -> `OPENFIL`. This discrepancy suggests either:
- The CA7 definitions represent a more complete operational picture that was not fully migrated to Control-M.
- The two schedulers managed different subsets of the batch workload (CA7 for transaction posting, Control-M for backups).

### Cross-Reference Note 2
**CREASTMT and PRTCATBL in CA7 but not in Control-M monthly folder.**
The CA7 trigger chains show `CREASTMT` (statement generation) and `PRTCATBL` (category balance report) as part of chains triggered after the monthly cycle, but neither appears in Control-M's `MONTHLY-InterestCalculation` folder. The CA7 chains may represent a more complete picture of the monthly batch cycle:
- CA7 monthly chain: `CLOSEFIL` -> `CREASTMT` -> `TXT2PDF1` -> `WAITSTEP` -> `OPENFIL` -> `CLOSEFIL` -> `PRTCATBL` -> `WAITSTEP` -> `OPENFIL`
- Control-M monthly chain: `CLOSEFIL` -> `INTCALC` -> `COMBTRAN` -> `WAITSTEP` -> `OPENFIL`

### Cross-Reference Note 3
**READACCT, READCARD, READCUST, READXREF audit chain.**
These four jobs appear only in the CA7 file as a triggered chain: `READACCT` -> `READCARD` -> `READCUST` -> `READXREF` -> `WAITSTEP` -> `OPENFIL`. They are chained off the daily cycle (triggered from a CLOSEFIL after TCATBALF completes) but serve a data audit/verification purpose - reading and printing each master VSAM file. Their frequency is classified as **triggered/on-demand** since they verify data integrity rather than perform business processing. They do not appear in Control-M.

### Cross-Reference Note 4
**JCL files not in either scheduler.**
The following JCL files exist in `app/jcl/` but appear in neither the Control-M nor CA7 scheduler definitions:

| JCL File | Purpose | Classification |
|:---------|:--------|:---------------|
| FTPJCL.JCL | FTP file transfer - sends a mainframe file to a remote server | On-Demand / Utility |
| INTRDRJ1.JCL | Internal reader demo - copies a file and triggers INTRDRJ2 via internal reader | On-Demand / Utility |
| INTRDRJ2.JCL | Internal reader demo - triggered by INTRDRJ1, copies backup file | On-Demand / Utility |
| CBADMCDJ.jcl | CICS CSD resource definition using DFHCSDUP | On-Demand / CICS Admin |
| REPTFILE.jcl | Defines GDG base for transaction report output | Setup / On-Demand |
| DALYREJS.jcl | Defines GDG base for rejected transaction output | Setup / On-Demand |

---

## Complete Job Inventory (Alphabetical)

| # | Job Name | Program | Frequency | Source | Pipeline | Dependencies |
|:--|:---------|:--------|:----------|:-------|:---------|:-------------|
| 1 | ACCTFILE | IDCAMS | Setup / One-Time | Neither | Initial Data Load | CLOSEFIL |
| 2 | CARDFILE | IDCAMS | Setup / One-Time | Neither | Initial Data Load | CLOSEFIL |
| 3 | CBADMCDJ | DFHCSDUP | On-Demand | Neither | CICS Administration | None |
| 4 | CBEXPORT | CBEXPORT | On-Demand | Neither | Branch Migration | None |
| 5 | CBIMPORT | CBIMPORT | On-Demand | Neither | Branch Migration | CBEXPORT output |
| 6 | CBPAUP0J | CBPAUP0C | Daily | CA7 | Daily Posting Chain | CLOSEFIL |
| 7 | CLOSEFIL | IEFBR14 | Utility (Daily/Weekly/Monthly) | Control-M, CA7 | Multiple chains | Varies by chain |
| 8 | COMBTRAN | SORT | Monthly | Control-M | Monthly Interest Calculation | INTCALC |
| 9 | CREADB21 | DSNTEP4 | Setup / One-Time | Neither | DB2 Setup | None |
| 10 | CREASTMT | CBSTM03A | Monthly | CA7 | Monthly Statement Generation | CLOSEFIL (CA7) |
| 11 | CUSTFILE | IDCAMS | Setup / One-Time | Neither | Initial Data Load | CLOSEFIL |
| 12 | DALYREJS | IDCAMS | On-Demand / Setup | Neither | Reporting Setup | None |
| 13 | DEFCUST | IDCAMS | Setup / One-Time | Neither | Environment Setup | None |
| 14 | DEFGDGB | IDCAMS | Setup / One-Time | Neither | Environment Setup | None |
| 15 | DEFGDGD | IDCAMS | Setup / One-Time | Neither | Environment Setup | None |
| 16 | DISCGRP | IDCAMS | Weekly | Control-M | Weekly Disclosure Refresh | CLOSEFIL (after MNTTRDB2) |
| 17 | DUSRSECJ | IEBGENER | Setup / One-Time | Neither | Security Setup | None |
| 18 | ESDSRRDS | IDCAMS | Setup / One-Time | Neither | Environment Setup | None |
| 19 | FTPJCL | FTP | On-Demand | Neither | File Transfer | None |
| 20 | INTCALC | CBACT04C | Monthly | Control-M | Monthly Interest Calculation | CLOSEFIL |
| 21 | INTRDRJ1 | IDCAMS / IEBGENER | On-Demand | Neither | Internal Reader Demo | None |
| 22 | INTRDRJ2 | IDCAMS | On-Demand | Neither | Internal Reader Demo | INTRDRJ1 |
| 23 | MNTTRDB2 | COBTUPDT | Weekly | Control-M | Weekly DB Refresh | None |
| 24 | OPENFIL | IEFBR14 | Utility (Daily/Weekly/Monthly) | Control-M, CA7 | Multiple chains | WAITSTEP |
| 25 | POSTTRAN | CBTRN02C | Daily | CA7 | Daily Posting Chain | CBPAUP0J |
| 26 | PRTCATBL | (see JCL) | Monthly | CA7 | Monthly Reporting | CLOSEFIL (after CREASTMT chain) |
| 27 | READACCT | CBACT01C | Triggered / On-Demand | CA7 | Data Audit/Verification | CLOSEFIL (CA7 chain) |
| 28 | READCARD | (see JCL) | Triggered / On-Demand | CA7 | Data Audit/Verification | READACCT |
| 29 | READCUST | (see JCL) | Triggered / On-Demand | CA7 | Data Audit/Verification | READCARD |
| 30 | READXREF | (see JCL) | Triggered / On-Demand | CA7 | Data Audit/Verification | READCUST |
| 31 | REPTFILE | IDCAMS | On-Demand / Setup | Neither | Reporting Setup | None |
| 32 | TCATBALF | IDCAMS | Weekly | CA7 | Weekly Reference Data Refresh | CLOSEFIL2 (after TRANTYPE) |
| 33 | TRANBKP | IDCAMS | Daily | Control-M | Daily Transaction Backup | CLOSEFIL |
| 34 | TRANCATG | IDCAMS | Weekly | CA7 | Weekly Reference Data Refresh | CLOSEFIL1 (after TRANTYPE) |
| 35 | TRANFILE | IDCAMS | Setup / One-Time | Neither | Initial Data Load | CLOSEFIL |
| 36 | TRANIDX | IDCAMS | Setup / One-Time | Neither | Environment Setup | TRANFILE |
| 37 | TRANREPT | CBTRN03C | On-Demand | Neither | On-Demand Reporting | None (CICS-triggered) |
| 38 | TRANTYPE | IDCAMS | Weekly | CA7 | Weekly Reference Data Refresh | CLOSEFIL (CA7 trigger) |
| 39 | TXT2PDF1 | IKJEFT1B (TXT2PDF REXX) | Monthly | CA7 | Monthly Statement Generation | CREASTMT |
| 40 | WAITSTEP | COBSWAIT | Utility (Daily/Weekly/Monthly) | Control-M, CA7 | Multiple chains | Varies by chain |
| 41 | XREFFILE | IDCAMS | Setup / One-Time | Neither | Initial Data Load | CLOSEFIL |

---

## Dependency Chain Diagrams

### Daily Chains

```
Control-M (DAILY-TransactionBackup, DAYS="ALL"):
  CLOSEFIL -> TRANBKP -> WAITSTEP -> OPENFIL

CA7 (Daily Posting Chain):
  CLOSEFIL -> CBPAUP0J -> POSTTRAN -> WAITSTEP -> OPENFIL
```

### Weekly Chains

```
Control-M (WEEKLY-TransactionTypesDBRefresh, DAYS="SA"):
  MNTTRDB2 -> TRANEXTR

Control-M (WEEKLY-DisclosureGroupsRefresh, DAYS="SA", depends on MNTTRDB2):
  CLOSEFIL -> DISCGRP -> WAITSTEP -> OPENFIL

CA7 (Weekly Reference Data Refresh):
  CLOSEFIL -> TRANTYPE -> WAITSTEP -> CLOSEFIL1 -> TRANCATG -> WAITSTEP -> CLOSEFIL -> ...
                                   -> CLOSEFIL2 -> TCATBALF -> WAITSTEP -> CLOSEFIL -> ...
```

### Monthly Chains

```
Control-M (MONTHLY-InterestCalculation):
  CLOSEFIL -> INTCALC -> COMBTRAN -> WAITSTEP -> OPENFIL

CA7 (Monthly Statement & Reporting):
  CLOSEFIL -> CREASTMT -> TXT2PDF1 -> WAITSTEP -> OPENFIL
  CLOSEFIL -> PRTCATBL -> WAITSTEP -> OPENFIL
```

### Data Audit Chain (CA7 only)

```
CA7 (Triggered after weekly TCATBALF chain):
  CLOSEFIL -> READACCT -> READCARD -> READCUST -> READXREF -> WAITSTEP -> OPENFIL
```
