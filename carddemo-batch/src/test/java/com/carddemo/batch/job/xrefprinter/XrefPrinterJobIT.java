package com.carddemo.batch.job.xrefprinter;

import com.carddemo.batch.BatchApplication;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest(classes = BatchApplication.class)
class XrefPrinterJobIT {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job xrefPrinterJob;

    @Autowired
    private CardXrefRepository xrefRepo;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(xrefPrinterJob);
    }

    @BeforeEach
    void loadData() throws IOException {
        xrefRepo.deleteAll();
        Path dataFile = resolveDataFile();
        List<String> lines = Files.readAllLines(dataFile);
        List<CardXref> records = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            CardXref xref = new CardXref();
            xref.setXrefCardNum(line.substring(0, 16));
            xref.setCustId(Long.parseLong(line.substring(16, 25)));
            xref.setAcctId(Long.parseLong(line.substring(25, 36)));
            records.add(xref);
        }
        xrefRepo.saveAll(records);
    }

    private Path resolveDataFile() {
        Path p = Paths.get("../app/data/ASCII/cardxref.txt");
        if (Files.exists(p)) {
            return p;
        }
        p = Paths.get("app/data/ASCII/cardxref.txt");
        if (Files.exists(p)) {
            return p;
        }
        throw new IllegalStateException("cardxref.txt not found");
    }

    @Test
    void jobProducesCorrectReport() throws Exception {
        Path outputFile = tempDir.resolve("xref-report.txt");

        JobParameters params = new JobParametersBuilder()
                .addString("outputPath", outputFile.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertTrue(Files.exists(outputFile), "Report file must exist");

        List<String> lines = Files.readAllLines(outputFile);
        // header + 50 data lines + footer
        assertEquals(52, lines.size(), "Expected header + 50 records + footer");
        assertEquals(XrefPrinterJobConfig.HEADER, lines.get(0));
        assertEquals(XrefPrinterJobConfig.FOOTER, lines.get(lines.size() - 1));

        for (int i = 1; i <= 50; i++) {
            assertEquals(XrefLineAggregator.RECORD_LEN, lines.get(i).length(),
                    "Data line " + i + " must be " + XrefLineAggregator.RECORD_LEN + " chars");
        }
    }

    @Test
    void reportLinesMatchDataFile() throws Exception {
        Path outputFile = tempDir.resolve("xref-report-verify.txt");

        JobParameters params = new JobParametersBuilder()
                .addString("outputPath", outputFile.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<String> reportLines = Files.readAllLines(outputFile);

        // Sorted by card number (the reader ORDER BY xrefCardNum)
        Path dataFile = resolveDataFile();
        List<String> rawLines = Files.readAllLines(dataFile);
        rawLines.sort(null);

        for (int i = 0; i < rawLines.size(); i++) {
            String raw = rawLines.get(i);
            String cardNum = raw.substring(0, 16);
            long custId = Long.parseLong(raw.substring(16, 25));
            long acctId = Long.parseLong(raw.substring(25, 36));

            String reportLine = reportLines.get(i + 1); // skip header
            assertEquals(cardNum, reportLine.substring(0, 16), "Card number mismatch at line " + i);
            assertEquals(custId, Long.parseLong(reportLine.substring(16, 25)), "Cust ID mismatch at line " + i);
            assertEquals(acctId, Long.parseLong(reportLine.substring(25, 36)), "Acct ID mismatch at line " + i);
        }
    }

    @Test
    void stepExecutionCountsMatch() throws Exception {
        Path outputFile = tempDir.resolve("xref-report-counts.txt");

        JobParameters params = new JobParametersBuilder()
                .addString("outputPath", outputFile.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        execution.getStepExecutions().forEach(step -> {
            assertEquals(50, step.getReadCount(), "Read count");
            assertEquals(50, step.getWriteCount(), "Write count");
            assertEquals(0, step.getSkipCount(), "Skip count");
        });
    }
}
