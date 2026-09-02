package com.carddemo.batch.cbact01c.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class RecordIoTest {

    @Test
    void variableRecordWriterPrefixesRdw() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] rec12 = new byte[12];
        byte[] rec39 = new byte[39];
        Arrays.fill(rec12, (byte) 0x11);
        Arrays.fill(rec39, (byte) 0x22);
        try (VariableRecordWriter w = new VariableRecordWriter(out, 84)) {
            w.write(rec12);
            w.write(rec39);
        }
        byte[] expected = new byte[4 + 12 + 4 + 39];
        System.arraycopy(Hex.bytes("00 10 00 00"), 0, expected, 0, 4);
        System.arraycopy(rec12, 0, expected, 4, 12);
        System.arraycopy(Hex.bytes("00 2B 00 00"), 0, expected, 16, 4);
        System.arraycopy(rec39, 0, expected, 20, 39);
        assertArrayEquals(expected, out.toByteArray());
        assertEquals(59, out.size());
    }

    @Test
    void variableRecordWriterRejectsOutOfRangeLengths() {
        VariableRecordWriter w = new VariableRecordWriter(new ByteArrayOutputStream(), 84);
        assertThrows(IllegalArgumentException.class, () -> w.write(new byte[9]));
        assertThrows(IllegalArgumentException.class, () -> w.write(new byte[81]));
    }

    @Test
    void variableRecordWriterAcceptsBoundaryLengths() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VariableRecordWriter w = new VariableRecordWriter(out, 84);
        w.write(new byte[10]);
        w.write(new byte[80]);
        assertEquals(14 + 84, out.size());
        assertArrayEquals(Hex.bytes("00 0E 00 00"), Arrays.copyOfRange(out.toByteArray(), 0, 4));
        assertArrayEquals(Hex.bytes("00 54 00 00"), Arrays.copyOfRange(out.toByteArray(), 14, 18));
    }

    @Test
    void fixedRecordWriterEnforcesLrecl() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FixedRecordWriter w = new FixedRecordWriter(out, 107);
        w.write(new byte[107]);
        assertThrows(IllegalArgumentException.class, () -> w.write(new byte[106]));
        assertThrows(IllegalArgumentException.class, () -> w.write(new byte[108]));
        assertEquals(107, out.size());
    }

    @Test
    void fixedRecordReaderHandlesAsciiTerminators() throws IOException {
        byte[] data = "AAAA\nBBBB\r\nCCCC".getBytes();
        try (FixedRecordReader r = new FixedRecordReader(new ByteArrayInputStream(data), 4, CobolCharset.ASCII)) {
            assertArrayEquals("AAAA".getBytes(), r.next().orElseThrow());
            assertArrayEquals("BBBB".getBytes(), r.next().orElseThrow());
            assertArrayEquals("CCCC".getBytes(), r.next().orElseThrow());
            assertTrue(r.next().isEmpty());
            assertTrue(r.next().isEmpty());
        }
    }

    @Test
    void fixedRecordReaderEbcdicIsContiguousAndFailsOnShortRecord() throws IOException {
        byte[] data = {1, 2, 3, 4, 5, 6, 7};
        try (FixedRecordReader r = new FixedRecordReader(new ByteArrayInputStream(data), 4, CobolCharset.EBCDIC)) {
            assertArrayEquals(new byte[] {1, 2, 3, 4}, r.next().orElseThrow());
            assertThrows(EOFException.class, r::next);
        }
    }
}
