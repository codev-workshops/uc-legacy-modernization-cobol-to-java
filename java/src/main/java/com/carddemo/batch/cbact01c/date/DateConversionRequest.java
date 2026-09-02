package com.carddemo.batch.cbact01c.date;

/**
 * Mirrors {@code CODATECN-IN-REC} ({@code CODATECN-TYPE} PIC X + {@code CODATECN-INP-DATE} PIC X(20))
 * plus {@code CODATECN-OUTTYPE} PIC X.
 *
 * <p>{@code inputDate} is normalised to exactly {@value #INPUT_DATE_LENGTH} characters the way a
 * COBOL {@code MOVE} into a PIC X(20) field behaves: shorter values are right-padded with spaces,
 * longer values are truncated on the right. A {@code null} input is treated as all spaces.
 */
public record DateConversionRequest(char inType, char outType, String inputDate) {

    public static final int INPUT_DATE_LENGTH = 20;

    public DateConversionRequest {
        inputDate = FixedWidth.fit(inputDate, INPUT_DATE_LENGTH, ' ');
    }
}
