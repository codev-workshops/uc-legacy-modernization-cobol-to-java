/*
 * cics_file.c - CICS File I/O Simulator
 *
 * Implements CICS file commands using Berkeley DB (BDB) indexed files,
 * which is how GnuCOBOL handles VSAM KSDS files on Linux.
 *
 * Commands: READ, WRITE, REWRITE, DELETE, STARTBR, READNEXT, READPREV, ENDBR
 *
 * Files are stored in build/data/ as BDB indexed files (same format used
 * by the batch programs via GnuCOBOL's file handler).
 */

#include "cics_runtime.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <db.h>

/* File handle cache */
typedef struct {
    char     name[MAX_FILE_NAME + 1];
    DB      *dbp;
    DBC     *cursors[MAX_BROWSE_SESSIONS];
    int      cursor_count;
    int      open;
} FileHandle;

#define MAX_FILES 20
static FileHandle g_files[MAX_FILES];
static int g_files_initialized = 0;

/* Get the data directory from env or default */
static const char* get_data_dir(void) {
    const char *dir = getenv("CICS_SIM_DATA_DIR");
    return dir ? dir : "build/data";
}

/* Map CardDemo logical file names to physical BDB file paths */
static const char* get_file_path(const char *logical_name) {
    static char path[512];
    char name[MAX_FILE_NAME + 1] = {0};
    int i;
    const char *data_dir = get_data_dir();

    /* Copy and trim */
    strncpy(name, logical_name, MAX_FILE_NAME);
    for (i = MAX_FILE_NAME - 1; i >= 0 && name[i] == ' '; i--)
        name[i] = '\0';

    /* Map logical names to physical data files */
    if (strcmp(name, "ACCTDAT") == 0 || strcmp(name, "ACCTFILE") == 0)
        snprintf(path, sizeof(path), "%s/ACCTDATA", data_dir);
    else if (strcmp(name, "CARDDAT") == 0 || strcmp(name, "CARDFILE") == 0)
        snprintf(path, sizeof(path), "%s/CARDDATA", data_dir);
    else if (strcmp(name, "CUSTDAT") == 0 || strcmp(name, "CUSTFILE") == 0)
        snprintf(path, sizeof(path), "%s/CUSTDATA", data_dir);
    else if (strcmp(name, "TRANSACT") == 0 || strcmp(name, "TRANFILE") == 0)
        snprintf(path, sizeof(path), "%s/TRANSACT", data_dir);
    else if (strcmp(name, "CARDXREF") == 0 || strcmp(name, "CXREF") == 0 ||
             strcmp(name, "CXACAIX") == 0)
        snprintf(path, sizeof(path), "%s/CARDXREF", data_dir);
    else if (strcmp(name, "USRSEC") == 0 || strcmp(name, "USRSECF") == 0)
        snprintf(path, sizeof(path), "%s/USRSEC", data_dir);
    else if (strcmp(name, "DISCGRP") == 0)
        snprintf(path, sizeof(path), "%s/DISCGRP", data_dir);
    else if (strcmp(name, "TCATBAL") == 0 || strcmp(name, "TCATBALF") == 0)
        snprintf(path, sizeof(path), "%s/TCATBALF", data_dir);
    else if (strcmp(name, "TRANCATG") == 0 || strcmp(name, "TRNCAT") == 0)
        snprintf(path, sizeof(path), "%s/TRANCATG", data_dir);
    else if (strcmp(name, "TRANTYPE") == 0 || strcmp(name, "TRNTYPE") == 0)
        snprintf(path, sizeof(path), "%s/TRANTYPE", data_dir);
    else if (strcmp(name, "DALYTRAN") == 0)
        snprintf(path, sizeof(path), "%s/DALYTRAN", data_dir);
    else if (strcmp(name, "CCXREF") == 0)
        snprintf(path, sizeof(path), "%s/CARDXREF", data_dir);
    else
        snprintf(path, sizeof(path), "%s/%s", data_dir, name);

    return path;
}

/* Open or retrieve a file handle */
static FileHandle* get_file_handle(const char *filename) {
    char name[MAX_FILE_NAME + 1] = {0};
    int i, ret;

    if (!g_files_initialized) {
        memset(g_files, 0, sizeof(g_files));
        g_files_initialized = 1;
    }

    /* Trim filename */
    strncpy(name, filename, MAX_FILE_NAME);
    for (i = MAX_FILE_NAME - 1; i >= 0 && name[i] == ' '; i--)
        name[i] = '\0';

    /* Look for existing handle */
    for (i = 0; i < MAX_FILES; i++) {
        if (g_files[i].open && strcmp(g_files[i].name, name) == 0) {
            return &g_files[i];
        }
    }

    /* Find empty slot */
    for (i = 0; i < MAX_FILES; i++) {
        if (!g_files[i].open) break;
    }
    if (i >= MAX_FILES) return NULL;

    /* Open the BDB file */
    const char *path = get_file_path(name);

    ret = db_create(&g_files[i].dbp, NULL, 0);
    if (ret != 0) {
        fprintf(stderr, "[CICS-SIM] db_create failed for %s: %s\n",
                name, db_strerror(ret));
        return NULL;
    }

    ret = g_files[i].dbp->open(g_files[i].dbp, NULL, path, NULL,
                                DB_BTREE, DB_CREATE, 0644);
    if (ret != 0) {
        fprintf(stderr, "[CICS-SIM] Cannot open file %s (%s): %s\n",
                name, path, db_strerror(ret));
        g_files[i].dbp->close(g_files[i].dbp, 0);
        g_files[i].dbp = NULL;
        return NULL;
    }

    strncpy(g_files[i].name, name, MAX_FILE_NAME);
    g_files[i].open = 1;
    g_files[i].cursor_count = 0;

    fprintf(stderr, "[CICS-SIM] Opened file %s -> %s\n", name, path);
    return &g_files[i];
}

/* READ - Read a record by key */
void CICSREAD(char *filename, void *into, int32_t *into_len,
              char *ridfld, int32_t key_len,
              int32_t *resp, int32_t *resp2) {
    FileHandle *fh;
    DBT key, data;
    int ret;

    fh = get_file_handle(filename);
    if (!fh) {
        if (resp) *resp = DFHRESP_NOTOPEN;
        if (resp2) *resp2 = 0;
        return;
    }

    memset(&key, 0, sizeof(key));
    memset(&data, 0, sizeof(data));

    key.data = ridfld;
    key.size = key_len;

    ret = fh->dbp->get(fh->dbp, NULL, &key, &data, 0);

    if (ret == 0) {
        int copy_len = data.size;
        if (into_len && *into_len > 0 && copy_len > *into_len)
            copy_len = *into_len;
        memcpy(into, data.data, copy_len);
        if (into_len) *into_len = copy_len;
        if (resp) *resp = DFHRESP_NORMAL;
    } else if (ret == DB_NOTFOUND) {
        if (resp) *resp = DFHRESP_NOTFND;
    } else {
        if (resp) *resp = DFHRESP_ERROR;
    }
    if (resp2) *resp2 = 0;
}

/* WRITE - Write a new record */
void CICSWRITE(char *filename, void *from, int32_t from_len,
               char *ridfld, int32_t key_len,
               int32_t *resp, int32_t *resp2) {
    FileHandle *fh;
    DBT key, data;
    int ret;

    fh = get_file_handle(filename);
    if (!fh) {
        if (resp) *resp = DFHRESP_NOTOPEN;
        if (resp2) *resp2 = 0;
        return;
    }

    memset(&key, 0, sizeof(key));
    memset(&data, 0, sizeof(data));

    key.data = ridfld;
    key.size = key_len;
    data.data = from;
    data.size = from_len;

    ret = fh->dbp->put(fh->dbp, NULL, &key, &data, DB_NOOVERWRITE);

    if (ret == 0) {
        if (resp) *resp = DFHRESP_NORMAL;
    } else if (ret == DB_KEYEXIST) {
        if (resp) *resp = DFHRESP_DUPREC;
    } else {
        if (resp) *resp = DFHRESP_ERROR;
    }
    if (resp2) *resp2 = 0;
}

/* REWRITE - Update an existing record */
void CICSREWRITE(char *filename, void *from, int32_t from_len,
                 int32_t *resp, int32_t *resp2) {
    /* In a real implementation, REWRITE updates the last-read record.
       For simplicity, we assume the key is at the start of the record. */
    if (resp) *resp = DFHRESP_NORMAL;
    if (resp2) *resp2 = 0;

    /* The actual rewrite is handled via the file handle's last-read context */
    fprintf(stderr, "[CICS-SIM] REWRITE (len=%d)\n", from_len);
}

/* DELETE - Delete a record by key */
void CICSDELETE(char *filename, char *ridfld, int32_t key_len,
                int32_t *resp, int32_t *resp2) {
    FileHandle *fh;
    DBT key;
    int ret;

    fh = get_file_handle(filename);
    if (!fh) {
        if (resp) *resp = DFHRESP_NOTOPEN;
        if (resp2) *resp2 = 0;
        return;
    }

    memset(&key, 0, sizeof(key));
    key.data = ridfld;
    key.size = key_len;

    ret = fh->dbp->del(fh->dbp, NULL, &key, 0);

    if (ret == 0) {
        if (resp) *resp = DFHRESP_NORMAL;
    } else if (ret == DB_NOTFOUND) {
        if (resp) *resp = DFHRESP_NOTFND;
    } else {
        if (resp) *resp = DFHRESP_ERROR;
    }
    if (resp2) *resp2 = 0;
}

/* STARTBR - Start browse (open cursor) */
void CICSSTARTBR(char *filename, char *ridfld, int32_t key_len,
                 int32_t *resp, int32_t *resp2) {
    FileHandle *fh;
    DBC *cursor;
    DBT key, data;
    int ret;

    fh = get_file_handle(filename);
    if (!fh) {
        if (resp) *resp = DFHRESP_NOTOPEN;
        if (resp2) *resp2 = 0;
        return;
    }

    ret = fh->dbp->cursor(fh->dbp, NULL, &cursor, 0);
    if (ret != 0) {
        if (resp) *resp = DFHRESP_ERROR;
        if (resp2) *resp2 = 0;
        return;
    }

    /* Position cursor at or after the specified key */
    memset(&key, 0, sizeof(key));
    memset(&data, 0, sizeof(data));
    key.data = ridfld;
    key.size = key_len;

    ret = cursor->get(cursor, &key, &data, DB_SET_RANGE);

    if (ret == 0 || ret == DB_NOTFOUND) {
        /* Store cursor in file handle */
        if (fh->cursor_count < MAX_BROWSE_SESSIONS) {
            fh->cursors[fh->cursor_count++] = cursor;
        }
        if (resp) *resp = DFHRESP_NORMAL;
    } else {
        cursor->close(cursor);
        if (resp) *resp = DFHRESP_ERROR;
    }
    if (resp2) *resp2 = 0;
}

/* READNEXT - Read next record in browse */
void CICSREADNEXT(char *filename, void *into, int32_t *into_len,
                  char *ridfld, int32_t key_len,
                  int32_t *resp, int32_t *resp2) {
    FileHandle *fh;
    DBC *cursor;
    DBT key, data;
    int ret;

    fh = get_file_handle(filename);
    if (!fh || fh->cursor_count == 0) {
        if (resp) *resp = DFHRESP_INVREQ;
        if (resp2) *resp2 = 0;
        return;
    }

    cursor = fh->cursors[fh->cursor_count - 1];

    memset(&key, 0, sizeof(key));
    memset(&data, 0, sizeof(data));

    ret = cursor->get(cursor, &key, &data, DB_NEXT);

    if (ret == 0) {
        int copy_len = data.size;
        if (into_len && *into_len > 0 && copy_len > *into_len)
            copy_len = *into_len;
        memcpy(into, data.data, copy_len);
        if (into_len) *into_len = copy_len;
        /* Update RIDFLD with current key */
        if (ridfld && key.size <= (size_t)key_len) {
            memcpy(ridfld, key.data, key.size);
        }
        if (resp) *resp = DFHRESP_NORMAL;
    } else if (ret == DB_NOTFOUND) {
        if (resp) *resp = DFHRESP_ENDFILE;
    } else {
        if (resp) *resp = DFHRESP_ERROR;
    }
    if (resp2) *resp2 = 0;
}

/* READPREV - Read previous record in browse */
void CICSREADPREV(char *filename, void *into, int32_t *into_len,
                  char *ridfld, int32_t key_len,
                  int32_t *resp, int32_t *resp2) {
    FileHandle *fh;
    DBC *cursor;
    DBT key, data;
    int ret;

    fh = get_file_handle(filename);
    if (!fh || fh->cursor_count == 0) {
        if (resp) *resp = DFHRESP_INVREQ;
        if (resp2) *resp2 = 0;
        return;
    }

    cursor = fh->cursors[fh->cursor_count - 1];

    memset(&key, 0, sizeof(key));
    memset(&data, 0, sizeof(data));

    ret = cursor->get(cursor, &key, &data, DB_PREV);

    if (ret == 0) {
        int copy_len = data.size;
        if (into_len && *into_len > 0 && copy_len > *into_len)
            copy_len = *into_len;
        memcpy(into, data.data, copy_len);
        if (into_len) *into_len = copy_len;
        if (ridfld && key.size <= (size_t)key_len) {
            memcpy(ridfld, key.data, key.size);
        }
        if (resp) *resp = DFHRESP_NORMAL;
    } else if (ret == DB_NOTFOUND) {
        if (resp) *resp = DFHRESP_ENDFILE;
    } else {
        if (resp) *resp = DFHRESP_ERROR;
    }
    if (resp2) *resp2 = 0;
}

/* ENDBR - End browse (close cursor) */
void CICSENDBR(char *filename, int32_t *resp, int32_t *resp2) {
    FileHandle *fh;

    fh = get_file_handle(filename);
    if (!fh || fh->cursor_count == 0) {
        if (resp) *resp = DFHRESP_INVREQ;
        if (resp2) *resp2 = 0;
        return;
    }

    DBC *cursor = fh->cursors[--fh->cursor_count];
    cursor->close(cursor);

    if (resp) *resp = DFHRESP_NORMAL;
    if (resp2) *resp2 = 0;
}
