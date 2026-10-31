import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from carddemo_harness.jobs import golden_records  # noqa: E402


@pytest.fixture(scope="session")
def golden():
    return golden_records()


def acct(acct_id=1, bal="100.00", limit="1000.00", credit="0.00", debit="0.00", exp="2030-12-31", group="A1"):
    return {
        "ACCT-ID": acct_id,
        "ACCT-ACTIVE-STATUS": "Y",
        "ACCT-CURR-BAL": bal,
        "ACCT-CREDIT-LIMIT": limit,
        "ACCT-CASH-CREDIT-LIMIT": "0.00",
        "ACCT-OPEN-DATE": "2020-01-01",
        "ACCT-EXPIRAION-DATE": exp,
        "ACCT-REISSUE-DATE": exp,
        "ACCT-CURR-CYC-CREDIT": credit,
        "ACCT-CURR-CYC-DEBIT": debit,
        "ACCT-ADDR-ZIP": "",
        "ACCT-GROUP-ID": group,
    }


def tran(tid="T1", card="C1", amt="10.00", ts="2024-01-01-10.00.00.000000", type_cd="01", cat=1):
    return {
        "DALYTRAN-ID": tid,
        "DALYTRAN-TYPE-CD": type_cd,
        "DALYTRAN-CAT-CD": cat,
        "DALYTRAN-SOURCE": "POS TERM",
        "DALYTRAN-DESC": "d",
        "DALYTRAN-AMT": amt,
        "DALYTRAN-MERCHANT-ID": 1,
        "DALYTRAN-MERCHANT-NAME": "m",
        "DALYTRAN-MERCHANT-CITY": "c",
        "DALYTRAN-MERCHANT-ZIP": "z",
        "DALYTRAN-CARD-NUM": card,
        "DALYTRAN-ORIG-TS": ts,
        "DALYTRAN-PROC-TS": "",
    }


def xref(card="C1", acct_id=1, cust=1):
    return {"XREF-CARD-NUM": card, "XREF-CUST-ID": cust, "XREF-ACCT-ID": acct_id}


def tcat(acct_id=1, type_cd="01", cat=1, bal="0.00"):
    return {"TRANCAT-ACCT-ID": acct_id, "TRANCAT-TYPE-CD": type_cd, "TRANCAT-CD": cat, "TRAN-CAT-BAL": bal}


def disc(group="A1", type_cd="01", cat=1, rate="12.00"):
    return {"DIS-ACCT-GROUP-ID": group, "DIS-TRAN-TYPE-CD": type_cd, "DIS-TRAN-CAT-CD": cat, "DIS-INT-RATE": rate}
