package com.carddemo.batch.converter;

import com.carddemo.batch.export.ExportRecord;
import com.carddemo.batch.export.RecordType;
import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecordConverterTest {

    // ---- Customer ----

    @Test
    void customerRoundTrip() {
        Customer orig = buildCustomer();
        String[] fields = RecordConverter.customerToFields(orig);
        ExportRecord record = new ExportRecord(RecordType.CUSTOMER, "ts", 1, "br", "rg", fields);
        Customer result = RecordConverter.toCustomer(record);

        assertEquals(orig.getCustId(), result.getCustId());
        assertEquals(orig.getFirstName(), result.getFirstName());
        assertEquals(orig.getMiddleName(), result.getMiddleName());
        assertEquals(orig.getLastName(), result.getLastName());
        assertEquals(orig.getAddrLine1(), result.getAddrLine1());
        assertEquals(orig.getAddrLine2(), result.getAddrLine2());
        assertEquals(orig.getAddrLine3(), result.getAddrLine3());
        assertEquals(orig.getStateCode(), result.getStateCode());
        assertEquals(orig.getCountryCode(), result.getCountryCode());
        assertEquals(orig.getZip(), result.getZip());
        assertEquals(orig.getPhone1(), result.getPhone1());
        assertEquals(orig.getPhone2(), result.getPhone2());
        assertEquals(orig.getSsn(), result.getSsn());
        assertEquals(orig.getGovtIssuedId(), result.getGovtIssuedId());
        assertEquals(orig.getDob(), result.getDob());
        assertEquals(orig.getEftAccountId(), result.getEftAccountId());
        assertEquals(orig.getPriCardHolderInd(), result.getPriCardHolderInd());
        assertEquals(orig.getFicoCreditScore(), result.getFicoCreditScore());
    }

    @Test
    void customerToFields_nullValues() {
        Customer c = new Customer();
        c.setCustId(null);
        String[] fields = RecordConverter.customerToFields(c);
        assertEquals("", fields[0]);
        assertEquals("", fields[1]);
    }

    // ---- Account ----

    @Test
    void accountRoundTrip() {
        Account orig = buildAccount();
        String[] fields = RecordConverter.accountToFields(orig);
        ExportRecord record = new ExportRecord(RecordType.ACCOUNT, "ts", 1, "br", "rg", fields);
        Account result = RecordConverter.toAccount(record);

        assertEquals(orig.getAcctId(), result.getAcctId());
        assertEquals(orig.getActiveStatus(), result.getActiveStatus());
        assertEquals(0, orig.getCurrBal().compareTo(result.getCurrBal()));
        assertEquals(0, orig.getCreditLimit().compareTo(result.getCreditLimit()));
        assertEquals(0, orig.getCashCreditLimit().compareTo(result.getCashCreditLimit()));
        assertEquals(orig.getOpenDate(), result.getOpenDate());
        assertEquals(orig.getExpirationDate(), result.getExpirationDate());
        assertEquals(orig.getReissueDate(), result.getReissueDate());
        assertEquals(0, orig.getCurrCycCredit().compareTo(result.getCurrCycCredit()));
        assertEquals(0, orig.getCurrCycDebit().compareTo(result.getCurrCycDebit()));
        assertEquals(orig.getAddrZip(), result.getAddrZip());
        assertEquals(orig.getGroupId(), result.getGroupId());
    }

    @Test
    void accountToFields_nullDecimals() {
        Account a = new Account();
        a.setAcctId(1L);
        String[] fields = RecordConverter.accountToFields(a);
        assertEquals("", fields[2]); // currBal
        assertEquals("", fields[3]); // creditLimit
    }

    // ---- CardXref ----

    @Test
    void cardXrefRoundTrip() {
        CardXref orig = buildCardXref();
        String[] fields = RecordConverter.cardXrefToFields(orig);
        assertEquals(3, fields.length);
        ExportRecord record = new ExportRecord(RecordType.CARD_XREF, "ts", 1, "br", "rg", fields);
        CardXref result = RecordConverter.toCardXref(record);

        assertEquals(orig.getXrefCardNum(), result.getXrefCardNum());
        assertEquals(orig.getCustId(), result.getCustId());
        assertEquals(orig.getAcctId(), result.getAcctId());
    }

    // ---- Transaction ----

    @Test
    void transactionRoundTrip() {
        Transaction orig = buildTransaction();
        String[] fields = RecordConverter.transactionToFields(orig);
        assertEquals(13, fields.length);
        ExportRecord record = new ExportRecord(RecordType.TRANSACTION, "ts", 1, "br", "rg", fields);
        Transaction result = RecordConverter.toTransaction(record);

        assertEquals(orig.getTranId(), result.getTranId());
        assertEquals(orig.getTypeCd(), result.getTypeCd());
        assertEquals(orig.getCatCd(), result.getCatCd());
        assertEquals(orig.getSource(), result.getSource());
        assertEquals(orig.getDesc(), result.getDesc());
        assertEquals(0, orig.getAmt().compareTo(result.getAmt()));
        assertEquals(orig.getMerchantId(), result.getMerchantId());
        assertEquals(orig.getMerchantName(), result.getMerchantName());
        assertEquals(orig.getMerchantCity(), result.getMerchantCity());
        assertEquals(orig.getMerchantZip(), result.getMerchantZip());
        assertEquals(orig.getCardNum(), result.getCardNum());
        assertEquals(orig.getOrigTs(), result.getOrigTs());
        assertEquals(orig.getProcTs(), result.getProcTs());
    }

    // ---- Card ----

    @Test
    void cardRoundTrip() {
        Card orig = buildCard();
        String[] fields = RecordConverter.cardToFields(orig);
        assertEquals(6, fields.length);
        ExportRecord record = new ExportRecord(RecordType.CARD, "ts", 1, "br", "rg", fields);
        Card result = RecordConverter.toCard(record);

        assertEquals(orig.getCardNum(), result.getCardNum());
        assertEquals(orig.getAcctId(), result.getAcctId());
        assertEquals(orig.getCvvCd(), result.getCvvCd());
        assertEquals(orig.getEmbossedName(), result.getEmbossedName());
        assertEquals(orig.getExpirationDate(), result.getExpirationDate());
        assertEquals(orig.getActiveStatus(), result.getActiveStatus());
    }

    // ---- TranCatBalance ----

    @Test
    void tranCatBalanceRoundTrip() {
        TranCatBalance orig = buildTranCatBalance();
        String[] fields = RecordConverter.tranCatBalanceToFields(orig);
        assertEquals(4, fields.length);
        ExportRecord record = new ExportRecord(RecordType.TRAN_CAT_BALANCE, "ts", 1, "br", "rg", fields);
        TranCatBalance result = RecordConverter.toTranCatBalance(record);

        assertEquals(orig.getAcctId(), result.getAcctId());
        assertEquals(orig.getTypeCd(), result.getTypeCd());
        assertEquals(orig.getCatCd(), result.getCatCd());
        assertEquals(0, orig.getTranCatBal().compareTo(result.getTranCatBal()));
    }

    // ---- Helpers ----

    @Test
    void nullSafe_null_returnsEmpty() {
        assertEquals("", RecordConverter.nullSafe(null));
    }

    @Test
    void nullSafe_value_returnsValue() {
        assertEquals("abc", RecordConverter.nullSafe("abc"));
    }

    @Test
    void emptyToNull_empty_returnsNull() {
        assertNull(RecordConverter.emptyToNull(""));
    }

    @Test
    void emptyToNull_null_returnsNull() {
        assertNull(RecordConverter.emptyToNull(null));
    }

    @Test
    void emptyToNull_value_returnsValue() {
        assertEquals("abc", RecordConverter.emptyToNull("abc"));
    }

    @Test
    void parseLong_empty_returnsNull() {
        assertNull(RecordConverter.parseLong(""));
    }

    @Test
    void parseLong_value_returnsLong() {
        assertEquals(42L, RecordConverter.parseLong("42"));
    }

    @Test
    void parseInt_empty_returnsNull() {
        assertNull(RecordConverter.parseInt(""));
    }

    @Test
    void parseDecimal_empty_returnsNull() {
        assertNull(RecordConverter.parseDecimal(""));
    }

    @Test
    void parseDecimal_value_returnsDecimal() {
        assertEquals(new BigDecimal("123.45"), RecordConverter.parseDecimal("123.45"));
    }

    // ---- Test data builders ----

    static Customer buildCustomer() {
        Customer c = new Customer();
        c.setCustId(1000001L);
        c.setFirstName("John");
        c.setMiddleName("M");
        c.setLastName("Doe");
        c.setAddrLine1("123 Main St");
        c.setAddrLine2("Apt 4B");
        c.setAddrLine3(null);
        c.setStateCode("NY");
        c.setCountryCode("US");
        c.setZip("10001");
        c.setPhone1("5551234567");
        c.setPhone2(null);
        c.setSsn(123456789L);
        c.setGovtIssuedId("DL12345");
        c.setDob("1990-01-15");
        c.setEftAccountId("EFT001");
        c.setPriCardHolderInd("Y");
        c.setFicoCreditScore(750);
        return c;
    }

    static Account buildAccount() {
        Account a = new Account();
        a.setAcctId(12345678901L);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal("5000.00"));
        a.setCreditLimit(new BigDecimal("10000.00"));
        a.setCashCreditLimit(new BigDecimal("2000.00"));
        a.setOpenDate("2020-01-01");
        a.setExpirationDate("2025-12-31");
        a.setReissueDate("2023-06-15");
        a.setCurrCycCredit(new BigDecimal("500.00"));
        a.setCurrCycDebit(new BigDecimal("250.00"));
        a.setAddrZip("10001");
        a.setGroupId("GRP01");
        return a;
    }

    static CardXref buildCardXref() {
        CardXref x = new CardXref();
        x.setXrefCardNum("4111111111111111");
        x.setCustId(1000001L);
        x.setAcctId(12345678901L);
        return x;
    }

    static Transaction buildTransaction() {
        Transaction t = new Transaction();
        t.setTranId("TRN0000000000001");
        t.setTypeCd("SA");
        t.setCatCd(5001);
        t.setSource("POS");
        t.setDesc("Coffee Shop Purchase");
        t.setAmt(new BigDecimal("4.50"));
        t.setMerchantId(900001L);
        t.setMerchantName("Joe's Coffee");
        t.setMerchantCity("New York");
        t.setMerchantZip("10001");
        t.setCardNum("4111111111111111");
        t.setOrigTs("2024-01-15 10:30:00.000");
        t.setProcTs("2024-01-15 10:30:01.000");
        return t;
    }

    static Card buildCard() {
        Card d = new Card();
        d.setCardNum("4111111111111111");
        d.setAcctId(12345678901L);
        d.setCvvCd(123);
        d.setEmbossedName("JOHN DOE");
        d.setExpirationDate("2025-12-31");
        d.setActiveStatus("Y");
        return d;
    }

    static TranCatBalance buildTranCatBalance() {
        TranCatBalance b = new TranCatBalance();
        b.setAcctId(12345678901L);
        b.setTypeCd("SA");
        b.setCatCd(5001);
        b.setTranCatBal(new BigDecimal("1500.75"));
        return b;
    }
}
