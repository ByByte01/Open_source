package com.dashan.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RequestRateLimiterConfig {

    /**
     * 针对ip限流
     * @return
     */
    @Primary
    @Bean(value = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress().getHostAddress());
    }

    /**
     * 针对路径限流
     * @return
     */
     @Bean(name = "apiUrlResolver")
    public KeyResolver apiUrlResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

    /**
     * 根据参数限流
     * @return
     */
    @Bean(name = "apiQueryParamsResolver")
    public KeyResolver apiQueryParamsResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getQueryParams().getFirst("paramKey"))
        );
    }

}
