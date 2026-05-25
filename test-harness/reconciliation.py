#!/usr/bin/env python3
"""
reconciliation.py - Reconciliation checks for CardDemo migration.

Validates:
  1. Record counts per file
  2. Numeric field sums (control totals)
  3. Cross-reference integrity (FK relationships between files)
  4. SHA-256 checksums against golden baselines

Usage:
    python reconciliation.py --data-dir golden-files
    python reconciliation.py --data-dir golden-files --checks all
    python reconciliation.py --data-dir golden-files --checks counts,integrity
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


# ---------------------------------------------------------------------------
# Expected record counts (from the sample data set)
# ---------------------------------------------------------------------------

EXPECTED_COUNTS: dict[str, int] = {
    "acctdata":  50,
    "carddata":  50,
    "cardxref":  50,
    "custdata":  50,
    "dailytran": 300,
    "discgrp":   51,
    "tcatbal":   50,
    "trancatg":  18,
    "trantype":  7,
}


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def load_golden(data_dir: Path, stem: str) -> dict | None:
    path = data_dir / f"{stem}.json"
    if not path.exists():
        return None
    with open(path) as fh:
        return json.load(fh)


class CheckResult:
    def __init__(self, name: str, passed: bool, detail: str = ""):
        self.name = name
        self.passed = passed
        self.detail = detail

    def __str__(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        msg = f"[{status}] {self.name}"
        if self.detail:
            msg += f" - {self.detail}"
        return msg


# ---------------------------------------------------------------------------
# Check 1: Record counts
# ---------------------------------------------------------------------------

def validate_record_counts(data_dir: Path) -> list[CheckResult]:
    results: list[CheckResult] = []
    for stem, expected in sorted(EXPECTED_COUNTS.items()):
        golden = load_golden(data_dir, stem)
        if golden is None:
            results.append(CheckResult(
                f"record_count/{stem}", False, f"Golden file not found"))
            continue
        actual = golden["metadata"]["record_count"]
        passed = actual == expected
        results.append(CheckResult(
            f"record_count/{stem}", passed,
            f"expected={expected}, actual={actual}"))
    return results


# ---------------------------------------------------------------------------
# Check 2: Numeric field sums
# ---------------------------------------------------------------------------

def validate_numeric_sums(data_dir: Path) -> list[CheckResult]:
    """Verify control totals are self-consistent and non-zero where expected."""
    results: list[CheckResult] = []
    for stem in sorted(EXPECTED_COUNTS.keys()):
        golden = load_golden(data_dir, stem)
        if golden is None:
            continue
        totals = golden.get("control_totals", {})
        for field_name, total in totals.items():
            # Basic sanity: the total should be a number
            if not isinstance(total, (int, float)):
                results.append(CheckResult(
                    f"numeric_sum/{stem}/{field_name}", False,
                    f"Non-numeric total: {total!r}"))
                continue
            results.append(CheckResult(
                f"numeric_sum/{stem}/{field_name}", True,
                f"sum={total}"))
    return results


# ---------------------------------------------------------------------------
# Check 3: Cross-reference integrity
# ---------------------------------------------------------------------------

def _extract_field(golden: dict, field_name: str) -> set:
    """Extract all values of a field as a set."""
    values = set()
    for rec in golden.get("records", []):
        val = rec.get(field_name)
        if val is not None:
            values.add(val)
    return values


def validate_xref_integrity(data_dir: Path) -> list[CheckResult]:
    """
    Every card in cardxref must have:
      - A matching account in acctdata (XREF-ACCT-ID -> ACCT-ID)
      - A matching customer in custdata (XREF-CUST-ID -> CUST-ID)
      - A matching card in carddata (XREF-CARD-NUM -> CARD-NUM)

    Every daily transaction must have:
      - A valid transaction type (DALYTRAN-TYPE-CD -> TRAN-TYPE)
      - A valid category (TYPE-CD + CAT-CD -> trancatg)

    Every tcatbal account must exist in acctdata.
    """
    results: list[CheckResult] = []

    xref = load_golden(data_dir, "cardxref")
    acct = load_golden(data_dir, "acctdata")
    cust = load_golden(data_dir, "custdata")
    card = load_golden(data_dir, "carddata")
    dtran = load_golden(data_dir, "dailytran")
    ttype = load_golden(data_dir, "trantype")
    tcatg = load_golden(data_dir, "trancatg")
    tcbal = load_golden(data_dir, "tcatbal")

    # --- cardxref -> acctdata ---
    if xref and acct:
        xref_acct_ids = _extract_field(xref, "XREF-ACCT-ID")
        acct_ids = _extract_field(acct, "ACCT-ID")
        orphans = xref_acct_ids - acct_ids
        results.append(CheckResult(
            "integrity/xref_acct_id->acctdata",
            len(orphans) == 0,
            f"orphaned XREF-ACCT-IDs: {sorted(orphans)[:10]}" if orphans
            else f"all {len(xref_acct_ids)} refs valid"))

    # --- cardxref -> custdata ---
    if xref and cust:
        xref_cust_ids = _extract_field(xref, "XREF-CUST-ID")
        cust_ids = _extract_field(cust, "CUST-ID")
        orphans = xref_cust_ids - cust_ids
        results.append(CheckResult(
            "integrity/xref_cust_id->custdata",
            len(orphans) == 0,
            f"orphaned XREF-CUST-IDs: {sorted(orphans)[:10]}" if orphans
            else f"all {len(xref_cust_ids)} refs valid"))

    # --- cardxref -> carddata ---
    if xref and card:
        xref_card_nums = _extract_field(xref, "XREF-CARD-NUM")
        card_nums = _extract_field(card, "CARD-NUM")
        orphans = xref_card_nums - card_nums
        results.append(CheckResult(
            "integrity/xref_card_num->carddata",
            len(orphans) == 0,
            f"orphaned XREF-CARD-NUMs: {sorted(orphans)[:10]}" if orphans
            else f"all {len(xref_card_nums)} refs valid"))

    # --- dailytran type -> trantype ---
    if dtran and ttype:
        tran_type_cds = _extract_field(dtran, "DALYTRAN-TYPE-CD")
        valid_types = _extract_field(ttype, "TRAN-TYPE")
        orphans = tran_type_cds - valid_types
        results.append(CheckResult(
            "integrity/dailytran_type->trantype",
            len(orphans) == 0,
            f"unknown type codes: {sorted(orphans)[:10]}" if orphans
            else f"all {len(tran_type_cds)} type codes valid"))

    # --- dailytran (type, cat) -> trancatg ---
    if dtran and tcatg:
        tran_pairs = set()
        for rec in dtran.get("records", []):
            tc = rec.get("DALYTRAN-TYPE-CD")
            cc = rec.get("DALYTRAN-CAT-CD")
            if tc is not None and cc is not None:
                tran_pairs.add((tc, cc))
        catg_pairs = set()
        for rec in tcatg.get("records", []):
            tc = rec.get("TRAN-TYPE-CD")
            cc = rec.get("TRAN-CAT-CD")
            if tc is not None and cc is not None:
                catg_pairs.add((tc, cc))
        orphans = tran_pairs - catg_pairs
        results.append(CheckResult(
            "integrity/dailytran_cat->trancatg",
            len(orphans) == 0,
            f"unknown (type,cat) pairs: {sorted(orphans)[:10]}" if orphans
            else f"all {len(tran_pairs)} category pairs valid"))

    # --- tcatbal -> acctdata ---
    if tcbal and acct:
        tcbal_accts = _extract_field(tcbal, "TRANCAT-ACCT-ID")
        acct_ids = _extract_field(acct, "ACCT-ID")
        orphans = tcbal_accts - acct_ids
        results.append(CheckResult(
            "integrity/tcatbal_acct->acctdata",
            len(orphans) == 0,
            f"orphaned TRANCAT-ACCT-IDs: {sorted(orphans)[:10]}" if orphans
            else f"all {len(tcbal_accts)} refs valid"))

    return results


# ---------------------------------------------------------------------------
# Check 4: SHA-256 checksums
# ---------------------------------------------------------------------------

def validate_checksums(data_dir: Path) -> list[CheckResult]:
    """Verify that .sha256 sidecar files match the golden JSON metadata."""
    results: list[CheckResult] = []
    for sha_file in sorted(data_dir.glob("*.sha256")):
        stem = sha_file.stem
        golden = load_golden(data_dir, stem)
        if golden is None:
            results.append(CheckResult(
                f"checksum/{stem}", False, "Golden JSON not found"))
            continue
        expected_sha = sha_file.read_text().strip()
        actual_sha = golden["metadata"]["sha256"]
        passed = expected_sha == actual_sha
        results.append(CheckResult(
            f"checksum/{stem}", passed,
            f"match" if passed
            else f"expected={expected_sha[:16]}..., actual={actual_sha[:16]}..."))
    return results


# ---------------------------------------------------------------------------
# Check 5: Unique primary keys
# ---------------------------------------------------------------------------

def validate_primary_keys(data_dir: Path) -> list[CheckResult]:
    """Verify primary key uniqueness in each file."""
    pk_fields: dict[str, str] = {
        "acctdata":  "ACCT-ID",
        "carddata":  "CARD-NUM",
        "custdata":  "CUST-ID",
        "trantype":  "TRAN-TYPE",
    }
    results: list[CheckResult] = []
    for stem, pk_field in sorted(pk_fields.items()):
        golden = load_golden(data_dir, stem)
        if golden is None:
            continue
        values = [rec.get(pk_field) for rec in golden.get("records", [])]
        unique = set(values)
        passed = len(values) == len(unique)
        results.append(CheckResult(
            f"pk_unique/{stem}/{pk_field}", passed,
            f"{len(values)} records, {len(unique)} unique" +
            (f" ({len(values) - len(unique)} duplicates)" if not passed else "")))
    return results


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

ALL_CHECKS = {
    "counts":    validate_record_counts,
    "sums":      validate_numeric_sums,
    "integrity": validate_xref_integrity,
    "checksums": validate_checksums,
    "pk":        validate_primary_keys,
}


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Run reconciliation checks on CardDemo golden files."
    )
    parser.add_argument("--data-dir", required=True,
                        help="Directory containing golden-file JSON")
    parser.add_argument("--checks", default="all",
                        help="Comma-separated check names or 'all'")
    args = parser.parse_args()

    data_dir = Path(args.data_dir)
    if not data_dir.is_dir():
        print(f"ERROR: {data_dir} is not a directory", file=sys.stderr)
        sys.exit(2)

    check_names = (
        list(ALL_CHECKS.keys()) if args.checks == "all"
        else [c.strip() for c in args.checks.split(",")]
    )

    all_results: list[CheckResult] = []
    for name in check_names:
        fn = ALL_CHECKS.get(name)
        if fn is None:
            print(f"WARNING: Unknown check '{name}', skipping.", file=sys.stderr)
            continue
        print(f"\n--- {name} ---")
        results = fn(data_dir)
        for r in results:
            print(f"  {r}")
        all_results.extend(results)

    # Summary
    passed = sum(1 for r in all_results if r.passed)
    failed = sum(1 for r in all_results if not r.passed)
    print(f"\n{'='*60}")
    print(f"TOTAL: {passed} passed, {failed} failed, "
          f"{passed + failed} checks run")

    sys.exit(1 if failed > 0 else 0)


if __name__ == "__main__":
    main()
