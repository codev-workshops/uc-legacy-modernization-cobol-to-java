# CBTRN01C — Daily Transaction Validator (Spring Batch)

Java 17 + Spring Boot 3 + Spring Batch rewrite of the COBOL batch program `CBTRN01C.cbl`.

## What It Does

CBTRN01C **pre-validates daily card transactions** before they are posted to accounts. For each transaction in the daily file it:

1. Reads the transaction sequentially from DALYTRAN (350-byte fixed-width records)
2. Looks up the card number in the cross-reference file (XREFFILE, 50-byte indexed records)
3. If the XREF lookup succeeds, reads the associated account from ACCTFILE (300-byte indexed records)
4. Logs success or failure for each transaction — does NOT update balances or write rejects

This is a **read-only validation pass**. The actual posting (balance updates, category balance tracking, reject file writing) happens in CBTRN02C.

## COBOL-to-Java Paragraph Mapping

| COBOL Paragraph | Java Equivalent |
|----------------|-----------------|
| `MAIN-PARA` (lines 155-197) | `Cbtrn01cApplication` + Spring Batch Job |
| `1000-DALYTRAN-GET-NEXT` (lines 202-225) | `DailyTransactionReader` (FlatFileItemReader) |
| `2000-LOOKUP-XREF` (lines 227-239) | `TransactionValidationProcessor` → `XrefRepository.findByCardNum()` |
| `3000-READ-ACCOUNT` (lines 241-250) | `TransactionValidationProcessor` → `AccountRepository.findByAcctId()` |
| `0000-DALYTRAN-OPEN` … `0500-TRANFILE-OPEN` | Spring Batch resource lifecycle |
| `9000-DALYTRAN-CLOSE` … `9500-TRANFILE-CLOSE` | Spring Batch resource lifecycle |
| `Z-ABEND-PROGRAM` (CEE3ABD, exit 999) | `System.exit(999)` on job failure |

## How to Run

```bash
cd cbtrn01c-java
mvn clean package -DskipTests

java -jar target/cbtrn01c-java-1.0.0-SNAPSHOT.jar \
    --app.files.dalytran=/path/to/dailytran.txt \
    --app.files.xreffile=/path/to/cardxref.txt \
    --app.files.acctfile=/path/to/acctdata.txt
```

Or via environment variables:
```bash
DALYTRAN_FILE=/path/to/dailytran.txt \
XREF_FILE=/path/to/cardxref.txt \
ACCT_FILE=/path/to/acctdata.txt \
java -jar target/cbtrn01c-java-1.0.0-SNAPSHOT.jar
```

## How to Test

```bash
cd cbtrn01c-java
mvn clean test
```

Tests include:
- **Unit tests**: `DailyTransactionTest`, `CardXrefRecordTest`, `TransactionValidationProcessorTest`
- **Integration tests**: `GoldenFileIntegrationTest` (runs full job with 300 transactions from golden-file JSON), `Cbtrn01cApplicationTest` (Spring context load)

## Architecture

```
┌─────────────────┐    ┌─────────────────────────┐    ┌───────────────┐
│ DailyTransaction│───>│ TransactionValidation    │───>│ ItemWriter    │
│ Reader          │    │ Processor                │    │ (log only)    │
│ (DALYTRAN)      │    │                          │    │               │
└─────────────────┘    │  ┌─────────────────────┐ │    └───────────────┘
                       │  │ XrefRepository      │ │
                       │  │ (pre-loaded Map)     │ │
                       │  └─────────────────────┘ │
                       │  ┌─────────────────────┐ │
                       │  │ AccountRepository   │ │
                       │  │ (pre-loaded Map)     │ │
                       │  └─────────────────────┘ │
                       └─────────────────────────┘
```

## Key Differences from cbact01c-java

| Aspect | cbact01c-java | cbtrn01c-java |
|--------|---------------|---------------|
| Framework | Plain Java 17 | Spring Boot 3 + Spring Batch |
| Processing | Single-file sequential read | Multi-file cross-lookup |
| I/O pattern | Sequential read → write | Sequential read + random-access lookups |
| State | Stateless | Pre-loaded lookup maps (simulate VSAM indexed access) |
| Restartability | None | Spring Batch built-in (chunk-based, skip/retry) |
| Chunk processing | N/A | Configurable chunk size (default 10) |

## Stepping Stone to CBTRN02C

This module validates but does not modify data. The CBTRN02C migration will extend this with:
- **Balance updates**: Update `ACCT-CURR-BAL`, `ACCT-CURR-CYC-DEBIT`/`CREDIT` after validation
- **Category balance tracking**: Update `TCATBAL` file with per-category transaction totals
- **Reject file writing**: Write transactions that fail validation to a reject file
- **Transaction file writing**: Post validated transactions to the TRANSACT indexed file

The `TransactionValidationProcessor` is designed to be extended — add an `ItemWriter` that performs the writes, or chain a second processor step.

## Record Layouts

### DALYTRAN (CVTRA06Y.cpy, 350 bytes)
| Field | PIC | Offset | Length |
|-------|-----|--------|--------|
| DALYTRAN-ID | X(16) | 0 | 16 |
| DALYTRAN-TYPE-CD | X(02) | 16 | 2 |
| DALYTRAN-CAT-CD | 9(04) | 18 | 4 |
| DALYTRAN-SOURCE | X(10) | 22 | 10 |
| DALYTRAN-DESC | X(100) | 32 | 100 |
| DALYTRAN-AMT | S9(09)V99 | 132 | 11 |
| DALYTRAN-MERCHANT-ID | 9(09) | 143 | 9 |
| DALYTRAN-MERCHANT-NAME | X(50) | 152 | 50 |
| DALYTRAN-MERCHANT-CITY | X(50) | 202 | 50 |
| DALYTRAN-MERCHANT-ZIP | X(10) | 252 | 10 |
| DALYTRAN-CARD-NUM | X(16) | 262 | 16 |
| DALYTRAN-ORIG-TS | X(26) | 278 | 26 |
| DALYTRAN-PROC-TS | X(26) | 304 | 26 |
| FILLER | X(20) | 330 | 20 |

### XREFFILE (CVACT03Y.cpy, 50 bytes)
| Field | PIC | Offset | Length |
|-------|-----|--------|--------|
| XREF-CARD-NUM | X(16) | 0 | 16 |
| XREF-CUST-ID | 9(09) | 16 | 9 |
| XREF-ACCT-ID | 9(11) | 25 | 11 |
| FILLER | X(14) | 36 | 14 |

### ACCTFILE (CVACT01Y.cpy, 300 bytes)
| Field | PIC | Offset | Length |
|-------|-----|--------|--------|
| ACCT-ID | 9(11) | 0 | 11 |
| ACCT-ACTIVE-STATUS | X(01) | 11 | 1 |
| ACCT-CURR-BAL | S9(10)V99 | 12 | 12 |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 24 | 12 |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 36 | 12 |
| ACCT-OPEN-DATE | X(10) | 48 | 10 |
| ACCT-EXPIRAION-DATE | X(10) | 58 | 10 |
| ACCT-REISSUE-DATE | X(10) | 68 | 10 |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 78 | 12 |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 90 | 12 |
| ACCT-ADDR-ZIP | X(10) | 102 | 10 |
| ACCT-GROUP-ID | X(10) | 112 | 10 |
| FILLER | X(178) | 122 | 178 |
