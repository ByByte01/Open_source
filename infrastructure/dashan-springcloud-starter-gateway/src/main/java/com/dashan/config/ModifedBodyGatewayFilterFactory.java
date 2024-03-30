package com.dashan.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fajianchen.sensitive.core.SensitiveDataConverter;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 脱敏过滤器
 */
public class ModifedBodyGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ModifedBodyGatewayFilterFactory.Config> {
    private final Logger log = LoggerFactory.getLogger(ModifedBodyGatewayFilterFactory.class);
    private ApplicationContext applicationContext;
    private SensitiveDataConverter sensitiveDataConverter;
    private final List<HttpMessageReader<?>> httpMessageReaders;
    public ModifedBodyGatewayFilterFactory(ApplicationContext applicationContext,SensitiveDataConverter sensitiveDataConverter) {
        super(ModifedBodyGatewayFilterFactory.Config.class);
        this.applicationContext = applicationContext;
        this.sensitiveDataConverter=sensitiveDataConverter;
        this.httpMessageReaders = HandlerStrategies.withDefaults().messageReaders();
    }
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("enable");
    }

    @Override
    public GatewayFilter apply(Config config) {
        ModifedBodyGateWayFilter sensitiveGateWayFilter = new ModifedBodyGateWayFilter();
        return sensitiveGateWayFilter;
    }

    public static class Config {
        private boolean enable; //是否开启数据脱敏过滤器
    }

    public class ModifedBodyGateWayFilter implements GatewayFilter, Ordered {
        public ModifedBodyGateWayFilter() {
        }
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerRequest serverRequest = ServerRequest.create(exchange, ModifedBodyGatewayFilterFactory.this.httpMessageReaders);
            Mono<DataBuffer> modifiedBody = serverRequest.bodyToMono(DataBuffer.class)
                    .flatMap((o) -> {
                        return this.addContext(exchange, o);
                    });
            BodyInserter<Mono<DataBuffer>, ReactiveHttpOutputMessage> bodyInserter
                    = BodyInserters.fromPublisher(modifiedBody, DataBuffer.class);
            HttpHeaders httpHeaders = new HttpHeaders();
            CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, httpHeaders);
            return bodyInserter.insert(outputMessage, new BodyInserterContext()).then
                    (Mono .defer(() -> {
                                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                                    @Override
                                    public HttpHeaders getHeaders() {
                                        httpHeaders.putAll(super.getHeaders());
                                        String flag = exchange.getRequest().getHeaders().getFirst("Flag");
                                        httpHeaders.set("sear", "333");
                                        return httpHeaders;
                                    }
                                    @Override
                                    public Flux<DataBuffer> getBody() {
                                        return outputMessage.getBody();
                                    }
                                };
                                ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
                                    DataBufferFactory bufferFactory = exchange.getResponse()
                                            .bufferFactory();
                                    ServerHttpResponse response = exchange.getResponse();
                                    @Override
                                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                        if (body instanceof Flux) {
                                            return super.writeWith(Flux.from(body).buffer().map((dataBuffers) ->
                                            {
                                                DataBufferFactory dataBufferFactory =
                                                        new DefaultDataBufferFactory();
                                                byte[] content = new byte[0];
                                                DataBuffer join = null;
                                                DataBuffer buffer;
                                                try {
                                                    join = dataBufferFactory.join(dataBuffers);
                                                    content = new byte[join.readableByteCount()];
                                                    join.read(content);
                                                    DataBufferUtils.release(join);
                                                        if (JSON.isValidObject(content)) {
                                                            JSONObject jsonObject = JSON.parseObject(content);
                                                            String str1=  sensitiveDataConverter.execute(
                                                                    new StringBuilder()
                                                                            .append(jsonObject.toString())).toString();
                                                            return this.bufferFactory.wrap(str1
                                                                    .getBytes(StandardCharsets.UTF_8));
                                                        }


                                                    buffer = this.bufferFactory.wrap(content);
                                                } catch (Exception exception) {
                                                    return bufferFactory.wrap(content);
                                                } finally {
                                                    if (null == join) {
                                                        DataBufferUtils.release(join);
                                                    }
                                                }
                                                return buffer;
                                            }));

                                        } else {
                                            return super.writeWith(body);
                                        }
                                    }

                                    @Override
                                    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                                        return this.writeWith(Flux.from(body).flatMapSequential((p ->
                                        {
                                            return p;
                                        })));

                                    }
                                };
                                return chain.filter(exchange.mutate().request(decorator).response(responseDecorator).build());

                            }));


        }


        /**
         * 要比NettyWriteResponseFilter更优先执行,如果已经被NettyWriteResponseFilter 输出了,则无法读取修改response
         *
         * @return
         */
        @Override
        public int getOrder() {
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 2;
        }

        private Mono<DataBuffer> addContext(ServerWebExchange exchange, DataBuffer buffer) {
            DataBufferFactory bufferFactory =
                    exchange.getResponse().bufferFactory();
            byte[] content = new byte[0];
            Mono mono = null;
            try {
                bufferFactory = exchange.getResponse().bufferFactory();
                content = new byte[buffer.readableByteCount()];
                buffer.read(content);
                String str = new String(content, StandardCharsets.UTF_8);
                log.info("【网关获取的请求头】" + exchange.getRequest().getHeaders().toString());

                if (ObjectUtils.isEmpty(str)) {
                    log.info("请求体为空");
                }
                log.info("【网关获取的请求体】" + str);
                exchange.getAttributes().put("sky", "333333");
                mono = Mono.just(bufferFactory.wrap(content));
            } catch (Exception exception) {
                exception.printStackTrace();
                return Mono.just(bufferFactory.wrap(content));
            }
            return mono;
        }

    }

}
