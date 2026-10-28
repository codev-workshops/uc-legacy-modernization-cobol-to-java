#!/usr/bin/env python3
"""Decode the CardDemo seed files into canonical golden JSON plus a reconciliation manifest.

Independent reference implementation (Python, stdlib only). The Java harness in
test_harness/ decodes the same raw files with its own codec and must reproduce these
files exactly; any disagreement is a codec defect in one of the two implementations.

Usage (from the repository root):  python3 golden_data/tools/generate_golden.py [--check]
Output is deterministic: identical inputs produce byte-identical files.
"""
import hashlib
import json
import re
import sys
from collections import Counter
from decimal import Decimal
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CPY = ROOT / "app" / "cpy"
ASCII_DIR = ROOT / "app" / "data" / "ASCII"
EBCDIC_DIR = ROOT / "app" / "data" / "EBCDIC"
OUT = ROOT / "golden_data"

# dataset -> (copybook, 01-level record, ASCII file, EBCDIC counterpart, key fields)
DATASETS = {
    "acctdata": ("CVACT01Y", "ACCOUNT-RECORD", "acctdata.txt", "AWS.M2.CARDDEMO.ACCTDATA.PS", ["ACCT-ID"]),
    "carddata": ("CVACT02Y", "CARD-RECORD", "carddata.txt", "AWS.M2.CARDDEMO.CARDDATA.PS", ["CARD-NUM"]),
    "cardxref": ("CVACT03Y", "CARD-XREF-RECORD", "cardxref.txt", "AWS.M2.CARDDEMO.CARDXREF.PS", ["XREF-CARD-NUM"]),
    "custdata": ("CVCUS01Y", "CUSTOMER-RECORD", "custdata.txt", "AWS.M2.CARDDEMO.CUSTDATA.PS", ["CUST-ID"]),
    "dailytran": ("CVTRA06Y", "DALYTRAN-RECORD", "dailytran.txt", "AWS.M2.CARDDEMO.DALYTRAN.PS", ["DALYTRAN-ID"]),
    "discgrp": ("CVTRA02Y", "DIS-GROUP-RECORD", "discgrp.txt", "AWS.M2.CARDDEMO.DISCGRP.PS",
                ["DIS-ACCT-GROUP-ID", "DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"]),
    "tcatbal": ("CVTRA01Y", "TRAN-CAT-BAL-RECORD", "tcatbal.txt", "AWS.M2.CARDDEMO.TCATBALF.PS",
                ["TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"]),
    "trancatg": ("CVTRA04Y", "TRAN-CAT-RECORD", "trancatg.txt", "AWS.M2.CARDDEMO.TRANCATG.PS",
                 ["TRAN-TYPE-CD", "TRAN-CAT-CD"]),
    "trantype": ("CVTRA03Y", "TRAN-TYPE-RECORD", "trantype.txt", "AWS.M2.CARDDEMO.TRANTYPE.PS", ["TRAN-TYPE"]),
}
# EBCDIC-only dataset: the only seed file that carries COMP / COMP-3 and discriminated REDEFINES.
EXPORT = ("CVEXPORT", "EXPORT-RECORD", "AWS.M2.CARDDEMO.EXPORT.DATA.PS", ["EXPORT-SEQUENCE-NUM"])
EXPORT_DISCRIMINATOR = "EXPORT-REC-TYPE"
EXPORT_BRANCHES = {  # from CBEXPORT.cbl MOVE 'x' TO EXPORT-REC-TYPE / CBIMPORT.cbl EVALUATE
    "C": "EXPORT-CUSTOMER-DATA", "A": "EXPORT-ACCOUNT-DATA", "X": "EXPORT-CARD-XREF-DATA",
    "T": "EXPORT-TRANSACTION-DATA", "D": "EXPORT-CARD-DATA",
}

ASCII_POS = {"{": 0, **{chr(ord("A") + i): i + 1 for i in range(9)}}
ASCII_NEG = {"}": 0, **{chr(ord("J") + i): i + 1 for i in range(9)}}


# --------------------------------------------------------------------------- copybook parser
class Field:
    def __init__(self, level, name):
        self.level, self.name = level, name
        self.pic = None
        self.usage = "DISPLAY"
        self.redefines = None
        self.occurs = 1
        self.children = []
        self.offset = 0  # 0-based within record
        self.length = 0  # one occurrence

    @property
    def is_group(self):
        return self.pic is None

    def numeric(self):
        """Return (signed, int_digits, scale) for numeric PICs, else None."""
        p = expand_pic(self.pic)
        if not re.fullmatch(r"S?9+(V9+)?", p):
            return None
        signed = p.startswith("S")
        body = p.lstrip("S")
        ip, _, fp = body.partition("V")
        return signed, len(ip), len(fp)


def expand_pic(pic):
    return re.sub(r"(.)\((\d+)\)", lambda m: m.group(1) * int(m.group(2)), pic.upper())


def storage_length(f):
    if f.is_group:
        return sum(c.length * c.occurs for c in f.children if c.redefines is None)
    num = f.numeric()
    if f.usage == "COMP-3":
        _, i, s = num
        return (i + s) // 2 + 1
    if f.usage in ("COMP", "COMP-4", "COMP-5", "BINARY"):
        _, i, s = num
        d = i + s
        return 2 if d <= 4 else 4 if d <= 9 else 8
    return len(expand_pic(f.pic).replace("S", "").replace("V", ""))


def parse_copybook(name, record):
    lines = []
    for raw in (CPY / f"{name}.cpy").read_text(encoding="ascii").splitlines():
        if len(raw) > 6 and raw[6] in "*/":
            continue
        lines.append(raw[7:72] if len(raw) > 7 else "")
    text = " ".join(lines)
    stmts = [s.strip() for s in re.split(r"\.(?=\s|$)", text) if s.strip()]
    root, stack, inside = None, [], False
    for st in stmts:
        tok = st.split()
        if not tok[0].isdigit():
            continue
        level = int(tok[0])
        if level == 88:
            continue
        if level == 1:
            inside = tok[1] == record
            if inside:
                root = Field(1, tok[1])
                stack = [root]
            continue
        if not inside:
            continue
        f = Field(level, tok[1])
        up = [t.upper() for t in tok]
        for i, t in enumerate(up):
            if t in ("PIC", "PICTURE"):
                f.pic = tok[i + 1]
            elif t == "REDEFINES":
                f.redefines = tok[i + 1]
            elif t == "OCCURS":
                f.occurs = int(tok[i + 1])
            elif t in ("COMP", "COMP-3", "COMP-4", "COMP-5", "BINARY", "COMPUTATIONAL", "COMPUTATIONAL-3"):
                f.usage = {"COMPUTATIONAL": "COMP", "COMPUTATIONAL-3": "COMP-3", "COMP-4": "COMP"}.get(t, t)
        while stack[-1].level >= level:
            stack.pop()
        stack[-1].children.append(f)
        stack.append(f)

    def layout(f, base):
        f.offset = base
        cur = base
        for c in f.children:
            if c.redefines:
                target = next(x for x in f.children if x.name == c.redefines)
                layout(c, target.offset)
                continue
            layout(c, cur)
            cur += c.length * c.occurs
        f.length = storage_length(f)
        if f.children:
            for c in f.children:
                if c.redefines:
                    target = next(x for x in f.children if x.name == c.redefines)
                    if c.length > target.length:
                        raise ValueError(f"{c.name} longer than {target.name}")
    layout(root, 0)
    return root


def layout_rows(f, out, prefix=""):
    for c in f.children:
        row = {"name": c.name, "level": c.level, "offset": c.offset + 1, "length": c.length}
        if c.pic:
            row["pic"] = c.pic
            row["usage"] = c.usage
        if c.occurs > 1:
            row["occurs"] = c.occurs
        if c.redefines:
            row["redefines"] = c.redefines
        out.append(row)
        if c.children and c.occurs == 1:
            layout_rows(c, out)
        elif c.children:
            sub = []
            layout_rows(c, sub)
            row["occursLayout"] = sub  # offsets of occurrence 1
    return out


# --------------------------------------------------------------------------- field decoders
class Stats:
    def __init__(self):
        self.undecodable = Counter()
        self.non_preferred_sign = Counter()


def canon(value: Decimal, scale: int) -> str:
    q = value.scaleb(-scale) if scale else value
    s = f"{q:.{scale}f}"
    return "0" + s[2:] if s.startswith("-0") and Decimal(s) == 0 else s


def undecodable(kind, b):
    return {"$type": kind, "hex": b.hex().upper()}


def decode_display_numeric(f, b, enc, st):
    signed, i, s = f.numeric()
    if enc == "ascii":
        txt = b.decode("latin-1")
        body, last = txt[:-1], txt[-1]
        if not body.isdigit() and body != "":
            st.undecodable[f.name] += 1
            return undecodable("ZONED", b)
        if last.isdigit():
            neg, d = False, int(last)
            if signed:
                st.non_preferred_sign[f.name] += 1
        elif signed and last in ASCII_POS:
            neg, d = False, ASCII_POS[last]
        elif signed and last in ASCII_NEG:
            neg, d = True, ASCII_NEG[last]
        else:
            st.undecodable[f.name] += 1
            return undecodable("ZONED", b)
        digits = body + str(d)
    else:
        if any(x >> 4 != 0xF or (x & 0xF) > 9 for x in b[:-1]):
            st.undecodable[f.name] += 1
            return undecodable("ZONED", b)
        zone, d = b[-1] >> 4, b[-1] & 0xF
        if d > 9 or zone not in (0xC, 0xD, 0xF) or (not signed and zone != 0xF):
            st.undecodable[f.name] += 1
            return undecodable("ZONED", b)
        if signed and zone == 0xF:
            st.non_preferred_sign[f.name] += 1
        neg = zone == 0xD
        digits = "".join(str(x & 0xF) for x in b[:-1]) + str(d)
    v = Decimal(int(digits))
    if neg and v == 0:
        st.undecodable[f.name] += 1  # negative zero has no canonical decimal form
        return undecodable("ZONED_NEGATIVE_ZERO", b)
    return canon(-v if neg else v, s)


def decode_packed(f, b, st):
    signed, _, s = f.numeric()
    nibbles = [n for x in b for n in (x >> 4, x & 0xF)]
    digits, sign = nibbles[:-1], nibbles[-1]
    if any(n > 9 for n in digits) or sign not in (0xC, 0xD, 0xF) or (not signed and sign != 0xF):
        st.undecodable[f.name] += 1
        return undecodable("PACKED", b)
    if signed and sign == 0xF:
        st.non_preferred_sign[f.name] += 1
    v = Decimal(int("".join(map(str, digits))))
    if sign == 0xD and v == 0:
        st.undecodable[f.name] += 1
        return undecodable("PACKED_NEGATIVE_ZERO", b)
    return canon(-v if sign == 0xD else v, s)


def decode_binary(f, b, st):
    signed, i, s = f.numeric()
    v = int.from_bytes(b, "big", signed=signed)
    if f.usage != "COMP-5" and abs(v) >= 10 ** (i + s):
        st.undecodable[f.name] += 1  # exceeds PIC digits: TRUNC(STD) would never produce it
        return undecodable("BINARY", b)
    return canon(Decimal(v), s)


def decode_alpha(f, b, enc, st):
    if enc == "ascii":
        if any(x < 0x20 or x > 0x7E for x in b):
            st.undecodable[f.name] += 1
            return undecodable("ALPHANUMERIC", b)
        return b.decode("ascii")
    t = b.decode("cp037")
    if any(not ch.isprintable() for ch in t):
        st.undecodable[f.name] += 1
        return undecodable("ALPHANUMERIC", b)
    return t


def decode_elem(f, b, enc, st):
    if f.numeric() is None:
        return decode_alpha(f, b, enc, st)
    if f.usage == "COMP-3":
        return decode_packed(f, b, st)
    if f.usage in ("COMP", "COMP-5", "BINARY"):
        return decode_binary(f, b, st)
    return decode_display_numeric(f, b, enc, st)


def decode_group(g, rec, enc, st, branch_for=None):
    obj = {}
    for c in g.children:
        if c.redefines:
            continue  # alternate views are decoded only when selected as a discriminated branch
        key = c.name
        if branch_for and c.name in branch_for:
            sel = branch_for[c.name](obj)
            if sel is None:
                obj[key] = undecodable("UNKNOWN_BRANCH", rec[c.offset:c.offset + c.length])
            else:
                bf = next(x for x in g.children if x.name == sel)
                obj[sel] = decode_group(bf, rec, enc, st)
            continue
        if c.occurs > 1:
            arr = []
            for k in range(c.occurs):
                shift = k * c.length
                arr.append(decode_group(shifted(c, shift), rec, enc, st) if c.children
                           else decode_elem(c, rec[c.offset + shift:c.offset + shift + c.length], enc, st))
            obj[key] = arr
        elif c.children:
            obj.update(decode_group(c, rec, enc, st, branch_for))
        else:
            obj[key] = decode_elem(c, rec[c.offset:c.offset + c.length], enc, st)
    return obj


def shifted(f, shift):
    import copy
    g = copy.deepcopy(f)

    def mv(x):
        x.offset += shift
        for y in x.children:
            mv(y)
    mv(g)
    g.occurs = 1
    return g


def elementary(g, out=None):
    out = [] if out is None else out
    for c in g.children:
        if c.redefines:
            continue
        if c.children:
            elementary(c, out)
        else:
            out.append(c)
    return out


# --------------------------------------------------------------------------- readers
def read_ascii_records(path, lrecl):
    data = path.read_bytes()
    term = Counter()
    recs, padded = [], 0
    parts = data.split(b"\n")
    if parts and parts[-1] == b"":
        parts = parts[:-1]
        trailing_newline = True
    else:
        trailing_newline = False
    for idx, p in enumerate(parts):
        last = idx == len(parts) - 1
        if p.endswith(b"\r"):
            p = p[:-1]
            term["CRLF"] += 1
        elif last and not trailing_newline:
            term["NONE"] += 1
        else:
            term["LF"] += 1
        if len(p) > lrecl:
            raise ValueError(f"{path.name} record {idx} length {len(p)} > LRECL {lrecl}")
        if len(p) < lrecl:
            padded += 1
            p = p + b" " * (lrecl - len(p))
        recs.append(p)
    return recs, dict(sorted(term.items())), padded, data


def sha256(b):
    return hashlib.sha256(b).hexdigest()


def dumps(obj):
    return json.dumps(obj, indent=2, ensure_ascii=False) + "\n"


def key_of(rec, keys):
    return "|".join(str(rec[k]) for k in keys)


def raw_key(raw, layout_fields, keys):
    return b"".join(raw[f.offset:f.offset + f.length] for k in keys for f in layout_fields if f.name == k)


def summarize(records, raws, root, keys, enc):
    elem = elementary(root)
    totals = {}
    for f in elem:
        n = f.numeric()
        if n and f.name not in keys and (n[0] or n[2] > 0):
            vals = [r[f.name] for r in records if isinstance(r.get(f.name), str)]
            totals[f.name] = canon(sum((Decimal(v) for v in vals), Decimal(0)).scaleb(n[2]), n[2])
    rk = [raw_key(r, elem, keys) for r in raws]
    ks = [key_of(r, keys) for r in records]
    dup = len(ks) - len(set(ks))
    asc = [k.decode("latin-1") if enc == "ascii" else k.decode("cp037") for k in rk]
    ebc = [a.encode("cp037") for a in asc]
    return {
        "keyFields": keys,
        "keyMin": min(asc), "keyMax": max(asc),
        "keyUnique": dup == 0, "duplicateKeyCount": dup,
        "sortedAscendingAsciiCollation": [a.encode("latin-1") for a in asc] == sorted(a.encode("latin-1") for a in asc),
        "sortedAscendingEbcdicCollation": ebc == sorted(ebc),
        "controlTotals": totals,
    }


def build():
    manifest = {
        "schemaVersion": 1,
        "generator": "golden_data/tools/generate_golden.py",
        "conventions": {
            "fileShape": "JSON array, one object per record, keys in copybook order",
            "alphanumeric": "PIC X decoded verbatim, never trimmed (dates/timestamps stay raw strings)",
            "numeric": "canonical decimal string: optional '-', no leading zeros, exactly <scale> fraction digits",
            "groups": "non-OCCURS groups are flattened to their elementary fields",
            "occurs": "array with one element (value or object) per occurrence",
            "redefines": "discriminated REDEFINES decoded as a nested object named after the selected branch; "
                         "non-discriminated alternate views (e.g. EXPORT-TIMESTAMP-R) are omitted, primary kept",
            "filler": "FILLER kept verbatim under key FILLER so re-encoding is byte-exact",
            "undecodable": "{\"$type\": ZONED|PACKED|BINARY|ALPHANUMERIC|..._NEGATIVE_ZERO|UNKNOWN_BRANCH, \"hex\": source bytes}",
            "asciiZonedSign": "trailing overpunch: '{' A-I = +0..+9, '}' J-R = -0..-9",
            "ebcdicZonedSign": "zone nibble of last byte: C=+, D=-, F=unsigned",
            "shortRecords": "ASCII lines shorter than LRECL are right-padded with spaces (trailing blanks were trimmed in the seed)",
        },
        "datasets": {},
    }
    golden = {}
    roots = {}
    for ds, (cpy, rec, fname, ebc, keys) in DATASETS.items():
        root = parse_copybook(cpy, rec)
        roots[ds] = root
        raws, term, padded, data = read_ascii_records(ASCII_DIR / fname, root.length)
        st = Stats()
        records = [decode_group(root, r, "ascii", st) for r in raws]
        golden[ds] = records
        # EBCDIC counterpart: same layout, CP037; reported, not used as golden
        eb = (EBCDIC_DIR / ebc).read_bytes()
        est = Stats()
        erecs = [decode_group(root, eb[i:i + root.length], "ebcdic", est) for i in range(0, len(eb), root.length)]
        diverg = []
        for i, (a, e) in enumerate(zip(records, erecs)):
            fields = [k for k in a if a[k] != e.get(k)]
            if fields:
                diverg.append({"recordIndex": i, "key": key_of(a, keys), "fields": fields,
                               "ascii": {k: a[k] for k in fields}, "ebcdic": {k: e[k] for k in fields}})
        manifest["datasets"][ds] = {
            "goldenFile": f"golden_data/{ds}.json",
            "sourceFile": f"app/data/ASCII/{fname}",
            "sourceEncoding": "ASCII",
            "sourceSha256": sha256(data),
            "sourceBytes": len(data),
            "lineTerminators": term,
            "copybook": f"app/cpy/{cpy}.cpy",
            "recordName": rec,
            "recordLength": root.length,
            "recordCount": len(records),
            "paddedRecordCount": padded,
            "undecodableFieldCount": sum(st.undecodable.values()),
            "nonPreferredSignCount": sum(st.non_preferred_sign.values()),
            **summarize(records, raws, root, keys, "ascii"),
            "ebcdicCounterpart": {
                "file": f"app/data/EBCDIC/{ebc}",
                "sha256": sha256(eb),
                "recordCount": len(erecs),
                "trailingBytes": len(eb) % root.length,
                "divergentRecords": diverg,
            },
            "layout": layout_rows(root, []),
        }

    # export file (EBCDIC only)
    cpy, rec, fname, keys = EXPORT
    root = parse_copybook(cpy, rec)
    eb = (EBCDIC_DIR / fname).read_bytes()
    if len(eb) % root.length:
        raise ValueError("export file is not a whole number of records")
    raws = [eb[i:i + root.length] for i in range(0, len(eb), root.length)]
    st = Stats()
    branch_for = {"EXPORT-RECORD-DATA": lambda o: EXPORT_BRANCHES.get(o.get(EXPORT_DISCRIMINATOR))}
    records = [decode_group(root, r, "ebcdic", st, branch_for) for r in raws]
    golden["export"] = records
    by_type = Counter(r[EXPORT_DISCRIMINATOR] for r in records)
    totals = {}
    for code, branch in sorted(EXPORT_BRANCHES.items()):
        bf = next(x for x in root.children if x.name == branch)
        for f in elementary(bf):
            n = f.numeric()
            if n and (n[0] or n[2] > 0):
                vals = [r[branch][f.name] for r in records if branch in r and isinstance(r[branch][f.name], str)]
                totals[f.name] = canon(sum((Decimal(v) for v in vals), Decimal(0)).scaleb(n[2]), n[2])
    seq = [int(r["EXPORT-SEQUENCE-NUM"]) for r in records]
    manifest["datasets"]["export"] = {
        "goldenFile": "golden_data/export.json",
        "sourceFile": f"app/data/EBCDIC/{fname}",
        "sourceEncoding": "EBCDIC-CP037",
        "sourceSha256": sha256(eb),
        "sourceBytes": len(eb),
        "copybook": f"app/cpy/{cpy}.cpy",
        "recordName": rec,
        "recordLength": root.length,
        "recordCount": len(records),
        "recordCountByType": dict(sorted(by_type.items())),
        "discriminator": {"field": EXPORT_DISCRIMINATOR, "branches": EXPORT_BRANCHES,
                          "source": "app/cbl/CBEXPORT.cbl (MOVE 'x' TO EXPORT-REC-TYPE), app/cbl/CBIMPORT.cbl EVALUATE"},
        "undecodableFieldCount": sum(st.undecodable.values()),
        "nonPreferredSignCount": sum(st.non_preferred_sign.values()),
        "keyFields": keys,
        "keyMin": str(min(seq)), "keyMax": str(max(seq)),
        "keyUnique": len(seq) == len(set(seq)), "duplicateKeyCount": len(seq) - len(set(seq)),
        "sequenceGaps": max(seq) - min(seq) + 1 - len(set(seq)),
        "controlTotals": totals,
        "layout": layout_rows(root, []),
    }
    manifest["referentialIntegrity"] = referential(golden)
    return golden, manifest


def referential(g):
    def ids(ds, *fields):
        return {tuple(r[f] for f in fields) for r in g[ds]}

    checks = [
        ("cardxref.XREF-ACCT-ID -> acctdata.ACCT-ID", "cardxref", ["XREF-ACCT-ID"], "acctdata", ["ACCT-ID"]),
        ("cardxref.XREF-CUST-ID -> custdata.CUST-ID", "cardxref", ["XREF-CUST-ID"], "custdata", ["CUST-ID"]),
        ("cardxref.XREF-CARD-NUM -> carddata.CARD-NUM", "cardxref", ["XREF-CARD-NUM"], "carddata", ["CARD-NUM"]),
        ("carddata.CARD-NUM -> cardxref.XREF-CARD-NUM", "carddata", ["CARD-NUM"], "cardxref", ["XREF-CARD-NUM"]),
        ("carddata.CARD-ACCT-ID -> acctdata.ACCT-ID", "carddata", ["CARD-ACCT-ID"], "acctdata", ["ACCT-ID"]),
        ("dailytran.DALYTRAN-CARD-NUM -> cardxref.XREF-CARD-NUM", "dailytran", ["DALYTRAN-CARD-NUM"],
         "cardxref", ["XREF-CARD-NUM"]),
        ("dailytran.DALYTRAN-TYPE-CD -> trantype.TRAN-TYPE", "dailytran", ["DALYTRAN-TYPE-CD"], "trantype", ["TRAN-TYPE"]),
        ("dailytran.(TYPE-CD,CAT-CD) -> trancatg", "dailytran", ["DALYTRAN-TYPE-CD", "DALYTRAN-CAT-CD"],
         "trancatg", ["TRAN-TYPE-CD", "TRAN-CAT-CD"]),
        ("trancatg.TRAN-TYPE-CD -> trantype.TRAN-TYPE", "trancatg", ["TRAN-TYPE-CD"], "trantype", ["TRAN-TYPE"]),
        ("tcatbal.TRANCAT-ACCT-ID -> acctdata.ACCT-ID", "tcatbal", ["TRANCAT-ACCT-ID"], "acctdata", ["ACCT-ID"]),
        ("tcatbal.(TYPE-CD,CD) -> trancatg", "tcatbal", ["TRANCAT-TYPE-CD", "TRANCAT-CD"],
         "trancatg", ["TRAN-TYPE-CD", "TRAN-CAT-CD"]),
        ("discgrp.(TYPE-CD,CAT-CD) -> trancatg", "discgrp", ["DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"],
         "trancatg", ["TRAN-TYPE-CD", "TRAN-CAT-CD"]),
        ("acctdata.ACCT-GROUP-ID -> discgrp.DIS-ACCT-GROUP-ID", "acctdata", ["ACCT-GROUP-ID"],
         "discgrp", ["DIS-ACCT-GROUP-ID"]),
        ("acctdata.ACCT-ADDR-ZIP -> discgrp.DIS-ACCT-GROUP-ID (seed-data quirk probe)", "acctdata", ["ACCT-ADDR-ZIP"],
         "discgrp", ["DIS-ACCT-GROUP-ID"]),
        ("acctdata.ACCT-ID -> cardxref.XREF-ACCT-ID", "acctdata", ["ACCT-ID"], "cardxref", ["XREF-ACCT-ID"]),
        ("custdata.CUST-ID -> cardxref.XREF-CUST-ID", "custdata", ["CUST-ID"], "cardxref", ["XREF-CUST-ID"]),
    ]
    out = []
    for name, cds, cf, pds, pf in checks:
        parent = ids(pds, *pf)
        child = [tuple(r[f] for f in cf) for r in g[cds]]
        orphans = sorted({c for c in child if c not in parent})
        out.append({"check": name, "childRecords": len(child), "orphanRecords": sum(1 for c in child if c not in parent),
                    "distinctOrphanKeys": ["|".join(o) for o in orphans]})
    return out


def main():
    golden, manifest = build()
    files = {f"{ds}.json": dumps(recs) for ds, recs in golden.items()}
    for ds, recs in golden.items():
        manifest["datasets"][ds]["goldenSha256"] = sha256(files[f"{ds}.json"].encode("utf-8"))
    files["manifest.json"] = dumps(manifest)
    check = "--check" in sys.argv
    stale = []
    for name, content in files.items():
        p = OUT / name
        if check:
            if not p.exists() or p.read_text(encoding="utf-8") != content:
                stale.append(name)
        else:
            p.write_text(content, encoding="utf-8")
    if stale:
        print("golden_data out of date:", ", ".join(stale))
        sys.exit(1)
    print(("verified " if check else "wrote ") + ", ".join(sorted(files)))


if __name__ == "__main__":
    main()
