"""Minimal fixed-format COBOL copybook parser producing byte layouts."""

from __future__ import annotations

import math
import re
from dataclasses import dataclass, field, replace
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


_NAME = re.compile(r"[A-Z0-9][A-Z0-9-]*", re.I)


def _value_clause(rest: str) -> str | None:
    """Return the literal list of a VALUE/VALUES [IS|ARE] clause, whitespace-normalised."""
    toks = rest.split()
    for i, tok in enumerate(toks):
        if tok.upper() in ("VALUE", "VALUES"):
            j = i + 1 if i + 1 < len(toks) and toks[i + 1].upper() in ("IS", "ARE") else i
            return " ".join(toks[j + 1 :]) or None
    return None


def parse_copybook(source: str | Path) -> Field:
    """Parse a copybook into a root group Field with computed offsets and lengths."""
    text = source.read_text(encoding="latin-1") if isinstance(source, Path) else source
    root = Field(level=0, name="ROOT")
    stack = [root]
    last: Field | None = None
    for st in _statements(text):
        parts = st.split(None, 2)
        if not parts or not parts[0].isdigit() or len(parts[0]) > 2:
            continue
        level = int(parts[0])
        if len(parts) > 1 and _NAME.fullmatch(parts[1]) and parts[1].upper() not in ("PIC", "PICTURE"):
            name, rest = parts[1].upper(), parts[2] if len(parts) > 2 else ""
        else:
            name, rest = "FILLER", " ".join(parts[1:])
        if level == 88:
            value = _value_clause(rest)
            if last is not None and value:
                last.values[name] = value
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


def occurrence_fields(group: Field, include_redefines: bool = False) -> list[Field]:
    """Elementary fields of a group with every OCCURS occurrence laid out separately.

    Occurrences are named with COBOL subscripts, e.g. ``EXP-CUST-ADDR-LINE(2)``.
    """
    out: list[Field] = []

    def walk(f: Field, base: int, suffix: str) -> None:
        for c in f.children:
            if c.redefines and not include_redefines:
                continue
            size = c.length // c.occurs
            for i in range(c.occurs):
                sub = f"{suffix}{',' if suffix else ''}{i + 1}" if c.occurs > 1 else suffix
                start = c.offset + base + i * size
                if c.is_group:
                    walk(c, start - c.offset, sub)
                else:
                    name = f"{c.name}({sub})" if sub else c.name
                    out.append(replace(c, name=name, offset=start, length=size, occurs=1, children=[]))

    walk(group, 0, "")
    return out


def find_field(root: Field, name: str) -> Field:
    """Depth-first lookup of a (group or elementary) field by name."""
    stack = [root]
    while stack:
        f = stack.pop()
        if f.name == name.upper():
            return f
        stack.extend(reversed(f.children))
    raise KeyError(name)
