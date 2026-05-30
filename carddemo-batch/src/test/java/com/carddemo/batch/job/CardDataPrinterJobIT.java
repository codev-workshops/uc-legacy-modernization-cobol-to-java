package com.carddemo.batch.job;

import com.carddemo.common.entity.Card;
import com.carddemo.common.repository.CardRepository;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest
class CardDataPrinterJobIT {

    private static Path outputFile;

    @TempDir
    static Path tempDir;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job cardDataPrinterJob;

    @Autowired
    private CardRepository cardRepository;

    @DynamicPropertySource
    static void configureOutputFile(DynamicPropertyRegistry registry) {
        outputFile = tempDir.resolve("card-data-report.txt");
        registry.add("carddemo.batch.output-file", () -> outputFile.toString());
    }

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        jobLauncherTestUtils.setJob(cardDataPrinterJob);
        loadCardDataFromFile();
    }

    private void loadCardDataFromFile() {
        Path dataFile = Path.of("app/data/ASCII/carddata.txt");
        if (!Files.exists(dataFile)) {
            dataFile = Path.of("../app/data/ASCII/carddata.txt");
        }

        List<Card> cards = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(dataFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < 91) {
                    continue;
                }
                Card card = new Card();
                card.setCardNum(line.substring(0, 16).trim());
                card.setAcctId(Long.parseLong(line.substring(16, 27).trim()));
                card.setCvvCd(Integer.parseInt(line.substring(27, 30).trim()));
                card.setEmbossedName(line.substring(30, 80).trim());
                card.setExpirationDate(line.substring(80, 90).trim());
                card.setActiveStatus(line.substring(90, 91).trim());
                cards.add(card);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load card data file", e);
        }
        cardRepository.saveAll(cards);
    }

    @Test
    void jobCompletesSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void outputFileContainsExpectedRecords() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);

        // First line is header
        assertEquals("START OF EXECUTION OF PROGRAM CBACT02C", lines.get(0));
        // Last line is footer
        assertEquals("END OF EXECUTION OF PROGRAM CBACT02C", lines.get(lines.size() - 1));

        // Data lines (50 records from carddata.txt)
        long recordCount = cardRepository.count();
        // lines = header + records + footer
        assertEquals(recordCount + 2, lines.size());

        // Verify first data record format (150 chars)
        String firstRecord = lines.get(1);
        assertEquals(150, firstRecord.length());
    }

    @Test
    void outputMatchesCobolFormat() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        List<String> lines = Files.readAllLines(outputFile);
        // Skip header and footer, check a data line
        String dataLine = lines.get(1);

        // First 16 chars should be card number
        String cardNum = dataLine.substring(0, 16);
        assertTrue(cardNum.matches("[0-9]+"));

        // Next 11 chars should be account ID (zero-padded numeric)
        String acctId = dataLine.substring(16, 27);
        assertTrue(acctId.matches("[0-9]{11}"));

        // Next 3 chars should be CVV (zero-padded numeric)
        String cvv = dataLine.substring(27, 30);
        assertTrue(cvv.matches("[0-9]{3}"));

        // Chars 80-90 should be date in YYYY-MM-DD format
        String expDate = dataLine.substring(80, 90).trim();
        assertTrue(expDate.matches("\\d{4}-\\d{2}-\\d{2}"));

        // Char 90 should be active status
        String status = dataLine.substring(90, 91);
        assertTrue(status.equals("Y") || status.equals("N"));
    }
}
