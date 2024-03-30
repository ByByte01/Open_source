package com.dashan.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.codec.ByteBufferDecoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.FormHttpMessageReader;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.http.codec.multipart.SynchronossPartHttpMessageReader;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>
 * Body Utils
 * </p>
 * body转换
 *
 * @author Gsealy
 */

@Slf4j
public class BodyUtils {

    public static final String LOCAL_CACHED_REQUEST_BODY =
            ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
    public static final String LOCAL_CACHED_SERVER_HTTP_REQUEST =
            ServerWebExchangeUtils.CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR;

    private static BodyExtractor.Context context;

    static {
        createContext();
    }

    private BodyUtils() {
        throw new UnsupportedOperationException("do not initialization Utils class");
    }

    /**
     * 获取body内的String, 针对其他类型content-type
     *
     * @param body
     * @return String
     */
    public static String toRaw(DataBuffer body) {
        byte[] bytes = new byte[body.readableByteCount()];
        body.read(bytes);
        DataBufferUtils.release(body);
        // return uriDecode(Strings.fromUTF8ByteArray(bytes));
        return uriDecode(new String(bytes, StandardCharsets.UTF_8));
    }

    /**
     * body 转为 FormData, 针对content-type为application/x-www-form-urlencoded
     *
     * @param httpRequest
     * @param body
     * @return Map
     */
    public static Map<String, String> toFormDataMap(ServerHttpRequest httpRequest,
            DataBuffer body) {
        ServerHttpRequest request = buildNewRequest(httpRequest, body);
        Mono<MultiValueMap<String, String>> formdata =
                BodyExtractors.toFormData().extract(request, context);
        return unPackMonoFormData(formdata);
    }

    /**
     * unpack {@code Mono<MultiValueMap<String, String>> } to {@code Map<String, String>}
     *
     * @param formdata Mono<MultiValueMap<String, String>>
     * @return Map<String , String>
     */
    public static Map<String, String> unPackMonoFormData(
            Mono<MultiValueMap<String, String>> formdata) {
        AtomicReference<Map<String, String>> formRef = new AtomicReference<>();
        formdata.subscribe(multiValueMap -> {
            Map<String, String> map = multiValueMap.toSingleValueMap();
            final Map<String, String> encodeMap = map.entrySet().stream().collect(Collectors.toMap(
                    entry -> uriDecode(entry.getKey()), entry -> uriDecode(entry.getValue())));
            formRef.set(encodeMap);
        });
        return formRef.get();
    }

    /**
     * 使用入参body, 创建新的请求
     *
     * @param httpRequest
     * @param body
     * @return
     */
    private static ServerHttpRequest buildNewRequest(ServerHttpRequest httpRequest,
            DataBuffer body) {
        return new ServerHttpRequestDecorator(httpRequest) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(body);
            }
        };
    }

    private static String uriDecode(String encodeValue) {
        if (StringUtils.isEmpty(encodeValue)) {
            return "";
        }
        return UriUtils.decode(encodeValue, StandardCharsets.UTF_8);
    }

    /**
     * body 转为 byte[], 针对content-type为multipart/form-data
     *
     * @param body
     * @return byte array
     */
    public static byte[] toByteArray(DataBuffer body) {
        byte[] bytes = new byte[body.readableByteCount()];
        body.read(bytes);
        DataBufferUtils.release(body);
        return bytes;
    }

    /**
     * 缓存请求体, 封装为{@link ServerHttpRequestDecorator}缓存到{@link ServerWebExchange}的属性中 适用于请求体的修改和过滤。
     * 因为Reactive Streams规范使用的是发布-订阅设计模式, 订阅操作最好由框架去完成, 否则就会报错. 同时后面的Filter及服务器无法获取请求.
     * 
     * @param exchange 此次请求和相应的封装
     * @param function 接收需要缓存操作的请求
     * @param <T> 请求体的泛型
     * @return Mono封装的请求泛型
     */
    public static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange,
            Function<ServerHttpRequest, Mono<T>> function) {
        return cacheRequestBody(exchange, false, function);
    }

    /**
     * 和{@link BodyUtils#cacheRequestBody(ServerWebExchange, Function)}相同, 多缓存一个完整请求体
     */
    public static <T> Mono<T> cacheRequestBodyAndRequest(ServerWebExchange exchange,
            Function<ServerHttpRequest, Mono<T>> function) {
        return cacheRequestBody(exchange, true, function);
    }

    private static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange,
            boolean cacheDecoratedRequest, Function<ServerHttpRequest, Mono<T>> function) {
        return DataBufferUtils.join(exchange.getRequest().getBody()).map(dataBuffer -> {
            if (dataBuffer.readableByteCount() > 0) {
                if (log.isTraceEnabled()) {
                    log.trace("retaining body in exchange attribute");
                }
                exchange.getAttributes().put(LOCAL_CACHED_REQUEST_BODY, dataBuffer);
            }

            ServerHttpRequest decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return Mono.<DataBuffer>fromSupplier(() -> {
                        if (exchange.getAttributeOrDefault(LOCAL_CACHED_REQUEST_BODY,
                                null) == null) {
                            // 响应已经关闭(超时)
                            return null;
                        }
                        NettyDataBuffer pdb = (NettyDataBuffer) dataBuffer;
                        return pdb.factory().wrap(pdb.getNativeBuffer().retainedSlice());
                    }).flux();
                }
            };
            if (cacheDecoratedRequest) {
                exchange.getAttributes().put(LOCAL_CACHED_SERVER_HTTP_REQUEST, decorator);
            }
            return decorator;
        }).switchIfEmpty(Mono.just(exchange.getRequest())).flatMap(function);
    }

    static void createContext() {
        final List<HttpMessageReader<?>> messageReaders = new ArrayList<>(8);
        messageReaders.add(new DecoderHttpMessageReader<>(new ByteBufferDecoder()));
        messageReaders.add(new DecoderHttpMessageReader<>(StringDecoder.allMimeTypes()));
        messageReaders.add(new DecoderHttpMessageReader<>(new Jaxb2XmlDecoder()));
        messageReaders.add(new DecoderHttpMessageReader<>(new Jackson2JsonDecoder()));
        messageReaders.add(new FormHttpMessageReader());
        SynchronossPartHttpMessageReader partReader = new SynchronossPartHttpMessageReader();
        messageReaders.add(partReader);
        messageReaders.add(new MultipartHttpMessageReader(partReader));

        messageReaders.add(new FormHttpMessageReader());

        context = new BodyExtractor.Context() {
            @Override
            public List<HttpMessageReader<?>> messageReaders() {
                return messageReaders;
            }

            @Override
            public Optional<ServerHttpResponse> serverResponse() {
                return Optional.empty();
            }

            @Override
            public Map<String, Object> hints() {
                return new HashMap<String, Object>();
            }
        };
    }
}
