# Data Dictionary — CardDemo COBOL Estate

## Overview

This document catalogs all data structures defined in the CardDemo copybook library (`app/cpy/` and sub-application `cpy/` directories). Fields are grouped by business entity with PIC clauses, data types, inferred business meaning, and validation rules.

> **Note on BMS-generated copybooks:** Screen field definitions generated from BMS
> maps (residing in `app/cpy-bms/`) are not documented here as they are auto-generated.
> Sub-application copybooks reside in their respective directories
> (e.g., `app/app-authorization-ims-db2-mq/cpy/`).

---

## 1. Account Entity

### Source: `CVACT01Y.cpy` — Account Record (RECLN 300)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | ACCOUNT-RECORD | — | Group | Root account record | Fixed 300-byte record |
| 05 | ACCT-ID | PIC 9(11) | Numeric | Unique account identifier (primary key) | 11-digit numeric, KSDS key at offset 0 |
| 05 | ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | Account status flag | 'Y' = Active, 'N' = Inactive |
| 05 | ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | Current account balance | Signed, 2 decimal places |
| 05 | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | Maximum credit limit | Must be positive |
| 05 | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | Cash advance credit limit | ≤ ACCT-CREDIT-LIMIT |
| 05 | ACCT-OPEN-DATE | PIC X(10) | Alpha-date | Account opening date | Format YYYY-MM-DD |
| 05 | ACCT-EXPIRAION-DATE | PIC X(10) | Alpha-date | Account expiration date | Format YYYY-MM-DD |
| 05 | ACCT-REISSUE-DATE | PIC X(10) | Alpha-date | Last card reissue date | Format YYYY-MM-DD |
| 05 | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | Current cycle credit total | Running total for billing cycle |
| 05 | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | Current cycle debit total | Running total for billing cycle |
| 05 | ACCT-ADDR-ZIP | PIC X(10) | Alpha | Account holder ZIP code | US ZIP or ZIP+4 format |
| 05 | ACCT-GROUP-ID | PIC X(10) | Alpha | Disclosure/rate group identifier | Links to DIS-GROUP-RECORD |
| 05 | FILLER | PIC X(178) | Alpha | Reserved space | Padding to 300 bytes |

---

## 2. Customer Entity

### Source: `CVCUS01Y.cpy` — Customer Record (RECLN 500)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | CUSTOMER-RECORD | — | Group | Root customer record | Fixed 500-byte record |
| 05 | CUST-ID | PIC 9(09) | Numeric | Unique customer identifier (primary key) | 9-digit numeric |
| 05 | CUST-FIRST-NAME | PIC X(25) | Alpha | Customer first name | Non-blank |
| 05 | CUST-MIDDLE-NAME | PIC X(25) | Alpha | Customer middle name | Optional |
| 05 | CUST-LAST-NAME | PIC X(25) | Alpha | Customer last name | Non-blank |
| 05 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | Street address line 1 | Non-blank |
| 05 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | Street address line 2 | Optional |
| 05 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | Street address line 3 | Optional |
| 05 | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | US state code | Validated against CSLKPCDY state list |
| 05 | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | ISO country code | 3-char country code |
| 05 | CUST-ADDR-ZIP | PIC X(10) | Alpha | ZIP/postal code | Validated against CSLKPCDY ZIP prefixes |
| 05 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | Primary phone number | Area code validated via CSLKPCDY |
| 05 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | Secondary phone number | Optional |
| 05 | CUST-SSN | PIC 9(09) | Numeric | Social Security Number | 9-digit numeric, sensitive PII |
| 05 | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | Government-issued ID number | Optional alternative ID |
| 05 | CUST-DOB-YYYY-MM-DD | PIC X(10) | Alpha-date | Date of birth | Format YYYY-MM-DD |
| 05 | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | EFT/ACH bank account ID | For electronic payments |
| 05 | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | Primary cardholder indicator | 'Y' = primary, 'N' = authorized user |
| 05 | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | FICO credit score | Range 300-850 |
| 05 | FILLER | PIC X(168) | Alpha | Reserved space | Padding to 500 bytes |

---

## 3. Card Entity

### Source: `CVACT02Y.cpy` — Card Record (RECLN 150)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | CARD-RECORD | — | Group | Root card record | Fixed 150-byte record |
| 05 | CARD-NUM | PIC X(16) | Alpha | Credit card number (primary key) | 16-digit card number (Luhn check) |
| 05 | CARD-ACCT-ID | PIC 9(11) | Numeric | Linked account ID | FK to ACCOUNT-RECORD.ACCT-ID |
| 05 | CARD-CVV-CD | PIC 9(03) | Numeric | Card verification value | 3-digit CVV |
| 05 | CARD-EMBOSSED-NAME | PIC X(50) | Alpha | Name printed on card | Uppercase |
| 05 | CARD-EXPIRAION-DATE | PIC X(10) | Alpha-date | Card expiration date | Format YYYY-MM-DD |
| 05 | CARD-ACTIVE-STATUS | PIC X(01) | Alpha | Card status | 'Y' = Active, 'N' = Inactive |
| 05 | FILLER | PIC X(59) | Alpha | Reserved space | Padding to 150 bytes |

### Source: `CVACT03Y.cpy` — Card Cross-Reference Record (RECLN 50)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | CARD-XREF-RECORD | — | Group | Cross-reference linking card→customer→account | Fixed 50-byte record |
| 05 | XREF-CARD-NUM | PIC X(16) | Alpha | Card number (primary key) | FK to CARD-RECORD |
| 05 | XREF-CUST-ID | PIC 9(09) | Numeric | Customer ID | FK to CUSTOMER-RECORD |
| 05 | XREF-ACCT-ID | PIC 9(11) | Numeric | Account ID | FK to ACCOUNT-RECORD |
| 05 | FILLER | PIC X(14) | Alpha | Reserved space | Padding to 50 bytes |

---

## 4. Transaction Entity

### Source: `CVTRA05Y.cpy` — Transaction Record (RECLN 350)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | TRAN-RECORD | — | Group | Root transaction record | Fixed 350-byte record |
| 05 | TRAN-ID | PIC X(16) | Alpha | Transaction unique identifier | System-generated |
| 05 | TRAN-TYPE-CD | PIC X(02) | Alpha | Transaction type code | FK to TRAN-TYPE-RECORD |
| 05 | TRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category code | FK to TRAN-CAT-RECORD |
| 05 | TRAN-SOURCE | PIC X(10) | Alpha | Transaction source channel | e.g., 'ONLINE', 'POS', 'ATM' |
| 05 | TRAN-DESC | PIC X(100) | Alpha | Transaction description | Free text |
| 05 | TRAN-AMT | PIC S9(09)V99 | Signed Decimal | Transaction amount | Positive=debit, Negative=credit |
| 05 | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | Merchant identifier | Acquiring merchant code |
| 05 | TRAN-MERCHANT-NAME | PIC X(50) | Alpha | Merchant business name | — |
| 05 | TRAN-MERCHANT-CITY | PIC X(50) | Alpha | Merchant city | — |
| 05 | TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | Merchant ZIP code | — |
| 05 | TRAN-CARD-NUM | PIC X(16) | Alpha | Card used for transaction | FK to CARD-RECORD |
| 05 | TRAN-ORIG-TS | PIC X(26) | Alpha-timestamp | Original transaction timestamp | Format: YYYY-MM-DD-HH.MM.SS.FFFFFF |
| 05 | TRAN-PROC-TS | PIC X(26) | Alpha-timestamp | Processing timestamp | Set when posted |
| 05 | FILLER | PIC X(20) | Alpha | Reserved space | Padding to 350 bytes |

### Source: `CVTRA06Y.cpy` — Daily Transaction Record (RECLN 350)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | DALYTRAN-RECORD | — | Group | Daily transaction input (same structure as TRAN-RECORD) | Sequential file |
| 05 | DALYTRAN-ID | PIC X(16) | Alpha | Transaction ID | System-generated |
| 05 | DALYTRAN-TYPE-CD | PIC X(02) | Alpha | Transaction type code | Validated against type file |
| 05 | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category code | Validated against category file |
| 05 | DALYTRAN-SOURCE | PIC X(10) | Alpha | Source channel | — |
| 05 | DALYTRAN-DESC | PIC X(100) | Alpha | Description | — |
| 05 | DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | Amount | — |
| 05 | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | Merchant ID | — |
| 05 | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | Merchant name | — |
| 05 | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | Merchant city | — |
| 05 | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | Merchant ZIP | — |
| 05 | DALYTRAN-CARD-NUM | PIC X(16) | Alpha | Card number | FK to CARD-XREF |
| 05 | DALYTRAN-ORIG-TS | PIC X(26) | Alpha-timestamp | Original timestamp | — |
| 05 | DALYTRAN-PROC-TS | PIC X(26) | Alpha-timestamp | Processing timestamp | — |
| 05 | FILLER | PIC X(20) | Alpha | Reserved | — |

### Source: `CVTRA01Y.cpy` — Transaction Category Balance (RECLN 50)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | TRAN-CAT-BAL-RECORD | — | Group | Running balance by account + type + category | Composite key |
| 05 | TRAN-CAT-KEY | — | Group | Composite key | — |
| 10 | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | Account ID | FK to ACCOUNT-RECORD |
| 10 | TRANCAT-TYPE-CD | PIC X(02) | Alpha | Transaction type | FK to TRAN-TYPE-RECORD |
| 10 | TRANCAT-CD | PIC 9(04) | Numeric | Transaction category | FK to TRAN-CAT-RECORD |
| 05 | TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | Cumulative category balance | Updated during posting |
| 05 | FILLER | PIC X(22) | Alpha | Reserved | Padding to 50 bytes |

### Source: `CVTRA02Y.cpy` — Disclosure Group (RECLN 50)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | DIS-GROUP-RECORD | — | Group | Interest rate by group + type + category | — |
| 05 | DIS-GROUP-KEY | — | Group | Composite key | — |
| 10 | DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | Rate group identifier | Links to ACCT-GROUP-ID |
| 10 | DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | Transaction type | — |
| 10 | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category | — |
| 05 | DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | Interest rate percentage | Annual rate |
| 05 | FILLER | PIC X(28) | Alpha | Reserved | Padding to 50 bytes |

### Source: `CVTRA03Y.cpy` — Transaction Type (RECLN 60)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | TRAN-TYPE-RECORD | — | Group | Transaction type reference data | — |
| 05 | TRAN-TYPE | PIC X(02) | Alpha | Type code (primary key) | 2-char code |
| 05 | TRAN-TYPE-DESC | PIC X(50) | Alpha | Type description | e.g., 'Purchase', 'Cash Advance' |
| 05 | FILLER | PIC X(08) | Alpha | Reserved | Padding to 60 bytes |

### Source: `CVTRA04Y.cpy` — Transaction Category (RECLN 60)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | TRAN-CAT-RECORD | — | Group | Transaction category reference data | — |
| 05 | TRAN-CAT-KEY | — | Group | Composite key | — |
| 10 | TRAN-TYPE-CD | PIC X(02) | Alpha | Parent type code | FK to TRAN-TYPE-RECORD |
| 10 | TRAN-CAT-CD | PIC 9(04) | Numeric | Category code | Unique within type |
| 05 | TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | Category description | e.g., 'Retail', 'Grocery' |
| 05 | FILLER | PIC X(04) | Alpha | Reserved | Padding to 60 bytes |

---

## 5. Security / User Entity

### Source: `CSUSR01Y.cpy` — User Security Record (RECLN 80)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | SEC-USER-DATA | — | Group | User authentication record | KSDS key = SEC-USR-ID |
| 05 | SEC-USR-ID | PIC X(08) | Alpha | User login ID (primary key) | 8-char uppercase |
| 05 | SEC-USR-FNAME | PIC X(20) | Alpha | User first name | — |
| 05 | SEC-USR-LNAME | PIC X(20) | Alpha | User last name | — |
| 05 | SEC-USR-PWD | PIC X(08) | Alpha | User password | 8-char, stored in clear |
| 05 | SEC-USR-TYPE | PIC X(01) | Alpha | User type | 'A' = Admin, 'U' = Regular |
| 05 | SEC-USR-FILLER | PIC X(23) | Alpha | Reserved | Padding to 80 bytes |

---

## 6. Application Communication

### Source: `COCOM01Y.cpy` — COMMAREA (Inter-program Communication)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | CARDDEMO-COMMAREA | — | Group | CICS communication area passed between programs | — |
| 05 | CDEMO-GENERAL-INFO | — | Group | Navigation and session state | — |
| 10 | CDEMO-FROM-TRANID | PIC X(04) | Alpha | Originating transaction ID | CICS TransID |
| 10 | CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | Calling program name | — |
| 10 | CDEMO-TO-TRANID | PIC X(04) | Alpha | Target transaction ID | — |
| 10 | CDEMO-TO-PROGRAM | PIC X(08) | Alpha | Target program name | — |
| 10 | CDEMO-USER-ID | PIC X(08) | Alpha | Current user ID | From sign-on |
| 10 | CDEMO-USER-TYPE | PIC X(01) | Alpha | User type | 88: 'A'=Admin, 'U'=User |
| 10 | CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | Program context | 88: 0=Enter, 1=Re-enter |
| 05 | CDEMO-CUSTOMER-INFO | — | Group | Customer context | — |
| 10 | CDEMO-CUST-ID | PIC 9(09) | Numeric | Selected customer | — |
| 10 | CDEMO-CUST-FNAME | PIC X(25) | Alpha | Customer first name | — |
| 10 | CDEMO-CUST-MNAME | PIC X(25) | Alpha | Customer middle name | — |
| 10 | CDEMO-CUST-LNAME | PIC X(25) | Alpha | Customer last name | — |
| 05 | CDEMO-ACCOUNT-INFO | — | Group | Account context | — |
| 10 | CDEMO-ACCT-ID | PIC 9(11) | Numeric | Selected account | — |
| 10 | CDEMO-ACCT-STATUS | PIC X(01) | Alpha | Account status | — |
| 05 | CDEMO-CARD-INFO | — | Group | Card context | — |
| 10 | CDEMO-CARD-NUM | PIC 9(16) | Numeric | Selected card number | — |
| 05 | CDEMO-MORE-INFO | — | Group | UI state | — |
| 10 | CDEMO-LAST-MAP | PIC X(7) | Alpha | Last BMS map sent | — |
| 10 | CDEMO-LAST-MAPSET | PIC X(7) | Alpha | Last BMS mapset | — |

---

## 7. Date/Time Structures

### Source: `CSDAT01Y.cpy` — Date/Time Working Storage

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| 01 | WS-DATE-TIME | — | Group | Current date/time container |
| 10 | WS-CURDATE-YEAR | PIC 9(04) | Numeric | Current year (4-digit) |
| 10 | WS-CURDATE-MONTH | PIC 9(02) | Numeric | Current month |
| 10 | WS-CURDATE-DAY | PIC 9(02) | Numeric | Current day |
| 10 | WS-CURDATE-N | PIC 9(08) | Numeric | Date as integer (YYYYMMDD) |
| 05 | WS-CURDATE-MM-DD-YY | — | Group | Formatted date (MM/DD/YY) |
| 05 | WS-CURTIME-HH-MM-SS | — | Group | Formatted time (HH:MM:SS) |
| 05 | WS-TIMESTAMP | — | Group | Full timestamp (YYYY-MM-DD HH:MM:SS.FFFFFF) |

### Source: `CODATECN.cpy` — Date Conversion Utility

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| 01 | CODATECN-REC | — | Group | Date format conversion record |
| 05 | CODATECN-IN-REC | — | Group | Input date specification |
| 10 | CODATECN-TYPE | PIC X | Alpha | Input format: '1'=YYYYMMDD, '2'=YYYY-MM-DD |
| 10 | CODATECN-INP-DATE | PIC X(20) | Alpha | Raw input date string |
| 05 | CODATECN-OUT-REC | — | Group | Output date specification |
| 10 | CODATECN-OUTTYPE | PIC X | Alpha | Output format: '1'=YYYY-MM-DD, '2'=YYYYMMDD |
| 10 | CODATECN-0UT-DATE | PIC X(20) | Alpha | Formatted output date |
| 05 | CODATECN-ERROR-MSG | PIC X(38) | Alpha | Conversion error message |

---

## 8. Export/Migration Structure

### Source: `CVEXPORT.cpy` — Multi-Record Export Layout (RECLN 500)

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| 01 | EXPORT-RECORD | — | Group | Branch migration export record |
| 05 | EXPORT-REC-TYPE | PIC X(1) | Alpha | Record type: 'C'=Customer, 'A'=Account, 'T'=Transaction, 'X'=Xref |
| 05 | EXPORT-TIMESTAMP | PIC X(26) | Alpha-timestamp | Export timestamp |
| 05 | EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | Sequence number |
| 05 | EXPORT-BRANCH-ID | PIC X(4) | Alpha | Source branch identifier |
| 05 | EXPORT-REGION-CODE | PIC X(5) | Alpha | Source region code |
| 05 | EXPORT-RECORD-DATA | PIC X(460) | Alpha | Record payload (REDEFINES per type) |

Redefines include: EXPORT-CUSTOMER-DATA, EXPORT-ACCOUNT-DATA, EXPORT-TRANSACTION-DATA (with COMP/COMP-3 optimization).

---

## 9. Reporting Structures

### Source: `CVTRA07Y.cpy` — Transaction Report Layout

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| 01 | REPORT-NAME-HEADER | — | Group | Report header block |
| 05 | REPT-SHORT-NAME | PIC X(38) | Alpha | Report ID ('DALYREPT') |
| 05 | REPT-LONG-NAME | PIC X(41) | Alpha | Full report title |
| 05 | REPT-START-DATE | PIC X(10) | Alpha-date | Report start date |
| 05 | REPT-END-DATE | PIC X(10) | Alpha-date | Report end date |
| 01 | TRANSACTION-DETAIL-REPORT | — | Group | Single report line |
| 05 | TRAN-REPORT-TRANS-ID | PIC X(16) | Alpha | Transaction ID |
| 05 | TRAN-REPORT-ACCOUNT-ID | PIC X(11) | Alpha | Account ID |
| 05 | TRAN-REPORT-TYPE-CD | PIC X(02) | Alpha | Type code |
| 05 | TRAN-REPORT-TYPE-DESC | PIC X(15) | Alpha | Type description |
| 05 | TRAN-REPORT-CAT-CD | PIC 9(04) | Numeric | Category code |
| 05 | TRAN-REPORT-CAT-DESC | PIC X(29) | Alpha | Category description |
| 05 | TRAN-REPORT-SOURCE | PIC X(10) | Alpha | Source channel |
| 05 | TRAN-REPORT-AMT | PIC -ZZZ,ZZZ,ZZZ.ZZ | Edited Numeric | Formatted amount |

### Source: `COSTM01.CPY` — Statement Layout (for CBSTM03A)

Transaction data re-keyed by CARD-NUM + TRAN-ID for statement generation with customer and account lookups.

---

## 10. Authorization Sub-Application Structures

### Source: `CIPAUDTY.cpy` — Pending Authorization Detail

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| — | PA-DT-* fields | Various | — | Authorization detail: card number, transaction amount, merchant, timestamps, approval status, fraud flags |

### Source: `CIPAUSMY.cpy` — Pending Authorization Summary

Summary view fields for the authorization message list screen.

### Source: `CCPAURQY.cpy` — Authorization Request (MQ Message)

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| 05 | PA-RQ-CARD-NUM | PIC X(16) | Alpha | Card number for authorization |
| 05 | PA-RQ-TRANSACTION-AMT | PIC +9(10).99 | Numeric | Requested transaction amount |
| 05 | PA-RQ-MERCHANT-ID | PIC X(15) | Alpha | Merchant identifier |
| 05 | PA-RQ-TRANSACTION-ID | PIC X(15) | Alpha | Unique transaction reference |

### Source: `CCPAURLY.cpy` — Authorization Response (MQ Message)

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| 05 | PA-RL-CARD-NUM | PIC X(16) | Alpha | Card number |
| 05 | PA-RL-TRANSACTION-ID | PIC X(15) | Alpha | Transaction reference |
| 05 | PA-RL-AUTH-ID-CODE | PIC X(06) | Alpha | Authorization ID code |
| 05 | PA-RL-AUTH-RESP-CODE | PIC X(02) | Alpha | Response code ('00'=Approved) |
| 05 | PA-RL-AUTH-RESP-REASON | PIC X(04) | Alpha | Reason code for declines |
| 05 | PA-RL-APPROVED-AMT | PIC +9(10).99 | Numeric | Approved amount |

### Source: `CCPAUERY.cpy` — Authorization Error Log

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|-----------------|
| 05 | ERR-DATE | PIC X(06) | Alpha | Error date |
| 05 | ERR-TIME | PIC X(06) | Alpha | Error time |
| 05 | ERR-APPLICATION | PIC X(08) | Alpha | Application name |
| 05 | ERR-PROGRAM | PIC X(08) | Alpha | Program name |
| 05 | ERR-LEVEL | PIC X(01) | Alpha | Severity: 'L'=Log, 'I'=Info, 'W'=Warning, 'C'=Critical |
| 05 | ERR-SUBSYSTEM | PIC X(01) | Alpha | Subsystem: 'A'=App, 'C'=CICS, 'I'=IMS, 'D'=DB2, 'M'=MQ, 'F'=File |
| 05 | ERR-MESSAGE | PIC X(50) | Alpha | Error message text |

---

## 11. Validation / Lookup Data

### Source: `CSLKPCDY.cpy` — Lookup Code Repository

Contains 88-level condition names for:
- **North America phone area codes** (validated against NANPA list)
- **US state codes** (2-character abbreviations)
- **US state + ZIP prefix** combinations (state + first 2 digits of ZIP)

Used by COACTUPC for field-level validation of customer address and phone data.

---

## 12. Menu Configuration

### Source: `COMEN02Y.cpy` — Main Menu Options

Defines 11 menu options mapping option numbers → program names → user types:
1. Account View (COACTVWC) — User
2. Account Update (COACTUPC) — User
3. Credit Card List (COCRDLIC) — User
4. Credit Card View (COCRDSLC) — User
5. Credit Card Update (COCRDUPC) — User
6. Transaction List (COTRN00C) — User
7. Transaction View (COTRN01C) — User
8. Transaction Add (COTRN02C) — User
9. Transaction Reports (CORPT00C) — User
10. Bill Payment (COBIL00C) — User
11. Pending Authorization View (COPAUS0C) — User

### Source: `COADM02Y.cpy` — Admin Menu Options

Defines 6 admin menu options:
1. User List / Security (COUSR00C)
2. User Add / Security (COUSR01C)
3. User Update / Security (COUSR02C)
4. User Delete / Security (COUSR03C)
5. Transaction Type List/Update - DB2 (COTRTLIC)
6. Transaction Type Maintenance - DB2 (COTRTUPC)

---

## 13. UI / Screen Support

### Source: `COTTL01Y.cpy` — Screen Titles

| Field | Value | Purpose |
|-------|-------|---------|
| CCDA-TITLE01 | 'AWS Mainframe Modernization' | Application title line 1 |
| CCDA-TITLE02 | 'CardDemo' | Application title line 2 |
| CCDA-THANK-YOU | 'Thank you for using CCDA application...' | Sign-off message |

### Source: `CSMSG01Y.cpy` — Common Messages

| Field | Value | Purpose |
|-------|-------|---------|
| CCDA-MSG-THANK-YOU | 'Thank you for using CardDemo application...' | Exit message |
| CCDA-MSG-INVALID-KEY | 'Invalid key pressed. Please see below...' | Key validation error |

### Source: `CSMSG02Y.cpy` — Abend / Error Messages

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | ABEND-DATA | — | Group | Work areas for abend routine | — |
| 05 | ABEND-CODE | PIC X(4) | Alpha | Abend error code | Spaces default |
| 05 | ABEND-CULPRIT | PIC X(8) | Alpha | Program that caused the abend | Spaces default |
| 05 | ABEND-REASON | PIC X(50) | Alpha | Abend reason description | Spaces default |
| 05 | ABEND-MSG | PIC X(72) | Alpha | Formatted abend message | Spaces default |

Used by: COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C.

### Source: `CSSTRPFY.cpy` — Store PFKey (Procedure Division Fragment)

Procedure-division copybook (not a data definition). Maps EIBAID values to CCARD-AID-* flags via EVALUATE block. Translates 3270 terminal key presses (ENTER, CLEAR, PA1, PA2, PF1–PF24) into the application's navigation model.

Used by: COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC.

### Source: `CSSETATY.cpy` — Set Screen Attributes (Procedure Division Fragment)

Procedure-division copybook template for setting BMS field color to red and inserting an asterisk (`*`) when a field fails validation. Uses parameterized variable names (TESTVAR1, SCRNVAR2, MAPNAME3) that are substituted at compile time.

Used by: COACTUPC.

### Source: `CSUTLDWY.cpy` — Date Utility Working Storage

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 10 | WS-EDIT-DATE-CCYYMMDD | — | Group | Editable date in CCYYMMDD format | — |
| 25 | WS-EDIT-DATE-CC | PIC X(2) | Alpha | Century (19 or 20) | 88: THIS-CENTURY=20, LAST-CENTURY=19 |
| 25 | WS-EDIT-DATE-YY | PIC X(2) | Alpha | Year within century | — |
| 20 | WS-EDIT-DATE-MM | PIC X(2) | Alpha | Month | 88: WS-VALID-MONTH VALUES 1–12 |
| 20 | WS-EDIT-DATE-DD | PIC X(2) | Alpha | Day | 88: WS-VALID-DAY VALUES 1–31 |
| 10 | WS-EDIT-DATE-CCYYMMDD-N | PIC 9(8) | Numeric | Numeric REDEFINES of date | — |
| 10 | WS-EDIT-DATE-BINARY | PIC S9(9) BINARY | Binary | Binary date for LE calls | — |
| 10 | WS-CURRENT-DATE | — | Group | Current system date | — |
| 10 | WS-EDIT-DATE-FLGS | — | Group | Validation flags | 88: IS-VALID=LOW-VALUES, IS-INVALID='000' |
| 10 | WS-DATE-FORMAT | PIC X(08) | Alpha | Date mask | Default 'YYYYMMDD' |
| 10 | WS-DATE-VALIDATION-RESULT | — | Group | LE CEEDAYS return area | Severity + message + test date |

Used by: COACTUPC (date field validation).

### Source: `CUSTREC.cpy` — Statement Customer Record (RECLN 500)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | CUSTOMER-RECORD | — | Group | Customer data for statement generation | Fixed 500-byte record |
| 05 | CUST-ID | PIC 9(09) | Numeric | Customer identifier | 9-digit numeric |
| 05 | CUST-FIRST-NAME | PIC X(25) | Alpha | Customer first name | — |
| 05 | CUST-MIDDLE-NAME | PIC X(25) | Alpha | Customer middle name | — |
| 05 | CUST-LAST-NAME | PIC X(25) | Alpha | Customer last name | — |
| 05 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | Street address line 1 | — |
| 05 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | Street address line 2 | — |
| 05 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | Street address line 3 | — |
| 05 | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | US state code | — |
| 05 | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | ISO country code | — |
| 05 | CUST-ADDR-ZIP | PIC X(10) | Alpha | ZIP/postal code | — |
| 05 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | Primary phone | — |
| 05 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | Secondary phone | — |
| 05 | CUST-SSN | PIC 9(09) | Numeric | Social Security Number | Sensitive PII |
| 05 | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | Government-issued ID | — |
| 05 | CUST-DOB-YYYYMMDD | PIC X(10) | Alpha-date | Date of birth | Format YYYYMMDD |
| 05 | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | EFT/ACH bank account ID | — |
| 05 | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | Primary cardholder indicator | — |
| 05 | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | FICO credit score | — |
| 05 | FILLER | PIC X(168) | Alpha | Reserved space | Padding to 500 bytes |

Used by: CBSTM03A (statement generation). Structurally identical to CVCUS01Y but with slightly different field naming (CUST-DOB-YYYYMMDD vs CUST-DOB-YYYY-MM-DD).

### Source: `CSUTLDPY.cpy` — Date Utility Procedure Division

Procedure-division copybook containing reusable date validation paragraphs (EDIT-DATE-CCYYMMDD, EDIT-YEAR-CCYY, EDIT-MONTH, EDIT-DAY, EDIT-DATE-OF-BIRTH). Companion to CSUTLDWY working-storage copybook. Despite being present in `app/cpy/`, no program currently references this copybook — likely dead code.

### Source: `UNUSED1Y.cpy` — Unused Data Structure

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|-----------------|------------------|
| 01 | UNUSED-DATA | — | Group | Explicitly unused record | Dead artifact |
| 05 | UNUSED-ID | PIC X(08) | Alpha | Unused ID field | — |
| 05 | UNUSED-FNAME | PIC X(20) | Alpha | Unused first name | — |
| 05 | UNUSED-LNAME | PIC X(20) | Alpha | Unused last name | — |
| 05 | UNUSED-PWD | PIC X(08) | Alpha | Unused password | — |
| 05 | UNUSED-TYPE | PIC X(01) | Alpha | Unused type flag | — |
| 05 | UNUSED-FILLER | PIC X(23) | Alpha | Reserved space | — |

Structurally identical to CSUSR01Y (User Security Record). No program references this copybook — candidate for deletion.

### Source: `CVCRD01Y.cpy` — Credit Card Work Areas

Navigation control fields (CCARD-AID-*, CCARD-NEXT-PROG, CCARD-NEXT-MAP, CCARD-ERROR-MSG) and current selection context (CC-ACCT-ID, CC-CARD-NUM, CC-CUST-ID).
