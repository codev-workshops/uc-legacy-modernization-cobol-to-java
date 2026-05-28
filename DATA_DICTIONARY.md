# DATA DICTIONARY — CardDemo Copybook Analysis

## 1. Account Entity

### CVACT01Y.cpy — Account Record (RECLN 300)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| ACCT-ID | PIC 9(11) | Numeric (11 digits) | Unique account identifier | Primary key; must be numeric |
| ACCT-ACTIVE-STATUS | PIC X(01) | Alphanumeric (1) | Account active/inactive status flag | 'Y'/'N' expected |
| ACCT-CURR-BAL | PIC S9(10)V99 | Signed decimal (12,2) | Current account balance | Signed; allows negative (overpayment) |
| ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal (12,2) | Maximum credit limit on account | Must be positive |
| ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal (12,2) | Cash advance credit limit | Must be ≤ ACCT-CREDIT-LIMIT |
| ACCT-OPEN-DATE | PIC X(10) | Alphanumeric (10) | Date account was opened | Format: YYYY-MM-DD |
| ACCT-EXPIRAION-DATE | PIC X(10) | Alphanumeric (10) | Account expiration date | Format: YYYY-MM-DD; must be > ACCT-OPEN-DATE |
| ACCT-REISSUE-DATE | PIC X(10) | Alphanumeric (10) | Date account was last reissued | Format: YYYY-MM-DD |
| ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed decimal (12,2) | Current billing cycle credit total | Running total for cycle |
| ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed decimal (12,2) | Current billing cycle debit total | Running total for cycle |
| ACCT-ADDR-ZIP | PIC X(10) | Alphanumeric (10) | Account holder ZIP/postal code | — |
| ACCT-GROUP-ID | PIC X(10) | Alphanumeric (10) | Disclosure group identifier for interest rates | FK to DIS-GROUP-RECORD |
| FILLER | PIC X(178) | Filler | Reserved/padding | — |

---

## 2. Customer Entity

### CVCUS01Y.cpy — Customer Record (RECLN 500)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| CUST-ID | PIC 9(09) | Numeric (9 digits) | Unique customer identifier | Primary key; must be numeric |
| CUST-FIRST-NAME | PIC X(25) | Alphanumeric (25) | Customer first name | Alpha-only validation in COACTUPC |
| CUST-MIDDLE-NAME | PIC X(25) | Alphanumeric (25) | Customer middle name | Alpha-only validation |
| CUST-LAST-NAME | PIC X(25) | Alphanumeric (25) | Customer last name | Alpha-only validation; mandatory |
| CUST-ADDR-LINE-1 | PIC X(50) | Alphanumeric (50) | Address line 1 | Mandatory |
| CUST-ADDR-LINE-2 | PIC X(50) | Alphanumeric (50) | Address line 2 | Optional |
| CUST-ADDR-LINE-3 | PIC X(50) | Alphanumeric (50) | Address line 3 | Optional |
| CUST-ADDR-STATE-CD | PIC X(02) | Alphanumeric (2) | US state code | Validated against CSLKPCDY state code list |
| CUST-ADDR-COUNTRY-CD | PIC X(03) | Alphanumeric (3) | Country code | — |
| CUST-ADDR-ZIP | PIC X(10) | Alphanumeric (10) | ZIP/postal code | Validated against state-ZIP prefix table in CSLKPCDY |
| CUST-PHONE-NUM-1 | PIC X(15) | Alphanumeric (15) | Primary phone number | Format: (NNN)NNN-NNNN; area code validated via CSLKPCDY |
| CUST-PHONE-NUM-2 | PIC X(15) | Alphanumeric (15) | Secondary phone number | Same format validation as PHONE-NUM-1 |
| CUST-SSN | PIC 9(09) | Numeric (9 digits) | Social Security Number | 9-digit numeric |
| CUST-GOVT-ISSUED-ID | PIC X(20) | Alphanumeric (20) | Government-issued ID (e.g. driver license) | — |
| CUST-DOB-YYYY-MM-DD | PIC X(10) | Alphanumeric (10) | Date of birth | Format: YYYY-MM-DD; validated via CSUTLDTC; cannot be in future |
| CUST-EFT-ACCOUNT-ID | PIC X(10) | Alphanumeric (10) | Electronic funds transfer account ID | — |
| CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alphanumeric (1) | Primary card holder indicator | 'Y'/'N' |
| CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric (3 digits) | FICO credit score | Range: 300–850 (implicit) |
| FILLER | PIC X(168) | Filler | Reserved/padding | — |

### CUSTREC.cpy — Customer Record (alternate layout)

Same structure as CVCUS01Y but with minor naming differences:
- `CUST-DOB-YYYYMMDD` instead of `CUST-DOB-YYYY-MM-DD`
- Used in legacy contexts where date format differs.

---

## 3. Card Entity

### CVACT02Y.cpy — Card Record (RECLN 150)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| CARD-NUM | PIC X(16) | Alphanumeric (16) | Credit card number | Primary key; 16 digits |
| CARD-ACCT-ID | PIC 9(11) | Numeric (11 digits) | Associated account ID | FK to ACCOUNT-RECORD.ACCT-ID |
| CARD-CVV-CD | PIC 9(03) | Numeric (3 digits) | Card verification value (CVV) | 3-digit numeric |
| CARD-EMBOSSED-NAME | PIC X(50) | Alphanumeric (50) | Name embossed on card | — |
| CARD-EXPIRAION-DATE | PIC X(10) | Alphanumeric (10) | Card expiration date | Format: YYYY-MM-DD |
| CARD-ACTIVE-STATUS | PIC X(01) | Alphanumeric (1) | Card active/inactive status | 'Y'/'N' |
| FILLER | PIC X(59) | Filler | Reserved/padding | — |

### CVACT03Y.cpy — Card Cross-Reference Record (RECLN 50)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| XREF-CARD-NUM | PIC X(16) | Alphanumeric (16) | Card number | Primary key; FK to CARD-RECORD |
| XREF-CUST-ID | PIC 9(09) | Numeric (9 digits) | Customer ID owning the card | FK to CUSTOMER-RECORD.CUST-ID |
| XREF-ACCT-ID | PIC 9(11) | Numeric (11 digits) | Account ID linked to card | FK to ACCOUNT-RECORD.ACCT-ID |
| FILLER | PIC X(14) | Filler | Reserved/padding | — |

### CVCRD01Y.cpy — CICS Card Work Areas

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| CCARD-AID | PIC X(5) | Alphanumeric (5) | Attention identifier (PF key pressed) | Values: ENTER, CLEAR, PA1, PA2, PFK01–PFK12 |
| CCARD-NEXT-PROG | PIC X(8) | Alphanumeric (8) | Next program to transfer to | Must be valid program name |
| CCARD-NEXT-MAPSET | PIC X(7) | Alphanumeric (7) | Next BMS mapset to display | — |
| CCARD-NEXT-MAP | PIC X(7) | Alphanumeric (7) | Next BMS map to display | — |
| CCARD-ERROR-MSG | PIC X(75) | Alphanumeric (75) | Error message for display | — |
| CCARD-RETURN-MSG | PIC X(75) | Alphanumeric (75) | Return/info message for display | LOW-VALUES = no message |
| CC-ACCT-ID | PIC X(11) / 9(11) | Alphanumeric/Numeric | Account ID in work area | REDEFINES for numeric access |
| CC-CARD-NUM | PIC X(16) / 9(16) | Alphanumeric/Numeric | Card number in work area | REDEFINES for numeric access |
| CC-CUST-ID | PIC X(09) / 9(9) | Alphanumeric/Numeric | Customer ID in work area | REDEFINES for numeric access |

---

## 4. Transaction Entity

### CVTRA05Y.cpy — Transaction Record (RECLN 350)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| TRAN-ID | PIC X(16) | Alphanumeric (16) | Unique transaction identifier | Primary key; auto-generated |
| TRAN-TYPE-CD | PIC X(02) | Alphanumeric (2) | Transaction type code (e.g. SA=Sale, CR=Credit) | FK to TRAN-TYPE-RECORD |
| TRAN-CAT-CD | PIC 9(04) | Numeric (4 digits) | Transaction category code | FK to TRAN-CAT-RECORD |
| TRAN-SOURCE | PIC X(10) | Alphanumeric (10) | Transaction source/channel | — |
| TRAN-DESC | PIC X(100) | Alphanumeric (100) | Transaction description | — |
| TRAN-AMT | PIC S9(09)V99 | Signed decimal (11,2) | Transaction amount | Signed; negative for credits |
| TRAN-MERCHANT-ID | PIC 9(09) | Numeric (9 digits) | Merchant identifier | — |
| TRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric (50) | Merchant name | — |
| TRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric (50) | Merchant city | — |
| TRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric (10) | Merchant ZIP code | — |
| TRAN-CARD-NUM | PIC X(16) | Alphanumeric (16) | Card number used | FK to CARD-RECORD |
| TRAN-ORIG-TS | PIC X(26) | Alphanumeric (26) | Original transaction timestamp | Format: YYYY-MM-DD HH:MM:SS.ffffff |
| TRAN-PROC-TS | PIC X(26) | Alphanumeric (26) | Processing timestamp | Same format as TRAN-ORIG-TS |
| FILLER | PIC X(20) | Filler | Reserved/padding | — |

### CVTRA06Y.cpy — Daily Transaction Record (RECLN 350)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| DALYTRAN-ID | PIC X(16) | Alphanumeric (16) | Daily transaction identifier | Same structure as TRAN-ID |
| DALYTRAN-TYPE-CD | PIC X(02) | Alphanumeric (2) | Transaction type code | Same as TRAN-TYPE-CD |
| DALYTRAN-CAT-CD | PIC 9(04) | Numeric (4) | Transaction category code | Same as TRAN-CAT-CD |
| DALYTRAN-SOURCE | PIC X(10) | Alphanumeric (10) | Transaction source | — |
| DALYTRAN-DESC | PIC X(100) | Alphanumeric (100) | Transaction description | — |
| DALYTRAN-AMT | PIC S9(09)V99 | Signed decimal (11,2) | Transaction amount | Signed |
| DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric (9) | Merchant ID | — |
| DALYTRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric (50) | Merchant name | — |
| DALYTRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric (50) | Merchant city | — |
| DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric (10) | Merchant ZIP | — |
| DALYTRAN-CARD-NUM | PIC X(16) | Alphanumeric (16) | Card number used | — |
| DALYTRAN-ORIG-TS | PIC X(26) | Alphanumeric (26) | Original timestamp | — |
| DALYTRAN-PROC-TS | PIC X(26) | Alphanumeric (26) | Processing timestamp | — |
| FILLER | PIC X(20) | Filler | Reserved | — |

### CVTRA01Y.cpy — Transaction Category Balance (RECLN 50)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| TRAN-CAT-KEY (composite) | — | — | Composite key | — |
| → TRANCAT-ACCT-ID | PIC 9(11) | Numeric (11) | Account ID | FK to ACCOUNT-RECORD |
| → TRANCAT-TYPE-CD | PIC X(02) | Alphanumeric (2) | Transaction type code | FK to TRAN-TYPE-RECORD |
| → TRANCAT-CD | PIC 9(04) | Numeric (4) | Transaction category code | FK to TRAN-CAT-RECORD |
| TRAN-CAT-BAL | PIC S9(09)V99 | Signed decimal (11,2) | Running balance for this account/type/category | Updated by CBTRN02C and CBACT04C |
| FILLER | PIC X(22) | Filler | Reserved | — |

### CVTRA02Y.cpy — Disclosure Group (RECLN 50)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| DIS-GROUP-KEY (composite) | — | — | Composite key | — |
| → DIS-ACCT-GROUP-ID | PIC X(10) | Alphanumeric (10) | Account group ID | FK from ACCT-GROUP-ID |
| → DIS-TRAN-TYPE-CD | PIC X(02) | Alphanumeric (2) | Transaction type code | — |
| → DIS-TRAN-CAT-CD | PIC 9(04) | Numeric (4) | Transaction category code | — |
| DIS-INT-RATE | PIC S9(04)V99 | Signed decimal (6,2) | Interest rate (annual %) | Used by CBACT04C for interest calculation |
| FILLER | PIC X(28) | Filler | Reserved | — |

### CVTRA03Y.cpy — Transaction Type (RECLN 60)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| TRAN-TYPE | PIC X(02) | Alphanumeric (2) | Transaction type code | Primary key (e.g. SA=Sale, CR=Credit) |
| TRAN-TYPE-DESC | PIC X(50) | Alphanumeric (50) | Description of transaction type | — |
| FILLER | PIC X(08) | Filler | Reserved | — |

### CVTRA04Y.cpy — Transaction Category (RECLN 60)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| TRAN-CAT-KEY (composite) | — | — | Composite key | — |
| → TRAN-TYPE-CD | PIC X(02) | Alphanumeric (2) | Transaction type code | FK to TRAN-TYPE-RECORD |
| → TRAN-CAT-CD | PIC 9(04) | Numeric (4) | Category code within type | — |
| TRAN-CAT-TYPE-DESC | PIC X(50) | Alphanumeric (50) | Category description | — |
| FILLER | PIC X(04) | Filler | Reserved | — |

### CVTRA07Y.cpy — Transaction Report Layout

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| REPT-SHORT-NAME | PIC X(38) | Alphanumeric | Report short name ('DALYREPT') | — |
| REPT-LONG-NAME | PIC X(41) | Alphanumeric | Report full name ('Daily Transaction Report') | — |
| REPT-START-DATE | PIC X(10) | Alphanumeric | Report date range start | — |
| REPT-END-DATE | PIC X(10) | Alphanumeric | Report date range end | — |
| TRAN-REPORT-TRANS-ID | PIC X(16) | Alphanumeric | Transaction ID in report line | — |
| TRAN-REPORT-ACCOUNT-ID | PIC X(11) | Alphanumeric | Account ID in report line | — |
| TRAN-REPORT-TYPE-CD | PIC X(02) | Alphanumeric | Type code in report | — |
| TRAN-REPORT-TYPE-DESC | PIC X(15) | Alphanumeric | Type description in report | — |
| TRAN-REPORT-CAT-CD | PIC 9(04) | Numeric | Category code in report | — |
| TRAN-REPORT-CAT-DESC | PIC X(29) | Alphanumeric | Category description in report | — |
| TRAN-REPORT-SOURCE | PIC X(10) | Alphanumeric | Transaction source | — |
| TRAN-REPORT-AMT | PIC -ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Formatted amount | — |
| REPT-PAGE-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Page subtotal | — |
| REPT-ACCOUNT-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Account subtotal | — |
| REPT-GRAND-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Grand total | — |

### COSTM01.CPY — Statement Transaction Record Layout

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| TRNX-KEY (composite) | — | — | Composite key for statement | — |
| → TRNX-CARD-NUM | PIC X(16) | Alphanumeric (16) | Card number | Alternate index key |
| → TRNX-ID | PIC X(16) | Alphanumeric (16) | Transaction ID | — |
| TRNX-TYPE-CD | PIC X(02) | Alphanumeric (2) | Transaction type code | — |
| TRNX-CAT-CD | PIC 9(04) | Numeric (4) | Transaction category code | — |
| TRNX-SOURCE | PIC X(10) | Alphanumeric (10) | Transaction source | — |
| TRNX-DESC | PIC X(100) | Alphanumeric (100) | Transaction description | — |
| TRNX-AMT | PIC S9(09)V99 | Signed decimal (11,2) | Transaction amount | — |
| TRNX-MERCHANT-ID | PIC 9(09) | Numeric (9) | Merchant ID | — |
| TRNX-MERCHANT-NAME | PIC X(50) | Alphanumeric (50) | Merchant name | — |
| TRNX-MERCHANT-CITY | PIC X(50) | Alphanumeric (50) | Merchant city | — |
| TRNX-MERCHANT-ZIP | PIC X(10) | Alphanumeric (10) | Merchant ZIP | — |
| TRNX-ORIG-TS | PIC X(26) | Alphanumeric (26) | Original timestamp | — |
| TRNX-PROC-TS | PIC X(26) | Alphanumeric (26) | Processing timestamp | — |
| FILLER | PIC X(20) | Filler | Reserved | — |

---

## 5. Export/Migration Entity

### CVEXPORT.cpy — Multi-Record Export Layout (RECLN 500)

**Header Fields (common to all record types):**

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| EXPORT-REC-TYPE | PIC X(1) | Alphanumeric (1) | Record type indicator | 'C'=Customer, 'A'=Account, 'T'=Transaction, 'X'=Xref, 'D'=Card |
| EXPORT-TIMESTAMP | PIC X(26) | Alphanumeric (26) | Export timestamp | Format: YYYY-MM-DD HH:MM:SS.ff |
| EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary numeric | Sequence number (primary key) | Auto-incremented |
| EXPORT-BRANCH-ID | PIC X(4) | Alphanumeric (4) | Source branch identifier | — |
| EXPORT-REGION-CODE | PIC X(5) | Alphanumeric (5) | Source region code | — |
| EXPORT-RECORD-DATA | PIC X(460) | Alphanumeric (460) | Type-specific payload (REDEFINES) | — |

**Storage optimizations:** Uses COMP/COMP-3 packed fields for EXP-CUST-ID (COMP), EXP-ACCT-CURR-BAL (COMP-3), EXP-ACCT-CASH-CREDIT-LIMIT (COMP-3), EXP-TRAN-AMT (COMP-3), EXP-CUST-FICO-CREDIT-SCORE (COMP-3), EXP-CARD-ACCT-ID (COMP), EXP-CARD-CVV-CD (COMP), EXP-XREF-ACCT-ID (COMP), EXP-TRAN-MERCHANT-ID (COMP), EXP-ACCT-CURR-CYC-DEBIT (COMP).

---

## 6. Security Entity

### CSUSR01Y.cpy — User Security Record

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|------------------|
| SEC-USR-ID | PIC X(08) | Alphanumeric (8) | User login ID | Primary key |
| SEC-USR-FNAME | PIC X(20) | Alphanumeric (20) | User first name | — |
| SEC-USR-LNAME | PIC X(20) | Alphanumeric (20) | User last name | — |
| SEC-USR-PWD | PIC X(08) | Alphanumeric (8) | User password | Plaintext storage |
| SEC-USR-TYPE | PIC X(01) | Alphanumeric (1) | User type | 'A' = Admin, 'U' = Regular User |
| SEC-USR-FILLER | PIC X(23) | Filler | Reserved | — |

---

## 7. Application Control / Infrastructure Copybooks

### COCOM01Y.cpy — CICS Communication Area (COMMAREA)

| Field Name | PIC Clause | Data Type | Business Meaning |
|------------|-----------|-----------|------------------|
| CDEMO-FROM-TRANID | PIC X(04) | Alphanumeric | Source transaction ID |
| CDEMO-FROM-PROGRAM | PIC X(08) | Alphanumeric | Source program name |
| CDEMO-TO-TRANID | PIC X(04) | Alphanumeric | Target transaction ID |
| CDEMO-TO-PROGRAM | PIC X(08) | Alphanumeric | Target program name |
| CDEMO-USER-ID | PIC X(08) | Alphanumeric | Current user ID |
| CDEMO-USER-TYPE | PIC X(01) | Alphanumeric | User type ('A'=Admin, 'U'=User) |
| CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 0=First entry, 1=Re-entry |
| CDEMO-CUST-ID | PIC 9(09) | Numeric | Current customer context |
| CDEMO-CUST-FNAME | PIC X(25) | Alphanumeric | Customer first name context |
| CDEMO-CUST-MNAME | PIC X(25) | Alphanumeric | Customer middle name context |
| CDEMO-CUST-LNAME | PIC X(25) | Alphanumeric | Customer last name context |
| CDEMO-ACCT-ID | PIC 9(11) | Numeric | Current account context |
| CDEMO-ACCT-STATUS | PIC X(01) | Alphanumeric | Account status context |
| CDEMO-CARD-NUM | PIC 9(16) | Numeric | Current card context |
| CDEMO-LAST-MAP | PIC X(7) | Alphanumeric | Last displayed BMS map |
| CDEMO-LAST-MAPSET | PIC X(7) | Alphanumeric | Last used BMS mapset |

### COADM02Y.cpy — Admin Menu Options

Defines 6 admin menu options routing to: COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC (Db2), COTRTUPC (Db2).

### COMEN02Y.cpy — Main Menu Options

Defines 11 regular user menu options routing to: COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C.

### CODATECN.cpy — Date Conversion Record

Used for date format conversion between YYYYMMDD and YYYY-MM-DD formats via COBDATFT assembler routine.

### CSDAT01Y.cpy — Date/Time Working Storage

Standard date/time fields: WS-CURDATE (YYYYMMDD), WS-CURTIME (HHMMSSCC), formatted display fields (MM/DD/YY, HH:MM:SS), and 26-char timestamp.

### CSSETATY.cpy — Screen Attribute Setter

Reusable macro (COPY REPLACING) for setting BMS field attributes to red when validation fails and '*' when blank.

### CSSTRPFY.cpy — PF Key Storage

Procedure-division copybook that maps EIBAID to PF key values in COMMAREA using EVALUATE.

### CSUTLDWY.cpy — Date Validation Working Storage

Defines date fields for CCYYMMDD validation: century (19/20), year, month (1–12), day (1–31), leap year handling, and LE CEEDAYS feedback codes.

### CSUTLDPY.cpy — Date Validation Procedures

Procedure-division copybook with reusable paragraphs: EDIT-DATE-CCYYMMDD, EDIT-YEAR-CCYY, EDIT-MONTH, EDIT-DAY, EDIT-DATE-OF-BIRTH. Calls CSUTLDTC for LE date validation.

### CSLKPCDY.cpy — Lookup Code Repository

Contains validation tables for: North American phone area codes (88-level VALUES), US state codes, and state-to-ZIP prefix mappings.

### CSMSG01Y.cpy — Common Messages

Standard application messages: CCDA-MSG-THANK-YOU, CCDA-MSG-INVALID-KEY.

### CSMSG02Y.cpy — Abend Data

Abend handling fields: ABEND-CODE (4), ABEND-CULPRIT (8), ABEND-REASON (50), ABEND-MSG (72).

### COTTL01Y.cpy — Screen Titles

Screen title literals for BMS maps: "AWS Mainframe Modernization" and "CardDemo".

### UNUSED1Y.cpy — Unused/Deprecated Record

Placeholder record with ID, name, password, type fields — appears to be a deprecated copy of CSUSR01Y.
