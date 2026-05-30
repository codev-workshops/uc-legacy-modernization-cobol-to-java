package com.carddemo.batch.importer;

import com.carddemo.batch.export.ExportRecord;
import com.carddemo.batch.export.RecordType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExportRecordLineMapperTest {

    private final ExportRecordLineMapper mapper = new ExportRecordLineMapper();

    @Test
    void mapLine_customerRecord() throws Exception {
        String line = "C|2024-01-15 10:30:00.000000|1|0001|NORTH"
                + "|1000001|John|M|Doe|123 Main St|||NY|US|10001"
                + "|5551234567||123456789|DL12345|1990-01-15|EFT001|Y|750";

        ExportRecord record = mapper.mapLine(line, 1);

        assertEquals(RecordType.CUSTOMER, record.getRecordType());
        assertEquals("2024-01-15 10:30:00.000000", record.getTimestamp());
        assertEquals(1L, record.getSequenceNum());
        assertEquals("0001", record.getBranchId());
        assertEquals("NORTH", record.getRegionCode());
        assertEquals(18, record.getFields().length);
        assertEquals("1000001", record.getFields()[0]);
        assertEquals("John", record.getFields()[1]);
        assertEquals("750", record.getFields()[17]);
    }

    @Test
    void mapLine_accountRecord() throws Exception {
        String line = "A|2024-01-15 10:30:00.000000|2|0001|NORTH"
                + "|12345678901|Y|5000.00|10000.00|2000.00"
                + "|2020-01-01|2025-12-31|2023-06-15|500.00|250.00|10001|GRP01";

        ExportRecord record = mapper.mapLine(line, 2);

        assertEquals(RecordType.ACCOUNT, record.getRecordType());
        assertEquals(12, record.getFields().length);
        assertEquals("12345678901", record.getFields()[0]);
        assertEquals("GRP01", record.getFields()[11]);
    }

    @Test
    void mapLine_cardXrefRecord() throws Exception {
        String line = "X|2024-01-15 10:30:00.000000|3|0001|NORTH|4111111111111111|1000001|12345678901";

        ExportRecord record = mapper.mapLine(line, 3);

        assertEquals(RecordType.CARD_XREF, record.getRecordType());
        assertEquals(3, record.getFields().length);
        assertEquals("4111111111111111", record.getFields()[0]);
    }

    @Test
    void mapLine_cardRecord() throws Exception {
        String line = "D|2024-01-15 10:30:00.000000|5|0001|NORTH"
                + "|4111111111111111|12345678901|123|JOHN DOE|2025-12-31|Y";

        ExportRecord record = mapper.mapLine(line, 5);

        assertEquals(RecordType.CARD, record.getRecordType());
        assertEquals(6, record.getFields().length);
        assertEquals("JOHN DOE", record.getFields()[3]);
    }

    @Test
    void mapLine_tranCatBalanceRecord() throws Exception {
        String line = "B|2024-01-15 10:30:00.000000|6|0001|NORTH|12345678901|SA|5001|1500.75";

        ExportRecord record = mapper.mapLine(line, 6);

        assertEquals(RecordType.TRAN_CAT_BALANCE, record.getRecordType());
        assertEquals(4, record.getFields().length);
        assertEquals("1500.75", record.getFields()[3]);
    }

    @Test
    void mapLine_headerOnly_shouldHaveEmptyFields() throws Exception {
        String line = "C|2024-01-15 10:30:00.000000|1|0001|NORTH";

        ExportRecord record = mapper.mapLine(line, 1);

        assertEquals(RecordType.CUSTOMER, record.getRecordType());
        assertArrayEquals(new String[0], record.getFields());
    }

    @Test
    void mapLine_emptyFieldValues() throws Exception {
        String line = "X||3|||card1||";

        ExportRecord record = mapper.mapLine(line, 1);

        assertEquals(RecordType.CARD_XREF, record.getRecordType());
        assertEquals("", record.getTimestamp());
        assertEquals("", record.getBranchId());
        assertEquals("", record.getRegionCode());
        assertEquals("card1", record.getFields()[0]);
        assertEquals("", record.getFields()[1]);
        assertEquals("", record.getFields()[2]);
    }

    @Test
    void mapLine_tooFewFields_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> mapper.mapLine("C|ts|1|br", 1));
    }

    @Test
    void mapLine_unknownType_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> mapper.mapLine("Z|ts|1|br|rg", 1));
    }
}
