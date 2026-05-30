package com.carddemo.common.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validUser() {
        User u = new User();
        u.setUsrId("ADMIN01");
        u.setFname("John");
        u.setLname("Doe");
        u.setPwd("pass1234");
        u.setUsrType("A");
        Set<ConstraintViolation<User>> violations = validator.validate(u);
        assertTrue(violations.isEmpty());
    }

    @Test
    void userIdBlankViolation() {
        User u = new User();
        u.setUsrId("");
        Set<ConstraintViolation<User>> violations = validator.validate(u);
        assertFalse(violations.isEmpty());
    }

    @Test
    void userIdTooLong() {
        User u = new User();
        u.setUsrId("TOOLONGID");
        Set<ConstraintViolation<User>> violations = validator.validate(u);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validAccount() {
        Account a = new Account();
        a.setAcctId(10000000001L);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal("1940.00"));
        a.setCreditLimit(new BigDecimal("20200.00"));
        Set<ConstraintViolation<Account>> violations = validator.validate(a);
        assertTrue(violations.isEmpty());
    }

    @Test
    void accountNullId() {
        Account a = new Account();
        Set<ConstraintViolation<Account>> violations = validator.validate(a);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validCustomer() {
        Customer c = new Customer();
        c.setCustId(100000001L);
        c.setFirstName("John");
        c.setLastName("Smith");
        c.setStateCode("NY");
        c.setCountryCode("USA");
        Set<ConstraintViolation<Customer>> violations = validator.validate(c);
        assertTrue(violations.isEmpty());
    }

    @Test
    void customerFirstNameTooLong() {
        Customer c = new Customer();
        c.setCustId(1L);
        c.setFirstName("A".repeat(26));
        Set<ConstraintViolation<Customer>> violations = validator.validate(c);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validCardXref() {
        CardXref x = new CardXref();
        x.setXrefCardNum("4500123456789012");
        x.setCustId(100000001L);
        x.setAcctId(10000000001L);
        Set<ConstraintViolation<CardXref>> violations = validator.validate(x);
        assertTrue(violations.isEmpty());
    }

    @Test
    void cardXrefCardNumTooLong() {
        CardXref x = new CardXref();
        x.setXrefCardNum("45001234567890123");
        x.setCustId(1L);
        x.setAcctId(1L);
        Set<ConstraintViolation<CardXref>> violations = validator.validate(x);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validTransaction() {
        Transaction t = new Transaction();
        t.setTranId("0000000000000001");
        t.setTypeCd("01");
        t.setCatCd(1);
        t.setAmt(new BigDecimal("50.47"));
        Set<ConstraintViolation<Transaction>> violations = validator.validate(t);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validTranType() {
        TranType tt = new TranType();
        tt.setTranType("01");
        tt.setTranTypeDesc("Purchase");
        Set<ConstraintViolation<TranType>> violations = validator.validate(tt);
        assertTrue(violations.isEmpty());
    }

    @Test
    void tranTypeBlank() {
        TranType tt = new TranType();
        tt.setTranType("");
        Set<ConstraintViolation<TranType>> violations = validator.validate(tt);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validCard() {
        Card c = new Card();
        c.setCardNum("4500123456789012");
        c.setAcctId(10000000001L);
        c.setCvvCd(123);
        c.setActiveStatus("Y");
        Set<ConstraintViolation<Card>> violations = validator.validate(c);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validTranCategory() {
        TranCategory tc = new TranCategory();
        tc.setTypeCd("01");
        tc.setCatCd(1);
        tc.setTranCatTypeDesc("Regular Sales Draft");
        Set<ConstraintViolation<TranCategory>> violations = validator.validate(tc);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validTranCatBalance() {
        TranCatBalance tcb = new TranCatBalance();
        tcb.setAcctId(10000000001L);
        tcb.setTypeCd("01");
        tcb.setCatCd(1);
        tcb.setTranCatBal(new BigDecimal("500.00"));
        Set<ConstraintViolation<TranCatBalance>> violations = validator.validate(tcb);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validDisclosureGroup() {
        DisclosureGroup dg = new DisclosureGroup();
        dg.setAcctGroupId("A000000000");
        dg.setTypeCd("01");
        dg.setCatCd(1);
        dg.setIntRate(new BigDecimal("15.00"));
        Set<ConstraintViolation<DisclosureGroup>> violations = validator.validate(dg);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validDailyTransaction() {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("0000000000000001");
        dt.setCardNum("4500123456789012");
        dt.setOrigTs("2024-01-15 10:30:00.000000");
        dt.setTypeCd("01");
        dt.setCatCd(1);
        dt.setAmt(new BigDecimal("50.47"));
        Set<ConstraintViolation<DailyTransaction>> violations = validator.validate(dt);
        assertTrue(violations.isEmpty());
    }
}
