"""Generate and load structured JSON golden references for the ASCII sample datasets."""

from __future__ import annotations

import hashlib
import json
from decimal import Decimal
from pathlib import Path

from .copybook import elementary_fields
from .datasets import DATA_DIR, DATASETS, GOLDEN_DIR, Dataset
from .records import load_layout, parse_dataset


def record_key(rec: dict, ds: Dataset) -> str:
    return "|".join(str(rec[k]) for k in ds.key)


def control_totals(records: list[dict], ds: Dataset) -> dict:
    totals = {f: str(sum((Decimal(r[f]) for r in records if r.get(f) is not None), Decimal("0"))) for f in ds.money_fields}
    return {"record_count": len(records), "sums": totals}


def build_golden(ds: Dataset) -> dict:
    res = parse_dataset(ds)
    layout = load_layout(ds)
    src = (DATA_DIR / ds.file).read_bytes()
    recs = sorted(res.records, key=lambda r: record_key(r, ds))
    return {
        "dataset": ds.name,
        "source_file": f"app/data/ASCII/{ds.file}",
        "source_sha256": hashlib.sha256(src).hexdigest(),
        "copybook": f"app/cpy/{ds.copybook}",
        "vsam_dataset": ds.vsam,
        "record_length": ds.record_length,
        "key": list(ds.key),
        "layout": [
            {
                "name": f.name,
                "offset": f.offset,
                "length": f.length,
                "pic": f.pic,
                "usage": f.usage or "DISPLAY",
                "kind": f.kind,
                "scale": f.scale,
                "signed": f.signed,
            }
            for f in elementary_fields(layout)
            if f.name != "FILLER"
        ],
        "control_totals": control_totals(recs, ds),
        "parse_issues": [vars(i) for i in res.issues],
        "records": recs,
    }


def write_all(out_dir: Path = GOLDEN_DIR) -> dict[str, Path]:
    out_dir.mkdir(parents=True, exist_ok=True)
    written = {}
    manifest = {}
    for name, ds in DATASETS.items():
        g = build_golden(ds)
        p = out_dir / f"{name}.json"
        p.write_text(json.dumps(g, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        written[name] = p
        manifest[name] = {
            "file": p.name,
            "records": g["control_totals"]["record_count"],
            "source_sha256": g["source_sha256"],
            "sums": g["control_totals"]["sums"],
            "parse_issues": len(g["parse_issues"]),
        }
    (out_dir / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    return written


def load_golden(name: str, golden_dir: Path = GOLDEN_DIR) -> dict:
    return json.loads((golden_dir / f"{name}.json").read_text(encoding="utf-8"))
