package com.soi.springcloudgateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class JwtVerificationFilter extends AbstractGatewayFilterFactory<JwtVerificationFilter.AuthConfig> {
    private final WebClient webClient = WebClient.create();

    @Value("${auth-verify-uri}")
    private String verifyUrl;

    @Override
    public GatewayFilter apply(AuthConfig config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return forbiddenResponse(exchange);
            }
            return webClient.method(HttpMethod.GET)
                    .uri(verifyUrl)
                    .header(HttpHeaders.AUTHORIZATION, authHeader) // 실제 Authorization 헤더 사용
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Invalid token")))
                    .bodyToMono(Boolean.class)
                    .flatMap(isValid -> {
                        if (isValid) {
                            return chain.filter(exchange); // 검증 성공 시 필터 체인 계속 진행
                        } else {
                            return forbiddenResponse(exchange); // 검증 실패 시 403 반환
                        }
                    })
                    .onErrorResume(error -> forbiddenResponse(exchange)); // 에러 발생 시 403 반환
        };
    }

    private Mono<Void> forbiddenResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Getter
    @NoArgsConstructor  // 기본 생성자
    @AllArgsConstructor
    static class AuthConfig {
        private final String verifyUrl = "http://localhost:9091/auth/verify"; // 기본 검증 URL
        private final HttpHeaders filterHeader = new HttpHeaders(); // 기본 헤더
        private final HttpStatus failStatus = HttpStatus.FORBIDDEN; // 기본 실패 상태
        private final String failMessage = "Invalid token"; // 기본 실패 메시지
    }
}
