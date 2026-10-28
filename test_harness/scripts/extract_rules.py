#!/usr/bin/env python3
"""Deterministic business-rule inventory for CardDemo COBOL programs.

Scans the PROCEDURE DIVISION of every program in app/cbl and emits one row per
decision or arithmetic statement (IF, EVALUATE, WHEN, INVALID KEY, NOT INVALID KEY,
AT END, ON SIZE ERROR, PERFORM ... UNTIL, COMPUTE, ADD, SUBTRACT, MULTIPLY, DIVIDE).
Each row is attributed to its enclosing paragraph and gets a stable rule ID and a
planned test-case ID, producing the rule-to-test traceability matrix.

Usage:
  python3 test_harness/scripts/extract_rules.py           # (re)write the matrix
  python3 test_harness/scripts/extract_rules.py --check   # fail if the matrix is stale
"""

import csv
import io
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE_DIRS = [ROOT / "app" / "cbl"]
OUT_CSV = ROOT / "test_harness" / "traceability" / "rule_inventory.csv"
OUT_SUMMARY = ROOT / "test_harness" / "traceability" / "rule_inventory_summary.md"

KINDS = [
    ("NOT-INVALID-KEY", re.compile(r"\bNOT\s+INVALID\s+KEY\b")),
    ("INVALID-KEY", re.compile(r"(?<!NOT )\bINVALID\s+KEY\b")),
    ("NOT-ON-SIZE-ERROR", re.compile(r"\bNOT\s+ON\s+SIZE\s+ERROR\b")),
    ("ON-SIZE-ERROR", re.compile(r"(?<!NOT )\bON\s+SIZE\s+ERROR\b")),
    ("AT-END", re.compile(r"(?<!NOT )\bAT\s+END\b(?!-)")),
    ("EVALUATE", re.compile(r"(?<![\w-])EVALUATE\b")),
    ("WHEN", re.compile(r"(?<![\w-])WHEN\b")),
    ("IF", re.compile(r"(?<![\w-])IF\b")),
    ("PERFORM-UNTIL", re.compile(r"(?<![\w-])PERFORM\b.*\bUNTIL\b")),
    ("COMPUTE", re.compile(r"(?<![\w-])COMPUTE\b")),
    ("ADD", re.compile(r"(?<![\w-])ADD\b")),
    ("SUBTRACT", re.compile(r"(?<![\w-])SUBTRACT\b")),
    ("MULTIPLY", re.compile(r"(?<![\w-])MULTIPLY\b")),
    ("DIVIDE", re.compile(r"(?<![\w-])DIVIDE\b")),
]
CONDITIONAL_KINDS = {"IF", "EVALUATE", "WHEN", "INVALID-KEY", "NOT-INVALID-KEY", "AT-END",
                     "ON-SIZE-ERROR", "NOT-ON-SIZE-ERROR", "PERFORM-UNTIL"}
PARAGRAPH = re.compile(r"^([A-Z0-9][A-Z0-9-]*)(\s+SECTION)?\s*\.\s*$")
STRING_LITERAL = re.compile(r"'[^']*'|\"[^\"]*\"")
EXEC_START = re.compile(r"\bEXEC\s+(CICS|SQL|DLI)\b")


def program_layer(name):
    if name.startswith("CO"):
        return "L1-online"
    if name.startswith("CS"):
        return "L1-utility"
    return "L1-batch"


def chain_level(name):
    if name in {"CBTRN01C", "CBTRN02C", "CBTRN03C", "CBSTM03A", "CBSTM03B"}:
        return "L2-posting-chain"
    if name in {"CBEXPORT", "CBIMPORT"}:
        return "L2-export-import"
    if name.startswith("CO"):
        return "L3-online-replay"
    return "L3-nightly-batch"


def code_area(raw):
    if len(raw) < 7 or raw[6] in "*/":
        return None
    return raw[7:72].rstrip() if len(raw) > 7 else ""


def scan(path):
    program = path.stem.upper()
    rows = []
    in_procedure = False
    paragraph = "(MAIN)"
    in_exec = False
    lines = path.read_text(encoding="latin-1").splitlines()
    for lineno, raw in enumerate(lines, start=1):
        code = code_area(raw)
        if code is None:
            continue
        upper = code.upper()
        if not in_procedure:
            in_procedure = "PROCEDURE DIVISION" in upper
            continue
        stripped = upper.strip()
        if in_exec:
            in_exec = "END-EXEC" not in upper
            continue
        if EXEC_START.search(upper) and "END-EXEC" not in upper:
            in_exec = True
            continue
        if raw[7:8].strip() and PARAGRAPH.match(stripped):
            paragraph = PARAGRAPH.match(stripped).group(1)
            continue
        scrubbed = STRING_LITERAL.sub("''", upper)
        for kind, pattern in KINDS:
            if pattern.search(scrubbed):
                rows.append({
                    "program": program,
                    "paragraph": paragraph,
                    "line": lineno,
                    "kind": kind,
                    "category": "decision" if kind in CONDITIONAL_KINDS else "arithmetic",
                    "statement": " ".join(stripped.split()),
                })
                break
    return rows


def build():
    sources = sorted(p for d in SOURCE_DIRS for p in d.iterdir()
                     if p.suffix.lower() == ".cbl")
    rows = []
    for path in sources:
        program_rows = scan(path)
        for index, row in enumerate(program_rows, start=1):
            row["rule_id"] = f"{row['program']}-R{index:03d}"
            row["source"] = path.relative_to(ROOT).as_posix()
            row["test_layer"] = program_layer(row["program"])
            row["test_case_id"] = f"{row['test_layer'].upper()}-{row['rule_id']}"
            row["chain_level"] = chain_level(row["program"])
            row["status"] = "PLANNED"
        rows.extend(program_rows)
    columns = ["rule_id", "program", "source", "paragraph", "line", "kind", "category",
               "statement", "test_layer", "test_case_id", "chain_level", "status"]
    buffer = io.StringIO()
    writer = csv.DictWriter(buffer, fieldnames=columns, lineterminator="\n")
    writer.writeheader()
    writer.writerows(rows)

    programs = [p.stem.upper() for p in sources]
    kinds = [k for k, _ in KINDS]
    summary = ["# Rule inventory summary",
               "",
               "Generated by `test_harness/scripts/extract_rules.py`; do not edit by hand.",
               "",
               f"Programs scanned: {len(programs)}; rules extracted: {len(rows)}.",
               "",
               "| Program | Paragraphs | " + " | ".join(kinds) + " | Total |",
               "|---|---:|" + "---:|" * len(kinds) + "---:|"]
    for program in programs:
        prog_rows = [r for r in rows if r["program"] == program]
        counts = [sum(1 for r in prog_rows if r["kind"] == k) for k in kinds]
        paragraphs = len({r["paragraph"] for r in prog_rows})
        summary.append(f"| {program} | {paragraphs} | "
                       + " | ".join(str(c) for c in counts) + f" | {len(prog_rows)} |")
    totals = [sum(1 for r in rows if r["kind"] == k) for k in kinds]
    summary.append("| **Total** | | " + " | ".join(str(c) for c in totals)
                   + f" | {len(rows)} |")
    return buffer.getvalue(), "\n".join(summary) + "\n"


def main():
    csv_text, summary_text = build()
    if "--check" in sys.argv:
        stale = [p for p, t in ((OUT_CSV, csv_text), (OUT_SUMMARY, summary_text))
                 if not p.exists() or p.read_text(encoding="utf-8") != t]
        if stale:
            print("stale: " + ", ".join(str(p.relative_to(ROOT)) for p in stale))
            return 1
        print("rule inventory up to date")
        return 0
    OUT_CSV.write_text(csv_text, encoding="utf-8")
    OUT_SUMMARY.write_text(summary_text, encoding="utf-8")
    print(f"wrote {OUT_CSV.relative_to(ROOT)} and {OUT_SUMMARY.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
