package com.carddemo.batch.cbact01c;

/**
 * Replicates the COBDATFT CALL behavior for date formatting.
 * Input type '2' = YYYY-MM-DD (with hyphens)
 * Output type '2' = YYYYMMDD (no hyphens)
 */
public final class DateFormatter {

    private DateFormatter() {}

    /**
     * Converts a date from YYYY-MM-DD format to YYYYMMDD format by stripping hyphens.
     *
     * @param yyyyMmDd date string in YYYY-MM-DD format
     * @return date string in YYYYMMDD format
     */
    public static String formatDate(String yyyyMmDd) {
        if (yyyyMmDd == null) {
            return "";
        }
        return yyyyMmDd.replace("-", "");
    }
}
