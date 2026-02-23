package com.edms.customerservice.kafka;

import com.edms.customerservice.model.Customer;
import customer.events.CustomerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Customer customer) {
        CustomerEvent customerEvent = CustomerEvent
                .newBuilder()
                .setCustomerId(customer.getId().toString())
                .setName(customer.getName())
                .setEmail(customer.getEmail())
                .setEventType(KafkaEvent.CUSTOMER_CREATED.name)
                .build();

        try {
            kafkaTemplate.send(KafkaTopic.CUSTOMER.name, customerEvent.toByteArray());
            log.info("Event successfully send to Kafka: {}", customerEvent);
        } catch (Exception ex) {
            log.error("Error sending CustomerCreated event: {}", customerEvent);
        }
    }
}
