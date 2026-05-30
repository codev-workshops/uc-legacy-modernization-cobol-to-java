package com.carddemo.batch.integration;

import com.carddemo.batch.writer.CustomerRecordFormatter;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.repository.CustomerRepository;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringBootTest
class CustomerReaderJobIT {

    private static Path outputDir;

    @TempDir
    static Path tempDir;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private CustomerRepository customerRepository;

    @DynamicPropertySource
    static void overrideOutputPath(DynamicPropertyRegistry registry) {
        outputDir = tempDir;
        registry.add("batch.customer-report.output-path",
                () -> tempDir.resolve("customer-report.txt").toString());
    }

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void jobCompletesSuccessfullyWithCustomerData() throws Exception {
        loadTestCustomers();

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueJobParams());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Path outputFile = outputDir.resolve("customer-report.txt");
        assertTrue(Files.exists(outputFile));

        List<String> lines = Files.readAllLines(outputFile, StandardCharsets.UTF_8);
        // header + 3 records + footer = 5 lines
        assertEquals(5, lines.size());
        assertEquals("START OF EXECUTION OF PROGRAM CBCUS01C", lines.get(0));
        assertEquals("END OF EXECUTION OF PROGRAM CBCUS01C", lines.get(lines.size() - 1));

        // Verify record content
        for (int i = 1; i <= 3; i++) {
            assertEquals(CustomerRecordFormatter.RECORD_LENGTH, lines.get(i).length());
        }
        // First record should start with custId=1
        assertTrue(lines.get(1).startsWith("000000001"));
        assertTrue(lines.get(2).startsWith("000000002"));
        assertTrue(lines.get(3).startsWith("000000003"));
    }

    @Test
    void jobCompletesSuccessfullyWithEmptyTable() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueJobParams());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Path outputFile = outputDir.resolve("customer-report.txt");
        assertTrue(Files.exists(outputFile));

        List<String> lines = Files.readAllLines(outputFile, StandardCharsets.UTF_8);
        // header + footer only
        assertEquals(2, lines.size());
        assertEquals("START OF EXECUTION OF PROGRAM CBCUS01C", lines.get(0));
        assertEquals("END OF EXECUTION OF PROGRAM CBCUS01C", lines.get(1));
    }

    @Test
    void jobOutputMatchesCustdataFormat() throws Exception {
        loadCustdataFromFile();

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueJobParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Path outputFile = outputDir.resolve("customer-report.txt");
        List<String> lines = Files.readAllLines(outputFile, StandardCharsets.UTF_8);

        // All data lines should be 500 chars
        for (int i = 1; i < lines.size() - 1; i++) {
            assertEquals(500, lines.get(i).length(),
                    "Record at line " + i + " should be 500 characters");
        }
    }

    private void loadTestCustomers() {
        customerRepository.save(buildCustomer(1L, "Alice", "B", "Smith",
                "100 First Ave", "Apt 1", "CityOne", "NY", "USA", "10001",
                "(212)555-0001", "(212)555-0002", 111111111L,
                "NY11111", "1980-01-01", "0011111111", "Y", 700));
        customerRepository.save(buildCustomer(2L, "Bob", "C", "Jones",
                "200 Second St", "Suite 2", "CityTwo", "CA", "USA", "90001",
                "(310)555-0003", "(310)555-0004", 222222222L,
                "CA22222", "1985-06-15", "0022222222", "N", 650));
        customerRepository.save(buildCustomer(3L, "Carol", "D", "White",
                "300 Third Blvd", "Unit 3", "CityThree", "TX", "USA", "73301",
                "(512)555-0005", "(512)555-0006", 333333333L,
                "TX33333", "1990-12-31", "0033333333", "Y", 800));
    }

    private void loadCustdataFromFile() throws IOException {
        Path custdata = Path.of("app/data/ASCII/custdata.txt");
        if (!Files.exists(custdata)) {
            custdata = Path.of("../app/data/ASCII/custdata.txt");
        }
        if (!Files.exists(custdata)) {
            // In CI, may not have file available — use test data
            loadTestCustomers();
            return;
        }
        List<String> lines = Files.readAllLines(custdata, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            Customer c = parseFixedWidthCustomer(line);
            customerRepository.save(c);
        }
    }

    static Customer parseFixedWidthCustomer(String line) {
        Customer c = new Customer();
        c.setCustId(Long.parseLong(line.substring(0, 9).trim()));
        c.setFirstName(line.substring(9, 34).trim());
        c.setMiddleName(line.substring(34, 59).trim());
        c.setLastName(line.substring(59, 84).trim());
        c.setAddrLine1(line.substring(84, 134).trim());
        c.setAddrLine2(line.substring(134, 184).trim());
        c.setAddrLine3(line.substring(184, 234).trim());
        c.setStateCode(line.substring(234, 236).trim());
        c.setCountryCode(line.substring(236, 239).trim());
        c.setZip(line.substring(239, 249).trim());
        c.setPhone1(line.substring(249, 264).trim());
        c.setPhone2(line.substring(264, 279).trim());
        c.setSsn(Long.parseLong(line.substring(279, 288).trim()));
        c.setGovtIssuedId(line.substring(288, 308).trim());
        c.setDob(line.substring(308, 318).trim());
        c.setEftAccountId(line.substring(318, 328).trim());
        c.setPriCardHolderInd(line.substring(328, 329).trim());
        c.setFicoCreditScore(Integer.parseInt(line.substring(329, 332).trim()));
        return c;
    }

    private Customer buildCustomer(
            Long id, String first, String middle, String last,
            String addr1, String addr2, String addr3,
            String state, String country, String zip,
            String phone1, String phone2, Long ssn,
            String govtId, String dob, String eftId,
            String priHolder, Integer fico) {
        Customer c = new Customer();
        c.setCustId(id);
        c.setFirstName(first);
        c.setMiddleName(middle);
        c.setLastName(last);
        c.setAddrLine1(addr1);
        c.setAddrLine2(addr2);
        c.setAddrLine3(addr3);
        c.setStateCode(state);
        c.setCountryCode(country);
        c.setZip(zip);
        c.setPhone1(phone1);
        c.setPhone2(phone2);
        c.setSsn(ssn);
        c.setGovtIssuedId(govtId);
        c.setDob(dob);
        c.setEftAccountId(eftId);
        c.setPriCardHolderInd(priHolder);
        c.setFicoCreditScore(fico);
        return c;
    }

    private JobParameters uniqueJobParams() {
        return new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
    }
}
