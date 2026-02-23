package com.edms.customerservice.kafka;

public enum KafkaTopic {
    CUSTOMER("customer");

    final String name;

    KafkaTopic(String name) {
        this.name = name;
    }
}
