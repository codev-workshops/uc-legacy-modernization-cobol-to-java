# CBACT01C → Java 17 Migration Design (shared contracts)

Scope: rewrite the batch program `app/cbl/CBACT01C.cbl` as a Java 17 Maven
project that produces the three output datasets byte-for-byte identical to the
mainframe program. This document is the single source of truth for the package
layout, record layouts, codec interfaces and behavioural assumptions that all
implementation work must follow.

Source artifacts analysed:

| Artifact | Role |
|---|---|
| `app/cbl/CBACT01C.cbl` | the program (PROCEDURE DIVISION is the spec for `Cbact01c`) |
| `app/cpy/CVACT01Y.cpy` | `ACCOUNT-RECORD`, 300 bytes (input master record) |
| `app/cpy/CODATECN.cpy` | request/response area for the date routine |
| `app/asm/COBDATFT.asm` | date-formatting routine (`CALL 'COBDATFT' USING CODATECN-REC`) |
| `app/jcl/READACCT.jcl` | DDs: `OUTFILE` LRECL=107 FB, `ARRYFILE` LRECL=110 FB, `VBRCFILE` LRECL=84 RECFM=VB |
| `app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS` | 50 EBCDIC account records × 300 bytes (no terminators) |
| `app/data/ASCII/acctdata.txt` | same 50 records, EBCDIC→ASCII translated, 300 chars + `\n` per line |

No COBOL-generated OUTFILE / ARRYFILE / VBRCFILE exists in the repo. Expected
fixtures are therefore *derived* by hand-computing the documented logic and must
be labelled as such (see §9).

---

## 1. Project layout

Single Maven module (Java 17, JUnit 5) under `java/`:

```
java/
  pom.xml                       groupId com.carddemo.batch, artifactId cbact01c
  src/main/java/com/carddemo/batch/cbact01c/
      Cbact01c.java             main(): CLI + PROCEDURE DIVISION flow (Child 3)
      AccountProcessor.java     per-record mapping 1300/1400/1500 (Child 3)
      IoStatus.java             file-status codes + 9910/9999 semantics (Child 3)
      model/
          AccountRecord.java    CVACT01Y                                  (Child 1)
          OutAcctRec.java       OUT-ACCT-REC (107)                        (Child 1)
          ArrArrayRec.java      ARR-ARRAY-REC (110), ArrAcctBal x5        (Child 1)
          Vb1Rec.java           VBRC-REC1 (12)                            (Child 1)
          Vb2Rec.java           VBRC-REC2 (39)                            (Child 1)
      codec/
          CobolCharset.java     EBCDIC (IBM037) / ASCII selection         (Child 1)
          ZonedDecimalCodec.java  PIC S9(n)V99 / PIC 9(n) DISPLAY         (Child 1)
          PackedDecimalCodec.java PIC S9(n)V99 COMP-3                     (Child 1)
          FixedRecordReader.java  FB reader (300-byte input)              (Child 1)
          FixedRecordWriter.java  FB writer (107 / 110)                   (Child 1)
          VariableRecordWriter.java RECFM=VB writer (RDW)                 (Child 1)
      date/
          DateFormatter.java    interface (this doc, §6)                  (Child 2)
          CobdatftDateFormatter.java  port of COBDATFT.asm                (Child 2)
          DateConversionRequest/Response (records, §6)                    (Child 2)
  src/test/java/com/carddemo/batch/cbact01c/...   unit tests per component
  src/test/resources/golden/                        derived expected fixtures (Child 4)
```

Build/test from `java/`: `mvn -B test`. No dependencies beyond JUnit 5
(`org.junit.jupiter:junit-jupiter:5.10.x`) and `maven-surefire-plugin` 3.x.
Run: `java -jar target/cbact01c.jar --acctfile <in> --outfile <o1> --arryfile <o2> --vbrcfile <o3> [--charset EBCDIC|ASCII] [--display]`.

---

## 2. Character set & comparison mode

* **Canonical mode is EBCDIC** (`java.nio.charset.Charset.forName("IBM037")`).
  Input `AWS.M2.CARDDEMO.ACCTDATA.PS`, all three outputs are raw mainframe
  bytes: EBCDIC text, zoned overpunch signs, COMP-3 packed bytes, binary RDWs.
  **Golden-master equivalence is asserted byte-for-byte on EBCDIC-mode output.**
* **ASCII mode** exists for readability/debugging (`app/data/ASCII/acctdata.txt`).
  Text fields are ASCII; zoned sign overpunch uses the ASCII rendering of the
  EBCDIC zone (`{`=+0, `A`–`I`=+1..+9, `}`=-0, `J`–`R`=-1..-9), which is exactly
  what `acctdata.txt` contains (`00000001940{`). COMP-3 fields and RDWs are
  still binary. ASCII-mode input records may be terminated by `\n` or `\r\n`;
  ASCII-mode output records are written **without** terminators (same as FB).
* `CobolCharset` enum: `EBCDIC(IBM037)`, `ASCII(US_ASCII)`. Every codec takes a
  `CobolCharset`. Because IBM037 maps `{`→0xC0, `}`→0xD0, `A`→0xC1, `J`→0xD1,
  `-`→0x60, space→0x40, the codecs may operate on chars and transcode.

---

## 3. Record layouts (byte offsets are 0-based)

All numeric DISPLAY fields are `PIC S9(10)V99` (12 bytes) unless noted. All
money values are `java.math.BigDecimal` with `scale()==2`.

### 3.1 `AccountRecord` — CVACT01Y, 300 bytes (input)

| off | len | COBOL | Java field | type |
|---|---|---|---|---|
| 0 | 11 | ACCT-ID PIC 9(11) | `acctId` | `String` (11 digits, unsigned) |
| 11 | 1 | ACCT-ACTIVE-STATUS X(1) | `activeStatus` | `String` |
| 12 | 12 | ACCT-CURR-BAL S9(10)V99 | `currBal` | `BigDecimal` |
| 24 | 12 | ACCT-CREDIT-LIMIT | `creditLimit` | `BigDecimal` |
| 36 | 12 | ACCT-CASH-CREDIT-LIMIT | `cashCreditLimit` | `BigDecimal` |
| 48 | 10 | ACCT-OPEN-DATE X(10) | `openDate` | `String` |
| 58 | 10 | ACCT-EXPIRAION-DATE X(10) | `expirationDate` | `String` |
| 68 | 10 | ACCT-REISSUE-DATE X(10) | `reissueDate` | `String` |
| 78 | 12 | ACCT-CURR-CYC-CREDIT | `currCycCredit` | `BigDecimal` |
| 90 | 12 | ACCT-CURR-CYC-DEBIT | `currCycDebit` | `BigDecimal` |
| 102 | 10 | ACCT-ADDR-ZIP X(10) | `addrZip` | `String` |
| 112 | 10 | ACCT-GROUP-ID X(10) | `groupId` | `String` |
| 122 | 178 | FILLER | `filler` | `byte[]` (kept for DISPLAY fidelity) |

`String` fields keep their fixed width (no trimming).

### 3.2 `OutAcctRec` — OUT-ACCT-REC, 107 bytes (OUTFILE, FB)

| off | len | COBOL | Java field |
|---|---|---|---|
| 0 | 11 | OUT-ACCT-ID 9(11) | `acctId` |
| 11 | 1 | OUT-ACCT-ACTIVE-STATUS | `activeStatus` |
| 12 | 12 | OUT-ACCT-CURR-BAL | `currBal` |
| 24 | 12 | OUT-ACCT-CREDIT-LIMIT | `creditLimit` |
| 36 | 12 | OUT-ACCT-CASH-CREDIT-LIMIT | `cashCreditLimit` |
| 48 | 10 | OUT-ACCT-OPEN-DATE | `openDate` |
| 58 | 10 | OUT-ACCT-EXPIRAION-DATE | `expirationDate` |
| 68 | 10 | OUT-ACCT-REISSUE-DATE | `reissueDate` (reformatted, §6) |
| 78 | 12 | OUT-ACCT-CURR-CYC-CREDIT | `currCycCredit` |
| 90 | 7 | OUT-ACCT-CURR-CYC-DEBIT S9(10)V99 **COMP-3** | `currCycDebit` |
| 97 | 10 | OUT-ACCT-GROUP-ID | `groupId` |

### 3.3 `ArrArrayRec` — ARR-ARRAY-REC, 110 bytes (ARRYFILE, FB)

| off | len | COBOL | Java |
|---|---|---|---|
| 0 | 11 | ARR-ACCT-ID 9(11) | `acctId` |
| 11 | 5×19 | ARR-ACCT-BAL OCCURS 5 | `ArrAcctBal[] bal` (length 5, Java index 0..4 = COBOL 1..5) |
| | 12 | ARR-ACCT-CURR-BAL S9(10)V99 | `ArrAcctBal.currBal` |
| | 7 | ARR-ACCT-CURR-CYC-DEBIT **COMP-3** | `ArrAcctBal.currCycDebit` |
| 106 | 4 | ARR-FILLER X(4) | spaces |

`INITIALIZE ARR-ARRAY-REC` semantics: `acctId="00000000000"`, every
`currBal = 0.00` (zoned **+0**, EBCDIC `F0…F0 C0`), every `currCycDebit = 0.00`
(packed `00 00 00 00 00 00 0C`), filler = 4 spaces. Provide
`ArrArrayRec.initialized()` to produce this state.

### 3.4 `Vb1Rec` — VBRC-REC1, 12 bytes

| off | len | COBOL | Java |
|---|---|---|---|
| 0 | 11 | VB1-ACCT-ID 9(11) | `acctId` |
| 11 | 1 | VB1-ACCT-ACTIVE-STATUS | `activeStatus` |

### 3.5 `Vb2Rec` — VBRC-REC2, 39 bytes

| off | len | COBOL | Java |
|---|---|---|---|
| 0 | 11 | VB2-ACCT-ID 9(11) | `acctId` |
| 11 | 12 | VB2-ACCT-CURR-BAL | `currBal` |
| 23 | 12 | VB2-ACCT-CREDIT-LIMIT | `creditLimit` |
| 35 | 4 | VB2-ACCT-REISSUE-YYYY X(4) | `reissueYyyy` — **first 4 chars of the ORIGINAL ACCT-REISSUE-DATE** |

Each model class exposes `byte[] toBytes(CobolCharset)` and a static
`fromBytes(byte[], CobolCharset)` (input record only needs `fromBytes`, output
records only need `toBytes`, but implementing both eases round-trip tests).

---

## 4. Codec contracts

```java
package com.carddemo.batch.cbact01c.codec;

public enum CobolCharset { EBCDIC, ASCII; public Charset charset(); }

/** PIC 9(n) and PIC S9(n)V(m) USAGE DISPLAY. */
public final class ZonedDecimalCodec {
    /** Unsigned PIC 9(n): digits only, zone F. */
    public static byte[] encodeUnsigned(long value, int digits, CobolCharset cs);
    public static byte[] encodeUnsigned(String digits, CobolCharset cs); // pass-through of an 11-char id
    /** Signed PIC S9(intDigits)V9(scale), sign overpunched in the last byte (C=+, D=-). */
    public static byte[] encodeSigned(BigDecimal value, int intDigits, int scale, CobolCharset cs);
    public static BigDecimal decodeSigned(byte[] src, int off, int intDigits, int scale, CobolCharset cs);
    public static String decodeUnsigned(byte[] src, int off, int len, CobolCharset cs);
}

/** PIC S9(intDigits)V9(scale) COMP-3. byteLength = (intDigits+scale+2)/2. */
public final class PackedDecimalCodec {
    public static byte[] encode(BigDecimal value, int intDigits, int scale);   // sign nibble C (+) / D (-)
    public static BigDecimal decode(byte[] src, int off, int intDigits, int scale); // accepts C, D, F
}

/** RECFM=FB reader. In ASCII mode tolerates \n or \r\n after each record. Throws on short final record. */
public final class FixedRecordReader implements Closeable {
    public FixedRecordReader(InputStream in, int lrecl, CobolCharset cs);
    public Optional<byte[]> next() throws IOException;   // empty at EOF (file status '10')
}

public final class FixedRecordWriter implements Closeable {
    public FixedRecordWriter(OutputStream out, int lrecl);
    public void write(byte[] record) throws IOException;   // IllegalArgumentException if length != lrecl
}

/** RECFM=VB writer. */
public final class VariableRecordWriter implements Closeable {
    public VariableRecordWriter(OutputStream out, int maxLrecl /* 84 */);
    public void write(byte[] record) throws IOException;   // 10 <= len <= 80
}
```

Zoned sign rules (COBOL `MOVE` into `PIC S9(n)V99 DISPLAY`): positive **and
zero** results carry zone **C** (`{` in ASCII rendering), negative zone **D**
(`}`, `J`–`R`). Unsigned `PIC 9(11)` carries zone **F** (plain digits). The
decoder must also accept zone **F** on a signed field (unsigned-source data).
Values that exceed the picture are truncated on the left (COBOL MOVE
semantics), never thrown — none occur in the sample data.

Packed sign rules: positive/zero → `0xC`, negative → `0xD`; decoder also accepts
`0xF`. Example: `2525.00` as `S9(10)V99 COMP-3` = `00 00 00 00 25 25 00` →
bytes `00 00 00 02 52 50 0C`. `-2500.00` = `00 00 00 02 50 00 0D`.

### 4.1 RECFM=VB on disk

The dataset is modelled as it is transferred off the mainframe with RDWs
preserved and **no BDWs** (block descriptors belong to the physical block, not
the logical record). Each record = **4-byte RDW** + data:

```
bytes 0-1  big-endian unsigned 16-bit  (data length + 4)
bytes 2-3  0x00 0x00
```

Hence `VBRCFILE` for one account = `00 10 00 00` + 12 bytes, then
`00 2B 00 00` + 39 bytes (16 + 43 = 59 bytes per account, 2950 bytes for the
50-record sample). `LRECL=84` = 80-byte max data + 4-byte RDW, confirming the
4-byte RDW. Only the first `WS-RECD-LEN` bytes of `VBR-REC` are written.

---

## 5. PROCEDURE DIVISION → Java flow (`Cbact01c`, `AccountProcessor`)

```
open ACCTFILE (input), OUTFILE, ARRYFILE, VBRCFILE (output)   -- any failure: 9910 + abend
loop:
  read next 300-byte record -> AccountRecord acct   (EOF -> stop)
  arr = ArrArrayRec.initialized()                    -- INITIALIZE ARR-ARRAY-REC
  display1100(acct)                                  -- only when --display
  out = populateOut(acct, previousOut)               -- 1300
  write OUTFILE(out.toBytes)                         -- 1350
  populateArr(arr, acct)                             -- 1400
  write ARRYFILE(arr.toBytes)                        -- 1450
  vb1 = new Vb1Rec(acct.acctId, acct.activeStatus)   -- 1500 (VBRC-REC1 was INITIALIZEd first)
  vb2 = new Vb2Rec(acct.acctId, acct.currBal, acct.creditLimit, acct.reissueDate.substring(0,4))
  write VBRCFILE(vb1, 12)  ; write VBRCFILE(vb2, 39) -- 1550 / 1575
  display "ACCOUNT-RECORD" raw 300 bytes             -- only when --display
close; exit 0
```

### 5.1 `populateOut` (1300-POPUL-ACCT-RECORD)

* All fields copied 1:1 except:
  * `reissueDate = dateFormatter.convert(type='2', outType='2', acct.reissueDate)` → first 10 bytes of the
    20-byte `CODATECN-0UT-DATE` (see §6).
  * `currCycDebit` (COMP-3): **only assigned when `acct.currCycDebit.signum()==0`**, value `2525.00`.
    When the input debit is non-zero the COBOL program does *not* move anything,
    so `OUT-ACCT-CURR-CYC-DEBIT` **retains the value from the previous record**
    (the FD record area is never re-initialised). Java replicates this by
    carrying `previousOut.currCycDebit` forward; for the very first record the
    carried value is packed **+0** (`00 00 00 00 00 00 0C`). This is a documented
    assumption (§9) — the sample data has debit = 0 on all 50 records so every
    output record carries `2525.00`.

### 5.2 `populateArr` (1400-POPUL-ARRAY-RECORD), Java indices in brackets

```
acctId          = acct.acctId
bal[0].currBal  = acct.currBal        bal[0].currCycDebit =  1005.00
bal[1].currBal  = acct.currBal        bal[1].currCycDebit =  1525.00
bal[2].currBal  = -1025.00            bal[2].currCycDebit = -2500.00
bal[3], bal[4]  stay at INITIALIZE values (+0 zoned, +0 packed)
filler          = 4 spaces
```

### 5.3 DISPLAY semantics (`--display`, stdout, optional)

Reproduced in ASCII regardless of data charset, one line each, exactly:

```
START OF EXECUTION OF PROGRAM CBACT01C
ACCT-ID                 :<11>
ACCT-ACTIVE-STATUS      :<1>
ACCT-CURR-BAL           :<12 zoned incl. overpunch, e.g. 00000001940{>
ACCT-CREDIT-LIMIT       :<12>
ACCT-CASH-CREDIT-LIMIT  :<12>
ACCT-OPEN-DATE          :<10>
ACCT-EXPIRAION-DATE     :<10>
ACCT-REISSUE-DATE       :<10>
ACCT-CURR-CYC-CREDIT    :<12>
ACCT-CURR-CYC-DEBIT     :<12>
ACCT-GROUP-ID           :<10>
-------------------------------------------------
VBRC-REC1:<12>
VBRC-REC2:<39>
<300-byte ACCOUNT-RECORD as text>
... (per record)
END OF EXECUTION OF PROGRAM CBACT01C
```

Note the ordering: 1100 field display and VBRC displays happen inside
1000-ACCTFILE-GET-NEXT, the raw `DISPLAY ACCOUNT-RECORD` happens afterwards in
the main loop. Console output is **not** part of the golden-master assertion.

### 5.4 Error / abend semantics (`IoStatus`)

Java I/O failures are mapped to COBOL file-status codes: open failure → `35`
(input not found) / `30` (other), read/write `IOException` → `30`, short final
input record → `04`. On any non-`00`/`10` status:

```
<context line, e.g. "ERROR OPENING ACCTFILE" or "ACCOUNT FILE WRITE STATUS IS:30">
FILE STATUS IS: NNNN0030          -- 9910-DISPLAY-IO-STATUS ("0000" + status for numeric status)
ABENDING PROGRAM                  -- 9999-ABEND-PROGRAM
```

then `System.exit(12)`. (CEE3ABD raises user abend U0999; a Unix exit code
cannot carry 999, so exit code 12 — the program's own `APPL-RESULT` error
value — is used. Tests assert "nonzero" and the two DISPLAY lines.) `main`
must delegate to an `int run(String[] args, PrintStream out)` so tests can
exercise abend paths without `System.exit`.

---

## 6. DateFormatter contract (port of COBDATFT.asm)

```java
package com.carddemo.batch.cbact01c.date;

/** Mirrors CODATECN-IN-REC: TYPE (1) + INP-DATE (20). */
public record DateConversionRequest(char inType, char outType, String inputDate /* padded/truncated to 20 */) {}

/** Mirrors CODATECN-OUT-REC + CODATECN-ERROR-MSG. */
public record DateConversionResponse(String outputDate /* exactly 20 chars */, String errorMessage /* exactly 38 chars */) {
    public boolean isError();                    // errorMessage is not all spaces
    public String outputDate10();                // first 10 chars -> OUT-ACCT-REISSUE-DATE
}

public interface DateFormatter {
    DateConversionResponse convert(DateConversionRequest request);
}
public final class CobdatftDateFormatter implements DateFormatter { ... }
```

Exact COBDATFT behaviour (output area starts as 20 fill bytes, error message as
38 spaces; the routine only overwrites the bytes listed — it never clears the rest):

| inType | outType | action |
|---|---|---|
| `'1'` (YYYYMMDD) | `'1'` | if `in[4]=='-'` → error; else `out[0..3]=in[0..3]; out[4]='-'; out[5..6]=in[4..5]; out[7]='-'; out[8..9]=in[6..7]` |
| `'1'` | `'2'` | error |
| `'2'` (YYYY-MM-DD) | `'2'` | `out[0..3]=in[0..3]; out[4..5]=in[5..6]; out[6..7]=in[8..9]` (no validation — separator check is commented out in the asm) |
| `'2'` | `'1'` | error |
| `'2'` | anything else | same as `'2'/'2'` (asm only rejects `'1'`) |
| `'1'` | anything else (not `'2'`) | same as `'1'/'1'` |
| other | any | error |

"error" = `errorMessage = "INVALID INPUT"` left-justified, space-padded to 38;
`outputDate` left untouched (fill). The caller (`CBACT01C`) never checks the
error message and moves `CODATECN-0UT-DATE(1:10)` regardless.

**Fill byte assumption:** bytes `out[8..19]` are never written for the
`'2'/'2'` path, so `OUT-ACCT-REISSUE-DATE` = `YYYYMMDD` + 2 bytes of whatever
`CODATECN-0UT-DATE` held. `CODATECN.cpy` has no `VALUE` clause; this design
models the WORKING-STORAGE initial state as **spaces** (`0x40` EBCDIC). It is a
single constant `CobdatftDateFormatter.FILL = ' '` so it can be flipped to
`'\0'` (LOW-VALUES) if a real mainframe output shows otherwise. Example:
`2025-05-20` → `20250520  ` (10 bytes).

Invalid dates such as `2023-13-45` or `ABCD-EF-GH` are passed through
positionally (`20231345`, `ABCDEFGH`) — no calendar validation exists.

---

## 7. Sample data facts (drive test design)

* 50 records, `ACCT-ID` 00000000001..00000000050, all `ACCT-ACTIVE-STATUS='Y'`.
* Record 1: `ACCT-CURR-BAL` text `00000001940{` = **194.00** under `S9(10)V99`
  (the `V99` implies two decimals: 0000000194.0+), credit limit 2020.00, cash
  credit limit 1020.00, open 2014-11-20, expiry 2025-05-20, reissue 2025-05-20.
* EBCDIC and ASCII sample files differ only in record 49 `ACCT-ADDR-ZIP`
  (`ZEROAPR   ` vs `A000000000`); zip is not written to any output file.
* All `ACCT-CURR-CYC-DEBIT = 0` → every OUTFILE record has packed `2525.00`.
* `ACCT-CURR-CYC-CREDIT = 0`, `ACCT-GROUP-ID` = 10 spaces, `ACCT-ADDR-ZIP = "A000000000"`.
* All amounts are positive (`{` overpunch); negatives appear only via the
  hard-coded `-1025.00` / `-2500.00` in ARRYFILE.
* All dates are `YYYY-MM-DD`.
* Expected output sizes for the 50-record sample: OUTFILE 5350 bytes,
  ARRYFILE 5500 bytes, VBRCFILE 2950 bytes.

---

## 8. Ownership

| Component | Owner |
|---|---|
| `pom.xml`, `model/*`, `codec/*`, codec unit tests | Child 1 |
| `date/*` + `CobdatftDateFormatterTest` | Child 2 |
| `Cbact01c`, `AccountProcessor`, `IoStatus`, runnable jar | Child 3 |
| `src/test/resources/golden/*`, `GoldenMasterTest`, edge-case tests, `java/README.md` | Child 4 |

Children must not change interfaces in §3, §4, §6 without raising it with the
parent session; additive helpers are fine.

---

## 9. Documented assumptions

1. Equivalence is asserted on raw EBCDIC bytes (IBM037 + zoned overpunch +
   COMP-3 + 4-byte RDW). ASCII mode is a convenience view.
2. RECFM=VB is written with 4-byte RDWs, no BDWs.
3. `OUT-ACCT-CURR-CYC-DEBIT` carries the previous record's value when the
   input debit is non-zero; initial carried value is packed +0.
4. `CODATECN-0UT-DATE` fill is spaces, so reformatted reissue date is
   `YYYYMMDD` + 2 spaces.
5. No calendar validation in COBDATFT for type '2'.
6. Input is read in file order (the KSDS sequential read returns key order;
   the sample flat files are already in key order). No sorting is applied.
7. Expected fixtures are derived from this specification, not from a real
   mainframe run.
8. Abend is modelled as exit code 12 with the `FILE STATUS IS: NNNN…` and
   `ABENDING PROGRAM` lines.
