package com.carddemo.batch.cbact01c.date;

/**
 * Mirrors {@code CODATECN-0UT-DATE} PIC X(20) and {@code CODATECN-ERROR-MSG} PIC X(38).
 *
 * <p>Both components are always exactly their field width; constructor arguments are
 * space-padded / truncated to guarantee the invariant.
 */
public record DateConversionResponse(String outputDate, String errorMessage) {

    public static final int OUTPUT_DATE_LENGTH = 20;
    public static final int ERROR_MESSAGE_LENGTH = 38;

    public DateConversionResponse {
        outputDate = FixedWidth.fit(outputDate, OUTPUT_DATE_LENGTH, ' ');
        errorMessage = FixedWidth.fit(errorMessage, ERROR_MESSAGE_LENGTH, ' ');
    }

    /** {@code true} when {@code CODATECN-ERROR-MSG} is not all spaces. */
    public boolean isError() {
        return !FixedWidth.isAll(errorMessage, ' ');
    }

    /** {@code CODATECN-0UT-DATE(1:10)}, the value CBACT01C moves to {@code OUT-ACCT-REISSUE-DATE}. */
    public String outputDate10() {
        return outputDate.substring(0, 10);
    }
}
