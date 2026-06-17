package com.carddemo.batch.cbact01c;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes OutAccountRecord objects to a Writer (pipe-delimited format).
 */
public class OutFileWriter {

    /**
     * Write all OutAccountRecords to the given writer.
     *
     * @param records output records to write
     * @param writer  destination writer
     * @throws FileProcessingException on I/O errors
     */
    public void writeAll(List<OutAccountRecord> records, Writer writer) {
        try {
            for (OutAccountRecord rec : records) {
                writer.write(formatRecord(rec));
                writer.write(System.lineSeparator());
            }
            writer.flush();
        } catch (IOException e) {
            throw new FileProcessingException("ACCOUNT FILE WRITE ERROR", e);
        }
    }

    private String formatRecord(OutAccountRecord rec) {
        return String.join("|",
                String.valueOf(rec.acctId()),
                rec.activeStatus(),
                rec.currBal().toPlainString(),
                rec.creditLimit().toPlainString(),
                rec.cashCreditLimit().toPlainString(),
                rec.openDate(),
                rec.expirationDate(),
                rec.reissueDate(),
                rec.currCycCredit().toPlainString(),
                rec.currCycDebit().toPlainString(),
                rec.groupId()
        );
    }
}
