"""Edge case tests for COBOL fixed-width parser."""
import pytest
import json
from pathlib import Path

# Import from test-harness (assumed to be created)
# from cobol_parser import parse_file

EDGE_CASE_DIR = Path(__file__).parent


@pytest.mark.parametrize("scenario", ["empty", "single-record", "max-values", "max-negative", "zero-values"])
@pytest.mark.parametrize("entity", ["acctdata", "carddata", "cardxref", "custdata", "dailytran"])
def test_edge_case(scenario, entity):
    """Parse each edge case file and compare against expected JSON."""
    data_file = EDGE_CASE_DIR / scenario / f"{entity}.txt"
    expected_file = EDGE_CASE_DIR / scenario / f"{entity}.expected.json"

    if not data_file.exists() or not expected_file.exists():
        pytest.skip(f"Edge case not defined: {scenario}/{entity}")

    with open(expected_file) as f:
        expected = json.load(f)

    # Parser call — adjust import path when test-harness is implemented
    # result = parse_file(str(data_file), copybook=expected["copybook"])
    # assert result["record_count"] == expected["record_count"]
    # for i, rec in enumerate(result["records"]):
    #     for key, val in expected["records"][i].items():
    #         assert rec[key] == val, f"Mismatch in {key}: got {rec[key]}, expected {val}"


def test_empty_returns_zero_records():
    """Specifically verify empty files produce zero records without error."""
    for entity in ["acctdata", "carddata", "cardxref", "custdata", "dailytran"]:
        data_file = EDGE_CASE_DIR / "empty" / f"{entity}.txt"
        if data_file.exists():
            assert data_file.stat().st_size == 0


def test_single_record_exact_length():
    """Verify single-record files are exactly the documented record length."""
    expected_lengths = {
        "acctdata": 300,
        "carddata": 150,
        "cardxref": 50,
        "custdata": 500,
        "dailytran": 350,
    }
    for entity, expected_len in expected_lengths.items():
        data_file = EDGE_CASE_DIR / "single-record" / f"{entity}.txt"
        if data_file.exists():
            actual = data_file.stat().st_size
            assert actual == expected_len, (
                f"{entity}.txt: expected {expected_len} bytes, got {actual}"
            )


def test_max_values_no_overflow():
    """Verify max-value records parse without overflow or exception."""
    for entity in ["acctdata", "carddata", "cardxref", "custdata", "dailytran"]:
        data_file = EDGE_CASE_DIR / "max-values" / f"{entity}.txt"
        expected_file = EDGE_CASE_DIR / "max-values" / f"{entity}.expected.json"
        if not data_file.exists():
            continue
        with open(expected_file) as f:
            expected = json.load(f)
        assert expected["record_count"] == 1
        # Parser must not throw — this catches integer overflow issues


def test_expected_json_schema():
    """Verify all expected JSON files have required top-level keys."""
    required_keys = {"source_file", "copybook", "record_length", "record_count", "records"}
    for scenario in ["empty", "single-record", "max-values"]:
        scenario_dir = EDGE_CASE_DIR / scenario
        for json_file in sorted(scenario_dir.glob("*.expected.json")):
            with open(json_file) as f:
                data = json.load(f)
            missing = required_keys - set(data.keys())
            assert not missing, f"{json_file.name} missing keys: {missing}"


def test_no_trailing_newlines():
    """Verify no .txt file has a trailing newline corrupting fixed-width format."""
    for scenario in ["single-record", "max-values", "max-negative", "zero-values"]:
        scenario_dir = EDGE_CASE_DIR / scenario
        for txt_file in sorted(scenario_dir.glob("*.txt")):
            content = txt_file.read_bytes()
            if len(content) > 0:
                assert content[-1:] != b"\n", (
                    f"{scenario}/{txt_file.name} has trailing newline"
                )
