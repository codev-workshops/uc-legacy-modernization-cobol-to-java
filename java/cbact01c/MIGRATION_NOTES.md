# CBACT01C → Java 17: migration notes

This module is a Java 17 rewrite of the CardDemo batch program `CBACT01C` (job `READACCT`).
It reads the account master and writes three extract files. The Java version is checked
against the **unmodified COBOL program**, compiled and run with GnuCOBOL 3.1 as a test oracle.

```bash
cd java/cbact01c
mvn -B verify                                   # build + 63 JUnit tests
java -jar target/cbact01c-1.0.0-SNAPSHOT.jar \
  ../../app/data/ASCII/acctdata.txt OUTFILE ARRYFILE VBRCFILE   # run the job
src/test/cobol/regenerate-golden.sh             # re-run the COBOL oracle (needs gnucobol3)
```

## 1. What the COBOL program does

| Item | Source |
|---|---|
| Program | `app/cbl/CBACT01C.cbl` (batch, no CICS, no DB2) |
| Copybooks | `CVACT01Y` (account record, 300 bytes), `CODATECN` (date conversion parameters) |
| Called routines | `COBDATFT` (assembler, `app/asm/COBDATFT.asm`), `CEE3ABD` (LE abend service) |
| JCL | `app/jcl/READACCT.jcl` step `STEP05` |

| DD | Dataset | Organisation | Use |
|---|---|---|---|
| `ACCTFILE` | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | KSDS, key `ACCT-ID` (11), LRECL 300 | input, read sequentially |
| `OUTFILE` | `…ACCTDATA.PSCOMP` | `RECFM=FB, LRECL=107` | output |
| `ARRYFILE` | `…ACCTDATA.ARRYPS` | `RECFM=FB, LRECL=110` | output |
| `VBRCFILE` | `…ACCTDATA.VBPS` | `RECFM=VB, LRECL=84` (data 10–80) | output |

For every account, in key order, the program:

1. **`OUTFILE`** (`OUT-ACCT-REC`): copies id, status, balance, credit limits, open and
   expiry dates and cycle credit. The reissue date goes through `COBDATFT` and is written as
   `YYYYMMDD` plus two spaces. The cycle debit becomes **COMP-3**: if the account's
   `ACCT-CURR-CYC-DEBIT` is zero the program moves **2525.00**; otherwise it moves nothing
   (see D-1). `ACCT-ADDR-ZIP` and the 178-byte filler are dropped.
2. **`ARRYFILE`** (`ARR-ARRAY-REC`, `OCCURS 5`): after `INITIALIZE`, entries 1 and 2 hold the
   current balance with fixed COMP-3 debits 1005.00 and 1525.00; entry 3 holds constants
   −1025.00 / −2500.00; entries 4–5 stay zero; the 4-byte filler is spaces.
3. **`VBRCFILE`**: two variable-length records: `VBRC-REC1` (12 bytes: id + status) and
   `VBRC-REC2` (39 bytes: id, balance, credit limit, reissue year).
4. **SYSOUT**: `DISPLAY`s every field, both VB records and the whole 300-byte record.

Errors: an open failure on any file, a read status other than `00`/`10`, or a write status
other than `00`/`10` prints `FILE STATUS IS: NNNNxxxx` (paragraph `9910`) and calls
`CEE3ABD` with code 999. Status `10` ends the loop. Only `ACCTFILE` is closed explicitly; the
outputs are closed implicitly at `GOBACK`.

The program has no business calculation; it is a format-conversion/extract job. The constants
(2525.00, 1005.00, 1525.00, −1025.00, −2500.00) look like test values written into the extract
to exercise COMP-3 and OCCURS handling downstream.

## 2. Java design

Package `com.carddemo.batch.cbact01c`, plain Java 17 (no framework; the job is a single
sequential pass, Spring Batch adds nothing here yet).

| COBOL | Java |
|---|---|
| `ACCOUNT-RECORD` (`CVACT01Y`) | `record Account` + `Account.parse` |
| `PIC S9(10)V99` DISPLAY | `record SignedAmount(BigDecimal value, boolean negative)` + `ZonedDecimal` codec |
| `PIC S9(10)V99 COMP-3` | `BigDecimal` + `PackedDecimal` codec |
| `OUT-ACCT-REC` | `record AccountOutputRecord` (`toBytes()` → 107 bytes) |
| `OUT-ACCT-CURR-CYC-DEBIT` storage | `sealed interface CycleDebit` (`Value` / `LowValues`) |
| `ARR-ARRAY-REC`, `OCCURS 5` | `record AccountArrayRecord(String, List<Balance>)` |
| `VBRC-REC1` / `VBRC-REC2` | `sealed interface VariableRecord` (`StatusRecord`, `BalanceRecord`) |
| `COBDATFT` + `CODATECN` | `DateFormatConverter.convert` returning a `Result` record |
| `9910-DISPLAY-IO-STATUS` | `FileStatus.format` |
| `9999-ABEND-PROGRAM` / `CEE3ABD` | `AbendException` (`U0999`); CLI exit status 16 |
| KSDS sequential read | `AccountFile` (loads the unload, returns records in key order) |
| FB / VB `WRITE` | `FixedRecordWriter`, `VariableRecordWriter` + `RdwFormat` |
| `PROCEDURE DIVISION` | `AccountExtractJob.run(Datasets)` |
| `EXEC PGM=CBACT01C` + DD cards | `Cbact01cApplication` (4 path arguments) |

Idioms used: records for every record layout, sealed interfaces for the variant records and
the cycle-debit storage, `BigDecimal` for all amounts (never `double`), try-with-resources for
the three outputs, immutable `List.copyOf`, and no shared mutable state except the two pieces
of COBOL working storage the program really depends on (D-1, D-3).

## 3. Translation decisions

| # | Decision | Why |
|---|---|---|
| D-1 | **Cycle debit carry-forward kept by default** (`--cycle-debit=legacy`). For a non-zero debit the COBOL program never moves a value, so the field keeps the previous record's value, or low-values (`X'00'×7`, not valid COMP-3) for the first record. Java keeps that state in `CycleDebit`. `--cycle-debit=copy-source` writes the real debit instead. | Byte-identical output is the acceptance test. This is almost certainly a defect (a missing `ELSE MOVE ACCT-CURR-CYC-DEBIT`). It doesn't show on the sample data because all 50 debits are zero; the edge data set covers it. Switching the default is a business decision. |
| D-2 | **Negative zero is preserved.** `SignedAmount` carries the sign separately because `BigDecimal` has no −0, and the oracle copies `00000000000}` unchanged. `isZero()` treats −0 as zero, as COBOL does. | Byte parity on the edge data. |
| D-3 | **`COBDATFT` ported one-for-one**, including that it only moves characters (no date validation: `XXXX/YY/ZZ` → `XXXXYYZZ`) and only writes the first 8 bytes of the 20-byte output buffer, so the rest keeps its earlier contents (spaces). Output type `'2'` = `YYYYMMDD` is a valid combination for input type `'2'`. | The output field is `X(10)`, so bytes 9–10 come from that untouched buffer. |
| D-4 | **`VBRCFILE` record prefix is configurable.** Default `--rdw=zos`: 4-byte RDW whose length includes the RDW (16 and 43), as in a binary transfer of a VB dataset. `--rdw=gnucobol`: length excludes the prefix (12 and 39), GnuCOBOL's default format, used for the byte-for-byte parity test. | The logical records are identical in both; only the framing differs by platform. |
| D-5 | **Input is the ASCII KSDS unload** (one 300-byte line per record, like `app/data/ASCII/acctdata.txt`). Records are sorted by key, short lines are padded with spaces, a line over 300 bytes or a duplicate key fails the open with status `39`. | A KSDS cannot hold either; failing loudly is safer than guessing. |
| D-6 | **Invalid numeric data abends** with `AbendException("S0C7")`. COBOL may copy bad bytes through, or fail with a data exception, depending on compiler options. | The rewrite should not carry undefined behaviour forward silently. Not seen in the sample data. |
| D-7 | **Abend → exception, CLI exit status 16.** The SYSOUT lines before the abend are identical to COBOL's; the process exit status cannot match z/OS (`U0999`), and GnuCOBOL reports 231 (999 mod 256). | A scheduler can map 16 to "job failed". |
| D-8 | **I/O status mapping**: input not found → `35`; output directory missing → `30`; output is a directory / access denied → `37`; other I/O errors → `30`. These match what GnuCOBOL reports for the same failures. | Keeps the `ERROR OPENING …`/`FILE STATUS IS:` messages operators know. |
| D-9 | **All outputs closed explicitly**, where COBOL closes only `ACCTFILE` and relies on implicit close. | Same file contents; no runtime warnings. |
| D-10 | **SYSOUT reproduced in z/OS form**: numeric fields are shown as their stored bytes (`00000001940{`), the way Enterprise COBOL `DISPLAY`s a signed zoned field. | Operators' log parsers see the same text. |
| D-12 | **Decimal values: exact, scale-insensitive, never silently truncated.** Every amount, display and COMP-3 alike, is a `SignedAmount` (`BigDecimal` plus an explicit sign flag, so −0 survives into both zoned `}` and packed `D` forms). Equality ignores scale (`2525` equals `2525.00`). Encoding uses `RoundingMode.UNNECESSARY` and a digit-count check, so a value with too many decimals or integer digits throws `ArithmeticException` instead of being cut down the way a COBOL `MOVE` would. | Every `MOVE` in CBACT01C is between identical pictures, or of a constant that fits, so COBOL never truncates here; an overflow in Java can only be a bug and should fail loudly. Scale-sensitive `BigDecimal.equals` would make `0` ≠ `0.00` in record comparisons. |
| D-11 | **Typos kept where they are visible** (`ACCT-EXPIRAION-DATE` label in SYSOUT). Java names use the corrected spelling (`expirationDate`). | Output parity; readable code. |

## 4. How "identical to COBOL" is tested

`src/test/cobol/` holds what is needed to run the **unmodified** `CBACT01C.cbl` under
GnuCOBOL (`cobc -std=ibm -fsign=EBCDIC`, so signs use the EBCDIC overpunch convention of the
ASCII data files):

* `COBDATFT.cbl`: COBOL translation of the HLASM routine, instruction by instruction.
* `CEE3ABD.cbl`: ends the run with the abend code as return code.
* `LOADKSDS.cbl`: loads the ASCII unload into an indexed file (the `IDCAMS REPRO` step).
* `run-oracle.sh`, `regenerate-golden.sh`: compile, load, run; outputs go to
  `src/test/resources/golden/<scenario>/{OUTFILE,ARRYFILE,VBRCFILE,SYSOUT,RC}`.

Scenarios:

| Scenario | Input | Accounts | What it covers |
|---|---|---|---|
| `sample` | `app/data/ASCII/acctdata.txt` | 50 | the shipped sample data |
| `edge` | `src/test/resources/input/acctdata-edge.txt` | 7 | unsorted keys, negative amounts, −0, max values, non-zero debit as first and later record (D-1), non-ISO reissue dates (D-3), blank fields |
| `empty` | empty file | 0 | immediate end of file |
| `missing-input` | no `ACCTFILE` | – | open error, status 35, abend 999 |

Tests:

| Test class | Checks |
|---|---|
| `CobolParityTest` | per scenario: `OUTFILE` and `VBRCFILE` **byte-identical**; `ARRYFILE` identical except the 2 documented sign bytes; SYSOUT identical after the documented display mapping; record counts; z/OS RDW variant has the same logical records; missing input abends identically |
| `LiveCobolOracleTest` | recompiles and reruns the COBOL program and checks the committed golden files are still what it produces (skipped if `cobc` is absent; CI installs it) |
| `AccountExtractJobTest` | each business rule on hand-built records: 2525.00 rule, carry-forward, `COPY_SOURCE`, array constants, key order, reissue date, RDWs, abends |
| `CodecTest`, `DateFormatConverterTest`, `FileStatusTest`, `Cbact01cApplicationTest` | codecs, every `COBDATFT` path, status formatting, CLI |

### Platform differences in the oracle

GnuCOBOL is not Enterprise COBOL. Two differences show up and the tests map them explicitly
(`OracleOutput`) instead of hiding them:

1. **`INITIALIZE` of a signed DISPLAY field**: GnuCOBOL writes an unsigned zero (`'0'`) into
   `ARR-ACCT-CURR-BAL(4)` and `(5)`; Enterprise COBOL stores the preferred positive sign
   (`X'C0'`, `'{'` in the ASCII rendition). Java writes `'{'`. The test asserts these are the
   **only** differing bytes (2 per record).
2. **`DISPLAY` of a signed field**: GnuCOBOL prints `000000019400+`; Enterprise COBOL prints
   the stored bytes `00000001940{`. Java prints the z/OS form.

Both z/OS behaviours come from IBM's documented semantics, **not from a run on a mainframe**.
Confirm them in the first parallel run on z/OS (see the migration test harness and
`docs/migration-blueprint/CUTOVER_PLAN.md`).

## 5. Open points

* **D-1 default**: keep the carry-forward (parity) or fix it (`COPY_SOURCE`)? Downstream
  consumers of `ACCTDATA.PSCOMP` should be checked first.
* **Unsigned (`F`-zone) digits in signed fields**: not in the sample data. Java treats them as
  positive and writes the preferred `C` sign. GnuCOBOL rewrites the *source* field's sign as a
  side effect, so it is not a usable oracle here; check on z/OS if such data exists.
* **`COBDATFT` error text**: `MVC COERMSG,=C'INVALID INPUT'` copies 38 bytes from a 13-byte
  literal, so on z/OS the last 25 bytes are whatever follows the literal. Java pads with spaces.
  CBACT01C never takes this path.
* **EBCDIC input**: the Java job reads the ASCII unload. Reading the EBCDIC KSDS directly
  would reuse the cp037 decoding already in the migration test harness.
