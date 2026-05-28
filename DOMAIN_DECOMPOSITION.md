# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo application by analyzing program-to-copybook relationships, JCL job groupings, data-file sharing patterns, and CICS transaction definitions. Each bounded context maps to a candidate microservice or module, with extraction seams rated by difficulty.

---

## Methodology

Bounded contexts are identified by three convergent analyses:

1. **Copybook sharing** — Programs that COPY the same data-structure definitions operate on the same domain entities.
2. **JCL job groupings** — Control-M folders and JCL step sequences reveal batch workflow boundaries.
3. **Data-file sharing** — VSAM datasets accessed by multiple programs indicate coupling; isolated datasets suggest natural boundaries.

---

## Data File Access Matrix

| VSAM Dataset | Key | Online Programs | Batch Programs | Shared? |
|-------------|-----|----------------|----------------|---------|
| **ACCTDAT** | Account ID (11) | COACTVWC, COACTUPC, COBIL00C, COTRN02C | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03B | **Yes — heavily** |
| **CARDDAT** | Card Number (16) | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC | CBACT02C, CBEXPORT, CBIMPORT | **Yes** |
| **CARDAIX** | Alt Index (Account) | COCRDLIC | — | No |
| **CCXREF** | Card-to-Account | COACTVWC, COACTUPC, COBIL00C, COTRN02C | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03B | **Yes — heavily** |
| **CXACAIX** | Alt Index (Account) | COACTUPC | — | No |
| **CUSTDAT** | Customer ID (9) | COACTVWC, COCRDSLC, COCRDUPC | CBCUS01C, CBEXPORT, CBIMPORT, CBSTM03B | **Yes** |
| **TRANSACT** | Transaction ID | COTRN00C, COTRN01C, COTRN02C, COBIL00C | CBTRN01C, CBTRN02C, CBTRN03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03B | **Yes — heavily** |
| **USRSEC** | User ID | COSGN00C, COUSR00C-03C | — | **Isolated** |
| **DALYTRAN** | — | — | CBTRN01C, CBTRN02C | Batch-only |
| **DALYREJS** | — | — | CBTRN02C | Batch-only |
| **TCATBALF** | Category Key | — | CBACT04C, CBTRN02C | Batch-only |
| **DISCGRP** | Group Key | — | CBACT04C | Batch-only |
| **TRANTYPE** | Type Code | — | CBTRN03C | Batch-only (+ DB2 extension) |
| **TRANCATG** | Category Code | — | CBTRN03C | Batch-only (+ DB2 extension) |

---

## Copybook-to-Program Affinity Map

| Copybook | Domain Entity | Programs Using It |
|----------|--------------|-------------------|
| **CVACT01Y** | Account Record | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVACT02Y** | Card Record | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT |
| **CVACT03Y** | Cross-Reference | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03B |
| **CVCUS01Y** | Customer Record | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT |
| **CVTRA05Y** | Transaction Record | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| **CVTRA06Y** | Daily Transaction | CBTRN01C, CBTRN02C |
| **CVTRA01Y** | Category Balance | CBACT04C, CBTRN02C |
| **CVCRD01Y** | Card Detail (UI) | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CSUSR01Y** | User Security | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| **COCOM01Y** | COMMAREA | All online CICS programs |
| **CVEXPORT** | Export Record | CBEXPORT, CBIMPORT |

---

## Bounded Contexts

### BC-1: Identity & Access Management

**Scope:** Authentication, authorization, and user lifecycle.

| Attribute | Value |
|-----------|-------|
| **Programs** | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **Copybooks** | CSUSR01Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| **Data stores** | USRSEC (exclusive) |
| **CICS Transactions** | CC00, CU00, CU01, CU02, CU03 |
| **Candidate service** | **Identity Service** (Spring Security + user store) |

**Isolation analysis:** USRSEC is accessed only by sign-on and user-admin programs. CSUSR01Y is also included by some account/card programs purely for display of the logged-in user name — this is a read-only, presentation-layer dependency that can be replaced with a JWT claim.

**Extraction seam:** COMMAREA `CDEMO-USER-ID` and `CDEMO-USER-TYPE` fields. Post-migration, these become JWT claims passed as HTTP headers.

---

### BC-2: Account & Card Domain

**Scope:** Account lifecycle, card management, and the cross-reference linking them.

| Attribute | Value |
|-----------|-------|
| **Programs (online)** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| **Programs (batch)** | CBACT01C, CBACT02C, CBACT03C, CBCUS01C |
| **Copybooks** | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSLKPCDY |
| **Data stores** | ACCTDAT, CARDDAT, CARDAIX, CCXREF, CXACAIX, CUSTDAT |
| **CICS Transactions** | CAVW, CAUP, CCLI, CCDL, CCUP |
| **Candidate service** | **Account Service** (REST API + PostgreSQL) |

**Isolation analysis:** This is the largest and most coupled context. ACCTDAT, CARDDAT, and CCXREF are shared with Transaction Processing and Batch Posting. The customer entity (CUSTDAT/CVCUS01Y) is used here and in transaction/statement programs. Customer data could be its own context but the tight key relationship (Account → Card → Customer via CCXREF) makes separation premature.

**Why merged (Account + Card + Customer):**
- CCXREF creates a 3-way join between Card Number → Account ID → Customer ID.
- Every card operation requires account lookup and vice versa.
- Splitting would require distributed transactions for card/account updates.

---

### BC-3: Transaction Processing

**Scope:** Online transaction entry/viewing and daily batch posting.

| Attribute | Value |
|-----------|-------|
| **Programs (online)** | COTRN00C, COTRN01C, COTRN02C |
| **Programs (batch)** | CBTRN01C, CBTRN02C |
| **Copybooks** | CVTRA05Y, CVTRA06Y, CVTRA01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CVACT02Y |
| **Data stores** | TRANSACT, DALYTRAN (input), DALYREJS (output), ACCTDAT, CCXREF, TCATBALF |
| **CICS Transactions** | CT00, CT01, CT02 |
| **JCL Jobs** | POSTTRAN, DALYREJS, TRANBKP |
| **Candidate service** | **Transaction Service** (REST API + PostgreSQL + Spring Batch) |

**Isolation analysis:** TRANSACT is the most shared dataset — accessed by 11 programs across 3 contexts. The daily-batch posting programs (CBTRN01C, CBTRN02C) validate transactions against ACCTDAT and CCXREF, creating a hard dependency on BC-2. DALYTRAN and DALYREJS are batch-only and isolated.

---

### BC-4: Billing & Payments

**Scope:** Bill payment processing.

| Attribute | Value |
|-----------|-------|
| **Programs** | COBIL00C |
| **Copybooks** | CVACT01Y, CVACT03Y, CVTRA05Y, COCOM01Y |
| **Data stores** | ACCTDAT (R/W), CCXREF (R), TRANSACT (W) |
| **CICS Transaction** | CB00 |
| **Candidate service** | **Payment Service** (REST API) |

**Isolation analysis:** Bill payment reads account data from BC-2 and writes transactions to BC-3's TRANSACT file. It acts as a cross-cutting operation between the two core contexts.

---

### BC-5: Financial Calculations (Interest & Category Balances)

**Scope:** Monthly interest calculation and transaction-category balance maintenance.

| Attribute | Value |
|-----------|-------|
| **Programs** | CBACT04C |
| **Copybooks** | CVTRA01Y, CVTRA02Y, CVTRA05Y, CVACT01Y, CVACT03Y |
| **Data stores** | TCATBALF (R/W), DISCGRP (R), ACCTDAT (R/W), CCXREF (R), TRANSACT (R) |
| **JCL Jobs** | INTCALC, COMBTRAN |
| **Control-M Folder** | MONTHLY-InterestCalculation |
| **Candidate service** | **Interest Calculation Engine** (Spring Batch job) |

**Isolation analysis:** TCATBALF and DISCGRP are exclusively used by this context. However, it reads ACCTDAT, CCXREF, and TRANSACT from BC-2/BC-3 and writes updated balances back to ACCTDAT. This is a high-coupling seam.

---

### BC-6: Reporting & Statements

**Scope:** Transaction reports and account statements.

| Attribute | Value |
|-----------|-------|
| **Programs** | CORPT00C (online), CBTRN03C, CBSTM03A, CBSTM03B (batch) |
| **Copybooks** | CVTRA05Y, CVTRA03Y, CVTRA04Y, CVTRA07Y, CVACT03Y, COSTM01, CUSTREC, CVACT01Y |
| **Data stores** | TRANSACT (R), CCXREF (R), TRANTYPE (R), TRANCATG (R), CUSTDAT (R), ACCTDAT (R) |
| **CICS Transaction** | CR00 |
| **JCL Jobs** | TRANREPT, CREASTMT |
| **Candidate service** | **Reporting Service** (Spring Batch + PDF/HTML generation) |

**Isolation analysis:** Entirely read-only against data from BC-2 and BC-3. TRANTYPE and TRANCATG reference files are used only here and in the DB2 Transaction Type extension. This context has no write-coupling to any other context, making it the easiest to extract.

---

### BC-7: Reference Data Management (DB2 Extension)

**Scope:** Transaction type and category maintenance.

| Attribute | Value |
|-----------|-------|
| **Programs** | COTRTLIC, COTRTUPC, COBTUPDT |
| **Copybooks** | DB2 DCL/DDL, extension-specific copybooks |
| **Data stores** | DB2 TRANSACTION_TYPE, TRANSACTION_TYPE_CATEGORY; VSAM TRANTYPE, TRANCATG (via extract) |
| **CICS Transactions** | CTLI, CTTU |
| **JCL Jobs** | CREADB21, TRANEXTR, MNTTRDB2 |
| **Control-M Folder** | WEEKLY-TransactionTypesDBRefresh |
| **Candidate service** | **Reference Data Service** (REST API + PostgreSQL) |

**Isolation analysis:** DB2 tables are mastered here and extracted to VSAM flat files consumed by BC-6 (Reporting). Once VSAM is eliminated, this context simply manages relational reference tables.

---

### BC-8: Authorization Processing (IMS/DB2/MQ Extension)

**Scope:** Real-time credit card authorization, fraud detection.

| Attribute | Value |
|-----------|-------|
| **Programs** | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, PAUDBLOD, PAUDBUNL, DBUNLDGS |
| **Data stores** | IMS DB (DBPAUTP0/DBPAUTX0), DB2 AUTHFRDS, MQ queues, ACCTDAT/CUSTDAT (VSAM read) |
| **CICS Transactions** | CAUT, CAUV, CAUF (from extension CSD) |
| **Candidate service** | **Authorization Service** (event-driven microservice) |

**Isolation analysis:** IMS DB and MQ queues are exclusive to this context. Reads ACCTDAT and CUSTDAT from BC-2 for validation. DB2 fraud table is exclusive. Cross-subsystem (IMS + DB2 + MQ) transactions make this the most technically complex context.

---

### BC-9: Data Migration (Branch Export/Import)

**Scope:** Cross-branch data transfer.

| Attribute | Value |
|-----------|-------|
| **Programs** | CBEXPORT, CBIMPORT |
| **Copybooks** | CVEXPORT (exclusive), plus CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y |
| **Data stores** | All core VSAM files (R for export, W for import), EXPFILE (exclusive) |
| **JCL Jobs** | CBEXPORT, CBIMPORT |
| **Candidate service** | **Data Migration Utility** (Spring Batch ETL) |

**Isolation analysis:** CVEXPORT copybook is exclusive. However, CBEXPORT reads and CBIMPORT writes every core VSAM file (accounts, cards, customers, cross-refs, transactions), creating maximum data-coupling. This context should be migrated last, after all data stores are in the relational target.

---

### BC-10: Infrastructure Utilities

**Scope:** File management, scheduling support, and wait utilities.

| Attribute | Value |
|-----------|-------|
| **Programs** | COBSWAIT, CSUTLDTC, COBDATFT (assembler) |
| **JCL Jobs** | CLOSEFIL, OPENFIL, WAITSTEP, DISCGRP |
| **Control-M Folders** | Shared across DAILY/WEEKLY/MONTHLY schedules |
| **Candidate service** | **Eliminated** — replaced by database connection pooling, cloud scheduler, and standard date libraries. |

**Isolation analysis:** These are platform utilities, not business logic. They become unnecessary once VSAM is replaced (no CICS file open/close) and mainframe scheduling is replaced with cloud-native orchestration.

---

## Extraction Seams

An extraction seam is a point where two bounded contexts interact. The difficulty rating reflects the effort to decouple them.

| Seam ID | From Context | To Context | Interaction Type | Shared Data | Difficulty | Rationale |
|---------|-------------|------------|-----------------|-------------|------------|-----------|
| S-1 | BC-1 (Identity) | BC-2..BC-8 (All online) | COMMAREA fields: USER-ID, USER-TYPE | None (in-memory) | **Easy** | Replace COMMAREA user fields with JWT claims in HTTP headers. No shared data files. |
| S-2 | BC-3 (Transactions) | BC-2 (Accounts) | VSAM reads: ACCTDAT, CCXREF during posting | ACCTDAT, CCXREF | **Hard** | Transaction posting validates against account data and updates account balances. Requires distributed transaction or saga pattern. |
| S-3 | BC-4 (Billing) | BC-2 (Accounts) | VSAM R/W: ACCTDAT balance update | ACCTDAT | **Hard** | Payment updates account balance — requires atomic consistency or compensating transactions. |
| S-4 | BC-4 (Billing) | BC-3 (Transactions) | VSAM W: TRANSACT (new payment record) | TRANSACT | **Medium** | Payment writes a transaction record. Can be decoupled via an event (PaymentCompleted → Transaction created). |
| S-5 | BC-5 (Interest) | BC-2 (Accounts) | VSAM R/W: ACCTDAT balance update | ACCTDAT | **Hard** | Interest calculation updates account balances. Must run as a batch saga with reconciliation. |
| S-6 | BC-5 (Interest) | BC-3 (Transactions) | VSAM R: TRANSACT for category sums | TRANSACT | **Medium** | Read-only; can be replaced with a database view or API call. |
| S-7 | BC-6 (Reporting) | BC-2 + BC-3 | VSAM R: ACCTDAT, CUSTDAT, TRANSACT, CCXREF | Multiple (read-only) | **Easy** | Read-only access. Replace with database queries or a read-replica/CQRS projection. |
| S-8 | BC-7 (Ref Data) | BC-6 (Reporting) | VSAM flat file: TRANTYPE, TRANCATG extract | TRANTYPE, TRANCATG | **Easy** | Replace file extract with direct DB table access. |
| S-9 | BC-8 (Auth) | BC-2 (Accounts) | VSAM R: ACCTDAT, CUSTDAT for validation | ACCTDAT, CUSTDAT | **Medium** | Read-only validation. Replace with API call to Account Service. MQ → event bus. |
| S-10 | BC-9 (Migration) | BC-2 + BC-3 | VSAM R/W: all core files | All core VSAM files | **Hard** | Touches every data entity. Must be rebuilt after all data stores migrate. |
| S-11 | BC-3 (Batch posting) | BC-5 (Interest) | VSAM R/W: TCATBALF category balances | TCATBALF | **Medium** | CBTRN02C updates TCATBALF during posting; CBACT04C reads it during interest calc. Sequential batch dependency. |
| S-12 | BC-10 (Infra) | All batch contexts | JCL: CLOSEFIL/OPENFIL/WAITSTEP | CICS file state | **Easy** | Eliminated when VSAM is replaced — no file open/close needed for a database. |

---

## Extraction Difficulty Summary

| Difficulty | Count | Seams |
|------------|-------|-------|
| **Easy** | 4 | S-1, S-7, S-8, S-12 |
| **Medium** | 4 | S-4, S-6, S-9, S-11 |
| **Hard** | 4 | S-2, S-3, S-5, S-10 |

---

## Candidate Microservice Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        API Gateway / BFF                        │
└──────┬──────────┬──────────┬──────────┬──────────┬──────────────┘
       │          │          │          │          │
  ┌────▼────┐ ┌───▼────┐ ┌──▼───┐ ┌───▼────┐ ┌───▼──────┐
  │Identity │ │Account │ │Trans │ │Payment │ │Reporting │
  │Service  │ │Service │ │Svc   │ │Service │ │Service   │
  └────┬────┘ └───┬────┘ └──┬───┘ └───┬────┘ └───┬──────┘
       │          │          │         │          │
       │     ┌────▼──────────▼─────────▼──┐       │
       │     │    Core Database           │◄──────┘ (read replica)
       │     │  (PostgreSQL / Aurora)     │
       │     └────────────┬───────────────┘
       │                  │
  ┌────▼────┐    ┌────────▼────────┐    ┌──────────────┐
  │User DB  │    │ Event Bus       │    │Authorization │
  │(or IdP) │    │ (Kafka / SQS)   │    │Service       │
  └─────────┘    └────────┬────────┘    └──────┬───────┘
                          │                    │
                 ┌────────▼────────┐   ┌───────▼───────┐
                 │Interest Calc   │   │Fraud Analytics│
                 │(Spring Batch)  │   │(PostgreSQL)   │
                 └────────────────┘   └───────────────┘
```

### Service Boundaries

| Service | Owns | Exposes | Consumes |
|---------|------|---------|----------|
| **Identity Service** | User store (USRSEC → relational/IdP) | JWT tokens, User CRUD API | — |
| **Account Service** | ACCTDAT, CARDDAT, CCXREF, CUSTDAT → `accounts`, `cards`, `card_xref`, `customers` tables | Account/Card/Customer CRUD, balance queries | Identity (JWT validation) |
| **Transaction Service** | TRANSACT, DALYTRAN → `transactions`, `daily_transactions` tables | Transaction CRUD, batch posting | Account Service (validation) |
| **Payment Service** | — (stateless) | Payment API | Account Service (balance update), Transaction Service (record creation) |
| **Reporting Service** | Report templates, generated output | Report generation API | Account + Transaction (read-only queries) |
| **Reference Data Service** | TRANTYPE, TRANCATG, DISCGRP → `transaction_types`, `transaction_categories`, `disclosure_groups` tables | Ref data CRUD API | — |
| **Authorization Service** | IMS DB → `authorizations` table, DB2 AUTHFRDS → `fraud_cases` table | Auth request/response (event-driven) | Account Service (validation), Event Bus |
| **Interest Calculation Engine** | TCATBALF → `category_balances` table | Scheduled batch job | Account + Transaction (reads), Account (balance writes) |

---

## Data Store Migration Target

| Current Store | Type | Target Table(s) | Migration Complexity |
|--------------|------|-----------------|---------------------|
| ACCTDAT | VSAM KSDS | `accounts` | Medium — shared across many programs |
| CARDDAT | VSAM KSDS | `cards` | Medium |
| CCXREF | VSAM KSDS + AIX | `card_account_xref` (or FK on `cards`) | Low — becomes a foreign key relationship |
| CUSTDAT | VSAM KSDS | `customers` | Medium |
| TRANSACT | VSAM KSDS | `transactions` | High — most shared dataset |
| USRSEC | VSAM KSDS | `users` (or external IdP) | Low — isolated |
| DALYTRAN | Sequential | `daily_transactions` (staging table) | Low — batch input only |
| TCATBALF | VSAM KSDS | `category_balances` | Low — batch only |
| DISCGRP | VSAM KSDS | `disclosure_groups` | Low — batch only |
| TRANTYPE | VSAM (from DB2 extract) | `transaction_types` (already in DB2) | Very Low |
| TRANCATG | VSAM (from DB2 extract) | `transaction_categories` (already in DB2) | Very Low |
| IMS DBPAUTP0 | IMS HIDAM | `authorizations` | High — hierarchical to relational mapping |
| DB2 AUTHFRDS | DB2 table | `fraud_cases` | Very Low — already relational |
| DB2 TRANSACTION_TYPE | DB2 table | `transaction_types` | Very Low — direct port |
