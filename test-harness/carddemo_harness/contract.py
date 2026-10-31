"""Contract checks: migrated records must fit the copybook layout (types, lengths, digits, scale, sign, 88-levels)."""

from __future__ import annotations

from decimal import Decimal, InvalidOperation

from .copybook import Field, elementary_fields


def json_schema(layout: Field, title: str) -> dict:
    """Derive a JSON Schema (draft 2020-12) for the golden/canonical JSON form of a copybook record."""
    props: dict = {}
    for f in elementary_fields(layout):
        if f.name == "FILLER" or f.name.endswith("-FILLER"):
            continue
        if f.kind == "alphanumeric":
            p = {"type": "string", "maxLength": f.length}
        elif f.scale:
            int_digits = f.digits - f.scale
            sign = "-?" if f.signed else ""
            p = {"type": "string", "pattern": rf"^{sign}\d{{1,{int_digits}}}\.\d{{{f.scale}}}$"}
        else:
            p = {"type": ["integer", "null"], "minimum": -(10**f.digits - 1) if f.signed else 0, "maximum": 10**f.digits - 1}
        enum = _enum(f)
        if enum:
            p["x-cobol-88"] = enum
        p["x-cobol-pic"] = f.pic
        p["x-cobol-offset"] = f.offset
        props[f.name] = p
    return {
        "$schema": "https://json-schema.org/draft/2020-12/schema",
        "title": title,
        "type": "object",
        "properties": props,
        "required": sorted(props),
        "additionalProperties": False,
    }


def _enum(f: Field) -> dict[str, list[str]]:
    out = {}
    for name, raw in f.values.items():
        if " THRU " in raw.upper():
            continue
        out[name] = [v.strip().strip("'\"") for v in raw.replace(",", " ").split() if v.strip()]
    return out


def validate(record: dict, layout: Field) -> list[str]:
    """Return human-readable contract violations (empty list = record honours the copybook contract)."""
    errors: list[str] = []
    fields = [f for f in elementary_fields(layout) if f.name != "FILLER" and not f.name.endswith("-FILLER")]
    names = {f.name for f in fields}
    for extra in sorted(set(record) - names):
        errors.append(f"{extra}: not in copybook")
    for f in fields:
        if f.name not in record:
            errors.append(f"{f.name}: missing")
            continue
        v = record[f.name]
        if f.kind == "alphanumeric":
            if not isinstance(v, str):
                errors.append(f"{f.name}: expected string, got {type(v).__name__}")
            elif len(v.encode("latin-1", errors="replace")) > f.length:
                errors.append(f"{f.name}: length {len(v)} exceeds PIC {f.pic} ({f.length})")
            continue
        if v is None:
            continue
        if isinstance(v, bool) or isinstance(v, float):
            errors.append(f"{f.name}: {type(v).__name__} not allowed for PIC {f.pic} (use integer or decimal string)")
            continue
        try:
            n = Decimal(str(v))
        except InvalidOperation:
            errors.append(f"{f.name}: {v!r} is not numeric")
            continue
        if n < 0 and not f.signed:
            errors.append(f"{f.name}: negative value in unsigned PIC {f.pic}")
        exp = n.as_tuple().exponent
        if isinstance(exp, int) and -exp > f.scale:
            errors.append(f"{f.name}: {v} has more than {f.scale} decimal places")
        if abs(n) >= Decimal(10) ** (f.digits - f.scale):
            errors.append(f"{f.name}: {v} overflows PIC {f.pic}")
    return errors
