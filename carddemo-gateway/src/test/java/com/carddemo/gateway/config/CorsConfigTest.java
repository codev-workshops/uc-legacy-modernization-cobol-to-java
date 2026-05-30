package com.carddemo.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CorsConfigTest {

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void corsFilterBeanExists() {
        assertThat(corsWebFilter).isNotNull();
    }

    @Test
    void corsConfigurationSourceBeanExists() {
        assertThat(corsConfigurationSource).isNotNull();
    }

    @Test
    void corsAllowsConfiguredOrigin() {
        org.springframework.mock.http.server.reactive.MockServerHttpRequest request =
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .options("http://localhost:8080/api/accounts/1")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                        .build();
        org.springframework.mock.web.server.MockServerWebExchange exchange =
                org.springframework.mock.web.server.MockServerWebExchange.from(request);

        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(exchange);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).contains("http://localhost:3000");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).contains(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE);
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void corsConfigurationAppliesToAllPaths() {
        org.springframework.mock.http.server.reactive.MockServerHttpRequest request =
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("http://localhost:8080/any/path")
                        .build();
        org.springframework.mock.web.server.MockServerWebExchange exchange =
                org.springframework.mock.web.server.MockServerWebExchange.from(request);

        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(exchange);
        assertThat(config).isNotNull();
    }
}
