package com.mainframe.carddemo.common.util;

/**
 * Maps all 9 COBOL CEEDAYS feedback codes from CSUTLDTC.cbl (lines 62-70).
 */
public enum DateFeedbackCode {

    FC_INVALID_DATE(0, "Date is valid"),
    FC_INSUFFICIENT_DATA(1, "Insufficient data"),
    FC_BAD_DATE_VALUE(2, "Bad date value"),
    FC_INVALID_ERA(3, "Invalid era"),
    FC_UNSUPP_RANGE(4, "Unsupported range"),
    FC_INVALID_MONTH(5, "Invalid month"),
    FC_BAD_PIC_STRING(6, "Bad picture string"),
    FC_NON_NUMERIC_DATA(7, "Non-numeric data in date"),
    FC_YEAR_IN_ERA_ZERO(8, "Year in era zero");

    private final int code;
    private final String message;

    DateFeedbackCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
