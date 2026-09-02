package com.carddemo.batch.cbact01c.golden;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.model.ArrArrayRec;
import com.carddemo.batch.cbact01c.model.OutAcctRec;
import com.carddemo.batch.cbact01c.model.SampleData;
import com.carddemo.batch.cbact01c.model.Vb1Rec;
import com.carddemo.batch.cbact01c.model.Vb2Rec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * ASCII mode is a view of the same data (design §2): running over
 * {@code app/data/ASCII/acctdata.txt} must yield output whose decoded content is identical
 * to the canonical EBCDIC run, record for record and field for field.
 */
class AsciiModeTest {

    private static final int RECORD_COUNT = 50;

    @TempDir
    Path tmp;

    private ProgramRun ebcdic;
    private ProgramRun ascii;

    @BeforeEach
    void runBothModes() throws IOException {
        Path ebcdicInput = ProgramRun.fixtureFile("acctdata.ebcdic.in", tmp);
        ebcdic = ProgramRun.execute(ebcdicInput, tmp.resolve("ebcdic"), CobolCharset.EBCDIC);
        ascii = ProgramRun.execute(SampleData.asciiAcctData(), tmp.resolve("ascii"), CobolCharset.ASCII);
        assertEquals(0, ebcdic.exitCode(), ebcdic.console());
        assertEquals(0, ascii.exitCode(), ascii.console());
    }

    @Test
    void datasetsHaveTheSameLayoutButDifferentBytes() {
        assertEquals(ebcdic.outfile().length, ascii.outfile().length);
        assertEquals(ebcdic.arryfile().length, ascii.arryfile().length);
        assertEquals(ebcdic.vbrcfile().length, ascii.vbrcfile().length);
        assertNotEquals(
                new String(ebcdic.outfile(), StandardCharsets.ISO_8859_1),
                new String(ascii.outfile(), StandardCharsets.ISO_8859_1),
                "text bytes must differ between the two character sets");
    }

    @Test
    void outfileRecordsDecodeIdentically() {
        List<byte[]> e = ProgramRun.fixedRecords(ebcdic.outfile(), OutAcctRec.LENGTH);
        List<byte[]> a = ProgramRun.fixedRecords(ascii.outfile(), OutAcctRec.LENGTH);
        assertEquals(RECORD_COUNT, e.size());
        assertEquals(RECORD_COUNT, a.size());
        for (int i = 0; i < RECORD_COUNT; i++) {
            assertEquals(OutAcctRec.fromBytes(e.get(i), CobolCharset.EBCDIC),
                    OutAcctRec.fromBytes(a.get(i), CobolCharset.ASCII), "OUTFILE record " + (i + 1));
        }
    }

    @Test
    void arryfileRecordsDecodeIdentically() {
        List<byte[]> e = ProgramRun.fixedRecords(ebcdic.arryfile(), ArrArrayRec.LENGTH);
        List<byte[]> a = ProgramRun.fixedRecords(ascii.arryfile(), ArrArrayRec.LENGTH);
        assertEquals(RECORD_COUNT, e.size());
        assertEquals(RECORD_COUNT, a.size());
        for (int i = 0; i < RECORD_COUNT; i++) {
            assertEquals(ArrArrayRec.fromBytes(e.get(i), CobolCharset.EBCDIC),
                    ArrArrayRec.fromBytes(a.get(i), CobolCharset.ASCII), "ARRYFILE record " + (i + 1));
        }
    }

    @Test
    void vbrcfileRecordsDecodeIdentically() {
        List<byte[]> e = ProgramRun.variableRecords(ebcdic.vbrcfile());
        List<byte[]> a = ProgramRun.variableRecords(ascii.vbrcfile());
        assertEquals(2 * RECORD_COUNT, e.size());
        assertEquals(2 * RECORD_COUNT, a.size());
        for (int i = 0; i < RECORD_COUNT; i++) {
            byte[] vb1e = e.get(2 * i);
            byte[] vb1a = a.get(2 * i);
            byte[] vb2e = e.get(2 * i + 1);
            byte[] vb2a = a.get(2 * i + 1);
            assertEquals(Vb1Rec.LENGTH, vb1a.length, "VBRC-REC1 length, record " + (i + 1));
            assertEquals(Vb2Rec.LENGTH, vb2a.length, "VBRC-REC2 length, record " + (i + 1));
            assertEquals(Vb1Rec.fromBytes(vb1e, CobolCharset.EBCDIC), Vb1Rec.fromBytes(vb1a, CobolCharset.ASCII),
                    "VBRC-REC1 record " + (i + 1));
            assertEquals(Vb2Rec.fromBytes(vb2e, CobolCharset.EBCDIC), Vb2Rec.fromBytes(vb2a, CobolCharset.ASCII),
                    "VBRC-REC2 record " + (i + 1));
        }
    }

    @Test
    void packedAndRdwBytesAreCharsetIndependent() {
        List<byte[]> e = ProgramRun.fixedRecords(ebcdic.outfile(), OutAcctRec.LENGTH);
        List<byte[]> a = ProgramRun.fixedRecords(ascii.outfile(), OutAcctRec.LENGTH);
        for (int i = 0; i < RECORD_COUNT; i++) {
            assertArrayEquals(Arrays.copyOfRange(e.get(i), 90, 97), Arrays.copyOfRange(a.get(i), 90, 97),
                    "OUT-ACCT-CURR-CYC-DEBIT COMP-3 bytes, record " + (i + 1));
        }
        assertArrayEquals(Arrays.copyOf(ebcdic.vbrcfile(), 4), Arrays.copyOf(ascii.vbrcfile(), 4));
    }
}
