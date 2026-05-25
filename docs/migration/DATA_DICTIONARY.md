# CardDemo COBOL Data Dictionary

> Auto-generated from the COBOL copybook estate in `app/cpy/`.
> Each section groups fields by business entity. For every field the table shows:
> **Field Name**, **PIC Clause**, **Java-equivalent Data Type**, **Inferred Business Meaning**, and **Validation Rules / Notes**.

---

## Table of Contents

1. [Account Entity](#1-account-entity)
2. [Customer Entity](#2-customer-entity)
3. [Card Entity](#3-card-entity)
4. [Card Cross-Reference Entity](#4-card-cross-reference-entity)
5. [Transaction Entity](#5-transaction-entity)
6. [Transaction Reference Data](#6-transaction-reference-data)
7. [Transaction Reporting](#7-transaction-reporting)
8. [Export / Branch Migration Record](#8-export--branch-migration-record)
9. [Security / User Entity](#9-security--user-entity)
10. [Communication Area (COMMAREA)](#10-communication-area-commarea)
11. [UI / Navigation Structures](#11-ui--navigation-structures)
12. [Date / Time Utilities](#12-date--time-utilities)
13. [Lookup / Validation Reference Data](#13-lookup--validation-reference-data)
14. [Abend / Error Handling](#14-abend--error-handling)
15. [Unused / Deprecated](#15-unused--deprecated)

---

## 1. Account Entity

### `CVACT01Y.cpy` — Account Record (RECLN 300)

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `ACCT-ID` | 05 | `9(11)` | `long` | Unique account identifier | Numeric, 11 digits |
| 2 | `ACCT-ACTIVE-STATUS` | 05 | `X(01)` | `String` | Account active/inactive flag | Single char status code |
| 3 | `ACCT-CURR-BAL` | 05 | `S9(10)V99` | `BigDecimal` | Current account balance | Signed, 2 decimal places |
| 4 | `ACCT-CREDIT-LIMIT` | 05 | `S9(10)V99` | `BigDecimal` | Credit limit for the account | Signed, 2 decimal places |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | 05 | `S9(10)V99` | `BigDecimal` | Cash advance credit limit | Signed, 2 decimal places |
| 6 | `ACCT-OPEN-DATE` | 05 | `X(10)` | `LocalDate` | Date the account was opened | Format YYYY-MM-DD (10 chars) |
| 7 | `ACCT-EXPIRAION-DATE` | 05 | `X(10)` | `LocalDate` | Account expiration date | Note: typo in original ("EXPIRAION") |
| 8 | `ACCT-REISSUE-DATE` | 05 | `X(10)` | `LocalDate` | Date the account/card was reissued | Format YYYY-MM-DD |
| 9 | `ACCT-CURR-CYC-CREDIT` | 05 | `S9(10)V99` | `BigDecimal` | Credits in the current billing cycle | Signed, 2 decimal places |
| 10 | `ACCT-CURR-CYC-DEBIT` | 05 | `S9(10)V99` | `BigDecimal` | Debits in the current billing cycle | Signed, 2 decimal places |
| 11 | `ACCT-ADDR-ZIP` | 05 | `X(10)` | `String` | Account holder ZIP/postal code | Up to 10 characters |
| 12 | `ACCT-GROUP-ID` | 05 | `X(10)` | `String` | Disclosure/interest rate group for the account | Links to `DIS-GROUP-RECORD` in CVTRA02Y |
| 13 | `FILLER` | 05 | `X(178)` | — | Padding to reach 300-byte record length | — |

---

## 2. Customer Entity

### `CVCUS01Y.cpy` — Customer Record (RECLN 500) — *primary*

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `CUST-ID` | 05 | `9(09)` | `int` / `long` | Unique customer identifier | Numeric, 9 digits |
| 2 | `CUST-FIRST-NAME` | 05 | `X(25)` | `String` | Customer first name | Max 25 chars |
| 3 | `CUST-MIDDLE-NAME` | 05 | `X(25)` | `String` | Customer middle name | Max 25 chars |
| 4 | `CUST-LAST-NAME` | 05 | `X(25)` | `String` | Customer last name | Max 25 chars |
| 5 | `CUST-ADDR-LINE-1` | 05 | `X(50)` | `String` | Address line 1 | Max 50 chars |
| 6 | `CUST-ADDR-LINE-2` | 05 | `X(50)` | `String` | Address line 2 | Max 50 chars |
| 7 | `CUST-ADDR-LINE-3` | 05 | `X(50)` | `String` | Address line 3 | Max 50 chars |
| 8 | `CUST-ADDR-STATE-CD` | 05 | `X(02)` | `String` | US state code | Validated against state-code list in CSLKPCDY |
| 9 | `CUST-ADDR-COUNTRY-CD` | 05 | `X(03)` | `String` | Country code | 3-char code |
| 10 | `CUST-ADDR-ZIP` | 05 | `X(10)` | `String` | ZIP / postal code | Validated against state+zip prefix list in CSLKPCDY |
| 11 | `CUST-PHONE-NUM-1` | 05 | `X(15)` | `String` | Primary phone number | Area code validated via CSLKPCDY 88-level values |
| 12 | `CUST-PHONE-NUM-2` | 05 | `X(15)` | `String` | Secondary phone number | Same validation as phone 1 |
| 13 | `CUST-SSN` | 05 | `9(09)` | `String` (sensitive) | Social Security Number | 9 numeric digits; PII — must be masked/encrypted in Java |
| 14 | `CUST-GOVT-ISSUED-ID` | 05 | `X(20)` | `String` | Government-issued ID (passport, driver license, etc.) | Max 20 chars; PII |
| 15 | `CUST-DOB-YYYY-MM-DD` | 05 | `X(10)` | `LocalDate` | Date of birth | Validated by CSUTLDPY date-of-birth paragraph (cannot be in future) |
| 16 | `CUST-EFT-ACCOUNT-ID` | 05 | `X(10)` | `String` | Linked EFT (electronic funds transfer) bank account | Max 10 chars |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | 05 | `X(01)` | `boolean` / `String` | Primary cardholder indicator | Single char flag (Y/N) |
| 18 | `CUST-FICO-CREDIT-SCORE` | 05 | `9(03)` | `int` | FICO credit score | 3-digit numeric, range typically 300–850 |
| 19 | `FILLER` | 05 | `X(168)` | — | Padding to 500 bytes | — |

### `CUSTREC.cpy` — Customer Record (alternate, RECLN 500)

Identical to `CVCUS01Y.cpy` except the date-of-birth field is named `CUST-DOB-YYYYMMDD` (no hyphens). Both define `01 CUSTOMER-RECORD`. This is likely a legacy duplicate; consolidate to one during Java migration.

---

## 3. Card Entity

### `CVACT02Y.cpy` — Card Record (RECLN 150)

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `CARD-NUM` | 05 | `X(16)` | `String` | Credit card number | 16 characters; PII — tokenize in Java |
| 2 | `CARD-ACCT-ID` | 05 | `9(11)` | `long` | Associated account ID | FK to ACCOUNT-RECORD.ACCT-ID |
| 3 | `CARD-CVV-CD` | 05 | `9(03)` | `String` (sensitive) | Card verification value | 3 numeric digits; PCI-DSS — never persist in Java |
| 4 | `CARD-EMBOSSED-NAME` | 05 | `X(50)` | `String` | Name embossed on card | Max 50 chars |
| 5 | `CARD-EXPIRAION-DATE` | 05 | `X(10)` | `LocalDate` / `YearMonth` | Card expiration date | Note: typo ("EXPIRAION"); format assumed YYYY-MM-DD |
| 6 | `CARD-ACTIVE-STATUS` | 05 | `X(01)` | `String` | Card active/inactive flag | Single char |
| 7 | `FILLER` | 05 | `X(59)` | — | Padding to 150 bytes | — |

---

## 4. Card Cross-Reference Entity

### `CVACT03Y.cpy` — Card ↔ Customer ↔ Account XREF (RECLN 50)

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `XREF-CARD-NUM` | 05 | `X(16)` | `String` | Card number (FK) | Links to CARD-RECORD.CARD-NUM |
| 2 | `XREF-CUST-ID` | 05 | `9(09)` | `long` | Customer ID (FK) | Links to CUSTOMER-RECORD.CUST-ID |
| 3 | `XREF-ACCT-ID` | 05 | `9(11)` | `long` | Account ID (FK) | Links to ACCOUNT-RECORD.ACCT-ID |
| 4 | `FILLER` | 05 | `X(14)` | — | Padding to 50 bytes | — |

---

## 5. Transaction Entity

### `CVTRA05Y.cpy` — Transaction Record (RECLN 350) — *primary*

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `TRAN-ID` | 05 | `X(16)` | `String` | Unique transaction identifier | 16-char string |
| 2 | `TRAN-TYPE-CD` | 05 | `X(02)` | `String` | Transaction type code | FK to TRAN-TYPE-RECORD (CVTRA03Y) |
| 3 | `TRAN-CAT-CD` | 05 | `9(04)` | `int` | Transaction category code | FK to TRAN-CAT-RECORD (CVTRA04Y) |
| 4 | `TRAN-SOURCE` | 05 | `X(10)` | `String` | Origination source of the transaction | E.g., POS, online, ATM |
| 5 | `TRAN-DESC` | 05 | `X(100)` | `String` | Transaction description | Free-text, max 100 chars |
| 6 | `TRAN-AMT` | 05 | `S9(09)V99` | `BigDecimal` | Transaction amount | Signed, 2 decimal places |
| 7 | `TRAN-MERCHANT-ID` | 05 | `9(09)` | `long` | Merchant identifier | 9-digit numeric |
| 8 | `TRAN-MERCHANT-NAME` | 05 | `X(50)` | `String` | Merchant name | Max 50 chars |
| 9 | `TRAN-MERCHANT-CITY` | 05 | `X(50)` | `String` | Merchant city | Max 50 chars |
| 10 | `TRAN-MERCHANT-ZIP` | 05 | `X(10)` | `String` | Merchant ZIP code | Max 10 chars |
| 11 | `TRAN-CARD-NUM` | 05 | `X(16)` | `String` | Card number used | FK to CARD-RECORD.CARD-NUM |
| 12 | `TRAN-ORIG-TS` | 05 | `X(26)` | `Instant` / `LocalDateTime` | Original transaction timestamp | 26-char ISO-style timestamp |
| 13 | `TRAN-PROC-TS` | 05 | `X(26)` | `Instant` / `LocalDateTime` | Processing timestamp | 26-char ISO-style timestamp |
| 14 | `FILLER` | 05 | `X(20)` | — | Padding to 350 bytes | — |

### `CVTRA06Y.cpy` — Daily Transaction Record (RECLN 350)

Identical layout to `CVTRA05Y.cpy` but all fields prefixed with `DALYTRAN-` instead of `TRAN-`. Used for daily batch processing. Record name: `DALYTRAN-RECORD`.

### `COSTM01.CPY` — Transaction Record for Reporting

Same data as CVTRA05Y but restructured with a composite key (`TRNX-CARD-NUM` + `TRNX-ID`) and `TRNX-` prefix. Record name: `TRNX-RECORD`.

---

## 6. Transaction Reference Data

### `CVTRA01Y.cpy` — Transaction Category Balance (RECLN 50)

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `TRANCAT-ACCT-ID` | 10 | `9(11)` | `long` | Account ID (part of composite key) | FK to ACCOUNT-RECORD |
| 2 | `TRANCAT-TYPE-CD` | 10 | `X(02)` | `String` | Transaction type code (part of key) | FK to TRAN-TYPE-RECORD |
| 3 | `TRANCAT-CD` | 10 | `9(04)` | `int` | Transaction category code (part of key) | FK to TRAN-CAT-RECORD |
| 4 | `TRAN-CAT-BAL` | 05 | `S9(09)V99` | `BigDecimal` | Aggregated balance for this category | Signed, 2 decimal places |

### `CVTRA02Y.cpy` — Disclosure Group (RECLN 50)

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `DIS-ACCT-GROUP-ID` | 10 | `X(10)` | `String` | Account group ID (part of composite key) | Links to ACCT-GROUP-ID |
| 2 | `DIS-TRAN-TYPE-CD` | 10 | `X(02)` | `String` | Transaction type code | FK to TRAN-TYPE-RECORD |
| 3 | `DIS-TRAN-CAT-CD` | 10 | `9(04)` | `int` | Transaction category code | FK to TRAN-CAT-RECORD |
| 4 | `DIS-INT-RATE` | 05 | `S9(04)V99` | `BigDecimal` | Interest rate for this disclosure group | Signed, 2 decimal places |

### `CVTRA03Y.cpy` — Transaction Type (RECLN 60)

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `TRAN-TYPE` | 05 | `X(02)` | `String` | Transaction type code (PK) | 2-char lookup key |
| 2 | `TRAN-TYPE-DESC` | 05 | `X(50)` | `String` | Description of the transaction type | Max 50 chars |

### `CVTRA04Y.cpy` — Transaction Category Type (RECLN 60)

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `TRAN-TYPE-CD` | 10 | `X(02)` | `String` | Transaction type code (composite PK part 1) | — |
| 2 | `TRAN-CAT-CD` | 10 | `9(04)` | `int` | Transaction category code (composite PK part 2) | — |
| 3 | `TRAN-CAT-TYPE-DESC` | 05 | `X(50)` | `String` | Description of the category within the type | Max 50 chars |

---

## 7. Transaction Reporting

### `CVTRA07Y.cpy` — Report Headers and Totals

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `REPT-SHORT-NAME` | 05 | `X(38)` | `String` | Short report identifier (`DALYREPT`) | Constant |
| 2 | `REPT-LONG-NAME` | 05 | `X(41)` | `String` | Full report title | `Daily Transaction Report` |
| 3 | `REPT-START-DATE` | 05 | `X(10)` | `LocalDate` | Report date range start | — |
| 4 | `REPT-END-DATE` | 05 | `X(10)` | `LocalDate` | Report date range end | — |
| 5 | `TRAN-REPORT-TRANS-ID` | 05 | `X(16)` | `String` | Transaction ID for detail line | — |
| 6 | `TRAN-REPORT-ACCOUNT-ID` | 05 | `X(11)` | `String` | Account ID for detail line | — |
| 7 | `TRAN-REPORT-TYPE-CD` | 05 | `X(02)` | `String` | Transaction type code | — |
| 8 | `TRAN-REPORT-TYPE-DESC` | 05 | `X(15)` | `String` | Type description (truncated for report) | — |
| 9 | `TRAN-REPORT-CAT-CD` | 05 | `9(04)` | `int` | Category code | — |
| 10 | `TRAN-REPORT-CAT-DESC` | 05 | `X(29)` | `String` | Category description (truncated) | — |
| 11 | `TRAN-REPORT-SOURCE` | 05 | `X(10)` | `String` | Transaction source | — |
| 12 | `TRAN-REPORT-AMT` | 05 | `-ZZZ,ZZZ,ZZZ.ZZ` | `String` (formatted) | Formatted transaction amount | Edit mask for display |
| 13 | `REPT-PAGE-TOTAL` | 05 | `+ZZZ,ZZZ,ZZZ.ZZ` | `String` (formatted) | Page subtotal | Edit mask |
| 14 | `REPT-ACCOUNT-TOTAL` | 05 | `+ZZZ,ZZZ,ZZZ.ZZ` | `String` (formatted) | Account subtotal | Edit mask |
| 15 | `REPT-GRAND-TOTAL` | 05 | `+ZZZ,ZZZ,ZZZ.ZZ` | `String` (formatted) | Grand total | Edit mask |

---

## 8. Export / Branch Migration Record

### `CVEXPORT.cpy` — Multi-Record Export (Total RECLN 500)

**Header fields (common to all record types):**

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `EXPORT-REC-TYPE` | 05 | `X(1)` | `String` / `enum` | Record type discriminator | Determines which REDEFINES applies |
| 2 | `EXPORT-TIMESTAMP` | 05 | `X(26)` | `Instant` | Export timestamp | REDEFINES into date + time |
| 3 | `EXPORT-SEQUENCE-NUM` | 05 | `9(9) COMP` | `int` | Sequence number within export batch | Binary (COMP) storage |
| 4 | `EXPORT-BRANCH-ID` | 05 | `X(4)` | `String` | Originating branch ID | 4 chars |
| 5 | `EXPORT-REGION-CODE` | 05 | `X(5)` | `String` | Geographic region code | 5 chars |

**REDEFINES overlay for each entity** — the `EXPORT-RECORD-DATA` field (460 bytes) is redefined as:

- `EXPORT-CUSTOMER-DATA` — mirrors CVCUS01Y with `COMP` for CUST-ID and `COMP-3` for FICO score
- `EXPORT-ACCOUNT-DATA` — mirrors CVACT01Y with `COMP-3` for balances and `COMP` for cycle debit
- `EXPORT-TRANSACTION-DATA` — mirrors CVTRA05Y with `COMP-3` for amount and `COMP` for merchant ID
- `EXPORT-CARD-XREF-DATA` — mirrors CVACT03Y with `COMP` for account ID
- `EXPORT-CARD-DATA` — mirrors CVACT02Y with `COMP` for account ID and CVV

> **Migration Note:** In Java, model this as an abstract `ExportRecord` base class with concrete subclasses per record type, or as a sealed interface with records.

---

## 9. Security / User Entity

### `CSUSR01Y.cpy` — Security User Record

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `SEC-USR-ID` | 05 | `X(08)` | `String` | User login ID | Max 8 chars |
| 2 | `SEC-USR-FNAME` | 05 | `X(20)` | `String` | User first name | Max 20 chars |
| 3 | `SEC-USR-LNAME` | 05 | `X(20)` | `String` | User last name | Max 20 chars |
| 4 | `SEC-USR-PWD` | 05 | `X(08)` | `String` (sensitive) | User password | Stored in clear text — must be hashed (e.g., bcrypt) in Java |
| 5 | `SEC-USR-TYPE` | 05 | `X(01)` | `String` / `enum` | User type (Admin/Regular) | See COMMAREA 88-levels: 'A' = Admin, 'U' = User |
| 6 | `SEC-USR-FILLER` | 05 | `X(23)` | — | Padding | — |

---

## 10. Communication Area (COMMAREA)

### `COCOM01Y.cpy` — Inter-Program Communication

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning | Validation / Notes |
|---|---|---|---|---|---|---|
| 1 | `CDEMO-FROM-TRANID` | 10 | `X(04)` | `String` | Source CICS transaction ID | 4-char CICS tran-id |
| 2 | `CDEMO-FROM-PROGRAM` | 10 | `X(08)` | `String` | Source program name | 8-char program name |
| 3 | `CDEMO-TO-TRANID` | 10 | `X(04)` | `String` | Target CICS transaction ID | — |
| 4 | `CDEMO-TO-PROGRAM` | 10 | `X(08)` | `String` | Target program name | — |
| 5 | `CDEMO-USER-ID` | 10 | `X(08)` | `String` | Logged-in user ID | — |
| 6 | `CDEMO-USER-TYPE` | 10 | `X(01)` | `String` | User type flag | 88: 'A' = Admin, 'U' = User |
| 7 | `CDEMO-PGM-CONTEXT` | 10 | `9(01)` | `int` | Program context (first entry vs re-entry) | 88: 0 = Enter, 1 = Re-enter |
| 8 | `CDEMO-CUST-ID` | 10 | `9(09)` | `long` | Customer ID in context | — |
| 9 | `CDEMO-CUST-FNAME` | 10 | `X(25)` | `String` | Customer first name in context | — |
| 10 | `CDEMO-CUST-MNAME` | 10 | `X(25)` | `String` | Customer middle name in context | — |
| 11 | `CDEMO-CUST-LNAME` | 10 | `X(25)` | `String` | Customer last name in context | — |
| 12 | `CDEMO-ACCT-ID` | 10 | `9(11)` | `long` | Account ID in context | — |
| 13 | `CDEMO-ACCT-STATUS` | 10 | `X(01)` | `String` | Account status in context | — |
| 14 | `CDEMO-CARD-NUM` | 10 | `9(16)` | `String` | Card number in context | — |
| 15 | `CDEMO-LAST-MAP` | 10 | `X(7)` | `String` | Last BMS map displayed | — |
| 16 | `CDEMO-LAST-MAPSET` | 10 | `X(7)` | `String` | Last BMS mapset used | — |

> **Migration Note:** Replace COMMAREA with HTTP session state, JWT claims, or a request-scoped DTO in Spring.

---

## 11. UI / Navigation Structures

### `COADM02Y.cpy` — Admin Menu Options

Defines `CARDDEMO-ADMIN-MENU-OPTIONS` with 6 admin menu items (User List, Add, Update, Delete, Transaction Type List/Update, Transaction Type Maintenance). Each option has:
- `CDEMO-ADMIN-OPT-NUM` — `PIC 9(02)` — menu option number
- `CDEMO-ADMIN-OPT-NAME` — `PIC X(35)` — display label
- `CDEMO-ADMIN-OPT-PGMNAME` — `PIC X(08)` — CICS program to invoke

### `COMEN02Y.cpy` — Main (User) Menu Options

Defines `CARDDEMO-MAIN-MENU-OPTIONS` with 11 options (Account View/Update, Credit Card List/View/Update, Transaction List/View/Add, Reports, Bill Payment, Pending Authorization View). Each option adds:
- `CDEMO-MENU-OPT-USRTYPE` — `PIC X(01)` — required user type

### `COTTL01Y.cpy` — Screen Titles

Constants: `CCDA-TITLE01`, `CCDA-TITLE02`, `CCDA-THANK-YOU` — all `PIC X(40)`.

### `CSMSG01Y.cpy` — Common Messages

`CCDA-MSG-THANK-YOU` and `CCDA-MSG-INVALID-KEY` — both `PIC X(50)`.

### `CVCRD01Y.cpy` — Card Work Areas / AID Key Mapping

Defines `CC-WORK-AREAS` with:
- `CCARD-AID` — `PIC X(5)` — CICS attention identifier (ENTER, CLEAR, PA1, PA2, PFK01–PFK12 via 88-levels)
- `CCARD-NEXT-PROG` — `PIC X(8)` — next program to XCTL to
- `CCARD-NEXT-MAPSET` / `CCARD-NEXT-MAP` — `PIC X(7)` — next BMS map
- `CCARD-ERROR-MSG` / `CCARD-RETURN-MSG` — `PIC X(75)` — error/return messages
- `CC-ACCT-ID` — `PIC X(11)` (redefined as `9(11)`) — working account ID
- `CC-CARD-NUM` — `PIC X(16)` (redefined as `9(16)`) — working card number
- `CC-CUST-ID` — `PIC X(09)` (redefined as `9(9)`) — working customer ID

### `CSSTRPFY.cpy` — Store PF Key Paragraph (Procedure Division)

Maps CICS EIBAID values to `CCARD-AID-*` 88-level flags. Not a data structure — procedural copybook.

### `CSSETATY.cpy` — Set Field Attribute (Procedure Division)

Template paragraph for setting BMS field color to red on validation error. Uses parameterized variable names (TESTVAR1, SCRNVAR2, MAPNAME3). Procedural copybook — no data fields.

---

## 12. Date / Time Utilities

### `CSDAT01Y.cpy` — Working Storage Date/Time Fields

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning |
|---|---|---|---|---|---|
| 1 | `WS-CURDATE-YEAR` | 15 | `9(04)` | `int` | Current year (4 digits) |
| 2 | `WS-CURDATE-MONTH` | 15 | `9(02)` | `int` | Current month |
| 3 | `WS-CURDATE-DAY` | 15 | `9(02)` | `int` | Current day |
| 4 | `WS-CURDATE-N` | 10 | `9(08)` | `int` | Numeric redefine of full date (YYYYMMDD) |
| 5 | `WS-CURTIME-HOURS` | 15 | `9(02)` | `int` | Current hours |
| 6 | `WS-CURTIME-MINUTE` | 15 | `9(02)` | `int` | Current minutes |
| 7 | `WS-CURTIME-SECOND` | 15 | `9(02)` | `int` | Current seconds |
| 8 | `WS-CURTIME-MILSEC` | 15 | `9(02)` | `int` | Current milliseconds (2 digits) |
| 9 | `WS-TIMESTAMP` | 10 | composite | `LocalDateTime` | Full timestamp `YYYY-MM-DD HH:MM:SS.NNNNNN` |

### `CSUTLDWY.cpy` — Date Edit Working Storage

Defines `WS-EDIT-DATE-CCYYMMDD` with:
- Century (`WS-EDIT-DATE-CC`) — 88: `THIS-CENTURY` = 20, `LAST-CENTURY` = 19
- Year, Month (88: `WS-VALID-MONTH` = 1–12, `WS-31-DAY-MONTH`, `WS-FEBRUARY`), Day (88: `WS-VALID-DAY` = 1–31, `WS-DAY-29/30/31`)
- Validation result structure: severity code, message code, result text, tested date, mask used

### `CSUTLDPY.cpy` — Date Edit Procedure Division

Paragraphs: `EDIT-DATE-CCYYMMDD`, `EDIT-YEAR-CCYY`, `EDIT-MONTH`, `EDIT-DAY`, `EDIT-DAY-MONTH-YEAR` (leap year logic), `EDIT-DATE-LE` (calls CSUTLDTC for Language Environment validation), `EDIT-DATE-OF-BIRTH` (cannot be in future).

### `CODATECN.cpy` — Date Conversion Record

Input/output date conversion structure supporting `YYYYMMDD` ↔ `YYYY-MM-DD` formats. Error message field: `CODATECN-ERROR-MSG PIC X(38)`.

---

## 13. Lookup / Validation Reference Data

### `CSLKPCDY.cpy` — Lookup Code Repository (1318 lines)

Defines `WS-US-PHONE-AREA-CODE-TO-EDIT PIC XXX` with three sets of 88-level condition names:
1. **`VALID-PHONE-AREA-CODE`** — full North American area code list (~490 codes from NANPA)
2. **`VALID-GENERAL-PURP-CODE`** — general-purpose assignable area codes
3. **`VALID-EASY-RECOG-AREA-CODE`** — easily recognizable/toll-free codes (200, 211, …, 999)

Additionally contains (beyond the first 1000 lines): US state codes and state + first-2-digits-of-ZIP validation sets.

> **Migration Note:** Replace 88-level value lists with a database lookup table or an enum/set in Java.

---

## 14. Abend / Error Handling

### `CSMSG02Y.cpy` — Abend Data

| # | Field Name | Level | PIC Clause | Java Type | Business Meaning |
|---|---|---|---|---|---|
| 1 | `ABEND-CODE` | 05 | `X(4)` | `String` | CICS/system abend code |
| 2 | `ABEND-CULPRIT` | 05 | `X(8)` | `String` | Program that caused the abend |
| 3 | `ABEND-REASON` | 05 | `X(50)` | `String` | Reason text |
| 4 | `ABEND-MSG` | 05 | `X(72)` | `String` | Full abend message |

---

## 15. Unused / Deprecated

### `UNUSED1Y.cpy`

Defines `UNUSED-DATA` — same structure as `CSUSR01Y.cpy` but with `UNUSED-` prefix. Likely a deprecated copy of the security user record. **Safe to exclude from Java migration.**

---

## PIC Clause to Java Type Quick Reference

| COBOL PIC | Java Type | Notes |
|---|---|---|
| `9(n)` | `int` / `long` | Use `long` when n > 9 |
| `S9(n)V99` | `BigDecimal` | Signed decimal with implied decimal point |
| `S9(n)V99 COMP-3` | `BigDecimal` | Packed decimal — same logical value |
| `9(n) COMP` | `int` / `long` | Binary integer storage |
| `X(n)` | `String` | Fixed-length; trim trailing spaces in Java |
| `PIC -ZZZ,ZZZ,ZZZ.ZZ` | `String` / `DecimalFormat` | Display-only edit mask |
| 88-level conditions | `enum` or `boolean` | Named constant value checks |

---

## Entity Relationship Summary

```
CUSTOMER ──< CARD-XREF >── ACCOUNT
                │
              CARD
                │
          TRANSACTION ──> TRAN-TYPE
                │              │
          TRAN-CAT-BAL    TRAN-CAT
                               │
                         DIS-GROUP (interest rates)
```
