package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVTRA06Y (DALYTRAN-RECORD, RECLN = 350).
 * Represents a single daily transaction record read sequentially from DALYTRAN file.
 */
public record DailyTransaction(
        String id,              // PIC X(16) - DALYTRAN-ID
        String typeCd,          // PIC X(02) - DALYTRAN-TYPE-CD
        int catCd,              // PIC 9(04) - DALYTRAN-CAT-CD
        String source,          // PIC X(10) - DALYTRAN-SOURCE
        String desc,            // PIC X(100) - DALYTRAN-DESC
        BigDecimal amt,         // PIC S9(09)V99 - DALYTRAN-AMT (signed decimal, 11 bytes)
        long merchantId,        // PIC 9(09) - DALYTRAN-MERCHANT-ID
        String merchantName,    // PIC X(50) - DALYTRAN-MERCHANT-NAME
        String merchantCity,    // PIC X(50) - DALYTRAN-MERCHANT-CITY
        String merchantZip,     // PIC X(10) - DALYTRAN-MERCHANT-ZIP
        String cardNum,         // PIC X(16) - DALYTRAN-CARD-NUM
        String origTs,          // PIC X(26) - DALYTRAN-ORIG-TS
        String procTs           // PIC X(26) - DALYTRAN-PROC-TS
) {

    private static final int RECORD_LENGTH = 350;

    /**
     * Parses a 350-byte fixed-width record line into a DailyTransaction.
     * Field offsets from CVTRA06Y copybook:
     *   0:   DALYTRAN-ID (16)
     *   16:  DALYTRAN-TYPE-CD (2)
     *   18:  DALYTRAN-CAT-CD (4)
     *   22:  DALYTRAN-SOURCE (10)
     *   32:  DALYTRAN-DESC (100)
     *   132: DALYTRAN-AMT (11) - PIC S9(09)V99 zoned decimal
     *   143: DALYTRAN-MERCHANT-ID (9)
     *   152: DALYTRAN-MERCHANT-NAME (50)
     *   202: DALYTRAN-MERCHANT-CITY (50)
     *   252: DALYTRAN-MERCHANT-ZIP (10)
     *   262: DALYTRAN-CARD-NUM (16)
     *   278: DALYTRAN-ORIG-TS (26)
     *   304: DALYTRAN-PROC-TS (26)
     *   330: FILLER (20)
     */
    public static DailyTransaction fromFixedLength(String line) {
        if (line.length() < 330) {
            throw new IllegalArgumentException(
                    "Daily transaction record too short: " + line.length() + " (minimum 330 chars required)");
        }

        String id = line.substring(0, 16).trim();
        String typeCd = line.substring(16, 18).trim();
        int catCd = parseNumeric(line.substring(18, 22));
        String source = line.substring(22, 32).trim();
        String desc = line.substring(32, 132).trim();
        BigDecimal amt = parseSignedDecimal(line.substring(132, 143));
        long merchantId = parseLong(line.substring(143, 152));
        String merchantName = line.substring(152, 202).trim();
        String merchantCity = line.substring(202, 252).trim();
        String merchantZip = line.substring(252, 262).trim();
        String cardNum = line.substring(262, 278).trim();
        String origTs = line.substring(278, 304).trim();
        String procTs = line.substring(304, 330).trim();

        return new DailyTransaction(id, typeCd, catCd, source, desc, amt,
                merchantId, merchantName, merchantCity, merchantZip,
                cardNum, origTs, procTs);
    }

    /**
     * Parses a COBOL signed numeric display field (PIC S9(09)V99) in mainframe
     * zoned-decimal format. The trailing character encodes the sign:
     * '{' = +0, 'A'-'I' = +1 to +9, '}' = -0, 'J'-'R' = -1 to -9.
     * Digits '0'-'9' are treated as positive unsigned.
     */
    static BigDecimal parseSignedDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        int lastDigit;
        boolean negative;

        if (lastChar >= '0' && lastChar <= '9') {
            lastDigit = lastChar - '0';
            negative = false;
        } else if (lastChar == '{') {
            lastDigit = 0;
            negative = false;
        } else if (lastChar == '}') {
            lastDigit = 0;
            negative = true;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            lastDigit = lastChar - 'A' + 1;
            negative = false;
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            lastDigit = lastChar - 'J' + 1;
            negative = true;
        } else {
            throw new IllegalArgumentException("Invalid signed numeric character: " + lastChar);
        }

        String numStr = digits + lastDigit;
        BigDecimal value = new BigDecimal(numStr).movePointLeft(2);
        return negative ? value.negate() : value;
    }

    private static int parseNumeric(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(trimmed);
    }

    private static long parseLong(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(trimmed);
    }
}
