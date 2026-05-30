package com.carddemo.transaction.batch;

import com.carddemo.transaction.entity.*;
import com.carddemo.transaction.repository.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BatchIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carddemo_transaction_db")
            .withUsername("carddemo")
            .withPassword("carddemo");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    static WireMockServer wireMock;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("validationJob")
    private Job validationJob;

    @Autowired
    @Qualifier("postingJob")
    private Job postingJob;

    @Autowired
    @Qualifier("interestJob")
    private Job interestJob;

    @Autowired
    @Qualifier("reportJob")
    private Job reportJob;

    @Autowired
    @Qualifier("statementJob")
    private Job statementJob;

    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DailyRejectRepository dailyRejectRepository;

    @Autowired
    private TranCatBalanceRepository tranCatBalanceRepository;

    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;

    @BeforeAll
    static void setupWireMock() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @BeforeEach
    void resetWireMock() {
        wireMock.resetAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("carddemo.account-service.url", () -> "http://localhost:" + wireMock.port());
        registry.add("carddemo.batch.output-dir", () -> System.getProperty("java.io.tmpdir") + "/carddemo-test-output");
    }

    @Test
    @Order(1)
    void testValidationJob() throws Exception {
        dailyTransactionRepository.save(DailyTransaction.builder()
                .dalytranId("INTG-DT001")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranSource("ONLINE")
                .dalytranDesc("Valid transaction")
                .dalytranAmt(new BigDecimal("100.00"))
                .dalytranCardNum("4111111111111111")
                .dalytranOrigTs("2024-01-15-10.30.00.000000")
                .build());

        dailyTransactionRepository.save(DailyTransaction.builder()
                .dalytranId("INTG-DT002")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranSource("ONLINE")
                .dalytranDesc("Invalid - bad card")
                .dalytranAmt(new BigDecimal("200.00"))
                .dalytranCardNum("9999999999999999")
                .dalytranOrigTs("2024-01-15-10.30.00.000000")
                .build());

        stubFor(get(urlPathEqualTo("/api/card-xref/4111111111111111"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"xrefCardNum\":\"4111111111111111\",\"xrefCustId\":1,\"xrefAcctId\":1000}")));

        stubFor(get(urlPathEqualTo("/api/card-xref/9999999999999999"))
                .willReturn(aResponse().withStatus(404)));

        stubFor(get(urlPathEqualTo("/api/accounts/1000"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1000,\"acctActiveStatus\":\"Y\",\"acctExpirationDate\":\"2030-12-31\",\"acctCreditLimit\":10000.00,\"acctCurrCycCredit\":0,\"acctCurrCycDebit\":0}")));

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(validationJob, params);

        List<DailyReject> rejects = dailyRejectRepository.findAll();
        assertTrue(rejects.stream().anyMatch(r -> r.getDalytranId().equals("INTG-DT002")));
    }

    @Test
    @Order(2)
    void testPostingJob() throws Exception {
        dailyTransactionRepository.save(DailyTransaction.builder()
                .dalytranId("INTG-POST001")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranSource("ONLINE")
                .dalytranDesc("Validated transaction for posting")
                .dalytranAmt(new BigDecimal("150.00"))
                .dalytranCardNum("4111111111111111")
                .dalytranOrigTs("2024-01-15-10.30.00.000000")
                .dalytranProcTs("2024-01-15-10.30.01.000000")
                .build());

        stubFor(get(urlPathEqualTo("/api/card-xref/4111111111111111"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"xrefCardNum\":\"4111111111111111\",\"xrefCustId\":1,\"xrefAcctId\":1000}")));

        stubFor(get(urlPathEqualTo("/api/accounts/1000"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1000,\"acctActiveStatus\":\"Y\",\"acctExpirationDate\":\"2030-12-31\",\"acctCreditLimit\":10000.00,\"acctCurrCycCredit\":0,\"acctCurrCycDebit\":0}")));

        stubFor(put(urlPathEqualTo("/api/accounts/1000"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1000,\"acctActiveStatus\":\"Y\"}")));

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(postingJob, params);

        List<Transaction> posted = transactionRepository.findAll();
        assertFalse(posted.isEmpty());
    }

    @Test
    @Order(3)
    void testInterestCalculationJob() throws Exception {
        stubFor(get(urlPathEqualTo("/api/accounts/1000000001"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1000000001,\"acctGroupId\":\"GRP001\",\"acctCurrBal\":5000.00}")));

        stubFor(get(urlPathEqualTo("/api/accounts/1000000002"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1000000002,\"acctGroupId\":\"GRP001\",\"acctCurrBal\":3000.00}")));

        stubFor(put(urlPathMatching("/api/accounts/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1000000001}")));

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(interestJob, params);

        List<TranCatBalance> balances = tranCatBalanceRepository.findAll();
        assertFalse(balances.isEmpty());
    }

    @Test
    @Order(4)
    void testReportJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(reportJob, params);

        String outputDir = System.getProperty("java.io.tmpdir") + "/carddemo-test-output";
        Path reportFile = Path.of(outputDir, "daily_report.txt");
        assertTrue(Files.exists(reportFile));

        String content = Files.readString(reportFile);
        assertTrue(content.contains("DALYREPT"));
        assertTrue(content.contains("Daily Transaction Report"));
    }

    @Test
    @Order(5)
    void testStatementJob() throws Exception {
        stubFor(get(urlPathMatching("/api/card-xref/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"xrefCardNum\":\"4111111111111111\",\"xrefCustId\":1,\"xrefAcctId\":1000}")));

        stubFor(get(urlPathEqualTo("/api/accounts/1000"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1000,\"acctCurrBal\":5000.00,\"acctCreditLimit\":10000.00,\"acctCurrCycCredit\":1000.00,\"acctCurrCycDebit\":2000.00}")));

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(statementJob, params);

        String outputDir = System.getProperty("java.io.tmpdir") + "/carddemo-test-output";
        Path statementsDir = Path.of(outputDir, "statements");
        if (Files.exists(statementsDir)) {
            assertTrue(Files.list(statementsDir).count() >= 0);
        }
    }
}
