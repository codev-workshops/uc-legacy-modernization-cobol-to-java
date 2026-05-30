package com.carddemo.authorization.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthorizationDtoTest {

    @Test
    void builder_andGetters() {
        LocalDateTime now = LocalDateTime.now();
        AuthorizationDto dto = AuthorizationDto.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authTs(now)
                .authType("SALE")
                .cardExpiryDate("1226")
                .messageType("100000")
                .messageSource("ONLINE")
                .authIdCode("ABC123")
                .authRespCode("00")
                .authRespReason("APRV")
                .processingCode("000000")
                .transactionAmt(new BigDecimal("150.00"))
                .approvedAmt(new BigDecimal("150.00"))
                .merchantCategoryCode("5411")
                .acqrCountryCode("840")
                .posEntryMode((short) 5)
                .merchantId("MERCH001")
                .merchantName("Test Merchant")
                .merchantCity("New York")
                .merchantState("NY")
                .merchantZip("10001")
                .transactionId("TXN001")
                .matchStatus("N")
                .acctId(1001L)
                .custId(2001L)
                .createdAt(now)
                .build();

        assertEquals(1, dto.getAuthId());
        assertEquals("4111111111111111", dto.getCardNum());
        assertEquals(now, dto.getAuthTs());
        assertEquals("SALE", dto.getAuthType());
        assertEquals("1226", dto.getCardExpiryDate());
        assertEquals("100000", dto.getMessageType());
        assertEquals("ONLINE", dto.getMessageSource());
        assertEquals("ABC123", dto.getAuthIdCode());
        assertEquals("00", dto.getAuthRespCode());
        assertEquals("APRV", dto.getAuthRespReason());
        assertEquals("000000", dto.getProcessingCode());
        assertEquals(new BigDecimal("150.00"), dto.getTransactionAmt());
        assertEquals(new BigDecimal("150.00"), dto.getApprovedAmt());
        assertEquals("5411", dto.getMerchantCategoryCode());
        assertEquals("840", dto.getAcqrCountryCode());
        assertEquals((short) 5, dto.getPosEntryMode());
        assertEquals("MERCH001", dto.getMerchantId());
        assertEquals("Test Merchant", dto.getMerchantName());
        assertEquals("New York", dto.getMerchantCity());
        assertEquals("NY", dto.getMerchantState());
        assertEquals("10001", dto.getMerchantZip());
        assertEquals("TXN001", dto.getTransactionId());
        assertEquals("N", dto.getMatchStatus());
        assertEquals(1001L, dto.getAcctId());
        assertEquals(2001L, dto.getCustId());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void noArgsConstructor_andSetters() {
        AuthorizationDto dto = new AuthorizationDto();
        dto.setAuthId(1);
        dto.setCardNum("4111111111111111");
        assertEquals(1, dto.getAuthId());
        assertEquals("4111111111111111", dto.getCardNum());
    }

    @Test
    void allArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        AuthorizationDto dto = new AuthorizationDto(1, "4111111111111111", now, "SALE", "1226",
                "100000", "ONLINE", "ABC123", "00", "APRV", "000000",
                new BigDecimal("150.00"), new BigDecimal("150.00"), "5411", "840",
                (short) 5, "MERCH001", "Test", "NYC", "NY", "10001", "TXN001", "N",
                1001L, 2001L, now);
        assertNotNull(dto);
        assertEquals(1, dto.getAuthId());
    }
}
