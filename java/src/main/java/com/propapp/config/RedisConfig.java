package com.propapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration; // 🚀 CHANGED: Standalone config
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RedisConfig {

    // 🚀 UPDATED: Checks for the environment variable names injected by task-def-uat.json
    @Value("${SPRING_DATA_REDIS_HOST:127.0.0.1}")
    private String redisHost;

    @Value("${SPRING_DATA_REDIS_PORT:6379}")
    private int redisPort;

    // 🚀 FIX: Connect as a standard standalone instance to talk directly to your sidecar
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(standaloneConfig); // .useSsl() is intentionally dropped here
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. Establish the basic Object Mapper configurations
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 2. Map polymorphic data streams (Array lists of your DTO structures) securely
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(), 
            ObjectMapper.DefaultTyping.EVERYTHING, // Preserved to catch root-level ArrayList date formatting
            com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        // 3. Wrap configurations into the robust Generic serializer 
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 4. Apply serializers natively to your data structure contexts
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }
}
