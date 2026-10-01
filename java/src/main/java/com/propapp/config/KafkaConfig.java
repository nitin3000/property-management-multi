package com.propapp.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import com.propapp.model.Listing;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // Helper method to collect common connection and cloud security settings
    private Map<String, Object> getCommonConfigs() {
        Map<String, Object> props = new HashMap<>();
        
        // Read the variables we set in OpenShift
        String bootstrapServers = System.getenv().getOrDefault("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String saslJaasConfig = System.getenv("SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG");
        String securityProtocol = System.getenv().getOrDefault("SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL", "PLAINTEXT");
        String saslMechanism = System.getenv().getOrDefault("SPRING_KAFKA_PROPERTIES_SASL_MECHANISM", "PLAIN");

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // CRITICAL: Inject secure TLS/SASL configurations for Confluent Cloud handshakes
        if ("SASL_SSL".equals(securityProtocol)) {
            props.put("security.protocol", "SASL_SSL");
            props.put("sasl.mechanism", saslMechanism);
            props.put("sasl.jaas.config", saslJaasConfig);
        }
        return props;
    }

    // 1. Existing KafkaAdmin Bean using secure configs
    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(getCommonConfigs());
    }

    // 2. Define the Producer Factory with matching serializations AND security layers
    @Bean
    public ProducerFactory<String, Listing> producerFactory() {
        Map<String, Object> configProps = getCommonConfigs();
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // 3. Define the KafkaTemplate Bean that PropertyProducerService targets
    @Bean
    public KafkaTemplate<String, Listing> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
