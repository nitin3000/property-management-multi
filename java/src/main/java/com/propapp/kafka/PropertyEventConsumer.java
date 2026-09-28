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
    	    groupId = "property-service-group-uat",
    	    containerFactory = "kafkaListenerContainerFactory"
    	)
    	@Transactional
    	public void consumeEvent(ConsumerRecord<String, Listing> record, Acknowledgment ack) {
    	    String messageId = record.key(); // This contains your unique, generated UUID transaction ID
    	    Listing listing = record.value();

    	    try {
    	        // 1. Enforce deduplication ledger table tracking
    	        String deduplicateSql = "INSERT INTO processed_messages (message_id) VALUES (?) ON CONFLICT (message_id) DO NOTHING";
    	        int rowsAffected = jdbcTemplate.update(deduplicateSql, messageId);

    	        if (rowsAffected == 0) {
    	            ack.acknowledge(); 
    	            return;
    	        }

    	        // 2. CRITICAL ARCHITECTURAL FIX: Copy the non-null Kafka message key straight into your entity's primary key
    	        if (listing.getId() == null || listing.getId().trim().isEmpty()) {
    	            listing.setId(messageId);
    	        }

    	        // 3. Save the listing now that the ID column is guaranteed to be populated
    	        listingRepository.save(listing);

    	        // 4. Commit manual Kafka offset
    	        ack.acknowledge();

    	    } catch (Exception e) {
    	        throw new RuntimeException("Database error processing message, rolling back", e);
    	    }
    	}

}
