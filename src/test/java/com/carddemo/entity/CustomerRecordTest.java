package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVCUS01Y.cpy has 18 non-FILLER fields
        assertEquals(18, CustomerRecord.class.getDeclaredFields().length);
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new CustomerRecord();
        record.setCustId(123456789);
        record.setCustFirstName("John");
        record.setCustMiddleName("M");
        record.setCustLastName("Doe");
        record.setCustAddrLine1("123 Main St");
        record.setCustAddrLine2("Apt 4");
        record.setCustAddrLine3("");
        record.setCustAddrStateCd("NY");
        record.setCustAddrCountryCd("USA");
        record.setCustAddrZip("10001");
        record.setCustPhoneNum1("555-123-4567");
        record.setCustPhoneNum2("555-765-4321");
        record.setCustSsn(123456789);
        record.setCustGovtIssuedId("DL12345");
        record.setCustDobYyyyMmDd(LocalDate.of(1990, 5, 15));
        record.setCustEftAccountId("EFT001");
        record.setCustPriCardHolderInd("Y");
        record.setCustFicoCreditScore(750);

        assertEquals(123456789, record.getCustId());
        assertEquals("John", record.getCustFirstName());
        assertEquals(750, record.getCustFicoCreditScore());
    }
}
