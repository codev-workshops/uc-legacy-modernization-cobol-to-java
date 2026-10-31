import json

import pytest

from carddemo_harness.__main__ import main
from carddemo_harness.golden import load_golden


@pytest.fixture(autouse=True)
def _in_tmp(tmp_path, monkeypatch):
    monkeypatch.chdir(tmp_path)


def test_paths_outside_working_directory_are_refused(tmp_path):
    with pytest.raises(SystemExit, match="outside the working directory"):
        main(["contract", "card", "../outside.json"])
    with pytest.raises(SystemExit, match="outside the working directory"):
        main(["reconcile", "--report", "/etc/report.json"])


def test_reconcile_cli(tmp_path, capsys):
    out = tmp_path / "r.json"
    assert main(["reconcile", "--report", str(out)]) == 0
    assert json.loads(out.read_text())["summary"]["failed"] == []
    assert "checks passed" in capsys.readouterr().out


def test_compare_cli(tmp_path):
    recs = load_golden("card")["records"]
    good, bad = tmp_path / "good.json", tmp_path / "bad.json"
    good.write_text(json.dumps(recs))
    bad.write_text(json.dumps(recs[1:]))
    assert main(["compare", "card", str(good)]) == 0
    assert main(["compare", "card", str(bad)]) == 1


def test_contract_cli(tmp_path):
    recs = load_golden("transaction_type")["records"]
    p = tmp_path / "t.json"
    p.write_text(json.dumps(recs))
    assert main(["contract", "transaction_type", str(p)]) == 0
    recs[0]["TRAN-TYPE"] = "TOO-LONG"
    p.write_text(json.dumps(recs))
    assert main(["contract", "transaction_type", str(p)]) == 1


def test_decode_cli(tmp_path, capsys):
    import shutil

    from carddemo_harness.datasets import EBCDIC_DIR

    shutil.copy(EBCDIC_DIR / "AWS.M2.CARDDEMO.EXPORT.DATA.PS", tmp_path / "export.bin")
    shutil.copy(EBCDIC_DIR / "AWS.M2.CARDDEMO.CARDDATA.PS", tmp_path / "card.bin")
    assert main(["decode", "export", "export.bin"]) == 0
    out = json.loads(capsys.readouterr().out)
    assert len(out["records"]) == 500 and out["parse_issues"] == []
    assert main(["decode", "card", "card.bin"]) == 0
    (tmp_path / "card.json").write_text(capsys.readouterr().out)
    assert main(["compare", "card", "card.json"]) == 0
    raw = bytearray((tmp_path / "export.bin").read_bytes())
    raw[52 + 500 * 50 : 59 + 500 * 50] = b"\x40" * 7
    (tmp_path / "bad.bin").write_bytes(bytes(raw))
    assert main(["decode", "export", "bad.bin"]) == 1
    assert "EXP-ACCT-CURR-BAL" in capsys.readouterr().err
