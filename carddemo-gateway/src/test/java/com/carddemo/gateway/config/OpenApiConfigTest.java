package com.carddemo.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class OpenApiConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void apiDocsEndpointReturnsServiceInfo() {
        webTestClient.get()
                .uri("/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.gateway").isEqualTo("carddemo-gateway")
                .jsonPath("$.services").exists();
    }
}
