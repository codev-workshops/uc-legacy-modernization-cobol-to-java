package com.mainframe.carddemo.batch.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class XrefReaderJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("xrefReaderJob")
    private Job xrefReaderJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(xrefReaderJob);

        jdbcTemplate.update("DELETE FROM card_xref");
        jdbcTemplate.update(
                "INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES (?, ?, ?)",
                "4111111111111111", 1000001L, 10000000001L
        );
    }

    @Test
    void shouldCompleteXrefReaderJob() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void shouldWriteOutputFile() throws Exception {
        jobLauncherTestUtils.launchJob();

        Path outputFile = Path.of("./target/test-batch-output/card_xref.dat");
        assertTrue(Files.exists(outputFile), "card_xref.dat should exist");
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());

        String line = lines.get(0);
        String[] fields = line.split(",");
        assertEquals("4111111111111111", fields[0]);
        assertEquals("1000001", fields[1]);
        assertEquals("10000000001", fields[2]);
        assertEquals(3, fields.length);
    }
}
