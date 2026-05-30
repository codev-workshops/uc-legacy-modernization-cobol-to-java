package com.carddemo.gateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayRoutingIntegrationTest {

    private static final String JWT_SECRET = "ThisIsADefaultSecretKeyForDevelopmentThatIs64BytesLongAtLeast!!";

    private static WireMockServer authService;
    private static WireMockServer accountService;
    private static WireMockServer transactionService;
    private static WireMockServer authorizationService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void startWireMocks() {
        authService = new WireMockServer(wireMockConfig().dynamicPort());
        accountService = new WireMockServer(wireMockConfig().dynamicPort());
        transactionService = new WireMockServer(wireMockConfig().dynamicPort());
        authorizationService = new WireMockServer(wireMockConfig().dynamicPort());

        authService.start();
        accountService.start();
        transactionService.start();
        authorizationService.start();

        setupAuthServiceStubs();
        setupAccountServiceStubs();
        setupTransactionServiceStubs();
        setupAuthorizationServiceStubs();
    }

    @AfterAll
    static void stopWireMocks() {
        authService.stop();
        accountService.stop();
        transactionService.stop();
        authorizationService.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gateway.routes[0].id", () -> "auth-service");
        registry.add("spring.cloud.gateway.routes[0].uri", () -> "http://localhost:" + authService.port());
        registry.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/api/auth/**,/api/users/**");

        registry.add("spring.cloud.gateway.routes[1].id", () -> "account-service");
        registry.add("spring.cloud.gateway.routes[1].uri", () -> "http://localhost:" + accountService.port());
        registry.add("spring.cloud.gateway.routes[1].predicates[0]", () -> "Path=/api/accounts/**,/api/cards/**,/api/card-xref/**,/api/customers/**,/api/batch/**");

        registry.add("spring.cloud.gateway.routes[2].id", () -> "transaction-service");
        registry.add("spring.cloud.gateway.routes[2].uri", () -> "http://localhost:" + transactionService.port());
        registry.add("spring.cloud.gateway.routes[2].predicates[0]", () -> "Path=/api/transactions/**,/api/reports/**,/api/transaction-types/**,/api/transaction-categories/**");

        registry.add("spring.cloud.gateway.routes[3].id", () -> "authorization-service");
        registry.add("spring.cloud.gateway.routes[3].uri", () -> "http://localhost:" + authorizationService.port());
        registry.add("spring.cloud.gateway.routes[3].predicates[0]", () -> "Path=/api/authorizations/**");

        registry.add("jwt.secret", () -> JWT_SECRET);
        registry.add("gateway.cors.allowed-origins", () -> "http://localhost:3000");
        registry.add("gateway.rate-limit.requests-per-minute", () -> "100");
    }

    private static void setupAuthServiceStubs() {
        authService.stubFor(post(urlEqualTo("/api/auth/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"accessToken\":\"mock-token\",\"refreshToken\":\"mock-refresh\"}")));
        authService.stubFor(get(urlEqualTo("/api/users/user01"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"userId\":\"user01\",\"firstName\":\"Test\"}")));
    }

    private static void setupAccountServiceStubs() {
        accountService.stubFor(get(urlEqualTo("/api/accounts/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"accountId\":1,\"accountNumber\":\"00000000001\"}")));
        accountService.stubFor(get(urlEqualTo("/api/cards/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"cardId\":1,\"cardNumber\":\"4111111111111111\"}")));
        accountService.stubFor(get(urlEqualTo("/api/customers/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"customerId\":1,\"firstName\":\"John\"}")));
    }

    private static void setupTransactionServiceStubs() {
        transactionService.stubFor(get(urlEqualTo("/api/transactions/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"transactionId\":1,\"amount\":100.00}")));
        transactionService.stubFor(get(urlEqualTo("/api/reports/daily"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"report\":\"daily-report\"}")));
        transactionService.stubFor(get(urlEqualTo("/api/transaction-types"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"typeCode\":\"SA\",\"description\":\"Sale\"}]")));
    }

    private static void setupAuthorizationServiceStubs() {
        authorizationService.stubFor(get(urlEqualTo("/api/authorizations/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"authorizationId\":1,\"status\":\"APPROVED\"}")));
    }

    private String generateValidToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(key)
                .compact();
    }

    @Test
    void authLoginRouteForwardsToAuthService() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userId\":\"admin01\",\"password\":\"password\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.accessToken").isEqualTo("mock-token");
    }

    @Test
    void accountRouteForwardsToAccountService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.accountId").isEqualTo(1);
    }

    @Test
    void cardRouteForwardsToAccountService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/cards/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.cardId").isEqualTo(1);
    }

    @Test
    void customerRouteForwardsToAccountService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/customers/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.customerId").isEqualTo(1);
    }

    @Test
    void transactionRouteForwardsToTransactionService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/transactions/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.transactionId").isEqualTo(1);
    }

    @Test
    void reportRouteForwardsToTransactionService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/reports/daily")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.report").isEqualTo("daily-report");
    }

    @Test
    void transactionTypeRouteForwardsToTransactionService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/transaction-types")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void authorizationRouteForwardsToAuthorizationService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/authorizations/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.authorizationId").isEqualTo(1);
    }

    @Test
    void protectedRouteWithoutTokenReturns401() {
        webTestClient.get()
                .uri("/api/accounts/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRouteWithInvalidTokenReturns401() {
        webTestClient.get()
                .uri("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authFlowLoginThenAccessProtectedRoute() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userId\":\"admin01\",\"password\":\"password\"}")
                .exchange()
                .expectStatus().isOk();

        String token = generateValidToken("admin01");
        webTestClient.get()
                .uri("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.accountId").isEqualTo(1);
    }

    @Test
    void userRouteForwardsToAuthService() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/users/user01")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.userId").isEqualTo("user01");
    }

    @Test
    void invalidRouteReturns404() {
        String token = generateValidToken("user01");
        webTestClient.get()
                .uri("/api/nonexistent/route")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void gatewayForwardsXUserIdHeaderToDownstream() {
        String token = generateValidToken("testuser01");
        webTestClient.get()
                .uri("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        accountService.verify(com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
                urlEqualTo("/api/accounts/1"))
                .withHeader("X-User-Id", com.github.tomakehurst.wiremock.client.WireMock.equalTo("testuser01")));
    }

    @Test
    void expiredTokenReturns401() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user01")
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();
        webTestClient.get()
                .uri("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
