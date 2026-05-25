# Domain Decomposition — CardDemo COBOL Estate

## Overview

This document identifies bounded contexts within the CardDemo application by analyzing three coupling indicators:

1. **Copybook sharing** — Programs that share data structure definitions are semantically coupled
2. **JCL job grouping** — Jobs that run in sequence share a processing pipeline
3. **Data file sharing vs. isolation** — Files read/written by multiple programs create coupling; isolated files indicate natural service boundaries

Each bounded context is mapped to a candidate microservice or module.

---

## 1. Coupling Analysis

### 1.1 Copybook Sharing Clusters

Programs are grouped by which core copybooks they reference. Highly overlapping copybook usage indicates they belong to the same bounded context.

**Cluster A — Account/Customer/Card/Xref Core (CVACT01Y + CVACT02Y + CVACT03Y + CVCUS01Y)**

These four copybooks define the core entity model. Programs referencing 3+ of them are deeply coupled to the account domain:

| Program | CVACT01Y (Account) | CVACT02Y (Card) | CVACT03Y (Xref) | CVCUS01Y (Customer) | Context |
|---------|:---:|:---:|:---:|:---:|---------|
| COACTUPC | ✓ | — | ✓ | ✓ | Account Management |
| COACTVWC | ✓ | ✓ | ✓ | ✓ | Account Management |
| COBIL00C | ✓ | — | ✓ | — | Account Management |
| COCRDLIC | — | ✓ | — | — | Card Management |
| COCRDSLC | — | ✓ | — | ✓ | Card Management |
| COCRDUPC | — | ✓ | — | ✓ | Card Management |
| CBTRN01C | ✓ | ✓ | ✓ | ✓ | Transaction Processing |
| CBTRN02C | ✓ | — | ✓ | — | Transaction Processing |
| CBEXPORT | ✓ | ✓ | ✓ | ✓ | Data Migration |
| CBIMPORT | ✓ | ✓ | ✓ | ✓ | Data Migration |
| CBSTM03A | ✓ | — | ✓ | ✓* | Statement Generation |
| COPAUS0C | ✓ | ✓ | ✓ | ✓ | Authorization |

*CBSTM03A uses CUSTREC (structurally identical to CVCUS01Y).

**Cluster B — Transaction Copybooks (CVTRA01Y–CVTRA07Y)**

| Program | CVTRA01Y (CatBal) | CVTRA02Y (DiscGrp) | CVTRA03Y (TranType) | CVTRA04Y (TranCat) | CVTRA05Y (Trans) | CVTRA06Y (DailyTrans) | Context |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|---------|
| CBTRN02C | ✓ | — | — | — | ✓ | ✓ | Transaction Processing |
| CBACT04C | ✓ | ✓ | — | — | — | — | Interest Calculation |
| CBTRN03C | — | — | ✓ | ✓ | ✓ | — | Reporting |
| COTRN00C | — | — | — | — | ✓ | — | Transaction Inquiry |
| COTRN01C | — | — | — | — | ✓ | — | Transaction Inquiry |
| COTRN02C | — | — | — | — | ✓ | — | Transaction Add |
| CORPT00C | — | — | — | — | ✓ | — | Report Submission |
| COBIL00C | — | — | — | — | ✓ | — | Bill Payment |

**Cluster C — CICS Infrastructure (COCOM01Y + COTTL01Y + CSDAT01Y + CSMSG01Y + DFHAID + DFHBMSCA)**

All 17 online programs share these copybooks. These define the CICS application framework (COMMAREA, screen titles, date handling, messages, key mapping). This cluster is an infrastructure concern that cuts across all bounded contexts — it does NOT define a business domain.

**Cluster D — Security (CSUSR01Y)**

Used by: COSGN00C, COMEN01C, COADM01C, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C–03C.

Security context is referenced by many programs for authorization checks but is conceptually isolated.

**Cluster E — Authorization Sub-App (CIPAUDTY + CIPAUSMY + MQ copybooks)**

Used exclusively by: COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, PAUDBLOD, PAUDBUNL.

This cluster is completely isolated from the core application — no core programs reference these copybooks.

**Cluster F — DB2 Sub-App (CSDB2RPY + CSDB2RWY)**

Used exclusively by: COTRTLIC, COTRTUPC, COBTUPDT.

Completely isolated from VSAM-based programs.

### 1.2 JCL Job Pipeline Groupings

| Pipeline | Jobs (in sequence) | Shared Data | Context |
|----------|--------------------|-------------|---------|
| **Daily Batch Cycle** | CLOSEFIL → POSTTRAN → INTCALC → TRANREPT → OPENFIL | DALYTRAN, TRANFILE, TCATBALF, ACCTFILE, XREFFILE, DISCGRP | Transaction Processing + Interest |
| **Statement Generation** | CREASTMT → CBSTM03A/B → TXT2PDF1 | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE → STMTFILE, HTMLFILE | Statement Generation |
| **Branch Migration** | CBEXPORT → CBIMPORT | All core VSAM files → EXPFILE | Data Migration |
| **Transaction Backup** | TRANBKP (after statements) | TRANSACT → backup GDG | Lifecycle/Operations |
| **File Initialization** | ACCTFILE → CARDFILE → CUSTFILE → ... (one-time) | All VSAM definitions | Infrastructure |
| **IMS Auth Batch** | LOADPADB / UNLDPADB / CBPAUP0J | IMS DB | Authorization |
| **DB2 Maintenance** | CREADB21 / MNTTRDB2 / TRANEXTR | DB2 tables | Reference Data |

### 1.3 Data File Isolation Analysis

| Data File | Writers | Readers | Isolation Level |
|-----------|---------|---------|-----------------|
| **USRSEC** | COUSR01C, COUSR02C, COUSR03C | COSGN00C, COUSR00C, COUSR02C, COUSR03C | **Fully isolated** — only security programs |
| **DISCGRP** | (Reference data — loaded once) | CBACT04C | **Fully isolated** — only interest calc reads it |
| **TRANTYPE** | (Reference data) | CBTRN03C | **Fully isolated** — only reporting |
| **TRANCATG** | (Reference data) | CBTRN03C | **Fully isolated** — only reporting |
| **DALYTRAN** | (External feed) | CBTRN01C, CBTRN02C | **Fully isolated** — only posting programs |
| **TCATBALF** | CBTRN02C | CBACT04C, CBTRN02C | **Bounded** — shared between posting and interest calc |
| **ACCTFILE** | CBACT04C, CBTRN02C, COACTUPC, COBIL00C, CBIMPORT | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBSTM03B, COACTUPC, COACTVWC, COBIL00C, COTRN02C | **Highly shared** — cross-cuts most contexts |
| **CARDFILE** | COCRDUPC, CBIMPORT | CBACT02C, CBTRN01C, CBEXPORT, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COACTUPC | **Moderately shared** — mostly card + account programs |
| **CUSTFILE** | COACTUPC, CBIMPORT | CBCUS01C, CBTRN01C, CBEXPORT, CBSTM03B, COACTVWC, COACTUPC, COCRDSLC | **Moderately shared** — account + card + statement |
| **XREFFILE** | CBIMPORT | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBSTM03B, COACTVWC, COACTUPC, COTRN02C | **Highly shared** — universal lookup table |
| **TRANSACT** | CBTRN01C, CBTRN02C, COTRN02C, COBIL00C | CBACT04C, CBTRN03C, CBEXPORT, COTRN00C, COTRN01C | **Moderately shared** — transaction pipeline |

---

## 2. Bounded Contexts

Based on the coupling analysis above, we identify 7 bounded contexts:

### BC-1: Account Management

**Core Entity:** Account (ACCTFILE)

**Programs:** COACTUPC, COACTVWC, COBIL00C

**Defining characteristic:** These programs read and write the account record. COACTUPC also modifies customer data — the Account context "owns" the account-customer relationship.

**Shared data dependencies:**
- Reads CARDFILE (via AIX — to display card info alongside account)
- Reads/writes CUSTFILE (customer updates happen through account screen)
- Reads XREFFILE (card→customer→account lookup)
- Writes TRANSACT (bill payment creates transaction records)

**Boundary tension:** COACTUPC's coupling to CUSTFILE and CARDFILE means Account Management and Customer/Card Management are not cleanly separated in the legacy system. In the microservice architecture, Account Service will call Card Service and Customer Service via APIs rather than direct file access.

### BC-2: Card Management

**Core Entity:** Card (CARDFILE), Cross-Reference (XREFFILE)

**Programs:** COCRDLIC, COCRDSLC, COCRDUPC

**Defining characteristic:** Card lifecycle operations (list, view, update). The Card context owns the Card entity and the XREFFILE cross-reference that links cards to customers and accounts.

**Shared data dependencies:**
- Reads ACCTFILE (display account info on card detail screens)
- Reads CUSTFILE (display customer info on card detail screens)

**Boundary notes:** XREFFILE is the single most referenced file in the estate (10 programs read it). In the microservice architecture, the Card Service should own the cross-reference data and expose a lookup API that other services call.

### BC-3: Transaction Processing

**Core Entity:** Transaction (TRANSACT/TRANFILE), Daily Transaction (DALYTRAN), Category Balance (TCATBALF)

**Programs:** CBTRN02C, CBTRN01C, CBTRN03C, COTRN00C, COTRN01C, COTRN02C, CORPT00C

**Defining characteristic:** Everything related to creating, posting, validating, browsing, and reporting on transactions. This is the highest-volume processing context.

**Sub-boundaries within this context:**
- **Posting** (CBTRN02C, CBTRN01C) — batch pipeline that reads daily feed and updates master files
- **Inquiry** (COTRN00C, COTRN01C) — read-only browsing
- **Entry** (COTRN02C) — online transaction creation
- **Reporting** (CBTRN03C, CORPT00C) — formatted reports

**Shared data dependencies:**
- Reads ACCTFILE (validate account status/limits during posting)
- Reads XREFFILE (card→account resolution)
- Reads/writes TCATBALF (category balances — shared with Interest Calculation)

**Boundary notes:** The known asymmetry between online add (COTRN02C, which does NOT update balances) and batch posting (CBTRN02C, which does) should be resolved in the microservice architecture by having a single Transaction Service that always updates balances.

### BC-4: Interest Calculation

**Core Entity:** Disclosure Group (DISCGRP), Category Balance (TCATBALF)

**Programs:** CBACT04C

**Defining characteristic:** Pure computation — reads category balances and rate tables, calculates interest, updates account balances, and writes interest transactions.

**Shared data dependencies:**
- Reads TCATBALF (shared with Transaction Processing — written by CBTRN02C)
- Reads DISCGRP (isolated — only this program reads it)
- Reads/rewrites ACCTFILE (updates interest charges)
- Reads XREFFILE (card→account lookup)
- Writes TRANSACT (interest charge transactions)

**Boundary notes:** Interest Calculation is a strong candidate for a standalone service. Its input contract is clear (category balances + rates → interest charges), and its output contract is well-defined (updated account balances + interest transactions). DISCGRP ownership belongs to this context.

### BC-5: User Security & Authentication

**Core Entity:** User Security Record (USRSEC)

**Programs:** COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C

**Defining characteristic:** Fully isolated data file (USRSEC). No other bounded context writes to or depends on the USRSEC data — they only check the user type ('A'/'U') that was passed into the COMMAREA at sign-on.

**Shared data dependencies:** None. USRSEC is the most isolated data store in the estate.

**Boundary notes:** This is the cleanest bounded context — zero data file overlap with other contexts. In the microservice architecture, this becomes the Auth Service (Spring Security + JWT), and all other services validate tokens rather than reading USRSEC.

### BC-6: Card Authorization

**Core Entities:** Pending Authorization (IMS DB), Authorization Fraud (DB2 AUTHFRDS), MQ Messages (request/reply/error queues)

**Programs:** COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, PAUDBLOD, PAUDBUNL, DBUNLDGS

**Defining characteristic:** Completely isolated sub-application. Uses its own data stores (IMS DB, DB2), its own copybooks (CIPAUDTY, CIPAUSMY, MQ copybooks), and its own JCL. No core program references authorization copybooks.

**Shared data dependencies:**
- COPAUA0C reads ACCTFILE and XREFFILE during authorization decisions (to check account status and credit limits)
- COPAUS0C reads the same files for display purposes

**Boundary notes:** Authorization needs read access to Account and Card data but never writes to them. In the microservice architecture, the Authorization Service calls the Account Service API for balance/limit checks rather than direct file reads.

### BC-7: Statement Generation

**Core Entity:** Statement output (STMTFILE, HTMLFILE)

**Programs:** CBSTM03A, CBSTM03B

**Defining characteristic:** Read-only ETL process. Reads sorted transactions, customer, account, and xref data to generate formatted statements. Never modifies source data.

**Shared data dependencies:**
- Reads TRNXFILE (sorted transactions — prepared by CREASTMT SORT step)
- Reads XREFFILE, CUSTFILE, ACCTFILE

**Boundary notes:** Statement Generation is a pure consumer of data from other contexts. In the microservice architecture, it becomes a scheduled job that calls Transaction Service, Account Service, and Customer APIs to gather data, then renders output using templates.

---

## 3. Candidate Microservices

### Service Map

```
┌──────────────────────────────────────────────────────────────────┐
│                        API Gateway                                │
│                    (Spring Cloud Gateway)                          │
└──────┬──────────┬──────────┬──────────┬──────────┬───────────────┘
       │          │          │          │          │
       ▼          ▼          ▼          ▼          ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│  Auth    │ │ Account  │ │  Card    │ │ Trans-   │ │ Auth-    │
│ Service  │ │ Service  │ │ Service  │ │ action   │ │ orization│
│          │ │          │ │          │ │ Service  │ │ Service  │
│ BC-5     │ │ BC-1     │ │ BC-2     │ │ BC-3     │ │ BC-6     │
└──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
                                            │
                                            ▼
                               ┌─────────────────────┐
                               │  Interest Calc       │
                               │  Service (BC-4)      │
                               │  (called by batch)   │
                               └─────────────────────┘

Async / Batch:
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Statement   │  │  Reference   │  │  Data        │
│  Service     │  │  Data Svc    │  │  Migration   │
│  BC-7        │  │  BC-9*       │  │  (Temporary) │
└──────────────┘  └──────────────┘  └──────────────┘
```

### Service Detail

| Service | Bounded Context | Source Programs | Database | Key APIs |
|---------|----------------|-----------------|----------|----------|
| **Auth Service** | BC-5 | COSGN00C, COUSR00C–03C | PostgreSQL (`users` table) | `POST /auth/login`, `POST /auth/register`, `GET /users`, `PUT /users/{id}`, `DELETE /users/{id}` |
| **Account Service** | BC-1 | COACTUPC, COACTVWC, COBIL00C | PostgreSQL (`accounts`, `customers` tables) | `GET /accounts/{id}`, `PUT /accounts/{id}`, `POST /accounts/{id}/payments`, `GET /accounts/{id}/customer` |
| **Card Service** | BC-2 | COCRDLIC, COCRDSLC, COCRDUPC | PostgreSQL (`cards`, `card_xref` tables) | `GET /cards`, `GET /cards/{num}`, `PUT /cards/{num}`, `GET /cards/by-account/{id}` |
| **Transaction Service** | BC-3 | CBTRN02C, CBTRN01C, COTRN00C–02C, CORPT00C | PostgreSQL (`transactions`, `category_balances` tables) | `GET /transactions`, `GET /transactions/{id}`, `POST /transactions`, `POST /transactions/batch-post` |
| **Interest Calculation Service** | BC-4 | CBACT04C | PostgreSQL (reads `category_balances`, `disclosure_groups`; writes `accounts`, `transactions`) | `POST /interest/calculate` (triggered by scheduler) |
| **Authorization Service** | BC-6 | COPAUA0C, COPAUS0C–2C, CBPAUP0C | PostgreSQL (`authorizations`, `fraud_flags` tables) | `POST /authorizations/decide`, `GET /authorizations`, `POST /authorizations/{id}/flag-fraud` |
| **Statement Service** | BC-7 | CBSTM03A/B | Reads from other services via API; outputs to file storage | `POST /statements/generate` (scheduled), `GET /statements/{id}/download` |
| **Reference Data Service** | (Cross-cutting) | COTRTLIC, COTRTUPC, COBTUPDT | PostgreSQL (`transaction_types`, `transaction_categories`, `disclosure_groups`) | `GET /ref/transaction-types`, `PUT /ref/transaction-types/{code}`, `GET /ref/categories` |
| **Reporting Service** | (Cross-cutting) | CBTRN03C, CORPT00C | Reads from Transaction Service via API | `POST /reports/daily-transactions`, `GET /reports/{id}` |

### Data Ownership Matrix

Each microservice owns its primary tables and exposes data to others via APIs:

| Data Entity | Owner Service | Consumers (via API) |
|-------------|--------------|---------------------|
| User/Security | Auth Service | All services (token validation) |
| Account | Account Service | Card, Transaction, Authorization, Statement |
| Customer | Account Service | Card, Statement |
| Card | Card Service | Account, Transaction, Authorization |
| Cross-Reference (card→acct→cust) | Card Service | Transaction, Statement, Authorization |
| Transaction | Transaction Service | Statement, Reporting, Interest Calc |
| Category Balance | Transaction Service | Interest Calc |
| Disclosure Group | Reference Data Service | Interest Calc |
| Transaction Type/Category | Reference Data Service | Reporting, Transaction |
| Pending Authorization | Authorization Service | (self-contained) |
| Fraud Flags | Authorization Service | (self-contained) |

### Inter-Service Communication

| From Service | To Service | Pattern | Purpose |
|-------------|------------|---------|---------|
| Transaction Service | Card Service | Sync (REST) | Resolve card→account during posting |
| Transaction Service | Account Service | Sync (REST) | Validate account status, update balances |
| Interest Calc Service | Transaction Service | Sync (REST) | Read category balances |
| Interest Calc Service | Account Service | Sync (REST) | Update account with interest charges |
| Interest Calc Service | Reference Data Service | Sync (REST) | Read disclosure group rates |
| Authorization Service | Account Service | Sync (REST) | Check credit limit during auth decision |
| Authorization Service | Card Service | Sync (REST) | Validate card status |
| Statement Service | Transaction Service | Sync (REST) | Read transactions for period |
| Statement Service | Account Service | Sync (REST) | Read account/customer details |
| Statement Service | Card Service | Sync (REST) | Read card cross-references |
| Transaction Service | — | Async (Event) | Publish `TransactionPosted` event |
| Authorization Service | — | Async (Event) | Publish `AuthorizationDecided` event |

---

## 4. Shared vs. Isolated Data Summary

### Isolated Data (Clean Service Boundaries)

| Data | Current File | Target Service | Migration Risk |
|------|-------------|----------------|----------------|
| User credentials | USRSEC | Auth Service | Low — fully isolated |
| Pending authorizations | IMS DB | Authorization Service | Low — isolated sub-app |
| Fraud flags | DB2 AUTHFRDS | Authorization Service | Low — isolated sub-app |
| Disclosure groups | DISCGRP | Reference Data Service | Low — read-only reference |
| Transaction types | DB2 tables | Reference Data Service | Low — isolated sub-app |
| Transaction categories | TRANCATG | Reference Data Service | Low — read-only reference |
| Statement output | STMTFILE/HTMLFILE | Statement Service | Low — output only |

### Shared Data (Requires Careful Decomposition)

| Data | Current File | Owning Service | Dependent Services | Decomposition Strategy |
|------|-------------|----------------|-------------------|----------------------|
| **Account** | ACCTFILE | Account Service | Transaction, Interest, Authorization, Statement | Account Service exposes REST API; dependents call API instead of direct reads |
| **Card** | CARDFILE | Card Service | Account, Transaction | Card Service exposes REST API |
| **Customer** | CUSTFILE | Account Service | Card, Statement | Embedded in Account Service; exposed via API |
| **Cross-Reference** | XREFFILE | Card Service | Transaction, Interest, Statement, Account | Card Service provides lookup API: `GET /cards/xref?cardNum={num}` |
| **Transaction** | TRANSACT/TRANFILE | Transaction Service | Interest, Statement, Reporting | Transaction Service exposes query API |
| **Category Balance** | TCATBALF | Transaction Service | Interest Calc | Transaction Service exposes API; Interest Calc reads via API |
