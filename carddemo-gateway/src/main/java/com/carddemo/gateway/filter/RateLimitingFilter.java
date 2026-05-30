package com.carddemo.gateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimitingFilter(@Value("${gateway.rate-limit.requests-per-minute:100}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientKey = resolveClientKey(exchange);
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> createBucket());

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for client: {}", clientKey);
        return tooManyRequests(exchange);
    }

    String resolveClientKey(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return userId;
        }
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "anonymous";
    }

    Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}".getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
