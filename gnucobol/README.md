# Local GnuCOBOL build and batch run

Compiles the CardDemo **batch** COBOL programs with GnuCOBOL and runs the core
batch cycle (posting, interest, reporting) on `app/data/ASCII`, without a mainframe.
Use it to get a baseline of the COBOL behaviour, for example when checking a Java port.

```bash
sudo apt-get install -y gnucobol3   # GnuCOBOL 3.1.2 on Ubuntu 22.04
gnucobol/build.sh                   # compile into gnucobol/build/
gnucobol/test.sh                    # build + full batch cycle + result checks
gnucobol/run-batch.sh [JOB...]      # run jobs; output in gnucobol/work/
```

## Jobs (`run-batch.sh`)

| Job | JCL | Program | Output in `gnucobol/work/` |
| :-- | :-- | :------ | :------------------------- |
| `load` | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TCATBALF, DISCGRP, TRANTYPE, TRANCATG, TRANFILE | `vsamutil` (IDCAMS REPRO) | `*.ksds`, `DALYTRAN.ps` |
| `posttran` | POSTTRAN | CBTRN02C | `DALYREJS.txt` (RC=4 when some are rejected) |
| `intcalc` | INTCALC | CBACT04C `PARM='2022071800'` | `SYSTRAN.txt` |
| `combtran` | COMBTRAN | sort + REPRO | `TRANSACT.combined.txt` |
| `tranrept` | TRANREPT | CBTRN03C | `TRANREPT.txt` |
| `readacct` / `readcard` / `readcust` / `readxref` | READ* | CBACT01C / CBACT02C / CBCUS01C / CBACT03C | stdout, `ACCTDATA.*` |

`all` (the default) runs them in this order. `posttran` updates the account and
category balance files, so run `load` first to start again from the sample data.
Overrides: `INTCALC_PARM`, `REPORT_START_DATE`, `REPORT_END_DATE`, `WORK_DIR`, `BUILD_DIR`.

## How it maps to z/OS

- **DD statements**: environment variables `DD_<ddname>=<path>`, set for each step by `run-batch.sh`.
- **EXEC PGM=...,PARM=...**: `build/bin/pgmrun PGM [PARM]` calls the program module
  with a halfword-length PARM area. Programs are `.so` modules found via `COB_LIBRARY_PATH`.
- **VSAM KSDS**: GnuCOBOL indexed files (Berkeley DB), defined and loaded by
  `tools/VSAMUTIL.cbl` with the same keys as the IDCAMS `DEFINE CLUSTER`/`AIX` in `app/jcl`.
- **PS datasets (RECFM=FB)**: fixed-length files without newlines (`*.ps`); a `*.txt` view is written next to each one.
- **Compiler options**: `-std=ibm -fsign=EBCDIC` (the ASCII sample data uses EBCDIC-style
  zoned sign overpunch, e.g. `{` = +0) and `-ftab-width=4` (`CUSTREC.cpy` has tabs).
- **System services**: `stubs/` replaces CEE3ABD (abend -> RC=12), COBDATFT (port of
  `app/asm/COBDATFT.asm`) and MVSWAIT (`app/asm/MVSWAIT.asm`).

## Not covered

- Online CICS programs (`CO*.cbl`), BMS maps, and the DB2/IMS/MQ modules: these need a CICS/DB2/IMS runtime.
- CBEXPORT/CBIMPORT do not compile: their `RECORD KEY EXPORT-SEQUENCE-NUM` is in WORKING-STORAGE, not in the FD.
- CBSTM03A (CREASTMT) compiles but crashes at run time because it reads z/OS control blocks (PSA/TCB/TIOT) directly.
