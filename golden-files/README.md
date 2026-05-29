# Golden Reference Files

This directory contains structured JSON representations of the CardDemo ASCII
data files, parsed using the COBOL copybook PIC clause definitions. Each
`.golden.json` file serves as the authoritative reference for migration testing.

## Contents

| Golden File | Source Data | Copybook | Records | Description |
|---|---|---|---|---|
| `acctdata.golden.json` | `app/data/ASCII/acctdata.txt` | `CVACT01Y.cpy` | 50 | Account master records |
| `carddata.golden.json` | `app/data/ASCII/carddata.txt` | `CVACT02Y.cpy` | 50 | Card master records |
| `cardxref.golden.json` | `app/data/ASCII/cardxref.txt` | `CVACT03Y.cpy` | 50 | Card-to-account-to-customer cross-references |
| `custdata.golden.json` | `app/data/ASCII/custdata.txt` | `CVCUS01Y.cpy` | 50 | Customer master records |
| `dailytran.golden.json` | `app/data/ASCII/dailytran.txt` | `CVTRA06Y.cpy` | 300 | Daily transaction feed |
| `discgrp.golden.json` | `app/data/ASCII/discgrp.txt` | `CVTRA02Y.cpy` | 51 | Disclosure group interest rates |
| `tcatbal.golden.json` | `app/data/ASCII/tcatbal.txt` | `CVTRA01Y.cpy` | 50 | Transaction category balances |
| `trancatg.golden.json` | `app/data/ASCII/trancatg.txt` | `CVTRA04Y.cpy` | 18 | Transaction category descriptions |
| `trantype.golden.json` | `app/data/ASCII/trantype.txt` | `CVTRA03Y.cpy` | 7 | Transaction type descriptions |

## JSON Structure

Each golden file contains:

```json
{
  "source_file": "app/data/ASCII/<filename>.txt",
  "copybook": "<copybook>.cpy",
  "description": "Human-readable description of the record type",
  "record_length": 300,
  "record_count": 50,
  "field_definitions": [
    {
      "name": "ACCT-ID",
      "offset": 0,
      "length": 11,
      "pic": "PIC 9(11)",
      "description": "Account identifier"
    }
  ],
  "records": [
    {
      "_record_number": 1,
      "ACCT-ID": "00000000001",
      "ACCT-ACTIVE-STATUS": "Y",
      "ACCT-CURR-BAL": "194.00"
    }
  ]
}
```

## Field Decoding

- **Alphanumeric (`PIC X`)**: Preserved as-is with trailing spaces stripped.
- **Unsigned numeric (`PIC 9`)**: Leading zeros preserved as a string.
- **Signed display (`PIC S9..V99`)**: Decoded from COBOL ASCII overpunch
  encoding where the last byte encodes the sign:
  - Positive: `{`=0, `A`=1 through `I`=9
  - Negative: `}`=0, `J`=1 through `R`=9
  - Implied decimal applied per the `V99` specification.
- **Filler fields**: Included but typically empty or all-spaces.

## Regeneration

To regenerate the golden files from the source data:

```bash
python3 golden-files/generate_golden_files.py
```
