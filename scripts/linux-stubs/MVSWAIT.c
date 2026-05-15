/*
 * MVSWAIT.c — Linux replacement for the z/OS MVSWAIT assembler routine.
 *
 * The original z/390 assembler accepts a 4-byte binary (COMP) value
 * representing centiseconds (1/100 seconds) and pauses execution.
 *
 * GnuCOBOL passes USING parameters as pointers. The COBOL caller
 * (COBSWAIT.cbl) defines: 01 MVSWAIT-TIME PIC 9(8) COMP.
 * COMP on GnuCOBOL is a 4-byte big-endian signed integer.
 *
 * Build:
 *   gcc -shared -fPIC -o MVSWAIT.so MVSWAIT.c
 * Install:
 *   cp MVSWAIT.so /usr/local/lib/gnucobol/
 *
 * Copyright Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0.
 */

#include <unistd.h>
#include <stdint.h>

void MVSWAIT(int32_t *centiseconds) {
    if (centiseconds && *centiseconds > 0) {
        usleep((useconds_t)(*centiseconds) * 10000);  /* centiseconds → microseconds */
    }
}
