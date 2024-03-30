package com.dashan.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class AddBusNumHeaderGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        /*return ((e,c)->
        {
            return null;
        });*/
        return ((e,c)->
        {
           // AbstractNameValueGatewayFilterFactory
           String numberStr="my01";// String.valueOf( e.getAttribute("number"));
           ServerHttpRequest request= e.getRequest().mutate().
                   header("number",numberStr).build();
           return  c.filter(e.mutate().request(request)
                   .build()).doFinally(f->{
                       e.getResponse().getHeaders().add("num","my01"
                               //request.getHeaders().getFirst("num2")
                       );
           });

        });

    }
}
