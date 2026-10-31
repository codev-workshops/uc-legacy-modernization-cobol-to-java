#!/usr/bin/env bash
# Runs the full migration test harness and writes evidence to test-harness/reports/.
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p reports
python3 -m pytest --junitxml=reports/junit.xml -q "$@"
python3 -m carddemo_harness reconcile --report reports/reconciliation.json > reports/reconciliation.txt
tail -n 1 reports/reconciliation.txt
