package com.carddemo.gateway.config;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class OpenApiConfig {

    @Bean
    public RouterFunction<ServerResponse> apiDocsRoutes(RouteDefinitionLocator locator) {
        return route(GET("/api-docs"), request ->
                locator.getRouteDefinitions().collectList().flatMap(routes -> {
                    Map<String, Object> docs = new LinkedHashMap<>();
                    docs.put("gateway", "carddemo-gateway");
                    docs.put("description", "Aggregate API documentation for CardDemo services");

                    Map<String, String> services = new LinkedHashMap<>();
                    for (RouteDefinition rd : routes) {
                        services.put(rd.getId(), rd.getUri().toString() + "/api-docs");
                    }
                    docs.put("services", services);
                    return ServerResponse.ok().bodyValue(docs);
                }));
    }
}
