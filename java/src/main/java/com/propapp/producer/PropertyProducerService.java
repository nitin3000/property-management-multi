package com.propapp.producer;

import org.springframework.stereotype.Service;

@Service
public class PropertyProducerService {
    
    // Add the producer methods your ListingController calls here
    // For example:
    public void sendPropertyEvent(Object event) {
        // Kafka publishing logic will be handled here
          System.out.println("Publishing event: " + eventType + " for listing: " + listing.getId());
    }
}
