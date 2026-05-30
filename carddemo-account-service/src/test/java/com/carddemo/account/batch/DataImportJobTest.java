package com.carddemo.account.batch;

import com.carddemo.account.entity.Account;
import com.carddemo.account.entity.Card;
import com.carddemo.account.entity.CardXref;
import com.carddemo.account.entity.Customer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DataImportJobTest {

    @Test
    void mapAccount_parsesFixedWidthRecord() {
        // Build a line matching the acctdata.txt format (300 chars)
        String line = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{10001     A000000000";
        line = padTo(line, 300);

        Account account = DataImportJob.mapAccount(line);

        assertEquals(1L, account.getAcctId());
        assertEquals("Y", account.getAcctActiveStatus());
        assertEquals(new BigDecimal("194.00"), account.getAcctCurrBal());
        assertEquals(new BigDecimal("2020.00"), account.getAcctCreditLimit());
        assertEquals(new BigDecimal("1020.00"), account.getAcctCashCreditLimit());
        assertEquals("2014-11-20", account.getAcctOpenDate());
        assertEquals("2025-05-20", account.getAcctExpirationDate());
        assertEquals("2025-05-20", account.getAcctReissueDate());
        assertEquals(new BigDecimal("0.00"), account.getAcctCurrCycCredit());
        assertEquals(new BigDecimal("0.00"), account.getAcctCurrCycDebit());
        assertEquals("10001", account.getAcctAddrZip());
        assertEquals("A000000000", account.getAcctGroupId());
    }

    @Test
    void mapAccount_handlesNegativeBalances() {
        // Negative balance with overpunch
        String line = "00000000002N00000001940}00000020200{00000010200{2013-06-192024-08-112024-08-1100000000010100000000005K12345     GRP002    ";
        line = padTo(line, 300);

        Account account = DataImportJob.mapAccount(line);

        assertEquals(2L, account.getAcctId());
        assertEquals("N", account.getAcctActiveStatus());
        assertEquals(new BigDecimal("-194.00"), account.getAcctCurrBal());
        assertEquals(new BigDecimal("2020.00"), account.getAcctCreditLimit());
        assertEquals(new BigDecimal("1020.00"), account.getAcctCashCreditLimit());
        assertEquals(new BigDecimal("1.01"), account.getAcctCurrCycCredit());
        assertEquals(new BigDecimal("-0.52"), account.getAcctCurrCycDebit());
    }

    @Test
    void mapAccount_shortLine() {
        String line = "00000000099Y";
        Account account = DataImportJob.mapAccount(line);
        assertEquals(99L, account.getAcctId());
        assertEquals("Y", account.getAcctActiveStatus());
    }

    @Test
    void mapCustomer_parsesFixedWidthRecord() {
        StringBuilder sb = new StringBuilder(500);
        sb.append("000000001");                                         // cust_id (9)
        sb.append(padRight("John", 25));                                // first_name
        sb.append(padRight("Michael", 25));                             // middle_name
        sb.append(padRight("Doe", 25));                                 // last_name
        sb.append(padRight("123 Main Street", 50));                     // addr_line_1
        sb.append(padRight("Apt 4B", 50));                              // addr_line_2
        sb.append(padRight("", 50));                                    // addr_line_3
        sb.append("NY");                                                // state_cd (2)
        sb.append("USA");                                               // country_cd (3)
        sb.append(padRight("10001", 10));                               // zip
        sb.append(padRight("555-123-4567", 15));                        // phone1
        sb.append(padRight("555-987-6543", 15));                        // phone2
        sb.append("123456789");                                         // ssn (9)
        sb.append(padRight("DL12345678", 20));                          // govt_issued_id
        sb.append(padRight("1990-05-15", 10));                          // dob
        sb.append(padRight("EFT001", 10));                              // eft_acct_id
        sb.append("Y");                                                 // pri_card_holder
        sb.append("750");                                               // fico_score (3)
        String line = padTo(sb.toString(), 500);

        Customer customer = DataImportJob.mapCustomer(line);

        assertEquals(1L, customer.getCustId());
        assertEquals("John", customer.getCustFirstName());
        assertEquals("Michael", customer.getCustMiddleName());
        assertEquals("Doe", customer.getCustLastName());
        assertEquals("123 Main Street", customer.getCustAddrLine1());
        assertEquals("Apt 4B", customer.getCustAddrLine2());
        assertEquals("NY", customer.getCustAddrStateCd());
        assertEquals("USA", customer.getCustAddrCountryCd());
        assertEquals("10001", customer.getCustAddrZip());
        assertEquals("555-123-4567", customer.getCustPhoneNum1());
        assertEquals("555-987-6543", customer.getCustPhoneNum2());
        assertEquals(123456789L, customer.getCustSsn());
        assertEquals("DL12345678", customer.getCustGovtIssuedId());
        assertEquals("1990-05-15", customer.getCustDob());
        assertEquals("EFT001", customer.getCustEftAccountId());
        assertEquals("Y", customer.getCustPriCardHolderInd());
        assertEquals(750, customer.getCustFicoCreditScore());
    }

    @Test
    void mapCard_parsesFixedWidthRecord() {
        StringBuilder sb = new StringBuilder(150);
        sb.append(padRight("4111111111111111", 16));  // card_num
        sb.append("00000000001");                      // card_acct_id (11)
        sb.append("747");                              // card_cvv_cd (3)
        sb.append(padRight("JOHN DOE", 50));           // embossed_name
        sb.append(padRight("2025-12-31", 10));         // expiration_date
        sb.append("Y");                                // active_status
        String line = padTo(sb.toString(), 150);

        Card card = DataImportJob.mapCard(line);

        assertEquals("4111111111111111", card.getCardNum());
        assertEquals(1L, card.getCardAcctId());
        assertEquals(747, card.getCardCvvCd());
        assertEquals("JOHN DOE", card.getCardEmbossedName());
        assertEquals("2025-12-31", card.getCardExpirationDate());
        assertEquals("Y", card.getCardActiveStatus());
    }

    @Test
    void mapCardXref_parsesFixedWidthRecord() {
        StringBuilder sb = new StringBuilder(50);
        sb.append(padRight("4111111111111111", 16));  // xref_card_num
        sb.append("000000001");                        // xref_cust_id (9)
        sb.append("00000000001");                      // xref_acct_id (11)
        String line = padTo(sb.toString(), 50);

        CardXref xref = DataImportJob.mapCardXref(line);

        assertEquals("4111111111111111", xref.getXrefCardNum());
        assertEquals(1L, xref.getXrefCustId());
        assertEquals(1L, xref.getXrefAcctId());
    }

    @Test
    void mapCard_withLargeAcctId() {
        StringBuilder sb = new StringBuilder(150);
        sb.append(padRight("5500000000000004", 16));
        sb.append("99999999999");                        // max 11-digit acct_id
        sb.append("999");
        sb.append(padRight("MAX ACCT TEST", 50));
        sb.append(padRight("2030-12-31", 10));
        sb.append("Y");
        String line = padTo(sb.toString(), 150);

        Card card = DataImportJob.mapCard(line);
        assertEquals(99999999999L, card.getCardAcctId());
    }

    @Test
    void mapCardXref_withVariousIds() {
        StringBuilder sb = new StringBuilder(50);
        sb.append(padRight("9876543210123456", 16));
        sb.append("000000050");
        sb.append("00000000100");
        String line = padTo(sb.toString(), 50);

        CardXref xref = DataImportJob.mapCardXref(line);
        assertEquals("9876543210123456", xref.getXrefCardNum());
        assertEquals(50L, xref.getXrefCustId());
        assertEquals(100L, xref.getXrefAcctId());
    }

    private static String padRight(String value, int length) {
        if (value == null) value = "";
        return String.format("%-" + length + "s", value);
    }

    private static String padTo(String value, int length) {
        if (value.length() >= length) return value.substring(0, length);
        return String.format("%-" + length + "s", value);
    }
}
