# Data Dictionary — CardDemo Legacy COBOL System

> Complete field-level documentation of all copybooks, DB2 tables, and BMS screen definitions.

---

## Section A: Core Copybooks (`app/cpy/`)

### Account Entity

#### CVACT01Y.cpy — Account Master Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | ACCT-RECORD | — | Group | Account master record |
| 05 | ACCT-ID | 9(11) | Numeric | Account identifier |
| 05 | ACCT-ACTIVE-STATUS | X(01) | Alpha | Account status (Y=Active) |
| 05 | ACCT-CURR-BAL | S9(10)V99 | Decimal | Current balance |
| 05 | ACCT-CREDIT-LIMIT | S9(10)V99 | Decimal | Credit limit |
| 05 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Decimal | Cash advance limit |
| 05 | ACCT-OPEN-DATE | X(10) | Date | Account open date |
| 05 | ACCT-EXPIRAION-DATE | X(10) | Date | Account expiration date |
| 05 | ACCT-REISSUE-DATE | X(10) | Date | Card reissue date |
| 05 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Decimal | Current cycle credit |
| 05 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Decimal | Current cycle debit |
| 05 | ACCT-ADDR-ZIP | X(10) | Alpha | Billing ZIP code |
| 05 | ACCT-GROUP-ID | X(10) | Alpha | Disclosure group ID |
| 05 | FILLER | X(178) | — | Reserved |

#### CVACT02Y.cpy — Card Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | CARD-RECORD | — | Group | Card data record |
| 05 | CARD-NUM | X(16) | Alpha | Card number (PAN) |
| 05 | CARD-ACCT-ID | 9(11) | Numeric | Associated account |
| 05 | CARD-CVV-CD | 9(03) | Numeric | CVV code |
| 05 | CARD-EMBOSSED-NAME | X(50) | Alpha | Name on card |
| 05 | CARD-EXPIRAION-DATE | X(10) | Date | Card expiry |
| 05 | CARD-ACTIVE-STATUS | X(01) | Alpha | Status (Y=Active) |
| 05 | FILLER | X(59) | — | Reserved |

#### CVACT03Y.cpy — Card Cross-Reference Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | CARD-XREF-RECORD | — | Group | Card-to-account cross-reference |
| 05 | XREF-CARD-NUM | X(16) | Alpha | Card number (key) |
| 05 | XREF-CUST-ID | 9(09) | Numeric | Customer ID |
| 05 | XREF-ACCT-ID | 9(11) | Numeric | Account ID |
| 05 | FILLER | X(14) | — | Reserved |

### Customer Entity

#### CVCUS01Y.cpy — Customer Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | CUSTOMER-RECORD | — | Group | Customer master |
| 05 | CUST-ID | 9(09) | Numeric | Customer identifier |
| 05 | CUST-FIRST-NAME | X(25) | Alpha | First name |
| 05 | CUST-MIDDLE-NAME | X(25) | Alpha | Middle name |
| 05 | CUST-LAST-NAME | X(25) | Alpha | Last name |
| 05 | CUST-ADDR-LINE-1 | X(50) | Alpha | Address line 1 |
| 05 | CUST-ADDR-LINE-2 | X(50) | Alpha | Address line 2 |
| 05 | CUST-ADDR-LINE-3 | X(50) | Alpha | Address line 3 |
| 05 | CUST-ADDR-STATE-CD | X(02) | Alpha | State code |
| 05 | CUST-ADDR-COUNTRY-CD | X(03) | Alpha | Country code |
| 05 | CUST-ADDR-ZIP | X(10) | Alpha | ZIP/postal code |
| 05 | CUST-PHONE-NUM-1 | X(15) | Alpha | Primary phone |
| 05 | CUST-PHONE-NUM-2 | X(15) | Alpha | Secondary phone |
| 05 | CUST-SSN | 9(09) | Numeric | Social Security Number |
| 05 | CUST-GOVT-ISSUED-ID | X(20) | Alpha | Government ID |
| 05 | CUST-DOB-YYYYMMDD | X(10) | Date | Date of birth |
| 05 | CUST-EFT-ACCOUNT-ID | X(10) | Alpha | EFT account |
| 05 | CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | Primary card holder (Y/N) |
| 05 | CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | FICO score |
| 05 | FILLER | X(88) | — | Reserved |

#### CUSTREC.cpy — Customer Record Variant
Alternate customer layout used by CBSTM03A for statement generation with same field structure as CVCUS01Y.

### Transaction Entity

#### CVTRA05Y.cpy — Transaction Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | TRAN-RECORD | — | Group | Transaction record (350 bytes) |
| 05 | TRAN-ID | X(16) | Alpha | Transaction ID |
| 05 | TRAN-TYPE-CD | X(02) | Alpha | Transaction type code |
| 05 | TRAN-CAT-CD | 9(04) | Numeric | Category code |
| 05 | TRAN-SOURCE | X(10) | Alpha | Transaction source |
| 05 | TRAN-DESC | X(100) | Alpha | Description |
| 05 | TRAN-AMT | S9(09)V99 | Decimal | Amount |
| 05 | TRAN-CARD-NUM | X(16) | Alpha | Card number |
| 05 | TRAN-MERCHANT-ID | X(09) | Alpha | Merchant ID |
| 05 | TRAN-MERCHANT-NAME | X(50) | Alpha | Merchant name |
| 05 | TRAN-MERCHANT-CITY | X(50) | Alpha | Merchant city |
| 05 | TRAN-MERCHANT-ZIP | X(10) | Alpha | Merchant ZIP |
| 05 | TRAN-ORIG-TS | X(26) | Timestamp | Original timestamp |
| 05 | TRAN-PROC-TS | X(26) | Timestamp | Processing timestamp |
| 05 | FILLER | X(20) | — | Reserved |

#### CVTRA06Y.cpy — Daily Transaction Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | DALYTRAN-RECORD | — | Group | Daily transaction input |
| 05 | DALYTRAN-ID | X(16) | Alpha | Transaction ID |
| 05 | DALYTRAN-TYPE-CD | X(02) | Alpha | Type code |
| 05 | DALYTRAN-CAT-CD | 9(04) | Numeric | Category |
| 05 | DALYTRAN-SOURCE | X(10) | Alpha | Source |
| 05 | DALYTRAN-DESC | X(100) | Alpha | Description |
| 05 | DALYTRAN-AMT | S9(09)V99 | Decimal | Amount |
| 05 | DALYTRAN-CARD-NUM | X(16) | Alpha | Card number |
| 05 | DALYTRAN-MERCHANT-ID | X(09) | Alpha | Merchant ID |
| 05 | DALYTRAN-MERCHANT-NAME | X(50) | Alpha | Merchant name |
| 05 | DALYTRAN-MERCHANT-CITY | X(50) | Alpha | City |
| 05 | DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | ZIP |
| 05 | DALYTRAN-ORIG-TS | X(26) | Timestamp | Original timestamp |
| 05 | DALYTRAN-PROC-TS | X(26) | Timestamp | Processing timestamp |

#### CVTRA01Y.cpy — Transaction Category Balance
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | TRAN-CAT-BAL-RECORD | — | Group | Per-category running balance |
| 05 | TRANCAT-ACCT-ID | 9(11) | Numeric | Account ID |
| 05 | TRANCAT-TYPE-CD | X(02) | Alpha | Transaction type |
| 05 | TRANCAT-CD | 9(04) | Numeric | Category code |
| 05 | TRAN-CAT-BAL | S9(10)V99 | Decimal | Running balance for this category |

#### CVTRA02Y.cpy — Disclosure Group
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | DIS-GROUP-RECORD | — | Group | Disclosure group definition |
| 05 | DIS-GROUP-ID | X(10) | Alpha | Group identifier |
| 05 | DIS-GROUP-NAME | X(50) | Alpha | Group name |
| 05 | DIS-INT-RATE | S9(03)V99 | Decimal | Interest rate |
| 05 | DIS-MIN-PAY-RATE | S9(03)V99 | Decimal | Minimum payment rate |

#### CVTRA03Y.cpy — Transaction Type
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | TRAN-TYPE-RECORD | — | Group | Transaction type reference |
| 05 | TRAN-TYPE | X(02) | Alpha | Type code (key) |
| 05 | TRAN-TYPE-DESC | X(50) | Alpha | Description |

#### CVTRA04Y.cpy — Transaction Category
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | TRAN-CAT-RECORD | — | Group | Transaction category reference |
| 05 | TRAN-CAT-TYPE | X(02) | Alpha | Parent type code |
| 05 | TRAN-CAT-CODE | 9(04) | Numeric | Category code (key) |
| 05 | TRAN-CAT-DESC | X(50) | Alpha | Description |

#### CVTRA07Y.cpy — Transaction Report Layout
Report formatting copybook for batch transaction report output.

### Export/Import Entity

#### CVEXPORT.cpy — Multi-Entity Export Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | EXPORT-RECORD | — | Group | Combined export record |
| 05 | EXPORT-REC-TYPE | X(01) | Alpha | Record type (A=Account, C=Customer, R=Card, X=Xref) |
| 05 | EXPORT-DATA | X(499) | Alpha | Entity-specific data |
|  | (redefines) | | | ACCT, CUST, CARD, or XREF record based on type |

### Statement

#### COSTM01.CPY — Statement Format
Working storage for statement generation including header/detail/summary line formats, page control, and HTML templates.

### Card Work Area

#### CVCRD01Y.cpy — Card Work Area
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | CARD-WORK-AREA | — | Group | Screen editing fields for card operations |
| 05 | CARD-EDIT-NUM | X(16) | Alpha | Edited card number |
| 05 | CARD-EDIT-ACCT-ID | X(11) | Alpha | Edited account ID |
| 05 | CARD-EDIT-CVV | X(03) | Alpha | Edited CVV |
| 05 | CARD-EDIT-NAME | X(50) | Alpha | Edited embossed name |
| 05 | CARD-EDIT-EXP-DATE | X(10) | Alpha | Edited expiry date |
| 05 | CARD-EDIT-STATUS | X(01) | Alpha | Edited status |

### UI / Commarea

#### COCOM01Y.cpy — Communication Area (COMMAREA)
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | CARDDEMO-COMMAREA | — | Group | Inter-program communication area |
| 05 | CDEMO-FROM-TRANID | X(04) | Alpha | Originating transaction |
| 05 | CDEMO-FROM-PROGRAM | X(08) | Alpha | Calling program |
| 05 | CDEMO-TO-TRANID | X(04) | Alpha | Target transaction |
| 05 | CDEMO-TO-PROGRAM | X(08) | Alpha | Target program |
| 05 | CDEMO-USER-ID | X(08) | Alpha | Current user |
| 05 | CDEMO-USER-TYPE | X(01) | Alpha | User type (A=Admin, U=User) |
| 05 | CDEMO-PGM-CONTEXT | X(256) | Alpha | Program-specific context data |

#### COADM02Y.cpy — Admin Menu Options
Defines the admin menu option table with 6 entries (User List, User Add, User Update, User Delete, Transaction Type List/Update, Transaction Type Maintenance), each containing option number, name (35 chars), and program name (8 chars).

#### COMEN02Y.cpy — Regular User Menu Options
Defines the main menu option table with 7 entries (Account View, Account Update, Credit Card List, Transaction View, Transaction Add, Bill Payment, Report), each containing option number, name, and program name.

#### COTTL01Y.cpy — Screen Titles
Standard title fields (application name, page title, transaction ID, program name) used across all BMS screens.

### Date

#### CSDAT01Y.cpy — Date Working Storage
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | WS-DATE-FIELDS | — | Group | Date manipulation working storage |
| 05 | WS-CURDATE | X(08) | Date | Current date (YYYYMMDD) |
| 05 | WS-CURDATE-YEAR | 9(04) | Numeric | Current year |
| 05 | WS-CURDATE-MONTH | 9(02) | Numeric | Current month |
| 05 | WS-CURDATE-DAY | 9(02) | Numeric | Current day |
| 05 | WS-CURTIME | X(08) | Time | Current time |

#### CODATECN.cpy — Date Conversion Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | CODATECN-REC | — | Group | Interface record for COBDATFT ASM routine |
| 05 | COINTYPE | X(01) | Alpha | Input type (1=YYYYMMDD, 2=YYYY-MM-DD) |
| 05 | COOUTYPE | X(01) | Alpha | Output type |
| 05 | COINPDT | X(10) | Date | Input date |
| 05 | COOUTDT | X(10) | Date | Output date |
| 05 | CORC | X(02) | Alpha | Return code |

### Messages

#### CSMSG01Y.cpy — Message Area (Short)
Single message field for screen status messages (PIC X(80)).

#### CSMSG02Y.cpy — Message Area (Extended)
Extended message area supporting multi-line messages for complex validation feedback.

### Security

#### CSUSR01Y.cpy — User Security Record
| Level | Field Name | PIC | Type | Business Meaning |
|-------|-----------|-----|------|-----------------|
| 01 | SEC-USER-DATA | — | Group | Security/authentication record (USRSEC VSAM) |
| 05 | SEC-USR-ID | X(08) | Alpha | User ID (key) |
| 05 | SEC-USR-FNAME | X(20) | Alpha | First name |
| 05 | SEC-USR-LNAME | X(20) | Alpha | Last name |
| 05 | SEC-USR-PWD | X(08) | Alpha | Password |
| 05 | SEC-USR-TYPE | X(01) | Alpha | Type (A=Admin, U=Regular) |
| 05 | SEC-USR-FILLER | X(23) | — | Reserved |

### Utility

#### CSUTLDPY.cpy — Date Utility Parameters
Interface parameters for CSUTLDTC date conversion calls.

#### CSUTLDWY.cpy — Date Utility Work Area
Working storage for date utility calculations (Gregorian/Julian conversion tables).

#### CSSTRPFY.cpy — PF Key Store
Storage area for preserving PF key assignments across screens.

#### CSLKPCDY.cpy — Phone Area Code Lookup
Table of US phone area codes for validation in account update screens.

#### CSSETATY.cpy — Set Attributes
COPY REPLACING pattern for setting BMS field attributes (UNPROT, PROT, BRT, DARK, etc.).

### Unused

#### UNUSED1Y.cpy
Placeholder copybook (empty or minimal content) reserved for future use.

---

## Section B: Authorization Extension Copybooks (`app/app-authorization-ims-db2-mq/cpy/`)

| Copybook | Purpose | Key Fields |
|----------|---------|------------|
| CCPAUERY.cpy | Authorization error logging structure | Error code, error message, timestamp, program ID |
| CCPAURLY.cpy | Authorization Response structure | Card number, auth response code, approved amount, response reason |
| CCPAURQY.cpy | Authorization Request structure | Card number, transaction amount, merchant info, auth type |
| CIPAUSMY.cpy | Pending Authorization Summary (IMS segment) | Account ID (P key), total pending count, total pending amount |
| CIPAUDTY.cpy | Pending Authorization Details (IMS segment) | Timestamp (seq key), card num, amount, merchant, auth code |
| IMSFUNCS.cpy | IMS function codes | GU, GN, GNP, ISRT, DLET, REPL, CHKP constants |
| PADFLPCB.CPY | IMS PCB for PAD flat file (GSAM) | DB name, segment level, status code |
| PASFLPCB.CPY | IMS PCB for PAS flat file (GSAM) | DB name, segment level, status code |
| PAUTBPCB.CPY | IMS PCB for PAUT database | DB name, segment level, status code, key feedback area |

---

## Section C: Transaction Type DB2 Extension Copybooks (`app/app-transaction-type-db2/cpy/`)

| Copybook | Purpose | Key Fields |
|----------|---------|------------|
| CSDB2RPY.cpy | DB2 read parameters | SQL return code, row count, cursor state |
| CSDB2RWY.cpy | DB2 read/write parameters | SQL return code, row count, update/insert/delete indicators |

---

## Section D: DB2 Table Definitions

### CARDDEMO.AUTHFRDS (Fraud Tracking)
_Source: `app/app-authorization-ims-db2-mq/ddl/AUTHFRDS.ddl`_

| Column | Type | Nullable | Business Meaning |
|--------|------|----------|-----------------|
| CARD_NUM | CHAR(16) | NOT NULL | Card number (PK part 1) |
| AUTH_TS | TIMESTAMP | NOT NULL | Authorization timestamp (PK part 2) |
| AUTH_TYPE | CHAR(4) | Yes | Authorization type |
| CARD_EXPIRY_DATE | CHAR(4) | Yes | Card expiry |
| MESSAGE_TYPE | CHAR(6) | Yes | Message type code |
| MESSAGE_SOURCE | CHAR(6) | Yes | Source system |
| AUTH_ID_CODE | CHAR(6) | Yes | Authorization ID |
| AUTH_RESP_CODE | CHAR(2) | Yes | Response code |
| AUTH_RESP_REASON | CHAR(4) | Yes | Response reason |
| PROCESSING_CODE | CHAR(6) | Yes | Processing code |
| TRANSACTION_AMT | DECIMAL(12,2) | Yes | Transaction amount |
| APPROVED_AMT | DECIMAL(12,2) | Yes | Approved amount |
| MERCHANT_CATAGORY_CODE | CHAR(4) | Yes | MCC |
| ACQR_COUNTRY_CODE | CHAR(3) | Yes | Acquirer country |
| POS_ENTRY_MODE | SMALLINT | Yes | POS entry mode |
| MERCHANT_ID | CHAR(15) | Yes | Merchant identifier |
| MERCHANT_NAME | VARCHAR(22) | Yes | Merchant name |
| MERCHANT_CITY | CHAR(13) | Yes | City |
| MERCHANT_STATE | CHAR(02) | Yes | State |
| MERCHANT_ZIP | CHAR(09) | Yes | ZIP code |
| TRANSACTION_ID | CHAR(15) | Yes | Transaction ID |
| MATCH_STATUS | CHAR(1) | Yes | Match status |
| AUTH_FRAUD | CHAR(1) | Yes | Fraud flag |
| FRAUD_RPT_DATE | DATE | Yes | Fraud report date |
| ACCT_ID | DECIMAL(11) | Yes | Account ID |
| CUST_ID | DECIMAL(9) | Yes | Customer ID |

**Index:** `XAUTHFRD` — UNIQUE on (CARD_NUM ASC, AUTH_TS DESC)

### CARDDEMO.TRANSACTION_TYPE
_Source: `app/app-transaction-type-db2/ddl/TRNTYPE.ddl`_

| Column | Type | Nullable | Business Meaning |
|--------|------|----------|-----------------|
| TR_TYPE | CHAR(2) | NOT NULL | Transaction type code (PK) |
| TR_DESCRIPTION | VARCHAR(50) | NOT NULL | Type description |

**Index:** `XTRAN_TYPE` — UNIQUE on (TR_TYPE ASC)

### CARDDEMO.TRANSACTION_TYPE_CATEGORY
_Source: `app/app-transaction-type-db2/ddl/TRNTYCAT.ddl`_

| Column | Type | Nullable | Business Meaning |
|--------|------|----------|-----------------|
| TRC_TYPE_CODE | CHAR(2) | NOT NULL | Type code (PK part 1, FK → TRANSACTION_TYPE) |
| TRC_TYPE_CATEGORY | CHAR(4) | NOT NULL | Category code (PK part 2) |
| TRC_CAT_DATA | VARCHAR(50) | NOT NULL | Category description |

**Index:** `X_TRAN_TYPE_CATG` — UNIQUE on (TRC_TYPE_CODE ASC, TRC_TYPE_CATEGORY ASC)
**FK:** TRC_TYPE_CODE REFERENCES TRANSACTION_TYPE(TR_TYPE) ON DELETE RESTRICT

---

## Section E: BMS-Generated Copybooks

### Core (`app/cpy-bms/`) — 17 Copybooks

| Copybook | Screen | Key Fields |
|----------|--------|------------|
| COACTUP.CPY | Account Update | Account fields, card fields, customer fields, PF key indicators |
| COACTVW.CPY | Account View | Read-only account/card/customer display fields |
| COADM01.CPY | Admin Menu | Option list, selection field |
| COBIL00.CPY | Bill Payment | Balance display, payment amount, confirmation |
| COCRDLI.CPY | Card List | Card number array, selection flags, page indicators |
| COCRDSL.CPY | Card Detail | Full card detail display (read-only) |
| COCRDUP.CPY | Card Update | Editable card fields with attribute bytes |
| COMEN01.CPY | Main Menu | Option list, selection field |
| CORPT00.CPY | Report Selection | Report type, date range, output options |
| COSGN00.CPY | Sign-On | User ID, password, error message |
| COTRN00.CPY | Transaction List | Transaction array, page controls |
| COTRN01.CPY | Transaction Detail | Full transaction display |
| COTRN02.CPY | Transaction Add | Editable transaction fields |
| COUSR00.CPY | User List | User array, selection flags |
| COUSR01.CPY | User Add | New user fields |
| COUSR02.CPY | User Update | Editable user fields |
| COUSR03.CPY | User Delete | User display with delete confirmation |

### Authorization Extension (`app/app-authorization-ims-db2-mq/cpy-bms/`) — 2 Copybooks

| Copybook | Screen | Key Fields |
|----------|--------|------------|
| COPAU00.cpy | Authorization Summary | Pending auth list, account info, amounts |
| COPAU01.cpy | Authorization Detail | Full authorization details, fraud marking option |

### Transaction Type DB2 Extension (`app/app-transaction-type-db2/cpy-bms/`) — 2 Copybooks

| Copybook | Screen | Key Fields |
|----------|--------|------------|
| COTRTLI.cpy | Transaction Type List | Type code array, selection (U/D) flags, paging |
| COTRTUP.cpy | Transaction Type Update | Editable type code, description, category |

---

## Section F: DCL Declarations

### AUTHFRDS.dcl — COBOL Host Variables for AUTHFRDS
_Source: `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl`_

COBOL DCLGEN declarations mapping all 26 DB2 columns to COBOL host variables with matching PIC clauses and NULL indicators.

### DCLTRTYP.dcl — COBOL Host Variables for TRANSACTION_TYPE
_Source: `app/app-transaction-type-db2/dcl/DCLTRTYP.dcl`_

| Host Variable | PIC | Maps To |
|--------------|-----|---------|
| TR-TYPE | X(02) | TR_TYPE |
| TR-DESCRIPTION | X(50) | TR_DESCRIPTION |

### DCLTRCAT.dcl — COBOL Host Variables for TRANSACTION_TYPE_CATEGORY
_Source: `app/app-transaction-type-db2/dcl/DCLTRCAT.dcl`_

| Host Variable | PIC | Maps To |
|--------------|-----|---------|
| TRC-TYPE-CODE | X(02) | TRC_TYPE_CODE |
| TRC-TYPE-CATEGORY | X(04) | TRC_TYPE_CATEGORY |
| TRC-CAT-DATA | X(50) | TRC_CAT_DATA |
