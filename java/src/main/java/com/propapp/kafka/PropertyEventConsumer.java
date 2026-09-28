package com.propapp.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class PropertyEventConsumer {

    private final JdbcTemplate jdbcTemplate;

    public PropertyEventConsumer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(
        topics = "property-events-prod", 
        groupId = "property-service-group-uat", // Target your UAT group
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        // The propertyId passed as the message key serves as our unique idempotency key
        String messageId = record.key(); 

        try {
            // 1. Atomically attempt to insert into the deduplication ledger table
            String deduplicateSql = "INSERT INTO processed_messages (message_id) VALUES (?) ON CONFLICT (message_id) DO NOTHING";
            int rowsAffected = jdbcTemplate.update(deduplicateSql, messageId);

            // 2. If no row was inserted, this message has already been processed previously
            if (rowsAffected == 0) {
                ack.acknowledge(); // Commit offset immediately to skip duplicate processing overhead
                return;
            }

            // 3. --- CORE BUSINESS PROCESSING OVERHEAD PLACEHOLDER ---
            // Inside this same database transaction, perform your core logic mutations.
            // e.g., jdbcTemplate.update("INSERT INTO listings... or update query calculations");

            // 4. Commit Kafka offset manually only AFTER database operations fully succeed
            ack.acknowledge();

        } catch (Exception e) {
            // Database transaction automatically rolls back.
            // Exception is thrown to the container; Kafka message is not acknowledged and will safely retry.
            throw new RuntimeException("Database persistence or constraint violation, rolling back transaction", e);
        }
    }
}
