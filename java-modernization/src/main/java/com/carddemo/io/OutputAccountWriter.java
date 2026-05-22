package com.carddemo.io;

import com.carddemo.model.OutputAccountRecord;
import com.carddemo.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes fixed-length output account records (OUT-ACCT-REC).
 * The COMP-3 field (currCycDebit) is written in packed-decimal hex
 * representation for fidelity to the COBOL layout.
 */
public class OutputAccountWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public OutputAccountWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void write(OutputAccountRecord record) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(record.acctId());                                              // PIC 9(11)
        sb.append(record.activeStatus());                                        // PIC X(01)
        sb.append(CobolDecimalParser.formatSignedDecimal(record.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(record.creditLimit(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(record.cashCreditLimit(), 12, 2));
        sb.append(padRight(record.openDate(), 10));                              // PIC X(10)
        sb.append(padRight(record.expirationDate(), 10));                        // PIC X(10)
        sb.append(padRight(record.reissueDate(), 10));                           // PIC X(10)
        sb.append(CobolDecimalParser.formatSignedDecimal(record.currCycCredit(), 12, 2));
        // COMP-3 field — represent as zoned decimal for text output
        sb.append(CobolDecimalParser.formatSignedDecimal(record.currCycDebit(), 12, 2));
        sb.append(padRight(record.groupId(), 10));                               // PIC X(10)
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
