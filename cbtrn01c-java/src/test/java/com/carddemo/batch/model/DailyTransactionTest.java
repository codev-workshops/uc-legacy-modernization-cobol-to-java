package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DailyTransaction.fromFixedLength() parsing.
 * Test data derived from golden-files/dailytran.json first record.
 */
class DailyTransactionTest {

    /**
     * Constructs a 350-byte fixed-width record matching the first record in dailytran.json:
     * id="0000000000683580", typeCd="01", catCd=1, source="POS TERM",
     * desc="Purchase at Abshire-Lowe", amt=504.77, merchantId=800000000,
     * merchantName="Abshire-Lowe", merchantCity="North Enoshaven",
     * merchantZip="72112", cardNum="4859452612877065",
     * origTs="2022-06-10 19:27:53.000000", procTs=""
     */
    private String buildSampleRecord() {
        StringBuilder sb = new StringBuilder();
        sb.append(padRight("0000000000683580", 16));  // DALYTRAN-ID
        sb.append(padRight("01", 2));                  // DALYTRAN-TYPE-CD
        sb.append(padLeft("1", 4, '0'));               // DALYTRAN-CAT-CD (9(04))
        sb.append(padRight("POS TERM", 10));           // DALYTRAN-SOURCE
        sb.append(padRight("Purchase at Abshire-Lowe", 100)); // DALYTRAN-DESC
        // DALYTRAN-AMT: PIC S9(09)V99 = 504.77 → "0000050477" (9 integer + 2 decimal = 11 chars with sign)
        // 504.77 → integer=50477, formatted as 0000005047 + sign char for 7 positive = 'G'
        sb.append("0000005047G");                      // DALYTRAN-AMT (50477 → 504.77, G = +7)
        sb.append(padLeft("800000000", 9, '0'));       // DALYTRAN-MERCHANT-ID
        sb.append(padRight("Abshire-Lowe", 50));       // DALYTRAN-MERCHANT-NAME
        sb.append(padRight("North Enoshaven", 50));    // DALYTRAN-MERCHANT-CITY
        sb.append(padRight("72112", 10));              // DALYTRAN-MERCHANT-ZIP
        sb.append(padRight("4859452612877065", 16));   // DALYTRAN-CARD-NUM
        sb.append(padRight("2022-06-10 19:27:53.000000", 26)); // DALYTRAN-ORIG-TS
        sb.append(padRight("", 26));                   // DALYTRAN-PROC-TS
        sb.append(padRight("", 20));                   // FILLER
        return sb.toString();
    }

    @Test
    void shouldParseFirstGoldenRecord() {
        String record = buildSampleRecord();
        assertEquals(350, record.length());

        DailyTransaction txn = DailyTransaction.fromFixedLength(record);

        assertEquals("0000000000683580", txn.id());
        assertEquals("01", txn.typeCd());
        assertEquals(1, txn.catCd());
        assertEquals("POS TERM", txn.source());
        assertEquals("Purchase at Abshire-Lowe", txn.desc());
        assertEquals(0, new BigDecimal("504.77").compareTo(txn.amt()));
        assertEquals(800000000L, txn.merchantId());
        assertEquals("Abshire-Lowe", txn.merchantName());
        assertEquals("North Enoshaven", txn.merchantCity());
        assertEquals("72112", txn.merchantZip());
        assertEquals("4859452612877065", txn.cardNum());
        assertEquals("2022-06-10 19:27:53.000000", txn.origTs());
        assertEquals("", txn.procTs());
    }

    @Test
    void shouldParseZeroAmount() {
        StringBuilder sb = new StringBuilder();
        sb.append(padRight("TXNID00000000001", 16));
        sb.append(padRight("02", 2));
        sb.append("0000");
        sb.append(padRight("ONLINE", 10));
        sb.append(padRight("Test desc", 100));
        sb.append("0000000000{");  // amt = 0.00 ('{' = +0)
        sb.append("000000001");
        sb.append(padRight("Merchant", 50));
        sb.append(padRight("City", 50));
        sb.append(padRight("12345", 10));
        sb.append(padRight("1234567890123456", 16));
        sb.append(padRight("2023-01-01 00:00:00.000000", 26));
        sb.append(padRight("", 26));
        sb.append(padRight("", 20));
        String record = sb.toString();

        DailyTransaction txn = DailyTransaction.fromFixedLength(record);
        assertEquals(0, BigDecimal.ZERO.compareTo(txn.amt()));
    }

    @Test
    void shouldParseNegativeAmount() {
        // PIC S9(09)V99 = 11 chars. -123.49 → 12349 → "0000001234" + R (R = -9)
        StringBuilder sb = new StringBuilder();
        sb.append(padRight("TXNID00000000002", 16));
        sb.append(padRight("03", 2));
        sb.append("0005");
        sb.append(padRight("REFUND", 10));
        sb.append(padRight("Refund transaction", 100));
        sb.append("0000001234R");  // amt = -123.49
        sb.append("000000001");
        sb.append(padRight("Merchant", 50));
        sb.append(padRight("City", 50));
        sb.append(padRight("12345", 10));
        sb.append(padRight("1234567890123456", 16));
        sb.append(padRight("2023-01-01 00:00:00.000000", 26));
        sb.append(padRight("", 26));
        sb.append(padRight("", 20));
        String record = sb.toString();

        DailyTransaction txn = DailyTransaction.fromFixedLength(record);
        assertEquals(0, new BigDecimal("-123.49").compareTo(txn.amt()));
    }

    @Test
    void shouldRejectTooShortRecord() {
        assertThrows(IllegalArgumentException.class, () ->
                DailyTransaction.fromFixedLength("short record"));
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    private static String padLeft(String s, int len, char padChar) {
        if (s.length() >= len) return s.substring(0, len);
        return String.valueOf(padChar).repeat(len - s.length()) + s;
    }
}
