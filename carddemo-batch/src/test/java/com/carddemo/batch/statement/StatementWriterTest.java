package com.carddemo.batch.statement;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatementWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesFormattedStatements() throws Exception {
        String outputPath = tempDir.resolve("stmt.txt").toString();
        StatementFormatterService formatter = mock(StatementFormatterService.class);
        when(formatter.formatStatement(any())).thenReturn(
                List.of("LINE1".concat(" ".repeat(75)),
                        "LINE2".concat(" ".repeat(75))));

        StatementWriter writer = new StatementWriter(outputPath, formatter);
        writer.beforeStep(null);

        Customer cust = new Customer();
        cust.setCustId(1L);
        Account acct = new Account();
        acct.setAcctId(1L);
        StatementData data = new StatementData(cust, acct, List.of());

        writer.write(new Chunk<>(List.of(data)));
        writer.afterStep(null);

        List<String> lines = Files.readAllLines(Path.of(outputPath));
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).startsWith("LINE1"));
        assertTrue(lines.get(1).startsWith("LINE2"));
    }

    @Test
    void multipleStatements_separatedByBlankLine() throws Exception {
        String outputPath = tempDir.resolve("multi.txt").toString();
        StatementFormatterService formatter = new StatementFormatterService();

        StatementWriter writer = new StatementWriter(outputPath, formatter);
        writer.beforeStep(null);

        StatementData data1 = makeData(1L, "Alice", "Wonderland");
        StatementData data2 = makeData(2L, "Bob", "Builder");

        writer.write(new Chunk<>(List.of(data1, data2)));
        writer.afterStep(null);

        String content = Files.readString(Path.of(outputPath));
        assertTrue(content.contains("END OF STATEMENT"));
        long stmtCount = content.lines()
                .filter(l -> l.contains("START OF STATEMENT"))
                .count();
        assertEquals(2, stmtCount, "Should contain two statements");
    }

    private StatementData makeData(Long acctId, String firstName, String lastName) {
        Customer cust = new Customer();
        cust.setCustId(acctId);
        cust.setFirstName(firstName);
        cust.setLastName(lastName);
        Account acct = new Account();
        acct.setAcctId(acctId);
        acct.setCurrBal(BigDecimal.ZERO);
        acct.setCreditLimit(new BigDecimal("5000.00"));

        Transaction tx = new Transaction();
        tx.setTranId("TXN" + acctId);
        tx.setDesc("Test");
        tx.setAmt(new BigDecimal("10.00"));

        return new StatementData(cust, acct, List.of(tx));
    }
}
