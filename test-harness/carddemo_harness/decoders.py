"""Decoders for COBOL data: zoned decimal (DISPLAY, overpunch sign), packed decimal (COMP-3) and binary (COMP)."""

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


_PACKED_POSITIVE = {0xA, 0xC, 0xE, 0xF}
_PACKED_NEGATIVE = {0xB, 0xD}


def decode_packed(raw: bytes, scale: int = 0, signed: bool = False) -> Decimal:
    """Decode IBM packed decimal (COMP-3): two BCD digits per byte, sign in the low nibble of the last byte.

    Sign nibbles C, A, E (positive), F (unsigned) and D, B (negative) are accepted, as on z/OS.
    """
    if not raw:
        raise DecodeError("empty packed field")
    nibbles = [n for b in raw for n in (b >> 4, b & 0x0F)]
    digits, sign_nibble = nibbles[:-1], nibbles[-1]
    if any(d > 9 for d in digits):
        raise DecodeError(f"invalid packed digit nibble in {raw.hex()}")
    if sign_nibble in _PACKED_NEGATIVE:
        sign = -1
    elif sign_nibble in _PACKED_POSITIVE:
        sign = 1
    else:
        raise DecodeError(f"invalid packed sign nibble {sign_nibble:X} in {raw.hex()}")
    if sign < 0 and not signed:
        raise DecodeError(f"negative value in unsigned packed field {raw.hex()}")
    value = Decimal(int("".join(map(str, digits)) or "0"))
    return (value * sign).scaleb(-scale)


def encode_packed(value: Decimal, digits: int, scale: int = 0, signed: bool = False) -> bytes:
    """Inverse of decode_packed. Signed fields get C/D, unsigned fields get F (what IBM COBOL writes)."""
    q = int(Decimal(value).scaleb(scale).to_integral_value(rounding="ROUND_DOWN"))
    if q < 0 and not signed:
        raise DecodeError("negative value for unsigned field")
    s = str(abs(q))
    if len(s) > digits:
        raise DecodeError(f"{value} does not fit in {digits} digits")
    nbytes = (digits + 2) // 2
    sign = (0xD if q < 0 else 0xC) if signed else 0xF
    nibbles = [int(c) for c in s.rjust(nbytes * 2 - 1, "0")] + [sign]
    return bytes((nibbles[i] << 4) | nibbles[i + 1] for i in range(0, len(nibbles), 2))


def decode_binary(raw: bytes, scale: int = 0, signed: bool = False) -> Decimal:
    """Decode big-endian binary (COMP/BINARY/COMP-5); signed fields are two's complement."""
    if len(raw) not in (2, 4, 8):
        raise DecodeError(f"binary field must be 2, 4 or 8 bytes, got {len(raw)}")
    return Decimal(int.from_bytes(raw, "big", signed=signed)).scaleb(-scale)


def encode_binary(value: Decimal, length: int, digits: int, scale: int = 0, signed: bool = False) -> bytes:
    """Inverse of decode_binary. Enforces the PIC digit count (COBOL TRUNC(STD) behaviour)."""
    q = int(Decimal(value).scaleb(scale).to_integral_value(rounding="ROUND_DOWN"))
    if q < 0 and not signed:
        raise DecodeError("negative value for unsigned field")
    if len(str(abs(q))) > digits:
        raise DecodeError(f"{value} does not fit in {digits} digits")
    return q.to_bytes(length, "big", signed=signed)


def _json_number(value: Decimal, f: Field):
    return str(value) if f.scale else int(value)


def decode_field_bytes(f: Field, raw: bytes, codepage: str = "cp037"):
    """Decode one elementary field from a binary (e.g. EBCDIC RECFM=FB) record."""
    kind = f.kind
    if kind == "packed":
        return _json_number(decode_packed(raw, f.scale, f.signed), f)
    if kind == "binary":
        return _json_number(decode_binary(raw, f.scale, f.signed), f)
    return decode_field(f, raw.decode(codepage))


def encode_field_bytes(f: Field, value, codepage: str = "cp037") -> bytes:
    """Inverse of decode_field_bytes."""
    kind = f.kind
    if kind == "packed":
        return encode_packed(Decimal(str(value)), f.digits, f.scale, f.signed)
    if kind == "binary":
        return encode_binary(Decimal(str(value)), f.length, f.digits, f.scale, f.signed)
    if kind == "zoned":
        text = " " * f.length if value is None else encode_zoned(Decimal(str(value)), f.digits, f.scale, f.signed)
    else:
        text = str(value)[: f.length].ljust(f.length)
    return text.encode(codepage)
