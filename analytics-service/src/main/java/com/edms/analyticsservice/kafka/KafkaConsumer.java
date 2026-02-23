package com.edms.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import customer.events.CustomerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "customer", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            CustomerEvent customerEvent = CustomerEvent.parseFrom(event);
            log.info("Consumed Customer Event {}", customerEvent.toString());
            // TODO: Perform any business logic related to this event
        } catch (InvalidProtocolBufferException ex) {
            log.error("Error consuming event {}", ex.getMessage());
        }
    }
}
