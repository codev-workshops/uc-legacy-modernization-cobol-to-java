# Data Dictionary — CardDemo COBOL Estate

> All field definitions extracted from copybooks in `app/cpy/` and `app/app-*/cpy/`.
> Fields are grouped by business entity.

---

## 1. Account Entity

### 1.1 CVACT01Y.cpy — Account Record (RECLN 300)

Record: `ACCOUNT-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| ACCT-ID | PIC 9(11) | Numeric (display) | 11 | Unique account identifier | Primary key for ACCTDATA VSAM KSDS |
| ACCT-ACTIVE-STATUS | PIC X(01) | Alphanumeric | 1 | Account active/inactive flag | 'Y' = active, 'N' = inactive |
| ACCT-CURR-BAL | PIC S9(10)V99 | Signed decimal | 12.2 | Current account balance | Signed; negative = credit balance |
| ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | 12.2 | Maximum credit limit | Must be > 0 |
| ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | 12.2 | Maximum cash advance limit | Subset of credit limit |
| ACCT-OPEN-DATE | PIC X(10) | Alphanumeric (date) | 10 | Date account was opened | Format: YYYY-MM-DD |
| ACCT-EXPIRAION-DATE | PIC X(10) | Alphanumeric (date) | 10 | Account expiration date | Format: YYYY-MM-DD |
| ACCT-REISSUE-DATE | PIC X(10) | Alphanumeric (date) | 10 | Last card reissue date | Format: YYYY-MM-DD |
| ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed decimal | 12.2 | Current billing cycle credits (payments) | Running total, reset each cycle |
| ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed decimal | 12.2 | Current billing cycle debits (charges) | Running total, reset each cycle |
| ACCT-ADDR-ZIP | PIC X(10) | Alphanumeric | 10 | Account holder ZIP/postal code | Used for validation lookups |
| ACCT-GROUP-ID | PIC X(10) | Alphanumeric | 10 | Discount/interest rate group | Links to DISCGRP for rate lookup |
| FILLER | PIC X(178) | Filler | 178 | Reserved space | Padding to RECLN 300 |

### 1.2 CVTRA01Y.cpy — Transaction Category Balance (RECLN 50)

Record: `TRAN-CAT-BAL-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| TRANCAT-ACCT-ID | PIC 9(11) | Numeric (display) | 11 | Account identifier | Part of composite key |
| TRANCAT-TYPE-CD | PIC X(02) | Alphanumeric | 2 | Transaction type code | Part of composite key; links to TRANTYPE |
| TRANCAT-CD | PIC 9(04) | Numeric (display) | 4 | Transaction category code | Part of composite key; links to TRANCATG |
| TRAN-CAT-BAL | PIC S9(09)V99 | Signed decimal | 11.2 | Running balance for this account/type/category | Updated during transaction posting |
| FILLER | PIC X(22) | Filler | 22 | Reserved | Padding to RECLN 50 |

### 1.3 CVTRA02Y.cpy — Discount Group (RECLN 50)

Record: `DIS-GROUP-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| DIS-ACCT-GROUP-ID | PIC X(10) | Alphanumeric | 10 | Account group identifier | Part of composite key; links to ACCT-GROUP-ID |
| DIS-TRAN-TYPE-CD | PIC X(02) | Alphanumeric | 2 | Transaction type code | Part of composite key |
| DIS-TRAN-CAT-CD | PIC 9(04) | Numeric (display) | 4 | Transaction category code | Part of composite key |
| DIS-INT-RATE | PIC S9(04)V99 | Signed decimal | 6.2 | Interest rate for this group/type/category | Annual percentage rate |
| FILLER | PIC X(28) | Filler | 28 | Reserved | Padding to RECLN 50 |

---

## 2. Customer Entity

### 2.1 CVCUS01Y.cpy — Customer Record (RECLN 500)

Record: `CUSTOMER-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| CUST-ID | PIC 9(09) | Numeric (display) | 9 | Unique customer identifier | Primary key for CUSTDATA VSAM KSDS |
| CUST-FIRST-NAME | PIC X(25) | Alphanumeric | 25 | Customer first name | |
| CUST-MIDDLE-NAME | PIC X(25) | Alphanumeric | 25 | Customer middle name | |
| CUST-LAST-NAME | PIC X(25) | Alphanumeric | 25 | Customer last name | |
| CUST-ADDR-LINE-1 | PIC X(50) | Alphanumeric | 50 | Address line 1 | |
| CUST-ADDR-LINE-2 | PIC X(50) | Alphanumeric | 50 | Address line 2 | |
| CUST-ADDR-LINE-3 | PIC X(50) | Alphanumeric | 50 | Address line 3 | |
| CUST-ADDR-STATE-CD | PIC X(02) | Alphanumeric | 2 | US state code | Validated via CSLKPCDY (88-level list of valid codes) |
| CUST-ADDR-COUNTRY-CD | PIC X(03) | Alphanumeric | 3 | Country code | |
| CUST-ADDR-ZIP | PIC X(10) | Alphanumeric | 10 | ZIP/postal code | Validated with state via CSLKPCDY |
| CUST-PHONE-NUM-1 | PIC X(15) | Alphanumeric | 15 | Primary phone number | Area code validated via CSLKPCDY |
| CUST-PHONE-NUM-2 | PIC X(15) | Alphanumeric | 15 | Secondary phone number | |
| CUST-SSN | PIC 9(09) | Numeric (display) | 9 | Social Security Number | Sensitive PII |
| CUST-GOVT-ISSUED-ID | PIC X(20) | Alphanumeric | 20 | Government-issued ID (e.g. driver's license) | |
| CUST-DOB-YYYY-MM-DD | PIC X(10) | Alphanumeric (date) | 10 | Date of birth | Format: YYYY-MM-DD |
| CUST-EFT-ACCOUNT-ID | PIC X(10) | Alphanumeric | 10 | Electronic Funds Transfer account | For automatic payments |
| CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alphanumeric | 1 | Primary cardholder indicator | 'Y' = primary, 'N' = authorized user |
| CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric (display) | 3 | FICO credit score | Range: 300-850 |
| FILLER | PIC X(168) | Filler | 168 | Reserved | Padding to RECLN 500 |

### 2.2 CUSTREC.cpy — Customer Record (alternate copy)

Identical structure to CVCUS01Y.cpy. Used only in CBSTM03A for statement generation. Field `CUST-DOB-YYYYMMDD` uses a slightly different name format from `CUST-DOB-YYYY-MM-DD` in CVCUS01Y. **Recommendation:** Consolidate with CVCUS01Y during modernization to eliminate this duplication.

---

## 3. Card Entity

### 3.1 CVACT02Y.cpy — Card Record (RECLN 150)

Record: `CARD-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| CARD-NUM | PIC X(16) | Alphanumeric | 16 | Credit card number | Primary key for CARDDATA VSAM KSDS |
| CARD-ACCT-ID | PIC 9(11) | Numeric (display) | 11 | Linked account ID | FK to ACCOUNT-RECORD |
| CARD-CVV-CD | PIC 9(03) | Numeric (display) | 3 | Card verification value | 3-digit security code |
| CARD-EMBOSSED-NAME | PIC X(50) | Alphanumeric | 50 | Name embossed on card | |
| CARD-EXPIRAION-DATE | PIC X(10) | Alphanumeric (date) | 10 | Card expiration date | Format: YYYY-MM-DD |
| CARD-ACTIVE-STATUS | PIC X(01) | Alphanumeric | 1 | Card active status | 'Y' = active, 'N' = inactive |
| FILLER | PIC X(59) | Filler | 59 | Reserved | Padding to RECLN 150 |

### 3.2 CVACT03Y.cpy — Card Cross-Reference (RECLN 50)

Record: `CARD-XREF-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| XREF-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number | Primary key; FK to CARD-RECORD |
| XREF-CUST-ID | PIC 9(09) | Numeric (display) | 9 | Customer ID | FK to CUSTOMER-RECORD |
| XREF-ACCT-ID | PIC 9(11) | Numeric (display) | 11 | Account ID | FK to ACCOUNT-RECORD |
| FILLER | PIC X(14) | Filler | 14 | Reserved | Padding to RECLN 50 |

### 3.3 CVCRD01Y.cpy — Card Work Areas (CICS screen support)

Record: `CC-WORK-AREAS`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| CCARD-AID | PIC X(5) | Alphanumeric | 5 | CICS attention identifier | 88-level: ENTER, CLEAR, PA1, PA2, PFK01-PFK12 |
| CCARD-NEXT-PROG | PIC X(8) | Alphanumeric | 8 | Next program to XCTL to | |
| CCARD-NEXT-MAPSET | PIC X(7) | Alphanumeric | 7 | Next BMS mapset name | |
| CCARD-NEXT-MAP | PIC X(7) | Alphanumeric | 7 | Next BMS map name | |
| CCARD-ERROR-MSG | PIC X(75) | Alphanumeric | 75 | Error message for screen | |
| CCARD-RETURN-MSG | PIC X(75) | Alphanumeric | 75 | Return/success message | 88 CCARD-RETURN-MSG-OFF = LOW-VALUES |
| CC-ACCT-ID | PIC X(11) | Alphanumeric | 11 | Account ID work field | REDEFINES as PIC 9(11) for numeric use |
| CC-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number work field | REDEFINES as PIC 9(16) for numeric use |
| CC-CUST-ID | PIC X(09) | Alphanumeric | 9 | Customer ID work field | REDEFINES as PIC 9(9) for numeric use |

---

## 4. Transaction Entity

### 4.1 CVTRA05Y.cpy — Transaction Record (RECLN 350)

Record: `TRAN-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| TRAN-ID | PIC X(16) | Alphanumeric | 16 | Unique transaction identifier | Primary key for TRANSACT VSAM KSDS |
| TRAN-TYPE-CD | PIC X(02) | Alphanumeric | 2 | Transaction type code | FK to TRAN-TYPE-RECORD |
| TRAN-CAT-CD | PIC 9(04) | Numeric (display) | 4 | Transaction category code | FK to TRAN-CAT-RECORD |
| TRAN-SOURCE | PIC X(10) | Alphanumeric | 10 | Source system/channel | e.g. 'POS', 'ONLINE', 'ATM' |
| TRAN-DESC | PIC X(100) | Alphanumeric | 100 | Transaction description | Free text |
| TRAN-AMT | PIC S9(09)V99 | Signed decimal | 11.2 | Transaction amount | Positive = debit, negative = credit |
| TRAN-MERCHANT-ID | PIC 9(09) | Numeric (display) | 9 | Merchant identifier | |
| TRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric | 50 | Merchant name | |
| TRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric | 50 | Merchant city | |
| TRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric | 10 | Merchant ZIP code | |
| TRAN-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number used | FK to CARD-RECORD |
| TRAN-ORIG-TS | PIC X(26) | Alphanumeric (timestamp) | 26 | Original transaction timestamp | Format: YYYY-MM-DD-HH.MM.SS.FFFFFF |
| TRAN-PROC-TS | PIC X(26) | Alphanumeric (timestamp) | 26 | Processing timestamp | Set at posting time |
| FILLER | PIC X(20) | Filler | 20 | Reserved | Padding to RECLN 350 |

### 4.2 CVTRA06Y.cpy — Daily Transaction Record (RECLN 350)

Record: `DALYTRAN-RECORD`

Same structure as CVTRA05Y with `DALYTRAN-` prefix. Used for daily transaction input files before posting to the master TRANSACT file.

> **Integration note:** DALYTRAN.PS is the critical integration seam between the real-time authorization pipeline (MQ/IMS) and the nightly batch posting pipeline (VSAM). Its origin — how authorized transactions flow from IMS into this file — is outside the application boundary (external acquiring network feed). This undocumented bridge is important for modernization planning.

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| DALYTRAN-ID | PIC X(16) | Alphanumeric | 16 | Daily transaction ID | Must be unique; becomes TRAN-ID after posting |
| DALYTRAN-TYPE-CD | PIC X(02) | Alphanumeric | 2 | Transaction type code | Must exist in TRANTYPE file |
| DALYTRAN-CAT-CD | PIC 9(04) | Numeric (display) | 4 | Transaction category code | Must exist in TRANCATG file |
| DALYTRAN-SOURCE | PIC X(10) | Alphanumeric | 10 | Source channel | |
| DALYTRAN-DESC | PIC X(100) | Alphanumeric | 100 | Transaction description | |
| DALYTRAN-AMT | PIC S9(09)V99 | Signed decimal | 11.2 | Transaction amount | |
| DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric (display) | 9 | Merchant ID | |
| DALYTRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric | 50 | Merchant name | |
| DALYTRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric | 50 | Merchant city | |
| DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric | 10 | Merchant ZIP | |
| DALYTRAN-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number | Validated against CARDXREF |
| DALYTRAN-ORIG-TS | PIC X(26) | Alphanumeric (timestamp) | 26 | Original timestamp | |
| DALYTRAN-PROC-TS | PIC X(26) | Alphanumeric (timestamp) | 26 | Processing timestamp | Set during posting |
| FILLER | PIC X(20) | Filler | 20 | Reserved | |

### 4.3 CVTRA03Y.cpy — Transaction Type (RECLN 60)

Record: `TRAN-TYPE-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| TRAN-TYPE | PIC X(02) | Alphanumeric | 2 | Transaction type code | Primary key for TRANTYPE VSAM KSDS |
| TRAN-TYPE-DESC | PIC X(50) | Alphanumeric | 50 | Type description | e.g. 'Purchase', 'Cash Advance', 'Payment' |
| FILLER | PIC X(08) | Filler | 8 | Reserved | Padding to RECLN 60 |

### 4.4 CVTRA04Y.cpy — Transaction Category Type (RECLN 60)

Record: `TRAN-CAT-RECORD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| TRAN-TYPE-CD | PIC X(02) | Alphanumeric | 2 | Transaction type code | Part of composite key |
| TRAN-CAT-CD | PIC 9(04) | Numeric (display) | 4 | Transaction category code | Part of composite key |
| TRAN-CAT-TYPE-DESC | PIC X(50) | Alphanumeric | 50 | Category description | e.g. 'Retail Purchase', 'Restaurant' |
| FILLER | PIC X(04) | Filler | 4 | Reserved | Padding to RECLN 60 |

### 4.5 CVTRA07Y.cpy — Transaction Report Layout

Working storage for the daily transaction report output.

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| REPT-SHORT-NAME | PIC X(38) | Alphanumeric | 38 | Report short name | VALUE 'DALYREPT' |
| REPT-LONG-NAME | PIC X(41) | Alphanumeric | 41 | Report long name | VALUE 'Daily Transaction Report' |
| REPT-DATE-HEADER | PIC X(12) | Alphanumeric | 12 | Date range label | VALUE 'Date Range: ' |
| REPT-START-DATE | PIC X(10) | Alphanumeric | 10 | Report period start date | |
| REPT-END-DATE | PIC X(10) | Alphanumeric | 10 | Report period end date | |
| TRAN-REPORT-TRANS-ID | PIC X(16) | Alphanumeric | 16 | Transaction ID (report) | |
| TRAN-REPORT-ACCOUNT-ID | PIC X(11) | Alphanumeric | 11 | Account ID (report) | |
| TRAN-REPORT-TYPE-CD | PIC X(02) | Alphanumeric | 2 | Type code (report) | |
| TRAN-REPORT-TYPE-DESC | PIC X(15) | Alphanumeric | 15 | Type description (report) | |
| TRAN-REPORT-CAT-CD | PIC 9(04) | Numeric (display) | 4 | Category code (report) | |
| TRAN-REPORT-CAT-DESC | PIC X(29) | Alphanumeric | 29 | Category description (report) | |
| TRAN-REPORT-SOURCE | PIC X(10) | Alphanumeric | 10 | Source (report) | |
| TRAN-REPORT-AMT | PIC -ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | 16 | Formatted amount | Includes sign and commas |
| REPT-PAGE-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | 16 | Page subtotal | |
| REPT-ACCOUNT-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | 16 | Account subtotal | |
| REPT-GRAND-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | 16 | Grand total | |

### 4.6 COSTM01.CPY — Statement Transaction Record

Working structure for statement printing (used by CBSTM03A).

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| TRNX-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number | |
| TRNX-ID | PIC X(16) | Alphanumeric | 16 | Transaction ID | |
| TRNX-TYPE-CD | PIC X(02) | Alphanumeric | 2 | Type code | |
| TRNX-CAT-CD | PIC 9(04) | Numeric (display) | 4 | Category code | |
| TRNX-SOURCE | PIC X(10) | Alphanumeric | 10 | Source | |
| TRNX-DESC | PIC X(100) | Alphanumeric | 100 | Description | |
| TRNX-AMT | PIC S9(09)V99 | Signed decimal | 11.2 | Amount | |
| TRNX-MERCHANT-ID | PIC 9(09) | Numeric (display) | 9 | Merchant ID | |
| TRNX-MERCHANT-NAME | PIC X(50) | Alphanumeric | 50 | Merchant name | |
| TRNX-MERCHANT-CITY | PIC X(50) | Alphanumeric | 50 | Merchant city | |
| TRNX-MERCHANT-ZIP | PIC X(10) | Alphanumeric | 10 | Merchant ZIP | |
| TRNX-ORIG-TS | PIC X(26) | Alphanumeric (timestamp) | 26 | Origination timestamp | |
| TRNX-PROC-TS | PIC X(26) | Alphanumeric (timestamp) | 26 | Processing timestamp | |
| FILLER | PIC X(20) | Filler | 20 | Reserved | |

---

## 5. Authorization Entity (IMS)

> The IMS segments defined in CIPAUSMY and CIPAUDTY correspond to physical database DBPAUTP0 (see `app/app-authorization-ims-db2-mq/ims/DBPAUTP0.dbd`). Root segment = PAUTSUM0 (keyed by ACCT-ID), child segment = PAUTDTL1 (keyed by PA-AUTH-DATE-9C + PA-AUTH-TIME-9C). Index database is DBPAUTX0.

### 5.1 CIPAUSMY.cpy — Pending Authorization Summary (IMS Root Segment)

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| PA-ACCT-ID | PIC S9(11) COMP-3 | Packed decimal | 6 bytes | Account identifier | Root segment key |
| PA-CUST-ID | PIC 9(09) | Numeric (display) | 9 | Customer ID | FK to Customer |
| PA-AUTH-STATUS | PIC X(01) | Alphanumeric | 1 | Authorization batch status | |
| PA-ACCOUNT-STATUS | PIC X(02) OCCURS 5 | Alphanumeric array | 10 | Account status history (5 entries) | |
| PA-CREDIT-LIMIT | PIC S9(09)V99 COMP-3 | Packed decimal | 6 bytes | Credit limit snapshot | |
| PA-CASH-LIMIT | PIC S9(09)V99 COMP-3 | Packed decimal | 6 bytes | Cash limit snapshot | |
| PA-CREDIT-BALANCE | PIC S9(09)V99 COMP-3 | Packed decimal | 6 bytes | Credit balance snapshot | |
| PA-CASH-BALANCE | PIC S9(09)V99 COMP-3 | Packed decimal | 6 bytes | Cash balance snapshot | |
| PA-APPROVED-AUTH-CNT | PIC S9(04) COMP | Binary | 2 bytes | Count of approved authorizations | |
| PA-DECLINED-AUTH-CNT | PIC S9(04) COMP | Binary | 2 bytes | Count of declined authorizations | |
| PA-APPROVED-AUTH-AMT | PIC S9(09)V99 COMP-3 | Packed decimal | 6 bytes | Total approved amount | |
| PA-DECLINED-AUTH-AMT | PIC S9(09)V99 COMP-3 | Packed decimal | 6 bytes | Total declined amount | |
| FILLER | PIC X(34) | Filler | 34 | Reserved | |

### 5.2 CIPAUDTY.cpy — Pending Authorization Detail (IMS Child Segment)

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| PA-AUTH-DATE-9C | PIC S9(05) COMP-3 | Packed decimal | 3 bytes | Authorization date (compressed) | Part of segment key |
| PA-AUTH-TIME-9C | PIC S9(09) COMP-3 | Packed decimal | 5 bytes | Authorization time (compressed) | Part of segment key |
| PA-AUTH-ORIG-DATE | PIC X(06) | Alphanumeric | 6 | Original auth date | Format: YYMMDD |
| PA-AUTH-ORIG-TIME | PIC X(06) | Alphanumeric | 6 | Original auth time | Format: HHMMSS |
| PA-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number | |
| PA-AUTH-TYPE | PIC X(04) | Alphanumeric | 4 | Authorization type | |
| PA-CARD-EXPIRY-DATE | PIC X(04) | Alphanumeric | 4 | Card expiry | Format: YYMM |
| PA-MESSAGE-TYPE | PIC X(06) | Alphanumeric | 6 | Message type indicator | ISO 8583 message type |
| PA-MESSAGE-SOURCE | PIC X(06) | Alphanumeric | 6 | Message source | |
| PA-AUTH-ID-CODE | PIC X(06) | Alphanumeric | 6 | Authorization ID code | Returned to merchant |
| PA-AUTH-RESP-CODE | PIC X(02) | Alphanumeric | 2 | Response code | 88 PA-AUTH-APPROVED VALUE '00' |
| PA-AUTH-RESP-REASON | PIC X(04) | Alphanumeric | 4 | Response reason code | Detailed decline reason |
| PA-PROCESSING-CODE | PIC 9(06) | Numeric (display) | 6 | Processing code | ISO 8583 processing code |
| PA-TRANSACTION-AMT | PIC S9(10)V99 COMP-3 | Packed decimal | 7 bytes | Requested transaction amount | |
| PA-APPROVED-AMT | PIC S9(10)V99 COMP-3 | Packed decimal | 7 bytes | Approved amount | May differ from requested |
| PA-MERCHANT-CATAGORY-CODE | PIC X(04) | Alphanumeric | 4 | Merchant category code (MCC) | ISO 18245 MCC |
| PA-ACQR-COUNTRY-CODE | PIC X(03) | Alphanumeric | 3 | Acquirer country code | |
| PA-POS-ENTRY-MODE | PIC 9(02) | Numeric (display) | 2 | Point of sale entry mode | |
| PA-MERCHANT-ID | PIC X(15) | Alphanumeric | 15 | Merchant identifier | |
| PA-MERCHANT-NAME | PIC X(22) | Alphanumeric | 22 | Merchant name | |
| PA-MERCHANT-CITY | PIC X(13) | Alphanumeric | 13 | Merchant city | |
| PA-MERCHANT-STATE | PIC X(02) | Alphanumeric | 2 | Merchant state | |
| PA-MERCHANT-ZIP | PIC X(09) | Alphanumeric | 9 | Merchant ZIP | |
| PA-TRANSACTION-ID | PIC X(15) | Alphanumeric | 15 | Transaction ID | |
| PA-MATCH-STATUS | PIC X(01) | Alphanumeric | 1 | Match status | 88: P=Pending, D=Declined, E=Expired, M=Matched |
| PA-AUTH-FRAUD | PIC X(01) | Alphanumeric | 1 | Fraud flag | 88: F=Fraud Confirmed, R=Fraud Removed |
| PA-FRAUD-RPT-DATE | PIC X(08) | Alphanumeric | 8 | Fraud report date | Format: YYYYMMDD |
| FILLER | PIC X(17) | Filler | 17 | Reserved | |

> **Note:** COPAUS2C (fraud marking) has no associated BMS map. It is invoked programmatically from COPAUS1C via XCTL, not as a standalone CICS transaction. It uses CIPAUDTY for the IMS detail segment and performs a DB2 INSERT into CARDDEMO.AUTHFRDS.

### 5.3 CCPAURQY.cpy — Authorization MQ Request

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| PA-RQ-AUTH-DATE | PIC X(06) | Alphanumeric | 6 | Request date | |
| PA-RQ-AUTH-TIME | PIC X(06) | Alphanumeric | 6 | Request time | |
| PA-RQ-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number | |
| PA-RQ-AUTH-TYPE | PIC X(04) | Alphanumeric | 4 | Authorization type | |
| PA-RQ-CARD-EXPIRY-DATE | PIC X(04) | Alphanumeric | 4 | Card expiry date | |
| PA-RQ-MESSAGE-TYPE | PIC X(06) | Alphanumeric | 6 | ISO message type | |
| PA-RQ-MESSAGE-SOURCE | PIC X(06) | Alphanumeric | 6 | Source system | |
| PA-RQ-PROCESSING-CODE | PIC 9(06) | Numeric (display) | 6 | Processing code | |
| PA-RQ-TRANSACTION-AMT | PIC +9(10).99 | Edited numeric | 14 | Transaction amount | |
| PA-RQ-MERCHANT-CATAGORY-CODE | PIC X(04) | Alphanumeric | 4 | MCC | |
| PA-RQ-ACQR-COUNTRY-CODE | PIC X(03) | Alphanumeric | 3 | Acquirer country | |
| PA-RQ-POS-ENTRY-MODE | PIC 9(02) | Numeric (display) | 2 | POS entry mode | |
| PA-RQ-MERCHANT-ID | PIC X(15) | Alphanumeric | 15 | Merchant ID | |
| PA-RQ-MERCHANT-NAME | PIC X(22) | Alphanumeric | 22 | Merchant name | |
| PA-RQ-MERCHANT-CITY | PIC X(13) | Alphanumeric | 13 | Merchant city | |
| PA-RQ-MERCHANT-STATE | PIC X(02) | Alphanumeric | 2 | Merchant state | |
| PA-RQ-MERCHANT-ZIP | PIC X(09) | Alphanumeric | 9 | Merchant ZIP | |
| PA-RQ-TRANSACTION-ID | PIC X(15) | Alphanumeric | 15 | Transaction ID | |

### 5.4 CCPAURLY.cpy — Authorization MQ Reply

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| PA-RL-CARD-NUM | PIC X(16) | Alphanumeric | 16 | Card number | Echo from request |
| PA-RL-TRANSACTION-ID | PIC X(15) | Alphanumeric | 15 | Transaction ID | Echo from request |
| PA-RL-AUTH-ID-CODE | PIC X(06) | Alphanumeric | 6 | Authorization ID code | Generated on approval |
| PA-RL-AUTH-RESP-CODE | PIC X(02) | Alphanumeric | 2 | Response code | '00' = approved |
| PA-RL-AUTH-RESP-REASON | PIC X(04) | Alphanumeric | 4 | Reason code | |
| PA-RL-APPROVED-AMT | PIC +9(10).99 | Edited numeric | 14 | Approved amount | |

### 5.5 CCPAUERY.cpy — Authorization Error Record

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| ERR-DATE | PIC X(06) | Alphanumeric | 6 | Error date | |
| ERR-TIME | PIC X(06) | Alphanumeric | 6 | Error time | |
| ERR-APPLICATION | PIC X(08) | Alphanumeric | 8 | Application name | |
| ERR-PROGRAM | PIC X(08) | Alphanumeric | 8 | Program name | |
| ERR-LOCATION | PIC X(04) | Alphanumeric | 4 | Error location in code | |
| ERR-LEVEL | PIC X(01) | Alphanumeric | 1 | Severity level | 88: L=Log, I=Info, W=Warning, C=Critical |
| ERR-SUBSYSTEM | PIC X(01) | Alphanumeric | 1 | Subsystem | 88: A=App, C=CICS, I=IMS, D=DB2, M=MQ, F=File |
| ERR-CODE-1 | PIC X(09) | Alphanumeric | 9 | Primary error code | |
| ERR-CODE-2 | PIC X(09) | Alphanumeric | 9 | Secondary error code | |
| ERR-MESSAGE | PIC X(50) | Alphanumeric | 50 | Error description | |

---

## 6. Security Entity

### 6.1 CSUSR01Y.cpy — User Security Record (RECLN 80)

Record: `SEC-USER-DATA`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| SEC-USR-ID | PIC X(08) | Alphanumeric | 8 | User login ID | Primary key for USRSEC VSAM KSDS |
| SEC-USR-FNAME | PIC X(20) | Alphanumeric | 20 | User first name | |
| SEC-USR-LNAME | PIC X(20) | Alphanumeric | 20 | User last name | |
| SEC-USR-PWD | PIC X(08) | Alphanumeric | 8 | User password | Stored in plaintext (security concern) |
| SEC-USR-TYPE | PIC X(01) | Alphanumeric | 1 | User type | 'A' = admin, 'U' = regular user |
| SEC-USR-FILLER | PIC X(23) | Filler | 23 | Reserved | Padding to RECLN 80 |

### 6.2 UNUSED1Y.cpy — Unused Security Record

Identical structure to CSUSR01Y with `UNUSED-` prefix. **Dead code candidate:** No program references this copybook. Recommend removal during modernization.

---

## 7. Application Control / Navigation

### 7.1 COCOM01Y.cpy — Communication Area (COMMAREA)

Record: `CARDDEMO-COMMAREA`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| CDEMO-FROM-TRANID | PIC X(04) | Alphanumeric | 4 | Source CICS transaction ID | |
| CDEMO-FROM-PROGRAM | PIC X(08) | Alphanumeric | 8 | Source program name | |
| CDEMO-TO-TRANID | PIC X(04) | Alphanumeric | 4 | Target transaction ID | |
| CDEMO-TO-PROGRAM | PIC X(08) | Alphanumeric | 8 | Target program name | |
| CDEMO-USER-ID | PIC X(08) | Alphanumeric | 8 | Logged-in user ID | |
| CDEMO-USER-TYPE | PIC X(01) | Alphanumeric | 1 | User type flag | 88: A=Admin, U=User |
| CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 1 | Program entry context | 88: 0=Enter, 1=Reenter |
| CDEMO-CUST-ID | PIC 9(09) | Numeric | 9 | Current customer ID | |
| CDEMO-CUST-FNAME | PIC X(25) | Alphanumeric | 25 | Customer first name (display) | |
| CDEMO-CUST-MNAME | PIC X(25) | Alphanumeric | 25 | Customer middle name (display) | |
| CDEMO-CUST-LNAME | PIC X(25) | Alphanumeric | 25 | Customer last name (display) | |
| CDEMO-ACCT-ID | PIC 9(11) | Numeric | 11 | Current account ID | |
| CDEMO-ACCT-STATUS | PIC X(01) | Alphanumeric | 1 | Account status | |
| CDEMO-CARD-NUM | PIC 9(16) | Numeric | 16 | Current card number | |
| CDEMO-LAST-MAP | PIC X(7) | Alphanumeric | 7 | Last BMS map used | |
| CDEMO-LAST-MAPSET | PIC X(7) | Alphanumeric | 7 | Last BMS mapset used | |

### 7.2 COMEN02Y.cpy — Main Menu Options

11 menu options mapping to program names:

| Option | Label | Program | User Type |
|--------|-------|---------|-----------|
| 1 | Account View | COACTVWC | U |
| 2 | Account Update | COACTUPC | U |
| 3 | Credit Card List | COCRDLIC | U |
| 4 | Credit Card Detail | COCRDSLC | U |
| 5 | Credit Card Update | COCRDUPC | U |
| 6 | Transaction List | COTRN00C | U |
| 7 | Transaction View | COTRN01C | U |
| 8 | Transaction Add | COTRN02C | U |
| 9 | Bill Payment | COBIL00C | U |
| 10 | Print Transaction Reports | CORPT00C | U |
| 11 | Authorization Summary | COPAUS0C | U |

### 7.3 COADM02Y.cpy — Admin Menu Options

| Option | Label | Program |
|--------|-------|---------|
| 1 | User List (Security) | COUSR00C |
| 2 | User Add (Security) | COUSR01C |
| 3 | User Update (Security) | COUSR02C |
| 4 | User Delete (Security) | COUSR03C |
| 5 | Transaction Type List/Update (DB2) | COTRTLIC |
| 6 | Transaction Type Maintenance (DB2) | COTRTUPC |

---

## 8. Export/Migration Entity

### 8.1 CVEXPORT.cpy — Multi-Record Export Layout (RECLN 500)

Record: `EXPORT-RECORD`

**Header fields (common to all record types):**

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Validation / Notes |
|------------|-----------|-----------|--------|-----------------|-------------------|
| EXPORT-REC-TYPE | PIC X(1) | Alphanumeric | 1 | Record type indicator | 'C'=Customer, 'A'=Account, 'T'=Transaction, 'X'=Xref, 'D'=Card |
| EXPORT-TIMESTAMP | PIC X(26) | Alphanumeric | 26 | Export timestamp | REDEFINES to EXPORT-DATE + EXPORT-TIME |
| EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | 4 bytes | Sequence number within export | |
| EXPORT-BRANCH-ID | PIC X(4) | Alphanumeric | 4 | Originating branch ID | |
| EXPORT-REGION-CODE | PIC X(5) | Alphanumeric | 5 | Region code | |
| EXPORT-RECORD-DATA | PIC X(460) | Alphanumeric | 460 | Record data (REDEFINES by type) | |

**REDEFINES structures** mirror the corresponding entity copybooks (Customer, Account, Transaction, Card, Cross-Reference) with COMP/COMP-3 optimizations for numeric fields in the export format.

---

## 9. Utility / Validation Copybooks

### 9.1 CSUTLDWY.cpy — Date Edit/Validation Utility

Comprehensive date validation copybook with 88-level conditions:

| Key Validation | Rule |
|----------------|------|
| Century check | 88 THIS-CENTURY VALUE 20, LAST-CENTURY VALUE 19 |
| Month validation | 88 WS-VALID-MONTH VALUES 1–12 |
| 31-day months | 88 WS-31-DAY-MONTH VALUES 1, 3, 5, 7, 8, 10, 12 |
| February | 88 WS-FEBRUARY VALUE 2 |
| Day validation | 88 WS-VALID-DAY VALUES 1–31, 88 WS-DAY-31 VALUE 31, WS-DAY-30 VALUE 30, WS-DAY-29 VALUE 29 |
| Overall validity | 88 WS-EDIT-DATE-IS-VALID VALUE LOW-VALUES (no errors) |
| Error flags | WS-EDIT-YEAR-FLG, WS-EDIT-MONTH, WS-EDIT-DAY with ISVALID/NOT-OK/BLANK conditions |

### 9.2 CSLKPCDY.cpy — Phone/State/ZIP Lookup Validation

| Validation | Type |
|------------|------|
| US phone area codes | 88 VALID-PHONE-AREA-CODE — extensive list of valid 3-digit codes |
| US state codes | 88 VALID-US-STATE-CODE — all 50 states + DC + territories |
| State-ZIP combinations | 88 VALID-US-STATE-ZIP-CD2-COMBO — validates first 2 ZIP digits match state |

### 9.3 CODATECN.cpy — Date Conversion Utility

Converts between YYYYMMDD and YYYY-MM-DD formats.

| Field Name | PIC Clause | Business Meaning |
|------------|-----------|-----------------|
| CODATECN-TYPE | PIC X | Input format type: 88 YYYYMMDD-IN='1', YYYY-MM-DD-IN='2' |
| CODATECN-INP-DATE | PIC X(20) | Input date (redefined for both formats) |
| CODATECN-OUTTYPE | PIC X | Output format type: 88 YYYY-MM-DD-OP='1', YYYYMMDD-OP='2' |
| CODATECN-OUT-DATE | PIC X(20) | Output date (redefined for both formats) |
| CODATECN-ERROR-MSG | PIC X(38) | Error message on conversion failure |

### 9.4 CSDAT01Y.cpy — Current Date/Time Working Storage

System date/time fields for screen display.

| Field Name | PIC Clause | Business Meaning |
|------------|-----------|-----------------|
| WS-CURDATE-YEAR | PIC 9(04) | Current year |
| WS-CURDATE-MONTH | PIC 9(02) | Current month |
| WS-CURDATE-DAY | PIC 9(02) | Current day |
| WS-CURTIME-HOURS | PIC 9(02) | Current hours |
| WS-CURTIME-MINUTE | PIC 9(02) | Current minutes |
| WS-CURTIME-SECOND | PIC 9(02) | Current seconds |
| WS-TIMESTAMP-* | Various | Full timestamp for DB2/CICS |

### 9.5 COTTL01Y.cpy — Screen Title Constants

| Field Name | Value |
|------------|-------|
| CCDA-TITLE01 | Application title line 1 |
| CCDA-TITLE02 | Application title line 2 |
| CCDA-THANK-YOU | Thank you message |

### 9.6 CSMSG01Y.cpy / CSMSG02Y.cpy — Standard Messages

Common screen messages (thank you, invalid key, etc.).

---

## 10. IMS PCB / DL/I Definitions

### 10.1 PAUTBPCB.CPY — IMS PCB for Authorization DB

| Field Name | PIC Clause | Business Meaning |
|------------|-----------|-----------------|
| PAUT-DBDNAME | PIC X(08) | Database description name |
| PAUT-SEG-LEVEL | PIC X(02) | Current segment level |
| PAUT-PCB-STATUS | PIC X(02) | DL/I status code (GE=not found, blank=OK) |
| PAUT-PCB-PROCOPT | PIC X(04) | Processing option (A=all, G=get, I=insert) |
| PAUT-SEG-NAME | PIC X(08) | Current segment name |
| PAUT-KEYFB | PIC X(255) | Key feedback area |

### 10.2 PASFLPCB.CPY / PADFLPCB.CPY — GSAM PCBs

PCBs for sequential GSAM file access during database unload/load operations.

### 10.3 IMSFUNCS.cpy — IMS DL/I Function Constants

| Constant | Value | Meaning |
|----------|-------|---------|
| FUNC-GU | 'GU  ' | Get Unique |
| FUNC-GHU | 'GHU ' | Get Hold Unique |
| FUNC-GN | 'GN  ' | Get Next |
| FUNC-GHN | 'GHN ' | Get Hold Next |
| FUNC-GNP | 'GNP ' | Get Next within Parent |
| FUNC-GHNP | 'GHNP' | Get Hold Next within Parent |
| FUNC-REPL | 'REPL' | Replace |
| FUNC-ISRT | 'ISRT' | Insert |
| FUNC-DLET | 'DLET' | Delete |

---

## 11. DB2 Table Definitions

### 11.1 CARDDEMO.AUTHFRDS — Authorization Fraud Detail

Source: `app/app-authorization-ims-db2-mq/ddl/AUTHFRDS.ddl`

| Column | DB2 Type | Business Meaning |
|--------|----------|------------------|
| CARD_NUM | CHAR(16) NOT NULL | Card number (PK part 1) |
| AUTH_TS | TIMESTAMP NOT NULL | Authorization timestamp (PK part 2) |
| AUTH_TYPE | CHAR(4) | Authorization type code |
| CARD_EXPIRY_DATE | CHAR(4) | Card expiry (YYMM) |
| MESSAGE_TYPE | CHAR(6) | ISO 8583 message type |
| MESSAGE_SOURCE | CHAR(6) | Message source system |
| AUTH_ID_CODE | CHAR(6) | Authorization ID returned to merchant |
| AUTH_RESP_CODE | CHAR(2) | Response code ('00' = approved) |
| AUTH_RESP_REASON | CHAR(4) | Detailed decline reason |
| PROCESSING_CODE | CHAR(6) | ISO 8583 processing code |
| TRANSACTION_AMT | DECIMAL(12,2) | Requested transaction amount |
| APPROVED_AMT | DECIMAL(12,2) | Approved amount (may differ from requested) |
| MERCHANT_CATAGORY_CODE | CHAR(4) | Merchant category code (MCC) |
| ACQR_COUNTRY_CODE | CHAR(3) | Acquirer country code |
| POS_ENTRY_MODE | SMALLINT | Point of sale entry mode |
| MERCHANT_ID | CHAR(15) | Merchant identifier |
| MERCHANT_NAME | VARCHAR(22) | Merchant name |
| MERCHANT_CITY | CHAR(13) | Merchant city |
| MERCHANT_STATE | CHAR(02) | Merchant state |
| MERCHANT_ZIP | CHAR(09) | Merchant ZIP code |
| TRANSACTION_ID | CHAR(15) | Transaction ID |
| MATCH_STATUS | CHAR(1) | Match status (P/D/E/M) |
| AUTH_FRAUD | CHAR(1) | Fraud flag (F=Fraud Confirmed, R=Removed) |
| FRAUD_RPT_DATE | DATE | Fraud report date |
| ACCT_ID | DECIMAL(11) | Account identifier |
| CUST_ID | DECIMAL(9) | Customer identifier |

**Primary Key:** (CARD_NUM, AUTH_TS)

**Index:** `CARDDEMO.XAUTHFRD` — UNIQUE on (CARD_NUM ASC, AUTH_TS DESC) — Source: `XAUTHFRD.ddl`

**DCL Grants:** `AUTHFRDS.dcl` contains DCLGEN-generated COBOL host variable declarations (record `DCLAUTHFRDS`) mapping DB2 columns to COBOL PIC clauses with COMP-3 for decimal fields.

### 11.2 CARDDEMO.TRANSACTION_TYPE — Transaction Type Master

Source: `app/app-transaction-type-db2/ddl/TRNTYPE.ddl`

| Column | DB2 Type | Business Meaning |
|--------|----------|------------------|
| TR_TYPE | CHAR(2) NOT NULL | Transaction type code (PK) |
| TR_DESCRIPTION | VARCHAR(50) NOT NULL | Type description (e.g. 'Purchase', 'Cash Advance') |

**Primary Key:** (TR_TYPE)

**Index:** `CARDDEMO.XTRAN_TYPE` — UNIQUE on (TR_TYPE ASC) — Source: `XTRNTYPE.ddl`

### 11.3 CARDDEMO.TRANSACTION_TYPE_CATEGORY — Transaction Type Category

Source: `app/app-transaction-type-db2/ddl/TRNTYCAT.ddl`

| Column | DB2 Type | Business Meaning |
|--------|----------|------------------|
| TRC_TYPE_CODE | CHAR(2) NOT NULL | Transaction type code (PK part 1, FK to TRANSACTION_TYPE) |
| TRC_TYPE_CATEGORY | CHAR(4) NOT NULL | Category code (PK part 2) |
| TRC_CAT_DATA | VARCHAR(50) NOT NULL | Category description (e.g. 'Retail Purchase', 'Restaurant') |

**Primary Key:** (TRC_TYPE_CODE, TRC_TYPE_CATEGORY)

**Foreign Key:** TRC_TYPE_CODE REFERENCES CARDDEMO.TRANSACTION_TYPE (TR_TYPE) ON DELETE RESTRICT

**Index:** `CARDDEMO.X_TRAN_TYPE_CATG` — UNIQUE on (TRC_TYPE_CODE ASC, TRC_TYPE_CATEGORY ASC) — Source: `XTRNTYCAT.ddl`
