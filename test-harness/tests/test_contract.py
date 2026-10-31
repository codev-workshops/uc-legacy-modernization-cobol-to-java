import pytest

from carddemo_harness.contract import json_schema, validate
from carddemo_harness.datasets import DATASETS
from carddemo_harness.records import load_layout


@pytest.mark.parametrize("name", sorted(DATASETS))
def test_every_golden_record_honours_contract(name, golden):
    layout = load_layout(DATASETS[name])
    for rec in golden[name]:
        assert validate(rec, layout) == []


@pytest.fixture
def account(golden):
    return dict(golden["account"][0])


@pytest.mark.parametrize(
    "field,value,fragment",
    [
        ("ACCT-CURR-BAL", "1.234", "decimal places"),
        ("ACCT-CURR-BAL", "10000000000.00", "overflows"),
        ("ACCT-ID", -1, "negative value in unsigned"),
        ("ACCT-ID", 1.5, "float not allowed"),
        ("ACCT-ACTIVE-STATUS", "YY", "exceeds"),
        ("ACCT-GROUP-ID", 12, "expected string"),
        ("ACCT-CURR-BAL", "abc", "not numeric"),
    ],
)
def test_violations_detected(account, field, value, fragment):
    account[field] = value
    errs = validate(account, load_layout(DATASETS["account"]))
    assert any(fragment in e for e in errs), errs


def test_missing_and_extra_fields(account):
    del account["ACCT-ID"]
    account["NEW-COL"] = 1
    errs = validate(account, load_layout(DATASETS["account"]))
    assert "ACCT-ID: missing" in errs and "NEW-COL: not in copybook" in errs


def test_schema_shape():
    s = json_schema(load_layout(DATASETS["account"]), "account")
    assert s["properties"]["ACCT-CURR-BAL"]["pattern"] == r"^-?\d{1,10}\.\d{2}$"
    assert s["properties"]["ACCT-ID"]["maximum"] == 10**11 - 1
    assert s["properties"]["ACCT-GROUP-ID"]["maxLength"] == 10
    assert s["additionalProperties"] is False
