package com.carddemo.batch.cbact01c.golden;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.model.ArrArrayRec;
import com.carddemo.batch.cbact01c.model.OutAcctRec;
import com.carddemo.batch.cbact01c.model.SampleData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Golden-master equivalence: EBCDIC-mode output must equal the derived fixtures under
 * {@code src/test/resources/golden} byte for byte. The fixtures were computed by
 * {@code generate_expected.py} from docs/MIGRATION_DESIGN.md, not by this program.
 */
class GoldenMasterTest {

    @TempDir
    Path tmp;

    private ProgramRun run;

    @BeforeEach
    void runProgram() throws IOException {
        Path acct = ProgramRun.fixtureFile("acctdata.ebcdic.in", tmp);
        run = ProgramRun.execute(acct, tmp.resolve("out"), CobolCharset.EBCDIC);
    }

    @Test
    void exitsZero() {
        assertEquals(0, run.exitCode(), () -> "console:\n" + run.console());
    }

    @Test
    void inputFixtureMatchesRepositoryData() throws IOException {
        assertArrayEquals(Files.readAllBytes(SampleData.ebcdicAcctData()), ProgramRun.fixture("acctdata.ebcdic.in"),
                "golden/acctdata.ebcdic.in is no longer a copy of app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS");
    }

    @Test
    void outfileMatchesExpected() {
        assertDataset("OUTFILE", run.outfile(), ProgramRun.fixture("OUTFILE.expected.bin"), OutAcctRec.LENGTH);
    }

    @Test
    void arryfileMatchesExpected() {
        assertDataset("ARRYFILE", run.arryfile(), ProgramRun.fixture("ARRYFILE.expected.bin"), ArrArrayRec.LENGTH);
    }

    @Test
    void vbrcfileMatchesExpected() {
        byte[] expected = ProgramRun.fixture("VBRCFILE.expected.bin");
        assertDataset("VBRCFILE", run.vbrcfile(), expected, 59);
    }

    /** @param recordLength bytes per record used only to name the record in a failure message */
    private static void assertDataset(String name, byte[] actual, byte[] expected, int recordLength) {
        int diff = firstDifference(actual, expected);
        if (diff >= 0) {
            throw new AssertionError(String.format(
                    "%s differs from the derived fixture at offset %d (record %d, offset %d within the record):"
                            + " expected %s, actual %s [expected %d bytes, actual %d bytes]",
                    name, diff, diff / recordLength + 1, diff % recordLength, hexAt(expected, diff),
                    hexAt(actual, diff), expected.length, actual.length));
        }
        assertArrayEquals(expected, actual, name);
    }

    private static int firstDifference(byte[] a, byte[] b) {
        int common = Math.min(a.length, b.length);
        for (int i = 0; i < common; i++) {
            if (a[i] != b[i]) {
                return i;
            }
        }
        return a.length == b.length ? -1 : common;
    }

    private static String hexAt(byte[] data, int offset) {
        if (offset >= data.length) {
            return "<end of data>";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < Math.min(offset + 4, data.length); i++) {
            sb.append(String.format("%02X ", data[i] & 0xFF));
        }
        return sb.toString().trim();
    }
}
