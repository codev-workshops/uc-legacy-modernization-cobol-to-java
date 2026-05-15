/*
 * COBDATFT.c — Linux replacement for the z/OS COBDATFT assembler routine.
 *
 * Converts dates between two formats:
 *   Type '1' input:  YYYYMMDD  → output: YYYY-MM-DD
 *   Type '2' input:  YYYY-MM-DD → output: YYYYMMDD
 *
 * The record layout (from COCDATFT.mac / CODATECN.cpy):
 *   Offset  0: COINTYPE  PIC X(1)   — input type ('1' or '2')
 *   Offset  1: COINPDT   PIC X(20)  — input date
 *   Offset 21: COOUTYPE  PIC X(1)   — output type (unused, same convention)
 *   Offset 22: COOUTDT   PIC X(20)  — output date
 *   Offset 42: COERMSG   PIC X(38)  — error message
 *   Total: 80 bytes
 *
 * Build:
 *   gcc -shared -fPIC -o COBDATFT.so COBDATFT.c
 * Install:
 *   cp COBDATFT.so /usr/local/lib/gnucobol/
 *
 * Copyright Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0.
 */

#include <string.h>

typedef struct {
    char cointype;       /*  1 byte  — '1' or '2' */
    char coinpdt[20];    /* 20 bytes — input date  */
    char cooutype;       /*  1 byte  — output type */
    char cooutdt[20];    /* 20 bytes — output date */
    char coermsg[38];    /* 38 bytes — error msg   */
} CODATECN_REC;

void COBDATFT(CODATECN_REC *rec) {
    memset(rec->cooutdt, ' ', 20);
    memset(rec->coermsg, ' ', 38);

    if (rec->cointype == '1') {
        /* YYYYMMDD → YYYY-MM-DD */
        if (rec->coinpdt[4] == '-') {
            memcpy(rec->coermsg, "INVALID INPUT", 13);
            return;
        }
        rec->cooutdt[0] = rec->coinpdt[0];  /* Y */
        rec->cooutdt[1] = rec->coinpdt[1];  /* Y */
        rec->cooutdt[2] = rec->coinpdt[2];  /* Y */
        rec->cooutdt[3] = rec->coinpdt[3];  /* Y */
        rec->cooutdt[4] = '-';
        rec->cooutdt[5] = rec->coinpdt[4];  /* M */
        rec->cooutdt[6] = rec->coinpdt[5];  /* M */
        rec->cooutdt[7] = '-';
        rec->cooutdt[8] = rec->coinpdt[6];  /* D */
        rec->cooutdt[9] = rec->coinpdt[7];  /* D */
    } else if (rec->cointype == '2') {
        /* YYYY-MM-DD → YYYYMMDD */
        rec->cooutdt[0] = rec->coinpdt[0];  /* Y */
        rec->cooutdt[1] = rec->coinpdt[1];  /* Y */
        rec->cooutdt[2] = rec->coinpdt[2];  /* Y */
        rec->cooutdt[3] = rec->coinpdt[3];  /* Y */
        rec->cooutdt[4] = rec->coinpdt[5];  /* M */
        rec->cooutdt[5] = rec->coinpdt[6];  /* M */
        rec->cooutdt[6] = rec->coinpdt[8];  /* D */
        rec->cooutdt[7] = rec->coinpdt[9];  /* D */
    } else {
        memcpy(rec->coermsg, "INVALID INPUT", 13);
    }
}
