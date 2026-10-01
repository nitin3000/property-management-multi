package com.propapp.producer;

import org.springframework.stereotype.Service;
import com.propapp.model.Listing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class PropertyProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void publishPropertyEvent(Listing listing, String eventType) {
        kafkaTemplate.send(topic, message);
    }

}
