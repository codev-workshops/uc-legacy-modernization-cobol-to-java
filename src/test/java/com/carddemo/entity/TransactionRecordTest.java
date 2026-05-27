package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVTRA05Y.cpy has 13 non-FILLER fields
        assertEquals(13, TransactionRecord.class.getDeclaredFields().length);
    }

    @Test
    void financialFieldsAreBigDecimal() throws NoSuchFieldException {
        assertEquals(BigDecimal.class, TransactionRecord.class.getDeclaredField("tranAmt").getType());
    }

    @Test
    void timestampFieldsAreLocalDateTime() throws NoSuchFieldException {
        assertEquals(LocalDateTime.class, TransactionRecord.class.getDeclaredField("tranOrigTs").getType());
        assertEquals(LocalDateTime.class, TransactionRecord.class.getDeclaredField("tranProcTs").getType());
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new TransactionRecord();
        record.setTranId("TRN0000000000001");
        record.setTranTypeCd("SA");
        record.setTranCatCd(5001);
        record.setTranSource("ONLINE");
        record.setTranDesc("Purchase at Store");
        record.setTranAmt(new BigDecimal("99.99"));
        record.setTranMerchantId(123456789);
        record.setTranMerchantName("Acme Store");
        record.setTranMerchantCity("New York");
        record.setTranMerchantZip("10001");
        record.setTranCardNum("4111111111111111");
        record.setTranOrigTs(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        record.setTranProcTs(LocalDateTime.of(2024, 1, 15, 10, 31, 0));

        assertEquals("TRN0000000000001", record.getTranId());
        assertEquals(new BigDecimal("99.99"), record.getTranAmt());
    }
}
