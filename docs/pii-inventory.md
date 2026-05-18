# PII Inventory for CardDemo Modernization

> **Purpose**: Catalog all Personally Identifiable Information (PII) fields across CardDemo copybooks to support data privacy compliance (GDPR, PCI-DSS, CCPA) during the COBOL-to-Java modernization effort.
>
> **Last Updated**: 2026-05-18
>
> **Applicable Regulations**: GDPR (EU), PCI-DSS v4.0, CCPA/CPRA (California), SOX (financial controls)

---

## Table of Contents

1. [Customer PII (High Sensitivity)](#1-customer-pii-high-sensitivity)
2. [Payment Card Industry (PCI-DSS) Data (Critical Sensitivity)](#2-payment-card-industry-pci-dss-data-critical-sensitivity)
3. [Account Identifiers (Medium Sensitivity)](#3-account-identifiers-medium-sensitivity)
4. [Authentication / Credential Data (Critical Sensitivity)](#4-authentication--credential-data-critical-sensitivity)
5. [Data Export / Migration (Aggregated PII Risk)](#5-data-export--migration-aggregated-pii-risk)
6. [Merchant Data (Low-Medium Sensitivity)](#6-merchant-data-low-medium-sensitivity)
7. [Critical Compliance Violations](#7-critical-compliance-violations)
8. [VSAM Dataset / DB2 Table Mapping](#8-vsam-dataset--db2-table-mapping)
9. [Modernization Recommendations Summary](#9-modernization-recommendations-summary)

---

## 1. Customer PII (High Sensitivity)

Customer identity data is stored in two identical copybooks that define the 500-byte `CUSTOMER-RECORD` layout.

### Source Copybooks

| Copybook | Path | Record | Length |
|---|---|---|---|
| `CVCUS01Y.cpy` | `app/cpy/CVCUS01Y.cpy` | `CUSTOMER-RECORD` | 500 bytes |
| `CUSTREC.cpy` | `app/cpy/CUSTREC.cpy` | `CUSTOMER-RECORD` (duplicate) | 500 bytes |

> **Note**: `CUSTREC.cpy` is a duplicate of `CVCUS01Y.cpy` with minor formatting differences (e.g., `CUST-DOB-YYYYMMDD` vs `CUST-DOB-YYYY-MM-DD`). Both should be consolidated during modernization.

### PII Fields

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `CUST-SSN` | `PIC 9(09)` | Personal Identity (SSN) | GDPR Art. 87, CCPA | **Tokenization** -- replace with opaque token; store original in isolated vault with strict access controls |
| `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Personal Identity (Government ID) | GDPR Art. 87, CCPA | **Tokenization** -- same vault strategy as SSN |
| `CUST-FIRST-NAME` | `PIC X(25)` | Personal Identity (Name) | GDPR, CCPA | **Encryption at rest** (AES-256); mask in logs and non-production environments |
| `CUST-MIDDLE-NAME` | `PIC X(25)` | Personal Identity (Name) | GDPR, CCPA | **Encryption at rest**; mask in logs |
| `CUST-LAST-NAME` | `PIC X(25)` | Personal Identity (Name) | GDPR, CCPA | **Encryption at rest**; mask in logs |
| `CUST-ADDR-LINE-1` | `PIC X(50)` | Personal Identity (Address) | GDPR, CCPA | **Encryption at rest**; mask in logs |
| `CUST-ADDR-LINE-2` | `PIC X(50)` | Personal Identity (Address) | GDPR, CCPA | **Encryption at rest**; mask in logs |
| `CUST-ADDR-LINE-3` | `PIC X(50)` | Personal Identity (Address) | GDPR, CCPA | **Encryption at rest**; mask in logs |
| `CUST-ADDR-STATE-CD` | `PIC X(02)` | Personal Identity (Address) | GDPR, CCPA | **Encryption at rest** |
| `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Personal Identity (Address) | GDPR, CCPA | **Encryption at rest** |
| `CUST-ADDR-ZIP` | `PIC X(10)` | Personal Identity (Address) | GDPR, CCPA | **Encryption at rest** |
| `CUST-PHONE-NUM-1` | `PIC X(15)` | Personal Identity (Phone) | GDPR, CCPA | **Encryption at rest**; mask in logs (show last 4 digits only) |
| `CUST-PHONE-NUM-2` | `PIC X(15)` | Personal Identity (Phone) | GDPR, CCPA | **Encryption at rest**; mask in logs (show last 4 digits only) |
| `CUST-DOB-YYYY-MM-DD` | `PIC X(10)` | Personal Identity (DOB) | GDPR, CCPA | **Encryption at rest**; mask in non-production environments |
| `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Financial (Bank Account Ref) | GDPR, PCI-DSS, CCPA | **Tokenization** -- external bank account reference must not be stored in cleartext |

> **Variant**: In `CUSTREC.cpy`, the date-of-birth field is named `CUST-DOB-YYYYMMDD` (no hyphens). Both map to the same data.

### VSAM Dataset

`AWS.M2.CARDDEMO.CUSTDATA.PS` -- Customer Data (FB, 500 bytes, copybook `CVCUS01Y`)

### Program References

| Program | Copybook Used | Access Pattern |
|---|---|---|
| `CBCUS01C.cbl` | `CVCUS01Y` | **READ/WRITE** -- Batch customer data maintenance |
| `CBSTM03A.CBL` | `CUSTREC` | **READ** -- Statement generation; reads customer name/address for statements |
| `CBTRN01C.cbl` | `CVCUS01Y` | **READ** -- Transaction posting; reads customer for validation |
| `CBEXPORT.cbl` | `CVCUS01Y` | **READ** -- Branch migration export; reads customer records for export |
| `CBIMPORT.cbl` | `CVCUS01Y` | **WRITE** -- Branch migration import; writes customer records from import file |
| `COCRDSLC.cbl` | `CVCUS01Y` | **READ** -- Online credit card search; reads customer data for display |
| `COCRDUPC.cbl` | `CVCUS01Y` | **READ/WRITE** -- Online credit card update; reads and updates customer data |
| `COACTUPC.cbl` | `CVCUS01Y` | **READ** -- Online account update; reads customer data for context |
| `COACTVWC.cbl` | `CVCUS01Y` | **READ** -- Online account view; displays customer information |
| `COPAUA0C.cbl` (auth) | `CVCUS01Y` | **READ** -- Authorization; reads customer data for card authorization |
| `COPAUS0C.cbl` (auth) | `CVCUS01Y` | **READ** -- Authorization search; reads customer info |

---

## 2. Payment Card Industry (PCI-DSS) Data (Critical Sensitivity)

Card data appears across multiple copybooks covering the card entity, cross-references, transactions, statement views, and authorization processing.

### 2.1 Card Entity -- `CVACT02Y.cpy`

**Path**: `app/cpy/CVACT02Y.cpy` | **Record**: `CARD-RECORD` | **Length**: 150 bytes

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** -- replace PAN with token; store in PCI-compliant token vault. Display only last 4 digits |
| `CARD-CVV-CD` | `PIC 9(03)` | PCI-DSS Sensitive Auth Data (CVV) | PCI-DSS Req 3.2 | **:rotating_light: CRITICAL VIOLATION -- MUST BE ELIMINATED.** CVV must NEVER be stored post-authorization per PCI-DSS Req 3.2. See [Critical Compliance Violations](#7-critical-compliance-violations) |
| `CARD-EMBOSSED-NAME` | `PIC X(50)` | PCI-DSS Cardholder Data (Name) | PCI-DSS Req 3 | **Encryption at rest** (AES-256); mask in logs |
| `CARD-EXPIRAION-DATE` | `PIC X(10)` | PCI-DSS Cardholder Data (Expiry) | PCI-DSS Req 3 | **Encryption at rest**; protect alongside PAN |

**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDDATA.PS` -- Card Data (FB, 150 bytes, copybook `CVACT02Y`)

**Program References**:

| Program | Access Pattern |
|---|---|
| `CBACT02C.cbl` | **READ/WRITE** -- Batch card data file maintenance |
| `CBEXPORT.cbl` | **READ** -- Export card records for branch migration |
| `CBIMPORT.cbl` | **WRITE** -- Import card records from migration file |
| `CBTRN01C.cbl` | **READ** -- Transaction posting; validates card |
| `COCRDSLC.cbl` | **READ** -- Online card search/display |
| `COCRDLIC.cbl` | **READ** -- Online card list display |
| `COCRDUPC.cbl` | **READ/WRITE** -- Online card update |
| `COACTVWC.cbl` | **READ** -- Online account view; shows card info |
| `COPAUS0C.cbl` (auth) | **READ** -- Authorization; reads card for validation |
| `COTRTLIC.cbl` (tran-type) | **READ** -- Transaction type list; reads card info |

### 2.2 Card Cross-Reference -- `CVACT03Y.cpy`

**Path**: `app/cpy/CVACT03Y.cpy` | **Record**: `CARD-XREF-RECORD` | **Length**: 50 bytes

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `XREF-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** -- same token as `CARD-NUM`; maintain referential integrity via token |

**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDXREF.PS` -- Cross Reference (FB, 50 bytes, copybook `CVACT03Y`)

**Program References**:

| Program | Access Pattern |
|---|---|
| `CBACT03C.cbl` | **READ/WRITE** -- Batch cross-reference maintenance |
| `CBACT04C.cbl` | **READ** -- Batch account/card processing |
| `CBSTM03A.CBL` | **READ** -- Statement generation; resolves card-to-customer links |
| `CBTRN01C.cbl` | **READ** -- Transaction posting; resolves card cross-references |
| `CBTRN03C.cbl` | **READ** -- Transaction purge; resolves cross-references |
| `CBEXPORT.cbl` | **READ** -- Export cross-reference records |
| `CBIMPORT.cbl` | **WRITE** -- Import cross-reference records |
| `COBIL00C.cbl` | **READ** -- Online billing; resolves card references |
| `COACTUPC.cbl` | **READ** -- Online account update; resolves card references |
| `COACTVWC.cbl` | **READ** -- Online account view |
| `COTRN02C.cbl` | **READ** -- Online transaction add/update |
| `CBTRN02C.cbl` | **READ** -- Batch transaction processing |
| `COPAUA0C.cbl` (auth) | **READ** -- Authorization; resolves card cross-ref |
| `COPAUS0C.cbl` (auth) | **READ** -- Authorization search |

### 2.3 Transaction Records -- `CVTRA05Y.cpy` and `CVTRA06Y.cpy`

| Copybook | Path | Record | Length | Field |
|---|---|---|---|---|
| `CVTRA05Y.cpy` | `app/cpy/CVTRA05Y.cpy` | `TRAN-RECORD` | 350 bytes | `TRAN-CARD-NUM` PIC X(16) |
| `CVTRA06Y.cpy` | `app/cpy/CVTRA06Y.cpy` | `DALYTRAN-RECORD` | 350 bytes | `DALYTRAN-CARD-NUM` PIC X(16) |

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `TRAN-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** -- use same token as card entity |
| `DALYTRAN-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** -- use same token as card entity |

**VSAM Datasets**:
- `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` -- Online transaction data (FB, 350 bytes, copybook `CVTRA05Y`)
- `AWS.M2.CARDDEMO.DALYTRAN.PS` -- Daily transaction data for posting (FB, 350 bytes, copybook `CVTRA06Y`)
- `AWS.M2.CARDDEMO.DALYTRAN.PS.INIT` -- Transaction DB initialization (FB, 350 bytes, copybook `CVTRA06Y`)

**Program References for `CVTRA05Y` (TRAN-CARD-NUM)**:

| Program | Access Pattern |
|---|---|
| `CBACT04C.cbl` | **READ** -- Batch account/card processing |
| `CBTRN01C.cbl` | **READ/WRITE** -- Transaction posting |
| `CBTRN02C.cbl` | **READ/WRITE** -- Batch transaction processing |
| `CBTRN03C.cbl` | **READ** -- Transaction purge |
| `CBEXPORT.cbl` | **READ** -- Export transaction records |
| `CBIMPORT.cbl` | **WRITE** -- Import transaction records |
| `COBIL00C.cbl` | **READ** -- Online billing |
| `CORPT00C.cbl` | **READ** -- Online reporting |
| `COTRN00C.cbl` | **READ** -- Online transaction list |
| `COTRN01C.cbl` | **READ/WRITE** -- Online transaction view/add |
| `COTRN02C.cbl` | **READ/WRITE** -- Online transaction update |

**Program References for `CVTRA06Y` (DALYTRAN-CARD-NUM)**:

| Program | Access Pattern |
|---|---|
| `CBTRN01C.cbl` | **READ/WRITE** -- Transaction posting; reads daily transactions |
| `CBTRN02C.cbl` | **READ/WRITE** -- Batch transaction processing |

### 2.4 Statement View -- `COSTM01.CPY`

**Path**: `app/cpy/COSTM01.CPY` | **Record**: `TRNX-RECORD` | **Length**: variable (statement layout)

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `TRNX-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization**; mask on printed statements (show last 4 only) |

**Program References**:

| Program | Access Pattern |
|---|---|
| `CBSTM03A.CBL` | **READ** -- Statement generation; reads transaction data keyed by card number |

### 2.5 Authorization Module -- IMS/DB2/MQ

#### `CIPAUDTY.cpy` -- Pending Authorization Details

**Path**: `app/app-authorization-ims-db2-mq/cpy/CIPAUDTY.cpy` | **Record**: IMS Segment

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `PA-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** |
| `PA-CARD-EXPIRY-DATE` | `PIC X(04)` | PCI-DSS Cardholder Data (Expiry) | PCI-DSS Req 3 | **Encryption at rest** |

**Program References**:

| Program | Access Pattern |
|---|---|
| `COPAUS2C.cbl` | **READ** -- Authorization inquiry |
| `COPAUS1C.cbl` | **READ/WRITE** -- Authorization processing |
| `COPAUA0C.cbl` | **READ/WRITE** -- Authorization approval |
| `COPAUS0C.cbl` | **READ** -- Authorization search |
| `CBPAUP0C.cbl` | **READ/WRITE** -- Batch pending auth processing |
| `DBUNLDGS.CBL` | **READ** -- IMS database unload |
| `PAUDBUNL.CBL` | **READ** -- Pending auth DB unload |
| `PAUDBLOD.CBL` | **WRITE** -- Pending auth DB load |

#### `CCPAURQY.cpy` -- Authorization Request

**Path**: `app/app-authorization-ims-db2-mq/cpy/CCPAURQY.cpy`

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `PA-RQ-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** in transit; never log |
| `PA-RQ-CARD-EXPIRY-DATE` | `PIC X(04)` | PCI-DSS Cardholder Data (Expiry) | PCI-DSS Req 3 | **Encryption**; never log |

**Program References**:

| Program | Access Pattern |
|---|---|
| `COPAUA0C.cbl` | **READ** -- Authorization processing; reads incoming request |

#### `CCPAURLY.cpy` -- Authorization Response

**Path**: `app/app-authorization-ims-db2-mq/cpy/CCPAURLY.cpy`

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `PA-RL-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** in response messages; mask in logs |

**Program References**:

| Program | Access Pattern |
|---|---|
| `COPAUA0C.cbl` | **WRITE** -- Authorization processing; writes response |

---

## 3. Account Identifiers (Medium Sensitivity)

### 3.1 Account Entity -- `CVACT01Y.cpy`

**Path**: `app/cpy/CVACT01Y.cpy` | **Record**: `ACCOUNT-RECORD` | **Length**: 300 bytes

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `ACCT-ID` | `PIC 9(11)` | Financial (Account ID) | GDPR, CCPA | **Encryption at rest**; mask in logs (show last 4 digits) |
| `ACCT-ADDR-ZIP` | `PIC X(10)` | Personal Identity (Address component) | GDPR, CCPA | **Encryption at rest** |

**VSAM Dataset**: `AWS.M2.CARDDEMO.ACCTDATA.PS` -- Account Data (FB, 300 bytes, copybook `CVACT01Y`)

**Program References**:

| Program | Access Pattern |
|---|---|
| `CBACT01C.cbl` | **READ/WRITE** -- Batch account file maintenance |
| `CBACT04C.cbl` | **READ** -- Batch account processing |
| `CBSTM03A.CBL` | **READ** -- Statement generation |
| `CBTRN01C.cbl` | **READ/WRITE** -- Transaction posting; updates account balances |
| `CBTRN02C.cbl` | **READ** -- Batch transaction processing |
| `CBEXPORT.cbl` | **READ** -- Export account records |
| `CBIMPORT.cbl` | **WRITE** -- Import account records |
| `COBIL00C.cbl` | **READ** -- Online billing view |
| `COACTUPC.cbl` | **READ/WRITE** -- Online account update |
| `COACTVWC.cbl` | **READ** -- Online account view |
| `COTRN02C.cbl` | **READ** -- Online transaction processing |
| `COACCT01.cbl` (vsam-mq) | **READ/WRITE** -- VSAM/MQ account processing |
| `COPAUA0C.cbl` (auth) | **READ** -- Authorization; reads account data |
| `COPAUS0C.cbl` (auth) | **READ** -- Authorization search |

### 3.2 COMMAREA -- `COCOM01Y.cpy` (In-Flight Session State)

**Path**: `app/cpy/COCOM01Y.cpy` | **Record**: `CARDDEMO-COMMAREA`

The COMMAREA carries PII identifiers in memory between CICS program invocations during a user's online session.

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `CDEMO-USER-ID` | `PIC X(08)` | Authentication (User ID) | GDPR, CCPA | **Encrypt in transit** between services; do not log in plaintext |
| `CDEMO-ACCT-ID` | `PIC 9(11)` | Financial (Account ID) | GDPR, CCPA | **Encrypt in transit**; mask in logs |
| `CDEMO-CARD-NUM` | `PIC 9(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization** in modernized session state |
| `CDEMO-CUST-ID` | `PIC 9(09)` | Personal Identity (Customer ID) | GDPR, CCPA | **Encrypt in transit**; mask in logs |
| `CDEMO-CUST-FNAME` | `PIC X(25)` | Personal Identity (Name) | GDPR, CCPA | **Encrypt in transit**; mask in logs |
| `CDEMO-CUST-MNAME` | `PIC X(25)` | Personal Identity (Name) | GDPR, CCPA | **Encrypt in transit**; mask in logs |
| `CDEMO-CUST-LNAME` | `PIC X(25)` | Personal Identity (Name) | GDPR, CCPA | **Encrypt in transit**; mask in logs |

**Program References** (used by virtually all online CICS programs):

| Program | Access Pattern |
|---|---|
| `COSGN00C.cbl` | **WRITE** -- Sign-on; populates COMMAREA with user context |
| `COMEN01C.cbl` | **READ/WRITE** -- Main menu; reads/routes COMMAREA |
| `COADM01C.cbl` | **READ/WRITE** -- Admin menu |
| `COBIL00C.cbl` | **READ** -- Billing view |
| `COCRDSLC.cbl` | **READ/WRITE** -- Card search |
| `COCRDLIC.cbl` | **READ/WRITE** -- Card list |
| `COCRDUPC.cbl` | **READ/WRITE** -- Card update |
| `COACTUPC.cbl` | **READ/WRITE** -- Account update |
| `COACTVWC.cbl` | **READ/WRITE** -- Account view |
| `CORPT00C.cbl` | **READ** -- Reporting |
| `COTRN00C.cbl` | **READ** -- Transaction list |
| `COTRN01C.cbl` | **READ/WRITE** -- Transaction view |
| `COTRN02C.cbl` | **READ/WRITE** -- Transaction update |
| `COUSR00C.cbl` | **READ/WRITE** -- User list |
| `COUSR01C.cbl` | **READ/WRITE** -- User add |
| `COUSR02C.cbl` | **READ/WRITE** -- User update |
| `COUSR03C.cbl` | **READ/WRITE** -- User delete |
| `COPAUS1C.cbl` (auth) | **READ** -- Authorization processing |
| `COPAUS0C.cbl` (auth) | **READ** -- Authorization search |
| `COTRTUPC.cbl` (tran-type) | **READ/WRITE** -- Transaction type update |
| `COTRTLIC.cbl` (tran-type) | **READ/WRITE** -- Transaction type list |

### 3.3 Working Storage -- `CVCRD01Y.cpy`

**Path**: `app/cpy/CVCRD01Y.cpy` | **Record**: `CC-WORK-AREAS`

Working storage fields used as intermediary variables in online card programs.

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `CC-ACCT-ID` | `PIC X(11)` | Financial (Account ID) | GDPR, CCPA | **Encrypt in memory** in modernized Java code; clear after use |
| `CC-CARD-NUM` | `PIC X(16)` | PCI-DSS Cardholder Data (PAN) | PCI-DSS Req 3 | **Tokenization**; never hold PAN in plaintext in memory longer than necessary |
| `CC-CUST-ID` | `PIC X(09)` | Personal Identity (Customer ID) | GDPR, CCPA | **Encrypt in memory**; clear after use |

**Program References**:

| Program | Access Pattern |
|---|---|
| `COCRDSLC.cbl` | **READ/WRITE** -- Card search working storage |
| `COCRDLIC.cbl` | **READ/WRITE** -- Card list working storage |
| `COCRDUPC.cbl` | **READ/WRITE** -- Card update working storage |
| `COACTUPC.cbl` | **READ/WRITE** -- Account update working storage |
| `COACTVWC.cbl` | **READ/WRITE** -- Account view working storage |
| `COTRTUPC.cbl` (tran-type) | **READ/WRITE** -- Transaction type update |
| `COTRTLIC.cbl` (tran-type) | **READ/WRITE** -- Transaction type list |

---

## 4. Authentication / Credential Data (Critical Sensitivity)

### Source Copybook

**Path**: `app/cpy/CSUSR01Y.cpy` | **Record**: `SEC-USER-DATA` | **Length**: 80 bytes

| Field | PIC Clause | PII Classification | Regulation | Recommended Handling |
|---|---|---|---|---|
| `SEC-USR-PWD` | `PIC X(08)` | Credential (Password) | PCI-DSS Req 8, GDPR, NIST 800-63 | **:rotating_light: CRITICAL VIOLATION -- CLEARTEXT PASSWORD.** Must be replaced with salted bcrypt/scrypt/Argon2 hash. See [Critical Compliance Violations](#7-critical-compliance-violations) |
| `SEC-USR-ID` | `PIC X(08)` | Authentication (User ID) | GDPR, CCPA | **Do not store in logs**; use as non-sensitive identifier with proper access controls |
| `SEC-USR-FNAME` | `PIC X(20)` | Personal Identity (Name) | GDPR, CCPA | **Encryption at rest**; mask in logs |
| `SEC-USR-LNAME` | `PIC X(20)` | Personal Identity (Name) | GDPR, CCPA | **Encryption at rest**; mask in logs |

**VSAM Dataset**: `AWS.M2.CARDDEMO.USRSEC.PS` -- User Security file (FB, 80 bytes, copybook `CSUSR01Y`)

**Program References**:

| Program | Access Pattern |
|---|---|
| `COSGN00C.cbl` | **READ** -- Sign-on; reads user credentials for authentication (compares cleartext password) |
| `COMEN01C.cbl` | **READ** -- Main menu; reads user record for session context |
| `COADM01C.cbl` | **READ** -- Admin functions; reads user data |
| `COUSR01C.cbl` | **WRITE** -- User add; writes new user with cleartext password |
| `COUSR02C.cbl` | **READ/WRITE** -- User update; reads and updates user including password |
| `COUSR03C.cbl` | **READ/WRITE** -- User delete; reads user for confirmation, then deletes |
| `COUSR00C.cbl` | **READ** -- User list; reads user records for display |
| `COCRDSLC.cbl` | **READ** -- Card search; reads user security context |
| `COCRDLIC.cbl` | **READ** -- Card list; reads user security context |
| `COCRDUPC.cbl` | **READ** -- Card update; reads user security context |
| `COACTUPC.cbl` | **READ** -- Account update; reads user security context |
| `COACTVWC.cbl` | **READ** -- Account view; reads user security context |
| `COTRTUPC.cbl` (tran-type) | **READ** -- Transaction type update; reads user security |
| `COTRTLIC.cbl` (tran-type) | **READ** -- Transaction type list; reads user security |

---

## 5. Data Export / Migration (Aggregated PII Risk)

### Source Copybook

**Path**: `app/cpy/CVEXPORT.cpy` | **Record**: `EXPORT-RECORD` | **Length**: 500 bytes

> **HIGHEST-RISK COPYBOOK**: This is the highest-risk copybook from a data-in-motion perspective. It consolidates PII from multiple entities (Customer, Account, Card, Transaction, Cross-Reference) into a single 500-byte polymorphic export record used for branch migration via `CBEXPORT.cbl` / `CBIMPORT.cbl`.

### Aggregated PII Fields by Record Type

#### Customer Export (`EXPORT-CUSTOMER-DATA`)

| Field | PII Classification |
|---|---|
| `EXP-CUST-SSN` (PIC 9(09)) | Personal Identity (SSN) |
| `EXP-CUST-GOVT-ISSUED-ID` (PIC X(20)) | Personal Identity (Government ID) |
| `EXP-CUST-FIRST-NAME`, `EXP-CUST-MIDDLE-NAME`, `EXP-CUST-LAST-NAME` | Personal Identity (Name) |
| `EXP-CUST-ADDR-LINE` (OCCURS 3), state, country, zip | Personal Identity (Address) |
| `EXP-CUST-PHONE-NUM` (OCCURS 2) | Personal Identity (Phone) |
| `EXP-CUST-DOB-YYYY-MM-DD` (PIC X(10)) | Personal Identity (DOB) |
| `EXP-CUST-EFT-ACCOUNT-ID` (PIC X(10)) | Financial (Bank Account Ref) |

#### Account Export (`EXPORT-ACCOUNT-DATA`)

| Field | PII Classification |
|---|---|
| `EXP-ACCT-ID` (PIC 9(11)) | Financial (Account ID) |
| `EXP-ACCT-ADDR-ZIP` (PIC X(10)) | Personal Identity (Address) |

#### Card Export (`EXPORT-CARD-DATA`)

| Field | PII Classification |
|---|---|
| `EXP-CARD-NUM` (PIC X(16)) | PCI-DSS (PAN) |
| `EXP-CARD-CVV-CD` (PIC 9(03) COMP) | PCI-DSS Sensitive Auth Data -- **MUST NOT be exported** |
| `EXP-CARD-EMBOSSED-NAME` (PIC X(50)) | PCI-DSS (Cardholder Name) |
| `EXP-CARD-EXPIRAION-DATE` (PIC X(10)) | PCI-DSS (Expiry) |

#### Transaction Export (`EXPORT-TRANSACTION-DATA`)

| Field | PII Classification |
|---|---|
| `EXP-TRAN-CARD-NUM` (PIC X(16)) | PCI-DSS (PAN) |
| `EXP-TRAN-MERCHANT-ID`, name, city, zip | Merchant Data |

#### Cross-Reference Export (`EXPORT-CARD-XREF-DATA`)

| Field | PII Classification |
|---|---|
| `EXP-XREF-CARD-NUM` (PIC X(16)) | PCI-DSS (PAN) |

### Recommended Handling for Export

- **Encrypt the entire export file** at rest and in transit (AES-256 + TLS 1.3)
- **Tokenize all PAN fields** before writing to the export stream
- **Remove CVV** from the card export structure entirely -- it must not appear in any export
- **Implement field-level encryption** for SSN, government ID, and bank account references
- **Add audit logging** for every export/import operation
- **Restrict access** to export files with role-based controls; apply retention policies

**Program References**:

| Program | Access Pattern |
|---|---|
| `CBEXPORT.cbl` | **WRITE** -- Reads from source VSAM files and writes consolidated export records |
| `CBIMPORT.cbl` | **READ** -- Reads export file and writes individual records to target VSAM files |

---

## 6. Merchant Data (Low-Medium Sensitivity)

Merchant data is not PII in the traditional sense, but merchant identification and location data can be commercially sensitive and subject to contractual obligations.

### 6.1 Transaction Records -- `CVTRA05Y.cpy` / `CVTRA06Y.cpy`

| Field | PIC Clause | Copybook | Classification | Recommended Handling |
|---|---|---|---|---|
| `TRAN-MERCHANT-ID` | `PIC 9(09)` | `CVTRA05Y` | Commercial (Merchant ID) | Encryption at rest; access controls |
| `TRAN-MERCHANT-NAME` | `PIC X(50)` | `CVTRA05Y` | Commercial (Merchant Name) | Encryption at rest |
| `TRAN-MERCHANT-CITY` | `PIC X(50)` | `CVTRA05Y` | Commercial (Merchant Location) | Encryption at rest |
| `TRAN-MERCHANT-ZIP` | `PIC X(10)` | `CVTRA05Y` | Commercial (Merchant Location) | Encryption at rest |
| `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | `CVTRA06Y` | Commercial (Merchant ID) | Encryption at rest |
| `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | `CVTRA06Y` | Commercial (Merchant Name) | Encryption at rest |
| `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | `CVTRA06Y` | Commercial (Merchant Location) | Encryption at rest |
| `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | `CVTRA06Y` | Commercial (Merchant Location) | Encryption at rest |

### 6.2 Authorization Details -- `CIPAUDTY.cpy`

| Field | PIC Clause | Classification | Recommended Handling |
|---|---|---|---|
| `PA-MERCHANT-ID` | `PIC X(15)` | Commercial (Merchant ID) | Encryption at rest; access controls |
| `PA-MERCHANT-NAME` | `PIC X(22)` | Commercial (Merchant Name) | Encryption at rest |
| `PA-MERCHANT-CITY` | `PIC X(13)` | Commercial (Merchant Location) | Encryption at rest |
| `PA-MERCHANT-STATE` | `PIC X(02)` | Commercial (Merchant Location) | Encryption at rest |
| `PA-MERCHANT-ZIP` | `PIC X(09)` | Commercial (Merchant Location) | Encryption at rest |

### 6.3 Authorization Request -- `CCPAURQY.cpy`

| Field | PIC Clause | Classification | Recommended Handling |
|---|---|---|---|
| `PA-RQ-MERCHANT-ID` | `PIC X(15)` | Commercial (Merchant ID) | Encryption at rest |
| `PA-RQ-MERCHANT-NAME` | `PIC X(22)` | Commercial (Merchant Name) | Encryption at rest |
| `PA-RQ-MERCHANT-CITY` | `PIC X(13)` | Commercial (Merchant Location) | Encryption at rest |
| `PA-RQ-MERCHANT-STATE` | `PIC X(02)` | Commercial (Merchant Location) | Encryption at rest |
| `PA-RQ-MERCHANT-ZIP` | `PIC X(09)` | Commercial (Merchant Location) | Encryption at rest |

---

## 7. Critical Compliance Violations

The following items represent **immediate compliance risks** that must be remediated during modernization, regardless of functional parity goals.

### VIOLATION 1: CVV Storage (PCI-DSS Req 3.2 -- CRITICAL)

- **Location**: `app/cpy/CVACT02Y.cpy`, field `CARD-CVV-CD` PIC 9(03)
- **Also in export**: `app/cpy/CVEXPORT.cpy`, field `EXP-CARD-CVV-CD` PIC 9(03) COMP
- **Dataset**: `AWS.M2.CARDDEMO.CARDDATA.PS`
- **Issue**: The Card Verification Value (CVV/CVC2/CVV2) is stored as a persistent field in the card entity record and is included in the export structure.
- **PCI-DSS Requirement 3.2**: *"Do not store sensitive authentication data after authorization (even if encrypted). Sensitive authentication data includes [...] Card verification codes or values (three-digit or four-digit number [...])."*
- **Impact**: Storing CVV is a **direct PCI-DSS violation** that could result in:
  - Failure of PCI-DSS compliance audit
  - Significant fines from card brands (Visa, Mastercard)
  - Loss of ability to process card transactions
- **Required Remediation**:
  1. **Remove** `CARD-CVV-CD` from the card entity entirely in the modernized Java data model
  2. **Remove** `EXP-CARD-CVV-CD` from the export structure
  3. CVV should only exist transiently during authorization request processing and must never be persisted to any datastore
  4. Ensure no database column, file field, or log entry captures CVV post-authorization
  5. Add automated scanning to CI/CD pipeline to detect any CVV storage patterns

### VIOLATION 2: Cleartext Password Storage (PCI-DSS Req 8.3.2, NIST 800-63B -- CRITICAL)

- **Location**: `app/cpy/CSUSR01Y.cpy`, field `SEC-USR-PWD` PIC X(08)
- **Dataset**: `AWS.M2.CARDDEMO.USRSEC.PS`
- **Programs Affected**: `COSGN00C.cbl` (sign-on compares password in cleartext), `COUSR01C.cbl` (stores new password in cleartext), `COUSR02C.cbl` (updates password in cleartext)
- **Issue**: User passwords are stored as 8-byte fixed-length cleartext strings in the VSAM security file. The sign-on program performs a direct string comparison for authentication.
- **PCI-DSS Requirement 8.3.2**: *"Passwords/passphrases are stored using strong cryptography."*
- **NIST 800-63B Section 5.1.1.2**: Passwords must be stored using a memory-hard hash function (e.g., Argon2id, bcrypt, scrypt).
- **Impact**: Cleartext password storage means:
  - Any compromise of the USRSEC dataset exposes all user credentials
  - No protection against insider threats
  - Direct violation of PCI-DSS, GDPR, and NIST guidelines
- **Required Remediation**:
  1. **Replace** the `SEC-USR-PWD` field with a salted hash field (e.g., VARCHAR(255) for bcrypt output)
  2. **Implement** proper password hashing using bcrypt (cost factor >= 12), scrypt, or Argon2id
  3. **Enforce** password complexity requirements (min 12 chars, no max limit, check against breach databases)
  4. **Remove** the 8-character length restriction
  5. **Force** password reset for all users upon migration
  6. Consider implementing multi-factor authentication (MFA)
  7. Add password change audit trail

---

## 8. VSAM Dataset / DB2 Table Mapping

Cross-reference between PII-containing copybooks and their physical storage.

| Dataset Name | Description | Copybook | PII Sensitivity | Key PII Fields |
|---|---|---|---|---|
| `AWS.M2.CARDDEMO.USRSEC.PS` | User Security | `CSUSR01Y` | **CRITICAL** | Cleartext password, user ID, names |
| `AWS.M2.CARDDEMO.CARDDATA.PS` | Card Data | `CVACT02Y` | **CRITICAL** | PAN, CVV (violation), cardholder name, expiry |
| `AWS.M2.CARDDEMO.CUSTDATA.PS` | Customer Data | `CVCUS01Y` | **HIGH** | SSN, government ID, full name, address, phone, DOB, bank account ref |
| `AWS.M2.CARDDEMO.ACCTDATA.PS` | Account Data | `CVACT01Y` | **MEDIUM** | Account ID, ZIP code |
| `AWS.M2.CARDDEMO.CARDXREF.PS` | Card Cross-Reference | `CVACT03Y` | **CRITICAL** | PAN (cross-reference) |
| `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | Online Transactions | `CVTRA05Y` | **CRITICAL** | PAN, merchant data |
| `AWS.M2.CARDDEMO.DALYTRAN.PS` | Daily Transactions | `CVTRA06Y` | **CRITICAL** | PAN, merchant data |
| `AWS.M2.CARDDEMO.DALYTRAN.PS.INIT` | Transaction Init | `CVTRA06Y` | **CRITICAL** | PAN, merchant data |

> **Note**: The IMS database segments used by the authorization module (`CIPAUDTY`) are not listed in the dataset catalog above, as they reside in IMS DB rather than VSAM. During modernization, these will likely migrate to DB2/PostgreSQL tables and must receive the same PCI-DSS protections.

---

## 9. Modernization Recommendations Summary

### By Data Classification

| Classification | Fields | Recommended Approach |
|---|---|---|
| **PAN (Card Numbers)** | `CARD-NUM`, `XREF-CARD-NUM`, `TRAN-CARD-NUM`, `DALYTRAN-CARD-NUM`, `TRNX-CARD-NUM`, `PA-CARD-NUM`, `PA-RQ-CARD-NUM`, `PA-RL-CARD-NUM`, `CDEMO-CARD-NUM`, `CC-CARD-NUM`, all `EXP-*-CARD-NUM` | **Tokenization** using a PCI-compliant token vault (e.g., AWS Payment Cryptography, Basis Theory, VGS). Display only last 4 digits. Never log full PAN. |
| **CVV** | `CARD-CVV-CD`, `EXP-CARD-CVV-CD` | **Eliminate entirely**. Must not exist in any persistent store. |
| **SSN / Government ID** | `CUST-SSN`, `CUST-GOVT-ISSUED-ID`, `EXP-CUST-SSN`, `EXP-CUST-GOVT-ISSUED-ID` | **Tokenization** with isolated vault. Access requires explicit authorization and audit trail. |
| **Passwords** | `SEC-USR-PWD` | **Replace with salted hash** (Argon2id/bcrypt). Enforce complexity. Add MFA. |
| **Names** | `CUST-FIRST/MIDDLE/LAST-NAME`, `SEC-USR-FNAME/LNAME`, `CARD-EMBOSSED-NAME`, `CDEMO-CUST-FNAME/MNAME/LNAME` | **Encryption at rest** (AES-256). Mask in logs and non-production environments. |
| **Addresses** | `CUST-ADDR-*`, `ACCT-ADDR-ZIP` | **Encryption at rest**. |
| **Phone Numbers** | `CUST-PHONE-NUM-1/2` | **Encryption at rest**. Mask in logs (show last 4 digits). |
| **Date of Birth** | `CUST-DOB-YYYY-MM-DD` / `CUST-DOB-YYYYMMDD` | **Encryption at rest**. Mask in non-production environments. |
| **Bank Account Ref** | `CUST-EFT-ACCOUNT-ID` | **Tokenization**. |
| **Card Expiry** | `CARD-EXPIRAION-DATE`, `PA-CARD-EXPIRY-DATE`, `PA-RQ-CARD-EXPIRY-DATE`, `EXP-CARD-EXPIRAION-DATE` | **Encryption at rest**. Protect alongside PAN. |
| **Account IDs** | `ACCT-ID`, `CDEMO-ACCT-ID`, `CC-ACCT-ID` | **Encryption at rest**. Mask in logs. |
| **Customer IDs** | `CUST-ID`, `CDEMO-CUST-ID`, `CC-CUST-ID` | **Encryption at rest**. Mask in logs. |
| **User IDs** | `SEC-USR-ID`, `CDEMO-USER-ID` | Access controls; do not log in sensitive contexts. |
| **Merchant Data** | `TRAN-MERCHANT-*`, `PA-MERCHANT-*`, `PA-RQ-MERCHANT-*` | **Encryption at rest**. Access controls per merchant agreements. |

### Cross-Cutting Modernization Actions

1. **Implement a centralized PII access layer** in the Java application to enforce encryption, tokenization, and masking policies consistently across all modules.
2. **Add structured audit logging** for all PII access (read and write), including the user, timestamp, purpose, and fields accessed.
3. **Implement data masking** in all non-production environments. Use synthetic data for development and testing.
4. **Add PII scanning to CI/CD pipeline** to detect any new code that stores, logs, or transmits PII without proper protections.
5. **Consolidate duplicate copybooks** (`CVCUS01Y.cpy` and `CUSTREC.cpy`) into a single Java entity class to reduce maintenance risk.
6. **Implement data retention policies** -- define maximum retention periods for each PII category and automate purging.
7. **Review the export mechanism** (`CVEXPORT.cpy` / `CBEXPORT.cbl` / `CBIMPORT.cbl`) -- this is the highest-risk data-in-motion pathway and must be secured end-to-end with encryption, tokenization, and access controls.
8. **Implement GDPR right-to-erasure** (Article 17) capabilities for customer data across all entities and exports.
9. **Document data flows** between services to support Data Protection Impact Assessment (DPIA) requirements.
