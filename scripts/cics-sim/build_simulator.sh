#!/bin/bash
# build_simulator.sh - Build the CICS Simulator for CardDemo
#
# This script:
#   1. Processes BMS maps into COBOL copybooks and C map definitions
#   2. Compiles the CICS runtime library (libcicssim.so)
#   3. Installs the library and copybooks
#
# Prerequisites:
#   - GCC, libdb-dev
#   - Python 3
#   - GnuCOBOL 3.2+ (for compiling the online programs afterwards)
#
# Usage:
#   cd <repo-root>
#   ./scripts/cics-sim/build_simulator.sh

set -e

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SIM_DIR="$REPO_ROOT/scripts/cics-sim"
RUNTIME_DIR="$SIM_DIR/runtime"
BMS_DIR="$REPO_ROOT/app/bms"
CPY_DIR="$REPO_ROOT/app/cpy"
BUILD_DIR="$REPO_ROOT/build/cics-sim"
INSTALL_DIR="/usr/local/lib/gnucobol"
GEN_CPY_DIR="$BUILD_DIR/copybooks"

echo "=== CICS Simulator Build ==="
echo "Repo:    $REPO_ROOT"
echo "Output:  $BUILD_DIR"
echo ""

# Step 1: Process BMS maps
echo "--- Step 1: Processing BMS Maps ---"
mkdir -p "$GEN_CPY_DIR"
python3 "$SIM_DIR/bms2cpy.py" "$BMS_DIR" "$GEN_CPY_DIR" \
    --c-header "$RUNTIME_DIR/cics_maps_generated.c"
echo ""

# Step 2: Copy DFHAID and DFHBMSCA copybooks
echo "--- Step 2: Installing Copybooks ---"
cp "$SIM_DIR/copybooks/DFHAID.cpy" "$GEN_CPY_DIR/"
cp "$SIM_DIR/copybooks/DFHBMSCA.cpy" "$GEN_CPY_DIR/"
echo "  Installed DFHAID.cpy"
echo "  Installed DFHBMSCA.cpy"
echo ""

# Step 3: Build runtime library
echo "--- Step 3: Building Runtime Library ---"
cd "$RUNTIME_DIR"
make clean 2>/dev/null || true
make maps
echo ""

# Step 4: Install
echo "--- Step 4: Installing ---"
sudo cp "$RUNTIME_DIR/libcicssim.so" "$INSTALL_DIR/"
sudo ldconfig

# Copy generated copybooks alongside application copybooks
cp "$GEN_CPY_DIR"/*.cpy "$CPY_DIR/" 2>/dev/null || true
echo "  Installed libcicssim.so to $INSTALL_DIR/"
echo "  Copied generated copybooks to $CPY_DIR/"
echo ""

echo "=== CICS Simulator Build Complete ==="
echo ""
echo "Next steps:"
echo "  ./scripts/cics-sim/compile_online.sh   - Compile online programs"
echo "  ./scripts/cics-sim/run_online.sh        - Run CardDemo interactively"
