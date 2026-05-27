package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRecordFDTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CUSTREC.cpy has 18 non-FILLER fields
        assertEquals(18, CustomerRecordFD.class.getDeclaredFields().length);
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new CustomerRecordFD();
        record.setCustId(123456789);
        record.setCustFirstName("Jane");
        record.setCustMiddleName("A");
        record.setCustLastName("Smith");
        record.setCustAddrLine1("456 Oak Ave");
        record.setCustAddrLine2("");
        record.setCustAddrLine3("");
        record.setCustAddrStateCd("CA");
        record.setCustAddrCountryCd("USA");
        record.setCustAddrZip("90210");
        record.setCustPhoneNum1("555-111-2222");
        record.setCustPhoneNum2("555-333-4444");
        record.setCustSsn(987654321);
        record.setCustGovtIssuedId("PP98765");
        record.setCustDobYyyymmdd(LocalDate.of(1985, 8, 20));
        record.setCustEftAccountId("EFT002");
        record.setCustPriCardHolderInd("N");
        record.setCustFicoCreditScore(680);

        assertEquals(123456789, record.getCustId());
        assertEquals("Jane", record.getCustFirstName());
        assertEquals(680, record.getCustFicoCreditScore());
    }
}
