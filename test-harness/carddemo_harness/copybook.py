"""Minimal fixed-format COBOL copybook parser producing byte layouts."""

from __future__ import annotations

import math
import re
from dataclasses import dataclass, field
from pathlib import Path

_USAGE = re.compile(r"\b(COMP-3|COMPUTATIONAL-3|PACKED-DECIMAL|COMP-5|COMP|BINARY|COMPUTATIONAL)\b")
_PACKED = {"COMP-3", "COMPUTATIONAL-3", "PACKED-DECIMAL"}
_BINARY = {"COMP", "COMP-5", "BINARY", "COMPUTATIONAL"}


@dataclass
class Field:
    level: int
    name: str
    pic: str = ""
    usage: str = ""
    occurs: int = 1
    redefines: str = ""
    offset: int = 0
    length: int = 0
    children: list[Field] = field(default_factory=list)
    values: dict[str, str] = field(default_factory=dict)

    @property
    def is_group(self) -> bool:
        return not self.pic

    @property
    def signed(self) -> bool:
        return self.pic.upper().startswith("S")

    @property
    def scale(self) -> int:
        p = expand_pic(self.pic)
        return p.split("V", 1)[1].count("9") if "V" in p else 0

    @property
    def digits(self) -> int:
        return expand_pic(self.pic).count("9")

    @property
    def kind(self) -> str:
        p = expand_pic(self.pic).lstrip("S")
        if not p:
            return "group"
        if re.fullmatch(r"[XA]+", p):
            return "alphanumeric"
        if re.fullmatch(r"9+(V9+)?", p):
            return "packed" if self.usage in _PACKED else "binary" if self.usage in _BINARY else "zoned"
        return "edited"


def expand_pic(pic: str) -> str:
    return re.sub(r"(.)\((\d+)\)", lambda m: m.group(1) * int(m.group(2)), pic.upper())


def pic_length(pic: str, usage: str) -> int:
    p = expand_pic(pic)
    digits = len(re.findall(r"[9PZ*]", p))
    if usage in _PACKED:
        return math.ceil((digits + 1) / 2)
    if usage in _BINARY:
        return 2 if digits <= 4 else 4 if digits <= 9 else 8
    return len(p.replace("S", "").replace("V", ""))


def _statements(text: str) -> list[str]:
    code = []
    for raw in text.splitlines():
        if len(raw) > 6 and raw[6] in "*/":
            continue
        body = raw[7:72] if len(raw) > 7 else ""
        if body.strip():
            code.append(body)
    joined = " ".join(code)
    return [s.strip() for s in re.split(r"\.(?:\s|$)", joined) if s.strip()]


def parse_copybook(source: str | Path) -> Field:
    """Parse a copybook into a root group Field with computed offsets and lengths."""
    text = source.read_text(encoding="latin-1") if isinstance(source, Path) else source
    root = Field(level=0, name="ROOT")
    stack = [root]
    last: Field | None = None
    for st in _statements(text):
        m = re.match(r"^(\d{1,2})\s+([A-Z0-9][A-Z0-9-]*)?\s*(.*)$", st, re.I)
        if not m:
            continue
        level, name, rest = int(m.group(1)), (m.group(2) or "FILLER").upper(), m.group(3)
        if name in ("PIC", "PICTURE"):
            name, rest = "FILLER", f"PIC {rest}"
        if level == 88:
            vm = re.search(r"VALUES?\s+(?:ARE\s+|IS\s+)?(.+)$", rest, re.I)
            if last is not None and vm:
                last.values[name] = re.sub(r"\s+", " ", vm.group(1)).strip()
            continue
        pic = re.search(r"\bPIC(?:TURE)?\s+(?:IS\s+)?(\S+)", rest, re.I)
        usage = _USAGE.search(rest.upper())
        occ = re.search(r"OCCURS\s+(\d+)", rest, re.I)
        red = re.search(r"REDEFINES\s+([A-Z0-9-]+)", rest, re.I)
        f = Field(
            level=level,
            name=name,
            pic=pic.group(1) if pic else "",
            usage=usage.group(1) if usage else "",
            occurs=int(occ.group(1)) if occ else 1,
            redefines=red.group(1).upper() if red else "",
        )
        while stack[-1] is not root and stack[-1].level >= level:
            stack.pop()
        stack[-1].children.append(f)
        stack.append(f)
        last = f
    _layout(root, 0)
    return root


def _layout(group: Field, offset: int) -> int:
    group.offset = offset
    pos = offset
    by_name: dict[str, Field] = {}
    for c in group.children:
        start = by_name[c.redefines].offset if c.redefines in by_name else pos
        if c.is_group:
            size = _layout(c, start) - start
        else:
            c.offset = start
            size = pic_length(c.pic, c.usage)
        c.length = size * c.occurs
        by_name[c.name] = c
        if not c.redefines:
            pos = start + c.length
    group.length = pos - offset
    return pos


def elementary_fields(root: Field, include_redefines: bool = False) -> list[Field]:
    out: list[Field] = []

    def walk(f: Field) -> None:
        for c in f.children:
            if c.redefines and not include_redefines:
                continue
            if c.is_group:
                walk(c)
            else:
                out.append(c)

    walk(root)
    return out
