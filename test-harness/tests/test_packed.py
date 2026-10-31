from decimal import Decimal
from pathlib import Path

import pytest

from carddemo_harness.copybook import elementary_fields, parse_copybook, pic_length
from carddemo_harness.decoders import (
    DecodeError,
    decode_binary,
    decode_field_bytes,
    decode_packed,
    encode_binary,
    encode_field_bytes,
    encode_packed,
)
from carddemo_harness.records import encode_record_bytes, parse_record_bytes, split_fixed


@pytest.mark.parametrize(
    "hexval,scale,signed,expected",
    [
        ("0000000000000c", 2, True, Decimal("0.00")),
        ("0000000102000c", 2, True, Decimal("1020.00")),
        ("12345c", 2, True, Decimal("123.45")),
        ("12345d", 2, True, Decimal("-123.45")),
        ("12345b", 0, True, Decimal(-12345)),
        ("12345a", 0, True, Decimal(12345)),
        ("12345e", 0, True, Decimal(12345)),
        ("12345f", 0, False, Decimal(12345)),
        ("300f", 0, False, Decimal(300)),
        ("0c", 0, True, Decimal(0)),
        ("9d", 0, True, Decimal(-9)),
        ("0999999999999d", 2, True, Decimal("-9999999999.99")),
    ],
)
def test_decode_packed(hexval, scale, signed, expected):
    assert decode_packed(bytes.fromhex(hexval), scale, signed) == expected


@pytest.mark.parametrize("hexval", ["", "1234", "12340c"[:-2] + "05", "1a3c", "a0", "404040", "000000"])
def test_invalid_packed_rejected(hexval):
    with pytest.raises(DecodeError):
        decode_packed(bytes.fromhex(hexval), 0, True)


def test_negative_packed_in_unsigned_field_rejected():
    with pytest.raises(DecodeError, match="unsigned"):
        decode_packed(bytes.fromhex("123d"), 0, False)


@pytest.mark.parametrize(
    "value,digits,scale,signed,hexval",
    [
        ("123.45", 5, 2, True, "12345c"),
        ("-123.45", 5, 2, True, "12345d"),
        ("0", 3, 0, True, "000c"),
        ("720", 3, 0, False, "720f"),
        ("1020.00", 12, 2, True, "0000000102000c"),
        ("-0.01", 11, 2, True, "00000000001d"),
        ("1.239", 5, 2, True, "00123c"),
    ],
)
def test_encode_packed(value, digits, scale, signed, hexval):
    assert encode_packed(Decimal(value), digits, scale, signed).hex() == hexval


def test_packed_byte_length_matches_copybook_length():
    for digits in range(1, 19):
        assert len(encode_packed(Decimal(0), digits)) == pic_length(f"9({digits})", "COMP-3")


@pytest.mark.parametrize("value", ["0.00", "0.01", "-0.01", "99999999.99", "-99999999.99", "123456.78", "-5.00"])
def test_packed_round_trip(value):
    v = Decimal(value)
    assert decode_packed(encode_packed(v, 10, 2, True), 2, True) == v


def test_packed_overflow_and_unsigned_negative_rejected():
    with pytest.raises(DecodeError, match="does not fit"):
        encode_packed(Decimal("1000"), 3)
    with pytest.raises(DecodeError, match="negative"):
        encode_packed(Decimal("-1"), 3, 0, False)


@pytest.mark.parametrize(
    "hexval,scale,signed,expected",
    [
        ("00000001", 0, False, Decimal(1)),
        ("000001fd", 0, False, Decimal(509)),
        ("ffffffff", 0, True, Decimal(-1)),
        ("fffe", 0, True, Decimal(-2)),
        ("0000000000003039", 2, True, Decimal("123.45")),
        ("ffffffffffffcfc7", 2, True, Decimal("-123.45")),
    ],
)
def test_decode_binary(hexval, scale, signed, expected):
    assert decode_binary(bytes.fromhex(hexval), scale, signed) == expected


def test_binary_round_trip_and_limits():
    for v in ["0", "1", "-1", "999999999", "-999999999"]:
        raw = encode_binary(Decimal(v), 4, 9, 0, True)
        assert decode_binary(raw, 0, True) == Decimal(v)
    with pytest.raises(DecodeError, match="does not fit"):
        encode_binary(Decimal("1000000000"), 4, 9)
    with pytest.raises(DecodeError, match="negative"):
        encode_binary(Decimal("-1"), 4, 9)
    with pytest.raises(DecodeError, match="2, 4 or 8"):
        decode_binary(b"\x00\x01\x02", 0)


COPYBOOK = "".join(
    " " * 7 + ln + "\n"
    for ln in [
        "01  REC.",
        "    05  ID            PIC 9(09) COMP.",
        "    05  NAME          PIC X(10).",
        "    05  BAL           PIC S9(10)V99 COMP-3.",
        "    05  SCORE         PIC 9(03) COMP-3.",
        "    05  LIMIT         PIC S9(10)V99.",
        "    05  DEBIT         PIC S9(10)V99 COMP.",
        "    05  FILLER        PIC X(5).",
    ]
)


def test_mixed_usage_record_round_trip():
    layout = parse_copybook(COPYBOOK)
    fields = elementary_fields(layout)
    assert [f.length for f in fields] == [4, 10, 7, 2, 12, 8, 5]
    rec = {"ID": 42, "NAME": "ALICE", "BAL": "-1234.56", "SCORE": 720, "LIMIT": "5000.00", "DEBIT": "-12.34"}
    raw = encode_record_bytes(rec, fields, layout.length)
    assert len(raw) == layout.length == 48
    assert raw[14:21].hex() == "0000000123456d"
    assert raw[21:23].hex() == "720f"
    assert raw[23:35].decode("cp037") == "00000050000{"
    assert raw[-5:] == "     ".encode("cp037")
    assert parse_record_bytes(raw, fields) == rec


def test_bad_packed_bytes_are_reported_not_raised():
    layout = parse_copybook(COPYBOOK)
    raw = bytearray(encode_record_bytes({"ID": 1, "BAL": "1.00", "SCORE": 1}, elementary_fields(layout), layout.length))
    raw[14:21] = b"\x40" * 7
    issues = []
    rec = parse_record_bytes(bytes(raw), elementary_fields(layout), issues=issues)
    assert rec["BAL"] == "40404040404040"
    assert [(i.field, "sign nibble" in i.message) for i in issues] == [("BAL", True)]


def test_field_bytes_dispatch_on_usage():
    by = {f.name: f for f in elementary_fields(parse_copybook(COPYBOOK))}
    assert decode_field_bytes(by["NAME"], "BOB".ljust(10).encode("cp037")) == "BOB"
    assert decode_field_bytes(by["LIMIT"], "00000000001A".encode("cp037")) == "0.11"
    assert encode_field_bytes(by["SCORE"], 5) == bytes.fromhex("005f")


def test_split_fixed(tmp_path: Path):
    assert split_fixed(b"abcdef", 3) == [b"abc", b"def"]
    with pytest.raises(ValueError, match="multiple"):
        split_fixed(b"abcd", 3)
