# CBACT01C — Java 17 rewrite

Java port of the CardDemo batch program `app/cbl/CBACT01C.cbl`: it reads the
account master file (`ACCTFILE`, 300-byte records) and writes three datasets —
`OUTFILE` (FB, LRECL 107), `ARRYFILE` (FB, LRECL 110) and `VBRCFILE` (VB, LRECL
84). `docs/MIGRATION_DESIGN.md` is the specification; this README only covers
building, running and testing the module.

## Build, run, test

```bash
cd java
mvn -B test        # unit + golden-master tests
mvn -B package     # target/cbact01c.jar (executable, no dependencies)
```

```bash
java -jar target/cbact01c.jar \
  --acctfile ../app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS \
  --outfile /tmp/OUTFILE --arryfile /tmp/ARRYFILE --vbrcfile /tmp/VBRCFILE \
  [--charset EBCDIC|ASCII] [--display]
```

| flag | meaning |
|---|---|
| `--acctfile` | input account master (`ACCTFILE` DD), required |
| `--outfile` / `--arryfile` / `--vbrcfile` | the three output DDs, required |
| `--charset` | `EBCDIC` (default, IBM037) or `ASCII` |
| `--display` | reproduce the COBOL `DISPLAY` output on stdout |

Exit codes: `0` on normal completion, `12` on an abend (see below). `main()`
delegates to `Cbact01c.run(String[], PrintStream)` so tests can exercise the
abend paths without `System.exit`.

## Comparison mode

Equivalence with the mainframe is asserted on **raw EBCDIC bytes**: IBM037 text,
zoned decimal with overpunched sign (`C` for positive/zero, `D` for negative),
`COMP-3` packed fields and 4-byte RDWs on the VB dataset. ASCII mode
(`app/data/ASCII/acctdata.txt`) is a readability view — text fields become
ASCII, the zoned overpunch keeps its ASCII rendering (`00000001940{`), and
packed fields and RDWs stay binary. Output records carry no line terminators in
either mode. `AsciiModeTest` proves the two modes decode to identical field
values for all 50 sample records.

## Documented assumptions (design §9)

1. Golden-master equivalence is byte-for-byte on EBCDIC-mode output; ASCII mode
   is a convenience view only.
2. `VBRCFILE` is written with a 4-byte RDW per record (big-endian length + 4,
   then two zero bytes) and **no** block descriptor words.
3. `OUT-ACCT-CURR-CYC-DEBIT` is only assigned (`2525.00`) when the input
   `ACCT-CURR-CYC-DEBIT` is zero; otherwise the COBOL FD record area keeps the
   previous record's value, which the Java port carries forward. The value
   carried into the first record is packed `+0`
   (`00 00 00 00 00 00 0C`).
4. `CODATECN-0UT-DATE` is modelled as initialised to spaces, so the reformatted
   reissue date is `YYYYMMDD` + two spaces (`CobdatftDateFormatter.FILL`, flip
   to `'\0'` if a real mainframe run shows LOW-VALUES).
5. `COBDATFT` performs no calendar validation for type `'2'`: `2023-13-45`
   becomes `20231345`.
6. Records are processed in file order; no sorting is applied.
7. The expected fixtures under `src/test/resources/golden/` are **derived from
   the design document**, not produced by a mainframe run.
8. The `CEE3ABD` U0999 abend is modelled as exit code `12` preceded by the
   `... STATUS IS:` context line, `FILE STATUS IS: NNNN00nn` and
   `ABENDING PROGRAM`.

## Tests

| test | what it covers |
|---|---|
| `codec/*Test`, `model/*Test`, `date/CobdatftDateFormatterTest` | unit-level encodings, record layouts, date routine |
| `Cbact01cTest`, `AccountProcessorTest` | CLI, flow, DISPLAY output, abend paths |
| `golden/GoldenMasterTest` | EBCDIC run over the 50-record sample vs the derived fixtures, byte for byte |
| `golden/AsciiModeTest` | ASCII run decodes field-for-field identically to the EBCDIC run |
| `golden/EdgeCaseTest` | debit carry-over, hard-coded negative array values, COMP-3 boundaries, VB record lengths, date reformatting, negative input balances |

## Regenerating the golden fixtures

```bash
python3 src/test/resources/golden/generate_expected.py
```

The generator computes the expected bytes directly from
`src/test/resources/golden/acctdata.ebcdic.in` using only the documented rules;
it does not import the production codecs, and the fixtures must never be
regenerated from this program's own output. See
`src/test/resources/golden/README.md`. Regenerate only when
`docs/MIGRATION_DESIGN.md` changes, and change the generator's rules to match
the new design first.
