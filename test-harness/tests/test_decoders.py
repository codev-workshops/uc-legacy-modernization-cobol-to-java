from decimal import Decimal

import pytest

from carddemo_harness.decoders import DecodeError, decode_zoned, encode_zoned


@pytest.mark.parametrize(
    "raw,scale,expected",
    [
        ("00000001940{", 2, Decimal("194.00")),
        ("0000000005047G", 2, Decimal("504.77")),
        ("00150{", 2, Decimal("15.00")),
        ("12345", 0, Decimal(12345)),
        ("0000123A", 2, Decimal("12.31")),
        ("0000123}", 2, Decimal("-12.30")),
        ("0000123R", 2, Decimal("-12.39")),
        ("J", 0, Decimal(-1)),
    ],
)
def test_decode_overpunch(raw, scale, expected):
    assert decode_zoned(raw, scale, signed=True) == expected


@pytest.mark.parametrize(
    "ch,digit,sign",
    [("{", 0, 1), ("}", 0, -1)] + [(chr(65 + i), i + 1, 1) for i in range(9)] + [(chr(74 + i), i + 1, -1) for i in range(9)],
)
def test_every_sign_byte(ch, digit, sign):
    assert decode_zoned("00" + ch, 0, signed=True) == sign * digit


@pytest.mark.parametrize("raw", ["", "   ", "12X4", "12!"])
def test_invalid_zoned_rejected(raw):
    with pytest.raises(DecodeError):
        decode_zoned(raw, 0, signed=True)


def test_negative_in_unsigned_field_rejected():
    with pytest.raises(DecodeError):
        decode_zoned("001}", 0, signed=False)


@pytest.mark.parametrize("value", ["0", "0.01", "-0.01", "123.45", "-99999999.99", "99999999.99", "-7.00"])
def test_round_trip(value):
    v = Decimal(value)
    assert decode_zoned(encode_zoned(v, 10, 2, True), 2, True) == v


def test_round_trip_exhaustive_small_range():
    for cents in range(-2000, 2001, 7):
        v = Decimal(cents).scaleb(-2)
        assert decode_zoned(encode_zoned(v, 6, 2, True), 2, True) == v


def test_encode_overflow_and_unsigned_negative():
    with pytest.raises(DecodeError):
        encode_zoned(Decimal("1000"), 3)
    with pytest.raises(DecodeError):
        encode_zoned(Decimal("-1"), 3, signed=False)


def test_encode_truncates_extra_scale():
    assert encode_zoned(Decimal("1.239"), 4, 2, True) == "012C"
