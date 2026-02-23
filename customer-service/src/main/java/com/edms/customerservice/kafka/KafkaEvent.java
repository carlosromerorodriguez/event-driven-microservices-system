package com.edms.customerservice.kafka;

public enum KafkaEvent {
    CUSTOMER_CREATED("CUSTOMER_CREATED"),
    CUSTOMER_DELETED("CUSTOMER_DELETED"),
    CUSTOMER_UPDATED("CUSTOMER_UPDATED");

    final String name;

    KafkaEvent(String name) {
        this.name = name;
    }
}
