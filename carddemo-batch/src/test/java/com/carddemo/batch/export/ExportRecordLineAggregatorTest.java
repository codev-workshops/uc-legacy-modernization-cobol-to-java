package com.carddemo.batch.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportRecordLineAggregatorTest {

    private final ExportRecordLineAggregator aggregator = new ExportRecordLineAggregator();

    @Test
    void aggregate_customerRecord() {
        ExportRecord record = new ExportRecord(
                RecordType.CUSTOMER,
                "2024-01-15 10:30:00.000000",
                1L,
                "0001",
                "NORTH",
                new String[]{"1000001", "John", "M", "Doe", "123 Main St", "", "", "NY", "US", "10001",
                        "5551234567", "", "123456789", "DL12345", "1990-01-15", "EFT001", "Y", "750"}
        );

        String line = aggregator.aggregate(record);

        assertEquals("C|2024-01-15 10:30:00.000000|1|0001|NORTH"
                + "|1000001|John|M|Doe|123 Main St|||NY|US|10001"
                + "|5551234567||123456789|DL12345|1990-01-15|EFT001|Y|750", line);
    }

    @Test
    void aggregate_accountRecord() {
        ExportRecord record = new ExportRecord(
                RecordType.ACCOUNT,
                "2024-01-15 10:30:00.000000",
                2L,
                "0001",
                "NORTH",
                new String[]{"12345678901", "Y", "5000.00", "10000.00", "2000.00",
                        "2020-01-01", "2025-12-31", "2023-06-15", "500.00", "250.00", "10001", "GRP01"}
        );

        String line = aggregator.aggregate(record);

        assertEquals("A|2024-01-15 10:30:00.000000|2|0001|NORTH"
                + "|12345678901|Y|5000.00|10000.00|2000.00"
                + "|2020-01-01|2025-12-31|2023-06-15|500.00|250.00|10001|GRP01", line);
    }

    @Test
    void aggregate_nullFields_shouldOutputEmpty() {
        ExportRecord record = new ExportRecord(
                RecordType.CARD_XREF,
                null,
                3L,
                null,
                null,
                new String[]{"4111111111111111", "1000001", "12345678901"}
        );

        String line = aggregator.aggregate(record);

        assertEquals("X||3|||4111111111111111|1000001|12345678901", line);
    }

    @Test
    void aggregate_nullFieldsArray_shouldOutputHeaderOnly() {
        ExportRecord record = new ExportRecord(
                RecordType.CUSTOMER,
                "2024-01-15 10:30:00.000000",
                1L,
                "0001",
                "NORTH",
                null
        );

        String line = aggregator.aggregate(record);

        assertEquals("C|2024-01-15 10:30:00.000000|1|0001|NORTH", line);
    }

    @Test
    void aggregate_cardRecord() {
        ExportRecord record = new ExportRecord(
                RecordType.CARD,
                "2024-01-15 10:30:00.000000",
                5L,
                "0001",
                "NORTH",
                new String[]{"4111111111111111", "12345678901", "123", "JOHN DOE", "2025-12-31", "Y"}
        );

        String line = aggregator.aggregate(record);

        assertEquals("D|2024-01-15 10:30:00.000000|5|0001|NORTH"
                + "|4111111111111111|12345678901|123|JOHN DOE|2025-12-31|Y", line);
    }

    @Test
    void aggregate_tranCatBalanceRecord() {
        ExportRecord record = new ExportRecord(
                RecordType.TRAN_CAT_BALANCE,
                "2024-01-15 10:30:00.000000",
                6L,
                "0001",
                "NORTH",
                new String[]{"12345678901", "SA", "5001", "1500.75"}
        );

        String line = aggregator.aggregate(record);

        assertEquals("B|2024-01-15 10:30:00.000000|6|0001|NORTH"
                + "|12345678901|SA|5001|1500.75", line);
    }
}
