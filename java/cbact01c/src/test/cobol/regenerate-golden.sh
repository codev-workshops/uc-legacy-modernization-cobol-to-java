#!/usr/bin/env bash
# Regenerates the committed COBOL golden outputs used by CobolParityTest.
set -euo pipefail
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
module="$(cd "$here/../../.." && pwd)"
repo="$(cd "$module/../.." && pwd)"
golden="$module/src/test/resources/golden"
"$here/run-oracle.sh" "$repo/app/data/ASCII/acctdata.txt" "$golden/sample"
"$here/run-oracle.sh" "$module/src/test/resources/input/acctdata-edge.txt" "$golden/edge"
"$here/run-oracle.sh" "$module/src/test/resources/input/acctdata-empty.txt" "$golden/empty"
ORACLE_SKIP_LOAD=1 "$here/run-oracle.sh" /dev/null "$golden/missing-input"
