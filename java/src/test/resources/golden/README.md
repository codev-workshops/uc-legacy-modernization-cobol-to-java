# Golden-master fixtures for CBACT01C

**DERIVED expected output computed from `docs/MIGRATION_DESIGN.md`, not produced
by the mainframe COBOL program.** No COBOL-generated `OUTFILE` / `ARRYFILE` /
`VBRCFILE` exists in this repository (design §9.7), so these files are an
independent oracle: they were computed straight from the input dataset with
`generate_expected.py`, which implements the documented encodings (IBM037 text,
zoned overpunch, hand-built COMP-3 constants, 4-byte RDW) and deliberately does
**not** import the production codecs under
`src/main/java/com/carddemo/batch/cbact01c/`.

| file | bytes | content |
|---|---|---|
| `acctdata.ebcdic.in` | 15000 | byte-identical copy of `app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS` (50 × 300, EBCDIC) |
| `OUTFILE.expected.bin` | 5350 | 50 × 107, RECFM=FB |
| `ARRYFILE.expected.bin` | 5500 | 50 × 110, RECFM=FB |
| `VBRCFILE.expected.bin` | 2950 | 50 × (RDW+12 and RDW+39), RECFM=VB |

`GoldenMasterTest` runs `Cbact01c.run(...)` over `acctdata.ebcdic.in` in EBCDIC
mode and asserts byte-for-byte equality against these three files.

## Regenerating

```
python3 java/src/test/resources/golden/generate_expected.py
```

Only regenerate after a **design** change (`docs/MIGRATION_DESIGN.md`), and then
only by changing the rules the generator implements. Never regenerate a fixture
from the Java program's own output — that would turn the golden master into a
tautology. If a golden test fails, the discrepancy has to be resolved against
the design document.

## Hand-checked bytes (record 1, EBCDIC)

`OUTFILE` record 1 (`00000000001`, status `Y`, balance 194.00, reissue
`2025-05-20`):

```
F0F0F0F0F0F0F0F0F0F0F1   OUT-ACCT-ID          "00000000001"
E8                       OUT-ACCT-ACTIVE-STATUS 'Y'
F0F0F0F0F0F0F0F1F9F4F0C0 OUT-ACCT-CURR-BAL    194.00 -> "00000001940" + '{' (C0)
F0F0F0F0F0F0F2F0F2F0F0C0 OUT-ACCT-CREDIT-LIMIT 2020.00
F0F0F0F0F0F0F1F0F2F0F0C0 OUT-ACCT-CASH-CREDIT-LIMIT 1020.00
F2F0F1F460F1F160F2F0     OUT-ACCT-OPEN-DATE   "2014-11-20"
F2F0F2F560F0F560F2F0     OUT-ACCT-EXPIRAION-DATE "2025-05-20"
F2F0F2F5F0F5F2F04040     OUT-ACCT-REISSUE-DATE "20250520" + 2 fill spaces
F0F0F0F0F0F0F0F0F0F0F0C0 OUT-ACCT-CURR-CYC-CREDIT 0.00 (+0 -> C0)
00000002 52500C          OUT-ACCT-CURR-CYC-DEBIT COMP-3 2525.00
40404040404040404040     OUT-ACCT-GROUP-ID    10 spaces
```

`ARRYFILE` record 1: id + occurrence 1 (194.00 / packed `00 00 00 01 00 50 0C`),
occurrence 2 (194.00 / `00 00 00 01 52 50 0C`), occurrence 3 (zoned -1025.00 =
`F0F0F0F0F0F0F1F0F2F5F0D0` / packed `00 00 00 02 50 00 0D`), occurrences 4 and 5
at INITIALIZE values (`…F0C0` / `00 00 00 00 00 00 0C`), then `40 40 40 40`.

`VBRCFILE` record 1: `00 10 00 00` + `F0…F1 E8` (12 bytes), then `00 2B 00 00` +
id, balance, credit limit and `F2F0F2F5` (`"2025"`, the first 4 characters of the
original `ACCT-REISSUE-DATE`).
