package com.carddemo.account.batch;

import com.carddemo.common.dto.ExportRecordDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportRecordLineAggregatorTest {

    private final ExportRecordLineAggregator aggregator = new ExportRecordLineAggregator();

    @Test
    void aggregate_accountRecord() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("A")
                .recordData("00000000001Y000000019400")
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
        assertEquals("A", result.substring(0, 1));
        assertTrue(result.startsWith("A00000000001Y000000019400"));
    }

    @Test
    void aggregate_customerRecord() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("U")
                .recordData("000000001John")
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
        assertEquals("U", result.substring(0, 1));
        assertTrue(result.startsWith("U000000001John"));
    }

    @Test
    void aggregate_cardRecord() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("C")
                .recordData("4111111111111111")
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
        assertEquals("C", result.substring(0, 1));
    }

    @Test
    void aggregate_xrefRecord() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("X")
                .recordData("4111111111111111000000001")
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
        assertEquals("X", result.substring(0, 1));
    }

    @Test
    void aggregate_padsToFixedLength() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("A")
                .recordData("short")
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
        assertTrue(result.startsWith("Ashort"));
    }

    @Test
    void aggregate_truncatesLongData() {
        String longData = "X".repeat(350);
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("A")
                .recordData(longData)
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
    }

    @Test
    void aggregate_nullRecordType() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordData("data")
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
        assertTrue(result.startsWith(" data"));
    }

    @Test
    void aggregate_nullRecordData() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("A")
                .build();

        String result = aggregator.aggregate(dto);

        assertEquals(300, result.length());
        assertEquals("A", result.substring(0, 1));
    }

    @Test
    void aggregate_differentTypeCodesProduceCorrectPrefix() {
        for (String type : new String[]{"A", "C", "U", "X"}) {
            ExportRecordDto dto = ExportRecordDto.builder()
                    .recordType(type)
                    .recordData("testdata")
                    .build();

            String result = aggregator.aggregate(dto);
            assertEquals(type, result.substring(0, 1));
            assertEquals(300, result.length());
        }
    }
}
