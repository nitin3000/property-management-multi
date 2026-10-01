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

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    // Inject your environment properties with safe, low fallback values
    @Value("${spring.kafka.consumer.max-poll-records:10}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.properties.max.partition.fetch.bytes:262144}")
    private int maxPartitionFetchBytes;

    @Value("${spring.kafka.consumer.properties.fetch.max.bytes:1048576}")
    private int fetchMaxBytes;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
            
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        // 1. Attach the core consumer factory
        factory.setConsumerFactory(consumerFactory);
        
        // 2. FORCE the container factory to honor the throttled properties
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetchMaxBytes);
        
        // Override the consumer factory's defaults
        factory.getContainerProperties().setKafkaConsumerProperties(props);

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
