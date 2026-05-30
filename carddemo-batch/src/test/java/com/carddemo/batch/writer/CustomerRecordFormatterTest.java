package com.carddemo.batch.writer;

import com.carddemo.common.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRecordFormatterTest {

    @Test
    void formatProducesFixedWidthRecord() {
        Customer c = buildCustomer();
        String record = CustomerRecordFormatter.format(c);
        assertEquals(CustomerRecordFormatter.RECORD_LENGTH, record.length());
    }

    @Test
    void formatCustIdZeroPadded() {
        Customer c = buildCustomer();
        c.setCustId(42L);
        String record = CustomerRecordFormatter.format(c);
        assertEquals("000000042", record.substring(0, 9));
    }

    @Test
    void formatFieldsAtCorrectOffsets() {
        Customer c = buildCustomer();
        c.setCustId(1L);
        c.setFirstName("John");
        c.setMiddleName("Q");
        c.setLastName("Doe");
        c.setAddrLine1("123 Main St");
        c.setAddrLine2("Apt 4");
        c.setAddrLine3("Springfield");
        c.setStateCode("IL");
        c.setCountryCode("USA");
        c.setZip("62704");
        c.setPhone1("(217)555-0100");
        c.setPhone2("(217)555-0200");
        c.setSsn(123456789L);
        c.setGovtIssuedId("DL12345");
        c.setDob("1990-01-15");
        c.setEftAccountId("0012345678");
        c.setPriCardHolderInd("Y");
        c.setFicoCreditScore(750);

        String record = CustomerRecordFormatter.format(c);

        assertEquals(500, record.length());
        // CUST-ID at offset 0, length 9
        assertEquals("000000001", record.substring(0, 9));
        // CUST-FIRST-NAME at offset 9, length 25
        assertTrue(record.substring(9, 34).startsWith("John"));
        assertEquals(25, record.substring(9, 34).length());
        // CUST-MIDDLE-NAME at offset 34, length 25
        assertTrue(record.substring(34, 59).startsWith("Q"));
        // CUST-LAST-NAME at offset 59, length 25
        assertTrue(record.substring(59, 84).startsWith("Doe"));
        // CUST-ADDR-LINE-1 at offset 84, length 50
        assertTrue(record.substring(84, 134).startsWith("123 Main St"));
        // CUST-ADDR-STATE-CD at offset 234, length 2
        assertEquals("IL", record.substring(234, 236));
        // CUST-ADDR-COUNTRY-CD at offset 236, length 3
        assertEquals("USA", record.substring(236, 239));
        // CUST-SSN at offset 279, length 9
        assertEquals("123456789", record.substring(279, 288));
        // CUST-DOB at offset 308, length 10
        assertEquals("1990-01-15", record.substring(308, 318));
        // CUST-PRI-CARD-HOLDER at offset 328, length 1
        assertEquals("Y", record.substring(328, 329));
        // CUST-FICO at offset 329, length 3
        assertEquals("750", record.substring(329, 332));
        // FILLER at offset 332, length 168 — should be spaces
        assertEquals(" ".repeat(168), record.substring(332, 500));
    }

    @Test
    void formatHandlesNullFields() {
        Customer c = new Customer();
        c.setCustId(0L);
        String record = CustomerRecordFormatter.format(c);
        assertEquals(500, record.length());
        assertEquals("000000000", record.substring(0, 9));
        // All alpha fields should be spaces
        assertEquals(" ".repeat(25), record.substring(9, 34));
    }

    @Test
    void padAlphaHandlesNull() {
        assertEquals("     ", CustomerRecordFormatter.padAlpha(null, 5));
    }

    @Test
    void padAlphaRightPadsShortStrings() {
        assertEquals("AB   ", CustomerRecordFormatter.padAlpha("AB", 5));
    }

    @Test
    void padAlphaTruncatesLongStrings() {
        assertEquals("ABCDE", CustomerRecordFormatter.padAlpha("ABCDEFGH", 5));
    }

    @Test
    void padNumericHandlesNull() {
        assertEquals("000", CustomerRecordFormatter.padNumeric(null, 3));
    }

    @Test
    void padNumericZeroPads() {
        assertEquals("042", CustomerRecordFormatter.padNumeric(42, 3));
    }

    @Test
    void padNumericTruncatesOverflow() {
        assertEquals("234", CustomerRecordFormatter.padNumeric(1234, 3));
    }

    private Customer buildCustomer() {
        Customer c = new Customer();
        c.setCustId(1L);
        c.setFirstName("Test");
        c.setMiddleName("M");
        c.setLastName("User");
        c.setAddrLine1("123 Test St");
        c.setAddrLine2("Suite 100");
        c.setAddrLine3("Testville");
        c.setStateCode("TX");
        c.setCountryCode("USA");
        c.setZip("75001");
        c.setPhone1("(555)123-4567");
        c.setPhone2("(555)987-6543");
        c.setSsn(111223333L);
        c.setGovtIssuedId("TX12345");
        c.setDob("1985-06-15");
        c.setEftAccountId("0099887766");
        c.setPriCardHolderInd("Y");
        c.setFicoCreditScore(700);
        return c;
    }
}
