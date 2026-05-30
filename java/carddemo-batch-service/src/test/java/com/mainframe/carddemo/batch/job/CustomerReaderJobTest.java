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
class CustomerReaderJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("customerReaderJob")
    private Job customerReaderJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(customerReaderJob);

        jdbcTemplate.update("DELETE FROM customer");
        jdbcTemplate.update(
                "INSERT INTO customer (cust_id, cust_first_name, cust_middle_name, cust_last_name, " +
                        "cust_addr_line_1, cust_addr_line_2, cust_addr_line_3, cust_addr_state_cd, " +
                        "cust_addr_country_cd, cust_addr_zip, cust_phone_num_1, cust_phone_num_2, " +
                        "cust_ssn, cust_govt_issued_id, cust_dob, cust_eft_account_id, " +
                        "cust_pri_card_holder_ind, cust_fico_credit_score) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1000001L, "John", "M", "Doe", "123 Main St", "", "",
                "NY", "USA", "10001", "555-0101", "555-0102",
                123456789L, "DL12345", Date.valueOf(LocalDate.of(1985, 3, 15)),
                "EFT001", "Y", 750
        );
    }

    @Test
    void shouldCompleteCustomerReaderJob() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void shouldWriteOutputFile() throws Exception {
        jobLauncherTestUtils.launchJob();

        Path outputFile = Path.of("./target/test-batch-output/customers.dat");
        assertTrue(Files.exists(outputFile), "customers.dat should exist");
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());

        String line = lines.get(0);
        String[] fields = line.split(",");
        assertEquals("1000001", fields[0]);
        assertEquals("John", fields[1]);
        assertEquals(18, fields.length);
    }
}
