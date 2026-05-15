#!/bin/bash
# build_stubs.sh — Compile and install Linux C stubs for z/OS assembler routines.
#
# These stubs replace z/390 assembler and z/OS Language Environment routines
# so that CardDemo batch programs can run locally with GnuCOBOL.
#
# Stubs:
#   MVSWAIT  — Timer (centisecond sleep)
#   COBDATFT — Date format conversion (YYYYMMDD ↔ YYYY-MM-DD)
#   CEE3ABD  — Abnormal termination (prints abend code and exits)
#   CEEDAYS  — Date-to-Lilian conversion
#
# Usage:
#   cd <repo-root>
#   ./scripts/linux-stubs/build_stubs.sh

set -e

STUB_DIR="$(cd "$(dirname "$0")" && pwd)"
INSTALL_DIR="/usr/local/lib/gnucobol"

echo "=== Building z/OS Replacement Stubs ==="

for src in "$STUB_DIR"/*.c; do
    name="$(basename "$src" .c)"
    echo -n "  Compiling $name... "
    gcc -shared -fPIC -o "$STUB_DIR/$name.so" "$src"
    echo "OK"
done

echo ""
echo "Installing to $INSTALL_DIR..."
sudo cp "$STUB_DIR"/*.so "$INSTALL_DIR/"
echo "Done."
echo ""
echo "Installed stubs:"
ls -la "$INSTALL_DIR"/*.so
