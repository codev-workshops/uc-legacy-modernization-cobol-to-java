import copy
from decimal import Decimal

import pytest

from carddemo_harness.golden import load_golden
from carddemo_harness.jobs import PARM_DATE, all_checks, run_intcalc, run_posttran
from carddemo_harness.reconcile import intcalc_checks, integrity_checks, load_checks, posttran_checks, summarize


def test_baseline_all_checks_pass(golden):
    s = summarize(all_checks(golden))
    assert s["failed"] == [] and s["total"] >= 50


def test_posttran_expected_volumes(golden):
    r = run_posttran(golden)
    assert (len(r.posted), len(r.rejected), r.return_code) == (262, 38, 4)


def failed(checks):
    return {c.check_id for c in checks if not c.passed}


def test_dropped_posted_transaction_is_caught(golden):
    r = run_posttran(golden)
    r.posted.pop()
    assert {"POSTTRAN.count.in=posted+rejected", "POSTTRAN.amount.in=posted+rejected"} <= failed(
        posttran_checks(golden["daily_transaction"], golden["account"], golden["category_balance"], r)
    )


def test_balance_not_updated_is_caught(golden):
    r = run_posttran(golden)
    r.accounts[1]["ACCT-CURR-BAL"] = str(Decimal(r.accounts[1]["ACCT-CURR-BAL"]) + Decimal("0.01"))
    assert "POSTTRAN.acct.balance-delta" in failed(
        posttran_checks(golden["daily_transaction"], golden["account"], golden["category_balance"], r)
    )


def test_wrong_cycle_bucket_is_caught(golden):
    r = run_posttran(golden)
    a = next(a for a in r.accounts.values() if Decimal(a["ACCT-CURR-CYC-DEBIT"]) != 0)
    a["ACCT-CURR-CYC-CREDIT"] = str(Decimal(a["ACCT-CURR-CYC-CREDIT"]) + Decimal(a["ACCT-CURR-CYC-DEBIT"]))
    a["ACCT-CURR-CYC-DEBIT"] = "0.00"
    f = failed(posttran_checks(golden["daily_transaction"], golden["account"], golden["category_balance"], r))
    assert {"POSTTRAN.acct.cycle-credit-delta", "POSTTRAN.acct.cycle-debit-delta"} <= f


def test_unknown_reject_code_is_caught(golden):
    r = run_posttran(golden)
    r.rejected[0]["reason_code"] = 999
    assert "POSTTRAN.reject.reason-codes" in failed(
        posttran_checks(golden["daily_transaction"], golden["account"], golden["category_balance"], r)
    )


def test_duplicate_tran_id_is_caught(golden):
    r = run_posttran(golden)
    r.posted.append(copy.deepcopy(r.posted[0]))
    r.posted[-1]["TRAN-AMT"] = "0.00"
    assert "POSTTRAN.transact.unique-key" in failed(
        posttran_checks(golden["daily_transaction"], golden["account"], golden["category_balance"], r)
    )


@pytest.fixture
def after_posting(golden):
    """INTCALC runs on the account and category-balance state POSTTRAN leaves behind (sample TCATBALF is all zero)."""
    post = run_posttran(golden)
    return list(post.accounts.values()), list(post.category_balances.values())


def test_intcalc_interest_not_applied_is_caught(golden, after_posting):
    accts, tcats = after_posting
    res = run_intcalc(golden, accts, tcats)
    aid = next(
        a
        for a in res.updated_account_ids
        if any(t["ACCT-ID"] == a and t["TRAN-AMT"] != "0.00" for t in res.interest_transactions)
    )
    res.accounts[aid]["ACCT-CURR-BAL"] = next(a for a in accts if a["ACCT-ID"] == aid)["ACCT-CURR-BAL"]
    assert "INTCALC.acct.balance-delta" in failed(intcalc_checks(accts, tcats, res, PARM_DATE))


def test_intcalc_cycle_not_reset_is_caught(golden, after_posting):
    accts, tcats = after_posting
    res = run_intcalc(golden, accts, tcats)
    res.accounts[res.updated_account_ids[0]]["ACCT-CURR-CYC-CREDIT"] = "1.00"
    assert "INTCALC.acct.cycle-reset" in failed(intcalc_checks(accts, tcats, res, PARM_DATE))


def test_intcalc_wrong_rounding_is_caught(golden, after_posting):
    accts, tcats = after_posting
    res = run_intcalc(golden, accts, tcats)
    t = next(t for t in res.interest_transactions if t["ACCT-ID"] in res.updated_account_ids and t["TRAN-AMT"] != "0.00")
    t["TRAN-AMT"] = str(Decimal(t["TRAN-AMT"]) + Decimal("0.01"))
    assert "INTCALC.acct.balance-delta" in failed(intcalc_checks(accts, tcats, res, PARM_DATE))


def test_intcalc_last_account_quirk_reproduced(golden, after_posting):
    accts, tcats = after_posting
    legacy = run_intcalc(golden, accts, tcats)
    fixed = run_intcalc(golden, accts, tcats, legacy_parity=False)
    last = legacy.skipped_last_account_id
    assert last == max(t["TRANCAT-ACCT-ID"] for t in tcats)
    assert legacy.accounts[last] == next(a for a in accts if a["ACCT-ID"] == last)
    assert fixed.accounts[last] != legacy.accounts[last]
    assert fixed.accounts[last]["ACCT-CURR-CYC-CREDIT"] == "0.00"


def test_sample_accounts_all_use_default_rates(golden, after_posting):
    """Sample ACCTDATA carries the group id in the ACCT-ADDR-ZIP position, so ACCT-GROUP-ID is blank everywhere."""
    accts, tcats = after_posting
    assert {a["ACCT-GROUP-ID"] for a in golden["account"]} == {""}
    assert len(run_intcalc(golden, accts, tcats).default_rate_keys) == len(tcats)


@pytest.mark.parametrize("name", ["account", "card", "customer"])
def test_load_checks_detect_lost_record(name, golden):
    assert failed(load_checks("LOAD", name, load_golden(name), golden[name][:-1]))


def test_integrity_detects_orphan(golden):
    g = dict(golden)
    g["card"] = [dict(golden["card"][0], **{"CARD-ACCT-ID": 99999})] + golden["card"][1:]
    assert "MASTER.card.fk.CARD-ACCT-ID" in failed(integrity_checks(g))
