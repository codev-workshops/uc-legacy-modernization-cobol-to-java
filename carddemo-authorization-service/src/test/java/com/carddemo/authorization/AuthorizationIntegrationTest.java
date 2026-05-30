package com.carddemo.authorization;

import com.carddemo.authorization.config.RabbitMQConfig;
import com.carddemo.authorization.entity.AuthFraud;
import com.carddemo.authorization.entity.Authorization;
import com.carddemo.authorization.repository.AuthFraudRepository;
import com.carddemo.authorization.repository.AuthorizationRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthorizationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carddemo_authz_db")
            .withUsername("carddemo")
            .withPassword("carddemo");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private AuthFraudRepository authFraudRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("carddemo.account-service.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        authFraudRepository.deleteAll();
        authorizationRepository.deleteAll();
        wireMockServer.resetAll();
    }

    @Test
    void fullMqFlow_approvedAuthorization() throws Exception {
        wireMockServer.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/card-xref/4111111111111111"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"xrefCardNum\":\"4111111111111111\",\"xrefAcctId\":1001,\"xrefCustId\":2001}")));

        wireMockServer.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/accounts/1001"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"acctId\":1001,\"acctActiveStatus\":\"Y\",\"acctCurrBal\":500.00,\"acctCreditLimit\":5000.00}")));

        String csvMessage = "2025-01-15,10:30:00,4111111111111111,SALE,1226,100000,ONLINE," +
                "000000,150.00,5411,840,05,MERCH001,Test Merchant,New York,NY,10001,TXN001";

        rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_REQUEST_QUEUE, csvMessage);

        // Poll for the authorization to be saved
        List<Authorization> auths = List.of();
        for (int i = 0; i < 20; i++) {
            Thread.sleep(500);
            auths = authorizationRepository.findByCardNum("4111111111111111");
            if (!auths.isEmpty()) break;
        }

        assertFalse(auths.isEmpty(), "Authorization should have been saved");
        assertEquals("00", auths.get(0).getAuthRespCode());
        assertEquals(new BigDecimal("150.00"), auths.get(0).getApprovedAmt());

        Object reply = rabbitTemplate.receiveAndConvert(RabbitMQConfig.AUTH_REPLY_QUEUE, 5000);
        assertNotNull(reply);
        String replyStr = reply.toString();
        assertTrue(replyStr.contains("4111111111111111"));
        assertTrue(replyStr.contains("TXN001"));
        assertTrue(replyStr.contains(",00,"));
    }

    @Test
    void restEndpoint_getSummary() throws Exception {
        Authorization auth = Authorization.builder()
                .cardNum("5222222222222222")
                .authTs(LocalDateTime.now())
                .authType("SALE")
                .authIdCode("XYZ789")
                .authRespCode("00")
                .authRespReason("APRV")
                .transactionAmt(new BigDecimal("200.00"))
                .approvedAmt(new BigDecimal("200.00"))
                .acctId(2002L)
                .custId(3003L)
                .createdAt(LocalDateTime.now())
                .build();
        authorizationRepository.save(auth);

        mockMvc.perform(get("/api/authorizations/summary")
                        .param("cardNum", "5222222222222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNum").value("5222222222222222"))
                .andExpect(jsonPath("$[0].authRespCode").value("00"));
    }

    @Test
    void restEndpoint_getDetail() throws Exception {
        Authorization auth = Authorization.builder()
                .cardNum("5222222222222222")
                .authTs(LocalDateTime.now())
                .authType("SALE")
                .authIdCode("XYZ789")
                .authRespCode("00")
                .transactionAmt(new BigDecimal("200.00"))
                .approvedAmt(new BigDecimal("200.00"))
                .acctId(2002L)
                .createdAt(LocalDateTime.now())
                .build();
        auth = authorizationRepository.save(auth);

        mockMvc.perform(get("/api/authorizations/" + auth.getAuthId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authId").value(auth.getAuthId()));
    }

    @Test
    void restEndpoint_markFraud() throws Exception {
        Authorization auth = Authorization.builder()
                .cardNum("5222222222222222")
                .authTs(LocalDateTime.now())
                .authType("SALE")
                .authIdCode("XYZ789")
                .authRespCode("00")
                .transactionAmt(new BigDecimal("200.00"))
                .approvedAmt(new BigDecimal("200.00"))
                .acctId(2002L)
                .custId(3003L)
                .merchantId("MERCH001")
                .createdAt(LocalDateTime.now())
                .build();
        auth = authorizationRepository.save(auth);

        mockMvc.perform(post("/api/authorizations/" + auth.getAuthId() + "/mark-fraud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authId").value(auth.getAuthId()));

        List<AuthFraud> fraudList = authFraudRepository.findAll();
        assertEquals(1, fraudList.size());
        assertEquals("5222222222222222", fraudList.get(0).getCardNum());
        assertEquals("Y", fraudList.get(0).getAuthFraudFlag());
    }

    @Test
    void restEndpoint_getDetail_notFound() throws Exception {
        mockMvc.perform(get("/api/authorizations/99999"))
                .andExpect(status().isNotFound());
    }
}
