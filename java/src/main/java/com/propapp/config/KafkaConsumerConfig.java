package com.propapp.config;

import com.propapp.model.Listing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Listing> kafkaListenerContainerFactory(
            ConsumerFactory<String, Listing> consumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, Listing> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory);
        
        // 1. CRITICAL FIX: Force the factory to use batch parsing
        factory.setBatchListener(true); 
        
        // 2. CRITICAL FIX: Tell the factory to convert batches using a JSON converter
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(new StringJsonMessageConverter()));
        
        // 3. Align with your properties file manual acknowledgement requirement
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }
}
