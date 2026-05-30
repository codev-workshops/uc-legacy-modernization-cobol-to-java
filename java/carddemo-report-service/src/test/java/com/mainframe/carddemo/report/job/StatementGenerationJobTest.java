package com.mainframe.carddemo.report.job;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.client.TransactionServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import com.mainframe.carddemo.common.dto.CustomerDto;
import com.mainframe.carddemo.common.dto.TransactionDto;
import com.mainframe.carddemo.report.entity.GeneratedReport;
import com.mainframe.carddemo.report.repository.GeneratedReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class StatementGenerationJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("statementGenerationJob")
    private Job statementGenerationJob;

    @Autowired
    private GeneratedReportRepository reportRepository;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private TransactionServiceClient transactionServiceClient;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(statementGenerationJob);
        reportRepository.deleteAll();
    }

    @Test
    void shouldGenerateStatementForAccount() throws Exception {
        AccountDto account = buildAccount(1L, new BigDecimal("2500.00"), new BigDecimal("10000.00"));
        CardXrefDto xref = buildXref("4111111111111111", 1L, 100L);
        CustomerDto customer = buildCustomer(100L, "John", "Doe");

        when(accountServiceClient.getAllAccounts()).thenReturn(List.of(account));
        when(accountServiceClient.getXrefByAccountId(1L)).thenReturn(List.of(xref));
        when(accountServiceClient.getInternalAccountById(1L)).thenReturn(account);
        when(accountServiceClient.getCustomerById(100L)).thenReturn(customer);

        TransactionDto tx = buildTransaction("TX001", "Purchase at Store", new BigDecimal("150.00"));
        when(transactionServiceClient.getTransactionsByCardNum("4111111111111111")).thenReturn(List.of(tx));

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<GeneratedReport> reports = reportRepository.findAll();
        assertEquals(1, reports.size());

        GeneratedReport report = reports.get(0);
        assertEquals(1L, report.getAccountId());
        assertEquals("STATEMENT", report.getReportType());
        assertEquals("John Doe", report.getCustomerName());
        assertTrue(report.getTextContent().contains("ACCOUNT STATEMENT"));
        assertTrue(report.getTextContent().contains("Purchase at Store"));
        assertTrue(report.getHtmlContent().contains("<h1>Account Statement</h1>"));
        assertTrue(report.getHtmlContent().contains("Purchase at Store"));
    }

    @Test
    void shouldIncludeAccountSummaryInStatement() throws Exception {
        AccountDto account = buildAccount(2L, new BigDecimal("3500.00"), new BigDecimal("15000.00"));
        CardXrefDto xref = buildXref("5222222222222222", 2L, 200L);
        CustomerDto customer = buildCustomer(200L, "Jane", "Smith");

        when(accountServiceClient.getAllAccounts()).thenReturn(List.of(account));
        when(accountServiceClient.getXrefByAccountId(2L)).thenReturn(List.of(xref));
        when(accountServiceClient.getInternalAccountById(2L)).thenReturn(account);
        when(accountServiceClient.getCustomerById(200L)).thenReturn(customer);
        when(transactionServiceClient.getTransactionsByCardNum("5222222222222222")).thenReturn(List.of());

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<GeneratedReport> reports = reportRepository.findAll();
        assertEquals(1, reports.size());

        GeneratedReport report = reports.get(0);
        assertTrue(report.getTextContent().contains("3500.00"));
        assertTrue(report.getTextContent().contains("15000.00"));
        assertTrue(report.getHtmlContent().contains("3500.00"));
    }

    @Test
    void shouldHandleEmptyAccountList() throws Exception {
        when(accountServiceClient.getAllAccounts()).thenReturn(List.of());

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(0, reportRepository.count());
    }

    @Test
    void shouldGenerateMultipleStatements() throws Exception {
        AccountDto account1 = buildAccount(10L, new BigDecimal("1000.00"), new BigDecimal("5000.00"));
        AccountDto account2 = buildAccount(20L, new BigDecimal("2000.00"), new BigDecimal("8000.00"));
        CardXrefDto xref1 = buildXref("1111111111111111", 10L, 101L);
        CardXrefDto xref2 = buildXref("2222222222222222", 20L, 102L);

        when(accountServiceClient.getAllAccounts()).thenReturn(List.of(account1, account2));
        when(accountServiceClient.getXrefByAccountId(10L)).thenReturn(List.of(xref1));
        when(accountServiceClient.getXrefByAccountId(20L)).thenReturn(List.of(xref2));
        when(accountServiceClient.getInternalAccountById(10L)).thenReturn(account1);
        when(accountServiceClient.getInternalAccountById(20L)).thenReturn(account2);
        when(accountServiceClient.getCustomerById(101L)).thenReturn(buildCustomer(101L, "Alice", "A"));
        when(accountServiceClient.getCustomerById(102L)).thenReturn(buildCustomer(102L, "Bob", "B"));
        when(transactionServiceClient.getTransactionsByCardNum(anyString())).thenReturn(List.of());

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(2, reportRepository.count());
    }

    private AccountDto buildAccount(Long id, BigDecimal balance, BigDecimal creditLimit) {
        AccountDto account = new AccountDto();
        account.setAccountId(id);
        account.setCurrentBalance(balance);
        account.setCreditLimit(creditLimit);
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        return account;
    }

    private CardXrefDto buildXref(String cardNum, Long acctId, Long custId) {
        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum(cardNum);
        xref.setAccountId(acctId);
        xref.setCustomerId(custId);
        return xref;
    }

    private CustomerDto buildCustomer(Long id, String first, String last) {
        CustomerDto customer = new CustomerDto();
        customer.setCustomerId(id);
        customer.setFirstName(first);
        customer.setLastName(last);
        customer.setAddressLine1("123 Main St");
        customer.setStateCode("CA");
        customer.setZip("90210");
        return customer;
    }

    private TransactionDto buildTransaction(String id, String desc, BigDecimal amt) {
        TransactionDto tx = new TransactionDto();
        tx.setTransactionId(id);
        tx.setDescription(desc);
        tx.setAmount(amt);
        tx.setTypeCd("01");
        tx.setCatCd(1);
        tx.setOrigTimestamp(LocalDateTime.of(2024, 6, 15, 10, 0));
        return tx;
    }
}
