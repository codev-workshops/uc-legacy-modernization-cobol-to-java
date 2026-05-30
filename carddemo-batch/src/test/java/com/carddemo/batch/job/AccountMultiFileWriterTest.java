package com.carddemo.batch.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carddemo.batch.model.AccountOutputBundle;
import com.carddemo.batch.model.ArryfileRecord;
import com.carddemo.batch.model.OutfileRecord;
import com.carddemo.batch.model.VbrcRec1;
import com.carddemo.batch.model.VbrcRec2;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;

class AccountMultiFileWriterTest {

    @TempDir
    Path tempDir;

    private AccountMultiFileWriter writer;
    private Path outfilePath;
    private Path arryfilePath;
    private Path vbrcfilePath;

    @BeforeEach
    void setUp() {
        outfilePath = tempDir.resolve("OUTFILE.dat");
        arryfilePath = tempDir.resolve("ARRYFILE.dat");
        vbrcfilePath = tempDir.resolve("VBRCFILE.dat");
        writer = new AccountMultiFileWriter(outfilePath, arryfilePath, vbrcfilePath);
    }

    private AccountOutputBundle createBundle() {
        OutfileRecord out = new OutfileRecord();
        out.setAcctId(10000000001L);
        out.setActiveStatus("Y");
        out.setCurrBal(new BigDecimal("1940.00"));
        out.setCreditLimit(new BigDecimal("20200.00"));
        out.setCashCreditLimit(new BigDecimal("10200.00"));
        out.setOpenDate("2014-11-20");
        out.setExpirationDate("2025-05-20");
        out.setReissueDate("2025-05-");
        out.setCurrCycCredit(new BigDecimal("0.00"));
        out.setCurrCycDebit(new BigDecimal("2525.00"));
        out.setGroupId("A000000000");

        ArryfileRecord arr = new ArryfileRecord();
        arr.setAcctId(10000000001L);
        arr.setBalance(0, new BigDecimal("1940.00"));
        arr.setCycDebit(0, new BigDecimal("1005.00"));
        arr.setBalance(1, new BigDecimal("1940.00"));
        arr.setCycDebit(1, new BigDecimal("1525.00"));
        arr.setBalance(2, new BigDecimal("-1025.00"));
        arr.setCycDebit(2, new BigDecimal("-2500.00"));

        VbrcRec1 vb1 = new VbrcRec1();
        vb1.setAcctId(10000000001L);
        vb1.setActiveStatus("Y");

        VbrcRec2 vb2 = new VbrcRec2();
        vb2.setAcctId(10000000001L);
        vb2.setCurrBal(new BigDecimal("1940.00"));
        vb2.setCreditLimit(new BigDecimal("20200.00"));
        vb2.setReissueYyyy("2025");

        return new AccountOutputBundle(out, arr, vb1, vb2);
    }

    @Test
    void testWriteCreatesAllFiles() throws Exception {
        writer.write(new Chunk<>(List.of(createBundle())));

        assertTrue(Files.exists(outfilePath));
        assertTrue(Files.exists(arryfilePath));
        assertTrue(Files.exists(vbrcfilePath));
    }

    @Test
    void testOutfileFormat() throws Exception {
        writer.write(new Chunk<>(List.of(createBundle())));

        List<String> lines = Files.readAllLines(outfilePath);
        assertEquals(1, lines.size());
        String[] fields = lines.get(0).split("\\|");
        assertEquals(11, fields.length);
        assertEquals("10000000001", fields[0]);
        assertEquals("Y", fields[1]);
        assertEquals("1940.00", fields[2]);
        assertEquals("20200.00", fields[3]);
        assertEquals("10200.00", fields[4]);
        assertEquals("2014-11-20", fields[5]);
        assertEquals("2025-05-20", fields[6]);
        assertEquals("2025-05-", fields[7]);
        assertEquals("0.00", fields[8]);
        assertEquals("2525.00", fields[9]);
        assertEquals("A000000000", fields[10]);
    }

    @Test
    void testArryfileFormat() throws Exception {
        writer.write(new Chunk<>(List.of(createBundle())));

        List<String> lines = Files.readAllLines(arryfilePath);
        assertEquals(1, lines.size());
        String[] fields = lines.get(0).split("\\|");
        // acctId + 5*(bal + debit) = 11 fields
        assertEquals(11, fields.length);
        assertEquals("10000000001", fields[0]);
        assertEquals("1940.00", fields[1]);   // bal(1)
        assertEquals("1005.00", fields[2]);   // debit(1)
        assertEquals("1940.00", fields[3]);   // bal(2)
        assertEquals("1525.00", fields[4]);   // debit(2)
        assertEquals("-1025.00", fields[5]);  // bal(3)
        assertEquals("-2500.00", fields[6]);  // debit(3)
        assertEquals("0.00", fields[7]);      // bal(4)
        assertEquals("0.00", fields[8]);      // debit(4)
        assertEquals("0.00", fields[9]);      // bal(5)
        assertEquals("0.00", fields[10]);     // debit(5)
    }

    @Test
    void testVbrcfileFormat() throws Exception {
        writer.write(new Chunk<>(List.of(createBundle())));

        List<String> lines = Files.readAllLines(vbrcfilePath);
        assertEquals(2, lines.size());

        // REC1
        String[] rec1 = lines.get(0).split("\\|");
        assertEquals(2, rec1.length);
        assertEquals("10000000001", rec1[0]);
        assertEquals("Y", rec1[1]);

        // REC2
        String[] rec2 = lines.get(1).split("\\|");
        assertEquals(4, rec2.length);
        assertEquals("10000000001", rec2[0]);
        assertEquals("1940.00", rec2[1]);
        assertEquals("20200.00", rec2[2]);
        assertEquals("2025", rec2[3]);
    }

    @Test
    void testMultipleRecords() throws Exception {
        writer.write(new Chunk<>(List.of(createBundle(), createBundle())));

        List<String> outLines = Files.readAllLines(outfilePath);
        assertEquals(2, outLines.size());

        List<String> arrLines = Files.readAllLines(arryfilePath);
        assertEquals(2, arrLines.size());

        // VBRC has 2 lines per account (rec1 + rec2)
        List<String> vbrcLines = Files.readAllLines(vbrcfilePath);
        assertEquals(4, vbrcLines.size());
    }

    @Test
    void testFormatDecimalNull() {
        assertEquals("0.00", AccountMultiFileWriter.formatDecimal(null));
    }

    @Test
    void testFormatDecimalNegative() {
        assertEquals("-1025.00", AccountMultiFileWriter.formatDecimal(new BigDecimal("-1025.00")));
    }

    @Test
    void testNullSafe() {
        assertEquals("", AccountMultiFileWriter.nullSafe(null));
        assertEquals("test", AccountMultiFileWriter.nullSafe("test"));
    }
}
