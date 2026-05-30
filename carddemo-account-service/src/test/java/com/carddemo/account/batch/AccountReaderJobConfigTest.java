package com.carddemo.account.batch;

import com.carddemo.account.entity.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountReaderJobConfigTest {

    @Test
    void testFormatAccountFixedWidth() {
        Account account = Account.builder()
                .acctId(12345678901L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1000.50"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("2000.00"))
                .acctOpenDate("2020-01-15")
                .acctExpirationDate("2025-01-15")
                .acctReissueDate("2023-01-15")
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("150.00"))
                .acctAddrZip("10001")
                .acctGroupId("GRP001")
                .build();

        String result = AccountReaderJobConfig.formatAccount(account);
        assertEquals(300, result.length());
        assertEquals("12345678901", result.substring(0, 11));
        assertEquals("Y", result.substring(11, 12));
    }

    @Test
    void testFormatAccountWithNulls() {
        Account account = Account.builder()
                .acctId(1L)
                .build();

        String result = AccountReaderJobConfig.formatAccount(account);
        assertEquals(300, result.length());
        assertEquals("00000000001", result.substring(0, 11));
    }

    @Test
    void testFormatAccountNegativeBalance() {
        Account account = Account.builder()
                .acctId(1L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("-500.25"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("2000.00"))
                .acctOpenDate("2020-01-15")
                .acctExpirationDate("2025-01-15")
                .acctReissueDate("2023-01-15")
                .acctCurrCycCredit(new BigDecimal("0.00"))
                .acctCurrCycDebit(new BigDecimal("0.00"))
                .acctAddrZip("10001")
                .acctGroupId("GRP001")
                .build();

        String result = AccountReaderJobConfig.formatAccount(account);
        assertEquals(300, result.length());
        // Negative: -500.25 -> 00000050025 with overpunch on last digit: 5->N
        assertTrue(result.substring(12, 24).endsWith("N"));
    }

    @Test
    void testPadRight() {
        assertEquals("abc  ", AccountReaderJobConfig.padRight("abc", 5));
        assertEquals("ab", AccountReaderJobConfig.padRight("abcde", 2));
        assertEquals("   ", AccountReaderJobConfig.padRight(null, 3));
        assertEquals("hello", AccountReaderJobConfig.padRight("hello", 5));
    }

    @Test
    void testNullSafe() {
        assertEquals("", AccountReaderJobConfig.nullSafe(null));
        assertEquals("test", AccountReaderJobConfig.nullSafe("test"));
    }

    @Test
    void testFormatLong() {
        assertEquals("00000000001", AccountReaderJobConfig.formatLong(1L, 11));
        assertEquals("12345678901", AccountReaderJobConfig.formatLong(12345678901L, 11));
        assertEquals("00000000000", AccountReaderJobConfig.formatLong(null, 11));
    }

    @Test
    void testFormatSignedDecimalPositive() {
        String result = AccountReaderJobConfig.formatSignedDecimal(new BigDecimal("1000.50"), 10, 2);
        assertEquals("000000100050", result);
    }

    @Test
    void testFormatSignedDecimalNegative() {
        String result = AccountReaderJobConfig.formatSignedDecimal(new BigDecimal("-1000.50"), 10, 2);
        assertEquals(12, result.length());
        assertEquals("00000010005}", result);
    }

    @Test
    void testFormatSignedDecimalNull() {
        String result = AccountReaderJobConfig.formatSignedDecimal(null, 10, 2);
        assertEquals("000000000000", result);
    }

    @Test
    void testFormatSignedDecimalZero() {
        String result = AccountReaderJobConfig.formatSignedDecimal(BigDecimal.ZERO, 10, 2);
        assertEquals("000000000000", result);
    }

    @Test
    void testFormatSignedDecimalNegativeWithDifferentLastDigits() {
        // Test overpunch for different ending digits: -1 -> J, -2 -> K, ..., -9 -> R
        assertEquals("000000000010", AccountReaderJobConfig.formatSignedDecimal(new BigDecimal("0.10"), 10, 2));
        String neg1 = AccountReaderJobConfig.formatSignedDecimal(new BigDecimal("-0.01"), 10, 2);
        assertEquals("00000000000J", neg1);
        String neg2 = AccountReaderJobConfig.formatSignedDecimal(new BigDecimal("-0.02"), 10, 2);
        assertEquals("00000000000K", neg2);
        String neg9 = AccountReaderJobConfig.formatSignedDecimal(new BigDecimal("-0.09"), 10, 2);
        assertEquals("00000000000R", neg9);
    }
}
