/*
 * CEE3ABD.c — Linux replacement for the z/OS LE CEE3ABD routine.
 *
 * CEE3ABD is the z/OS Language Environment "abnormal termination" call.
 * On Linux we simply print the abend code and exit with a non-zero status.
 *
 * Called as: CALL 'CEE3ABD' USING ABCODE, TIMING.
 *   ABCODE: PIC S9(9) COMP — abend code
 *   TIMING: PIC S9(9) COMP — timing flag (0=immediate, 1=allow cleanup)
 *
 * Build:
 *   gcc -shared -fPIC -o CEE3ABD.so CEE3ABD.c
 *
 * Copyright Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

void CEE3ABD(int32_t *abcode, int32_t *timing) {
    int code = abcode ? *abcode : 999;
    fprintf(stderr, "CEE3ABD: ABEND with code %d\n", code);
    exit(code != 0 ? code : 1);
}
