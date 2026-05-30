package com.carddemo.batch.statement;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatementFormatterServiceTest {

    private StatementFormatterService service;

    @BeforeEach
    void setUp() {
        service = new StatementFormatterService();
    }

    @Test
    void formatStatement_producesCorrectLayout() {
        Customer cust = makeCustomer(1001L, "John", "M", "Doe",
                "123 Main St", "Apt 2", "Seattle", "WA", "US", "98101");
        Account acct = makeAccount(12345L, new BigDecimal("5000.00"),
                new BigDecimal("10000.00"));
        cust.setFicoCreditScore(750);

        Transaction tx1 = makeTransaction("TXN001", "Grocery purchase", new BigDecimal("45.50"));
        Transaction tx2 = makeTransaction("TXN002", "Gas station", new BigDecimal("30.00"));

        StatementData data = new StatementData(cust, acct, List.of(tx1, tx2));
        List<String> lines = service.formatStatement(data);

        assertTrue(lines.get(0).contains("START OF STATEMENT"));
        assertTrue(lines.get(lines.size() - 1).contains("END OF STATEMENT"));

        for (String line : lines) {
            assertEquals(StatementFormatterService.LINE_WIDTH, line.length(),
                    "Line must be 80 chars: '" + line + "'");
        }

        assertTrue(lines.stream().anyMatch(l -> l.contains("John M Doe")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("123 Main St")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Account ID")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("12345")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("5000.00")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("10000.00")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("750")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("TXN001")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("TXN002")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Grocery purchase")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("TRANSACTION SUMMARY")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Total Debits:")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Total Credits:")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("New Balance:")));
    }

    @Test
    void formatStatement_withNoTransactions() {
        Customer cust = makeCustomer(1001L, "Jane", null, "Smith",
                "456 Oak Ave", null, "Portland", "OR", "US", "97201");
        Account acct = makeAccount(99999L, BigDecimal.ZERO, new BigDecimal("5000.00"));

        StatementData data = new StatementData(cust, acct, Collections.emptyList());
        List<String> lines = service.formatStatement(data);

        assertTrue(lines.get(0).contains("START OF STATEMENT"));
        assertTrue(lines.get(lines.size() - 1).contains("END OF STATEMENT"));

        for (String line : lines) {
            assertEquals(StatementFormatterService.LINE_WIDTH, line.length());
        }

        assertTrue(lines.stream().anyMatch(l -> l.contains("Jane Smith")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("0.00")));
    }

    @Test
    void formatStatement_withNegativeAmounts() {
        Customer cust = makeCustomer(1001L, "Alice", null, "Wong",
                "789 Pine", null, "LA", "CA", "US", "90001");
        Account acct = makeAccount(55555L, new BigDecimal("1500.00"),
                new BigDecimal("8000.00"));

        Transaction tx1 = makeTransaction("TXN100", "Purchase", new BigDecimal("100.00"));
        Transaction tx2 = makeTransaction("TXN101", "Return", new BigDecimal("-25.00"));

        StatementData data = new StatementData(cust, acct, List.of(tx1, tx2));
        List<String> lines = service.formatStatement(data);

        boolean hasDebits = lines.stream().anyMatch(l -> l.contains("Total Debits:") && l.contains("100.00"));
        boolean hasCredits = lines.stream().anyMatch(l -> l.contains("Total Credits:") && l.contains("25.00"));
        boolean hasNewBal = lines.stream().anyMatch(l -> l.contains("New Balance:") && l.contains("75.00"));

        assertTrue(hasDebits, "Should show total debits of 100.00");
        assertTrue(hasCredits, "Should show total credits of 25.00");
        assertTrue(hasNewBal, "Should show new balance of 75.00");
    }

    @Test
    void buildCustomerName_handlesNullMiddleName() {
        Customer cust = new Customer();
        cust.setFirstName("Bob");
        cust.setMiddleName(null);
        cust.setLastName("Jones");

        assertEquals("Bob Jones", service.buildCustomerName(cust));
    }

    @Test
    void buildCustomerName_handlesBlankMiddleName() {
        Customer cust = new Customer();
        cust.setFirstName("Bob");
        cust.setMiddleName("   ");
        cust.setLastName("Jones");

        assertEquals("Bob Jones", service.buildCustomerName(cust));
    }

    @Test
    void buildAddressLine3_concatenatesFields() {
        Customer cust = new Customer();
        cust.setAddrLine3("Bellevue");
        cust.setStateCode("WA");
        cust.setCountryCode("US");
        cust.setZip("98004");

        assertEquals("Bellevue WA US 98004", service.buildAddressLine3(cust));
    }

    @Test
    void formatAmount_nullReturnsZero() {
        assertEquals("0.00", service.formatAmount(null));
    }

    @Test
    void formatAmount_formatsCorrectly() {
        assertEquals("1234.56", service.formatAmount(new BigDecimal("1234.56")));
    }

    @Test
    void formatTransactionAmount_padsToWidth() {
        String result = service.formatTransactionAmount(new BigDecimal("99.99"));
        assertEquals(12, result.length());
        assertTrue(result.trim().equals("99.99"));
    }

    @Test
    void padAndCenterHelpers() {
        assertEquals("abc   ", StatementFormatterService.pad("abc", 6));
        assertEquals("abc", StatementFormatterService.pad("abcdef", 3));
        assertEquals(" hi ", StatementFormatterService.center("hi", 4));
        assertEquals("abc   ", StatementFormatterService.rpad("abc", 6));
    }

    private Customer makeCustomer(Long id, String first, String middle, String last,
                                   String addr1, String addr2, String city,
                                   String state, String country, String zip) {
        Customer c = new Customer();
        c.setCustId(id);
        c.setFirstName(first);
        c.setMiddleName(middle);
        c.setLastName(last);
        c.setAddrLine1(addr1);
        c.setAddrLine2(addr2);
        c.setAddrLine3(city);
        c.setStateCode(state);
        c.setCountryCode(country);
        c.setZip(zip);
        return c;
    }

    private Account makeAccount(Long id, BigDecimal balance, BigDecimal creditLimit) {
        Account a = new Account();
        a.setAcctId(id);
        a.setCurrBal(balance);
        a.setCreditLimit(creditLimit);
        return a;
    }

    private Transaction makeTransaction(String id, String desc, BigDecimal amt) {
        Transaction t = new Transaction();
        t.setTranId(id);
        t.setDesc(desc);
        t.setAmt(amt);
        return t;
    }
}
