#!/bin/bash
# compile_all.sh — Compile all CardDemo batch COBOL programs with GnuCOBOL.
#
# Prerequisites:
#   - GnuCOBOL 3.2+ with BDB indexed file support (cobc)
#   - C stubs MVSWAIT.so and COBDATFT.so installed in /usr/local/lib/gnucobol/
#
# Usage:
#   cd <repo-root>
#   ./scripts/compile_all.sh
#
# Output: compiled executables and modules in build/bin/

set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC_DIR="$REPO_ROOT/app/cbl"
CPY_DIR="$REPO_ROOT/app/cpy"
BUILD_DIR="$REPO_ROOT/build/bin"

mkdir -p "$BUILD_DIR"

COBC="/usr/local/bin/cobc"
COBC_FLAGS="-I $CPY_DIR -ftab-width=1"

PASS=0
FAIL=0

compile_exe() {
    local src="$1"
    local name
    name="$(basename "$src" .cbl)"
    name="$(basename "$name" .CBL)"
    echo -n "  Compiling $name (executable)... "
    if $COBC -x $COBC_FLAGS -o "$BUILD_DIR/$name" "$src" 2>/tmp/cobc_err_$$; then
        echo "OK"
        PASS=$((PASS + 1))
    else
        echo "FAILED"
        cat /tmp/cobc_err_$$
        FAIL=$((FAIL + 1))
    fi
    rm -f /tmp/cobc_err_$$
}

compile_mod() {
    local src="$1"
    local name
    name="$(basename "$src" .cbl)"
    name="$(basename "$name" .CBL)"
    echo -n "  Compiling $name (module)... "
    if $COBC -m $COBC_FLAGS -o "$BUILD_DIR/$name.so" "$src" 2>/tmp/cobc_err_$$; then
        echo "OK"
        PASS=$((PASS + 1))
    else
        echo "FAILED"
        cat /tmp/cobc_err_$$
        FAIL=$((FAIL + 1))
    fi
    rm -f /tmp/cobc_err_$$
}

echo "=== CardDemo Batch Compilation ==="
echo "Source:  $SRC_DIR"
echo "Output:  $BUILD_DIR"
echo ""

echo "--- Batch Executables ---"
for f in CBACT01C.cbl CBACT02C.cbl CBACT03C.cbl CBCUS01C.cbl \
         CBTRN01C.cbl CBTRN02C.cbl CBTRN03C.cbl \
         CBSTM03A.CBL COBSWAIT.cbl; do
    compile_exe "$SRC_DIR/$f"
done

echo ""
echo "--- Batch Modules (subprograms) ---"
for f in CBACT04C.cbl CBSTM03B.CBL CSUTLDTC.cbl; do
    compile_mod "$SRC_DIR/$f"
done

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
