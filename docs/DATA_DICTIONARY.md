# CardDemo Data Dictionary

Every field below was parsed directly from the copybook and DCLGEN sources in `app/cpy/`, `app/app-authorization-ims-db2-mq/cpy/`, `app/app-authorization-ims-db2-mq/dcl/`, `app/app-transaction-type-db2/cpy/` and `app/app-transaction-type-db2/dcl/` — 44 members, 753 data items in total. `Size` is the digit/character count of the `PIC` clause; `Derived type` is computed from the `PIC` and `USAGE` clauses. Record lengths and key lengths quoted in the notes come from `app/catlg/LISTCAT.txt`. BMS map copybooks (17 in `app/cpy-bms/` plus 4 in the extension `cpy-bms/` directories) are screen definitions generated from the maps and are catalogued in `APPLICATION_INVENTORY.md` instead.


Type conventions found in the source: unsuffixed `9(n)` is zoned decimal `DISPLAY`, and `S9(n)V99` is a signed display amount with two implied decimal digits. Every VSAM master record (`CVACT01Y`, `CVCUS01Y`, `CVACT02Y`, `CVACT03Y`, `CVTRA01Y`-`CVTRA06Y`, `CSUSR01Y`) is pure `DISPLAY` — amounts are character data on disk, so the record length equals the digit count and the files can be read as fixed-width text. `COMP-3` and `COMP` appear only in `app/cpy/CVEXPORT.cpy` (export/import multiplex), the IMS segment copybooks `CIPAUSMY`/`CIPAUDTY`, `CSDB2RWY` and the DB2 DCLGENs (`49`-level VARCHAR length fields, `S9(n)` `COMP` counters). For those fields the `Size` column below is the declared digit count, not the byte length: `COMP-3` occupies `digits/2 + 1` bytes and `S9(4) COMP` occupies 2 bytes. All amounts must be converted to `BigDecimal` (never `double`) when modernized.


## 1. Account

Account master, the ACCTDATA cluster.


### `CVACT01Y` — `app/cpy/CVACT01Y.cpy`

Account master record used by every account program and by the ACCTDATA VSAM KSDS (`AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`, `LISTCAT` reports keylen 11, avg/max reclen 300). (20 lines, 14 non-comment lines, 14 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `ACCOUNT-RECORD` | - | - | group item | 300-byte ACCTDATA KSDS record (key = ACCT-ID, length 11) | - |
| 05 | `ACCT-ID` | `9(11)` | 11 | zoned decimal (DISPLAY), unsigned | Account number; primary key of ACCTDATA and of the CARDXREF/TRANSACT category keys | - |
| 05 | `ACCT-ACTIVE-STATUS` | `X(01)` | 1 | alphanumeric (DISPLAY) | Account open/closed flag | - |
| 05 | `ACCT-CURR-BAL` | `S9(10)V99` | 12 | zoned decimal (DISPLAY), signed, implied decimal | Current outstanding balance; updated by CBTRN02C and CBACT04C | - |
| 05 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | 12 | zoned decimal (DISPLAY), signed, implied decimal | Approved purchase credit limit; CBTRN02C rejects a transaction that would exceed it | - |
| 05 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 12 | zoned decimal (DISPLAY), signed, implied decimal | Approved cash-advance limit | - |
| 05 | `ACCT-OPEN-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Account opening date | - |
| 05 | `ACCT-EXPIRAION-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Account expiry date (field name misspelt in source) | - |
| 05 | `ACCT-REISSUE-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Date the plastic was last reissued | - |
| 05 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 12 | zoned decimal (DISPLAY), signed, implied decimal | Credits posted in the current billing cycle | - |
| 05 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 12 | zoned decimal (DISPLAY), signed, implied decimal | Debits posted in the current billing cycle | - |
| 05 | `ACCT-ADDR-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Account statement ZIP code | - |
| 05 | `ACCT-GROUP-ID` | `X(10)` | 10 | alphanumeric (DISPLAY) | Disclosure group; joins to DIS-ACCT-GROUP-ID for the interest rate | - |
| 05 | `FILLER` | `X(178)` | 178 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

## 2. Customer

Customer master. Two copybooks describe the same 500-byte record.


### `CVCUS01Y` — `app/cpy/CVCUS01Y.cpy`

Customer master record (`AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`, keylen 9, reclen 500). (26 lines, 20 non-comment lines, 20 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CUSTOMER-RECORD` | - | - | group item | 500-byte CUSTDATA KSDS record (key = CUST-ID, length 9) | - |
| 05 | `CUST-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Customer number; primary key of CUSTDATA | - |
| 05 | `CUST-FIRST-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Customer given name | - |
| 05 | `CUST-MIDDLE-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Customer middle name | - |
| 05 | `CUST-LAST-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Customer family name | - |
| 05 | `CUST-ADDR-LINE-1` | `X(50)` | 50 | alphanumeric (DISPLAY) | Address line 1 | - |
| 05 | `CUST-ADDR-LINE-2` | `X(50)` | 50 | alphanumeric (DISPLAY) | Address line 2 | - |
| 05 | `CUST-ADDR-LINE-3` | `X(50)` | 50 | alphanumeric (DISPLAY) | Address line 3 (city) | - |
| 05 | `CUST-ADDR-STATE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | US state code | - |
| 05 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | 3 | alphanumeric (DISPLAY) | Country code | - |
| 05 | `CUST-ADDR-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Postal code | - |
| 05 | `CUST-PHONE-NUM-1` | `X(15)` | 15 | alphanumeric (DISPLAY) | Primary phone, (999)999-9999 layout | - |
| 05 | `CUST-PHONE-NUM-2` | `X(15)` | 15 | alphanumeric (DISPLAY) | Secondary phone | - |
| 05 | `CUST-SSN` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | US social security number | - |
| 05 | `CUST-GOVT-ISSUED-ID` | `X(20)` | 20 | alphanumeric (DISPLAY) | Government issued identity document reference | - |
| 05 | `CUST-DOB-YYYY-MM-DD` | `X(10)` | 10 | alphanumeric (DISPLAY) | Date of birth, CCYY-MM-DD | - |
| 05 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | 10 | alphanumeric (DISPLAY) | Bank account used for electronic funds transfer / autopay | - |
| 05 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | 1 | alphanumeric (DISPLAY) | Primary card-holder indicator | - |
| 05 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | 3 | zoned decimal (DISPLAY), unsigned | FICO credit score | - |
| 05 | `FILLER` | `X(168)` | 168 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CUSTREC` — `app/cpy/CUSTREC.cpy`

Byte-for-byte duplicate of `CVACT01Y`'s sibling `CVCUS01Y` apart from the `CUST-DOB-YYYYMMDD` / `CUST-DOB-YYYY-MM-DD` name; used by `CBCUS01C`. (26 lines, 20 non-comment lines, 20 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CUSTOMER-RECORD` | - | - | group item | 500-byte CUSTDATA KSDS record (key = CUST-ID, length 9) | - |
| 05 | `CUST-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Customer number; primary key of CUSTDATA | - |
| 05 | `CUST-FIRST-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Customer given name | - |
| 05 | `CUST-MIDDLE-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Customer middle name | - |
| 05 | `CUST-LAST-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Customer family name | - |
| 05 | `CUST-ADDR-LINE-1` | `X(50)` | 50 | alphanumeric (DISPLAY) | Address line 1 | - |
| 05 | `CUST-ADDR-LINE-2` | `X(50)` | 50 | alphanumeric (DISPLAY) | Address line 2 | - |
| 05 | `CUST-ADDR-LINE-3` | `X(50)` | 50 | alphanumeric (DISPLAY) | Address line 3 (city) | - |
| 05 | `CUST-ADDR-STATE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | US state code | - |
| 05 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | 3 | alphanumeric (DISPLAY) | Country code | - |
| 05 | `CUST-ADDR-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Postal code | - |
| 05 | `CUST-PHONE-NUM-1` | `X(15)` | 15 | alphanumeric (DISPLAY) | Primary phone, (999)999-9999 layout | - |
| 05 | `CUST-PHONE-NUM-2` | `X(15)` | 15 | alphanumeric (DISPLAY) | Secondary phone | - |
| 05 | `CUST-SSN` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | US social security number | - |
| 05 | `CUST-GOVT-ISSUED-ID` | `X(20)` | 20 | alphanumeric (DISPLAY) | Government issued identity document reference | - |
| 05 | `CUST-DOB-YYYYMMDD` | `X(10)` | 10 | alphanumeric (DISPLAY) | Date of birth, CCYY-MM-DD (CUSTREC spelling) | - |
| 05 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | 10 | alphanumeric (DISPLAY) | Bank account used for electronic funds transfer / autopay | - |
| 05 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | 1 | alphanumeric (DISPLAY) | Primary card-holder indicator | - |
| 05 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | 3 | zoned decimal (DISPLAY), unsigned | FICO credit score | - |
| 05 | `FILLER` | `X(168)` | 168 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

## 3. Card

Card master and the card screen work area.


### `CVACT02Y` — `app/cpy/CVACT02Y.cpy`

Card master record (`AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`, keylen 16, reclen 150). (14 lines, 8 non-comment lines, 8 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CARD-RECORD` | - | - | group item | 150-byte CARDDATA KSDS record (key = CARD-NUM, length 16; AIX on CARD-ACCT-ID at RKP 5) | - |
| 05 | `CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | 16-digit card number; primary key of CARDDATA and of TRANSACT AIX | - |
| 05 | `CARD-ACCT-ID` | `9(11)` | 11 | zoned decimal (DISPLAY), unsigned | Owning account; alternate-index key of CARDDATA (CARDAIX) | - |
| 05 | `CARD-CVV-CD` | `9(03)` | 3 | zoned decimal (DISPLAY), unsigned | Card verification value | - |
| 05 | `CARD-EMBOSSED-NAME` | `X(50)` | 50 | alphanumeric (DISPLAY) | Name embossed on the plastic | - |
| 05 | `CARD-EXPIRAION-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Card expiry date (field name misspelt in source) | - |
| 05 | `CARD-ACTIVE-STATUS` | `X(01)` | 1 | alphanumeric (DISPLAY) | Card active flag | - |
| 05 | `FILLER` | `X(59)` | 59 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVCRD01Y` — `app/cpy/CVCRD01Y.cpy`

Card-screen work area shared by `COCRDLIC`, `COCRDSLC` and `COCRDUPC`. (46 lines, 34 non-comment lines, 31 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CC-WORK-AREAS` | - | - | group item | Cc work areas | - |
| 05 | `CC-WORK-AREA` | - | - | group item | Cc work area | - |
| 10 | `CCARD-AID` | `X(5)` | 5 | alphanumeric (DISPLAY) | Ccard aid | - |
| 88 | `CCARD-AID-ENTER` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'ENTER' |
| 88 | `CCARD-AID-CLEAR` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'CLEAR' |
| 88 | `CCARD-AID-PA1` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PA1 ' |
| 88 | `CCARD-AID-PA2` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PA2 ' |
| 88 | `CCARD-AID-PFK01` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK01' |
| 88 | `CCARD-AID-PFK02` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK02' |
| 88 | `CCARD-AID-PFK03` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK03' |
| 88 | `CCARD-AID-PFK04` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK04' |
| 88 | `CCARD-AID-PFK05` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK05' |
| 88 | `CCARD-AID-PFK06` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK06' |
| 88 | `CCARD-AID-PFK07` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK07' |
| 88 | `CCARD-AID-PFK08` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK08' |
| 88 | `CCARD-AID-PFK09` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK09' |
| 88 | `CCARD-AID-PFK10` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK10' |
| 88 | `CCARD-AID-PFK11` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK11' |
| 88 | `CCARD-AID-PFK12` | - | - | condition name | Condition name on `CCARD-AID` | VALUE 'PFK12' |
| 10 | `CCARD-NEXT-PROG` | `X(8)` | 8 | alphanumeric (DISPLAY) | Ccard next prog | - |
| 10 | `CCARD-NEXT-MAPSET` | `X(7)` | 7 | alphanumeric (DISPLAY) | Ccard next BMS mapset | - |
| 10 | `CCARD-NEXT-MAP` | `X(7)` | 7 | alphanumeric (DISPLAY) | Ccard next BMS map | - |
| 10 | `CCARD-ERROR-MSG` | `X(75)` | 75 | alphanumeric (DISPLAY) | Ccard error message | - |
| 10 | `CCARD-RETURN-MSG` | `X(75)` | 75 | alphanumeric (DISPLAY) | Ccard return message | - |
| 88 | `CCARD-RETURN-MSG-OFF` | - | - | condition name | Condition name on `CCARD-RETURN-MSG` | VALUE LOW-VALUES |
| 10 | `CC-ACCT-ID` | `X(11)` | 11 | alphanumeric (DISPLAY) | Cc account identifier | VALUE SPACES |
| 10 | `CC-ACCT-ID-N` | `9(11)` | 11 | zoned decimal (DISPLAY), unsigned | Cc account identifier n | REDEFINES `CC-ACCT-ID` |
| 10 | `CC-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Cc card number | VALUE SPACES |
| 10 | `CC-CARD-NUM-N` | `9(16)` | 16 | zoned decimal (DISPLAY), unsigned | Cc card number n | REDEFINES `CC-CARD-NUM` |
| 10 | `CC-CUST-ID` | `X(09)` | 9 | alphanumeric (DISPLAY) | Cc customer identifier | VALUE SPACES |
| 10 | `CC-CUST-ID-N` | `9(9)` | 9 | zoned decimal (DISPLAY), unsigned | Cc customer identifier n | REDEFINES `CC-CUST-ID` |

## 4. Card cross-reference

Card to customer/account cross-reference.


### `CVACT03Y` — `app/cpy/CVACT03Y.cpy`

Card / customer / account cross-reference (`AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`, keylen 16, reclen 50). (11 lines, 5 non-comment lines, 5 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CARD-XREF-RECORD` | - | - | group item | 50-byte CARDXREF KSDS record (key = XREF-CARD-NUM, length 16; AIX on XREF-ACCT-ID at RKP 5) | - |
| 05 | `XREF-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Card number; primary key of CARDXREF | - |
| 05 | `XREF-CUST-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Customer owning the card | - |
| 05 | `XREF-ACCT-ID` | `9(11)` | 11 | zoned decimal (DISPLAY), unsigned | Account the card draws on; alternate-index key (CXACAIX) | - |
| 05 | `FILLER` | `X(14)` | 14 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

## 5. Transaction

Posted master, daily feed, category balances, disclosure groups and the type/category reference files.


### `CVTRA05Y` — `app/cpy/CVTRA05Y.cpy`

Posted transaction master (`AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`, keylen 16, reclen 350). (21 lines, 15 non-comment lines, 15 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `TRAN-RECORD` | - | - | group item | 350-byte TRANSACT KSDS record (key = TRAN-ID, length 16; AIX of 26 on card number + id) | - |
| 05 | `TRAN-ID` | `X(16)` | 16 | alphanumeric (DISPLAY) | Transaction identifier; primary key of TRANSACT | - |
| 05 | `TRAN-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Transaction type (joins TRANTYPE / DB2 TRANSACTION_TYPE) | - |
| 05 | `TRAN-CAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Transaction category (joins TRANCATG / DB2 TRANSACTION_TYPE_CATEGORY) | - |
| 05 | `TRAN-SOURCE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Origin channel of the transaction | - |
| 05 | `TRAN-DESC` | `X(100)` | 100 | alphanumeric (DISPLAY) | Free-text transaction description | - |
| 05 | `TRAN-AMT` | `S9(09)V99` | 11 | zoned decimal (DISPLAY), signed, implied decimal | Signed transaction amount | - |
| 05 | `TRAN-MERCHANT-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Acquiring merchant number | - |
| 05 | `TRAN-MERCHANT-NAME` | `X(50)` | 50 | alphanumeric (DISPLAY) | Merchant name | - |
| 05 | `TRAN-MERCHANT-CITY` | `X(50)` | 50 | alphanumeric (DISPLAY) | Merchant city | - |
| 05 | `TRAN-MERCHANT-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Merchant ZIP | - |
| 05 | `TRAN-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Card the transaction was made with; AIX component | - |
| 05 | `TRAN-ORIG-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Timestamp the transaction was originated | - |
| 05 | `TRAN-PROC-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Timestamp the transaction was posted by CBTRN02C | - |
| 05 | `FILLER` | `X(20)` | 20 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVTRA06Y` — `app/cpy/CVTRA06Y.cpy`

Daily incoming transaction feed (`AWS.M2.CARDDEMO.DALYTRAN.PS`), same 350-byte layout as `CVTRA05Y`. (21 lines, 15 non-comment lines, 15 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `DALYTRAN-RECORD` | - | - | group item | 350-byte DALYTRAN sequential record: the daily incoming transaction feed | - |
| 05 | `DALYTRAN-ID` | `X(16)` | 16 | alphanumeric (DISPLAY) | Daily transaction identifier | - |
| 05 | `DALYTRAN-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Daily transaction type code | - |
| 05 | `DALYTRAN-CAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Daily transaction category code | - |
| 05 | `DALYTRAN-SOURCE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Daily transaction source | - |
| 05 | `DALYTRAN-DESC` | `X(100)` | 100 | alphanumeric (DISPLAY) | Daily transaction description | - |
| 05 | `DALYTRAN-AMT` | `S9(09)V99` | 11 | zoned decimal (DISPLAY), signed, implied decimal | Daily transaction amount | - |
| 05 | `DALYTRAN-MERCHANT-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Daily transaction merchant identifier | - |
| 05 | `DALYTRAN-MERCHANT-NAME` | `X(50)` | 50 | alphanumeric (DISPLAY) | Daily transaction merchant name | - |
| 05 | `DALYTRAN-MERCHANT-CITY` | `X(50)` | 50 | alphanumeric (DISPLAY) | Daily transaction merchant city | - |
| 05 | `DALYTRAN-MERCHANT-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Daily transaction merchant ZIP code | - |
| 05 | `DALYTRAN-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Daily transaction card number | - |
| 05 | `DALYTRAN-ORIG-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Daily transaction orig timestamp | - |
| 05 | `DALYTRAN-PROC-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Daily transaction proc timestamp | - |
| 05 | `FILLER` | `X(20)` | 20 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVTRA01Y` — `app/cpy/CVTRA01Y.cpy`

Transaction category balance (`TCATBALF`, keylen 17, reclen 50). (13 lines, 7 non-comment lines, 7 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `TRAN-CAT-BAL-RECORD` | - | - | group item | 50-byte TCATBALF KSDS record: balance per account/type/category (key length 17) | - |
| 05 | `TRAN-CAT-KEY` | - | - | group item | Composite key | - |
| 10 | `TRANCAT-ACCT-ID` | `9(11)` | 11 | zoned decimal (DISPLAY), unsigned | Account of the category balance | - |
| 10 | `TRANCAT-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Transaction type of the category balance | - |
| 10 | `TRANCAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Transaction category of the category balance | - |
| 05 | `TRAN-CAT-BAL` | `S9(09)V99` | 11 | zoned decimal (DISPLAY), signed, implied decimal | Accumulated balance for the account/type/category, input to interest calculation | - |
| 05 | `FILLER` | `X(22)` | 22 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVTRA02Y` — `app/cpy/CVTRA02Y.cpy`

Disclosure group interest rates (`DISCGRP`, keylen 16, reclen 50). (13 lines, 7 non-comment lines, 7 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `DIS-GROUP-RECORD` | - | - | group item | 50-byte DISCGRP KSDS record: interest rate per group/type/category (key length 16) | - |
| 05 | `DIS-GROUP-KEY` | - | - | group item | Composite key | - |
| 10 | `DIS-ACCT-GROUP-ID` | `X(10)` | 10 | alphanumeric (DISPLAY) | Disclosure group; matches ACCT-GROUP-ID, `DEFAULT` used as fallback by CBACT04C | - |
| 10 | `DIS-TRAN-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Transaction type the rate applies to | - |
| 10 | `DIS-TRAN-CAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Transaction category the rate applies to | - |
| 05 | `DIS-INT-RATE` | `S9(04)V99` | 6 | zoned decimal (DISPLAY), signed, implied decimal | Annual interest rate percentage; CBACT04C divides by 1200 for the monthly charge | - |
| 05 | `FILLER` | `X(28)` | 28 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVTRA03Y` — `app/cpy/CVTRA03Y.cpy`

Transaction type reference (`TRANTYPE`, keylen 2, reclen 60). (10 lines, 4 non-comment lines, 4 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `TRAN-TYPE-RECORD` | - | - | group item | 60-byte TRANTYPE KSDS record (key length 2) | - |
| 05 | `TRAN-TYPE` | `X(02)` | 2 | alphanumeric (DISPLAY) | Transaction type code | - |
| 05 | `TRAN-TYPE-DESC` | `X(50)` | 50 | alphanumeric (DISPLAY) | Transaction type description | - |
| 05 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVTRA04Y` — `app/cpy/CVTRA04Y.cpy`

Transaction category reference (`TRANCATG`, keylen 6, reclen 60). (12 lines, 6 non-comment lines, 6 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `TRAN-CAT-RECORD` | - | - | group item | 60-byte TRANCATG KSDS record (key length 6) | - |
| 05 | `TRAN-CAT-KEY` | - | - | group item | Composite key | - |
| 10 | `TRAN-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Transaction type (joins TRANTYPE / DB2 TRANSACTION_TYPE) | - |
| 10 | `TRAN-CAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Transaction category (joins TRANCATG / DB2 TRANSACTION_TYPE_CATEGORY) | - |
| 05 | `TRAN-CAT-TYPE-DESC` | `X(50)` | 50 | alphanumeric (DISPLAY) | Transaction category description | - |
| 05 | `FILLER` | `X(04)` | 4 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVTRA07Y` — `app/cpy/CVTRA07Y.cpy`

Transaction-report line layouts used by `CBTRN03C`. (73 lines, 57 non-comment lines, 45 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `REPORT-NAME-HEADER` | - | - | group item | Report name header | - |
| 05 | `REPT-SHORT-NAME` | `X(38)` | 38 | alphanumeric (DISPLAY) | Report short name | VALUE 'DALYREPT' |
| 05 | `REPT-LONG-NAME` | `X(41)` | 41 | alphanumeric (DISPLAY) | Report long name | VALUE 'Daily Transaction Report' |
| 05 | `REPT-DATE-HEADER` | `X(12)` | 12 | alphanumeric (DISPLAY) | Report date header | VALUE 'Date Range: ' |
| 05 | `REPT-START-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Report start date | VALUE SPACES |
| 05 | `FILLER` | `X(04)` | 4 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ' to ' |
| 05 | `REPT-END-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Report end date | VALUE SPACES |
| 01 | `TRANSACTION-DETAIL-REPORT` | - | - | group item | Transaction detail report | - |
| 05 | `TRAN-REPORT-TRANS-ID` | `X(16)` | 16 | alphanumeric (DISPLAY) | Transaction report trans identifier | - |
| 05 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |
| 05 | `TRAN-REPORT-ACCOUNT-ID` | `X(11)` | 11 | alphanumeric (DISPLAY) | Transaction report account identifier | - |
| 05 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |
| 05 | `TRAN-REPORT-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Transaction report type code | - |
| 05 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE '-' |
| 05 | `TRAN-REPORT-TYPE-DESC` | `X(15)` | 15 | alphanumeric (DISPLAY) | Transaction report type description | - |
| 05 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |
| 05 | `TRAN-REPORT-CAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Transaction report category code | - |
| 05 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE '-' |
| 05 | `TRAN-REPORT-CAT-DESC` | `X(29)` | 29 | alphanumeric (DISPLAY) | Transaction report category description | - |
| 05 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |
| 05 | `TRAN-REPORT-SOURCE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Transaction report source | - |
| 05 | `FILLER` | `X(04)` | 4 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |
| 05 | `TRAN-REPORT-AMT` | `-ZZZ,ZZZ,ZZZ.ZZ` | 11 | display | Transaction report amount | - |
| 05 | `FILLER` | `X(02)` | 2 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |
| 01 | `TRANSACTION-HEADER-1` | - | - | group item | Transaction header 1 | - |
| 05 | `FILLER` | `X(17)` | 17 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction ID' |
| 05 | `FILLER` | `X(12)` | 12 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Account ID' |
| 05 | `FILLER` | `X(19)` | 19 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction Type' |
| 05 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Tran Category' |
| 05 | `FILLER` | `X(14)` | 14 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Tran Source' |
| 05 | `FILLER` | `X` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |
| 05 | `FILLER` | `X(16)` | 16 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ' Amount' |
| 01 | `TRANSACTION-HEADER-2` | `X(133)` | 133 | alphanumeric (DISPLAY) | Transaction header 2 | VALUE ALL '-' |
| 01 | `REPORT-PAGE-TOTALS` | - | - | group item | Report page totals | - |
| 05 | `FILLER` | `X(11)` | 11 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Page Total' |
| 05 | `FILLER` | `X(86)` | 86 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ALL '.' |
| 05 | `REPT-PAGE-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | 11 | display | Report page total | - |
| 01 | `REPORT-ACCOUNT-TOTALS` | - | - | group item | Report account totals | - |
| 05 | `FILLER` | `X(13)` | 13 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Account Total' |
| 05 | `FILLER` | `X(84)` | 84 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ALL '.' |
| 05 | `REPT-ACCOUNT-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | 11 | display | Report account total | - |
| 01 | `REPORT-GRAND-TOTALS` | - | - | group item | Report grand totals | - |
| 05 | `FILLER` | `X(11)` | 11 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Grand Total' |
| 05 | `FILLER` | `X(86)` | 86 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ALL '.' |
| 05 | `REPT-GRAND-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | 11 | display | Report grand total | - |

## 6. Supporting

Commarea, security, statement, export multiplex, date handling, lookup tables, messages, menu tables and screen titles.


### `COCOM01Y` — `app/cpy/COCOM01Y.cpy`

CICS commarea shared by every online program. (47 lines, 26 non-comment lines, 26 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CARDDEMO-COMMAREA` | - | - | group item | Pseudo-conversational CICS commarea passed on every XCTL | - |
| 05 | `CDEMO-GENERAL-INFO` | - | - | group item | Cdemo general information | - |
| 10 | `CDEMO-FROM-TRANID` | `X(04)` | 4 | alphanumeric (DISPLAY) | Transaction id of the calling program | - |
| 10 | `CDEMO-FROM-PROGRAM` | `X(08)` | 8 | alphanumeric (DISPLAY) | Name of the calling program | - |
| 10 | `CDEMO-TO-TRANID` | `X(04)` | 4 | alphanumeric (DISPLAY) | Transaction id being transferred to | - |
| 10 | `CDEMO-TO-PROGRAM` | `X(08)` | 8 | alphanumeric (DISPLAY) | Name of the program being transferred to | - |
| 10 | `CDEMO-USER-ID` | `X(08)` | 8 | alphanumeric (DISPLAY) | Signed-on user id, carried for the whole session | - |
| 10 | `CDEMO-USER-TYPE` | `X(01)` | 1 | alphanumeric (DISPLAY) | Signed-on user type, drives admin vs regular menu | - |
| 88 | `CDEMO-USRTYP-ADMIN` | - | - | condition name | Condition name on `CDEMO-USER-TYPE` | VALUE 'A' |
| 88 | `CDEMO-USRTYP-USER` | - | - | condition name | Condition name on `CDEMO-USER-TYPE` | VALUE 'U' |
| 10 | `CDEMO-PGM-CONTEXT` | `9(01)` | 1 | zoned decimal (DISPLAY), unsigned | First-entry vs re-entry flag for pseudo-conversational restart | - |
| 88 | `CDEMO-PGM-ENTER` | - | - | condition name | Condition name on `CDEMO-PGM-CONTEXT` | VALUE 0 |
| 88 | `CDEMO-PGM-REENTER` | - | - | condition name | Condition name on `CDEMO-PGM-CONTEXT` | VALUE 1 |
| 05 | `CDEMO-CUSTOMER-INFO` | - | - | group item | Cdemo customer information | - |
| 10 | `CDEMO-CUST-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Cdemo customer identifier | - |
| 10 | `CDEMO-CUST-FNAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Cdemo customer first name | - |
| 10 | `CDEMO-CUST-MNAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Cdemo customer middle name | - |
| 10 | `CDEMO-CUST-LNAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Cdemo customer last name | - |
| 05 | `CDEMO-ACCOUNT-INFO` | - | - | group item | Cdemo account information | - |
| 10 | `CDEMO-ACCT-ID` | `9(11)` | 11 | zoned decimal (DISPLAY), unsigned | Cdemo account identifier | - |
| 10 | `CDEMO-ACCT-STATUS` | `X(01)` | 1 | alphanumeric (DISPLAY) | Cdemo account status | - |
| 05 | `CDEMO-CARD-INFO` | - | - | group item | Cdemo card information | - |
| 10 | `CDEMO-CARD-NUM` | `9(16)` | 16 | zoned decimal (DISPLAY), unsigned | Cdemo card number | - |
| 05 | `CDEMO-MORE-INFO` | - | - | group item | Cdemo more information | - |
| 10 | `CDEMO-LAST-MAP` | `X(7)` | 7 | alphanumeric (DISPLAY) | Cdemo last BMS map | - |
| 10 | `CDEMO-LAST-MAPSET` | `X(7)` | 7 | alphanumeric (DISPLAY) | Cdemo last BMS mapset | - |

### `CSUSR01Y` — `app/cpy/CSUSR01Y.cpy`

User security record (`AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`, keylen 8, reclen 80). (26 lines, 7 non-comment lines, 7 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `SEC-USER-DATA` | - | - | group item | 80-byte USRSEC KSDS record (key = SEC-USR-ID, length 8) | - |
| 05 | `SEC-USR-ID` | `X(08)` | 8 | alphanumeric (DISPLAY) | Sign-on user id; primary key of USRSEC | - |
| 05 | `SEC-USR-FNAME` | `X(20)` | 20 | alphanumeric (DISPLAY) | User first name | - |
| 05 | `SEC-USR-LNAME` | `X(20)` | 20 | alphanumeric (DISPLAY) | User last name | - |
| 05 | `SEC-USR-PWD` | `X(08)` | 8 | alphanumeric (DISPLAY) | Sign-on password, stored in clear text | - |
| 05 | `SEC-USR-TYPE` | `X(01)` | 1 | alphanumeric (DISPLAY) | `A` admin (COADM01C menu) or `U` regular user (COMEN01C menu) | - |
| 05 | `SEC-USR-FILLER` | `X(23)` | 23 | alphanumeric (DISPLAY) | Security user filler | - |

### `COSTM01` — `app/cpy/COSTM01.CPY`

Statement work record for `CBSTM03A`/`CBSTM03B`. (38 lines, 17 non-comment lines, 17 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `TRNX-RECORD` | - | - | group item | Statement work record: TRANSACT data re-keyed by card number for CBSTM03A/B | - |
| 05 | `TRNX-KEY` | - | - | group item | Transaction key | - |
| 10 | `TRNX-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Transaction card number | - |
| 10 | `TRNX-ID` | `X(16)` | 16 | alphanumeric (DISPLAY) | Transaction identifier | - |
| 05 | `TRNX-REST` | - | - | group item | Transaction rest | - |
| 10 | `TRNX-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Transaction type code | - |
| 10 | `TRNX-CAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Transaction category code | - |
| 10 | `TRNX-SOURCE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Transaction source | - |
| 10 | `TRNX-DESC` | `X(100)` | 100 | alphanumeric (DISPLAY) | Transaction description | - |
| 10 | `TRNX-AMT` | `S9(09)V99` | 11 | zoned decimal (DISPLAY), signed, implied decimal | Transaction amount | - |
| 10 | `TRNX-MERCHANT-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Transaction merchant identifier | - |
| 10 | `TRNX-MERCHANT-NAME` | `X(50)` | 50 | alphanumeric (DISPLAY) | Transaction merchant name | - |
| 10 | `TRNX-MERCHANT-CITY` | `X(50)` | 50 | alphanumeric (DISPLAY) | Transaction merchant city | - |
| 10 | `TRNX-MERCHANT-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Transaction merchant ZIP code | - |
| 10 | `TRNX-ORIG-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Transaction orig timestamp | - |
| 10 | `TRNX-PROC-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Transaction proc timestamp | - |
| 10 | `FILLER` | `X(20)` | 20 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CVEXPORT` — `app/cpy/CVEXPORT.cpy`

Multiplexed export/import record: one physical 500-byte record (per the copybook header comment) carries customer, account, card, cross-reference or transaction data selected by `EXPORT-REC-TYPE`, via `REDEFINES` of `EXPORT-RECORD-DATA`. (103 lines, 72 non-comment lines, 72 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `EXPORT-RECORD` | - | - | group item | Export record | - |
| 05 | `EXPORT-REC-TYPE` | `X(1)` | 1 | alphanumeric (DISPLAY) | Export record type | - |
| 05 | `EXPORT-TIMESTAMP` | `X(26)` | 26 | alphanumeric (DISPLAY) | Export timestamp | - |
| 05 | `EXPORT-TIMESTAMP-R` | - | - | group item | Export timestamp r | REDEFINES `EXPORT-TIMESTAMP` |
| 10 | `EXPORT-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export date | - |
| 10 | `EXPORT-DATE-TIME-SEP` | `X(1)` | 1 | alphanumeric (DISPLAY) | Export date time sep | - |
| 10 | `EXPORT-TIME` | `X(15)` | 15 | alphanumeric (DISPLAY) | Export time | - |
| 05 | `EXPORT-SEQUENCE-NUM` | `9(9)` | 9 | binary (COMP), unsigned | Export sequence number | - |
| 05 | `EXPORT-BRANCH-ID` | `X(4)` | 4 | alphanumeric (DISPLAY) | Export branch identifier | - |
| 05 | `EXPORT-REGION-CODE` | `X(5)` | 5 | alphanumeric (DISPLAY) | Export region code | - |
| 05 | `EXPORT-RECORD-DATA` | `X(460)` | 460 | alphanumeric (DISPLAY) | Export record data | - |
| 05 | `EXPORT-CUSTOMER-DATA` | - | - | group item | Export customer data | REDEFINES `EXPORT-RECORD-DATA` |
| 10 | `EXP-CUST-ID` | `9(09)` | 9 | binary (COMP), unsigned | Export customer identifier | - |
| 10 | `EXP-CUST-FIRST-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Export customer first name | - |
| 10 | `EXP-CUST-MIDDLE-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Export customer middle name | - |
| 10 | `EXP-CUST-LAST-NAME` | `X(25)` | 25 | alphanumeric (DISPLAY) | Export customer last name | - |
| 10 | `EXP-CUST-ADDR-LINES` | - | - | group item | Export customer address lines | OCCURS 3 |
| 15 | `EXP-CUST-ADDR-LINE` | `X(50)` | 50 | alphanumeric (DISPLAY) | Export customer address line | - |
| 10 | `EXP-CUST-ADDR-STATE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Export customer address state code | - |
| 10 | `EXP-CUST-ADDR-COUNTRY-CD` | `X(03)` | 3 | alphanumeric (DISPLAY) | Export customer address country code | - |
| 10 | `EXP-CUST-ADDR-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export customer address ZIP code | - |
| 10 | `EXP-CUST-PHONE-NUMS` | - | - | group item | Export customer phone nums | OCCURS 2 |
| 15 | `EXP-CUST-PHONE-NUM` | `X(15)` | 15 | alphanumeric (DISPLAY) | Export customer phone number | - |
| 10 | `EXP-CUST-SSN` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Export customer social security number | - |
| 10 | `EXP-CUST-GOVT-ISSUED-ID` | `X(20)` | 20 | alphanumeric (DISPLAY) | Export customer govt issued identifier | - |
| 10 | `EXP-CUST-DOB-YYYY-MM-DD` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export customer date of birth year month day | - |
| 10 | `EXP-CUST-EFT-ACCOUNT-ID` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export customer EFT account identifier | - |
| 10 | `EXP-CUST-PRI-CARD-HOLDER-IND` | `X(01)` | 1 | alphanumeric (DISPLAY) | Export customer pri card holder indicator | - |
| 10 | `EXP-CUST-FICO-CREDIT-SCORE` | `9(03)` | 3 | packed decimal (COMP-3) | Export customer FICO credit score | - |
| 10 | `FILLER` | `X(134)` | 134 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |
| 05 | `EXPORT-ACCOUNT-DATA` | - | - | group item | Export account data | REDEFINES `EXPORT-RECORD-DATA` |
| 10 | `EXP-ACCT-ID` | `9(11)` | 11 | zoned decimal (DISPLAY), unsigned | Export account identifier | - |
| 10 | `EXP-ACCT-ACTIVE-STATUS` | `X(01)` | 1 | alphanumeric (DISPLAY) | Export account active status | - |
| 10 | `EXP-ACCT-CURR-BAL` | `S9(10)V99` | 12 | packed decimal (COMP-3), 2 dp | Export account current balance | - |
| 10 | `EXP-ACCT-CREDIT-LIMIT` | `S9(10)V99` | 12 | zoned decimal (DISPLAY), signed, implied decimal | Export account credit limit | - |
| 10 | `EXP-ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 12 | packed decimal (COMP-3), 2 dp | Export account cash credit limit | - |
| 10 | `EXP-ACCT-OPEN-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export account open date | - |
| 10 | `EXP-ACCT-EXPIRAION-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export account expiraion date | - |
| 10 | `EXP-ACCT-REISSUE-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export account reissue date | - |
| 10 | `EXP-ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 12 | zoned decimal (DISPLAY), signed, implied decimal | Export account current cycle credit | - |
| 10 | `EXP-ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 12 | binary (COMP), signed | Export account current cycle debit | - |
| 10 | `EXP-ACCT-ADDR-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export account address ZIP code | - |
| 10 | `EXP-ACCT-GROUP-ID` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export account group identifier | - |
| 10 | `FILLER` | `X(352)` | 352 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |
| 05 | `EXPORT-TRANSACTION-DATA` | - | - | group item | Export transaction data | REDEFINES `EXPORT-RECORD-DATA` |
| 10 | `EXP-TRAN-ID` | `X(16)` | 16 | alphanumeric (DISPLAY) | Export transaction identifier | - |
| 10 | `EXP-TRAN-TYPE-CD` | `X(02)` | 2 | alphanumeric (DISPLAY) | Export transaction type code | - |
| 10 | `EXP-TRAN-CAT-CD` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Export transaction category code | - |
| 10 | `EXP-TRAN-SOURCE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export transaction source | - |
| 10 | `EXP-TRAN-DESC` | `X(100)` | 100 | alphanumeric (DISPLAY) | Export transaction description | - |
| 10 | `EXP-TRAN-AMT` | `S9(09)V99` | 11 | packed decimal (COMP-3), 2 dp | Export transaction amount | - |
| 10 | `EXP-TRAN-MERCHANT-ID` | `9(09)` | 9 | binary (COMP), unsigned | Export transaction merchant identifier | - |
| 10 | `EXP-TRAN-MERCHANT-NAME` | `X(50)` | 50 | alphanumeric (DISPLAY) | Export transaction merchant name | - |
| 10 | `EXP-TRAN-MERCHANT-CITY` | `X(50)` | 50 | alphanumeric (DISPLAY) | Export transaction merchant city | - |
| 10 | `EXP-TRAN-MERCHANT-ZIP` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export transaction merchant ZIP code | - |
| 10 | `EXP-TRAN-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Export transaction card number | - |
| 10 | `EXP-TRAN-ORIG-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Export transaction orig timestamp | - |
| 10 | `EXP-TRAN-PROC-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Export transaction proc timestamp | - |
| 10 | `FILLER` | `X(140)` | 140 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |
| 05 | `EXPORT-CARD-XREF-DATA` | - | - | group item | Export card cross-reference data | REDEFINES `EXPORT-RECORD-DATA` |
| 10 | `EXP-XREF-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Export cross-reference card number | - |
| 10 | `EXP-XREF-CUST-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Export cross-reference customer identifier | - |
| 10 | `EXP-XREF-ACCT-ID` | `9(11)` | 11 | binary (COMP), unsigned | Export cross-reference account identifier | - |
| 10 | `FILLER` | `X(427)` | 427 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |
| 05 | `EXPORT-CARD-DATA` | - | - | group item | Export card data | REDEFINES `EXPORT-RECORD-DATA` |
| 10 | `EXP-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Export card number | - |
| 10 | `EXP-CARD-ACCT-ID` | `9(11)` | 11 | binary (COMP), unsigned | Export card account identifier | - |
| 10 | `EXP-CARD-CVV-CD` | `9(03)` | 3 | binary (COMP), unsigned | Export card card verification value code | - |
| 10 | `EXP-CARD-EMBOSSED-NAME` | `X(50)` | 50 | alphanumeric (DISPLAY) | Export card embossed name | - |
| 10 | `EXP-CARD-EXPIRAION-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Export card expiraion date | - |
| 10 | `EXP-CARD-ACTIVE-STATUS` | `X(01)` | 1 | alphanumeric (DISPLAY) | Export card active status | - |
| 10 | `FILLER` | `X(373)` | 373 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CSDAT01Y` — `app/cpy/CSDAT01Y.cpy`

Current-date / timestamp work fields. (58 lines, 39 non-comment lines, 39 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `WS-DATE-TIME` | - | - | group item | Ws date time | - |
| 05 | `WS-CURDATE-DATA` | - | - | group item | Ws curdate data | - |
| 10 | `WS-CURDATE` | - | - | group item | Ws curdate | - |
| 15 | `WS-CURDATE-YEAR` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Ws curdate year | - |
| 15 | `WS-CURDATE-MONTH` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curdate month | - |
| 15 | `WS-CURDATE-DAY` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curdate day | - |
| 10 | `WS-CURDATE-N` | `9(08)` | 8 | zoned decimal (DISPLAY), unsigned | Ws curdate n | REDEFINES `WS-CURDATE` |
| 10 | `WS-CURTIME` | - | - | group item | Ws curtime | - |
| 15 | `WS-CURTIME-HOURS` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curtime hours | - |
| 15 | `WS-CURTIME-MINUTE` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curtime minute | - |
| 15 | `WS-CURTIME-SECOND` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curtime second | - |
| 15 | `WS-CURTIME-MILSEC` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curtime milsec | - |
| 10 | `WS-CURTIME-N` | `9(08)` | 8 | zoned decimal (DISPLAY), unsigned | Ws curtime n | REDEFINES `WS-CURTIME` |
| 05 | `WS-CURDATE-MM-DD-YY` | - | - | group item | Ws curdate month day yy | - |
| 10 | `WS-CURDATE-MM` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curdate month | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE '/' |
| 10 | `WS-CURDATE-DD` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curdate day | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE '/' |
| 10 | `WS-CURDATE-YY` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curdate yy | - |
| 05 | `WS-CURTIME-HH-MM-SS` | - | - | group item | Ws curtime hour month second | - |
| 10 | `WS-CURTIME-HH` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curtime hour | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ':' |
| 10 | `WS-CURTIME-MM` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curtime month | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ':' |
| 10 | `WS-CURTIME-SS` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws curtime second | - |
| 05 | `WS-TIMESTAMP` | - | - | group item | Ws timestamp | - |
| 10 | `WS-TIMESTAMP-DT-YYYY` | `9(04)` | 4 | zoned decimal (DISPLAY), unsigned | Ws timestamp date year | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE '-' |
| 10 | `WS-TIMESTAMP-DT-MM` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws timestamp date month | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE '-' |
| 10 | `WS-TIMESTAMP-DT-DD` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws timestamp date day | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ' ' |
| 10 | `WS-TIMESTAMP-TM-HH` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws timestamp tm hour | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ':' |
| 10 | `WS-TIMESTAMP-TM-MM` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws timestamp tm month | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE ':' |
| 10 | `WS-TIMESTAMP-TM-SS` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws timestamp tm second | - |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE '.' |
| 10 | `WS-TIMESTAMP-TM-MS6` | `9(06)` | 6 | zoned decimal (DISPLAY), unsigned | Ws timestamp tm ms6 | - |

### `CODATECN` — `app/cpy/CODATECN.cpy`

Date constants and month/day tables. (52 lines, 36 non-comment lines, 36 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CODATECN-REC` | - | - | group item | Codatecn record | - |
| 05 | `CODATECN-IN-REC` | - | - | group item | Codatecn in record | - |
| 10 | `CODATECN-TYPE` | `X` | 1 | alphanumeric (DISPLAY) | Codatecn type | - |
| 88 | `YYYYMMDD-IN` | - | - | condition name | Condition name on `CODATECN-TYPE` | VALUE "1" |
| 88 | `YYYY-MM-DD-IN` | - | - | condition name | Condition name on `CODATECN-TYPE` | VALUE "2" |
| 10 | `CODATECN-INP-DATE` | `X(20)` | 20 | alphanumeric (DISPLAY) | Codatecn inp date | - |
| 10 | `CODATECN-1INP` | - | - | group item | Codatecn 1inp | REDEFINES `CODATECN-INP-DATE` |
| 15 | `CODATECN-1YYYY` | `XXXX` | 4 | alphanumeric (DISPLAY) | Codatecn 1yyyy | - |
| 15 | `CODATECN-1MM` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 1mm | - |
| 15 | `CODATECN-1DD` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 1dd | - |
| 15 | `CODATECN-1FIL` | `X(12)` | 12 | alphanumeric (DISPLAY) | Codatecn 1fil | - |
| 10 | `CODATECN-2INP` | - | - | group item | Codatecn 2inp | REDEFINES `CODATECN-INP-DATE` |
| 15 | `CODATECN-1O-YYYY` | `XXXX` | 4 | alphanumeric (DISPLAY) | Codatecn 1o year | - |
| 15 | `CODATECN-1I-S1` | `X` | 1 | alphanumeric (DISPLAY) | Codatecn 1i s1 | - |
| 15 | `CODATECN-1MM` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 1mm | - |
| 15 | `CODATECN-1I-S2` | `X` | 1 | alphanumeric (DISPLAY) | Codatecn 1i s2 | - |
| 15 | `CODATECN-2YY` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 2yy | - |
| 15 | `CODATECN-2FIL` | `X(10)` | 10 | alphanumeric (DISPLAY) | Codatecn 2fil | - |
| 05 | `CODATECN-OUT-REC` | - | - | group item | Codatecn out record | - |
| 10 | `CODATECN-OUTTYPE` | `X` | 1 | alphanumeric (DISPLAY) | Codatecn outtype | - |
| 88 | `YYYY-MM-DD-OP` | - | - | condition name | Condition name on `CODATECN-OUTTYPE` | VALUE "1" |
| 88 | `YYYYMMDD-OP` | - | - | condition name | Condition name on `CODATECN-OUTTYPE` | VALUE "2" |
| 10 | `CODATECN-0UT-DATE` | `X(20)` | 20 | alphanumeric (DISPLAY) | Codatecn 0ut date | - |
| 10 | `CODATECN-1OUT` | - | - | group item | Codatecn 1out | REDEFINES `CODATECN-0UT-DATE` |
| 15 | `CODATECN-1O-YYYY` | `XXXX` | 4 | alphanumeric (DISPLAY) | Codatecn 1o year | - |
| 15 | `CODATECN-1O-S1` | `X` | 1 | alphanumeric (DISPLAY) | Codatecn 1o s1 | - |
| 15 | `CODATECN-1O-MM` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 1o month | - |
| 15 | `CODATECN-1O-S2` | `X` | 1 | alphanumeric (DISPLAY) | Codatecn 1o s2 | - |
| 15 | `CODATECN-1O-DD` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 1o day | - |
| 15 | `CODATECN-1OFIL` | `X(10)` | 10 | alphanumeric (DISPLAY) | Codatecn 1ofil | - |
| 10 | `CODATECN-2OUT` | - | - | group item | Codatecn 2out | REDEFINES `CODATECN-0UT-DATE` |
| 15 | `CODATECN-2O-YYYY` | `XXXX` | 4 | alphanumeric (DISPLAY) | Codatecn 2o year | - |
| 15 | `CODATECN-2O-MM` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 2o month | - |
| 15 | `CODATECN-2O-DD` | `XX` | 2 | alphanumeric (DISPLAY) | Codatecn 2o day | - |
| 15 | `CODATECN-2OFIL` | `X(12)` | 12 | alphanumeric (DISPLAY) | Codatecn 2ofil | - |
| 05 | `CODATECN-ERROR-MSG` | `X(38)` | 38 | alphanumeric (DISPLAY) | Codatecn error message | - |

### `CSUTLDWY` — `app/cpy/CSUTLDWY.cpy`

Date-edit work area consumed by the `CSUTLDPY` procedural copybook. (89 lines, 82 non-comment lines, 59 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 10 | `WS-EDIT-DATE-CCYYMMDD` | - | - | group item | Ws edit date ccyymmdd | - |
| 20 | `WS-EDIT-DATE-CCYY` | - | - | group item | Ws edit date century year | - |
| 25 | `WS-EDIT-DATE-CC` | `X(2)` | 2 | alphanumeric (DISPLAY) | Ws edit date cc | - |
| 25 | `WS-EDIT-DATE-CC-N` | `9(2)` | 2 | zoned decimal (DISPLAY), unsigned | Ws edit date cc n | REDEFINES `WS-EDIT-DATE-CC` |
| 88 | `THIS-CENTURY` | - | - | condition name | Condition name on `WS-EDIT-DATE-CC-N` | VALUE 20 |
| 88 | `LAST-CENTURY` | - | - | condition name | Condition name on `WS-EDIT-DATE-CC-N` | VALUE 19 |
| 25 | `WS-EDIT-DATE-YY` | `X(2)` | 2 | alphanumeric (DISPLAY) | Ws edit date yy | - |
| 25 | `WS-EDIT-DATE-YY-N` | `9(2)` | 2 | zoned decimal (DISPLAY), unsigned | Ws edit date yy n | REDEFINES `WS-EDIT-DATE-YY` |
| 20 | `WS-EDIT-DATE-CCYY-N` | `9(4)` | 4 | zoned decimal (DISPLAY), unsigned | Ws edit date century year n | REDEFINES `WS-EDIT-DATE-CCYY` |
| 20 | `WS-EDIT-DATE-MM` | `X(2)` | 2 | alphanumeric (DISPLAY) | Ws edit date month | - |
| 20 | `WS-EDIT-DATE-MM-N` | `9(2)` | 2 | zoned decimal (DISPLAY), unsigned | Ws edit date month n | REDEFINES `WS-EDIT-DATE-MM` |
| 88 | `WS-VALID-MONTH` | - | - | condition name | Condition name on `WS-EDIT-DATE-MM-N` | VALUE 1 THROUGH 12 |
| 88 | `WS-31-DAY-MONTH` | - | - | condition name | Condition name on `WS-EDIT-DATE-MM-N` | VALUES 1, 3, 5, 7, 8, 10, 12 |
| 88 | `WS-FEBRUARY` | - | - | condition name | Condition name on `WS-EDIT-DATE-MM-N` | VALUE 2 |
| 20 | `WS-EDIT-DATE-DD` | `X(2)` | 2 | alphanumeric (DISPLAY) | Ws edit date day | - |
| 20 | `WS-EDIT-DATE-DD-N` | `9(2)` | 2 | zoned decimal (DISPLAY), unsigned | Ws edit date day n | REDEFINES `WS-EDIT-DATE-DD` |
| 88 | `WS-VALID-DAY` | - | - | condition name | Condition name on `WS-EDIT-DATE-DD-N` | VALUE 1 THROUGH 31 |
| 88 | `WS-DAY-31` | - | - | condition name | Condition name on `WS-EDIT-DATE-DD-N` | VALUE 31 |
| 88 | `WS-DAY-30` | - | - | condition name | Condition name on `WS-EDIT-DATE-DD-N` | VALUE 30 |
| 88 | `WS-DAY-29` | - | - | condition name | Condition name on `WS-EDIT-DATE-DD-N` | VALUE 29 |
| 88 | `WS-VALID-FEB-DAY` | - | - | condition name | Condition name on `WS-EDIT-DATE-DD-N` | VALUE 1 THROUGH 28 |
| 10 | `WS-EDIT-DATE-CCYYMMDD-N` | `9(8)` | 8 | zoned decimal (DISPLAY), unsigned | Ws edit date ccyymmdd n | REDEFINES `WS-EDIT-DATE-CCYYMMDD` |
| 10 | `WS-EDIT-DATE-BINARY` | `S9(9)` | 9 | binary (Binary), signed | Ws edit date binary | - |
| 10 | `WS-CURRENT-DATE` | - | - | group item | Ws current date | - |
| 20 | `WS-CURRENT-DATE-YYYYMMDD` | `X(8)` | 8 | alphanumeric (DISPLAY) | Ws current date yyyymmdd | - |
| 20 | `WS-CURRENT-DATE-YYYYMMDD-N` | `9(8)` | 8 | zoned decimal (DISPLAY), unsigned | Ws current date yyyymmdd n | REDEFINES `WS-CURRENT-DATE-YYYYMMDD` |
| 20 | `WS-CURRENT-DATE-BINARY` | `S9(9)` | 9 | binary (Binary), signed | Ws current date binary | - |
| 10 | `WS-EDIT-DATE-FLGS` | - | - | group item | Ws edit date flags | - |
| 88 | `WS-EDIT-DATE-IS-VALID` | - | - | condition name | Condition name on `WS-EDIT-DATE-FLGS` | VALUE LOW-VALUES |
| 88 | `WS-EDIT-DATE-IS-INVALID` | - | - | condition name | Condition name on `WS-EDIT-DATE-FLGS` | VALUE '000' |
| 20 | `WS-EDIT-YEAR-FLG` | `X(01)` | 1 | alphanumeric (DISPLAY) | Ws edit year flag | - |
| 88 | `FLG-YEAR-ISVALID` | - | - | condition name | Condition name on `WS-EDIT-YEAR-FLG` | VALUE LOW-VALUES |
| 88 | `FLG-YEAR-NOT-OK` | - | - | condition name | Condition name on `WS-EDIT-YEAR-FLG` | VALUE '0' |
| 88 | `FLG-YEAR-BLANK` | - | - | condition name | Condition name on `WS-EDIT-YEAR-FLG` | VALUE 'B' |
| 20 | `WS-EDIT-MONTH` | `X(01)` | 1 | alphanumeric (DISPLAY) | Ws edit month | - |
| 88 | `FLG-MONTH-ISVALID` | - | - | condition name | Condition name on `WS-EDIT-MONTH` | VALUE LOW-VALUES |
| 88 | `FLG-MONTH-NOT-OK` | - | - | condition name | Condition name on `WS-EDIT-MONTH` | VALUE '0' |
| 88 | `FLG-MONTH-BLANK` | - | - | condition name | Condition name on `WS-EDIT-MONTH` | VALUE 'B' |
| 20 | `WS-EDIT-DAY` | `X(01)` | 1 | alphanumeric (DISPLAY) | Ws edit day | - |
| 88 | `FLG-DAY-ISVALID` | - | - | condition name | Condition name on `WS-EDIT-DAY` | VALUE LOW-VALUES |
| 88 | `FLG-DAY-NOT-OK` | - | - | condition name | Condition name on `WS-EDIT-DAY` | VALUE '0' |
| 88 | `FLG-DAY-BLANK` | - | - | condition name | Condition name on `WS-EDIT-DAY` | VALUE 'B' |
| 10 | `WS-DATE-FORMAT` | `X(08)` | 8 | alphanumeric (DISPLAY) | Ws date format | VALUE 'YYYYMMDD' |
| 10 | `WS-DATE-VALIDATION-RESULT` | - | - | group item | Ws date validation result | - |
| 20 | `WS-SEVERITY` | `X(04)` | 4 | alphanumeric (DISPLAY) | Ws severity | - |
| 20 | `WS-SEVERITY-N` | `9(4)` | 4 | zoned decimal (DISPLAY), unsigned | Ws severity n | REDEFINES `WS-SEVERITY` |
| 20 | `FILLER` | `X(11)` | 11 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Mesg Code:' |
| 20 | `WS-MSG-NO` | `X(04)` | 4 | alphanumeric (DISPLAY) | Ws message no | - |
| 20 | `WS-MSG-NO-N` | `9(4)` | 4 | zoned decimal (DISPLAY), unsigned | Ws message no n | REDEFINES `WS-MSG-NO` |
| 20 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACE |
| 20 | `WS-RESULT` | `X(15)` | 15 | alphanumeric (DISPLAY) | Ws result | - |
| 20 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACE |
| 20 | `FILLER` | `X(09)` | 9 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'TstDate:' |
| 20 | `WS-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Ws date | - |
| 20 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACE |
| 20 | `FILLER` | `X(10)` | 10 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Mask used:' |
| 20 | `WS-DATE-FMT` | `X(10)` | 10 | alphanumeric (DISPLAY) | Ws date fmt | - |
| 20 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACE |
| 20 | `FILLER` | `X(03)` | 3 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE SPACES |

### `CSUTLDPY` — `app/cpy/CSUTLDPY.cpy`

Procedural copybook (no data items): paragraphs `EDIT-DATE-CCYYMMDD`, `EDIT-YEAR-CCYY`, `EDIT-MONTH`, `EDIT-DAY`, `EDIT-DAY-MONTH-YEAR` that validate a date held in `CSUTLDWY` fields; contains deliberate `GO TO` exits. (375 lines, 289 non-comment lines, 0 data items)


### `CSSTRPFY` — `app/cpy/CSSTRPFY.cpy`

Procedural copybook (no data items): `EVALUATE EIBAID` that maps the CICS attention identifier onto the `CCARD-AID-*` condition names. (85 lines, 63 non-comment lines, 0 data items)


### `CSSETATY` — `app/cpy/CSSETATY.cpy`

Procedural copybook (no data items): parameterised snippet that sets a BMS field to `DFHRED` and `*` when the matching `FLG-...-NOT-OK` / `-BLANK` flag is on. (30 lines, 10 non-comment lines, 0 data items)


### `CSLKPCDY` — `app/cpy/CSLKPCDY.cpy`

Lookup-code repository: three `01`-level edit fields whose `88 ... VALUES` lists hold the NANPA area codes, the US state codes and the state + first-two-of-ZIP combinations. (1318 lines, 1283 non-comment lines, 10 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `WS-US-PHONE-AREA-CODE-TO-EDIT` | `XXX` | 3 | alphanumeric (DISPLAY) | Ws us phone area code to edit | - |
| 88 | `VALID-PHONE-AREA-CODE` | - | - | condition name | Condition name on `WS-US-PHONE-AREA-CODE-TO-EDIT` | VALUES '201', '202', '203', '204', '205', '206', '207', '208', '... (490 literals) |
| 88 | `VALID-GENERAL-PURP-CODE` | - | - | condition name | Condition name on `WS-US-PHONE-AREA-CODE-TO-EDIT` | VALUES '201', '202', '203', '204', '205', '206', '207', '208', '... (410 literals) |
| 88 | `VALID-EASY-RECOG-AREA-CODE` | - | - | condition name | Condition name on `WS-US-PHONE-AREA-CODE-TO-EDIT` | VALUES '200', '211', '222', '233', '244', '255', '266', '277', '... (80 literals) |
| 01 | `US-STATE-CODE-TO-EDIT` | `X(2)` | 2 | alphanumeric (DISPLAY) | Us state code to edit | - |
| 88 | `VALID-US-STATE-CODE` | - | - | condition name | Condition name on `US-STATE-CODE-TO-EDIT` | VALUES 'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA... (56 literals) |
| 01 | `US-STATE-ZIPCODE-TO-EDIT` | - | - | group item | Us state zipcode to edit | - |
| 02 | `US-STATE-AND-FIRST-ZIP2` | `X(4)` | 4 | alphanumeric (DISPLAY) | Us state and first zip2 | - |
| 88 | `VALID-US-STATE-ZIP-CD2-COMBO` | - | - | condition name | Condition name on `US-STATE-AND-FIRST-ZIP2` | VALUES 'AA34', 'AE90', 'AE91', 'AE92', 'AE93', 'AE94', 'AE95', '... (240 literals) |
| 02 | `LAST-3-OF-ZIP` | `X(3)` | 3 | alphanumeric (DISPLAY) | Last 3 of ZIP code | - |

### `CSMSG01Y` — `app/cpy/CSMSG01Y.cpy`

Standard error / thank-you messages. (24 lines, 5 non-comment lines, 3 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CCDA-COMMON-MESSAGES` | - | - | group item | Ccda common messages | - |
| 05 | `CCDA-MSG-THANK-YOU` | `X(50)` | 50 | alphanumeric (DISPLAY) | Ccda message thank you | VALUE 'Thank you for using CardDemo application |
| 05 | `CCDA-MSG-INVALID-KEY` | `X(50)` | 50 | alphanumeric (DISPLAY) | Ccda message invalid key | VALUE 'Invalid key pressed |

### `CSMSG02Y` — `app/cpy/CSMSG02Y.cpy`

Sign-on and abend message fields. (35 lines, 9 non-comment lines, 5 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `ABEND-DATA` | - | - | group item | Abend data | - |
| 05 | `ABEND-CODE` | `X(4)` | 4 | alphanumeric (DISPLAY) | Abend code | VALUE SPACES |
| 05 | `ABEND-CULPRIT` | `X(8)` | 8 | alphanumeric (DISPLAY) | Abend culprit | VALUE SPACES |
| 05 | `ABEND-REASON` | `X(50)` | 50 | alphanumeric (DISPLAY) | Abend reason | VALUE SPACES |
| 05 | `ABEND-MSG` | `X(72)` | 72 | alphanumeric (DISPLAY) | Abend message | VALUE SPACES |

### `COADM02Y` — `app/cpy/COADM02Y.cpy`

Admin menu option table (6 options, `OCCURS 9` redefinition). (62 lines, 32 non-comment lines, 26 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CARDDEMO-ADMIN-MENU-OPTIONS` | - | - | group item | Carddemo admin menu options | - |
| 05 | `CDEMO-ADMIN-OPT-COUNT` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Cdemo admin menu option count | VALUE 6 |
| 05 | `CDEMO-ADMIN-OPTIONS-DATA` | - | - | group item | Cdemo admin options data | - |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 1 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'User List (Security) ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COUSR00C' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 2 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'User Add (Security) ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COUSR01C' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 3 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'User Update (Security) ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COUSR02C' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 4 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'User Delete (Security) ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COUSR03C' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 5 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction Type List/Update (Db2) ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COTRTLIC' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 6 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction Type Maintenance (Db2) ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COTRTUPC' |
| 05 | `CDEMO-ADMIN-OPTIONS` | - | - | group item | Cdemo admin options | REDEFINES `CDEMO-ADMIN-OPTIONS-DATA` |
| 10 | `CDEMO-ADMIN-OPT` | - | - | group item | Cdemo admin menu option | OCCURS 9 |
| 15 | `CDEMO-ADMIN-OPT-NUM` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Cdemo admin menu option number | - |
| 15 | `CDEMO-ADMIN-OPT-NAME` | `X(35)` | 35 | alphanumeric (DISPLAY) | Cdemo admin menu option name | - |
| 15 | `CDEMO-ADMIN-OPT-PGMNAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Cdemo admin menu option pgmname | - |

### `COMEN02Y` — `app/cpy/COMEN02Y.cpy`

Regular-user menu option table. (101 lines, 64 non-comment lines, 53 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CARDDEMO-MAIN-MENU-OPTIONS` | - | - | group item | Carddemo main menu options | - |
| 05 | `CDEMO-MENU-OPT-COUNT` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Cdemo menu menu option count | VALUE 11 |
| 05 | `CDEMO-MENU-OPTIONS-DATA` | - | - | group item | Cdemo menu options data | - |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 1 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Account View ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COACTVWC' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 2 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Account Update ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COACTUPC' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 3 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Credit Card List ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COCRDLIC' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 4 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Credit Card View ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COCRDSLC' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 5 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Credit Card Update ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COCRDUPC' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 6 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction List ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COTRN00C' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 7 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction View ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COTRN01C' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 8 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction Add ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COTRN02C' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 9 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Transaction Reports ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'CORPT00C' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 10 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Bill Payment ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COBIL00C' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 10 | `FILLER` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Literal text / column spacing constant | VALUE 11 |
| 10 | `FILLER` | `X(35)` | 35 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'Pending Authorization View ' |
| 10 | `FILLER` | `X(08)` | 8 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'COPAUS0C' |
| 10 | `FILLER` | `X(01)` | 1 | alphanumeric (DISPLAY) | Literal text / column spacing constant | VALUE 'U' |
| 05 | `CDEMO-MENU-OPTIONS` | - | - | group item | Cdemo menu options | REDEFINES `CDEMO-MENU-OPTIONS-DATA` |
| 10 | `CDEMO-MENU-OPT` | - | - | group item | Cdemo menu menu option | OCCURS 12 |
| 15 | `CDEMO-MENU-OPT-NUM` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Cdemo menu menu option number | - |
| 15 | `CDEMO-MENU-OPT-NAME` | `X(35)` | 35 | alphanumeric (DISPLAY) | Cdemo menu menu option name | - |
| 15 | `CDEMO-MENU-OPT-PGMNAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Cdemo menu menu option pgmname | - |
| 15 | `CDEMO-MENU-OPT-USRTYPE` | `X(01)` | 1 | alphanumeric (DISPLAY) | Cdemo menu menu option usrtype | - |

### `COTTL01Y` — `app/cpy/COTTL01Y.cpy`

Screen title constants. (27 lines, 7 non-comment lines, 4 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `CCDA-SCREEN-TITLE` | - | - | group item | Ccda screen screen title | - |
| 05 | `CCDA-TITLE01` | `X(40)` | 40 | alphanumeric (DISPLAY) | Ccda title01 | VALUE ' AWS Mainframe Modernization ' |
| 05 | `CCDA-TITLE02` | `X(40)` | 40 | alphanumeric (DISPLAY) | Ccda title02 | VALUE ' CardDemo ' |
| 05 | `CCDA-THANK-YOU` | `X(40)` | 40 | alphanumeric (DISPLAY) | Ccda thank you | VALUE 'Thank you for using CCDA application |

### `UNUSED1Y` — `app/cpy/UNUSED1Y.cpy`

Declared but referenced by no program in this repository. (10 lines, 7 non-comment lines, 7 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `UNUSED-DATA` | - | - | group item | Unused data | - |
| 05 | `UNUSED-ID` | `X(08)` | 8 | alphanumeric (DISPLAY) | Unused identifier | - |
| 05 | `UNUSED-FNAME` | `X(20)` | 20 | alphanumeric (DISPLAY) | Unused first name | - |
| 05 | `UNUSED-LNAME` | `X(20)` | 20 | alphanumeric (DISPLAY) | Unused last name | - |
| 05 | `UNUSED-PWD` | `X(08)` | 8 | alphanumeric (DISPLAY) | Unused password | - |
| 05 | `UNUSED-TYPE` | `X(01)` | 1 | alphanumeric (DISPLAY) | Unused type | - |
| 05 | `UNUSED-FILLER` | `X(23)` | 23 | alphanumeric (DISPLAY) | Unused filler | - |

## 7. Authorization extension (IMS / DB2 / MQ)

Copybooks and the DCLGEN under `app/app-authorization-ims-db2-mq/`.


### `CIPAUDTY` — `app/app-authorization-ims-db2-mq/cpy/CIPAUDTY.cpy`

IMS `PAUTDTL1` pending-authorization detail segment layout. (54 lines, 36 non-comment lines, 36 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 05 | `PA-AUTHORIZATION-KEY` | - | - | group item | Pending-authorization authorization key | - |
| 10 | `PA-AUTH-DATE-9C` | `S9(05)` | 5 | packed decimal (COMP-3) | Pending-authorization authorization date 9c | - |
| 10 | `PA-AUTH-TIME-9C` | `S9(09)` | 9 | packed decimal (COMP-3) | Pending-authorization authorization time 9c | - |
| 05 | `PA-AUTH-ORIG-DATE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization authorization orig date | - |
| 05 | `PA-AUTH-ORIG-TIME` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization authorization orig time | - |
| 05 | `PA-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Pending-authorization card number | - |
| 05 | `PA-AUTH-TYPE` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization authorization type | - |
| 05 | `PA-CARD-EXPIRY-DATE` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization card expiry date | - |
| 05 | `PA-MESSAGE-TYPE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization message type | - |
| 05 | `PA-MESSAGE-SOURCE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization message source | - |
| 05 | `PA-AUTH-ID-CODE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization authorization identifier code | - |
| 05 | `PA-AUTH-RESP-CODE` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pending-authorization authorization CICS response code | - |
| 88 | `PA-AUTH-APPROVED` | - | - | condition name | Condition name on `PA-AUTH-RESP-CODE` | VALUE '00' |
| 05 | `PA-AUTH-RESP-REASON` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization authorization CICS response reason | - |
| 05 | `PA-PROCESSING-CODE` | `9(06)` | 6 | zoned decimal (DISPLAY), unsigned | Pending-authorization processing code | - |
| 05 | `PA-TRANSACTION-AMT` | `S9(10)V99` | 12 | packed decimal (COMP-3), 2 dp | Pending-authorization transaction amount | - |
| 05 | `PA-APPROVED-AMT` | `S9(10)V99` | 12 | packed decimal (COMP-3), 2 dp | Pending-authorization approved amount | - |
| 05 | `PA-MERCHANT-CATAGORY-CODE` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization merchant catagory code | - |
| 05 | `PA-ACQR-COUNTRY-CODE` | `X(03)` | 3 | alphanumeric (DISPLAY) | Pending-authorization acqr country code | - |
| 05 | `PA-POS-ENTRY-MODE` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Pending-authorization pos entry mode | - |
| 05 | `PA-MERCHANT-ID` | `X(15)` | 15 | alphanumeric (DISPLAY) | Pending-authorization merchant identifier | - |
| 05 | `PA-MERCHANT-NAME` | `X(22)` | 22 | alphanumeric (DISPLAY) | Pending-authorization merchant name | - |
| 05 | `PA-MERCHANT-CITY` | `X(13)` | 13 | alphanumeric (DISPLAY) | Pending-authorization merchant city | - |
| 05 | `PA-MERCHANT-STATE` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pending-authorization merchant state | - |
| 05 | `PA-MERCHANT-ZIP` | `X(09)` | 9 | alphanumeric (DISPLAY) | Pending-authorization merchant ZIP code | - |
| 05 | `PA-TRANSACTION-ID` | `X(15)` | 15 | alphanumeric (DISPLAY) | Pending-authorization transaction identifier | - |
| 05 | `PA-MATCH-STATUS` | `X(01)` | 1 | alphanumeric (DISPLAY) | Pending-authorization match status | - |
| 88 | `PA-MATCH-PENDING` | - | - | condition name | Condition name on `PA-MATCH-STATUS` | VALUE 'P' |
| 88 | `PA-MATCH-AUTH-DECLINED` | - | - | condition name | Condition name on `PA-MATCH-STATUS` | VALUE 'D' |
| 88 | `PA-MATCH-PENDING-EXPIRED` | - | - | condition name | Condition name on `PA-MATCH-STATUS` | VALUE 'E' |
| 88 | `PA-MATCHED-WITH-TRAN` | - | - | condition name | Condition name on `PA-MATCH-STATUS` | VALUE 'M' |
| 05 | `PA-AUTH-FRAUD` | `X(01)` | 1 | alphanumeric (DISPLAY) | Pending-authorization authorization fraud | - |
| 88 | `PA-FRAUD-CONFIRMED` | - | - | condition name | Condition name on `PA-AUTH-FRAUD` | VALUE 'F' |
| 88 | `PA-FRAUD-REMOVED` | - | - | condition name | Condition name on `PA-AUTH-FRAUD` | VALUE 'R' |
| 05 | `PA-FRAUD-RPT-DATE` | `X(08)` | 8 | alphanumeric (DISPLAY) | Pending-authorization fraud rpt date | - |
| 05 | `FILLER` | `X(17)` | 17 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CIPAUSMY` — `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy`

IMS `PAUTSUM0` pending-authorization summary segment layout. (31 lines, 13 non-comment lines, 13 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 05 | `PA-ACCT-ID` | `S9(11)` | 11 | packed decimal (COMP-3) | Pending-authorization account identifier | - |
| 05 | `PA-CUST-ID` | `9(09)` | 9 | zoned decimal (DISPLAY), unsigned | Pending-authorization customer identifier | - |
| 05 | `PA-AUTH-STATUS` | `X(01)` | 1 | alphanumeric (DISPLAY) | Pending-authorization authorization status | - |
| 05 | `PA-ACCOUNT-STATUS` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pending-authorization account status | OCCURS 5 |
| 05 | `PA-CREDIT-LIMIT` | `S9(09)V99` | 11 | packed decimal (COMP-3), 2 dp | Pending-authorization credit limit | - |
| 05 | `PA-CASH-LIMIT` | `S9(09)V99` | 11 | packed decimal (COMP-3), 2 dp | Pending-authorization cash limit | - |
| 05 | `PA-CREDIT-BALANCE` | `S9(09)V99` | 11 | packed decimal (COMP-3), 2 dp | Pending-authorization credit balance | - |
| 05 | `PA-CASH-BALANCE` | `S9(09)V99` | 11 | packed decimal (COMP-3), 2 dp | Pending-authorization cash balance | - |
| 05 | `PA-APPROVED-AUTH-CNT` | `S9(04)` | 4 | binary (COMP), signed | Pending-authorization approved authorization count | - |
| 05 | `PA-DECLINED-AUTH-CNT` | `S9(04)` | 4 | binary (COMP), signed | Pending-authorization declined authorization count | - |
| 05 | `PA-APPROVED-AUTH-AMT` | `S9(09)V99` | 11 | packed decimal (COMP-3), 2 dp | Pending-authorization approved authorization amount | - |
| 05 | `PA-DECLINED-AUTH-AMT` | `S9(09)V99` | 11 | packed decimal (COMP-3), 2 dp | Pending-authorization declined authorization amount | - |
| 05 | `FILLER` | `X(34)` | 34 | alphanumeric (DISPLAY) | Reserved slack bytes (pads the record to its catalogued length) | - |

### `CCPAURQY` — `app/app-authorization-ims-db2-mq/cpy/CCPAURQY.cpy`

MQ authorization request message layout. (36 lines, 18 non-comment lines, 18 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 05 | `PA-RQ-AUTH-DATE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization rq authorization date | - |
| 05 | `PA-RQ-AUTH-TIME` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization rq authorization time | - |
| 05 | `PA-RQ-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Pending-authorization rq card number | - |
| 05 | `PA-RQ-AUTH-TYPE` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization rq authorization type | - |
| 05 | `PA-RQ-CARD-EXPIRY-DATE` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization rq card expiry date | - |
| 05 | `PA-RQ-MESSAGE-TYPE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization rq message type | - |
| 05 | `PA-RQ-MESSAGE-SOURCE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization rq message source | - |
| 05 | `PA-RQ-PROCESSING-CODE` | `9(06)` | 6 | zoned decimal (DISPLAY), unsigned | Pending-authorization rq processing code | - |
| 05 | `PA-RQ-TRANSACTION-AMT` | `+9(10).99` | 12 | edited numeric (display) | Pending-authorization rq transaction amount | - |
| 05 | `PA-RQ-MERCHANT-CATAGORY-CODE` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization rq merchant catagory code | - |
| 05 | `PA-RQ-ACQR-COUNTRY-CODE` | `X(03)` | 3 | alphanumeric (DISPLAY) | Pending-authorization rq acqr country code | - |
| 05 | `PA-RQ-POS-ENTRY-MODE` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Pending-authorization rq pos entry mode | - |
| 05 | `PA-RQ-MERCHANT-ID` | `X(15)` | 15 | alphanumeric (DISPLAY) | Pending-authorization rq merchant identifier | - |
| 05 | `PA-RQ-MERCHANT-NAME` | `X(22)` | 22 | alphanumeric (DISPLAY) | Pending-authorization rq merchant name | - |
| 05 | `PA-RQ-MERCHANT-CITY` | `X(13)` | 13 | alphanumeric (DISPLAY) | Pending-authorization rq merchant city | - |
| 05 | `PA-RQ-MERCHANT-STATE` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pending-authorization rq merchant state | - |
| 05 | `PA-RQ-MERCHANT-ZIP` | `X(09)` | 9 | alphanumeric (DISPLAY) | Pending-authorization rq merchant ZIP code | - |
| 05 | `PA-RQ-TRANSACTION-ID` | `X(15)` | 15 | alphanumeric (DISPLAY) | Pending-authorization rq transaction identifier | - |

### `CCPAURLY` — `app/app-authorization-ims-db2-mq/cpy/CCPAURLY.cpy`

MQ authorization reply message layout. (24 lines, 6 non-comment lines, 6 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 05 | `PA-RL-CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Pending-authorization rl card number | - |
| 05 | `PA-RL-TRANSACTION-ID` | `X(15)` | 15 | alphanumeric (DISPLAY) | Pending-authorization rl transaction identifier | - |
| 05 | `PA-RL-AUTH-ID-CODE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Pending-authorization rl authorization identifier code | - |
| 05 | `PA-RL-AUTH-RESP-CODE` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pending-authorization rl authorization CICS response code | - |
| 05 | `PA-RL-AUTH-RESP-REASON` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending-authorization rl authorization CICS response reason | - |
| 05 | `PA-RL-APPROVED-AMT` | `+9(10).99` | 12 | edited numeric (display) | Pending-authorization rl approved amount | - |

### `CCPAUERY` — `app/app-authorization-ims-db2-mq/cpy/CCPAUERY.cpy`

Authorization error/reason code area. (40 lines, 22 non-comment lines, 22 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `ERROR-LOG-RECORD` | - | - | group item | Error log record | - |
| 05 | `ERR-DATE` | `X(06)` | 6 | alphanumeric (DISPLAY) | Error date | - |
| 05 | `ERR-TIME` | `X(06)` | 6 | alphanumeric (DISPLAY) | Error time | - |
| 05 | `ERR-APPLICATION` | `X(08)` | 8 | alphanumeric (DISPLAY) | Error application | - |
| 05 | `ERR-PROGRAM` | `X(08)` | 8 | alphanumeric (DISPLAY) | Error program | - |
| 05 | `ERR-LOCATION` | `X(04)` | 4 | alphanumeric (DISPLAY) | Error location | - |
| 05 | `ERR-LEVEL` | `X(01)` | 1 | alphanumeric (DISPLAY) | Error level | - |
| 88 | `ERR-LOG` | - | - | condition name | Condition name on `ERR-LEVEL` | VALUE 'L' |
| 88 | `ERR-INFO` | - | - | condition name | Condition name on `ERR-LEVEL` | VALUE 'I' |
| 88 | `ERR-WARNING` | - | - | condition name | Condition name on `ERR-LEVEL` | VALUE 'W' |
| 88 | `ERR-CRITICAL` | - | - | condition name | Condition name on `ERR-LEVEL` | VALUE 'C' |
| 05 | `ERR-SUBSYSTEM` | `X(01)` | 1 | alphanumeric (DISPLAY) | Error subsystem | - |
| 88 | `ERR-APP` | - | - | condition name | Condition name on `ERR-SUBSYSTEM` | VALUE 'A' |
| 88 | `ERR-CICS` | - | - | condition name | Condition name on `ERR-SUBSYSTEM` | VALUE 'C' |
| 88 | `ERR-IMS` | - | - | condition name | Condition name on `ERR-SUBSYSTEM` | VALUE 'I' |
| 88 | `ERR-DB2` | - | - | condition name | Condition name on `ERR-SUBSYSTEM` | VALUE 'D' |
| 88 | `ERR-MQ` | - | - | condition name | Condition name on `ERR-SUBSYSTEM` | VALUE 'M' |
| 88 | `ERR-FILE` | - | - | condition name | Condition name on `ERR-SUBSYSTEM` | VALUE 'F' |
| 05 | `ERR-CODE-1` | `X(09)` | 9 | alphanumeric (DISPLAY) | Error code 1 | - |
| 05 | `ERR-CODE-2` | `X(09)` | 9 | alphanumeric (DISPLAY) | Error code 2 | - |
| 05 | `ERR-MESSAGE` | `X(50)` | 50 | alphanumeric (DISPLAY) | Error message | - |
| 05 | `ERR-EVENT-KEY` | `X(20)` | 20 | alphanumeric (DISPLAY) | Error event key | - |

### `IMSFUNCS` — `app/app-authorization-ims-db2-mq/cpy/IMSFUNCS.cpy`

IMS DL/I function codes (`GU`, `GN`, `GHU`, `ISRT`, `REPL`, `DLET`, ...). (27 lines, 11 non-comment lines, 11 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `FUNC-CODES` | - | - | group item | Func codes | - |
| 05 | `FUNC-GU` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func gu | VALUE 'GU ' |
| 05 | `FUNC-GHU` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func ghu | VALUE 'GHU ' |
| 05 | `FUNC-GN` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func gn | VALUE 'GN ' |
| 05 | `FUNC-GHN` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func ghn | VALUE 'GHN ' |
| 05 | `FUNC-GNP` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func gnp | VALUE 'GNP ' |
| 05 | `FUNC-GHNP` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func ghnp | VALUE 'GHNP' |
| 05 | `FUNC-REPL` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func repl | VALUE 'REPL' |
| 05 | `FUNC-ISRT` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func isrt | VALUE 'ISRT' |
| 05 | `FUNC-DLET` | `X(04)` | 4 | alphanumeric (DISPLAY) | Func dlet | VALUE 'DLET' |
| 05 | `PARMCOUNT` | `S9(05)` | 5 | binary (Comp-5), signed | Parmcount | VALUE +4 |

### `PADFLPCB` — `app/app-authorization-ims-db2-mq/cpy/PADFLPCB.CPY`

IMS PCB mask for the detail database. (26 lines, 10 non-comment lines, 10 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `PADFLPCB` | - | - | group item | Padflpcb | - |
| 05 | `PADFL-DBDNAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Padfl dbdname | - |
| 05 | `PADFL-SEG-LEVEL` | `X(02)` | 2 | alphanumeric (DISPLAY) | Padfl IMS segment level | - |
| 05 | `PADFL-PCB-STATUS` | `X(02)` | 2 | alphanumeric (DISPLAY) | Padfl IMS PCB status | - |
| 05 | `PADFL-PCB-PROCOPT` | `X(04)` | 4 | alphanumeric (DISPLAY) | Padfl IMS PCB procopt | - |
| 05 | `FILLER` | `S9(05)` | 5 | binary (COMP), signed | Reserved slack bytes (pads the record to its catalogued length) | - |
| 05 | `PADFL-SEG-NAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Padfl IMS segment name | - |
| 05 | `PADFL-KEYFB-NAME` | `S9(05)` | 5 | binary (COMP), signed | Padfl keyfb name | - |
| 05 | `PADFL-NUM-SENSEGS` | `S9(05)` | 5 | binary (COMP), signed | Padfl number sensegs | - |
| 05 | `PADFL-KEYFB` | `X(255)` | 255 | alphanumeric (DISPLAY) | Padfl keyfb | - |

### `PASFLPCB` — `app/app-authorization-ims-db2-mq/cpy/PASFLPCB.CPY`

IMS PCB mask for the summary database. (26 lines, 10 non-comment lines, 10 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `PASFLPCB` | - | - | group item | Pasflpcb | - |
| 05 | `PASFL-DBDNAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Pasfl dbdname | - |
| 05 | `PASFL-SEG-LEVEL` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pasfl IMS segment level | - |
| 05 | `PASFL-PCB-STATUS` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pasfl IMS PCB status | - |
| 05 | `PASFL-PCB-PROCOPT` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pasfl IMS PCB procopt | - |
| 05 | `FILLER` | `S9(05)` | 5 | binary (COMP), signed | Reserved slack bytes (pads the record to its catalogued length) | - |
| 05 | `PASFL-SEG-NAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Pasfl IMS segment name | - |
| 05 | `PASFL-KEYFB-NAME` | `S9(05)` | 5 | binary (COMP), signed | Pasfl keyfb name | - |
| 05 | `PASFL-NUM-SENSEGS` | `S9(05)` | 5 | binary (COMP), signed | Pasfl number sensegs | - |
| 05 | `PASFL-KEYFB` | `X(100)` | 100 | alphanumeric (DISPLAY) | Pasfl keyfb | - |

### `PAUTBPCB` — `app/app-authorization-ims-db2-mq/cpy/PAUTBPCB.CPY`

IMS PCB mask used by the BMP load/unload programs. (26 lines, 10 non-comment lines, 10 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `PAUTBPCB` | - | - | group item | Pautbpcb | - |
| 05 | `PAUT-DBDNAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Pending authorization dbdname | - |
| 05 | `PAUT-SEG-LEVEL` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pending authorization IMS segment level | - |
| 05 | `PAUT-PCB-STATUS` | `X(02)` | 2 | alphanumeric (DISPLAY) | Pending authorization IMS PCB status | - |
| 05 | `PAUT-PCB-PROCOPT` | `X(04)` | 4 | alphanumeric (DISPLAY) | Pending authorization IMS PCB procopt | - |
| 05 | `FILLER` | `S9(05)` | 5 | binary (COMP), signed | Reserved slack bytes (pads the record to its catalogued length) | - |
| 05 | `PAUT-SEG-NAME` | `X(08)` | 8 | alphanumeric (DISPLAY) | Pending authorization IMS segment name | - |
| 05 | `PAUT-KEYFB-NAME` | `S9(05)` | 5 | binary (COMP), signed | Pending authorization keyfb name | - |
| 05 | `PAUT-NUM-SENSEGS` | `S9(05)` | 5 | binary (COMP), signed | Pending authorization number sensegs | - |
| 05 | `PAUT-KEYFB` | `X(255)` | 255 | alphanumeric (DISPLAY) | Pending authorization keyfb | - |

### `AUTHFRDS` — `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl`

DB2 `DCLGEN` for `CARDDEMO.AUTHFRDS`, the fraud/authorization table written by `COPAUS2C`. (89 lines, 60 non-comment lines, 29 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `DCLAUTHFRDS` | - | - | group item | Db2 host structure for table `CARDDEMO.AUTHFRDS` | - |
| 10 | `CARD-NUM` | `X(16)` | 16 | alphanumeric (DISPLAY) | Card number | - |
| 10 | `AUTH-TS` | `X(26)` | 26 | alphanumeric (DISPLAY) | Authorization timestamp | - |
| 10 | `AUTH-TYPE` | `X(4)` | 4 | alphanumeric (DISPLAY) | Authorization type | - |
| 10 | `CARD-EXPIRY-DATE` | `X(4)` | 4 | alphanumeric (DISPLAY) | Card expiry date | - |
| 10 | `MESSAGE-TYPE` | `X(6)` | 6 | alphanumeric (DISPLAY) | Message type | - |
| 10 | `MESSAGE-SOURCE` | `X(6)` | 6 | alphanumeric (DISPLAY) | Message source | - |
| 10 | `AUTH-ID-CODE` | `X(6)` | 6 | alphanumeric (DISPLAY) | Authorization identifier code | - |
| 10 | `AUTH-RESP-CODE` | `X(2)` | 2 | alphanumeric (DISPLAY) | Authorization CICS response code | - |
| 10 | `AUTH-RESP-REASON` | `X(4)` | 4 | alphanumeric (DISPLAY) | Authorization CICS response reason | - |
| 10 | `PROCESSING-CODE` | `X(6)` | 6 | alphanumeric (DISPLAY) | Processing code | - |
| 10 | `TRANSACTION-AMT` | `S9(10)V9(2)` | 12 | packed decimal (COMP-3), 1 dp | Transaction amount | - |
| 10 | `APPROVED-AMT` | `S9(10)V9(2)` | 12 | packed decimal (COMP-3), 1 dp | Approved amount | - |
| 10 | `MERCHANT-CATAGORY-CODE` | `X(4)` | 4 | alphanumeric (DISPLAY) | Merchant catagory code | - |
| 10 | `ACQR-COUNTRY-CODE` | `X(3)` | 3 | alphanumeric (DISPLAY) | Acqr country code | - |
| 10 | `POS-ENTRY-MODE` | `S9(4)` | 4 | binary (COMP), signed | Pos entry mode | - |
| 10 | `MERCHANT-ID` | `X(15)` | 15 | alphanumeric (DISPLAY) | Merchant identifier | - |
| 10 | `MERCHANT-NAME` | - | - | group item | Merchant name | - |
| 49 | `MERCHANT-NAME-LEN` | `S9(4)` | 4 | binary (COMP), signed | VARCHAR length half of the preceding group item | - |
| 49 | `MERCHANT-NAME-TEXT` | `X(22)` | 22 | alphanumeric (DISPLAY) | VARCHAR text half of the preceding group item | - |
| 10 | `MERCHANT-CITY` | `X(13)` | 13 | alphanumeric (DISPLAY) | Merchant city | - |
| 10 | `MERCHANT-STATE` | `X(2)` | 2 | alphanumeric (DISPLAY) | Merchant state | - |
| 10 | `MERCHANT-ZIP` | `X(9)` | 9 | alphanumeric (DISPLAY) | Merchant ZIP code | - |
| 10 | `TRANSACTION-ID` | `X(15)` | 15 | alphanumeric (DISPLAY) | Transaction identifier | - |
| 10 | `MATCH-STATUS` | `X(1)` | 1 | alphanumeric (DISPLAY) | Match status | - |
| 10 | `AUTH-FRAUD` | `X(1)` | 1 | alphanumeric (DISPLAY) | Authorization fraud | - |
| 10 | `FRAUD-RPT-DATE` | `X(10)` | 10 | alphanumeric (DISPLAY) | Fraud rpt date | - |
| 10 | `ACCT-ID` | `S9(11)V` | 11 | packed decimal (COMP-3), 0 dp | Account identifier | - |
| 10 | `CUST-ID` | `S9(9)V` | 9 | packed decimal (COMP-3), 0 dp | Customer identifier | - |

## 8. Transaction-type extension (DB2)

DCLGENs and the Db2 status handling copybooks under `app/app-transaction-type-db2/`.


### `DCLTRTYP` — `app/app-transaction-type-db2/dcl/DCLTRTYP.dcl`

DB2 `DCLGEN` for `CARDDEMO.TRANSACTION_TYPE`; the `49`-level pair is a VARCHAR length/text host structure. (49 lines, 11 non-comment lines, 5 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `DCLTRANSACTION-TYPE` | - | - | group item | Db2 host structure for table `CARDDEMO.TRANSACTION_TYPE` | - |
| 10 | `DCL-TR-TYPE` | `X(2)` | 2 | alphanumeric (DISPLAY) | Db2 column: transaction type | - |
| 10 | `DCL-TR-DESCRIPTION` | - | - | group item | Db2 column: transaction description | - |
| 49 | `DCL-TR-DESCRIPTION-LEN` | `S9(4)` | 4 | binary (COMP), signed | Db2 column: transaction description length | - |
| 49 | `DCL-TR-DESCRIPTION-TEXT` | `X(50)` | 50 | alphanumeric (DISPLAY) | Db2 column: transaction description text | - |

### `DCLTRCAT` — `app/app-transaction-type-db2/dcl/DCLTRCAT.dcl`

DB2 `DCLGEN` for `CARDDEMO.TRANSACTION_TYPE_CATEGORY`. (54 lines, 14 non-comment lines, 6 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 01 | `DCLTRANSACTION-TYPE-CATEGORY` | - | - | group item | Db2 host structure for table `CARDDEMO.TRANSACTION_TYPE_CATEGORY` | - |
| 10 | `DCL-TRC-TYPE-CODE` | `X(2)` | 2 | alphanumeric (DISPLAY) | Db2 column: transaction category type code | - |
| 10 | `DCL-TRC-TYPE-CATEGORY` | `X(4)` | 4 | alphanumeric (DISPLAY) | Db2 column: transaction category type category | - |
| 10 | `DCL-TRC-CAT-DATA` | - | - | group item | Db2 column: transaction category category data | - |
| 49 | `DCL-TRC-CAT-DATA-LEN` | `S9(4)` | 4 | binary (COMP), signed | Db2 column: transaction category category data length | - |
| 49 | `DCL-TRC-CAT-DATA-TEXT` | `X(50)` | 50 | alphanumeric (DISPLAY) | Db2 column: transaction category category data text | - |

### `CSDB2RWY` — `app/app-transaction-type-db2/cpy/CSDB2RWY.cpy`

Db2 status / message work area for the transaction-type extension. (46 lines, 22 non-comment lines, 16 data items)

| Level | Field | PIC | Size | Derived type | Inferred business meaning | Validation / notes |
|---|---|---|---|---|---|---|
| 05 | `WS-DB2-COMMON-VARS` | - | - | group item | Ws Db2 common vars | - |
| 10 | `WS-DISP-SQLCODE` | `----9` | 1 | edited numeric (display) | Ws disp sqlcode | - |
| 10 | `WS-DUMMY-DB2-INT` | `S9(4)` | 4 | packed decimal (COMP-3) | Ws dummy Db2 interest | VALUE 0 |
| 10 | `WS-DB2-PROCESSING-FLAG` | `X(1)` | 1 | alphanumeric (DISPLAY) | Ws Db2 processing flag | - |
| 88 | `WS-DB2-OK` | - | - | condition name | Condition name on `WS-DB2-PROCESSING-FLAG` | VALUE '0' |
| 88 | `WS-DB2-ERROR` | - | - | condition name | Condition name on `WS-DB2-PROCESSING-FLAG` | VALUE '1' |
| 10 | `WS-DB2-CURRENT-ACTION` | `X(72)` | 72 | alphanumeric (DISPLAY) | Ws Db2 current action | VALUE SPACES |
| 05 | `WS-DSNTIAC-FORMATTED` | - | - | group item | Ws dsntiac formatted | - |
| 10 | `WS-DSNTIAC-MESG-LEN` | `S9(4)` | 4 | binary (COMP), signed | Ws dsntiac mesg length | VALUE +720 |
| 10 | `WS-DSNTIAC-FMTD-TEXT` | - | - | group item | Ws dsntiac fmtd text | - |
| 15 | `WS-DSNTIAC-FMTD-TEXT-LINE` | `X(72)` | 72 | alphanumeric (DISPLAY) | Ws dsntiac fmtd text line | OCCURS 10; VALUE SPACES |
| 05 | `WS-DSNTIAC-LRECL` | `S9(4)` | 4 | binary (COMP), signed | Ws dsntiac lrecl | VALUE +72 |
| 05 | `WS-DSNTIAC-ERROR` | - | - | group item | Ws dsntiac error | - |
| 10 | `WS-DSNTIAC-ERR-MSG` | `X(10)` | 10 | alphanumeric (DISPLAY) | Ws dsntiac error message | VALUE 'DSNTIAC CD' |
| 10 | `WS-DSNTIAC-ERR-CD-X` | `X(02)` | 2 | alphanumeric (DISPLAY) | Ws dsntiac error code x | VALUE SPACES |
| 10 | `WS-DSNTIAC-ERR-CD` | `9(02)` | 2 | zoned decimal (DISPLAY), unsigned | Ws dsntiac error code | REDEFINES `WS-DSNTIAC-ERR-CD-X` |

### `CSDB2RPY` — `app/app-transaction-type-db2/cpy/CSDB2RPY.cpy`

Procedural copybook (no data items): `9998-PRIMING-QUERY` (`SELECT 1 FROM SYSIBM.SYSDUMMY1`) plus SQLCODE handling paragraphs. (89 lines, 51 non-comment lines, 0 data items)


## 9. Cross-record validation rules found in the programs

The copybooks themselves carry almost no `88`-level validation for the business records; the rules live in the programs that read them. The ones verified in source:

| Field | Rule | Source |
|---|---|---|
| `ACCT-ACTIVE-STATUS`, `CUST-PRI-CARD-HOLDER-IND` | must be `Y` or `N` (`88 FLG-YES-NO-ISVALID VALUES 'Y', 'N'`) | `app/cbl/COACTUPC.cbl` lines 76-80, 1473, 1659 |
| `CUST-FICO-CREDIT-SCORE` | `88 FICO-RANGE-IS-VALID VALUES 300 THROUGH 850` | `app/cbl/COACTUPC.cbl` lines 845-849 |
| `ACCT-OPEN-DATE`, `ACCT-EXPIRAION-DATE`, `ACCT-REISSUE-DATE`, `CUST-DOB-*` | century/month/day edits, leap-year check, and "not in the future" check for the date of birth | `app/cpy/CSUTLDPY.cpy` + `app/cpy/CSUTLDWY.cpy`, called from `COACTUPC` |
| `CUST-PHONE-NUM-1/2` area code | must appear in `88 VALID-PHONE-AREA-CODE` | `app/cpy/CSLKPCDY.cpy` |
| `CUST-ADDR-STATE-CD` | must appear in `88 VALID-US-STATE-CODE` | `app/cpy/CSLKPCDY.cpy` |
| `CUST-ADDR-STATE-CD` + `CUST-ADDR-ZIP` | first two ZIP digits must be a valid combination with the state (`88 VALID-US-STATE-ZIP-CD2-COMBO`) | `app/cpy/CSLKPCDY.cpy` |
| `TRAN-AMT` vs `ACCT-CREDIT-LIMIT` | a transaction that would push `ACCT-CURR-BAL` over the credit limit is rejected with reason code 102 | `app/cbl/CBTRN02C.cbl` |
| `DALYTRAN-CARD-NUM` | must exist in `CARDXREF`, else reason code 100 | `app/cbl/CBTRN02C.cbl` |
| `SEC-USR-TYPE` | `A` routes to `COADM01C`, anything else to `COMEN01C` | `app/cbl/COSGN00C.cbl` |
