package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.dto.TransactionTypeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carddemo_transaction_db")
            .withUsername("carddemo")
            .withPassword("carddemo");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    static WireMockServer wireMock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        registry.add("carddemo.batch.output-dir", () -> System.getProperty("java.io.tmpdir") + "/carddemo-rest-test");
    }

    @Test
    @Order(1)
    void createTransactionType() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("IT")
                .tranTypeDesc("Integration Test Type")
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranType").value("IT"))
                .andExpect(jsonPath("$.tranTypeDesc").value("Integration Test Type"));
    }

    @Test
    @Order(2)
    void listTransactionTypes() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/transaction-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void getTransactionType() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/transaction-types/IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranType").value("IT"));
    }

    @Test
    @Order(4)
    void updateTransactionType() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("IT")
                .tranTypeDesc("Updated IT Type")
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/transaction-types/IT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranTypeDesc").value("Updated IT Type"));
    }

    @Test
    @Order(5)
    void createTransaction_withWireMock() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/api/card-xref/4111111111111111"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"xrefCardNum\":\"4111111111111111\"," +
                                "\"xrefCustId\":2001,\"xrefAcctId\":1001}")));

        stubFor(WireMock.get(urlPathEqualTo("/api/accounts/1001"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1001,\"acctActiveStatus\":\"Y\"," +
                                "\"acctCurrBal\":100.00,\"acctCreditLimit\":5000.00}")));

        stubFor(WireMock.put(urlPathEqualTo("/api/accounts/1001"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1001,\"acctActiveStatus\":\"Y\"," +
                                "\"acctCurrBal\":150.00,\"acctCreditLimit\":5000.00}")));

        TransactionDto txDto = TransactionDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranSource("ONLINE")
                .tranDesc("Test transaction")
                .tranAmt(new BigDecimal("50.00"))
                .tranCardNum("4111111111111111")
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").exists())
                .andExpect(jsonPath("$.tranCardNum").value("4111111111111111"));
    }

    @Test
    @Order(6)
    void listTransactions() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(7)
    void listTransactionCategories() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/transaction-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(8)
    void generateReport() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/reports/transactions")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @Order(9)
    void deleteTransactionType() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("99")
                .tranTypeDesc("ToDelete")
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/transaction-types/99"))
                .andExpect(status().isNoContent());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/transaction-types/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void getTransactionType_notFound() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/transaction-types/XX"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    void getTransactionCategory_byCompositeKey_found() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/transaction-categories/01/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranTypeCd").value("01"))
                .andExpect(jsonPath("$.tranCatCd").value(1));
    }
}
