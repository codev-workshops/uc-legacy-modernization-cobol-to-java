"""Registry of CardDemo ASCII sample datasets and their copybook layouts."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = REPO_ROOT / "app" / "data" / "ASCII"
EBCDIC_DIR = REPO_ROOT / "app" / "data" / "EBCDIC"
CPY_DIR = REPO_ROOT / "app" / "cpy"
GOLDEN_DIR = REPO_ROOT / "golden-files"


@dataclass(frozen=True)
class Dataset:
    name: str
    file: str
    copybook: str
    key: tuple[str, ...]
    record_length: int
    vsam: str
    money_fields: tuple[str, ...] = ()


# EBCDIC (RECFM=FB, code page 037) twin of each ASCII sample file, as unloaded from the mainframe.
EBCDIC_FILES: dict[str, str] = {
    "account": "AWS.M2.CARDDEMO.ACCTDATA.PS",
    "card": "AWS.M2.CARDDEMO.CARDDATA.PS",
    "card_xref": "AWS.M2.CARDDEMO.CARDXREF.PS",
    "customer": "AWS.M2.CARDDEMO.CUSTDATA.PS",
    "daily_transaction": "AWS.M2.CARDDEMO.DALYTRAN.PS",
    "disclosure_group": "AWS.M2.CARDDEMO.DISCGRP.PS",
    "category_balance": "AWS.M2.CARDDEMO.TCATBALF.PS",
    "transaction_category": "AWS.M2.CARDDEMO.TRANCATG.PS",
    "transaction_type": "AWS.M2.CARDDEMO.TRANTYPE.PS",
}

# Records whose EBCDIC unload decodes differently from the ASCII sample (data differences, not codec defects):
# account 49 has ACCT-ADDR-ZIP 'ZEROAPR' vs 'A000000000'; DEFAULT/07/0001 has DIS-INT-RATE 15.00 vs 0.00.
KNOWN_EBCDIC_DIFFS: dict[str, list[str]] = {
    "account": ["49"],
    "disclosure_group": ["DEFAULT|07|1"],
}

DATASETS: dict[str, Dataset] = {
    d.name: d
    for d in [
        Dataset(
            "account",
            "acctdata.txt",
            "CVACT01Y.cpy",
            ("ACCT-ID",),
            300,
            "AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS",
            ("ACCT-CURR-BAL", "ACCT-CREDIT-LIMIT", "ACCT-CASH-CREDIT-LIMIT", "ACCT-CURR-CYC-CREDIT", "ACCT-CURR-CYC-DEBIT"),
        ),
        Dataset("card", "carddata.txt", "CVACT02Y.cpy", ("CARD-NUM",), 150, "AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS"),
        Dataset("card_xref", "cardxref.txt", "CVACT03Y.cpy", ("XREF-CARD-NUM",), 50, "AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS"),
        Dataset("customer", "custdata.txt", "CVCUS01Y.cpy", ("CUST-ID",), 500, "AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS"),
        Dataset(
            "daily_transaction",
            "dailytran.txt",
            "CVTRA06Y.cpy",
            ("DALYTRAN-ID",),
            350,
            "AWS.M2.CARDDEMO.DALYTRAN.PS",
            ("DALYTRAN-AMT",),
        ),
        Dataset(
            "disclosure_group",
            "discgrp.txt",
            "CVTRA02Y.cpy",
            ("DIS-ACCT-GROUP-ID", "DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"),
            50,
            "AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS",
            ("DIS-INT-RATE",),
        ),
        Dataset(
            "category_balance",
            "tcatbal.txt",
            "CVTRA01Y.cpy",
            ("TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"),
            50,
            "AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS",
            ("TRAN-CAT-BAL",),
        ),
        Dataset(
            "transaction_category",
            "trancatg.txt",
            "CVTRA04Y.cpy",
            ("TRAN-TYPE-CD", "TRAN-CAT-CD"),
            60,
            "AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS",
        ),
        Dataset("transaction_type", "trantype.txt", "CVTRA03Y.cpy", ("TRAN-TYPE",), 60, "AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS"),
    ]
}
