package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatementGenerationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    private StatementGenerationJob statementJob;

    @BeforeEach
    void setUp() {
        statementJob = new StatementGenerationJob(transactionRepository, accountServiceClient);
    }

    @Test
    void testGenerateTextStatement(@TempDir Path tempDir) throws Exception {
        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctCurrBal(new BigDecimal("5000.00"))
                .acctCreditLimit(new BigDecimal("10000.00"))
                .acctCurrCycCredit(new BigDecimal("1000.00"))
                .acctCurrCycDebit(new BigDecimal("2000.00"))
                .build();

        List<Transaction> transactions = List.of(
                Transaction.builder()
                        .tranId("TRN001")
                        .tranTypeCd("01")
                        .tranDesc("Purchase at Store")
                        .tranAmt(new BigDecimal("100.00"))
                        .tranOrigTs("2024-01-15")
                        .tranCardNum("4111111111111111")
                        .build(),
                Transaction.builder()
                        .tranId("TRN002")
                        .tranTypeCd("02")
                        .tranDesc("Payment received")
                        .tranAmt(new BigDecimal("-50.00"))
                        .tranOrigTs("2024-01-16")
                        .tranCardNum("4111111111111111")
                        .build()
        );

        statementJob.generateTextStatement(tempDir, "4111111111111111", account, transactions);

        Path file = tempDir.resolve("statement_4111111111111111.txt");
        assertTrue(Files.exists(file));

        String content = Files.readString(file);
        assertTrue(content.contains("ACCOUNT STATEMENT"));
        assertTrue(content.contains("4111111111111111"));
        assertTrue(content.contains("TRN001"));
        assertTrue(content.contains("TRN002"));
        assertTrue(content.contains("Purchase at Store"));
        assertTrue(content.contains("Payment received"));
        assertTrue(content.contains("Current Balance:"));
        assertTrue(content.contains("Credit Limit:"));
    }

    @Test
    void testGenerateHtmlStatement(@TempDir Path tempDir) throws Exception {
        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctCurrBal(new BigDecimal("5000.00"))
                .acctCreditLimit(new BigDecimal("10000.00"))
                .build();

        List<Transaction> transactions = List.of(
                Transaction.builder()
                        .tranId("TRN001")
                        .tranDesc("Test purchase")
                        .tranAmt(new BigDecimal("100.00"))
                        .tranOrigTs("2024-01-15")
                        .tranCardNum("4111111111111111")
                        .build()
        );

        statementJob.generateHtmlStatement(tempDir, "4111111111111111", account, transactions);

        Path file = tempDir.resolve("statement_4111111111111111.html");
        assertTrue(Files.exists(file));

        String content = Files.readString(file);
        assertTrue(content.contains("<!DOCTYPE html>"));
        assertTrue(content.contains("Account Statement"));
        assertTrue(content.contains("4111111111111111"));
        assertTrue(content.contains("TRN001"));
        assertTrue(content.contains("Current Balance:"));
        assertTrue(content.contains("Credit Limit:"));
    }

    @Test
    void testGenerateTextStatementNullAccount(@TempDir Path tempDir) throws Exception {
        List<Transaction> transactions = List.of(
                Transaction.builder()
                        .tranId("TRN001")
                        .tranDesc("Test")
                        .tranAmt(new BigDecimal("100.00"))
                        .tranOrigTs("2024-01-15")
                        .build()
        );

        statementJob.generateTextStatement(tempDir, "4111111111111111", null, transactions);

        Path file = tempDir.resolve("statement_4111111111111111.txt");
        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("ACCOUNT STATEMENT"));
    }

    @Test
    void testTruncate() {
        assertEquals("abc", StatementGenerationJob.truncate("abc", 5));
        assertEquals("abcde", StatementGenerationJob.truncate("abcdefgh", 5));
    }

    @Test
    void testEscapeHtml() {
        assertEquals("", StatementGenerationJob.escapeHtml(null));
        assertEquals("&amp;", StatementGenerationJob.escapeHtml("&"));
        assertEquals("&lt;b&gt;", StatementGenerationJob.escapeHtml("<b>"));
        assertEquals("&quot;test&quot;", StatementGenerationJob.escapeHtml("\"test\""));
    }
}
