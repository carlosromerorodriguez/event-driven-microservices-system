package com.edms.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import com.edms.billingservice.enums.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(BillingRequest billingRequest, StreamObserver<BillingResponse> responseObserver) {
        log.info("CreateBillingAccount request received {}", billingRequest.toString());

        // TODO Future Next Step: Implement Business logic (save to database, perform calculations, ...)

        BillingResponse billingResponse = BillingResponse.newBuilder()
                .setAccountId(billingRequest.getCustomerId())
                .setStatus(Status.ACTIVE.name())
                .build();

        responseObserver.onNext(billingResponse);
        responseObserver.onCompleted();
    }
}
