package com.carddemo.batch.util;

/**
 * Replaces the COBDATFT assembler routine called from COBOL.
 * Converts dates between formats:
 *   Type "1" input: YYYYMMDD
 *   Type "2" input: YYYY-MM-DD
 *   Type "1" output: YYYY-MM-DD
 *   Type "2" output: YYYYMMDD
 *
 * In CBACT01C, the call uses type='2' for both input and output,
 * meaning it takes YYYY-MM-DD input and produces YYYYMMDD output.
 */
public final class DateFormatter {

    private DateFormatter() {
    }

    /**
     * Converts a date string based on input and output type codes.
     *
     * @param inputDate the input date string
     * @param inputType "1" for YYYYMMDD, "2" for YYYY-MM-DD
     * @param outputType "1" for YYYY-MM-DD, "2" for YYYYMMDD
     * @return the formatted date string
     */
    public static String formatDate(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return "";
        }

        String yyyy;
        String mm;
        String dd;

        if ("1".equals(inputType)) {
            // Input is YYYYMMDD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(4, 6);
            dd = inputDate.substring(6, 8);
        } else if ("2".equals(inputType)) {
            // Input is YYYY-MM-DD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(5, 7);
            dd = inputDate.substring(8, 10);
        } else {
            throw new IllegalArgumentException("Unknown input type: " + inputType);
        }

        if ("1".equals(outputType)) {
            // Output as YYYY-MM-DD
            return yyyy + "-" + mm + "-" + dd;
        } else if ("2".equals(outputType)) {
            // Output as YYYYMMDD
            return yyyy + mm + dd;
        } else {
            throw new IllegalArgumentException("Unknown output type: " + outputType);
        }
    }
}
