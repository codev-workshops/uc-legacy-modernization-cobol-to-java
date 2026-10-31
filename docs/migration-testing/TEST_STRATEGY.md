# CardDemo Migration Test Strategy (COBOL to Java 17)

This strategy defines how the Java 17 replacement of CardDemo is proven equivalent to the COBOL system, phase by phase
of the [cutover plan](../migration-blueprint/CUTOVER_PLAN.md). It is backed by executable tooling:

- [`test-harness/`](../../test-harness/README.md): copybook parser, record codecs, reference models, comparison, reconciliation and contract checks, plus 186 pytest tests.
- [`golden-files/`](../../golden-files/README.md): JSON golden references generated from `app/data/ASCII/` through the copybooks.
- [RECONCILIATION_CHECKS.md](RECONCILIATION_CHECKS.md): per-job validation specs.

## 1. Objectives

1. Every migrated record round-trips with no loss of precision, sign, length or encoding.
2. Every migrated batch job produces the same business outputs as its COBOL program on the same inputs (counts, money, reject codes, keys).
3. Every cutover ends with reconciled data: counts, control totals and referential integrity match the source of record.
4. Every interface (copybook records, MQ messages, COMMAREA, DB2 tables, files exchanged between jobs) honours its legacy contract until consumers are migrated.

## 2. The four testing dimensions

| Dimension | Question it answers | Oracle | Harness component | When it runs |
|---|---|---|---|---|
| **Golden-file** | Do we read and write legacy data exactly? | Committed JSON from the ASCII sample data via copybooks | `copybook.py`, `decoders.py`, `records.py`, `golden.py`, `golden-files/*.json` | Every build |
| **Differential** | Does Java behave like COBOL on the same input? | Legacy output (mainframe run) or the reference model in `models.py` | `models.py`, `compare.py`, `golden-files/jobs/*.expected.json` | Every build (model), every parallel-run cycle (mainframe) |
| **Reconciliation** | Do the books balance after a job or a cutover? | Arithmetic invariants and source-of-record totals | `reconcile.py`, `jobs.py`, `reconciliation-baseline.json` | Every batch run in parallel run, every cutover rehearsal and go-live |
| **Contract** | Do records and messages still fit the shape consumers expect? | Copybook layout (PIC, length, sign, scale, 88-levels) | `contract.py`, `golden-files/schemas/*.schema.json` | Every build, plus consumer-driven tests per interface |

### 2.1 Golden-file testing

**What it is.** The ASCII sample files are parsed with their copybooks into canonical JSON (`golden-files/<dataset>.json`). Each file records the source SHA-256, the copybook, the derived byte layout, control totals and every record sorted by key.

**Canonical form.**
- Decimals are strings with the copybook scale (`ACCT-CURR-BAL: "194.00"`), so no floating-point loss.
- Whole numbers are integers.
- Alphanumerics are right-trimmed.
- Field names are the copybook names, so COBOL and Java teams use one vocabulary.

**What the harness proves today.**
- The copybook length equals the record length for all 9 datasets.
- Fields are contiguous.
- All 626 sample records parse with zero issues.
- Every named field re-encodes to its exact source bytes (`test_byte_round_trip_against_source`).
- All 20 overpunch sign bytes decode correctly, and encoding and decoding round-trip.
- The committed golden files match a fresh regeneration (`test_committed_golden_files_are_up_to_date`), so any change to the data or a copybook is caught.

**How Java uses it.**
1. Java's codecs (`migration-tools` module in the blueprint) must parse the same ASCII and EBCDIC files into the same JSON. CI then diffs the result with `python -m carddemo_harness compare <dataset> <java.json>`.
2. Java's writers must reproduce the source bytes from the JSON. This round-trip is what makes rollback to VSAM possible.
3. For production volumes, golden files are replaced by control-total manifests (`manifest.json` style) plus sampled records, because full-volume JSON is not practical.

**EBCDIC, packed decimal (COMP-3) and binary (COMP).** The mainframe unloads in `app/data/EBCDIC/` (code page 037, RECFM=FB, no line delimiters) are decoded with the same copybooks:
- `decode_packed` / `encode_packed`: two BCD digits per byte, sign in the last nibble. C, A, E are positive, F unsigned, D, B negative; signed fields are written with C/D and unsigned with F. Bad digit or sign nibbles (for example spaces, `X'40'`) are reported as parse issues, never silently turned into zero.
- `decode_binary` / `encode_binary`: big-endian, two's complement when signed, with the PIC digit limit enforced on encode (TRUNC(STD)).
- Zoned EBCDIC fields need no extra code: cp037 maps the overpunch zones `X'C0'`-`X'C9'` / `X'D0'`-`X'D9'` to the same `{`, `A`-`I`, `}`, `J`-`R` characters as the ASCII files.
- What is proven: the EBCDIC twins of all 9 master files decode to the ASCII golden records (two known data differences, finding F-6); the CBEXPORT file (`AWS.M2.CARDDEMO.EXPORT.DATA.PS`, CVEXPORT.cpy, 5 record types over REDEFINES with COMP and COMP-3 fields) parses all 500 records with zero issues and re-encodes byte for byte; its golden is `golden-files/export.json`.
- `python -m carddemo_harness decode <dataset|export> <unload.bin>` turns a mainframe or Java-written binary file into the canonical JSON that `compare` and `contract` accept.

### 2.2 Differential testing

**What it is.** The same input is run through the legacy logic and the Java logic, and the outputs are compared record by record by key. Ignored fields are only those that legitimately differ, such as processing timestamps.

**Two oracles.**
- **Reference model (every build).** `models.py` is an executable, line-cited reading of CBTRN02C (POSTTRAN) and CBACT04C (INTCALC), the two highest-risk batch programs (hotspots #3 and #4). It runs on golden inputs or on generated edge cases in milliseconds, so Java developers get feedback before a mainframe run is available.
- **Mainframe (parallel run).** During Phase 5 parallel run, the real COBOL outputs are unloaded, parsed with the same copybooks and compared. The reference model is itself validated against the first parallel-run cycles; a model mismatch is a harness defect, not a Java defect.

**Comparison rules (`compare.py`).**

| Rule | Behaviour |
|---|---|
| Keys | Copybook keys (`ACCT-ID`, `CARD-NUM`, composite `TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD`, ...) |
| Results | Missing, unexpected and duplicate keys, plus per-field mismatches |
| Numbers | Compared by value (`1`, `"1"` and `"1.00"` are equal); default tolerance 0 for money |
| Strings | Compared after right-trim, the equivalent of COBOL space padding |
| Ignored fields | Declared per job in `golden-files/jobs/*.expected.json` (`ignore_fields`), never ad hoc |

**Rules the reference model pins down.**

| Rule | Source |
|---|---|
| Reject precedence 100, then 101, then 102 and 103 both evaluated, so 103 overwrites 102 | CBTRN02C `1500-VALIDATE-TRAN` |
| Credit limit test uses cycle credit - cycle debit + amount, not current balance | CBTRN02C line 403 |
| Expiry compares `ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10)` as strings | CBTRN02C line 414 |
| Amounts >= 0 go to cycle credit, < 0 to cycle debit (zero is a credit) | CBTRN02C `2800-UPDATE-ACCOUNT-REC` |
| Missing TCATBALF row is created | CBTRN02C `2700-UPDATE-TCATBAL` |
| Accounts are re-read per transaction, so later transactions see earlier postings | CBTRN02C |
| Monthly interest = `TRAN-CAT-BAL * DIS-INT-RATE / 1200`, truncated toward zero (no `ROUNDED`) | CBACT04C `1300-COMPUTE-INTEREST` |
| Rate fallback to group `DEFAULT`; abend when that is also missing | CBACT04C `1200-GET-INTEREST-RATE` |
| Zero rate writes no transaction | CBACT04C |
| Interest `TRAN-ID` = PARM-DATE + 6-digit suffix; type 01, category 05, source `System` | CBACT04C `1300-B-WRITE-TX` |

**Legacy parity switches.** Behaviour that is probably a defect is reproduced by default and can be switched off once the business decides (decision 2 in section 9 of [MIGRATION_BLUEPRINT.md](../migration-blueprint/MIGRATION_BLUEPRINT.md)). Example: `calculate_interest(..., legacy_parity=True)` reproduces the last-account quirk described in section 6.

**Edge-case generation.** Use `encode_zoned` and `encode_record` to write synthetic legacy-format files: boundary amounts at the credit limit, ± 0.01 around it, negative balances, expiry on the transaction date, unknown cards and missing categories. Run them through both COBOL (on the mainframe test LPAR) and Java.

### 2.3 Reconciliation testing

**What it is.** Invariants that must hold for every job run and every data migration, independent of record-level comparison. They catch lost, duplicated or double-applied work, which record diffs can miss when both sides are wrong in the same way.

**Check families (`reconcile.py`).**
- **Count:** input = output + rejects.
- **Control totals:** sums of money fields.
- **Balancing:** the change in account balances equals posted amounts.
- **Key uniqueness.**
- **Referential integrity:** card → account, xref → card/account/customer, category balance → account, category → type.
- **Domain:** status flags, transaction type codes.
- **Return code.**

**Baseline.** `golden-files/jobs/reconciliation-baseline.json` holds 76 checks over the sample data, all passing (51 for the ASCII masters and jobs, 9 EBCDIC twin parity checks, 16 CBEXPORT/CBIMPORT checks).

**Mutation tests prove the checks bite.** The tests inject one fault each and assert the right check fails:
- a dropped posting
- a 1-cent balance drift
- a wrong cycle bucket
- an unknown reject code
- a duplicate `TRAN-ID`
- interest not applied
- cycle totals not reset
- a 1-cent rounding difference
- a lost load record
- an orphan card

**At cutover.** Phase 5 go/no-go requires the full reconciliation set to pass on the unloaded VSAM data versus the loaded target database, with zero tolerance on money and counts. See [CUTOVER_PLAN.md](../migration-blueprint/CUTOVER_PLAN.md).

### 2.4 Contract testing

**What it is.** Each migrated record, message or table row must fit the legacy layout while any COBOL consumer, file exchange or MQ peer still depends on it.

**Provider side (`contract.py`).** `validate(record, layout)` reports:
- missing or extra fields
- wrong types
- strings longer than the PIC
- negatives in unsigned fields
- too many decimal places
- digit overflow
- floats used for money

`json_schema(layout)` emits draft 2020-12 schemas into `golden-files/schemas/`, annotated with `x-cobol-pic`, `x-cobol-offset` and `x-cobol-88`. Java services can validate API and event payloads against them, or generate DTOs from them.

**Interfaces under contract.**

| Interface | Contract source | Consumers during coexistence |
|---|---|---|
| VSAM master records (ACCTDATA, CARDDATA, CUSTDATA, CARDXREF) | CVACT01Y, CVACT02Y, CVCUS01Y, CVACT03Y | COBOL online and batch until Phase 5 |
| DALYTRAN feed / TRANSACT / SYSTRAN | CVTRA06Y, CVTRA05Y | POSTTRAN, COMBTRAN, TRANREPT, CREASTMT |
| TCATBALF, DISCGRP | CVTRA01Y, CVTRA02Y | INTCALC |
| MQ authorization request/response | CCPAURQY, CCPAURLY | Card network / COPAUA0C (Phase 6 shadow) |
| MQ inquiry (CDRA, CDRD) | Request/reply layouts inside COACCT01 and CODATE01 | MQ clients (Phase 1) |
| Export file | CVEXPORT (multi-record REDEFINES) | Branch import (CBIMPORT) |
| DB2 TRANSACTION_TYPE / _CATEGORY, AUTHFRDS | DDL in `app/app-authorization-ims-db2-mq/ddl` and `app/app-transaction-type-db2/ddl` | COTRTLIC/COTRTUPC, COPAUS2C |
| COMMAREA (COCOM01Y) | Session fields between transactions | CICS screens still on COBOL (Phases 2-5) |

**Consumer-driven tests.** For every interface that crosses the strangler boundary, the consumer team writes example payloads; the provider build runs `contract.py` (or the JSON Schema) against them. MQ contracts additionally require byte-exact fixed-width comparison, because COBOL peers read them by offset.

## 3. Test levels and where each dimension applies

| Level | Golden-file | Differential | Reconciliation | Contract |
|---|---|---|---|---|
| Unit (Java codecs, rules) | Yes: parse and round-trip each copybook | Yes: rule-level cases against `models.py` | No | Yes: DTO validation |
| Component (Spring Batch job) | Inputs from golden | Yes: whole-job output vs `jobs/*.expected.json` | Yes: per-job checks | Yes: output files |
| Integration (MQ, DB, files) | No | Shadow traffic compare (authorization) | Queue and message counts | Yes: MQ and DB2 |
| Parallel run (Phase 5, 6) | No | Yes: mainframe vs Java, every cycle | Yes: every cycle | Yes |
| Cutover rehearsal and go-live | Unload and load round-trip | Sampled records | Yes: full set, zero tolerance | No |

## 4. Entry and exit criteria per cutover phase

| Phase | Must pass before go-live |
|---|---|
| 0 Foundations | Harness in CI; golden files current; Java codecs pass golden round-trip for all 9 datasets |
| 1 Reference data + MQ inquiry | DB2 vs VSAM reference reconciliation (TRANTYPE/TRANCATG); MQ CDRA/CDRD contract tests |
| 2 Read-only inquiries | Differential on screen data for 100% of sample accounts and cards; COMMAREA contract |
| 3 Statements and reporting | TRANREPT and CREASTMT reconciliation: totals per account and grand total; output layout contract |
| 4 Identity | USRSEC migration count and role reconciliation; no plain-text password stored in the target |
| 5 Account/card/ledger | 20 consecutive POSTTRAN cycles and 2 INTCALC cycles with zero differential mismatches and all reconciliation checks green |
| 6 Authorization | 100% decision match in MQ shadow for 2 weeks; IMS to target reconciliation for PAUTSUM0/PAUTDTL1 |
| 7 Decommission | Final reconciliation archived; golden files retained as regression baseline |

The parallel-run thresholds (20 posting cycles, 2 interest cycles, 2 weeks of shadow) come from the blueprint and are recommendations awaiting business approval.

## 5. Running the harness

```bash
cd test-harness && pip install -r requirements.txt
./run_tests.sh                    # 263 tests + 76 reconciliation checks; JUnit XML and JSON in reports/
python3 -m carddemo_harness compare daily_transaction java-out.json
python3 -m carddemo_harness contract account java-accounts.json
python3 -m carddemo_harness decode export unloads/EXPORT.DATA.PS > unloads/export.json
```

CI: `.github/workflows/migration-test-harness.yml` runs ruff and `run_tests.sh` on changes to the harness, golden files, copybooks or sample data, and uploads `reports/` as an artifact.

## 6. Findings from building the harness

These come from reading the code and running the reference models on the sample data. None have been confirmed on a mainframe.

| ID | Finding | Evidence | Impact on testing |
|---|---|---|---|
| F-1 | In every sample ACCTDATA record, the disclosure group value (`A000000000`) sits in the `ACCT-ADDR-ZIP` position (offset 102) and `ACCT-GROUP-ID` (offset 112) is blank | `golden-files/account.json`; `test_sample_accounts_all_use_default_rates` | INTCALC takes the `DEFAULT` rate for all 100 category balances. Either the data or CVACT01Y is out of step; confirm with the data owners before using the samples as a pricing oracle |
| F-2 | CBACT04C's main loop is `PERFORM UNTIL END-OF-FILE = 'Y'`, which tests before each pass, so its `ELSE PERFORM 1050-UPDATE-ACCOUNT` branch never runs. The last account's interest transactions are written, but its balance and cycle totals are not updated | CBACT04C main loop; `test_legacy_parity_skips_last_account_update`; `INTCALC.expected.json` `last_account_not_updated: 50` | Reproduced by default (`legacy_parity=True`); business must decide whether Java fixes it (decision 2 in section 9 of [MIGRATION_BLUEPRINT.md](../migration-blueprint/MIGRATION_BLUEPRINT.md)) |
| F-3 | Sample TCATBALF balances are all zero, so INTCALC on raw samples produces only zero-amount interest | `golden-files/category_balance.json` | The golden INTCALC run is chained after POSTTRAN, the realistic daily-then-monthly order |
| F-4 | On the sample data, 38 of 300 daily transactions are rejected, all with code 102 (over limit) | `POSTTRAN.expected.json` | Codes 100, 101 and 103 are covered only by synthetic unit tests; add sample records for them |
| F-5 | When a transaction fails both checks, reason 103 overwrites 102 | CBTRN02C lines 403-420 | Java must keep this order, or DALYREJS reason distributions will differ |
| F-6 | The EBCDIC and ASCII sample masters are not identical: account 49 has `ACCT-ADDR-ZIP` `ZEROAPR` (ASCII `A000000000`), and the `DEFAULT`/07/0001 disclosure rate is 15.00 (ASCII 0.00) | `EBCDIC.account.parity`, `EBCDIC.disclosure_group.parity`; `KNOWN_EBCDIC_DIFFS` in `datasets.py` | The codec is not at fault (every other field of all 626 records matches). Pick one rendition as the oracle per test; any new difference fails the parity check |
| F-7 | The export file was taken from a slightly different snapshot than the ASCII masters: customers 1 and 50 and accounts 1, 49 and 50 differ (upper-cased names and addresses, balance and dates), and the exported accounts 1 and 50 carry low-values (`X'00'`) in `EXP-ACCT-ADDR-ZIP` and `EXP-ACCT-GROUP-ID`. Cards, xrefs and all 300 transactions match exactly | `CBEXPORT.*.parity`; `KNOWN_SNAPSHOT_DIFFS` in `export.py` | Low-values are legal COBOL but not valid text for many Java/DB targets; the Java import must decide how to map them |
| F-8 | `EXPORT-SEQUENCE-NUM` jumps from 450 (last transaction) to 460 (first card); numbers stay unique and ascending | `golden-files/export.json` | CBIMPORT must not assume contiguous sequence numbers; the checks only require unique and ascending |

## 7. Gaps and next steps

| ID | Gap | Plan |
|---|---|---|
| G-1 | Done: COMP-3, COMP and cp037 decoding, golden-tested against `app/data/EBCDIC/` | Extend to other code pages (for example cp1140 for the euro sign) only if a source system uses them |
| G-2 | Reference models cover POSTTRAN and INTCALC only | Add CBTRN03C (TRANREPT totals), CBSTM03A (statement totals), CBPAUP0C (purge) as those phases start (CBEXPORT/CBIMPORT round-trip is automated) |
| G-3 | No mainframe-produced expected outputs yet | Capture DALYREJS, TRANSACT, ACCTDATA and TCATBALF after a real POSTTRAN/INTCALC run on the sample data and diff against `jobs/*.expected.json` to validate the model |
| G-4 | Online (CICS) differential testing | Record 3270 screen and COMMAREA traces per transaction and replay them against the Java UI/API (Phase 2 onward) |
| G-5 | MQ and IMS contracts are specified but not automated | Add CCPAURQY/CCPAURLY and IMS segment layouts to `datasets.py` when Phase 6 starts |
