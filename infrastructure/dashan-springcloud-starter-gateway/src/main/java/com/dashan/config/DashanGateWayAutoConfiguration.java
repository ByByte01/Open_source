package com.dashan.config;

import com.fajianchen.sensitive.core.SensitiveDataConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class DashanGateWayAutoConfiguration {
    @Bean
    public  AddBusNumHeaderGatewayFilterFactory addBusNumHeaderGatewayFilterFactory
            (ApplicationContext applicationContext)
    {
        return  new AddBusNumHeaderGatewayFilterFactory();
    }
    @Bean
    public  ModifedBodyGatewayFilterFactory modifedBodyGatewayFilterFactory(ApplicationContext applicationContext,
                                                                            SensitiveDataConverter sensitiveDataConverter)
    {
        return  new ModifedBodyGatewayFilterFactory(applicationContext,sensitiveDataConverter);
    }

    @Bean
    @ConditionalOnClass({RedisTemplate.class})
    public RedisTemplate<String, Object> springSessionRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jdkSerializationRedisSerializer);
        template.setHashValueSerializer(jdkSerializationRedisSerializer);
        template.afterPropertiesSet();
        template.setEnableTransactionSupport(false);
        return template;
    }
}
