package com.carddemo.batch.cbact01c.codec;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Optional;

/**
 * RECFM=FB reader. In ASCII mode a record may be followed by {@code \n} or {@code \r\n};
 * in EBCDIC mode records are contiguous. A short final record raises {@link EOFException}.
 */
public final class FixedRecordReader implements Closeable {

    private final PushbackInputStream in;
    private final int lrecl;
    private final CobolCharset cs;

    public FixedRecordReader(InputStream in, int lrecl, CobolCharset cs) {
        if (lrecl <= 0) {
            throw new IllegalArgumentException("lrecl must be positive: " + lrecl);
        }
        this.in = new PushbackInputStream(in, 2);
        this.lrecl = lrecl;
        this.cs = cs;
    }

    /** Next record, or empty at EOF (COBOL file status '10'). */
    public Optional<byte[]> next() throws IOException {
        byte[] rec = new byte[lrecl];
        int read = in.readNBytes(rec, 0, lrecl);
        if (read == 0) {
            return Optional.empty();
        }
        if (read < lrecl) {
            throw new EOFException("short final record: expected " + lrecl + " bytes, got " + read);
        }
        if (cs == CobolCharset.ASCII) {
            skipLineTerminator();
        }
        return Optional.of(rec);
    }

    private void skipLineTerminator() throws IOException {
        int b = in.read();
        if (b == '\r') {
            int n = in.read();
            if (n != '\n' && n != -1) {
                in.unread(n);
            }
        } else if (b != '\n' && b != -1) {
            in.unread(b);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
