package com.propapp.consumer;

import com.propapp.model.Listing;
import com.propapp.service.ListingIngestionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaPropertyConsumer {

    @Autowired
    private ListingIngestionService ingestionService;

    @KafkaListener(topics = "${spring.kafka.property-topic}", groupId = "property-ingestion-group")
    public void listen(List<ConsumerRecord<String, Listing>> records, Acknowledgment ack) {
        List<Listing> listingsBatch = new ArrayList<>();

        for (ConsumerRecord<String, Listing> record : records) {
            if (record.value() != null) {
                listingsBatch.add(record.value());
            }
        }

        if (!listingsBatch.isEmpty()) {
            try {
                // Batch upsert to database unburdened by Hibernate object tracking
                ingestionService.upsertListingsBatch(listingsBatch);
                
                // Explicitly ACK back to Confluent Cloud once Postgres storage is finalized
                ack.acknowledge();
                
            } catch (Exception e) {
                System.err.println("Database ingestion batch failed. Rolling back offsets for retry: " + e.getMessage());
                throw e; // Throwing triggers Kafka's error handler & prevents silent packet drops
            }
        }
    }
}
