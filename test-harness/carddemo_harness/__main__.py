"""CLI: python -m carddemo_harness {golden|reconcile|compare|contract}."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .compare import compare_records
from .contract import validate
from .datasets import DATASETS, GOLDEN_DIR
from .golden import load_golden
from .jobs import all_checks, golden_records, regenerate
from .reconcile import summarize
from .records import load_layout


def _safe_path(path: str) -> Path:
    """Resolve a CLI path, refusing anything outside the current working directory."""
    base = Path.cwd().resolve()
    resolved = (base / path).resolve()
    if not resolved.is_relative_to(base):
        raise SystemExit(f"error: {path} is outside the working directory {base}")
    return resolved


def _records(path: str) -> list[dict]:
    data = json.loads(_safe_path(path).read_text(encoding="utf-8"))
    return data["records"] if isinstance(data, dict) and "records" in data else data


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(prog="carddemo_harness")
    sub = ap.add_subparsers(dest="cmd", required=True)
    sub.add_parser("golden", help="regenerate golden-files/ from app/data/ASCII")
    r = sub.add_parser("reconcile", help="run every reconciliation check against golden-files/")
    r.add_argument("--report", help="write JSON report to this path")
    c = sub.add_parser("compare", help="differential compare a migrated JSON extract with a golden dataset")
    c.add_argument("dataset", choices=sorted(DATASETS))
    c.add_argument("actual")
    c.add_argument("--ignore", nargs="*", default=[])
    k = sub.add_parser("contract", help="validate a migrated JSON extract against the copybook contract")
    k.add_argument("dataset", choices=sorted(DATASETS))
    k.add_argument("actual")
    a = ap.parse_args(argv)

    if a.cmd == "golden":
        regenerate(GOLDEN_DIR)
        print(f"golden files written to {GOLDEN_DIR}")
        return 0
    if a.cmd == "reconcile":
        checks = all_checks(golden_records())
        s = summarize(checks)
        for ch in checks:
            print(f"{'PASS' if ch.passed else 'FAIL'}  {ch.check_id}  expected={ch.expected} actual={ch.actual}")
        print(f"{s['passed']}/{s['total']} checks passed")
        if a.report:
            _safe_path(a.report).write_text(
                json.dumps({"summary": s, "checks": [x.as_dict() for x in checks]}, indent=2, default=str) + "\n"
            )
        return 0 if not s["failed"] else 1
    if a.cmd == "compare":
        g = load_golden(a.dataset)
        rep = compare_records(g["records"], _records(a.actual), g["key"], a.ignore)
        print(rep.summary())
        for m in rep.mismatches[:50]:
            print(f"  {m.key} {m.field}: expected={m.expected!r} actual={m.actual!r}")
        return 0 if rep.ok else 1
    if a.cmd == "contract":
        layout = load_layout(DATASETS[a.dataset])
        bad = 0
        for i, rec in enumerate(_records(a.actual)):
            for e in validate(rec, layout):
                bad += 1
                print(f"record {i}: {e}")
        print(f"{bad} contract violation(s)")
        return 0 if bad == 0 else 1
    return 2


if __name__ == "__main__":
    sys.exit(main())
