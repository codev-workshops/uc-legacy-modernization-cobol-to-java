"""Executable reference models of the COBOL batch business rules (the oracle for differential tests).

Each model is a line-by-line reading of the COBOL paragraph it cites, including known legacy quirks,
so a Java implementation can be compared against it on any input.
"""

from __future__ import annotations

import copy
from dataclasses import dataclass, field
from decimal import ROUND_DOWN, Decimal

REJECT_REASONS = {
    100: "INVALID CARD NUMBER FOUND",
    101: "ACCOUNT RECORD NOT FOUND",
    102: "OVERLIMIT TRANSACTION",
    103: "TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
    109: "ACCOUNT RECORD NOT FOUND",
}
CENT = Decimal("0.01")


def d(v) -> Decimal:
    return Decimal(str(v))


def trunc2(v: Decimal) -> Decimal:
    """COBOL MOVE/COMPUTE into PIC S9(n)V99 without ROUNDED truncates toward zero."""
    return v.quantize(CENT, rounding=ROUND_DOWN)


def tcat_key(acct_id: int, type_cd: str, cat_cd: int) -> str:
    return f"{acct_id}|{type_cd}|{cat_cd}"


@dataclass
class PostingResult:
    posted: list[dict] = field(default_factory=list)
    rejected: list[dict] = field(default_factory=list)
    accounts: dict[int, dict] = field(default_factory=dict)
    category_balances: dict[str, dict] = field(default_factory=dict)
    created_category_keys: list[str] = field(default_factory=list)

    @property
    def return_code(self) -> int:
        return 4 if self.rejected else 0


def post_daily_transactions(daily: list[dict], xref: list[dict], accounts: list[dict], tcatbal: list[dict]) -> PostingResult:
    """CBTRN02C (job POSTTRAN): validate, post, reject.

    1500-VALIDATE-TRAN: 100 when card not in XREF; 101 when account not found; otherwise both 102
    (credit limit < cyc_credit - cyc_debit + amt) and 103 (expiration < orig_ts(1:10)) are evaluated
    in order, so 103 overwrites 102 when both fail. 2000-POST-TRANSACTION updates TCATBALF, ACCTFILE and
    writes TRANFILE. Accounts are re-read per transaction, so later transactions see earlier postings.
    """
    xref_by_card = {x["XREF-CARD-NUM"]: x for x in xref}
    res = PostingResult(
        accounts={a["ACCT-ID"]: copy.deepcopy(a) for a in accounts},
        category_balances={
            tcat_key(t["TRANCAT-ACCT-ID"], t["TRANCAT-TYPE-CD"], t["TRANCAT-CD"]): copy.deepcopy(t) for t in tcatbal
        },
    )
    for tran in daily:
        amt = d(tran["DALYTRAN-AMT"])
        reason = 0
        x = xref_by_card.get(tran["DALYTRAN-CARD-NUM"])
        acct = None
        if x is None:
            reason = 100
        else:
            acct = res.accounts.get(x["XREF-ACCT-ID"])
            if acct is None:
                reason = 101
            else:
                temp = d(acct["ACCT-CURR-CYC-CREDIT"]) - d(acct["ACCT-CURR-CYC-DEBIT"]) + amt
                if not d(acct["ACCT-CREDIT-LIMIT"]) >= temp:
                    reason = 102
                if not acct["ACCT-EXPIRAION-DATE"] >= tran["DALYTRAN-ORIG-TS"][:10]:
                    reason = 103
        if reason:
            res.rejected.append({"transaction": tran, "reason_code": reason, "reason": REJECT_REASONS[reason]})
            continue
        key = tcat_key(x["XREF-ACCT-ID"], tran["DALYTRAN-TYPE-CD"], tran["DALYTRAN-CAT-CD"])
        cat = res.category_balances.get(key)
        if cat is None:
            cat = {
                "TRANCAT-ACCT-ID": x["XREF-ACCT-ID"],
                "TRANCAT-TYPE-CD": tran["DALYTRAN-TYPE-CD"],
                "TRANCAT-CD": tran["DALYTRAN-CAT-CD"],
                "TRAN-CAT-BAL": "0.00",
            }
            res.category_balances[key] = cat
            res.created_category_keys.append(key)
        cat["TRAN-CAT-BAL"] = str(d(cat["TRAN-CAT-BAL"]) + amt)
        acct["ACCT-CURR-BAL"] = str(d(acct["ACCT-CURR-BAL"]) + amt)
        bucket = "ACCT-CURR-CYC-CREDIT" if amt >= 0 else "ACCT-CURR-CYC-DEBIT"
        acct[bucket] = str(d(acct[bucket]) + amt)
        res.posted.append(
            {
                "TRAN-ID": tran["DALYTRAN-ID"],
                "TRAN-TYPE-CD": tran["DALYTRAN-TYPE-CD"],
                "TRAN-CAT-CD": tran["DALYTRAN-CAT-CD"],
                "TRAN-SOURCE": tran["DALYTRAN-SOURCE"],
                "TRAN-DESC": tran["DALYTRAN-DESC"],
                "TRAN-AMT": str(amt),
                "TRAN-MERCHANT-ID": tran["DALYTRAN-MERCHANT-ID"],
                "TRAN-MERCHANT-NAME": tran["DALYTRAN-MERCHANT-NAME"],
                "TRAN-MERCHANT-CITY": tran["DALYTRAN-MERCHANT-CITY"],
                "TRAN-MERCHANT-ZIP": tran["DALYTRAN-MERCHANT-ZIP"],
                "TRAN-CARD-NUM": tran["DALYTRAN-CARD-NUM"],
                "TRAN-ORIG-TS": tran["DALYTRAN-ORIG-TS"],
            }
        )
    return res


@dataclass
class InterestResult:
    interest_transactions: list[dict] = field(default_factory=list)
    accounts: dict[int, dict] = field(default_factory=dict)
    updated_account_ids: list[int] = field(default_factory=list)
    skipped_last_account_id: int | None = None
    default_rate_keys: list[str] = field(default_factory=list)


class MissingDisclosureGroup(LookupError):
    """CBACT04C abends (9999-ABEND-PROGRAM) when neither the group nor DEFAULT rate exists."""


def calculate_interest(
    tcatbal: list[dict], xref: list[dict], accounts: list[dict], discgrp: list[dict], parm_date: str, legacy_parity: bool = True
) -> InterestResult:
    """CBACT04C (job INTCALC): monthly interest per category balance.

    1300-COMPUTE-INTEREST: monthly = TRAN-CAT-BAL * DIS-INT-RATE / 1200, truncated to PIC S9(09)V99.
    1200-GET-INTEREST-RATE falls back to group 'DEFAULT'. 1050-UPDATE-ACCOUNT adds the total and zeroes the
    cycle credit/debit, and runs only when the account id changes. The main PERFORM UNTIL tests before each
    iteration, so its ELSE branch never runs at end of file: the last account's interest transactions are
    written but its balance is never updated. legacy_parity=True reproduces that; False shows the intended rule.
    """
    rates = {(g["DIS-ACCT-GROUP-ID"], g["DIS-TRAN-TYPE-CD"], g["DIS-TRAN-CAT-CD"]): d(g["DIS-INT-RATE"]) for g in discgrp}
    card_by_acct: dict[int, str] = {}
    for x in xref:
        card_by_acct.setdefault(x["XREF-ACCT-ID"], x["XREF-CARD-NUM"])
    res = InterestResult(accounts={a["ACCT-ID"]: copy.deepcopy(a) for a in accounts})
    ordered = sorted(tcatbal, key=lambda t: (t["TRANCAT-ACCT-ID"], t["TRANCAT-TYPE-CD"], t["TRANCAT-CD"]))
    suffix = 0
    current: int | None = None
    total = Decimal("0")

    def apply(acct_id: int, amount: Decimal) -> None:
        a = res.accounts[acct_id]
        a["ACCT-CURR-BAL"] = str(d(a["ACCT-CURR-BAL"]) + amount)
        a["ACCT-CURR-CYC-CREDIT"] = "0.00"
        a["ACCT-CURR-CYC-DEBIT"] = "0.00"
        res.updated_account_ids.append(acct_id)

    for t in ordered:
        acct_id = t["TRANCAT-ACCT-ID"]
        if acct_id != current:
            if current is not None:
                apply(current, total)
            total = Decimal("0")
            current = acct_id
            if acct_id not in res.accounts:
                raise MissingDisclosureGroup(f"account {acct_id} not found")
        acct = res.accounts[acct_id]
        k = (acct["ACCT-GROUP-ID"], t["TRANCAT-TYPE-CD"], t["TRANCAT-CD"])
        rate = rates.get(k)
        if rate is None:
            rate = rates.get(("DEFAULT", t["TRANCAT-TYPE-CD"], t["TRANCAT-CD"]))
            if rate is None:
                raise MissingDisclosureGroup(f"no rate for {k} or DEFAULT")
            res.default_rate_keys.append(tcat_key(acct_id, t["TRANCAT-TYPE-CD"], t["TRANCAT-CD"]))
        if rate == 0:
            continue
        monthly = trunc2(d(t["TRAN-CAT-BAL"]) * rate / Decimal(1200))
        total += monthly
        suffix += 1
        res.interest_transactions.append(
            {
                "TRAN-ID": f"{parm_date[:10]}{suffix:06d}",
                "TRAN-TYPE-CD": "01",
                "TRAN-CAT-CD": 5,
                "TRAN-SOURCE": "System",
                "TRAN-DESC": f"Int. for a/c {acct_id:011d}",
                "TRAN-AMT": str(monthly),
                "TRAN-MERCHANT-ID": 0,
                "TRAN-CARD-NUM": card_by_acct.get(acct_id, ""),
                "ACCT-ID": acct_id,
            }
        )
    if current is not None:
        if legacy_parity:
            res.skipped_last_account_id = current
        else:
            apply(current, total)
    return res
