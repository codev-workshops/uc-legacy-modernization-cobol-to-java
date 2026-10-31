"""Reconciliation checks: counts, control totals, referential integrity and per-job balancing rules."""

from __future__ import annotations

from collections import Counter
from dataclasses import asdict, dataclass
from decimal import Decimal
from itertools import pairwise

from .compare import compare_records, index
from .models import InterestResult, PostingResult, d


@dataclass
class Check:
    check_id: str
    job: str
    description: str
    passed: bool
    expected: object = None
    actual: object = None

    def as_dict(self) -> dict:
        return {k: (str(v) if isinstance(v, Decimal) else v) for k, v in asdict(self).items()}


def _eq(check_id, job, desc, expected, actual) -> Check:
    return Check(check_id, job, desc, expected == actual, expected, actual)


def total(records, f) -> Decimal:
    return sum((d(r[f]) for r in records if r.get(f) is not None), Decimal("0"))


# ---------- generic ----------


def record_count(job, name, expected: int, actual: list) -> Check:
    return _eq(f"{job}.{name}.count", job, f"{name} record count", expected, len(actual))


def key_unique(job, name, records, key) -> Check:
    _, dups = index(records, key)
    return Check(f"{job}.{name}.unique-key", job, f"{name} key {'+'.join(key)} is unique", not dups, [], sorted(set(dups)))


def control_total(job, name, field_name, expected: Decimal, records) -> Check:
    return _eq(f"{job}.{name}.sum.{field_name}", job, f"sum of {field_name}", Decimal(expected), total(records, field_name))


def referential(job, child_name, children, child_field, parent_name, parents, parent_field) -> Check:
    keys = {p[parent_field] for p in parents}
    orphans = sorted({str(c[child_field]) for c in children if c[child_field] not in keys})
    return Check(
        f"{job}.{child_name}.fk.{child_field}",
        job,
        f"every {child_name}.{child_field} exists in {parent_name}.{parent_field}",
        not orphans,
        [],
        orphans,
    )


def domain(job, name, records, field_name, allowed) -> Check:
    bad = sorted({str(r[field_name]) for r in records if r[field_name] not in allowed})
    return Check(f"{job}.{name}.domain.{field_name}", job, f"{field_name} within allowed values", not bad, [], bad)


def load_checks(job: str, name: str, golden: dict, target: list[dict]) -> list[Check]:
    """Initial-load jobs (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANTYPE, TRANCATG, DISCGRP, TCATBALF)."""
    ct = golden["control_totals"]
    out = [record_count(job, name, ct["record_count"], target), key_unique(job, name, target, golden["key"])]
    out += [control_total(job, name, f, Decimal(v), target) for f, v in ct["sums"].items()]
    return out


def integrity_checks(g: dict[str, list[dict]]) -> list[Check]:
    """Cross-file referential integrity of the master files (precondition for every batch job)."""
    job = "MASTER"
    out = [
        referential(job, "card", g["card"], "CARD-ACCT-ID", "account", g["account"], "ACCT-ID"),
        referential(job, "card_xref", g["card_xref"], "XREF-CARD-NUM", "card", g["card"], "CARD-NUM"),
        referential(job, "card_xref", g["card_xref"], "XREF-ACCT-ID", "account", g["account"], "ACCT-ID"),
        referential(job, "card_xref", g["card_xref"], "XREF-CUST-ID", "customer", g["customer"], "CUST-ID"),
        referential(job, "category_balance", g["category_balance"], "TRANCAT-ACCT-ID", "account", g["account"], "ACCT-ID"),
        referential(
            job,
            "transaction_category",
            g["transaction_category"],
            "TRAN-TYPE-CD",
            "transaction_type",
            g["transaction_type"],
            "TRAN-TYPE",
        ),
        domain(job, "account", g["account"], "ACCT-ACTIVE-STATUS", {"Y", "N"}),
        domain(job, "card", g["card"], "CARD-ACTIVE-STATUS", {"Y", "N"}),
        domain(
            job, "daily_transaction", g["daily_transaction"], "DALYTRAN-TYPE-CD", {t["TRAN-TYPE"] for t in g["transaction_type"]}
        ),
    ]
    cats = {(c["TRAN-TYPE-CD"], c["TRAN-CAT-CD"]) for c in g["transaction_category"]}
    bad = sorted(
        {
            f"{t['DALYTRAN-TYPE-CD']}/{t['DALYTRAN-CAT-CD']}"
            for t in g["daily_transaction"]
            if (t["DALYTRAN-TYPE-CD"], t["DALYTRAN-CAT-CD"]) not in cats
        }
    )
    out.append(
        Check(f"{job}.daily_transaction.fk.type-category", job, "daily type/category exists in TRANCATG", not bad, [], bad)
    )
    return out


# ---------- POSTTRAN / CBTRN02C ----------


def posttran_checks(daily: list[dict], before_accounts: list[dict], before_tcat: list[dict], res: PostingResult) -> list[Check]:
    job = "POSTTRAN"
    posted_amt = total(res.posted, "TRAN-AMT")
    rejected_amt = sum((d(r["transaction"]["DALYTRAN-AMT"]) for r in res.rejected), Decimal("0"))
    after_acc = list(res.accounts.values())
    after_tcat = list(res.category_balances.values())
    pos = sum((d(p["TRAN-AMT"]) for p in res.posted if d(p["TRAN-AMT"]) >= 0), Decimal("0"))
    neg = posted_amt - pos
    reasons = Counter(r["reason_code"] for r in res.rejected)
    return [
        _eq(
            "POSTTRAN.count.in=posted+rejected",
            job,
            "DALYTRAN records = TRANSACT writes + DALYREJS writes",
            len(daily),
            len(res.posted) + len(res.rejected),
        ),
        _eq(
            "POSTTRAN.amount.in=posted+rejected",
            job,
            "sum DALYTRAN-AMT = posted amount + rejected amount",
            total(daily, "DALYTRAN-AMT"),
            posted_amt + rejected_amt,
        ),
        _eq(
            "POSTTRAN.acct.balance-delta",
            job,
            "delta sum ACCT-CURR-BAL = posted amount",
            posted_amt,
            total(after_acc, "ACCT-CURR-BAL") - total(before_accounts, "ACCT-CURR-BAL"),
        ),
        _eq(
            "POSTTRAN.acct.cycle-credit-delta",
            job,
            "delta sum ACCT-CURR-CYC-CREDIT = posted amounts >= 0",
            pos,
            total(after_acc, "ACCT-CURR-CYC-CREDIT") - total(before_accounts, "ACCT-CURR-CYC-CREDIT"),
        ),
        _eq(
            "POSTTRAN.acct.cycle-debit-delta",
            job,
            "delta sum ACCT-CURR-CYC-DEBIT = posted amounts < 0",
            neg,
            total(after_acc, "ACCT-CURR-CYC-DEBIT") - total(before_accounts, "ACCT-CURR-CYC-DEBIT"),
        ),
        _eq(
            "POSTTRAN.tcatbal.balance-delta",
            job,
            "delta sum TRAN-CAT-BAL = posted amount",
            posted_amt,
            total(after_tcat, "TRAN-CAT-BAL") - total(before_tcat, "TRAN-CAT-BAL"),
        ),
        _eq(
            "POSTTRAN.tcatbal.rows-created",
            job,
            "new TCATBALF rows = new (account, type, category) keys",
            len(after_tcat) - len(before_tcat),
            len(res.created_category_keys),
        ),
        _eq("POSTTRAN.acct.count-unchanged", job, "ACCTFILE record count unchanged", len(before_accounts), len(after_acc)),
        Check(
            "POSTTRAN.reject.reason-codes",
            job,
            "every reject has reason 100/101/102/103/109",
            set(reasons) <= {100, 101, 102, 103, 109},
            "{100,101,102,103,109}",
            dict(sorted(reasons.items())),
        ),
        _eq("POSTTRAN.return-code", job, "RC=4 when any reject, else 0", 4 if res.rejected else 0, res.return_code),
        key_unique(job, "transact", res.posted, ["TRAN-ID"]),
    ]


# ---------- INTCALC / CBACT04C ----------


def intcalc_checks(before_accounts: list[dict], tcatbal: list[dict], res: InterestResult, parm_date: str) -> list[Check]:
    job = "INTCALC"
    by_acct: dict[int, Decimal] = {}
    for t in res.interest_transactions:
        by_acct[t["ACCT-ID"]] = by_acct.get(t["ACCT-ID"], Decimal("0")) + d(t["TRAN-AMT"])
    applied = sum((v for k, v in by_acct.items() if k in res.updated_account_ids), Decimal("0"))
    before = {a["ACCT-ID"]: a for a in before_accounts}
    after = res.accounts
    bal_delta = total(after.values(), "ACCT-CURR-BAL") - total(before_accounts, "ACCT-CURR-BAL")
    not_reset = sorted(
        a for a in res.updated_account_ids if d(after[a]["ACCT-CURR-CYC-CREDIT"]) != 0 or d(after[a]["ACCT-CURR-CYC-DEBIT"]) != 0
    )
    untouched_changed = sorted(a for a in before if a not in res.updated_account_ids and before[a] != after[a])
    bad_ids = [
        t["TRAN-ID"]
        for t in res.interest_transactions
        if not (t["TRAN-ID"].startswith(parm_date[:10]) and len(t["TRAN-ID"]) == 16)
    ]
    bad_type = [
        t["TRAN-ID"]
        for t in res.interest_transactions
        if (t["TRAN-TYPE-CD"], t["TRAN-CAT-CD"], t["TRAN-SOURCE"]) != ("01", 5, "System")
    ]
    return [
        _eq("INTCALC.acct.balance-delta", job, "delta sum ACCT-CURR-BAL = interest of updated accounts", applied, bal_delta),
        Check("INTCALC.acct.cycle-reset", job, "updated accounts have cycle credit/debit = 0", not not_reset, [], not_reset),
        Check(
            "INTCALC.acct.untouched",
            job,
            "accounts without category balances are unchanged",
            not untouched_changed,
            [],
            untouched_changed,
        ),
        Check("INTCALC.tran.id-format", job, "TRAN-ID = PARM-DATE(10) + 6-digit suffix", not bad_ids, [], bad_ids),
        Check(
            "INTCALC.tran.type",
            job,
            "interest transactions are type 01 / category 05 / source System",
            not bad_type,
            [],
            bad_type,
        ),
        key_unique(job, "interest_tran", res.interest_transactions, ["TRAN-ID"]),
        Check(
            "INTCALC.tran.count",
            job,
            "one interest transaction per category balance with a non-zero rate",
            len(res.interest_transactions) <= len(tcatbal),
            f"<= {len(tcatbal)}",
            len(res.interest_transactions),
        ),
    ]


def parity(job, name, expected, actual, key, known_keys=()) -> Check:
    """Keyed record parity between two renditions of the same data; only ``known_keys`` may differ."""
    rep = compare_records(expected, actual, list(key))
    differing = sorted({m.key for m in rep.mismatches} | set(rep.missing) | set(rep.unexpected) | set(rep.duplicates))
    return Check(
        f"{job}.{name}.parity",
        job,
        f"{name} records match by {'+'.join(key)}",
        differing == sorted(known_keys),
        sorted(known_keys),
        differing,
    )


def ebcdic_checks(ascii_g: dict[str, list[dict]], ebcdic_g: dict[str, list[dict]], keys: dict, known: dict) -> list[Check]:
    """The EBCDIC (cp037, RECFM=FB) unload of each master must decode to the same records as its ASCII twin."""
    return [parity("EBCDIC", n, ascii_g[n], ebcdic_g[n], keys[n], known.get(n, ())) for n in ebcdic_g]


def export_checks(raws: list[bytes], records: list[dict], reencoded: list[bytes], g: dict[str, list[dict]]) -> list[Check]:
    """CBEXPORT (multi-record EBCDIC export with COMP/COMP-3 fields) and the CBIMPORT round trip."""
    from .export import KNOWN_SNAPSHOT_DIFFS, RECORD_TYPES, split_by_type

    job = "CBEXPORT"
    seq = [r["EXPORT-SEQUENCE-NUM"] for r in records]
    by = split_by_type(records)
    masters = {**g, "transaction": [{k.replace("DALYTRAN-", "TRAN-"): v for k, v in t.items()} for t in g["daily_transaction"]]}
    keys = {
        "customer": ["CUST-ID"],
        "account": ["ACCT-ID"],
        "card_xref": ["XREF-CARD-NUM"],
        "card": ["CARD-NUM"],
        "transaction": ["TRAN-ID"],
    }
    out = [
        domain(job, "export", records, "EXPORT-REC-TYPE", set(RECORD_TYPES)),
        key_unique(job, "export", records, ["EXPORT-SEQUENCE-NUM"]),
        Check(
            f"{job}.export.sequence-ascending",
            job,
            "EXPORT-SEQUENCE-NUM strictly ascending in file order",
            all(a < b for a, b in pairwise(seq)),
            True,
            all(a < b for a, b in pairwise(seq)),
        ),
    ]
    for t, (_, name, _, _) in RECORD_TYPES.items():
        out.append(
            _eq(f"{job}.count-by-type.{t}", job, f"type {t} records = {name} source records", len(masters[name]), len(by[name]))
        )
    out.append(
        _eq(
            f"{job}.sum.TRAN-AMT",
            job,
            "sum of exported TRAN-AMT (COMP-3) = sum of source amounts",
            total(masters["transaction"], "TRAN-AMT"),
            total(by["transaction"], "TRAN-AMT"),
        )
    )
    for name, key in keys.items():
        out.append(parity(job, name, masters[name], by[name], key, KNOWN_SNAPSHOT_DIFFS[name]))
    bad = [i + 1 for i, (a, b) in enumerate(zip(raws, reencoded, strict=True)) if a != b]
    out.append(
        Check(
            "CBIMPORT.round-trip",
            "CBIMPORT",
            "decode then re-encode reproduces every export record byte for byte",
            not bad,
            [],
            bad,
        )
    )
    out.append(
        _eq(
            "CBIMPORT.errors",
            "CBIMPORT",
            "records with an unknown EXPORT-REC-TYPE (ERROUT)",
            0,
            sum(r["EXPORT-REC-TYPE"] not in RECORD_TYPES for r in records),
        )
    )
    return out


def summarize(checks: list[Check]) -> dict:
    return {"total": len(checks), "passed": sum(c.passed for c in checks), "failed": [c.check_id for c in checks if not c.passed]}
