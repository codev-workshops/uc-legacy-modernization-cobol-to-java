import hashlib
import json

import pytest

from carddemo_harness.copybook import elementary_fields
from carddemo_harness.datasets import DATA_DIR, DATASETS, GOLDEN_DIR
from carddemo_harness.golden import load_golden, write_all
from carddemo_harness.jobs import build_job_goldens
from carddemo_harness.records import encode_record, load_layout, parse_dataset


@pytest.mark.parametrize("name", sorted(DATASETS))
def test_parses_every_line_without_issues(name):
    ds = DATASETS[name]
    res = parse_dataset(ds)
    lines = [ln for ln in (DATA_DIR / ds.file).read_text(encoding="latin-1").splitlines() if ln.strip()]
    assert len(res.records) == len(lines)
    assert res.issues == []


@pytest.mark.parametrize("name", sorted(DATASETS))
def test_byte_round_trip_against_source(name):
    """parse -> encode reproduces every named field's bytes in the source record, proving the layout is exact."""
    ds = DATASETS[name]
    layout = load_layout(ds)
    spans = [
        (f.offset, f.offset + f.length)
        for f in elementary_fields(layout)
        if f.name != "FILLER" and not f.name.endswith("-FILLER")
    ]
    lines = [ln for ln in (DATA_DIR / ds.file).read_text(encoding="latin-1").splitlines() if ln.strip()]
    for line, rec in zip(lines, parse_dataset(ds).records, strict=True):
        rebuilt, src = encode_record(rec, layout, ds.record_length), line.ljust(ds.record_length)
        for a, b in spans:
            assert rebuilt[a:b].rstrip() == src[a:b].rstrip(), (a, b, src[a:b])


@pytest.mark.parametrize("name", sorted(DATASETS))
def test_golden_matches_source(name):
    g = load_golden(name)
    ds = DATASETS[name]
    assert g["source_sha256"] == hashlib.sha256((DATA_DIR / ds.file).read_bytes()).hexdigest(), (
        "source changed: run `python -m carddemo_harness golden`"
    )
    assert g["control_totals"]["record_count"] == len(g["records"])
    assert g["parse_issues"] == []


@pytest.mark.parametrize("name", sorted(DATASETS))
def test_golden_keys_unique_and_sorted(name):
    g = load_golden(name)
    keys = ["|".join(str(r[k]) for k in g["key"]) for r in g["records"]]
    assert len(keys) == len(set(keys))
    assert keys == sorted(keys)


def test_committed_golden_files_are_up_to_date(tmp_path):
    write_all(tmp_path)
    build_job_goldens(tmp_path)
    for p in sorted(tmp_path.rglob("*.json")):
        committed = GOLDEN_DIR / p.relative_to(tmp_path)
        assert committed.exists(), committed
        assert json.loads(committed.read_text()) == json.loads(p.read_text()), f"{committed} is stale"


def test_expected_record_counts(golden):
    counts = {n: len(r) for n, r in golden.items()}
    assert counts == {
        "account": 50,
        "card": 50,
        "card_xref": 50,
        "customer": 50,
        "daily_transaction": 300,
        "disclosure_group": 51,
        "category_balance": 50,
        "transaction_category": 18,
        "transaction_type": 7,
    }


def test_sample_values_decoded(golden):
    a1 = next(a for a in golden["account"] if a["ACCT-ID"] == 1)
    assert a1["ACCT-CURR-BAL"] == "194.00" and a1["ACCT-CREDIT-LIMIT"] == "2020.00"
    assert a1["ACCT-EXPIRAION-DATE"] == "2025-05-20"
    assert {t["TRAN-TYPE"]: t["TRAN-TYPE-DESC"] for t in golden["transaction_type"]}["01"] == "Purchase"
