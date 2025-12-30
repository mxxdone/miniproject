package com.mxxdone.miniproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    /**
     * 스프링의 캐시 추상화가 사용할 CacheManager 빈을 정의
     * '@Cacheable' 의 동작 방식을 Redis에 맞게 커스터마이징
     * @param cf 스프링 부트가 자동 설정한 Redis 연결 팩토리
     * @return 커스텀 RedisCacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        // PolymorphicTypeValidator: 역직렬화 시 안전하지 않은 클래스가 생성되는 것을 막는 보안 설정
        // allowIfBaseType(Object.class)는 기본적으로 모든 타입을 허용하되, 보안 위협이 될 수 있는 일부 클래스는 막아줍니다.
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Object.class)
                .build();

        ObjectMapper objectMapper = new ObjectMapper(); //자바 객체와 JSON 간의 변환을 담당
        objectMapper.registerModule(new JavaTimeModule()); // Java 8 시간 API를 표준 형식으로 직렬화/역직렬화
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        // 커스텀 ObjectMapper를 사용하는 새로운 JSON 직렬화기 생성
        GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Redis 캐시 동작 규칙 정의
        // 직렬화기를 사용하여 Redis 캐시 설정 생성
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .entryTtl(Duration.ofMinutes(3L));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(cf)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}