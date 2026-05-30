package com.mainframe.carddemo.report.job.export;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.client.TransactionServiceClient;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema-export.sql", "/data-export.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DataExportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("dataExportJob")
    private Job dataExportJob;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private TransactionServiceClient transactionServiceClient;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(dataExportJob);
    }

    @Test
    void shouldExportAllTablesWithCorrectPrefixes() throws Exception {
        File outputFile = tempDir.resolve("export.dat").toFile();

        JobParameters params = new JobParametersBuilder()
                .addString("outputFile", outputFile.getAbsolutePath())
                .addString("schemaPrefix", "false")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<String> lines = Files.readAllLines(outputFile.toPath());
        Map<String, List<String>> byType = lines.stream()
                .collect(Collectors.groupingBy(l -> l.substring(0, l.indexOf('|'))));

        assertEquals(2, byType.getOrDefault("USR", List.of()).size());
        assertEquals(1, byType.getOrDefault("CUST", List.of()).size());
        assertEquals(1, byType.getOrDefault("ACCT", List.of()).size());
        assertEquals(1, byType.getOrDefault("CARD", List.of()).size());
        assertEquals(1, byType.getOrDefault("XREF", List.of()).size());
        assertEquals(1, byType.getOrDefault("TRAN", List.of()).size());
        assertEquals(1, byType.getOrDefault("DTRAN", List.of()).size());
        assertEquals(2, byType.getOrDefault("TYPE", List.of()).size());
        assertEquals(2, byType.getOrDefault("CAT", List.of()).size());
        assertEquals(1, byType.getOrDefault("BAL", List.of()).size());
        assertEquals(1, byType.getOrDefault("DISC", List.of()).size());
    }

    @Test
    void shouldExportRecordsWithCorrectFormat() throws Exception {
        File outputFile = tempDir.resolve("export-format.dat").toFile();

        JobParameters params = new JobParametersBuilder()
                .addString("outputFile", outputFile.getAbsolutePath())
                .addString("schemaPrefix", "false")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<String> lines = Files.readAllLines(outputFile.toPath());

        String usrLine = lines.stream().filter(l -> l.startsWith("USR|USER0001")).findFirst().orElseThrow();
        assertTrue(usrLine.contains("John"));
        assertTrue(usrLine.contains("Doe"));

        String acctLine = lines.stream().filter(l -> l.startsWith("ACCT|")).findFirst().orElseThrow();
        assertTrue(acctLine.contains("1500"));
        assertTrue(acctLine.contains("10000"));

        String tranLine = lines.stream().filter(l -> l.startsWith("TRAN|")).findFirst().orElseThrow();
        assertTrue(tranLine.contains("Purchase at Store"));

        String discLine = lines.stream().filter(l -> l.startsWith("DISC|")).findFirst().orElseThrow();
        assertTrue(discLine.contains("A000000000"));
        assertTrue(discLine.contains("15"));
    }

    @Test
    void shouldProduceCorrectRecordCount() throws Exception {
        File outputFile = tempDir.resolve("export-count.dat").toFile();

        JobParameters params = new JobParametersBuilder()
                .addString("outputFile", outputFile.getAbsolutePath())
                .addString("schemaPrefix", "false")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(14, lines.size());
    }
}
