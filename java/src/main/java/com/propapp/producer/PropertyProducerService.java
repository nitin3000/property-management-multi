package com.propapp.producer;

import org.springframework.stereotype.Service;
import com.propapp.model.Listing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;


@Service
public class PropertyProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.property-topic}")
    private String propertyTopic;
    
    public void publishPropertyEvent(Listing listing, String eventType) {
        kafkaTemplate.send(propertyTopic, eventType, listing);
    }

}
