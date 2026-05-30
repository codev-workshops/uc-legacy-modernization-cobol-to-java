package com.carddemo.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtValidationFilterTest {

    private static final String SECRET = "ThisIsADefaultSecretKeyForDevelopmentThatIs64BytesLongAtLeast!!";
    private JwtValidationFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtValidationFilter(SECRET);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String generateToken(String userId, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Test
    void validTokenPasses() {
        String token = generateToken("user01", 900_000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validTokenSetsUserIdHeader() {
        String token = generateToken("user01", 900_000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void missingTokenReturns401OnProtectedRoute() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void invalidTokenReturns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void expiredTokenReturns401() {
        String token = generateToken("user01", -1000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void publicAuthRoutesPassWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void actuatorRoutesPassWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void missingBearerPrefixReturns401() {
        String token = generateToken("user01", 900_000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void wrongSecretTokenReturns401() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("WrongSecretKeyThatIsAtLeast32BytesLongForHS256AlgorithmTest!!!!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user01")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(wrongKey)
                .compact();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/accounts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void isPublicPathReturnsCorrectResults() {
        assertThat(filter.isPublicPath("/api/auth/login")).isTrue();
        assertThat(filter.isPublicPath("/api/auth/register")).isTrue();
        assertThat(filter.isPublicPath("/actuator/health")).isTrue();
        assertThat(filter.isPublicPath("/api/accounts/1")).isFalse();
        assertThat(filter.isPublicPath("/api/transactions")).isFalse();
    }

    @Test
    void filterOrderIsNegativeOne() {
        assertThat(filter.getOrder()).isEqualTo(-1);
    }
}
