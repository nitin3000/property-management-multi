package com.propapp.producer;

import org.springframework.stereotype.Service;
import com.propapp.model/.Listing;


@Service
public class PropertyProducerService {

    public void publishPropertyEvent(Listing listing, String eventType) {
        // Your autonomous agent will inject the KafkaTemplate logic here later.
        // For now, we provide the stub to pass the compilation gate.
        System.out.println("Publishing event: " + eventType + " for listing: " + listing.getId());
    }

}
