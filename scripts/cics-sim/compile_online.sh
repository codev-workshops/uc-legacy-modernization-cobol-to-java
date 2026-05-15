#!/bin/bash
# compile_online.sh - Preprocess and compile CardDemo online CICS programs
#
# This script:
#   1. Preprocesses each online program (EXEC CICS -> CALL statements)
#   2. Compiles the preprocessed source with GnuCOBOL
#   3. Links against the CICS simulator runtime library
#
# Prerequisites:
#   - Run build_simulator.sh first to build libcicssim.so
#   - GnuCOBOL 3.2+ installed at /usr/local/bin/cobc
#
# Usage:
#   cd <repo-root>
#   ./scripts/cics-sim/compile_online.sh

set -e

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SIM_DIR="$REPO_ROOT/scripts/cics-sim"
SRC_DIR="$REPO_ROOT/app/cbl"
CPY_DIR="$REPO_ROOT/app/cpy"
BUILD_DIR="$REPO_ROOT/build/online"
PREPROC_DIR="$BUILD_DIR/preprocessed"
GEN_CPY_DIR="$REPO_ROOT/build/cics-sim/copybooks"

COBC="/usr/local/bin/cobc"
COBC_FLAGS="-I $CPY_DIR -I $GEN_CPY_DIR -ftab-width=1 -flarger-redefines=ok -frelax-syntax-checks"
LINK_FLAGS="-L /usr/local/lib/gnucobol -lcicssim -ldb"

mkdir -p "$BUILD_DIR" "$PREPROC_DIR"

PASS=0
FAIL=0

# Online CICS programs (CO*.cbl)
ONLINE_PROGRAMS=(
    COSGN00C
    COMEN01C
    COADM01C
    COACTUPC
    COACTVWC
    COBIL00C
    COCRDLIC
    COCRDSLC
    COCRDUPC
    CORPT00C
    COTRN00C
    COTRN01C
    COTRN02C
    COUSR00C
    COUSR01C
    COUSR02C
    COUSR03C
)

echo "=== CardDemo Online Program Compilation ==="
echo "Source:       $SRC_DIR"
echo "Preprocessed: $PREPROC_DIR"
echo "Output:       $BUILD_DIR"
echo ""

echo "--- Preprocessing ---"
for pgm in "${ONLINE_PROGRAMS[@]}"; do
    src="$SRC_DIR/${pgm}.cbl"
    if [ ! -f "$src" ]; then
        echo "  SKIP $pgm (source not found)"
        continue
    fi

    echo -n "  Preprocessing $pgm... "
    if python3 "$SIM_DIR/preprocess.py" "$src" "$PREPROC_DIR/${pgm}.cbl" \
        --copydir "$CPY_DIR" 2>/tmp/preproc_err_$$; then
        echo "OK"
    else
        echo "FAILED"
        cat /tmp/preproc_err_$$ 2>/dev/null
        FAIL=$((FAIL + 1))
    fi
    rm -f /tmp/preproc_err_$$
done

echo ""
echo "--- Compiling ---"
for pgm in "${ONLINE_PROGRAMS[@]}"; do
    pp_src="$PREPROC_DIR/${pgm}.cbl"
    if [ ! -f "$pp_src" ]; then
        continue
    fi

    echo -n "  Compiling $pgm... "
    if $COBC -x $COBC_FLAGS -o "$BUILD_DIR/$pgm" "$pp_src" $LINK_FLAGS \
        2>/tmp/cobc_err_$$; then
        echo "OK"
        PASS=$((PASS + 1))
    else
        echo "FAILED"
        cat /tmp/cobc_err_$$ 2>/dev/null
        FAIL=$((FAIL + 1))
    fi
    rm -f /tmp/cobc_err_$$
done

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
    echo ""
    echo "Note: Some failures may be due to missing copybook references"
    echo "or CICS commands not yet supported by the simulator."
    exit 1
fi
