"""Fixed-width record parsing driven by copybook layouts."""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

from .copybook import Field, elementary_fields, parse_copybook
from .datasets import CPY_DIR, DATA_DIR, Dataset
from .decoders import DecodeError, decode_field, decode_field_bytes, encode_field_bytes


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


def _named(fields: list[Field]) -> list[Field]:
    return [f for f in fields if f.name != "FILLER" and not f.name.endswith("-FILLER")]


def parse_record_bytes(
    raw: bytes,
    fields: list[Field],
    codepage: str = "cp037",
    rec_no: int = 0,
    issues: list | None = None,
) -> dict:
    """Parse one binary record (EBCDIC DISPLAY, COMP-3 and COMP fields) using elementary copybook fields."""
    rec: dict = {}
    for f in _named(fields):
        chunk = raw[f.offset : f.offset + f.length]
        try:
            rec[f.name] = decode_field_bytes(f, chunk, codepage)
        except DecodeError as e:
            rec[f.name] = chunk.hex()
            if issues is not None:
                issues.append(ParseIssue(rec_no, f.name, str(e)))
    return rec


def split_fixed(data: bytes, record_length: int) -> list[bytes]:
    """Split a RECFM=FB dataset (no line delimiters) into records."""
    if len(data) % record_length:
        raise ValueError(f"file length {len(data)} is not a multiple of record length {record_length}")
    return [data[i : i + record_length] for i in range(0, len(data), record_length)]


def parse_binary_dataset(ds: Dataset, path: Path, codepage: str = "cp037") -> ParseResult:
    """Parse the EBCDIC (mainframe binary) form of a registered dataset."""
    layout = load_layout(ds)
    fields = elementary_fields(layout)
    result = ParseResult(ds.name)
    for i, raw in enumerate(split_fixed(path.read_bytes(), ds.record_length), 1):
        result.raw_lengths.add(len(raw))
        result.records.append(parse_record_bytes(raw, fields, codepage, i, result.issues))
    return result


def encode_record_bytes(rec: dict, fields: list[Field], record_length: int, codepage: str = "cp037") -> bytes:
    """Inverse of parse_record_bytes: FILLER and absent fields are written as spaces in the code page."""
    buf = bytearray(" ".encode(codepage) * record_length)
    for f in _named(fields):
        if f.name in rec:
            buf[f.offset : f.offset + f.length] = encode_field_bytes(f, rec[f.name], codepage)
    return bytes(buf)
