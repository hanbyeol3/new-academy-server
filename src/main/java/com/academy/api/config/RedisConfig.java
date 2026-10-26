package com.academy.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Local 환경용 Redis Connection Factory.
     */
    @Bean
    @Profile("local")
    public RedisConnectionFactory localRedisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }
    
    /**
     * 운영 환경용 Redis Connection Factory.
     */
    @Bean
    @Profile("!local")
    public RedisConnectionFactory prodRedisConnectionFactory() {
        // 운영 환경에서는 환경변수나 설정 파일에서 읽어옴
        String redisHost = System.getenv("REDIS_HOST") != null ? 
                          System.getenv("REDIS_HOST") : "localhost";
        int redisPort = System.getenv("REDIS_PORT") != null ? 
                       Integer.parseInt(System.getenv("REDIS_PORT")) : 6379;
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * RedisTemplate 설정.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key는 String 타입
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value는 JSON 형태로 직렬화
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}