package com.mainframe.carddemo.migration.parser;

import com.mainframe.carddemo.migration.parser.FixedWidthField.FieldType;

import java.util.List;

/**
 * Record layouts derived from COBOL copybooks in app/cpy/.
 */
public final class RecordLayouts {

    private RecordLayouts() {}

    /** CVACT01Y.cpy — ACCOUNT-RECORD, 300 bytes */
    public static List<FixedWidthField> accountLayout() {
        return List.of(
                new FixedWidthField("ACCT-ID", 0, 11, FieldType.NUMERIC),
                new FixedWidthField("ACCT-ACTIVE-STATUS", 11, 1, FieldType.ALPHANUMERIC),
                new FixedWidthField("ACCT-CURR-BAL", 12, 12, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("ACCT-CREDIT-LIMIT", 24, 12, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("ACCT-CASH-CREDIT-LIMIT", 36, 12, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("ACCT-OPEN-DATE", 48, 10, FieldType.DATE),
                new FixedWidthField("ACCT-EXPIRAION-DATE", 58, 10, FieldType.DATE),
                new FixedWidthField("ACCT-REISSUE-DATE", 68, 10, FieldType.DATE),
                new FixedWidthField("ACCT-CURR-CYC-CREDIT", 78, 12, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("ACCT-CURR-CYC-DEBIT", 90, 12, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("ACCT-ADDR-ZIP", 102, 10, FieldType.ALPHANUMERIC),
                new FixedWidthField("ACCT-GROUP-ID", 112, 10, FieldType.ALPHANUMERIC),
                new FixedWidthField("FILLER", 122, 178, FieldType.FILLER));
    }

    /** CVCUS01Y.cpy — CUSTOMER-RECORD, 500 bytes */
    public static List<FixedWidthField> customerLayout() {
        return List.of(
                new FixedWidthField("CUST-ID", 0, 9, FieldType.NUMERIC),
                new FixedWidthField("CUST-FIRST-NAME", 9, 25, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-MIDDLE-NAME", 34, 25, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-LAST-NAME", 59, 25, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-ADDR-LINE-1", 84, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-ADDR-LINE-2", 134, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-ADDR-LINE-3", 184, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-ADDR-STATE-CD", 234, 2, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-ADDR-COUNTRY-CD", 236, 3, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-ADDR-ZIP", 239, 10, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-PHONE-NUM-1", 249, 15, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-PHONE-NUM-2", 264, 15, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-SSN", 279, 9, FieldType.NUMERIC),
                new FixedWidthField("CUST-GOVT-ISSUED-ID", 288, 20, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-DOB-YYYY-MM-DD", 308, 10, FieldType.DATE),
                new FixedWidthField("CUST-EFT-ACCOUNT-ID", 318, 10, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-PRI-CARD-HOLDER-IND", 328, 1, FieldType.ALPHANUMERIC),
                new FixedWidthField("CUST-FICO-CREDIT-SCORE", 329, 3, FieldType.NUMERIC),
                new FixedWidthField("FILLER", 332, 168, FieldType.FILLER));
    }

    /** CVACT02Y.cpy — CARD-RECORD, 150 bytes */
    public static List<FixedWidthField> cardLayout() {
        return List.of(
                new FixedWidthField("CARD-NUM", 0, 16, FieldType.ALPHANUMERIC),
                new FixedWidthField("CARD-ACCT-ID", 16, 11, FieldType.NUMERIC),
                new FixedWidthField("CARD-CVV-CD", 27, 3, FieldType.NUMERIC),
                new FixedWidthField("CARD-EMBOSSED-NAME", 30, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("CARD-EXPIRAION-DATE", 80, 10, FieldType.DATE),
                new FixedWidthField("CARD-ACTIVE-STATUS", 90, 1, FieldType.ALPHANUMERIC),
                new FixedWidthField("FILLER", 91, 59, FieldType.FILLER));
    }

    /** CVACT03Y.cpy — CARD-XREF-RECORD, actual data 36 bytes */
    public static List<FixedWidthField> cardXrefLayout() {
        return List.of(
                new FixedWidthField("XREF-CARD-NUM", 0, 16, FieldType.ALPHANUMERIC),
                new FixedWidthField("XREF-CUST-ID", 16, 9, FieldType.NUMERIC),
                new FixedWidthField("XREF-ACCT-ID", 25, 11, FieldType.NUMERIC));
    }

    /** CVTRA05Y.cpy — TRAN-RECORD / DALYTRAN-RECORD, 350 bytes */
    public static List<FixedWidthField> transactionLayout() {
        return List.of(
                new FixedWidthField("TRAN-ID", 0, 16, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-TYPE-CD", 16, 2, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-CAT-CD", 18, 4, FieldType.NUMERIC),
                new FixedWidthField("TRAN-SOURCE", 22, 10, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-DESC", 32, 100, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-AMT", 132, 11, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("TRAN-MERCHANT-ID", 143, 9, FieldType.NUMERIC),
                new FixedWidthField("TRAN-MERCHANT-NAME", 152, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-MERCHANT-CITY", 202, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-MERCHANT-ZIP", 252, 10, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-CARD-NUM", 262, 16, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-ORIG-TS", 278, 26, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-PROC-TS", 304, 26, FieldType.ALPHANUMERIC),
                new FixedWidthField("FILLER", 330, 20, FieldType.FILLER));
    }

    /** CVTRA03Y.cpy — TRAN-TYPE-RECORD, 60 bytes */
    public static List<FixedWidthField> tranTypeLayout() {
        return List.of(
                new FixedWidthField("TRAN-TYPE", 0, 2, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-TYPE-DESC", 2, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("FILLER", 52, 8, FieldType.FILLER));
    }

    /** CVTRA04Y.cpy — TRAN-CAT-RECORD, 60 bytes */
    public static List<FixedWidthField> tranCategoryLayout() {
        return List.of(
                new FixedWidthField("TRAN-TYPE-CD", 0, 2, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRAN-CAT-CD", 2, 4, FieldType.NUMERIC),
                new FixedWidthField("TRAN-CAT-TYPE-DESC", 6, 50, FieldType.ALPHANUMERIC),
                new FixedWidthField("FILLER", 56, 4, FieldType.FILLER));
    }

    /** CVTRA01Y.cpy — TRAN-CAT-BAL-RECORD, 50 bytes */
    public static List<FixedWidthField> tranCatBalanceLayout() {
        return List.of(
                new FixedWidthField("TRANCAT-ACCT-ID", 0, 11, FieldType.NUMERIC),
                new FixedWidthField("TRANCAT-TYPE-CD", 11, 2, FieldType.ALPHANUMERIC),
                new FixedWidthField("TRANCAT-CD", 13, 4, FieldType.NUMERIC),
                new FixedWidthField("TRAN-CAT-BAL", 17, 11, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("FILLER", 28, 22, FieldType.FILLER));
    }

    /** CVTRA02Y.cpy — DIS-GROUP-RECORD, 50 bytes */
    public static List<FixedWidthField> disclosureGroupLayout() {
        return List.of(
                new FixedWidthField("DIS-ACCT-GROUP-ID", 0, 10, FieldType.ALPHANUMERIC),
                new FixedWidthField("DIS-TRAN-TYPE-CD", 10, 2, FieldType.ALPHANUMERIC),
                new FixedWidthField("DIS-TRAN-CAT-CD", 12, 4, FieldType.NUMERIC),
                new FixedWidthField("DIS-INT-RATE", 16, 6, FieldType.SIGNED_NUMERIC, 2),
                new FixedWidthField("FILLER", 22, 28, FieldType.FILLER));
    }
}
