package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y (ACCOUNT-RECORD, RECLN 300).
 * Represents a single account record from the indexed VSAM KSDS file.
 * Reuses the same parsing logic as cbact01c-java.
 */
public record AccountRecord(
        long acctId,                   // PIC 9(11)
        String activeStatus,           // PIC X(01)
        BigDecimal currentBalance,     // PIC S9(10)V99
        BigDecimal creditLimit,        // PIC S9(10)V99
        BigDecimal cashCreditLimit,    // PIC S9(10)V99
        String openDate,               // PIC X(10)
        String expirationDate,         // PIC X(10)
        String reissueDate,            // PIC X(10)
        BigDecimal currentCycleCredit, // PIC S9(10)V99
        BigDecimal currentCycleDebit,  // PIC S9(10)V99
        String addressZip,             // PIC X(10)
        String groupId                 // PIC X(10)
) {

    /**
     * Parses a fixed-length 300-character account record line.
     * Field layout matches CVACT01Y copybook:
     *   ACCT-ID(11) + STATUS(1) + CURR-BAL(12) + CREDIT-LIMIT(12) + CASH-CREDIT-LIMIT(12)
     *   + OPEN-DATE(10) + EXPIRATION-DATE(10) + REISSUE-DATE(10)
     *   + CURR-CYC-CREDIT(12) + CURR-CYC-DEBIT(12) + ADDR-ZIP(10) + GROUP-ID(10)
     *   + FILLER(178) = 300
     */
    public static AccountRecord fromFixedLength(String line) {
        if (line.length() < 122) {
            throw new IllegalArgumentException(
                    "Account record line too short: " + line.length() + " (minimum 122 chars required)");
        }

        int pos = 0;
        long acctId = Long.parseLong(line.substring(pos, pos + 11).trim());
        pos += 11;

        String activeStatus = line.substring(pos, pos + 1);
        pos += 1;

        BigDecimal currentBalance = parseSignedDecimal(line.substring(pos, pos + 12));
        pos += 12;

        BigDecimal creditLimit = parseSignedDecimal(line.substring(pos, pos + 12));
        pos += 12;

        BigDecimal cashCreditLimit = parseSignedDecimal(line.substring(pos, pos + 12));
        pos += 12;

        String openDate = line.substring(pos, pos + 10);
        pos += 10;

        String expirationDate = line.substring(pos, pos + 10);
        pos += 10;

        String reissueDate = line.substring(pos, pos + 10);
        pos += 10;

        BigDecimal currentCycleCredit = parseSignedDecimal(line.substring(pos, pos + 12));
        pos += 12;

        BigDecimal currentCycleDebit = parseSignedDecimal(line.substring(pos, pos + 12));
        pos += 12;

        String addressZip = line.substring(pos, pos + 10);
        pos += 10;

        String groupId = line.substring(pos, pos + 10);

        return new AccountRecord(acctId, activeStatus, currentBalance, creditLimit,
                cashCreditLimit, openDate, expirationDate, reissueDate,
                currentCycleCredit, currentCycleDebit, addressZip, groupId);
    }

    /**
     * Parses a COBOL signed numeric display field (PIC S9(10)V99) in mainframe
     * zoned-decimal format. The trailing character encodes the sign:
     * '{' = +0, 'A'-'I' = +1 to +9, '}' = -0, 'J'-'R' = -1 to -9.
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
}
