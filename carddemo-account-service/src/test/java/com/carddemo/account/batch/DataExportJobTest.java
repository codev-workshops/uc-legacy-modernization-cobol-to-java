package com.carddemo.account.batch;

import com.carddemo.account.entity.Account;
import com.carddemo.account.entity.Card;
import com.carddemo.account.entity.CardXref;
import com.carddemo.account.entity.Customer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DataExportJobTest {

    @Test
    void formatAccountData_correctFields() {
        Account account = Account.builder()
                .acctId(1L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("194.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("2000.00"))
                .acctOpenDate("2020-01-15")
                .acctExpirationDate("2025-01-15")
                .acctReissueDate("2023-01-15")
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("150.00"))
                .acctAddrZip("10001")
                .acctGroupId("GRP001")
                .build();

        String data = DataExportJob.formatAccountData(account);

        assertTrue(data.startsWith("00000000001Y"));
        assertTrue(data.contains("2020-01-15"));
        assertTrue(data.contains("2025-01-15"));
        assertTrue(data.contains("10001"));
    }

    @Test
    void formatAccountData_nullFields() {
        Account account = Account.builder()
                .acctId(1L)
                .acctActiveStatus("Y")
                .build();

        String data = DataExportJob.formatAccountData(account);
        assertNotNull(data);
        assertTrue(data.startsWith("00000000001Y"));
    }

    @Test
    void formatCustomerData_correctFields() {
        Customer customer = Customer.builder()
                .custId(1L)
                .custFirstName("John")
                .custMiddleName("M")
                .custLastName("Doe")
                .custAddrLine1("123 Main St")
                .custAddrLine2("Apt 4B")
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
                .build();

        String data = DataExportJob.formatCustomerData(customer);

        assertTrue(data.startsWith("000000001John"));
        assertTrue(data.contains("NY"));
        assertTrue(data.contains("USA"));
        assertTrue(data.contains("123456789"));
        assertTrue(data.endsWith("750"));
    }

    @Test
    void formatCustomerData_nullOptionalFields() {
        Customer customer = Customer.builder()
                .custId(42L)
                .custFirstName("Jane")
                .custLastName("Smith")
                .custAddrStateCd("CA")
                .custAddrCountryCd("USA")
                .build();

        String data = DataExportJob.formatCustomerData(customer);
        assertNotNull(data);
        assertTrue(data.startsWith("000000042Jane"));
    }

    @Test
    void formatCardData_correctFields() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .cardAcctId(1L)
                .cardCvvCd(123)
                .cardEmbossedName("JOHN DOE")
                .cardExpirationDate("2025-12-31")
                .cardActiveStatus("Y")
                .build();

        String data = DataExportJob.formatCardData(card);

        assertTrue(data.startsWith("4111111111111111"));
        assertTrue(data.contains("00000000001"));
        assertTrue(data.contains("123"));
        assertTrue(data.contains("JOHN DOE"));
        assertTrue(data.contains("2025-12-31"));
        assertTrue(data.contains("Y"));
    }

    @Test
    void formatCardData_nullCvv() {
        Card card = Card.builder()
                .cardNum("5500000000000004")
                .cardAcctId(2L)
                .cardEmbossedName("JANE DOE")
                .cardExpirationDate("2026-06-30")
                .cardActiveStatus("Y")
                .build();

        String data = DataExportJob.formatCardData(card);
        assertNotNull(data);
        assertTrue(data.contains("000")); // null CVV → 000
    }

    @Test
    void formatCardXrefData_correctFields() {
        CardXref xref = CardXref.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(1L)
                .xrefAcctId(1L)
                .build();

        String data = DataExportJob.formatCardXrefData(xref);

        assertTrue(data.startsWith("4111111111111111"));
        assertTrue(data.contains("000000001"));
        assertTrue(data.contains("00000000001"));
    }

    @Test
    void formatAccountData_negativeBalance() {
        Account account = Account.builder()
                .acctId(5L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("-500.25"))
                .acctCreditLimit(new BigDecimal("10000.00"))
                .acctCashCreditLimit(BigDecimal.ZERO)
                .acctOpenDate("2020-01-01")
                .acctExpirationDate("2025-01-01")
                .acctReissueDate("2023-01-01")
                .acctCurrCycCredit(BigDecimal.ZERO)
                .acctCurrCycDebit(BigDecimal.ZERO)
                .acctAddrZip("12345")
                .acctGroupId("G1")
                .build();

        String data = DataExportJob.formatAccountData(account);
        assertNotNull(data);
        // The signed decimal for -500.25: unscaled=50025, fmt to 12 chars, overpunch '5'→'N'
        assertTrue(data.contains("00000005002N"));
    }
}
