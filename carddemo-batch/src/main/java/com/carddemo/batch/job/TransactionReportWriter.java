package com.carddemo.batch.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Writes the Daily Transaction Report file, replicating the COBOL CBTRN03C
 * report layout: headers, detail lines grouped by card, page totals every
 * {@link ReportFormatter#PAGE_SIZE} lines, account totals on card-number
 * changes, and a grand total at the end.
 */
public class TransactionReportWriter implements ItemWriter<TransactionReportItem>, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportWriter.class);

    private final String outputPath;
    private final String startDate;
    private final String endDate;

    private BufferedWriter writer;
    private boolean firstTime = true;
    private int lineCounter;
    private BigDecimal pageTotal = BigDecimal.ZERO;
    private BigDecimal accountTotal = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private String currentCardNum = "";

    public TransactionReportWriter(String outputPath, String startDate, String endDate) {
        this.outputPath = outputPath;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            writer = new BufferedWriter(new FileWriter(outputPath));
            log.info("Opened report file: {}", outputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open report file: " + outputPath, e);
        }
    }

    @Override
    public void write(Chunk<? extends TransactionReportItem> items) throws Exception {
        for (TransactionReportItem item : items) {
            if (!currentCardNum.equals(item.getCardNum())) {
                if (!firstTime) {
                    writeAccountTotals();
                }
                currentCardNum = item.getCardNum();
            }

            if (firstTime) {
                firstTime = false;
                writeHeaders();
            }

            if (lineCounter > 0 && lineCounter % ReportFormatter.PAGE_SIZE == 0) {
                writePageTotals();
                writeHeaders();
            }

            pageTotal = pageTotal.add(item.getAmount());
            accountTotal = accountTotal.add(item.getAmount());

            writeLine(ReportFormatter.formatDetailLine(item));
            lineCounter++;
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            if (!firstTime) {
                writeAccountTotals();
                writePageTotals();
                writeLine(ReportFormatter.formatGrandTotal(grandTotal));
            }
            writer.close();
            log.info("Closed report file: {}", outputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Error finalizing report", e);
        }
        return ExitStatus.COMPLETED;
    }

    private void writeHeaders() throws IOException {
        writeLine(ReportFormatter.formatNameHeader(startDate, endDate));
        lineCounter++;
        writeLine(ReportFormatter.blankLine());
        lineCounter++;
        writeLine(ReportFormatter.formatColumnHeader1());
        lineCounter++;
        writeLine(ReportFormatter.formatSeparator());
        lineCounter++;
    }

    private void writePageTotals() throws IOException {
        writeLine(ReportFormatter.formatPageTotal(pageTotal));
        grandTotal = grandTotal.add(pageTotal);
        pageTotal = BigDecimal.ZERO;
        lineCounter++;
        writeLine(ReportFormatter.formatSeparator());
        lineCounter++;
    }

    private void writeAccountTotals() throws IOException {
        writeLine(ReportFormatter.formatAccountTotal(accountTotal));
        accountTotal = BigDecimal.ZERO;
        lineCounter++;
        writeLine(ReportFormatter.formatSeparator());
        lineCounter++;
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    int getLineCounter() { return lineCounter; }
    BigDecimal getGrandTotal() { return grandTotal; }
}
