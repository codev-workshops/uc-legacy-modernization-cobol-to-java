import json

from carddemo_harness.__main__ import main
from carddemo_harness.golden import load_golden


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
