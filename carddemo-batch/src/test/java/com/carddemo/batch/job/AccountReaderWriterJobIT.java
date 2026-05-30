package com.carddemo.batch.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carddemo.batch.BatchApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(classes = BatchApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data/accounts.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AccountReaderWriterJobIT {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("accountReaderWriterJob")
    private Job accountReaderWriterJob;

    @Value("${batch.output.dir:./output}")
    private String outputDir;

    private Path outfilePath;
    private Path arryfilePath;
    private Path vbrcfilePath;

    @BeforeEach
    void setUp() throws Exception {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);
        outfilePath = dir.resolve("OUTFILE.dat");
        arryfilePath = dir.resolve("ARRYFILE.dat");
        vbrcfilePath = dir.resolve("VBRCFILE.dat");
        Files.deleteIfExists(outfilePath);
        Files.deleteIfExists(arryfilePath);
        Files.deleteIfExists(vbrcfilePath);
    }

    private JobExecution launchUniqueJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.nanoTime())
                .toJobParameters();
        return jobLauncher.run(accountReaderWriterJob, params);
    }

    @Test
    void testJobCompletesSuccessfully() throws Exception {
        JobExecution execution = launchUniqueJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void testOutputFilesCreated() throws Exception {
        launchUniqueJob();

        assertTrue(Files.exists(outfilePath), "OUTFILE.dat should be created");
        assertTrue(Files.exists(arryfilePath), "ARRYFILE.dat should be created");
        assertTrue(Files.exists(vbrcfilePath), "VBRCFILE.dat should be created");
    }

    @Test
    void testCorrectRecordCount() throws Exception {
        launchUniqueJob();

        List<String> outLines = Files.readAllLines(outfilePath);
        assertEquals(3, outLines.size());

        List<String> arrLines = Files.readAllLines(arryfilePath);
        assertEquals(3, arrLines.size());

        List<String> vbrcLines = Files.readAllLines(vbrcfilePath);
        assertEquals(6, vbrcLines.size());
    }

    @Test
    void testZeroDebitSubstitutionInOutput() throws Exception {
        launchUniqueJob();

        List<String> outLines = Files.readAllLines(outfilePath);
        String[] firstRecord = outLines.get(0).split("\\|");
        assertEquals("2525.00", firstRecord[9], "Zero debit should be substituted with 2525.00");
    }

    @Test
    void testNonZeroDebitPassthroughInOutput() throws Exception {
        launchUniqueJob();

        List<String> outLines = Files.readAllLines(outfilePath);
        String[] thirdRecord = outLines.get(2).split("\\|");
        assertEquals("1500.00", thirdRecord[9], "Non-zero debit should pass through");
    }

    @Test
    void testDateTruncationInOutput() throws Exception {
        launchUniqueJob();

        List<String> outLines = Files.readAllLines(outfilePath);
        for (String line : outLines) {
            String[] fields = line.split("\\|");
            assertEquals(8, fields[7].length(), "Reissue date should be 8 chars");
        }
    }

    @Test
    void testArrayRecordHardcodedValues() throws Exception {
        launchUniqueJob();

        List<String> arrLines = Files.readAllLines(arryfilePath);
        String[] first = arrLines.get(0).split("\\|");

        assertEquals("1005.00", first[2]);
        assertEquals("1525.00", first[4]);
        assertEquals("-1025.00", first[5]);
        assertEquals("-2500.00", first[6]);
        assertEquals("0.00", first[7]);
        assertEquals("0.00", first[8]);
        assertEquals("0.00", first[9]);
        assertEquals("0.00", first[10]);
    }

    @Test
    void testVbrcRecordPairing() throws Exception {
        launchUniqueJob();

        List<String> vbrcLines = Files.readAllLines(vbrcfilePath);
        for (int i = 0; i < vbrcLines.size(); i += 2) {
            String[] rec1 = vbrcLines.get(i).split("\\|");
            assertEquals(2, rec1.length, "VB REC1 should have 2 fields");

            String[] rec2 = vbrcLines.get(i + 1).split("\\|");
            assertEquals(4, rec2.length, "VB REC2 should have 4 fields");

            assertEquals(rec1[0], rec2[0], "REC1 and REC2 should have same account ID");
        }
    }

    @Test
    void testVbrcReissueYearExtraction() throws Exception {
        launchUniqueJob();

        List<String> vbrcLines = Files.readAllLines(vbrcfilePath);
        String[] rec2 = vbrcLines.get(1).split("\\|");
        assertEquals("2025", rec2[3], "VBRC REC2 should extract reissue year");
    }
}
