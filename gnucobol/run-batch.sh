#!/usr/bin/env bash
# Run CardDemo batch jobs locally with GnuCOBOL, emulating the JCL in app/jcl.
# Usage: gnucobol/run-batch.sh [JOB...]   (default: all)
# Jobs:  load posttran intcalc combtran tranrept
#        readacct readcard readcust readxref
#  all = load posttran intcalc combtran tranrept readacct readcard readcust readxref
# Datasets live in $WORK_DIR (default gnucobol/work); *.txt files there are readable views.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
BUILD="${BUILD_DIR:-$HERE/build}"
WORK="${WORK_DIR:-$HERE/work}"
DATA="$ROOT/app/data/ASCII"
PGMRUN="$BUILD/bin/pgmrun"
VSAMUTIL="$BUILD/bin/vsamutil"
INTCALC_PARM="${INTCALC_PARM:-2022071800}"
REPORT_START_DATE="${REPORT_START_DATE:-2000-01-01}"
REPORT_END_DATE="${REPORT_END_DATE:-2099-12-31}"

[[ -x "$PGMRUN" && -x "$VSAMUTIL" ]] || { echo "Not built yet: run gnucobol/build.sh" >&2; exit 1; }
mkdir -p "$WORK"
export COB_LIBRARY_PATH="$BUILD/lib"
export LC_ALL=C

# VSAM KSDS datasets, by DD name used in the programs.
ACCTFILE="$WORK/ACCTDATA.ksds"; CARDFILE="$WORK/CARDDATA.ksds"; CUSTFILE="$WORK/CUSTDATA.ksds"
XREFFILE="$WORK/CARDXREF.ksds"; TCATBALF="$WORK/TCATBALF.ksds"; DISCGRP="$WORK/DISCGRP.ksds"
TRANTYPE="$WORK/TRANTYPE.ksds"; TRANCATG="$WORK/TRANCATG.ksds"; TRANSACT="$WORK/TRANSACT.ksds"

# Text lines -> fixed-length records (RECFM=FB), and back.
to_fixed() { awk -v n="$1" 'BEGIN { f = "%-" n "." n "s" } { gsub("\r", ""); printf f, $0 }'; }
to_lines() { fold -w "$1"; echo; }

# step NAME PGM [PARM] -- DD=path ...
step() {
  local name="$1" pgm="$2" parm="" rc=0
  shift 2
  if [[ "${1:-}" != "--" ]]; then parm="$1"; shift; fi
  shift
  echo "---- $name: EXEC PGM=$pgm${parm:+,PARM='$parm'}"
  ( for dd in "$@"; do export "DD_${dd%%=*}=${dd#*=}"; done
    exec "$PGMRUN" "$pgm" "$parm" ) || rc=$?
  echo "---- $name: RC=$rc"
  if (( rc > ${MAXRC:-4} )); then echo "Job step $name failed (RC=$rc)" >&2; exit "$rc"; fi
}

ksds_load() {    # DATASET KSDS-DD KSDS-PATH INPUT-TEXT
  echo "---- REPRO $1 -> $3"
  rm -f "$3" "$3".*
  ( export DD_SEQIN="$4" "DD_$2=$3"; exec "$VSAMUTIL" LOAD "$1" )
}
ksds_unload() {  # DATASET KSDS-DD KSDS-PATH OUTPUT-TEXT
  ( export DD_SEQOUT="$4" "DD_$2=$3"; exec "$VSAMUTIL" UNLOAD "$1" ) >/dev/null
}

job_load() {
  echo "==== LOAD: define and load VSAM files from app/data/ASCII"
  ksds_load ACCTDATA ACCTFILE "$ACCTFILE" "$DATA/acctdata.txt"
  ksds_load CARDDATA CARDFILE "$CARDFILE" "$DATA/carddata.txt"
  ksds_load CUSTDATA CUSTFILE "$CUSTFILE" "$DATA/custdata.txt"
  ksds_load CARDXREF XREFFILE "$XREFFILE" "$DATA/cardxref.txt"
  ksds_load TCATBALF TCATBALF "$TCATBALF" "$DATA/tcatbal.txt"
  ksds_load DISCGRP  DISCGRP  "$DISCGRP"  "$DATA/discgrp.txt"
  ksds_load TRANTYPE TRANTYPE "$TRANTYPE" "$DATA/trantype.txt"
  ksds_load TRANCATG TRANCATG "$TRANCATG" "$DATA/trancatg.txt"
  ksds_load TRANSACT TRANFILE "$TRANSACT" /dev/null
  to_fixed 350 < "$DATA/dailytran.txt" > "$WORK/DALYTRAN.ps"
}

job_posttran() {
  echo "==== POSTTRAN: post daily transactions (CBTRN02C)"
  rm -f "$WORK/DALYREJS.ps"
  step STEP15 CBTRN02C -- TRANFILE="$TRANSACT" DALYTRAN="$WORK/DALYTRAN.ps" \
    XREFFILE="$XREFFILE" DALYREJS="$WORK/DALYREJS.ps" ACCTFILE="$ACCTFILE" TCATBALF="$TCATBALF"
  to_lines 430 < "$WORK/DALYREJS.ps" > "$WORK/DALYREJS.txt"
}

job_intcalc() {
  echo "==== INTCALC: interest calculation (CBACT04C)"
  rm -f "$WORK/SYSTRAN.ps"
  step STEP15 CBACT04C "$INTCALC_PARM" -- TCATBALF="$TCATBALF" XREFFILE="$XREFFILE" \
    ACCTFILE="$ACCTFILE" DISCGRP="$DISCGRP" TRANSACT="$WORK/SYSTRAN.ps"
  to_lines 350 < "$WORK/SYSTRAN.ps" > "$WORK/SYSTRAN.txt"
}

job_combtran() {
  echo "==== COMBTRAN: merge posted + interest transactions into TRANSACT"
  ksds_unload TRANSACT TRANFILE "$TRANSACT" "$WORK/TRANSACT.bkup.txt"
  cat "$WORK/TRANSACT.bkup.txt" "$WORK/SYSTRAN.txt" 2>/dev/null | grep -v '^$' \
    | sort -s -t $'\001' -k1.1,1.16 > "$WORK/TRANSACT.combined.txt"
  ksds_load TRANSACT TRANFILE "$TRANSACT" "$WORK/TRANSACT.combined.txt"
}

job_tranrept() {
  echo "==== TRANREPT: transaction report $REPORT_START_DATE..$REPORT_END_DATE (CBTRN03C)"
  ksds_unload TRANSACT TRANFILE "$TRANSACT" "$WORK/TRANSACT.bkup.txt"
  awk -v s="$REPORT_START_DATE" -v e="$REPORT_END_DATE" \
      '{ d = substr($0, 305, 10) } d >= s && d <= e' "$WORK/TRANSACT.bkup.txt" \
    | sort -s -t $'\001' -k1.263,1.278 | to_fixed 350 > "$WORK/TRANSACT.daly.ps"
  printf '%s %s\n' "$REPORT_START_DATE" "$REPORT_END_DATE" | to_fixed 80 > "$WORK/DATEPARM.ps"
  rm -f "$WORK/TRANREPT.ps"
  step STEP10R CBTRN03C -- TRANFILE="$WORK/TRANSACT.daly.ps" CARDXREF="$XREFFILE" \
    TRANTYPE="$TRANTYPE" TRANCATG="$TRANCATG" DATEPARM="$WORK/DATEPARM.ps" TRANREPT="$WORK/TRANREPT.ps"
  to_lines 133 < "$WORK/TRANREPT.ps" > "$WORK/TRANREPT.txt"
}

job_readacct() {
  echo "==== READACCT (CBACT01C)"
  rm -f "$WORK"/ACCTDATA.{PSCOMP,ARRYPS,VBPS}
  step STEP05 CBACT01C -- ACCTFILE="$ACCTFILE" OUTFILE="$WORK/ACCTDATA.PSCOMP" \
    ARRYFILE="$WORK/ACCTDATA.ARRYPS" VBRCFILE="$WORK/ACCTDATA.VBPS"
}
job_readcard() { echo "==== READCARD (CBACT02C)"; step STEP05 CBACT02C -- CARDFILE="$CARDFILE"; }
job_readcust() { echo "==== READCUST (CBCUS01C)"; step STEP05 CBCUS01C -- CUSTFILE="$CUSTFILE"; }
job_readxref() { echo "==== READXREF (CBACT03C)"; step STEP05 CBACT03C -- XREFFILE="$XREFFILE"; }

ALL=(load posttran intcalc combtran tranrept readacct readcard readcust readxref)
jobs=("$@"); (( ${#jobs[@]} )) || jobs=(all)
for j in "${jobs[@]}"; do
  if [[ "$j" == all ]]; then for a in "${ALL[@]}"; do "job_$a"; done
  elif declare -F "job_$j" >/dev/null; then "job_$j"
  else echo "Unknown job: $j (known: ${ALL[*]})" >&2; exit 2; fi
done
