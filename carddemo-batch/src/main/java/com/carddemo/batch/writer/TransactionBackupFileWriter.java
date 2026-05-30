package com.carddemo.batch.writer;

import com.carddemo.common.entity.Transaction;
import com.carddemo.common.util.DateFormatUtil;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes Transaction records to a flat backup file,
 * mirroring the COBOL CBTRN01C program's sequential output.
 * Each line contains pipe-delimited transaction fields with
 * dates formatted via DateFormatUtil (COBDATFT replacement).
 */
public class TransactionBackupFileWriter implements ItemStreamWriter<Transaction> {

    private static final String DELIMITER = "|";

    private final Path outputPath;
    private BufferedWriter writer;

    public TransactionBackupFileWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            writer = Files.newBufferedWriter(outputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open backup file: " + outputPath, e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        // no checkpoint state needed
    }

    @Override
    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to close backup file", e);
            }
        }
    }

    @Override
    public void write(Chunk<? extends Transaction> chunk) throws Exception {
        for (Transaction txn : chunk) {
            writer.write(formatRecord(txn));
            writer.newLine();
        }
        writer.flush();
    }

    String formatRecord(Transaction txn) {
        String origDate = formatTimestamp(txn.getOrigTs());
        String procDate = formatTimestamp(txn.getProcTs());

        return String.join(DELIMITER,
                safe(txn.getTranId()),
                safe(txn.getTypeCd()),
                txn.getCatCd() != null ? String.valueOf(txn.getCatCd()) : "",
                safe(txn.getSource()),
                safe(txn.getDesc()),
                txn.getAmt() != null ? txn.getAmt().toPlainString() : "",
                txn.getMerchantId() != null ? String.valueOf(txn.getMerchantId()) : "",
                safe(txn.getMerchantName()),
                safe(txn.getMerchantCity()),
                safe(txn.getMerchantZip()),
                safe(txn.getCardNum()),
                origDate,
                procDate);
    }

    private String formatTimestamp(String ts) {
        if (ts == null || ts.isBlank()) {
            return "";
        }
        String datePart = ts.length() >= 10 ? ts.substring(0, 10) : ts;
        if (datePart.contains("-")) {
            return DateFormatUtil.formatDate(datePart, "2", "1").trim();
        }
        return DateFormatUtil.formatDate(datePart, "1", "1").trim();
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
