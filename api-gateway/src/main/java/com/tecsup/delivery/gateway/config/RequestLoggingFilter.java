package com.tecsup.delivery.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long inicio = System.currentTimeMillis();
        String metodo = exchange.getRequest().getMethod().name();
        String ruta = exchange.getRequest().getURI().getPath();
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duracion = System.currentTimeMillis() - inicio;
            HttpStatusCode estado = exchange.getResponse().getStatusCode();
            log.info("{} {} -> {} ({} ms)", metodo, ruta, estado == null ? "SIN_ESTADO" : estado.value(), duracion);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
