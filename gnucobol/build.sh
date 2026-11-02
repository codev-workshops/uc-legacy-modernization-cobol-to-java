#!/usr/bin/env bash
# Compile the CardDemo batch COBOL programs with GnuCOBOL.
# Output: gnucobol/build/lib/*.so (programs) and gnucobol/build/bin/ (drivers).
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
BUILD="${BUILD_DIR:-$HERE/build}"
LIB="$BUILD/lib"
BIN="$BUILD/bin"

# IBM dialect, EBCDIC-style zoned sign overpunch (matches app/data/ASCII),
# tab width 4 for copybooks containing tabs (CUSTREC.cpy).
COBFLAGS=(-std=ibm -fsign=EBCDIC -ftab-width=4 -I "$ROOT/app/cpy" -I "$ROOT/app/cpy-bms")

# Batch programs that compile and run without CICS/DB2/IMS/MQ.
BATCH_PROGRAMS=(
  CBACT01C CBACT02C CBACT03C CBACT04C CBCUS01C
  CBTRN01C CBTRN02C CBTRN03C
  CBSTM03A CBSTM03B
  CSUTLDTC COBSWAIT
)
# Known source defects: RECORD KEY EXPORT-SEQUENCE-NUM is declared in
# WORKING-STORAGE (CVEXPORT.cpy) rather than in the indexed file's FD.
SKIPPED_PROGRAMS=(CBEXPORT CBIMPORT)

command -v cobc >/dev/null || { echo "cobc not found: sudo apt-get install -y gnucobol3" >&2; exit 1; }
mkdir -p "$LIB" "$BIN"

src_of() {
  local f
  for f in "$ROOT/app/cbl/$1.cbl" "$ROOT/app/cbl/$1.CBL" "$HERE/stubs/$1.cbl" "$HERE/tools/$1.cbl"; do
    [[ -f "$f" ]] && { echo "$f"; return; }
  done
  echo "source not found for $1" >&2; return 1
}

failed=0
compile_module() {
  local src; src="$(src_of "$1")"
  if cobc -m "${COBFLAGS[@]}" -o "$LIB/$1.so" "$src"; then
    echo "  OK    $1"
  else
    echo "  FAIL  $1"; failed=$((failed + 1))
  fi
}

echo "Compiling stubs for z/OS services (CEE3ABD, COBDATFT, MVSWAIT)"
for p in CEE3ABD COBDATFT MVSWAIT; do compile_module "$p"; done

echo "Compiling batch programs"
for p in "${BATCH_PROGRAMS[@]}"; do compile_module "$p"; done
for p in "${SKIPPED_PROGRAMS[@]}"; do echo "  SKIP  $p (known source defect, see build.sh)"; done

echo "Compiling local drivers (pgmrun, vsamutil)"
cobc -x "${COBFLAGS[@]}" -o "$BIN/pgmrun" "$HERE/tools/PGMRUN.cbl" && echo "  OK    pgmrun" || failed=$((failed + 1))
cobc -x "${COBFLAGS[@]}" -o "$BIN/vsamutil" "$HERE/tools/VSAMUTIL.cbl" && echo "  OK    vsamutil" || failed=$((failed + 1))

if (( failed > 0 )); then
  echo "Build finished with $failed failure(s)" >&2
  exit 1
fi
echo "Build succeeded: $LIB"
