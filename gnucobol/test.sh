#!/usr/bin/env bash
# Smoke/regression test: build, run the batch cycle on app/data/ASCII, check results.
set -uo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export WORK_DIR="${WORK_DIR:-$HERE/work}"
LOG="$WORK_DIR/test-run.log"
pass=0; fail=0

check() {  # DESCRIPTION EXPECTED ACTUAL
  if [[ "$2" == "$3" ]]; then echo "  PASS  $1"; pass=$((pass + 1))
  else echo "  FAIL  $1: expected '$2', got '$3'"; fail=$((fail + 1)); fi
}
records() { [[ -f "$1" ]] && echo $(( $(stat -c %s "$1") / $2 )) || echo 0; }
rc_of() {
  awk -v job="==== $1" 'index($0, job) == 1 { f = 1 }
    f && match($0, /^---- [^ ]+: RC=[0-9]+/) { sub(/.*RC=/, ""); print; exit }' "$LOG"
}

echo "== build"
"$HERE/build.sh" || { echo "FAIL: build"; exit 1; }

echo "== batch cycle"
rm -rf "$WORK_DIR"; mkdir -p "$WORK_DIR"
"$HERE/run-batch.sh" all > "$LOG" 2>&1
check "run-batch.sh all exits 0" 0 "$?"

echo "== results"
for ds in "ACCTDATA 50" "CARDDATA 50" "CUSTDATA 50" "CARDXREF 50" "TCATBALF 50" \
          "DISCGRP 51" "TRANTYPE 7" "TRANCATG 18"; do
  set -- $ds
  check "load $1 record count" "$(printf '%09d' "$2")" \
    "$(grep -oP "LOAD $1 RECORDS: \K\d+" "$LOG" | head -1)"
done
check "POSTTRAN RC (4 = some rejects)" 4 "$(rc_of POSTTRAN)"
check "POSTTRAN processed" 000000300 "$(grep -oP 'TRANSACTIONS PROCESSED :\K\d+' "$LOG")"
check "POSTTRAN rejected"  000000038 "$(grep -oP 'TRANSACTIONS REJECTED  :\K\d+' "$LOG")"
check "DALYREJS records" 38 "$(records "$WORK_DIR/DALYREJS.ps" 430)"
check "INTCALC RC" 0 "$(rc_of INTCALC)"
check "INTCALC interest transactions (SYSTRAN)" 50 "$(records "$WORK_DIR/SYSTRAN.ps" 350)"
check "COMBTRAN TRANSACT records" 000000312 \
  "$(grep -oP 'LOAD TRANSACT RECORDS: \K\d+' "$LOG" | tail -1)"
check "TRANREPT RC" 0 "$(rc_of TRANREPT)"
check "TRANREPT grand total" "79,254.29" \
  "$(grep -oP '^Grand Total\.+\+\s+\K[\d,.]+' "$WORK_DIR/TRANREPT.txt")"
for j in READACCT READCARD READCUST READXREF; do check "$j RC" 0 "$(rc_of $j)"; done
check "READACCT output records (OUTFILE)" 50 "$(records "$WORK_DIR/ACCTDATA.PSCOMP" 107)"

echo "== $pass passed, $fail failed (log: $LOG)"
(( fail == 0 ))
