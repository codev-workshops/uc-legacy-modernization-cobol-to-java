# CardDemo migration parity harness

This Maven module is independent of the migrated application. It decodes fixed-record
files and golden JSON into file/record values, compares outputs, and performs data
reconciliation. It does not load application classes or modify the scanned repository.

## Layout

- `codec/` defines copybook-backed layouts and ASCII/CP037 DISPLAY, COMP, and COMP-3 codecs.
- `loader/` reads fixed blocks and line records, and converts between raw and golden data.
- `compare/` provides keyed record comparison, explicit ignored-field reporting, and byte
  round-trip diagnostics.
- `recon/` checks record totals, keys, referential integrity, balances, and reject reasons.
- `app/` is the file-based extension point for a migrated application.
- `src/test/` exercises the independent Java decoder against the Python-generated golden
  files and manifest.

Run the complete harness from the repository root:

```bash
mvn -B -f test_harness/pom.xml verify
```

JaCoCo writes a report to `test_harness/target/site/jacoco/index.html`; it is informational
and has no coverage threshold.

## Plug in the migrated application

Implement `MigratedAppAdapter` in a class available on the test classpath, then set
`-Dcarddemo.app.adapter=your.package.YourAdapter`. The adapter receives file paths and
returns output file paths and process results; it does not depend on application classes.
When no adapter is configured, the parity integration test is skipped with a clear
assumption message.

## Golden data

The golden JSON and manifest are generated from source seed data by the independent Python
decoder:

```bash
python3 golden_data/tools/generate_golden.py
python3 golden_data/tools/generate_golden.py --check
```

The Java harness only reads these artifacts. A mismatch is reported as a decoder or
reference-data disagreement; neither the golden data nor its generator should be edited to
make the Java implementation pass.
