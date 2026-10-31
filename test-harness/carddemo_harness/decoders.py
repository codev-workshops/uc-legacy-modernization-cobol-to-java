"""Decoders for COBOL DISPLAY data in ASCII sample files (zoned decimal with overpunch sign)."""

from __future__ import annotations

from decimal import Decimal

from .copybook import Field

_POS = {"{": 0, **{chr(ord("A") + i): i + 1 for i in range(9)}}
_NEG = {"}": 0, **{chr(ord("J") + i): i + 1 for i in range(9)}}


class DecodeError(ValueError):
    pass


def decode_zoned(raw: str, scale: int = 0, signed: bool = False) -> Decimal:
    """Decode an ASCII zoned-decimal string; the last byte may carry an overpunched sign."""
    if raw == "" or raw.strip() == "":
        raise DecodeError("blank numeric field")
    body, last = raw[:-1], raw[-1]
    sign = 1
    if last.isdigit():
        digit = int(last)
    elif last in _POS:
        digit = _POS[last]
    elif last in _NEG:
        digit, sign = _NEG[last], -1
    else:
        raise DecodeError(f"invalid zoned sign byte {last!r} in {raw!r}")
    if body and not body.isdigit():
        raise DecodeError(f"non-numeric zoned digits in {raw!r}")
    if sign < 0 and not signed:
        raise DecodeError(f"negative value in unsigned field {raw!r}")
    value = Decimal(int(body + str(digit)) if body else digit)
    return (value * sign).scaleb(-scale)


def encode_zoned(value: Decimal, digits: int, scale: int = 0, signed: bool = False) -> str:
    """Inverse of decode_zoned: used by tests and to build differential fixtures."""
    q = int((Decimal(value).scaleb(scale)).to_integral_value(rounding="ROUND_DOWN"))
    neg = q < 0
    s = str(abs(q)).rjust(digits, "0")
    if len(s) > digits:
        raise DecodeError(f"{value} does not fit in {digits} digits")
    if not signed:
        if neg:
            raise DecodeError("negative value for unsigned field")
        return s
    table = {v: k for k, v in (_NEG if neg else _POS).items()}
    return s[:-1] + table[int(s[-1])]


def decode_field(f: Field, raw: str):
    """Return a JSON-friendly Python value for one elementary field."""
    kind = f.kind
    if kind == "alphanumeric" or kind == "edited":
        return raw.rstrip()
    if kind == "zoned":
        if raw.strip() == "":
            return None
        value = decode_zoned(raw, f.scale, f.signed)
        return str(value) if f.scale else int(value)
    raise DecodeError(f"{f.name}: {kind} fields cannot be decoded from ASCII text files")
