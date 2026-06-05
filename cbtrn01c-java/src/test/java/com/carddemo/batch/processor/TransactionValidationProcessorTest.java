package com.carddemo.batch.processor;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.XrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionValidationProcessor.
 * Tests three scenarios: valid card+account, invalid card, valid card but missing account.
 */
class TransactionValidationProcessorTest {

    @TempDir
    Path tempDir;

    private TransactionValidationProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        // Create XREF file with one record: card "4859452612877065" → custId=1, acctId=1
        Path xrefFile = tempDir.resolve("cardxref.txt");
        String xrefRecord = padRight("4859452612877065", 16) + padLeft("1", 9) + padLeft("1", 11) + "              ";
        Files.write(xrefFile, List.of(xrefRecord));

        // Create account file with one record: acctId=1
        Path acctFile = tempDir.resolve("acctdata.txt");
        String acctRecord = buildAccountRecord(1);
        Files.write(acctFile, List.of(acctRecord));

        XrefRepository xrefRepo = new XrefRepository(xrefFile);
        AccountRepository acctRepo = new AccountRepository(acctFile);
        processor = new TransactionValidationProcessor(xrefRepo, acctRepo);
    }

    @Test
    void shouldReturnTransactionWhenCardAndAccountExist() {
        DailyTransaction txn = buildTransaction("4859452612877065");
        DailyTransaction result = processor.process(txn);
        assertNotNull(result);
        assertEquals(txn, result);
    }

    @Test
    void shouldReturnNullWhenCardNotInXref() {
        DailyTransaction txn = buildTransaction("9999999999999999");
        DailyTransaction result = processor.process(txn);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenAccountNotFound() throws IOException {
        // Create XREF that maps to account 99 which doesn't exist
        Path xrefFile = tempDir.resolve("cardxref2.txt");
        String xrefRecord = padRight("1111222233334444", 16) + padLeft("1", 9) + padLeft("99", 11) + "              ";
        Files.write(xrefFile, List.of(xrefRecord));

        Path acctFile = tempDir.resolve("acctdata2.txt");
        String acctRecord = buildAccountRecord(1); // only acctId=1 exists
        Files.write(acctFile, List.of(acctRecord));

        XrefRepository xrefRepo = new XrefRepository(xrefFile);
        AccountRepository acctRepo = new AccountRepository(acctFile);
        TransactionValidationProcessor proc = new TransactionValidationProcessor(xrefRepo, acctRepo);

        DailyTransaction txn = buildTransaction("1111222233334444");
        DailyTransaction result = proc.process(txn);
        assertNull(result);
    }

    private DailyTransaction buildTransaction(String cardNum) {
        return new DailyTransaction(
                "0000000000000001", "01", 1, "POS TERM",
                "Test purchase", new BigDecimal("100.00"), 123456789L,
                "Test Merchant", "Test City", "12345",
                cardNum, "2023-01-01 00:00:00.000000", ""
        );
    }

    private String buildAccountRecord(long acctId) {
        StringBuilder sb = new StringBuilder();
        sb.append(padLeft(String.valueOf(acctId), 11));  // ACCT-ID
        sb.append("Y");                                   // ACCT-ACTIVE-STATUS
        sb.append("00000001940{");                        // ACCT-CURR-BAL (19400 → 194.00)
        sb.append("00000020200{");                        // ACCT-CREDIT-LIMIT
        sb.append("00000010200{");                        // ACCT-CASH-CREDIT-LIMIT
        sb.append("2014-11-20");                         // ACCT-OPEN-DATE
        sb.append("2025-05-20");                         // ACCT-EXPIRAION-DATE
        sb.append("2025-05-20");                         // ACCT-REISSUE-DATE
        sb.append("00000000000{");                        // ACCT-CURR-CYC-CREDIT
        sb.append("00000000000{");                        // ACCT-CURR-CYC-DEBIT
        sb.append("A000000000");                         // ACCT-ADDR-ZIP
        sb.append("          ");                         // ACCT-GROUP-ID
        sb.append(" ".repeat(178));                      // FILLER
        return sb.toString();
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    private static String padLeft(String s, int len) {
        if (s.length() >= len) return s.substring(0, len);
        return "0".repeat(len - s.length()) + s;
    }
}
