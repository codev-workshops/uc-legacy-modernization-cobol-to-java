# CardDemo — Data Dictionary

All copybooks under `app/cpy/` and the sub-app `cpy/` directories, grouped by business entity. Record lengths are computed by summing elementary PIC sizes (COMP-3 `S9(n)`/`S9(n)V9(m)` = ⌈(digits+1)/2⌉ bytes; COMP/BINARY = 2/4/8 bytes for ≤4/≤9/≤18 digits; REDEFINES and 88-levels excluded). Types: inferred from PIC/usage. Business meaning is inferred from usage in the programs; entries marked "(inferred)" are best-effort.

## 1. Account — CVACT01Y

Layout of ACCTDAT (RECLN 300, KSDS key = ACCT-ID).

### CVACT01Y — `app/cpy/CVACT01Y.cpy` — computed length **300** bytes

Used by: CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, COACCT01, COACTUPC, COACTVWC, COBIL00C, COPAUA0C, COPAUS0C, COTRN02C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| ACCOUNT-RECORD | 01 |  | Group | 300-byte ACCTDAT KSDS record image. | none in code |
| ACCT-ID | 05 | 9(11) | Zoned numeric | Account identifier (KSDS primary key, 11 digits). | COACTUPC `1210-EDIT-ACCOUNT`: must be a non-zero 11-digit number; looked up in ACCTDAT. |
| ACCT-ACTIVE-STATUS | 05 | X(01) | Alphanumeric | Account status flag ('Y' active). | COACTUPC `1220-EDIT-YESNO`: must be 'Y' or 'N'. |
| ACCT-CURR-BAL | 05 | S9(10)V99 | Signed zoned | Current balance. | COACTUPC `1250-EDIT-SIGNED-9V2`: signed numeric (TEST-NUMVAL-C). CBTRN02C `2800-UPDATE-ACCOUNT-REC` adds posted amount; COBIL00C rejects bill-pay when balance <= 0. |
| ACCT-CREDIT-LIMIT | 05 | S9(10)V99 | Signed zoned | Credit limit. | COACTUPC `1250-EDIT-SIGNED-9V2`: signed numeric. CBTRN02C `1500-B-LOOKUP-ACCT`: rejects when ACCT-CURR-CYC-CREDIT − ACCT-CURR-CYC-DEBIT + tran amt > limit (reason 102 OVERLIMIT). |
| ACCT-CASH-CREDIT-LIMIT | 05 | S9(10)V99 | Signed zoned | Cash-advance credit limit. | COACTUPC `1250-EDIT-SIGNED-9V2`: signed numeric. |
| ACCT-OPEN-DATE | 05 | X(10) | Alphanumeric | Account open date (YYYY-MM-DD). | COACTUPC: `EDIT-DATE-CCYYMMDD` (CSUTLDPY) valid calendar date. |
| ACCT-EXPIRAION-DATE | 05 | X(10) | Alphanumeric | Account expiry date (YYYY-MM-DD; spelling as in source). | COACTUPC: `EDIT-DATE-CCYYMMDD`. CBTRN02C `1500-B`: must be >= tran orig date else reason 103 'TRANSACTION RECEIVED AFTER ACCT EXPIRATION'. |
| ACCT-REISSUE-DATE | 05 | X(10) | Alphanumeric | Card/account reissue date. | COACTUPC: `EDIT-DATE-CCYYMMDD`. |
| ACCT-CURR-CYC-CREDIT | 05 | S9(10)V99 | Signed zoned | Sum of positive amounts posted this cycle; reset to 0 by CBACT04C after interest computation. | COACTUPC `1250-EDIT-SIGNED-9V2`. Maintained by CBTRN02C `2800` (positive amts); reset to 0 by CBACT04C `1050-UPDATE-ACCOUNT`. |
| ACCT-CURR-CYC-DEBIT | 05 | S9(10)V99 | Signed zoned | Sum of debits posted this cycle. | COACTUPC `1250-EDIT-SIGNED-9V2`. Maintained by CBTRN02C `2800` (negative amts); reset to 0 by CBACT04C `1050-UPDATE-ACCOUNT`. |
| ACCT-ADDR-ZIP | 05 | X(10) | Alphanumeric | Account address zip code. | none in code |
| ACCT-GROUP-ID | 05 | X(10) | Alphanumeric | Disclosure-group id joining to DISCGRP for interest-rate lookup. | Used by CBACT04C `1200-GET-INTEREST-RATE` to key DISCGRP (fallback default rate `1200-A`). |
| FILLER | 05 | X(178) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- `ACCT-CURR-BAL ≤ ACCT-CREDIT-LIMIT` enforced indirectly: CBTRN02C `1500-B-LOOKUP-ACCT` computes `cyc-credit − cyc-debit + tran-amt` and rejects with reason 102 'OVERLIMIT TRANSACTION' when it exceeds `ACCT-CREDIT-LIMIT`; also rejects when `ACCT-EXPIRAION-DATE < tran orig date` (reason 103) and on missing account (101).
- `ACCT-OPEN/EXPIRAION/REISSUE-DATE` validated as real dates via CSUTLDPY `EDIT-DATE-CCYYMMDD` when updated in COACTUPC.

## 2. Customer — CVCUS01Y, CUSTREC

CVCUS01Y is the canonical CUSTDAT layout (RECLN 500). CUSTREC is a duplicate used only by CBSTM03A; it is identical except the DOB field is named `CUST-DOB-YYYYMMDD` instead of `CUST-DOB-YYYY-MM-DD` (same PIC X(10)).

### CVCUS01Y — `app/cpy/CVCUS01Y.cpy` — computed length **500** bytes

Used by: CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COPAUA0C, COPAUS0C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CUSTOMER-RECORD | 01 |  | Group | 500-byte CUSTDAT KSDS record image. | none in code |
| CUST-ID | 05 | 9(09) | Zoned numeric | Customer id (KSDS key). | none in code (key; obtained via XREF/commarea). |
| CUST-FIRST-NAME | 05 | X(25) | Alphanumeric | First name. | COACTUPC `1225-EDIT-ALPHA-REQD`: required, alpha only. |
| CUST-MIDDLE-NAME | 05 | X(25) | Alphanumeric | Middle name. | COACTUPC `1235-EDIT-ALPHA-OPT`: optional, alpha only. |
| CUST-LAST-NAME | 05 | X(25) | Alphanumeric | Last name. | COACTUPC `1225-EDIT-ALPHA-REQD`: required, alpha only. |
| CUST-ADDR-LINE-1 | 05 | X(50) | Alphanumeric | Address line 1. | COACTUPC `1215-EDIT-MANDATORY`: must be supplied. |
| CUST-ADDR-LINE-2 | 05 | X(50) | Alphanumeric | Address line 2. | none in code — edit commented out in COACTUPC `1200-EDIT-MAP-INPUTS`. |
| CUST-ADDR-LINE-3 | 05 | X(50) | Alphanumeric | Address line 3. | COACTUPC `1225-EDIT-ALPHA-REQD` (city): required, alpha only. |
| CUST-ADDR-STATE-CD | 05 | X(02) | Alphanumeric | US state code. | COACTUPC `1225-EDIT-ALPHA-REQD` + `1270-EDIT-US-STATE-CD`: must be in CSLKPCDY `VALID-US-STATE-CODE` list; combined with zip in `1280-EDIT-US-STATE-ZIP-CD` (`VALID-US-STATE-ZIP-CD2-COMBO`). |
| CUST-ADDR-COUNTRY-CD | 05 | X(03) | Alphanumeric | Country code. | COACTUPC `1225-EDIT-ALPHA-REQD`: required, alpha only. |
| CUST-ADDR-ZIP | 05 | X(10) | Alphanumeric | ZIP code. | COACTUPC `1245-EDIT-NUM-REQD` (5 digits) + `1280-EDIT-US-STATE-ZIP-CD` state+zip2 combination must be valid. |
| CUST-PHONE-NUM-1 | 05 | X(15) | Alphanumeric | Primary phone (US format). | COACTUPC `1260-EDIT-US-PHONE-NUM`: optional; if present, area code numeric/non-zero and in NANP table (CSLKPCDY), prefix+line numeric. |
| CUST-PHONE-NUM-2 | 05 | X(15) | Alphanumeric | Secondary phone. | COACTUPC `1260-EDIT-US-PHONE-NUM`: same as phone 1. |
| CUST-SSN | 05 | 9(09) | Zoned numeric | Social security number. | COACTUPC `1265-EDIT-US-SSN`: 9-digit numeric (3+2+4 parts). |
| CUST-GOVT-ISSUED-ID | 05 | X(20) | Alphanumeric | Government-issued id. | none in code |
| CUST-DOB-YYYY-MM-DD | 05 | X(10) | Alphanumeric | Date of birth (YYYY-MM-DD). | COACTUPC: `EDIT-DATE-CCYYMMDD` then `EDIT-DATE-OF-BIRTH` (CSUTLDPY): valid date, not in the future. |
| CUST-EFT-ACCOUNT-ID | 05 | X(10) | Alphanumeric | EFT account id. | COACTUPC `1245-EDIT-NUM-REQD`: 10-digit numeric. |
| CUST-PRI-CARD-HOLDER-IND | 05 | X(01) | Alphanumeric | Primary card-holder indicator (Y/N). | COACTUPC `1220-EDIT-YESNO`: 'Y' or 'N'. |
| CUST-FICO-CREDIT-SCORE | 05 | 9(03) | Zoned numeric | FICO credit score. | COACTUPC `1245-EDIT-NUM-REQD` (3 digits) + `1275-EDIT-FICO-SCORE`: must be 300–850 (`FICO-RANGE-IS-VALID`). |
| FILLER | 05 | X(168) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- COACTUPC edits: `CUST-ADDR-STATE-CD` must be in CSLKPCDY `VALID-US-STATE-CODE` (`1270-EDIT-US-STATE-CD`); state+zip2 combo checked in `1280-EDIT-US-STATE-ZIP-CD`; phones checked against NANP area-code table (`1260-EDIT-US-PHONE-NUM`); `CUST-SSN` numeric (`1265-EDIT-US-SSN`); `CUST-FICO-CREDIT-SCORE` numeric 3 digits (`1275-EDIT-FICO-SCORE`); DOB validated via `EDIT-DATE-OF-BIRTH` (not in the future).

### CUSTREC — `app/cpy/CUSTREC.cpy` — computed length **500** bytes

Used by: CBSTM03A

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CUSTOMER-RECORD | 01 |  | Group | 500-byte CUSTDAT KSDS record image. | none in code — read verbatim by CBSTM03A, no edits. |
| CUST-ID | 05 | 9(09) | Zoned numeric | Customer id (KSDS key). | none in code |
| CUST-FIRST-NAME | 05 | X(25) | Alphanumeric | First name. | none in code |
| CUST-MIDDLE-NAME | 05 | X(25) | Alphanumeric | Middle name. | none in code |
| CUST-LAST-NAME | 05 | X(25) | Alphanumeric | Last name. | none in code |
| CUST-ADDR-LINE-1 | 05 | X(50) | Alphanumeric | Address line 1. | none in code |
| CUST-ADDR-LINE-2 | 05 | X(50) | Alphanumeric | Address line 2. | none in code |
| CUST-ADDR-LINE-3 | 05 | X(50) | Alphanumeric | Address line 3. | none in code |
| CUST-ADDR-STATE-CD | 05 | X(02) | Alphanumeric | US state code. | none in code |
| CUST-ADDR-COUNTRY-CD | 05 | X(03) | Alphanumeric | Country code. | none in code |
| CUST-ADDR-ZIP | 05 | X(10) | Alphanumeric | ZIP code. | none in code |
| CUST-PHONE-NUM-1 | 05 | X(15) | Alphanumeric | Primary phone (US format). | none in code |
| CUST-PHONE-NUM-2 | 05 | X(15) | Alphanumeric | Secondary phone. | none in code |
| CUST-SSN | 05 | 9(09) | Zoned numeric | Social security number. | none in code |
| CUST-GOVT-ISSUED-ID | 05 | X(20) | Alphanumeric | Government-issued id. | none in code |
| CUST-DOB-YYYYMMDD | 05 | X(10) | Alphanumeric | Date of birth — field name differs from CVCUS01Y only; format not distinguishable from the copybook (same PIC X(10)), and CBSTM03A does not parse it. | none in code — CBSTM03A does not parse DOB. |
| CUST-EFT-ACCOUNT-ID | 05 | X(10) | Alphanumeric | EFT account id. | none in code |
| CUST-PRI-CARD-HOLDER-IND | 05 | X(01) | Alphanumeric | Primary card-holder indicator (Y/N). | none in code |
| CUST-FICO-CREDIT-SCORE | 05 | 9(03) | Zoned numeric | FICO credit score. | none in code |
| FILLER | 05 | X(168) | Alphanumeric | Unused padding to record length. | none in code |

## 3. Card — CVACT02Y

CARDDAT layout (RECLN 150, key CARD-NUM; AIX CARDAIX on CARD-ACCT-ID).

### CVACT02Y — `app/cpy/CVACT02Y.cpy` — computed length **150** bytes

Used by: CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C, COTRTLIC

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CARD-RECORD | 01 |  | Group | 150-byte CARDDAT KSDS record image. | none in code |
| CARD-NUM | 05 | X(16) | Alphanumeric | Card number (KSDS key). | COCRDUPC `1220-EDIT-CARD`: if supplied must be a 16-digit number; key read of CARDDAT. |
| CARD-ACCT-ID | 05 | 9(11) | Zoned numeric | Owning account id; CARDAIX alternate-index key. | COCRDUPC `1210-EDIT-ACCOUNT`: non-zero 11-digit number; verified against ACCTDAT/CXACAIX. |
| CARD-CVV-CD | 05 | 9(03) | Zoned numeric | Card verification value. | none in code |
| CARD-EMBOSSED-NAME | 05 | X(50) | Alphanumeric | Name embossed on card. | COCRDUPC `1230-EDIT-NAME`: required name edit. |
| CARD-EXPIRAION-DATE | 05 | X(10) | Alphanumeric | Card expiry date. | COCRDUPC `1250-EDIT-EXPIRY-MON` (month 1–12, `VALID-MONTH`) and `1260-EDIT-EXPIRY-YEAR` (1950–2099, `VALID-YEAR`). |
| CARD-ACTIVE-STATUS | 05 | X(01) | Alphanumeric | Card status flag. | COCRDUPC `1240-EDIT-CARDSTATUS`: 'Y' or 'N' (`FLG-YES-NO-VALID`). |
| FILLER | 05 | X(59) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- COCRDUPC edits: `1220-EDIT-CARD` (numeric), `1230-EDIT-NAME`, `1240-EDIT-CARDSTATUS` (Y/N), `1250-EDIT-EXPIRY-MON` (1-12), `1260-EDIT-EXPIRY-YEAR`.

## 4. Card/Account/Customer cross-reference — CVACT03Y

CARDXREF/CCXREF layout (RECLN 50, key XREF-CARD-NUM; AIX CXACAIX on XREF-ACCT-ID).

### CVACT03Y — `app/cpy/CVACT03Y.cpy` — computed length **50** bytes

Used by: CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COPAUA0C, COPAUS0C, COTRN02C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CARD-XREF-RECORD | 01 |  | Group | 50-byte CARDXREF/CCXREF KSDS record image. | none in code |
| XREF-CARD-NUM | 05 | X(16) | Alphanumeric | Card number (key). | CBTRN02C `1500-A-LOOKUP-XREF`: primary-key read of XREF file; miss = reason 100 'INVALID CARD NUMBER FOUND'. COTRN02C `VALIDATE-INPUT-KEY-FIELDS`: card num must be numeric then CCXREF read. |
| XREF-CUST-ID | 05 | 9(09) | Zoned numeric | Customer id. | none in code |
| XREF-ACCT-ID | 05 | 9(11) | Zoned numeric | Account id; CXACAIX AIX key for account→cards lookup. | CXACAIX AIX key — account→card lookup used by COBIL00C, COACTUPC/COACTVWC, COTRN02C, COPAUS0C. |
| FILLER | 05 | X(14) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- Two access paths: CCXREF base KSDS keyed on XREF-CARD-NUM (read by COTRN02C `VALIDATE-INPUT-KEY-FIELDS` when a card number is entered, COPAUA0C `5100-READ-XREF-RECORD`, and CBTRN02C `1500-A-LOOKUP-XREF` which reads the XREF file by card number — miss = reason 100 'INVALID CARD NUMBER FOUND'); and CXACAIX alternate index keyed on XREF-ACCT-ID (read by COBIL00C, COACTUPC/COACTVWC, COTRN02C when an account id is entered, and COPAUS0C).

## 5. Transaction — CVTRA05Y, CVTRA06Y, CVTRA07Y

CVTRA05Y = posted TRANSACT record (RECLN 350). CVTRA06Y = DALYTRAN input record — identical field-for-field with prefix DALYTRAN- instead of TRAN-. CVTRA07Y = CBTRN03C report-line work layouts.

### CVTRA05Y — `app/cpy/CVTRA05Y.cpy` — computed length **350** bytes

Used by: CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| TRAN-RECORD | 01 |  | Group | 350-byte TRANSACT KSDS record image. | none in code |
| TRAN-ID | 05 | X(16) | Alphanumeric | Transaction id (key; generated from last record +1 by COTRN02C). | Generated by COTRN02C as last TRAN-ID + 1 (STARTBR/READPREV on TRANSACT). |
| TRAN-TYPE-CD | 05 | X(02) | Alphanumeric | Transaction type code. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required and numeric. |
| TRAN-CAT-CD | 05 | 9(04) | Zoned numeric | Transaction category code. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required and numeric. |
| TRAN-SOURCE | 05 | X(10) | Alphanumeric | Transaction source (e.g. POS/online). | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required ('Source can NOT be empty'). |
| TRAN-DESC | 05 | X(100) | Alphanumeric | Description. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required. |
| TRAN-AMT | 05 | S9(09)V99 | Signed zoned | Amount, signed. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required; must match `+99999999.99` picture. |
| TRAN-MERCHANT-ID | 05 | 9(09) | Zoned numeric | Merchant id. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required. |
| TRAN-MERCHANT-NAME | 05 | X(50) | Alphanumeric | Merchant name. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required. |
| TRAN-MERCHANT-CITY | 05 | X(50) | Alphanumeric | Merchant city. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required. |
| TRAN-MERCHANT-ZIP | 05 | X(10) | Alphanumeric | Merchant zip. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required. |
| TRAN-CARD-NUM | 05 | X(16) | Alphanumeric | Card number used. | COTRN02C `VALIDATE-INPUT-KEY-FIELDS`: must be numeric (or account id given and resolved via CXACAIX); resolved via CCXREF. |
| TRAN-ORIG-TS | 05 | X(26) | Alphanumeric | Original timestamp. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required; date parts validated as YYYY-MM-DD via CSUTLDTC. |
| TRAN-PROC-TS | 05 | X(26) | Alphanumeric | Processing timestamp. | COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: required; same date validation. |
| FILLER | 05 | X(20) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- COTRN02C `VALIDATE-INPUT-DATA-FIELDS`: type & category numeric, amount must match `+99999999.99` picture (sign, 8 digits, '.', 2 digits), dates numeric YYYY-MM-DD parts; new TRAN-ID = last TRAN-ID + 1 (`STARTBR/READPREV`).

### CVTRA06Y — `app/cpy/CVTRA06Y.cpy` — computed length **350** bytes

Used by: CBTRN01C, CBTRN02C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| DALYTRAN-RECORD | 01 |  | Group | 350-byte DALYTRAN sequential input record image (same layout as TRAN-RECORD, different prefix). | CBTRN02C `1500-VALIDATE-TRAN` chain; failures → DALYREJS via `2500-WRITE-REJECT-REC`. |
| DALYTRAN-ID | 05 | X(16) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-TYPE-CD | 05 | X(02) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-CAT-CD | 05 | 9(04) | Zoned numeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-SOURCE | 05 | X(10) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-DESC | 05 | X(100) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-AMT | 05 | S9(09)V99 | Signed zoned | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | CBTRN02C `1500-B-LOOKUP-ACCT`: cyc-credit − cyc-debit + amt must not exceed ACCT-CREDIT-LIMIT (reason 102). |
| DALYTRAN-MERCHANT-ID | 05 | 9(09) | Zoned numeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-MERCHANT-NAME | 05 | X(50) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-MERCHANT-CITY | 05 | X(50) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-MERCHANT-ZIP | 05 | X(10) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| DALYTRAN-CARD-NUM | 05 | X(16) | Alphanumeric | Card number (KSDS key). | CBTRN02C `1500-A-LOOKUP-XREF`: must exist in XREF file else reason 100. |
| DALYTRAN-ORIG-TS | 05 | X(26) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | CBTRN02C `1500-B`: date part must be <= ACCT-EXPIRAION-DATE (reason 103). |
| DALYTRAN-PROC-TS | 05 | X(26) | Alphanumeric | DALYTRAN input field (same semantics as CVTRA05Y counterpart). | none in code |
| FILLER | 05 | X(20) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- CBTRN02C `1500-VALIDATE-TRAN` chain: xref must exist, account must exist (101), over-limit (102), expiry (103); failures written to DALYREJS via `2500-WRITE-REJECT-REC`.

### CVTRA07Y — `app/cpy/CVTRA07Y.cpy` — computed length **812** bytes

Used by: CBTRN03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| REPORT-NAME-HEADER | 01 |  | Group | Report title line layout. | none in code |
| REPT-SHORT-NAME | 05 | X(38) VALUE 'DALYREPT' | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| REPT-LONG-NAME | 05 | X(41) VALUE 'DAILY TRANSACTION REPORT' | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| REPT-DATE-HEADER | 05 | X(12) VALUE 'DATE RANGE: ' | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| REPT-START-DATE | 05 | X(10) VALUE SPACES | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(04) VALUE ' TO ' | Alphanumeric | Unused padding to record length. | none in code |
| REPT-END-DATE | 05 | X(10) VALUE SPACES | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| TRANSACTION-DETAIL-REPORT | 01 |  | Group | One report detail line. | none in code |
| TRAN-REPORT-TRANS-ID | 05 | X(16) | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(01) VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |
| TRAN-REPORT-ACCOUNT-ID | 05 | X(11) | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(01) VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |
| TRAN-REPORT-TYPE-CD | 05 | X(02) | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(01) VALUE '-' | Alphanumeric | Unused padding to record length. | none in code |
| TRAN-REPORT-TYPE-DESC | 05 | X(15) | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(01) VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |
| TRAN-REPORT-CAT-CD | 05 | 9(04) | Zoned numeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(01) VALUE '-' | Alphanumeric | Unused padding to record length. | none in code |
| TRAN-REPORT-CAT-DESC | 05 | X(29) | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(01) VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |
| TRAN-REPORT-SOURCE | 05 | X(10) | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(04) VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |
| TRAN-REPORT-AMT | 05 | -ZZZ,ZZZ,ZZZ.ZZ | Edited display | Report line field used by CBTRN03C print logic. | none in code |
| FILLER | 05 | X(02) VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |
| TRANSACTION-HEADER-1 | 01 |  | Group | Column-heading line. | none in code |
| FILLER | 05 | X(17) VALUE 'TRANSACTION ID' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(12) VALUE 'ACCOUNT ID' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(19) VALUE 'TRANSACTION TYPE' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(35) VALUE 'TRAN CATEGORY' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(14) VALUE 'TRAN SOURCE' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(16) VALUE '        AMOUNT' | Alphanumeric | Unused padding to record length. | none in code |
| TRANSACTION-HEADER-2 | 01 | X(133) VALUE ALL '-' | Alphanumeric | Report line field used by CBTRN03C print logic. | none in code |
| REPORT-PAGE-TOTALS | 01 |  | Group | Page-total line. | none in code |
| FILLER | 05 | X(11) VALUE 'PAGE TOTAL' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(86) VALUE ALL '.' | Alphanumeric | Unused padding to record length. | none in code |
| REPT-PAGE-TOTAL | 05 | +ZZZ,ZZZ,ZZZ.ZZ | Edited display | Report line field used by CBTRN03C print logic. | none in code |
| REPORT-ACCOUNT-TOTALS | 01 |  | Group | Account-total line. | none in code |
| FILLER | 05 | X(13) VALUE 'ACCOUNT TOTAL' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(84) VALUE ALL '.' | Alphanumeric | Unused padding to record length. | none in code |
| REPT-ACCOUNT-TOTAL | 05 | +ZZZ,ZZZ,ZZZ.ZZ | Edited display | Report line field used by CBTRN03C print logic. | none in code |
| REPORT-GRAND-TOTALS | 01 |  | Group | Grand-total line. | none in code |
| FILLER | 05 | X(11) VALUE 'GRAND TOTAL' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 05 | X(86) VALUE ALL '.' | Alphanumeric | Unused padding to record length. | none in code |
| REPT-GRAND-TOTAL | 05 | +ZZZ,ZZZ,ZZZ.ZZ | Edited display | Report line field used by CBTRN03C print logic. | none in code |

## 6. Transaction reference data — CVTRA01Y … CVTRA04Y

Category balances (TCATBALF), disclosure groups (DISCGRP), transaction types (TRANTYPE) and categories (TRANCATG).

### CVTRA01Y — `app/cpy/CVTRA01Y.cpy` — computed length **50** bytes

Used by: CBACT04C, CBTRN02C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| TRAN-CAT-BAL-RECORD | 01 |  | Group | 150-byte TCATBALF KSDS record image. | none in code |
| TRAN-CAT-KEY | 05 |  | Group | Composite KSDS key: account id + transaction type + category. | none in code |
| TRANCAT-ACCT-ID | 10 | 9(11) | Zoned numeric | Account id (part of key). | none in code |
| TRANCAT-TYPE-CD | 10 | X(02) | Alphanumeric | Transaction type code (part of key). | none in code |
| TRANCAT-CD | 10 | 9(04) | Zoned numeric | Transaction category code (part of key). | none in code |
| TRAN-CAT-BAL | 05 | S9(09)V99 | Signed zoned | Balance accumulated for this acct/type/category. | Read by CBACT04C `1300-COMPUTE-INTEREST`: interest = (TRAN-CAT-BAL × DIS-INT-RATE) / 1200. |
| FILLER | 05 | X(22) | Alphanumeric | Unused padding to record length. | none in code |

### CVTRA02Y — `app/cpy/CVTRA02Y.cpy` — computed length **50** bytes

Used by: CBACT04C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| DIS-GROUP-RECORD | 01 |  | Group | 50-byte DISCGRP KSDS record image. | none in code |
| DIS-GROUP-KEY | 05 |  | Group | Composite KSDS key: account group + transaction type + category. | none in code |
| DIS-ACCT-GROUP-ID | 10 | X(10) | Alphanumeric | Account disclosure group (part of key). | none in code |
| DIS-TRAN-TYPE-CD | 10 | X(02) | Alphanumeric | Transaction type (part of key). | none in code |
| DIS-TRAN-CAT-CD | 10 | 9(04) | Zoned numeric | Transaction category (part of key). | none in code |
| DIS-INT-RATE | 05 | S9(04)V99 | Signed zoned | Interest rate for this group/type/category. | Looked up by CBACT04C `1200-GET-INTEREST-RATE`; on miss `1200-A-GET-DEFAULT-INT-RATE` uses a default. |
| FILLER | 05 | X(28) | Alphanumeric | Unused padding to record length. | none in code |

### CVTRA03Y — `app/cpy/CVTRA03Y.cpy` — computed length **60** bytes

Used by: CBTRN03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| TRAN-TYPE-RECORD | 01 |  | Group | 60-byte TRANTYPE KSDS record image. | none in code |
| TRAN-TYPE | 05 | X(02) | Alphanumeric | Transaction type code (key). | none in code |
| TRAN-TYPE-DESC | 05 | X(50) | Alphanumeric | Description. | none in code |
| FILLER | 05 | X(08) | Alphanumeric | Unused padding to record length. | none in code |

### CVTRA04Y — `app/cpy/CVTRA04Y.cpy` — computed length **60** bytes

Used by: CBTRN03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| TRAN-CAT-RECORD | 01 |  | Group | 60-byte TRANCATG KSDS record image. | none in code |
| TRAN-CAT-KEY | 05 |  | Group | Composite KSDS key: account id + transaction type + category. | none in code |
| TRAN-TYPE-CD | 10 | X(02) | Alphanumeric | Transaction type code. | none in code |
| TRAN-CAT-CD | 10 | 9(04) | Zoned numeric | Transaction category code. | none in code |
| TRAN-CAT-TYPE-DESC | 05 | X(50) | Alphanumeric | Category description. | none in code |
| FILLER | 05 | X(04) | Alphanumeric | Unused padding to record length. | none in code |

## 7. Security / User — CSUSR01Y

USRSEC record (80 bytes).

### CSUSR01Y — `app/cpy/CSUSR01Y.cpy` — computed length **80** bytes

Used by: COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COTRTLIC, COTRTUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| SEC-USER-DATA | 01 |  | Group | 80-byte USRSEC KSDS record image. | none in code |
| SEC-USR-ID | 05 | X(08) | Alphanumeric | User id (key). | COUSR01C `PROCESS-ENTER-KEY`: mandatory (non-spaces/low-values). COSGN00C compares id against USRSEC. |
| SEC-USR-FNAME | 05 | X(20) | Alphanumeric | First name. | COUSR01C `PROCESS-ENTER-KEY`: mandatory. |
| SEC-USR-LNAME | 05 | X(20) | Alphanumeric | Last name. | COUSR01C `PROCESS-ENTER-KEY`: mandatory. |
| SEC-USR-PWD | 05 | X(08) | Alphanumeric | Password (plaintext in demo). | COUSR01C `PROCESS-ENTER-KEY`: mandatory. COSGN00C compares entered password to this field. |
| SEC-USR-TYPE | 05 | X(01) | Alphanumeric | 'A' admin / 'U' regular user. | COUSR01C `PROCESS-ENTER-KEY`: mandatory; seed data uses 'A' admin / 'U' user. |
| SEC-USR-FILLER | 05 | X(23) | Alphanumeric | Unused padding to 80-byte record length. | none in code |

**Validation / usage notes**
- COSGN00C `READ-USER-SEC-FILE` compares entered id/password. COUSR01C requires all five fields non-blank/non-low-values (mandatory check in PROCESS-ENTER-KEY); `SEC-USR-TYPE` expected 'A' or 'U' (seed data).

## 8. Pending Authorization (IMS) — CIPAUSMY, CIPAUDTY

I/O areas for the two segments of IMS HIDAM DB DBPAUTP0: root PAUTSUM0 (100 B) and child PAUTDTL1 (200 B).

### CIPAUSMY — `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy` — computed length **100** bytes

Used by: CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, DBUNLDGS, PAUDBLOD, PAUDBUNL

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| PA-ACCT-ID | 05 | S9(11) COMP-3 | Packed decimal (COMP-3) | Account id (IMS segment seq field ACCNTID, packed). | IMS root seq field ACCNTID; set from XREF-ACCT-ID by COPAUA0C `8500-INSERT-AUTH`. |
| PA-CUST-ID | 05 | 9(09) | Zoned numeric | Customer id. | none in code |
| PA-AUTH-STATUS | 05 | X(01) | Alphanumeric | Summary auth status. | none in code |
| PA-ACCOUNT-STATUS | 05 | X(02) OCCURS 5 TIMES | Alphanumeric | Up to 5 account status codes. | none in code |
| PA-CREDIT-LIMIT | 05 | S9(09)V99 COMP-3 | Packed decimal (COMP-3) | Credit limit snapshot. | COPAUA0C `6000-MAKE-DECISION`: available credit = PA-CREDIT-LIMIT − PA-CREDIT-BALANCE. |
| PA-CASH-LIMIT | 05 | S9(09)V99 COMP-3 | Packed decimal (COMP-3) | Cash limit. | none in code |
| PA-CREDIT-BALANCE | 05 | S9(09)V99 COMP-3 | Packed decimal (COMP-3) | Credit balance. | See PA-CREDIT-LIMIT; incremented by approved amt in `8400-UPDATE-SUMMARY`. |
| PA-CASH-BALANCE | 05 | S9(09)V99 COMP-3 | Packed decimal (COMP-3) | Cash balance. | none in code |
| PA-APPROVED-AUTH-CNT | 05 | S9(04) COMP | Binary (COMP) | Count of approved auths. | COPAUA0C `8400-UPDATE-SUMMARY` +1 on approval; CBPAUP0C `4000` −1 when auth expires. |
| PA-DECLINED-AUTH-CNT | 05 | S9(04) COMP | Binary (COMP) | Count of declined auths. | COPAUA0C `8400` +1 on decline; CBPAUP0C `4000` −1 on expiry. |
| PA-APPROVED-AUTH-AMT | 05 | S9(09)V99 COMP-3 | Packed decimal (COMP-3) | Total approved amount. | COPAUA0C `8400` adds approved amt; CBPAUP0C `4000` subtracts on expiry. |
| PA-DECLINED-AUTH-AMT | 05 | S9(09)V99 COMP-3 | Packed decimal (COMP-3) | Total declined amount. | COPAUA0C `8400` adds requested amt on decline; CBPAUP0C `4000` subtracts on expiry. |
| FILLER | 05 | X(34) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- Updated by COPAUA0C `8400-UPDATE-SUMMARY` (approved/declined counts & amounts) and COPAUS0C/COPAUS1C; root key ACCNTID = PA-ACCT-ID (packed S9(11) COMP-3).

### CIPAUDTY — `app/app-authorization-ims-db2-mq/cpy/CIPAUDTY.cpy` — computed length **200** bytes

Used by: CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, DBUNLDGS, PAUDBLOD, PAUDBUNL

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| PA-AUTHORIZATION-KEY | 05 |  | Group | Descending sort key — date stored as 99999−YYDDD and time as 999999999−HHMMSSTTT so newest auths retrieve first (COPAUA0C 8500). | none in code |
| PA-AUTH-DATE-9C | 10 | S9(05) COMP-3 | Packed decimal (COMP-3) | Auth date, packed (part of detail key). | COPAUA0C `8500-INSERT-AUTH`: = 99999 − YYDDD (descending key). CBPAUP0C `4000-CHECK-IF-EXPIRED`: age (CURRENT-YYDDD − (99999 − this)) >= WS-EXPIRY-DAYS (PARM, default 5) → deleted. |
| PA-AUTH-TIME-9C | 10 | S9(09) COMP-3 | Packed decimal (COMP-3) | Auth time, packed. | COPAUA0C `8500`: = 999999999 − time-with-ms (descending key). |
| PA-AUTH-ORIG-DATE | 05 | X(06) | Alphanumeric | Original auth date. | Copied from request (COPAUA0C `8500`). |
| PA-AUTH-ORIG-TIME | 05 | X(06) | Alphanumeric | Original auth time. | Copied from request (COPAUA0C `8500`). |
| PA-CARD-NUM | 05 | X(16) | Alphanumeric | Card number. | Copied from request (COPAUA0C `8500`). |
| PA-AUTH-TYPE | 05 | X(04) | Alphanumeric | Authorization type. | Copied from request. |
| PA-CARD-EXPIRY-DATE | 05 | X(04) | Alphanumeric | Card expiry (MMYY). | Copied from request. |
| PA-MESSAGE-TYPE | 05 | X(06) | Alphanumeric | Message type (e.g. 0100). | Copied from request. |
| PA-MESSAGE-SOURCE | 05 | X(06) | Alphanumeric | Message source. | Copied from request. |
| PA-AUTH-ID-CODE | 05 | X(06) | Alphanumeric | Approval code. | COPAUA0C `6000-MAKE-DECISION`: set to request auth time. |
| PA-AUTH-RESP-CODE | 05 | X(02) | Alphanumeric | Response code ('00' approved/'05' declined). | COPAUA0C `6000-MAKE-DECISION`: '00' approved, '05' declined (amount > available credit, or account/xref not found). |
| PA-AUTH-APPROVED | 88 |  VALUE '00' | Condition (88) | True when PA-AUTH-RESP-CODE = '00' (approved). | Set true when decision approves (resp '00'); drives PA-MATCH-PENDING in `8500`. |
| PA-AUTH-RESP-REASON | 05 | X(04) | Alphanumeric | Response reason code. | COPAUA0C `6000`: '0000' approved; declines: '3100' card/acct/cust not found, '4100' insufficient funds; '4200'/'4300'/'5100'/'5200' coded but their condition flags are never set (dead branches); other '9000'. |
| PA-PROCESSING-CODE | 05 | 9(06) | Zoned numeric | Processing code. | Copied from request. |
| PA-TRANSACTION-AMT | 05 | S9(10)V99 COMP-3 | Packed decimal (COMP-3) | Requested amount. | COPAUA0C `2100-EXTRACT-REQUEST-MSG`: FUNCTION NUMVAL of edited +9(10).99 field. |
| PA-APPROVED-AMT | 05 | S9(10)V99 COMP-3 | Packed decimal (COMP-3) | Approved amount. | COPAUA0C `6000`: = transaction amount if approved, else 0. |
| PA-MERCHANT-CATAGORY-CODE | 05 | X(04) | Alphanumeric | Merchant category code. | Copied from request. |
| PA-ACQR-COUNTRY-CODE | 05 | X(03) | Alphanumeric | Acquirer country. | Copied from request. |
| PA-POS-ENTRY-MODE | 05 | 9(02) | Zoned numeric | POS entry mode. | Copied from request. |
| PA-MERCHANT-ID | 05 | X(15) | Alphanumeric | Merchant id. | Copied from request. |
| PA-MERCHANT-NAME | 05 | X(22) | Alphanumeric | Merchant name. | Copied from request. |
| PA-MERCHANT-CITY | 05 | X(13) | Alphanumeric | Merchant city. | Copied from request. |
| PA-MERCHANT-STATE | 05 | X(02) | Alphanumeric | Merchant state. | Copied from request. |
| PA-MERCHANT-ZIP | 05 | X(09) | Alphanumeric | Merchant zip. | Copied from request. |
| PA-TRANSACTION-ID | 05 | X(15) | Alphanumeric | Transaction id. | Copied from request. |
| PA-MATCH-STATUS | 05 | X(01) | Alphanumeric | Match status P/D/E/M. | COPAUA0C `8500`: 'P' when approved, 'D' when declined; 'E'/'M' never set by any program. |
| PA-MATCH-PENDING | 88 |  VALUE 'P' | Condition (88) | 'P' — approved auth awaiting matching posted transaction. | Set by COPAUA0C `8500` on approved auths. |
| PA-MATCH-AUTH-DECLINED | 88 |  VALUE 'D' | Condition (88) | 'D' — declined auth (no posting expected). | Set by COPAUA0C `8500` on declined auths. |
| PA-MATCH-PENDING-EXPIRED | 88 |  VALUE 'E' | Condition (88) | 'E' — defined but never set by any program; expired auths are deleted by CBPAUP0C instead. | Never set — CBPAUP0C deletes expired auths instead of flagging 'E'. |
| PA-MATCHED-WITH-TRAN | 88 |  VALUE 'M' | Condition (88) | 'M' — defined but never set by any program. | Never set by any program. |
| PA-AUTH-FRAUD | 05 | X(01) | Alphanumeric | Fraud flag ('F' confirmed, 'R' removed). | 'F' set by COPAUS1C mark-fraud flow (COPAUS2C writes DB2 AUTHFRDS); 'R' removes. Otherwise space. |
| PA-FRAUD-CONFIRMED | 88 |  VALUE 'F' | Condition (88) | 'F' — fraud confirmed; written to DB2 AUTHFRDS by COPAUS2C. | Set via COPAUS1C update path. |
| PA-FRAUD-REMOVED | 88 |  VALUE 'R' | Condition (88) | 'R' — fraud flag removed. | Set via COPAUS1C update path. |
| PA-FRAUD-RPT-DATE | 05 | X(08) | Alphanumeric | Fraud report date. | none in code |
| FILLER | 05 | X(17) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- `PA-AUTH-RESP-CODE`: '00'=approved, '05'=declined (COPAUA0C `6000-MAKE-DECISION`; decline when amt > available credit). `PA-MATCH-STATUS` 88s: P pending, D declined, E expired, M matched. `PA-AUTH-FRAUD`: 'F'/'R' set by COPAUS1C `MARK-AUTH-FRAUD` → COPAUS2C DB2 write. CBPAUP0C deletes segments where `99999 − PA-AUTH-DATE-9C` age ≥ WS-EXPIRY-DAYS (PARM, default 5).

## 9. Authorization messaging — CCPAURQY, CCPAURLY, CCPAUERY

MQ message layouts for the authorization request, response and error log used by COPAUA0C.

### CCPAURQY — `app/app-authorization-ims-db2-mq/cpy/CCPAURQY.cpy` — computed length **153** bytes

Used by: COPAUA0C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| PA-RQ-AUTH-DATE | 05 | X(06) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | COPAUA0C `2100-EXTRACT-REQUEST-MSG`: comma-delimited UNSTRING; no field-level edits. |
| PA-RQ-AUTH-TIME | 05 | X(06) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-CARD-NUM | 05 | X(16) | Alphanumeric | Card number (KSDS key). | Used for CCXREF lookup `5100-READ-XREF-RECORD`; miss → decline reason 3100. |
| PA-RQ-AUTH-TYPE | 05 | X(04) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-CARD-EXPIRY-DATE | 05 | X(04) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-MESSAGE-TYPE | 05 | X(06) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-MESSAGE-SOURCE | 05 | X(06) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-PROCESSING-CODE | 05 | 9(06) | Zoned numeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-TRANSACTION-AMT | 05 | +9(10).99 | Edited display | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | Edited picture `+9(10).99`; converted with FUNCTION NUMVAL in `2100`; drives limit check in `6000-MAKE-DECISION`. |
| PA-RQ-MERCHANT-CATAGORY-CODE | 05 | X(04) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-ACQR-COUNTRY-CODE | 05 | X(03) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-POS-ENTRY-MODE | 05 | 9(02) | Zoned numeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-MERCHANT-ID | 05 | X(15) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-MERCHANT-NAME | 05 | X(22) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-MERCHANT-CITY | 05 | X(13) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-MERCHANT-STATE | 05 | X(02) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-MERCHANT-ZIP | 05 | X(09) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |
| PA-RQ-TRANSACTION-ID | 05 | X(15) | Alphanumeric | MQ request field — mirrored into CIPAUDTY segment by COPAUA0C 8500. | none in code |

**Validation / usage notes**
- MQ request; `PA-RQ-TRANSACTION-AMT` is edited `+9(10).99`. Decision rules in COPAUA0C `6000-MAKE-DECISION`.

### CCPAURLY — `app/app-authorization-ims-db2-mq/cpy/CCPAURLY.cpy` — computed length **57** bytes

Used by: COPAUA0C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| PA-RL-CARD-NUM | 05 | X(16) | Alphanumeric | MQ reply field populated by COPAUA0C 6000-MAKE-DECISION. | Echo of request card number (COPAUA0C `6000`). |
| PA-RL-TRANSACTION-ID | 05 | X(15) | Alphanumeric | MQ reply field populated by COPAUA0C 6000-MAKE-DECISION. | Echo of request transaction id. |
| PA-RL-AUTH-ID-CODE | 05 | X(06) | Alphanumeric | MQ reply field populated by COPAUA0C 6000-MAKE-DECISION. | Set to request auth time (`6000`). |
| PA-RL-AUTH-RESP-CODE | 05 | X(02) | Alphanumeric | MQ reply field populated by COPAUA0C 6000-MAKE-DECISION. | '00' approved / '05' declined (`6000-MAKE-DECISION`). |
| PA-RL-AUTH-RESP-REASON | 05 | X(04) | Alphanumeric | MQ reply field populated by COPAUA0C 6000-MAKE-DECISION. | '0000' approved; '3100' not found; '4100' insufficient funds; '4200/4300/5100/5200' coded but unreachable; else '9000'. |
| PA-RL-APPROVED-AMT | 05 | +9(10).99 | Edited display | MQ reply field populated by COPAUA0C 6000-MAKE-DECISION. | = request amount if approved else 0 (`6000`). |

### CCPAUERY — `app/app-authorization-ims-db2-mq/cpy/CCPAUERY.cpy` — computed length **122** bytes

Used by: COPAUA0C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| ERROR-LOG-RECORD | 01 |  | Group | MQ error-queue message layout. | none in code |
| ERR-DATE | 05 | X(06) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-TIME | 05 | X(06) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-APPLICATION | 05 | X(08) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-PROGRAM | 05 | X(08) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-LOCATION | 05 | X(04) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-LEVEL | 05 | X(01) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-LOG | 88 |  VALUE 'L' | Condition (88) | Condition name — true when the field it is defined under equals 'L'. | none in code |
| ERR-INFO | 88 |  VALUE 'I' | Condition (88) | Condition name — true when the field it is defined under equals 'I'. | none in code |
| ERR-WARNING | 88 |  VALUE 'W' | Condition (88) | Condition name — true when the field it is defined under equals 'W'. | none in code |
| ERR-CRITICAL | 88 |  VALUE 'C' | Condition (88) | Condition name — true when the field it is defined under equals 'C'. | none in code |
| ERR-SUBSYSTEM | 05 | X(01) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-APP | 88 |  VALUE 'A' | Condition (88) | Condition name — true when the field it is defined under equals 'A'. | none in code |
| ERR-CICS | 88 |  VALUE 'C' | Condition (88) | Condition name — true when the field it is defined under equals 'C'. | none in code |
| ERR-IMS | 88 |  VALUE 'I' | Condition (88) | Condition name — true when the field it is defined under equals 'I'. | none in code |
| ERR-DB2 | 88 |  VALUE 'D' | Condition (88) | Condition name — true when the field it is defined under equals 'D'. | none in code |
| ERR-MQ | 88 |  VALUE 'M' | Condition (88) | Condition name — true when the field it is defined under equals 'M'. | none in code |
| ERR-FILE | 88 |  VALUE 'F' | Condition (88) | Condition name — true when the field it is defined under equals 'F'. | none in code |
| ERR-CODE-1 | 05 | X(09) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-CODE-2 | 05 | X(09) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-MESSAGE | 05 | X(50) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |
| ERR-EVENT-KEY | 05 | X(20) | Alphanumeric | Error-log message field written by COPAUA0C 9500-LOG-ERROR. | none in code |

**Validation / usage notes**
- `ERR-LEVEL`: L/I/W/C; `ERR-SUBSYSTEM`: A/C/I/D/M/F — written by COPAUA0C `9500-LOG-ERROR` to an MQ error queue (inferred).

## 10. Import/Export & statement — CVEXPORT, COSTM01

Branch-migration export record and the TRXFL statement-file record.

### CVEXPORT — `app/cpy/CVEXPORT.cpy` — computed length **500** bytes

Used by: CBEXPORT, CBIMPORT

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| EXPORT-RECORD | 01 |  | Group | Branch export record image (type + header + payload). | none in code |
| EXPORT-REC-TYPE | 05 | X(1) | Alphanumeric | Record type discriminator (C customer / A account / X xref / T tran / D card); dispatch value for CBIMPORT 2200-PROCESS-RECORD-BY-TYPE. | none in code |
| EXPORT-TIMESTAMP | 05 | X(26) | Alphanumeric | Extract timestamp. | none in code |
| EXPORT-TIMESTAMP-R | 05 |  REDEFINES EXPORT-TIMESTAMP | Group | Timestamp REDEFINES group. | none in code |
| EXPORT-DATE | 10 | X(10) | Alphanumeric | Export payload field (mirrors the corresponding base-record field). | none in code |
| EXPORT-DATE-TIME-SEP | 10 | X(1) | Alphanumeric | Export payload field (mirrors the corresponding base-record field). | none in code |
| EXPORT-TIME | 10 | X(15) | Alphanumeric | Export payload field (mirrors the corresponding base-record field). | none in code |
| EXPORT-SEQUENCE-NUM | 05 | 9(9) COMP | Binary (COMP) | Sequence number. | none in code |
| EXPORT-BRANCH-ID | 05 | X(4) | Alphanumeric | Source branch. | none in code |
| EXPORT-REGION-CODE | 05 | X(5) | Alphanumeric | Region. | none in code |
| EXPORT-RECORD-DATA | 05 | X(460) | Alphanumeric | Payload area, redefined per record type. | none in code |
| EXPORT-CUSTOMER-DATA | 05 |  REDEFINES EXPORT-RECORD-DATA | Group | Payload when type C: customer fields. | none in code |
| EXP-CUST-ID | 10 | 9(09) COMP | Binary (COMP) | Customer id (KSDS key). | none in code |
| EXP-CUST-FIRST-NAME | 10 | X(25) | Alphanumeric | First name. | none in code |
| EXP-CUST-MIDDLE-NAME | 10 | X(25) | Alphanumeric | Middle name. | none in code |
| EXP-CUST-LAST-NAME | 10 | X(25) | Alphanumeric | Last name. | none in code |
| EXP-CUST-ADDR-LINES | 10 |  OCCURS 3 TIMES | Group | Customer address lines. | none in code |
| EXP-CUST-ADDR-LINE | 15 | X(50) | Alphanumeric | Export payload field (mirrors the corresponding base-record field). | none in code |
| EXP-CUST-ADDR-STATE-CD | 10 | X(02) | Alphanumeric | US state code. | none in code |
| EXP-CUST-ADDR-COUNTRY-CD | 10 | X(03) | Alphanumeric | Country code. | none in code |
| EXP-CUST-ADDR-ZIP | 10 | X(10) | Alphanumeric | ZIP code. | none in code |
| EXP-CUST-PHONE-NUMS | 10 |  OCCURS 2 TIMES | Group | Customer phone numbers. | none in code |
| EXP-CUST-PHONE-NUM | 15 | X(15) | Alphanumeric | Export payload field (mirrors the corresponding base-record field). | none in code |
| EXP-CUST-SSN | 10 | 9(09) | Zoned numeric | Social security number. | none in code |
| EXP-CUST-GOVT-ISSUED-ID | 10 | X(20) | Alphanumeric | Government-issued id. | none in code |
| EXP-CUST-DOB-YYYY-MM-DD | 10 | X(10) | Alphanumeric | Date of birth (YYYY-MM-DD). | none in code |
| EXP-CUST-EFT-ACCOUNT-ID | 10 | X(10) | Alphanumeric | EFT account id. | none in code |
| EXP-CUST-PRI-CARD-HOLDER-IND | 10 | X(01) | Alphanumeric | Primary card-holder indicator (Y/N). | none in code |
| EXP-CUST-FICO-CREDIT-SCORE | 10 | 9(03) COMP-3 | Packed decimal (COMP-3) | FICO credit score. | none in code |
| FILLER | 10 | X(134) | Alphanumeric | Unused padding to record length. | none in code |
| EXPORT-ACCOUNT-DATA | 05 |  REDEFINES EXPORT-RECORD-DATA | Group | Payload when type A: account fields. | none in code |
| EXP-ACCT-ID | 10 | 9(11) | Zoned numeric | Account identifier (KSDS primary key, 11 digits). | none in code |
| EXP-ACCT-ACTIVE-STATUS | 10 | X(01) | Alphanumeric | Account status flag ('Y' active). | none in code |
| EXP-ACCT-CURR-BAL | 10 | S9(10)V99 COMP-3 | Packed decimal (COMP-3) | Current balance. | none in code |
| EXP-ACCT-CREDIT-LIMIT | 10 | S9(10)V99 | Signed zoned | Credit limit. | none in code |
| EXP-ACCT-CASH-CREDIT-LIMIT | 10 | S9(10)V99 COMP-3 | Packed decimal (COMP-3) | Cash-advance credit limit. | none in code |
| EXP-ACCT-OPEN-DATE | 10 | X(10) | Alphanumeric | Account open date (YYYY-MM-DD). | none in code |
| EXP-ACCT-EXPIRAION-DATE | 10 | X(10) | Alphanumeric | Account expiry date (YYYY-MM-DD; spelling as in source). | none in code |
| EXP-ACCT-REISSUE-DATE | 10 | X(10) | Alphanumeric | Card/account reissue date. | none in code |
| EXP-ACCT-CURR-CYC-CREDIT | 10 | S9(10)V99 | Signed zoned | Sum of positive amounts posted this cycle; reset to 0 by CBACT04C after interest computation. | none in code |
| EXP-ACCT-CURR-CYC-DEBIT | 10 | S9(10)V99 COMP | Binary (COMP) | Sum of debits posted this cycle. | none in code |
| EXP-ACCT-ADDR-ZIP | 10 | X(10) | Alphanumeric | Account address zip code. | none in code |
| EXP-ACCT-GROUP-ID | 10 | X(10) | Alphanumeric | Disclosure-group id joining to DISCGRP for interest-rate lookup. | none in code |
| FILLER | 10 | X(352) | Alphanumeric | Unused padding to record length. | none in code |
| EXPORT-TRANSACTION-DATA | 05 |  REDEFINES EXPORT-RECORD-DATA | Group | Payload when type T: transaction fields. | none in code |
| EXP-TRAN-ID | 10 | X(16) | Alphanumeric | Transaction id (key; generated from last record +1 by COTRN02C). | none in code |
| EXP-TRAN-TYPE-CD | 10 | X(02) | Alphanumeric | Transaction type code. | none in code |
| EXP-TRAN-CAT-CD | 10 | 9(04) | Zoned numeric | Transaction category code. | none in code |
| EXP-TRAN-SOURCE | 10 | X(10) | Alphanumeric | Transaction source (e.g. POS/online). | none in code |
| EXP-TRAN-DESC | 10 | X(100) | Alphanumeric | Description. | none in code |
| EXP-TRAN-AMT | 10 | S9(09)V99 COMP-3 | Packed decimal (COMP-3) | Amount, signed. | none in code |
| EXP-TRAN-MERCHANT-ID | 10 | 9(09) COMP | Binary (COMP) | Merchant id. | none in code |
| EXP-TRAN-MERCHANT-NAME | 10 | X(50) | Alphanumeric | Merchant name. | none in code |
| EXP-TRAN-MERCHANT-CITY | 10 | X(50) | Alphanumeric | Merchant city. | none in code |
| EXP-TRAN-MERCHANT-ZIP | 10 | X(10) | Alphanumeric | Merchant zip. | none in code |
| EXP-TRAN-CARD-NUM | 10 | X(16) | Alphanumeric | Card number used. | none in code |
| EXP-TRAN-ORIG-TS | 10 | X(26) | Alphanumeric | Original timestamp. | none in code |
| EXP-TRAN-PROC-TS | 10 | X(26) | Alphanumeric | Processing timestamp. | none in code |
| FILLER | 10 | X(140) | Alphanumeric | Unused padding to record length. | none in code |
| EXPORT-CARD-XREF-DATA | 05 |  REDEFINES EXPORT-RECORD-DATA | Group | Payload when type X: xref fields. | none in code |
| EXP-XREF-CARD-NUM | 10 | X(16) | Alphanumeric | Card number (key). | none in code |
| EXP-XREF-CUST-ID | 10 | 9(09) | Zoned numeric | Customer id. | none in code |
| EXP-XREF-ACCT-ID | 10 | 9(11) COMP | Binary (COMP) | Account id; CXACAIX AIX key for account→cards lookup. | none in code |
| FILLER | 10 | X(427) | Alphanumeric | Unused padding to record length. | none in code |
| EXPORT-CARD-DATA | 05 |  REDEFINES EXPORT-RECORD-DATA | Group | Payload when type D: card fields. | none in code |
| EXP-CARD-NUM | 10 | X(16) | Alphanumeric | Card number (KSDS key). | none in code |
| EXP-CARD-ACCT-ID | 10 | 9(11) COMP | Binary (COMP) | Owning account id; CARDAIX alternate-index key. | none in code |
| EXP-CARD-CVV-CD | 10 | 9(03) COMP | Binary (COMP) | Card verification value. | none in code |
| EXP-CARD-EMBOSSED-NAME | 10 | X(50) | Alphanumeric | Name embossed on card. | none in code |
| EXP-CARD-EXPIRAION-DATE | 10 | X(10) | Alphanumeric | Card expiry date. | none in code |
| EXP-CARD-ACTIVE-STATUS | 10 | X(01) | Alphanumeric | Card status flag. | none in code |
| FILLER | 10 | X(373) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- CBIMPORT `2200-PROCESS-RECORD-BY-TYPE` dispatches on EXPORT-REC-TYPE; unknown types counted/logged to ERROUT.

### COSTM01 — `app/cpy/COSTM01.CPY` — computed length **350** bytes

Used by: CBSTM03A

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| TRNX-RECORD | 01 |  | Group | TRXFL statement-file record image. | none in code |
| TRNX-KEY | 05 |  | Group | KSDS key: card number + transaction id. | none in code |
| TRNX-CARD-NUM | 10 | X(16) | Alphanumeric | Card number (key part). | none in code |
| TRNX-ID | 10 | X(16) | Alphanumeric | Transaction id (key part). | none in code |
| TRNX-REST | 05 |  | Group | Non-key transaction data (same field order as TRANSACT). | none in code |
| TRNX-TYPE-CD | 10 | X(02) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-CAT-CD | 10 | 9(04) | Zoned numeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-SOURCE | 10 | X(10) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-DESC | 10 | X(100) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-AMT | 10 | S9(09)V99 | Signed zoned | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-MERCHANT-ID | 10 | 9(09) | Zoned numeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-MERCHANT-NAME | 10 | X(50) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-MERCHANT-CITY | 10 | X(50) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-MERCHANT-ZIP | 10 | X(10) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-ORIG-TS | 10 | X(26) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| TRNX-PROC-TS | 10 | X(26) | Alphanumeric | TRXFL record field (mirrors TRANSACT layout). | none in code |
| FILLER | 10 | X(20) | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- Used by CBSTM03A `4000-TRNXFILE-GET` / CBSTM03B; key = card-num + tran-id (TRXFL KSDS KEYS(32 0)).

## 11. Session / navigation — COCOM01Y, CVCRD01Y, COMEN02Y, COADM02Y

CICS commarea, screen work area and the two menu tables.

### COCOM01Y — `app/cpy/COCOM01Y.cpy` — computed length **160** bytes

Used by: COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COPAUS0C, COPAUS1C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COTRTLIC, COTRTUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CARDDEMO-COMMAREA | 01 |  | Group | 60-byte CICS commarea passed on every XCTL/RETURN. | none in code |
| CDEMO-GENERAL-INFO | 05 |  | Group | Commarea header: source/target tranid and program, signed-on user, re-entry flag. | none in code |
| CDEMO-FROM-TRANID | 10 | X(04) | Alphanumeric | Tranid that transferred control. | none in code |
| CDEMO-FROM-PROGRAM | 10 | X(08) | Alphanumeric | Program that transferred control. | none in code |
| CDEMO-TO-TRANID | 10 | X(04) | Alphanumeric | Target tranid. | none in code |
| CDEMO-TO-PROGRAM | 10 | X(08) | Alphanumeric | Target program for XCTL. | none in code |
| CDEMO-USER-ID | 10 | X(08) | Alphanumeric | Signed-on user id. | Set by COSGN00C on successful sign-on. |
| CDEMO-USER-TYPE | 10 | X(01) | Alphanumeric | 'A'/'U'. | 'A'/'U' from SEC-USR-TYPE (COSGN00C); drives admin vs user menu. |
| CDEMO-USRTYP-ADMIN | 88 |  VALUE 'A' | Condition (88) | 'A' — administrator. | none in code |
| CDEMO-USRTYP-USER | 88 |  VALUE 'U' | Condition (88) | 'U' — regular user. | none in code |
| CDEMO-PGM-CONTEXT | 10 | 9(01) | Zoned numeric | 0 = first entry (ENTER), 1 = re-entry after display. | 0 first entry / 1 re-entry; set by each transaction program. |
| CDEMO-PGM-ENTER | 88 |  VALUE 0 | Condition (88) | 0 — first entry into program (ENTER key). | none in code |
| CDEMO-PGM-REENTER | 88 |  VALUE 1 | Condition (88) | 1 — re-entry after map display. | none in code |
| CDEMO-CUSTOMER-INFO | 05 |  | Group | Session snapshot of current customer (id + name). | none in code |
| CDEMO-CUST-ID | 10 | 9(09) | Zoned numeric | Current customer id in session. | none in code |
| CDEMO-CUST-FNAME | 10 | X(25) | Alphanumeric | Customer first name. | none in code |
| CDEMO-CUST-MNAME | 10 | X(25) | Alphanumeric | Middle. | none in code |
| CDEMO-CUST-LNAME | 10 | X(25) | Alphanumeric | Last. | none in code |
| CDEMO-ACCOUNT-INFO | 05 |  | Group | Session snapshot of current account (id + status). | none in code |
| CDEMO-ACCT-ID | 10 | 9(11) | Zoned numeric | Current account id. | none in code |
| CDEMO-ACCT-STATUS | 10 | X(01) | Alphanumeric | Current account status. | none in code |
| CDEMO-CARD-INFO | 05 |  | Group | Session card number. | none in code |
| CDEMO-CARD-NUM | 10 | 9(16) | Zoned numeric | Current card number. | none in code |
| CDEMO-MORE-INFO | 05 |  | Group | Last map/mapset shown (for return navigation). | none in code |
| CDEMO-LAST-MAP | 10 | X(7) | Alphanumeric | Last map shown. | none in code |
| CDEMO-LAST-MAPSET | 10 | X(7) | Alphanumeric | Last mapset. | none in code |

### CVCRD01Y — `app/cpy/CVCRD01Y.cpy` — computed length **213** bytes

Used by: COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CC-WORK-AREAS | 01 |  | Group | Card-demo screen work area (AID flag, next-program fields, message areas). | none in code |
| CC-WORK-AREA | 05 |  | Group | Screen navigation work area. | none in code |
| CCARD-AID | 10 | X(5) | Alphanumeric | Attention-id flag set from EIBAID. | none in code |
| CCARD-AID-ENTER | 88 |  VALUE 'ENTER' | Condition (88) | Condition name — true when the field it is defined under equals 'ENTER'. | none in code |
| CCARD-AID-CLEAR | 88 |  VALUE 'CLEAR' | Condition (88) | Condition name — true when the field it is defined under equals 'CLEAR'. | none in code |
| CCARD-AID-PA1 | 88 |  VALUE 'PA1  ' | Condition (88) | Condition name — true when the field it is defined under equals 'PA1  '. | none in code |
| CCARD-AID-PA2 | 88 |  VALUE 'PA2  ' | Condition (88) | Condition name — true when the field it is defined under equals 'PA2  '. | none in code |
| CCARD-AID-PFK01 | 88 |  VALUE 'PFK01' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK01'. | none in code |
| CCARD-AID-PFK02 | 88 |  VALUE 'PFK02' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK02'. | none in code |
| CCARD-AID-PFK03 | 88 |  VALUE 'PFK03' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK03'. | none in code |
| CCARD-AID-PFK04 | 88 |  VALUE 'PFK04' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK04'. | none in code |
| CCARD-AID-PFK05 | 88 |  VALUE 'PFK05' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK05'. | none in code |
| CCARD-AID-PFK06 | 88 |  VALUE 'PFK06' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK06'. | none in code |
| CCARD-AID-PFK07 | 88 |  VALUE 'PFK07' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK07'. | none in code |
| CCARD-AID-PFK08 | 88 |  VALUE 'PFK08' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK08'. | none in code |
| CCARD-AID-PFK09 | 88 |  VALUE 'PFK09' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK09'. | none in code |
| CCARD-AID-PFK10 | 88 |  VALUE 'PFK10' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK10'. | none in code |
| CCARD-AID-PFK11 | 88 |  VALUE 'PFK11' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK11'. | none in code |
| CCARD-AID-PFK12 | 88 |  VALUE 'PFK12' | Condition (88) | Condition name — true when the field it is defined under equals 'PFK12'. | none in code |
| CCARD-NEXT-PROG | 10 | X(8) | Alphanumeric | Next program to XCTL to. | none in code |
| CCARD-NEXT-MAPSET | 10 | X(7) | Alphanumeric | Next mapset. | none in code |
| CCARD-NEXT-MAP | 10 | X(7) | Alphanumeric | Next map. | none in code |
| CCARD-ERROR-MSG | 10 | X(75) | Alphanumeric | Screen error message. | none in code |
| CCARD-RETURN-MSG | 10 | X(75) | Alphanumeric | Return/info message. | none in code |
| CCARD-RETURN-MSG-OFF | 88 |  VALUE LOW-VALUES | Condition (88) | Condition name — true when the field it is defined under equals LOW-VALUES. | none in code |
| CC-ACCT-ID | 10 | X(11) VALUE SPACES | Alphanumeric | Screen account id (alpha form). | none in code |
| CC-ACCT-ID-N | 10 | 9(11) REDEFINES CC-ACCT-ID | Zoned numeric | Numeric view of CC-ACCT-ID. | none in code |
| CC-CARD-NUM | 10 | X(16) VALUE SPACES | Alphanumeric | Screen card number. | none in code |
| CC-CARD-NUM-N | 10 | 9(16) REDEFINES CC-CARD-NUM | Zoned numeric | Numeric view. | none in code |
| CC-CUST-ID | 10 | X(09) VALUE SPACES | Alphanumeric | Screen customer id. | none in code |
| CC-CUST-ID-N | 10 | 9(9) REDEFINES CC-CUST-ID | Zoned numeric | Numeric view. | none in code |

### COMEN02Y — `app/cpy/COMEN02Y.cpy` — computed length **508** bytes

Used by: COMEN01C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CARDDEMO-MAIN-MENU-OPTIONS | 01 |  | Group | Main menu table (11 entries, user-type U). | none in code |
| CDEMO-MENU-OPT-COUNT | 05 | 9(02) VALUE 11 | Zoned numeric | Number of menu entries. | none in code |
| CDEMO-MENU-OPTIONS-DATA | 05 |  | Group | Header: option count. | none in code |
| FILLER | 10 | 9(02) VALUE 1 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'ACCOUNT VIEW                       ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COACTVWC' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 2 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'ACCOUNT UPDATE                     ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COACTUPC' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 3 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'CREDIT CARD LIST                   ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COCRDLIC' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 4 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'CREDIT CARD VIEW                   ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COCRDSLC' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 5 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'CREDIT CARD UPDATE                 ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COCRDUPC' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 6 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'TRANSACTION LIST                   ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COTRN00C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 7 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'TRANSACTION VIEW                   ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COTRN01C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 8 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'TRANSACTION ADD                    ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COTRN02C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 9 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'TRANSACTION REPORTS                ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'CORPT00C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 10 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'BILL PAYMENT                       ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COBIL00C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 11 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'PENDING AUTHORIZATION VIEW         ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COPAUS0C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(01) VALUE 'U' | Alphanumeric | Unused padding to record length. | none in code |
| CDEMO-MENU-OPTIONS | 05 |  REDEFINES CDEMO-MENU-OPTIONS-DATA | Group | OCCURS array of menu options. | none in code |
| CDEMO-MENU-OPT | 10 |  OCCURS 12 TIMES | Group | One menu option (key, tranid, program, text, user type). | none in code |
| CDEMO-MENU-OPT-NUM | 15 | 9(02) | Zoned numeric | Main-menu option attribute. | none in code |
| CDEMO-MENU-OPT-NAME | 15 | X(35) | Alphanumeric | Main-menu option attribute. | none in code |
| CDEMO-MENU-OPT-PGMNAME | 15 | X(08) | Alphanumeric | Main-menu option attribute. | none in code |
| CDEMO-MENU-OPT-USRTYPE | 15 | X(01) | Alphanumeric | Main-menu option attribute. | none in code |

**Validation / usage notes**
- 11 seed options; each entry carries user-type 'U' so all regular users see them.

### COADM02Y — `app/cpy/COADM02Y.cpy` — computed length **272** bytes

Used by: COADM01C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CARDDEMO-ADMIN-MENU-OPTIONS | 01 |  | Group | Admin menu table (6 entries, user-type A). | none in code |
| CDEMO-ADMIN-OPT-COUNT | 05 | 9(02) VALUE 6 | Zoned numeric | Number of admin menu entries. | none in code |
| CDEMO-ADMIN-OPTIONS-DATA | 05 |  | Group | Header: option count. | none in code |
| FILLER | 10 | 9(02) VALUE 1 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'USER LIST (SECURITY)               ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COUSR00C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 2 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'USER ADD (SECURITY)                ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COUSR01C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 3 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'USER UPDATE (SECURITY)             ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COUSR02C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 4 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'USER DELETE (SECURITY)             ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COUSR03C' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 5 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'TRANSACTION TYPE LIST/UPDATE (DB2) ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COTRTLIC' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | 9(02) VALUE 6 | Zoned numeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(35) VALUE 'TRANSACTION TYPE MAINTENANCE (DB2) ' | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 10 | X(08) VALUE 'COTRTUPC' | Alphanumeric | Unused padding to record length. | none in code |
| CDEMO-ADMIN-OPTIONS | 05 |  REDEFINES CDEMO-ADMIN-OPTIONS-DATA | Group | OCCURS array of admin menu options. | none in code |
| CDEMO-ADMIN-OPT | 10 |  OCCURS 9 TIMES | Group | One admin menu option (key, tranid, program, text, user type). | none in code |
| CDEMO-ADMIN-OPT-NUM | 15 | 9(02) | Zoned numeric | Admin-menu option attribute. | none in code |
| CDEMO-ADMIN-OPT-NAME | 15 | X(35) | Alphanumeric | Admin-menu option attribute. | none in code |
| CDEMO-ADMIN-OPT-PGMNAME | 15 | X(08) | Alphanumeric | Admin-menu option attribute. | none in code |

**Validation / usage notes**
- 6 seed options (4 user-security programs + 2 DB2 transaction-type programs).

## 12. Utility / infrastructure

Date/time work areas, messages, screen titles, date-conversion interface, lookup tables, DB2 error areas, DL/I function codes and PCB masks.

### CSDAT01Y — `app/cpy/CSDAT01Y.cpy` — computed length **58** bytes

Used by: COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COPAUS0C, COPAUS1C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COTRTLIC, COTRTUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| WS-DATE-TIME | 01 |  | Group | Current date/time work area. | none in code |
| WS-CURDATE-DATA | 05 |  | Group | Gregorian current date/time (YYYYMMDD.HHMMSSTH). | none in code |
| WS-CURDATE | 10 |  | Group | Current date parts. | none in code |
| WS-CURDATE-YEAR | 15 | 9(04) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURDATE-MONTH | 15 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURDATE-DAY | 15 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURDATE-N | 10 | 9(08) REDEFINES WS-CURDATE | Zoned numeric | Alternate (redefined) view of WS-CURDATE. | none in code |
| WS-CURTIME | 10 |  | Group | Current time parts. | none in code |
| WS-CURTIME-HOURS | 15 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURTIME-MINUTE | 15 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURTIME-SECOND | 15 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURTIME-MILSEC | 15 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURTIME-N | 10 | 9(08) REDEFINES WS-CURTIME | Zoned numeric | Alternate (redefined) view of WS-CURTIME. | none in code |
| WS-CURDATE-MM-DD-YY | 05 |  | Group | Display date MM/DD/YY. | none in code |
| WS-CURDATE-MM | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE '/' | Alphanumeric | Unused padding to record length. | none in code |
| WS-CURDATE-DD | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE '/' | Alphanumeric | Unused padding to record length. | none in code |
| WS-CURDATE-YY | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-CURTIME-HH-MM-SS | 05 |  | Group | Display time HH:MM:SS. | none in code |
| WS-CURTIME-HH | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE ':' | Alphanumeric | Unused padding to record length. | none in code |
| WS-CURTIME-MM | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE ':' | Alphanumeric | Unused padding to record length. | none in code |
| WS-CURTIME-SS | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| WS-TIMESTAMP | 05 |  | Group | Formatted timestamp YYYY-MM-DD HH:MM:SS.HH. | none in code |
| WS-TIMESTAMP-DT-YYYY | 10 | 9(04) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE '-' | Alphanumeric | Unused padding to record length. | none in code |
| WS-TIMESTAMP-DT-MM | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE '-' | Alphanumeric | Unused padding to record length. | none in code |
| WS-TIMESTAMP-DT-DD | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE ' ' | Alphanumeric | Unused padding to record length. | none in code |
| WS-TIMESTAMP-TM-HH | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE ':' | Alphanumeric | Unused padding to record length. | none in code |
| WS-TIMESTAMP-TM-MM | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE ':' | Alphanumeric | Unused padding to record length. | none in code |
| WS-TIMESTAMP-TM-SS | 10 | 9(02) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |
| FILLER | 10 | X(01) VALUE '.' | Alphanumeric | Unused padding to record length. | none in code |
| WS-TIMESTAMP-TM-MS6 | 10 | 9(06) | Zoned numeric | Date/time work field (populated from CICS ASKTIME/FORMATTIME). | none in code |

### CSMSG01Y — `app/cpy/CSMSG01Y.cpy` — computed length **100** bytes

Used by: COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COPAUS0C, COPAUS1C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COTRTLIC, COTRTUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CCDA-COMMON-MESSAGES | 01 |  | Group | Shared screen message literals. | none in code |
| CCDA-MSG-THANK-YOU | 05 | X(50) VALUE 'THANK YOU FOR USING CARDDEMO APPLICATION...       | Alphanumeric | Thank-you message literal. | none in code |
| CCDA-MSG-INVALID-KEY | 05 | X(50) VALUE 'INVALID KEY PRESSED. PLEASE SEE BELOW...          | Alphanumeric | Invalid-key message literal. | none in code |

### CSMSG02Y — `app/cpy/CSMSG02Y.cpy` — computed length **134** bytes

Used by: COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C, COTRTUPC

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| ABEND-DATA | 01 |  | Group | Abend-report fields (code, culprit, reason, message). | none in code |
| ABEND-CODE | 05 | X(4) VALUE SPACES | Alphanumeric | Abend code. | none in code |
| ABEND-CULPRIT | 05 | X(8) VALUE SPACES | Alphanumeric | Program that abended. | none in code |
| ABEND-REASON | 05 | X(50) VALUE SPACES | Alphanumeric | Reason text. | none in code |
| ABEND-MSG | 05 | X(72) VALUE SPACES | Alphanumeric | Formatted message. | none in code |

### COTTL01Y — `app/cpy/COTTL01Y.cpy` — computed length **120** bytes

Used by: COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COPAUS0C, COPAUS1C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COTRTLIC, COTRTUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CCDA-SCREEN-TITLE | 01 |  | Group | Screen title lines and common literals. | none in code |
| CCDA-TITLE01 | 05 | X(40) VALUE '      AWS MAINFRAME MODERNIZATION       ' | Alphanumeric | Screen title line 1. | none in code |
| CCDA-TITLE02 | 05 | X(40) VALUE '              CARDDEMO                  ' | Alphanumeric | Title line 2. | none in code |
| CCDA-THANK-YOU | 05 | X(40) VALUE 'THANK YOU FOR USING CCDA APPLICATION... ' | Alphanumeric | Signoff message. | none in code |

### CODATECN — `app/cpy/CODATECN.cpy` — computed length **80** bytes

Used by: CBACT01C

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| CODATECN-REC | 01 |  | Group | Date-conversion interface record for CALL CSUTLDTC. | none in code |
| CODATECN-IN-REC | 05 |  | Group | Input date + format selector. | none in code |
| CODATECN-TYPE | 10 | X | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| YYYYMMDD-IN | 88 |  VALUE "1" | Condition (88) | Condition name — true when the field it is defined under equals "1". | none in code |
| YYYY-MM-DD-IN | 88 |  VALUE "2" | Condition (88) | Condition name — true when the field it is defined under equals "2". | none in code |
| CODATECN-INP-DATE | 10 | X(20) | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1INP | 10 |  REDEFINES CODATECN-INP-DATE | Group | Input format selector (see 88s). | none in code |
| CODATECN-1YYYY | 15 | XXXX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1MM | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1DD | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1FIL | 15 | X(12) | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-2INP | 10 |  REDEFINES CODATECN-INP-DATE | Group | Input date value. | none in code |
| CODATECN-1O-YYYY | 15 | XXXX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1I-S1 | 15 | X | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1MM | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1I-S2 | 15 | X | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-2YY | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-2FIL | 15 | X(10) | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-OUT-REC | 05 |  | Group | Output date + result. | none in code |
| CODATECN-OUTTYPE | 10 | X | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| YYYY-MM-DD-OP | 88 |  VALUE "1" | Condition (88) | Condition name — true when the field it is defined under equals "1". | none in code |
| YYYYMMDD-OP | 88 |  VALUE "2" | Condition (88) | Condition name — true when the field it is defined under equals "2". | none in code |
| CODATECN-0UT-DATE | 10 | X(20) | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1OUT | 10 |  REDEFINES CODATECN-0UT-DATE | Group | Output format selector. | none in code |
| CODATECN-1O-YYYY | 15 | XXXX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1O-S1 | 15 | X | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1O-MM | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1O-S2 | 15 | X | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1O-DD | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-1OFIL | 15 | X(10) | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-2OUT | 10 |  REDEFINES CODATECN-0UT-DATE | Group | Converted output date. | none in code |
| CODATECN-2O-YYYY | 15 | XXXX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-2O-MM | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-2O-DD | 15 | XX | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-2OFIL | 15 | X(12) | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |
| CODATECN-ERROR-MSG | 05 | X(38) | Alphanumeric | Date-conversion interface field for CALL CSUTLDTC. | none in code |

**Validation / usage notes**
- `CODATECN-TYPE` 88s: '1'=YYYYMMDD in, '2'=YYYY-MM-DD in; `CODATECN-OUTTYPE`: '1'=YYYY-MM-DD out, '2'=YYYYMMDD out.

### CSUTLDWY — `app/cpy/CSUTLDWY.cpy` — computed length **115** bytes

Used by: COACTUPC, COTRTUPC

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| WS-EDIT-DATE-CCYYMMDD | 10 |  | Group | Date under validation (CCYY MM DD parts). | none in code |
| WS-EDIT-DATE-CCYY | 20 |  | Group | Century-year part. | none in code |
| WS-EDIT-DATE-CC | 25 | X(2) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-EDIT-DATE-CC-N | 25 | 9(2) REDEFINES WS-EDIT-DATE-CC | Zoned numeric | Alternate (redefined) view of WS-EDIT-DATE-CC. | none in code |
| THIS-CENTURY | 88 |  VALUE 20 | Condition (88) | Condition name — true when the field it is defined under equals 20. | none in code |
| LAST-CENTURY | 88 |  VALUE 19 | Condition (88) | Condition name — true when the field it is defined under equals 19. | none in code |
| WS-EDIT-DATE-YY | 25 | X(2) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-EDIT-DATE-YY-N | 25 | 9(2) REDEFINES WS-EDIT-DATE-YY | Zoned numeric | Alternate (redefined) view of WS-EDIT-DATE-YY. | none in code |
| WS-EDIT-DATE-CCYY-N | 20 | 9(4) REDEFINES WS-EDIT-DATE-CCYY | Zoned numeric | Alternate (redefined) view of WS-EDIT-DATE-CCYY. | none in code |
| WS-EDIT-DATE-MM | 20 | X(2) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-EDIT-DATE-MM-N | 20 | 9(2) REDEFINES WS-EDIT-DATE-MM | Zoned numeric | Alternate (redefined) view of WS-EDIT-DATE-MM. | none in code |
| WS-VALID-MONTH | 88 |  VALUE 1 THROUGH 12 | Condition (88) | Condition name — true when the field it is defined under equals 1 THROUGH 12. | none in code |
| WS-31-DAY-MONTH | 88 |  VALUE 1, 3, 5, 7, 8, 10, 12 | Condition (88) | Condition name — true when the field it is defined under equals 1, 3, 5, 7, 8, 10, 12. | none in code |
| WS-FEBRUARY | 88 |  VALUE 2 | Condition (88) | Condition name — true when the field it is defined under equals 2. | none in code |
| WS-EDIT-DATE-DD | 20 | X(2) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-EDIT-DATE-DD-N | 20 | 9(2) REDEFINES WS-EDIT-DATE-DD | Zoned numeric | Alternate (redefined) view of WS-EDIT-DATE-DD. | none in code |
| WS-VALID-DAY | 88 |  VALUE 1 THROUGH 31 | Condition (88) | Condition name — true when the field it is defined under equals 1 THROUGH 31. | none in code |
| WS-DAY-31 | 88 |  VALUE 31 | Condition (88) | Condition name — true when the field it is defined under equals 31. | none in code |
| WS-DAY-30 | 88 |  VALUE 30 | Condition (88) | Condition name — true when the field it is defined under equals 30. | none in code |
| WS-DAY-29 | 88 |  VALUE 29 | Condition (88) | Condition name — true when the field it is defined under equals 29. | none in code |
| WS-VALID-FEB-DAY | 88 |  VALUE 1 THROUGH 28 | Condition (88) | Condition name — true when the field it is defined under equals 1 THROUGH 28. | none in code |
| WS-EDIT-DATE-CCYYMMDD-N | 10 | 9(8) REDEFINES WS-EDIT-DATE-CCYYMMDD | Zoned numeric | Alternate (redefined) view of WS-EDIT-DATE-CCYYMMDD. | none in code |
| WS-EDIT-DATE-BINARY | 10 | S9(9) BINARY | Binary (BINARY) | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-CURRENT-DATE | 10 |  | Group | Today (for not-in-future checks). | none in code |
| WS-CURRENT-DATE-YYYYMMDD | 20 | X(8) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-CURRENT-DATE-YYYYMMDD-N | 20 | 9(8) REDEFINES WS-CURRENT-DATE-YYYYMMDD | Zoned numeric | Alternate (redefined) view of WS-CURRENT-DATE-YYYYMMDD. | none in code |
| WS-CURRENT-DATE-BINARY | 20 | S9(9) BINARY | Binary (BINARY) | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-EDIT-DATE-FLGS | 10 |  | Group | Per-field validity flags. | none in code |
| WS-EDIT-DATE-IS-VALID | 88 |  VALUE LOW-VALUES | Condition (88) | Condition name — true when the field it is defined under equals LOW-VALUES. | none in code |
| WS-EDIT-DATE-IS-INVALID | 88 |  VALUE '000' | Condition (88) | Condition name — true when the field it is defined under equals '000'. | none in code |
| WS-EDIT-YEAR-FLG | 20 | X(01) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| FLG-YEAR-ISVALID | 88 |  VALUE LOW-VALUES | Condition (88) | Condition name — true when the field it is defined under equals LOW-VALUES. | none in code |
| FLG-YEAR-NOT-OK | 88 |  VALUE '0' | Condition (88) | Condition name — true when the field it is defined under equals '0'. | none in code |
| FLG-YEAR-BLANK | 88 |  VALUE 'B' | Condition (88) | Condition name — true when the field it is defined under equals 'B'. | none in code |
| WS-EDIT-MONTH | 20 | X(01) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| FLG-MONTH-ISVALID | 88 |  VALUE LOW-VALUES | Condition (88) | Condition name — true when the field it is defined under equals LOW-VALUES. | none in code |
| FLG-MONTH-NOT-OK | 88 |  VALUE '0' | Condition (88) | Condition name — true when the field it is defined under equals '0'. | none in code |
| FLG-MONTH-BLANK | 88 |  VALUE 'B' | Condition (88) | Condition name — true when the field it is defined under equals 'B'. | none in code |
| WS-EDIT-DAY | 20 | X(01) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| FLG-DAY-ISVALID | 88 |  VALUE LOW-VALUES | Condition (88) | Condition name — true when the field it is defined under equals LOW-VALUES. | none in code |
| FLG-DAY-NOT-OK | 88 |  VALUE '0' | Condition (88) | Condition name — true when the field it is defined under equals '0'. | none in code |
| FLG-DAY-BLANK | 88 |  VALUE 'B' | Condition (88) | Condition name — true when the field it is defined under equals 'B'. | none in code |
| WS-DATE-FORMAT | 10 | X(08) VALUE 'YYYYMMDD' | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-DATE-VALIDATION-RESULT | 10 |  | Group | Aggregate result (LOW-VALUES = valid). | none in code |
| WS-SEVERITY | 20 | X(04) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-SEVERITY-N | 20 | 9(4) REDEFINES WS-SEVERITY | Zoned numeric | Alternate (redefined) view of WS-SEVERITY. | none in code |
| FILLER | 20 | X(11) VALUE 'MESG CODE:' | Alphanumeric | Unused padding to record length. | none in code |
| WS-MSG-NO | 20 | X(04) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| WS-MSG-NO-N | 20 | 9(4) REDEFINES WS-MSG-NO | Zoned numeric | Alternate (redefined) view of WS-MSG-NO. | none in code |
| FILLER | 20 | X(01) VALUE SPACE | Alphanumeric | Unused padding to record length. | none in code |
| WS-RESULT | 20 | X(15) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| FILLER | 20 | X(01) VALUE SPACE | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 20 | X(09) VALUE 'TSTDATE:' | Alphanumeric | Unused padding to record length. | none in code |
| WS-DATE | 20 | X(10) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| FILLER | 20 | X(01) VALUE SPACE | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 20 | X(10) VALUE 'MASK USED:' | Alphanumeric | Unused padding to record length. | none in code |
| WS-DATE-FMT | 20 | X(10) | Alphanumeric | Date-validation work field (companion to CSUTLDPY). | none in code |
| FILLER | 20 | X(01) VALUE SPACE | Alphanumeric | Unused padding to record length. | none in code |
| FILLER | 20 | X(03) VALUE SPACES | Alphanumeric | Unused padding to record length. | none in code |

**Validation / usage notes**
- Flag values: LOW-VALUES=valid, '0'=not ok, 'B'=blank; month 88s 1-12 / 31-day months / February; day 88s 1-31 incl. leap-year logic in CSUTLDPY.

### CSLKPCDY — `app/cpy/CSLKPCDY.cpy` — computed length **12** bytes

Used by: COACTUPC

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| WS-US-PHONE-AREA-CODE-TO-EDIT | 01 | XXX | Alphanumeric | 3-digit area code under edit; 88s enumerate valid NANP codes (~300 general-purpose plus easily-recognised N11-like codes). | none in code |
| VALID-PHONE-AREA-CODE | 88 |  VALUE '201', '202', '203', '204', '205', '206', '207', ' | Condition (88) | Condition name — true when the field it is defined under equals '201', '202', '203', '204', '205', '206', '207', '208', '209', '210', '212', '213', '214', '215', '216', '217', '218', '. | none in code |
| VALID-GENERAL-PURP-CODE | 88 |  VALUE '201', '202', '203', '204', '205', '206', '207', ' | Condition (88) | Condition name — true when the field it is defined under equals '201', '202', '203', '204', '205', '206', '207', '208', '209', '210', '212', '213', '214', '215', '216', '217', '218', '. | none in code |
| VALID-EASY-RECOG-AREA-CODE | 88 |  VALUE '200', '211', '222', '233', '244', '255', '266', ' | Condition (88) | Condition name — true when the field it is defined under equals '200', '211', '222', '233', '244', '255', '266', '277', '288', '299', '300', '311', '322', '333', '344', '355', '366', '. | none in code |
| US-STATE-CODE-TO-EDIT | 01 | X(2) | Alphanumeric | 2-char state code; 88 lists 50 states + DC + territories. | none in code |
| VALID-US-STATE-CODE | 88 |  VALUE 'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'F | Condition (88) | Condition name — true when the field it is defined under equals 'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA', 'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD', . | none in code |
| US-STATE-ZIPCODE-TO-EDIT | 01 |  | Group | Input area validated against CSLKPCDY 88-lists. | none in code |
| US-STATE-AND-FIRST-ZIP2 | 02 | X(4) | Alphanumeric | State + first 2 zip digits; 88 lists valid combinations. | none in code |
| VALID-US-STATE-ZIP-CD2-COMBO | 88 |  VALUE 'AA34', 'AE90', 'AE91', 'AE92', 'AE93', 'AE94', 'A | Condition (88) | Condition name — true when the field it is defined under equals 'AA34', 'AE90', 'AE91', 'AE92', 'AE93', 'AE94', 'AE95', 'AE96', 'AE97', 'AE98', 'AK99', 'AL35', 'AL36', 'AP96', 'AR71', . | none in code |
| LAST-3-OF-ZIP | 02 | X(3) | Alphanumeric | Last 3 digits of zip. | none in code |

**Validation / usage notes**
- Domains used by COACTUPC: ~300 valid NANP area codes (VALID-PHONE-AREA-CODE / VALID-GENERAL-PURP-CODE / VALID-EASY-RECOG-AREA-CODE 88 lists), 50 US states + DC + territories (VALID-US-STATE-CODE), and state+zip2 combinations (VALID-US-STATE-ZIP-CD2-COMBO).

### CSDB2RWY — `app/app-transaction-type-db2/cpy/CSDB2RWY.cpy` — computed length **817** bytes

Used by: COTRTLIC

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| WS-DB2-COMMON-VARS | 05 |  | Group | Common DB2 work fields. | none in code |
| WS-DISP-SQLCODE | 10 | ----9 | Edited display | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DUMMY-DB2-INT | 10 | S9(4) COMP-3 VALUE 0 | Packed decimal (COMP-3) | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DB2-PROCESSING-FLAG | 10 | X(1) | Alphanumeric | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DB2-OK | 88 |  VALUE '0' | Condition (88) | Condition name — true when the field it is defined under equals '0'. | none in code |
| WS-DB2-ERROR | 88 |  VALUE '1' | Condition (88) | Condition name — true when the field it is defined under equals '1'. | none in code |
| WS-DB2-CURRENT-ACTION | 10 | X(72) VALUE SPACES | Alphanumeric | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DSNTIAC-FORMATTED | 05 |  | Group | Formatted SQLCA text returned by DSNTIAC. | none in code |
| WS-DSNTIAC-MESG-LEN | 10 | S9(4) COMP VALUE +720 | Binary (COMP) | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DSNTIAC-FMTD-TEXT | 10 |  | Group | Message text lines of formatted SQLCA. | none in code |
| WS-DSNTIAC-FMTD-TEXT-LINE | 15 | X(72) OCCURS 10 TIMES VALUE SPACES | Alphanumeric | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DSNTIAC-LRECL | 05 | S9(4) COMP VALUE +72 | Binary (COMP) | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DSNTIAC-ERROR | 05 |  | Group | DSNTIAC error fields. | none in code |
| WS-DSNTIAC-ERR-MSG | 10 | X(10) VALUE 'DSNTIAC CD' | Alphanumeric | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DSNTIAC-ERR-CD-X | 10 | X(02) VALUE SPACES | Alphanumeric | DB2 error-handling work field (DSNTIAC formatting). | none in code |
| WS-DSNTIAC-ERR-CD | 10 | 9(02) REDEFINES WS-DSNTIAC-ERR-CD-X | Zoned numeric | Alternate (redefined) view of WS-DSNTIAC-ERR-CD-X. | none in code |

### IMSFUNCS — `app/app-authorization-ims-db2-mq/cpy/IMSFUNCS.cpy` — computed length **40** bytes

Used by: DBUNLDGS, PAUDBLOD, PAUDBUNL

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| FUNC-CODES | 01 |  | Group | DL/I call-function constants and parm count. | none in code |
| FUNC-GU | 05 | X(04) VALUE 'GU  ' | Alphanumeric | DL/I Get Unique. | none in code |
| FUNC-GHU | 05 | X(04) VALUE 'GHU ' | Alphanumeric | Get Hold Unique. | none in code |
| FUNC-GN | 05 | X(04) VALUE 'GN  ' | Alphanumeric | Get Next. | none in code |
| FUNC-GHN | 05 | X(04) VALUE 'GHN ' | Alphanumeric | Get Hold Next. | none in code |
| FUNC-GNP | 05 | X(04) VALUE 'GNP ' | Alphanumeric | Get Next within Parent. | none in code |
| FUNC-GHNP | 05 | X(04) VALUE 'GHNP' | Alphanumeric | Get Hold Next within Parent. | none in code |
| FUNC-REPL | 05 | X(04) VALUE 'REPL' | Alphanumeric | Replace. | none in code |
| FUNC-ISRT | 05 | X(04) VALUE 'ISRT' | Alphanumeric | Insert. | none in code |
| FUNC-DLET | 05 | X(04) VALUE 'DLET' | Alphanumeric | Delete. | none in code |
| PARMCOUNT | 05 | S9(05) COMP-5 VALUE +4 COMP-5 | Binary (COMP-5) | DL/I parm count for CBLTDLI. | none in code |

### PAUTBPCB — `app/app-authorization-ims-db2-mq/cpy/PAUTBPCB.CPY` — computed length **291** bytes

Used by: DBUNLDGS, PAUDBLOD, PAUDBUNL

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| PAUTBPCB | 01 |  | Group | PCB mask for IMS DB DBPAUTP0 (pending authorization). | none in code |
| PAUT-DBDNAME | 05 | X(08) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PAUT-SEG-LEVEL | 05 | X(02) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PAUT-PCB-STATUS | 05 | X(02) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PAUT-PCB-PROCOPT | 05 | X(04) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| FILLER | 05 | S9(05) COMP | Binary (COMP) | Unused padding to record length. | none in code |
| PAUT-SEG-NAME | 05 | X(08) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PAUT-KEYFB-NAME | 05 | S9(05) COMP | Binary (COMP) | PCB mask field for DL/I calls. | none in code |
| PAUT-NUM-SENSEGS | 05 | S9(05) COMP | Binary (COMP) | PCB mask field for DL/I calls. | none in code |
| PAUT-KEYFB | 05 | X(255) | Alphanumeric | PCB mask field for DL/I calls. | none in code |

### PASFLPCB — `app/app-authorization-ims-db2-mq/cpy/PASFLPCB.CPY` — computed length **136** bytes

Used by: DBUNLDGS

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| PASFLPCB | 01 |  | Group | PCB mask for IMS DB PAUTSFL (summary). | none in code |
| PASFL-DBDNAME | 05 | X(08) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PASFL-SEG-LEVEL | 05 | X(02) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PASFL-PCB-STATUS | 05 | X(02) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PASFL-PCB-PROCOPT | 05 | X(04) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| FILLER | 05 | S9(05) COMP | Binary (COMP) | Unused padding to record length. | none in code |
| PASFL-SEG-NAME | 05 | X(08) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PASFL-KEYFB-NAME | 05 | S9(05) COMP | Binary (COMP) | PCB mask field for DL/I calls. | none in code |
| PASFL-NUM-SENSEGS | 05 | S9(05) COMP | Binary (COMP) | PCB mask field for DL/I calls. | none in code |
| PASFL-KEYFB | 05 | X(100) | Alphanumeric | PCB mask field for DL/I calls. | none in code |

### PADFLPCB — `app/app-authorization-ims-db2-mq/cpy/PADFLPCB.CPY` — computed length **291** bytes

Used by: DBUNLDGS

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| PADFLPCB | 01 |  | Group | PCB mask for IMS DB PAUTDFL (fraud flags) — fields per segment. | none in code |
| PADFL-DBDNAME | 05 | X(08) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PADFL-SEG-LEVEL | 05 | X(02) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PADFL-PCB-STATUS | 05 | X(02) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PADFL-PCB-PROCOPT | 05 | X(04) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| FILLER | 05 | S9(05) COMP | Binary (COMP) | Unused padding to record length. | none in code |
| PADFL-SEG-NAME | 05 | X(08) | Alphanumeric | PCB mask field for DL/I calls. | none in code |
| PADFL-KEYFB-NAME | 05 | S9(05) COMP | Binary (COMP) | PCB mask field for DL/I calls. | none in code |
| PADFL-NUM-SENSEGS | 05 | S9(05) COMP | Binary (COMP) | PCB mask field for DL/I calls. | none in code |
| PADFL-KEYFB | 05 | X(255) | Alphanumeric | PCB mask field for DL/I calls. | none in code |

## 13. Procedural copybooks

Copybooks containing PROCEDURE DIVISION code, no data fields.

### CSSETATY — `app/cpy/CSSETATY.cpy`

Used by: COACTUPC, COTRTUPC

Inline IF fragment: when `FLG-(field)-NOT-OK` or `-BLANK` and `CDEMO-PGM-REENTER`, moves DFHRED to the map output field attribute and `*` to the field — used with COPY … REPLACING substitutions (TESTVAR1/SCRNVAR2/MAPNAME3) by COACTUPC.

### CSSTRPFY — `app/cpy/CSSTRPFY.cpy`

Used by: COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC

Paragraph `YYYY-STORE-PFKEY`: EVALUATE mapping EIBAID (DFHENTER/DFHCLEAR/DFHPA1-2/DFHPF1-12) onto the CCARD-AID-* 88-levels in CVCRD01Y. Included by COACTUPC.

### CSUTLDPY — `app/cpy/CSUTLDPY.cpy`

Used by: COACTUPC

Date-validation paragraphs `EDIT-DATE-CCYYMMDD`, `EDIT-DAY`, `EDIT-MONTH`, `EDIT-YEAR`, `EDIT-DAY-MONTH-YEAR` (30/31-day and leap-year rules), `EDIT-DATE-LE` (final check by CALL CSUTLDTC/CEEDAYS) and `EDIT-DATE-OF-BIRTH` (not-in-future). Companion WORKING-STORAGE: CSUTLDWY.

### CSDB2RPY — `app/app-transaction-type-db2/cpy/CSDB2RPY.cpy`

Used by: COTRTLIC

`9998-PRIMING-QUERY` — `SELECT 1 FROM SYSIBM.SYSDUMMY1` connectivity probe; `9999-FORMAT-DB2-MESSAGE` — calls DSNTIAC to format SQLCA into WS-RETURN-MSG. Used by COTRTLIC.

## 14. Unused — UNUSED1Y

No program references this copybook (a CSUSR01Y-like layout).

### UNUSED1Y — `app/cpy/UNUSED1Y.cpy` — computed length **80** bytes

Used by: **no program references it**

| Field | Level | PIC / attributes | Type | Business meaning | Validation rules |
|---|---|---|---|---|---|
| UNUSED-DATA | 01 |  | Group | Unused record layout (no program references it). | none in code |
| UNUSED-ID | 05 | X(08) | Alphanumeric | Unused field (no program references this layout). | none in code |
| UNUSED-FNAME | 05 | X(20) | Alphanumeric | Unused field (no program references this layout). | none in code |
| UNUSED-LNAME | 05 | X(20) | Alphanumeric | Unused field (no program references this layout). | none in code |
| UNUSED-PWD | 05 | X(08) | Alphanumeric | Unused field (no program references this layout). | none in code |
| UNUSED-TYPE | 05 | X(01) | Alphanumeric | Unused field (no program references this layout). | none in code |
| UNUSED-FILLER | 05 | X(23) | Alphanumeric | Unused padding to record length. | none in code |

## PII and sensitive-data inventory

Fields that identify or could be used to harm a natural person, or that are regulated payment data, grouped by
sensitivity class. Classes: **Direct identifier** (name, SSN, government id), **Contact** (address, phone),
**Demographic / financial profile** (DOB, credit score, EFT account), **PAN / PCI** (card number, expiry, CVV —
PCI-DSS cardholder and sensitive-authentication data), **Account / customer key** (indirect identifier — links to a person
via CCXREF/CUSTDAT), **Credential** (passwords). Persistence column says where the field is *stored at rest*, not just
carried in memory.

### PII by copybook

| Copybook | Store / role | Direct identifier | Contact | Demographic / financial | PAN / PCI | Account / customer key | Credential |
|---|---|---|---|---|---|---|---|
| **CVCUS01Y** | CUSTDAT KSDS record | CUST-FIRST-NAME, CUST-MIDDLE-NAME, CUST-LAST-NAME, **CUST-SSN 9(09)**, CUST-GOVT-ISSUED-ID | CUST-ADDR-LINE-1/2/3, CUST-ADDR-STATE-CD, CUST-ADDR-COUNTRY-CD, CUST-ADDR-ZIP, CUST-PHONE-NUM-1/2 | CUST-DOB-YYYY-MM-DD, CUST-FICO-CREDIT-SCORE, CUST-EFT-ACCOUNT-ID (external bank account) | — | CUST-ID | — |
| **CUSTREC** | duplicate of CVCUS01Y (CBSTM03A) | same as CVCUS01Y | same | CUST-DOB-YYYYMMDD, FICO, EFT | — | CUST-ID | — |
| **CVACT02Y** | CARDDAT KSDS record | CARD-EMBOSSED-NAME | — | — | **CARD-NUM X(16)** (full PAN), **CARD-CVV-CD 9(03)**, CARD-EXPIRAION-DATE | CARD-ACCT-ID | — |
| **CVACT01Y** | ACCTDAT KSDS record | — | ACCT-ADDR-ZIP | balances / limits (financial, not PII per se) | — | ACCT-ID | — |
| **CVACT03Y** | CCXREF KSDS record | — | — | — | XREF-CARD-NUM (PAN) | XREF-CUST-ID, XREF-ACCT-ID | — |
| **CVTRA05Y** | TRANSACT KSDS record | — | — | purchase history (merchant name/city/zip, amount, timestamps) | TRAN-CARD-NUM (PAN) | — | — |
| **CVTRA06Y** | DALYTRAN.PS feed | — | — | same as CVTRA05Y | DALYTRAN-CARD-NUM (PAN) | — | — |
| **COSTM01** | TRXFL KSDS (statement temp) | — | — | purchase history | TRNX-CARD-NUM (PAN) | — | — |
| **CVTRA01Y** | TCATBALF KSDS | — | — | per-category balances | — | TRANCAT-ACCT-ID | — |
| **CVTRA07Y** | TRANREPT print lines | — | — | purchase detail lines (type/category, amount) | — (CBTRN03C breaks on TRAN-CARD-NUM but does not print it) | TRAN-REPORT-ACCOUNT-ID, TRAN-REPORT-TRANS-ID | — |
| **CVEXPORT** | EXPORT.DATA KSDS (multi-entity) | EXP-CUST-FIRST/MIDDLE/LAST-NAME, **EXP-CUST-SSN**, EXP-CUST-GOVT-ISSUED-ID, EXP-CARD-EMBOSSED-NAME | EXP-CUST-ADDR-LINE (×3), STATE, COUNTRY, ZIP, EXP-CUST-PHONE-NUM (×2), EXP-ACCT-ADDR-ZIP | EXP-CUST-DOB-YYYY-MM-DD, EXP-CUST-FICO-CREDIT-SCORE, EXP-CUST-EFT-ACCOUNT-ID | **EXP-CARD-NUM, EXP-CARD-CVV-CD, EXP-CARD-EXPIRAION-DATE**, EXP-XREF-CARD-NUM, EXP-TRAN-CARD-NUM | EXP-CUST-ID, EXP-ACCT-ID, EXP-XREF-*, EXP-CARD-ACCT-ID | — |
| **CSUSR01Y** | USRSEC KSDS record | SEC-USR-FNAME, SEC-USR-LNAME | — | — | — | SEC-USR-ID | **SEC-USR-PWD X(08) plaintext** |
| **UNUSED1Y** | (unreferenced) | UNUSED-FNAME, UNUSED-LNAME | — | — | — | UNUSED-ID | UNUSED-PWD |
| **COCOM01Y** | CICS COMMAREA (in flight between every screen) | CDEMO-CUST-FNAME/MNAME/LNAME | — | — | CDEMO-CARD-NUM 9(16) (PAN) | CDEMO-USER-ID, CDEMO-CUST-ID, CDEMO-ACCT-ID | — |
| **CVCRD01Y** | screen work area | — | — | — | CC-CARD-NUM / CC-CARD-NUM-N (PAN) | CC-ACCT-ID(-N), CC-CUST-ID(-N) | — |
| **CIPAUSMY** | IMS PAUTSUM0 root segment | — | — | credit/cash limits & balances | — | PA-ACCT-ID (COMP-3), PA-CUST-ID | — |
| **CIPAUDTY** | IMS PAUTDTL1 child segment | — | — | merchant / amount detail | **PA-CARD-NUM (PAN)**, PA-CARD-EXPIRY-DATE | — | — |
| **CCPAURQY** | MQ request (PAUTH.REQUEST) | — | — | merchant / amount | **PA-RQ-CARD-NUM (PAN)**, PA-RQ-CARD-EXPIRY-DATE | — | — |
| **CCPAURLY** | MQ reply (PAUTH.REPLY) | — | — | approved amount | PA-RL-CARD-NUM (PAN) | — | — |
| **CCPAUERY** | error log record | — | — | — | ERR-EVENT-KEY X(20) — loaded with `PA-CARD-NUM` / `XREF-CARD-NUM` in COPAUA0C 3100/5100 (PAN leaks into error logging) | — | — |

Copybooks with **no PII**: COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CODATECN, CSUTLDWY, CSLKPCDY (reference lists only),
CSDB2RWY, IMSFUNCS, PAUTBPCB, PASFLPCB, PADFLPCB, COMEN02Y, COADM02Y, CVTRA02Y, CVTRA03Y, CVTRA04Y, and the four procedural
copybooks.

### PII outside the `cpy/` copybooks

| Asset | PII present |
|---|---|
| BMS symbolic maps `cpy-bms/COACTUP`, `COACTVW` | Customer name, SSN (split `ACTSSN1I/2I/3I`), DOB, address, phones, FICO, account id, card number rendered on 3270 screens |
| `cpy-bms/COCRDLI`, `COCRDSL`, `COCRDUP` | Full card number, embossed name, expiry; CVV displayed/edited in card view/update (`CARD-CVV-CD-X` in COCRDSLC/COCRDUPC) |
| `cpy-bms/COUSR00/01/02/03`, `COSGN00` | User ids, names, **password field** |
| `cpy-bms/COTRN00/01/02`, `COBIL00`, `CORPT00`, `COPAU00/01` | Card number, account id, transaction detail |
| DB2 `CARDDEMO.AUTHFRDS` (`ddl/AUTHFRDS.ddl`, `dcl/AUTHFRDS.dcl`) | CARD_NUM CHAR(16) (PAN, also in PK and index XAUTHFRD), CARD_EXPIRY_DATE, ACCT_ID, CUST_ID, merchant detail |
| IMS `DBPAUTP0` / `DBPAUTX0` | PAUTSUM0 keyed on account id; PAUTDTL1 carries full PAN + expiry |
| MQ queues `AWS.M2.CARDDEMO.PAUTH.REQUEST/REPLY`, `CARDDEMO.REQUEST/RESPONSE.QUEUE` | PAN and expiry in clear-text CSV (auth); COACCT01 reply carries account id, status, balance, credit/cash limits as labelled text |
| Sample data `app/data/EBCDIC/*`, `app/data/ASCII/*` | CUSTDATA (names, SSN, DOB, addresses), CARDDATA (PAN + CVV), USRSEC (plaintext passwords), EXPORT.DATA, IMSDATA.DBPAUTP0 — synthetic demo data, but the same shapes as production |
| JCL `DUSRSECJ.jcl`, `ESDSRRDS.jcl` | In-stream user records including plaintext passwords |
| Batch outputs | `STATEMNT.PS/.HTML/.PDF` (name, full address, account id, FICO score, card-level transaction list), `TRANREPT(+1)` (account id + transaction id per line), `DALYREJS(+1)` (rejected transactions with PAN), `ACCTDATA.PSCOMP/ARRYPS/VBPS`, `*.IMPORT` files, `PAUTDB.ROOT/CHILD.*` unloads |
| SYSOUT | CBACT02C, CBACT03C, CBCUS01C `DISPLAY` whole card / xref / customer records (PAN, CVV, SSN) to the job log; CBTRN02C logs TCATBAL keys; CBPAUP0C debug mode displays account ids |

### Observations for the Java target

1. **Full PAN is stored in six places** (CARDDAT, CCXREF, TRANSACT, TRXFL, IMS PAUTDTL1, DB2 AUTHFRDS) plus the export
   file, statement and report outputs, and is the *primary key* of CARDDAT/CCXREF and half the PK of AUTHFRDS. Tokenising or
   surrogate-keying the card is a schema decision that must be made before the domain model is fixed (Wave 1 of the hotspot plan).
2. **CVV is persisted** (`CARD-CVV-CD` in CARDDAT and CVEXPORT, displayed by COCRDSLC/COCRDUPC). PCI-DSS Req. 3.2 prohibits
   storing sensitive authentication data after authorisation — this field should be dropped, not migrated.
3. **SSN and DOB are stored unmasked** in CUSTDAT and its export copy and are shown on COACTUP/COACTVW screens; the Java model
   needs field-level encryption / masked display and audited access.
4. **Plaintext credentials**: `SEC-USR-PWD` in USRSEC, in `DUSRSECJ` JCL, and echoed through the COUSR* screens. Replace with an
   identity provider; never migrate the field.
5. **Leakage paths to fix in code, not just storage**: PAN in `ERR-EVENT-KEY` error records, PAN/SSN/CVV in batch `DISPLAY`
   output, PAN in the `DALYREJS` reject GDG, account balances/limits over MQ in clear text in `COACCT01`.
6. `CDEMO-CARD-NUM 9(16)` / `CC-CARD-NUM-N` treat the PAN as numeric — a leading-zero or non-numeric token would break these;
   another reason to introduce a surrogate card id early.

## Cross-entity keys

| Key field | PIC / type | Appears in |
|---|---|---|
| Account id | `9(11)` zoned | CVACT01Y.ACCT-ID · CVACT02Y.CARD-ACCT-ID · CVACT03Y.XREF-ACCT-ID · CVTRA01Y.TRANCAT-ACCT-ID · COCOM01Y.CDEMO-ACCT-ID · CVCRD01Y.CC-ACCT-ID(-N) · CVTRA07Y.TRAN-REPORT-ACCOUNT-ID (X(11)) |
| Account id — **type mismatch** | `S9(11) COMP-3` packed (6 bytes) | CIPAUSMY.PA-ACCT-ID (IMS ACCNTID seq field) and DB2 AUTHFRDS.ACCT_ID DECIMAL(11) — the IMS/DB2 sub-app stores the same key packed where VSAM/commarea use zoned/char |
| Customer id | `9(09)` | CVCUS01Y/CUSTREC.CUST-ID · CVACT03Y.XREF-CUST-ID · COCOM01Y.CDEMO-CUST-ID · CVCRD01Y.CC-CUST-ID(-N) · CIPAUSMY.PA-CUST-ID · AUTHFRDS.CUST_ID DECIMAL(9) |
| Card number | `X(16)` | CVACT02Y.CARD-NUM · CVACT03Y.XREF-CARD-NUM · CVTRA05Y.TRAN-CARD-NUM · CVTRA06Y.DALYTRAN-CARD-NUM · COSTM01.TRNX-CARD-NUM · CIPAUDTY.PA-CARD-NUM · CCPAURQY/RLY.PA-R*/PA-RL-CARD-NUM · AUTHFRDS.CARD_NUM CHAR(16) |
| Card number — **type mismatch** | `9(16)` zoned | COCOM01Y.CDEMO-CARD-NUM and CVCRD01Y.CC-CARD-NUM-N treat it as numeric where files carry X(16) |
| Transaction id | `X(16)` | CVTRA05Y/06Y.TRAN-ID/DALYTRAN-ID · COSTM01.TRNX-ID · CVTRA07Y.TRAN-REPORT-TRANS-ID |
| Tran type + category | `X(02)` + `9(04)` | CVTRA01Y/02Y/04Y/05Y/06Y keys · DB2 TRANSACTION_TYPE_CATEGORY (CHAR(2)/CHAR(4)) |
| Disclosure group | `X(10)` | CVACT01Y.ACCT-GROUP-ID ↔ CVTRA02Y.DIS-ACCT-GROUP-ID |
| User id | `X(08)` | CSUSR01Y.SEC-USR-ID ↔ COCOM01Y.CDEMO-USER-ID |
| Amount / money | `S9(09)V99` or `S9(10)V99` | CVTRA05Y.TRAN-AMT, CVTRA01Y.TRAN-CAT-BAL, CVACT01Y balances (zoned) vs CIPAUSMY/CIPAUDTY PA-*-AMT (COMP-3) and AUTHFRDS DECIMAL(12,2) — same domain, different storage |
