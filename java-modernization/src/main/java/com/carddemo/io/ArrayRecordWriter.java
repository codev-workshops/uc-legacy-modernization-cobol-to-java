package com.carddemo.io;

import com.carddemo.model.ArrayRecord;
import com.carddemo.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes array-based account records (ARR-ARRAY-REC).
 *
 * <pre>
 * 05 ARR-ACCT-ID                PIC 9(11)
 * 05 ARR-ACCT-BAL OCCURS 5 TIMES
 *    10 ARR-ACCT-CURR-BAL       PIC S9(10)V99
 *    10 ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3
 * 05 ARR-FILLER                 PIC X(04)
 * </pre>
 */
public class ArrayRecordWriter implements AutoCloseable {

    private static final int OCCURS_COUNT = 5;

    private final BufferedWriter writer;

    public ArrayRecordWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void write(ArrayRecord record) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(record.acctId());

        for (int i = 0; i < OCCURS_COUNT; i++) {
            if (i < record.balanceEntries().size()) {
                ArrayRecord.BalanceEntry entry = record.balanceEntries().get(i);
                sb.append(CobolDecimalParser.formatSignedDecimal(entry.currBal(), 12, 2));
                sb.append(CobolDecimalParser.formatSignedDecimal(entry.currCycDebit(), 12, 2));
            } else {
                // Initialized to zeros (COBOL INITIALIZE)
                sb.append(CobolDecimalParser.formatSignedDecimal(BigDecimal.ZERO, 12, 2));
                sb.append(CobolDecimalParser.formatSignedDecimal(BigDecimal.ZERO, 12, 2));
            }
        }

        String filler = record.filler() != null ? record.filler() : "    ";
        sb.append(padRight(filler, 4));

        writer.write(sb.toString());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        return s + " ".repeat(len - s.length());
    }
}
