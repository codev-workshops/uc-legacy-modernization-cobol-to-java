package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void userDto() {
        UserDto dto = new UserDto("U001", "John", "Doe", "ADMIN");
        assertEquals("U001", dto.getUserId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("ADMIN", dto.getUserType());

        dto.setUserId("U002");
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setUserType("USER");
        LocalDateTime now = LocalDateTime.now();
        dto.setLastLoginTimestamp(now);

        assertEquals("U002", dto.getUserId());
        assertEquals("Jane", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("USER", dto.getUserType());
        assertEquals(now, dto.getLastLoginTimestamp());

        assertNotNull(new UserDto());
    }

    @Test
    void accountDto() {
        AccountDto dto = new AccountDto();
        dto.setAccountId(1L);
        dto.setAccountStatus("ACTIVE");
        dto.setCurrentBalance(BigDecimal.valueOf(1000));
        dto.setCreditLimit(BigDecimal.valueOf(5000));
        dto.setCashCreditLimit(BigDecimal.valueOf(500));
        dto.setOpenDate(LocalDate.of(2020, 1, 1));
        dto.setExpirationDate(LocalDate.of(2025, 12, 31));
        dto.setReissueDate("20250101");
        dto.setGroupId("GRP1");

        assertEquals(1L, dto.getAccountId());
        assertEquals("ACTIVE", dto.getAccountStatus());
        assertEquals(BigDecimal.valueOf(1000), dto.getCurrentBalance());
        assertEquals(BigDecimal.valueOf(5000), dto.getCreditLimit());
        assertEquals(BigDecimal.valueOf(500), dto.getCashCreditLimit());
        assertEquals(LocalDate.of(2020, 1, 1), dto.getOpenDate());
        assertEquals(LocalDate.of(2025, 12, 31), dto.getExpirationDate());
        assertEquals("20250101", dto.getReissueDate());
        assertEquals("GRP1", dto.getGroupId());
    }

    @Test
    void cardDto() {
        CardDto dto = new CardDto();
        dto.setCardNumber(1234567890L);
        dto.setAccountId(1L);
        dto.setCustomerId(100L);
        dto.setCardStatus("ACTIVE");
        dto.setExpirationDate(LocalDate.of(2025, 12, 31));

        assertEquals(1234567890L, dto.getCardNumber());
        assertEquals(1L, dto.getAccountId());
        assertEquals(100L, dto.getCustomerId());
        assertEquals("ACTIVE", dto.getCardStatus());
        assertEquals(LocalDate.of(2025, 12, 31), dto.getExpirationDate());
    }

    @Test
    void customerDto() {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(1L);
        dto.setFirstName("John");
        dto.setMiddleName("M");
        dto.setLastName("Doe");
        dto.setAddressLine1("123 Main St");
        dto.setAddressLine2("Apt 4");
        dto.setCity("Springfield");
        dto.setState("IL");
        dto.setZipCode("62704");
        dto.setPhone("555-1234");
        dto.setSsn("123-45-6789");
        dto.setFico("750");
        dto.setGovtIssuedId("DL12345");
        dto.setDateOfBirth("19900101");

        assertEquals(1L, dto.getCustomerId());
        assertEquals("John", dto.getFirstName());
        assertEquals("M", dto.getMiddleName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("123 Main St", dto.getAddressLine1());
        assertEquals("Apt 4", dto.getAddressLine2());
        assertEquals("Springfield", dto.getCity());
        assertEquals("IL", dto.getState());
        assertEquals("62704", dto.getZipCode());
        assertEquals("555-1234", dto.getPhone());
        assertEquals("123-45-6789", dto.getSsn());
        assertEquals("750", dto.getFico());
        assertEquals("DL12345", dto.getGovtIssuedId());
        assertEquals("19900101", dto.getDateOfBirth());
    }

    @Test
    void transactionDto() {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionId("TXN001");
        dto.setCardNumber(1234567890L);
        dto.setTransactionTypeCode(1);
        dto.setTransactionCategoryCode(2);
        dto.setTransactionSource("ONLINE");
        dto.setTransactionDescription("Purchase");
        dto.setTransactionAmount(BigDecimal.valueOf(99.99));
        dto.setMerchantId("M001");
        dto.setMerchantName("Store");
        dto.setMerchantCity("Chicago");
        dto.setMerchantZip("60601");
        LocalDate date = LocalDate.of(2024, 3, 15);
        dto.setTransactionDate(date);
        LocalDateTime ts = LocalDateTime.of(2024, 3, 15, 10, 30);
        dto.setTransactionTimestamp(ts);
        dto.setOrigTimestamp(ts);
        dto.setProcessedTimestamp(ts);

        assertEquals("TXN001", dto.getTransactionId());
        assertEquals(1234567890L, dto.getCardNumber());
        assertEquals(1, dto.getTransactionTypeCode());
        assertEquals(2, dto.getTransactionCategoryCode());
        assertEquals("ONLINE", dto.getTransactionSource());
        assertEquals("Purchase", dto.getTransactionDescription());
        assertEquals(BigDecimal.valueOf(99.99), dto.getTransactionAmount());
        assertEquals("M001", dto.getMerchantId());
        assertEquals("Store", dto.getMerchantName());
        assertEquals("Chicago", dto.getMerchantCity());
        assertEquals("60601", dto.getMerchantZip());
        assertEquals(date, dto.getTransactionDate());
        assertEquals(ts, dto.getTransactionTimestamp());
        assertEquals(ts, dto.getOrigTimestamp());
        assertEquals(ts, dto.getProcessedTimestamp());
    }

    @Test
    void cardXrefDto() {
        CardXrefDto dto = new CardXrefDto(1234L, 100L, 200L);
        assertEquals(1234L, dto.getCardNumber());
        assertEquals(100L, dto.getCustomerId());
        assertEquals(200L, dto.getAccountId());

        dto.setCardNumber(5678L);
        dto.setCustomerId(300L);
        dto.setAccountId(400L);
        assertEquals(5678L, dto.getCardNumber());
        assertEquals(300L, dto.getCustomerId());
        assertEquals(400L, dto.getAccountId());

        assertNotNull(new CardXrefDto());
    }

    @Test
    void tranCatBalDto() {
        TranCatBalDto dto = new TranCatBalDto();
        dto.setAccountId(1L);
        dto.setTranTypeCode(1);
        dto.setTranCategoryCode(2);
        dto.setBalance(BigDecimal.valueOf(5000));

        assertEquals(1L, dto.getAccountId());
        assertEquals(1, dto.getTranTypeCode());
        assertEquals(2, dto.getTranCategoryCode());
        assertEquals(BigDecimal.valueOf(5000), dto.getBalance());
    }

    @Test
    void disclosureGroupDto() {
        DisclosureGroupDto dto = new DisclosureGroupDto();
        dto.setGroupId("G1");
        dto.setGroupName("Terms");
        dto.setDisclosureText("Some text");

        assertEquals("G1", dto.getGroupId());
        assertEquals("Terms", dto.getGroupName());
        assertEquals("Some text", dto.getDisclosureText());
    }

    @Test
    void tranTypeDto() {
        TranTypeDto dto = new TranTypeDto(1, "Purchase");
        assertEquals(1, dto.getTypeCode());
        assertEquals("Purchase", dto.getTypeDescription());

        dto.setTypeCode(2);
        dto.setTypeDescription("Refund");
        assertEquals(2, dto.getTypeCode());
        assertEquals("Refund", dto.getTypeDescription());

        assertNotNull(new TranTypeDto());
    }

    @Test
    void tranCategoryDto() {
        TranCategoryDto dto = new TranCategoryDto(1, "Retail");
        assertEquals(1, dto.getCategoryCode());
        assertEquals("Retail", dto.getCategoryDescription());

        dto.setCategoryCode(2);
        dto.setCategoryDescription("Online");
        assertEquals(2, dto.getCategoryCode());
        assertEquals("Online", dto.getCategoryDescription());

        assertNotNull(new TranCategoryDto());
    }

    @Test
    void dailyTransactionDto() {
        DailyTransactionDto dto = new DailyTransactionDto();
        dto.setAccountId(1L);
        dto.setTransactionDate(LocalDate.of(2024, 3, 15));
        dto.setTotalAmount(BigDecimal.valueOf(500));
        dto.setTransactionCount(5);

        assertEquals(1L, dto.getAccountId());
        assertEquals(LocalDate.of(2024, 3, 15), dto.getTransactionDate());
        assertEquals(BigDecimal.valueOf(500), dto.getTotalAmount());
        assertEquals(5, dto.getTransactionCount());
    }
}
