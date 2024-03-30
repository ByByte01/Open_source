package com.dashan.config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class ContextPathConfig {
    @Bean
    public GlobalFilter globalFilter() {
        return (exchange, chain) -> {
            System.out.println("First Global filter");
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("Second Global filter");
            }));
        };
    }
    @Bean
    @ConditionalOnProperty("server.servlet.context-path")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter contextPathWebFilter(ServerProperties serverProperties){
        String contextPath = serverProperties.getServlet().getContextPath();
        return (serverWebExchange, webFilterChain) ->{
            ServerHttpRequest request = serverWebExchange.getRequest();
            String requestPath = request.getURI().getPath();

            if(requestPath.contains(contextPath)){
                String newPath = requestPath.replaceFirst(contextPath, "");
                ServerHttpRequest newRequest = request.mutate()
                        .path(newPath).build();
                return webFilterChain.filter(serverWebExchange.mutate()
                        .request(newRequest)
                        .build()
                );
            }else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        };
    }
}
