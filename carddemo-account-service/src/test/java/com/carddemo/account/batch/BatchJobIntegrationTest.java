package com.carddemo.account.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BatchJobIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carddemo_account_db")
            .withUsername("carddemo")
            .withPassword("carddemo");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Map<String, Job> jobs;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FlatFileItemWriter<?> accountItemWriter;

    @Autowired
    private FlatFileItemWriter<?> cardItemWriter;

    @Autowired
    private FlatFileItemWriter<?> xrefItemWriter;

    @Autowired
    private FlatFileItemWriter<?> customerItemWriter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM card_xref");
        jdbcTemplate.execute("DELETE FROM cards");
        jdbcTemplate.execute("DELETE FROM customers");
        jdbcTemplate.execute("DELETE FROM accounts");

        // Redirect writers to temp directory
        accountItemWriter.setResource(new FileSystemResource(tempDir.resolve("accounts.dat").toFile()));
        cardItemWriter.setResource(new FileSystemResource(tempDir.resolve("cards.dat").toFile()));
        xrefItemWriter.setResource(new FileSystemResource(tempDir.resolve("cardxref.dat").toFile()));
        customerItemWriter.setResource(new FileSystemResource(tempDir.resolve("customers.dat").toFile()));
    }

    @Test
    void testAccountReaderJob() throws Exception {
        jdbcTemplate.update("INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, " +
                "acct_credit_limit, acct_cash_credit_limit, acct_open_date, acct_expiration_date, " +
                "acct_reissue_date, acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1L, "Y", 1000.50, 5000.00, 2000.00, "2020-01-15", "2025-01-15", "2023-01-15",
                200.00, 150.00, "10001", "GRP001");

        jdbcTemplate.update("INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, " +
                "acct_credit_limit, acct_cash_credit_limit, acct_open_date, acct_expiration_date, " +
                "acct_reissue_date, acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                2L, "N", 2500.00, 10000.00, 3000.00, "2019-06-01", "2024-06-01", "2022-06-01",
                500.00, 300.00, "90210", "GRP002");

        Job job = jobs.get("accountReaderJob");
        assertNotNull(job);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(job, params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        File outputFile = tempDir.resolve("accounts.dat").toFile();
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(2, lines.size());
        assertEquals(300, lines.get(0).length());
        assertEquals(300, lines.get(1).length());
        assertTrue(lines.get(0).startsWith("00000000001Y"));
    }

    @Test
    void testCardReaderJob() throws Exception {
        jdbcTemplate.update("INSERT INTO accounts (acct_id, acct_active_status) VALUES (?, ?)", 1L, "Y");
        jdbcTemplate.update("INSERT INTO cards (card_num, card_acct_id, card_cvv_cd, card_embossed_name, " +
                "card_expiration_date, card_active_status) VALUES (?, ?, ?, ?, ?, ?)",
                "4111111111111111", 1L, 123, "JOHN DOE", "2025-12-31", "Y");

        Job job = jobs.get("cardReaderJob");
        assertNotNull(job);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(job, params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        File outputFile = tempDir.resolve("cards.dat").toFile();
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(1, lines.size());
        assertEquals(150, lines.get(0).length());
        assertTrue(lines.get(0).startsWith("4111111111111111"));
    }

    @Test
    void testXrefReaderJob() throws Exception {
        jdbcTemplate.update("INSERT INTO accounts (acct_id, acct_active_status) VALUES (?, ?)", 1L, "Y");
        jdbcTemplate.update("INSERT INTO customers (cust_id, cust_first_name, cust_last_name) VALUES (?, ?, ?)",
                1L, "John", "Doe");
        jdbcTemplate.update("INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES (?, ?, ?)",
                "4111111111111111", 1L, 1L);

        Job job = jobs.get("xrefReaderJob");
        assertNotNull(job);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(job, params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        File outputFile = tempDir.resolve("cardxref.dat").toFile();
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(1, lines.size());
        assertEquals(50, lines.get(0).length());
        assertTrue(lines.get(0).startsWith("4111111111111111"));
    }

    @Test
    void testCustomerReaderJob() throws Exception {
        jdbcTemplate.update("INSERT INTO customers (cust_id, cust_first_name, cust_middle_name, " +
                "cust_last_name, cust_addr_line_1, cust_addr_line_2, cust_addr_line_3, " +
                "cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip, cust_phone_num_1, " +
                "cust_phone_num_2, cust_ssn, cust_govt_issued_id, cust_dob, cust_eft_account_id, " +
                "cust_pri_card_holder_ind, cust_fico_credit_score) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1L, "John", "M", "Doe", "123 Main St", "Apt 4", "", "NY", "USA", "10001",
                "555-123-4567", "555-987-6543", 123456789L, "DL12345", "1990-05-15", "EFT001", "Y", 750);

        Job job = jobs.get("customerReaderJob");
        assertNotNull(job);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(job, params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        File outputFile = tempDir.resolve("customers.dat").toFile();
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(1, lines.size());
        assertEquals(500, lines.get(0).length());
        assertTrue(lines.get(0).startsWith("000000001John"));
    }

    @Test
    void testFlywayMigrationsApply() {
        // The fact that the context loads and we can execute queries means Flyway ran successfully
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name IN ('accounts', 'customers', 'cards', 'card_xref')", Integer.class);
        assertEquals(4, count);
    }

    @Test
    void testEmptyDataProducesEmptyFile() throws Exception {
        Job job = jobs.get("accountReaderJob");
        assertNotNull(job);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(job, params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        File outputFile = tempDir.resolve("accounts.dat").toFile();
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(0, lines.size());
    }
}
