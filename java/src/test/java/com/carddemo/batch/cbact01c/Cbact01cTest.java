package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.model.OutAcctRec;
import com.carddemo.batch.cbact01c.model.SampleData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Cbact01cTest {

    @TempDir
    Path tmp;

    private ByteArrayOutputStream stdout;

    private int run(Path acct, String... extra) {
        stdout = new ByteArrayOutputStream();
        List<String> args = new ArrayList<>(List.of(
                "--acctfile", acct.toString(),
                "--outfile", tmp.resolve("outfile").toString(),
                "--arryfile", tmp.resolve("arryfile").toString(),
                "--vbrcfile", tmp.resolve("vbrcfile").toString()));
        args.addAll(Arrays.asList(extra));
        return Cbact01c.run(args.toArray(new String[0]), new PrintStream(stdout, true, StandardCharsets.UTF_8));
    }

    private String out() {
        return stdout.toString(StandardCharsets.UTF_8);
    }

    @Test
    void ebcdicEndToEnd() throws IOException {
        assertEquals(0, run(SampleData.ebcdicAcctData()));
        assertTrue(out().startsWith(Cbact01c.START_MSG + System.lineSeparator()));
        assertTrue(out().endsWith(Cbact01c.END_MSG + System.lineSeparator()));
        assertFalse(out().contains("ACCT-ID"));

        byte[] outfile = Files.readAllBytes(tmp.resolve("outfile"));
        byte[] arryfile = Files.readAllBytes(tmp.resolve("arryfile"));
        byte[] vbrcfile = Files.readAllBytes(tmp.resolve("vbrcfile"));
        assertEquals(5350, outfile.length);
        assertEquals(5500, arryfile.length);
        assertEquals(2950, vbrcfile.length);

        OutAcctRec first = OutAcctRec.fromBytes(Arrays.copyOf(outfile, OutAcctRec.LENGTH), CobolCharset.EBCDIC);
        assertEquals("00000000001", first.acctId());
        assertEquals(new BigDecimal("194.00"), first.currBal());
        assertEquals("20250520  ", first.reissueDate());
        assertEquals(new BigDecimal("2525.00"), first.currCycDebit());
        assertArrayEquals(new byte[] {0x00, 0x10, 0x00, 0x00}, Arrays.copyOf(vbrcfile, 4));
        assertArrayEquals(new byte[] {0x00, 0x2B, 0x00, 0x00}, Arrays.copyOfRange(vbrcfile, 16, 20));
    }

    @Test
    void asciiEndToEnd() throws IOException {
        assertEquals(0, run(SampleData.asciiAcctData(), "--charset", "ASCII"));
        byte[] outfile = Files.readAllBytes(tmp.resolve("outfile"));
        assertEquals(5350, outfile.length);
        assertTrue(new String(outfile, 0, 24, StandardCharsets.US_ASCII).equals("00000000001Y00000001940{"));
    }

    @Test
    void outputsAreTruncatedOnOpen() throws IOException {
        Files.write(tmp.resolve("outfile"), new byte[10_000]);
        assertEquals(0, run(SampleData.ebcdicAcctData()));
        assertEquals(5350, Files.size(tmp.resolve("outfile")));
    }

    @Test
    void missingAcctFileAbends() {
        assertEquals(12, run(tmp.resolve("does-not-exist")));
        String o = out();
        assertTrue(o.contains("ERROR OPENING ACCTFILE"));
        assertTrue(o.contains("FILE STATUS IS: NNNN0035"));
        assertTrue(o.contains("ABENDING PROGRAM"));
        assertFalse(o.contains(Cbact01c.END_MSG));
    }

    @Test
    void unwritableOutputAbends() {
        stdout = new ByteArrayOutputStream();
        int rc = Cbact01c.run(new String[] {
                "--acctfile", SampleData.ebcdicAcctData().toString(),
                "--outfile", tmp.resolve("outfile").toString(),
                "--arryfile", tmp.resolve("missing-dir/arryfile").toString(),
                "--vbrcfile", tmp.resolve("vbrcfile").toString()},
                new PrintStream(stdout, true, StandardCharsets.UTF_8));
        assertEquals(12, rc);
        assertTrue(out().contains("ERROR OPENING ARRAYFILE30"));
        assertTrue(out().contains("FILE STATUS IS: NNNN0030"));
        assertTrue(out().contains("ABENDING PROGRAM"));
    }

    @Test
    void shortFinalRecordAbends() throws IOException {
        byte[] all = Files.readAllBytes(SampleData.ebcdicAcctData());
        Path truncated = tmp.resolve("truncated.ps");
        Files.write(truncated, Arrays.copyOf(all, 300 + 150));
        assertEquals(12, run(truncated));
        assertTrue(out().contains("ERROR READING ACCOUNT FILE"));
        assertTrue(out().contains("FILE STATUS IS: NNNN0004"));
        assertTrue(out().contains("ABENDING PROGRAM"));
        assertEquals(OutAcctRec.LENGTH, Files.size(tmp.resolve("outfile")));
    }

    @Test
    void badArgumentsReturn12() {
        stdout = new ByteArrayOutputStream();
        assertEquals(12, Cbact01c.run(new String[] {"--acctfile"}, new PrintStream(stdout)));
    }

    @Test
    void displayReproducesCobolLines() {
        assertEquals(0, run(SampleData.ebcdicAcctData(), "--display"));
        String[] lines = out().split("\\R");
        assertEquals(Cbact01c.START_MSG, lines[0]);
        String[] expected = {
            "ACCT-ID                 :00000000001",
            "ACCT-ACTIVE-STATUS      :Y",
            "ACCT-CURR-BAL           :00000001940{",
            "ACCT-CREDIT-LIMIT       :00000020200{",
            "ACCT-CASH-CREDIT-LIMIT  :00000010200{",
            "ACCT-OPEN-DATE          :2014-11-20",
            "ACCT-EXPIRAION-DATE     :2025-05-20",
            "ACCT-REISSUE-DATE       :2025-05-20",
            "ACCT-CURR-CYC-CREDIT    :00000000000{",
            "ACCT-CURR-CYC-DEBIT     :00000000000{",
            "ACCT-GROUP-ID           :          ",
            "-------------------------------------------------",
            "VBRC-REC1:00000000001Y",
            "VBRC-REC2:0000000000100000001940{00000020200{2025",
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], lines[1 + i], "line " + (1 + i));
        }
        String raw = lines[1 + expected.length];
        assertEquals(300, raw.length());
        assertTrue(raw.startsWith("00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"));
        assertEquals(1 + 50 * (expected.length + 1) + 1, lines.length);
        assertEquals(Cbact01c.END_MSG, lines[lines.length - 1]);
    }
}
