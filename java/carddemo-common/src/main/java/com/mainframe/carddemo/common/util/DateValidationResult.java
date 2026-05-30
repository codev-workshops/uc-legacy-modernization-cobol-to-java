package com.mainframe.carddemo.common.util;

/**
 * Result of a date validation, mirroring the COBOL CEEDAYS feedback structure.
 */
public record DateValidationResult(
        int severityCode,
        DateFeedbackCode feedbackCode,
        String message,
        boolean isValid
) {

    public static DateValidationResult valid() {
        return new DateValidationResult(0, DateFeedbackCode.FC_INVALID_DATE,
                DateFeedbackCode.FC_INVALID_DATE.getMessage(), true);
    }

    public static DateValidationResult invalid(DateFeedbackCode feedbackCode) {
        return new DateValidationResult(feedbackCode.getCode(), feedbackCode,
                feedbackCode.getMessage(), false);
    }
}
