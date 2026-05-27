# Dependency Map

## 1. Program Call Graph

### 1.1 Online Program Flow (XCTL)

```mermaid
graph TD
    COSGN00C[COSGN00C<br/>Sign-on]
    COADM01C[COADM01C<br/>Admin Menu]
    COMEN01C[COMEN01C<br/>Main Menu]

    COSGN00C -->|Admin user| COADM01C
    COSGN00C -->|Regular user| COMEN01C

    %% Admin menu targets
    COADM01C --> COUSR00C[COUSR00C<br/>User List]
    COADM01C --> COUSR01C[COUSR01C<br/>User Add]
    COADM01C --> COUSR02C[COUSR02C<br/>User Update]
    COADM01C --> COUSR03C[COUSR03C<br/>User Delete]
    COADM01C --> COTRTLIC[COTRTLIC<br/>Tran Type List]
    COADM01C --> COTRTUPC[COTRTUPC<br/>Tran Type Update]

    %% Main menu targets
    COMEN01C --> COACTVWC[COACTVWC<br/>Account View]
    COMEN01C --> COACTUPC[COACTUPC<br/>Account Update]
    COMEN01C --> COCRDLIC[COCRDLIC<br/>Card List]
    COMEN01C --> COCRDSLC[COCRDSLC<br/>Card View]
    COMEN01C --> COCRDUPC[COCRDUPC<br/>Card Update]
    COMEN01C --> COTRN00C[COTRN00C<br/>Transaction List]
    COMEN01C --> COTRN01C[COTRN01C<br/>Transaction View]
    COMEN01C --> COTRN02C[COTRN02C<br/>Transaction Add]
    COMEN01C --> CORPT00C[CORPT00C<br/>Reports]
    COMEN01C --> COBIL00C[COBIL00C<br/>Bill Payment]
    COMEN01C --> COPAUS0C[COPAUS0C<br/>Auth View]

    %% Sub-navigation
    COUSR00C --> COUSR02C
    COUSR00C --> COUSR03C
    COCRDLIC --> COCRDSLC
    COCRDLIC --> COCRDUPC
    COTRN00C --> COTRN01C
    COACTUPC --> COCRDUPC
    COACTUPC --> COCRDLIC
    COACTUPC --> COMEN01C
    COACTVWC --> COCRDSLC
    COACTVWC --> COCRDUPC
    COACTVWC --> COMEN01C
    COPAUS0C --> COPAUS1C[COPAUS1C<br/>Auth Detail]
    COPAUS0C --> COPAUS2C[COPAUS2C<br/>Mark Fraud]
```

### 1.2 Batch Program CALL Dependencies

```mermaid
graph LR
    CBACT01C[CBACT01C<br/>Read Accounts] -->|CALL| COBDATFT[COBDATFT<br/>Date Format]
    CBACT01C -->|CALL| CEE3ABD[CEE3ABD<br/>Abend Handler]
    CBACT04C[CBACT04C<br/>Interest Calc] -->|CALL| CEE3ABD
    CBTRN02C[CBTRN02C<br/>Post Trans] -->|CALL| CEE3ABD
    CORPT00C[CORPT00C<br/>Reports] -->|CALL| CSUTLDTC[CSUTLDTC<br/>Date Validate]
    COTRN02C[COTRN02C<br/>Add Trans] -->|CALL| CSUTLDTC
    CSUTLDTC -->|CALL| CEEDAYS[CEEDAYS<br/>LE Date Service]
    CBSTM03A[CBSTM03A<br/>Statements] -->|CALL| CBSTM03B[CBSTM03B<br/>File Processing]
    COBSWAIT[COBSWAIT<br/>Wait Utility] -->|CALL| MVSWAIT[MVSWAIT<br/>System Wait]
```

---

## 2. Dataset Lineage

### 2.1 VSAM File Ownership

| VSAM Dataset | Definition JCL | Record Layout | Primary Key | Programs That READ | Programs That WRITE/UPDATE |
|-------------|---------------|---------------|-------------|-------------------|---------------------------|
| ACCTFILE (Account) | ACCTFILE.jcl | CVACT01Y (300 bytes) | ACCT-ID | CBACT01C, CBACT04C, CBEXPORT, CBTRN01C, CBTRN02C, COACTVWC, COACTUPC, COBIL00C | CBACT04C, CBTRN02C, COACTUPC, COBIL00C |
| CARDFILE (Card) | CARDFILE.jcl | CVACT02Y (150 bytes) | CARD-NUM | CBACT02C, CBEXPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC |
| CUSTFILE (Customer) | CUSTFILE.jcl | CVCUS01Y (500 bytes) | CUST-ID | CBCUS01C, CBEXPORT, CBTRN01C, COACTVWC, COACTUPC | — |
| XREFFILE (Cross-Ref) | XREFFILE.jcl | CVACT03Y (50 bytes) | XREF-CARD-NUM | CBACT03C, CBACT04C, CBEXPORT, CBTRN01C, CBTRN02C, COTRN02C | — |
| TRANSACT (Transaction) | TRANFILE.jcl | CVTRA05Y (350 bytes) | TRAN-ID | CBEXPORT, CBTRN03C, COTRN00C, COTRN01C, COTRN02C, COBIL00C | CBACT04C, CBTRN02C, COTRN02C, COBIL00C |
| TCATBALF (Category Bal) | TCATBALF.jcl | CVTRA01Y (50 bytes) | TRAN-CAT-KEY | CBACT04C, CBTRN02C | CBACT04C, CBTRN02C |
| DALYTRAN (Daily Trans) | — (input PS) | CVTRA06Y (350 bytes) | — | CBTRN01C, CBTRN02C | External feed |
| DALYREJS (Rejects) | DALYREJS.jcl (GDG) | CVTRA06Y (350 bytes) | — | — | CBTRN02C |
| DISCGRP (Disclosure) | DISCGRP.jcl | CVTRA02Y (50 bytes) | DIS-GROUP-KEY | CBACT04C | — |
| USRSEC (User Security) | DUSRSECJ.jcl | CSUSR01Y (80 bytes) | SEC-USR-ID | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C, COUSR02C, COUSR03C |
| TRANTYPE (Tran Types) | TRANTYPE.jcl | CVTRA03Y (60 bytes) | TRAN-TYPE | CBTRN03C | — |
| TRANCATG (Tran Category) | TRANCATG.jcl | CVTRA04Y (60 bytes) | TRAN-CAT-KEY | CBTRN03C | — |

### 2.2 Alternate Index (AIX) Paths

| AIX Path | Base Cluster | Alternate Key | Used By |
|----------|-------------|---------------|---------|
| CARDAIX | CARDFILE | CARD-ACCT-ID | COCRDLIC |
| CXACAIX | XREFFILE | XREF-ACCT-ID | COACTVWC, COACTUPC, COBIL00C, COTRN02C |
| CCXREF | XREFFILE | XREF-CARD-NUM | COTRN02C |
| TRANIDX | TRANSACT | TRAN-CARD-NUM | COTRN00C |

---

## 3. Batch Pipeline Flow

### 3.1 End-to-End Processing Sequence

```mermaid
graph TD
    subgraph Infrastructure
        ACCTFILE_JCL[ACCTFILE.jcl<br/>Define Account VSAM]
        CARDFILE_JCL[CARDFILE.jcl<br/>Define Card VSAM]
        CUSTFILE_JCL[CUSTFILE.jcl<br/>Define Customer VSAM]
        XREFFILE_JCL[XREFFILE.jcl<br/>Define XREF VSAM]
        TRANFILE_JCL[TRANFILE.jcl<br/>Define Transaction VSAM]
        TCATBALF_JCL[TCATBALF.jcl<br/>Define TCATBAL VSAM]
        TRANTYPE_JCL[TRANTYPE.jcl<br/>Define TranType VSAM]
        TRANCATG_JCL[TRANCATG.jcl<br/>Define TranCat VSAM]
        DISCGRP_JCL[DISCGRP.jcl<br/>Define Disclosure VSAM]
    end

    subgraph Daily Batch Cycle
        CLOSEFIL[CLOSEFIL.jcl<br/>Close CICS files]
        POSTTRAN[POSTTRAN.jcl<br/>CBTRN02C: Validate + Post]
        INTCALC[INTCALC.jcl<br/>CBACT04C: Interest Calc]
        COMBTRAN[COMBTRAN.jcl<br/>Combine Transactions]
        TRANREPT[TRANREPT.jcl<br/>CBTRN03C: Report]
        CREASTMT[CREASTMT.JCL<br/>CBSTM03A: Statements]
        TRANBKP[TRANBKP.jcl<br/>Backup Transactions]
        OPENFIL[OPENFIL.jcl<br/>Open CICS files]
    end

    subgraph Data Migration
        CBEXPORT_JCL[CBEXPORT.jcl<br/>Export Branch Data]
        CBIMPORT_JCL[CBIMPORT.jcl<br/>Import Branch Data]
    end

    subgraph Reporting
        READACCT[READACCT.jcl<br/>CBACT01C: Print Accounts]
        READCARD[READCARD.jcl<br/>CBACT02C: Print Cards]
        READCUST[READCUST.jcl<br/>CBCUS01C: Print Customers]
        READXREF[READXREF.jcl<br/>CBACT03C: Print XREF]
        PRTCATBL[PRTCATBL.jcl<br/>Print Category Balances]
    end

    CLOSEFIL --> POSTTRAN
    POSTTRAN --> INTCALC
    INTCALC --> COMBTRAN
    COMBTRAN --> TRANREPT
    TRANREPT --> CREASTMT
    CREASTMT --> TRANBKP
    TRANBKP --> OPENFIL
```

### 3.2 Daily Batch Cycle — Detailed Flow

1. **CLOSEFIL.jcl** — Closes CICS-managed VSAM files (via SDSF commands) to allow exclusive batch access.

2. **POSTTRAN.jcl** (CBTRN02C) — Core transaction posting:
   - Reads DALYTRAN (daily input transactions)
   - Validates: card expiration, overlimit check, cross-reference lookup
   - Posts valid transactions to TRANSACT
   - Updates ACCTFILE (balances) and TCATBALF (category balances)
   - Writes rejected transactions to DALYREJS (GDG)

3. **INTCALC.jcl** (CBACT04C) — Interest calculation:
   - Reads TCATBALF for category balances per account
   - Looks up interest rates from DISCGRP via account group (XREFFILE)
   - Computes interest and writes interest transactions to TRANSACT
   - Updates ACCTFILE with accrued interest

4. **COMBTRAN.jcl** — Sorts and merges transaction records from multiple sources.

5. **TRANREPT.jcl** (CBTRN03C) — Transaction detail report:
   - Reads TRANFILE (sorted), CARDXREF, TRANTYPE, TRANCATG, DATEPARM
   - Produces formatted TRANREPT output

6. **CREASTMT.JCL** (CBSTM03A) — Account statement generation:
   - Produces plain text and HTML statement formats

7. **TRANBKP.jcl** — Backs up transaction master to GDG and purges processed records.

8. **OPENFIL.jcl** — Reopens CICS-managed VSAM files for online access.

### 3.3 Data Migration Pipeline

```mermaid
graph LR
    subgraph Source System
        SRC_CUST[CUSTFILE]
        SRC_ACCT[ACCTFILE]
        SRC_XREF[XREFFILE]
        SRC_TRAN[TRANSACT]
        SRC_CARD[CARDFILE]
    end

    CBEXPORT[CBEXPORT<br/>Export Program]
    EXPFILE[EXPFILE<br/>Sequential Export]
    CBIMPORT[CBIMPORT<br/>Import Program]

    subgraph Target System
        TGT_CUST[CUSTOUT]
        TGT_ACCT[ACCTOUT]
        TGT_XREF[XREFOUT]
        TGT_TRAN[TRNXOUT]
        TGT_CARD[CARDOUT]
        TGT_ERR[ERROUT]
    end

    SRC_CUST --> CBEXPORT
    SRC_ACCT --> CBEXPORT
    SRC_XREF --> CBEXPORT
    SRC_TRAN --> CBEXPORT
    SRC_CARD --> CBEXPORT
    CBEXPORT --> EXPFILE
    EXPFILE --> CBIMPORT
    CBIMPORT --> TGT_CUST
    CBIMPORT --> TGT_ACCT
    CBIMPORT --> TGT_XREF
    CBIMPORT --> TGT_TRAN
    CBIMPORT --> TGT_CARD
    CBIMPORT --> TGT_ERR
```

---

## 4. Online-to-Batch Integration Points

| Integration Mechanism | Online Program | Batch Program | Description |
|-----------------------|---------------|---------------|-------------|
| TDQ (Transient Data Queue) | CORPT00C | CBTRN03C | Online submits report request; batch generates report |
| Shared VSAM (TRANSACT) | COTRN02C, COBIL00C | CBTRN02C, CBTRN03C | Online adds transactions; batch validates and reports |
| Shared VSAM (ACCTFILE) | COACTUPC, COBIL00C | CBTRN02C, CBACT04C | Online updates accounts; batch posts and calculates |
| CICS File Control | OPENFIL/CLOSEFIL | All batch | Manages file exclusivity between online and batch |
