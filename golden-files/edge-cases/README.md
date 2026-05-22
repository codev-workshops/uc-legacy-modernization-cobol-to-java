# Edge Case Golden Files

Test data for boundary conditions in the COBOL fixed-width parser.

## Categories

### empty/
Files with zero records. Parser must return `{"records": [], "record_count": 0}` without errors.

### single-record/
Files with exactly one valid record using typical values from the production data set.
Validates that the parser works without requiring multi-record batching.

### max-values/
Files where every numeric field is at its maximum representable value per PIC clause.
Tests for overflow handling, correct sign decoding at boundaries, and full-width field parsing.

### max-negative/
Files where every signed numeric field carries the maximum negative overpunch value.
Tests negative sign decoding and correct handling of large negative amounts.

### zero-values/
Files where all signed numeric fields are positive zero (`00000000000{`).
Tests the CBACT01C bug: when ACCT-CURR-CYC-DEBIT = 0, the program substitutes 2525.00.
Also tests whether CBTRN02C posts a zero-amount transaction.

## Overpunch Encoding Reference
- Positive: { = +0, A=+1, B=+2, C=+3, D=+4, E=+5, F=+6, G=+7, H=+8, I=+9
- Negative: } = -0, J=-1, K=-2, L=-3, M=-4, N=-5, O=-6, P=-7, Q=-8, R=-9

## Max Values Per PIC Type
| PIC Clause | Storage Bytes | Max Positive | Encoding |
|---|---|---|---|
| PIC 9(11) | 11 | 99999999999 | Plain digits |
| PIC S9(10)V99 | 12 | +9999999999.99 | `99999999999I` |
| PIC S9(09)V99 | 11 | +999999999.99 | `9999999999I` |
| PIC S9(04)V99 | 6 | +9999.99 | `99999I` |
| PIC X(n) | n | n printable characters | As-is |

## Record Lengths
| Entity | Copybook | Record Length |
|---|---|---|
| acctdata | CVACT01Y.cpy | 300 |
| carddata | CVACT02Y.cpy | 150 |
| cardxref | CVACT03Y.cpy | 50 |
| custdata | CVCUS01Y.cpy | 500 |
| dailytran | CVTRA06Y.cpy | 350 |

## File Format Notes
- All `.txt` files are exactly the documented record length in bytes (no trailing newline).
- All `.expected.json` files include both raw string representation and decoded `_numeric`/`_decimal` values for numeric/signed fields.
- Empty `.txt` files are truly 0 bytes.
