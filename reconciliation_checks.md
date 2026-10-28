# CardDemo Reconciliation Checks

Validation catalogue for the COBOL → Java migration. Every check names its data source,
its rule (with the COBOL source line where applicable) and its pass criterion. Checks are
implemented by `test_harness` (`com.carddemo.harness.recon`, `…compare`) and their seed
values come from `golden_data/manifest.json`. Companion document:
[`test_strategy.md`](test_strategy.md).

Check IDs: `DS-*` dataset level, `RI-*` referential integrity, `PG-*` program level,
`FS-*` file-status / error path.

---

## 1. Tolerance policy (applies to every check)

* **Exact equality.** Amounts are parsed into `java.math.BigDecimal` from canonical strings
  and compared exactly — value **and** scale (`"10.00"` ≠ `"10.0"`). No floating-point
  types are used anywhere in decode, arithmetic or comparison; there is no epsilon.
* **Scale comes from the PIC.** A Java output value is canonicalized with
  `Canonical.of(value, picScale)`, which fails instead of rounding when the value has
  more fraction digits than the PIC allows.
* **COBOL arithmetic semantics are part of the expectation.** `COMPUTE` without
  `ROUNDED` truncates (e.g. `CBACT04C` interest
  `( TRAN-CAT-BAL * DIS-INT-RATE) / 1200`); results that exceed the receiving PIC lose
  high-order digits (no `ON SIZE ERROR` exists in the main app). The Java side must
  reproduce this; parity is still exact equality.
* **Byte level.** Output files are additionally compared byte-for-byte after
  decode→re-encode (`ByteRoundTripChecker`), which catches sign-nibble (`C` vs `F`),
  overpunch, padding and FILLER differences that a field comparison can mask.

## 2. Non-comparable-field policy

A field is non-comparable only when its value is legitimately run-dependent. Each
exclusion is declared **per test case** in
`test_harness/src/test/resources/comparison-policies.json`, never globally, and every
skipped field is listed in the comparison report (it is never silently dropped). For each
excluded field a substitute check is mandatory:

| Case | Field(s) | Why | Substitute check |
|---|---|---|---|
| `CBTRN02C-posted-transactions` | `TRAN-PROC-TS` | set from `FUNCTION CURRENT-DATE` at posting | format `YYYY-MM-DD-HH.MM.SS.nnnnnn`, not earlier than job start |
| `CBACT04C-interest-transactions` | `TRAN-ID`, `TRAN-ORIG-TS`, `TRAN-PROC-TS` | `TRAN-ID` = PARM date ‖ run suffix; timestamps = run time | `TRAN-ID` prefix = PARM date and suffix strictly increasing; timestamps well-formed |
| `CBEXPORT-export` | `EXPORT-TIMESTAMP` | export run time | identical across all records of one run; well-formed |
| Online replay (to be added with L3) | screen date/time fields, `EIBTASKN` | wall clock / task number | format only |

Generated keys are compared by **sequence pattern**, and business keys (account, card,
customer, transaction IDs from input) are always comparable.

---

## 3. Dataset-level checks

### 3.1 Domains and formats (`DS-DOM-*`)

Derived from the copybooks; enforced on golden data, on every intermediate file, and on
migrated outputs.

| Dataset (copybook, LRECL) | Field rules |
|---|---|
| `acctdata` (CVACT01Y, 300) | `ACCT-ID` 11 digits; `ACCT-ACTIVE-STATUS` ∈ {`Y`,`N`}; `ACCT-CURR-BAL`, `ACCT-CREDIT-LIMIT`, `ACCT-CASH-CREDIT-LIMIT`, `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT` signed `S9(10)V99`; `ACCT-OPEN-DATE`, `ACCT-EXPIRAION-DATE`, `ACCT-REISSUE-DATE` `YYYY-MM-DD` valid calendar dates (raw string preserved); `ACCT-CASH-CREDIT-LIMIT ≤ ACCT-CREDIT-LIMIT`; `ACCT-CURR-CYC-CREDIT ≥ 0`, `ACCT-CURR-CYC-DEBIT ≤ 0` after posting |
| `carddata` (CVACT02Y, 150) | `CARD-NUM` 16 digits; `CARD-ACCT-ID` 11 digits; `CARD-CVV-CD` 3 digits; `CARD-EXPIRAION-DATE` valid date; `CARD-ACTIVE-STATUS` ∈ {`Y`,`N`} |
| `cardxref` (CVACT03Y, 50) | `XREF-CARD-NUM` 16 digits; `XREF-CUST-ID` 9 digits; `XREF-ACCT-ID` 11 digits |
| `custdata` (CVCUS01Y, 500) | `CUST-ID` 9 digits; `CUST-SSN` 9 digits; `CUST-DOB-YYYY-MM-DD` valid date; `CUST-PRI-CARD-HOLDER-IND` ∈ {`Y`,`N`}; `CUST-FICO-CREDIT-SCORE` 300-850; state/country codes non-blank |
| `dailytran` (CVTRA06Y, 350) | `DALYTRAN-ID` 16 chars non-blank; `DALYTRAN-TYPE-CD` 2, `DALYTRAN-CAT-CD` 4 digits; `DALYTRAN-AMT` `S9(09)V99`; `DALYTRAN-CARD-NUM` 16 digits; `DALYTRAN-ORIG-TS`/`PROC-TS` 26-char timestamps |
| `discgrp` (CVTRA02Y, 50) | key (`DIS-ACCT-GROUP-ID`, `DIS-TRAN-TYPE-CD`, `DIS-TRAN-CAT-CD`); `DIS-INT-RATE` `S9(04)V99` ≥ 0; a `DEFAULT` group exists for every (type, category) used by `CBACT04C` |
| `tcatbal` (CVTRA01Y, 50) | key (`TRANCAT-ACCT-ID`, `TRANCAT-TYPE-CD`, `TRANCAT-CD`); `TRAN-CAT-BAL` `S9(09)V99` |
| `trancatg` (CVTRA04Y, 60) | key (`TRAN-TYPE-CD`, `TRAN-CAT-CD`); description non-blank |
| `trantype` (CVTRA03Y, 60) | `TRAN-TYPE` 2 chars; description non-blank |
| `export` (CVEXPORT, 500, EBCDIC) | `EXPORT-REC-TYPE` ∈ {`C`,`A`,`X`,`T`,`D`}; branch fields per the selected REDEFINES; `EXPORT-SEQUENCE-NUM` `COMP` unique |

Undecodable values (`$type`/`hex` objects) are domain failures unless listed in the
manifest for that dataset; in the seed data the only ones are LOW-VALUES in
`EXP-ACCT-ADDR-ZIP`/`EXP-ACCT-GROUP-ID` of export records 50 and 99.

### 3.2 Counts, control totals, keys (`DS-CNT-*`, `DS-TOT-*`, `DS-KEY-*`)

Golden values (from `manifest.json`; the harness asserts them every build):

| Dataset | Records | Key | Key range | Control totals |
|---|---:|---|---|---|
| acctdata | 50 | `ACCT-ID` | 00000000001 – 00000000050 | `ACCT-CURR-BAL` 12269.00; `ACCT-CREDIT-LIMIT` 233711.00; `ACCT-CASH-CREDIT-LIMIT` 122148.00; cycle credit/debit 0.00 |
| carddata | 50 | `CARD-NUM` | 0500024453765740 – 9805583408996588 | — |
| cardxref | 50 | `XREF-CARD-NUM` | same as carddata | — |
| custdata | 50 | `CUST-ID` | 000000001 – 000000050 | — |
| dailytran | 300 | `DALYTRAN-ID` | 0000000000683580 – 0000000996722787 | `DALYTRAN-AMT` 104801.54 |
| discgrp | 51 | group+type+cat | `A000000000/01/0001` – `ZEROAPR/07/0001` | `DIS-INT-RATE` 375.00 |
| tcatbal | 50 | acct+type+cat | | `TRAN-CAT-BAL` 0.00 |
| trancatg | 18 | type+cat | 01/0001 – 07/0001 | — |
| trantype | 7 | `TRAN-TYPE` | 01 – 07 | — |
| export | 500 | `EXPORT-SEQUENCE-NUM` | 1 – 509 (9 gaps) | `EXP-TRAN-AMT` 104801.54; `EXP-ACCT-CURR-BAL` 11583.00 |

* `DS-KEY-UNIQ`: primary keys unique in every dataset (all `true` today).
* `DS-KEY-SORT`: VSAM KSDS order is the EBCDIC collating sequence of the key bytes; files
  fed to group-break logic must be sorted as the JCL SORT step defines.
* `DS-SRC-SHA`: SHA-256 of each source file equals the manifest (detects fixture drift).
* Export snapshot vs ASCII seeds: transaction amounts reconcile (104801.54 both), account
  balances do not (11583.00 vs 12269.00 — the export was taken from a different account
  snapshot), so export ↔ seed reconciliation is restricted to counts and transaction totals.

### 3.3 Referential chain (`RI-*`)

Chain: `custdata → acctdata → carddata → cardxref → dailytran/transactions →
trancatg/trantype/discgrp/tcatbal`.

| ID | Check | Golden orphans |
|---|---|---:|
| RI-01 | `cardxref.XREF-ACCT-ID → acctdata.ACCT-ID` | 0 |
| RI-02 | `cardxref.XREF-CUST-ID → custdata.CUST-ID` | 0 |
| RI-03 | `cardxref.XREF-CARD-NUM → carddata.CARD-NUM` | 0 |
| RI-04 | `carddata.CARD-NUM → cardxref.XREF-CARD-NUM` | 0 |
| RI-05 | `carddata.CARD-ACCT-ID → acctdata.ACCT-ID` | 0 |
| RI-06 | `dailytran.DALYTRAN-CARD-NUM → cardxref.XREF-CARD-NUM` (CBTRN02C reason 100 if missing) | 0 |
| RI-07 | `dailytran.DALYTRAN-TYPE-CD → trantype.TRAN-TYPE` (CBTRN03C abend if missing) | 0 |
| RI-08 | `dailytran.(TYPE-CD,CAT-CD) → trancatg` (CBTRN03C abend if missing) | 0 |
| RI-09 | `trancatg.TRAN-TYPE-CD → trantype.TRAN-TYPE` | 0 |
| RI-10 | `tcatbal.TRANCAT-ACCT-ID → acctdata.ACCT-ID` | 0 |
| RI-11 | `tcatbal.(TYPE-CD,CD) → trancatg` | 0 |
| RI-12 | `discgrp.(TYPE-CD,CAT-CD) → trancatg` | 0 |
| RI-13 | `acctdata.ACCT-GROUP-ID → discgrp.DIS-ACCT-GROUP-ID` | **50** (all blank) |
| RI-14 | `acctdata.ACCT-ADDR-ZIP → discgrp.DIS-ACCT-GROUP-ID` (seed-data probe) | 0 |
| RI-15 | `acctdata.ACCT-ID → cardxref.XREF-ACCT-ID` (every account has a card) | 0 |
| RI-16 | `custdata.CUST-ID → cardxref.XREF-CUST-ID` (every customer has a card) | 0 |

RI-13 is an **expected, declared** exception: in the ASCII seed the group code sits in
the ZIP slot (RI-14), and `ACCT-GROUP-ID` is blank. It is not a migration defect —
`CBACT04C` resolves blank groups via its `DEFAULT` fallback — so the harness asserts the
exact orphan count (50) rather than zero. Any change to that count fails the build.

For outputs, the same checks run on after-images plus:
`TRANSACT.TRAN-CARD-NUM → cardxref` (posted transactions only reference known cards) and
`tcatbal` after-image keys ⊇ before-image keys.

---

## 4. Program-level checks (tied to the rule inventory)

Rule IDs refer to `test_harness/traceability/rule_inventory.csv`.

### 4.1 CBTRN02C — daily posting (`PG-POST-*`)

| ID | Check | Source |
|---|---|---|
| PG-POST-01 | `posted + rejected = DALYTRAN record count` (`WS-TRANSACTION-COUNT`) | lines 206-216 |
| PG-POST-02 | reject-reason parity: count per `WS-VALIDATION-FAIL-REASON` (100 invalid card, 101 account not found, 102 over limit, 103 after expiration, 109 account rewrite failed) equals reference | lines 385-417, 556 |
| PG-POST-03 | 102 rule: reject iff `ACCT-CURR-CYC-CREDIT − ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT > ACCT-CREDIT-LIMIT`; boundary `=` limit is accepted | lines 403-411 |
| PG-POST-04 | 103 rule: reject iff `ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10)` (string compare); both 102 and 103 are evaluated, 103 wins when both fail | lines 414-419 |
| PG-POST-05 | reject record = 350-byte input image + reason `9(04)` + description `X(76)` (430 bytes); input image byte-identical | 2500-WRITE-REJECT-REC |
| PG-POST-06 | return code 4 iff rejects > 0, else 0 | lines 228-231 |
| PG-POST-07 | balance recomputation per account: `new ACCT-CURR-BAL = old ACCT-CURR-BAL + Σ posted DALYTRAN-AMT` (payments are negative amounts, i.e. `+ Σ posted purchases − Σ payments`); `CYC-CREDIT += Σ amt ≥ 0`, `CYC-DEBIT += Σ amt < 0`; accounts with no posted transaction unchanged | 2800-UPDATE-ACCOUNT-REC, lines 547-552 |
| PG-POST-08 | category balances: for every posted transaction, `TRAN-CAT-BAL(acct,type,cat)` increases by the amount; missing key creates the record (status `23` path) | lines 500-530 |
| PG-POST-09 | posted TRANSACT records = accepted DALYTRAN records field-for-field (except `TRAN-PROC-TS`, §2) | 2900-WRITE-TRANSACTION-FILE |
| PG-POST-10 | Σ TRANSACT amounts = Σ accepted DALYTRAN amounts = Σ Δ`ACCT-CURR-BAL` = Σ Δ`TRAN-CAT-BAL` | cross-file |

Note the 109 path: the category balance has already been updated and the transaction is
still written when the account rewrite fails; parity must reproduce this ordering.

### 4.2 CBTRN03C — transaction report (`PG-RPT-*`)

| ID | Check | Source |
|---|---|---|
| PG-RPT-01 | date-range inclusion: a transaction is reported iff `WS-START-DATE ≤ TRAN-PROC-TS(1:10) ≤ WS-END-DATE`; both boundaries inclusive; first out-of-range record ends processing (`NEXT SENTENCE`, see strategy §2.4) | R003, lines 173-178 |
| PG-RPT-02 | page totals: each page total = Σ detail amounts on that page; page break when `MOD(WS-LINE-COUNTER, 20) = 0` before a detail; header, page-total and account-total lines count toward the counter | R021, lines 282-303 |
| PG-RPT-03 | account totals: one block per change of `TRAN-CARD-NUM` (suppressed on first record); value = Σ details since previous break | R005/R006, lines 181-188, 304-315 |
| PG-RPT-04 | grand total = Σ page totals (including the final page total written at EOF, which re-adds the last `TRAN-AMT`) | lines 197-205, 297 |
| PG-RPT-05 | Σ detail amounts = Σ filtered TRANSACT amounts; detail count = filtered record count | — |
| PG-RPT-06 | missing CARDXREF / TRANTYPE / TRANCATG key → abend 999, `IO-STATUS` 23 displayed as `0023`, report truncated at the failing record | R047-R049 |
| PG-RPT-07 | empty TRANSACT → no headers, no detail; totals per reference | R020 |

### 4.3 CBSTM03A / CBSTM03B — statements (`PG-STM-*`)

| ID | Check |
|---|---|
| PG-STM-01 | one statement per card/account in CARDXREF order; customer name/address from CUSTDATA, account data from ACCTDATA |
| PG-STM-02 | statement total = Σ detail transaction amounts for that card (`WS-TOTAL-AMT`) |
| PG-STM-03 | every TRANSACT record of a card appears exactly once (table limit: 51 cards × 10 transactions per card — overflow behavior compared against reference) |
| PG-STM-04 | text and HTML statement outputs are consistent (same totals, same transactions) |
| PG-STM-05 | `CBSTM03B` file-operation codes (open/read/read-key/close) return the same statuses as the reference |

### 4.4 CBACT04C — interest (`PG-INT-*`)

| ID | Check |
|---|---|
| PG-INT-01 | per tcatbal record: `WS-MONTHLY-INT = TRAN-CAT-BAL × DIS-INT-RATE / 1200`, truncated to the PIC scale |
| PG-INT-02 | disclosure group lookup by (`ACCT-GROUP-ID`, type, category); on status 23 retry with `DEFAULT`; failure of the default lookup abends |
| PG-INT-03 | one interest TRAN per non-zero category (type `01`, category `05`, source `System`), `TRAN-ID` = PARM date ‖ suffix |
| PG-INT-04 | per account: new `ACCT-CURR-BAL` = old + Σ monthly interest; `CYC-CREDIT` and `CYC-DEBIT` reset to 0 (including the last account, updated at EOF) |

### 4.5 CBEXPORT / CBIMPORT — round trip (`PG-EXP-*`)

| ID | Check |
|---|---|
| PG-EXP-01 | export record count per type = source dataset counts (C = custdata, A = acctdata, X = cardxref, T = transactions, D = carddata) |
| PG-EXP-02 | `EXPORT-SEQUENCE-NUM` unique and increasing in file order |
| PG-EXP-03 | import split files equal their source datasets field-for-field; Σ `EXP-TRAN-AMT` = Σ `TRAN-AMT` |
| PG-EXP-04 | unknown `EXPORT-REC-TYPE` → `2700-PROCESS-UNKNOWN-RECORD` (error file), counted, no abend |
| PG-EXP-05 | export file byte round trip (COMP sequence, COMP-3 amounts, OCCURS address/phone arrays) |

### 4.6 Online programs (`PG-ONL-*`)

For every `CO*` program: COMMAREA out = reference, BMS output fields and error message
text = reference, next transaction / `XCTL` target = reference, and VSAM after-images =
reference (e.g. `COACTUPC` account/customer update, `COBIL00C` payment posting
`new bal = old bal − payment`, `COTRN02C` new transaction ID = last ID + 1).

---

## 5. File-status and error-path parity (`FS-*`)

| ID | Requirement |
|---|---|
| FS-01 | Every `EVALUATE …-STATUS` / `IF …-STATUS = '00'` branch has a case: `'00'` success, `'10'` EOF, `'23'` record not found, `'35'` file missing, `'39'`/other → abend. |
| FS-02 | Abend parity: same abend code (999 via `CEE3ABD`), same point of failure (record number), same output written before the abend. |
| FS-03 | Displayed status text parity for the `9910-DISPLAY-IO-STATUS` decode: numeric statuses `00nn`; `'9'`-class or non-numeric statuses `9` + 3-digit binary value of the second byte (EBCDIC byte value on the reference platform). |
| FS-04 | Return codes per program and per JCL step equal the reference (e.g. CBTRN02C RC 4 with rejects); conditional JCL steps run/skip identically. |
| FS-05 | Error records (DALYREJS, CBIMPORT error file) compared like data files, including descriptions. |
| FS-06 | Java exceptions must be mapped to the COBOL outcome (status/abend/RC); an unmapped exception is a failure even if the output files match. |

---

## 6. Reporting

Each run produces a `ReconReport`: check ID, PASS/FAIL, expected vs actual values,
orphan keys (first N), field diffs with record key and path, and the list of skipped
non-comparable fields per case. A run is green only when every check passes.
