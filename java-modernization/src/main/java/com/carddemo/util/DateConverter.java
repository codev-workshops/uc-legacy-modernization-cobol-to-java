package com.carddemo.util;

/**
 * Replaces the COBDATFT assembler program called via CODATECN copybook.
 * Converts between date formats:
 * <ul>
 *   <li>Type 1 input: YYYYMMDD</li>
 *   <li>Type 2 input: YYYY-MM-DD</li>
 *   <li>Type 1 output: YYYY-MM-DD</li>
 *   <li>Type 2 output: YYYYMMDD</li>
 * </ul>
 */
public final class DateConverter {

    private DateConverter() {
    }

    public static String convert(String inputDate, int inputType, int outputType) {
        String yyyy;
        String mm;
        String dd;

        if (inputType == 1) {
            // YYYYMMDD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(4, 6);
            dd = inputDate.substring(6, 8);
        } else if (inputType == 2) {
            // YYYY-MM-DD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(5, 7);
            dd = inputDate.substring(8, 10);
        } else {
            throw new IllegalArgumentException("Unknown input type: " + inputType);
        }

        if (outputType == 1) {
            return yyyy + "-" + mm + "-" + dd;
        } else if (outputType == 2) {
            return yyyy + mm + dd;
        } else {
            throw new IllegalArgumentException("Unknown output type: " + outputType);
        }
    }
}
