package com.carddemo.account.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder()
                .custId(123456789L)
                .custFirstName("John")
                .custMiddleName("M")
                .custLastName("Doe")
                .custAddrLine1("123 Main St")
                .custAddrLine2("Apt 4")
                .custAddrLine3("")
                .custAddrStateCd("NY")
                .custAddrCountryCd("USA")
                .custAddrZip("10001")
                .custPhoneNum1("555-123-4567")
                .custPhoneNum2("555-987-6543")
                .custSsn(123456789L)
                .custGovtIssuedId("DL12345")
                .custDob("1990-05-15")
                .custEftAccountId("EFT001")
                .custPriCardHolderInd("Y")
                .custFicoCreditScore(750)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(123456789L, customer.getCustId());
        assertEquals("John", customer.getCustFirstName());
        assertEquals("M", customer.getCustMiddleName());
        assertEquals("Doe", customer.getCustLastName());
        assertEquals("123 Main St", customer.getCustAddrLine1());
        assertEquals("Apt 4", customer.getCustAddrLine2());
        assertEquals("", customer.getCustAddrLine3());
        assertEquals("NY", customer.getCustAddrStateCd());
        assertEquals("USA", customer.getCustAddrCountryCd());
        assertEquals("10001", customer.getCustAddrZip());
        assertEquals("555-123-4567", customer.getCustPhoneNum1());
        assertEquals("555-987-6543", customer.getCustPhoneNum2());
        assertEquals(123456789L, customer.getCustSsn());
        assertEquals("DL12345", customer.getCustGovtIssuedId());
        assertEquals("1990-05-15", customer.getCustDob());
        assertEquals("EFT001", customer.getCustEftAccountId());
        assertEquals("Y", customer.getCustPriCardHolderInd());
        assertEquals(750, customer.getCustFicoCreditScore());
        assertEquals(now, customer.getCreatedAt());
        assertEquals(now, customer.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Customer customer = new Customer();
        assertNull(customer.getCustId());
        assertNull(customer.getCustFirstName());
    }

    @Test
    void testEqualsAndHashCode() {
        Customer c1 = Customer.builder().custId(1L).custFirstName("John").build();
        Customer c2 = Customer.builder().custId(1L).custFirstName("John").build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
