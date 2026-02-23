package com.edms.customerservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9002}") int serverPort
    ) {
        log.info("Connecting to Billing Service GRPC at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String customerId, String name, String email) {
        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setCustomerId(customerId)
                .setName(name)
                .setEmail(email)
                .build();

        BillingResponse billingResponse = blockingStub.createBillingAccount(billingRequest);
        log.info("Received response from Billing Service GRPC {}", billingResponse.toString());
        return billingResponse;
    }
}
