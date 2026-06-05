package com.carddemo.io;

import com.carddemo.model.VariableLengthRecord1;
import com.carddemo.model.VariableLengthRecord2;
import com.carddemo.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes variable-length records to a single output file.
 * Each account produces two records:
 * <ul>
 *   <li>VB1 (12 bytes): account ID + active status</li>
 *   <li>VB2 (39 bytes): account ID + balance + credit limit + reissue year</li>
 * </ul>
 * In the COBOL version the file uses RECORDING MODE V.
 * Here each record is a separate text line.
 */
public class VariableLengthRecordWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public VariableLengthRecordWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void writeRecord1(VariableLengthRecord1 record) throws IOException {
        String line = record.acctId() + record.activeStatus();
        writer.write(line);
        writer.newLine();
    }

    public void writeRecord2(VariableLengthRecord2 record) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(record.acctId());
        sb.append(CobolDecimalParser.formatSignedDecimal(record.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(record.creditLimit(), 12, 2));
        sb.append(padRight(record.reissueYear(), 4));
        writer.write(sb.toString());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private static String padRight(String s, int len) {
        if (s == null) {
            return " ".repeat(len);
        }
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        return s + " ".repeat(len - s.length());
    }
}
