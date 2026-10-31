#!/usr/bin/env bash
# Runs the unmodified app/cbl/CBACT01C.cbl under GnuCOBOL as the parity oracle.
#   run-oracle.sh <account-unload.txt> <output-dir>
# Writes OUTFILE, ARRYFILE, VBRCFILE, SYSOUT and RC into <output-dir>.
set -euo pipefail
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo="$(cd "$here/../../../../.." && pwd)"
input="$(realpath "$1")"
mkdir -p "$2"
out="$(realpath "$2")"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

flags=(-x -std=ibm -fsign=EBCDIC -I "$repo/app/cpy")
cobc "${flags[@]}" -o "$work/cbact01c" "$repo/app/cbl/CBACT01C.cbl" \
  "$here/COBDATFT.cbl" "$here/CEE3ABD.cbl"
cobc "${flags[@]}" -o "$work/loadksds" "$here/LOADKSDS.cbl"

if [[ "${ORACLE_SKIP_LOAD:-}" != "1" ]]; then
  DD_INFILE="$input" DD_ACCTFILE="$work/acct.ksds" "$work/loadksds"
fi
rc=0
DD_ACCTFILE="$work/acct.ksds" DD_OUTFILE="$out/OUTFILE" DD_ARRYFILE="$out/ARRYFILE" \
  DD_VBRCFILE="$out/VBRCFILE" COB_SET_RUNTIME_ERRORS_OFF=1 \
  "$work/cbact01c" >"$out/SYSOUT" 2>/dev/null || rc=$?
echo "$rc" >"$out/RC"
