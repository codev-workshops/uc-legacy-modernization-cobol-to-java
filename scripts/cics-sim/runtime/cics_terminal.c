/*
 * cics_terminal.c - CICS Terminal I/O Simulator (ncurses-based 3270)
 *
 * Provides a text-mode simulation of IBM 3270 terminal handling,
 * implementing SEND MAP, RECEIVE MAP, and SEND TEXT commands.
 *
 * The terminal uses ncurses to render BMS maps on a standard terminal,
 * supporting field-based input/output similar to a real 3270 display.
 *
 * When ncurses is not available or output is redirected, falls back
 * to a simple line-mode interface.
 */

#include "cics_runtime.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#ifdef USE_NCURSES
#include <ncurses.h>
static int g_ncurses_initialized = 0;
#endif

/* Map registry - loaded at startup from generated map definitions */
#define MAX_MAPS 50
static MapDef g_maps[MAX_MAPS];
static int g_map_count = 0;

/* Current screen state */
static char g_screen_buf[MAX_SCREEN_ROWS][MAX_SCREEN_COLS + 1];
static char g_input_buf[MAX_SCREEN_ROWS][MAX_SCREEN_COLS + 1];
static int g_cursor_row = 0;
static int g_cursor_col = 0;

/* Forward declarations */
static MapDef* find_map(const char *mapset, const char *map);
static void render_map_text(MapDef *md, void *data_area);
static void collect_input_text(MapDef *md, void *data_area, int32_t area_len);

#ifdef USE_NCURSES
static void init_ncurses(void);
static void render_map_ncurses(MapDef *md, void *data_area);
static void collect_input_ncurses(MapDef *md, void *data_area, int32_t area_len);
#endif

/* Register a map definition (called during initialization) */
void cics_register_map(const char *mapset, const char *map,
                       int rows, int cols,
                       MapField *fields, int field_count) {
    if (g_map_count >= MAX_MAPS) return;

    MapDef *md = &g_maps[g_map_count];
    strncpy(md->mapset_name, mapset, 7);
    strncpy(md->map_name, map, 7);
    md->rows = rows;
    md->cols = cols;
    md->field_count = field_count;
    if (field_count > 0 && fields) {
        int count = (field_count > 100) ? 100 : field_count;
        memcpy(md->fields, fields, count * sizeof(MapField));
        md->field_count = count;
    }
    g_map_count++;
}

/* Find a registered map */
static MapDef* find_map(const char *mapset, const char *map) {
    int i;
    char ms[8] = {0}, mn[8] = {0};

    /* Trim and uppercase */
    if (mapset) {
        strncpy(ms, mapset, 7);
        for (i = 6; i >= 0 && ms[i] == ' '; i--) ms[i] = '\0';
    }
    if (map) {
        strncpy(mn, map, 7);
        for (i = 6; i >= 0 && mn[i] == ' '; i--) mn[i] = '\0';
    }

    for (i = 0; i < g_map_count; i++) {
        if (strncmp(g_maps[i].mapset_name, ms, 7) == 0 &&
            strncmp(g_maps[i].map_name, mn, 7) == 0) {
            return &g_maps[i];
        }
    }

    fprintf(stderr, "[CICS-SIM] Map not found: mapset=%s map=%s\n", ms, mn);
    return NULL;
}

/* SEND MAP - Display a BMS map */
void CICSSENDMAP(char *mapset, char *map, void *from_area,
                 int32_t from_len, int32_t *resp, int32_t *resp2,
                 int32_t erase, int32_t cursor_pos) {
    MapDef *md;

    md = find_map(mapset, map);

    if (erase) {
        memset(g_screen_buf, ' ', sizeof(g_screen_buf));
        for (int i = 0; i < MAX_SCREEN_ROWS; i++)
            g_screen_buf[i][MAX_SCREEN_COLS] = '\0';
    }

    if (!md) {
        /* No map definition loaded - display raw data */
        fprintf(stderr, "[CICS-SIM] SEND MAP: mapset=%.*s map=%.*s (no def loaded)\n",
                7, mapset ? mapset : "?", 7, map ? map : "?");
        if (from_area && from_len > 0) {
            printf("\n--- Screen Output (raw) ---\n");
            char *p = (char *)from_area;
            int i;
            for (i = 0; i < from_len && i < 1920; i++) {
                char c = p[i];
                if (c >= 0x20 && c <= 0x7E)
                    putchar(c);
                else if (c == '\0' || c == ' ')
                    putchar(' ');
                else
                    putchar('.');
                if ((i + 1) % 80 == 0) putchar('\n');
            }
            printf("\n--- End Screen ---\n");
        }
        if (resp) *resp = DFHRESP_NORMAL;
        if (resp2) *resp2 = 0;
        return;
    }

#ifdef USE_NCURSES
    if (g_ncurses_initialized) {
        render_map_ncurses(md, from_area);
    } else {
        render_map_text(md, from_area);
    }
#else
    render_map_text(md, from_area);
#endif

    if (resp) *resp = DFHRESP_NORMAL;
    if (resp2) *resp2 = 0;
}

/* Render map in text mode (line-based) */
static void render_map_text(MapDef *md, void *data_area) {
    int i;
    char line[MAX_SCREEN_COLS + 1];

    printf("\n");
    printf("+");
    for (i = 0; i < 80; i++) printf("-");
    printf("+\n");

    /* Clear screen buffer */
    for (i = 0; i < MAX_SCREEN_ROWS; i++)
        memset(g_screen_buf[i], ' ', MAX_SCREEN_COLS);

    /* Place field values from data area */
    if (data_area) {
        /* The data area follows BMS conventions:
           For each field: 2-byte length (halfword) + 1-byte flag + 1-byte attr + data
           We'll display based on field positions */
        char *p = (char *)data_area;
        int offset = 12; /* Skip TIOAPFX (12 bytes) */

        for (i = 0; i < md->field_count; i++) {
            MapField *f = &md->fields[i];
            if (f->row <= 0 || f->row > MAX_SCREEN_ROWS) continue;
            if (f->col <= 0 || f->col > MAX_SCREEN_COLS) continue;

            /* Check if there's data in the output area for this field */
            int fld_offset = offset;
            offset += 2 + 1 + 1 + f->length; /* len + flag + attr + data */

            if (fld_offset + 4 + f->length <= (int)(data_area ? 4096 : 0)) {
                char *fld_data = p + fld_offset + 4; /* skip len+flag+attr */
                int row = f->row - 1;
                int col = f->col - 1;
                int len = f->length;
                if (col + len > MAX_SCREEN_COLS) len = MAX_SCREEN_COLS - col;

                /* Place on screen */
                for (int j = 0; j < len && j < f->length; j++) {
                    char c = fld_data[j];
                    if (c >= 0x20 && c <= 0x7E)
                        g_screen_buf[row][col + j] = c;
                }
            }

            /* Also place initial values for fields with INITIAL */
            if (f->initial[0] != '\0') {
                int row = f->row - 1;
                int col = f->col - 1;
                int len = strlen(f->initial);
                if (col + len > MAX_SCREEN_COLS) len = MAX_SCREEN_COLS - col;
                for (int j = 0; j < len; j++) {
                    if (g_screen_buf[row][col + j] == ' ')
                        g_screen_buf[row][col + j] = f->initial[j];
                }
            }
        }
    }

    /* Render screen */
    for (i = 0; i < MAX_SCREEN_ROWS; i++) {
        memcpy(line, g_screen_buf[i], MAX_SCREEN_COLS);
        line[MAX_SCREEN_COLS] = '\0';
        /* Trim trailing spaces */
        int len = MAX_SCREEN_COLS;
        while (len > 0 && line[len - 1] == ' ') len--;
        line[len] = '\0';
        printf("|%-80s|\n", line);
    }

    printf("+");
    for (i = 0; i < 80; i++) printf("-");
    printf("+\n");
    fflush(stdout);
}

/* RECEIVE MAP - Get input from terminal */
void CICSRECEIVEMAP(char *mapset, char *map, void *into_area,
                    int32_t into_len, int32_t *resp, int32_t *resp2) {
    MapDef *md;
    CICSState *state = cics_get_state();

    md = find_map(mapset, map);

#ifdef USE_NCURSES
    if (g_ncurses_initialized && md) {
        collect_input_ncurses(md, into_area, into_len);
        if (resp) *resp = DFHRESP_NORMAL;
        if (resp2) *resp2 = 0;
        return;
    }
#endif

    /* Text-mode input collection */
    collect_input_text(md, into_area, into_len);

    if (resp) *resp = DFHRESP_NORMAL;
    if (resp2) *resp2 = 0;
}

/* Collect input in text mode */
static void collect_input_text(MapDef *md, void *data_area, int32_t area_len) {
    CICSState *state = cics_get_state();
    char input_line[256];

    if (!md) {
        /* No map - just read a line */
        printf("Input> ");
        fflush(stdout);
        if (fgets(input_line, sizeof(input_line), stdin) == NULL) {
            state->eib.EIBAID = DFHPF3; /* EOF = PF3 (exit) */
            return;
        }
        state->eib.EIBAID = DFHENTER;
        return;
    }

    /* Prompt for each input field */
    printf("\n--- Input Fields ---\n");
    printf("(Press Enter to skip, type 'PF3' to exit, 'PF7' for prev, 'PF8' for next)\n\n");

    if (data_area) {
        memset(data_area, 0, area_len > 0 ? area_len : MAX_MAP_SIZE);
    }

    /* The BMS output/input area structure:
       12-byte TIOAPFX
       For each field: 2-byte length (INT16) + 1-byte flag + 1-byte attr + N-byte data */
    char *p = (char *)data_area;
    int offset = 12; /* Skip TIOAPFX */

    int got_pfkey = 0;

    for (int i = 0; i < md->field_count && !got_pfkey; i++) {
        MapField *f = &md->fields[i];
        if (!f->is_input) {
            /* Output-only field - skip in area */
            offset += 2 + 1 + 1 + f->length;
            continue;
        }

        printf("  %s [%d chars]: ", f->name, f->length);
        fflush(stdout);

        if (fgets(input_line, sizeof(input_line), stdin) == NULL) {
            state->eib.EIBAID = DFHPF3;
            got_pfkey = 1;
            break;
        }

        /* Remove trailing newline */
        int len = strlen(input_line);
        if (len > 0 && input_line[len - 1] == '\n') input_line[--len] = '\0';

        /* Check for PF key commands */
        if (strcasecmp(input_line, "PF3") == 0 || strcasecmp(input_line, "EXIT") == 0) {
            state->eib.EIBAID = DFHPF3;
            got_pfkey = 1;
            break;
        } else if (strcasecmp(input_line, "PF7") == 0 || strcasecmp(input_line, "UP") == 0) {
            state->eib.EIBAID = DFHPF7;
            got_pfkey = 1;
            break;
        } else if (strcasecmp(input_line, "PF8") == 0 || strcasecmp(input_line, "DOWN") == 0) {
            state->eib.EIBAID = DFHPF8;
            got_pfkey = 1;
            break;
        } else if (strcasecmp(input_line, "CLEAR") == 0) {
            state->eib.EIBAID = DFHCLEAR;
            got_pfkey = 1;
            break;
        }

        /* Store field data in BMS area */
        if (p && offset + 4 + f->length <= area_len) {
            int16_t fld_len = (int16_t)len;
            memcpy(p + offset, &fld_len, 2);          /* Length */
            p[offset + 2] = (len > 0) ? 0x80 : 0x00; /* Modified flag */
            p[offset + 3] = 0x00;                     /* Attribute */
            if (len > 0) {
                int copy_len = (len > f->length) ? f->length : len;
                memcpy(p + offset + 4, input_line, copy_len);
            }
        }
        offset += 2 + 1 + 1 + f->length;
    }

    if (!got_pfkey) {
        state->eib.EIBAID = DFHENTER;
    }
}

/* SEND TEXT - Send plain text to terminal */
void CICSSENDTEXT(char *text, int32_t text_len, int32_t erase, int32_t freekb) {
    if (erase) {
        printf("\033[2J\033[H"); /* ANSI clear screen */
    }

    if (text && text_len > 0) {
        printf("\n%.*s\n", text_len, text);
    }
    fflush(stdout);
}

#ifdef USE_NCURSES
static void init_ncurses(void) {
    if (g_ncurses_initialized) return;

    initscr();
    cbreak();
    noecho();
    keypad(stdscr, TRUE);

    if (has_colors()) {
        start_color();
        init_pair(1, COLOR_BLUE, COLOR_BLACK);
        init_pair(2, COLOR_GREEN, COLOR_BLACK);
        init_pair(3, COLOR_RED, COLOR_BLACK);
        init_pair(4, COLOR_YELLOW, COLOR_BLACK);
        init_pair(5, COLOR_WHITE, COLOR_BLACK);
    }

    g_ncurses_initialized = 1;
}

static void render_map_ncurses(MapDef *md, void *data_area) {
    int i;
    clear();

    /* Render fields */
    char *p = (char *)data_area;
    int offset = 12; /* Skip TIOAPFX */

    for (i = 0; i < md->field_count; i++) {
        MapField *f = &md->fields[i];
        int row = f->row - 1;
        int col = f->col - 1;

        if (row < 0 || row >= MAX_SCREEN_ROWS) continue;
        if (col < 0 || col >= MAX_SCREEN_COLS) continue;

        /* Set color */
        int color_pair = 5;
        if (strcmp(f->color, "BLUE") == 0) color_pair = 1;
        else if (strcmp(f->color, "GREEN") == 0) color_pair = 2;
        else if (strcmp(f->color, "RED") == 0) color_pair = 3;
        else if (strcmp(f->color, "YELLOW") == 0) color_pair = 4;

        attron(COLOR_PAIR(color_pair));

        /* Display initial or data */
        if (f->initial[0] != '\0') {
            mvprintw(row, col, "%s", f->initial);
        }

        if (p && offset + 4 + f->length <= 4096) {
            char *fld_data = p + offset + 4;
            int has_data = 0;
            for (int j = 0; j < f->length; j++) {
                if (fld_data[j] != '\0' && fld_data[j] != ' ') {
                    has_data = 1;
                    break;
                }
            }
            if (has_data) {
                mvprintw(row, col, "%.*s", f->length, fld_data);
            }
        }
        offset += 2 + 1 + 1 + f->length;

        attroff(COLOR_PAIR(color_pair));

        /* Underline input fields */
        if (f->is_input) {
            attron(A_UNDERLINE);
            for (int j = 0; j < f->length; j++) {
                if (mvinch(row, col + j) == ' ')
                    mvaddch(row, col + j, '_');
            }
            attroff(A_UNDERLINE);
        }
    }

    /* Status line */
    mvprintw(23, 0, " PF3=Exit  PF7=Prev  PF8=Next  ENTER=Submit");

    refresh();
}

static void collect_input_ncurses(MapDef *md, void *data_area, int32_t area_len) {
    CICSState *state = cics_get_state();
    int ch;
    int current_field = 0;
    int cursor_pos = 0;
    char field_bufs[100][MAX_FIELD_SIZE];

    memset(field_bufs, 0, sizeof(field_bufs));
    if (data_area) memset(data_area, 0, area_len);

    /* Find first input field */
    for (current_field = 0; current_field < md->field_count; current_field++) {
        if (md->fields[current_field].is_input) break;
    }

    if (current_field >= md->field_count) {
        /* No input fields - just wait for a key */
        ch = getch();
        if (ch == KEY_F(3)) state->eib.EIBAID = DFHPF3;
        else state->eib.EIBAID = DFHENTER;
        return;
    }

    /* Position cursor at first input field */
    MapField *f = &md->fields[current_field];
    move(f->row - 1, f->col - 1);
    refresh();

    while (1) {
        ch = getch();

        if (ch == KEY_F(3)) {
            state->eib.EIBAID = DFHPF3;
            break;
        } else if (ch == KEY_F(7)) {
            state->eib.EIBAID = DFHPF7;
            break;
        } else if (ch == KEY_F(8)) {
            state->eib.EIBAID = DFHPF8;
            break;
        } else if (ch == '\n' || ch == KEY_ENTER) {
            state->eib.EIBAID = DFHENTER;
            break;
        } else if (ch == '\t' || ch == KEY_DOWN) {
            /* Move to next input field */
            int next = current_field + 1;
            while (next < md->field_count && !md->fields[next].is_input)
                next++;
            if (next < md->field_count) {
                current_field = next;
                cursor_pos = 0;
                f = &md->fields[current_field];
                move(f->row - 1, f->col - 1);
                refresh();
            }
        } else if (ch == KEY_BACKSPACE || ch == 127) {
            if (cursor_pos > 0) {
                cursor_pos--;
                field_bufs[current_field][cursor_pos] = '\0';
                f = &md->fields[current_field];
                mvaddch(f->row - 1, f->col - 1 + cursor_pos, '_');
                move(f->row - 1, f->col - 1 + cursor_pos);
                refresh();
            }
        } else if (ch >= 32 && ch <= 126) {
            f = &md->fields[current_field];
            if (cursor_pos < f->length) {
                field_bufs[current_field][cursor_pos] = (char)ch;
                mvaddch(f->row - 1, f->col - 1 + cursor_pos, ch);
                cursor_pos++;
                refresh();
            }
        }
    }

    /* Pack field data into BMS area */
    char *p = (char *)data_area;
    int offset = 12;

    for (int i = 0; i < md->field_count; i++) {
        f = &md->fields[i];
        if (f->is_input && p && offset + 4 + f->length <= area_len) {
            int len = strlen(field_bufs[i]);
            int16_t fld_len = (int16_t)len;
            memcpy(p + offset, &fld_len, 2);
            p[offset + 2] = (len > 0) ? 0x80 : 0x00;
            p[offset + 3] = 0x00;
            if (len > 0) {
                int copy_len = (len > f->length) ? f->length : len;
                memcpy(p + offset + 4, field_bufs[i], copy_len);
            }
        }
        offset += 2 + 1 + 1 + f->length;
    }
}
#endif /* USE_NCURSES */
