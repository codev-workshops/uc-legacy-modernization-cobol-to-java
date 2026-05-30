package com.mainframe.carddemo.report.job.importjob;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema-export.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DataImportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("dataImportJob")
    private Job dataImportJob;

    @Autowired
    @Qualifier("dataExportJob")
    private Job dataExportJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private TransactionServiceClient transactionServiceClient;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(dataImportJob);
    }

    @Test
    void shouldImportAllRecordTypes() throws Exception {
        File inputFile = tempDir.resolve("import.dat").toFile();
        List<String> lines = List.of(
                "USR|ADMIN001|Admin|User|secret123|A",
                "TYPE|01|Purchase",
                "TYPE|02|Payment",
                "CAT|01|1|Regular Sales Draft",
                "ACCT|100|Y|5000.00|20000.00|10000.00|2020-01-01|2025-12-31|2025-01-01|0.00|0.00|10001|A000000000",
                "CARD|4111111111111111|100|123|ADMIN USER|2025-12-31|Y",
                "XREF|4111111111111111|1|100",
                "DISC|A000000000|01|1|15.00",
                "BAL|100|01|1|250.00"
        );
        Files.write(inputFile.toPath(), lines);

        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", inputFile.getAbsolutePath())
                .addString("schemaPrefix", "false")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        assertEquals(1, countRows("user_security"));
        assertEquals(2, countRows("tran_type"));
        assertEquals(1, countRows("tran_category"));
        assertEquals(1, countRows("account"));
        assertEquals(1, countRows("card"));
        assertEquals(1, countRows("card_xref"));
        assertEquals(1, countRows("disclosure_group"));
        assertEquals(1, countRows("tran_cat_balance"));
    }

    @Test
    void shouldSkipInvalidRecords() throws Exception {
        File inputFile = tempDir.resolve("import-invalid.dat").toFile();
        List<String> lines = List.of(
                "INVALID|some|data",
                "TYPE|03|Refund",
                "",
                "TYPE|04|Transfer"
        );
        Files.write(inputFile.toPath(), lines);

        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", inputFile.getAbsolutePath())
                .addString("schemaPrefix", "false")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        assertEquals(2, countRows("tran_type"));
    }

    @Test
    void shouldRoundTripExportThenImport() throws Exception {
        // Insert test data
        jdbcTemplate.execute("INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) " +
                "VALUES ('RT_USR01', 'Round', 'Trip', 'pwd', 'A')");
        jdbcTemplate.execute("INSERT INTO tran_type (tran_type, tran_type_desc) VALUES ('05', 'Test Type')");

        // Run export
        File exportFile = tempDir.resolve("roundtrip.dat").toFile();
        jobLauncherTestUtils.setJob(dataExportJob);
        JobParameters exportParams = new JobParametersBuilder()
                .addString("outputFile", exportFile.getAbsolutePath())
                .addString("schemaPrefix", "false")
                .toJobParameters();
        JobExecution exportExec = jobLauncherTestUtils.launchJob(exportParams);
        assertEquals(BatchStatus.COMPLETED, exportExec.getStatus());

        List<String> exportedLines = Files.readAllLines(exportFile.toPath());
        long usrCount = exportedLines.stream().filter(l -> l.startsWith("USR|")).count();
        long typeCount = exportedLines.stream().filter(l -> l.startsWith("TYPE|")).count();
        assertEquals(1, usrCount);
        assertEquals(1, typeCount);

        // Clear tables
        jdbcTemplate.execute("DELETE FROM user_security");
        jdbcTemplate.execute("DELETE FROM tran_type");
        assertEquals(0, countRows("user_security"));
        assertEquals(0, countRows("tran_type"));

        // Run import with exported data
        jobLauncherTestUtils.setJob(dataImportJob);
        JobParameters importParams = new JobParametersBuilder()
                .addString("inputFile", exportFile.getAbsolutePath())
                .addString("schemaPrefix", "false")
                .toJobParameters();
        JobExecution importExec = jobLauncherTestUtils.launchJob(importParams);
        assertEquals(BatchStatus.COMPLETED, importExec.getStatus());

        // Verify round-trip integrity
        assertEquals(1, countRows("user_security"));
        assertEquals("Round", jdbcTemplate.queryForObject(
                "SELECT usr_fname FROM user_security WHERE usr_id = 'RT_USR01'", String.class));
        assertEquals(1, countRows("tran_type"));
        assertEquals("Test Type", jdbcTemplate.queryForObject(
                "SELECT tran_type_desc FROM tran_type WHERE tran_type = '05'", String.class));
    }

    private int countRows(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
