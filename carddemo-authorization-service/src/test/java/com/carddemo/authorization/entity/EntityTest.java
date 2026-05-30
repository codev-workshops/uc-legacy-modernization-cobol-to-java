package com.carddemo.authorization.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityTest {

    @Test
    void authorization_builder() {
        Authorization auth = Authorization.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authTs(LocalDateTime.of(2025, 1, 15, 10, 30))
                .authType("SALE")
                .authIdCode("ABC123")
                .authRespCode("00")
                .transactionAmt(new BigDecimal("150.00"))
                .approvedAmt(new BigDecimal("150.00"))
                .acctId(1001L)
                .custId(2001L)
                .build();

        assertEquals(1, auth.getAuthId());
        assertEquals("4111111111111111", auth.getCardNum());
        assertEquals("SALE", auth.getAuthType());
        assertEquals("00", auth.getAuthRespCode());
    }

    @Test
    void authFraud_builder() {
        AuthFraud fraud = AuthFraud.builder()
                .cardNum("4111111111111111")
                .authTs(LocalDateTime.of(2025, 1, 15, 10, 30))
                .authFraudFlag("Y")
                .fraudRptDate(LocalDate.of(2025, 1, 15))
                .transactionAmt(new BigDecimal("150.00"))
                .build();

        assertEquals("4111111111111111", fraud.getCardNum());
        assertEquals("Y", fraud.getAuthFraudFlag());
        assertNotNull(fraud.getFraudRptDate());
    }

    @Test
    void authFraudId_equalsAndHashCode() {
        AuthFraudId id1 = new AuthFraudId("4111111111111111", LocalDateTime.of(2025, 1, 15, 10, 30));
        AuthFraudId id2 = new AuthFraudId("4111111111111111", LocalDateTime.of(2025, 1, 15, 10, 30));
        AuthFraudId id3 = new AuthFraudId("5222222222222222", LocalDateTime.of(2025, 1, 15, 10, 30));

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
    }

    @Test
    void authorization_settersAndGetters() {
        Authorization auth = new Authorization();
        auth.setAuthId(1);
        auth.setCardNum("4111111111111111");
        auth.setAuthTs(LocalDateTime.now());
        auth.setCardExpiryDate("1226");
        auth.setMessageType("100000");
        auth.setMessageSource("ONLINE");
        auth.setProcessingCode("000000");
        auth.setMerchantCategoryCode("5411");
        auth.setAcqrCountryCode("840");
        auth.setPosEntryMode((short) 5);
        auth.setMerchantId("MERCH001");
        auth.setMerchantName("Test");
        auth.setMerchantCity("NYC");
        auth.setMerchantState("NY");
        auth.setMerchantZip("10001");
        auth.setTransactionId("TXN001");
        auth.setMatchStatus("N");
        auth.setCreatedAt(LocalDateTime.now());

        assertEquals(1, auth.getAuthId());
        assertEquals("4111111111111111", auth.getCardNum());
        assertEquals("1226", auth.getCardExpiryDate());
        assertEquals("5411", auth.getMerchantCategoryCode());
        assertEquals((short) 5, auth.getPosEntryMode());
    }

    @Test
    void authFraud_settersAndGetters() {
        AuthFraud fraud = new AuthFraud();
        fraud.setCardNum("4111111111111111");
        fraud.setAuthTs(LocalDateTime.now());
        fraud.setAuthType("SALE");
        fraud.setCardExpiryDate("1226");
        fraud.setMessageType("100000");
        fraud.setMessageSource("ONLINE");
        fraud.setAuthIdCode("ABC123");
        fraud.setAuthRespCode("00");
        fraud.setAuthRespReason("APRV");
        fraud.setProcessingCode("000000");
        fraud.setTransactionAmt(new BigDecimal("100.00"));
        fraud.setApprovedAmt(new BigDecimal("100.00"));
        fraud.setMerchantCategoryCode("5411");
        fraud.setAcqrCountryCode("840");
        fraud.setPosEntryMode((short) 5);
        fraud.setMerchantId("MERCH001");
        fraud.setMerchantName("Test");
        fraud.setMerchantCity("NYC");
        fraud.setMerchantState("NY");
        fraud.setMerchantZip("10001");
        fraud.setTransactionId("TXN001");
        fraud.setMatchStatus("N");
        fraud.setAuthFraudFlag("Y");
        fraud.setFraudRptDate(LocalDate.now());
        fraud.setAcctId(1001L);
        fraud.setCustId(2001L);

        assertEquals("4111111111111111", fraud.getCardNum());
        assertEquals("Y", fraud.getAuthFraudFlag());
        assertEquals(1001L, fraud.getAcctId());
    }
}
