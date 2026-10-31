from decimal import Decimal

import pytest

from carddemo_harness.compare import compare_records, values_equal


@pytest.mark.parametrize(
    "e,a,eq",
    [
        ("1.00", "1", True),
        (1, "1.00", True),
        ("ABC  ", "ABC", True),
        ("ABC", "ABD", False),
        (None, None, True),
        (None, "", False),
        ("194.00", "194.01", False),
        ("007", 7, True),
    ],
)
def test_values_equal(e, a, eq):
    assert values_equal(e, a) is eq


def test_tolerance():
    assert values_equal("1.00", "1.01", Decimal("0.01"))
    assert not values_equal("1.00", "1.02", Decimal("0.01"))


def test_identical_sets_ok(golden):
    rep = compare_records(golden["account"], [dict(r) for r in reversed(golden["account"])], ["ACCT-ID"])
    assert rep.ok, rep.summary()


def test_detects_missing_unexpected_duplicate_and_field_diff():
    exp = [{"K": 1, "V": "a"}, {"K": 2, "V": "b"}, {"K": 3, "V": "c"}]
    act = [{"K": 1, "V": "a"}, {"K": 2, "V": "X"}, {"K": 4, "V": "d"}, {"K": 4, "V": "d"}]
    rep = compare_records(exp, act, ["K"])
    assert rep.missing == ["3"] and rep.unexpected == ["4"] and rep.duplicates == ["4"]
    assert [(m.key, m.field) for m in rep.mismatches] == [("2", "V")]
    assert not rep.ok


def test_ignore_fields_and_composite_key():
    exp = [{"A": 1, "B": "01", "TS": "x"}]
    act = [{"A": 1, "B": "01", "TS": "y"}]
    assert compare_records(exp, act, ["A", "B"], ignore=["TS"]).ok
    assert not compare_records(exp, act, ["A", "B"]).ok


def test_one_cent_drift_detected_in_golden(golden):
    act = [dict(r) for r in golden["account"]]
    act[7]["ACCT-CURR-BAL"] = str(Decimal(act[7]["ACCT-CURR-BAL"]) + Decimal("0.01"))
    rep = compare_records(golden["account"], act, ["ACCT-ID"])
    assert len(rep.mismatches) == 1 and rep.mismatches[0].field == "ACCT-CURR-BAL"
