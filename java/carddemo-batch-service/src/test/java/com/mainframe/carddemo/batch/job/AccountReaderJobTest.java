package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class AccountReaderJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("accountReaderJob")
    private Job accountReaderJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(accountReaderJob);

        jdbcTemplate.update("DELETE FROM account");
        jdbcTemplate.update(
                "INSERT INTO account (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit, " +
                        "acct_cash_credit_limit, acct_open_date, acct_expiration_date, acct_reissue_date, " +
                        "acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                10000000001L, "Y", new BigDecimal("1500.00"), new BigDecimal("5000.00"),
                new BigDecimal("1000.00"), Date.valueOf(LocalDate.of(2020, 1, 15)),
                Date.valueOf(LocalDate.of(2025, 1, 15)), Date.valueOf(LocalDate.of(2023, 6, 1)),
                new BigDecimal("200.00"), new BigDecimal("50.00"), "10001", "GRP001"
        );
    }

    @Test
    void shouldCompleteAccountReaderJob() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void shouldWriteOutputFile() throws Exception {
        jobLauncherTestUtils.launchJob();

        Path outputFile = Path.of("./target/test-batch-output/accounts.dat");
        assertTrue(Files.exists(outputFile), "accounts.dat should exist");
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());

        String line = lines.get(0);
        String[] fields = line.split(",");
        assertEquals("10000000001", fields[0]);
        assertEquals("Y", fields[1]);

        // Validate against test-harness RecordLayout expectations:
        // outfileLayout() defines 12 fields matching our delimited output field order.
        int expectedFieldCount = 12;
        assertEquals(expectedFieldCount, fields.length,
                "Output field count should match COBOL OUTFILE layout field count");
    }
}
