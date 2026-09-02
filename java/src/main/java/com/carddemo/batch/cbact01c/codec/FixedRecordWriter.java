package com.carddemo.batch.cbact01c.codec;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/** RECFM=FB writer: every record must be exactly {@code lrecl} bytes, no terminators. */
public final class FixedRecordWriter implements Closeable {

    private final OutputStream out;
    private final int lrecl;

    public FixedRecordWriter(OutputStream out, int lrecl) {
        if (lrecl <= 0) {
            throw new IllegalArgumentException("lrecl must be positive: " + lrecl);
        }
        this.out = out;
        this.lrecl = lrecl;
    }

    public void write(byte[] record) throws IOException {
        if (record.length != lrecl) {
            throw new IllegalArgumentException("record length " + record.length + " != LRECL " + lrecl);
        }
        out.write(record);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
