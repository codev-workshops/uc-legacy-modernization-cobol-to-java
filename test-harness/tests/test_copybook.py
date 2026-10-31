import pytest

from carddemo_harness.copybook import elementary_fields, expand_pic, parse_copybook, pic_length
from carddemo_harness.datasets import CPY_DIR, DATASETS
from carddemo_harness.records import load_layout


@pytest.mark.parametrize("name", sorted(DATASETS))
def test_layout_length_matches_record_length(name):
    ds = DATASETS[name]
    assert load_layout(ds).length == ds.record_length


@pytest.mark.parametrize("name", sorted(DATASETS))
def test_fields_are_contiguous_and_within_record(name):
    ds = DATASETS[name]
    pos = 0
    for f in elementary_fields(load_layout(ds)):
        assert f.offset == pos, f.name
        pos += f.length
    assert pos == ds.record_length


def test_account_offsets_from_cvact01y():
    f = {x.name: x for x in elementary_fields(load_layout(DATASETS["account"]))}
    assert (f["ACCT-ID"].offset, f["ACCT-ID"].length) == (0, 11)
    assert (f["ACCT-CURR-BAL"].offset, f["ACCT-CURR-BAL"].length, f["ACCT-CURR-BAL"].scale) == (12, 12, 2)
    assert f["ACCT-CURR-BAL"].signed and f["ACCT-CURR-BAL"].kind == "zoned"
    assert (f["ACCT-ADDR-ZIP"].offset, f["ACCT-GROUP-ID"].offset, f["ACCT-GROUP-ID"].length) == (102, 112, 10)


@pytest.mark.parametrize(
    "pic,usage,expected",
    [
        ("X(10)", "", 10),
        ("9(11)", "", 11),
        ("S9(10)V99", "", 12),
        ("S9(7)V99", "COMP-3", 5),
        ("S9(4)", "COMP", 2),
        ("9(9)", "BINARY", 4),
        ("S9(18)", "COMP", 8),
        ("XX", "", 2),
    ],
)
def test_pic_length(pic, usage, expected):
    assert pic_length(pic, usage) == expected


def test_expand_pic():
    assert expand_pic("S9(3)V9(2)") == "S999V99"


def test_parse_inline_copybook_with_redefines_occurs_and_88():
    src = "\n".join(
        "       " + s
        for s in [
            "01  REC.",
            "    05  CODE          PIC X(02).",
            "        88  IS-A      VALUE 'AA'.",
            "        88  IS-B-OR-C VALUES 'BB' 'CC'.",
            "    05  AMT           PIC S9(5)V99 COMP-3.",
            "    05  ALT REDEFINES AMT PIC X(04).",
            "    05  ITEMS OCCURS 3 TIMES.",
            "        10  ITEM      PIC 9(02).",
            "*   comment line ignored",
            "    05  FILLER        PIC X(05).",
        ]
    )
    src = src.replace("       *", "      *")
    root = parse_copybook(src)
    rec = root.children[0]
    by = {c.name: c for c in rec.children}
    assert by["CODE"].values == {"IS-A": "'AA'", "IS-B-OR-C": "'BB' 'CC'"}
    assert by["AMT"].length == 4 and by["AMT"].kind == "packed"
    assert by["ALT"].offset == by["AMT"].offset
    assert by["ITEMS"].length == 6
    assert rec.length == 2 + 4 + 6 + 5


def test_real_copybook_88_levels():
    root = parse_copybook(CPY_DIR / "COCOM01Y.cpy")
    names = {f.name: f for f in elementary_fields(root)}
    assert "CDEMO-USRTYP-ADMIN" in names["CDEMO-USER-TYPE"].values


def test_value_clause_forms():
    from carddemo_harness.copybook import _value_clause

    assert _value_clause("VALUE 'Y'") == "'Y'"
    assert _value_clause("VALUES ARE 'A'   'B'") == "'A' 'B'"
    assert _value_clause("VALUE IS 0 THRU 9") == "0 THRU 9"
    assert _value_clause("PIC X") is None


def test_unnamed_and_filler_entries():
    src = "".join(" " * 7 + ln + "\n" for ln in ["01 REC.", "   05 PIC X(3).", "   05 FILLER PIC 9(2).", "   05 NAME-A PIC X."])
    root = parse_copybook(src)
    assert [(c.name, c.length) for c in root.children[0].children] == [("FILLER", 3), ("FILLER", 2), ("NAME-A", 1)]
