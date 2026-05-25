package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for parsing fixed-length account records (CVACT01Y copybook format).
 * Validates the signed numeric (zoned-decimal) parsing logic matches COBOL behavior.
 *
 * PIC S9(10)V99 = 12 bytes in DISPLAY format. The V99 means last 2 digits
 * are fractional (implied decimal). Trailing character encodes sign + last digit.
 */
class AccountRecordParsingTest {

    // First record from acctdata.txt:
    // 00000001940{ → 000000019400 → V99 → 194.00
    private static final String SAMPLE_LINE_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000" +
            " ".repeat(178);

    // Second record:
    // 00000001580{ → 000000015800 → V99 → 158.00
    private static final String SAMPLE_LINE_2 =
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000" +
            " ".repeat(178);

    @Test
    @DisplayName("Parse first account record - positive balances with '{' terminator")
    void parseFirstRecord() {
        AccountRecord record = AccountRecord.fromFixedLength(SAMPLE_LINE_1);

        assertEquals(1L, record.acctId());
        assertEquals("Y", record.activeStatus());
        assertEquals(new BigDecimal("194.00"), record.currentBalance());
        assertEquals(new BigDecimal("2020.00"), record.creditLimit());
        assertEquals(new BigDecimal("1020.00"), record.cashCreditLimit());
        assertEquals("2014-11-20", record.openDate());
        assertEquals("2025-05-20", record.expirationDate());
        assertEquals("2025-05-20", record.reissueDate());
        assertEquals(new BigDecimal("0.00"), record.currentCycleCredit());
        assertEquals(new BigDecimal("0.00"), record.currentCycleDebit());
        assertEquals("A000000000", record.addressZip());
        assertEquals("          ", record.groupId());
    }

    @Test
    @DisplayName("Parse second account record")
    void parseSecondRecord() {
        AccountRecord record = AccountRecord.fromFixedLength(SAMPLE_LINE_2);

        assertEquals(2L, record.acctId());
        assertEquals("Y", record.activeStatus());
        assertEquals(new BigDecimal("158.00"), record.currentBalance());
        assertEquals(new BigDecimal("6130.00"), record.creditLimit());
        assertEquals(new BigDecimal("5448.00"), record.cashCreditLimit());
        assertEquals("2013-06-19", record.openDate());
        assertEquals("2024-08-11", record.expirationDate());
        assertEquals("2024-08-11", record.reissueDate());
    }

    @Test
    @DisplayName("Reject too-short record line")
    void rejectShortLine() {
        assertThrows(IllegalArgumentException.class,
                () -> AccountRecord.fromFixedLength("short"));
    }

    @ParameterizedTest
    @DisplayName("Signed numeric parsing - zoned decimal trailing sign with V99")
    @CsvSource({
            "00000001940{, 194.00",       // { = +0, full=000000019400, V99=194.00
            "00000000000{, 0.00",          // zero
            "00000001234A, 123.41",        // A = +1, full=000000012341, V99=123.41
            "00000001234I, 123.49",        // I = +9, full=000000012349, V99=123.49
            "00000001234J, -123.41",       // J = -1, full=-000000012341, V99=-123.41
            "00000001234R, -123.49",       // R = -9, full=-000000012349, V99=-123.49
            "00000001234}, -123.40"        // } = -0, full=-000000012340, V99=-123.40
    })
    void signedNumericParsing(String input, String expected) {
        // Build a minimal record with the given value in the CURR-BAL field
        String line = "00000000001Y" + input +
                "00000000000{00000000000{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000" +
                " ".repeat(178);
        AccountRecord record = AccountRecord.fromFixedLength(line);
        assertEquals(new BigDecimal(expected), record.currentBalance());
    }
}
