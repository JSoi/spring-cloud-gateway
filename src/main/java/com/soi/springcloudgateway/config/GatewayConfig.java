package com.soi.springcloudgateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {
    private final JwtVerificationFilter jwtVerificationFilter;
    @Bean
    public RouteLocator productRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("product",
                        r -> r.path("/product/**")
                                .uri("http://localhost:8080"))
                .build();
    }

    @Bean
    public RouteLocator reviewRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("review",
                        r -> r.path("/review/**")
//                                .filters(f -> f.filter(jwtVerificationFilter.apply(new JwtVerificationFilter.AuthConfig())))
                                .uri("http://localhost:9092"))
                .build();
    }

    @Bean
    public RouteLocator authRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth",
                        r -> r.path("/auth/**")
                                .uri("http://localhost:9091"))
                .build();
    }
}
