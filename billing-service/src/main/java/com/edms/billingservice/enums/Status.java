package com.edms.billingservice.enums;

public enum Status {
    ACTIVE("ACTIVE"),
    PENDING("PENDING"),
    RETRYING("RETRYING"),
    CANCELLED("CANCELLED");

    final String name;

    Status(String name) {
        this.name = name;
    }
}
