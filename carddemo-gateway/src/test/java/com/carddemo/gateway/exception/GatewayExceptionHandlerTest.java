package com.carddemo.gateway.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayExceptionHandlerTest {

    private GatewayExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GatewayExceptionHandler();
    }

    @Test
    void responseStatusExceptionUsesCorrectStatus() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        handler.handle(exchange, new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void connectExceptionReturns503() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        handler.handle(exchange, new ConnectException("Connection refused")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void unknownExceptionReturns500() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        handler.handle(exchange, new RuntimeException("unexpected")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void responseStatusExceptionWithNullReasonUsesDefault() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        handler.handle(exchange, new ResponseStatusException(HttpStatus.BAD_REQUEST)).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
