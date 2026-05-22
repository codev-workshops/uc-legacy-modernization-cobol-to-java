#!/usr/bin/env python3
"""
field_comparator.py - Compare two sets of parsed COBOL data (golden vs actual)
field-by-field and report mismatches with field names and positions.

Usage:
    python field_comparator.py --expected golden-files --actual output/java
    python field_comparator.py --expected golden-files/acctdata.json \
                               --actual   output/java/acctdata.json
"""
from __future__ import annotations

import argparse
import json
import sys
from dataclasses import dataclass
from pathlib import Path


NUMERIC_EPSILON = 0.005


@dataclass
class Mismatch:
    """A single field-level mismatch."""
    file: str
    record_index: int
    field_name: str
    field_position: int
    field_length: int
    expected: object
    actual: object
    mismatch_type: str  # "value", "type", "missing_field", "extra_field"

    def __str__(self) -> str:
        return (
            f"[{self.file}] record {self.record_index}, "
            f"field '{self.field_name}' (pos {self.field_position}, "
            f"len {self.field_length}): "
            f"{self.mismatch_type} - expected={self.expected!r}, "
            f"actual={self.actual!r}"
        )


def _field_index(fields: list[dict]) -> dict[str, dict]:
    """Build a name -> field-def lookup from the fields array."""
    return {f["name"]: f for f in fields}


def _values_match(expected: object, actual: object) -> bool:
    """Compare two field values with numeric tolerance."""
    if isinstance(expected, (int, float)) and isinstance(actual, (int, float)):
        return abs(expected - actual) < NUMERIC_EPSILON
    return expected == actual


def compare_files(
    expected_path: Path,
    actual_path: Path,
) -> list[Mismatch]:
    """Compare two golden-file JSON files field-by-field."""
    with open(expected_path) as fh:
        expected = json.load(fh)
    with open(actual_path) as fh:
        actual = json.load(fh)

    file_name = expected_path.stem
    mismatches: list[Mismatch] = []

    exp_fields = _field_index(expected.get("fields", []))
    act_fields = _field_index(actual.get("fields", []))

    # Check metadata
    exp_meta = expected.get("metadata", {})
    act_meta = actual.get("metadata", {})
    if exp_meta.get("record_count") != act_meta.get("record_count"):
        mismatches.append(Mismatch(
            file=file_name, record_index=-1,
            field_name="__record_count__", field_position=0, field_length=0,
            expected=exp_meta.get("record_count"),
            actual=act_meta.get("record_count"),
            mismatch_type="record_count",
        ))

    # Compare records
    exp_records = expected.get("records", [])
    act_records = actual.get("records", [])
    max_records = max(len(exp_records), len(act_records))

    for i in range(max_records):
        if i >= len(exp_records):
            mismatches.append(Mismatch(
                file=file_name, record_index=i,
                field_name="__extra_record__", field_position=0, field_length=0,
                expected=None, actual="<extra record>",
                mismatch_type="extra_record",
            ))
            continue
        if i >= len(act_records):
            mismatches.append(Mismatch(
                file=file_name, record_index=i,
                field_name="__missing_record__", field_position=0, field_length=0,
                expected="<expected record>", actual=None,
                mismatch_type="missing_record",
            ))
            continue

        exp_rec = exp_records[i]
        act_rec = act_records[i]

        all_keys = set(exp_rec.keys()) | set(act_rec.keys())
        for key in sorted(all_keys):
            fdef = exp_fields.get(key) or act_fields.get(key) or {}
            pos = fdef.get("start", -1)
            length = fdef.get("length", -1)

            if key not in exp_rec:
                mismatches.append(Mismatch(
                    file=file_name, record_index=i,
                    field_name=key, field_position=pos, field_length=length,
                    expected=None, actual=act_rec[key],
                    mismatch_type="extra_field",
                ))
            elif key not in act_rec:
                mismatches.append(Mismatch(
                    file=file_name, record_index=i,
                    field_name=key, field_position=pos, field_length=length,
                    expected=exp_rec[key], actual=None,
                    mismatch_type="missing_field",
                ))
            elif not _values_match(exp_rec[key], act_rec[key]):
                mismatches.append(Mismatch(
                    file=file_name, record_index=i,
                    field_name=key, field_position=pos, field_length=length,
                    expected=exp_rec[key], actual=act_rec[key],
                    mismatch_type="value",
                ))

    # Compare control totals
    exp_totals = expected.get("control_totals", {})
    act_totals = actual.get("control_totals", {})
    for key in sorted(set(exp_totals) | set(act_totals)):
        exp_val = exp_totals.get(key)
        act_val = act_totals.get(key)
        if exp_val is not None and act_val is not None:
            if not _values_match(exp_val, act_val):
                mismatches.append(Mismatch(
                    file=file_name, record_index=-1,
                    field_name=f"__control_total__{key}",
                    field_position=0, field_length=0,
                    expected=exp_val, actual=act_val,
                    mismatch_type="control_total",
                ))

    return mismatches


def compare_directories(
    expected_dir: Path,
    actual_dir: Path,
) -> dict[str, list[Mismatch]]:
    """Compare all matching JSON files in two directories."""
    results: dict[str, list[Mismatch]] = {}
    for exp_file in sorted(expected_dir.glob("*.json")):
        act_file = actual_dir / exp_file.name
        if not act_file.exists():
            results[exp_file.stem] = [Mismatch(
                file=exp_file.stem, record_index=-1,
                field_name="__file__", field_position=0, field_length=0,
                expected=str(exp_file), actual=None,
                mismatch_type="missing_file",
            )]
            continue
        results[exp_file.stem] = compare_files(exp_file, act_file)
    return results


def print_report(results: dict[str, list[Mismatch]]) -> int:
    """Print a human-readable comparison report. Returns exit code."""
    total_mismatches = 0
    for file_name, mismatches in sorted(results.items()):
        if not mismatches:
            print(f"PASS  {file_name}: all fields match")
        else:
            print(f"FAIL  {file_name}: {len(mismatches)} mismatch(es)")
            for m in mismatches[:50]:  # cap output
                print(f"  {m}")
            if len(mismatches) > 50:
                print(f"  ... and {len(mismatches) - 50} more")
            total_mismatches += len(mismatches)

    print(f"\nSummary: {len(results)} file(s) compared, "
          f"{total_mismatches} total mismatch(es)")
    return 1 if total_mismatches > 0 else 0


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Field-by-field comparison of COBOL data golden files."
    )
    parser.add_argument("--expected", required=True,
                        help="Expected golden-file JSON or directory")
    parser.add_argument("--actual", required=True,
                        help="Actual output JSON or directory")
    args = parser.parse_args()

    expected = Path(args.expected)
    actual = Path(args.actual)

    if expected.is_file() and actual.is_file():
        mismatches = compare_files(expected, actual)
        results = {expected.stem: mismatches}
    elif expected.is_dir() and actual.is_dir():
        results = compare_directories(expected, actual)
    else:
        print("ERROR: --expected and --actual must both be files or both be "
              "directories.", file=sys.stderr)
        sys.exit(2)

    exit_code = print_report(results)
    sys.exit(exit_code)


if __name__ == "__main__":
    main()
