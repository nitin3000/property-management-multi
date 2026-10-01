package com.propapp.producer;

import org.springframework.stereotype.Service;
import com.propapp.model.Listing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class PropertyProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private static final String TOPIC = "property-events";

    public void publishPropertyEvent(Listing listing, String eventType) {
        String message = createMessage(listing, eventType);
        kafkaTemplate.send(TOPIC, message);
    }

    private String createMessage(Listing listing, String eventType) {
        // Assuming Listing has a toString method or you can customize the message format
        return eventType + ": " + listing.toString();
    }
}