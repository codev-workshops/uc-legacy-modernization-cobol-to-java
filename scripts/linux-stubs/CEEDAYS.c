/*
 * CEEDAYS.c — Linux replacement for the z/OS LE CEEDAYS routine.
 *
 * CEEDAYS converts a date string to a Lilian date (days since Oct 15, 1582).
 *
 * Called as: CALL "CEEDAYS" USING date-string, format-string,
 *                                 lilian-output, feedback-code.
 *
 * Parameters are COBOL varying-length strings (2-byte length prefix).
 *
 * Build:
 *   gcc -shared -fPIC -o CEEDAYS.so CEEDAYS.c
 *
 * Copyright Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0.
 */

#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <time.h>

/* Lilian date: days since October 15, 1582 (Gregorian calendar start) */
static int32_t to_lilian(int year, int month, int day) {
    struct tm ref = {0}, target = {0};
    ref.tm_year = 1582 - 1900;
    ref.tm_mon  = 10 - 1;  /* October */
    ref.tm_mday = 15;

    target.tm_year = year - 1900;
    target.tm_mon  = month - 1;
    target.tm_mday = day;

    time_t t_ref    = mktime(&ref);
    time_t t_target = mktime(&target);

    return (int32_t)((t_target - t_ref) / 86400);
}

void CEEDAYS(void *date_str, void *format_str, int32_t *lilian, int32_t *fc) {
    /* date_str and format_str are COBOL varying-length strings:
       2-byte signed binary length + character data */
    int16_t date_len = *(int16_t *)date_str;
    char *date_data  = (char *)date_str + 2;

    int16_t fmt_len  = *(int16_t *)format_str;
    char *fmt_data   = (char *)format_str + 2;

    /* Default: assume YYYYMMDD or YYYY-MM-DD format */
    int year = 0, month = 0, day = 0;

    if (date_len >= 10 && date_data[4] == '-') {
        /* YYYY-MM-DD */
        char buf[11];
        memcpy(buf, date_data, 10);
        buf[10] = '\0';
        sscanf(buf, "%d-%d-%d", &year, &month, &day);
    } else if (date_len >= 8) {
        /* YYYYMMDD */
        char buf[9];
        memcpy(buf, date_data, 8);
        buf[8] = '\0';
        year  = (buf[0]-'0')*1000 + (buf[1]-'0')*100 + (buf[2]-'0')*10 + (buf[3]-'0');
        month = (buf[4]-'0')*10 + (buf[5]-'0');
        day   = (buf[6]-'0')*10 + (buf[7]-'0');
    }

    if (year > 0 && month > 0 && day > 0) {
        *lilian = to_lilian(year, month, day);
        if (fc) *fc = 0;  /* success */
    } else {
        *lilian = 0;
        if (fc) *fc = 1;  /* error */
    }
}
