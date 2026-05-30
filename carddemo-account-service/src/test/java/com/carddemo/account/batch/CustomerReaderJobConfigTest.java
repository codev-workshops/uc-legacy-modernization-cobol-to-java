package com.carddemo.account.batch;

import com.carddemo.account.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerReaderJobConfigTest {

    @Test
    void testFormatCustomerFixedWidth() {
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
                .custSsn(987654321L)
                .custGovtIssuedId("DL12345")
                .custDob("1990-05-15")
                .custEftAccountId("EFT001")
                .custPriCardHolderInd("Y")
                .custFicoCreditScore(750)
                .build();

        String result = CustomerReaderJobConfig.formatCustomer(customer);
        assertEquals(500, result.length());
        assertEquals("123456789", result.substring(0, 9));
        // first name starts at 9, 25 chars
        assertTrue(result.substring(9, 34).startsWith("John"));
    }

    @Test
    void testFormatCustomerWithNulls() {
        Customer customer = Customer.builder()
                .custId(1L)
                .build();

        String result = CustomerReaderJobConfig.formatCustomer(customer);
        assertEquals(500, result.length());
        assertEquals("000000001", result.substring(0, 9));
    }

    @Test
    void testFormatCustomerFieldPositions() {
        Customer customer = Customer.builder()
                .custId(100L)
                .custFirstName("Alice")
                .custMiddleName("B")
                .custLastName("Smith")
                .custAddrLine1("456 Oak Ave")
                .custAddrLine2("")
                .custAddrLine3("")
                .custAddrStateCd("CA")
                .custAddrCountryCd("USA")
                .custAddrZip("90210")
                .custPhoneNum1("310-555-1234")
                .custPhoneNum2("")
                .custSsn(111223333L)
                .custGovtIssuedId("PP12345")
                .custDob("1985-03-20")
                .custEftAccountId("EFT002")
                .custPriCardHolderInd("N")
                .custFicoCreditScore(680)
                .build();

        String result = CustomerReaderJobConfig.formatCustomer(customer);
        assertEquals(500, result.length());
        // Verify SSN position: 9+25+25+25+50+50+50+2+3+10+15+15 = 279
        assertEquals("111223333", result.substring(279, 288));
    }
}
