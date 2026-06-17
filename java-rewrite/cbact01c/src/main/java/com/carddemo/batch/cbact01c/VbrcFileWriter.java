package com.carddemo.batch.cbact01c;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes VBRC records (VbrcRecord1 and VbrcRecord2) to a Writer.
 * Records alternate: VB1, VB2, VB1, VB2, ... (two per input account).
 */
public class VbrcFileWriter {

    /**
     * Write all VBRC records to the given writer.
     *
     * @param records list of VbrcRecord1 and VbrcRecord2 objects (alternating)
     * @param writer  destination writer
     * @throws FileProcessingException on I/O errors
     */
    public void writeAll(List<Object> records, Writer writer) {
        try {
            for (Object rec : records) {
                if (rec instanceof VbrcRecord1 vb1) {
                    writer.write(formatVb1(vb1));
                } else if (rec instanceof VbrcRecord2 vb2) {
                    writer.write(formatVb2(vb2));
                }
                writer.write(System.lineSeparator());
            }
            writer.flush();
        } catch (IOException e) {
            throw new FileProcessingException("VBRC FILE WRITE ERROR", e);
        }
    }

    private String formatVb1(VbrcRecord1 rec) {
        return String.join("|",
                String.valueOf(rec.acctId()),
                rec.acctActiveStatus()
        );
    }

    private String formatVb2(VbrcRecord2 rec) {
        return String.join("|",
                String.valueOf(rec.acctId()),
                rec.currBal().toPlainString(),
                rec.creditLimit().toPlainString(),
                rec.reissueYear()
        );
    }
}
