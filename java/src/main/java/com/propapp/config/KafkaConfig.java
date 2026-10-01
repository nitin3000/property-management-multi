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

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import java.util.Properties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {


    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:property-service-group-uat}")
    private String groupId;

    @Value("${spring.kafka.consumer.max-poll-records:10}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.properties.max.partition.fetch.bytes:262144}")
    private int maxPartitionFetchBytes;

    @Value("${spring.kafka.consumer.properties.fetch.max.bytes:1048576}")
    private int fetchMaxBytes;

    // Optional: Include these if you are connecting securely to Confluent Cloud via JAAS
    @Value("${spring.kafka.properties.security.protocol:SASL_SSL}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:PLAIN}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        
        // 1. Manually build the base connection maps
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        
        // Add security configs if they are filled in application.properties for Confluent
        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            configProps.put("security.protocol", securityProtocol);
            configProps.put("sasl.mechanism", saslMechanism);
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }

        // 2. Create the consumer factory layout
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(configProps);
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        
        // 3. Inject our strict, low-memory safety throttles explicitly onto the container properties
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        kafkaProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        kafkaProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetchMaxBytes);
        
        factory.getContainerProperties().setKafkaConsumerProperties(kafkaProps);
        
        return factory;
    }
    
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


        // 2. FIXED: Consumer Factory now calls getCommonConfigs() to inject Confluent Cloud security layers!
    @Bean
    public ConsumerFactory<String, Listing> consumerFactory() {
        Map<String, Object> configProps = getCommonConfigs(); // <--- CRITICAL SECURITY LAYER
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Match a generic consumer group if not specified in your OpenShift environment
        String groupId = System.getenv().getOrDefault("SPRING_KAFKA_CONSUMER_GROUP_ID", "property-service-group");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        JsonDeserializer<Listing> jsonDeserializer = new JsonDeserializer<>(Listing.class, false);
        jsonDeserializer.addTrustedPackages("com.propapp.model", "com.propapp.dto");
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), jsonDeserializer);
    }
}
