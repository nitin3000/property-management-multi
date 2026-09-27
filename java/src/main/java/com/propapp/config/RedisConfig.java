package com.propapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import java.util.Collections;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    // 🚀 FIX 1: Explicitly configure the connection factory for ElastiCache Serverless
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Enforce Cluster Configuration for Serverless Redis topology
        String clusterNode = String.format("%s:%d", redisHost, redisPort);
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(Collections.singleton(clusterNode));
        
        // Enforce mandatory TLS/SSL encryption handshake
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl()
                .build();
                
        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    // 🚀 FIX 2: Explicitly pass the custom connection factory into the template configuration
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
            ObjectMapper.DefaultTyping.EVERYTHING, // Updated to EVERYTHING to catch root-level ArrayLists
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
