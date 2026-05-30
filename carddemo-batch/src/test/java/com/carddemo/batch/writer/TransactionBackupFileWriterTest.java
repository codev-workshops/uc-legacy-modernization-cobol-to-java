package com.carddemo.batch.writer;

import com.carddemo.common.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionBackupFileWriterTest {

    @TempDir
    Path tempDir;

    private TransactionBackupFileWriter writer;
    private Path outputFile;

    @BeforeEach
    void setUp() {
        outputFile = tempDir.resolve("backup.dat");
        writer = new TransactionBackupFileWriter(outputFile);
        writer.open(new ExecutionContext());
    }

    @AfterEach
    void tearDown() {
        writer.close();
    }

    @Test
    void writeSingleTransaction() throws Exception {
        Transaction txn = createTransaction("0000000000000001", "SA", 5000,
                "ONLINE", "Purchase", new BigDecimal("125.50"), 100000001L,
                "Test Merchant", "Test City", "12345",
                "1234567890123456", "2024-01-15 10:30:00.000000",
                "2024-01-15 11:00:00.000000");

        writer.write(new Chunk<>(List.of(txn)));
        writer.close();

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());
        String line = lines.get(0);
        assertTrue(line.contains("0000000000000001"));
        assertTrue(line.contains("SA"));
        assertTrue(line.contains("125.50"));
        assertTrue(line.contains("Test Merchant"));
        assertTrue(line.contains("20240115"));
    }

    @Test
    void writeMultipleTransactions() throws Exception {
        Transaction txn1 = createTransaction("0000000000000001", "SA", 5000,
                "ONLINE", "Purchase 1", new BigDecimal("100.00"), 100000001L,
                "Merchant A", "City A", "11111",
                "1111111111111111", "2024-01-10 08:00:00.000000", null);
        Transaction txn2 = createTransaction("0000000000000002", "CR", 6000,
                "BATCH", "Refund", new BigDecimal("50.25"), 100000002L,
                "Merchant B", "City B", "22222",
                "2222222222222222", "2024-02-20 14:30:00.000000",
                "2024-02-20 15:00:00.000000");

        writer.write(new Chunk<>(List.of(txn1, txn2)));
        writer.close();

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("0000000000000001"));
        assertTrue(lines.get(1).contains("0000000000000002"));
    }

    @Test
    void writeTransactionWithNullFields() throws Exception {
        Transaction txn = new Transaction();
        txn.setTranId("0000000000000099");

        writer.write(new Chunk<>(List.of(txn)));
        writer.close();

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("0000000000000099|"));
    }

    @Test
    void formatRecordDelimiterCount() {
        Transaction txn = createTransaction("ID001", "SA", 1000,
                "WEB", "Test desc", new BigDecimal("99.99"), 123L,
                "Shop", "Town", "55555",
                "4444333322221111", "2024-03-01 00:00:00.000000",
                "2024-03-01 01:00:00.000000");

        String record = writer.formatRecord(txn);
        String[] fields = record.split("\\|", -1);
        assertEquals(13, fields.length, "Should have 13 pipe-delimited fields");
    }

    @Test
    void formatRecordDateConversion() {
        Transaction txn = createTransaction("ID002", "SA", 2000,
                "POS", "Swipe", new BigDecimal("10.00"), 456L,
                "Store", "Metro", "77777",
                "5555666677778888", "2024-06-15 12:00:00.000000", null);

        String record = writer.formatRecord(txn);
        assertTrue(record.contains("20240615"), "Should contain YYYYMMDD date");
    }

    @Test
    void openCreatesParentDirectories() throws Exception {
        writer.close();

        Path nestedPath = tempDir.resolve("sub/dir/backup.dat");
        TransactionBackupFileWriter nestedWriter = new TransactionBackupFileWriter(nestedPath);
        nestedWriter.open(new ExecutionContext());

        Transaction txn = createTransaction("ID003", "DB", 3000,
                "ATM", "Withdrawal", new BigDecimal("200.00"), 789L,
                "ATM Corp", "Downtown", "33333",
                "9999888877776666", "2024-07-01 09:00:00.000000", null);

        nestedWriter.write(new Chunk<>(List.of(txn)));
        nestedWriter.close();

        assertTrue(Files.exists(nestedPath));
        assertEquals(1, Files.readAllLines(nestedPath).size());
    }

    @Test
    void closeIsIdempotent() {
        writer.close();
        assertDoesNotThrow(() -> writer.close());
    }

    private Transaction createTransaction(String tranId, String typeCd, int catCd,
                                           String source, String desc, BigDecimal amt,
                                           Long merchantId, String merchantName,
                                           String merchantCity, String merchantZip,
                                           String cardNum, String origTs, String procTs) {
        Transaction txn = new Transaction();
        txn.setTranId(tranId);
        txn.setTypeCd(typeCd);
        txn.setCatCd(catCd);
        txn.setSource(source);
        txn.setDesc(desc);
        txn.setAmt(amt);
        txn.setMerchantId(merchantId);
        txn.setMerchantName(merchantName);
        txn.setMerchantCity(merchantCity);
        txn.setMerchantZip(merchantZip);
        txn.setCardNum(cardNum);
        txn.setOrigTs(origTs);
        txn.setProcTs(procTs);
        return txn;
    }
}
