package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountOutputBundle;
import com.carddemo.batch.model.ArryfileRecord;
import com.carddemo.batch.model.OutfileRecord;
import com.carddemo.batch.model.VbrcRec1;
import com.carddemo.batch.model.VbrcRec2;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * Writes three output files mirroring CBACT01C paragraphs 1350, 1450, 1550/1575.
 * Uses CSV format for each output file to make the records human-readable and testable.
 */
public class AccountMultiFileWriter implements ItemWriter<AccountOutputBundle> {

    private final Path outfilePath;
    private final Path arryfilePath;
    private final Path vbrcfilePath;

    public AccountMultiFileWriter(Path outfilePath, Path arryfilePath, Path vbrcfilePath) {
        this.outfilePath = outfilePath;
        this.arryfilePath = arryfilePath;
        this.vbrcfilePath = vbrcfilePath;
    }

    @Override
    public void write(Chunk<? extends AccountOutputBundle> chunk) throws Exception {
        for (AccountOutputBundle bundle : chunk) {
            writeOutfileRecord(bundle.getOutfileRecord());
            writeArryfileRecord(bundle.getArryfileRecord());
            writeVbrcRecords(bundle.getVbrcRec1(), bundle.getVbrcRec2());
        }
    }

    private void writeOutfileRecord(OutfileRecord rec) throws IOException {
        String line = String.join("|",
                String.valueOf(rec.getAcctId()),
                nullSafe(rec.getActiveStatus()),
                formatDecimal(rec.getCurrBal()),
                formatDecimal(rec.getCreditLimit()),
                formatDecimal(rec.getCashCreditLimit()),
                nullSafe(rec.getOpenDate()),
                nullSafe(rec.getExpirationDate()),
                nullSafe(rec.getReissueDate()),
                formatDecimal(rec.getCurrCycCredit()),
                formatDecimal(rec.getCurrCycDebit()),
                nullSafe(rec.getGroupId()));
        appendLine(outfilePath, line);
    }

    private void writeArryfileRecord(ArryfileRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(rec.getAcctId());
        for (int i = 0; i < 5; i++) {
            sb.append('|').append(formatDecimal(rec.getBalance(i)));
            sb.append('|').append(formatDecimal(rec.getCycDebit(i)));
        }
        appendLine(arryfilePath, sb.toString());
    }

    private void writeVbrcRecords(VbrcRec1 rec1, VbrcRec2 rec2) throws IOException {
        // REC1: 12-byte record (acctId|activeStatus)
        String line1 = String.join("|",
                String.valueOf(rec1.getAcctId()),
                nullSafe(rec1.getActiveStatus()));
        appendLine(vbrcfilePath, line1);

        // REC2: 39-byte record (acctId|currBal|creditLimit|reissueYyyy)
        String line2 = String.join("|",
                String.valueOf(rec2.getAcctId()),
                formatDecimal(rec2.getCurrBal()),
                formatDecimal(rec2.getCreditLimit()),
                nullSafe(rec2.getReissueYyyy()));
        appendLine(vbrcfilePath, line2);
    }

    private void appendLine(Path path, String line) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        }
    }

    static String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
