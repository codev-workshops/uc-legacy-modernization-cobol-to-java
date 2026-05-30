package com.carddemo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(5);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void requestsWithinLimitSucceed() {
        for (int i = 0; i < 5; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                    .header("X-User-Id", "user01")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            filter.filter(exchange, chain).block();
            assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Test
    void requestsOverLimitReturn429() {
        for (int i = 0; i < 5; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                    .header("X-User-Id", "user02")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            filter.filter(exchange, chain).block();
        }

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header("X-User-Id", "user02")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        filter.filter(exchange, chain).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void differentUsersHaveSeparateBuckets() {
        for (int i = 0; i < 5; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                    .header("X-User-Id", "userA")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            filter.filter(exchange, chain).block();
        }

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header("X-User-Id", "userB")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        filter.filter(exchange, chain).block();
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void resolveClientKeyWithUserIdHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", "user01")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(filter.resolveClientKey(exchange)).isEqualTo("user01");
    }

    @Test
    void resolveClientKeyWithoutUserIdFallsBackToIp() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        String key = filter.resolveClientKey(exchange);
        assertThat(key).isNotNull();
    }

    @Test
    void filterOrderIsZero() {
        assertThat(filter.getOrder()).isEqualTo(0);
    }
}
