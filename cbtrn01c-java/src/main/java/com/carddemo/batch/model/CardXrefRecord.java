package com.carddemo.batch.model;

/**
 * Maps to COBOL copybook CVACT03Y (CARD-XREF-RECORD, RECLN = 50).
 * Cross-references a card number to a customer and account.
 */
public record CardXrefRecord(
        String cardNum,   // PIC X(16) - XREF-CARD-NUM
        long custId,      // PIC 9(09) - XREF-CUST-ID
        long acctId       // PIC 9(11) - XREF-ACCT-ID
) {

    /**
     * Parses a 50-byte fixed-width record line.
     * Field layout from CVACT03Y:
     *   0:  XREF-CARD-NUM (16)
     *   16: XREF-CUST-ID (9)
     *   25: XREF-ACCT-ID (11)
     *   36: FILLER (14)
     */
    public static CardXrefRecord fromFixedLength(String line) {
        if (line.length() < 36) {
            throw new IllegalArgumentException(
                    "Card XREF record too short: " + line.length() + " (minimum 36 chars required)");
        }

        String cardNum = line.substring(0, 16).trim();
        long custId = parseLong(line.substring(16, 25));
        long acctId = parseLong(line.substring(25, 36));

        return new CardXrefRecord(cardNum, custId, acctId);
    }

    private static long parseLong(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(trimmed);
    }
}
