#!/bin/bash
# run_batch.sh — Run CardDemo batch programs locally using GnuCOBOL.
#
# This script replicates the mainframe JCL batch job sequence using
# GnuCOBOL-compiled executables and BDB-backed VSAM files.
#
# Prerequisites:
#   1. Run scripts/compile_all.sh  (compile batch programs)
#   2. Run scripts/load_data.sh    (load ASCII data into BDB indexed files)
#
# Usage:
#   cd <repo-root>
#   ./scripts/run_batch.sh [job_name]
#
# Run without arguments to execute the full batch sequence.
# Run with a job name to execute a single job (e.g., ./scripts/run_batch.sh CBACT01C)

set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BIN_DIR="$REPO_ROOT/build/bin"
DATA_DIR="$REPO_ROOT/build/data"
OUTPUT_DIR="$REPO_ROOT/build/output"

mkdir -p "$OUTPUT_DIR"

# GnuCOBOL library path for C stubs and subprograms
export COB_LIBRARY_PATH="/usr/local/lib/gnucobol:$BIN_DIR"

PASS=0
FAIL=0

run_job() {
    local name="$1"
    local desc="$2"
    shift 2
    echo "--- Job: $name — $desc ---"

    # Set environment variables passed as key=value pairs
    while [ $# -gt 0 ]; do
        export "$1"
        shift
    done

    if "$BIN_DIR/$name" 2>&1 | tail -5; then
        echo "  -> $name completed (exit $?)"
        PASS=$((PASS + 1))
    else
        echo "  -> $name FAILED (exit $?)"
        FAIL=$((FAIL + 1))
    fi
    echo ""
}

run_single_job() {
    case "$1" in
        CBACT01C) run_cbact01c ;;
        CBACT02C) run_cbact02c ;;
        CBACT03C) run_cbact03c ;;
        CBCUS01C) run_cbcus01c ;;
        CBTRN01C) run_cbtrn01c ;;
        CBTRN02C) run_cbtrn02c ;;
        CBTRN03C) run_cbtrn03c ;;
        *)
            echo "Unknown job: $1"
            echo "Available: CBACT01C CBACT02C CBACT03C CBCUS01C CBTRN01C CBTRN02C CBTRN03C"
            exit 1
            ;;
    esac
}

# --- Individual job definitions ---
# Each maps the JCL DD names to local file paths via environment variables

run_cbact01c() {
    run_job CBACT01C "Read Account File and write outputs" \
        "ACCTFILE=$DATA_DIR/ACCTDATA" \
        "OUTFILE=$OUTPUT_DIR/CBACT01C_OUT.txt" \
        "ARRYFILE=$OUTPUT_DIR/CBACT01C_ARRY.txt" \
        "VBRCFILE=$OUTPUT_DIR/CBACT01C_VBRC.txt"
}

run_cbact02c() {
    run_job CBACT02C "Read Card Data file" \
        "CARDFILE=$DATA_DIR/CARDDATA"
}

run_cbact03c() {
    run_job CBACT03C "Read Card Cross-Reference file" \
        "XREFFILE=$DATA_DIR/CARDXREF"
}

run_cbcus01c() {
    run_job CBCUS01C "Read Customer Data file" \
        "CUSTFILE=$DATA_DIR/CUSTDATA"
}

run_cbtrn01c() {
    # CBTRN01C validates daily transactions against customer, card, account data
    run_job CBTRN01C "Validate daily transactions" \
        "DALYTRAN=$DATA_DIR/DALYTRAN" \
        "CUSTFILE=$DATA_DIR/CUSTDATA" \
        "XREFFILE=$DATA_DIR/CARDXREF" \
        "CARDFILE=$DATA_DIR/CARDDATA" \
        "ACCTFILE=$DATA_DIR/ACCTDATA" \
        "TRANFILE=$DATA_DIR/TRANSACT"
}

run_cbtrn02c() {
    # CBTRN02C processes daily transactions, updates account and transaction master
    run_job CBTRN02C "Process daily transactions (posting)" \
        "DALYTRAN=$DATA_DIR/DALYTRAN" \
        "TRANFILE=$DATA_DIR/TRANSACT" \
        "XREFFILE=$DATA_DIR/CARDXREF" \
        "DALYREJS=$OUTPUT_DIR/DALYREJS.txt" \
        "ACCTFILE=$DATA_DIR/ACCTDATA" \
        "TCATBALF=$DATA_DIR/TCATBALF"
}

run_cbtrn03c() {
    # CBTRN03C generates transaction report — needs a DATEPARM file with date range
    # Create default DATEPARM if it doesn't exist (wide date range)
    if [ ! -f "$DATA_DIR/DATEPARM.txt" ]; then
        echo "2020-01-01 2030-12-31" > "$DATA_DIR/DATEPARM.txt"
    fi
    run_job CBTRN03C "Generate transaction report" \
        "TRANFILE=$DATA_DIR/TRANSACT" \
        "CARDXREF=$DATA_DIR/CARDXREF" \
        "TRANTYPE=$DATA_DIR/TRANTYPE" \
        "TRANCATG=$DATA_DIR/TRANCATG" \
        "TRANREPT=$OUTPUT_DIR/TRANREPT.txt" \
        "DATEPARM=$DATA_DIR/DATEPARM.txt"
}

echo "=== CardDemo Local Batch Runner ==="
echo "Programs: $BIN_DIR"
echo "Data:     $DATA_DIR"
echo "Output:   $OUTPUT_DIR"
echo ""

if [ -n "$1" ]; then
    run_single_job "$1"
else
    echo "Running full batch sequence..."
    echo ""

    # Phase 1: Data reading/validation jobs
    echo "== Phase 1: Data reading =="
    run_cbact01c
    run_cbact02c
    run_cbact03c
    run_cbcus01c

    # Phase 2: Transaction processing (core batch workflow)
    echo "== Phase 2: Transaction processing =="
    run_cbtrn02c

    # Phase 3: Reporting
    echo "== Phase 3: Reporting =="
    run_cbtrn03c
fi

echo "=== Batch complete: $PASS passed, $FAIL failed ==="
