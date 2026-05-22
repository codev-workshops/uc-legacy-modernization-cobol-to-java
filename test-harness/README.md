# CardDemo COBOL-to-Java Test Harness

Validates the Java rewrite of COBOL batch programs by comparing Java output files
against known-good COBOL output, field by field, using the exact record layouts
derived from the COBOL copybooks and FD sections.

## Quick Start

### Run unit tests

```bash
cd test-harness
mvn test
```

### Run the comparison harness

```bash
cd test-harness
mvn exec:java \
  -Dexec.mainClass=com.carddemo.harness.Main \
  -Dexec.args="--cobol-dir=<path-to-cobol-output> --java-dir=<path-to-java-output>"
```

Optionally supply a tolerance configuration file:

```bash
mvn exec:java \
  -Dexec.mainClass=com.carddemo.harness.Main \
  -Dexec.args="--cobol-dir=<path> --java-dir=<path> --tolerance=tolerance.properties"
```

## Project Structure

```
test-harness/
├── pom.xml
├── README.md
├── src/main/java/com/carddemo/harness/
│   ├── Main.java                          # CLI entry point
│   ├── config/
│   │   └── ToleranceConfig.java           # Tolerance rules
│   ├── parser/
│   │   ├── FieldDefinition.java           # COBOL field descriptor
│   │   └── RecordLayout.java              # Hard-coded + JSON layouts
│   ├── codec/
│   │   ├── ZonedDecimalCodec.java         # ASCII zoned decimal (overpunch)
│   │   └── PackedDecimalCodec.java        # COMP-3 packed decimal
│   ├── comparator/
│   │   ├── CompareResult.java             # MATCH / MISMATCH / SKIPPED
│   │   ├── FieldComparator.java           # Type-aware field comparison
│   │   └── RecordComparator.java          # Full file comparison orchestrator
│   ├── edgecase/
│   │   └── EdgeCaseHandler.java           # CBACT01C-specific edge cases
│   ├── validation/
│   │   ├── ValidationResult.java          # Pass/fail result
│   │   └── BusinessValidator.java         # CBTRN02C/CBACT04C business rules
│   └── report/
│       ├── FieldComparisonDetail.java     # Single field comparison
│       └── ComparisonReport.java          # Full comparison report generator
├── src/main/resources/
│   ├── tolerance.properties               # Default tolerance settings
│   └── layouts/                           # JSON layout definitions
│       ├── outfile-layout.json
│       ├── arryfile-layout.json
│       ├── vbrcfile-layout.json
│       ├── acctdata-layout.json
│       └── tranfile-layout.json
└── src/test/java/com/carddemo/harness/
    ├── ZonedDecimalCodecTest.java
    ├── PackedDecimalCodecTest.java
    ├── EdgeCaseHandlerTest.java
    ├── BusinessValidatorTest.java
    └── RecordComparatorTest.java
```

## Tolerance Configuration

| Property             | Default | Description                                             |
|----------------------|---------|---------------------------------------------------------|
| `numeric.tolerance`  | `0.00`  | Max allowed difference for numeric fields               |
| `rtrim.alphanumeric` | `true`  | Right-trim PIC X fields before comparison               |
| `ignore.filler`      | `true`  | Skip FILLER bytes entirely                              |
| `strip.rdw`          | `true`  | Strip 4-byte RDW prefix on variable-length records      |
| `normalize.dates`    | `true`  | Normalize dates to YYYYMMDD before comparison           |

## Record Layouts

All layouts are hard-coded in `RecordLayout.java` and also available as JSON in
`src/main/resources/layouts/` for external tooling. Source copybooks:

| Layout           | Source                         | Record Length |
|------------------|--------------------------------|--------------|
| OUTFILE          | `app/cbl/CBACT01C.cbl:57-69`  | 107 bytes    |
| ARRY-FILE        | `app/cbl/CBACT01C.cbl:72-78`  | 110 bytes    |
| VBRC-FILE (REC1) | `app/cbl/CBACT01C.cbl:123-126`| 12 bytes     |
| VBRC-FILE (REC2) | `app/cbl/CBACT01C.cbl:127-130`| 39 bytes     |
| ACCOUNT-RECORD   | `app/cpy/CVACT01Y.cpy`        | 300 bytes    |
| TRAN-RECORD      | `app/cpy/CVTRA05Y.cpy`        | 350 bytes    |

## Edge Cases

These are documented in `EdgeCaseHandler.java` with references to the exact COBOL source lines:

1. **Zero debit substitution** (`CBACT01C.cbl:236-238`): When `ACCT-CURR-CYC-DEBIT` is zero,
   COBOL writes 2525.00 to the OUTFILE.

2. **Date truncation** (`CBACT01C.cbl:223-233`): `COBDATFT` outputs a 20-byte date, but only
   the first 8 characters (YYYYMMDD) are meaningful. The remaining bytes may contain garbage.

3. **Unpopulated array slots** (`CBACT01C.cbl:253-261`): Only array indices 1-3 are populated
   in paragraph `1400-POPUL-ARRAY-RECORD`. Indices 4 and 5 remain at INITIALIZE values (zeros).

4. **Hardcoded array values** (`CBACT01C.cbl:256-260`):
   - `ARR-ACCT-CURR-CYC-DEBIT(1)` = 1005.00
   - `ARR-ACCT-CURR-CYC-DEBIT(2)` = 1525.00
   - `ARR-ACCT-CURR-BAL(3)` = -1025.00
   - `ARR-ACCT-CURR-CYC-DEBIT(3)` = -2500.00

## Business Validation Rules

Implemented in `BusinessValidator.java`, derived from CBTRN02C and CBACT04C:

| Rule | Description                                        | Source                          |
|------|----------------------------------------------------|---------------------------------|
| 1    | Record count: input = processed + rejected         | `CBTRN02C.cbl:184-186,206-216` |
| 2    | Balance: new = old + SUM(posted amounts)           | `CBTRN02C.cbl:545-552`         |
| 3    | Cycle split: credit/debit match posted amounts     | `CBTRN02C.cbl:548-551`         |
| 4    | Category balance = SUM(matching trans amounts)     | `CBTRN02C.cbl:467-508`         |
| 5    | Credit limit: reject if overlimit (reason 102)     | `CBTRN02C.cbl:393-421`         |
| 6    | Expiration: reject if tran date > expiry (reason 103) | `CBTRN02C.cbl:414-420`      |
| 7    | Post-interest: cycle fields reset to zero          | `CBACT04C.cbl:350-356`         |

## Codec Details

### Zoned Decimal (DISPLAY format, ASCII)

The sign is encoded in the last byte using ASCII overpunch:

| Character | Sign | Digit |
|-----------|------|-------|
| `{`       | +    | 0     |
| `A`–`I`   | +    | 1–9   |
| `}`       | –    | 0     |
| `J`–`R`   | –    | 1–9   |

### Packed Decimal (COMP-3)

Each byte holds two BCD digits. The last nibble is the sign:
- `0x0C` = positive
- `0x0D` = negative
- `0x0F` = unsigned

Example: PIC S9(10)V99 COMP-3 = (12 + 1) / 2 = 7 bytes.

## Dependencies

- Java 17+
- JUnit 5 (test only)
- SLF4J (logging)
- Jackson (JSON layout parsing)
- No external COBOL libraries — all codecs are hand-rolled from the PIC clauses
