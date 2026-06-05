# Data Dictionary

This document catalogs every field definition from the CardDemo copybooks (`app/cpy/`), grouped by business entity.

---

## 1. Account Entity

### CVACT01Y — Account Record (RECLN 300)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| ACCT-ID | PIC 9(11) | Numeric | Unique account identifier |
| ACCT-ACTIVE-STATUS | PIC X(01) | Alphanumeric | Account status flag (active/inactive) |
| ACCT-CURR-BAL | PIC S9(10)V99 | Signed decimal | Current account balance |
| ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | Cash advance credit limit |
| ACCT-OPEN-DATE | PIC X(10) | Date string | Account opening date |
| ACCT-EXPIRAION-DATE | PIC X(10) | Date string | Account expiration date |
| ACCT-REISSUE-DATE | PIC X(10) | Date string | Card reissue date |
| ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed decimal | Current cycle credit total |
| ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed decimal | Current cycle debit total |
| ACCT-ADDR-ZIP | PIC X(10) | Alphanumeric | Account holder ZIP/postal code |
| ACCT-GROUP-ID | PIC X(10) | Alphanumeric | Disclosure/interest rate group identifier |
| FILLER | PIC X(178) | Padding | Reserved space to 300 bytes |

---

## 2. Card Entity

### CVACT02Y — Card Record (RECLN 150)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CARD-NUM | PIC X(16) | Alphanumeric | Credit card number (primary key) |
| CARD-ACCT-ID | PIC 9(11) | Numeric | Associated account ID |
| CARD-CVV-CD | PIC 9(03) | Numeric | Card verification value |
| CARD-EMBOSSED-NAME | PIC X(50) | Alphanumeric | Name embossed on physical card |
| CARD-EXPIRAION-DATE | PIC X(10) | Date string | Card expiration date |
| CARD-ACTIVE-STATUS | PIC X(01) | Alphanumeric | Card status (active/inactive) |
| FILLER | PIC X(59) | Padding | Reserved space to 150 bytes |

### CVCRD01Y — Credit Card Work Areas

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CCARD-AID | PIC X(5) | Alphanumeric | Attention identifier from terminal |
| CCARD-NEXT-PROG | PIC X(8) | Alphanumeric | Next program to transfer control |
| CCARD-NEXT-MAPSET | PIC X(7) | Alphanumeric | Next BMS mapset name |
| CCARD-NEXT-MAP | PIC X(7) | Alphanumeric | Next BMS map name |

**88-level conditions (CCARD-AID):**

| Condition | Value | Meaning |
|-----------|-------|---------|
| CCARD-AID-ENTER | 'ENTER' | Enter key pressed |
| CCARD-AID-CLEAR | 'CLEAR' | Clear key pressed |
| CCARD-AID-PA1 | 'PA1' | Program Attention 1 |
| CCARD-AID-PA2 | 'PA2' | Program Attention 2 |
| CCARD-AID-PFK01–PFK12 | 'PFK01'–'PFK12' | Function keys 1–12 |

---

## 3. Customer Entity

### CVCUS01Y — Customer Record (RECLN 500)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CUST-ID | PIC 9(09) | Numeric | Unique customer identifier |
| CUST-FIRST-NAME | PIC X(25) | Alphanumeric | Customer first name |
| CUST-MIDDLE-NAME | PIC X(25) | Alphanumeric | Customer middle name |
| CUST-LAST-NAME | PIC X(25) | Alphanumeric | Customer last name |
| CUST-ADDR-LINE-1 | PIC X(50) | Alphanumeric | Address line 1 |
| CUST-ADDR-LINE-2 | PIC X(50) | Alphanumeric | Address line 2 |
| CUST-ADDR-LINE-3 | PIC X(50) | Alphanumeric | Address line 3 |
| CUST-ADDR-STATE-CD | PIC X(02) | Alphanumeric | State code |
| CUST-ADDR-COUNTRY-CD | PIC X(03) | Alphanumeric | Country code |
| CUST-ADDR-ZIP | PIC X(10) | Alphanumeric | ZIP/postal code |
| CUST-PHONE-NUM-1 | PIC X(15) | Alphanumeric | Primary phone number |
| CUST-PHONE-NUM-2 | PIC X(15) | Alphanumeric | Secondary phone number |
| CUST-SSN | PIC 9(09) | Numeric | Social Security Number |
| CUST-GOVT-ISSUED-ID | PIC X(20) | Alphanumeric | Government-issued ID |
| CUST-DOB-YYYY-MM-DD | PIC X(10) | Date string | Date of birth |
| CUST-EFT-ACCOUNT-ID | PIC X(10) | Alphanumeric | Electronic funds transfer account |
| CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alphanumeric | Primary cardholder indicator |
| CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | FICO credit score |
| FILLER | PIC X(168) | Padding | Reserved space to 500 bytes |

### CUSTREC — Customer Record (duplicate layout)

Same structure as CVCUS01Y with minor variation: `CUST-DOB-YYYYMMDD` (no hyphens in field name).

---

## 4. Transaction Entity

### CVTRA01Y — Transaction Category Balance Record (RECLN 50)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| TRAN-CAT-KEY (group) | — | — | Composite key |
| TRANCAT-ACCT-ID | PIC 9(11) | Numeric | Account identifier |
| TRANCAT-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code |
| TRANCAT-CD | PIC 9(04) | Numeric | Transaction category code |
| TRAN-CAT-BAL | PIC S9(09)V99 | Signed decimal | Category balance amount |
| FILLER | PIC X(22) | Padding | Reserved |

### CVTRA02Y — Disclosure Group Record (RECLN 50)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| DIS-GROUP-KEY (group) | — | — | Composite key |
| DIS-ACCT-GROUP-ID | PIC X(10) | Alphanumeric | Account group identifier |
| DIS-TRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code |
| DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category code |
| DIS-INT-RATE | PIC S9(04)V99 | Signed decimal | Interest rate for this group/type/category |
| FILLER | PIC X(28) | Padding | Reserved |

### CVTRA03Y — Transaction Type Record (RECLN 60)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| TRAN-TYPE | PIC X(02) | Alphanumeric | Transaction type code (primary key) |
| TRAN-TYPE-DESC | PIC X(50) | Alphanumeric | Transaction type description |
| FILLER | PIC X(08) | Padding | Reserved |

### CVTRA04Y — Transaction Category Record (RECLN 60)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| TRAN-CAT-KEY (group) | — | — | Composite key |
| TRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code |
| TRAN-CAT-CD | PIC 9(04) | Numeric | Category code |
| TRAN-CAT-TYPE-DESC | PIC X(50) | Alphanumeric | Category description |
| FILLER | PIC X(04) | Padding | Reserved |

### CVTRA05Y — Transaction Record (RECLN 350)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| TRAN-ID | PIC X(16) | Alphanumeric | Unique transaction identifier |
| TRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code |
| TRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category code |
| TRAN-SOURCE | PIC X(10) | Alphanumeric | Transaction source (channel) |
| TRAN-DESC | PIC X(100) | Alphanumeric | Transaction description |
| TRAN-AMT | PIC S9(09)V99 | Signed decimal | Transaction amount |
| TRAN-MERCHANT-ID | PIC 9(09) | Numeric | Merchant identifier |
| TRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric | Merchant name |
| TRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric | Merchant city |
| TRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric | Merchant ZIP code |
| TRAN-CARD-NUM | PIC X(16) | Alphanumeric | Card number used |
| TRAN-ORIG-TS | PIC X(26) | Timestamp | Transaction origination timestamp |
| TRAN-PROC-TS | PIC X(26) | Timestamp | Transaction processing timestamp |
| FILLER | PIC X(20) | Padding | Reserved |

### CVTRA06Y — Daily Transaction Record (RECLN 350)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| DALYTRAN-ID | PIC X(16) | Alphanumeric | Daily transaction identifier |
| DALYTRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code |
| DALYTRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category code |
| DALYTRAN-SOURCE | PIC X(10) | Alphanumeric | Transaction source |
| DALYTRAN-DESC | PIC X(100) | Alphanumeric | Transaction description |
| DALYTRAN-AMT | PIC S9(09)V99 | Signed decimal | Transaction amount |
| DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | Merchant ID |
| DALYTRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric | Merchant name |
| DALYTRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric | Merchant city |
| DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric | Merchant ZIP |
| DALYTRAN-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| DALYTRAN-ORIG-TS | PIC X(26) | Timestamp | Origination timestamp |
| DALYTRAN-PROC-TS | PIC X(26) | Timestamp | Processing timestamp |
| FILLER | PIC X(20) | Padding | Reserved |

### CVTRA07Y — Transaction Report Structures

**REPORT-NAME-HEADER:**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| REPT-SHORT-NAME | PIC X(38) | Alphanumeric | Report short name ('DALYREPT') |
| REPT-LONG-NAME | PIC X(41) | Alphanumeric | Report title ('Daily Transaction Report') |
| REPT-DATE-HEADER | PIC X(12) | Alphanumeric | Date range label |
| REPT-START-DATE | PIC X(10) | Date string | Report start date |
| REPT-END-DATE | PIC X(10) | Date string | Report end date |

**TRANSACTION-DETAIL-REPORT:**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| TRAN-REPORT-TRANS-ID | PIC X(16) | Alphanumeric | Transaction ID |
| TRAN-REPORT-ACCOUNT-ID | PIC X(11) | Alphanumeric | Account ID |
| TRAN-REPORT-TYPE-CD | PIC X(02) | Alphanumeric | Type code |
| TRAN-REPORT-TYPE-DESC | PIC X(15) | Alphanumeric | Type description |
| TRAN-REPORT-CAT-CD | PIC 9(04) | Numeric | Category code |
| TRAN-REPORT-CAT-DESC | PIC X(29) | Alphanumeric | Category description |
| TRAN-REPORT-SOURCE | PIC X(10) | Alphanumeric | Transaction source |
| TRAN-REPORT-AMT | PIC -ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Formatted transaction amount |

---

## 5. Cross-Reference Entity

### CVACT03Y — Card Cross-Reference Record (RECLN 50)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| XREF-CARD-NUM | PIC X(16) | Alphanumeric | Card number (primary key) |
| XREF-CUST-ID | PIC 9(09) | Numeric | Customer ID (links card to customer) |
| XREF-ACCT-ID | PIC 9(11) | Numeric | Account ID (links card to account) |
| FILLER | PIC X(14) | Padding | Reserved |

---

## 6. Export Entity

### CVEXPORT — Multi-Record Export Layout (RECLN 500)

**Header fields (common to all record types):**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| EXPORT-REC-TYPE | PIC X(1) | Alphanumeric | Record type discriminator |
| EXPORT-TIMESTAMP | PIC X(26) | Timestamp | Export timestamp |
| EXPORT-DATE (REDEFINES) | PIC X(10) | Date string | Date portion of timestamp |
| EXPORT-TIME (REDEFINES) | PIC X(15) | Alphanumeric | Time portion of timestamp |
| EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | Sequence number within export |
| EXPORT-BRANCH-ID | PIC X(4) | Alphanumeric | Branch identifier |
| EXPORT-REGION-CODE | PIC X(5) | Alphanumeric | Region code |
| EXPORT-RECORD-DATA | PIC X(460) | Alphanumeric | Payload (REDEFINES by type) |

**EXPORT-CUSTOMER-DATA (REDEFINES EXPORT-RECORD-DATA):**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| EXP-CUST-ID | PIC 9(09) COMP | Binary | Customer ID |
| EXP-CUST-FIRST-NAME | PIC X(25) | Alphanumeric | First name |
| EXP-CUST-MIDDLE-NAME | PIC X(25) | Alphanumeric | Middle name |
| EXP-CUST-LAST-NAME | PIC X(25) | Alphanumeric | Last name |
| EXP-CUST-ADDR-LINE | PIC X(50) OCCURS 3 | Alphanumeric | Address lines (array) |
| EXP-CUST-ADDR-STATE-CD | PIC X(02) | Alphanumeric | State code |
| EXP-CUST-ADDR-COUNTRY-CD | PIC X(03) | Alphanumeric | Country code |
| EXP-CUST-ADDR-ZIP | PIC X(10) | Alphanumeric | ZIP code |
| EXP-CUST-PHONE-NUM | PIC X(15) OCCURS 2 | Alphanumeric | Phone numbers (array) |
| EXP-CUST-SSN | PIC 9(09) | Numeric | SSN |
| EXP-CUST-GOVT-ISSUED-ID | PIC X(20) | Alphanumeric | Government ID |
| EXP-CUST-DOB-YYYY-MM-DD | PIC X(10) | Date string | Date of birth |
| EXP-CUST-EFT-ACCOUNT-ID | PIC X(10) | Alphanumeric | EFT account |
| EXP-CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alphanumeric | Primary holder flag |
| EXP-CUST-FICO-CREDIT-SCORE | PIC 9(03) COMP-3 | Packed decimal | FICO score |

**EXPORT-ACCOUNT-DATA (REDEFINES EXPORT-RECORD-DATA):**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| EXP-ACCT-ID | PIC 9(11) | Numeric | Account ID |
| EXP-ACCT-ACTIVE-STATUS | PIC X(01) | Alphanumeric | Active status |
| EXP-ACCT-CURR-BAL | PIC S9(10)V99 COMP-3 | Packed decimal | Current balance |
| EXP-ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | Credit limit |
| EXP-ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 COMP-3 | Packed decimal | Cash credit limit |
| EXP-ACCT-OPEN-DATE | PIC X(10) | Date string | Open date |
| EXP-ACCT-EXPIRAION-DATE | PIC X(10) | Date string | Expiration date |
| EXP-ACCT-REISSUE-DATE | PIC X(10) | Date string | Reissue date |
| EXP-ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed decimal | Cycle credits |
| EXP-ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 COMP | Binary | Cycle debits |
| EXP-ACCT-ADDR-ZIP | PIC X(10) | Alphanumeric | ZIP code |
| EXP-ACCT-GROUP-ID | PIC X(10) | Alphanumeric | Group ID |

**EXPORT-TRANSACTION-DATA (REDEFINES EXPORT-RECORD-DATA):**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| EXP-TRAN-ID | PIC X(16) | Alphanumeric | Transaction ID |
| EXP-TRAN-TYPE-CD | PIC X(02) | Alphanumeric | Type code |
| EXP-TRAN-CAT-CD | PIC 9(04) | Numeric | Category code |
| EXP-TRAN-SOURCE | PIC X(10) | Alphanumeric | Source |
| EXP-TRAN-DESC | PIC X(100) | Alphanumeric | Description |
| EXP-TRAN-AMT | PIC S9(09)V99 COMP-3 | Packed decimal | Amount |
| EXP-TRAN-MERCHANT-ID | PIC 9(09) COMP | Binary | Merchant ID |
| EXP-TRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric | Merchant name |
| EXP-TRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric | Merchant city |
| EXP-TRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric | Merchant ZIP |
| EXP-TRAN-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| EXP-TRAN-ORIG-TS | PIC X(26) | Timestamp | Origination timestamp |
| EXP-TRAN-PROC-TS | PIC X(26) | Timestamp | Processing timestamp |

**EXPORT-CARD-XREF-DATA (REDEFINES EXPORT-RECORD-DATA):**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| EXP-XREF-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| EXP-XREF-CUST-ID | PIC 9(09) | Numeric | Customer ID |
| EXP-XREF-ACCT-ID | PIC 9(11) COMP | Binary | Account ID |

**EXPORT-CARD-DATA (REDEFINES EXPORT-RECORD-DATA):**

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| EXP-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| EXP-CARD-ACCT-ID | PIC 9(11) COMP | Binary | Account ID |
| EXP-CARD-CVV-CD | PIC 9(03) COMP | Binary | CVV code |
| EXP-CARD-EMBOSSED-NAME | PIC X(50) | Alphanumeric | Embossed name |
| EXP-CARD-EXPIRAION-DATE | PIC X(10) | Date string | Expiration date |
| EXP-CARD-ACTIVE-STATUS | PIC X(01) | Alphanumeric | Active status |

---

## 7. Application/UI Entities

### COCOM01Y — COMMAREA (Communication Area)

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CDEMO-FROM-TRANID | PIC X(04) | Alphanumeric | Originating transaction ID |
| CDEMO-FROM-PROGRAM | PIC X(08) | Alphanumeric | Originating program name |
| CDEMO-TO-TRANID | PIC X(04) | Alphanumeric | Target transaction ID |
| CDEMO-TO-PROGRAM | PIC X(08) | Alphanumeric | Target program name |
| CDEMO-USER-ID | PIC X(08) | Alphanumeric | Logged-in user ID |
| CDEMO-USER-TYPE | PIC X(01) | Alphanumeric | User type flag |
| CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | Program context (enter/re-enter) |
| CDEMO-CUST-ID | PIC 9(09) | Numeric | Current customer ID |
| CDEMO-CUST-FNAME | PIC X(25) | Alphanumeric | Customer first name |
| CDEMO-CUST-MNAME | PIC X(25) | Alphanumeric | Customer middle name |
| CDEMO-CUST-LNAME | PIC X(25) | Alphanumeric | Customer last name |
| CDEMO-ACCT-ID | PIC 9(11) | Numeric | Current account ID |
| CDEMO-ACCT-STATUS | PIC X(01) | Alphanumeric | Account status |
| CDEMO-CARD-NUM | PIC 9(16) | Numeric | Current card number |
| CDEMO-LAST-MAP | PIC X(7) | Alphanumeric | Last displayed map |
| CDEMO-LAST-MAPSET | PIC X(7) | Alphanumeric | Last displayed mapset |

**88-level conditions:**

| Condition | Value | Meaning |
|-----------|-------|---------|
| CDEMO-USRTYP-ADMIN | 'A' | Administrator user |
| CDEMO-USRTYP-USER | 'U' | Regular user |
| CDEMO-PGM-ENTER | 0 | First entry into program |
| CDEMO-PGM-REENTER | 1 | Re-entry (pseudo-conversational return) |

### COADM02Y — Admin Menu Options

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CDEMO-ADMIN-OPT-COUNT | PIC 9(02) | Numeric | Number of admin options (6) |
| CDEMO-ADMIN-OPT-NUM | PIC 9(02) | Numeric | Option number |
| CDEMO-ADMIN-OPT-NAME | PIC X(35) | Alphanumeric | Option display name |
| CDEMO-ADMIN-OPT-PGMNAME | PIC X(08) | Alphanumeric | Target program name |

**Menu entries:** User List (COUSR00C), User Add (COUSR01C), User Update (COUSR02C), User Delete (COUSR03C), Transaction Type List/Update Db2 (COTRTLIC), Transaction Type Maintenance Db2 (COTRTUPC).

### COMEN02Y — Main Menu Options

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CDEMO-MENU-OPT-COUNT | PIC 9(02) | Numeric | Number of menu options (11) |
| CDEMO-MENU-OPT-NUM | PIC 9(02) | Numeric | Option number |
| CDEMO-MENU-OPT-NAME | PIC X(35) | Alphanumeric | Option display name |
| CDEMO-MENU-OPT-PGMNAME | PIC X(08) | Alphanumeric | Target program name |
| CDEMO-MENU-OPT-USRTYPE | PIC X(01) | Alphanumeric | Required user type |

**Menu entries:** Account View (COACTVWC), Account Update (COACTUPC), Credit Card List (COCRDLIC), Credit Card View (COCRDSLC), Credit Card Update (COCRDUPC), Transaction List (COTRN00C), Transaction View (COTRN01C), Transaction Add (COTRN02C), Transaction Reports (CORPT00C), Bill Payment (COBIL00C), Pending Authorization View (COPAUS0C).

### COTTL01Y — Screen Title

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CCDA-TITLE01 | PIC X(40) | Alphanumeric | Title line 1 ('AWS Mainframe Modernization') |
| CCDA-TITLE02 | PIC X(40) | Alphanumeric | Title line 2 ('CardDemo') |
| CCDA-THANK-YOU | PIC X(40) | Alphanumeric | Sign-off message |

### CSMSG01Y — Common Messages

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CCDA-MSG-THANK-YOU | PIC X(50) | Alphanumeric | Application thank-you message |
| CCDA-MSG-INVALID-KEY | PIC X(50) | Alphanumeric | Invalid key press error message |

### CSMSG02Y — Abend Data

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| ABEND-CODE | PIC X(4) | Alphanumeric | Abend code |
| ABEND-CULPRIT | PIC X(8) | Alphanumeric | Program that caused the abend |
| ABEND-REASON | PIC X(50) | Alphanumeric | Reason for abend |
| ABEND-MSG | PIC X(72) | Alphanumeric | Abend message text |

### CSUSR01Y — User Security Record

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| SEC-USR-ID | PIC X(08) | Alphanumeric | User login ID (primary key) |
| SEC-USR-FNAME | PIC X(20) | Alphanumeric | User first name |
| SEC-USR-LNAME | PIC X(20) | Alphanumeric | User last name |
| SEC-USR-PWD | PIC X(08) | Alphanumeric | User password |
| SEC-USR-TYPE | PIC X(01) | Alphanumeric | User type (A=Admin, U=User) |
| SEC-USR-FILLER | PIC X(23) | Padding | Reserved |

---

## 8. Utility Entities

### CODATECN — Date Conversion Record

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| CODATECN-TYPE | PIC X | Alphanumeric | Input format type |
| CODATECN-INP-DATE | PIC X(20) | Alphanumeric | Input date value |
| CODATECN-1YYYY | PIC XXXX | Alphanumeric | Year (YYYYMMDD format) |
| CODATECN-1MM | PIC XX | Alphanumeric | Month |
| CODATECN-1DD | PIC XX | Alphanumeric | Day |
| CODATECN-OUTTYPE | PIC X | Alphanumeric | Output format type |
| CODATECN-0UT-DATE | PIC X(20) | Alphanumeric | Output date value |
| CODATECN-ERROR-MSG | PIC X(38) | Alphanumeric | Error message if conversion fails |

**88-level conditions:**

| Condition | Value | Meaning |
|-----------|-------|---------|
| YYYYMMDD-IN | "1" | Input is YYYYMMDD format |
| YYYY-MM-DD-IN | "2" | Input is YYYY-MM-DD format |
| YYYY-MM-DD-OP | "1" | Output as YYYY-MM-DD |
| YYYYMMDD-OP | "2" | Output as YYYYMMDD |

### CSDAT01Y — Date/Time Working Storage

| Field Name | PIC Clause | Data Type | Business Meaning |
|-----------|-----------|-----------|------------------|
| WS-CURDATE-YEAR | PIC 9(04) | Numeric | Current year |
| WS-CURDATE-MONTH | PIC 9(02) | Numeric | Current month |
| WS-CURDATE-DAY | PIC 9(02) | Numeric | Current day |
| WS-CURDATE-N | PIC 9(08) | Numeric (REDEFINES) | Date as single number |
| WS-CURTIME-HOURS | PIC 9(02) | Numeric | Hours |
| WS-CURTIME-MINUTE | PIC 9(02) | Numeric | Minutes |
| WS-CURTIME-SECOND | PIC 9(02) | Numeric | Seconds |
| WS-CURTIME-MILSEC | PIC 9(02) | Numeric | Milliseconds |
| WS-CURDATE-MM-DD-YY | — | Group | Formatted date MM/DD/YY |
| WS-CURTIME-HH-MM-SS | — | Group | Formatted time HH:MM:SS |
| WS-TIMESTAMP | — | Group | Full timestamp YYYY-MM-DD HH:MM:SS.NNNNNN |

### CSUTLDPY — Date Utility Parameters

Used by CSUTLDTC date validation utility for passing date parameters.

### CSUTLDWY — Date Utility Work Areas

Work areas used by COACTUPC for day-of-week calculations and date arithmetic.

### CSLKPCDY — Lookup Code Data

Defines lookup code structures used for field validation in account update screens.

### CSSETATY — Set Attribute Macro

Template copybook for setting BMS field attributes (color/highlight) based on validation flags. Uses pattern replacement with `(TESTVAR1)`, `(SCRNVAR2)`, `(MAPNAME3)` placeholders.

### CSSTRPFY — Store PFKey Paragraph

Contains `YYYY-STORE-PFKEY` paragraph that maps EIBAID (terminal attention identifier) to the CCARD-AID field in COMMAREA using EVALUATE TRUE logic.

### COSTM01.CPY — Statement Report Copybook

Record layouts for the account statement report generation (used by CBSTM03A).

### UNUSED1Y — Unused Copybook

Reserved/placeholder copybook with no active field definitions.
