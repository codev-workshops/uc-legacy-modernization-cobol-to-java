# CardDemo migration test harness

Python 3.10+ tooling that turns the COBOL copybooks and ASCII sample data into executable test oracles for the
COBOL-to-Java 17 migration. Strategy: [docs/migration-testing/TEST_STRATEGY.md](../docs/migration-testing/TEST_STRATEGY.md).
Per-job checks: [docs/migration-testing/RECONCILIATION_CHECKS.md](../docs/migration-testing/RECONCILIATION_CHECKS.md).

| Module | Purpose |
|---|---|
| `carddemo_harness/copybook.py` | Parses fixed-format copybooks (levels, PIC, COMP/COMP-3, OCCURS, REDEFINES, 88-levels) into byte layouts |
| `carddemo_harness/decoders.py` | Zoned-decimal decoding/encoding with ASCII overpunch signs (`{`, `A`-`I`, `}`, `J`-`R`) |
| `carddemo_harness/records.py` | Fixed-width record parse (`parse_line`, `parse_dataset`) and inverse `encode_record` |
| `carddemo_harness/datasets.py` | Registry: ASCII file, copybook, key, record length, VSAM name, money fields |
| `carddemo_harness/golden.py` | Builds `golden-files/*.json` (layout, SHA-256 of source, control totals, sorted records) |
| `carddemo_harness/models.py` | Reference models of CBTRN02C (POSTTRAN) and CBACT04C (INTCALC), the differential-test oracle |
| `carddemo_harness/compare.py` | Keyed record-set diff: missing, unexpected, duplicates, field mismatches, ignore list, tolerance |
| `carddemo_harness/reconcile.py` | Count, control-total, uniqueness, referential-integrity, domain and per-job balancing checks |
| `carddemo_harness/contract.py` | Copybook contract validation and JSON Schema generation for migrated records |
| `carddemo_harness/jobs.py` | Runs the POSTTRAN -> INTCALC chain and writes `golden-files/jobs/` and `golden-files/schemas/` |

## Run

```bash
cd test-harness
pip install -r requirements.txt
./run_tests.sh                     # pytest (JUnit XML) + reconciliation report in reports/
python3 -m carddemo_harness golden # regenerate golden-files/ after an intentional data/copybook change
python3 -m carddemo_harness compare account path/to/java-extract.json --ignore ACCT-ADDR-ZIP
python3 -m carddemo_harness contract account path/to/java-extract.json
```

`compare` and `contract` accept either a JSON array of records or an object with a `records` array, using the field
names from the copybook (for example `ACCT-CURR-BAL`). Decimals are strings (`"194.00"`), so no precision is lost.
Exit code 0 means pass, 1 means at least one difference or violation. File arguments (`actual`, `--report`) must resolve inside the current working directory. CI installs pinned, hash-locked wheels from `requirements-ci.lock` (regenerate with `uv pip compile requirements-ci.in --generate-hashes --only-binary :all: -o requirements-ci.lock`).
