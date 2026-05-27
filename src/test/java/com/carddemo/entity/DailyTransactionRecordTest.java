package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DailyTransactionRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVTRA06Y.cpy has 13 non-FILLER fields
        assertEquals(13, DailyTransactionRecord.class.getDeclaredFields().length);
    }

    @Test
    void financialFieldsAreBigDecimal() throws NoSuchFieldException {
        assertEquals(BigDecimal.class, DailyTransactionRecord.class.getDeclaredField("dalytranAmt").getType());
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new DailyTransactionRecord();
        record.setDalytranId("DTR0000000000001");
        record.setDalytranTypeCd("SA");
        record.setDalytranCatCd(5001);
        record.setDalytranSource("BATCH");
        record.setDalytranDesc("Daily batch transaction");
        record.setDalytranAmt(new BigDecimal("250.00"));
        record.setDalytranMerchantId(987654321);
        record.setDalytranMerchantName("Merchant Inc");
        record.setDalytranMerchantCity("Chicago");
        record.setDalytranMerchantZip("60601");
        record.setDalytranCardNum("5500000000000004");
        record.setDalytranOrigTs(LocalDateTime.of(2024, 3, 1, 8, 0, 0));
        record.setDalytranProcTs(LocalDateTime.of(2024, 3, 1, 9, 0, 0));

        assertEquals("DTR0000000000001", record.getDalytranId());
        assertEquals(new BigDecimal("250.00"), record.getDalytranAmt());
    }
}
