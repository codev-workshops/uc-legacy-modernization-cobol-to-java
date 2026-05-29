# CardDemo Modernization Blueprint

## 1. Executive Summary

CardDemo is a CICS/COBOL credit card management system comprising approximately 30 programs (online and batch), 12+ VSAM data files, and 3 optional extension modules:

- **IMS/DB2/MQ Authorization** -- credit card authorization processing with IMS hierarchical DB, DB2 fraud analytics, and MQ message queuing
- **DB2 Transaction Types** -- transaction type reference data management via embedded static SQL
- **VSAM-MQ Account Inquiry** -- account data extraction and system date inquiry through MQ channels

The **target state** is a set of Java microservices deployed behind an API gateway, migrated incrementally using the **strangler fig pattern**. Transaction Processing is the first extraction candidate.

---

## 2. Current Architecture

### 2.1 VSAM Data Store Registry

The following table lists every VSAM file used by CardDemo, including CICS-managed files (defined in `app/csd/CARDDEMO.CSD`) and batch-only files.

| CICS Name | Dataset Name | Type | Primary Key | Record Length | Copybook | Description |
|:----------|:-------------|:-----|:------------|-------------:|:---------|:------------|
| ACCTDAT | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | KSDS | Account ID (11) | 300 | CVACT01Y | Account master |
| CARDDAT | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | KSDS | Card Number (16) | 150 | CVACT02Y | Credit card master |
| CARDAIX | `AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH` | AIX | Account ID | 150 | CVACT02Y | Card-by-account alternate index |
| CCXREF | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | KSDS | Card Number (16) | 50 | CVACT03Y | Card-to-account cross-reference |
| CXACAIX | `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH` | AIX | Account ID | 50 | CVACT03Y | Cross-ref by account alternate index |
| CUSTDAT | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | KSDS | Customer ID (9) | 500 | CVCUS01Y | Customer master |
| TRANSACT | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | KSDS | Transaction ID (16) | 350 | CVTRA05Y | Online transaction log |
| USRSEC | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | KSDS | User ID (8) | 80 | CSUSR01Y | User security credentials |
| *(batch)* DALYTRAN | `AWS.M2.CARDDEMO.DALYTRAN.PS` | Sequential | N/A | 350 | CVTRA06Y | Daily transaction feed for batch posting |
| *(batch)* TCATBALF | `AWS.M2.CARDDEMO.TCATBALF.PS` | Sequential | Type+Category | 50 | CVTRA01Y | Transaction category balance totals |
| *(batch)* DISCGRP | `AWS.M2.CARDDEMO.DISCGRP.PS` | Sequential | Group code | 50 | CVTRA02Y | Disclosure groups |
| *(batch)* TRANTYPE | `AWS.M2.CARDDEMO.TRANTYPE.PS` | Sequential | Type code (2) | 60 | CVTRA03Y | Transaction types |
| *(batch)* TRANCATG | `AWS.M2.CARDDEMO.TRANCATG.PS` | Sequential | Type+Category | 60 | CVTRA04Y | Transaction categories |
| *(batch)* DALYREJS | `AWS.M2.CARDDEMO.DALYREJS.PS` | Sequential | N/A | 350 | CVTRA06Y | Daily rejected transactions |

### 2.2 Program Inventory

#### Online (CICS) Programs

| Tran ID | BMS Map | Program | Function | Optional Module |
|:--------|:--------|:--------|:---------|:----------------|
| CC00 | COSGN00 | COSGN00C | Signon Screen | |
| CM00 | COMEN01 | COMEN01C | Main Menu | |
| CAVW | COACTVW | COACTVWC | Account View | |
| CAUP | COACTUP | COACTUPC | Account Update | |
| CCLI | COCRDLI | COCRDLIC | Credit Card List | |
| CCDL | COCRDSL | COCRDSLC | Credit Card View | |
| CCUP | COCRDUP | COCRDUPC | Credit Card Update | |
| CT00 | COTRN00 | COTRN00C | Transaction List | |
| CT01 | COTRN01 | COTRN01C | Transaction View | |
| CT02 | COTRN02 | COTRN02C | Transaction Add | |
| CR00 | CORPT00 | CORPT00C | Transaction Reports | |
| CB00 | COBIL00 | COBIL00C | Bill Payment | |
| CA00 | COADM01 | COADM01C | Admin Menu | Db2: Transaction Type Mgmt |
| CU00 | COUSR00 | COUSR00C | List Users | |
| CU01 | COUSR01 | COUSR01C | Add User | |
| CU02 | COUSR02 | COUSR02C | Update User | |
| CU03 | COUSR03 | COUSR03C | Delete User | |
| CTTU | COTRTUP | COTRTUPC | Tran Type add/edit | Db2: Transaction Type Mgmt |
| CTLI | COTRTLI | COTRTLIC | Tran Type list/update/delete | Db2: Transaction Type Mgmt |
| CPVS | COPAU00 | COPAUS0C | Pending Auth Summary | IMS-DB2-MQ: Pending Authorizations |
| CPVD | COPAU01 | COPAUS1C | Pending Auth Details | IMS-DB2-MQ: Pending Authorizations |
| CP00 | -- | COPAUA0C | Process Auth Requests | IMS-DB2-MQ: Pending Authorizations |
| CDRD | -- | CODATE01 | System Date via MQ | VSAM-MQ |
| CDRA | -- | COACCT01 | Account Details via MQ | VSAM-MQ |

#### Batch Programs

| Job Name | Program | Function | Optional Module |
|:---------|:--------|:---------|:----------------|
| CLOSEFIL | IEFBR14 | Close VSAM files in CICS | |
| ACCTFILE | IDCAMS | Refresh Account Master | |
| CARDFILE | IDCAMS | Refresh Card Master | |
| CUSTFILE | IDCAMS | Refresh Customer Master | |
| XREFFILE | IDCAMS | Account/Card/Customer cross reference | |
| TRANFILE | IDCAMS | Load Transaction Master | |
| TRANBKP | IDCAMS | Backup/Refresh Transaction Master | |
| TRANIDX | IDCAMS | Define AIX for transaction file | |
| DISCGRP | IDCAMS | Load Disclosure Group file | |
| TCATBALF | IDCAMS | Refresh Transaction Category Balance | |
| TRANCATG | IDCAMS | Load Transaction category types | |
| TRANTYPE | IDCAMS | Load Transaction type file | |
| DUSRSECJ | IEBGENER | Initial Load of User security file | |
| DEFGDGB | IDCAMS | Setup GDG Bases | |
| DEFGDGD | IDCAMS | Setup GDG Bases for Db2 | |
| OPENFIL | IEFBR14 | Open files in CICS | |
| ESDSRRDS | IDCAMS | Create ESDS and RRDS VSAM files | |
| WAITSTEP | COBSWAIT | Wait job for given time | |
| POSTTRAN | CBTRN02C | Transaction processing (daily post) | |
| INTCALC | CBACT04C | Interest calculations | |
| COMBTRAN | SORT | Combine transaction files | |
| CREASTMT | CBSTM03A | Produce transaction statement | |
| TRANREPT | CBTRN03C | Transaction Report (submitted from CICS) | |
| CREADB21 | DSNTEP4 | Create CardDemo Db2 database and load tables | Db2: Transaction Type Mgmt |
| TRANEXTR | DSNTIAUL | Extract Db2 data for transaction types | Db2: Transaction Type Mgmt |
| MNTTRDB2 | COBTUPDT | Maintain Transaction type table | Db2: Transaction Type Mgmt |
| CBPAUP0J | CBPAUP0C | Purge Expired Authorizations | IMS-DB2-MQ: Pending Authorizations |

### 2.3 Shared Cross-Cutting Artifacts

#### Shared Copybooks

| Copybook | Purpose |
|:---------|:--------|
| COCOM01Y | COMMAREA -- session state passed between all online programs |
| COTTL01Y | Standard screen title line layout |
| CSDAT01Y | Date-related working storage fields |
| CSMSG01Y | Standard message area (line 1) |
| CSMSG02Y | Standard message area (line 2) |
| CSUSR01Y | User security record layout |
| CSSETATY | Set attribute byte utility |
| CSSTRPFY | String-to-PIC field utility |
| CODATECN | Date conversion condition names |
| CSLKPCDY | Lookup code utility |
| CSUTLDPY | Date utility parameters |
| CSUTLDWY | Date utility working storage |

#### Shared Utility Programs

| Program | Purpose |
|:--------|:--------|
| CSUTLDTC | Date utility (date arithmetic and formatting) |
| COBSWAIT | Timer control for batch job wait steps |

---

## 3. Target Architecture

### 3.1 Microservice Decomposition

The target state decomposes CardDemo into **7 core bounded contexts** plus 2 extension contexts, each mapping to a deployable microservice:

| # | Microservice | Core Programs | Primary Data |
|:-:|:-------------|:--------------|:-------------|
| 1 | **Auth & Session Service** | COSGN00C, COMEN01C, COADM01C | USRSEC |
| 2 | **Account Service** | COACTVWC, COACTUPC, CBACT01-04C | ACCTDAT, CUSTDAT |
| 3 | **Card Management Service** | COCRDLIC, COCRDSLC, COCRDUPC | CARDDAT, CARDAIX, CCXREF, CXACAIX |
| 4 | **Transaction Service** | COTRN00-02C, CORPT00C, COBIL00C, CBTRN01-03C, CBSTM03A/B | TRANSACT, DALYTRAN, TCATBALF, DISCGRP, TRANTYPE, TRANCATG |
| 5 | **User Security Service** | COUSR00-03C | USRSEC |
| 6 | **Customer Migration Service** | CBCUS01C, CBEXPORT, CBIMPORT | CUSTDAT |
| 7 | **Authorization Extension** | COPAUA0C, COPAUS0-2C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL | IMS DB + DB2 |
| 8 | **Tran Type Extension** | COTRTLIC, COTRTUPC, COBTUPDT | DB2 tables |
| 9 | **Account Inquiry Extension** | CODATE01, COACCT01 | ACCTDAT (via MQ) |

### 3.2 Strangler API Surface -- Transaction Processing (First Microservice)

Transaction Processing is the first extraction candidate because it has well-defined read/write boundaries and the highest business value.

#### Exposed APIs

| Method | Endpoint | Source Program(s) | Description |
|:-------|:---------|:------------------|:------------|
| GET | `/api/v1/transactions` | COTRN00C | List transactions with filtering and pagination |
| GET | `/api/v1/transactions/{tranId}` | COTRN01C | View single transaction detail |
| POST | `/api/v1/transactions` | COTRN02C (online) | Add a new transaction |
| POST | `/api/v1/transactions/bill-payment` | COBIL00C | Process bill payment |
| POST | `/api/v1/transactions/batch/post` | CBTRN02C (batch) | Trigger daily transaction posting |
| GET | `/api/v1/transactions/reports` | CORPT00C, CBTRN03C | Generate transaction reports |
| GET | `/api/v1/transactions/statements/{acct}` | CBSTM03A/B | Generate account statement |

#### Consumed APIs (Anti-Corruption Layer)

| Dependency | Method | Endpoint | Purpose |
|:-----------|:-------|:---------|:--------|
| Account Service | GET | `/api/v1/accounts/{acctId}` | Retrieve account details for validation |
| Account Service | PUT | `/api/v1/accounts/{acctId}` | Update balances after posting |
| Account Service | GET | `/api/v1/accounts/by-card/{cardNum}` | Card-to-account lookup via CCXREF |
| Customer Service | GET | `/api/v1/customers/{custId}` | Retrieve customer data for statements |

### 3.3 COMMAREA Replacement Strategy

The COBOL COMMAREA (`COCOM01Y`) passes session state (user ID, program name, screen data, return codes) between every online CICS program. In the Java target:

- **JWT / OAuth2 tokens** replace user identity and role propagation
- **Shared session store** (Redis or database-backed Spring Session) replaces per-conversation state
- **Request/response DTOs** replace screen data passing between programs
- Each microservice maintains its own session context; cross-service calls use API contracts

### 3.4 Batch Scheduling Replacement

The 13 core JCL batch jobs are replaced by **Spring Batch** (or Apache Airflow) with explicit dependency ordering:

```
CLOSEFIL -> COMBTRAN -> POSTTRAN -> INTCALC -> CREASTMT -> TRANREPT -> OPENFIL
```

Additional data-load jobs (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, DUSRSECJ) become idempotent initialization tasks managed by the scheduler.

---

## 4. Migration Phases

### Phase 1 -- Low Risk

| Programs | Domain | Rationale |
|:---------|:-------|:----------|
| COUSR00C, COUSR01C, COUSR02C, COUSR03C | User Security | Simple CRUD on USRSEC, no cross-domain writes |
| COSGN00C, COMEN01C, COADM01C | Auth & Session | Menu navigation and authentication; read-only data access |
| COTRN00C, COTRN01C | Transaction Read | Read-only access to TRANSACT and CCXREF |

### Phase 2 -- Medium Risk

| Programs | Domain | Rationale |
|:---------|:-------|:----------|
| COCRDLIC, COCRDSLC | Card List/View | Read-only with browse; moderate complexity |
| COACTVWC | Account View | Read-only account display |
| CORPT00C, CBTRN03C | Reports | Report generation; submits batch job from CICS |
| COTRTLIC, COTRTUPC, COBTUPDT | Tran Type Extension | DB2 CRUD; isolated from core VSAM files |

### Phase 3 -- High Risk

| Programs | Domain | Rationale |
|:---------|:-------|:----------|
| COCRDUPC | Card Update | 10-state machine with optimistic locking |
| COTRN02C (online) | Transaction Add | CCXREF validation, WRITE to TRANSACT |
| CBEXPORT, CBIMPORT | Customer Migration | COMP/COMP-3 conversions, REDEFINES for polymorphic records |

### Phase 4 -- Critical Risk

| Programs | Domain | Rationale |
|:---------|:-------|:----------|
| COBIL00C | Bill Payment | Dual-write, race condition on ID generation |
| CBTRN02C (batch) | Batch Post | Triple-write, no transactional boundary |
| CBSTM03A, CBSTM03B | Statement Generation | ALTER/GO TO, PSA addressing; must rewrite |
| CBACT04C | Interest Calculation | Undocumented formula, destructive cycle-reset |
| COACTUPC | Account Update | 4,236 lines, ~50 validation flags, dual-entity |
| COPAUA0C, COPAUS0-2C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL | Authorization Extension | IMS DB + DB2 + MQ two-phase commit |

---

## 5. Key Architectural Decisions

### ADR-1: COMMAREA Replacement

**Decision:** Replace the monolithic COMMAREA with JWT tokens for identity, Spring Session for conversational state, and typed DTOs for inter-program data.

**Rationale:** COCOM01Y is a single flat structure shared by every program. Decomposing it into service-specific contracts is a prerequisite for independent deployment.

### ADR-2: CCXREF Ownership

**Decision:** Card Management Service owns CCXREF. Other services access it via a synchronous lookup API (`GET /api/v1/cards/xref/{cardNum}`).

**Rationale:** CCXREF is read by 10+ programs across 4 domains but written only by card management operations. A shared lookup service prevents dual ownership while maintaining read performance.

### ADR-3: Dual-Write Resolution

**Decision:** Use the saga pattern with compensating transactions for any operation that writes to multiple data stores.

**Rationale:** COBIL00C and CBTRN02C (batch) both perform multi-file writes without transactional boundaries. Sagas provide eventual consistency while preserving data integrity across microservice boundaries.

### ADR-4: Statement Generation Rewrite

**Decision:** Rewrite CBSTM03A from scratch in Java rather than attempting mechanical translation.

**Rationale:** CBSTM03A uses ALTER/GO TO self-modifying control flow and PSA/TCB/TIOT pointer arithmetic that are z/OS-specific and have no Java equivalent. Capture the output format as a behavioral specification and build the Java implementation against it.

### ADR-5: Batch Scheduling Technology

**Decision:** Spring Batch for Java-native batch processing with Spring Cloud Data Flow or Apache Airflow for orchestration.

**Rationale:** The 13 JCL jobs have implicit ordering dependencies. Spring Batch provides chunk-oriented processing, restart/retry semantics, and transaction management that map well to the existing COBOL batch patterns.
