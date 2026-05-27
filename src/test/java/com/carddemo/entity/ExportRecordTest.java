package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportRecordTest {

    @Test
    void exportRecordHeaderFieldCount() {
        assertEquals(6, ExportRecord.class.getDeclaredFields().length);
    }

    @Test
    void sealedInterfacePermitsCorrectSubclasses() {
        var permitted = ExportRecordData.class.getPermittedSubclasses();
        assertNotNull(permitted);
        assertEquals(5, permitted.length);
    }

    @Test
    void canInstantiateWithCustomerData() {
        var customerData = new CustomerExportData(
                123456789, "John", "M", "Doe",
                List.of("123 Main St", "Apt 4", ""),
                "NY", "USA", "10001",
                List.of("555-123-4567", "555-765-4321"),
                999999999, "DL12345",
                LocalDate.of(1990, 5, 15), "EFT001", "Y",
                new BigDecimal("750")
        );

        var export = new ExportRecord();
        export.setRecType("C");
        export.setTimestamp(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        export.setSequenceNum(1);
        export.setBranchId("BR01");
        export.setRegionCode("US-NE");
        export.setRecordData(customerData);

        assertInstanceOf(CustomerExportData.class, export.getRecordData());
        assertEquals("C", export.getRecType());
    }

    @Test
    void canInstantiateWithAccountData() {
        var accountData = new AccountExportData(
                12345678901L, "Y",
                new BigDecimal("1000.50"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                LocalDate.of(2020, 1, 15), LocalDate.of(2025, 12, 31),
                LocalDate.of(2023, 6, 1),
                new BigDecimal("500.00"), new BigDecimal("200.00"),
                "10001", "GRP001"
        );

        var export = new ExportRecord();
        export.setRecType("A");
        export.setRecordData(accountData);

        assertInstanceOf(AccountExportData.class, export.getRecordData());
    }

    @Test
    void canInstantiateWithTransactionData() {
        var tranData = new TransactionExportData(
                "TRN0000000000001", "SA", 5001, "ONLINE",
                "Purchase at Store", new BigDecimal("99.99"),
                123456789, "Acme Store", "New York", "10001",
                "4111111111111111",
                LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                LocalDateTime.of(2024, 1, 15, 10, 31, 0)
        );

        assertInstanceOf(ExportRecordData.class, tranData);
    }

    @Test
    void canInstantiateWithCardXrefData() {
        var xrefData = new CardXrefExportData("4111111111111111", 123456789, 12345678901L);
        assertInstanceOf(ExportRecordData.class, xrefData);
    }

    @Test
    void canInstantiateWithCardData() {
        var cardData = new CardExportData("4111111111111111", 12345678901L, 123, "JOHN DOE", "2025-12-31", "Y");
        assertInstanceOf(ExportRecordData.class, cardData);
    }
}
