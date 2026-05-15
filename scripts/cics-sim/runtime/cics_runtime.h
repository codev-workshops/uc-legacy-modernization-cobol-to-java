/*
 * cics_runtime.h - CICS Simulator Runtime for CardDemo
 *
 * Provides a lightweight simulation of IBM CICS transaction processing
 * services for running the CardDemo online programs locally with GnuCOBOL
 * on Linux, removing the need for a commercial emulator (UniKix/Micro Focus).
 *
 * Supported CICS commands:
 *   - SEND MAP / SEND TEXT       (terminal output)
 *   - RECEIVE MAP                (terminal input)
 *   - RETURN (with/without TRANSID/COMMAREA)
 *   - XCTL                       (transfer control)
 *   - READ / WRITE / REWRITE / DELETE (file I/O)
 *   - STARTBR / READNEXT / READPREV / ENDBR (file browse)
 *   - ASSIGN                     (system info)
 *   - HANDLE ABEND / ABEND       (error handling)
 *   - WRITEQ TD                  (transient data)
 *   - ASKTIME / FORMATTIME       (time services)
 *   - INQUIRE                    (system inquiry)
 *
 * Copyright Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0.
 */

#ifndef CICS_RUNTIME_H
#define CICS_RUNTIME_H

#include <stdint.h>
#include <stddef.h>

/* CICS response codes */
#define DFHRESP_NORMAL       0
#define DFHRESP_ERROR        1
#define DFHRESP_NOTFND      13
#define DFHRESP_DUPREC      14
#define DFHRESP_DUPKEY      15
#define DFHRESP_INVREQ      16
#define DFHRESP_NOTOPEN     19
#define DFHRESP_ENDFILE     20
#define DFHRESP_LENGERR     22
#define DFHRESP_PGMIDERR    27
#define DFHRESP_MAPFAIL     36
#define DFHRESP_DISABLED    84

/* AID key values (matching DFHAID) */
#define DFHENTER  0x7D
#define DFHCLEAR  0x6D
#define DFHPF1    0xF1
#define DFHPF2    0xF2
#define DFHPF3    0xF3
#define DFHPF4    0xF4
#define DFHPF5    0xF5
#define DFHPF6    0xF6
#define DFHPF7    0xF7
#define DFHPF8    0xF8
#define DFHPF9    0xF9
#define DFHPF10   0x7A
#define DFHPF11   0x7B
#define DFHPF12   0x7C
#define DFHPA1    0x6C
#define DFHPA2    0x6E
#define DFHPA3    0x6B

/* Maximum sizes */
#define MAX_COMMAREA_SIZE   32767
#define MAX_MAP_SIZE        4096
#define MAX_FIELD_SIZE      256
#define MAX_FILE_NAME       8
#define MAX_PROGRAM_NAME    8
#define MAX_TRANS_ID        4
#define MAX_KEY_LENGTH      256
#define MAX_RECORD_LENGTH   4096
#define MAX_BROWSE_SESSIONS 10
#define MAX_SCREEN_ROWS     24
#define MAX_SCREEN_COLS     80

/* EIB - Execute Interface Block */
typedef struct {
    char     EIBAID;                  /* AID key pressed */
    int32_t  EIBCALEN;               /* COMMAREA length */
    char     EIBTRNID[4];            /* Transaction ID */
    char     EIBRSRCE[8];            /* Resource name */
    int32_t  EIBRESP;                /* Primary response */
    int32_t  EIBRESP2;               /* Secondary response */
    char     EIBDATE[8];             /* Current date (0CYYDDD) */
    char     EIBTIME[8];             /* Current time (0HHMMSS) */
    int32_t  EIBTASKN;               /* Task number */
} CICS_EIB;

/* Map field definition */
typedef struct {
    char     name[32];
    int      row;
    int      col;
    int      length;
    int      attr;
    char     color[8];
    char     initial[256];
    int      is_input;
} MapField;

/* Map definition */
typedef struct {
    char     mapset_name[8];
    char     map_name[8];
    int      rows;
    int      cols;
    int      field_count;
    MapField fields[100];
} MapDef;

/* Browse session */
typedef struct {
    int      active;
    char     filename[MAX_FILE_NAME + 1];
    void    *cursor;
    int      direction;  /* 1=forward, -1=backward */
} BrowseSession;

/* CICS runtime state */
typedef struct {
    CICS_EIB        eib;
    char            commarea[MAX_COMMAREA_SIZE];
    int32_t         commarea_len;
    char            current_program[MAX_PROGRAM_NAME + 1];
    char            current_transid[MAX_TRANS_ID + 1];
    char            applid[8];
    char            sysid[4];
    int             running;
    int             return_transid_set;
    char            return_transid[MAX_TRANS_ID + 1];
    BrowseSession   browse_sessions[MAX_BROWSE_SESSIONS];
    char            screen[MAX_SCREEN_ROWS][MAX_SCREEN_COLS + 1];
    int             screen_initialized;
} CICSState;

/* Global state accessor */
CICSState* cics_get_state(void);

/* Initialization and shutdown */
void cics_init(const char *transid, const char *program);
void cics_register_all_maps(void);
void cics_shutdown(void);

/* Terminal I/O */
void CICSSENDMAP(char *mapset, char *map, void *from_area,
                 int32_t from_len, int32_t *resp, int32_t *resp2,
                 int32_t erase, int32_t cursor_pos);
void CICSRECEIVEMAP(char *mapset, char *map, void *into_area,
                    int32_t into_len, int32_t *resp, int32_t *resp2);
void CICSSENDTEXT(char *text, int32_t text_len, int32_t erase, int32_t freekb);

/* Program control */
void CICSRETURN(char *transid, void *commarea, int32_t comm_len);
void CICSXCTL(char *program, void *commarea, int32_t comm_len,
              int32_t *resp, int32_t *resp2);

/* File I/O */
void CICSREAD(char *filename, void *into, int32_t *into_len,
              char *ridfld, int32_t key_len,
              int32_t *resp, int32_t *resp2);
void CICSWRITE(char *filename, void *from, int32_t from_len,
               char *ridfld, int32_t key_len,
               int32_t *resp, int32_t *resp2);
void CICSREWRITE(char *filename, void *from, int32_t from_len,
                 int32_t *resp, int32_t *resp2);
void CICSDELETE(char *filename, char *ridfld, int32_t key_len,
                int32_t *resp, int32_t *resp2);

/* File browse */
void CICSSTARTBR(char *filename, char *ridfld, int32_t key_len,
                 int32_t *resp, int32_t *resp2);
void CICSREADNEXT(char *filename, void *into, int32_t *into_len,
                  char *ridfld, int32_t key_len,
                  int32_t *resp, int32_t *resp2);
void CICSREADPREV(char *filename, void *into, int32_t *into_len,
                  char *ridfld, int32_t key_len,
                  int32_t *resp, int32_t *resp2);
void CICSENDBR(char *filename, int32_t *resp, int32_t *resp2);

/* System services */
void CICSASSIGN(char *field_name, char *value, int32_t value_len);
void CICSASKTIME(char *abstime);
void CICSFORMATTIME(char *abstime, char *output, int32_t out_len,
                    char *format);
void CICSHANDLEABEND(char *label);
void CICSABEND(char *abcode);

/* Queue services */
void CICSWRITEQTD(char *queue, char *from, int32_t from_len,
                   int32_t *resp, int32_t *resp2);

/* Inquiry */
void CICSINQUIRE(char *resource_type, char *resource_name,
                 char *field, char *value, int32_t value_len);

/* Map registration */
void cics_register_map(const char *mapset, const char *map,
                       int rows, int cols,
                       MapField *fields, int field_count);

#endif /* CICS_RUNTIME_H */
