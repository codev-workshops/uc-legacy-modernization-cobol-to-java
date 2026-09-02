package com.carddemo.batch.cbact01c.model;

import static com.carddemo.batch.cbact01c.codec.CobolCharset.ASCII;
import static com.carddemo.batch.cbact01c.codec.CobolCharset.EBCDIC;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class AccountRecordTest {

    @Test
    void readsAllFiftyEbcdicRecords() throws IOException {
        List<byte[]> records = SampleData.readAll(SampleData.ebcdicAcctData(), EBCDIC);
        assertEquals(50, records.size());
        assertFirstRecord(AccountRecord.fromBytes(records.get(0), EBCDIC));
        assertEquals("00000000050", AccountRecord.fromBytes(records.get(49), EBCDIC).acctId());
    }

    @Test
    void readsAllFiftyAsciiRecords() throws IOException {
        List<byte[]> records = SampleData.readAll(SampleData.asciiAcctData(), ASCII);
        assertEquals(50, records.size());
        assertFirstRecord(AccountRecord.fromBytes(records.get(0), ASCII));
    }

    private static void assertFirstRecord(AccountRecord acct) {
        assertEquals("00000000001", acct.acctId());
        assertEquals("Y", acct.activeStatus());
        assertEquals(new BigDecimal("194.00"), acct.currBal());
        assertEquals(new BigDecimal("2020.00"), acct.creditLimit());
        assertEquals(new BigDecimal("1020.00"), acct.cashCreditLimit());
        assertEquals("2014-11-20", acct.openDate());
        assertEquals("2025-05-20", acct.expirationDate());
        assertEquals("2025-05-20", acct.reissueDate());
        assertEquals(new BigDecimal("0.00"), acct.currCycCredit());
        assertEquals(new BigDecimal("0.00"), acct.currCycDebit());
        assertEquals("A000000000", acct.addrZip());
        assertEquals("          ", acct.groupId());
        assertEquals(178, acct.filler().length);
    }

    /** Record 49 (index 48) differs between the two sample files in ACCT-ADDR-ZIP only ("ZEROAPR" vs "A000000000"). */
    private static final int KNOWN_ZIP_MISMATCH_INDEX = 48;

    @Test
    void ebcdicAndAsciiFilesDecodeToEqualRecords() throws IOException {
        List<byte[]> ebcdic = SampleData.readAll(SampleData.ebcdicAcctData(), EBCDIC);
        List<byte[]> ascii = SampleData.readAll(SampleData.asciiAcctData(), ASCII);
        assertEquals(ebcdic.size(), ascii.size());
        for (int i = 0; i < ebcdic.size(); i++) {
            AccountRecord e = AccountRecord.fromBytes(ebcdic.get(i), EBCDIC);
            AccountRecord a = AccountRecord.fromBytes(ascii.get(i), ASCII);
            assertEquals(e.acctId(), a.acctId(), "record " + i);
            assertEquals(e.activeStatus(), a.activeStatus());
            assertEquals(e.currBal(), a.currBal());
            assertEquals(e.creditLimit(), a.creditLimit());
            assertEquals(e.cashCreditLimit(), a.cashCreditLimit());
            assertEquals(e.openDate(), a.openDate());
            assertEquals(e.expirationDate(), a.expirationDate());
            assertEquals(e.reissueDate(), a.reissueDate());
            assertEquals(e.currCycCredit(), a.currCycCredit());
            assertEquals(e.currCycDebit(), a.currCycDebit());
            if (i == KNOWN_ZIP_MISMATCH_INDEX) {
                assertEquals("ZEROAPR   ", e.addrZip());
                assertEquals("A000000000", a.addrZip());
            } else {
                assertEquals(e.addrZip(), a.addrZip(), "record " + i);
            }
            assertEquals(e.groupId(), a.groupId());
            assertEquals(EBCDIC.decode(e.filler(), 0, 178), ASCII.decode(a.filler(), 0, 178));
        }
    }

    @Test
    void roundTripsEveryEbcdicSampleRecord() throws IOException {
        for (byte[] bytes : SampleData.readAll(SampleData.ebcdicAcctData(), EBCDIC)) {
            assertArrayEquals(bytes, AccountRecord.fromBytes(bytes, EBCDIC).toBytes(EBCDIC));
        }
    }

    @Test
    void roundTripsEveryAsciiSampleRecord() throws IOException {
        for (byte[] bytes : SampleData.readAll(SampleData.asciiAcctData(), ASCII)) {
            assertArrayEquals(bytes, AccountRecord.fromBytes(bytes, ASCII).toBytes(ASCII));
        }
    }

    @Test
    void rejectsWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> AccountRecord.fromBytes(new byte[299], EBCDIC));
    }
}
