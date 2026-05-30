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
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class CardReaderJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("cardReaderJob")
    private Job cardReaderJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(cardReaderJob);

        jdbcTemplate.update("DELETE FROM card");
        jdbcTemplate.update(
                "INSERT INTO card (card_num, card_acct_id, card_cvv_cd, card_embossed_name, " +
                        "card_expiration_date, card_active_status) VALUES (?, ?, ?, ?, ?, ?)",
                "4111111111111111", 10000000001L, 123, "JOHN M DOE",
                Date.valueOf(LocalDate.of(2025, 12, 31)), "Y"
        );
    }

    @Test
    void shouldCompleteCardReaderJob() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void shouldWriteOutputFile() throws Exception {
        jobLauncherTestUtils.launchJob();

        Path outputFile = Path.of("./target/test-batch-output/cards.dat");
        assertTrue(Files.exists(outputFile), "cards.dat should exist");
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());

        String line = lines.get(0);
        String[] fields = line.split(",");
        assertEquals("4111111111111111", fields[0]);
        assertEquals(6, fields.length);
    }
}
