package com.carddemo.batch.cbact01c;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes ArrayRecord objects to a Writer (pipe-delimited format).
 */
public class ArrayFileWriter {

    /**
     * Write all ArrayRecords to the given writer.
     *
     * @param records array records to write
     * @param writer  destination writer
     * @throws FileProcessingException on I/O errors
     */
    public void writeAll(List<ArrayRecord> records, Writer writer) {
        try {
            for (ArrayRecord rec : records) {
                writer.write(formatRecord(rec));
                writer.write(System.lineSeparator());
            }
            writer.flush();
        } catch (IOException e) {
            throw new FileProcessingException("ARRAY FILE WRITE ERROR", e);
        }
    }

    private String formatRecord(ArrayRecord rec) {
        String entriesStr = rec.entries().stream()
                .map(e -> e.currBal().toPlainString() + "," + e.currCycDebit().toPlainString())
                .collect(Collectors.joining("|"));
        return rec.acctId() + "|" + entriesStr;
    }
}
