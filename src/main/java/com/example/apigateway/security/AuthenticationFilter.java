package com.example.apigateway.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.SignatureException;

@Component
public class AuthenticationFilter implements GlobalFilter {

    @Autowired
    private JwtValidationUtils jwtValidationUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if(path.contains("/api/auth")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            jwtValidationUtils.validateToken(token);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        System.out.println("`error is: " + message);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
