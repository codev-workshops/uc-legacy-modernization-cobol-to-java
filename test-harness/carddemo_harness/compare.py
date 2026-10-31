"""Differential comparison of legacy (expected) and migrated (actual) record sets."""

from __future__ import annotations

from collections.abc import Iterable
from dataclasses import dataclass, field
from decimal import Decimal, InvalidOperation


@dataclass
class FieldDiff:
    key: str
    field: str
    expected: object
    actual: object


@dataclass
class DiffReport:
    expected_count: int = 0
    actual_count: int = 0
    missing: list[str] = field(default_factory=list)
    unexpected: list[str] = field(default_factory=list)
    duplicates: list[str] = field(default_factory=list)
    mismatches: list[FieldDiff] = field(default_factory=list)

    @property
    def ok(self) -> bool:
        return not (self.missing or self.unexpected or self.duplicates or self.mismatches)

    def summary(self) -> str:
        return (
            f"expected={self.expected_count} actual={self.actual_count} missing={len(self.missing)} "
            f"unexpected={len(self.unexpected)} duplicates={len(self.duplicates)} mismatches={len(self.mismatches)}"
        )


def _num(v):
    if isinstance(v, bool) or v is None:
        return None
    try:
        return Decimal(str(v))
    except (InvalidOperation, ValueError):
        return None


def values_equal(expected, actual, tolerance: Decimal = Decimal("0"), strip: bool = True) -> bool:
    """Numbers compare by value (so 1, "1", "1.00" are equal); strings compare after trailing-space trim."""
    if expected is None or actual is None:
        return expected is None and actual is None
    if isinstance(expected, str) and isinstance(actual, str) and _num(expected) is None:
        return expected.rstrip() == actual.rstrip() if strip else expected == actual
    ne, na = _num(expected), _num(actual)
    if ne is not None and na is not None:
        return abs(ne - na) <= tolerance
    return str(expected).rstrip() == str(actual).rstrip()


def index(records: Iterable[dict], key: Iterable[str]) -> tuple[dict[str, dict], list[str]]:
    key = tuple(key)
    out: dict[str, dict] = {}
    dups: list[str] = []
    for r in records:
        k = "|".join(str(r.get(f)) for f in key)
        if k in out:
            dups.append(k)
        out[k] = r
    return out, dups


def compare_records(
    expected: Iterable[dict],
    actual: Iterable[dict],
    key: Iterable[str],
    ignore: Iterable[str] = (),
    tolerance: Decimal = Decimal("0"),
    fields: Iterable[str] | None = None,
) -> DiffReport:
    key = tuple(key)
    ignore = set(ignore)
    e_idx, e_dups = index(expected, key)
    a_idx, a_dups = index(actual, key)
    rep = DiffReport(
        expected_count=len(e_idx) + len(e_dups),
        actual_count=len(a_idx) + len(a_dups),
        duplicates=sorted(set(e_dups) | set(a_dups)),
    )
    rep.missing = sorted(set(e_idx) - set(a_idx))
    rep.unexpected = sorted(set(a_idx) - set(e_idx))
    for k in sorted(set(e_idx) & set(a_idx)):
        e, a = e_idx[k], a_idx[k]
        names = list(fields) if fields is not None else sorted(set(e) | set(a))
        for f in names:
            if f in ignore:
                continue
            if not values_equal(e.get(f), a.get(f), tolerance):
                rep.mismatches.append(FieldDiff(k, f, e.get(f), a.get(f)))
    return rep
