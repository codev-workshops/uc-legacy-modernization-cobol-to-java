package com.carddemo.batch.converter;

import com.carddemo.batch.export.ExportRecord;
import com.carddemo.batch.export.RecordType;
import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.Transaction;

import java.math.BigDecimal;

/**
 * Converts between JPA entities and {@link ExportRecord} field arrays.
 * Field ordering follows the CVEXPORT.cpy copybook layout for each record type.
 */
public final class RecordConverter {

    private RecordConverter() {}

    // ---- Customer (C) ----

    public static String[] customerToFields(Customer c) {
        return new String[] {
            longToStr(c.getCustId()),
            nullSafe(c.getFirstName()),
            nullSafe(c.getMiddleName()),
            nullSafe(c.getLastName()),
            nullSafe(c.getAddrLine1()),
            nullSafe(c.getAddrLine2()),
            nullSafe(c.getAddrLine3()),
            nullSafe(c.getStateCode()),
            nullSafe(c.getCountryCode()),
            nullSafe(c.getZip()),
            nullSafe(c.getPhone1()),
            nullSafe(c.getPhone2()),
            longToStr(c.getSsn()),
            nullSafe(c.getGovtIssuedId()),
            nullSafe(c.getDob()),
            nullSafe(c.getEftAccountId()),
            nullSafe(c.getPriCardHolderInd()),
            intToStr(c.getFicoCreditScore())
        };
    }

    public static Customer toCustomer(ExportRecord record) {
        String[] f = record.getFields();
        Customer c = new Customer();
        c.setCustId(parseLong(f[0]));
        c.setFirstName(emptyToNull(f[1]));
        c.setMiddleName(emptyToNull(f[2]));
        c.setLastName(emptyToNull(f[3]));
        c.setAddrLine1(emptyToNull(f[4]));
        c.setAddrLine2(emptyToNull(f[5]));
        c.setAddrLine3(emptyToNull(f[6]));
        c.setStateCode(emptyToNull(f[7]));
        c.setCountryCode(emptyToNull(f[8]));
        c.setZip(emptyToNull(f[9]));
        c.setPhone1(emptyToNull(f[10]));
        c.setPhone2(emptyToNull(f[11]));
        c.setSsn(parseLong(f[12]));
        c.setGovtIssuedId(emptyToNull(f[13]));
        c.setDob(emptyToNull(f[14]));
        c.setEftAccountId(emptyToNull(f[15]));
        c.setPriCardHolderInd(emptyToNull(f[16]));
        c.setFicoCreditScore(parseInt(f[17]));
        return c;
    }

    // ---- Account (A) ----

    public static String[] accountToFields(Account a) {
        return new String[] {
            longToStr(a.getAcctId()),
            nullSafe(a.getActiveStatus()),
            decimalToStr(a.getCurrBal()),
            decimalToStr(a.getCreditLimit()),
            decimalToStr(a.getCashCreditLimit()),
            nullSafe(a.getOpenDate()),
            nullSafe(a.getExpirationDate()),
            nullSafe(a.getReissueDate()),
            decimalToStr(a.getCurrCycCredit()),
            decimalToStr(a.getCurrCycDebit()),
            nullSafe(a.getAddrZip()),
            nullSafe(a.getGroupId())
        };
    }

    public static Account toAccount(ExportRecord record) {
        String[] f = record.getFields();
        Account a = new Account();
        a.setAcctId(parseLong(f[0]));
        a.setActiveStatus(emptyToNull(f[1]));
        a.setCurrBal(parseDecimal(f[2]));
        a.setCreditLimit(parseDecimal(f[3]));
        a.setCashCreditLimit(parseDecimal(f[4]));
        a.setOpenDate(emptyToNull(f[5]));
        a.setExpirationDate(emptyToNull(f[6]));
        a.setReissueDate(emptyToNull(f[7]));
        a.setCurrCycCredit(parseDecimal(f[8]));
        a.setCurrCycDebit(parseDecimal(f[9]));
        a.setAddrZip(emptyToNull(f[10]));
        a.setGroupId(emptyToNull(f[11]));
        return a;
    }

    // ---- CardXref (X) ----

    public static String[] cardXrefToFields(CardXref x) {
        return new String[] {
            nullSafe(x.getXrefCardNum()),
            longToStr(x.getCustId()),
            longToStr(x.getAcctId())
        };
    }

    public static CardXref toCardXref(ExportRecord record) {
        String[] f = record.getFields();
        CardXref x = new CardXref();
        x.setXrefCardNum(emptyToNull(f[0]));
        x.setCustId(parseLong(f[1]));
        x.setAcctId(parseLong(f[2]));
        return x;
    }

    // ---- Transaction (T) ----

    public static String[] transactionToFields(Transaction t) {
        return new String[] {
            nullSafe(t.getTranId()),
            nullSafe(t.getTypeCd()),
            intToStr(t.getCatCd()),
            nullSafe(t.getSource()),
            nullSafe(t.getDesc()),
            decimalToStr(t.getAmt()),
            longToStr(t.getMerchantId()),
            nullSafe(t.getMerchantName()),
            nullSafe(t.getMerchantCity()),
            nullSafe(t.getMerchantZip()),
            nullSafe(t.getCardNum()),
            nullSafe(t.getOrigTs()),
            nullSafe(t.getProcTs())
        };
    }

    public static Transaction toTransaction(ExportRecord record) {
        String[] f = record.getFields();
        Transaction t = new Transaction();
        t.setTranId(emptyToNull(f[0]));
        t.setTypeCd(emptyToNull(f[1]));
        t.setCatCd(parseInt(f[2]));
        t.setSource(emptyToNull(f[3]));
        t.setDesc(emptyToNull(f[4]));
        t.setAmt(parseDecimal(f[5]));
        t.setMerchantId(parseLong(f[6]));
        t.setMerchantName(emptyToNull(f[7]));
        t.setMerchantCity(emptyToNull(f[8]));
        t.setMerchantZip(emptyToNull(f[9]));
        t.setCardNum(emptyToNull(f[10]));
        t.setOrigTs(emptyToNull(f[11]));
        t.setProcTs(emptyToNull(f[12]));
        return t;
    }

    // ---- Card (D) ----

    public static String[] cardToFields(Card d) {
        return new String[] {
            nullSafe(d.getCardNum()),
            longToStr(d.getAcctId()),
            intToStr(d.getCvvCd()),
            nullSafe(d.getEmbossedName()),
            nullSafe(d.getExpirationDate()),
            nullSafe(d.getActiveStatus())
        };
    }

    public static Card toCard(ExportRecord record) {
        String[] f = record.getFields();
        Card d = new Card();
        d.setCardNum(emptyToNull(f[0]));
        d.setAcctId(parseLong(f[1]));
        d.setCvvCd(parseInt(f[2]));
        d.setEmbossedName(emptyToNull(f[3]));
        d.setExpirationDate(emptyToNull(f[4]));
        d.setActiveStatus(emptyToNull(f[5]));
        return d;
    }

    // ---- TranCatBalance (B) ----

    public static String[] tranCatBalanceToFields(TranCatBalance b) {
        return new String[] {
            longToStr(b.getAcctId()),
            nullSafe(b.getTypeCd()),
            intToStr(b.getCatCd()),
            decimalToStr(b.getTranCatBal())
        };
    }

    public static TranCatBalance toTranCatBalance(ExportRecord record) {
        String[] f = record.getFields();
        TranCatBalance b = new TranCatBalance();
        b.setAcctId(parseLong(f[0]));
        b.setTypeCd(emptyToNull(f[1]));
        b.setCatCd(parseInt(f[2]));
        b.setTranCatBal(parseDecimal(f[3]));
        return b;
    }

    // ---- Helpers ----

    static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    static String longToStr(Long value) {
        return value == null ? "" : value.toString();
    }

    static String intToStr(Integer value) {
        return value == null ? "" : value.toString();
    }

    static String decimalToStr(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    static Long parseLong(String value) {
        return (value == null || value.isEmpty()) ? null : Long.parseLong(value);
    }

    static Integer parseInt(String value) {
        return (value == null || value.isEmpty()) ? null : Integer.parseInt(value);
    }

    static BigDecimal parseDecimal(String value) {
        return (value == null || value.isEmpty()) ? null : new BigDecimal(value);
    }
}
