/*
 * cics_cobol_interface.c - GnuCOBOL-callable CICS API Entry Points
 *
 * This file provides the C functions that the CICS preprocessor generates
 * CALL statements to. GnuCOBOL passes parameters as pointers.
 *
 * Naming convention: CICS_xxx where xxx matches the EXEC CICS verb.
 * The preprocessor converts:
 *   EXEC CICS SEND MAP('COSGN0A') MAPSET('COSGN00') FROM(...) ERASE END-EXEC
 * to:
 *   CALL 'CICSSNDM' USING mapset map from-area from-len resp resp2 erase cursor
 *
 * All parameters are passed BY REFERENCE (pointers) per GnuCOBOL convention.
 */

#include "cics_runtime.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

/* ===== Terminal I/O ===== */

/* SEND MAP */
void CICSSNDM(char *mapset, char *map, char *from_area,
              int32_t *from_len, int32_t *resp, int32_t *resp2,
              int32_t *erase, int32_t *cursor_pos) {
    CICSSENDMAP(mapset, map, from_area,
                from_len ? *from_len : 0,
                resp, resp2,
                erase ? *erase : 0,
                cursor_pos ? *cursor_pos : -1);
}

/* RECEIVE MAP */
void CICSRCVM(char *mapset, char *map, char *into_area,
              int32_t *into_len, int32_t *resp, int32_t *resp2) {
    int32_t len = into_len ? *into_len : MAX_MAP_SIZE;
    CICSRECEIVEMAP(mapset, map, into_area, len, resp, resp2);
}

/* SEND TEXT */
void CICSSNTX(char *text, int32_t *text_len, int32_t *erase, int32_t *freekb) {
    CICSSENDTEXT(text,
                 text_len ? *text_len : 0,
                 erase ? *erase : 0,
                 freekb ? *freekb : 0);
}

/* ===== Program Control ===== */

/* RETURN */
void CICSRETN(char *transid, char *commarea, int32_t *comm_len) {
    CICSRETURN(transid, commarea, comm_len ? *comm_len : 0);
}

/* XCTL */
void CICSXCTL_(char *program, char *commarea, int32_t *comm_len,
               int32_t *resp, int32_t *resp2) {
    CICSXCTL(program, commarea, comm_len ? *comm_len : 0, resp, resp2);
}

/* ===== File I/O ===== */

/* READ */
void CICSREAD_(char *filename, char *into, int32_t *into_len,
               char *ridfld, int32_t *key_len,
               int32_t *resp, int32_t *resp2) {
    CICSREAD(filename, into, into_len,
             ridfld, key_len ? *key_len : 0,
             resp, resp2);
}

/* WRITE */
void CICSWRIT(char *filename, char *from, int32_t *from_len,
              char *ridfld, int32_t *key_len,
              int32_t *resp, int32_t *resp2) {
    CICSWRITE(filename, from, from_len ? *from_len : 0,
              ridfld, key_len ? *key_len : 0,
              resp, resp2);
}

/* REWRITE */
void CICSRWRT(char *filename, char *from, int32_t *from_len,
              int32_t *resp, int32_t *resp2) {
    CICSREWRITE(filename, from, from_len ? *from_len : 0, resp, resp2);
}

/* DELETE */
void CICSDELT(char *filename, char *ridfld, int32_t *key_len,
              int32_t *resp, int32_t *resp2) {
    CICSDELETE(filename, ridfld, key_len ? *key_len : 0, resp, resp2);
}

/* STARTBR */
void CICSSTBR(char *filename, char *ridfld, int32_t *key_len,
              int32_t *resp, int32_t *resp2) {
    CICSSTARTBR(filename, ridfld, key_len ? *key_len : 0, resp, resp2);
}

/* READNEXT */
void CICSRDNX(char *filename, char *into, int32_t *into_len,
              char *ridfld, int32_t *key_len,
              int32_t *resp, int32_t *resp2) {
    CICSREADNEXT(filename, into, into_len,
                 ridfld, key_len ? *key_len : 0,
                 resp, resp2);
}

/* READPREV */
void CICSRDPV(char *filename, char *into, int32_t *into_len,
              char *ridfld, int32_t *key_len,
              int32_t *resp, int32_t *resp2) {
    CICSREADPREV(filename, into, into_len,
                 ridfld, key_len ? *key_len : 0,
                 resp, resp2);
}

/* ENDBR */
void CICSENDB(char *filename, int32_t *resp, int32_t *resp2) {
    CICSENDBR(filename, resp, resp2);
}

/* ===== System Services ===== */

/* ASSIGN */
void CICSASGN(char *field_name, char *value, int32_t *value_len) {
    CICSASSIGN(field_name, value, value_len ? *value_len : 0);
}

/* ASKTIME */
void CICSASKT(char *abstime) {
    CICSASKTIME(abstime);
}

/* FORMATTIME */
void CICSFMTT(char *abstime, char *output, int32_t *out_len, char *format) {
    CICSFORMATTIME(abstime, output, out_len ? *out_len : 0, format);
}

/* HANDLE ABEND */
void CICSHABA(char *label) {
    CICSHANDLEABEND(label);
}

/* ABEND */
void CICSABND(char *abcode) {
    CICSABEND(abcode);
}

/* WRITEQ TD */
void CICSWQTD(char *queue, char *from, int32_t *from_len,
              int32_t *resp, int32_t *resp2) {
    CICSWRITEQTD(queue, from, from_len ? *from_len : 0, resp, resp2);
}

/* INQUIRE */
void CICSINQR(char *resource_type, char *resource_name,
              char *field, char *value, int32_t *value_len) {
    CICSINQUIRE(resource_type, resource_name, field, value,
                value_len ? *value_len : 0);
}

/* ===== EIB Access Functions ===== */

/* Get EIB fields - called by generated COBOL to access EIB values */
void CICSEIB(char *eib_area, int32_t *eib_len) {
    CICSState *state = cics_get_state();
    if (eib_area && eib_len && *eib_len > 0) {
        int len = (*eib_len > (int32_t)sizeof(CICS_EIB)) ?
                  (int32_t)sizeof(CICS_EIB) : *eib_len;
        memcpy(eib_area, &state->eib, len);
    }
}

/* Set EIBAID */
void CICSAID(char *aid) {
    CICSState *state = cics_get_state();
    if (aid) {
        state->eib.EIBAID = *aid;
    }
}

/* Get COMMAREA pointer and length */
void CICSCOMA(char *commarea_out, int32_t *comm_len) {
    CICSState *state = cics_get_state();
    if (commarea_out && state->commarea_len > 0) {
        int len = state->commarea_len;
        if (comm_len && *comm_len > 0 && *comm_len < len)
            len = *comm_len;
        memcpy(commarea_out, state->commarea, len);
    }
    if (comm_len) *comm_len = state->commarea_len;
}
