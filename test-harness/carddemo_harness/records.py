"""Fixed-width record parsing driven by copybook layouts."""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

from .copybook import Field, elementary_fields, parse_copybook
from .datasets import CPY_DIR, DATA_DIR, Dataset
from .decoders import DecodeError, decode_field


@dataclass
class ParseIssue:
    line: int
    field: str
    message: str


@dataclass
class ParseResult:
    dataset: str
    records: list[dict] = field(default_factory=list)
    issues: list[ParseIssue] = field(default_factory=list)
    raw_lengths: set[int] = field(default_factory=set)


def load_layout(ds: Dataset) -> Field:
    return parse_copybook(CPY_DIR / ds.copybook)


def parse_line(line: str, layout: Field, record_length: int, line_no: int = 0, issues: list | None = None) -> dict:
    """Parse one record. Short lines are right-padded with spaces (trailing FILLER is often trimmed)."""
    text = line.rstrip("\r\n")
    if len(text) > record_length:
        text = text[:record_length] if text[record_length:].strip() == "" else text
    text = text.ljust(record_length)
    if len(text) != record_length and issues is not None:
        issues.append(ParseIssue(line_no, "*", f"record length {len(text)} != {record_length}"))
    rec: dict = {}
    for f in elementary_fields(layout):
        if f.name == "FILLER" or f.name.endswith("-FILLER"):
            continue
        raw = text[f.offset : f.offset + f.length]
        try:
            rec[f.name] = decode_field(f, raw)
        except DecodeError as e:
            rec[f.name] = raw
            if issues is not None:
                issues.append(ParseIssue(line_no, f.name, str(e)))
    return rec


def parse_dataset(ds: Dataset, path: Path | None = None) -> ParseResult:
    layout = load_layout(ds)
    if layout.length != ds.record_length:
        raise ValueError(f"{ds.copybook} length {layout.length} != expected {ds.record_length}")
    result = ParseResult(ds.name)
    for i, line in enumerate((path or DATA_DIR / ds.file).read_text(encoding="latin-1").splitlines(), 1):
        if not line.strip():
            continue
        result.raw_lengths.add(len(line))
        result.records.append(parse_line(line, layout, ds.record_length, i, result.issues))
    return result


def encode_record(rec: dict, layout: Field, record_length: int) -> str:
    """Inverse of parse_line: rebuild the fixed-width DISPLAY record (FILLER as spaces). Used for round-trip tests
    and to write migrated data back into legacy format for differential runs."""
    from decimal import Decimal

    from .decoders import encode_zoned

    buf = [" "] * record_length
    for f in elementary_fields(layout):
        if f.name == "FILLER" or f.name.endswith("-FILLER") or f.name not in rec:
            continue
        v = rec[f.name]
        if f.kind == "zoned":
            text = " " * f.length if v is None else encode_zoned(Decimal(str(v)), f.digits, f.scale, f.signed)
        else:
            text = str(v)[: f.length].ljust(f.length)
        buf[f.offset : f.offset + f.length] = list(text)
    return "".join(buf)
