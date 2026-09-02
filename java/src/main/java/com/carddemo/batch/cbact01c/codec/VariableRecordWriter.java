package com.carddemo.batch.cbact01c.codec;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * RECFM=VB writer. Each record is prefixed with a 4-byte RDW: big-endian (dataLength + 4) followed by
 * two zero bytes. No block descriptors are written.
 */
public final class VariableRecordWriter implements Closeable {

    public static final int RDW_LENGTH = 4;
    public static final int MIN_RECORD_LENGTH = 10;

    private final OutputStream out;
    private final int maxLrecl;

    /** @param maxLrecl dataset LRECL including the RDW (84 for VBRCFILE => 80 data bytes max) */
    public VariableRecordWriter(OutputStream out, int maxLrecl) {
        if (maxLrecl <= RDW_LENGTH) {
            throw new IllegalArgumentException("maxLrecl must exceed the RDW length: " + maxLrecl);
        }
        this.out = out;
        this.maxLrecl = maxLrecl;
    }

    public int maxDataLength() {
        return maxLrecl - RDW_LENGTH;
    }

    public void write(byte[] record) throws IOException {
        int len = record.length;
        if (len < MIN_RECORD_LENGTH || len > maxDataLength()) {
            throw new IllegalArgumentException("record length " + len + " outside " + MIN_RECORD_LENGTH
                    + ".." + maxDataLength());
        }
        int rdw = len + RDW_LENGTH;
        out.write((rdw >>> 8) & 0xFF);
        out.write(rdw & 0xFF);
        out.write(0);
        out.write(0);
        out.write(record);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
