"""Run the reference models over the golden inputs and build per-job expected outputs and reconciliation results."""

from __future__ import annotations

import json
from pathlib import Path

from .contract import json_schema
from .datasets import DATASETS, EBCDIC_DIR, EBCDIC_FILES, GOLDEN_DIR, KNOWN_EBCDIC_DIFFS
from .export import (
    CODEPAGE,
    EXPORT_FILE,
    RECORD_LENGTH,
    RECORD_TYPES,
    encode_export_record,
    load_export_layout,
    parse_export,
    record_layout,
)
from .golden import load_golden, write_all
from .models import calculate_interest, post_daily_transactions
from .reconcile import (
    ebcdic_checks,
    export_checks,
    intcalc_checks,
    integrity_checks,
    load_checks,
    posttran_checks,
    summarize,
)
from .records import load_layout, parse_binary_dataset, split_fixed

PARM_DATE = "2022-07-21"
LOAD_JOBS = {
    "ACCTFILE": "account",
    "CARDFILE": "card",
    "CUSTFILE": "customer",
    "XREFFILE": "card_xref",
    "TRANTYPE": "transaction_type",
    "TRANCATG": "transaction_category",
    "DISCGRP": "disclosure_group",
    "TCATBALF": "category_balance",
}


def _dump(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False, default=str) + "\n", encoding="utf-8")


def golden_records(golden_dir: Path = GOLDEN_DIR) -> dict[str, list[dict]]:
    return {n: load_golden(n, golden_dir)["records"] for n in DATASETS}


def run_posttran(g: dict[str, list[dict]]):
    return post_daily_transactions(g["daily_transaction"], g["card_xref"], g["account"], g["category_balance"])


def run_intcalc(g: dict[str, list[dict]], accounts=None, tcatbal=None, legacy_parity=True):
    return calculate_interest(
        tcatbal or g["category_balance"],
        g["card_xref"],
        accounts or g["account"],
        g["disclosure_group"],
        PARM_DATE,
        legacy_parity,
    )


def ebcdic_records() -> dict[str, list[dict]]:
    """Decode the EBCDIC (cp037, RECFM=FB) twin of every master file with the same copybooks."""
    return {n: parse_binary_dataset(DATASETS[n], EBCDIC_DIR / f, CODEPAGE).records for n, f in EBCDIC_FILES.items()}


def run_export_checks(g: dict[str, list[dict]]) -> list:
    layout = load_export_layout()
    raws = split_fixed((EBCDIC_DIR / EXPORT_FILE).read_bytes(), RECORD_LENGTH)
    records = parse_export().records
    return export_checks(raws, records, [encode_export_record(r, layout) for r in records], g)


def all_checks(g: dict[str, list[dict]], golden_dir: Path = GOLDEN_DIR) -> list:
    checks = integrity_checks(g)
    checks += ebcdic_checks(g, ebcdic_records(), {n: ds.key for n, ds in DATASETS.items()}, KNOWN_EBCDIC_DIFFS)
    checks += run_export_checks(g)
    for job, name in LOAD_JOBS.items():
        checks += load_checks(job, name, load_golden(name, golden_dir), g[name])
    post = run_posttran(g)
    checks += posttran_checks(g["daily_transaction"], g["account"], g["category_balance"], post)
    after_tcat = list(post.category_balances.values())
    interest = run_intcalc(g, list(post.accounts.values()), after_tcat)
    checks += intcalc_checks(list(post.accounts.values()), after_tcat, interest, PARM_DATE)
    return checks


def build_job_goldens(golden_dir: Path = GOLDEN_DIR) -> None:
    """Write expected outputs for POSTTRAN then INTCALC (the daily-then-monthly chain) and JSON Schemas."""
    g = golden_records(golden_dir)
    post = run_posttran(g)
    _dump(
        golden_dir / "jobs" / "POSTTRAN.expected.json",
        {
            "job": "POSTTRAN",
            "program": "CBTRN02C",
            "inputs": ["daily_transaction", "card_xref", "account", "category_balance"],
            "return_code": post.return_code,
            "counts": {
                "input": len(g["daily_transaction"]),
                "posted": len(post.posted),
                "rejected": len(post.rejected),
                "category_rows_created": len(post.created_category_keys),
            },
            "ignore_fields": ["TRAN-PROC-TS"],
            "transact": post.posted,
            "dalyrejs": [
                {"DALYTRAN-ID": r["transaction"]["DALYTRAN-ID"], "reason_code": r["reason_code"], "reason": r["reason"]}
                for r in post.rejected
            ],
            "acctdata_after": sorted(post.accounts.values(), key=lambda a: a["ACCT-ID"]),
            "tcatbalf_after": sorted(
                post.category_balances.values(), key=lambda t: (t["TRANCAT-ACCT-ID"], t["TRANCAT-TYPE-CD"], t["TRANCAT-CD"])
            ),
        },
    )
    after_tcat = list(post.category_balances.values())
    interest = run_intcalc(g, list(post.accounts.values()), after_tcat)
    _dump(
        golden_dir / "jobs" / "INTCALC.expected.json",
        {
            "job": "INTCALC",
            "program": "CBACT04C",
            "parm_date": PARM_DATE,
            "inputs": ["POSTTRAN.tcatbalf_after", "POSTTRAN.acctdata_after", "card_xref", "disclosure_group"],
            "legacy_parity": True,
            "last_account_not_updated": interest.skipped_last_account_id,
            "ignore_fields": ["TRAN-ORIG-TS", "TRAN-PROC-TS"],
            "counts": {
                "interest_transactions": len(interest.interest_transactions),
                "accounts_updated": len(interest.updated_account_ids),
                "default_rate_lookups": len(interest.default_rate_keys),
            },
            "systran": interest.interest_transactions,
            "acctdata_after": sorted(interest.accounts.values(), key=lambda a: a["ACCT-ID"]),
        },
    )
    for name, ds in DATASETS.items():
        _dump(golden_dir / "schemas" / f"{name}.schema.json", json_schema(load_layout(ds), f"{name} ({ds.copybook})"))
    layout = load_export_layout()
    for t, (_, name, _, _) in RECORD_TYPES.items():
        _dump(
            golden_dir / "schemas" / f"export.{t}.schema.json",
            json_schema(record_layout(layout, t), f"export {t} ({name}, CVEXPORT.cpy)"),
        )
    checks = all_checks(g, golden_dir)
    _dump(
        golden_dir / "jobs" / "reconciliation-baseline.json",
        {"summary": summarize(checks), "checks": [c.as_dict() for c in checks]},
    )


def regenerate(golden_dir: Path = GOLDEN_DIR) -> None:
    write_all(golden_dir)
    build_job_goldens(golden_dir)
