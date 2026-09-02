# CardDemo COBOL Estate — Data Dictionary

Every field of every copybook in `app/cpy/` (30), `app/app-authorization-ims-db2-mq/cpy/` (10) and `app/app-transaction-type-db2/cpy/` (2). Field names, levels, `PIC` clauses, `USAGE`, `OCCURS`, `REDEFINES`, `VALUE` and `88` conditions were read from the source; **business meaning is inferred** from naming, program usage and header comments and is marked as such by being in the "Meaning (inferred)" column. BMS symbolic copybooks (`app/cpy-bms/`, `*/cpy-bms/`) are screen buffers generated from the maps and are summarised, not itemised.

## 0. Type-derivation rules and migration gotchas

| COBOL pattern | Derived Java type | Notes |
|---|---|---|
| `PIC X(n)` / `PIC A(n)` | `String` (fixed length *n*, space-padded) | Trim on read; re-pad on write if byte-compatible files are required. |
| `PIC 9(n)`, n ≤ 9 | `int` (or `Integer`) | Unsigned zoned decimal; leading zeros significant in keys (`ACCT-ID`, `CUST-ID`). |
| `PIC 9(n)`, 10 ≤ n ≤ 18 | `long` | e.g. `ACCT-ID 9(11)`, `CARD-NUM-N 9(16)`. Prefer `String` for identifiers that are never arithmetic. |
| `PIC S9(n)V99` | `BigDecimal` scale 2 | **`V` is an implied decimal point — no byte in storage.** All money fields in the estate are `S9(09)V99` or `S9(10)V99`. Sign is an overpunch on the last zoned digit. |
| `… COMP` / `BINARY` | `int`/`long`, big-endian two's complement | `S9(4) COMP`=2 bytes, `S9(5)`–`S9(9) COMP`=4 bytes, `S9(10)`–`S9(18) COMP`=8 bytes. |
| `… COMP-3` | `BigDecimal` (packed decimal) | Nibble-packed, sign nibble last. Present in IMS segments and `CVEXPORT`. |
| `PIC +9(10).99`, `-ZZZ,ZZZ,ZZZ.ZZ`, `----9` | `String` (edited numeric, display only) | Real `.` and `+`/`-`/`Z`/`,` occupy bytes; parse to `BigDecimal` only if re-ingested. |
| `88 NAME VALUE …` | `enum` / boolean predicate | Validation rule — listed per field below. |
| `X REDEFINES Y` | union/view | Same bytes, two interpretations; typical pattern is `X(n)` text with a `9(n)` numeric redefinition used for `IS NUMERIC` tests. |
| `FILLER X(n)` at record end | none | Pads to fixed LRECL (e.g. `ACCOUNT-RECORD` = 300 bytes, `CUSTOMER-RECORD` = 500, `TRAN-RECORD` = 350, `CARD-RECORD` = 150, `CARD-XREF-RECORD` = 50, `SEC-USER-DATA` = 80). Drop in Java entities, but preserve when writing legacy files. |

Cross-cutting gotchas:

1. **Dates/timestamps are text**: `X(10)` (`YYYY-MM-DD`, e.g. `ACCT-OPEN-DATE`, `CUST-DOB-YYYY-MM-DD`) and `X(26)` (`YYYY-MM-DD HH:MM:SS.ffffff`, e.g. `TRAN-ORIG-TS`, `TRAN-PROC-TS`). Programs slice them by offset (`ACCT-EXPIRAION-DATE(1:4)`, `DALYTRAN-ORIG-TS(1:10)`). Map to `LocalDate`/`LocalDateTime` with explicit formatters; blanks/low-values are possible. The authorization module uses a different convention: `X(06)` date + `X(06)` time and packed `S9(05)`/`S9(09) COMP-3` for the IMS key.
2. **Implied decimals (`V`)**: every balance/limit/amount/rate. `DIS-INT-RATE S9(04)V99` is an annual percentage; `CBACT04C` divides by 1200 for monthly interest.
3. **`FILLER` padding**: every VSAM record layout ends with a `FILLER` to the LRECL declared in the JCL DEFINE CLUSTER.
4. **Plain-text password**: `SEC-USR-PWD PIC X(08)` in `CSUSR01Y` is stored and compared in clear (`IF SEC-USR-PWD = WS-USER-PWD` in `COSGN00C.cbl`). Must be hashed in the target.
5. **Menu/lookup tables encoded as `FILLER VALUE` + `REDEFINES OCCURS`** (`COMEN02Y`, `COADM02Y`): program names are data, so navigation targets are only discoverable by reading the copybooks.
6. **Numeric-vs-text `REDEFINES`**: `CC-ACCT-ID X(11)` / `CC-ACCT-ID-N 9(11)`, `CC-CARD-NUM X(16)` / `-N 9(16)`, `WS-EDIT-DATE-*` — the `X` view is what is displayed/entered, the `9` view is used after an `IS NUMERIC` check.

---

## 1. Account — `CVACT01Y.cpy`

`01 ACCOUNT-RECORD` (300 bytes; VSAM KSDS `ACCTDAT`, key `ACCT-ID`).

| Field | PIC | Java type | Meaning (inferred) | Validation / notes |
|---|---|---|---|---|
| `ACCT-ID` | `9(11)` | `long` / `String` key | Account number (primary key) | 11 digits, zero-padded; joined to `XREF-ACCT-ID`, `CARD-ACCT-ID`, `TRANCAT-ACCT-ID` |
| `ACCT-ACTIVE-STATUS` | `X(01)` | `String`/enum | Active flag | Programs compare to `'Y'`/`'N'` (COACTUPC edits); no `88` in copybook |
| `ACCT-CURR-BAL` | `S9(10)V99` | `BigDecimal(12,2)` | Current balance | Updated by `CBTRN02C` (posting), `CBACT04C` (interest), `COBIL00C` (bill pay) |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `BigDecimal(12,2)` | Credit limit | Over-limit check in `CBTRN02C` (reason 102) and `COPAUA0C` |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `BigDecimal(12,2)` | Cash-advance limit | |
| `ACCT-OPEN-DATE` | `X(10)` | `LocalDate` | Open date `YYYY-MM-DD` | text date |
| `ACCT-EXPIRAION-DATE` | `X(10)` | `LocalDate` | Expiry date (sic) | `CBTRN02C`: reject 103 if `< DALYTRAN-ORIG-TS(1:10)` |
| `ACCT-REISSUE-DATE` | `X(10)` | `LocalDate` | Reissue date | text date |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `BigDecimal(12,2)` | Current-cycle credits | Over-limit uses `CYC-CREDIT - CYC-DEBIT + amount` |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `BigDecimal(12,2)` | Current-cycle debits | |
| `ACCT-ADDR-ZIP` | `X(10)` | `String` | ZIP of account address | |
| `ACCT-GROUP-ID` | `X(10)` | `String` | Disclosure/pricing group | FK to `DIS-ACCT-GROUP-ID`; `CBACT04C` falls back to `'DEFAULT'` |
| `FILLER` | `X(178)` | — | pad to 300 | |

## 2. Card — `CVACT02Y.cpy`, `CVCRD01Y.cpy`

### 2.1 `CVACT02Y.cpy` — `01 CARD-RECORD` (150 bytes; KSDS `CARDDAT` key `CARD-NUM`, AIX `CARDAIX` on `CARD-ACCT-ID`)

| Field | PIC | Java type | Meaning (inferred) | Validation / notes |
|---|---|---|---|---|
| `CARD-NUM` | `X(16)` | `String` | Card number (PAN), primary key | 16 chars; programs test `IS NUMERIC` via `CC-CARD-NUM-N` |
| `CARD-ACCT-ID` | `9(11)` | `long` | Owning account | AIX key |
| `CARD-CVV-CD` | `9(03)` | `int`/`String` | CVV | sensitive |
| `CARD-EMBOSSED-NAME` | `X(50)` | `String` | Name on card | `COCRDUPC` edits alphabetic+space |
| `CARD-EXPIRAION-DATE` | `X(10)` | `LocalDate` | Expiry `YYYY-MM-DD` | text date |
| `CARD-ACTIVE-STATUS` | `X(01)` | enum | Active flag | `'Y'`/`'N'` in `COCRDUPC` |
| `FILLER` | `X(59)` | — | pad to 150 | |

### 2.2 `CVCRD01Y.cpy` — `01 CC-WORK-AREAS` (online working storage, not a file)

| Field | PIC | Java type | Meaning (inferred) | Validation / notes |
|---|---|---|---|---|
| `CCARD-AID` | `X(5)` | enum | Last attention key | `88`: `CCARD-AID-ENTER 'ENTER'`, `-CLEAR 'CLEAR'`, `-PA1 'PA1 '`, `-PA2 'PA2 '`, `-PFK01…-PFK12 'PFK01'…'PFK12'` |
| `CCARD-NEXT-PROG` | `X(8)` | `String` | Next program for XCTL | |
| `CCARD-NEXT-MAPSET` / `CCARD-NEXT-MAP` | `X(7)` / `X(7)` | `String` | Next BMS mapset/map | |
| `CCARD-ERROR-MSG` | `X(75)` | `String` | Error text | |
| `CCARD-RETURN-MSG` | `X(75)` | `String` | Return text | `88 CCARD-RETURN-MSG-OFF VALUE LOW-VALUES` |
| `CC-ACCT-ID` / `CC-ACCT-ID-N` | `X(11)` / `9(11) REDEFINES` | `String`/`long` | Account id as typed / numeric view | numeric-vs-text redefinition |
| `CC-CARD-NUM` / `CC-CARD-NUM-N` | `X(16)` / `9(16) REDEFINES` | `String`/`long` | Card number typed / numeric | |
| `CC-CUST-ID` / `CC-CUST-ID-N` | `X(09)` / `9(9) REDEFINES` | `String`/`int` | Customer id typed / numeric | |

## 3. Card Xref — `CVACT03Y.cpy`

`01 CARD-XREF-RECORD` (50 bytes; KSDS `CCXREF` key `XREF-CARD-NUM`, AIX `CXACAIX` on `XREF-ACCT-ID`).

| Field | PIC | Java type | Meaning (inferred) | Notes |
|---|---|---|---|---|
| `XREF-CARD-NUM` | `X(16)` | `String` | Card number (key) | |
| `XREF-CUST-ID` | `9(09)` | `int` | Customer id | |
| `XREF-ACCT-ID` | `9(11)` | `long` | Account id | AIX key |
| `FILLER` | `X(14)` | — | pad to 50 | |

## 4. Customer — `CVCUS01Y.cpy` (and duplicate `CUSTREC.cpy`)

`01 CUSTOMER-RECORD` (500 bytes; KSDS `CUSTDAT` key `CUST-ID`). `CUSTREC.cpy` is byte-identical except the DOB field is named `CUST-DOB-YYYYMMDD` (used by `CBSTM03A`).

| Field | PIC | Java type | Meaning (inferred) | Validation (from `COACTUPC` edits) |
|---|---|---|---|---|
| `CUST-ID` | `9(09)` | `int` | Customer number (key) | |
| `CUST-FIRST-NAME` / `-MIDDLE-NAME` / `-LAST-NAME` | `X(25)` ×3 | `String` | Names | first/last mandatory alphabetic |
| `CUST-ADDR-LINE-1/2/3` | `X(50)` ×3 | `String` | Address lines | line 1 mandatory |
| `CUST-ADDR-STATE-CD` | `X(02)` | `String` | US state | `VALID-US-STATE-CODE` (`CSLKPCDY`) |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | `String` | Country | |
| `CUST-ADDR-ZIP` | `X(10)` | `String` | ZIP | state+zip2 combo `VALID-US-STATE-ZIP-CD2-COMBO` |
| `CUST-PHONE-NUM-1` / `-2` | `X(15)` ×2 | `String` | Phones `(AAA)NNN-NNNN` | area code `VALID-PHONE-AREA-CODE` |
| `CUST-SSN` | `9(09)` | `String` (PII) | SSN | `COACTUPC` edits part 1 ≠ 000/666/9xx, part 2 ≠ 00, part 3 ≠ 0000 |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | `String` | Govt id | |
| `CUST-DOB-YYYY-MM-DD` | `X(10)` | `LocalDate` | Date of birth | `EDIT-DATE-OF-BIRTH` in `CSUTLDPY`: valid date, not future |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | `String` | EFT/bank account | |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | enum | Primary holder | `'Y'`/`'N'` |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | `int` | FICO | `COACTUPC`: 300–850 |
| `FILLER` | `X(168)` | — | pad to 500 | |

## 5. Transaction — `CVTRA05Y.cpy`, `CVTRA06Y.cpy` (+ `COSTM01.CPY`)

### 5.1 `CVTRA05Y.cpy` — `01 TRAN-RECORD` (350 bytes; KSDS `TRANSACT` key `TRAN-ID`)

| Field | PIC | Java type | Meaning (inferred) | Notes |
|---|---|---|---|---|
| `TRAN-ID` | `X(16)` | `String` | Transaction id (key) | Online programs derive next id via `READPREV` + 1 |
| `TRAN-TYPE-CD` | `X(02)` | `String` | Type code | FK `TRAN-TYPE` (`CVTRA03Y`) / DB2 `TR_TYPE` |
| `TRAN-CAT-CD` | `9(04)` | `int` | Category code | FK `TRAN-CAT-CD` (`CVTRA04Y`) |
| `TRAN-SOURCE` | `X(10)` | `String` | Source system | `CBACT04C` writes `'System'`, `COBIL00C` `'POS TERM'` |
| `TRAN-DESC` | `X(100)` | `String` | Description | |
| `TRAN-AMT` | `S9(09)V99` | `BigDecimal(11,2)` | Amount (signed) | implied decimal |
| `TRAN-MERCHANT-ID` | `9(09)` | `int` | Merchant id | |
| `TRAN-MERCHANT-NAME` / `-CITY` | `X(50)` ×2 | `String` | Merchant | |
| `TRAN-MERCHANT-ZIP` | `X(10)` | `String` | Merchant ZIP | |
| `TRAN-CARD-NUM` | `X(16)` | `String` | Card | AIX in `TRANSACT.VSAM.AIX` (`TRANIDX.jcl`) |
| `TRAN-ORIG-TS` / `TRAN-PROC-TS` | `X(26)` ×2 | `LocalDateTime` | Origination / processing timestamps | text timestamps |
| `FILLER` | `X(20)` | — | pad to 350 | |

### 5.2 `CVTRA06Y.cpy` — `01 DALYTRAN-RECORD` (350 bytes; sequential `DALYTRAN.PS`)

Same 13 fields and PICs as `TRAN-RECORD` with prefix `DALYTRAN-` (`DALYTRAN-ID X(16)`, `DALYTRAN-TYPE-CD X(02)`, `DALYTRAN-CAT-CD 9(04)`, `DALYTRAN-SOURCE X(10)`, `DALYTRAN-DESC X(100)`, `DALYTRAN-AMT S9(09)V99`, `DALYTRAN-MERCHANT-ID 9(09)`, `DALYTRAN-MERCHANT-NAME X(50)`, `DALYTRAN-MERCHANT-CITY X(50)`, `DALYTRAN-MERCHANT-ZIP X(10)`, `DALYTRAN-CARD-NUM X(16)`, `DALYTRAN-ORIG-TS X(26)`, `DALYTRAN-PROC-TS X(26)`, `FILLER X(20)`). Inbound feed for `CBTRN02C`; rejects are written as this record + 80-byte reason suffix (`WS-VALIDATION-FAIL-REASON 9(04)` + `-DESC X(76)`) to `DALYREJS`.

### 5.3 `COSTM01.CPY` — `01 TRNX-RECORD` (statement input, `TRXFL.VSAM.KSDS`)

Re-keyed view produced by `CREASTMT` SORT `OUTREC FIELDS=(1:263,16,17:1,262,279:279,50)`: `TRNX-KEY` = `TRNX-CARD-NUM X(16)` + `TRNX-ID X(16)`, then `TRNX-TYPE-CD X(02)`, `TRNX-CAT-CD 9(04)`, `TRNX-SOURCE X(10)`, `TRNX-DESC X(100)`, `TRNX-AMT S9(09)V99`, `TRNX-MERCHANT-ID 9(09)`, `TRNX-MERCHANT-NAME X(50)`, `TRNX-MERCHANT-CITY X(50)`, `TRNX-MERCHANT-ZIP X(10)`, `TRNX-ORIG-TS X(26)`, `TRNX-PROC-TS X(26)`, `FILLER X(20)`.

## 6. Reference / lookup — `CVTRA01Y`–`CVTRA04Y`, `CVTRA07Y`

| Copybook / record | Field | PIC | Java type | Meaning (inferred) / notes |
|---|---|---|---|---|
| `CVTRA01Y.cpy` `01 TRAN-CAT-BAL-RECORD` (50 bytes; KSDS `TCATBALF`) | `TRANCAT-ACCT-ID` | `9(11)` | `long` | key part 1 — account |
| | `TRANCAT-TYPE-CD` | `X(02)` | `String` | key part 2 — type |
| | `TRANCAT-CD` | `9(04)` | `int` | key part 3 — category |
| | `TRAN-CAT-BAL` | `S9(09)V99` | `BigDecimal` | running balance per account/type/category; interest base for `CBACT04C` |
| | `FILLER` | `X(22)` | — | |
| `CVTRA02Y.cpy` `01 DIS-GROUP-RECORD` (50 bytes; KSDS `DISCGRP`) | `DIS-ACCT-GROUP-ID` | `X(10)` | `String` | key part 1 — group (`'DEFAULT'` fallback) |
| | `DIS-TRAN-TYPE-CD` | `X(02)` | `String` | key part 2 |
| | `DIS-TRAN-CAT-CD` | `9(04)` | `int` | key part 3 |
| | `DIS-INT-RATE` | `S9(04)V99` | `BigDecimal(6,2)` | annual interest rate %, `/1200` per month |
| | `FILLER` | `X(28)` | — | |
| `CVTRA03Y.cpy` `01 TRAN-TYPE-RECORD` (60 bytes; KSDS `TRANTYPE`) | `TRAN-TYPE` | `X(02)` | `String` | key; mirrors DB2 `TR_TYPE CHAR(2)` |
| | `TRAN-TYPE-DESC` | `X(50)` | `String` | mirrors `TR_DESCRIPTION VARCHAR(50)` |
| | `FILLER` | `X(08)` | — | |
| `CVTRA04Y.cpy` `01 TRAN-CAT-RECORD` (60 bytes; KSDS `TRANCATG`) | `TRAN-TYPE-CD` | `X(02)` | `String` | key part 1; mirrors `TRC_TYPE_CODE` |
| | `TRAN-CAT-CD` | `9(04)` | `int` | key part 2; mirrors `TRC_TYPE_CATEGORY CHAR(4)` |
| | `TRAN-CAT-TYPE-DESC` | `X(50)` | `String` | mirrors `TRC_CAT_DATA` |
| | `FILLER` | `X(04)` | — | |
| `CVTRA07Y.cpy` (report print lines for `CBTRN03C`) | `REPORT-NAME-HEADER`: `REPT-SHORT-NAME X(38) VALUE 'DALYREPT'`, `REPT-LONG-NAME X(41) VALUE 'Daily Transaction Report'`, `REPT-DATE-HEADER X(12)`, `REPT-START-DATE X(10)`, `FILLER X(04) VALUE ' to '`, `REPT-END-DATE X(10)` | | `String` | header line |
| | `TRANSACTION-DETAIL-REPORT`: `TRAN-REPORT-TRANS-ID X(16)`, `TRAN-REPORT-ACCOUNT-ID X(11)`, `TRAN-REPORT-TYPE-CD X(02)`, `TRAN-REPORT-TYPE-DESC X(15)`, `TRAN-REPORT-CAT-CD 9(04)`, `TRAN-REPORT-CAT-DESC X(29)`, `TRAN-REPORT-SOURCE X(10)`, `TRAN-REPORT-AMT -ZZZ,ZZZ,ZZZ.ZZ`, separators `FILLER X(01)`/`X(04)`/`X(02)` | | `String` | 133-byte detail line; amount is edited numeric |
| | `TRANSACTION-HEADER-1` (6 `FILLER VALUE` captions), `TRANSACTION-HEADER-2 X(133) VALUE ALL '-'` | | | column headings |
| | `REPORT-PAGE-TOTALS` / `REPORT-ACCOUNT-TOTALS` / `REPORT-GRAND-TOTALS`: caption `FILLER`, `FILLER X(86|84|86) VALUE ALL ' '`, `REPT-PAGE-TOTAL` / `REPT-ACCOUNT-TOTAL` / `REPT-GRAND-TOTAL` `+ZZZ,ZZZ,ZZZ.ZZ` | | `String` | totals; edited numeric with sign |

## 7. User security — `CSUSR01Y.cpy` (and `UNUSED1Y.cpy`)

`01 SEC-USER-DATA` (80 bytes; KSDS `USRSEC` key `SEC-USR-ID`).

| Field | PIC | Java type | Meaning (inferred) | Notes |
|---|---|---|---|---|
| `SEC-USR-ID` | `X(08)` | `String` | User id (key) | |
| `SEC-USR-FNAME` / `SEC-USR-LNAME` | `X(20)` ×2 | `String` | Names | |
| **`SEC-USR-PWD`** | `X(08)` | `char[]`→hash | **Plain-text password** | compared literally in `COSGN00C`; 8-char max, case-sensitive as stored |
| `SEC-USR-TYPE` | `X(01)` | enum | Role | `'A'` admin / `'U'` user (88s live in `COCOM01Y`: `CDEMO-USRTYP-ADMIN`, `CDEMO-USRTYP-USER`) |
| `SEC-USR-FILLER` | `X(23)` | — | pad to 80 | |

`UNUSED1Y.cpy` — `01 UNUSED-DATA` is a field-for-field clone (`UNUSED-ID X(08)`, `UNUSED-FNAME X(20)`, `UNUSED-LNAME X(20)`, `UNUSED-PWD X(08)`, `UNUSED-TYPE X(01)`, `UNUSED-FILLER X(23)`) referenced by no program — dead code.

## 8. Utility / common layouts

### 8.1 `COCOM01Y.cpy` — `01 CARDDEMO-COMMAREA` (CICS COMMAREA passed on every XCTL)

| Field | PIC | Java type | Meaning (inferred) | 88s |
|---|---|---|---|---|
| `CDEMO-FROM-TRANID` / `CDEMO-TO-TRANID` | `X(04)` | `String` | Source/target CICS tranid | |
| `CDEMO-FROM-PROGRAM` / `CDEMO-TO-PROGRAM` | `X(08)` | `String` | Source/target program (XCTL target) | |
| `CDEMO-USER-ID` | `X(08)` | `String` | Signed-on user | |
| `CDEMO-USER-TYPE` | `X(01)` | enum | Role | `CDEMO-USRTYP-ADMIN 'A'`, `CDEMO-USRTYP-USER 'U'` |
| `CDEMO-PGM-CONTEXT` | `9(01)` | enum | First entry vs re-entry | `CDEMO-PGM-ENTER 0`, `CDEMO-PGM-REENTER 1` |
| `CDEMO-CUST-ID` | `9(09)` | `int` | Selected customer | |
| `CDEMO-CUST-FNAME` / `-MNAME` / `-LNAME` | `X(25)` ×3 | `String` | | |
| `CDEMO-ACCT-ID` | `9(11)` | `long` | Selected account | |
| `CDEMO-ACCT-STATUS` | `X(01)` | `String` | | |
| `CDEMO-CARD-NUM` | `9(16)` | `long`/`String` | Selected card | |
| `CDEMO-LAST-MAP` / `CDEMO-LAST-MAPSET` | `X(7)` ×2 | `String` | Return-to screen | |

### 8.2 `COMEN02Y.cpy` — `01 CARDDEMO-MAIN-MENU-OPTIONS`

`CDEMO-MENU-OPT-COUNT 9(02) VALUE 11`; `CDEMO-MENU-OPTIONS-DATA` = 11 × (`FILLER 9(02)` option no., `FILLER X(35)` label, `FILLER X(08)` program, `FILLER X(01)` user type `'U'`) redefined by `CDEMO-MENU-OPTIONS` → `CDEMO-MENU-OPT OCCURS 12` (`CDEMO-MENU-OPT-NUM 9(02)`, `-NAME X(35)`, `-PGMNAME X(08)`, `-USRTYPE X(01)`). Programs in order: `COACTVWC`, `COACTUPC`, `COCRDLIC`, `COCRDSLC`, `COCRDUPC`, `COTRN00C`, `COTRN01C`, `COTRN02C`, `CORPT00C`, `COBIL00C`, `COPAUS0C`. (OCCURS 12 > 11 populated entries; slot 12 is uninitialised.)

### 8.3 `COADM02Y.cpy` — `01 CARDDEMO-ADMIN-MENU-OPTIONS`

`CDEMO-ADMIN-OPT-COUNT 9(02) VALUE 6`; 6 × (`FILLER 9(02)`, `FILLER X(35)`, `FILLER X(08)`) redefined by `CDEMO-ADMIN-OPT OCCURS 9` (`CDEMO-ADMIN-OPT-NUM 9(02)`, `-NAME X(35)`, `-PGMNAME X(08)`). Programs: `COUSR00C`, `COUSR01C`, `COUSR02C`, `COUSR03C`, `COTRTLIC`, `COTRTUPC`.

### 8.4 `CSUTLDWY.cpy` — date-edit working storage (used with `CSUTLDPY.cpy` procedures)

| Field | PIC | Java type | Meaning / 88s |
|---|---|---|---|
| `WS-EDIT-DATE-CCYYMMDD` group → `WS-EDIT-DATE-CCYY` (`WS-EDIT-DATE-CC X(2)` / `-CC-N 9(2) REDEFINES`; `WS-EDIT-DATE-YY X(2)` / `-YY-N 9(2)`), `WS-EDIT-DATE-CCYY-N 9(4) REDEFINES`, `WS-EDIT-DATE-MM X(2)` / `-MM-N 9(2)`, `WS-EDIT-DATE-DD X(2)` / `-DD-N 9(2)`; `WS-EDIT-DATE-CCYYMMDD-N 9(8) REDEFINES` whole | text/numeric pairs | `LocalDate` | 88s: `THIS-CENTURY 20`, `LAST-CENTURY 19`; `WS-VALID-MONTH 1 THRU 12`, `WS-31-DAY-MONTH 1,3,5,7,8,10,12`, `WS-FEBRUARY 2`; `WS-VALID-DAY 1 THRU 31`, `WS-DAY-31 31`, `WS-DAY-30 30`, `WS-DAY-29 29`, `WS-VALID-FEB-DAY 1 THRU 28` |
| `WS-EDIT-DATE-BINARY` | `S9(9) BINARY` | `int` | Lilian day from `CEEDAYS` |
| `WS-CURRENT-DATE-YYYYMMDD` / `-N REDEFINES` / `WS-CURRENT-DATE-BINARY S9(9) BINARY` | `X(8)`/`9(8)`/binary | | today, for DOB-not-in-future check |
| `WS-EDIT-DATE-FLGS` (`WS-EDIT-YEAR-FLG`, `WS-EDIT-MONTH`, `WS-EDIT-DAY` each `X(01)`) | | enum | `WS-EDIT-DATE-IS-VALID LOW-VALUES`, `WS-EDIT-DATE-IS-INVALID '000'`; per part `FLG-*-ISVALID LOW-VALUES`, `FLG-*-NOT-OK '0'`, `FLG-*-BLANK 'B'` |
| `WS-DATE-FORMAT` | `X(08) VALUE 'YYYYMMDD'` | `String` | mask passed to `CSUTLDTC` |
| `WS-DATE-VALIDATION-RESULT`: `WS-SEVERITY X(04)` / `-N 9(4) REDEFINES`, `WS-MSG-NO X(04)` / `-N 9(4) REDEFINES`, `WS-RESULT X(15)`, `WS-DATE X(10)`, `WS-DATE-FMT X(10)`, caption `FILLER`s | | | return area of `CSUTLDTC` |

### 8.5 `CSLKPCDY.cpy` — lookup code tables (all `88 VALUE` lists)

| Field | PIC | Java type | 88 conditions |
|---|---|---|---|
| `WS-US-PHONE-AREA-CODE-TO-EDIT` | `XXX` | `String` | `VALID-PHONE-AREA-CODE` (NANPA list, ~300 values '201'…'989'), `VALID-GENERAL-PURP-CODE`, `VALID-EASY-RECOG-AREA-CODE` ('200','211','222',…) |
| `US-STATE-CODE-TO-EDIT` | `X(2)` | `String` | `VALID-US-STATE-CODE` ('AL','AK',…,'WY' + territories) |
| `US-STATE-ZIPCODE-TO-EDIT` → `US-STATE-AND-FIRST-ZIP2 X(4)`, `LAST-3-OF-ZIP X(3)` | `X(4)`+`X(3)` | `String` | `VALID-US-STATE-ZIP-CD2-COMBO` ('AA34','AE90',…) |

### 8.6 `CODATECN.cpy` — `01 CODATECN-REC` (parameter to asm `COBDATFT`)

`CODATECN-IN-REC`: `CODATECN-TYPE X` (88 `YYYYMMDD-IN "1"`, `YYYY-MM-DD-IN "2"`), `CODATECN-INP-DATE X(20)` with two `REDEFINES` views (`CODATECN-1INP`: `-1YYYY XXXX`, `-1MM XX`, `-1DD XX`, `-1FIL X(12)`; `CODATECN-2INP`: `-1O-YYYY XXXX`, `-1I-S1 X`, `-1MM XX`, `-1I-S2 X`, `-2YY XX`, `-2FIL X(10)`). `CODATECN-OUT-REC`: `CODATECN-OUTTYPE X` (88 `YYYY-MM-DD-OP "1"`, `YYYYMMDD-OP "2"`), `CODATECN-0UT-DATE X(20)` (note zero in name) with `CODATECN-1OUT`/`CODATECN-2OUT` redefinitions, `CODATECN-ERROR-MSG X(38)`.

### 8.7 `CSDAT01Y.cpy` — `01 WS-DATE-TIME`

`WS-CURDATE` (`-YEAR 9(04)`, `-MONTH 9(02)`, `-DAY 9(02)`) / `WS-CURDATE-N 9(08) REDEFINES`; `WS-CURTIME` (`-HOURS`, `-MINUTE`, `-SECOND`, `-MILSEC` each `9(02)`) / `WS-CURTIME-N 9(08) REDEFINES`; display forms `WS-CURDATE-MM-DD-YY` (`9(02)` parts with `FILLER X(01) VALUE '/'`), `WS-CURTIME-HH-MM-SS` (`':'` separators), `WS-TIMESTAMP` (`YYYY-MM-DD HH:MM:SS.` + `WS-TIMESTAMP-TM-MS6 9(06)`) — 26-byte DB2-style timestamp assembled from parts.

### 8.8 Message / title / abend

* `COTTL01Y.cpy` `01 CCDA-SCREEN-TITLE`: `CCDA-TITLE01 X(40)`, `CCDA-TITLE02 X(40)`, `CCDA-THANK-YOU X(40)` (constants).
* `CSMSG01Y.cpy` `01 CCDA-COMMON-MESSAGES`: `CCDA-MSG-THANK-YOU X(50)`, `CCDA-MSG-INVALID-KEY X(50)`.
* `CSMSG02Y.cpy` `01 ABEND-DATA`: `ABEND-CODE X(4)`, `ABEND-CULPRIT X(8)`, `ABEND-REASON X(50)`, `ABEND-MSG X(72)` (all `VALUE SPACES`).

### 8.9 Procedure copybooks (no data fields)

* `CSSETATY.cpy` — `COPY … REPLACING` template that sets BMS attribute (`DFHRED`, `'*'`) for a field flagged `FLG-(TESTVAR1)-NOT-OK`/`-BLANK` on re-entry.
* `CSSTRPFY.cpy` — paragraph `YYYY-STORE-PFKEY`: `EVALUATE EIBAID` → sets `CCARD-AID-*` 88s.
* `CSUTLDPY.cpy` — paragraphs `EDIT-DATE-CCYYMMDD`, `EDIT-YEAR-CCYY`, `EDIT-MONTH`, `EDIT-DAY`, `EDIT-DAY-MONTH-YEAR` (30/31-day and leap-year checks), `EDIT-DATE-LE` (calls `CSUTLDTC`), `EDIT-DATE-OF-BIRTH`.

### 8.10 `CVEXPORT.cpy` — `01 EXPORT-RECORD` (polymorphic branch-migration record, `CBEXPORT`/`CBIMPORT`)

Header: `EXPORT-REC-TYPE X(1)` (values set by `CBEXPORT`: `'C'` customer, `'A'` account, `'X'` xref, `'T'` transaction, `'D'` card — no 88s), `EXPORT-TIMESTAMP X(26)` (`REDEFINES` → `EXPORT-DATE X(10)`, `EXPORT-DATE-TIME-SEP X(1)`, `EXPORT-TIME X(15)`), `EXPORT-SEQUENCE-NUM 9(9) COMP`, `EXPORT-BRANCH-ID X(4)`, `EXPORT-REGION-CODE X(5)`, `EXPORT-RECORD-DATA X(460)` redefined five ways:

| View (`REDEFINES EXPORT-RECORD-DATA`) | Fields (PIC → Java) |
|---|---|
| `EXPORT-CUSTOMER-DATA` | `EXP-CUST-ID 9(09) COMP`→int; names `X(25)`×3; `EXP-CUST-ADDR-LINES OCCURS 3` of `X(50)`; `-STATE-CD X(02)`, `-COUNTRY-CD X(03)`, `-ZIP X(10)`; `EXP-CUST-PHONE-NUMS OCCURS 2` of `X(15)`; `EXP-CUST-SSN 9(09)`; `-GOVT-ISSUED-ID X(20)`; `-DOB-YYYY-MM-DD X(10)`; `-EFT-ACCOUNT-ID X(10)`; `-PRI-CARD-HOLDER-IND X(01)`; `EXP-CUST-FICO-CREDIT-SCORE 9(03) COMP-3`; `FILLER X(134)` |
| `EXPORT-ACCOUNT-DATA` | `EXP-ACCT-ID 9(11)`; `-ACTIVE-STATUS X(01)`; `EXP-ACCT-CURR-BAL S9(10)V99 COMP-3`; `-CREDIT-LIMIT S9(10)V99` (display); `-CASH-CREDIT-LIMIT S9(10)V99 COMP-3`; dates `X(10)`×3; `-CURR-CYC-CREDIT S9(10)V99`; `-CURR-CYC-DEBIT S9(10)V99 COMP`; `-ADDR-ZIP X(10)`; `-GROUP-ID X(10)`; `FILLER X(352)`. **Mixed DISPLAY/COMP/COMP-3 for the same business quantity — high-risk for byte-level conversion.** |
| `EXPORT-TRANSACTION-DATA` | as `TRAN-RECORD` but `EXP-TRAN-AMT S9(09)V99 COMP-3`, `EXP-TRAN-MERCHANT-ID 9(09) COMP`; `FILLER X(140)` |
| `EXPORT-CARD-XREF-DATA` | `EXP-XREF-CARD-NUM X(16)`, `EXP-XREF-CUST-ID 9(09)`, `EXP-XREF-ACCT-ID 9(11) COMP`; `FILLER X(427)` |
| `EXPORT-CARD-DATA` | `EXP-CARD-NUM X(16)`, `EXP-CARD-ACCT-ID 9(11) COMP`, `EXP-CARD-CVV-CD 9(03) COMP`, `-EMBOSSED-NAME X(50)`, `-EXPIRAION-DATE X(10)`, `-ACTIVE-STATUS X(01)`; `FILLER X(373)` |

---

## 9. Authorization module copybooks — `app/app-authorization-ims-db2-mq/cpy/`

### 9.1 `CIPAUSMY.cpy` — IMS root segment `PAUTSUM0` (pending-authorization summary per account)

| Field | PIC | Java type | Meaning (inferred) |
|---|---|---|---|
| `PA-ACCT-ID` | `S9(11) COMP-3` | `long` | key — account |
| `PA-CUST-ID` | `9(09)` | `int` | customer |
| `PA-AUTH-STATUS` | `X(01)` | enum | account auth status |
| `PA-ACCOUNT-STATUS` | `X(02) OCCURS 5` | `String[5]` | status codes |
| `PA-CREDIT-LIMIT`, `PA-CASH-LIMIT`, `PA-CREDIT-BALANCE`, `PA-CASH-BALANCE` | `S9(09)V99 COMP-3` | `BigDecimal` | limits/balances snapshot |
| `PA-APPROVED-AUTH-CNT`, `PA-DECLINED-AUTH-CNT` | `S9(04) COMP` | `short`/`int` | counters |
| `PA-APPROVED-AUTH-AMT`, `PA-DECLINED-AUTH-AMT` | `S9(09)V99 COMP-3` | `BigDecimal` | running totals |
| `FILLER` | `X(34)` | — | |

### 9.2 `CIPAUDTY.cpy` — IMS child segment `PAUTDTL1` (one authorization)

| Field | PIC | Java type | Meaning (inferred) / 88s |
|---|---|---|---|
| `PA-AUTHORIZATION-KEY`: `PA-AUTH-DATE-9C S9(05) COMP-3`, `PA-AUTH-TIME-9C S9(09) COMP-3` | packed | key | packed date/time (stored as 9's complement per name — *inferred* to give descending order) |
| `PA-AUTH-ORIG-DATE` / `PA-AUTH-ORIG-TIME` | `X(06)` ×2 | `LocalDateTime` | `YYMMDD` / `HHMMSS` text |
| `PA-CARD-NUM` | `X(16)` | `String` | |
| `PA-AUTH-TYPE` | `X(04)` | `String` | |
| `PA-CARD-EXPIRY-DATE` | `X(04)` | `String` | `MMYY`/`YYMM` 4-char (differs from `X(10)` elsewhere) |
| `PA-MESSAGE-TYPE` / `PA-MESSAGE-SOURCE` | `X(06)` ×2 | `String` | |
| `PA-AUTH-ID-CODE` | `X(06)` | `String` | approval code |
| `PA-AUTH-RESP-CODE` | `X(02)` | enum | `88 PA-AUTH-APPROVED '00'` |
| `PA-AUTH-RESP-REASON` | `X(04)` | `String` | decline reason |
| `PA-PROCESSING-CODE` | `9(06)` | `int` | |
| `PA-TRANSACTION-AMT`, `PA-APPROVED-AMT` | `S9(10)V99 COMP-3` | `BigDecimal(12,2)` | matches DB2 `DECIMAL(12,2)` |
| `PA-MERCHANT-CATAGORY-CODE X(04)`, `PA-ACQR-COUNTRY-CODE X(03)`, `PA-POS-ENTRY-MODE 9(02)`, `PA-MERCHANT-ID X(15)`, `PA-MERCHANT-NAME X(22)`, `PA-MERCHANT-CITY X(13)`, `PA-MERCHANT-STATE X(02)`, `PA-MERCHANT-ZIP X(09)`, `PA-TRANSACTION-ID X(15)` | | `String`/`int` | merchant/ISO-8583-style data |
| `PA-MATCH-STATUS` | `X(01)` | enum | `PA-MATCH-PENDING 'P'`, `PA-MATCH-AUTH-DECLINED 'D'`, `PA-MATCH-PENDING-EXPIRED 'E'`, `PA-MATCHED-WITH-TRAN 'M'` |
| `PA-AUTH-FRAUD` | `X(01)` | enum | `PA-FRAUD-CONFIRMED 'F'`, `PA-FRAUD-REMOVED 'R'` |
| `PA-FRAUD-RPT-DATE` | `X(08)` | `LocalDate` | |
| `FILLER` | `X(17)` | — | |

### 9.3 `CCPAURQY.cpy` — MQ authorization request (`05`-level, host in `COPAUA0C`)

`PA-RQ-AUTH-DATE X(06)`, `PA-RQ-AUTH-TIME X(06)`, `PA-RQ-CARD-NUM X(16)`, `PA-RQ-AUTH-TYPE X(04)`, `PA-RQ-CARD-EXPIRY-DATE X(04)`, `PA-RQ-MESSAGE-TYPE X(06)`, `PA-RQ-MESSAGE-SOURCE X(06)`, `PA-RQ-PROCESSING-CODE 9(06)`, **`PA-RQ-TRANSACTION-AMT PIC +9(10).99`** (edited numeric with real sign and decimal point — 14 bytes, `String`→`BigDecimal`), `PA-RQ-MERCHANT-CATAGORY-CODE X(04)`, `PA-RQ-ACQR-COUNTRY-CODE X(03)`, `PA-RQ-POS-ENTRY-MODE 9(02)`, `PA-RQ-MERCHANT-ID X(15)`, `PA-RQ-MERCHANT-NAME X(22)`, `PA-RQ-MERCHANT-CITY X(13)`, `PA-RQ-MERCHANT-STATE X(02)`, `PA-RQ-MERCHANT-ZIP X(09)`, `PA-RQ-TRANSACTION-ID X(15)`.

### 9.4 `CCPAURLY.cpy` — MQ authorization reply

`PA-RL-CARD-NUM X(16)`, `PA-RL-TRANSACTION-ID X(15)`, `PA-RL-AUTH-ID-CODE X(06)`, `PA-RL-AUTH-RESP-CODE X(02)`, `PA-RL-AUTH-RESP-REASON X(04)`, `PA-RL-APPROVED-AMT PIC +9(10).99` (edited).

### 9.5 `CCPAUERY.cpy` — `01 ERROR-LOG-RECORD`

`ERR-DATE X(06)`, `ERR-TIME X(06)`, `ERR-APPLICATION X(08)`, `ERR-PROGRAM X(08)`, `ERR-LOCATION X(04)`, `ERR-LEVEL X(01)` (88 `ERR-LOG 'L'`, `ERR-INFO 'I'`, `ERR-WARNING 'W'`, `ERR-CRITICAL 'C'`), `ERR-SUBSYSTEM X(01)` (88 `ERR-APP 'A'`, `ERR-CICS 'C'`, `ERR-IMS 'I'`, `ERR-DB2 'D'`, `ERR-MQ 'M'`, `ERR-FILE 'F'`), `ERR-CODE-1 X(09)`, `ERR-CODE-2 X(09)`, `ERR-MESSAGE X(50)`, `ERR-EVENT-KEY X(20)`.

### 9.6 IMS plumbing — `IMSFUNCS.cpy`, `PAUTBPCB.CPY`, `PASFLPCB.CPY`, `PADFLPCB.CPY`

* `IMSFUNCS` `01 FUNC-CODES`: `FUNC-GU`, `FUNC-GHU`, `FUNC-GN`, `FUNC-GHN`, `FUNC-GNP`, `FUNC-GHNP`, `FUNC-REPL`, `FUNC-ISRT`, `FUNC-DLET` all `X(04)` constants; `PARMCOUNT S9(05) COMP-5 VALUE +4`.
* `PAUTBPCB` / `PASFLPCB` / `PADFLPCB` — standard DL/I PCB masks: `*-DBDNAME X(08)`, `*-SEG-LEVEL X(02)`, `*-PCB-STATUS X(02)` (`'  '` = OK, `'GB'` = end, `'GE'` = not found — IMS convention, *inferred*), `*-PCB-PROCOPT X(04)`, `FILLER S9(05) COMP`, `*-SEG-NAME X(08)`, `*-KEYFB-NAME S9(05) COMP`, `*-NUM-SENSEGS S9(05) COMP`, `*-KEYFB X(255)` (`PASFL-KEYFB X(100)`).

### 9.7 DB2 `dcl/AUTHFRDS.dcl` → table `CARDDEMO.AUTHFRDS`

`CARD_NUM CHAR(16) NOT NULL`, `AUTH_TS TIMESTAMP NOT NULL` (PK with `CARD_NUM`), `AUTH_TYPE CHAR(4)`, `CARD_EXPIRY_DATE CHAR(4)`, `MESSAGE_TYPE CHAR(6)`, `MESSAGE_SOURCE CHAR(6)`, `AUTH_ID_CODE CHAR(6)`, `AUTH_RESP_CODE CHAR(2)`, `AUTH_RESP_REASON CHAR(4)`, `PROCESSING_CODE CHAR(6)`, `TRANSACTION_AMT DECIMAL(12,2)`, `APPROVED_AMT DECIMAL(12,2)`, `MERCHANT_CATAGORY_CODE CHAR(4)`, `ACQR_COUNTRY_CODE CHAR(3)`, `POS_ENTRY_MODE SMALLINT`, `MERCHANT_ID CHAR(15)`, `MERCHANT_NAME VARCHAR(22)`, `MERCHANT_CITY CHAR(13)`, `MERCHANT_STATE CHAR(2)`, `MERCHANT_ZIP CHAR(9)`, `TRANSACTION_ID CHAR(15)`, `MATCH_STATUS CHAR(1)`, `AUTH_FRAUD CHAR(1)`, `FRAUD_RPT_DATE DATE`, `ACCT_ID DECIMAL(11,0)`, `CUST_ID DECIMAL(9,0)`. Column-for-column copy of `PAUTDTL1` — the same authorization exists in IMS and DB2 once flagged as fraud.

---

## 10. Transaction-type module copybooks — `app/app-transaction-type-db2/cpy/`

* `CSDB2RWY.cpy` (`05 WS-DB2-COMMON-VARS`): `WS-DISP-SQLCODE PIC ----9` (edited), `WS-DUMMY-DB2-INT S9(4) COMP-3`, `WS-DB2-PROCESSING-FLAG X(1)` (88 `WS-DB2-OK '0'`, `WS-DB2-ERROR '1'`), `WS-DB2-CURRENT-ACTION X(72)`; `WS-DSNTIAC-FORMATTED`: `WS-DSNTIAC-MESG-LEN S9(4) COMP VALUE +720`, `WS-DSNTIAC-FMTD-TEXT-LINE X(72) OCCURS 10`; `WS-DSNTIAC-LRECL S9(4) COMP VALUE +72`; `WS-DSNTIAC-ERROR`: `WS-DSNTIAC-ERR-MSG X(10)`, `WS-DSNTIAC-ERR-CD-X X(02)` / `WS-DSNTIAC-ERR-CD 9(02) REDEFINES`.
* `CSDB2RPY.cpy` — procedure copybook: `9998-PRIMING-QUERY` (`SELECT 1 … FROM SYSIBM.SYSDUMMY1`) and `9999-FORMAT-DB2-MESSAGE` (calls `DSNTIAC`).
* `dcl/DCLTRTYP.dcl`: `CARDDEMO.TRANSACTION_TYPE (TR_TYPE CHAR(2) NOT NULL, TR_DESCRIPTION VARCHAR(50) NOT NULL)` → host vars `TR-TYPE X(2)`, `TR-DESCRIPTION` varchar (49 `S9(4) COMP` length + `X(50)`).
* `dcl/DCLTRCAT.dcl`: `CARDDEMO.TRANSACTION_TYPE_CATEGORY (TRC_TYPE_CODE CHAR(2), TRC_TYPE_CATEGORY CHAR(4), TRC_CAT_DATA VARCHAR(50))`.

## 11. BMS symbolic copybooks (`app/cpy-bms/*.CPY`, `*/cpy-bms/*`)

17 core maps (`COACTUP`, `COACTVW`, `COADM01`, `COBIL00`, `COCRDLI`, `COCRDSL`, `COCRDUP`, `COMEN01`, `CORPT00`, `COSGN00`, `COTRN00`, `COTRN01`, `COTRN02`, `COUSR00`, `COUSR01`, `COUSR02`, `COUSR03`) plus `COPAU00`, `COPAU01` (auth) and `COTRTLI`, `COTRTUP` (DB2). Each follows the BMS-generated pattern `<field>L PIC S9(4) COMP` (length), `<field>F PIC X` (flag), `<field>A PIC X` (attribute), `<field>I`/`<field>O PIC X(n)` (input/output) with an `…O REDEFINES …I` overlay. These are presentation buffers — in a Java target they become DTO/form objects and are not persisted.
