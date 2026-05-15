#!/bin/bash
# load_data.sh — Load ASCII flat files into BDB-backed indexed (VSAM) files.
#
# GnuCOBOL's BDB handler creates indexed files automatically.
# This script generates a small COBOL loader for each data file,
# compiles it, runs it, then cleans up the temporary source.
#
# Usage:
#   cd <repo-root>
#   ./scripts/load_data.sh
#
# Output: BDB indexed files in build/data/

set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASCII_DIR="$REPO_ROOT/app/data/ASCII"
DATA_DIR="$REPO_ROOT/build/data"
TMP_DIR="$REPO_ROOT/build/tmp"
COBC="/usr/local/bin/cobc"

mkdir -p "$DATA_DIR" "$TMP_DIR"

generate_loader() {
    local name="$1"      # e.g. ACCTDATA
    local reclen="$2"     # e.g. 300
    local keylen="$3"     # e.g. 11
    local keyoff="$4"     # e.g. 0 (1-based for COBOL)
    local src="$TMP_DIR/LOAD${name}.cbl"

    # COBOL key offset is 1-based in the RECORD KEY definition
    # but the KEYS(len offset) in JCL is 0-based byte offset
    # GnuCOBOL handles key position from the record definition

    cat > "$src" << ENDCOBOL
       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOAD${name}.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT INFILE ASSIGN TO INFILE
                  ORGANIZATION IS LINE SEQUENTIAL
                  FILE STATUS IS WS-IN-STATUS.
           SELECT OUTFILE ASSIGN TO OUTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS OUT-KEY
                  FILE STATUS  IS WS-OUT-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  INFILE.
       01  IN-RECORD               PIC X(${reclen}).
       FD  OUTFILE.
       01  OUT-RECORD.
           05  OUT-KEY             PIC X(${keylen}).
           05  OUT-REST            PIC X($((reclen - keylen))).
       WORKING-STORAGE SECTION.
       01  WS-IN-STATUS            PIC XX.
       01  WS-OUT-STATUS           PIC XX.
       01  WS-COUNT                PIC 9(6) VALUE 0.
       01  WS-EOF                  PIC 9    VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT  INFILE.
           OPEN OUTPUT OUTFILE.
           PERFORM UNTIL WS-EOF = 1
               READ INFILE INTO OUT-RECORD
                   AT END
                       MOVE 1 TO WS-EOF
                   NOT AT END
                       WRITE OUT-RECORD
                       ADD 1 TO WS-COUNT
               END-READ
           END-PERFORM.
           CLOSE INFILE.
           CLOSE OUTFILE.
           DISPLAY "LOAD${name}: " WS-COUNT " records loaded".
           STOP RUN.
ENDCOBOL
}

load_file() {
    local name="$1"
    local ascii_file="$2"
    local reclen="$3"
    local keylen="$4"

    echo -n "  Loading $name ($ascii_file → $DATA_DIR/$name)... "

    generate_loader "$name" "$reclen" "$keylen"

    local src="$TMP_DIR/LOAD${name}.cbl"
    local exe="$TMP_DIR/LOAD${name}"

    if ! $COBC -x -o "$exe" "$src" 2>/tmp/loader_err_$$; then
        echo "COMPILE FAILED"
        cat /tmp/loader_err_$$
        rm -f /tmp/loader_err_$$
        return 1
    fi

    # Remove old data file if exists
    rm -f "$DATA_DIR/$name"

    # GnuCOBOL uses environment variables for file assignment
    export INFILE="$ASCII_DIR/$ascii_file"
    export OUTFILE="$DATA_DIR/$name"

    if "$exe" 2>/tmp/loader_err_$$; then
        echo "OK"
    else
        echo "LOAD FAILED"
        cat /tmp/loader_err_$$
    fi

    rm -f /tmp/loader_err_$$ "$src" "$exe"
    unset INFILE OUTFILE
}

echo "=== CardDemo Data Loading ==="
echo "Source:  $ASCII_DIR"
echo "Output:  $DATA_DIR"
echo ""

# File definitions from JCL: name, ascii_file, record_length, key_length
#                              KEYS(keylen, keyoff) from JCL
load_file "ACCTDATA"  "acctdata.txt"  300 11
load_file "CARDDATA"  "carddata.txt"  150 16
load_file "CUSTDATA"  "custdata.txt"  500  9
load_file "CARDXREF"  "cardxref.txt"   50 16
load_file "DISCGRP"   "discgrp.txt"    50 16
load_file "TCATBALF"  "tcatbal.txt"    50 17
load_file "TRANCATG"  "trancatg.txt"   60  6
load_file "TRANTYPE"  "trantype.txt"   60  2

# TRANSACT: transaction master VSAM — initially loaded from dailytran.txt
# In mainframe JCL (TRANFILE), DALYTRAN.PS.INIT is REPRO'd into TRANSACT VSAM
load_file "TRANSACT"  "dailytran.txt"  350 16

# DALYTRAN: sequential daily transaction file (copy for batch processing)
echo -n "  Copying DALYTRAN (dailytran.txt)... "
cp "$ASCII_DIR/dailytran.txt" "$DATA_DIR/DALYTRAN"
echo "OK ($(wc -l < "$ASCII_DIR/dailytran.txt") records)"

# USRSEC: user security file (from DUSRSECJ JCL)
# Record layout: SEC-USR-ID(8) + FNAME(20) + LNAME(20) + PWD(8) + TYPE(1) + FILLER(23) = 80 bytes
load_file "USRSEC"    "usrsec.txt"     80  8

echo ""
echo "=== Data loading complete ==="
echo ""
echo "Indexed files created in $DATA_DIR:"
ls -la "$DATA_DIR/"

# Clean up
rm -rf "$TMP_DIR"
