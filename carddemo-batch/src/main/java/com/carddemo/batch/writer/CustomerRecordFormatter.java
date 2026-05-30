package com.carddemo.batch.writer;

import com.carddemo.common.entity.Customer;

/**
 * Formats a {@link Customer} entity into the fixed-width 500-character record
 * layout defined by the COBOL copybook CVCUS01Y.cpy.
 *
 * <pre>
 * Field                  PIC          Len  Offset
 * CUST-ID                9(09)          9       0
 * CUST-FIRST-NAME        X(25)         25       9
 * CUST-MIDDLE-NAME       X(25)         25      34
 * CUST-LAST-NAME         X(25)         25      59
 * CUST-ADDR-LINE-1       X(50)         50      84
 * CUST-ADDR-LINE-2       X(50)         50     134
 * CUST-ADDR-LINE-3       X(50)         50     184
 * CUST-ADDR-STATE-CD     X(02)          2     234
 * CUST-ADDR-COUNTRY-CD   X(03)          3     236
 * CUST-ADDR-ZIP          X(10)         10     239
 * CUST-PHONE-NUM-1       X(15)         15     249
 * CUST-PHONE-NUM-2       X(15)         15     264
 * CUST-SSN               9(09)          9     279
 * CUST-GOVT-ISSUED-ID    X(20)         20     288
 * CUST-DOB-YYYY-MM-DD    X(10)         10     308
 * CUST-EFT-ACCOUNT-ID    X(10)         10     318
 * CUST-PRI-CARD-HOLDER   X(01)          1     328
 * CUST-FICO-CREDIT-SCORE 9(03)          3     329
 * FILLER                 X(168)       168     332
 * </pre>
 */
public final class CustomerRecordFormatter {

    public static final int RECORD_LENGTH = 500;

    private CustomerRecordFormatter() {}

    public static String format(Customer c) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(padNumeric(c.getCustId(), 9));
        sb.append(padAlpha(c.getFirstName(), 25));
        sb.append(padAlpha(c.getMiddleName(), 25));
        sb.append(padAlpha(c.getLastName(), 25));
        sb.append(padAlpha(c.getAddrLine1(), 50));
        sb.append(padAlpha(c.getAddrLine2(), 50));
        sb.append(padAlpha(c.getAddrLine3(), 50));
        sb.append(padAlpha(c.getStateCode(), 2));
        sb.append(padAlpha(c.getCountryCode(), 3));
        sb.append(padAlpha(c.getZip(), 10));
        sb.append(padAlpha(c.getPhone1(), 15));
        sb.append(padAlpha(c.getPhone2(), 15));
        sb.append(padNumeric(c.getSsn(), 9));
        sb.append(padAlpha(c.getGovtIssuedId(), 20));
        sb.append(padAlpha(c.getDob(), 10));
        sb.append(padAlpha(c.getEftAccountId(), 10));
        sb.append(padAlpha(c.getPriCardHolderInd(), 1));
        sb.append(padNumeric(c.getFicoCreditScore(), 3));
        sb.append(padAlpha(null, 168)); // FILLER
        return sb.toString();
    }

    static String padAlpha(String value, int length) {
        if (value == null) {
            return " ".repeat(length);
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return value + " ".repeat(length - value.length());
    }

    static String padNumeric(Number value, int length) {
        long v = (value != null) ? value.longValue() : 0;
        String s = String.valueOf(Math.abs(v));
        if (s.length() >= length) {
            return s.substring(s.length() - length);
        }
        return "0".repeat(length - s.length()) + s;
    }
}
