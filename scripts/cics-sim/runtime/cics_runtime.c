/*
 * cics_runtime.c - Core CICS Simulator Runtime
 *
 * Implements the main CICS transaction processing loop and coordinates
 * the various subsystems (terminal, file, program control).
 *
 * This simulator replaces the need for a commercial CICS emulator
 * (UniKix or Micro Focus) for running CardDemo online programs.
 */

#include "cics_runtime.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <signal.h>
#include <setjmp.h>

/* Global CICS state */
static CICSState g_state;
static int g_initialized = 0;
static jmp_buf g_abend_jmp;
static int g_abend_handler_set = 0;
static char g_abend_label[64] = {0};

CICSState* cics_get_state(void) {
    if (!g_initialized) {
        const char *transid = getenv("CICS_SIM_TRANSID");
        const char *program = getenv("CICS_SIM_PROGRAM");
        cics_init(transid ? transid : "CDSM",
                  program ? program : "UNKNOWN");
    }
    return &g_state;
}

void cics_init(const char *transid, const char *program) {
    memset(&g_state, 0, sizeof(CICSState));

    if (transid) {
        strncpy(g_state.current_transid, transid, MAX_TRANS_ID);
        memcpy(g_state.eib.EIBTRNID, transid, 4);
    }
    if (program) {
        strncpy(g_state.current_program, program, MAX_PROGRAM_NAME);
    }

    strncpy(g_state.applid, "CARDDEMO", 8);
    strncpy(g_state.sysid, "CDSM", 4);

    g_state.eib.EIBTASKN = (int32_t)(getpid() % 99999);
    g_state.running = 1;
    g_initialized = 1;

    /* Set initial date/time in EIB */
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    snprintf(g_state.eib.EIBDATE, 8, "0%03d%03d",
             t->tm_year - 100 + 100, t->tm_yday + 1);
    snprintf(g_state.eib.EIBTIME, 8, "0%02d%02d%02d",
             t->tm_hour, t->tm_min, t->tm_sec);

    /* Register all BMS map definitions */
    cics_register_all_maps();

    fprintf(stderr, "[CICS-SIM] Initialized: TRAN=%s PGM=%s TASK=%d\n",
            g_state.current_transid, g_state.current_program,
            g_state.eib.EIBTASKN);
}

void cics_shutdown(void) {
    if (g_initialized) {
        fprintf(stderr, "[CICS-SIM] Shutdown\n");
        g_initialized = 0;
        g_state.running = 0;
    }
}

/* RETURN - Return control to CICS */
void CICSRETURN(char *transid, void *commarea, int32_t comm_len) {
    if (transid && transid[0] != ' ' && transid[0] != '\0') {
        memcpy(g_state.return_transid, transid, 4);
        g_state.return_transid[4] = '\0';
        g_state.return_transid_set = 1;
    } else {
        g_state.return_transid_set = 0;
    }

    if (commarea && comm_len > 0) {
        int len = (comm_len > MAX_COMMAREA_SIZE) ? MAX_COMMAREA_SIZE : comm_len;
        memcpy(g_state.commarea, commarea, len);
        g_state.commarea_len = len;
        g_state.eib.EIBCALEN = len;
    }

    fprintf(stderr, "[CICS-SIM] RETURN TRANSID=%.*s COMMLEN=%d\n",
            4, g_state.return_transid_set ? g_state.return_transid : "none",
            g_state.commarea_len);
}

/* XCTL - Transfer control to another program */
void CICSXCTL(char *program, void *commarea, int32_t comm_len,
              int32_t *resp, int32_t *resp2) {
    char pgm_name[MAX_PROGRAM_NAME + 1] = {0};
    int i;

    if (!program) {
        if (resp) *resp = DFHRESP_PGMIDERR;
        if (resp2) *resp2 = 0;
        return;
    }

    /* Copy and trim program name */
    memcpy(pgm_name, program, MAX_PROGRAM_NAME);
    for (i = MAX_PROGRAM_NAME - 1; i >= 0 && pgm_name[i] == ' '; i--)
        pgm_name[i] = '\0';

    if (commarea && comm_len > 0) {
        int len = (comm_len > MAX_COMMAREA_SIZE) ? MAX_COMMAREA_SIZE : comm_len;
        memcpy(g_state.commarea, commarea, len);
        g_state.commarea_len = len;
        g_state.eib.EIBCALEN = len;
    }

    strncpy(g_state.current_program, pgm_name, MAX_PROGRAM_NAME);

    fprintf(stderr, "[CICS-SIM] XCTL to program=%s COMMLEN=%d\n",
            pgm_name, g_state.commarea_len);

    if (resp) *resp = DFHRESP_NORMAL;
    if (resp2) *resp2 = 0;
}

/* ASSIGN - Get system information */
void CICSASSIGN(char *field_name, char *value, int32_t value_len) {
    if (!field_name || !value) return;

    if (strncmp(field_name, "APPLID", 6) == 0) {
        int len = (value_len < 8) ? value_len : 8;
        memset(value, ' ', value_len);
        memcpy(value, g_state.applid, len);
    } else if (strncmp(field_name, "SYSID", 5) == 0) {
        int len = (value_len < 4) ? value_len : 4;
        memset(value, ' ', value_len);
        memcpy(value, g_state.sysid, len);
    } else {
        memset(value, ' ', value_len);
    }
}

/* ASKTIME - Get current time */
void CICSASKTIME(char *abstime) {
    time_t now = time(NULL);
    struct tm *t = localtime(&now);

    /* Update EIB */
    snprintf(g_state.eib.EIBDATE, 8, "0%03d%03d",
             t->tm_year - 100 + 100, t->tm_yday + 1);
    snprintf(g_state.eib.EIBTIME, 8, "0%02d%02d%02d",
             t->tm_hour, t->tm_min, t->tm_sec);

    if (abstime) {
        /* ABSTIME is packed decimal milliseconds since epoch - approximate */
        snprintf(abstime, 16, "%015ld", (long)(now * 1000));
    }
}

/* FORMATTIME - Format a time value */
void CICSFORMATTIME(char *abstime, char *output, int32_t out_len,
                    char *format) {
    time_t now = time(NULL);
    struct tm *t = localtime(&now);

    if (!output || out_len <= 0) return;

    if (format) {
        if (strstr(format, "YYYYMMDD") || strstr(format, "DATEFORM")) {
            snprintf(output, out_len, "%04d%02d%02d",
                     t->tm_year + 1900, t->tm_mon + 1, t->tm_mday);
        } else if (strstr(format, "MMDDYY")) {
            snprintf(output, out_len, "%02d%02d%02d",
                     t->tm_mon + 1, t->tm_mday, t->tm_year % 100);
        } else if (strstr(format, "TIME")) {
            snprintf(output, out_len, "%02d:%02d:%02d",
                     t->tm_hour, t->tm_min, t->tm_sec);
        } else {
            snprintf(output, out_len, "%04d%02d%02d",
                     t->tm_year + 1900, t->tm_mon + 1, t->tm_mday);
        }
    } else {
        snprintf(output, out_len, "%04d%02d%02d%02d%02d%02d",
                 t->tm_year + 1900, t->tm_mon + 1, t->tm_mday,
                 t->tm_hour, t->tm_min, t->tm_sec);
    }
}

/* HANDLE ABEND - Set abend handler */
void CICSHANDLEABEND(char *label) {
    if (label) {
        strncpy(g_abend_label, label, sizeof(g_abend_label) - 1);
        g_abend_handler_set = 1;
    }
}

/* ABEND - Force abnormal termination */
void CICSABEND(char *abcode) {
    char code[5] = "????";
    if (abcode) {
        memcpy(code, abcode, 4);
        code[4] = '\0';
    }

    fprintf(stderr, "[CICS-SIM] ABEND code=%s\n", code);

    if (g_abend_handler_set) {
        fprintf(stderr, "[CICS-SIM] Abend handled by label: %s\n",
                g_abend_label);
        longjmp(g_abend_jmp, 1);
    }

    cics_shutdown();
    exit(1);
}

/* WRITEQ TD - Write to transient data queue */
void CICSWRITEQTD(char *queue, char *from, int32_t from_len,
                   int32_t *resp, int32_t *resp2) {
    char qname[9] = {0};
    if (queue) {
        memcpy(qname, queue, 8);
        qname[8] = '\0';
    }

    /* Write to a file simulating the TD queue */
    char filename[256];
    snprintf(filename, sizeof(filename), "build/queues/%s.log", qname);

    FILE *f = fopen(filename, "a");
    if (f) {
        fwrite(from, 1, from_len, f);
        fputc('\n', f);
        fclose(f);
        if (resp) *resp = DFHRESP_NORMAL;
    } else {
        /* Try creating directory first */
        system("mkdir -p build/queues");
        f = fopen(filename, "a");
        if (f) {
            fwrite(from, 1, from_len, f);
            fputc('\n', f);
            fclose(f);
            if (resp) *resp = DFHRESP_NORMAL;
        } else {
            if (resp) *resp = DFHRESP_NOTOPEN;
        }
    }
    if (resp2) *resp2 = 0;
}

/* INQUIRE - System inquiry (stub) */
void CICSINQUIRE(char *resource_type, char *resource_name,
                 char *field, char *value, int32_t value_len) {
    if (value && value_len > 0) {
        memset(value, ' ', value_len);
    }
}
