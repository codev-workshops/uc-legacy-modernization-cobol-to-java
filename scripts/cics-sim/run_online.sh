#!/bin/bash
# run_online.sh - Run the CardDemo application interactively
#
# Starts the CardDemo sign-on screen (COSGN00C) and manages the
# CICS transaction loop, transferring control between programs
# as the user navigates the application.
#
# Prerequisites:
#   - Run build_simulator.sh first
#   - Run compile_online.sh to compile the programs
#   - Run ../load_data.sh to load data files
#
# Usage:
#   cd <repo-root>
#   ./scripts/cics-sim/run_online.sh [PROGRAM] [TRANSID]
#
# Examples:
#   ./scripts/cics-sim/run_online.sh              # Start at sign-on
#   ./scripts/cics-sim/run_online.sh COMEN01C     # Start at main menu

set -e

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
BUILD_DIR="$REPO_ROOT/build/online"
DATA_DIR="$REPO_ROOT/build/data"

# Default starting program and transaction
START_PGM="${1:-COSGN00C}"
START_TRAN="${2:-CC00}"

# Ensure data directory exists
if [ ! -d "$DATA_DIR" ]; then
    echo "Error: Data directory not found at $DATA_DIR"
    echo "Run: ./scripts/load_data.sh first"
    exit 1
fi

# Ensure program exists
if [ ! -f "$BUILD_DIR/$START_PGM" ]; then
    echo "Error: Program $START_PGM not found in $BUILD_DIR/"
    echo "Run: ./scripts/cics-sim/compile_online.sh first"
    exit 1
fi

# Set environment for CICS simulator
export CICS_SIM_TRANSID="$START_TRAN"
export CICS_SIM_PROGRAM="$START_PGM"
export CICS_SIM_DATA_DIR="$DATA_DIR"
export COB_LIBRARY_PATH="/usr/local/lib/gnucobol:$BUILD_DIR"
export LD_LIBRARY_PATH="/usr/local/lib/gnucobol:${LD_LIBRARY_PATH:-}"

# Create queue directory
mkdir -p "$REPO_ROOT/build/queues"

echo "=== CardDemo CICS Simulator ==="
echo "Program:  $START_PGM"
echo "TransID:  $START_TRAN"
echo "Data:     $DATA_DIR"
echo ""
echo "Controls:"
echo "  Type 'PF3' or 'EXIT' to go back / exit"
echo "  Type 'PF7' or 'UP' for previous page"
echo "  Type 'PF8' or 'DOWN' for next page"
echo "  Press Ctrl+C to force quit"
echo ""
echo "Starting..."
echo ""

# Run the starting program
cd "$REPO_ROOT"
exec "$BUILD_DIR/$START_PGM"
