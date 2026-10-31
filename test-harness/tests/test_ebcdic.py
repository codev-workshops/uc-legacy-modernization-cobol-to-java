import copy
from decimal import Decimal

import pytest

from carddemo_harness.contract import validate
from carddemo_harness.datasets import DATASETS, EBCDIC_DIR, EBCDIC_FILES, KNOWN_EBCDIC_DIFFS
from carddemo_harness.export import (
    EXPORT_FILE,
    RECORD_LENGTH,
    RECORD_TYPES,
    body_fields,
    build_export_golden,
    encode_export_record,
    load_export_layout,
    parse_export,
    record_layout,
    split_by_type,
    to_master,
)
from carddemo_harness.golden import load_golden
from carddemo_harness.jobs import ebcdic_records
from carddemo_harness.reconcile import ebcdic_checks, export_checks
from carddemo_harness.records import parse_binary_dataset, split_fixed


@pytest.fixture(scope="module")
def export():
    raws = split_fixed((EBCDIC_DIR / EXPORT_FILE).read_bytes(), RECORD_LENGTH)
    return raws, parse_export().records


@pytest.mark.parametrize("name", sorted(EBCDIC_FILES))
def test_ebcdic_twin_decodes_to_ascii_golden(name):
    ds = DATASETS[name]
    res = parse_binary_dataset(ds, EBCDIC_DIR / EBCDIC_FILES[name])
    assert res.issues == [] and res.raw_lengths == {ds.record_length}
    golden = load_golden(name)
    from carddemo_harness.compare import compare_records

    rep = compare_records(golden["records"], res.records, golden["key"])
    assert not (rep.missing or rep.unexpected or rep.duplicates)
    assert sorted({m.key for m in rep.mismatches}) == KNOWN_EBCDIC_DIFFS.get(name, [])


def test_ebcdic_known_differences_are_data_not_codec(golden):
    eb = ebcdic_records()
    acct = {a["ACCT-ID"]: a for a in eb["account"]}
    assert acct[49]["ACCT-ADDR-ZIP"] == "ZEROAPR"
    disc = {(r["DIS-ACCT-GROUP-ID"], r["DIS-TRAN-TYPE-CD"], r["DIS-TRAN-CAT-CD"]): r for r in eb["disclosure_group"]}
    assert disc[("DEFAULT", "07", 1)]["DIS-INT-RATE"] == "15.00"


def test_ebcdic_parity_check_detects_drift(golden):
    eb = copy.deepcopy(ebcdic_records())
    eb["card"][3]["CARD-ACTIVE-STATUS"] = "N"
    keys = {n: ds.key for n, ds in DATASETS.items()}
    failed = [c.check_id for c in ebcdic_checks(golden, eb, keys, KNOWN_EBCDIC_DIFFS) if not c.passed]
    assert failed == ["EBCDIC.card.parity"]


def test_export_layout_is_500_bytes_for_every_record_type():
    layout = load_export_layout()
    assert layout.length == RECORD_LENGTH
    for t in RECORD_TYPES:
        last = body_fields(layout, t)[-1]
        assert last.offset + last.length == RECORD_LENGTH


def test_export_usage_mix():
    layout = load_export_layout()
    acct = {f.name: f for f in body_fields(layout, "A")}
    assert (acct["EXP-ACCT-CURR-BAL"].kind, acct["EXP-ACCT-CURR-BAL"].length) == ("packed", 7)
    assert (acct["EXP-ACCT-CREDIT-LIMIT"].kind, acct["EXP-ACCT-CREDIT-LIMIT"].length) == ("zoned", 12)
    assert (acct["EXP-ACCT-CURR-CYC-DEBIT"].kind, acct["EXP-ACCT-CURR-CYC-DEBIT"].length) == ("binary", 8)
    cust = [f.name for f in body_fields(layout, "C")]
    assert cust.count("EXP-CUST-ADDR-LINE(1)") == 1 and "EXP-CUST-ADDR-LINE(3)" in cust and "EXP-CUST-PHONE-NUM(2)" in cust


def test_export_parses_cleanly(export):
    raws, records = export
    assert len(records) == 500
    assert parse_export().issues == []
    first = records[0]
    assert first["EXPORT-REC-TYPE"] == "C" and first["EXP-CUST-ID"] == 1 and first["EXP-CUST-FICO-CREDIT-SCORE"] == 300
    acct = next(r for r in records if r["EXPORT-REC-TYPE"] == "A")
    assert acct["EXP-ACCT-CASH-CREDIT-LIMIT"] == "1020.00"


def test_export_round_trips_byte_for_byte(export):
    raws, records = export
    layout = load_export_layout()
    assert all(encode_export_record(r, layout) == raw for r, raw in zip(records, raws, strict=True))


def test_export_records_honour_copybook_contract(export):
    _, records = export
    layout = load_export_layout()
    for t in RECORD_TYPES:
        lay = record_layout(layout, t)
        errors = [e for r in records if r["EXPORT-REC-TYPE"] == t for e in validate(r, lay)]
        assert errors == [], t


def test_export_tran_amounts_match_daily_transactions(golden, export):
    _, records = export
    tran = split_by_type(records)["transaction"]
    daily = {t["DALYTRAN-ID"]: Decimal(t["DALYTRAN-AMT"]) for t in golden["daily_transaction"]}
    assert {t["TRAN-ID"]: Decimal(t["TRAN-AMT"]) for t in tran} == daily


def test_to_master_renames_occurs_subscripts():
    rec = {"EXPORT-REC-TYPE": "C", "EXPORT-SEQUENCE-NUM": 1, "EXP-CUST-ID": 7, "EXP-CUST-ADDR-LINE(2)": "x"}
    assert to_master(rec) == {"CUST-ID": 7, "CUST-ADDR-LINE-2": "x"}


def test_export_checks_pass_on_sample(golden, export):
    raws, records = export
    layout = load_export_layout()
    checks = export_checks(raws, records, [encode_export_record(r, layout) for r in records], golden)
    assert [c.check_id for c in checks if not c.passed] == []
    assert len(checks) == 16


def _failed(golden, raws, records, reencoded=None):
    layout = load_export_layout()
    re = reencoded or [encode_export_record(r, layout) for r in records]
    return [c.check_id for c in export_checks(raws, records, re, golden) if not c.passed]


@pytest.mark.parametrize(
    "mutate,expected",
    [
        (
            lambda rs: rs[200].update({"EXP-TRAN-AMT": str(Decimal(rs[200]["EXP-TRAN-AMT"]) + Decimal("0.01"))}),
            ["CBEXPORT.sum.TRAN-AMT", "CBEXPORT.transaction.parity"],
        ),
        (lambda rs: rs[120].update({"EXP-XREF-ACCT-ID": 999}), ["CBEXPORT.card_xref.parity"]),
        (lambda rs: rs.pop(470), ["CBEXPORT.count-by-type.D", "CBEXPORT.card.parity"]),
        (
            lambda rs: rs[5].update({"EXPORT-SEQUENCE-NUM": 3}),
            ["CBEXPORT.export.unique-key", "CBEXPORT.export.sequence-ascending"],
        ),
        (lambda rs: rs[60].update({"EXP-ACCT-CURR-BAL": "1.00"}), ["CBEXPORT.account.parity"]),
    ],
)
def test_export_checks_detect_faults(golden, export, mutate, expected):
    """A faulty export (as a Java CBEXPORT might write it) is re-encoded and checked as if read from disk."""
    _, records = export
    rs = copy.deepcopy(records)
    mutate(rs)
    layout = load_export_layout()
    written = [encode_export_record(r, layout) for r in rs]
    assert _failed(golden, written, rs, written) == expected


def test_round_trip_check_detects_packed_corruption(golden, export):
    raws, records = export
    layout = load_export_layout()
    reencoded = [encode_export_record(r, layout) for r in records]
    reencoded[60] = reencoded[60][:52] + bytes.fromhex("0000000000000f") + reencoded[60][59:]
    assert _failed(golden, raws, records, reencoded) == ["CBIMPORT.round-trip"]


def test_unknown_record_type_is_counted(golden, export):
    raws, records = export
    rs = copy.deepcopy(records)
    rs[0]["EXPORT-REC-TYPE"] = "Z"
    failed = _failed(golden, raws, rs, list(raws))
    assert {"CBIMPORT.errors", "CBEXPORT.export.domain.EXPORT-REC-TYPE", "CBEXPORT.count-by-type.C"} <= set(failed)


def test_export_golden_matches_source():
    g = build_export_golden()
    on_disk = load_golden("export")
    assert g == on_disk
    assert on_disk["control_totals"]["by_type"] == {"A": 50, "C": 50, "D": 50, "T": 300, "X": 50}
    assert on_disk["control_totals"]["sums"]["EXP-TRAN-AMT"] == "104801.54"
