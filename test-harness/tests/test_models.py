from decimal import Decimal

import pytest
from conftest import acct, disc, tcat, tran, xref

from carddemo_harness.models import MissingDisclosureGroup, calculate_interest, post_daily_transactions, trunc2


def post(trans, accounts=None, xrefs=None, tcats=None):
    return post_daily_transactions(trans, xrefs if xrefs is not None else [xref()], accounts or [acct()], tcats or [])


def test_valid_purchase_posts_to_balance_cycle_credit_and_category():
    r = post([tran(amt="25.50")], tcats=[tcat(bal="5.00")])
    a = r.accounts[1]
    assert (a["ACCT-CURR-BAL"], a["ACCT-CURR-CYC-CREDIT"], a["ACCT-CURR-CYC-DEBIT"]) == ("125.50", "25.50", "0.00")
    assert r.category_balances["1|01|1"]["TRAN-CAT-BAL"] == "30.50"
    assert r.posted[0]["TRAN-ID"] == "T1" and r.return_code == 0


def test_negative_amount_goes_to_cycle_debit():
    a = post([tran(amt="-40.00")]).accounts[1]
    assert (a["ACCT-CURR-BAL"], a["ACCT-CURR-CYC-CREDIT"], a["ACCT-CURR-CYC-DEBIT"]) == ("60.00", "0.00", "-40.00")


def test_zero_amount_counts_as_credit():
    assert post([tran(amt="0.00")]).accounts[1]["ACCT-CURR-CYC-CREDIT"] == "0.00"
    assert len(post([tran(amt="0.00")]).posted) == 1


def test_missing_category_balance_row_is_created():
    r = post([tran(type_cd="03", cat=2)])
    assert r.created_category_keys == ["1|03|2"]
    assert r.category_balances["1|03|2"]["TRAN-CAT-BAL"] == "10.00"


def test_reject_100_unknown_card():
    r = post([tran(card="NOPE")])
    assert [x["reason_code"] for x in r.rejected] == [100] and r.return_code == 4
    assert r.accounts[1]["ACCT-CURR-BAL"] == "100.00"


def test_reject_101_account_missing():
    assert post([tran()], xrefs=[xref(acct_id=99)]).rejected[0]["reason_code"] == 101


def test_reject_102_over_limit_boundary():
    ok = post([tran(amt="1000.00")])
    assert not ok.rejected
    bad = post([tran(amt="1000.01")])
    assert bad.rejected[0]["reason_code"] == 102


def test_limit_uses_cycle_credit_minus_debit_not_balance():
    r = post([tran(amt="100.00")], accounts=[acct(bal="999999.00", limit="500.00", credit="450.00", debit="50.00")])
    assert not r.rejected
    r = post([tran(amt="100.01")], accounts=[acct(limit="500.00", credit="450.00", debit="50.00")])
    assert r.rejected[0]["reason_code"] == 102


def test_reject_103_after_expiry_and_same_day_ok():
    assert post([tran(ts="2031-01-01-00.00.00.000000")], accounts=[acct(exp="2030-12-31")]).rejected[0]["reason_code"] == 103
    assert not post([tran(ts="2030-12-31-23.59.59.999999")], accounts=[acct(exp="2030-12-31")]).rejected


def test_103_overwrites_102_when_both_fail():
    r = post([tran(amt="5000.00", ts="2031-01-01-00.00.00.000000")], accounts=[acct(exp="2030-12-31")])
    assert r.rejected[0]["reason_code"] == 103


def test_later_transactions_see_earlier_postings():
    r = post([tran("T1", amt="600.00"), tran("T2", amt="600.00")])
    assert [p["TRAN-ID"] for p in r.posted] == ["T1"] and r.rejected[0]["transaction"]["DALYTRAN-ID"] == "T2"


def test_inputs_not_mutated():
    a, t = [acct()], [tcat()]
    post([tran()], accounts=a, tcats=t)
    assert a[0]["ACCT-CURR-BAL"] == "100.00" and t[0]["TRAN-CAT-BAL"] == "0.00"


@pytest.mark.parametrize("v,expected", [("1.239", "1.23"), ("-1.239", "-1.23"), ("0.009", "0.00"), ("2.5", "2.50")])
def test_trunc2_truncates_toward_zero(v, expected):
    assert str(trunc2(Decimal(v))) == expected


def interest(tcats, accounts=None, discs=None, parity=True):
    return calculate_interest(
        tcats,
        [xref("C1", 1), xref("C2", 2)],
        accounts or [acct(1), acct(2)],
        discs if discs is not None else [disc()],
        "2022-07-21",
        parity,
    )


def test_monthly_interest_formula_and_truncation():
    r = interest([tcat(1, bal="1234.56")], discs=[disc(rate="15.00")], parity=False)
    t = r.interest_transactions[0]
    assert t["TRAN-AMT"] == "15.43"  # 1234.56 * 15 / 1200 = 15.432
    assert t["TRAN-ID"] == "2022-07-21000001" and t["TRAN-DESC"] == "Int. for a/c 00000000001"
    assert (t["TRAN-TYPE-CD"], t["TRAN-CAT-CD"], t["TRAN-SOURCE"], t["TRAN-CARD-NUM"]) == ("01", 5, "System", "C1")
    assert r.accounts[1]["ACCT-CURR-BAL"] == "115.43"


def test_negative_category_balance_truncates_toward_zero():
    r = interest([tcat(1, bal="-1234.56")], discs=[disc(rate="15.00")], parity=False)
    assert r.interest_transactions[0]["TRAN-AMT"] == "-15.43"


def test_default_group_fallback():
    r = interest([tcat(1)], accounts=[acct(1, group="UNKNOWN")], discs=[disc("DEFAULT", rate="24.00")])
    assert r.default_rate_keys == ["1|01|1"]


def test_missing_default_abends():
    with pytest.raises(MissingDisclosureGroup):
        interest([tcat(1)], accounts=[acct(1, group="UNKNOWN")], discs=[disc("A1")])


def test_zero_rate_writes_no_transaction():
    r = interest([tcat(1, bal="100.00")], discs=[disc(rate="0.00")], parity=False)
    assert r.interest_transactions == [] and r.accounts[1]["ACCT-CURR-BAL"] == "100.00"


def test_cycle_totals_reset_and_suffix_increments_across_accounts():
    r = interest(
        [tcat(1, bal="1200.00"), tcat(1, "02", 1, "1200.00"), tcat(2, bal="2400.00")],
        accounts=[acct(1, credit="5.00", debit="-2.00"), acct(2)],
        discs=[disc(rate="12.00"), disc(type_cd="02", rate="6.00")],
        parity=False,
    )
    assert [t["TRAN-ID"][-6:] for t in r.interest_transactions] == ["000001", "000002", "000003"]
    assert r.accounts[1]["ACCT-CURR-BAL"] == "118.00"  # 100 + 12 + 6
    assert r.accounts[1]["ACCT-CURR-CYC-CREDIT"] == "0.00" and r.accounts[1]["ACCT-CURR-CYC-DEBIT"] == "0.00"


def test_legacy_parity_skips_last_account_update():
    r = interest([tcat(1, bal="1200.00"), tcat(2, bal="1200.00")], discs=[disc(rate="12.00")], parity=True)
    assert r.updated_account_ids == [1] and r.skipped_last_account_id == 2
    assert r.accounts[2]["ACCT-CURR-BAL"] == "100.00"
    assert len(r.interest_transactions) == 2


def test_input_order_irrelevant_ksds_order_used():
    a = interest([tcat(2, bal="1200.00"), tcat(1, bal="1200.00")], discs=[disc(rate="12.00")])
    assert [t["ACCT-ID"] for t in a.interest_transactions] == [1, 2]
