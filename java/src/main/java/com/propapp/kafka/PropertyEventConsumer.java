package com.propapp.consumer;

import com.propapp.model.Listing;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Component
public class PropertyEventConsumer {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection (Removed the slow ListingRepository entirely)
    public PropertyEventConsumer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(
        topics = "properties-lifecycle", 
        groupId = "property-service-group-uat",
        containerFactory = "kafkaListenerContainerFactory",
        autoStartup = "${spring.kafka.listener.auto-startup:false}"
    )
    @Transactional
    public void consumeEvent(List<ConsumerRecord<String, Listing>> records, Acknowledgment ack) {
        List<Listing> listingsToUpsert = new ArrayList<>();

        for (ConsumerRecord<String, Listing> record : records) {
            String messageId = record.key(); 
            Listing listing = record.value();

            if (listing == null || messageId == null) {
                continue;
            }

            // 1. Efficient single-row deduplication check
            String deduplicateSql = "INSERT INTO processed_messages (message_id) VALUES (?) ON CONFLICT (message_id) DO NOTHING";
            int rowsAffected = jdbcTemplate.update(deduplicateSql, messageId);

            // If rowsAffected is 0, it's a duplicate message. Skip it!
            if (rowsAffected == 0) {
                continue;
            }

            // 2. Map Kafka message key straight into your entity's primary key if missing
            if (listing.getId() == null || listing.getId().trim().isEmpty()) {
                listing.setId(messageId);
            }

            listingsToUpsert.add(listing);
        }

        // 3. High-Performance Batch Upsert (Bypasses slow JPA save cycles)
        if (!listingsToUpsert.isEmpty()) {
            executeBatchUpsert(listingsToUpsert);
        }

        // 4. Commit collective manual Kafka offset for the entire batch of 50
        ack.acknowledge();
    }

    private void executeBatchUpsert(List<Listing> listings) {
        String sql = "INSERT INTO listings (id, source, address, city, state, zip, price, bedrooms, bathrooms, sqft, latitude, longitude, listed_date, status, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id, source) " +
                     "DO UPDATE SET " +
                     "  address = EXCLUDED.address, " +
                     "  city = EXCLUDED.city, " +
                     "  state = EXCLUDED.state, " +
                     "  zip = EXCLUDED.zip, " +
                     "  price = EXCLUDED.price, " +
                     "  bedrooms = EXCLUDED.bedrooms, " +
                     "  bathrooms = EXCLUDED.bathrooms, " +
                     "  sqft = EXCLUDED.sqft, " +
                     "  latitude = EXCLUDED.latitude, " +
                     "  longitude = EXCLUDED.longitude, " +
                     "  listed_date = EXCLUDED.listed_date, " +
                     "  status = EXCLUDED.status, " +
                     "  description = EXCLUDED.description";

        jdbcTemplate.batchUpdate(sql, listings, listings.size(), (ps, listing) -> {
            ps.setString(1, listing.getId());
            ps.setString(2, listing.getSource());
            ps.setString(3, listing.getAddress());
            ps.setString(4, listing.getCity());
            ps.setString(5, listing.getState());
            ps.setString(6, listing.getZip());
            
            if (listing.getPrice() != null) ps.setDouble(7, listing.getPrice()); else ps.setNull(7, java.sql.Types.DOUBLE);
            if (listing.getBedrooms() != null) ps.setInt(8, listing.getBedrooms()); else ps.setNull(8, java.sql.Types.INTEGER);
            if (listing.getBathrooms() != null) ps.setFloat(9, listing.getBathrooms()); else ps.setNull(9, java.sql.Types.REAL);
            if (listing.getSqft() != null) ps.setInt(10, listing.getSqft()); else ps.setNull(10, java.sql.Types.INTEGER);
            if (listing.getLatitude() != null) ps.setFloat(11, listing.getLatitude()); else ps.setNull(11, java.sql.Types.REAL);
            if (listing.getLongitude() != null) ps.setFloat(12, listing.getLongitude()); else ps.setNull(12, java.sql.Types.REAL);
            
            if (listing.getListedDate() != null) {
                ps.setDate(13, Date.valueOf(listing.getListedDate()));
            } else {
                ps.setNull(13, java.sql.Types.DATE);
            }
            
            ps.setString(14, listing.getStatus());
            ps.setString(15, listing.getDescription());
        });
    }
}
