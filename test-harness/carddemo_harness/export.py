"""CBEXPORT multi-record export file (CVEXPORT.cpy): EBCDIC with COMP and COMP-3 fields.

Each 500-byte record has a common header and one of five REDEFINES bodies selected by EXPORT-REC-TYPE.
"""

from __future__ import annotations

import hashlib
from pathlib import Path

from .copybook import Field, find_field, occurrence_fields, parse_copybook
from .datasets import CPY_DIR, EBCDIC_DIR
from .records import ParseIssue, ParseResult, encode_record_bytes, parse_record_bytes, split_fixed

COPYBOOK = "CVEXPORT.cpy"
EXPORT_FILE = "AWS.M2.CARDDEMO.EXPORT.DATA.PS"
RECORD_LENGTH = 500
CODEPAGE = "cp037"

# The export unload in app/data/EBCDIC was taken from a slightly different snapshot of the masters than the ASCII
# sample files: these keys differ field by field (edited names/addresses, balances and dates; low-values in
# ACCT-ADDR-ZIP/ACCT-GROUP-ID). Any other divergence fails the CBEXPORT parity checks.
KNOWN_SNAPSHOT_DIFFS: dict[str, list[str]] = {
    "customer": ["1", "50"],
    "account": ["1", "49", "50"],
    "card_xref": [],
    "card": [],
    "transaction": [],
}

# EXPORT-REC-TYPE -> (REDEFINES body, master dataset it was exported from, export field prefix, master prefix)
RECORD_TYPES: dict[str, tuple[str, str, str, str]] = {
    "C": ("EXPORT-CUSTOMER-DATA", "customer", "EXP-CUST-", "CUST-"),
    "A": ("EXPORT-ACCOUNT-DATA", "account", "EXP-ACCT-", "ACCT-"),
    "X": ("EXPORT-CARD-XREF-DATA", "card_xref", "EXP-XREF-", "XREF-"),
    "T": ("EXPORT-TRANSACTION-DATA", "transaction", "EXP-TRAN-", "TRAN-"),
    "D": ("EXPORT-CARD-DATA", "card", "EXP-CARD-", "CARD-"),
}


def load_export_layout() -> Field:
    return parse_copybook(CPY_DIR / COPYBOOK)


def header_fields(layout: Field) -> list[Field]:
    return [f for f in occurrence_fields(layout) if f.name != "EXPORT-RECORD-DATA"]


def body_fields(layout: Field, rec_type: str) -> list[Field]:
    return occurrence_fields(find_field(layout, RECORD_TYPES[rec_type][0]))


def parse_export_record(raw: bytes, layout: Field, rec_no: int = 0, issues: list | None = None) -> dict:
    rec = parse_record_bytes(raw, header_fields(layout), CODEPAGE, rec_no, issues)
    rec_type = rec["EXPORT-REC-TYPE"]
    if rec_type not in RECORD_TYPES:
        if issues is not None:
            issues.append(ParseIssue(rec_no, "EXPORT-REC-TYPE", f"unknown record type {rec_type!r}"))
        return rec
    rec.update(parse_record_bytes(raw, body_fields(layout, rec_type), CODEPAGE, rec_no, issues))
    return rec


def parse_export(path: Path | None = None) -> ParseResult:
    layout = load_export_layout()
    if layout.length != RECORD_LENGTH:
        raise ValueError(f"{COPYBOOK} length {layout.length} != {RECORD_LENGTH}")
    result = ParseResult("export")
    for i, raw in enumerate(split_fixed((path or EBCDIC_DIR / EXPORT_FILE).read_bytes(), RECORD_LENGTH), 1):
        result.raw_lengths.add(len(raw))
        result.records.append(parse_export_record(raw, layout, i, result.issues))
    return result


def encode_export_record(rec: dict, layout: Field) -> bytes:
    fields = header_fields(layout) + body_fields(layout, rec["EXPORT-REC-TYPE"])
    return encode_record_bytes(rec, fields, RECORD_LENGTH, CODEPAGE)


def to_master(rec: dict) -> dict:
    """Rename an export body back to the master-file field names (what CBIMPORT does).

    ``EXP-CUST-ADDR-LINE(2)`` becomes ``CUST-ADDR-LINE-2``.
    """
    _, _, exp_prefix, master_prefix = RECORD_TYPES[rec["EXPORT-REC-TYPE"]]
    out = {}
    for k, v in rec.items():
        if k.startswith(exp_prefix):
            name = master_prefix + k[len(exp_prefix) :]
            out[name.replace("(", "-").rstrip(")")] = v
    return out


def split_by_type(records: list[dict]) -> dict[str, list[dict]]:
    """Group export records by the master dataset they came from, renamed to master field names."""
    out: dict[str, list[dict]] = {RECORD_TYPES[t][1]: [] for t in RECORD_TYPES}
    for r in records:
        if r.get("EXPORT-REC-TYPE") in RECORD_TYPES:
            out[RECORD_TYPES[r["EXPORT-REC-TYPE"]][1]].append(to_master(r))
    return out


def record_layout(layout: Field, rec_type: str) -> Field:
    """Flat layout (header + one REDEFINES body) for contract checks and JSON Schemas of one record type."""
    root = Field(level=0, name=f"EXPORT-{rec_type}", offset=0, length=RECORD_LENGTH)
    root.children = header_fields(layout) + body_fields(layout, rec_type)
    return root


def _layout_meta(fields: list[Field]) -> list[dict]:
    return [
        {
            "name": f.name,
            "offset": f.offset,
            "length": f.length,
            "pic": f.pic,
            "usage": f.usage or "DISPLAY",
            "kind": f.kind,
            "scale": f.scale,
            "signed": f.signed,
        }
        for f in fields
        if f.name != "FILLER"
    ]


def build_export_golden(path: Path | None = None) -> dict:
    from collections import Counter
    from decimal import Decimal

    src = path or EBCDIC_DIR / EXPORT_FILE
    res = parse_export(src)
    layout = load_export_layout()
    by_type = Counter(r["EXPORT-REC-TYPE"] for r in res.records)

    def total(t: str, f: str) -> str:
        return str(sum((Decimal(r[f]) for r in res.records if r["EXPORT-REC-TYPE"] == t), Decimal("0")))

    return {
        "dataset": "export",
        "source_file": f"app/data/EBCDIC/{src.name}",
        "source_sha256": hashlib.sha256(src.read_bytes()).hexdigest(),
        "copybook": f"app/cpy/{COPYBOOK}",
        "codepage": CODEPAGE,
        "record_length": RECORD_LENGTH,
        "key": ["EXPORT-SEQUENCE-NUM"],
        "record_types": {t: {"body": b, "source": m} for t, (b, m, _, _) in RECORD_TYPES.items()},
        "layout": {
            "header": _layout_meta(header_fields(layout)),
            **{t: _layout_meta(body_fields(layout, t)) for t in RECORD_TYPES},
        },
        "control_totals": {
            "record_count": len(res.records),
            "by_type": dict(sorted(by_type.items())),
            "sums": {
                "EXP-ACCT-CURR-BAL": total("A", "EXP-ACCT-CURR-BAL"),
                "EXP-ACCT-CREDIT-LIMIT": total("A", "EXP-ACCT-CREDIT-LIMIT"),
                "EXP-ACCT-CASH-CREDIT-LIMIT": total("A", "EXP-ACCT-CASH-CREDIT-LIMIT"),
                "EXP-ACCT-CURR-CYC-DEBIT": total("A", "EXP-ACCT-CURR-CYC-DEBIT"),
                "EXP-TRAN-AMT": total("T", "EXP-TRAN-AMT"),
            },
        },
        "known_snapshot_diffs": KNOWN_SNAPSHOT_DIFFS,
        "parse_issues": [vars(i) for i in res.issues],
        "records": res.records,
    }
